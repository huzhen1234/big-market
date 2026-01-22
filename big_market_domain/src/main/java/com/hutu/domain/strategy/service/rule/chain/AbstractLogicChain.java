package com.hutu.domain.strategy.service.rule.chain;

/**
 * 抽象责任链接口
 */
public abstract class AbstractLogicChain implements ILogicChain{

    protected ILogicChain next;

    @Override
    public ILogicChain addLogic(ILogicChain logic) {
        this.next = logic;
        return logic;
    }

    @Override
    public ILogicChain getLogic() {
        return this.next;
    }
}
