package ru.javabegin.micro.planner.todo.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
// возможные значения, по которым можно искать категории
public class CategorySearchValues {

    private String title; // такое же название должно быть у объекта на frontend
    private Long userId; // для фильтрации значений конкретного пользователя

}
