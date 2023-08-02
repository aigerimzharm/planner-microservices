package ru.javabegin.micro.planner.todo.service;

import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.Priority;
import ru.javabegin.micro.planner.todo.repo.PriorityRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional

public class PriorityService {

    private final PriorityRepository repository;


    public PriorityService(PriorityRepository repository) {
        this.repository = repository;
    }

    public List<Priority> findAll(Long userId) {
        return repository.findByUserIdOrderByTitleAsc(userId);
    }

    public Priority add(Priority priority) {
        return repository.save(priority);
    }

    public Priority update(Priority priority) {
        return repository.save(priority);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<Priority> findByTitle(String title, Long userId) {
        return repository.findByTitle(title, userId);
    }

    public Priority findById(Long id) {
        return repository.findById(id).get();
    }

}
