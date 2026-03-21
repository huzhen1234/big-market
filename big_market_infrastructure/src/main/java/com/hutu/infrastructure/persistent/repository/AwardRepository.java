package com.hutu.infrastructure.persistent.repository;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hutu.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.hutu.domain.award.model.entity.TaskEntity;
import com.hutu.domain.award.model.entity.UserAwardRecordEntity;
import com.hutu.domain.award.repository.IAwardRepository;
import com.hutu.infrastructure.event.EventPublisher;
import com.hutu.infrastructure.persistent.mapper.TaskMapper;
import com.hutu.infrastructure.persistent.mapper.UserAwardRecordMapper;
import com.hutu.infrastructure.persistent.po.Task;
import com.hutu.infrastructure.persistent.po.UserAwardRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * @description 奖品仓储服务
 */
@Slf4j
@Component
public class AwardRepository implements IAwardRepository {

    @Resource
    private TaskMapper taskMapper;
    @Resource
    private UserAwardRecordMapper userAwardRecordMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {

        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();
        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();

        UserAwardRecord userAwardRecord = new UserAwardRecord();
        userAwardRecord.setUserId(userAwardRecordEntity.getUserId());
        userAwardRecord.setActivityId(userAwardRecordEntity.getActivityId());
        userAwardRecord.setStrategyId(userAwardRecordEntity.getStrategyId());
        userAwardRecord.setOrderId(userAwardRecordEntity.getOrderId());
        userAwardRecord.setAwardId(userAwardRecordEntity.getAwardId());
        userAwardRecord.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        userAwardRecord.setAwardTime(userAwardRecordEntity.getAwardTime());
        userAwardRecord.setAwardState(userAwardRecordEntity.getAwardState().getCode());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setTopic(taskEntity.getTopic());
        task.setMessageId(taskEntity.getMessageId());
        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
        task.setState(taskEntity.getState().getCode());
        // 写入记录
        userAwardRecordMapper.insert(userAwardRecord);
        // 写入任务
        taskMapper.insert(task);
        try {
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 更新数据库记录，task 任务表
            taskMapper.update(
                    new LambdaUpdateWrapper<Task>()
                            .eq(Task::getUserId, task.getUserId())
                            .eq(Task::getMessageId, task.getMessageId())
                            .set(Task::getState, "completed")
            );
        } catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskMapper.update(
                    new LambdaUpdateWrapper<Task>()
                            .eq(Task::getUserId, task.getUserId())
                            .eq(Task::getMessageId, task.getMessageId())
                            .set(Task::getState, "fail")
            );
        }

    }

}
