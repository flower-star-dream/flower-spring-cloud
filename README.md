# Flower Spring Cloud

微服务基础框架，提供自主研发的状态机引擎、Bean全生命周期追踪、统一异常处理、基础实体类等核心能力。

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/flower-star-dream/flower-spring-cloud/blob/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-brightgreen.svg)](https://spring.io/projects/spring-cloud)
[![JDK](https://img.shields.io/badge/JDK-17+-orange.svg)](https://openjdk.org/)

## 项目简介

Flower Spring Cloud 是一个基于 Spring Boot 3.x 和 Spring Cloud 的微服务基础框架，旨在为微服务架构提供统一的基础能力支撑。框架采用模块化设计，核心功能自主研发，不依赖外部状态机框架。

### 核心特性

- **自主研发状态机引擎** - 不依赖 Spring State Machine 等外部框架，自研轻量级状态机实现
- **Bean 全生命周期追踪** - 从 Bootstrap 到启动完成的 8 阶段完整追踪
- **丰富的基础实体类** - 提供多种审计字段组合的基础实体
- **统一异常处理** - 标准化业务异常体系
- **自动配置** - 开箱即用，零配置启动

## 技术栈

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.5.12 |
| Spring Cloud | 2025.0.1 |
| JDK | 17+ |
| MyBatis Plus | 3.5.15 |
| Hutool | 5.8.40 |

## 模块结构

```
flower-spring-cloud
├── flower-core                          # 核心模块
│   ├── annotation                       # 注解
│   │   ├── AutoStateMachine            # 自动注入状态机
│   │   ├── TraceLifecycle              # 生命周期追踪
│   │   ├── StateRouter                 # 状态路由标记
│   │   └── ...
│   ├── beans/factory                    # Bean工厂
│   │   ├── StateMachineFactory         # 状态机工厂
│   │   ├── StateMachineFactoryBean     # 状态机 FactoryBean
│   │   └── StateMachineBeanRegistrar   # 状态机 Bean 注册器
│   ├── bo/eo                            # 基础实体类
│   │   ├── BaseEO                      # 基础实体（仅ID）
│   │   ├── AuditBaseEO                 # 审计实体（ID+时间+操作人）
│   │   ├── TimeAuditBaseEO             # 时间审计实体（ID+时间）
│   │   ├── BizBaseEO                   # 业务实体（ID+状态）
│   │   └── LogBaseEO                   # 日志实体
│   ├── listener                         # 监听器
│   │   └── SpringBootStartupTracer     # Bean全生命周期追踪器
│   ├── properties                       # 配置属性
│   │   └── BeanTracerProperties        # 追踪器配置
│   ├── state                            # 状态机引擎
│   │   ├── StateMachine                # 状态机核心类
│   │   ├── IStateRouter                # 状态路由接口
│   │   ├── IBaseState                  # 状态接口
│   │   └── IBaseEvent                  # 事件接口
│   └── ...
├── flower-spring-cloud-autoconfigure    # 自动配置模块
└── flower-spring-cloud-starter          # 起步依赖模块
```

## 核心功能详解

### 1. 自主研发状态机引擎

状态机引擎完全自主研发，不依赖 Spring State Machine 等外部框架，核心组件包括：

#### 核心接口与类

| 组件 | 说明 |
|------|------|
| `StateMachine<S, E, D>` | 状态机核心类，负责状态转换执行 |
| `IStateRouter<S, E, D>` | 状态路由接口，业务实现此接口定义状态流转规则 |
| `IBaseState<C>` | 状态接口，状态枚举需实现此接口 |
| `IBaseEvent<C>` | 事件接口，事件枚举需实现此接口 |
| `StateMachineFactory` | 状态机工厂，负责创建和管理状态机实例 |
| `StateMachineBeanRegistrar` | Bean 注册器，扫描并自动注册状态机 |

#### 使用示例

**定义状态枚举：**

```java
public enum OrderStatus implements IBaseState<Integer> {
    PENDING_PAYMENT(1, "待支付"),
    PAID(2, "已支付"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String name;

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getName() { return name; }
}
```

**定义事件枚举：**

```java
public enum OrderEvent implements IBaseEvent<Integer> {
    PAY(1, "支付"),
    SHIP(2, "发货"),
    COMPLETE(3, "完成"),
    CANCEL(4, "取消");

    private final Integer code;
    private final String name;

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getName() { return name; }
}
```

**实现状态路由：**

```java
@Service
@StateRouter
public class OrderStateRouter implements IStateRouter<OrderStatus, OrderEvent, OrderEO> {

    @Override
    public Map<OrderStatus, Map<OrderEvent, OrderStatus>> getStateEventTargetConfig() {
        Map<OrderStatus, Map<OrderEvent, OrderStatus>> config = new HashMap<>();

        // 待支付 -> 支付 -> 已支付
        config.put(PENDING_PAYMENT, Map.of(PAY, PAID));
        // 已支付 -> 发货 -> 已发货
        config.put(PAID, Map.of(SHIP, SHIPPED));
        // 已发货 -> 完成 -> 已完成
        config.put(SHIPPED, Map.of(COMPLETE, COMPLETED));

        return config;
    }

    @Override
    public Map<OrderEvent, Function<StateRouteParams, OrderStatus>> getEventDispatcher() {
        Map<OrderEvent, Function<StateRouteParams, OrderStatus>> dispatcher = new HashMap<>();

        dispatcher.put(PAY, params -> {
            // 执行业务逻辑
            return PAID;
        });

        return dispatcher;
    }
}
```

**触发状态转换：**

```java
@Service
public class OrderService {

    @Autowired
    private StateMachine<OrderStatus, OrderEvent, OrderEO> orderMachine;

    public void payOrder(Long orderId) {
        OrderEO order = getById(orderId);

        // 触发状态转换
        OrderStatus newStatus = orderMachine.fire(
            OrderStatus.valueOf(order.getStatus()),
            OrderEvent.PAY,
            new StateRouteParams(orderId, order)
        );

        // 更新状态
        order.setStatus(newStatus.getCode());
        updateById(order);
    }
}
```

### 2. Bean 全生命周期追踪器

`SpringBootStartupTracer` 是一个强大的 Bean 生命周期追踪组件，实现了从 Bootstrap 到应用启动完成的 8 个阶段完整追踪：

#### 实现的扩展点

| 阶段 | 接口 | 说明 |
|------|------|------|
| 1 | `BootstrapRegistryInitializer` | 极早期（SpringApplication 创建前） |
| 2 | `ApplicationListener` | 环境准备 & 上下文事件 |
| 3 | `BeanDefinitionRegistryPostProcessor` | Bean 定义注册阶段 |
| 4 | `BeanFactoryPostProcessor` | Bean 工厂后置处理 |
| 5 | `InstantiationAwareBeanPostProcessor` | Bean 实例化前后 |
| 6 | `BeanPostProcessor` | Bean 初始化前后 |
| 7 | `SmartInitializingSingleton` | 所有单例就绪后 |
| 8 | `CommandLineRunner` | 应用启动完成前 |

#### 配置属性

```yaml
bean:
  tracer:
    enabled: true                      # 是否启用
    base-packages:                     # 追踪的包路径
      - com.example.service
    interface-classes:                 # 追踪的接口
      - com.example.StateMachine
    annotation-classes:                # 追踪的注解
      - org.springframework.stereotype.Service
    name-patterns:                     # 名称匹配模式
      - Machine
      - Router
    verbose: false                     # 是否打印所有 Bean
```

#### 输出示例

```
╔════════════════════════════════════════════════════════════════╗
║                  应用启动完成 - Bean生命周期报告                   ║
╠════════════════════════════════════════════════════════════════╣
║ 启动总耗时: 3.21s                                               ║
║ 追踪Bean数: 15 个                                               ║
╠════════════════════════════════════════════════════════════════╣
║ Bean名称                          状态     耗时    来源           ║
╠════════════════════════════════════════════════════════════════╣
║ orderStateMachine                 READY    15ms   自动配置        ║
║ userService                       READY    23ms   组件扫描        ║
╚════════════════════════════════════════════════════════════════╝
```

### 3. 基础实体类

框架提供多种基础实体类，满足不同场景的审计需求：

| 实体类 | 字段 | 适用场景 |
|--------|------|----------|
| `BaseEO` | id | 简单实体，仅需主键 |
| `TimeAuditBaseEO` | id + createTime + updateTime | 需要时间审计 |
| `AuditBaseEO` | id + 时间 + createPersonId + updatePersonId | 需要完整审计信息 |
| `BizBaseEO` | id + status | 带状态的实体 |
| `LogBaseEO` | id + 日志相关字段 | 日志记录 |

使用示例：

```java
@Data
@TableName("t_order")
public class OrderEO extends AuditBaseEO {
    private String orderNo;
    private BigDecimal amount;
    private Integer status;
}
```

## 快速开始

### 1. 添加依赖

在业务项目的 `pom.xml` 中引入 BOM：

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

添加起步依赖：

```xml
<dependencies>
    <dependency>
        <groupId>top.flowerstardream.base</groupId>
        <artifactId>flower-spring-cloud-starter</artifactId>
    </dependency>
</dependencies>
```

### 2. 开始使用

参考【核心功能详解】章节，开始使用状态机、基础实体类等功能。

## 构建安装

```bash
# 克隆项目
git clone https://github.com/flower-star-dream/flower-spring-cloud.git

# 进入项目目录
cd flower-spring-cloud

# 编译安装到本地 Maven 仓库
mvn clean install
```

## 项目信息

| 项目 | 地址 |
|------|------|
| 主页 | https://github.com/flower-star-dream/flower-spring-cloud |
| 依赖管理 | https://github.com/flower-star-dream/flower-spring-cloud-dependencies |
| 问题反馈 | https://github.com/flower-star-dream/flower-spring-cloud/issues |

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

```
Copyright 2026 FlowerStarDream(花海)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

## 致谢

感谢所有为本项目做出贡献的开发者！
