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
                    StrategyRuleModelEnum modelEnum = StrategyRuleModelEnum.valueOf(model.toUpperCase());
                    int paramValue = Integer.parseInt(param);
                    switch (modelEnum) {
                        case RANDOM_SCORE:
                            // 随机积分类别，参数表示积分范围上限，如200表示1-200随机积分
                            strategyRuleEntity.setRandomScore(paramValue);
                            break;
                        case DRAW_TIMES_UNLOCK:
                            // 抽奖次数解锁类别，参数表示需要的抽奖次数，如1表示需要抽奖1次解锁
                            strategyRuleEntity.setDrawTimesUnlock(paramValue);
                            break;
                        case LUCK:
                            // 幸运奖类别，参数表示幸运奖的值
                            strategyRuleEntity.setLuck(paramValue);
                            break;
                        case RULE_BLACKLIST:
                            // 黑名单类别，参数表示黑名单相关值
                            strategyRuleEntity.setRuleBlacklist(paramValue);
                            break;
                    }
                }
                return strategyRuleEntity;
            }
        }
        return null;
    }
}
