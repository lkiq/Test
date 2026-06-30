# 聊一聊与HR实时沟通功能 — 实现计划书

> **版本：** V1.0  
> **日期：** 2026-06-28  
> **状态：** 待评审  
> **评分机制说明：** 每个需求项按 **复杂度（C）**、**优先级（P）**、**业务价值（V）** 三个维度评分，满分 10 分。  
> **总分 = C×0.3 + P×0.4 + V×0.3**，总分 ≥ 7.0 为 P0 核心需求，5.0~6.9 为 P1 重要需求，< 5.0 为 P2 可选需求。

---

## 一、现状分析

### 1.1 当前实现

当前系统中"聊一聊"按钮出现在岗位卡片和岗位详情页，点击后直接跳转到 `/customer-service`（AI 智能客服页面）。

```
用户点击「聊一聊」
    → 跳转 /customer-service
    → AI 智能客服（FAQ匹配 + DeepSeek AI）
    → HTTP 请求-响应模式，无实时通信
```

### 1.2 存在的问题

| # | 问题 | 影响 |
|---|------|------|
| 1 | "聊一聊"名不副实，用户期待的是跟发布岗位的 HR 真人沟通 | 体验断裂，信任感下降 |
| 2 | 无实时通信能力，系统完全基于 HTTP 请求-响应 | 无法实现即时聊天 |
| 3 | 无聊天消息持久化，无历史会话记录 | 沟通记录丢失 |
| 4 | HR 端无从查看或回复学生消息的入口 | HR 无法参与沟通 |
| 5 | 无消息通知机制，新消息无法触达对方 | 沟通断层 |

### 1.3 数据库现状

当前 `career_platform` 数据库共 15 张表，**不包含任何聊天消息、会话相关的表**。需要新增 2 张核心表 + 可选辅助表。

---

## 二、功能需求清单与评分

### 2.1 核心功能矩阵

| 序号 | 功能模块 | 功能描述 | 复杂度(C) | 优先级(P) | 业务价值(V) | 总分 | 等级 |
|------|---------|---------|:---:|:---:|:---:|:---:|:---:|
| F1 | 会话创建 | 学生从岗位卡片发起与HR的一对一会话 | 4 | 10 | 10 | **8.2** | P0 |
| F2 | 实时消息收发 | 双方通过WebSocket实时发送/接收文本消息 | 8 | 10 | 9 | **8.7** | P0 |
| F3 | 消息持久化 | 所有聊天消息存入数据库，支持历史回顾 | 5 | 10 | 9 | **7.8** | P0 |
| F4 | 会话列表 | 学生端/HR端各自查看自己的会话列表（联系人列表） | 6 | 9 | 8 | **7.5** | P0 |
| F5 | 新消息通知 | 收到新消息时顶部显示红点/数字角标 | 5 | 9 | 8 | **7.3** | P0 |
| F6 | 聊天窗口UI | 独立的1v1聊天窗口，替代当前客服页面 | 6 | 9 | 8 | **7.3** | P0 |
| F7 | 图片消息 | 支持发送图片（如简历截图） | 7 | 6 | 6 | **6.1** | P1 |
| F8 | 消息已读状态 | 标记已读/未读，显示对方是否已读 | 5 | 7 | 6 | **5.8** | P1 |
| F9 | 快捷回复 | HR端预设常用语快捷发送 | 3 | 6 | 5 | **4.8** | P2 |
| F10 | 会话搜索 | 按岗位/公司/学生姓名搜索会话 | 3 | 5 | 4 | **4.0** | P2 |
| F11 | 文件消息 | 支持发送简历PDF等附件 | 7 | 4 | 5 | **5.0** | P2 |
| F12 | 敏感词过滤 | 聊天内容自动过滤敏感词汇 | 6 | 6 | 5 | **5.4** | P1 |
| F13 | 智能客服兜底 | HR离线时自动转AI客服 | 7 | 5 | 6 | **5.6** | P1 |

