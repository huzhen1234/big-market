package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;
import com.hutu.domain.strategy.repository.IStrategyRuleRepository;
import com.hutu.infrastructure.persistent.mapper.StrategyRuleMapper;
import com.hutu.infrastructure.persistent.po.StrategyRule;
import com.hutu.types.enums.StrategyRuleModelEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static com.hutu.types.common.Constants.RULE_MODEL_SPLIT;

@Repository
public class StrategyRuleRepository implements IStrategyRuleRepository {
    @Resource
    private StrategyRuleMapper strategyRuleMapper;

    // todo 逻辑改动点
    @Override
    public StrategyRuleEntity findByStrategyIdAndAwardId(Long strategyId, Long awardId) {
        LambdaQueryWrapper<StrategyRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyRule::getStrategyId, strategyId)
                .eq(StrategyRule::getAwardId, awardId);
        StrategyRule strategyRule = strategyRuleMapper.selectOne(queryWrapper);
        if (strategyRule != null) {
            String ruleModel = strategyRule.getRuleModel();
            String ruleParam = strategyRule.getRuleParam();
            if (StringUtils.isNotEmpty(ruleModel)) {
                // 获取规则模型
                String[] models = ruleModel.split(RULE_MODEL_SPLIT);
                String[] ruleParams = ruleParam.split(RULE_MODEL_SPLIT);
                StrategyRuleEntity strategyRuleEntity = new StrategyRuleEntity();
                strategyRuleEntity.setStrategyId(strategyRule.getStrategyId());
                strategyRuleEntity.setAwardId(strategyRule.getAwardId());
                strategyRuleEntity.setRuleDesc(strategyRule.getRuleDesc());
                // 解析模型和参数，构建策略规则实体
                for (int i = 0; i < models.length && i < ruleParams.length; i++) {
                    String model = models[i];
                    String param = ruleParams[i];
                    StrategyRuleModelEnum modelEnum =
                            StrategyRuleModelEnum.valueOf(model.toUpperCase());
                    int paramValue = Integer.parseInt(param);
                    strategyRuleEntity.setModelEnum(modelEnum);
                    switch (modelEnum) {
                        case RANDOM_SCORE:
                            strategyRuleEntity.setRandomScore(paramValue);
                            return strategyRuleEntity;
                        case DRAW_TIMES_UNLOCK:
                            strategyRuleEntity.setDrawTimesUnlock(paramValue);
                            return strategyRuleEntity;
                        case LUCK:
                            strategyRuleEntity.setLuck(paramValue);
                            return strategyRuleEntity;
                        default:
                            break;
                    }
                }
                return strategyRuleEntity;
            }
        }
        return null;
    }
}
