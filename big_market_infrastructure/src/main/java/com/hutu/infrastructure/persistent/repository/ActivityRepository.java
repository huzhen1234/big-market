package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hutu.domain.activity.model.aggregate.CreateOrderAggregate;
import com.hutu.domain.activity.model.entity.ActivityCountEntity;
import com.hutu.domain.activity.model.entity.ActivityEntity;
import com.hutu.domain.activity.model.entity.ActivityOrderEntity;
import com.hutu.domain.activity.model.entity.ActivitySkuEntity;
import com.hutu.domain.activity.model.valobj.ActivityStateVO;
import com.hutu.domain.activity.repository.IActivityRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ActivityRepository implements IActivityRepository {

    private final RaffleActivitySkuMapper raffleActivitySkuMapper;

    private final RaffleActivityMapper raffleActivityMapper;

    private final RaffleActivityCountMapper raffleActivityCountMapper;

    private final RaffleActivityOrderMapper raffleActivityOrderMapper;

    private final RaffleActivityAccountMapper raffleActivityAccountMapper;

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

    /**
     * 保存或更新活动账户
     * @param createOrderAggregate 创建订单聚合对象
     */
    private void saveOrUpdateAccount(CreateOrderAggregate createOrderAggregate) {
        // 构建账户对象
        RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
        raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
        raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
        raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
        raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
        raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
        raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
        raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());

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
}
