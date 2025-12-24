package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hutu.infrastructure.persistent.po.StrategyGuarantee;
import generator.service.StrategyGuaranteeService;
import com.hutu.infrastructure.persistent.mapper.StrategyGuaranteeMapper;
import org.springframework.stereotype.Service;

/**
* @author huzhen
* @description 针对表【strategy_guarantee(策略保底配置表)】的数据库操作Service实现
* @createDate 2025-12-24 21:42:35
*/
@Service
public class StrategyGuaranteeServiceImpl extends ServiceImpl<StrategyGuaranteeMapper, StrategyGuarantee>
    implements StrategyGuaranteeService{

}




