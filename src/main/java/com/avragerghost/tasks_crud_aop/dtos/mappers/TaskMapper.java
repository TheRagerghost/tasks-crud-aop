package com.avragerghost.tasks_crud_aop.dtos.mappers;

import org.springframework.stereotype.Component;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.models.Task;

@Component
public class TaskMapper {
    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setState(task.getState());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        return dto;
    }

    public Task toEntity(TaskDTO dto, Task task) {
        task.setState(dto.getState());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        return task;
    }
}
