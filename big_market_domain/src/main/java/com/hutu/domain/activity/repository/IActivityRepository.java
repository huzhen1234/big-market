package com.hutu.domain.activity.repository;

import com.hutu.domain.activity.model.entity.ActivityCountEntity;
import com.hutu.domain.activity.model.entity.ActivityEntity;
import com.hutu.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @description 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

}