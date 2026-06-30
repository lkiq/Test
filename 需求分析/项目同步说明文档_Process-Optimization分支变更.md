# 项目同步说明文档 — Process-Optimization 分支变更

> **生成日期**: 2026-06-29  
> **当前分支**: `Process-Optimization`  
> **基准分支**: `origin/master`  
> **目的**: 确保团队成员进行 GitHub 同步时，了解本地所有变更内容，避免产生文件冲突和代码覆盖。

---

## 一、分支状态总览

| 项目 | 状态 |
|------|------|
| 当前分支 | `Process-Optimization`（基于 `master`） |
| 本地提交 | 3 个 commit（含合并提交） |
| 已提交变更（vs master） | **82 个文件**，+8099 行，-134 行 |
| 已暂存未提交变更 | **17 个文件**，+635 行，-236 行 |
| 远程分支 | `origin/master`（云端仓库主分支） |

### 本地提交历史（Process-Optimization vs master 新增）

| commit | 说明 |
|--------|------|
| `3461621` | merge: 合并 origin/master 到 career-platform-v2 |
| `40890fc` | sync: 同步当前项目到 origin 仓库 |
| `a7bbd86` | init: AI求职平台 - 能力测评、职业探索、岗位匹配、实时聊天、简历优化、模拟面试、智能客服等模块 |

---

## 二、已提交变更文件清单（82 个文件）

### 2.1 新增模块：实时聊天系统（Chat）

**后端新增文件：**

| 文件路径 | 说明 |
|----------|------|
| `backend/.../controller/ChatController.java` | 聊天控制器，WebSocket 消息发送/接收 |
| `backend/.../dto/request/CreateConversationRequest.java` | 创建会话请求 DTO |
| `backend/.../dto/request/SendLoginCodeRequest.java` | 登录验证码请求 DTO |
| `backend/.../dto/request/SendMessageRequest.java` | 发送消息请求 DTO |
| `backend/.../dto/response/ConversationResponse.java` | 会话响应 DTO |
| `backend/.../dto/response/MessageResponse.java` | 消息响应 DTO |
| `backend/.../dto/response/UnreadCountResponse.java` | 未读计数响应 DTO |
| `backend/.../entity/ChatMessage.java` | 聊天消息实体 |
| `backend/.../entity/Conversation.java` | 会话实体 |
| `backend/.../mapper/ChatMessageMapper.java` | 聊天消息 Mapper |
| `backend/.../mapper/ConversationMapper.java` | 会话 Mapper |
| `backend/.../service/ChatService.java` | 聊天服务接口 |
| `backend/.../service/impl/ChatServiceImpl.java` | 聊天服务实现（289行） |

**前端新增文件：**

| 文件路径 | 说明 |
|----------|------|
| `frontend/src/api/chat.ts` | 聊天 API 封装 |
| `frontend/src/stores/chat.ts` | 聊天状态管理（Pinia） |
| `frontend/src/types/chat.ts` | 聊天类型定义 |
| `frontend/src/views/chat/ChatView.vue` | 聊天页面组件（472行） |

**数据库脚本：**

| 文件路径 | 说明 |
|----------|------|
| `mysql-runtime/add_chat_tables.sql` | 聊天相关表建表脚本 |

**技术栈**: STOMP over WebSocket + SockJS，实现学生-HR 一对一实时通信。

---

### 2.2 新增模块：密码找回与验证码登录

**后端新增文件：**

| 文件路径 | 说明 |
|----------|------|
| `backend/.../dto/request/ForgotPasswordRequest.java` | 忘记密码请求 DTO |
| `backend/.../dto/request/LoginByCodeRequest.java` | 验证码登录请求 DTO |
| `backend/.../dto/request/ResetPasswordRequest.java` | 重置密码请求 DTO |
| `backend/.../service/MailService.java` | 邮件发送服务（136行） |

**前端新增文件：**

| 文件路径 | 说明 |
|----------|------|
| `frontend/src/views/login/ForgotPasswordView.vue` | 忘记密码页面（433行） |

**修改文件：**

| 文件路径 | 变更 |
|----------|------|
| `backend/.../controller/AuthController.java` | +53/-0，新增验证码登录/密码重置接口 |
| `backend/.../dto/request/RegisterRequest.java` | +40/-0，扩展注册字段 |
| `backend/.../service/impl/AuthServiceImpl.java` | +205/-0，新增验证码/密码找回逻辑 |
| `frontend/src/api/auth.ts` | +35/-0，新增验证码/密码相关 API |
| `frontend/src/stores/user.ts` | +19/-0，扩展用户状态 |
| `frontend/src/views/login/LoginView.vue` | +172/-0，支持验证码登录 |
| `frontend/src/views/register/RegisterView.vue` | +269/-0，扩展注册表单字段 |

