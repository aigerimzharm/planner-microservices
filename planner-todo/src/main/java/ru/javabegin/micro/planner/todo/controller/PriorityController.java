package ru.javabegin.micro.planner.todo.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.Priority;
import ru.javabegin.micro.planner.todo.search.PrioritySearchValues;
import ru.javabegin.micro.planner.todo.service.PriorityService;
import ru.javabegin.micro.planner.utils.resttemplate.UserRestBuilder;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/priority")

public class PriorityController {

    private PriorityService priorityService;

    private UserRestBuilder userRestBuilder;

    public PriorityController(PriorityService priorityService, UserRestBuilder userRestBuilder) {
        this.priorityService = priorityService;
        this.userRestBuilder = userRestBuilder;
    }

    @PostMapping("/all")
    public List<Priority> findAll (@RequestBody Long userId) {
        return priorityService.findAll(userId);
    }

    @PostMapping("/add")
    public ResponseEntity<Priority> add (@RequestBody Priority priority) {

        if(priority.getId() != null && priority.getId() != 0) {
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }
        if(priority.getTitle() == null || priority.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }
        if(priority.getColor() == null || priority.getColor().trim().length() == 0) {
            return new ResponseEntity("missed param: color MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если такой пользователь существует
        if (userRestBuilder.userExists(priority.getUserId())) { // вызываем микросервисы
            return ResponseEntity.ok(priorityService.add(priority));// возвращаем добавленный объект с заполненным ID
        }

        // если пользователя не существует
        return new ResponseEntity("user id " + priority.getUserId() + " not found", HttpStatus.NOT_ACCEPTABLE);
    }

    @PutMapping("/update")
    public ResponseEntity<Priority> update(@RequestBody Priority priority) {

        if(priority.getId() == null || priority.getId() == 0) {
            return new ResponseEntity("missed param: id MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }
        if(priority.getTitle() == null || priority.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }
        if(priority.getColor() == null || priority.getColor().trim().length() == 0) {
            return new ResponseEntity("missed param: color MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }
        priorityService.update(priority);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {

        try {
            priorityService.deleteById(id);
        }
        catch (EmptyResultDataAccessException e ) {
            e.printStackTrace();
            return new ResponseEntity("id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Priority>> search(@RequestBody PrioritySearchValues prioritySearchValues) {

        if(prioritySearchValues.getUserId() == null || prioritySearchValues.getUserId() == 0) {
            return new ResponseEntity("missed param: user id MUST be not null", HttpStatus.NOT_ACCEPTABLE);
        }
        List<Priority> list = priorityService.findByTitle(prioritySearchValues.getTitle(), prioritySearchValues.getUserId());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/id")
    public ResponseEntity<Priority> findById(@RequestBody Long id) {

        Priority priority = null;

        try{
            priority = priorityService.findById(id);
        }
        catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("id: " + id + " is not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(priority);
    }

}