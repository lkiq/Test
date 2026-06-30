<template>
  <div class="assessment-page">
    <!-- ========== 加载中：查询历史测评结果 ========== -->
    <div v-if="loading" class="result-loading-wrap">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <span>正在加载测评结果...</span>
    </div>

    <!-- ========== 未开始：AI主题开场 ========== -->
    <div v-else-if="!started && !hasExistingResult" class="assessment-hero">
      <div class="hero-bg-decor">
        <div class="floating-icon i1">🧠</div>
        <div class="floating-icon i2">💡</div>
        <div class="floating-icon i3">🎯</div>
        <div class="floating-icon i4">⚡</div>
        <div class="floating-icon i5">🔬</div>
        <div class="hero-dot-grid"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>AI 驱动
        </div>
        <h1 class="hero-title">
          <span class="title-gradient">五维能力测评</span>
        </h1>
        <p class="hero-desc">基于AI算法全面评估你的编程、逻辑、产品思维、技术素养与沟通表达能力</p>
        
        <div class="dimension-cards">
          <div class="dim-card" v-for="dim in dimensions" :key="dim.key">
            <div class="dim-icon" :style="{ background: dim.color }">{{ dim.icon }}</div>
            <div class="dim-name">{{ dim.label }}</div>
            <div class="dim-hint">{{ dim.hint }}</div>
          </div>
        </div>

        <div class="hero-info">
          <div class="info-item"><span class="info-value">25</span><span class="info-label">精选题目</span></div>
          <div class="info-divider"></div>
          <div class="info-item"><span class="info-value">5</span><span class="info-label">能力维度</span></div>
          <div class="info-divider"></div>
          <div class="info-item"><span class="info-value">25</span><span class="info-label">分钟限时</span></div>
          <div class="info-divider"></div>
          <div class="info-item"><span class="info-value">AI</span><span class="info-label">智能分析</span></div>
        </div>

        <el-button type="primary" size="large" class="start-btn" @click="startAssessment">
          开始测评 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- ========== 答题中 ========== -->
    <div v-else-if="!finished && !submitting" class="quiz-section">
      <!-- 顶部：计时器 + 进度信息 -->
      <div class="quiz-header">
        <div class="quiz-meta">
          <el-tag effect="dark" :color="currentDim?.color" size="large">{{ currentDim?.icon }} {{ currentDim?.label }}</el-tag>
          <span class="quiz-count">第 {{ assessmentStore.currentIndex + 1 }} / {{ assessmentStore.questions.length }} 题</span>
        </div>

        <!-- 倒计时区域 -->
        <div class="timer-section">
          <div class="timer-info">
            <el-icon class="timer-icon" :style="{ color: assessmentStore.timeColor }"><Clock /></el-icon>
            <span class="timer-text" :style="{ color: assessmentStore.timeColor }">
              {{ assessmentStore.formattedTime }}
            </span>
            <span class="timer-label">剩余时间</span>
          </div>
          <div class="timer-progress-bar">
            <div
              class="timer-progress-fill"
              :style="{
                width: assessmentStore.timeProgress + '%',
                background: assessmentStore.timeColor
              }"
            ></div>
          </div>
        </div>

        <!-- 题目导航圆点（含已答/未答状态） -->
        <div class="quiz-nav-dots">
          <span
            v-for="(_, i) in assessmentStore.questions.length"
            :key="i"
            class="nav-dot"
            :class="{
              done: assessmentStore.answeredIndices.has(i),
              active: i === assessmentStore.currentIndex,
              current: i === assessmentStore.currentIndex
            }"
            @click="jumpTo(i)"
            :title="`第${i + 1}题${assessmentStore.answeredIndices.has(i) ? '（已答）' : '（未答）'}`"
          >
            {{ assessmentStore.answeredIndices.has(i) ? '✓' : '·' }}
          </span>
        </div>
      </div>

      <!-- 题目卡片 -->
      <div v-if="currentQ" class="quiz-card">
        <div class="quiz-question-wrap">
          <span class="quiz-q-num">Q{{ assessmentStore.currentIndex + 1 }}</span>
          <h3 class="quiz-question">{{ currentQ.content }}</h3>
        </div>
        <div class="quiz-options">
          <div
            v-for="(opt, i) in parseOptions(currentQ.options)"
            :key="i"
            class="quiz-option"
            :class="{ selected: selectedAnswer === String.fromCharCode(65 + i) }"
            @click="selectedAnswer = String.fromCharCode(65 + i)"
          >
            <span class="opt-letter">{{ String.fromCharCode(65 + i) }}</span>
            <span class="opt-text">{{ opt }}</span>
            <span class="opt-check" v-if="selectedAnswer === String.fromCharCode(65 + i)">✓</span>
          </div>
        </div>
        <div class="quiz-actions">
          <el-button @click="handleExit" size="large" plain type="info">
            <el-icon><Close /></el-icon> 退出测评
          </el-button>
          <div class="quiz-nav-btns">
            <el-button @click="prev" :disabled="assessmentStore.currentIndex === 0" size="large">
              <el-icon><ArrowLeft /></el-icon> 上一题
            </el-button>
            <el-button type="primary" @click="next" size="large">
              {{ assessmentStore.currentIndex >= assessmentStore.questions.length - 1 ? '提交答卷' : '下一题' }}
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 提交按钮（独立区域，在最后一道题时高亮提示） -->
      <div v-if="assessmentStore.currentIndex >= assessmentStore.questions.length - 1" class="submit-area">
        <div class="submit-hint">
          <el-icon><WarningFilled /></el-icon>
          已答 {{ assessmentStore.answeredCount }} / {{ assessmentStore.questions.length }} 题
          <span v-if="assessmentStore.unansweredCount > 0" class="unanswered-warn">
            ，还有 {{ assessmentStore.unansweredCount }} 题未作答
          </span>
        </div>
      </div>
    </div>

    <!-- ========== 提交中：AI分析加载动画 ========== -->
    <div v-else-if="submitting" class="submitting-section">
      <div class="submitting-animation">
        <div class="ai-pulse-dots">
          <span class="dot dot1"></span>
          <span class="dot dot2"></span>
          <span class="dot dot3"></span>
        </div>
        <h3>AI 正在分析你的能力...</h3>
        <p class="submitting-tips">
          基于你的答题数据，DeepSeek AI 正在生成个性化能力评估报告
        </p>
        <div class="submitting-steps">
          <div class="s-step" :class="{ active: step >= 1, done: step > 1 }">
            <span class="s-step-icon">📊</span>
            <span class="s-step-text">汇总答题数据</span>
          </div>
          <div class="s-step-connector" :class="{ active: step >= 1 }"></div>
          <div class="s-step" :class="{ active: step >= 2, done: step > 2 }">
            <span class="s-step-icon">🧠</span>
            <span class="s-step-text">AI 深度分析</span>
          </div>
          <div class="s-step-connector" :class="{ active: step >= 2 }"></div>
          <div class="s-step" :class="{ active: step >= 3 }">
            <span class="s-step-icon">📝</span>
            <span class="s-step-text">生成评估报告</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 结果报告 ========== -->
    <div v-else-if="result" class="result-section">
      <!-- 状态提示标签 -->
      <div class="result-status-bar">
        <el-tag type="success" effect="light" size="large">
          ✓ 你已完成本次测评
        </el-tag>
        <span class="result-time" v-if="result.createdAt">测评时间：{{ result.createdAt }}</span>
      </div>

      <div class="result-hero">
        <div class="result-badge" :class="resultLevelClass">
          <span class="level-icon">{{ resultLevelIcon }}</span>
          <span class="level-text">{{ result.level }}</span>
        </div>
        <h2 class="result-title">测评结果报告</h2>
        <p class="result-subtitle">AI 已全面分析你的能力画像</p>
      </div>

      <!-- 总分仪表盘 -->
      <div class="result-score-card">
        <div class="score-ring-wrap">
          <svg class="score-ring" viewBox="0 0 160 160">
            <circle cx="80" cy="80" r="70" fill="none" stroke="#e5e7eb" stroke-width="12" />
            <circle cx="80" cy="80" r="70" fill="none" :stroke="scoreColor"
              stroke-width="12" stroke-linecap="round"
              :stroke-dasharray="totalScore * 4.4 + ' ' + (440 - totalScore * 4.4)"
              transform="rotate(-90 80 80)"
              class="score-ring-animate" />
          </svg>
          <div class="score-ring-center">
            <span class="score-number">{{ totalScore }}</span>
            <span class="score-unit">分</span>
          </div>
        </div>
        <div class="score-level-tag" :style="{ color: scoreColor }">
          {{ levelLabel }}
        </div>
        <p class="score-hint">继续加油，你的潜力远超想象！</p>
      </div>

      <!-- 维度得分卡片 -->
      <div class="result-dimensions">
        <h3 class="section-label">
          <span class="label-icon">📊</span> 各维度表现
        </h3>
        <div class="dim-scores-grid">
          <div v-for="dim in dimensions" :key="dim.key" class="dim-score-card" :style="{ '--dim-color': dim.color }">
            <div class="ds-header">
              <span class="ds-icon">{{ dim.icon }}</span>
              <span class="ds-name">{{ dim.label }}</span>
            </div>
            <div class="ds-bar-wrap">
              <div class="ds-bar">
                <div class="ds-bar-fill" :style="{ width: (getDimScore(dim.key) || 0) + '%', background: dim.color }"></div>
              </div>
              <span class="ds-value">{{ getDimScore(dim.key) || 0 }}分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== AI 综合分析（替代原有的简单优劣势显示） ===== -->
      <div v-if="aiAnalysis" class="result-ai-analysis">
        <!-- AI 分析来源标记 -->
        <div class="ai-source-tag">
          <span v-if="aiAnalysis.source === 'AI'" class="source-badge ai-badge">🤖 AI 智能分析</span>
          <span v-else class="source-badge fallback-badge">📋 系统分析</span>
        </div>

        <!-- 综合评语 -->
        <div v-if="aiAnalysis.overallSummary" class="ai-summary-card">
          <h3 class="section-label">
            <span class="label-icon">📝</span> 综合评语
          </h3>
          <p class="ai-summary-text">{{ aiAnalysis.overallSummary }}</p>
        </div>

        <!-- 优势维度分析 -->
        <div v-if="aiAnalysis.strengthAnalysis && aiAnalysis.strengthAnalysis.length > 0" class="ai-strength-section">
          <h3 class="section-label">
            <span class="label-icon">✅</span> 优势维度深度分析
          </h3>
          <div class="ai-analysis-cards">
            <div
              v-for="(item, idx) in aiAnalysis.strengthAnalysis"
              :key="'s-' + idx"
              class="ai-card strength-card"
            >
              <div class="ai-card-header">
                <span class="ai-card-dim">{{ item.dimension }}</span>
                <el-tag :type="getScoreTagType(item.score)" size="small">{{ item.score }}分 · {{ item.level }}</el-tag>
              </div>
              <p class="ai-card-analysis">{{ item.analysis }}</p>

              <!-- 落地应用方法 -->
              <div v-if="item.applications && item.applications.length > 0" class="ai-app-list">
                <h4 class="ai-sub-label">💡 落地应用方法</h4>
                <div v-for="(app, ai) in item.applications" :key="'app-' + ai" class="ai-app-item">
                  <span class="app-title">{{ app.title }}</span>
                  <p class="app-desc">{{ app.desc }}</p>
                </div>
              </div>

              <!-- 以长补短策略 -->
              <div v-if="item.compensateWeakness" class="ai-compensate">
                <h4 class="ai-sub-label">🔄 以长补短策略</h4>
                <p class="compensate-text">{{ item.compensateWeakness }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 薄弱维度分析 -->
        <div v-if="aiAnalysis.weaknessAnalysis && aiAnalysis.weaknessAnalysis.length > 0" class="ai-weakness-section">
          <h3 class="section-label">
            <span class="label-icon">🎯</span> 薄弱维度提升计划
          </h3>
          <div class="ai-analysis-cards">
            <div
              v-for="(item, idx) in aiAnalysis.weaknessAnalysis"
              :key="'w-' + idx"
              class="ai-card weakness-card"
            >
              <div class="ai-card-header">
                <span class="ai-card-dim">{{ item.dimension }}</span>
                <el-tag :type="getScoreTagType(item.score)" size="small">{{ item.score }}分 · {{ item.level }}</el-tag>
              </div>
              <p class="ai-card-analysis">{{ item.analysis }}</p>

              <!-- 分层提升计划 -->
              <div v-if="item.improvementPlan && item.improvementPlan.length > 0" class="ai-plan-list">
                <h4 class="ai-sub-label">📋 分层提升计划</h4>
                <div v-for="(plan, pi) in item.improvementPlan" :key="'plan-' + pi" class="plan-level">
                  <div class="plan-level-header">
                    <span class="plan-badge" :class="'plan-' + pi">{{ plan.level }}</span>
                    <span class="plan-duration">{{ plan.duration }}</span>
                  </div>
                  <ul class="plan-tasks">
                    <li v-for="(task, ti) in plan.tasks" :key="'task-' + ti" class="plan-task">
                      <span class="task-dot"></span>{{ task }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 保留原有的简单优势/薄弱作为兜底（当AI分析不可用时显示） -->
      <div v-else class="result-analysis">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="analysis-card strength">
              <div class="analysis-header">
                <span class="analysis-icon">✅</span>
                <span>优势维度</span>
              </div>
              <p class="analysis-text">{{ result.strengths }}</p>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="analysis-card weakness">
              <div class="analysis-header">
                <span class="analysis-icon">🎯</span>
                <span>薄弱维度</span>
              </div>
              <p class="analysis-text">{{ result.weaknesses }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 行动按钮 -->
      <div class="result-actions">
        <el-button type="primary" size="large" @click="exploreCareerFromAssessment">
          🎯 基于测评结果探索职业方向
        </el-button>
        <el-button size="large" @click="$router.push('/student/gap-analysis')">
          📈 查看差距分析
        </el-button>
        <el-button size="large" @click="$router.push('/student/learning-path')">
          📚 规划学习路径
        </el-button>
        <el-button size="large" @click="reset">🔄 重新测评</el-button>
      </div>

      <!-- 推荐岗位（基于测评结果） -->
      <div v-if="recommendedJobs.length > 0" class="result-recommended-jobs">
        <h3 class="section-label">
          <span class="label-icon">🎯</span> 推荐匹配岗位
        </h3>
        <div class="recommended-job-cards">
          <div
            v-for="rj in recommendedJobs"
            :key="rj.jobId"
            class="recommended-job-card"
            @click="$router.push(`/student/gap-analysis?jobId=${rj.jobId}`)"
          >
            <div class="rjc-header">
              <span class="rjc-title">{{ rj.jobTitle }}</span>
              <el-tag
                :type="rj.matchScore >= 70 ? 'success' : rj.matchScore >= 40 ? 'warning' : 'danger'"
                size="small"
                effect="dark"
              >
                {{ rj.matchScore }}% 匹配
              </el-tag>
            </div>
            <div class="rjc-meta">
              <span>{{ rj.companyName }}</span>
              <span v-if="rj.city">· {{ rj.city }}</span>
            </div>
            <el-button type="primary" size="small" class="rjc-action">
              查看差距分析 →
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, ArrowLeft, Close, Clock, WarningFilled, Loading } from '@element-plus/icons-vue'
import { useAssessmentStore } from '@/stores/assessment'
import { getAssessmentQuestions, submitAssessment, getLatestAssessmentResult, getRecommendedJobs } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const assessmentStore = useAssessmentStore()
const started = computed(() => assessmentStore.questions.length > 0)
const finished = ref(false)
const result = ref<any>(null)
const selectedAnswer = ref('')

/** 页面初始加载状态 */
const loading = ref(false)
/** 是否有已存在的测评结果 */
const hasExistingResult = ref(false)
/** 提交中的加载状态 */
const submitting = ref(false)
/** 提交动画步骤 0-3 */
const step = ref(0)
/** 步骤定时器 */
let stepTimer: ReturnType<typeof setInterval> | null = null
/** 推荐岗位列表 */
const recommendedJobs = ref<any[]>([])

// 页面初始化：检查是否有历史测评结果
onMounted(async () => {
  // 优先检查 Pinia 内存缓存
  if (assessmentStore.result) {
    result.value = assessmentStore.result
    finished.value = true
    hasExistingResult.value = true
    fetchRecommendedJobs()
    return
  }

  // 从后端查询最新结果
  loading.value = true
  try {
    const res: any = await getLatestAssessmentResult()
    if (res.data) {
      result.value = res.data
      finished.value = true
      hasExistingResult.value = true
      fetchRecommendedJobs()
    }
  } catch {
    // 无结果，停留在开场页
  } finally {
    loading.value = false
  }
})

/** 获取推荐岗位 */
async function fetchRecommendedJobs() {
  try {
    const res: any = await getRecommendedJobs()
    recommendedJobs.value = res.data || []
  } catch {
    recommendedJobs.value = []
  }
}

/**
 * 基于测评结果直达职业探索推荐
 * 跳转到职业探索页并携带 resultId，目标页 onMounted 时自动调用 from-assessment 接口
 */
function exploreCareerFromAssessment() {
  const resultId = result.value?.id
  if (!resultId) {
    ElMessage.warning('未找到测评结果，请先完成测评')
    return
  }
  router.push({
    path: '/student/career-exploration',
    query: { fromAssessment: 'true', resultId: String(resultId) }
  })
}

// 监听剩余时间，时间到自动提交
watch(() => assessmentStore.remainingTime, (val) => {
  if (val <= 0 && !finished.value && started.value) {
    ElMessage.warning('答题时间已到，系统自动提交答卷')
    submitNow()
  }
})

// 组件卸载时清除定时器
onUnmounted(() => {
  assessmentStore.clearTimer()
  clearStepTimer()
})

const dimensions = [
  { key: 'programming', label: '编程能力', icon: '💻', hint: '代码功底与算法思维', color: '#3b82f6' },
  { key: 'logic', label: '逻辑推理', icon: '🧩', hint: '分析问题与推理能力', color: '#8b5cf6' },
  { key: 'product', label: '产品思维', icon: '🎨', hint: '用户洞察与产品设计', color: '#ec4899' },
  { key: 'techLiteracy', label: '技术素养', icon: '🔧', hint: '技术广度与深度', color: '#f59e0b' },
  { key: 'communication', label: '沟通表达', icon: '💬', hint: '表达清晰与协作能力', color: '#10b981' }
]

const currentQ = computed(() => assessmentStore.questions[assessmentStore.currentIndex])

const currentDim = computed(() => {
  const q = currentQ.value
  if (!q) return null
  return dimensions[Math.floor(assessmentStore.currentIndex / 5)] || dimensions[0]
})

const totalScore = computed(() => {
  if (!result.value) return 0
  return result.value.dimensionScores?.totalScore || result.value.totalScore || 0
})

const scoreColor = computed(() => {
  const s = totalScore.value
  if (s >= 85) return '#10b981'
  if (s >= 70) return '#3b82f6'
  if (s >= 55) return '#f59e0b'
  return '#ef4444'
})

const levelLabel = computed(() => {
  const s = totalScore.value
  if (s >= 85) return '优秀 · Excellent'
  if (s >= 70) return '良好 · Good'
  if (s >= 55) return '中等 · Average'
  return '需提升 · Needs Improvement'
})

const resultLevelClass = computed(() => {
  const s = totalScore.value
  if (s >= 85) return 'level-excellent'
  if (s >= 70) return 'level-good'
  if (s >= 55) return 'level-average'
  return 'level-need'
})

const resultLevelIcon = computed(() => {
  const s = totalScore.value
  if (s >= 85) return '🏆'
  if (s >= 70) return '🌟'
  if (s >= 55) return '📈'
  return '💪'
})

/** AI 分析数据 */
const aiAnalysis = computed(() => {
  return result.value?.aiAnalysis || null
})

function getDimScore(key: string) {
  if (!result.value?.dimensionScores) return 0
  // 尝试中文名和英文名
  const dimMap: Record<string, string> = {
    programming: '编程能力',
    logic: '逻辑推理',
    product: '产品思维',
    techLiteracy: '技术素养',
    communication: '沟通表达'
  }
  const cnKey = dimMap[key]
  return result.value.dimensionScores[cnKey] || result.value.dimensionScores[key] || 0
}

function parseOptions(opts: any): string[] {
  try {
    const arr = typeof opts === 'string' ? JSON.parse(opts) : opts || []
    if (arr.length === 0) return []
    // 如果是 [{key, text}] 格式，提取 text 字段
    if (typeof arr[0] === 'object' && arr[0] !== null && 'text' in arr[0]) {
      return arr.map((item: any) => item.text)
    }
    // 普通字符串数组
    return arr
  } catch { return [] }
}

function getScoreTagType(score: number): string {
  if (score >= 85) return 'success'
  if (score >= 70) return 'primary'
  if (score >= 55) return 'warning'
  return 'danger'
}

async function startAssessment() {
  const res: any = await getAssessmentQuestions()
  assessmentStore.setQuestions(res.data || [])
}

function prev() {
  if (assessmentStore.currentIndex > 0) {
    assessmentStore.currentIndex--
    selectedAnswer.value = assessmentStore.answers.get(currentQ.value?.id) || ''
  }
}

function jumpTo(index: number) {
  // 保存当前答案
  if (currentQ.value) assessmentStore.setAnswer(currentQ.value.id, selectedAnswer.value)
  assessmentStore.currentIndex = index
  selectedAnswer.value = assessmentStore.answers.get(assessmentStore.questions[index]?.id) || ''
}

async function next() {
  if (currentQ.value) assessmentStore.setAnswer(currentQ.value.id, selectedAnswer.value)
  if (assessmentStore.currentIndex >= assessmentStore.questions.length - 1) {
    await confirmSubmit()
  } else {
    assessmentStore.currentIndex++
    selectedAnswer.value = assessmentStore.answers.get(assessmentStore.questions[assessmentStore.currentIndex]?.id) || ''
  }
}

/** 提交确认对话框 */
async function confirmSubmit() {
  const unanswered = assessmentStore.unansweredCount
  let confirmText = '确认提交答卷？提交后无法修改。'
  if (unanswered > 0) {
    confirmText = `还有 ${unanswered} 题未作答，确认提交吗？未答题将计0分。`
  }

  try {
    await ElMessageBox.confirm(confirmText, '提交确认', {
      confirmButtonText: '确认提交',
      cancelButtonText: '继续答题',
      type: unanswered > 0 ? 'warning' : 'info',
      distinguishCancelAndClose: true
    })
    await submitNow()
  } catch {
    // 用户取消提交，不做任何操作
  }
}

/** 步骤定时器清除 */
function clearStepTimer() {
  if (stepTimer) {
    clearInterval(stepTimer)
    stepTimer = null
  }
}

/** 启动提交加载动画 */
function startStepAnimation() {
  step.value = 0
  clearStepTimer()
  stepTimer = setInterval(() => {
    if (step.value < 3) {
      step.value++
    }
  }, 3000)
}

/** 实际提交逻辑 */
async function submitNow() {
  if (finished.value) return

  // 保存最后一题答案
  if (currentQ.value) assessmentStore.setAnswer(currentQ.value.id, selectedAnswer.value)

  assessmentStore.clearTimer()

  // 启动提交加载动画
  submitting.value = true
  startStepAnimation()

  const answers: any[] = []
  assessmentStore.answers.forEach((answer, questionId) => {
    answers.push({ questionId, answer })
  })

  try {
    const res: any = await submitAssessment({ answers })
    result.value = res.data
    // 同时存入 Pinia store 内存缓存
    assessmentStore.setResult(res.data)
    finished.value = true
    // 获取推荐岗位
    fetchRecommendedJobs()
  } catch (err: any) {
    ElMessage.error(err?.message || '提交失败，请重试')
  } finally {
    clearStepTimer()
    submitting.value = false
  }
}

/** 退出确认对话框 */
async function handleExit() {
  const answered = assessmentStore.answeredCount
  let exitText = '确定退出测评？'
  if (answered > 0) {
    exitText = `你已作答 ${answered} 题，退出后当前进度将不会保存。确定退出吗？`
  }

  try {
    await ElMessageBox.confirm(exitText, '退出确认', {
      confirmButtonText: '确 定',
      cancelButtonText: '继续答题',
      type: 'warning'
    })
    reset()
  } catch {
    // 用户取消退出
  }
}

function reset() {
  assessmentStore.reset()
  finished.value = false
  result.value = null
  selectedAnswer.value = ''
  hasExistingResult.value = false
}
</script>

<style scoped lang="scss">
// ======== 加载中 ========
.result-loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  gap: 16px;
  color: #6b7280;
  font-size: 16px;
}

// ======== 未开始：AI主题开场 ========
.assessment-hero {
  position: relative;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #1e3a5f 70%, #1a1a2e 100%);
  border-radius: 20px;
  overflow: hidden;
  padding: 60px 40px 50px;
  text-align: center;
}

.hero-bg-decor {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.floating-icon {
  position: absolute;
  font-size: 36px;
  opacity: 0.08;
  animation: floatAround 12s ease-in-out infinite;
  &.i1 { top: 10%; left: 8%; animation-delay: 0s; font-size: 48px; }
  &.i2 { top: 15%; right: 12%; animation-delay: 2s; font-size: 40px; }
  &.i3 { bottom: 20%; left: 10%; animation-delay: 4s; font-size: 44px; }
  &.i4 { top: 40%; right: 6%; animation-delay: 6s; font-size: 38px; }
  &.i5 { bottom: 10%; right: 20%; animation-delay: 3s; font-size: 50px; }
}

.hero-dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.06) 1px, transparent 1px);
  background-size: 30px 30px;
}

