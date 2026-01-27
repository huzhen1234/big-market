package com.hutu.domain.strategy.service.raffle;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.AbstractRaffleStrategy;
import com.hutu.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import com.hutu.domain.strategy.service.rule.filter.ILogicFilter;
import lombok.extern.slf4j.Slf4j;
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