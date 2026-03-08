package top.flowerstardream.base.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import top.flowerstardream.base.annotation.AutoConfigProperties;

import java.util.Arrays;

/**
 * 属性条件类
 * @author 花海
 * @date 2026/03/08/16:01
 */
public class OnPropertiesEnabledCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String prefix = (String) metadata.getAnnotationAttributes(AutoConfigProperties.class.getName()).get("prefix");
        ConfigurableEnvironment env = (ConfigurableEnvironment) context.getEnvironment();

        boolean match = env.getPropertySources().stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .flatMap(ps -> Arrays.stream(((EnumerablePropertySource<?>) ps).getPropertyNames()))
                .anyMatch(name -> name.startsWith(prefix + "."));

        return match ? ConditionOutcome.match() : new ConditionOutcome(false, "No " + prefix);
    }
}