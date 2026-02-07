package com.hutu.infrastructure.persistent.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hutu.domain.strategy.model.valobj.RuleLimitTypeVO;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeNodeVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeVO;
import com.hutu.domain.strategy.repository.IRuleTreeRepository;
import com.hutu.infrastructure.persistent.mapper.RuleTreeMapper;
import com.hutu.infrastructure.persistent.mapper.RuleTreeNodeLineMapper;
import com.hutu.infrastructure.persistent.mapper.RuleTreeNodeMapper;
import com.hutu.infrastructure.persistent.po.RuleTree;
import com.hutu.infrastructure.persistent.po.RuleTreeNode;
import com.hutu.infrastructure.persistent.po.RuleTreeNodeLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据库表构建规则树
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RuleTreeRepository implements IRuleTreeRepository {

    private final RuleTreeMapper ruleTreeMapper;
    private final RuleTreeNodeMapper ruleTreeNodeMapper;
    private final RuleTreeNodeLineMapper ruleTreeNodeLineMapper;
    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 从数据库获取
        LambdaQueryWrapper<RuleTree> ruleTreeWrapper = new LambdaQueryWrapper<>();
        ruleTreeWrapper.eq(RuleTree::getTreeId, treeId);
        RuleTree ruleTree = ruleTreeMapper.selectOne(ruleTreeWrapper);

        LambdaQueryWrapper<RuleTreeNode> treeNodeWrapper = new LambdaQueryWrapper<>();
        treeNodeWrapper.eq(RuleTreeNode::getTreeId, treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeMapper.selectList(treeNodeWrapper);

        LambdaQueryWrapper<RuleTreeNodeLine> treeNodeLineWrapper = new LambdaQueryWrapper<>();
        treeNodeLineWrapper.eq(RuleTreeNodeLine::getTreeId, treeId);

        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineMapper.selectList(treeNodeLineWrapper);
        // 1. tree node line 转换Map结构
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();

            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 2. tree node 转换为Map结构
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 3. 构建 Rule Tree
        return RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeNodeRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();
    }
}
