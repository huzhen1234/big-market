package com.hutu.domain.strategy.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.repository.IStrategyRepository;
import com.hutu.types.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class StrategyCacheService {

    @Resource
    private IStrategyRepository strategyRepository;

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
        BigDecimal cumulative = BigDecimal.ZERO;
        for (StrategyAwardEntity award : list) {
            cumulative = cumulative.add(award.getWinRate());
            award.setCumulativeRate(cumulative);
        }
        return list;
    }
}