> **评分公式：** 总分 = C×0.3 + P×0.4 + V×0.3  
> - 复杂度(C)：越高越难实现（反向参考工作量评估）  
> - 优先级(P)：越高的必须先做  
> - 业务价值(V)：越高越能提升用户体验

### 2.2 分期实施规划

```
Phase 1（MVP，2周）          Phase 2（体验增强，1周）       Phase 3（完善优化，1周）
┌─────────────────┐      ┌─────────────────┐        ┌─────────────────┐
│ F1 会话创建      │      │ F7 图片消息      │        │ F9 快捷回复      │
│ F2 实时消息收发  │  →   │ F8 消息已读状态  │   →   │ F10 会话搜索    │
│ F3 消息持久化    │      │ F12 敏感词过滤   │        │ F11 文件消息     │
│ F4 会话列表      │      │                 │        │ F13 智能客服兜底 │
│ F5 新消息通知    │      │                 │        │                  │
│ F6 聊天窗口UI    │      │                 │        │                  │
└─────────────────┘      └─────────────────┘        └─────────────────┘
```

---

## 三、技术架构设计

### 3.1 通信方案选型

#### 方案对比

| 维度 | WebSocket（推荐） | 短轮询 | 长轮询 | SSE |
|------|:-:|:-:|:-:|:-:|
| 实时性 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 双向通信 | ✅ 原生支持 | ❌ 需HTTP请求 | ❌ 单向 | ❌ 仅服务端→客户端 |
| 服务器开销 | 中等 | 高（频繁连接） | 中等 | 低 |
| 实现复杂度 | ⭐⭐⭐⭐ | ⭐ | ⭐⭐ | ⭐⭐ |
| Spring Boot支持 | ⭐⭐⭐⭐⭐ | ✅ | ✅ | ✅ |
| 浏览器兼容 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **综合推荐** | **✅ 首选** | ❌ | ❌ | ❌ |

#### 结论：选择 Spring Boot WebSocket (STOMP协议)

理由：
- 浏览器原生 `WebSocket` API 支持良好
- Spring Boot `spring-boot-starter-websocket` 开箱即用
- STOMP 子协议提供消息路由、订阅/发布模型，适合聊天场景
- 服务器可主动推送消息，实现即时通知

### 3.2 架构总览图

```
┌──────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                        │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ 学生端聊天页  │  │  HR端聊天页  │  │ 消息通知组件  │   │
│  │ ChatView     │  │ ChatView     │  │ Badge        │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                  │            │
│         └─────────┬───────┘                  │            │
│                   │ SockJS + STOMP           │            │
└───────────────────┼──────────────────────────┼────────────┘
                    │                          │
            ws://localhost:8080/ws      GET /api/messages/unread
                    │                          │
┌───────────────────┼──────────────────────────┼────────────┐
│                   │        后端 (Spring Boot) │            │
│  ┌────────────────▼──────────────────┐       │            │
│  │      WebSocketConfig             │  ┌────▼─────────┐  │
│  │  端点: /ws, /topic, /queue       │  │ MessageController│
│  └────────────────┬─────────────────┘  │ REST: 历史消息│  │
│                   │                     │ 未读数/已读/会话│  │
│  ┌────────────────▼─────────────────┐  └────┬─────────┘  │
│  │      ChatService                 │       │            │
│  │  消息存储、会话管理、通知推送    │◄──────┘            │
│  └────────────────┬─────────────────┘                    │
│                   │                                      │
│  ┌────────────────▼─────────────────┐                    │
│  │         MySQL 数据库              │                    │
│  │  conversation / chat_message     │                    │
│  └──────────────────────────────────┘                    │
└──────────────────────────────────────────────────────────┘
```

### 3.3 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 前端 WebSocket | `@stomp/stompjs` + `sockjs-client` | latest |
| 后端 WebSocket | `spring-boot-starter-websocket` | 2.7.x |
| 消息协议 | STOMP over SockJS | — |
| 消息代理 | Spring SimpleBroker（内置） | — |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis-Plus | 3.5.x |
| 认证 | JWT Token（已有） | — |

---

## 四、数据库设计

