package com.avragerghost.tasks_crud_aop.kafka;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTaskUpdStateProducer {
    private final KafkaTemplate<String, TaskStateDTO> template;

    public void send(TaskStateDTO taskStateDTO) {
        log.info("TaskUpdState producer sends a message.");
        try {
            template.sendDefault(UUID.randomUUID().toString(), taskStateDTO);
            template.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendTo(String topic, TaskStateDTO taskStateDTO) {
        try {
            template.send(topic, taskStateDTO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
