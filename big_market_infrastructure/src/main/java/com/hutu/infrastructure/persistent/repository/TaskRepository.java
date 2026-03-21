package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hutu.domain.task.model.entity.TaskEntity;
import com.hutu.domain.task.model.repository.ITaskRepository;
import com.hutu.infrastructure.event.EventPublisher;
import com.hutu.infrastructure.persistent.mapper.TaskMapper;
import com.hutu.infrastructure.persistent.po.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 任务服务仓储实现
 * @create 2024-04-06 10:57
 */
@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private TaskMapper taskMapper;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        // 使用LambdaQueryWrapper替代XML中的查询语句
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Task::getUserId, Task::getTopic, Task::getMessageId, Task::getMessage)
               .and(i -> i.eq(Task::getState, "fail")
                          .or()
                          .eq(Task::getState, "create")
                          .apply("now() - update_time > 6"));
        queryWrapper.last("limit 10");
    
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        List<TaskEntity> taskEntities = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setUserId(task.getUserId());
            taskEntity.setTopic(task.getTopic());
            taskEntity.setMessageId(task.getMessageId());
            taskEntity.setMessage(task.getMessage());
            taskEntities.add(taskEntity);
        }
        return taskEntities;
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        // 使用LambdaUpdateWrapper替代XML中的更新语句
        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getUserId, userId)
                .eq(Task::getMessageId, messageId)
                .set(Task::getState, "completed")
                .set(Task::getUpdateTime, new Date());
    
        taskMapper.update(null, updateWrapper);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        // 使用LambdaUpdateWrapper替代XML中的更新语句
        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getUserId, userId)
                .eq(Task::getMessageId, messageId)
                .set(Task::getState, "fail")
                .set(Task::getUpdateTime, new Date());
    
        taskMapper.update(null, updateWrapper);
    }

}
