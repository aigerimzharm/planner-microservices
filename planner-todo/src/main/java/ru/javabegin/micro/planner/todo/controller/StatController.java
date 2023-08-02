package ru.javabegin.micro.planner.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.micro.planner.entity.Stat;
import ru.javabegin.micro.planner.todo.service.StatService;

@RestController

public class StatController {

    private StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @PostMapping("/stat")
    public ResponseEntity<Stat> findByEmail(@RequestBody Long userId) {

        // можно не использовать ResponseEntity, а просто вернуть коллекцию, код все равно будет 200 OK
        return ResponseEntity.ok(statService.findStat(userId));
    }

}
