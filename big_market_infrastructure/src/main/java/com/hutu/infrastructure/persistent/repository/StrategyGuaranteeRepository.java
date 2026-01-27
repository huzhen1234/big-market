package com.hutu.infrastructure.persistent.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.repository.IStrategyGuaranteeRepository;
import com.hutu.infrastructure.persistent.mapper.StrategyGuaranteeMapper;
import com.hutu.infrastructure.persistent.po.StrategyGuarantee;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.hutu.types.common.Constants.RULE_BLACKLIST;
import static com.hutu.types.common.Constants.RULE_WEIGHT;

@Repository
public class StrategyGuaranteeRepository implements IStrategyGuaranteeRepository {

    @Resource
    private StrategyGuaranteeMapper guaranteeMapper;


    // 根据策略id查询策略配置 权重
    @Override
    public List<StrategyGuaranteeEntity> queryStrategyGuaranteeWeight(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId)
                .eq(StrategyGuarantee::getStrategyType, RULE_WEIGHT)
                .orderByAsc(StrategyGuarantee::getTriggerValue); // 按 triggerValue 字段升序排列
        List<StrategyGuarantee> strategyGuarantee = guaranteeMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(strategyGuarantee)) {
            return strategyGuarantee.stream().map(tee -> StrategyGuaranteeEntity.builder()
                            .strategyId(tee.getStrategyId())
                            .triggerCondition(tee.getTriggerCondition())
                            .triggerValue(tee.getTriggerValue())
                            .guaranteeAwards(JSONUtil.toList(JSONUtil.toJsonStr(tee.getGuaranteeAwards()), StrategyGuaranteeEntity.AwardWeight.class))
                            .build())
                    .collect(Collectors.toList());
        }
        return CollectionUtil.empty(StrategyGuaranteeEntity.class);
    }

    @Override
    public StrategyGuaranteeEntity queryStrategyGuaranteeBlack(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId)
                .eq(StrategyGuarantee::getStrategyType, RULE_BLACKLIST);
        StrategyGuarantee strategyGuarantee = guaranteeMapper.selectOne(queryWrapper);
        return StrategyGuaranteeEntity.builder()
                .strategyId(strategyGuarantee.getStrategyId())
                .triggerCondition(strategyGuarantee.getTriggerCondition())
                .triggerValue(strategyGuarantee.getTriggerValue()) // 触发值 黑名单的值 [1,2,3]
                .guaranteeAwards(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getGuaranteeAwards()), StrategyGuaranteeEntity.AwardWeight.class))
                .backListUserIds(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getTriggerValue()), Long.class))
                .build();

    }

    // 根据策略ID获取策略类型 去重
    @Override
    public List<String> queryStrategyType(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId);
        List<StrategyGuarantee> strategyGuarantees = guaranteeMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(strategyGuarantees)) {
            return strategyGuarantees.stream().map(StrategyGuarantee::getStrategyType).distinct().collect(Collectors.toList());
        }
        return CollectionUtil.empty(String.class);
    }
}