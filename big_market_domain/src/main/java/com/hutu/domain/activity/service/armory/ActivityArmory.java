package com.hutu.domain.activity.service.armory;

import com.hutu.domain.activity.model.entity.ActivitySkuEntity;
import com.hutu.domain.activity.repository.IActivityRepository;
import com.hutu.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动sku预热
 * @create 2024-03-30 09:12
 */
@Slf4j
@Service
public class ActivityArmory implements IActivityArmory,IActivityDispatch{

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleActivitySku(Long sku) {
        // 预热活动sku库存
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySku(sku);
        cacheActivitySkuStockCount(sku, activitySkuEntity.getStockCount());

        // 预热活动【todo查询时预热到缓存】
        activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());

        // 预热活动次数【todo查询时预热到缓存】
        activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        return true;
    }

    private void cacheActivitySkuStockCount(Long sku, Integer stockCount) {
        String cacheKey = String.format(Constants.ACTIVITY_SKU_STOCK_COUNT_KEY, sku);
        activityRepository.cacheActivitySkuStockCount(cacheKey, stockCount);
    }


    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        String cacheKey = String.format(Constants.ACTIVITY_SKU_STOCK_COUNT_KEY, sku);
        return activityRepository.subtractionActivitySkuStock(sku, cacheKey, endDateTime);
    }
}
