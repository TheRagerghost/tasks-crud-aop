package com.avragerghost.tasks_crud_aop.controllers;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.dtos.mappers.TaskMapper;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.exceptions.TaskNotFoundException;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.services.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    private Task getSampleTask(Long id) {
        Task task = new Task();
        task.setId(id);
        task.setState(TaskState.VISIBLE);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setUserId(1L);
        return task;
    }

    private TaskDTO getSampleTaskDTO() {
        TaskDTO dto = new TaskDTO();
        dto.setState(TaskState.VISIBLE);
        dto.setTitle("Test Task");
        dto.setDescription("Test Description");
        return dto;
    }

    @Test
    @DisplayName("POST /tasks - создание задачи")
    void createTask() throws Exception {
        Task task = getSampleTask(1L);
        TaskDTO dto = getSampleTaskDTO();
        Mockito.when(taskService.createTask(any(TaskDTO.class))).thenReturn(task);
        Mockito.when(taskMapper.toDTO(any(Task.class))).thenReturn(dto);

        String json = "{" +
                "\"state\":\"VISIBLE\"," +
                "\"title\":\"Test Task\"," +
                "\"description\":\"Test Description\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("PUT /tasks/{id} - обновление задачи")
    void updateTask() throws Exception {
        Task task = getSampleTask(1L);
        TaskDTO dto = getSampleTaskDTO();
        Mockito.when(taskService.updateTask(eq(1L), any(TaskDTO.class))).thenReturn(task);
        Mockito.when(taskMapper.toDTO(any(Task.class))).thenReturn(dto);

        String json = "{" +
                "\"state\":\"VISIBLE\"," +
                "\"title\":\"Test Task\"," +
                "\"description\":\"Test Description\"}";

        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - удаление задачи")
    void deleteTask() throws Exception {
        Mockito.doNothing().when(taskService).deleteTask(1L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /tasks/{id} - получение задачи по id")
    void getTaskById() throws Exception {
        Task task = getSampleTask(1L);
        TaskDTO dto = getSampleTaskDTO();
        Mockito.when(taskService.getTaskById(1L)).thenReturn(task);
        Mockito.when(taskMapper.toDTO(task)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /tasks - получение всех задач кроме удаленных")
    void getAllTasksExceptDeleted() throws Exception {
        Task task = getSampleTask(1L);
        TaskDTO dto = getSampleTaskDTO();
        List<Task> tasks = Arrays.asList(task);
        Mockito.when(taskService.getAllTasks(TaskState.DELETED)).thenReturn(tasks);
        Mockito.when(taskMapper.toDTO(task)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /tasks - создание задачи с неверным вводом должно завершиться неудачей")
    void createTask_invalidInput_shouldFail() throws Exception {
        String invalidJson = "{\"title\":\"\"}"; // missing required fields
        mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /tasks/{id} - обновление несуществующей задачи должно завершиться неудачей")
    void updateTask_notFound_shouldFail() throws Exception {
        String json = "{" +
                "\"state\":\"VISIBLE\"," +
                "\"title\":\"Test Task\"," +
                "\"description\":\"Test Description\"}";
        Mockito.when(taskService.updateTask(eq(999L), any(TaskDTO.class)))
                .thenThrow(new TaskNotFoundException("Задача не найдена"));
        mockMvc.perform(MockMvcRequestBuilders.put("/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - удаление несуществующей задачи должно завершиться неудачей")
    void deleteTask_notFound_shouldFail() throws Exception {
        Mockito.doThrow(new TaskNotFoundException("Задача не найдена")).when(taskService).deleteTask(999L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks/{id} - получение несуществующей задачи должно завершиться неудачей")
    void getTaskById_notFound_shouldFail() throws Exception {
        Mockito.when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException("Задача не найдена"));
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks - внутренняя ошибка должна завершиться неудачей")
    void getAllTasks_internalError_shouldFail() throws Exception {
        Mockito.when(taskService.getAllTasks(TaskState.DELETED)).thenThrow(new RuntimeException("Внутренняя ошибка"));
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks"))
                .andExpect(status().isInternalServerError());
    }
}