@keyframes floatAround {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(8deg); }
  50% { transform: translateY(-10px) rotate(-5deg); }
  75% { transform: translateY(-25px) rotate(3deg); }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 18px;
  border-radius: 20px;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #93c5fd;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 20px;
  .badge-dot {
    width: 8px; height: 8px;
    border-radius: 50%;
    background: #60a5fa;
    animation: gentlePulse 2s ease-in-out infinite;
  }
}

@keyframes gentlePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.hero-title {
  font-size: 40px;
  font-weight: 800;
  margin-bottom: 14px;
  .title-gradient {
    background: linear-gradient(135deg, #60a5fa, #a78bfa, #f472b6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-desc {
  color: #94a3b8;
  font-size: 16px;
  max-width: 500px;
  margin: 0 auto 36px;
  line-height: 1.6;
}

.dimension-cards {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 36px;
  flex-wrap: wrap;
}

.dim-card {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 14px;
  padding: 18px 14px;
  width: 130px;
  transition: all 0.3s;
  &:hover {
    transform: translateY(-4px);
    background: rgba(255,255,255,0.1);
    border-color: rgba(255,255,255,0.15);
  }
  .dim-icon {
    width: 44px; height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    margin: 0 auto 8px;
  }
  .dim-name {
    color: #e2e8f0;
    font-size: 14px;
    font-weight: 600;
  }
  .dim-hint {
    color: #64748b;
    font-size: 11px;
    margin-top: 4px;
  }
}

.hero-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 32px;
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 0 28px;
  }
  .info-value {
    font-size: 24px;
    font-weight: 700;
    color: #e2e8f0;
  }
  .info-label {
    font-size: 12px;
    color: #64748b;
  }
  .info-divider {
    width: 1px; height: 36px;
    background: rgba(255,255,255,0.1);
  }
}

