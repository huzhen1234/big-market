package com.hutu.domain.strategy.service.rule.chain;


/**
 * 责任链接口
 * 只提供核心抽奖方法，其余方法在其他接口，由它来继承
 */
public interface ILogicChain extends ILogicChainArmory {

    /** 根据策略ID，用户ID获取抽奖结果 */
    Long doChain(Long strategyId, Long userId);
}
