# 【Server】YunaNexusCore - 系统核心模块后端

## 选用技术栈
本系统选用技术：
- 应用开发：SpringBoot
- 微服务治理：SpringCloudAlibaba
- API 网关：Gateway
- 服务注册配置：Nacos
- 数据存储：MySQL+MyBatisPlus+Redis
- 熔断限流：Sentinel
- 消息队列：RocketMQ
- 分布式事务：Seata
- 链路追踪：SkyWalking+Elasticsearch+Prometheus+Node Exporter+AlertManager
- 任务调度：Spring Scheduler
- 邮箱告警：SpringBootStarterMail

## 配置说明
- 时区：Asia/Shanghai(UTC+8)

## 模块划分
### 通用模块：服务共用部分
- 核心公共组件：yuna-common-core
- Web 相关组件：yuna-common-web
- 安全相关组件：yuna-common-security
- 数据库组件：yuna-common-database
- 缓存组件：yuna-common-redis
- 消息队列组件：yuna-common-rocketmq

### 业务服务模块：具体服务
- 用户服务：yuna-user-service
- 订单服务：yuna-order-service(暂时不做或绕过逻辑直接返回结果,后续再扩展)
- 商品服务：yuna-product-service
- 支付服务：yuna-payment-service(暂时不做或绕过逻辑直接返回结果,后续再扩展)
- 文件服务：yuna-file-service
- 通知服务：yuna-notification-service
- 控制台服务：yuna-console-service(系统信息管理后台)
- API 网关：yuna-gateway
- 其它单体服务：
    - YunaNexus-OsuCompetitionCenter
    - YunaNexus-NBLOG
    - YunaNexus-MaiTTx

## 注意事项
### 密钥安全
- 密钥：yuna-common-security 模块中的 JwtProperties 类中的 secret 属性为开发者自己填写的值，由YunaNexusCore\yuna-user-service\src\main\resources\application.yml配置文件中的 yuna.jwt.secret 属性指定，在生产环境中请自行替换为512位的随机字符串。
- 密钥：yuna-file-service 模块中的 FileStorageProperties 类中的 uuidSecret 属性为开发者自己填写的值，在生产环境中请自行替换。