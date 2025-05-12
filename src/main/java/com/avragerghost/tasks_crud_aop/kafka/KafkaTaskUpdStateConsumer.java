package com.avragerghost.tasks_crud_aop.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;
import com.avragerghost.tasks_crud_aop.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTaskUpdStateConsumer {

    @Value("${send.to.my.mail}")
    private String emailTo;
    private final NotificationService emailService;

    @KafkaListener(id = "${tasksapp.kafka.consumer.group.id}", topics = "${tasksapp.kafka.topic.task_updated_state}", containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload TaskStateDTO taskStateDTO, Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("TaskUpdState consumer: обработка новых сообщений.");
        try {
            String subject = "Tasks App: Task state was updated";
            String text = String
                    .format("В топик '%1$s' было получено сообщение о том, что Задача#%2$s изменила статус на [%3$s]",
                            topic, taskStateDTO.getTaskId(), taskStateDTO.getState());
            emailService.send(emailTo, subject, text);
        } catch (Exception e) {
            log.error("Email failed to send from TaskUpdState consumer.", e);
        } finally {
            ack.acknowledge();
        }
    }
}