---

### 2.3 新增模块：企业信息与岗位发布

**后端新增文件：**

| 文件路径 | 说明 |
|----------|------|
| `backend/.../entity/Enterprise.java` | 企业信息实体 |
| `backend/.../mapper/EnterpriseMapper.java` | 企业 Mapper |
| `backend/.../service/EnterpriseInfoService.java` | 企业信息服务接口 |
| `backend/.../service/JobPublishService.java` | 岗位发布服务接口 |
| `backend/.../service/impl/EnterpriseInfoServiceImpl.java` | 企业信息服务实现 |
| `backend/.../service/impl/JobPublishServiceImpl.java` | 岗位发布服务实现（125行） |
| `backend/.../util/FileUtil.java` | 文件上传工具类（77行） |

**修改文件：**

| 文件路径 | 变更 |
|----------|------|
| `backend/.../controller/EnterpriseController.java` | +60/-0，新增企业/岗位发布接口 |
| `backend/.../entity/JobPosition.java` | +12/-0，新增岗位发布相关字段 |
| `backend/.../entity/User.java` | +12/-0，新增用户扩展字段 |
| `frontend/src/api/enterprise.ts` | +18/-0，新增企业相关 API |
| `frontend/src/views/enterprise/EnterpriseHome.vue` | 小幅修改 |
| `frontend/src/views/enterprise/JobPostView.vue` | +327/-0，岗位发布页面重写 |

---

### 2.4 修改：WebMvc 配置与前端路由

**后端修改：**

| 文件路径 | 变更 | 说明 |
|----------|------|------|
| `backend/.../config/WebMvcConfig.java` | +18/-0 | 新增 CORS/静态资源配置 |
| `backend/pom.xml` | +6/-0 | 依赖调整 |
| `backend/src/main/resources/application-dev.yml` | +25/-0 | 开发环境配置调整 |
| `软件实训项目-code/启动后端.bat` | 修改 | 启动脚本调整 |

**前端修改：**

| 文件路径 | 变更 | 说明 |
|----------|------|------|
| `frontend/src/router/index.ts` | +6/-0 | 新增聊天/密码找回路由 |
| `frontend/src/api/request.ts` | +4/-0 | 请求拦截器调整 |
| `frontend/src/components/layout/HeaderBar.vue` | +40/-0 | 导航栏新增聊天入口 |
| `frontend/src/components/job/JobCard.vue` | +11/-0 | 岗位卡片调整 |
| `frontend/vite.config.ts` | +1 | Vite 配置调整 |

---

### 2.5 需求分析文档（新增）

| 文件路径 | 大小 | 说明 |
|----------|------|------|
| `需求分析/岗位发布与查看-实现计划书.md` | 33.65 KB | 岗位发布写入链路实现计划 |
| `需求分析/岗位发布与求职搜索模块实现计划书.md` | 65.26 KB | 岗位发布与搜索技术方案 |
| `需求分析/注册优化与密码找回可行性评估.md` | 12.46 KB | 注册/密码找回可行性分析 |
| `需求分析/聊一聊与HR实时沟通功能实现计划书.md` | 26.09 KB | 实时聊天功能计划 |

---

### 2.6 数据库变更脚本（新增）

| 文件路径 | 说明 |
|----------|------|
| `backend/src/main/resources/db/init.sql` | 初始化脚本（45行） |
| `backend/src/main/resources/db/migration_v2.sql` | 迁移脚本 v2（40行） |
| `backend/src/main/resources/db/migration_v3.sql` | 迁移脚本 v3（17行） |
| `mysql-runtime/add_chat_tables.sql` | 聊天表建表脚本（41行） |

---

### 2.7 其他新增文件

| 文件路径 | 说明 |
|----------|------|
| `学生能力测试模块优化_c914d87b(未完成).md` | 测试模块优化文档（268行） |
| `每日修改清单/2026-06-28_修改清单.md` | 每日修改记录（177行） |
| `.playwright-cli/` (10个文件) | 浏览器自动化测试截图/配置 |
| `uploads/resumes/` | 简历文件上传目录 |

---

## 三、已暂存但未提交的变更（17 个文件）⭐ 重点关注

> 这些是最近一次工作会话的修改，尚未 commit。团队成员拉取代码后需特别注意。

