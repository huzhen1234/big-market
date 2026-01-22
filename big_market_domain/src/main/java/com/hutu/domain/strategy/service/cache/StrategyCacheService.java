package com.hutu.domain.strategy.service.cache;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.repository.IStrategyGuaranteeRepository;
import com.hutu.domain.strategy.repository.IStrategyRepository;
import com.hutu.types.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Service
public class StrategyCacheService {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    private IStrategyGuaranteeRepository guaranteeRepository;

    /**
     * 组装策略奖品缓存
     *
     * @param strategyId 策略id
     * @return 策略奖品集合
     */
    @Cached(
            name = Constants.STRATEGY_AWARD_KEY_WITH_RATE,
            key = "#strategyId",
            expire = -1,
            cacheType = CacheType.BOTH
    )
    public List<StrategyAwardEntity> assembleLotteryStrategy(Long strategyId) {
        List<StrategyAwardEntity> list =
                strategyRepository.queryAllStrategyAward(strategyId);
        list.sort(Comparator.comparing(StrategyAwardEntity::getWinRate));
        return list;
    }


    /**
     * 获取策略权重
     *
     * @param strategyId 策略id
     * @return 策略权重
     */
    @Cached(
            name = Constants.STRATEGY_WEIGHT_KEY,
            key = "#strategyId",
            expire = -1,
            cacheType = CacheType.BOTH
    )
    public List<StrategyGuaranteeEntity> queryStrategyGuaranteeWeight(Long strategyId){
        return guaranteeRepository.queryStrategyGuaranteeWeight(strategyId);
    }


    /**
     * 获取策略黑名单
     * @param strategyId 策略id
     * @return 策略黑名单
     */
    @Cached(
            name = Constants.STRATEGY_BLACK_KEY,
            key = "#strategyId",
            expire = -1,
            cacheType = CacheType.BOTH
    )
    public StrategyGuaranteeEntity queryStrategyGuaranteeBlack(Long strategyId){
        return guaranteeRepository.queryStrategyGuaranteeBlack(strategyId);
    }



}
