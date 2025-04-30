package com.avragerghost.tasks_crud_aop.dtos;

import com.avragerghost.tasks_crud_aop.enums.TaskState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
    @NotNull
    private TaskState state;

    @NotBlank
    private String title;

    @NotBlank
    private String description;
}
