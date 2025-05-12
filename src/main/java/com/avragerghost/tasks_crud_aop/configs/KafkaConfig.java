package com.avragerghost.tasks_crud_aop.configs;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import com.avragerghost.tasks_crud_aop.dtos.TaskStateDTO;
import com.avragerghost.tasks_crud_aop.kafka.KafkaTaskUpdStateProducer;
import com.avragerghost.tasks_crud_aop.kafka.MessageDeserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${tasksapp.kafka.consumer.group.id}")
    private String groupId;
    @Value("${tasksapp.kafka.bootstrap.server}")
    private String servers;
    @Value("${tasksapp.kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${tasksapp.kafka.topic.task_updated_state}")
    private String taskUpdStateTopic;

    @Bean
    public ConsumerFactory<String, TaskStateDTO> consumerListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // ErrorHandlingDeserializer для DLQ
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TaskStateDTO.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.avragerghost.tasks_crud_aop");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName());

        DefaultKafkaConsumerFactory<String, TaskStateDTO> factory = new DefaultKafkaConsumerFactory<>(props);

        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TaskStateDTO> kafkaListenerContainerFactory(
            @Qualifier("consumerListenerFactory") ConsumerFactory<String, TaskStateDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskStateDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryBuilder(consumerFactory, factory);
        return factory;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory,
            ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        // Обработка по-одному
        factory.setBatchListener(false);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler(recoverer(kafkaDLQTemplate(producerDLQFactory()))));
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(
            @Qualifier("kafkaDLQTemplate") KafkaTemplate<String, byte[]> dlqTemplate) {
        return new DeadLetterPublishingRecoverer(dlqTemplate);
    }

    private CommonErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, exception, deliveryAttempt) -> {
            log.error("Retry listeners message = {} - Offset = {} - Delivery Attempt = {}", exception.getMessage(),
                    record.offset(), deliveryAttempt);
        });
        return handler;
    }

    @Bean("task-upd-state")
    public KafkaTemplate<String, TaskStateDTO> kafkaTaskStateTemplate(
            ProducerFactory<String, TaskStateDTO> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Primary
    @Bean
    @ConditionalOnProperty(value = "tasksapp.kafka.producer.enable", havingValue = "true", matchIfMissing = true)
    public KafkaTaskUpdStateProducer producerTaskUpdState(
            @Qualifier("task-upd-state") KafkaTemplate<String, TaskStateDTO> template) {
        template.setDefaultTopic(taskUpdStateTopic);
        return new KafkaTaskUpdStateProducer(template);
    }

    @Bean
    public ProducerFactory<String, TaskStateDTO> producerTaskUpdStateFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaDLQTemplate(ProducerFactory<String, byte[]> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, byte[]> producerDLQFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}