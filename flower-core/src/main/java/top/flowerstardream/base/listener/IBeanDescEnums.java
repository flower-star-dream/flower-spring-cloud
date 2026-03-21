package top.flowerstardream.base.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 花海
 * @Date: 2026/03/21/02:34
 * @Description: Bean描述枚举接口，用于统一获取Bean的来源信息
 */
public interface IBeanDescEnums{
    static final Logger log = LoggerFactory.getLogger(IBeanDescEnums.class);

    List<String> getDesc();
    String getValue();

    Map<Class<?>, IBeanDescEnums[]> ALL_ENUMS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static <E extends Enum<E> & IBeanDescEnums> void registerEnum(Class<? extends Enum<?>> enumClass) {
        if (!ALL_ENUMS.containsKey(enumClass)) {
            try {
                E[] constants = (E[]) enumClass.getEnumConstants();
                if (constants != null) {
                    ALL_ENUMS.put(enumClass, constants);
                }
            } catch (Exception e) {
                log.error("[IBeanDescEnums] 注册枚举失败：{}", enumClass.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <E extends Enum<E> & IBeanDescEnums> E[] getAllEnums(Class<E> enumClass) {
        return (E[]) ALL_ENUMS.get(enumClass);
    }

    static List<Class<?>> getAllRegisteredEnumClasses() {
        return List.copyOf(ALL_ENUMS.keySet());
    }

     /**
     * 静态工具方法：根据desc查找对应的value
     * 
     * @param enumClass 枚举类Class（用于获取所有枚举实例）
     * @param desc 传入的描述字符串
     * @param <E> 枚举类型，必须实现IBeanDescEnums接口
     * @return 匹配到的value，未匹配返回null
     */
    static <E extends Enum<E> & IBeanDescEnums> String findValueByDesc(Class<E> enumClass, String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return null;
        }
        desc = desc.toLowerCase();
        for (E enumItem : enumClass.getEnumConstants()) {
            List<String> descList = enumItem.getDesc();
            if (descList != null) {
                for (String enumDesc : descList) {
                    enumDesc = enumDesc.toLowerCase();
                    if (desc.contains(enumDesc)) {
                        return enumItem.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 静态工具方法：根据desc查找枚举实例
     */
    static <E extends Enum<E> & IBeanDescEnums> E findByDesc(Class<E> enumClass, String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return null;
        }
        desc = desc.toLowerCase();
        for (E enumItem : enumClass.getEnumConstants()) {
            List<String> descList = enumItem.getDesc();
            if (descList != null) {
                for (String enumDesc : descList) {
                    enumDesc = enumDesc.toLowerCase();
                    if (desc.contains(enumDesc)) {
                        return enumItem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 全局查找：在所有已注册的枚举类中根据 desc 查找 value
     * @param desc 描述字符串
     * @return 匹配到的 value，未匹配返回 null
     */
    static String findValueByDescGlobal(String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return null;
        }
        desc = desc.toLowerCase();
        for (Map.Entry<Class<?>, IBeanDescEnums[]> entry : ALL_ENUMS.entrySet()) {
            for (IBeanDescEnums enumItem : entry.getValue()) {
                List<String> descList = enumItem.getDesc();
                if (descList != null) {
                    for (String enumDesc : descList) {
                        enumDesc = enumDesc.toLowerCase();
                        if (desc.contains(enumDesc)) {
                            return enumItem.getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 全局查找：在所有已注册的枚举类中根据 desc 查找枚举实例
     * @param desc 描述字符串
     * @return 匹配到的枚举实例，未匹配返回 null
     */
    static IBeanDescEnums findByDescGlobal(String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return null;
        }
        desc = desc.toLowerCase();
        for (Map.Entry<Class<?>, IBeanDescEnums[]> entry : ALL_ENUMS.entrySet()) {
            for (IBeanDescEnums enumItem : entry.getValue()) {
                List<String> descList = enumItem.getDesc();
                if (descList != null) {
                    for (String enumDesc : descList) {
                        enumDesc = enumDesc.toLowerCase();
                        if (desc.contains(enumDesc)) {
                            return enumItem;
                        }
                    }
                }
            }
        }
        return null;
    }
}