### 3.1 后端变更（10 个文件）

| 文件 | 变更 | 说明 |
|------|------|------|
| `controller/student/AssessmentController.java` | +15 | 能力测评接口优化 |
| `controller/student/GapAnalysisController.java` | +14 | 差距分析接口优化 |
| `controller/student/LearningPathController.java` | +10/-0 | 学习路径控制器优化 |
| `service/AssessmentService.java` | +7 | 新增测评服务方法 |
| `service/GapAnalysisService.java` | +10 | 新增差距分析服务方法 |
| `service/LearningPathService.java` | +1 | 新增学习路径服务方法 |
| `service/impl/AssessmentServiceImpl.java` | +14 | 测评实现逻辑增强 |
| `service/impl/GapAnalysisServiceImpl.java` | +45 | 差距分析实现增强 |
| **`service/impl/LearningPathServiceImpl.java`** | **+66/-0** | **核心修复：`findActivePath` 和 `findActiveMergedPath` 方法将 `selectOne` 改为 `selectList` + `LIMIT 1`，解决 `TooManyResultsException` 异常** |

### 3.2 前端变更（6 个文件）

| 文件 | 变更 | 说明 |
|------|------|------|
| `api/student.ts` | +11/-0 | 学生端 API 扩展 |
| `stores/job.ts` | +41/-0 | 岗位状态管理扩展 |
| **`views/student/AssessmentView.vue`** | **+347/-0** | **能力测评页面大幅增强** |
| `views/student/FavoritesView.vue` | +6/-0 | 收藏页面调整 |
| `views/student/GapAnalysisView.vue` | +15/-0 | 差距分析页面优化 |
| `views/student/JobDetailView.vue` | +10/-0 | 岗位详情页面优化 |
| `views/student/LearningPathView.vue` | +50/-0 | 学习路径页面增强 |

### 3.3 删除文件

| 文件 | 说明 |
|------|------|
| `.codebuddy/plant/学生能力测试模块优化_c914d87b(未完成).md` | 已移动到项目根目录 |

---

## 四、模块变更架构总图

```
Process-Optimization 分支 (vs master)
│
├── 🆕 实时聊天系统 (Chat)
│   ├── WebSocket/STOMP 通信
│   ├── 会话管理 (Conversation + ChatMessage)
│   └── 前端 ChatView.vue
│
├── 🆕 密码找回 & 验证码登录
│   ├── ForgotPassword / ResetPassword / LoginByCode
│   ├── MailService 邮件服务
│   └── ForgotPasswordView.vue
│
├── 🆕 企业岗位发布
│   ├── Enterprise 实体 / JobPublishService
│   ├── FileUtil 文件上传
│   └── JobPostView.vue 发布表单
│
├── 📝 能力测评 & 学习路径优化 (暂存未提交)
│   ├── Assessment + GapAnalysis + LearningPath
│   ├── 核心修复: selectOne → selectList + LIMIT 1
│   └── AssessmentView.vue 大幅增强
│
├── 📄 需求分析文档 (4个)
│   ├── 岗位发布与求职搜索模块实现计划书
│   ├── 聊一聊与HR实时沟通功能实现计划书
│   ├── 注册优化与密码找回可行性评估
│   └── 岗位发布与查看-实现计划书
│
├── 🗄️ 数据库变更 (4个脚本)
│   ├── db/init.sql
│   ├── db/migration_v2.sql
│   ├── db/migration_v3.sql
│   └── add_chat_tables.sql
│
└── ⚙️ 配置变更
    ├── pom.xml (依赖调整)
    ├── application-dev.yml (环境配置)
    ├── WebMvcConfig.java (CORS/静态资源)
    ├── router/index.ts (新路由)
    └── 启动脚本 (.bat)
```

---

## 五、同步操作指南（团队成员必读）

### 5.1 如果你是从 GitHub 首次拉取此分支

```bash
# 1. 拉取远程最新
git fetch origin

# 2. 切换到 Process-Optimization 分支
git checkout -b Process-Optimization origin/Process-Optimization

# 3. 确认文件完整性
git status
```

### 5.2 如果你本地已有 Process-Optimization 分支且有自己的修改

```bash
# 1. 先保存本地未提交的修改
git stash save "同步前暂存本地修改"

# 2. 拉取远程更新
git pull origin Process-Optimization

# 3. 恢复本地修改（可能需要手动解决冲突）
git stash pop

# 4. 检查冲突文件
git status
```