.start-btn {
  height: 52px;
  padding: 0 48px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border: none;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.4);
  transition: all 0.3s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 40px rgba(59, 130, 246, 0.5);
  }
}

// ======== 答题中 ========
.quiz-header {
  background: #fff;
  border-radius: 16px;
  padding: 20px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}

.quiz-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  .quiz-count { font-size: 15px; color: #6b7280; font-weight: 500; }
}

// 倒计时样式
.timer-section {
  margin-bottom: 14px;
  .timer-info {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    .timer-icon { font-size: 18px; }
    .timer-text {
      font-size: 22px;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
      transition: color 0.5s;
    }
    .timer-label {
      font-size: 13px;
      color: #909399;
      margin-left: 4px;
    }
  }
  .timer-progress-bar {
    height: 6px;
    background: #e5e7eb;
    border-radius: 3px;
    overflow: hidden;
    .timer-progress-fill {
      height: 100%;
      border-radius: 3px;
      transition: width 1s linear, background 0.5s;
    }
  }
}

// 题目导航圆点
.quiz-nav-dots {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding-top: 4px;
  .nav-dot {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    background: #f3f4f6;
    color: #9ca3af;
    border: 2px solid transparent;
    transition: all 0.2s;
    &:hover {
      border-color: #93c5fd;
      background: #eff6ff;
    }
    &.done {
      background: #dbeafe;
      color: #2563eb;
    }
    &.current {
      border-color: #3b82f6;
      background: #eff6ff;
      color: #3b82f6;
      transform: scale(1.15);
    }
  }
}

