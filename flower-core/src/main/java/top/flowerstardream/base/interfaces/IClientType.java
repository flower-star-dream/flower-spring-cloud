package top.flowerstardream.base.interfaces;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 花海
 * @Date: 2026/02/09/10:39
 * @Description: 客户端类型接口
 */
public interface IClientType {
    Integer getCode();
    String getName();
}