### 5.3 如果你在 master 分支上工作

```bash
# 建议：基于 Process-Optimization 创建自己的功能分支
git checkout Process-Optimization
git checkout -b your-feature-branch
```

### 5.4 数据库同步

⚠️ **重要**：拉取代码后必须执行以下数据库脚本：

```bash
# 按顺序执行
mysql -u root -p < backend/src/main/resources/db/migration_v2.sql
mysql -u root -p < backend/src/main/resources/db/migration_v3.sql
mysql -u root -p < mysql-runtime/add_chat_tables.sql
```

这些脚本会新增以下表：
- `conversation` — 会话表
- `chat_message` — 聊天消息表
- `enterprise` — 企业信息表
- 以及主表的字段扩展

---

## 六、冲突风险分析与预防

### 6.1 🔴 高风险文件（多人修改可能性大）

| 文件 | 风险原因 |
|------|----------|
| `backend/pom.xml` | 依赖版本可能冲突 |
| `frontend/src/router/index.ts` | 路由定义可能冲突 |
| `frontend/src/api/student.ts` | API 定义可能冲突 |
| `frontend/vite.config.ts` | 构建配置可能冲突 |
| `backend/.../application-dev.yml` | 环境配置可能冲突 |

### 6.2 🟡 中风险文件

| 文件 | 风险原因 |
|------|----------|
| `AssessmentView.vue` (+347行) | 大幅改造，与任何对该页面的修改都会冲突 |
| `LearningPathServiceImpl.java` (+66行) | 核心逻辑修改，`selectOne` → `selectList` |
| `AuthServiceImpl.java` (+205行) | 认证逻辑大幅扩展 |

### 6.3 🟢 低风险文件

新增文件（`A` 标记）通常不会产生冲突，除非不同团队成员创建了同名文件。

### 6.4 冲突预防建议

1. **提交前先拉取**：`git pull --rebase` 避免合并提交堆积
2. **小步提交**：每个功能点独立 commit，降低冲突复杂度
3. **功能分支策略**：每个成员基于 `Process-Optimization` 创建自己的功能分支
4. **数据库变更先行**：执行迁移脚本后再启动后端
5. **前后端对齐**：如果改了 API 接口，务必同步更新前端 `api/` 目录

---

## 七、当前暂存变更提交建议

以下 17 个文件目前处于 `staged` 状态，建议按以下顺序提交：

```bash
# 提交组 1: 后端核心修复
git commit -m "fix: 学习路径 selectOne→selectList 修复 TooManyResultsException
- findActivePath/findActiveMergedPath 改用 selectList + LIMIT 1
- 解决合并学习路径生成失败问题"

# 提交组 2: 后端服务增强
git commit -m "feat: 能力测评与差距分析接口增强
- AssessmentController/Service 新增方法
- GapAnalysisController/Service 扩展功能"

# 提交组 3: 前端页面优化
git commit -m "feat: 前端测评/学习路径/差距分析页面优化
- AssessmentView.vue 大幅增强 (+347行)
- LearningPathView.vue 学习路径展示优化
- GapAnalysisView.vue 差距分析展示优化"
```

---

## 八、附录：已修改文件的完整路径清单

### 后端 Java 文件（已提交）

```
软件实训项目-code/backend/src/main/java/com/xuelian/career/
├── config/WebMvcConfig.java                          [M]
├── controller/AuthController.java                     [M]
├── controller/ChatController.java                     [A]
├── controller/EnterpriseController.java               [M]
├── dto/request/CreateConversationRequest.java         [A]
├── dto/request/ForgotPasswordRequest.java             [A]
├── dto/request/LoginByCodeRequest.java                [A]
├── dto/request/RegisterRequest.java                   [M]
├── dto/request/ResetPasswordRequest.java              [A]
├── dto/request/SendLoginCodeRequest.java              [A]
├── dto/request/SendMessageRequest.java                [A]
├── dto/response/ConversationResponse.java             [A]
├── dto/response/MessageResponse.java                  [A]
├── dto/response/UnreadCountResponse.java              [A]
├── entity/ChatMessage.java                            [A]
├── entity/Conversation.java                           [A]
├── entity/Enterprise.java                             [A]
├── entity/JobPosition.java                            [M]
├── entity/User.java                                   [M]
├── mapper/ChatMessageMapper.java                      [A]
├── mapper/ConversationMapper.java                     [A]
├── mapper/EnterpriseMapper.java                       [A]
├── service/ChatService.java                           [A]
├── service/EnterpriseInfoService.java                 [A]
├── service/JobPublishService.java                     [A]
├── service/MailService.java                           [A]
├── service/UserService.java                           [M]
├── service/impl/AuthServiceImpl.java                  [M]
├── service/impl/ChatServiceImpl.java                  [A]
├── service/impl/EnterpriseInfoServiceImpl.java        [A]
├── service/impl/EnterpriseServiceImpl.java            [M]
├── service/impl/JobMatchingServiceImpl.java           [M]
├── service/impl/JobPublishServiceImpl.java            [A]
├── service/impl/UserServiceImpl.java                  [M]
└── util/FileUtil.java                                 [A]
```

