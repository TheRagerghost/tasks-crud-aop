package com.avragerghost.tasks_crud_aop.dtos.mappers;

import org.springframework.stereotype.Component;

import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;
import com.avragerghost.tasks_crud_aop.models.Task;

@Component
public class TaskStateMapper {
    public TaskStateDTO toDto(Task task) {
        TaskStateDTO taskStateDTO = new TaskStateDTO();
        taskStateDTO.setTaskId(task.getId());
        taskStateDTO.setState(task.getState());
        return taskStateDTO;
    }

    /* public Task toEntity(TaskStateDTO taskStateDTO) {

    } */
}
