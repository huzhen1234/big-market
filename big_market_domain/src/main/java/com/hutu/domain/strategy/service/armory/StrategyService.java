package com.hutu.domain.strategy.service.armory;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.repository.cache.StrategyCacheService;
import com.hutu.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.hutu.types.common.Constants.MIN_SCORE;

@Slf4j
@Service
public class StrategyService implements IStrategyService {

    @Resource
    private StrategyCacheService cacheService;

    // TODO 获取用户积分
    int score = 12345;


    /**
     * 装配概率
     * @param strategyId 策略ID
     */
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 抽奖，获取所有策略奖品-全量商品 -- 同时也缓存了每个策略商品的库存
        List<StrategyAwardEntity> strategyAwardEntities = cacheService.assembleLotteryStrategy(strategyId);
        // 默认装配配置【全量抽奖概率】
        assembleLotteryOrigin(strategyAwardEntities,strategyId);
        // 判断是否有权重规则
        List<StrategyGuaranteeEntity> rules = cacheService.queryStrategyGuaranteeWeight(strategyId);
        if (CollectionUtil.isEmpty(rules)) return true;
        // 根据权重和个人积分情况来过滤奖品 --选择权重策略
        StrategyGuaranteeEntity strategyGuaranteeEntity = matchWeightRule(rules, score);
        // 根据权重规则进行抽奖
        assembleLotteryByWeightRule(strategyId,strategyAwardEntities, strategyGuaranteeEntity);
        return true;
    }


    public void assembleLotteryOrigin(List<StrategyAwardEntity> strategyAwardEntities,Long strategyId) {
        cacheDrawLotteryByOriginalRate(strategyId,strategyAwardEntities);
    }

    private void assembleLotteryByWeightRule(Long strategyId,List<StrategyAwardEntity> strategyAwardEntities, StrategyGuaranteeEntity strategyGuaranteeEntity) {
        List<StrategyGuaranteeEntity.AwardWeight> guaranteeAwards = strategyGuaranteeEntity.getGuaranteeAwards();
        if (CollectionUtil.isEmpty(guaranteeAwards)) {
            log.error("权重规则配置错误，没有匹配的奖品");
            throw new RuntimeException("权重规则配置错误");
        }
        // 创建奖品的权重映射
        Map<Long, Integer> awardWeightMap = new HashMap<>();
        for (StrategyGuaranteeEntity.AwardWeight awardWeight : guaranteeAwards) {
            awardWeightMap.put(awardWeight.getAwardId(), awardWeight.getWeight());
        }
        // 过滤出规则中存在的奖品
        List<StrategyAwardEntity> validAwards = strategyAwardEntities.stream()
                .filter(award -> awardWeightMap.containsKey(award.getAwardId()))
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(validAwards)) {
            log.error("权重规则配置错误，策略商品不包含权重商品");
            throw new RuntimeException("权重规则配置错误");
        }
        
        String cacheKey = String.format(Constants.STRATEGY_AWARD_RATE_KEY_TEMPLATE, strategyId);
        // 将权重转换为概率并计算累计概率
        Map<Long, BigDecimal> awardRateMap = new HashMap<>();
        BigDecimal cumulativeRate = BigDecimal.ZERO;
        for (StrategyAwardEntity award : validAwards) {
            Long awardId = award.getAwardId();
            Integer weight = awardWeightMap.get(awardId);
            // 将权重除以100转换为概率（例如权重50转换为0.50）
            BigDecimal probability = new BigDecimal(weight).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
            cumulativeRate = cumulativeRate.add(probability);
            awardRateMap.put(awardId, cumulativeRate);
        }
        // 缓存奖品概率
        cacheService.cacheStrategyAwardRate(cacheKey, awardRateMap);
/*        // 生成1到totalWeight之间的随机整数
        log.info("抽奖，随机数：{}", randomValue);
        int currentWeight = 0;
        for (StrategyAwardEntity award : validAwards) {
            int awardWeight = awardWeightMap.get(award.getAwardId());
            currentWeight += awardWeight;
            if (randomValue <= currentWeight) {
                return award.getAwardId();
            }
        }*/
    }

    private void cacheDrawLotteryByOriginalRate(Long strategyId,List<StrategyAwardEntity> strategyAwardEntities) {
        BigDecimal cumulative = BigDecimal.ZERO;
        for (StrategyAwardEntity award : strategyAwardEntities) {
            cumulative = cumulative.add(award.getWinRate());
            award.setCumulativeRate(cumulative);
        }
        // strategyAwardEntities排序，根据 cumulativeRate来排序，从小到大，然后转成map key为awardId，value为cumulativeRate概率
        strategyAwardEntities.sort(Comparator.comparing(StrategyAwardEntity::getCumulativeRate));
        Map<Long, BigDecimal> awardRateMap = strategyAwardEntities.stream()
                .collect(Collectors.toMap(StrategyAwardEntity::getAwardId, StrategyAwardEntity::getCumulativeRate));
        String cacheKey = String.format(Constants.STRATEGY_AWARD_RATE_KEY_TEMPLATE, strategyId);
        // 缓存奖品概率
        cacheService.cacheStrategyAwardRate(cacheKey, awardRateMap);
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

    private StrategyGuaranteeEntity matchWeightRule(List<StrategyGuaranteeEntity> rules, Integer userScore) {
        // 用于记录匹配的规则
        StrategyGuaranteeEntity matchedRule = null;
        for (StrategyGuaranteeEntity rule : rules) {
            if (MIN_SCORE.equals(rule.getTriggerCondition())) {
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
