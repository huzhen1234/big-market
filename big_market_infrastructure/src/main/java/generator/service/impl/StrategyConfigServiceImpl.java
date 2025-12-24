package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hutu.infrastructure.persistent.po.StrategyConfig;
import generator.service.StrategyConfigService;
import com.hutu.infrastructure.persistent.mapper.StrategyConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author huzhen
* @description 针对表【strategy_config(策略配置表)】的数据库操作Service实现
* @createDate 2025-12-24 21:38:14
*/
@Service
public class StrategyConfigServiceImpl extends ServiceImpl<StrategyConfigMapper, StrategyConfig>
    implements StrategyConfigService{

}




