package com.hutu.domain.strategy.service;

import com.hutu.domain.strategy.model.entity.RaffleAwardEntity;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.model.valobj.TreeActionEntity;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.hutu.types.enums.ResponseCode;
import com.hutu.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static com.hutu.types.common.Constants.RULE_DEFAULT;

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

        // 2. 责任链抽奖计算【这步拿到的是初步的抽奖ID，之后需要根据ID处理抽奖】注意；黑名单、权重等非默认抽奖的直接返回抽奖结果
        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
        log.info("抽奖策略计算-责任链 {} {} {} {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
        // 3. 抽奖结果非默认，直接返回结果()
        if (!RULE_DEFAULT.equals(chainStrategyAwardVO.getLogicModel())) {
            return RaffleAwardEntity.builder()
                    .awardId(chainStrategyAwardVO.getAwardId())
                    .build();
        }
        // todo 为什么这么做的原因是：添加积分条件(权重)，可以拦截积分低于某个值，可以让他们只抽中廉价商品，激励用户获取积分。
        // todo 当用户积分大于某个值时就不添加权重了，此时会走兜底，也就是会进入规则树抽奖。

        // 3. 规则树抽奖过滤【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
        TreeActionEntity.StrategyAwardVO treeStrategyAwardVO = raffleLogicTree(userId, strategyId, chainStrategyAwardVO.getAwardId());




        // 切换为规则树

        // 抽奖后
        return null;

    }

    /**
     * 抽奖计算，责任链抽象方法
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(Long userId, Long strategyId);


    /**
     * 抽奖结果过滤，决策树抽象方法
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 过滤结果【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
     */
    public abstract TreeActionEntity.StrategyAwardVO raffleLogicTree(Long userId, Long strategyId, Long awardId);
}