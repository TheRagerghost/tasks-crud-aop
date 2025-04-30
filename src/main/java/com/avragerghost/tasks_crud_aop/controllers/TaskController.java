package com.avragerghost.tasks_crud_aop.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.dtos.mappers.TaskMapper;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.services.TaskService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping
    public TaskDTO createTask(@Valid @RequestBody TaskDTO dto) {
        Task task = taskService.createTask(dto);

        return taskMapper.toDTO(task);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) {
        Task task = taskService.updateTask(id, dto);

        return taskMapper.toDTO(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return;
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);

        return taskMapper.toDTO(task);
    }

    @GetMapping
    public List<TaskDTO> getAllTasksExceptDeleted() {
        List<Task> tasks = taskService.getAllTasks(TaskState.DELETED);
        List<TaskDTO> taskDTOs = tasks.stream().map(taskMapper::toDTO).collect(Collectors.toList());

        return taskDTOs;
    }
}
