package top.flowerstardream.base.state;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 花海
 * @Date: 2025/12/15/22:11
 * @Description: 基础状态枚举
 */
@Getter
@AllArgsConstructor
public enum BaseStatus implements IBaseState<Integer>, IEnum<Integer> {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private final Integer code;
    private final String name;

    private static final Map<Integer, BaseStatus> CODE_MAP = Arrays.stream(values())
        .collect(Collectors.toMap(BaseStatus::getCode, Function.identity()));

    public static BaseStatus valueOf(Integer code) {
        return CODE_MAP.get(code);
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    @Override
    public Integer getValue() {
        return this.code;
    }
}