.quiz-card {
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.quiz-question-wrap {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 28px;
  .quiz-q-num {
    flex-shrink: 0;
    width: 40px; height: 40px;
    background: linear-gradient(135deg, #3b82f6, #6366f1);
    color: #fff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
  }
  .quiz-question {
    font-size: 18px;
    color: #1f2937;
    line-height: 1.6;
    font-weight: 600;
  }
}

.quiz-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
}

.quiz-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  &:hover { border-color: #93c5fd; background: #f8faff; }
  &.selected {
    border-color: #3b82f6;
    background: #eff6ff;
    .opt-letter { background: #3b82f6; color: #fff; }
  }
  .opt-letter {
    flex-shrink: 0;
    width: 32px; height: 32px;
    border-radius: 8px;
    background: #f3f4f6;
    color: #6b7280;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    transition: all 0.25s;
  }
  .opt-text { flex: 1; font-size: 15px; color: #374151; }
  .opt-check { color: #3b82f6; font-weight: 700; font-size: 18px; }
}

.quiz-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .quiz-nav-btns {
    display: flex;
    gap: 10px;
  }
}

// 提交提示区域
.submit-area {
  margin-top: 20px;
  text-align: center;
  .submit-hint {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    background: #fff;
    border-radius: 10px;
    font-size: 14px;
    color: #606266;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    .unanswered-warn {
      color: #e6a23c;
      font-weight: 600;
    }
  }
}

// ======== 提交中：AI分析加载动画 ========
.submitting-section {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.submitting-animation {
  text-align: center;
  padding: 60px 40px;
  h3 {
    font-size: 20px;
    font-weight: 700;
    color: #1f2937;
    margin: 24px 0 10px;
  }
  .submitting-tips {
    color: #6b7280;
    font-size: 14px;
    margin-bottom: 32px;
  }
}

// 跳动圆点
.ai-pulse-dots {
  display: flex;
  gap: 10px;
  justify-content: center;
  .dot {
    width: 14px; height: 14px;
    border-radius: 50%;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    animation: aiPulseDot 1.4s ease-in-out infinite;
    &.dot1 { animation-delay: 0s; }
    &.dot2 { animation-delay: 0.2s; }
    &.dot3 { animation-delay: 0.4s; }
  }
}

@keyframes aiPulseDot {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1.2); opacity: 1; }
}

// 三步递进
.submitting-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  .s-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    width: 100px;
    opacity: 0.35;
    transition: all 0.5s ease;
    &.active {
      opacity: 1;
      .s-step-icon { transform: scale(1.2); }
    }
    &.done {
      opacity: 0.8;
      .s-step-icon {
        background: #d1fae5;
        border-color: #10b981;
      }
    }
    .s-step-icon {
      width: 48px; height: 48px;
      border-radius: 14px;
      background: #f3f4f6;
      border: 2px solid #e5e7eb;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      transition: all 0.5s ease;
    }
    .s-step-text {
      font-size: 13px;
      color: #6b7280;
      font-weight: 500;
    }
  }
  .s-step-connector {
    width: 40px; height: 3px;
    background: #e5e7eb;
    border-radius: 2px;
    margin-bottom: 22px;
    transition: background 0.5s ease;
    &.active {
      background: linear-gradient(90deg, #6366f1, #8b5cf6);
    }
  }
}

