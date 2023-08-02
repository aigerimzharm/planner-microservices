package ru.javabegin.micro.planner.todo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.javabegin.micro.planner.entity.Priority;

import java.util.List;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {

    List<Priority> findByUserIdOrderByTitleAsc (Long id);

    @Query ("select p from Priority p where " +
    "(:title is null or :title = '' " + "or lower(p.title) like lower(concat('%', :title, '%')))" +
            "and p.userId = :id " +
            "order by p.title asc ")

    List<Priority> findByTitle(@Param("title") String title, @Param("id") Long id);

}
