# Flower Spring Cloud

一个自定义的 Spring Cloud 微服务基础框架，提供统一的依赖管理、自动配置和核心功能支持。

## 项目简介

Flower Spring Cloud 是一个为微服务架构设计的基础框架起步依赖，整合了 Spring Cloud、Spring Cloud Alibaba、MyBatis-Plus 等主流技术栈，提供标准化的开发规范和基础设施支持。

## 功能特性

- **统一依赖管理**: 集中管理 Spring Cloud、Spring Boot、MyBatis-Plus 等组件版本
- **基础实体封装**: 提供包含通用字段（ID、时间戳、逻辑删除等）的基础实体类
- **统一响应格式**: 标准化的 API 响应封装（Result、PageResult）
- **异常处理体系**: 业务异常（BizException）和全局异常处理器
- **状态机框架**: 基于 Spring State Machine 的状态流转引擎
- **自动分页查询**: 支持动态查询条件构建的增强型 Mapper
- **线程池配置**: 预置业务、通用、消息三种线程池，支持线程上下文传递
- **JWT 认证支持**: 内置 JWT 令牌配置和解析工具
- **文件存储服务**: 支持 MinIO 对象存储的文件上传下载接口

## 技术栈

| 技术 | 版本 |
|------|------|
| JDK | 17 |
| Spring Boot | 3.4.12 |
| Spring Cloud | 2023.0.6 |
| Spring Cloud Alibaba | 2023.0.3.3 |
| MyBatis Plus | 3.5.5 |
| Seata | 2.5.0 |
| RocketMQ | 5.3.2 |
| Knife4j | 4.5.0 |
| JWT | 0.12.6 |
| Fastjson2 | 2.0.49 |
| Hutool | 5.8.40 |
| MinIO | 8.5.10 |
| Spring State Machine | 4.0.0 |

## 模块结构

```
flower-spring-cloud
├── flower-core                          # 核心功能模块
├── flower-spring-cloud-autoconfigure    # 自动配置模块
├── flower-spring-cloud-starter          # Starter 起步依赖模块
└── flower-spring-cloud-dependencies     # 依赖管理模块 (BOM)
```

### 模块说明

#### flower-core
核心功能模块，包含：
- 基础实体类（BaseEO）
- 增强型 Mapper（BaseMapperX）
- 统一响应结果（Result、PageResult）
- 异常处理体系
- 状态机框架
- 线程池配置
- 工具类

#### flower-spring-cloud-autoconfigure
自动配置模块，基于 Spring Boot 自动配置机制，自动装配核心功能。

#### flower-spring-cloud-starter
Starter 起步依赖模块，业务项目直接引入此模块即可获得完整的基础能力。

#### flower-spring-cloud-dependencies
依赖管理模块（BOM），统一管理所有子模块和第三方依赖的版本。

## 快速开始

### 1. 引入依赖

在业务项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>top.flowerstardream.base</groupId>
    <artifactId>flower-spring-cloud-starter</artifactId>
    <version>release-1.0.0</version>
</dependency>
```

或者通过 BOM 管理依赖：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>top.flowerstardream.base</groupId>
            <artifactId>flower-spring-cloud-dependencies</artifactId>
            <version>release-1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 基础实体类

业务实体继承 `BaseEO`：

```java
@Data
@TableName("user")
public class User extends BaseEO {
    private String username;
    private String email;
}
```

`BaseEO` 提供以下通用字段：
- `id`: 主键（雪花算法生成）
- `createTime`: 创建时间
- `updateTime`: 更新时间
- `createPersonId`: 创建人 ID
- `updatePersonId`: 更新人 ID
- `deleted`: 逻辑删除标记
- `version`: 乐观锁版本号

### 3. 基础 Mapper

```java
@Mapper
public interface UserMapper extends BaseMapperX<User> {
}
```

使用 `BaseMapperX` 的自动分页功能：

```java
// 自动构建查询条件并分页
Page<User> page = userMapper.autoPage(userQueryDTO, true);
```

### 4. 基础 Service

```java
public interface UserService extends IBaseService<User> {
}

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
}
```

### 5. 统一响应

Controller 层返回统一格式：

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @GetMapping("/page")
    public Result<PageResult<User>> page(UserQueryDTO dto) {
        Page<User> page = userService.autoPage(dto, true);
        return Result.success(PageResult.of(page));
    }
}
```

## 核心功能详解

### 状态机

基于 Spring State Machine 实现的状态流转框架。

#### 定义状态和事件

