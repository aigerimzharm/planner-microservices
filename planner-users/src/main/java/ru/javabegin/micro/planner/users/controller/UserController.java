package ru.javabegin.micro.planner.users.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.users.search.UserSearchValues;
import ru.javabegin.micro.planner.users.service.UserService;
import ru.javabegin.micro.planner.utils.webclient.UserWebClientBuilder;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final String ID_COLUMN = "id"; // имя столбца
    public final UserService userService; // сервис для доступа к данным (напрямую к репозиторию не обращается
    public final UserWebClientBuilder userWebClientBuilder;

    public UserController(UserService userService, UserWebClientBuilder userWebClientBuilder) {
        this.userService = userService;
        this.userWebClientBuilder = userWebClientBuilder;
    }

    @PostMapping("/add")

    public ResponseEntity<User> add(@RequestBody User user) {

        // проверка на обязательные параметры
        // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть задвоение
        if (user.getId() != null && user.getId() != 0) {
            return new ResponseEntity("redundant param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
        }

        if (user.getPassword() == null || user.getPassword().trim().length() == 0) {
            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
        }

        if (user.getUsername() == null || user.getUsername().trim().length() == 0) {
            return new ResponseEntity("missed param: username", HttpStatus.NOT_ACCEPTABLE);
        }

        // добавляем пользователя
        user = userService.add(user);

        if (user != null) {
            // заполняем начальные данные пользователя (в параллельном потоке)
            userWebClientBuilder.initUserData(user.getId()).subscribe(result -> {
                        System.out.println("user is populated = " + result);
                    }
            );
        }
        return ResponseEntity.ok(user); // возвращаем созданный объект
    }

    @PutMapping("/update")

    public ResponseEntity<User> update(@RequestBody User user) {

        // проверка на обязательные параметры
        if (user.getId() == null || user.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }
        if (user.getEmail() == null || user.getEmail().trim().length() == 0) {
            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
        }
        if (user.getPassword() == null || user.getPassword().trim().length() == 0) {
            return new ResponseEntity("missed param: password", HttpStatus.NOT_ACCEPTABLE);
        }
        if (user.getUsername() == null || user.getUsername().trim().length() == 0) {
            return new ResponseEntity("missed param: username", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        userService.update(user);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/deletebyid")

    public ResponseEntity deleteByUserId(@RequestBody Long userId) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            userService.deleteBiId(userId);
        }
        catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("userId:" + userId + "not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK);// просто отправляем статус 200 ОК (операция прошла успешно)
    }

    @PostMapping("/deletebyemail")

    public ResponseEntity deleteByUserEmail(@RequestBody String email) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            userService.deleteByUserEmail(email);
        }
        catch (EmptyResultDataAccessException e){
            e.printStackTrace();
            return new ResponseEntity("email:" + email + "not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK);// просто отправляем статус 200 ОК (операция прошла успешно)
    }

    // получение уникального объекта по id
    @PostMapping("/id")

    public ResponseEntity<User> findById(@RequestBody Long id) {

//        User user = null;
//
//        try {
//            user=userService.findById(id);
//        }
//        catch (NoSuchElementException e) {
//            e.printStackTrace();
//            return new ResponseEntity("id:" + id + "not found", HttpStatus.NOT_ACCEPTABLE);
//        }
//        return ResponseEntity.ok(user);

        Optional<User> userOptional = userService.findById(id);

        try {
            if (userOptional.isPresent()) { //если объект найден
                return ResponseEntity.ok(userOptional.get()); // получаем User из контейнера
            }
        }
        catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
        }

        // пользователь с таким id не найден
        return new ResponseEntity("id = " + id + " not found", HttpStatus.NOT_ACCEPTABLE);

    }

    // получение уникального объекта по email
    @PostMapping("/email")

    public ResponseEntity<User> findByEmail(@RequestBody String email) {

        User user = null;

        try {
            user=userService.findByEmail(email);
        }
        catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("email:" + email + "not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(user);
    }

    // поиск по любым параметрам UserSearchValues
    @PostMapping("/search")

    public ResponseEntity<Page<User>> search(@RequestBody UserSearchValues userSearchValues) throws ParseException {

        // все заполненные условия проверяются параметром ИЛИ - это можно изменять в запросе репозитория

        // можно передавать не полный email/username, а любой текст для поиска
        String email = userSearchValues.getEmail() != null ? userSearchValues.getEmail() : null;

        String username = userSearchValues.getUsername() != null ? userSearchValues.getUsername() : null;

        // проверка на обязательные параметры - если они нужны по задаче
//        if (email == null || email.trim().length() == 0) {
//            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
//        }

        Integer pageSize = userSearchValues.getPageSize() != null ? userSearchValues.getPageSize() : null;
        Integer pageNumber = userSearchValues.getPageNumber() != null ? userSearchValues.getPageNumber() : null;

        String sortColumn = userSearchValues.getSortColumn() != null ? userSearchValues.getSortColumn() : null;
        String sortDirection = userSearchValues.getSortDirection() != null ? userSearchValues.getSortDirection() : null;

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
        Page<User> result = userService.findByParams(email, username, pageRequest);

        // результат запроса
        return ResponseEntity.ok(result);

    }

}
