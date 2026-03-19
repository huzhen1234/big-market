package com.hutu.domain.activity.repository;

import com.hutu.domain.activity.model.aggregate.CreateOrderAggregate;
import com.hutu.domain.activity.model.entity.ActivityCountEntity;
import com.hutu.domain.activity.model.entity.ActivityEntity;
import com.hutu.domain.activity.model.entity.ActivitySkuEntity;
import com.hutu.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;

/**
 * @description 活动仓储接口
 */
public interface IActivityRepository {

    /**
     * 根据sku获取实体(活动id + 活动限制次数)
     * @param sku 商品sku
     * @return 商品sku
     */
    ActivitySkuEntity queryActivitySku(Long sku);

    /**
     * 根据活动id获取活动信息
     * @param activityId 活动id
     * @return 活动信息
     */
    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据活动次数id获取活动次数信息
     * @param activityCountId 活动次数id
     * @return 活动次数信息
     */
    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    /**
     * 保存订单信息
     * @param createOrderAggregate 下单聚合对象
     */
    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    /**
     * 缓存商品sku库存
     * @param cacheKey 缓存key
     * @param stockCount 库存数量
     */
    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    /**
     * 减库存
     * @param sku 商品sku
     * @param cacheKey 缓存key
     * @param endDateTime 缓存有效期
     * @return 是否成功
     */
    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);


    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);


}