```java
public enum OrderStatus implements IBaseState<Integer> {
    PENDING_PAYMENT(1, "待付款"),
    PAID(2, "已付款"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成");

    private final Integer code;
    private final String name;

    // getter...
}

public enum OrderEvent implements IBaseEvent<String> {
    PAY("PAY", "支付"),
    SHIP("SHIP", "发货"),
    COMPLETE("COMPLETE", "完成");

    private final String code;
    private final String name;

    // getter...
}
```

#### 配置状态路由

```java
@Service
public class OrderRouter extends BaseRouter<Order, OrderMapper> implements IStateRouter<OrderStatus, OrderEvent, Order, OrderDTO> {

    @Override
    public Map<OrderStatus, Map<OrderEvent, OrderStatus>> getStateEventTargetConfig() {
        Map<OrderStatus, Map<OrderEvent, OrderStatus>> config = new HashMap<>();
        // 待付款 -> 支付 -> 已付款
        config.put(OrderStatus.PENDING_PAYMENT, Map.of(OrderEvent.PAY, OrderStatus.PAID));
        // 已付款 -> 发货 -> 已发货
        config.put(OrderStatus.PAID, Map.of(OrderEvent.SHIP, OrderStatus.SHIPPED));
        // 已发货 -> 完成 -> 已完成
        config.put(OrderStatus.SHIPPED, Map.of(OrderEvent.COMPLETE, OrderStatus.COMPLETED));
        return config;
    }

    @Override
    public Map<OrderEvent, Function<OrderDTO, OrderStatus>> getEventDispatcher() {
        Map<OrderEvent, Function<OrderDTO, OrderStatus>> dispatcher = new HashMap<>();
        dispatcher.put(OrderEvent.PAY, this::handlePay);
        dispatcher.put(OrderEvent.SHIP, this::handleShip);
        dispatcher.put(OrderEvent.COMPLETE, this::handleComplete);
        return dispatcher;
    }

    private OrderStatus handlePay(OrderDTO dto) {
        // 处理支付逻辑
        return OrderStatus.PAID;
    }

    // 其他事件处理...
}
```

#### 触发状态流转

```java
@Service
public class OrderService {
    @Autowired
    private StateMachine<OrderStatus, OrderEvent, Order, OrderDTO> stateMachine;

    public void pay(Long orderId) {
        Order order = getById(orderId);
        OrderDTO dto = new OrderDTO();
        dto.setId(orderId);

        OrderStatus newStatus = stateMachine.fire(
            OrderStatus.valueOf(order.getStatus()),
            OrderEvent.PAY,
            dto
        );

        // 更新订单状态
        order.setStatus(newStatus.getCode());
        updateById(order);
    }
}
```

### 动态查询条件

使用 `@Query` 注解定义查询条件：

```java
@Data
public class UserQueryDTO {

    @Query.Condition(type = Query.Condition.Type.LIKE)
    private String username;

    @Query.Condition(type = Query.Condition.Type.GE, field = "create_time")
    private LocalDateTime startTime;

    @Query.Condition(type = Query.Condition.Type.LE, field = "create_time")
    private LocalDateTime endTime;

    @Query.Condition(type = Query.Condition.Type.IN)
    private List<Integer> statusList;

    @Query.Ignore
    private String ignoreField;
}
```

支持的查询类型：
- `EQ`: 等于
- `NE`: 不等于
- `LIKE`: 模糊查询
- `GT`: 大于
- `GE`: 大于等于
- `LT`: 小于
- `LE`: 小于等于
- `IN`: IN 查询
- `NOT_IN`: NOT IN 查询
- `BETWEEN`: BETWEEN 查询
- `NOT_BETWEEN`: NOT BETWEEN 查询

## 配置说明

### JWT 配置

```yaml
hcd:
  jwt:
    employee-secret-key: your-employee-secret-key
    employee-ttl: 7200000  # 员工端令牌有效期（毫秒）
    user-secret-key: your-user-secret-key
    user-ttl: 7200000      # 用户端令牌有效期（毫秒）
```

### MinIO 配置

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: your-access-key
  secret-key: your-secret-key
  bucket-name: your-bucket
```

### 线程池配置

```yaml
thread-pool:
  business:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 100
    keep-alive-seconds: 60
    rejection-policy: CALLER_RUNS
```

## 版本管理

项目使用 `${revision}` 占位符进行版本管理，支持 CI/CD 场景：

```xml
<properties>
    <revision>release-1.0.0</revision>
</properties>
```

使用 flatten-maven-plugin 插件在构建时解析版本占位符，生成扁平化的 POM 文件。

## 项目状态

**注意**: 该项目目前处于开发阶段，部分功能尚未完全实现或可能在未来版本中调整。

## 许可证

[Apache License 2.0](LICENSE)
