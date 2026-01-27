package com.hutu.domain.strategy.service.armory;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.service.cache.StrategyCacheService;
import com.hutu.types.enums.StrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StrategyService implements IStrategyService {

    @Resource
    private StrategyCacheService cacheService;

    // TODO 获取用户积分
    int score = 12345;

    @Override
    public Long findWeightStrategyAwardId(Long strategyId,Long userId) {
        // 抽奖，获取所有策略奖品(未过滤出权重商品)
        List<StrategyAwardEntity> strategyAwardEntities = cacheService.assembleLotteryStrategy(strategyId);
        // 根据权重和个人积分情况来过滤奖品
        StrategyGuaranteeEntity strategyGuaranteeEntity = matchWeightRule(strategyId, score);
        // 根据权重规则进行抽奖
        return drawLotteryByWeightRule(strategyAwardEntities, strategyGuaranteeEntity);
    }

    @Override
    public Long findOriginStrategyAwardId(Long strategyId, Long userId) {
        // 抽奖，获取所有策略奖品(未过滤出权重商品)
        List<StrategyAwardEntity> strategyAwardEntities = cacheService.assembleLotteryStrategy(strategyId);
        return drawLotteryByOriginalRate(strategyAwardEntities);
    }

    private Long drawLotteryByWeightRule(List<StrategyAwardEntity> strategyAwardEntities, StrategyGuaranteeEntity strategyGuaranteeEntity) {
        // 如果没有权重规则，则使用原始概率进行抽奖
        if (strategyGuaranteeEntity == null){
            return null;
        }
        List<StrategyGuaranteeEntity.AwardWeight> guaranteeAwards = strategyGuaranteeEntity.getGuaranteeAwards();
        if (CollectionUtil.isEmpty(guaranteeAwards)) {
            // todo 如果规则中未配置任何奖品，则无奖品可抽
            return -1L;
        }
        // 创建奖品的权重映射
        Map<Long, Integer> awardWeightMap = new HashMap<>();
        int totalWeight = 0;
        for (StrategyGuaranteeEntity.AwardWeight awardWeight : guaranteeAwards) {
            awardWeightMap.put(awardWeight.getAwardId(), awardWeight.getWeight());
            totalWeight += awardWeight.getWeight();
        }
        // 过滤出规则中存在的奖品
        List<StrategyAwardEntity> validAwards = strategyAwardEntities.stream()
                .filter(award -> awardWeightMap.containsKey(award.getAwardId()))
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(validAwards)) {
            // todo
            return -1L;
        }
        // 生成1到totalWeight之间的随机整数
        int randomValue = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);
        log.info("抽奖，随机数：{}", randomValue);
        int currentWeight = 0;
        for (StrategyAwardEntity award : validAwards) {
            int awardWeight = awardWeightMap.get(award.getAwardId());
            currentWeight += awardWeight;
            if (randomValue <= currentWeight) {
                return award.getAwardId();
            }
        }
        // todo 理论上不会走到这里，但为了安全返回最后一个奖品
        return validAwards.get(validAwards.size() - 1).getAwardId();
    }

    private Long drawLotteryByOriginalRate(List<StrategyAwardEntity> strategyAwardEntities) {
        BigDecimal cumulative = BigDecimal.ZERO;
        for (StrategyAwardEntity award : strategyAwardEntities) {
            cumulative = cumulative.add(award.getWinRate());
            award.setCumulativeRate(cumulative);
        }
        double r = ThreadLocalRandom.current().nextDouble(); // 生成一个大于等于 0.0 且小于 1.0 的随机浮点数
        log.info("抽奖，随机数：{}", r);
        for (StrategyAwardEntity award : strategyAwardEntities) {
            if (r <= award.getCumulativeRate().doubleValue()) {
                return award.getAwardId();
            }
        }
        // todo兜底，如果没中奖品
        return null;
    }


    private StrategyGuaranteeEntity matchWeightRule(Long strategyId, Integer userScore) {
        List<StrategyGuaranteeEntity> rules = cacheService.queryStrategyGuaranteeWeight(strategyId);
        // 用于记录匹配的规则
        StrategyGuaranteeEntity matchedRule = null;
        for (StrategyGuaranteeEntity rule : rules) {
            if (StrategyEnum.MIN_SCORE.name().equals(rule.getTriggerCondition())) {
                int minScore = Integer.parseInt(rule.getTriggerValue());

                // 如果用户积分大于等于当前阈值，记录此规则
                if (userScore >= minScore) {
                    matchedRule = rule;
                } else {
                    // 由于rules是按TriggerValue从小到大排序的，
                    // 一旦遇到用户积分小于的阈值，后面的阈值都会更大，可以提前结束循环
                    break;
                }
            }
        }
        return matchedRule;
    }


}
