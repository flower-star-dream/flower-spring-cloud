package top.flowerstardream.base.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.flowerstardream.base.resolver.UserNameResolver;
import top.flowerstardream.base.resolver.UserNameResolverProvider;
import top.flowerstardream.base.result.Result;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author: 花海
 * @Date: 2026/03/10/22:21
 * @Description: 用户ID转名称注解切面
 */
@Aspect
@Slf4j
public class UserNameConvertAspect implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext ctx) {
        context = ctx;
    }

    private static UserNameResolverProvider getProvider() {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext 未初始化");
        }
        log.debug("获取 UserNameResolverProvider Bean");
        return context.getBean(UserNameResolverProvider.class);
    }
    
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object convert(ProceedingJoinPoint point) throws Throwable {
        log.debug("进入 UserNameConvertAspect");
        Object result = point.proceed();
        if (result == null) {
            return null;
        }

        Object data = unwrapResult(result);
        if (data == null) {
            return result;
        }

        List<Object> targets = flattenToList(data);
        if (!targets.isEmpty()) {
            List<Object> processedTargets = batchConvert(targets);
            // 如果有对象被转换为Map，需要更新Result中的数据
            if (processedTargets != targets) {
                return updateResultData(result, data, processedTargets);
            }
        }

        return result;
    }

    /**
     * 更新Result中的数据
     *
     * @param originalResult 原始Result对象
     * @param originalData   原始数据（从Result中提取的）
     * @param newData        处理后的数据列表
     * @return 更新后的Result
     */
    private Object updateResultData(Object originalResult, Object originalData, List<Object> newData) {
        // 如果原始数据是列表，直接替换
        if (originalData instanceof Collection) {
            return replaceResultData(originalResult, newData);
        }
        // 如果原始数据是数组
        if (originalData.getClass().isArray()) {
            return replaceResultData(originalResult, newData.toArray());
        }
        // 如果原始数据是PageResult或Page类型
        if (originalData.getClass().getName().contains("PageResult") ||
            originalData.getClass().getName().contains("Page")) {
            return replacePageResultData(originalResult, originalData, newData);
        }
        // 单对象，取列表第一个元素
        if (!newData.isEmpty()) {
            return replaceResultData(originalResult, newData.get(0));
        }
        return originalResult;
    }

    /**
     * 替换Result中的data字段
     */
    @SuppressWarnings("unchecked")
    private Object replaceResultData(Object result, Object newData) {
        if (result instanceof Result) {
            ((Result<Object>) result).setData(newData);
        }
        return result;
    }

    /**
     * 替换分页对象中的records字段
     */
    private Object replacePageResultData(Object result, Object pageData, List<Object> newRecords) {
        try {
            Method setRecords = pageData.getClass().getMethod("setRecords", List.class);
            setRecords.invoke(pageData, newRecords);
        } catch (Exception e) {
            log.debug("替换分页数据失败: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 判断类是否需要处理（类名以RES、EO结尾）
     */
    private boolean isConvertibleClass(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return false;
        }
        String className = clazz.getSimpleName();
        return className.endsWith("RES") || className.endsWith("EO");
    }

    /**
     * 获取对象中的目标字段值（createPersonId 或 updatePersonId）
     */
    private Object getIdFieldValue(Object target, String fieldName) {
        try {
            log.debug("获取对象{}的{}字段值", target, fieldName);
            Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
        } catch (Exception e) {
            log.debug("获取字段{}失败: {}", fieldName, e.getMessage());
        }
        return null;
    }

    /**
     * 设置对象中的目标字段值（createPerson 或 updatePerson）
     * 如果字段在原实体类中不存在，则将对象转换为Map并添加新字段
     *
     * @return 如果转换为Map则返回Map对象，否则返回null表示保持原对象
     */
    private Object setNameFieldValue(Object target, String targetFieldName, String value) {
        try {
            log.debug("设置对象{}的{}字段值为{}", target, targetFieldName, value);
            Field field = findField(target.getClass(), targetFieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
                // 字段存在，无需转换
                return null;
            }
            // 字段不存在，将对象转换为Map并添加新字段
            return convertToMapWithExtraField(target, targetFieldName, value);
        } catch (Exception e) {
            log.debug("设置字段{}失败: {}", targetFieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 将对象转换为Map，复制所有原字段并添加额外字段
     */
    private Map<String, Object> convertToMapWithExtraField(Object target, String fieldName, String value) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = target.getClass();

        // 复制原对象所有字段到Map
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    map.put(f.getName(), f.get(target));
                } catch (Exception e) {
                    log.debug("复制字段{}失败: {}", f.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }

        // 添加新字段
        map.put(fieldName, value);
        log.debug("对象{}转换为Map并添加字段{}", target.getClass().getSimpleName(), fieldName);
        return map;
    }

    /**
     * 解析响应结果，提取实际数据对象
     * 响应格式为 Result<T>，其中 T 是我们需要处理的数据
     */
    private Object unwrapResult(Object result) {
        if (result instanceof Result) {
            log.debug("获取响应结果中的数据对象");
            return ((Result<?>) result).getData();
        }
        return result;
    }
    
    /** 拍平为对象列表（支持单对象、List、Page等） */
    @SuppressWarnings("unchecked")
    private List<Object> flattenToList(Object data) {
        log.debug("拍平为对象列表");
        if (data instanceof Collection) {
            return new ArrayList<>((Collection<?>) data);
        }

        if (data.getClass().isArray()) {
            int len = Array.getLength(data);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(Array.get(data, i));
            }
            return list;
        }
        // PageResult<T> 类型处理
        if (data.getClass().getName().contains("PageResult")) {
            try {
                Method getRecords = data.getClass().getMethod("getRecords");
                List<Object> records = (List<Object>) getRecords.invoke(data);
                return records != null ? records : Collections.emptyList();
            } catch (Exception e) {
                log.debug("解析PageResult失败: {}", e.getMessage());
            }
        }
        // 分页对象（PageHelper/IPage等）
        if (data.getClass().getName().contains("Page")) {
            try {
                Method getRecords = data.getClass().getMethod("getRecords");
                List<Object> records = (List<Object>) getRecords.invoke(data);
                return records != null ? records : Collections.emptyList();
            } catch (Exception e) {
                // 不是标准Page，当作单对象处理
            }
        }
        // 单对象包装成List
        return Collections.singletonList(data);
    }

    /** 在类及其父类中查找字段 */
    private Field findField(Class<?> clazz, String fieldName) {
        log.debug("在类{}中查找字段{}", clazz, fieldName);
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    // ========== 批量转换核心逻辑 ==========

    /**
     * 批量转换，返回处理后的对象列表（可能包含Map替换原对象）
     */
    private List<Object> batchConvert(List<Object> targets) {
        log.debug("批量转换开始");
        // 收集所有ID
        Set<Long> allIds = new HashSet<>();
        // 存储需要转换的对象及其对应的ID字段信息
        List<ConvertInfo> convertInfos = new ArrayList<>();
        // 记录对象索引位置，用于后续替换
        Map<Object, Integer> targetIndexMap = new HashMap<>();

        for (int i = 0; i < targets.size(); i++) {
            Object target = targets.get(i);
            // 只处理类名以RES或EO结尾的对象
            if (!isConvertibleClass(target.getClass())) {
                continue;
            }

            targetIndexMap.put(target, i);

            // 处理 createPersonId
            Object createIdObj = getIdFieldValue(target, "createPersonId");
            if (createIdObj instanceof Number) {
                Long id = ((Number) createIdObj).longValue();
                allIds.add(id);
                convertInfos.add(new ConvertInfo(target, "createPerson", id));
            }

            // 处理 updatePersonId
            Object updateIdObj = getIdFieldValue(target, "updatePersonId");
            if (updateIdObj instanceof Number) {
                Long id = ((Number) updateIdObj).longValue();
                allIds.add(id);
                convertInfos.add(new ConvertInfo(target, "updatePerson", id));
            }
        }

        if (allIds.isEmpty()) {
            return targets;
        }

        // 统一解析
        UserNameResolver resolver = getProvider().getResolver();
        Map<Long, String> nameMap = resolver.resolve(allIds);

        // 回填用户名，处理字段不存在时转换为Map的情况
        Map<Object, Object> replacementMap = new HashMap<>(); // 原对象 -> 转换后的对象(Map或原对象)

        for (ConvertInfo info : convertInfos) {
            String name = nameMap.get(info.userId());
            if (name != null) {
                Object target = info.target();
                // 如果该对象已经被转换为Map，则从Map中获取
                if (replacementMap.containsKey(target)) {
                    target = replacementMap.get(target);
                }

                Object converted = setNameFieldValue(target, info.targetField(), name);

                // 如果返回了Map（字段不存在），记录替换关系
                if (converted != null) {
                    replacementMap.put(info.target(), converted);
                }
            }
        }

        // 如果需要替换，构建新的列表
        if (!replacementMap.isEmpty()) {
            List<Object> result = new ArrayList<>(targets);
            for (Map.Entry<Object, Object> entry : replacementMap.entrySet()) {
                Integer index = targetIndexMap.get(entry.getKey());
                if (index != null) {
                    result.set(index, entry.getValue());
                }
            }
            return result;
        }

        return targets;
    }

    /**
     * 转换信息记录
     */
    private record ConvertInfo(Object target, String targetField, Long userId) {
    }
}