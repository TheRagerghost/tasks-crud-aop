package com.avragerghost.tasks_crud_aop.services;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.exceptions.TaskNotFoundException;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TaskServiceIntegrationTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRepository taskRepo;

    private TaskDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new TaskDTO();
        sampleDTO.setState(TaskState.VISIBLE);
        sampleDTO.setTitle("Integration Task");
        sampleDTO.setDescription("Integration Description");
    }

    @Test
    @DisplayName("createTask должен сохранять и возвращать новую задачу")
    void createTask_success() {
        Task task = taskService.createTask(sampleDTO);
        assertNotNull(task.getId());
        assertEquals("Integration Task", task.getTitle());
        assertEquals("Integration Description", task.getDescription());
        assertEquals(TaskState.VISIBLE, task.getState());
        assertEquals(1L, task.getUserId());
        assertTrue(taskRepo.findById(task.getId()).isPresent());
    }

    @Test
    @DisplayName("getTaskById должен возвращать задачу, если она найдена")
    void getTaskById_success() {
        Task created = taskService.createTask(sampleDTO);
        Task found = taskService.getTaskById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("getTaskById должен создавать исключение, если задача не найдена")
    void getTaskById_notFound() {
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(9999L));
    }

    @Test
    @DisplayName("getAllTasks должен возвращать все задачи")
    void getAllTasks_success() {
        taskService.createTask(sampleDTO);
        List<Task> tasks = taskService.getAllTasks();
        assertFalse(tasks.isEmpty());
    }

    @Test
    @DisplayName("getAllTasks(state) должен возвращать отфильтрованные задачи")
    void getAllTasks_state_success() {
        TaskDTO deletedDTO = new TaskDTO();
        deletedDTO.setState(TaskState.DELETED);
        deletedDTO.setTitle("Deleted Task");
        deletedDTO.setDescription("Deleted Desc");
        taskService.createTask(sampleDTO);
        taskService.createTask(deletedDTO);
        List<Task> tasks = taskService.getAllTasks(TaskState.DELETED);
        assertTrue(tasks.stream().noneMatch(t -> t.getState() == TaskState.DELETED));
    }

    @Test
    @DisplayName("updateTask должен обновлять и возвращать задачу")
    void updateTask_success() {
        Task created = taskService.createTask(sampleDTO);
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setState(TaskState.HIDDEN);
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        Task updated = taskService.updateTask(created.getId(), updateDTO);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(TaskState.HIDDEN, updated.getState());
    }

    @Test
    @DisplayName("updateTask должен создавать исключение, если задача не найдена")
    void updateTask_notFound() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setState(TaskState.HIDDEN);
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(9999L, updateDTO));
    }

    @Test
    @DisplayName("deleteTask должен возвращать 403, т.к. это публичный API (соответствующая аннотация запрещает удаление)")
    void deleteTask_forbidden() {
        Task created = taskService.createTask(sampleDTO);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.deleteTask(created.getId()));
        assertEquals(403, ex.getStatusCode().value());
    }
}
