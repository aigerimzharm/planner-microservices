package ru.javabegin.micro.planner.todo.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.Task;
import ru.javabegin.micro.planner.todo.search.TaskSearchValues;
import ru.javabegin.micro.planner.todo.service.TaskService;
import ru.javabegin.micro.planner.utils.resttemplate.UserRestBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/task")
public class TaskController {

    private static final String ID_COLUMN = "id"; // имя столбца id
    private final TaskService taskService; // сервис для доступа к данным (напрямую к репозиторию не обращается

    private UserRestBuilder userRestBuilder;

    public TaskController(TaskService taskService, UserRestBuilder userRestBuilder) {
        this.taskService = taskService;
        this.userRestBuilder = userRestBuilder;
    }

    @PostMapping("/all")
    public ResponseEntity<List<Task>> findAll(@RequestBody Long userId) {
        return ResponseEntity.ok(taskService.findAll(userId)); // поиск всех задач конкретного пользователя
    }

    @PostMapping("/add")
    public ResponseEntity<Task> add(@RequestBody Task task) {

        // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть задвоение
        if(task.getId() != null && task.getId() != 0) {
            return new ResponseEntity("redundant param: id", HttpStatus.NOT_ACCEPTABLE);
        }
        if ((task.getTitle() == null || task.getTitle().trim().length() == 0)) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }
        // если такой пользователь существует
        if (userRestBuilder.userExists(task.getUserId())) { // вызываем микросервисы
            return ResponseEntity.ok(taskService.add(task));// возвращаем добавленный объект с заполненным ID
        }
        // если пользователя не существует
        return new ResponseEntity("user id " + task.getUserId() + " not found", HttpStatus.NOT_ACCEPTABLE);
    }

    @PutMapping("/update")
    public ResponseEntity<Task> update(@RequestBody Task task) {
        if(task.getId() == null || task.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }
        if (task.getTitle() == null || task.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }
        taskService.update(task);
        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 ОК (операция прошла успешно)
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Task> delete(@PathVariable("id") Long id) {
        try {
            taskService.deleteById(id);
        }
        catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id " + id + "is not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 ОК (операция прошла успешно)
    }

    @PostMapping("/id")
    public ResponseEntity<Task> findById(@RequestBody Long id) {

        Task task = null;
        try {
            task = taskService.findById(id);
        }
        catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("id: " + id + "is not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(task);
    }

    // поиск по любым параметрам TaskSearchValues
    @PostMapping("/search")
    public ResponseEntity<Page<Task>> search(@RequestBody TaskSearchValues taskSearchValues) throws ParseException {

        // исключить NullPointerException
        String title = taskSearchValues.getTitle() != null ? taskSearchValues.getTitle() : null;

        // конвертируем Boolean в Integer
        Boolean completed = taskSearchValues.getCompleted() != null && taskSearchValues.getCompleted() == 1 ? true : false;

        Long priorityId = taskSearchValues.getPriorityId() != null ? taskSearchValues.getPriorityId() : null;
        Long categoryId = taskSearchValues.getCategoryId() != null ? taskSearchValues.getCategoryId() : null;

        String sortColumn = taskSearchValues.getSortColumn() != null ? taskSearchValues.getSortColumn() : null;
        String sortDirection = taskSearchValues.getSortDirection() != null ? taskSearchValues.getSortDirection() : null;

        Integer pageNumber = taskSearchValues.getPageNumber() != null ? taskSearchValues.getPageNumber() : null;
        Integer pageSize = taskSearchValues.getPageSize() != null ? taskSearchValues.getPageSize() : null;

        Long userId = taskSearchValues.getUserId() != null ? taskSearchValues.getUserId() : null;

        // проверка на обязательные параметры


        if (userId == null || userId == 0) {
            return new ResponseEntity("missed param: user id", HttpStatus.NOT_ACCEPTABLE);
        }

        // чтобы захватить в выборке все задачи по датам независимо от времени - можно выставить время с 00:00 до 23:59
        Date dateFrom = null;
        Date dateTo = null;

        // выставить 00:01 для начальной даты (если она указана)
        if (taskSearchValues.getDateFrom() != null) {
            Calendar calendarFrom = Calendar.getInstance();
            calendarFrom.setTime(taskSearchValues.getDateFrom());
            calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
            calendarFrom.set(Calendar.MINUTE, 0);
            calendarFrom.set(Calendar.SECOND, 0);
            calendarFrom.set(Calendar.MILLISECOND, 1);

            dateFrom = calendarFrom.getTime();
        }

        // выставить 23:59 для конечной даты (если она указана)
        if (taskSearchValues.getDateTo() != null) {
            Calendar calendarTo = Calendar.getInstance();
            calendarTo.setTime(taskSearchValues.getDateTo());
            calendarTo.set(Calendar.HOUR_OF_DAY, 23);
            calendarTo.set(Calendar.MINUTE, 59);
            calendarTo.set(Calendar.SECOND, 59);
            calendarTo.set(Calendar.MILLISECOND, 999);

            dateTo = calendarTo.getTime();
        }

        // направление сортировки
        Sort.Direction direction = sortDirection == null || sortDirection.trim().length() == 0 || sortDirection.trim().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок
        Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
        Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к не указано второе поле сортировки
        Поэтому и используем ID - Тогда все записи с одинаковым значением приоритета будут следовать в одном порядке
         */
        // объект сортировки
        Sort sort = Sort.by(direction, sortColumn, ID_COLUMN);

        // объект постраничности
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        // результат запроса с постраничным выводом, // важен тот же порядок, что и в классе-контейнере taskSearchValues и taskService
        Page<Task> result = taskService.findByParams(title, completed, priorityId, categoryId, userId, dateFrom, dateTo, pageRequest);

        // результат запроса
        return ResponseEntity.ok(result);


    }

}
