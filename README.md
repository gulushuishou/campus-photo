# 校园约拍平台

基于 SpringBoot + Vue 的校园约拍平台设计与实现（毕业设计项目）

## 项目简介

面向高校学生打造的线上约拍结伴平台，解决校园内约拍需求与摄影师供给之间的信息不对称、信任缺失、沟通不畅三大核心问题。

## 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue3 + Vite + Element-Plus + Pinia + Axios + ECharts + STOMP |
| 后端 | SpringBoot 3 + MyBatis-Plus + Spring Security + JWT + WebSocket |
| 数据库 | MySQL 8 |

## 核心功能

1. 用户注册登录（JWT 认证 + 多角色权限控制）
2. 约拍需求发布与作品多图展示
3. 基于余弦相似度的智能推荐（含信用分权重）
4. 预约订单状态机（待确认→已确认→拍摄完成→已评价）
5. WebSocket 实时私信
6. 双向评价与信用积分体系
7. 管理端数据统计（ECharts）

## 目录结构

```
campus-photo/
├── backend/          # SpringBoot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/campus/
│       │   ├── controller/   # 接口层
│       │   ├── service/      # 业务层
│       │   ├── mapper/       # 数据访问层
│       │   ├── entity/       # 实体类
│       │   ├── dto/          # 数据传输对象
│       │   ├── config/       # 配置类
│       │   ├── security/     # 安全认证
│       │   └── common/       # 公共组件
│       └── resources/
│           ├── application.yml
│           └── sql/init.sql  # 数据库初始化脚本
└── frontend/         # Vue3 前端
    ├── package.json
    └── src/
        ├── api/      # 接口封装
        ├── stores/   # Pinia 状态管理
        ├── router/   # 路由
        ├── layout/   # 布局
        └── views/    # 页面
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/sql/init.sql
```

### 2. 启动后端

修改 `backend/src/main/resources/application.yml` 中的数据库账号密码，然后：

```bash
cd backend
mvn spring-boot:run
# 或使用 Maven Wrapper：./mvnw spring-boot:run
```

后端启动后监听 http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 http://localhost:5173

## 测试账号

| 角色 | 用户名 | 说明 |
|------|--------|------|
| 管理员 | admin | 管理后台 |
| 摄影师 | photographer | 作品发布、接单 |
| 普通用户 | user | 发布需求、下单 |

> 注：初始密码需通过后端注册接口生成（BCrypt 加密），或替换 init.sql 中的占位密码哈希。

## 核心亮点说明

### 余弦相似度推荐
- 标签词典固定维度（日系、古风、校园、韩系、清新、复古、街拍、夜景、情侣、毕业照）
- 需求标签 → 特征向量，摄影师作品标签 → 特征向量
- 相似度 = cos(A,B)，叠加信用分归一化权重
- 参考 `backend/src/main/java/com/campus/service/CosineSimilarityService.java`

### 订单状态机 + 乐观锁
- 状态流转：待确认 → 已确认 → 拍摄完成 → 已评价（+ 已取消分支）
- 并发控制：同一摄影师同一时段仅一个确认订单
- 参考 `backend/src/main/java/com/campus/service/OrderService.java`

### 敏感词过滤（DFA）
- 基于有限状态自动机，时间复杂度 O(n)
- 接入需求、作品、评价、私信
- 参考 `backend/src/main/java/com/campus/common/SensitiveWordFilter.java`

## 常见问题解决方案（FAQ）

### Q1：后端启动报 "Port 8080 already in use"
端口被占用。解决：换一个端口启动
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Q2：后端启动报 "Access denied for user 'root'"
数据库密码不对。检查 `backend/src/main/resources/application.yml` 里的 `password` 是否与 MySQL 实际密码一致。

### Q3：前端请求接口报 404 或跨域错误
确认后端已启动（http://localhost:8080），且前端通过 `npm run dev` 启动（走 Vite 代理），不要直接双击 index.html。

### Q4：头像/图片不显示
确认后端上传目录 `backend/upload/` 存在，且图片已上传成功。前端通过 Vite 代理访问 `/upload`，确保 vite.config.js 里配置了 `/upload` 代理。

### Q5：私信收不到实时消息
WebSocket 需要前后端都正常运行。检查浏览器控制台是否有 WebSocket 连接错误。两个账号测试时请用不同浏览器（避免 localStorage 互相覆盖）。

### Q6：数据库初始化失败（中文乱码或 Data too long）
导入脚本时加字符集参数：
```bash
mysql -u root -p --default-character-set=utf8mb4 < backend/src/main/resources/sql/init.sql
```

### Q7：端口被 WorkBuddy/IDE 环境变量覆盖
某些开发环境注入 `SERVER_PORT` 环境变量覆盖配置。启动后端时显式指定端口即可（见 Q1）。

## 文档

- `docs/系统设计文档.md`：用例图、ER 图、类图、顺序图、接口设计
- `docs/测试文档.md`：单元测试、功能测试、JMeter 压测方案

