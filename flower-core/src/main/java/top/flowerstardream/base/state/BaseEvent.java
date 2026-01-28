package top.flowerstardream.base.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 花海
 * @Date: 2025/12/15/22:08
 * @Description: 基础事件枚举
 */
@Getter
@AllArgsConstructor
public enum BaseEvent implements IBaseEvent<String> {
    START_OR_STOP("START_OR_STOP", "启用或禁用");

    private final String code;
    private final String name;

    private static final Map<String, BaseEvent> CODE_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(BaseEvent::getCode, Function.identity()));

    public static BaseEvent fromCode(String code) {
        return CODE_MAP.get(code);
    }
}
