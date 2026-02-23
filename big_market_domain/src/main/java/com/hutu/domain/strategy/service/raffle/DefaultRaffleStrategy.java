package com.hutu.domain.strategy.service.raffle;

import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;
import com.hutu.domain.strategy.model.valobj.RuleTreeVO;
import com.hutu.domain.strategy.model.valobj.TreeActionEntity;
import com.hutu.domain.strategy.repository.IStrategyRuleRepository;
import com.hutu.domain.strategy.repository.cache.StrategyCacheService;
import com.hutu.domain.strategy.service.AbstractRaffleStrategy;
import com.hutu.domain.strategy.service.rule.chain.ILogicChain;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.hutu.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import com.hutu.types.enums.StrategyRuleModelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Resource
    private DefaultChainFactory defaultChainFactory;
    @Resource
    private StrategyCacheService cacheService;
    @Resource
    private IStrategyRuleRepository strategyRuleRepository;
    @Resource
    private IDecisionTreeEngine treeEngine;

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(Long userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.createChain(strategyId);
        return logicChain.doChain(userId, strategyId);
    }

    /**
     * 抽奖结果过滤
     * 已经获取到商品了
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return
     */
    @Override
    public TreeActionEntity.StrategyAwardVO raffleLogicTree(Long userId, Long strategyId, Long awardId) {
        String ruleModels = strategyRuleRepository.queryStrategyAwardRuleModels(strategyId, awardId);
        // 如果对应该商品没有规则模型，则直接返回该商品(抽中了)
        if (StringUtils.isEmpty(ruleModels)) {
            return TreeActionEntity.StrategyAwardVO.builder().awardId(awardId).build();
        }
        // 查找对应的规则模型，在规则树中查找(构建)
        RuleTreeVO ruleTreeVO = cacheService.buildRuleTree(ruleModels);
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + ruleModels);
        }
        return treeEngine.process(userId, strategyId, awardId, ruleTreeVO);
    }
}