package ru.javabegin.micro.planner.users.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

// возможные значения, по которым можно искать задачи + значения сортировки
public class UserSearchValues {

    // поля поиска (все типы - объектные, не примитивные. Чтобы можно было передать null)
    private String email;
    private String username;

    // постраничность
    private Integer pageNumber;
    private Integer pageSize;

    // сортировка
    private String sortDirection;
    private String sortColumn;

}
