const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, BorderStyle, WidthType, ShadingType,
  LevelFormat, Header, Footer, PageNumber
} = require('docx');
const fs = require('fs');

const A4_CONTENT_WIDTH = 9026; // 11906 - 2880 (1 inch margins)
const pageMargin = { top: 1440, right: 1440, bottom: 1440, left: 1440 };

const border = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
const tableBorders = { top: border, bottom: border, left: border, right: border };

function cell(text, width, options = {}) {
  return new TableCell({
    borders: tableBorders,
    width: { size: width, type: WidthType.DXA },
    shading: options.fill ? { fill: options.fill, type: ShadingType.CLEAR } : undefined,
    margins: { top: 80, bottom: 80, left: 120, right: 120 },
    verticalAlign: 'center',
    children: [new Paragraph({
      spacing: { before: 0, after: 0 },
      children: [new TextRun({ text: text, bold: !!options.bold, size: 21, font: 'Arial' })]
    })]
  });
}

function multiCell(lines, width, options = {}) {
  return new TableCell({
    borders: tableBorders,
    width: { size: width, type: WidthType.DXA },
    shading: options.fill ? { fill: options.fill, type: ShadingType.CLEAR } : undefined,
    margins: { top: 80, bottom: 80, left: 120, right: 120 },
    children: lines.map(line => new Paragraph({
      spacing: { before: 40, after: 40 },
      children: [new TextRun({ text: line, size: 21, font: 'Arial' })]
    }))
  });
}

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 240, after: 160 },
    children: [new TextRun({ text, bold: true, size: 32, font: 'Arial' })]
  });
}

function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 200, after: 120 },
    children: [new TextRun({ text, bold: true, size: 28, font: 'Arial' })]
  });
}

function h3(text) {
  return new Paragraph({
    spacing: { before: 160, after: 80 },
    children: [new TextRun({ text, bold: true, size: 24, font: 'Arial' })]
  });
}

function p(text, options = {}) {
  return new Paragraph({
    spacing: { before: 80, after: 80, line: 360 },
    children: [new TextRun({ text, size: 24, font: 'Arial', ...options })]
  });
}

function bullet(text, reference = 'bullets') {
  return new Paragraph({
    numbering: { reference, level: 0 },
    spacing: { before: 40, after: 40, line: 340 },
    children: [new TextRun({ text, size: 24, font: 'Arial' })]
  });
}

function numbered(text, reference = 'numbers') {
  return new Paragraph({
    numbering: { reference, level: 0 },
    spacing: { before: 40, after: 40, line: 340 },
    children: [new TextRun({ text, size: 24, font: 'Arial' })]
  });
}

