package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.repository.IStrategyRepository;
import com.hutu.infrastructure.persistent.mapper.StrategyAwardMapper;
import com.hutu.infrastructure.persistent.po.StrategyAward;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private StrategyAwardMapper awardMapper;


    /**
     * 根据策略id查询策略奖品集合 缓存
     * @param strategyId 策略id
     */
    @Override
    public List<StrategyAwardEntity> queryAllStrategyAward(Long strategyId) {
        // Todo参数校验
        List<StrategyAwardEntity> strategyAwardEntities = new ArrayList<>();
        LambdaQueryWrapper<StrategyAward> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyAward::getStrategyId, strategyId);
        List<StrategyAward> strategyAwards = awardMapper.selectList(queryWrapper);
        for (StrategyAward strategyAward : strategyAwards) {
            strategyAwardEntities.add(StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardCount(strategyAward.getAwardCount())
                    .awardRemainCount(strategyAward.getAwardRemainCount())
                    .winRate(strategyAward.getWinRate())
                    .build());
        }
        return strategyAwardEntities;
    }
}
