package com.avragerghost.tasks_crud_aop.kafka;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageDeserializer<T> extends JsonDeserializer<T> {

    // Повторил на всякий случай, лишним не будет
    private String getMessage(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            return super.deserialize(topic, headers, data);
        } catch (Exception e) {
            log.error("Произошла ошибка при десериализации сообщения: {} - Error: {}", getMessage(data), e.getMessage());
            log.debug("Full error: {}", e);
            // Должно отправлять автоматически в DLQ
            throw new SerializationException("Произошла ошибка при десериализации сообщения: [%s]".formatted(getMessage(data)), e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (Exception e) {
            log.error("Произошла ошибка при десериализации сообщения: {} - Error: {}", getMessage(data), e.getMessage());
            log.debug("Full error: {}", e);
            // Должно отправлять автоматически в DLQ
            throw new SerializationException("Произошла ошибка при десериализации сообщения: [%s]".formatted(getMessage(data)), e);
        }
    }
    
}
