package com.avragerghost.tasks_crud_aop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Базовая фильтрация для исключения, например, {@code DELETED} задач.
     * 
     * @param state типа {@link TaskState}
     * @return Список задач, исключая задачи с указанным состоянием
     */
    @Query("SELECT t FROM Task WHERE t.state != :state")
    List<Task> findAllByStateNot(TaskState state);
}
