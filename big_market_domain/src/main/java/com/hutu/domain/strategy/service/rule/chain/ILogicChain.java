package com.hutu.domain.strategy.service.rule.chain;


/**
 * 责任链接口
 */
public interface ILogicChain {

    /** 根据策略ID，用户ID获取抽奖结果 */
    Long doChain(Long strategyId, Long userId);

    /** 添加责任链 */
    ILogicChain addLogic(ILogicChain logic);

    /** 获取责任链 */
    ILogicChain getLogic();









}
