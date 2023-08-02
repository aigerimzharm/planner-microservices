package ru.javabegin.micro.planner.todo.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.javabegin.micro.planner.entity.Category;


import java.util.List;

// вы можете уже сразу использовать все методы CRUD (create, read, update, delete)
// принцип ООП: абстракция-реализация, здесь описываем все доступные способы доставки
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // поиск категорий пользователя (по названию)
    List<Category> findByUserIdOrderByTitleAsc(Long userId);

    // поиск значений по названию для конкретного пользователя
    @Query("select c from Category c where " +
            "(:title is null or :title ='' " + // если передадим параметр title пустым, то выберутся все записи
            "or lower(c.title) like lower(concat('%', :title, '%')))" + // если параметр title непустой, то выполняется следующее условие:
            "and c.userId=:userId " + // фильтрация для конкретного пользователя
            "order by c.title asc ") // сортировка по названию
    List<Category> findByTitle(@Param("title") String title, @Param("userId") Long userId);



}