### 4.1 新增表结构

#### 表1：`conversation`（会话表）

```sql
CREATE TABLE `conversation` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `student_id`      BIGINT       NOT NULL COMMENT '学生用户ID',
  `hr_id`           BIGINT       NOT NULL COMMENT 'HR用户ID',
  `job_id`          BIGINT       DEFAULT NULL COMMENT '关联岗位ID（可为空）',
  `job_title`       VARCHAR(100) DEFAULT NULL COMMENT '岗位名称快照',
  `enterprise_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称快照',
  `last_message`    VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_message_at` DATETIME     DEFAULT NULL COMMENT '最后消息时间',
  `student_unread`  INT          DEFAULT 0 COMMENT '学生未读数',
  `hr_unread`       INT          DEFAULT 0 COMMENT 'HR未读数',
  `status`          TINYINT      DEFAULT 1 COMMENT '状态: 1=活跃 0=已关闭',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_hr` (`hr_id`),
  INDEX `idx_job` (`job_id`),
  UNIQUE KEY `uk_student_hr_job` (`student_id`, `hr_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';
```

#### 表2：`chat_message`（聊天消息表）

```sql
CREATE TABLE `chat_message` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT       NOT NULL COMMENT '会话ID',
  `sender_id`       BIGINT       NOT NULL COMMENT '发送者用户ID',
  `sender_role`     VARCHAR(10)  NOT NULL COMMENT '发送者角色: STUDENT/HR',
  `receiver_id`     BIGINT       NOT NULL COMMENT '接收者用户ID',
  `content`         TEXT         NOT NULL COMMENT '消息内容',
  `msg_type`        VARCHAR(20)  DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE/SYSTEM',
  `file_url`        VARCHAR(500) DEFAULT NULL COMMENT '附件URL（图片/文件）',
  `is_read`         TINYINT      DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
  `read_at`         DATETIME     DEFAULT NULL COMMENT '阅读时间',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_conversation` (`conversation_id`),
  INDEX `idx_sender` (`sender_id`),
  INDEX `idx_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
```

### 4.2 已有表复用

无需修改已有表结构。`conversation` 表通过 `student_id`、`hr_id`、`job_id` 与现有的 `user` 表和 `job_position` 表关联。

---

## 五、后端接口设计

### 5.1 WebSocket 端点

| 端点 | 说明 |
|------|------|
| `ws://localhost:8080/ws` | WebSocket 连接端点（SockJS） |
| `/user/{userId}/queue/messages` | 用户私有消息队列（点对点推送） |
| `/topic/conversation/{conversationId}` | 会话消息广播（群聊预留） |

### 5.2 REST API 端点

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/api/chat/conversations` | 创建/获取会话（学生点击"聊一聊"） | ✅ |
| `GET` | `/api/chat/conversations` | 获取当前用户的会话列表 | ✅ |
| `GET` | `/api/chat/conversations/{id}/messages` | 获取会话历史消息（分页） | ✅ |
| `PUT` | `/api/chat/conversations/{id}/read` | 标记会话已读 | ✅ |
| `GET` | `/api/chat/unread-count` | 获取未读消息总数 | ✅ |
| `POST` | `/api/chat/conversations/{id}/close` | 关闭会话 | ✅ |

### 5.3 WebSocket 消息协议 (STOMP)

**发送消息（客户端 → 服务端）：**

```
目的地: /app/chat.send
消息体:
{
  "conversationId": 1,
  "senderId": 10,
  "senderRole": "STUDENT",
  "receiverId": 20,
  "content": "您好，对这个岗位很感兴趣",
  "msgType": "TEXT"
}
```

**接收消息（服务端 → 客户端）：**

```
目的地: /user/{userId}/queue/messages
消息体:
{
  "id": 101,
  "conversationId": 1,
  "senderId": 10,
  "senderName": "张三",
  "senderAvatar": "/avatar/10.png",
  "senderRole": "STUDENT",
  "receiverId": 20,
  "content": "您好，对这个岗位很感兴趣",
  "msgType": "TEXT",
  "createdAt": "2026-06-28T18:30:00"
}
```

### 5.4 消息处理流程

```
Client A (学生)                         Server                         Client B (HR)
      │                                    │                                │
      │  STOMP SEND /app/chat.send          │                                │
      │──────────────────────────────────►  │                                │
      │                                    │  1. 校验JWT Token              │
      │                                    │  2. 校验会话权限               │
      │                                    │  3. 敏感词过滤（可选）         │
      │                                    │  4. 存入 chat_message 表       │
      │                                    │  5. 更新 conversation 未读数   │
      │                                    │  6. 获取接收者在线状态          │
      │                                    │                                │
      │                                    │  STOMP SEND /user/20/queue/messages
      │                                    │──────────────────────────────►  │
      │                                    │                                │
      │  STOMP SEND /user/10/queue/messages│                                │
      │◄──────────────────────────────────│  (回调确认，可选)              │
```

---

## 六、前端设计

### 6.1 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| **新增** | `src/views/chat/ChatView.vue` | 通用聊天视图（学生端+HR端共用） |
| **新增** | `src/components/chat/ConversationList.vue` | 会话列表侧边栏组件 |
| **新增** | `src/components/chat/MessageBubble.vue` | 消息气泡组件（支持文本/图片） |
| **新增** | `src/stores/chat.ts` | 聊天状态管理 Pinia Store |
| **新增** | `src/api/chat.ts` | 聊天 REST API 封装 |
| **新增** | `src/composables/useWebSocket.ts` | WebSocket 连接管理 Composable |
| **新增** | `src/types/chat.ts` | 聊天类型定义 |
| **修改** | `src/router/index.ts` | 新增 `/chat` 路由 |
| **修改** | `src/components/job/JobCard.vue` | "聊一聊"跳转改为 `/chat?jobId=xxx` |
| **修改** | `src/views/student/JobDetailView.vue` | "聊一聊"跳转改为 `/chat?jobId=xxx` |
| **修改** | `src/components/layout/HeaderBar.vue` | 学生下拉：`/chat`；HR下拉：`/chat`（替换客服入口，新增消息角标） |
| **删除** | `src/views/customer/CustomerServiceView.vue` | 可选保留但不再从"聊一聊"进入 |
| **保留** | `src/components/customer-service/FloatCustomerService.vue` | 浮动客服保留（独立AI客服入口） |

### 6.2 页面布局设计

```
┌──────────────────────────────────────────────────────────┐
│  HeaderBar (顶部导航，带消息未读数角标)                    │
├──────────────────┬───────────────────────────────────────┤
│  会话列表 (300px) │          聊天窗口 (flex:1)             │
│                  │                                       │
│  ┌──────────────┐│  ┌─────────────────────────────────┐  │
│  │ 🔍 搜索会话  ││  │ HR: 张经理 · Java开发工程师      │  │
│  ├──────────────┤│  ├─────────────────────────────────┤  │
│  │ ○ 张三       ││  │                                 │  │
│  │   Java后端.. ││  │   ┌───────────────────┐         │  │
│  │   你好，我.. ││  │   │ 你好，我对这个岗位  │(学生)   │  │
│  │   2分钟前    ││  │   │ 很感兴趣           │         │  │
│  ├──────────────┤│  │   └───────────────────┘         │  │
│  │ ● 李四    ② ││  │       ┌───────────────────┐      │  │
│  │   前端开发.. ││  │       │ 您好！请发一份简历  │(HR)  │  │
│  │   收到您的.. ││  │       │ 过来看看           │      │  │
│  │   5分钟前    ││  │       └───────────────────┘      │  │
│  ├──────────────┤│  │                                 │  │
│  │ ○ 王五       ││  │                                 │  │
│  │   数据分析.. ││  ├─────────────────────────────────┤  │
│  │   好的谢谢   ││  │ [📎] [😊]  ________________ [发送]│  │
│  │   昨天       ││  └─────────────────────────────────┘  │
│  └──────────────┘│                                       │
└──────────────────┴───────────────────────────────────────┘
```

### 6.3 Chat Pinia Store 设计

```typescript
// stores/chat.ts
interface ChatState {
  conversations: Conversation[]     // 会话列表
  activeConversationId: number | null
  messages: Map<number, Message[]>  // key=conversationId
  unreadTotal: number
  onlineUsers: Set<number>          // 在线用户ID集合
  stompClient: Client | null
  connected: boolean
}

// 核心 actions
actions: {
  connect()            // 建立 WebSocket 连接
  disconnect()         // 断开连接
  sendMessage(text, convId, receiverId)
  fetchConversations()
  fetchMessages(convId, page)
  markAsRead(convId)
  addMessage(msg)      // 收到新消息时调用
  updateUnreadCount()
}
```

### 6.4 关键交互流程

**学生发起聊天：**
1. 岗位卡片/详情页 → 点击「聊一聊」
2. 前端调用 `POST /api/chat/conversations { jobId, hrId }`
3. 后端创建/获取已存在的会话，返回 `conversationId`
4. 前端跳转 `/chat?conversationId=xxx`
5. `ChatView` 加载会话和消息历史，建立 WebSocket 连接
6. 学生输入第一条消息 → STOMP 发送 → 服务端存储并推送给 HR

**HR 收到消息：**
1. HR 端的 WebSocket 收到 `/user/{hrId}/queue/messages` 推送
2. 消息加入 `messages[]`，未读数 +1
3. HeaderBar 的铃铛角标更新
4. 如果在聊天页面，消息实时显示；否则顶部红点提示

---

## 七、后端代码结构

### 7.1 新增文件清单

```
backend/src/main/java/com/xuelian/career/
├── config/
│   └── WebSocketConfig.java          # WebSocket + STOMP 配置
├── controller/
│   └── ChatController.java           # 聊天 REST 接口
├── entity/
│   ├── Conversation.java             # 会话实体
│   └── ChatMessage.java              # 消息实体
├── mapper/
│   ├── ConversationMapper.java       # 会话 Mapper
│   └── ChatMessageMapper.java        # 消息 Mapper
├── service/
│   ├── ChatService.java              # 聊天服务接口
│   └── impl/
│       └── ChatServiceImpl.java      # 聊天服务实现
├── dto/
│   ├── request/
│   │   ├── CreateConversationRequest.java
│   │   └── SendMessageRequest.java
│   └── response/
│       ├── ConversationResponse.java
│       ├── MessageResponse.java
│       └── UnreadCountResponse.java
└── websocket/
    ├── WebSocketAuthInterceptor.java # WebSocket JWT 鉴权拦截器
    └── WebSocketEventListener.java   # 连接/断开事件监听
```

### 7.2 WebSocket 鉴权方案

由于 STOMP over WebSocket 无法直接携带 HTTP Authorization header，采用以下方案：

```
客户端连接时通过 URL 参数携带 Token：
new WebSocket('ws://localhost:8080/ws?token=' + jwtToken)

WebSocketAuthInterceptor:
  → 从 CONNECT 帧的 URL 参数中提取 token
  → JWT 解析验证
  → 将 userId / role 存入 StompHeaderAccessor 的 session attributes
  → 后续消息处理中从 session attributes 获取用户身份
```

---

## 八、实施计划

### Phase 1: MVP 核心功能（预计 2 周，6个工作日）

| 工作日 | 任务 | 产出 |
|:--:|------|------|
| D1 | 数据库建表 `conversation` + `chat_message`，创建 Entity/Mapper | 数据层就绪 |
| D2 | WebSocket 配置 + STOMP 端点 + JWT 鉴权拦截器 | WebSocket 通道打通 |
| D3 | `ChatService` 核心逻辑：消息收发、存储、会话创建 | 后端核心完成 |
| D4 | 前端 `ChatView` + `ConversationList` 组件，`useWebSocket` composable | 聊天界面完成 |
| D5 | 前端 Chat Store + API 对接，修改 HeaderBar 和 JobCard | 功能串联完成 |
| D6 | 联调测试 + Bug 修复 + 边界情况处理 | MVP 交付 |

### Phase 2: 体验增强（预计 1 周）

| 工作日 | 任务 |
|:--:|------|
| D7 | 消息已读/未读状态，已读回执 |
| D8 | 图片消息支持（上传 + 预览） |
| D9 | 敏感词过滤集成 |

### Phase 3: 完善优化（预计 1 周）

| 工作日 | 任务 |
|:--:|------|
| D10 | HR 快捷回复模板 |
| D11 | 文件消息（简历 PDF 等） |
| D12 | HR 离线时智能客服兜底提示 |

---

## 九、风险与应对

| 风险 | 等级 | 应对措施 |
|------|:--:|----------|
| WebSocket 连接不稳定（网络/NAT） | 🟡 | SockJS 自动降级到 HTTP 长轮询；前端自动重连机制 |
| 高并发下 STOMP 消息积压 | 🟡 | Phase 1 仅使用内置 SimpleBroker；规模增长后切换 RabbitMQ/ActiveMQ |
| 历史消息查询性能随数据增长下降 | 🟢 | `chat_message` 表首期就建立 `(conversation_id, created_at DESC)` 联合索引；必要时分表 |
| JWT Token 在 WebSocket URL 中暴露 | 🟡 | 服务端同时验证 Token 与 userId 的一致性；token 有过期时间 |
| 学生/HR 同时发起多个会话难以管理 | 🟢 | 唯一的 `uk_student_hr_job` 约束避免重复会话；前端限制同一岗位仅一个会话 |

---

## 十、验收标准

### Phase 1 MVP 验收

| # | 验收项 | 通过条件 |
|---|--------|---------|
| 1 | 学生点击"聊一聊" | 成功创建/打开与岗位HR的对话窗口 |
| 2 | 消息实时发送 | 发送后 < 1 秒对方收到 |
| 3 | 消息持久化 | 刷新页面后历史消息仍然存在 |
| 4 | 会话列表 | 学生/HR 各自可看到所有会话 |
| 5 | 新消息通知 | HR 收到新消息时顶部有未读角标 |
| 6 | 角色隔离 | 学生只能看到自己的会话，HR 只能看到自己的会话 |

### Phase 2 验收

| # | 验收项 | 通过条件 |
|---|--------|---------|
| 7 | 已读状态 | 对方阅读后发送方可见"已读"标识 |
| 8 | 图片消息 | 支持发送图片，缩略图+点击放大 |
| 9 | 敏感词 | 含敏感词的消息被拦截或替换 |

---

## 十一、对现有系统的影响评估

| 影响范围 | 程度 | 说明 |
|----------|:--:|------|
| 数据库 | 🟢 低 | 新增 2 张表，不修改已有表 |
| 已有 API | 🟢 低 | 不修改已有 Controller，新增独立的 `/api/chat/*` |
| 前端路由 | 🟢 低 | 新增 `/chat` 路由，修改 JobCard/JobDetail 跳转 |
| HeaderBar | 🟡 中 | 替换客服入口，新增消息角标 |
| 智能客服 | 🟢 低 | 保留 FloatCustomerService 独立入口，不影响现有客服功能 |
| 部署 | 🟡 中 | WebSocket 需反向代理（Nginx）配置 `Upgrade` 头 |

---

## 十二、附录：与 AI 智能客服的关系

聊一聊功能上线后，两个客服入口的分工：

| 入口 | 用途 | 路由 |
|------|------|------|
| 岗位卡片「聊一聊」 | 与 HR 真人 1v1 实时聊天 | `/chat?jobId=xxx` |
| 浮动客服球 | AI 智能客服（FAQ+AI） | 悬浮球弹出窗口 |
| 导航下拉「智能客服」 | AI 智能客服（完整页面） | `/customer-service`（保留） |

三者互不干扰，各自独立运行，满足不同场景需求。

---

> **审批状态：** 待评审  
> **下一步：** 请评审评分和分期方案，确认后进入 Phase 1 实施。
