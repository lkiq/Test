# AI 智能求职辅导平台

基于 DeepSeek 大模型的 AI 智能求职辅导平台 — 为求职者提供从能力测评到岗位匹配、学习规划、简历优化、模拟面试的全流程智能服务。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + ECharts + Pinia + Vue Router + Axios + TypeScript |
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5 + JWT (JJWT 0.12) + BCrypt |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| AI | DeepSeek API |
| 部署 | Docker Compose + Nginx + JDK 17 |

## 功能模块

### 学生端
- **求职画像**：教育背景、技能标签、求职偏好
- **能力测评**：五维测评（编程能力/逻辑推理/产品思维/技术素养/沟通表达）
- **AI职业方向探索**：基于画像和测评的智能方向推荐
- **岗位匹配推荐**：技能匹配(50%) + 测评适配(30%) + 城市薪资(20%)
- **能力差距分析**：雷达图双线对比 + 差距矩阵 + 行动建议
- **学习路径规划**：四阶段学习（基础→框架→项目→面试）
- **简历智能优化**：上传简历 → AI分析 → 四维评分 → 优化建议
- **AI模拟面试**：多轮问答 → 追问 → 五维评估报告
- **学习进度展示**：任务完成率、技能掌握度、成长趋势

### 企业端 (HR)
- **项目需求解析**：项目描述 → AI拆解岗位和技能
- **候选人推荐**：加权评分排序 + 推荐理由

### 管理端
- **用户管理**：查询/启用/禁用/重置密码
- **技能词典**：增删改查技能标签
- **运营看板**：用户数/测评数/匹配数等核心指标

### 智能客服
- FAQ关键词匹配优先 → AI兜底回答

## 快速启动

### 方式一：Docker Compose 一键部署

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 DeepSeek API Key

# 2. 构建后端 jar（安装 Maven 和 JDK 17）
cd backend
mvn clean package -DskipTests
cd ..

# 3. 一键启动
docker-compose up -d

# 4. 访问 http://localhost
```

### 方式二：本地开发

**后端**：
```bash
cd backend
# MySQL 需先创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS career_platform"
# 执行初始化脚本
mysql -u root -p career_platform < src/main/resources/db/init.sql
# 启动
mvn spring-boot:run
```

**前端**：
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 目录结构

```
├── docker-compose.yml          # Docker部署编排
├── .env                        # 环境变量
├── README.md
├── backend/                    # 后端 Spring Boot 项目
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/xuelian/career/
│       │   ├── CareerApplication.java
│       │   ├── config/         # 配置类
│       │   ├── common/         # 公共类（Result、异常）
│       │   ├── enums/          # 枚举
│       │   ├── entity/         # 15个实体
│       │   ├── mapper/         # 15个Mapper
│       │   ├── dto/request/    # 请求DTO
│       │   ├── dto/response/   # 响应DTO
│       │   ├── vo/             # VO
│       │   ├── controller/     # 13个Controller
│       │   ├── service/        # 14组Service
│       │   ├── util/           # 工具类
│       │   └── interceptor/    # JWT/角色拦截器
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-docker.yml
│           ├── db/init.sql     # 数据库初始化（15表+预置数据）
│           └── prompts/        # 5个AI Prompt模板
└── frontend/                   # 前端 Vue 3 项目
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── main.ts
        ├── App.vue
        ├── router/             # 路由
        ├── stores/             # Pinia状态管理
        ├── api/                # API封装
        ├── views/              # 16个页面
        ├── components/         # 布局/公共/图表组件
        ├── assets/styles/      # 全局样式
        └── utils/              # 工具函数
```

## API 文档

所有接口返回统一结构：`{ code: 200, message: "success", data: {...} }`

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

详细接口列表参见后端代码中的 Controller 注释。

## 安全性

- 密码 BCrypt 哈希存储
- JWT 无状态认证，Token 7天有效期
- 角色拦截器（ADMIN/HR/STUDENT）
- 文件上传格式和大小校验（PDF/DOCX ≤5MB）
- CORS 跨域白名单
- MyBatis-Plus 参数化查询防SQL注入

## License

仅供实训教学使用
