package com.hutu;

import com.alibaba.fastjson2.JSON;
import com.hutu.infrastructure.persistent.mapper.StrategyConfigMapper;
import com.hutu.infrastructure.persistent.po.StrategyConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
public class AwardDaoTest {

    @Resource
    private StrategyConfigMapper configMapper;

    @Test
    public void test_queryAwardList() {
        for (StrategyConfig strategyConfig : configMapper.selectList(null)) {
            log.info("strategyConfig: {}", JSON.toJSONString(strategyConfig));
        }
    }

}