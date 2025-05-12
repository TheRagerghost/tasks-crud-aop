package com.avragerghost.tasks_crud_aop.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.avragerghost.tasks_crud_aop.aspects.annotations.ForbidForPublicAPI;
import com.avragerghost.tasks_crud_aop.aspects.annotations.LogExecTime;
import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;
import com.avragerghost.tasks_crud_aop.dtos.mappers.TaskStateMapper;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.kafka.KafkaTaskUpdStateProducer;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.repositories.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepo;
    private final KafkaTaskUpdStateProducer kTaskUpdStateProd;

    /**
     * Метод для получения задачи по {@code id}.
     * 
     * @param id
     * @return {@link Task}
     */
    public Task getTaskById(Long id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
    }

    /**
     * Метод для получения списка задач.
     * 
     * @return Список всех задач ({@link Task})
     */
    @LogExecTime
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    /**
     * Метод для получения списка задач.
     * 
     * @param state ({@link TaskState}) состояние для исключения из списка
     * @return Список задач ({@link Task}) без выбранного состояния
     */
    @LogExecTime
    public List<Task> getAllTasks(TaskState state) {
        return taskRepo.findAllByStateNot(state);
    }

    /**
     * Метод-заглушка. Всегда возвращает гостя.
     * 
     * @return пользователь с {@code id 1}
     */
    public Long getCurrentUserId() {
        return 1L;
    }

    /**
     * Метод для создания задачи.
     * 
     * @param dto ({@link TaskDTO})
     * @return созданная задача ({@link Task})
     */
    @LogExecTime
    public Task createTask(TaskDTO dto) {
        Task task = new Task();
        task.setState(dto.getState());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setUserId(getCurrentUserId());
        return taskRepo.save(task);
    }

    /**
     * Метод для обновления содержимого задачи.
     * 
     * @param id  : id задачи для изменения
     * @param dto ({@link TaskDTO})
     * @return измененная задача ({@link Task})
     */
    @LogExecTime
    public Task updateTask(Long id, TaskDTO dto) {
        Task task = getTaskById(id);
        // task.setState(dto.getState());
        taskSwitchStateIfNeeded(dto.getState(), task);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        return taskRepo.save(task);
    }

    private void taskSwitchStateIfNeeded(TaskState newState, Task task) {
        TaskState currentState = task.getState();
        log.info("Change state try: {} => {}", currentState, newState);
        if (currentState.toString() != newState.toString()) {
            log.info("Changing state: {} => {}", currentState, newState);
            task.setState(newState);
            TaskStateDTO taskStateDTO = new TaskStateMapper().toDto(task);
            kTaskUpdStateProd.send(taskStateDTO);
        }
    }

    /**
     * Метод для удаления задачи.
     * <p>
     * Подразумевает наличие прав доступа.
     * 
     * @param id : id задачи для удаления
     */
    @ForbidForPublicAPI
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepo.delete(task);
    }
}
