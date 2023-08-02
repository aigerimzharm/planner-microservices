package ru.javabegin.micro.planner.users.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.users.repo.UserRepository;

import javax.transaction.Transactional;
import java.util.Optional;

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что методов мало и это все можно сразу реализовать в контроллере
// такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями
@Service
// все методы должны выполняться без ошибки, чтобы транзакция завершилась
// если в методе выполняется несколько SQL запросов и возникнет исключение, то все выполненные операции откатятся (rollback)
@Transactional

public class UserService {

    private final UserRepository repository; // сервис имеет право обращаться к репозиторию

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User add(User user) {
        return repository.save(user); // метод save обновляет или создает объект, если его не было
    }

    public  User update(User user) {
        return repository.save(user); // метод save обновляет или создает объект, если его не было
    }

    public void deleteBiId(Long id) {
        repository.deleteById(id);
    }
    public void deleteByUserEmail(String email) {
        repository.deleteByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id); // т.к возвращается Optional - можно получить объект методом get()
    }

    public Page<User> findByParams(String email, String username, PageRequest paging) {
        return repository.findByParams(email, username, paging);
    }

}
