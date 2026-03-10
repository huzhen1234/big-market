package com.hutu.trigger.http;

import com.alibaba.fastjson2.JSON;
import com.hutu.api.IRaffleService;
import com.hutu.domain.strategy.service.armory.IStrategyArmory;
import com.hutu.types.enums.ResponseCode;
import com.hutu.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 营销抽奖服务
 */
@Slf4j
@RestController("/api/${app.config.api-version}/raffle/")
@CrossOrigin("${app.config.cross-origin}")
public class RaffleController implements IRaffleService {

    @Resource
    private IStrategyArmory strategyArmory;

    /**
     * 策略装配，将策略信息装配到缓存中
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    @GetMapping("strategy_armory")
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {
        try {
            log.info("抽奖策略装配开始 strategyId：{}", strategyId);
            boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(armoryStatus)
                    .build();
            log.info("抽奖策略装配完成 strategyId：{} response: {}", strategyId, JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("抽奖策略装配失败 strategyId：{}", strategyId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}