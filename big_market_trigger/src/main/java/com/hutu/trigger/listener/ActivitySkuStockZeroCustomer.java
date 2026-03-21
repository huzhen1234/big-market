package com.hutu.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.hutu.domain.activity.service.IRaffleActivitySkuStockService;
import com.hutu.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 活动 sku 库存耗尽
 */
@Slf4j
@Component
public class ActivitySkuStockZeroCustomer {

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Resource
    private IRaffleActivitySkuStockService skuStock;

    @KafkaListener(topics = "${kafka.topic.activity_sku_stock_zero}", groupId = "${spring.kafka.consumer.group-id}")
    public void listener(ConsumerRecord<?, ?> record, Acknowledgment acknowledgment) {
        try {
            String message = (String) record.value();
            log.info("监听活动 sku 库存消耗为 0 消息 topic: {} partition: {} offset: {} message: {}", 
                    record.topic(), 
                    record.partition(), 
                    record.offset(), 
                    message);
            
            // 转换对象
            BaseEvent.EventMessage<Long> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<Long>>() {
            }.getType());
            Long sku = eventMessage.getData();
            
            // 更新库存
            skuStock.clearActivitySkuStock(sku);
            
            // 清空队列 「此时就不需要延迟更新数据库记录了」
            skuStock.clearQueueValue();
            
            // 手动提交 offset
            acknowledgment.acknowledge();
            log.info("活动 sku 库存消耗为 0 消息处理成功，已提交 offset，sku: {}", sku);
        } catch (Exception e) {
            log.error("监听活动 sku 库存消耗为 0 消息，消费失败 topic: {} message: {}", record.topic(), e.getLocalizedMessage());
            // 不提交 offset，让消息重新消费
            throw e;
        }
    }

}
