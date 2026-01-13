package com.hutu;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.hutu.domain.strategy.model.entity.RaffleAwardEntity;
import com.hutu.domain.strategy.model.entity.RaffleFactorEntity;
import com.hutu.domain.strategy.service.IRaffleStrategy;
import com.hutu.domain.strategy.service.IStrategyService;
import com.hutu.domain.strategy.service.cache.StrategyCacheService;
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
    @Resource
    private IStrategyService strategyService;
    @Resource
    private StrategyCacheService cacheService;
    @Resource
    private IRaffleStrategy raffleStrategy;

    @Test
    public void test_queryAwardList() {
        for (StrategyConfig strategyConfig : configMapper.selectList(null)) {
            log.info("strategyConfig: {}", JSON.toJSONString(strategyConfig));
        }
    }

    @Test
    public void test_queryAwardList2() {
//        cacheService.assembleLotteryStrategy(1001L);
        System.out.println(strategyService.findWeightStrategyAwardId(1001L,1001L));
    }


    /**
     * 抽奖
     */
    @Test
    public void testRaff() {

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(new RaffleFactorEntity(1001L, 1001L));
        log.info("抽奖结果：{}", JSONUtil.toJsonStr(raffleAwardEntity));
    }





}