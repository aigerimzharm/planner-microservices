package ru.javabegin.micro.planner.todo.controller;


import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.Category;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.todo.feign.UserFeignClient;
import ru.javabegin.micro.planner.todo.search.CategorySearchValues;
import ru.javabegin.micro.planner.todo.service.CategoryService;
import ru.javabegin.micro.planner.utils.resttemplate.UserRestBuilder;
import ru.javabegin.micro.planner.utils.webclient.UserWebClientBuilder;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/category") // базовый URI
public class CategoryController {

    // доступ к данным из БД
    private CategoryService categoryService;

    // микросервисы для работы с пользователями
    private UserRestBuilder userRestBuilder;

    // клиент для вызова микросервисов
    private UserWebClientBuilder userWebClientBuilder;

    private UserFeignClient userFeignClient;

    // автоматическое внедрение экземпляра класса через конструктор
    // не используем @Autowired для переменной класса, т.к "Field injection is not recommended"

    public CategoryController(CategoryService categoryService, UserRestBuilder userRestBuilder, UserWebClientBuilder userWebClientBuilder, UserFeignClient userFeignClient) {
        this.categoryService = categoryService;
        this.userRestBuilder = userRestBuilder;
        this.userWebClientBuilder = userWebClientBuilder;
        this.userFeignClient = userFeignClient;
    }

//    @GetMapping("/id")
//    public Category findById() {
//        return categoryService.findById(121452L);
//    }

    @PostMapping("/all") // в этом методе уже нет необходимости, т.к есть метод search
    public List<Category> findAll(@RequestBody Long userId) {
        return categoryService.findAll(userId);
    }

    @PostMapping("/add")
    public ResponseEntity<Category> add(@RequestBody Category category) {

        // проверка на обязательные параметры (можно выстроить и на уровне БД, но лучше сразу на уровне backend, чтобы лишние запросы не попадали в БД
        if (category.getId() != null && category.getId() != 0) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если такой пользователь существует
//        if (userRestBuilder.userExists(category.getUserId())) { // вызываем микросервисы
//            return ResponseEntity.ok(categoryService.add(category));// возвращаем добавленный объект с заполненным ID
//        }

        // если такой пользователь существует
//        if (userWebClientBuilder.userExists(category.getUserId())) { // вызываем микросерсвисы
//            return ResponseEntity.ok(categoryService.add(category)); // возвращаем добавленный объект с заполненным ID
//        }

        // подписываемся на резудьтат
//        userWebClientBuilder.userExistAsync(category.getUserId()).subscribe(user -> System.out.println("user = " + user));

        // вызов мс через feign интерфейс
        if (userFeignClient.findUserById(category.getUserId()) != null) {
            return ResponseEntity.ok(categoryService.add(category));
        }

        // вызов мс через feign интерфейс + cirquitbreakers
//        ResponseEntity<User> result = userFeignClient.findUserById(category.getUserId());
//
//        if (result == null) { // если мс недоступен, вернется null
//            return new ResponseEntity("система пользователей недоступна", HttpStatus.NOT_FOUND);
//        }
//        if (result.getBody() != null) { // если пользователь не пустой
//            return ResponseEntity.ok(categoryService.add(category));
//        }

        // если пользователя НЕ существует
        return new ResponseEntity("user id " + category.getUserId() + " not found", HttpStatus.NOT_ACCEPTABLE);
    }

    @PutMapping("/update")
    public ResponseEntity<Category> update(@RequestBody Category category) {

        // проверка на обязательные параметры
        if (category.getId() == null || category.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }
        // если передали пустое значение title
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        categoryService.update(category);

        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            categoryService.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 без объекта(операция прошла успешно)
    }

    @PostMapping("/search")
    public ResponseEntity<List<Category>> search(@RequestBody CategorySearchValues categorySearchValues) {

        // проверка на обязательные параметры
        if(categorySearchValues.getUserId() == null || categorySearchValues.getUserId() == 0) {
            return new ResponseEntity("missed param: user id", HttpStatus.NOT_ACCEPTABLE);
        }
        // поиск категорий пользователя по названию
        // в requestBody передаем json, из json создаем объекты, и из объекта получаем два поля для поиска
        List<Category> list = categoryService.findByTitle(categorySearchValues.getTitle(), categorySearchValues.getUserId());

        return ResponseEntity.ok(list);
    }

    @PostMapping("/id")
    public ResponseEntity<Category> findById(@RequestBody Long id) {

        Category category = null;

        try{
            category=categoryService.findById(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("id= " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(category);
    }

}
