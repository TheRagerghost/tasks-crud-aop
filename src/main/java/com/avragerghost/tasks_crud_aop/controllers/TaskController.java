package com.avragerghost.tasks_crud_aop.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avragerghost.request_logger_aop.aspects.annotations.LoggableController;
import com.avragerghost.request_logger_aop.aspects.annotations.LoggableControllerMethod;
import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.dtos.mappers.TaskMapper;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.services.TaskService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@LoggableController
@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO dto) {
        Task task = taskService.createTask(dto);

        return ResponseEntity.ok(taskMapper.toDTO(task));
    }

    @PutMapping("/{id}")
    @LoggableControllerMethod
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) {
        Task task = taskService.updateTask(id, dto);

        return ResponseEntity.ok(taskMapper.toDTO(task));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);

        return ResponseEntity.ok(taskMapper.toDTO(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasksExceptDeleted() {
        List<Task> tasks = taskService.getAllTasks(TaskState.DELETED);
        List<TaskDTO> taskDTOs = tasks.stream().map(taskMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(taskDTOs);
    }
}