// ======== 结果状态栏 ========
.result-status-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0 0;
  .result-time {
    font-size: 13px;
    color: #9ca3af;
  }
}

// ======== 结果报告 ========
.result-hero {
  text-align: center;
  padding: 30px 0 20px;
}

.result-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 22px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
  &.level-excellent { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; }
  &.level-good { background: #dbeafe; color: #1e40af; border: 1px solid #93c5fd; }
  &.level-average { background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; }
  &.level-need { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
  .level-icon { font-size: 20px; }
}

.result-title {
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
}

.result-subtitle {
  color: #6b7280;
  font-size: 15px;
  margin-top: 6px;
}

.result-score-card {
  text-align: center;
  padding: 20px 0;
}

.score-ring-wrap {
  position: relative;
  display: inline-block;
  margin: 0 auto;
}

.score-ring {
  width: 180px;
  height: 180px;
  circle { transition: stroke-dasharray 1.2s ease-out; }
}

.score-ring-animate {
  animation: ringFill 1.5s ease-out forwards;
}

@keyframes ringFill {
  from { stroke-dasharray: 0 440; }
}

.score-ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .score-number { font-size: 42px; font-weight: 800; color: #1f2937; line-height: 1; }
  .score-unit { font-size: 14px; color: #6b7280; }
}

.score-level-tag {
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
}

.score-hint {
  color: #9ca3af;
  font-size: 13px;
  margin-top: 4px;
}

// 维度得分
.result-dimensions {
  margin-top: 24px;
}

.section-label {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  .label-icon { font-size: 20px; }
}

.dim-scores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.dim-score-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 18px 20px;
  transition: all 0.3s;
  &:hover {
    border-color: var(--dim-color);
    box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  }
  .ds-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    .ds-icon { font-size: 20px; }
    .ds-name { font-size: 14px; font-weight: 600; color: #374151; }
  }
  .ds-bar-wrap {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .ds-bar {
    flex: 1;
    height: 10px;
    background: #e5e7eb;
    border-radius: 5px;
    overflow: hidden;
    .ds-bar-fill {
      height: 100%;
      border-radius: 5px;
      transition: width 1s ease-out;
      position: relative;
      &::after {
        content: '';
        position: absolute;
        right: 0; top: 0; bottom: 0;
        width: 4px;
        background: rgba(255,255,255,0.6);
        border-radius: 2px;
      }
    }
  }
  .ds-value {
    font-size: 15px;
    font-weight: 700;
    color: var(--dim-color);
    min-width: 50px;
    text-align: right;
  }
}

// ======== AI 分析展示区 ========
.result-ai-analysis {
  margin-top: 28px;
}

.ai-source-tag {
  text-align: center;
  margin-bottom: 20px;
  .source-badge {
    display: inline-block;
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    &.ai-badge {
      background: linear-gradient(135deg, #ede9fe, #dbeafe);
      color: #6366f1;
    }
    &.fallback-badge {
      background: #f3f4f6;
      color: #6b7280;
    }
  }
}

.ai-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
  .ai-summary-text {
    font-size: 15px;
    color: #374151;
    line-height: 1.8;
  }
}

.ai-analysis-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  }
  &.strength-card { border-left: 4px solid #10b981; }
  &.weakness-card { border-left: 4px solid #f59e0b; }

  .ai-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    .ai-card-dim {
      font-size: 16px;
      font-weight: 700;
      color: #1f2937;
    }
  }

  .ai-card-analysis {
    font-size: 14px;
    color: #4b5563;
    line-height: 1.7;
    margin-bottom: 16px;
  }
}

.ai-sub-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
  margin-top: 0;
}

