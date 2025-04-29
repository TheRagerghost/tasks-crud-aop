package com.avragerghost.tasks_crud_aop.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.services.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO dto) {
        Task task = taskService.createTask(dto);

        return ResponseEntity.ok(TaskDTO.fromTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) {
        Task task = taskService.updateTask(id, dto);

        return ResponseEntity.ok(TaskDTO.fromTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);

        return ResponseEntity.ok(TaskDTO.fromTask(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasksExceptDeleted() {
        List<Task> tasks = taskService.getAllTasks(TaskState.DELETED);
        List<TaskDTO> taskDTOs = tasks.stream().map(TaskDTO::fromTask).collect(Collectors.toList());

        return ResponseEntity.ok(taskDTOs);
    }
}
