package com.avragerghost.tasks_crud_aop.dtos.mappers;

import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;
import com.avragerghost.tasks_crud_aop.enums.TaskState;
import com.avragerghost.tasks_crud_aop.models.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskStateMapperTest {
    private final TaskStateMapper mapper = new TaskStateMapper();

    @Test
    void testToDto() {
        Task task = new Task();
        task.setId(99L);
        task.setState(TaskState.DELETED);
        task.setTitle("irrelevant");
        task.setDescription("irrelevant");
        task.setUserId(1L);

        TaskStateDTO dto = mapper.toDto(task);
        assertThat(dto.getTaskId()).isEqualTo(99L);
        assertThat(dto.getState()).isEqualTo(TaskState.DELETED);
    }
}
