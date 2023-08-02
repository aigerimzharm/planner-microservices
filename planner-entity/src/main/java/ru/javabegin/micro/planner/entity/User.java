package ru.javabegin.micro.planner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*

пользователь - основной объект, с которым связаны все остальные (через внешние ключи)

 */

@Entity
@Table(name = "user_data", schema = "users", catalog = "planner_users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;

    @Column(name = "userpassword")
    private String password;

    // если нам не нужна таблица UserRole и ее данные - мы можем просто сразу получить все Role пользователя
    // (fetch = FetchType.EAGER) можно указать, но не обязательно, т.к это дефолтное значение для @OneToOne и @OneToMany
    // таблица role ссылается на user через промежуточную таблицу user_role

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToMany(mappedBy = "users")
    private Set<Role> roles=new HashSet<>();




//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Task> tasks;
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Category> categories;
//
//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private List<Priority> priorities;
//
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = false)
//    public Activity activity; // активность пользователя (активация и любые другие)
//
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = false)
//    public Stat stat; // общая статистика пользователя по всем задачам

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return username;
    }
}
