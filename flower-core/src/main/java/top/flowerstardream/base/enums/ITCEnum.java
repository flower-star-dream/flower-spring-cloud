package top.flowerstardream.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 花海
 * @Date: 2026/03/18/03:07
 * @Description: 信息发送渠道类型枚举类
 */
@Getter
@AllArgsConstructor
public enum ITCEnum {
    EMAIL("email", "邮箱"),
    SMS("sms", "短信");

    private final String type;
    private final String desc;

    private static final Map<String, ITCEnum> CODE_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(ITCEnum::getType, Function.identity()));

    public static ITCEnum fromCode(String code) {
        return CODE_MAP.get(code);
    }
}
