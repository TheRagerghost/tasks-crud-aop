package com.avragerghost.tasks_crud_aop.dtos;

import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskDTO {
    @NotNull(message = "Поле state не может быть null")
    private TaskState state;

    @NotBlank(message = "Поле title не может быть пустым")
    private String title;
    private String description;

    public static TaskDTO fromTask(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setState(task.getState());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        return dto;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
