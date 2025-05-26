package com.avragerghost.tasks_crud_aop.dtos.mappers;

import com.avragerghost.tasks_crud_aop.dtos.TaskDTO;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskMapperTest {
    private final TaskMapper mapper = new TaskMapper();

    @Test
    void testToDTO() {
        Task task = new Task();
        task.setState(TaskState.HIDDEN);
        task.setTitle("Test Title");
        task.setDescription("Test Desc");
        task.setUserId(123L);

        TaskDTO dto = mapper.toDTO(task);
        assertThat(dto.getState()).isEqualTo(TaskState.HIDDEN);
        assertThat(dto.getTitle()).isEqualTo("Test Title");
        assertThat(dto.getDescription()).isEqualTo("Test Desc");
    }

    @Test
    void testToEntity() {
        TaskDTO dto = new TaskDTO();
        dto.setState(TaskState.VISIBLE);
        dto.setTitle("DTO Title");
        dto.setDescription("DTO Desc");

        Task task = new Task();
        task.setState(TaskState.HIDDEN);
        task.setTitle("Old Title");
        task.setDescription("Old Desc");
        task.setUserId(42L);

        Task result = mapper.toEntity(dto, task);
        assertThat(result.getState()).isEqualTo(TaskState.VISIBLE);
        assertThat(result.getTitle()).isEqualTo("DTO Title");
        assertThat(result.getDescription()).isEqualTo("DTO Desc");
        assertThat(result.getUserId()).isEqualTo(42L);
    }
}
