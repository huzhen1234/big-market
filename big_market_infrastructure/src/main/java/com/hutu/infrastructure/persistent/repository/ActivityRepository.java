package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hutu.domain.activity.model.aggregate.CreateOrderAggregate;
import com.hutu.domain.activity.model.entity.ActivityCountEntity;
import com.hutu.domain.activity.model.entity.ActivityEntity;
import com.hutu.domain.activity.model.entity.ActivityOrderEntity;
import com.hutu.domain.activity.model.entity.ActivitySkuEntity;
import com.hutu.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.hutu.domain.activity.model.valobj.ActivityStateVO;
import com.hutu.domain.activity.repository.IActivityRepository;
import com.hutu.infrastructure.event.EventPublisher;
import com.hutu.infrastructure.persistent.mapper.RaffleActivityAccountMapper;
import com.hutu.infrastructure.persistent.mapper.RaffleActivityCountMapper;
import com.hutu.infrastructure.persistent.mapper.RaffleActivityMapper;
import com.hutu.infrastructure.persistent.mapper.RaffleActivityOrderMapper;
import com.hutu.infrastructure.persistent.mapper.RaffleActivitySkuMapper;
import com.hutu.infrastructure.persistent.po.RaffleActivity;
import com.hutu.infrastructure.persistent.po.RaffleActivityAccount;
import com.hutu.infrastructure.persistent.po.RaffleActivityCount;
import com.hutu.infrastructure.persistent.po.RaffleActivityOrder;
import com.hutu.infrastructure.persistent.po.RaffleActivitySku;
import com.hutu.infrastructure.persistent.redis.IRedisService;
import com.hutu.types.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.hutu.types.common.Constants.ACTIVITY_SKU_STOCK_COUNT_KAFKA_KEY;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ActivityRepository implements IActivityRepository {

    private final RaffleActivitySkuMapper raffleActivitySkuMapper;

    private final RaffleActivityMapper raffleActivityMapper;

    private final RaffleActivityCountMapper raffleActivityCountMapper;

    private final RaffleActivityOrderMapper raffleActivityOrderMapper;

    private final RaffleActivityAccountMapper raffleActivityAccountMapper;

    private final IRedisService redisService;
    private final EventPublisher eventPublisher;

    @Override
    public ActivitySkuEntity queryActivitySku(Long sku) {
        LambdaQueryWrapper<RaffleActivitySku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RaffleActivitySku::getSku, sku);
        RaffleActivitySku raffleActivitySku = raffleActivitySkuMapper.selectOne(queryWrapper);
        if (raffleActivitySku != null) {
            return ActivitySkuEntity.builder()
                    .sku(raffleActivitySku.getSku())
                    .activityId(raffleActivitySku.getActivityId())
                    .activityCountId(raffleActivitySku.getActivityCountId())
                    .stockCount(raffleActivitySku.getStockCount())
                    .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                    .build();
        }
        // todo
        return null;
    }

    @Override
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        LambdaQueryWrapper<RaffleActivity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RaffleActivity::getActivityId, activityId);
        RaffleActivity raffleActivity = raffleActivityMapper.selectOne(queryWrapper);
        if (raffleActivity != null) {
            return ActivityEntity.builder()
                    .activityId(raffleActivity.getActivityId())
                    .activityName(raffleActivity.getActivityName())
                    .activityDesc(raffleActivity.getActivityDesc())
                    .beginDateTime(raffleActivity.getBeginDateTime())
                    .endDateTime(raffleActivity.getEndDateTime())
                    .strategyId(raffleActivity.getStrategyId())
                    .state(ActivityStateVO.valueOf(raffleActivity.getState()))
                    .build();
        }
        return null;
    }

    @Override
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        LambdaQueryWrapper<RaffleActivityCount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RaffleActivityCount::getActivityCountId, activityCountId);
        RaffleActivityCount raffleActivityCount = raffleActivityCountMapper.selectOne(queryWrapper);
        if (raffleActivityCount != null) {
            return ActivityCountEntity.builder()
                    .activityCountId(raffleActivityCount.getActivityCountId())
                    .totalCount(raffleActivityCount.getTotalCount())
                    .dayCount(raffleActivityCount.getDayCount())
                    .monthCount(raffleActivityCount.getMonthCount())
                    .build();
        }
        return null;
    }

    @Override
    public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        // 订单对象
        ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
        RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
        raffleActivityOrder.setUserId(activityOrderEntity.getUserId());
        raffleActivityOrder.setSku(activityOrderEntity.getSku());
        raffleActivityOrder.setActivityId(activityOrderEntity.getActivityId());
        raffleActivityOrder.setActivityName(activityOrderEntity.getActivityName());
        raffleActivityOrder.setStrategyId(activityOrderEntity.getStrategyId());
        raffleActivityOrder.setOrderId(activityOrderEntity.getOrderId());
        raffleActivityOrder.setOrderTime(activityOrderEntity.getOrderTime());
        raffleActivityOrder.setTotalCount(activityOrderEntity.getTotalCount());
        raffleActivityOrder.setDayCount(activityOrderEntity.getDayCount());
        raffleActivityOrder.setMonthCount(activityOrderEntity.getMonthCount());
        raffleActivityOrder.setState(activityOrderEntity.getState().getCode());
        raffleActivityOrder.setOutBusinessNo(activityOrderEntity.getOutBusinessNo());

        // 1. 写入订单
        raffleActivityOrderMapper.insert(raffleActivityOrder);
        
        // 2. 更新或插入账户
        saveOrUpdateAccount(createOrderAggregate);
    }

    @Override
    public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, stockCount);
    }

    // todo
    @Override
    public boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if (surplus == 0) {
            // 库存消耗没了以后，发送kafka消息，更新数据库库存
            eventPublisher.publish(ACTIVITY_SKU_STOCK_COUNT_KAFKA_KEY, sku);
            return false;
        } else if (surplus < 0) {
            // 库存小于0，恢复为0个
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }

        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等【运营是人来操作，会有这种情况发放，系统要做防护】，也不会超卖。因为所有的可用库存key，都被加锁了。
        // 3. 设置加锁时间为活动到期 + 延迟1天
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        if (!lock) {
            log.info("活动sku库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String cacheKey = Constants.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(activitySkuStockKeyVO, 3, TimeUnit.SECONDS);
    }

    /**
     * 保存或更新活动账户
     * @param createOrderAggregate 创建订单聚合对象
     */
    private void saveOrUpdateAccount(CreateOrderAggregate createOrderAggregate) {
        // 构建账户对象
        RaffleActivityAccount raffleActivityAccount = getRaffleActivityAccount(createOrderAggregate);

        // 尝试更新账户 - 使用 LambdaUpdateWrapper
        int count = raffleActivityAccountMapper.update(
            raffleActivityAccount,
            new LambdaUpdateWrapper<RaffleActivityAccount>()
                .eq(RaffleActivityAccount::getUserId, createOrderAggregate.getUserId())
                .eq(RaffleActivityAccount::getActivityId, createOrderAggregate.getActivityId())
        );
        
        // 如果更新失败（影响行数为 0），则插入新账户
        if (count == 0) {
            raffleActivityAccountMapper.insert(raffleActivityAccount);
        }
    }

    private static RaffleActivityAccount getRaffleActivityAccount(CreateOrderAggregate createOrderAggregate) {
        RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
        raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
        raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
        raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
        raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
        raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
        raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());
        return raffleActivityAccount;
    }
}
