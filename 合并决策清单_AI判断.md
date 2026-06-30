# 本地项目与云端仓库合并决策清单（AI 判断）

> 生成时间：2026-06-30 23:00  
> 判断依据：`今日功能与代码修改清单.md` + 全量 diff 分析 + 依赖关系核查  
> 合并原则：**以云端 `upstream/main` 为主干**，选择性移植本地"职业方向探索"功能完善  
> 审核要求：以下决策需**人工审核确认**后方可执行，防止项目功能受损

---

## 一、合并背景

| 项目 | 说明 |
|------|------|
| 云端仓库 | `git@github.com:lhj424/career-platform-code.git`（主项目，多人协作） |
| 本地仓库 | 本地 master 分支（职业探索/能力测试功能完善） |
| 历史关系 | **无共同祖先**（两个独立 Git 历史，不可直接 merge） |
| 远程配置 | `origin` = lkiq/Test.git（本地备份）；`upstream` = lhj424/career-platform-code.git（主仓库） |

### 差异规模

| 类别 | 数量 | 含义 |
|------|------|------|
| A 类（云端独有） | 117 | 云端有、本地无的功能文件 → **全部接受** |
| D 类（本地独有） | 32 | 本地有、云端无 → 全为 `.codebuddy/`、`.trae/` 工具配置（已 gitignore），**不带入** |
| M 类（双方都有，内容不同） | 89 | **冲突点**，逐文件决策 |

---

## 二、文件决策清单（AI 判断）

### ✅ 第一类：用本地版本覆盖（本地明确完善，接口一致，依赖满足）

> 依据：`今日功能与代码修改清单.md` 记录的本地完善 + 接口/依赖核查通过

| 文件 | 决策 | 理由 |
|------|------|------|
| `service/impl/CareerExplorationServiceImpl.java` | **用本地** | 本地 1514 行 vs 云端 816 行；核心重构：重复请求缓存、历史去重双源合并、兜底按兴趣推荐、matchScore 规范化、意图分类、追问策略。本地是明确的功能完善版 |
| `service/impl/DeepSeekServiceImpl.java` | **用本地** | 本地有 Resilience4j 重试+熔断优化；`DeepSeekService` 接口双方一致，覆盖安全；pom.xml 中 resilience4j 依赖双方一致 |
| `service/impl/JobMatchingServiceImpl.java` | **用本地** | 本地有匹配分数优化；`JobMatchingService` 接口双方一致，覆盖安全 |
| `controller/student/CareerExplorationController.java` | **用本地** | 配套本地 Service 调整 |
| `dto/request/CareerExplorationRequest.java` | **用本地** | 配套本地 Service 调整（M 类） |
| `resources/prompts/career_exploration.txt` | **用本地** | 本地优化了提示词（42 vs 41 行） |
| `frontend/.../CareerExplorationView.vue` | **用本地** | 本地增加 loading 防重复提交拦截 |
| `frontend/.../ChatWindow.vue` | **用本地** | 本地增加 loading 时禁用输入框和发送按钮 |

**覆盖安全性核查结果：**
- `DeepSeekService` 接口：✅ 双方一致（不在 diff 中）
- `JobMatchingService` 接口：✅ 双方一致（不在 diff 中）
- `JobMatchResponse`、`CareerDirectionResponse`、`PromptTemplateUtil`：✅ 双方一致
- `resilience4j` 依赖：✅ 双方 pom 一致

### 🆕 第二类：本地新增文件带入（云端无）

| 文件 | 决策 | 理由 |
|------|------|------|
| `resources/prompts/career_clarification.txt` | **带入** | 本地新增的追问优化 prompt，`CareerExplorationServiceImpl` 依赖此文件 |

### 🔧 第三类：配置融合（以云端为基础 + 本地补充）

| 文件 | 决策 | 融合点 |
|------|------|--------|
| `backend/pom.xml` | **融合** | 以云端版为基础，**加回**本地的 `project.build.sourceEncoding=UTF-8` 和 `project.reporting.outputEncoding=UTF-8`（修复 Windows 中文编译乱码）；接受云端的 `spring-boot-starter-mail` 依赖 |

