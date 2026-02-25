package com.hutu.types.common;

public class Constants {

    // 策略奖品缓存key
    public final static String STRATEGY_AWARD_KEY_WITH_RATE = "big_market_strategy_award_rate_key_";
    // 策略权重缓存key
    public final static String STRATEGY_WEIGHT_KEY = "big_market_strategy_weight_key_";
    // 策略黑名单缓存key
    public final static String STRATEGY_BLACK_KEY = "big_market_strategy_black_key_";
    public final static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";

    // 商品规则模型分隔符
    public final static String RULE_MODEL_SPLIT = "#";




    // --------------------------------------------抽奖策略规则类型----------------------------


    // 权重策略
    public final static String RULE_WEIGHT = "RULE_WEIGHT";
    // 黑名单策略
    public final static String RULE_BLACKLIST = "RULE_BLACKLIST";
    // 兜底策略(业务使用，数据库并无该类型）
    public final static String RULE_DEFAULT = "RULE_DEFAULT";



    // --------------------------------------------规则树业务key----------------------------
    public final static String RULE_TREE_KEY = "big_market_rule_tree_key_";



    // --------------------------------------------奖品库存key----------------------------
    public final static String STRATEGY_AWARD_STOCK_KEY_TEMPLATE = "big_market_award_stock_key_%s_%s";








    // --------------------------------------------抽奖策略规则触发条件----------------------------


    // 触发条件类型 最小值
    public final static String MIN_SCORE = "MIN_SCORE";

    // 触发条件类型 最大值
    public final static String MAX_SCORE = "MAX_SCORE";




    // --------------------------------------------规则树节点标志----------------------------
    // todo 标记规则是树节点
    public final static String RULE_TREE_NODE_FLAG_TRUE = "RULETREE";







}
