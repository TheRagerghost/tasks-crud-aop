package com.avragerghost.tasks_crud_aop.services;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.dtos.mappers.TaskStateMapper;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.exceptions.TaskNotFoundException;
import com.avragerghost.tasks_crud_aop.kafka.KafkaTaskUpdStateProducer;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
public class TaskServiceModuleTest {
    @Mock
    private TaskRepository taskRepo;
    @Mock
    private KafkaTaskUpdStateProducer kTaskUpdStateProd;
    @Mock
    private TaskStateMapper taskStateMapper;
    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private TaskDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setState(TaskState.VISIBLE);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setUserId(1L);
        sampleDTO = new TaskDTO();
        sampleDTO.setState(TaskState.VISIBLE);
        sampleDTO.setTitle("Test Task");
        sampleDTO.setDescription("Test Description");
    }

    @Test
    @DisplayName("getTaskById должен возвращать задачу, если она найдена")
    void getTaskById_success() {
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.of(sampleTask));
        Task result = taskService.getTaskById(1L);
        assertEquals(sampleTask, result);
    }

    @Test
    @DisplayName("getTaskById должен создавать исключение, если задача не найдена")
    void getTaskById_notFound() {
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    @DisplayName("getAllTasks должен возвращать все задачи")
    void getAllTasks_success() {
        List<Task> tasks = Arrays.asList(sampleTask);
        Mockito.when(taskRepo.findAll()).thenReturn(tasks);
        List<Task> result = taskService.getAllTasks();
        assertEquals(tasks, result);
    }

    @Test
    @DisplayName("getAllTasks(state) должен возвращать отфильтрованные задачи")
    void getAllTasks_state_success() {
        List<Task> tasks = Arrays.asList(sampleTask);
        Mockito.when(taskRepo.findAllByStateNot(TaskState.DELETED)).thenReturn(tasks);
        List<Task> result = taskService.getAllTasks(TaskState.DELETED);
        assertEquals(tasks, result);
    }

    @Test
    @DisplayName("createTask должен сохранять и возвращать новую задачу")
    void createTask_success() {
        Mockito.when(taskRepo.save(any(Task.class))).thenReturn(sampleTask);
        Task result = taskService.createTask(sampleDTO);
        assertEquals(sampleTask.getTitle(), result.getTitle());
        assertEquals(sampleTask.getDescription(), result.getDescription());
        assertEquals(sampleTask.getState(), result.getState());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("updateTask должен обновлять и возвращать задачу, если состояние изменилось. Без кафки")
    void updateTask_success_stateChanged() {
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setState(TaskState.HIDDEN);
        updatedTask.setTitle("Test Task");
        updatedTask.setDescription("Test Description");
        updatedTask.setUserId(1L);
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.of(sampleTask));
        Mockito.when(taskRepo.save(any(Task.class))).thenReturn(updatedTask);
        TaskDTO dto = new TaskDTO();
        dto.setState(TaskState.HIDDEN);
        dto.setTitle("Test Task");
        dto.setDescription("Test Description");
        try (MockedStatic<TransactionSynchronizationManager> mocked = Mockito
                .mockStatic(TransactionSynchronizationManager.class)) {
            mocked.when(() -> TransactionSynchronizationManager.registerSynchronization(any()))
                    .thenAnswer(invocation -> null);
            Task result = taskService.updateTask(1L, dto);
            assertEquals(TaskState.HIDDEN, result.getState());
        }
    }

    @Test
    @DisplayName("updateTask должен создавать исключение, если задача не найдена")
    void updateTask_notFound() {
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, sampleDTO));
    }

    @Test
    @DisplayName("deleteTask должен удалять задачу, если она найдена")
    void deleteTask_success() {
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.of(sampleTask));
        Mockito.doNothing().when(taskRepo).delete(sampleTask);
        assertDoesNotThrow(() -> taskService.deleteTask(1L));
    }

    @Test
    @DisplayName("deleteTask должен создавать исключение, если задача не найдена")
    void deleteTask_notFound() {
        Mockito.when(taskRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
    }
}
