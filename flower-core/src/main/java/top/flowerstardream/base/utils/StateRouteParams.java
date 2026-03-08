package top.flowerstardream.base.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态路由参数容器
 * 用于承载任意数量、任意类型的参数
 * @Author: 花海
 * @Date: 2026/03/06/22:24
 */
public final class StateRouteParams {
    
    /** 存储路由参数的内部映射，键为参数名，值为参数对象 */
    private final Map<String, Object> params = new HashMap<>();

    /**
     * 添加一个路由参数
     * @param key 参数键
     * @param value 参数值
     * @return 当前实例，支持链式调用
     */
    public StateRouteParams addParam(String key, Object value) {
        params.put(key, value);
        return this;
    }
    
    /**
     * 获取指定键的参数值
     * @param key 参数键
     * @param <T> 期望返回值的类型
     * @return 转换后的参数值，若不存在则返回 null
     */
    public <T> T getParam(String key) {
        @SuppressWarnings("unchecked")
        T value = (T) params.get(key);
        return value;
    }
    
    /**
     * 获取指定键的参数值，若不存在则返回默认值
     * @param key 参数键
     * @param defaultValue 默认值
     * @param <T> 期望返回值的类型
     * @return 参数值或默认值
     */
    public <T> T getParam(String key, T defaultValue) {
        T value = getParam(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 检查是否包含指定键的参数
     * @param key 参数键
     * @return 如果包含返回 true，否则返回 false
     */
    public boolean contains(String key) {
        return params.containsKey(key);
    }
    
    /**
     * 获取当前参数的数量
     * @return 参数个数
     */
    public int size() {
        return params.size();
    }
    
    /**
     * 创建一个新的 StateRouteParams 实例
     * @return 新的实例
     */
    public static StateRouteParams create() {
        return new StateRouteParams();
    }
}
