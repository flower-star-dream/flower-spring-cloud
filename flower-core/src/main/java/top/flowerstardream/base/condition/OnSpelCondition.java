package top.flowerstardream.base.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import top.flowerstardream.base.annotation.ConditionalOnSpel;

import java.util.Map;

/**
 * @Author: 花海
 * @Date: 2026/03/21/23:26
 * @Description: SpEL表达式条件类
 */
public class OnSpelCondition extends SpringBootCondition {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext context = new StandardEvaluationContext();

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext ctx, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnSpel.class.getName());
        String expression = null;
        if (attrs != null) {
            expression = (String) attrs.get("value");
        }

        // 创建可以访问 Environment 的上下文
        Environment env = ctx.getEnvironment();
        context.setVariable("env", env);
        context.setVariable("appName", env.getProperty("spring.application.name"));
        context.setVariable("profiles", env.getActiveProfiles());
        // 解析 SpEL
        boolean match = false;
        if (expression != null) {
            match = Boolean.TRUE.equals(parser.parseExpression(expression).getValue(context, Boolean.class));
        }

        return new ConditionOutcome(match, "SpEL: " + expression + " = " + match);
    }
}
