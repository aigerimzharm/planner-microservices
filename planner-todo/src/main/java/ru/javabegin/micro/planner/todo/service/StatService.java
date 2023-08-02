package ru.javabegin.micro.planner.todo.service;

import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.Stat;
import ru.javabegin.micro.planner.todo.repo.StatRepository;

import javax.transaction.Transactional;

@Service
@Transactional

public class StatService {

    private final StatRepository repository; // сервис имеет право обращаться к репозиторию

    public StatService(StatRepository repository) {
        this.repository = repository;
    }

    public Stat findStat (Long userId) {
        return repository.findByUserId(userId);
    }
}
