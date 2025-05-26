package com.avragerghost.tasks_crud_aop.repositories;

import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = com.avragerghost.tasks_crud_aop.TasksCrudAopApplication.class)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("findAllByStateNot должен возвращать только не удаленные задачи")
    void testFindAllByStateNot() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Desc 1");
        task1.setUserId(1L);
        task1.setState(TaskState.VISIBLE);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Desc 2");
        task2.setUserId(2L);
        task2.setState(TaskState.HIDDEN);

        Task task3 = new Task();
        task3.setTitle("Task 3");
        task3.setDescription("Desc 3");
        task3.setUserId(3L);
        task3.setState(TaskState.DELETED);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> nonDeletedTasks = taskRepository.findAllByStateNot(TaskState.DELETED);
        assertThat(nonDeletedTasks)
                .hasSize(2)
                .allMatch(t -> t.getState() != TaskState.DELETED);
    }

    @Test
    @DisplayName("Базовые CRUD: сохранение и поиск по id")
    void testSaveAndFindById() {
        Task task = new Task();
        task.setTitle("CRUD Task");
        task.setDescription("CRUD Desc");
        task.setUserId(42L);
        task.setState(TaskState.VISIBLE);

        Task saved = taskRepository.save(task);
        assertThat(saved.getId()).isNotNull();

        Task found = taskRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("CRUD Task");
        assertThat(found.getState()).isEqualTo(TaskState.VISIBLE);
    }
}
