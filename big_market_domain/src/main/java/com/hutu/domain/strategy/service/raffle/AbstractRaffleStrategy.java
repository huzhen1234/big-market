package com.hutu.domain.strategy.service.raffle;

import com.hutu.domain.strategy.model.entity.RaffleAwardEntity;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.IRaffleStrategy;
import com.hutu.types.enums.ResponseCode;
import com.hutu.types.exception.AppException;

public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        Long userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || userId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        // 2. 抽奖前的逻辑处理
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> raffleBeforeEntityRuleActionEntity = doCheckRaffleBeforeLogic(raffleFactorEntity);

        // 3. 经过抽奖前的逻辑处理之后，如果返回了抽奖结果，则直接返回抽奖结果
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(raffleBeforeEntityRuleActionEntity.getCode())) {
            return RaffleAwardEntity.builder()
                    .awardId(raffleBeforeEntityRuleActionEntity.getData().getAwardId())
                    .build();
        }

        // 经过抽奖前的逻辑肯定会有商品ID获取到，因此不会为空
        return null;

    }

    /**
     * 抽奖前的逻辑处理 抽象方法，留给其子类来实现
     *
     * @param raffleFactorEntity 抽奖因子
     * @param logics             抽奖前的逻辑处理
     * @return 抽奖结果
     */
    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);
}