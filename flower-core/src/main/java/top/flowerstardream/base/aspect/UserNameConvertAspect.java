package top.flowerstardream.base.aspect;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.flowerstardream.base.annotation.UserIdToName;
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
        return context.getBean(UserNameResolverProvider.class);
    }
    
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object convert(ProceedingJoinPoint point) throws Throwable {
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
            batchConvert(targets);
        }

        return result;
    }

    /**
     * 解析响应结果，提取实际数据对象
     * 响应格式为 Result<T>，其中 T 是我们需要处理的数据
     */
    private Object unwrapResult(Object result) {
        if (result instanceof Result) {
            return ((Result<?>) result).getData();
        }
        return result;
    }
    
    /** 拍平为对象列表（支持单对象、List、Page等） */
    @SuppressWarnings("unchecked")
    private static List<Object> flattenToList(Object data) {
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

    /** 获取类及其父类中所有带注解的字段 */
    private static List<Field> getAnnotatedFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(UserIdToName.class)
                    && !fieldNames.contains(field.getName())) {
                    field.setAccessible(true);
                    fields.add(field);
                    fieldNames.add(field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /** 反射获取字段值（支持多级，如 "user.createBy"） */
    private static Object getFieldValue(Object target, String fieldName) {
        if (target == null) {
            return null;
        }

        String[] paths = fieldName.split("\\.");
        Object current = target;

        for (String path : paths) {
            if (current == null) {
                return null;
            }
            try {
                Field field = findField(current.getClass(), path);
                if (field == null) {
                    return null;
                }
                field.setAccessible(true);
                current = field.get(current);
            } catch (Exception e) {
                return null;
            }
        }
        return current;
    }

    /** 反射设置字段值 */
    private static void setFieldValue(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置字段失败: " + field.getName(), e);
        }
    }

    /** 在类及其父类中查找字段 */
    private static Field findField(Class<?> clazz, String fieldName) {
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

    private void batchConvert(List<Object> targets) {
        // 收集所有ID和对应的字段映射
        Set<Long> allIds = new HashSet<>();
        Map<Object, List<Pair<Field, Long>>> targetFieldMap = new IdentityHashMap<>();

        for (Object target : targets) {
            List<Field> fields = getAnnotatedFields(target.getClass());
            if (fields.isEmpty()) {
                continue;
            }

            List<Pair<Field, Long>> fieldPairs = new ArrayList<>();
            for (Field field : fields) {
                UserIdToName anno = field.getAnnotation(UserIdToName.class);
                Object idObj = getFieldValue(target, anno.field());

                if (idObj instanceof Number) {
                    Long id = ((Number) idObj).longValue();
                    allIds.add(id);
                    fieldPairs.add(Pair.of(field, id));
                }
            }

            if (!fieldPairs.isEmpty()) {
                targetFieldMap.put(target, fieldPairs);
            }
        }

        if (allIds.isEmpty()) {
            return;
        }

        // 统一解析
        UserNameResolver resolver = getProvider().getResolver();
        Map<Long, String> nameMap = resolver.resolve(allIds);

        // 回填
        targetFieldMap.forEach((target, pairs) -> {
            for (Pair<Field, Long> pair : pairs) {
                String name = nameMap.get(pair.right());
                if (name != null) {
                    setFieldValue(target, pair.left(), name);
                }
            }
        });
    }

    /**
     * 简单Pair实现（避免引入Apache Commons）
     */
    private record Pair<L, R>(L left, R right) {
        static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair<>(left, right);
        }
    }
}