// 应用方法列表
.ai-app-list {
  margin-bottom: 16px;
  .ai-app-item {
    padding: 10px 14px;
    background: #f0fdf4;
    border-radius: 8px;
    margin-bottom: 8px;
    .app-title {
      font-size: 13px;
      font-weight: 600;
      color: #065f46;
      display: block;
      margin-bottom: 4px;
    }
    .app-desc {
      font-size: 13px;
      color: #374151;
      line-height: 1.6;
      margin: 0;
    }
  }
}

// 以长补短策略
.ai-compensate {
  padding: 12px 16px;
  background: linear-gradient(135deg, #eff6ff, #f0fdf4);
  border-radius: 10px;
  border: 1px dashed #93c5fd;
  .compensate-text {
    font-size: 14px;
    color: #1e40af;
    line-height: 1.7;
    margin: 0;
  }
}

// 分层提升计划
.ai-plan-list {
  .plan-level {
    margin-bottom: 14px;
    &:last-child { margin-bottom: 0; }

    .plan-level-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;

      .plan-badge {
        display: inline-block;
        padding: 3px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 600;
        color: #fff;
        &.plan-0 { background: #3b82f6; }
        &.plan-1 { background: #8b5cf6; }
        &.plan-2 { background: #ec4899; }
      }
      .plan-duration {
        font-size: 12px;
        color: #9ca3af;
        font-weight: 500;
      }
    }

    .plan-tasks {
      list-style: none;
      padding: 0 0 0 8px;
      margin: 0;

      .plan-task {
        font-size: 13px;
        color: #4b5563;
        line-height: 1.6;
        padding: 4px 0;
        display: flex;
        align-items: flex-start;
        gap: 8px;
        .task-dot {
          flex-shrink: 0;
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: #d1d5db;
          margin-top: 7px;
        }
      }
    }
  }
}

// ===== 原有的简单优劣势（兜底） =====
.result-analysis {
  margin-top: 24px;
}

.analysis-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  &.strength { border-left: 4px solid #10b981; }
  &.weakness { border-left: 4px solid #f59e0b; }
  .analysis-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 12px;
    .analysis-icon { font-size: 20px; }
  }
  .analysis-text {
    color: #6b7280;
    font-size: 14px;
    line-height: 1.7;
  }
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 28px;
  flex-wrap: wrap;
}

// ======== 推荐岗位 ========
.result-recommended-jobs {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.recommended-job-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.recommended-job-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  &:hover {
    border-color: #6366f1;
    box-shadow: 0 4px 16px rgba(99,102,241,0.1);
    transform: translateY(-2px);
    .rjc-action { opacity: 1; }
  }
  .rjc-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    .rjc-title {
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;
    }
  }
  .rjc-meta {
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 14px;
  }
  .rjc-action {
    width: 100%;
    opacity: 0.7;
    transition: opacity 0.3s;
  }
}
</style>
