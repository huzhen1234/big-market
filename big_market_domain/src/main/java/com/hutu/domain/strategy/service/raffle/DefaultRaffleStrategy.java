package com.hutu.domain.strategy.service.raffle;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import com.hutu.domain.strategy.service.rule.filter.ILogicFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Resource
    private DefaultLogicFactory logicFactory;

    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 1. 获取抽奖前的逻辑过滤器
        Map<String, ILogicFilter<RuleActionEntity.RaffleBeforeEntity>> logicFilterGroup = logicFactory.openLogicFilter();
        List<String> ruleFilterModel = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(logicFilterGroup)){
            ruleFilterModel = new ArrayList<>(logicFilterGroup.keySet());
        }
        // 2. 黑名单过滤(黑名单优先）判断是否存在黑名单过滤规则
        String ruleBackList = ruleFilterModel.stream()
                .filter(str -> str.contains(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                        && "before".equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getType()))
                .findFirst()
                .orElse(null);
        if (StringUtils.isNotBlank(ruleBackList)) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode());
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) {
                return ruleActionEntity;
            }
        }

        // 3. 顺序过滤剩余规则 只需处理抽奖前的规则
        List<String> ruleList = ruleFilterModel.stream()
                .filter(s -> !s.equals(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                        && "before".equals(DefaultLogicFactory.LogicModel.getType(s)))
                .collect(Collectors.toList());

        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = null;
        for (String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleBeforeEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖前规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) return ruleActionEntity;
        }
        return ruleActionEntity;
    }

    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        // 1. 获取抽奖中的逻辑过滤器
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> stringILogicFilterMap = logicFactory.openLogicFilter();
        List<String> ruleFilterModel = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(stringILogicFilterMap)){
            ruleFilterModel = new ArrayList<>(stringILogicFilterMap.keySet());
        }
        // 2. 顺序过滤剩余规则 只需处理抽奖中的规则
        List<String> ruleList = ruleFilterModel.stream()
                .filter(s -> s.equals(DefaultLogicFactory.LogicModel.RULE_LOCK.getCode()))
                .collect(Collectors.toList());
        for (String ruleModel : ruleList) {
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = stringILogicFilterMap.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(raffleFactorEntity.getUserId());
            ruleMatterEntity.setStrategyId(raffleFactorEntity.getStrategyId());
            ruleMatterEntity.setAwardId(raffleFactorEntity.getAwardId());
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) return ruleActionEntity;
        }
        return ruleActionEntity;
    }
}