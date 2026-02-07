package com.hutu.domain.strategy.service.rule.chain.factory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.hutu.domain.strategy.repository.IStrategyGuaranteeRepository;
import com.hutu.domain.strategy.service.rule.chain.ILogicChain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.hutu.types.common.Constants.RULE_BLACKLIST;
import static com.hutu.types.common.Constants.RULE_DEFAULT;
import static com.hutu.types.common.Constants.RULE_WEIGHT;

/**
 * 默认责任链工厂
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultChainFactory {

    private final Map<String, ILogicChain> chainMap;
    private final IStrategyGuaranteeRepository repository;

    public ILogicChain createChain(Long strategyId) {

        // 1. 查询策略启用的规则
        List<String> strategyTypes = repository.queryStrategyType(strategyId);

        // 2. 固定的责任链执行顺序（唯一权威）
        List<String> allOrderedTypes = Arrays.asList(
                RULE_BLACKLIST, // 黑名单优先
                RULE_WEIGHT     // 权重其次
        );

        ILogicChain head = null;
        ILogicChain current = null;

        // 3. 按顺序构建责任链
        for (String ruleType : allOrderedTypes) {
            if (CollectionUtil.isNotEmpty(strategyTypes) && strategyTypes.contains(ruleType)) {

                ILogicChain chain = chainMap.get(ruleType);
                if (chain == null) {
                    continue;
                }

                if (head == null) {
                    head = chain;
                    current = chain;
                } else {
                    current = current.addLogic(chain);
                }
            }
        }

        // 4. 默认责任链兜底
        ILogicChain defaultChain = chainMap.get(RULE_DEFAULT);

        if (head == null) {
            // 策略未配置任何规则，直接走默认
            log.info("责任链：仅默认规则");
            return defaultChain;
        }

        current.addLogic(defaultChain);

        log.info("责任链 {}", JSONUtil.toJsonStr(head));
        return head;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Long awardId;
        /**  */
        private String logicModel;
    }
}
