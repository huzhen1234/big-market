package com.hutu.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @description 消息发送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送 Kafka 消息
     *
     * @param topic   主题
     * @param message 消息内容
     */
    public void publish(String topic, Object message) {
        log.info("开始发送 Kafka 消息，topic: {}, message: {}", topic, message);
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message).completable();
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka 消息发送成功，topic: {}, offset: {}, partition: {}", 
                        topic, 
                        result.getRecordMetadata().offset(), 
                        result.getRecordMetadata().partition());
                } else {
                    log.error("Kafka 消息发送失败，topic: {}, error: {}", topic, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("发送 Kafka 消息异常，topic: {}, error: {}", topic, e.getMessage(), e);
            throw new RuntimeException("发送 Kafka 消息失败", e);
        }
    }

    /**
     * 发送 Kafka 消息（带 key）
     *
     * @param topic   主题
     * @param key     消息 key
     * @param message 消息内容
     */
    public void publish(String topic, String key, Object message) {
        log.info("开始发送 Kafka 消息，topic: {}, key: {}, message: {}", topic, key, message);
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message).completable();
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka 消息发送成功，topic: {}, key: {}, offset: {}, partition: {}", 
                        topic, 
                        key,
                        result.getRecordMetadata().offset(), 
                        result.getRecordMetadata().partition());
                } else {
                    log.error("Kafka 消息发送失败，topic: {}, key: {}, error: {}", topic, key, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("发送 Kafka 消息异常，topic: {}, key: {}, error: {}", topic, key, e.getMessage(), e);
            throw new RuntimeException("发送 Kafka 消息失败", e);
        }
    }

    /**
     * 发送 Kafka 消息并等待结果（同步）
     *
     * @param topic   主题
     * @param message 消息内容
     * @return 发送结果
     */
    public SendResult<String, Object> publishSync(String topic, Object message) {
        log.info("开始同步发送 Kafka 消息，topic: {}, message: {}", topic, message);
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, message).get();
            log.info("Kafka 消息同步发送成功，topic: {}, offset: {}, partition: {}", 
                topic, 
                result.getRecordMetadata().offset(), 
                result.getRecordMetadata().partition());
            return result;
        } catch (Exception e) {
            log.error("同步发送 Kafka 消息失败，topic: {}, error: {}", topic, e.getMessage(), e);
            throw new RuntimeException("同步发送 Kafka 消息失败", e);
        }
    }

    /**
     * 发送 Kafka 消息并等待结果（同步，带 key）
     *
     * @param topic   主题
     * @param key     消息 key
     * @param message 消息内容
     * @return 发送结果
     */
    public SendResult<String, Object> publishSync(String topic, String key, Object message) {
        log.info("开始同步发送 Kafka 消息，topic: {}, key: {}, message: {}", topic, key, message);
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, message).get();
            log.info("Kafka 消息同步发送成功，topic: {}, key: {}, offset: {}, partition: {}", 
                topic, 
                key,
                result.getRecordMetadata().offset(), 
                result.getRecordMetadata().partition());
            return result;
        } catch (Exception e) {
            log.error("同步发送 Kafka 消息失败，topic: {}, key: {}, error: {}", topic, key, e.getMessage(), e);
            throw new RuntimeException("同步发送 Kafka 消息失败", e);
        }
    }
}