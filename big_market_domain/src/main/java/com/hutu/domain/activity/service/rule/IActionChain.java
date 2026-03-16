package com.hutu.domain.activity.service.rule;

import com.hutu.domain.activity.model.entity.ActivityCountEntity;
import com.hutu.domain.activity.model.entity.ActivityEntity;
import com.hutu.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @description 下单规则过滤接口
 */
public interface IActionChain extends IActionChainArmory {

    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}