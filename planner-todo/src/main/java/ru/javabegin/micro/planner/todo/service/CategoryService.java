package ru.javabegin.micro.planner.todo.service;

import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.Category;
import ru.javabegin.micro.planner.todo.repo.CategoryRepository;

import javax.transaction.Transactional;
import java.util.List;

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что методов мало и это все можно сразу реализовать в контроллере
// такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями
@Service

// все методы должны выполняться без ошибки, чтобы транзакция завершилась
// если в методе выполняется несколько SQL запросов и возникнет исключение, то все выполненные операции откатятся (rollback)

@Transactional
public class CategoryService {

    // работает встроенный механизм DI из Spring, который при старте приложения подставит в эту переменную нужный класс-реализацию
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

//    public Category findById(Long id) {
//        return repository.findById(id).get();
//    }

    public List<Category> findAll(Long userId) {
        return repository.findByUserIdOrderByTitleAsc(userId);
    }

    public Category add(Category category) {
        return repository.save(category); // метод save обновляет или создает объект, если его не было
    }

    public Category update(Category category) {
        return repository.save(category); // метод save обновляет или создает объект, если его не было
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<Category> findByTitle(String title, Long userId) {
        return repository.findByTitle(title, userId);
    }

    public Category findById(Long id) {
        return repository.findById(id).get(); // т.к возвращается Optional - можно получить объект методом get()
    }
}
