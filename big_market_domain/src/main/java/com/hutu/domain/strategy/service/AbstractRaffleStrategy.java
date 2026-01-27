package com.hutu.domain.strategy.service;

import com.hutu.domain.strategy.model.entity.RaffleAwardEntity;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.rule.chain.ILogicChain;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.hutu.types.enums.ResponseCode;
import com.hutu.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    @Resource
    private DefaultChainFactory defaultChainFactory;

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        Long userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || userId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        // 优化替换为责任链模式 -- 抽奖前的逻辑处理
        ILogicChain logicChain = defaultChainFactory.createChain(strategyId);
        Long awardId = logicChain.doChain(strategyId, userId);
        raffleFactorEntity.setAwardId(awardId);
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> raffleCenterEntityRuleActionEntity = doCheckRaffleCenterLogic(raffleFactorEntity);
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(raffleCenterEntityRuleActionEntity.getCode())) {
            log.info("抽奖中被拦截，走兜底策略商品 ！！！");
            return RaffleAwardEntity.builder()
                    .awardDesc("暂时策略商品")
                    .build();
        }

        // 经过抽奖前的逻辑肯定会有商品ID获取到，因此不会为空
        return null;

    }

    /**
     * 抽奖中的逻辑处理 抽象方法，留给其子类来实现
     * 解锁次数
     *
     * @param raffleFactorEntity 抽奖因子
     * @param logics             抽奖中的逻辑处理
     * @return 抽奖结果
     */
    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics);
}