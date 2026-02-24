package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.repository.IStrategyRepository;
import com.hutu.infrastructure.persistent.mapper.StrategyAwardMapper;
import com.hutu.infrastructure.persistent.po.StrategyAward;
import com.hutu.infrastructure.persistent.redis.IRedisService;
import com.hutu.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private StrategyAwardMapper awardMapper;

    @Resource
    private IRedisService redisService;


    /**
     * 根据策略id查询策略奖品集合 缓存
     * @param strategyId 策略id
     */
    @Override
    public List<StrategyAwardEntity> queryAllStrategyAward(Long strategyId) {
        // Todo参数校验
        List<StrategyAwardEntity> strategyAwardEntities = new ArrayList<>();
        LambdaQueryWrapper<StrategyAward> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyAward::getStrategyId, strategyId);
        List<StrategyAward> strategyAwards = awardMapper.selectList(queryWrapper);
        for (StrategyAward strategyAward : strategyAwards) {
            strategyAwardEntities.add(StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardCount(strategyAward.getAwardCount())
                    .awardRemainCount(strategyAward.getAwardRemainCount())
                    .winRate(strategyAward.getWinRate())
                    .build());
            // 为里面的奖品key映射奖品库存
            String key = String.format(
                    Constants.STRATEGY_AWARD_STOCK_KEY_TEMPLATE,
                    strategyAward.getStrategyId(),
                    strategyAward.getAwardId()
            );
            // 缓存奖品库存
            cacheStrategyAwardCount(key, strategyAward.getAwardRemainCount());
        }
        return strategyAwardEntities;
    }

    @Override
    public Boolean substractionAwardStock(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            // 库存小于0，恢复为0个
            redisService.setValue(cacheKey, 0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. todo 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了。
        String lockKey = cacheKey + Constants.RULE_MODEL_SPLIT + surplus;
        Boolean lock = redisService.setNx(lockKey);
        if (!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }


    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }
}
