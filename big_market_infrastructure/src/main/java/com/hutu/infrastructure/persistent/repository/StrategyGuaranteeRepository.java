package com.hutu.infrastructure.persistent.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.repository.IStrategyGuaranteeRepository;
import com.hutu.infrastructure.persistent.mapper.StrategyGuaranteeMapper;
import com.hutu.infrastructure.persistent.po.StrategyGuarantee;
import com.hutu.types.enums.StrategyEnum;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StrategyGuaranteeRepository implements IStrategyGuaranteeRepository {

    @Resource
    private StrategyGuaranteeMapper guaranteeMapper;


    @Override
    public List<StrategyGuaranteeEntity> queryStrategyGuaranteeWeight(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId)
                .eq(StrategyGuarantee::getStrategyType, StrategyEnum.RULE_WEIGHT.name())
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
                .eq(StrategyGuarantee::getStrategyType, StrategyEnum.RULE_BLACKLIST.name());
        StrategyGuarantee strategyGuarantee = guaranteeMapper.selectOne(queryWrapper);
        return StrategyGuaranteeEntity.builder()
                .strategyId(strategyGuarantee.getStrategyId())
                .triggerCondition(strategyGuarantee.getTriggerCondition())
                .triggerValue(strategyGuarantee.getTriggerValue()) // 触发值 黑名单的值 [1,2,3]
                .guaranteeAwards(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getGuaranteeAwards()), StrategyGuaranteeEntity.AwardWeight.class))
                .backListUserIds(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getTriggerValue()), Long.class))
                .build();

    }
}