const doc = new Document({
  styles: {
    default: { document: { run: { font: 'Arial', size: 24 } } },
    paragraphStyles: [
      { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 32, bold: true, font: 'Arial' },
        paragraph: { spacing: { before: 240, after: 160 }, outlineLevel: 0 } },
      { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 28, bold: true, font: 'Arial' },
        paragraph: { spacing: { before: 200, after: 120 }, outlineLevel: 1 } },
    ]
  },
  numbering: {
    config: [
      { reference: 'bullets',
        levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
      { reference: 'numbers',
        levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } } }] },
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: pageMargin
      }
    },
    headers: {
      default: new Header({ children: [new Paragraph({
        alignment: AlignmentType.RIGHT,
        children: [new TextRun({ text: '求职平台前端改造计划书', size: 18, color: '888888', font: 'Arial' })]
      })] })
    },
    footers: {
      default: new Footer({ children: [new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [
          new TextRun({ text: '第 ', size: 20, font: 'Arial' }),
          new TextRun({ children: [PageNumber.CURRENT], size: 20, font: 'Arial' }),
          new TextRun({ text: ' 页', size: 20, font: 'Arial' })
        ]
      })] })
    },
    children: [
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 600, after: 200 },
        children: [new TextRun({ text: '求职平台前端界面升级改造计划书', bold: true, size: 44, font: 'Arial' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 600 },
        children: [new TextRun({ text: '——让系统更贴近真实招聘/求职平台体验', size: 28, color: '555555', font: 'Arial' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 800 },
        children: [new TextRun({ text: '版本：V1.0    日期：2026-06-27', size: 22, color: '666666', font: 'Arial' })]
      }),

      h1('1. 项目背景与目标'),
      p('当前系统已具备学生端 AI 求职辅导、企业端项目推荐、管理端数据看板等基础能力，前端技术栈为 Vue 3 + Vite + Element Plus + Pinia。但从“真实求职平台”视角看，现有页面仍存在以下典型问题：'),
      bullet('页面整体偏“后台管理”风格：左侧深色菜单 + 右侧简单表单/表格，缺少招聘平台常见的信息流、卡片 Feed、信息密度和视觉层级。'),
      bullet('岗位列表只有“标题 + 城市 + 薪资 + 简短 JD”，缺少公司信息、发布时间、经验要求、福利标签、HR 状态、收藏/投递/沟通等真实招聘场景要素。'),
      bullet('学生端缺少“我的投递”“面试邀约”“消息通知”“收藏岗位”等求职核心链路；企业端缺少“职位管理”“候选人库”“招聘流程”等 HR 工作台。'),
      bullet('空数据、加载中、网络错误、操作成功等状态反馈不够统一，部分页面直接留白或仅显示文字。'),
      bullet('响应式适配较弱，移动端/小屏设备体验不佳。'),
      p('本次改造的目标是在尽量复用现有接口的前提下，通过前端组件升级、页面重构、字段映射补齐、交互优化，使平台在视觉与体验上更接近 BOSS 直聘、智联招聘、猎聘等真实求职产品，同时保持与现有后端服务的兼容性。'),

      h1('2. 现状盘点'),
      h2('2.1 技术栈与目录结构'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [2400, 6626],
        rows: [
          new TableRow({ children: [cell('框架/工具', 2400, { bold: true, fill: 'E8F4FC' }), cell('说明', 6626, { bold: true, fill: 'E8F4FC' })] }),
          new TableRow({ children: [cell('Vue 3 + Vite', 2400), cell('组合式 API + <script setup>，构建工具 Vite', 6626)] }),
          new TableRow({ children: [cell('Element Plus', 2400), cell('UI 组件库，当前大量直接使用 el-card / el-table / el-form', 6626)] }),
          new TableRow({ children: [cell('Pinia', 2400), cell('状态管理，已有 user / profile / assessment 三个 store', 6626)] }),
          new TableRow({ children: [cell('Axios', 2400), cell('API 请求封装在 src/api/request.ts', 6626)] }),
          new TableRow({ children: [cell('ECharts', 2400), cell('已有 RadarChart 组件，可用于扩展数据可视化', 6626)] }),
          new TableRow({ children: [cell('SCSS', 2400), cell('全局样式 global.scss，含 --primary-color、page-card、page-title 等公共类', 6626)] }),
        ]
      }),
      p(''),
      p('前端目录结构：'),
      bullet('src/views/：登录/注册、学生端、企业端、管理端、客服、错误页。'),
      bullet('src/components/：布局（AppLayout / HeaderBar / Sidebar）、图表（RadarChart）、通用（ChatWindow / FileUpload / SkillTag）。'),
      bullet('src/api/：auth / student / enterprise / admin / customerService。'),
      bullet('src/stores/：user / profile / assessment。'),

      h2('2.2 现有页面与问题速览'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [2000, 3500, 3526],
        rows: [
          new TableRow({ children: [
            cell('页面模块', 2000, { bold: true, fill: 'E8F4FC' }),
            cell('主要问题', 3500, { bold: true, fill: 'E8F4FC' }),
            cell('改造方向', 3526, { bold: true, fill: 'E8F4FC' })
          ]}),
          new TableRow({ children: [cell('全局布局', 2000), cell('侧边栏深色固定、顶部仅面包屑+头像，缺少搜索/通知/快捷入口', 3500), cell('升级为顶部全局导航 + 分组侧边栏 + 消息中心', 3526)] }),
          new TableRow({ children: [cell('登录/注册', 2000), cell('视觉已较完整，但手机号/验证码、企业信息未接入', 3500), cell('补充短信验证码 UI、企业注册扩展字段、隐私协议弹窗', 3526)] }),
          new TableRow({ children: [cell('学生首页', 2000), cell('只有步骤条和三个快捷入口，缺少数据概览与 Feed', 3500), cell('改造为“求职工作台”数据看板', 3526)] }),
          new TableRow({ children: [cell('岗位匹配', 2000), cell('卡片信息稀疏、无筛选、无详情、无收藏投递', 3500), cell('重构为真实岗位列表 + 详情 + 收藏/投递', 3526)] }),
          new TableRow({ children: [cell('求职画像', 2000), cell('纯表单，缺少简历预览与经历增删', 3500), cell('升级为简历编辑器 + 左侧实时预览', 3526)] }),
          new TableRow({ children: [cell('简历优化', 2000), cell('上传后仅展示评分和建议，缺少历史与改写应用', 3500), cell('增加评分历史、分模块诊断、建议一键应用', 3526)] }),
          new TableRow({ children: [cell('模拟面试', 2000), cell('聊天界面简单，缺少面试历史与报告沉淀', 3500), cell('增强聊天室体验、历史列表、报告可视化', 3526)] }),
          new TableRow({ children: [cell('企业端', 2000), cell('仅项目推荐，HR 无法管理职位/候选人/流程', 3500), cell('新增职位管理、候选人库、面试日程', 3526)] }),
          new TableRow({ children: [cell('管理端', 2000), cell('只有数字统计卡片，缺少图表与审核', 3500), cell('引入 ECharts 图表、企业审核、岗位管理', 3526)] }),
        ]
      }),
      p(''),

      h1('3. 改造总体思路'),
      numbered('信息流优先：将“岗位”作为平台核心资产，用卡片 Feed 承载公司、薪资、地点、标签、时间、操作按钮等高密度信息。'),
      numbered('角色工作台化：学生端首页 = 数据概览 + 待办 + 推荐流；企业端首页 = 待处理简历 + 在招职位 + 日程 + 推荐人才。'),
      numbered('补齐真实字段：在前端通过字段映射、默认值、mock 补全公司名、经验、学历、福利、发布时间、HR 信息等字段；必要时推动后端扩展。'),
      numbered('统一状态反馈：抽象 EmptyState / LoadingSkeleton / ErrorRetry / GlobalMessage 组件，覆盖空数据、加载、错误、成功提示。'),
      numbered('移动端适配：对登录页、岗位列表、简历预览等高频页面做响应式改造，小屏下侧边栏收起、卡片单列、筛选抽屉化。'),
      numbered('渐进式实施：优先改造全局布局和学生端核心链路（岗位/简历/投递），再扩展企业端、管理端与 AI 工具。'),

      h1('4. 具体改造清单'),
      h2('4.1 全局布局与导航'),
      h3('要修改什么'),
      bullet('HeaderBar.vue：顶部仅面包屑+头像，功能单一。'),
      bullet('Sidebar.vue：所有菜单平铺，无分组，深色风格偏后台。'),
      bullet('global.scss / AppLayout.vue：缺少 CSS 变量体系、响应式断点、全局搜索/通知入口。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('HeaderBar 升级为“顶部全局导航条”：左侧保留面包屑与页面标题；中间加入全局岗位搜索框（带历史记录与热门搜索）；右侧加入消息通知铃铛（红点 + 下拉列表）、站内信入口、用户头像下拉菜单（个人设置/退出）。'),
      bullet('Sidebar 分组展示：学生端分“求职工作台（首页/岗位/投递/收藏/消息）”“AI 辅导（测评/方向/差距/学习/简历/面试）”“个人中心（画像/设置）”；企业端分“招聘概览”“职位管理”“候选人”“项目推荐”。'),
      bullet('引入 CSS 变量体系：在 global.scss 中定义 --primary-color、--success-color、--warning-color、--danger-color、--text-primary、--text-secondary、--bg-page、--border-color、--sidebar-width、--header-height，所有组件统一引用。'),
      bullet('响应式：992px 以下侧边栏自动收起为图标抽屉；岗位列表小屏下筛选区变为 el-drawer；卡片单列展示。'),
      bullet('操作步骤：1) 改造 HeaderBar.vue / Sidebar.vue；2) 新建 components/common/GlobalSearch.vue、NotificationBell.vue；3) 调整 global.scss 与 AppLayout.vue 的 flex 布局。'),

      h2('4.2 登录/注册页'),
      h3('要修改什么'),
      bullet('LoginView.vue：已有账号密码登录，但“扫码登录”“短信登录”Tab 仅占位，无真实功能。'),
      bullet('RegisterView.vue：企业注册未要求公司信息，角色选择后字段单一。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('登录页 Tab 保留“账号登录”优先，将“短信登录”补全为手机号 + 验证码 UI（验证码按钮 60s 倒计时，后端接口可先行 mock）。'),
      bullet('扫码登录 Tab 展示二维码占位图 + 刷新机制，提示“请使用微信/APP 扫码”。'),
      bullet('注册页根据角色动态表单：学生需补充姓名、学校、专业、手机号；HR 需补充公司名称、所属行业、企业邮箱、职位。'),
      bullet('增加《用户服务协议》《隐私政策》弹窗或跳转，注册按钮在未勾选时禁用。'),
      bullet('OAuth 登录图标目前为 Element Plus 图标，可替换为企业微信/钉钉/飞书真实 SVG 图标（仅 UI，后续后端对接）。'),

      h2('4.3 学生首页（求职工作台）'),
      h3('要修改什么'),
      bullet('StudentHome.vue：当前为步骤条 + 3 个快捷入口，信息单薄，没有数据概览。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('顶部数据卡：使用 el-statistic 展示“投递数、待面试、收藏岗位、简历评分、待学任务、消息通知”，数据来自 progress/overview、getInterviewHistory、getResumeHistory 等接口聚合。'),
      bullet('中部求职流程步骤条：保留现有步骤条，但增加“当前步骤说明”与“去完成”按钮，点击跳转到对应页面。'),
      bullet('右侧推荐区：展示“为你推荐”岗位卡片（调用 recommendJobs）、“热门公司”占位列表、“平台公告”。'),
      bullet('快捷入口升级：使用大图标 + 标题 + 描述 + 操作按钮的卡片，hover 时带阴影动效。'),
      bullet('数据为空时显示 EmptyState 组件并引导用户完善画像。'),

      h2('4.4 岗位匹配（核心改造）'),
      h3('要修改什么'),
      bullet('JobMatchingView.vue：只有关键词、城市筛选，岗位卡片信息少，无详情、无收藏、无投递。'),
      bullet('后端 job_position 表字段有限：title / direction / jd / city / salaryRange，缺少 companyName、experience、education、jobType、welfare、publishTime、hrName 等。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('重构为“左筛选 + 右列表”经典招聘平台布局：'),
      bullet('左侧筛选栏使用 el-collapse：城市（多选）、岗位方向（后端/前端/数据/产品/测试等）、薪资范围（单选）、经验要求（应届/1-3年/3-5年/5年以上）、学历要求、工作性质（全职/实习/兼职）、技能标签（多选）。'),
      bullet('右侧岗位列表：每条岗位显示公司 logo 占位、公司名称、岗位标题、薪资高亮、地点、经验、学历、福利标签、发布时间、匹配度标签；底部操作区：立即投递、收藏、聊一聊（进入客服/沟通页）。'),
      bullet('新增 JobCard.vue 组件统一岗位卡片样式，支持列表模式与卡片模式切换。'),
      bullet('新增 JobFilter.vue 组件封装所有筛选条件，支持“清空筛选”。'),
      bullet('新增岗位详情页 JobDetailView.vue（路由 /student/job/:id）：展示完整 JD、公司介绍、技能要求、薪资详情、相似岗位推荐；底部固定操作栏“投递简历/收藏/返回”。'),
      bullet('分页与排序：列表底部增加 el-pagination；顶部增加排序选项（智能推荐/最新发布/薪资从高到低）。'),
      bullet('字段映射处理：后端返回字段不足时，前端通过工具函数 enrichJob(job) 补全 companyName（从 direction 或 mock 公司库）、experience、education、publishTime 等默认值，并标注为“前端 mock，后续需后端扩展”。'),
      bullet('接口调整：扩展 searchJobs(keyword, city) 为 searchJobs({ keyword, city, direction, salaryMin, salaryMax, experience, education, jobType, skillIds, sort, page, size })，若后端未支持则先前端过滤+mock 分页。'),
      bullet('状态反馈：列表加载使用骨架屏 JobListSkeleton.vue；无结果时显示“未找到合适岗位，试试调整筛选条件”。'),

      h2('4.5 求职画像（简历编辑器）'),
      h3('要修改什么'),
      bullet('ProfileView.vue：当前为左右两列表单，缺少简历预览、经历增删、头像上传。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('改为“左侧简历预览 + 右侧编辑表单”的双栏布局，实时联动。'),
      bullet('基础信息：头像上传（Element Plus el-upload 到 /student/resume/upload 复用文件上传接口）、姓名、手机号、邮箱、期望城市、期望职位、期望薪资、求职状态。'),
      bullet('教育经历：支持新增/删除多条（学校、专业、学历、时间段、在校经历）。'),
      bullet('工作经历/实习经历：公司、职位、时间段、工作内容（支持多条）。'),
      bullet('项目经历：项目名称、角色、时间段、项目描述、技术栈、项目链接。'),
      bullet('技能标签：保留现有回车添加标签，但改为从技能词典联想选择，减少拼写不一致。'),
      bullet('个人优势：求职优势/自我评价文本框。'),
      bullet('组件拆分：新增 ResumePreview.vue（左侧预览）、ExperienceEditor.vue（经历增删）、AvatarUploader.vue。'),
      bullet('后端依赖：CareerProfile 实体当前字段有限，需要后端扩展字段或使用 JSON 字段存储经历数组；若后端无法立即支持，可先本地结构化并在保存时序列化到现有字段。'),

      h2('4.6 简历智能优化'),
      h3('要修改什么'),
      bullet('ResumeOptimizeView.vue：上传后只显示一次评分和建议，缺少历史、分模块诊断、建议应用。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('顶部增加“简历评分历史”时间轴（调用 getResumeHistory），可切换历史报告。'),
      bullet('分模块诊断：将建议按“个人信息/教育背景/工作经历/项目经历/技能/自我评价”分类展示，每个模块显示得分条与问题列表。'),
      bullet('AI 改写建议：每条建议旁增加“一键应用”按钮，将建议文本写入对应的简历字段（需与 ProfileView 共享 Pinia profile store）。'),
      bullet('对比视图：左右分屏展示“原简历内容”与“AI 优化后内容”。'),
      bullet('导出功能：提供“导出简历 PDF”按钮（可先使用 window.print 或后续接入 html2pdf.js）。'),
      bullet('状态：上传中显示进度条；分析中显示 Skeleton；无简历时显示 EmptyState。'),

      h2('4.7 模拟面试'),
      h3('要修改什么'),
      bullet('InterviewView.vue：聊天窗口简单，缺少面试历史、题目卡片、报告沉淀。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('新增 InterviewHistory.vue 列表：展示历史面试记录（岗位、类型、时间、综合评分），点击可查看报告。'),
      bullet('面试房间界面：左侧题目/进度，右侧 ChatWindow；消息气泡区分面试官/求职者，支持 Markdown 简单渲染。'),
      bullet('题目卡片：每道题显示题号、考点标签、难度、建议回答时长。'),
      bullet('面试报告页：使用 RadarChart 展示表达能力、技术深度、逻辑思维、沟通能力等维度；列出亮点、改进点、推荐学习资源。'),
      bullet('结束面试后增加“再练一次”与“查看学习路径”入口。'),

      h2('4.8 能力测评'),
      h3('要修改什么'),
      bullet('AssessmentView.vue：答题流程较简单，缺少历史与能力趋势。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('开始页展示测评分类（综合测评/编程能力/产品思维/沟通表达）与预计时长。'),
      bullet('答题中增加题目收藏、标记不确定、倒计时显示。'),
      bullet('结果页增加“能力雷达长期趋势”：展示多次测评各维度分数折线/面积图。'),
      bullet('新增测评历史列表（调用 getAssessmentHistory）。'),

      h2('4.9 差距分析与学习路径'),
      h3('要修改什么'),
      bullet('GapAnalysisView.vue / LearningPathView.vue：报告展示偏技术，缺少行动感。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('差距分析页：顶部展示目标岗位卡片；中部使用 RadarChart 对比“当前能力”与“岗位要求”；下方技能缺口列表增加“去学习”按钮直达对应资源。'),
      bullet('学习路径页：使用 Kanban/Timeline 展示待学习/学习中/已完成任务，支持拖拽打卡。'),
      bullet('学习进度页：增加周/月学习统计与最近学习资源推荐。'),

      h2('4.10 企业端改造'),
      h3('要修改什么'),
      bullet('EnterpriseHome.vue / RecommendView.vue：HR 工作台薄弱，只有项目推荐。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('企业首页升级为 HR 工作台：顶部数据卡（在招职位、待处理简历、今日面试、沟通中候选人）；中部待办列表（新投递、面试安排、Offer 审批）；右侧人才推荐。'),
      bullet('新增职位管理 JobPostView.vue：列表展示职位状态（招聘中/已下架/审核中）；新增/编辑职位表单包含公司名称、职位名称、方向、薪资、城市、经验、学历、职位描述、技能要求、福利待遇、招聘人数。'),
      bullet('新增候选人库 CandidatePoolView.vue：简历列表/卡片，支持按岗位、匹配度、状态筛选；操作：查看简历、邀约面试、不合适、发送 Offer。'),
      bullet('新增面试日程 InterviewScheduleView.vue：日历/列表展示已安排面试，支持新建面试（时间、方式、面试官、备注）。'),
      bullet('保留并优化项目推荐 RecommendView.vue：增加推荐历史与一键联系候选人。'),
      bullet('后端依赖：企业端需要新增大量接口（职位 CRUD、简历投递、流程状态、面试日程），建议与后端同步排期。'),

      h2('4.11 管理端改造'),
      h3('要修改什么'),
      bullet('AdminHome.vue：只有 6 个统计卡片，缺少可视化图表。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('数据看板：使用 ECharts 增加“用户增长曲线”“测评/面试/匹配趋势图”“热门岗位词云”“技能缺口 Top10 柱状图”。'),
      bullet('用户管理：保留 UserManagement.vue，增加角色筛选、状态开关、重置密码弹窗。'),
      bullet('技能词典：保留 SkillDictionary.vue，增加导入/导出、批量分类。'),
      bullet('新增企业管理 EnterpriseAudit.vue：审核企业注册、查看企业资质、禁用/启用企业账号。'),
      bullet('新增岗位管理 JobManagement.vue：审核企业发布职位、下架违规职位。'),

      h2('4.12 智能客服'),
      h3('要修改什么'),
      bullet('CustomerServiceView.vue：当前为独立页面，入口较深。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('将客服改造为全局悬浮聊天机器人 ChatBot.vue，固定在页面右下角，随时唤起。'),
      bullet('机器人支持常见问题快捷按钮、历史会话、人工客服转接占位。'),
      bullet('原 CustomerServiceView.vue 改造为“帮助中心”页面，展示 FAQ 分类与使用指南。'),

      h2('4.13 通用状态与体验组件'),
      h3('要修改什么'),
      bullet('各页面空数据、加载、错误提示不统一。'),
      h3('怎么修改 / 具体怎么做'),
      bullet('新建 EmptyState.vue：根据场景显示不同插图与文案（无岗位、无简历、无消息、无结果、404）。'),
      bullet('新建 LoadingSkeleton.vue：列表骨架屏、卡片骨架屏、详情骨架屏。'),
      bullet('新建 ErrorRetry.vue：网络错误/接口异常时展示错误码、错误信息、重试按钮。'),
      bullet('封装 useAsync 组合式函数：统一处理 loading / error / retry，替代各页面重复 try/catch。'),
      bullet('request.ts 拦截器：统一 401 跳转登录、5xx 提示“服务繁忙”、业务错误码翻译为中文消息。'),

      h1('5. 接口与字段映射调整'),
      p('前端改造过程中，需明确现有接口字段与目标展示字段的映射关系。下表列出核心映射：'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [1800, 2200, 2500, 2526],
        rows: [
          new TableRow({ children: [
            cell('模块', 1800, { bold: true, fill: 'E8F4FC' }),
            cell('目标字段', 2200, { bold: true, fill: 'E8F4FC' }),
            cell('现有字段/来源', 2500, { bold: true, fill: 'E8F4FC' }),
            cell('处理方式', 2526, { bold: true, fill: 'E8F4FC' })
          ]}),
          new TableRow({ children: [
            cell('岗位列表', 1800),
            cell('companyName / logo / publishTime / experience / education / welfare / hrName', 2200),
            cell('job_position 表无以上字段', 2500),
            cell('前端 enrichJob 补全默认值 + 标注后端扩展', 2526)
          ]}),
          new TableRow({ children: [
            cell('岗位搜索', 1800),
            cell('direction / salaryMin / salaryMax / experience / jobType / page / size / sort', 2200),
            cell('仅 keyword、city', 2500),
            cell('扩展 searchJobs 参数；后端未支持则前端过滤+mock 分页', 2526)
          ]}),
          new TableRow({ children: [
            cell('简历画像', 1800),
            cell('头像 / 教育经历 / 工作经历 / 项目经历 / 求职优势', 2200),
            cell('career_profile 字段有限', 2500),
            cell('后端扩展字段 或 JSON 序列化存储', 2526)
          ]}),
          new TableRow({ children: [
            cell('投递/收藏', 1800),
            cell('投递记录 / 收藏列表 / 状态', 2200),
            cell('无相关接口', 2500),
            cell('后端新增 job_application / job_favorite 表及接口', 2526)
          ]}),
          new TableRow({ children: [
            cell('企业职位', 1800),
            cell('职位 CRUD / 上下架 / 审核', 2200),
            cell('无 HR 职位管理接口', 2500),
            cell('后端新增 enterprise/job 模块', 2526)
          ]}),
          new TableRow({ children: [
            cell('候选人流程', 1800),
            cell('简历状态 / 面试日程 / Offer', 2200),
            cell('无相关接口', 2500),
            cell('后端新增 recruitment_process 模块', 2526)
          ]}),
        ]
      }),
      p(''),
      p('建议与后端协商优先扩展岗位表字段（company_name、experience、education、welfare、publish_time、hr_name）和学生投递/收藏接口，这是前端“真实感”提升的关键依赖。'),

      h1('6. 技术实施方案'),
      h2('6.1 组件拆分策略'),
      bullet('通用组件：components/common/EmptyState.vue、LoadingSkeleton.vue、ErrorRetry.vue、GlobalSearch.vue、NotificationBell.vue、ChatBot.vue、AvatarUploader.vue。'),
      bullet('业务组件：components/job/JobCard.vue、JobFilter.vue、JobList.vue、JobDetailHeader.vue；components/resume/ResumePreview.vue、ExperienceEditor.vue；components/interview/InterviewHistory.vue、InterviewReport.vue。'),
      bullet('布局组件：优化 layout/AppLayout.vue、HeaderBar.vue、Sidebar.vue，新增 MobileDrawer.vue。'),
      h2('6.2 样式与响应式'),
      bullet('全局使用 CSS 变量，统一色彩、字号、间距、圆角、阴影。'),
      bullet('定义断点：≥1200px 双栏、992px-1200px 单栏+侧边栏、<992px 移动端抽屉。'),
      bullet('岗位列表小屏下：筛选按钮打开 el-drawer，卡片单列，详情页全屏。'),
      h2('6.3 状态管理'),
      bullet('新增 useJobStore：管理岗位列表、筛选条件、分页、收藏/投递状态缓存。'),
      bullet('扩展 useProfileStore：存储完整简历结构（教育/工作/项目经历）。'),
      bullet('新增 useNotificationStore：管理消息通知、红点计数。'),
      h2('6.4 路由调整'),
      bullet('新增 /student/jobs 作为岗位列表默认页；/student/job/:id 为详情页。'),
      bullet('新增 /student/applications、/student/favorites、/student/messages。'),
      bullet('新增 /enterprise/jobs、/enterprise/candidates、/enterprise/interviews。'),
      bullet('新增 /admin/enterprises、/admin/job-management。'),
      h2('6.5 性能优化'),
      bullet('岗位列表使用虚拟滚动（el-table-infinite-scroll 或自定义 VirtualList）应对大量数据。'),
      bullet('图片懒加载：公司 logo、用户头像使用 el-image lazy。'),
      bullet('keep-alive 缓存岗位列表、简历编辑等页面状态。'),
      bullet('路由懒加载已存在，继续保持。'),

      h1('7. 实施计划（建议阶段）'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [1200, 3500, 2800, 1526],
        rows: [
          new TableRow({ children: [
            cell('阶段', 1200, { bold: true, fill: 'E8F4FC' }),
            cell('内容', 3500, { bold: true, fill: 'E8F4FC' }),
            cell('目标', 2800, { bold: true, fill: 'E8F4FC' }),
            cell('预计工期', 1526, { bold: true, fill: 'E8F4FC' })
          ]}),
          new TableRow({ children: [
            cell('Phase 1', 1200),
            cell('全局布局、Header/Sidebar、空/错/加载组件、CSS 变量、响应式基础', 3500),
            cell('统一视觉与状态反馈', 2800),
            cell('2-3 天', 1526)
          ]}),
          new TableRow({ children: [
            cell('Phase 2', 1200),
            cell('学生首页工作台、岗位列表/筛选/详情、收藏/投递、简历编辑器', 3500),
            cell('核心求职链路可用', 2800),
            cell('5-7 天', 1526)
          ]}),
          new TableRow({ children: [
            cell('Phase 3', 1200),
            cell('简历优化、模拟面试、能力测评、差距分析、学习路径打磨', 3500),
            cell('AI 辅导体验提升', 2800),
            cell('4-5 天', 1526)
          ]}),
          new TableRow({ children: [
            cell('Phase 4', 1200),
            cell('企业端 HR 工作台、职位管理、候选人库、面试日程', 3500),
            cell('企业招聘流程闭环', 2800),
            cell('5-7 天', 1526)
          ]}),
          new TableRow({ children: [
            cell('Phase 5', 1200),
            cell('管理端数据看板图表、企业/岗位审核、用户管理增强', 3500),
            cell('运营管理能力完善', 2800),
            cell('3-4 天', 1526)
          ]}),
          new TableRow({ children: [
            cell('Phase 6', 1200),
            cell('全量联调、响应式测试、性能优化、文档与代码整理', 3500),
            cell('稳定上线', 2800),
            cell('3-4 天', 1526)
          ]}),
        ]
      }),
      p(''),

      h1('8. 依赖与风险'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [2200, 3500, 3326],
        rows: [
          new TableRow({ children: [
            cell('风险点', 2200, { bold: true, fill: 'E8F4FC' }),
            cell('影响', 3500, { bold: true, fill: 'E8F4FC' }),
            cell('应对措施', 3326, { bold: true, fill: 'E8F4FC' })
          ]}),
          new TableRow({ children: [
            cell('后端字段扩展不及时', 2200),
            cell('岗位/简历/企业信息展示不完整', 3500),
            cell('前端先使用 mock 数据与 enrichJob 函数补全，接口ready后替换', 3326)
          ]}),
          new TableRow({ children: [
            cell('企业端接口缺失', 2200),
            cell('职位/候选人/流程功能无法真正闭环', 3500),
            cell('与后端同步排期，优先保证学生端核心链路', 3326)
          ]}),
          new TableRow({ children: [
            cell('改造范围过大导致工期失控', 2200),
            cell('无法按期交付', 3500),
            cell('按 Phase 分阶段交付，每阶段可独立运行与演示', 3326)
          ]}),
          new TableRow({ children: [
            cell('Element Plus 自定义样式冲突', 2200),
            cell('组件样式被覆盖或升级后失效', 3500),
            cell('使用 CSS 变量与 ::v-deep 集中管理，避免散弹式修改', 3326)
          ]}),
          new TableRow({ children: [
            cell('移动端适配不足', 2200),
            cell('小屏体验差', 3500),
            cell('在 Phase 1 即确定断点与移动端交互规范', 3326)
          ]}),
        ]
      }),
      p(''),

      h1('9. 验收标准'),
      bullet('视觉真实感：岗位列表、简历编辑器、HR 工作台在信息密度、布局、色彩上接近主流招聘平台。'),
      bullet('功能完整性：学生可完成“搜索岗位 - 查看详情 - 投递/收藏 - 查看投递记录 - 模拟面试 - 学习提升”主流程；HR 可完成“发布职位 - 查看候选人 - 邀约面试”主流程（后端接口配合）。'),
      bullet('状态反馈覆盖：所有列表/详情/表单均有 loading、空数据、错误、成功提示。'),
      bullet('响应式：在 1366px、992px、768px、375px 宽度下无严重布局错乱。'),
      bullet('接口稳定：改造后现有登录、测评、面试、推荐等接口仍可正常调用。'),
      bullet('代码质量：组件拆分合理、命名清晰、重复代码减少、无明显 console 报错。'),

      h1('10. 附录：主要文件改动清单'),
      new Table({
        width: { size: A4_CONTENT_WIDTH, type: WidthType.DXA },
        columnWidths: [2800, 6226],
        rows: [
          new TableRow({ children: [cell('文件/目录', 2800, { bold: true, fill: 'E8F4FC' }), cell('改动说明', 6226, { bold: true, fill: 'E8F4FC' })] }),
          new TableRow({ children: [cell('src/assets/styles/global.scss', 2800), cell('新增 CSS 变量、响应式断点、全局工具类', 6226)] }),
          new TableRow({ children: [cell('src/components/layout/*', 2800), cell('HeaderBar / Sidebar / AppLayout 重构', 6226)] }),
          new TableRow({ children: [cell('src/components/common/*', 2800), cell('新增 EmptyState、LoadingSkeleton、ErrorRetry、GlobalSearch、NotificationBell、ChatBot、AvatarUploader', 6226)] }),
          new TableRow({ children: [cell('src/components/job/*', 2800), cell('新增 JobCard、JobFilter、JobList、JobDetailHeader', 6226)] }),
          new TableRow({ children: [cell('src/components/resume/*', 2800), cell('新增 ResumePreview、ExperienceEditor', 6226)] }),
          new TableRow({ children: [cell('src/components/interview/*', 2800), cell('新增 InterviewHistory、InterviewReport', 6226)] }),
          new TableRow({ children: [cell('src/views/student/*.vue', 2800), cell('StudentHome、JobMatchingView、ProfileView、ResumeOptimizeView、InterviewView 等重构；新增 JobDetailView、ApplicationsView、FavoritesView', 6226)] }),
          new TableRow({ children: [cell('src/views/enterprise/*.vue', 2800), cell('EnterpriseHome、RecommendView 重构；新增 JobPostView、CandidatePoolView、InterviewScheduleView', 6226)] }),
          new TableRow({ children: [cell('src/views/admin/*.vue', 2800), cell('AdminHome 增加图表；新增 EnterpriseAudit、JobManagement', 6226)] }),
          new TableRow({ children: [cell('src/views/customer/*.vue', 2800), cell('CustomerServiceView 改造为帮助中心', 6226)] }),
          new TableRow({ children: [cell('src/router/index.ts', 2800), cell('新增岗位详情、投递/收藏/消息、企业职位/候选人/日程、管理端审核等路由', 6226)] }),
          new TableRow({ children: [cell('src/api/*.ts', 2800), cell('扩展 searchJobs、新增投递/收藏/消息/企业职位/候选人/日程/审核等接口', 6226)] }),
          new TableRow({ children: [cell('src/stores/*.ts', 2800), cell('新增 useJobStore、useNotificationStore；扩展 profile store', 6226)] }),
          new TableRow({ children: [cell('src/utils/*', 2800), cell('新增 jobEnrich.ts、useAsync.ts、formatters.ts', 6226)] }),
        ]
      }),
      p(''),
      p('以上计划书可作为本次前端改造的执行依据。建议先按 Phase 分阶段落地，每阶段完成后进行小范围演示与评审，及时调整细节。'),
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  const outPath = 'd:\\企业实训作业\\codebuddy\\gitcode\\软件实训项目-code\\软件实训项目-code\\求职平台前端改造计划书_V1.0.docx';
  fs.writeFileSync(outPath, buffer);
  console.log('已生成：' + outPath);
});
