package top.flowerstardream.base.resolver;

/**
 * @Author: 花海
 * @Date: 2026/03/10/22:21
 * @Description: 用户ID转名称注解解析器提供者
 */
public interface UserNameResolverProvider {
    UserNameResolver getResolver();
}