### ⚠️ 第四类：高风险——需人工审核（连锁依赖）

> **这是合并的核心风险点，必须人工确认！**

| 文件 | 问题 | AI 建议 | 需人工确认 |
|------|------|---------|------------|
| `entity/CareerProfile.java` | M 类，本地与云端版本不同。本地 `CareerExplorationServiceImpl` 依赖此实体，云端企业端功能也依赖此实体 | **建议融合**：以云端版为基础，补充本地版独有的字段/方法 | 确认本地版有哪些独有字段，云端企业端是否兼容 |
| `entity/JobPosition.java` | M 类，同上。本地岗位匹配逻辑依赖，云端企业端岗位发布也依赖 | **建议融合**：同上 | 确认字段差异，避免破坏云端企业端岗位功能 |

**为什么高风险：**
- 若用本地版覆盖 → 可能删除云端企业端依赖的字段 → 企业端功能受损
- 若用云端版 → 本地 `CareerExplorationServiceImpl` 调用的本地独有字段缺失 → 编译错误
- **融合**是最安全方案，但需逐字段对比

### ☁️ 第五类：以云端为主（云端更丰富 / 云端是主项目主干）

| 文件 | 决策 | 理由 |
|------|------|------|
| `frontend/.../AssessmentView.vue` | **用云端** | 云端 1508 行 vs 本地 1192 行；云端有 AI 分析动画、步骤展示等更丰富 UI |
| `service/impl/AssessmentServiceImpl.java` | **用云端** | 云端为主项目版本（529 vs 517） |
| `service/AssessmentService.java` | **用云端** | 配套云端 Impl |
| `controller/student/AssessmentController.java` | **用云端** | 配套云端 Service |
| 企业端全部 M 类文件 | **用云端** | 云端是主项目，企业端以云端为主（CandidatePool/EnterpriseHome/InterviewSchedule/JobPost/Recommend/TalentMatch 等） |
| 学习路径全部 M 类文件 | **用云端** | 同上（LearningPathView/LearningProgress 等） |
| 聊天/登录/注册 M 类文件 | **用云端** | 同上（ChatView/LoginView/RegisterView 等） |
| `application-dev.yml` / `application.yml` | **用云端** | 云端配置为主（注意：本地 .env 中 API Key 不入库） |
| `start_backend.bat` / `启动后端.bat` | **用云端** | 云端已含正确启动脚本 |
| `db/init.sql` 及 migration | **用云端** | 数据库结构以云端为主 |
| 其余 M 类文件 | **用云端** | 默认以云端主项目为主 |

### 🚫 第六类：不带入（工具配置，已 gitignore）

`.codebuddy/`、`.trae/`、测试脚本（`run_*.py`、`deepseek_*.json`）、`dump.rdb`、`uploads/` 等

---

## 三、推荐执行方案

```
基础：git checkout -b merge/career-enhancements upstream/main

步骤 1：覆盖第一类文件（本地版）
  git checkout master -- <8个文件>

步骤 2：带入第二类文件（本地新增）
  git checkout master -- career_clarification.txt

步骤 3：融合第三类（pom.xml）
  以云端版为基础，手动加回 UTF-8 编码配置

步骤 4：【人工审核】融合第四类（CareerProfile / JobPosition）
  逐字段对比本地 vs 云端，合并双方字段

步骤 5：第五类保持云端版（无需操作）

步骤 6：提交到 merge/career-enhancements 分支，人工验证后推送
```

---

## 四、待人工确认事项

1. **CareerProfile.java 和 JobPosition.java 的融合方案**：是否同意 AI 逐字段对比后融合？还是倾向于全用本地版/全用云端版？
2. **AssessmentView.vue 用云端版**：本地能力测评前端是否有云端未包含的关键逻辑？（从 diff 看 local 版主要是少了云端新增的 UI，逻辑差异较小）
3. **是否在新分支执行**：建议在 `merge/career-enhancements` 分支执行，验证无误后再合并到 main，不影响主干
4. **推送目标**：合并完成后推送到 `upstream`（lhj424 仓库，需有写权限）还是推送到 `origin`（lkiq/Test 个人仓库）？

---

> 请审核以上决策。确认后 AI 将按方案执行合并。
