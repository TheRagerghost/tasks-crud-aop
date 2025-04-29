package com.avragerghost.tasks_crud_aop.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.enums.UserRole;
import com.avragerghost.tasks_crud_aop.models.Task;
import com.avragerghost.tasks_crud_aop.models.User;
import com.avragerghost.tasks_crud_aop.repositories.TaskRepository;
import com.avragerghost.tasks_crud_aop.repositories.UserRepository;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private UserRepository userRepo;

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
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    /**
     * Метод для получения списка задач.
     * 
     * @param state ({@link TaskState}) состояние для исключения из списка
     * @return Список задач ({@link Task}) без выбранного состояния
     */
    public List<Task> getAllTasks(TaskState state) {
        return taskRepo.findAllByStateNot(state);
    }

    /**
     * Метод-заглушка. Всегда возвращает гостя.
     * 
     * @return пользователь с ролью {@code GUEST}
     */
    public User getCurrentUser() {
        Long userId = 1L;
        return userRepo.findById(userId).orElseGet(() -> {
            User guest = new User();
            guest.setRole(UserRole.GUEST);
            guest.setName("Guest " + userId);
            return userRepo.save(guest);
        });
    }

    /**
     * Метод для создания задачи.
     * 
     * @param dto ({@link TaskDTO})
     * @return созданная задача ({@link Task})
     */
    public Task createTask(TaskDTO dto) {
        User user = getCurrentUser();
        Task task = new Task();
        task.setState(dto.getState());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setUser(user);
        return taskRepo.save(task);
    }

    /**
     * Метод для обновления содержимого задачи.
     * 
     * @param id  : id задачи для изменения
     * @param dto ({@link TaskDTO})
     * @return измененная задача ({@link Task})
     */
    public Task updateTask(Long id, TaskDTO dto) {
        Task task = getTaskById(id);
        task.setState(dto.getState());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        return taskRepo.save(task);
    }

    /**
     * Метод для удаления задачи.
     * <p>
     * Подразумевает наличие прав доступа.
     * 
     * @param id : id задачи для удаления
     */
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepo.delete(task);
    }
}
