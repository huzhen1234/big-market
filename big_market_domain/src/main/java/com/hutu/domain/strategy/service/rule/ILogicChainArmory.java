package com.hutu.domain.strategy.service.rule;

import com.hutu.domain.strategy.service.rule.chain.ILogicChain;

public interface ILogicChainArmory {

    /** 添加责任链 */
    ILogicChain addLogic(ILogicChain logic);

    /** 获取责任链 */
    ILogicChain getLogic();
}
