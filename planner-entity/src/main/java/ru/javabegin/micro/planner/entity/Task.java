package ru.javabegin.micro.planner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "task", schema = "todo", catalog = "planner_todo")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class Task {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String title;
    
    @Type(type = "org.hibernate.type.NumericBooleanType") // для автоматической конвертации числа в true/false
    private Boolean completed; // 1 = true, 0 = false
    
    @Column(name = "task_date") // в БД поле называется task_date, т.к нельзя использовать системное слово date
    private Date taskDate;

    // задача может иметь только один приоритет (с обратной стороны один и тот же приоритет может использоваться несколькими задачами)
    @ManyToOne // (fetch = FetchType.EAGER) можно указать, но не обязательно, т.к это дефолтное значение
    @JoinColumn(name = "priority_id", referencedColumnName = "id") // по каким полям связывать "foreign key"
    private Priority priority;

    // задача может иметь только одну категорию (с обратной стороны одна и та же категория может использоваться несколькими задачами)
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id") // по каким полям связывать "foreign key"
    private Category category;

//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    @ManyToOne (fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id") // по каким полям связывать "foreign key"
//    private User user;

    @Column(name="user_id")
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return title;
    }
}
