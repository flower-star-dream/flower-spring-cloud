package top.flowerstardream.base.constant;

/**
 * @Author: 花海
 * @Date: 2026/02/10/01:18
 * @Description: 基础 Redis key 常量
 */
public class BaseRedisKeyConstant {
    // 验证码存储
    public static final String KEY_CODE = "verify:code:%s:%s";
    // 发送间隔限制
    public static final String KEY_INTERVAL = "verify:interval:%s:%s";
    // 每日发送次数（带日期）
    public static final String KEY_DAILY = "verify:daily:%s:%s:%s";
    // 验证错误次数
    public static final String KEY_ERROR = "verify:error:%s:%s";
}
