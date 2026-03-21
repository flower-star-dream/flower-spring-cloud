package top.flowerstardream.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 花海
 * @Date: 2026/03/20/20:19
 * @Description: Bean追踪属性配置
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "bean.tracer")
public class BeanTracerProperties {
     /** 是否启用 */
    private boolean enabled = true;
    /** 扫描的基础包路径 */
    private List<String> basePackages = new ArrayList<>();
    /** 追踪的接口类名（全限定名） */
    private List<String> interfaceClasses = new ArrayList<>();
    /** 追踪的注解类名（全限定名） */
    private List<String> annotationClasses = new ArrayList<>();
    /** Bean名称包含这些字符串也追踪（兜底） */
    private List<String> namePatterns = new ArrayList<>();
    /** 是否打印所有Bean（调试用） */
    private boolean verbose = false;
}