> 图例: `[A]` = 新增文件, `[M]` = 修改文件

### 前端文件（已提交）

```
软件实训项目-code/frontend/src/
├── api/auth.ts                                        [M]
├── api/chat.ts                                        [A]
├── api/enterprise.ts                                  [A]
├── api/request.ts                                     [M]
├── components/job/JobCard.vue                         [M]
├── components/layout/HeaderBar.vue                    [M]
├── router/index.ts                                    [M]
├── stores/chat.ts                                     [A]
├── stores/user.ts                                     [M]
├── types/chat.ts                                      [A]
├── utils/jobEnrich.ts                                 [M]
├── views/chat/ChatView.vue                            [A]
├── views/enterprise/EnterpriseHome.vue                [M]
├── views/enterprise/JobPostView.vue                   [M]
├── views/login/ForgotPasswordView.vue                 [A]
├── views/login/LoginView.vue                          [M]
├── views/register/RegisterView.vue                    [M]
├── views/student/JobDetailView.vue                    [M]
└── views/student/JobMatchingView.vue                  [M]
```

---

## 九、未跟踪的新增文件（?? 状态，尚未添加到 Git）

> 以下文件存在于工作目录但尚未被 `git add`。团队成员需确认是否需要跟踪这些文件。

### 9.1 后端新增文件（未跟踪）

```
软件实训项目-code/backend/src/main/java/com/xuelian/career/
├── controller/student/FavoriteController.java        [新增] 岗位收藏控制器
├── dto/response/PathStatsDTO.java                     [新增] 学习路径统计 DTO
├── dto/response/SkillsMatrixDTO.java                  [新增] 技能矩阵 DTO
├── dto/response/TestQuestionDTO.java                  [新增] 测试题目 DTO
├── dto/response/TestResultDTO.java                    [新增] 测试结果 DTO
├── entity/SkillTestQuestion.java                      [新增] 技能测试题目实体
├── entity/UserFavoriteJob.java                        [新增] 用户收藏岗位实体
├── entity/UserSkillMastery.java                       [新增] 用户技能掌握度实体
├── mapper/SkillTestQuestionMapper.java                [新增] 测试题目 Mapper
├── mapper/UserFavoriteJobMapper.java                  [新增] 收藏 Mapper
├── mapper/UserSkillMasteryMapper.java                 [新增] 技能掌握度 Mapper
├── service/MasteryMigrationService.java               [新增] 掌握度迁移服务
└── resources/db/data/                                 [新增] 数据库种子数据目录
```

### 9.2 前端新增文件（未跟踪）

```
软件实训项目-code/frontend/src/views/student/
├── LearningPathDetailView.vue                         [新增] 学习路径详情页
└── LearningPathList.vue                               [新增] 学习路径列表页

软件实训项目-code/frontend/
└── vite.config.js                                     [新增] Vite 备用配置
```

### 9.3 未跟踪文件的处理建议

- 以上文件为 **能力测试模块 + 学习路径 V2** 的开发内容
- 建议在确认功能稳定后统一 `git add` 并提交
- 如果仅需同步已提交内容，暂时保持未跟踪状态即可

---

## 十、快速检查清单（同步前必做）

- [ ] 确认当前在 `Process-Optimization` 分支：`git branch`
- [ ] 暂存本地未提交修改：`git stash save "描述"`
- [ ] 拉取远程最新：`git pull origin Process-Optimization`
- [ ] 执行数据库迁移脚本（见第五章 5.4）
- [ ] 恢复本地修改：`git stash pop`
- [ ] 解决冲突（如有）：`git status` 查看冲突文件
- [ ] 编译验证：`mvn clean package -DskipTests`
- [ ] 启动测试：确认前后端正常运行

---

> **文档结束** — 如有疑问请联系项目负责人。建议将此文档与 `项目计划/` 和 `需求分析/` 目录中的计划书配套阅读。
