package com.hutu.domain.strategy.service.impl;

import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.service.IStrategyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StrategyService implements IStrategyService {

    @Resource
    private StrategyCacheService cacheService;

    @Override
    public Long findStrategyAwardId(Long strategyId) {
        List<StrategyAwardEntity> strategyAwardEntities = cacheService.assembleLotteryStrategy(strategyId);
        double r = ThreadLocalRandom.current().nextDouble(); // 生成一个大于等于 0.0 且小于 1.0 的随机浮点数
        for (StrategyAwardEntity award : strategyAwardEntities) {
            if (r <= award.getCumulativeRate().doubleValue()) {
                return award.getAwardId();
            }
        }
        return -1L;
    }
}
