package com.avragerghost.tasks_crud_aop.dtos;

import com.avragerghost.tasks_crud_aop.enums.TaskState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStateDTO {

    @PositiveOrZero
    private Long taskId;

    @NotBlank
    private TaskState state;
}
