<template>
  <div class="interview-page">
    <!-- Hero -->
    <div class="iv-hero">
      <div class="ivh-glow"></div>
      <div class="ivh-content">
        <div class="ivh-icon-wrap">🎤</div>
        <h1>AI 模拟面试</h1>
        <p>与AI面试官进行真实模拟对话，获取专业评估报告</p>
      </div>
    </div>

    <!-- ======== 未开始：选择面试类型 ======== -->
    <div v-if="!session && !report" class="iv-setup">
      <div class="iv-type-cards">
        <div
          v-for="type in interviewTypes"
          :key="type.value"
          class="iv-type-card"
          :class="{ active: interviewType === type.value }"
          @click="interviewType = type.value"
        >
          <div class="ivtc-icon" :style="{ background: type.color }">{{ type.icon }}</div>
          <div class="ivtc-info">
            <h4>{{ type.label }}</h4>
            <p>{{ type.desc }}</p>
          </div>
          <div class="ivtc-check" v-if="interviewType === type.value">✓</div>
        </div>
      </div>

      <div class="iv-form-card">
        <div class="ivf-row">
          <label>🎯 目标岗位</label>
          <el-input v-model="jobTitle" placeholder="如：Java后端开发、前端工程师" size="large" class="ivf-input">
            <template #prefix><el-icon><Briefcase /></el-icon></template>
          </el-input>
        </div>
        <el-button type="primary" size="large" class="ivf-start-btn" @click="start" :loading="loading">
          🤖 开始面试
        </el-button>
      </div>

      <div class="iv-tips">
        <div class="ivt-header">💡 面试提示</div>
        <div class="ivt-grid">
          <div class="ivt-item">
            <span class="ivt-num">1</span>
            <div><strong>保持冷静</strong><p>这是模拟面试，无需紧张</p></div>
          </div>
          <div class="ivt-item">
            <span class="ivt-num">2</span>
            <div><strong>详细回答</strong><p>充分展示你的思考过程</p></div>
          </div>
          <div class="ivt-item">
            <span class="ivt-num">3</span>
            <div><strong>注意时间</strong><p>每题建议2-3分钟</p></div>
          </div>
          <div class="ivt-item">
            <span class="ivt-num">4</span>
            <div><strong>获取反馈</strong><p>面试后查看详细报告</p></div>
          </div>
        </div>
      </div>
    </div>

    <!-- ======== 面试中 ======== -->
    <div v-else-if="!report" class="iv-active">
      <div class="iva-header">
        <div class="iva-left">
          <span class="iva-badge" :style="{ background: currentType?.color + '20', color: currentType?.color }">
            {{ currentType?.icon }} {{ currentType?.label }}
          </span>
          <span class="iva-progress">
            第 {{ session.questionIndex }} / {{ session.totalQuestions }} 题
          </span>
        </div>
        <div class="iva-right">
          <div class="iva-progress-bar">
            <div class="ivap-fill" :style="{ width: (session.questionIndex / session.totalQuestions * 100) + '%' }"></div>
          </div>
          <el-button type="warning" size="small" @click="endInterview" plain>结束面试</el-button>
        </div>
      </div>

      <div class="iva-chat">
        <ChatWindow
          :messages="chatMessages"
          :loading="sending"
          placeholder="输入你的回答..."
          @send="handleAnswer"
        />
      </div>
    </div>

    <!-- ======== 面试报告 ======== -->
    <div v-else class="iv-report">
      <div class="ivr-hero">
        <div class="ivrh-badge" :class="'score-' + scoreLevel">
          <span>{{ scoreEmoji }}</span>
          {{ scoreLevelText }}
        </div>
        <h2>面试评估报告</h2>
        <p>{{ currentType?.label }} · 共 {{ report.totalQuestions || 0 }} 题</p>
      </div>

      <!-- 总分圆环 -->
      <div class="ivr-score-wrap">
        <div class="ivrs-ring">
          <svg viewBox="0 0 160 160">
            <circle cx="80" cy="80" r="68" fill="none" stroke="#e5e7eb" stroke-width="12" />
            <circle cx="80" cy="80" r="68" fill="none" :stroke="scoreColor"
              stroke-width="12" stroke-linecap="round"
              :stroke-dasharray="(report.totalScore || 75) * 4.27 + ' ' + (427 - (report.totalScore || 75) * 4.27)"
              transform="rotate(-90 80 80)"
              class="ivrs-animate" />
          </svg>
          <div class="ivrs-center">
            <span class="ivrs-value">{{ report.totalScore || 75 }}</span>
            <span class="ivrs-label">综合评分</span>
          </div>
        </div>
      </div>

      <!-- 维度得分 -->
      <div class="ivr-dimensions" v-if="report.dimensionScores">
        <h3>📊 各维度表现</h3>
        <div class="ivrd-grid">
          <div v-for="(v, k) in report.dimensionScores" :key="k" class="ivrd-card">
            <div class="ivrd-name">{{ k }}</div>
            <div class="ivrd-bar-wrap">
              <div class="ivrd-bar">
                <div class="ivrd-fill" :style="{ width: v + '%', background: dimGradient(v) }"></div>
              </div>
              <span class="ivrd-value" :style="{ color: dimColor(v) }">{{ v }}分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 反馈 -->
      <el-row :gutter="20" class="ivr-feedback">
        <el-col :span="12">
          <div class="ivrf-card highlight">
            <div class="ivrf-header">
              <span class="ivrf-icon">✨</span> 亮点
            </div>
            <ul>
              <li v-for="(h, i) in (report.highlights || [])" :key="i">{{ h }}</li>
            </ul>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="ivrf-card improve">
            <div class="ivrf-header">
              <span class="ivrf-icon">🎯</span> 改进建议
            </div>
            <ul>
              <li v-for="(im, i) in (report.improvements || [])" :key="i">{{ im }}</li>
            </ul>
          </div>
        </el-col>
      </el-row>

      <div class="ivr-actions">
        <el-button size="large" @click="reset">🔄 重新面试</el-button>
        <el-button type="primary" size="large" @click="$router.push('/student/learning-path')">
          📚 规划学习
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Briefcase } from '@element-plus/icons-vue'
import ChatWindow from '@/components/common/ChatWindow.vue'
import { startInterview, submitAnswer, endInterview as endInterviewApi } from '@/api/student'

const interviewTypes = [
  { label: '技术面试', value: 'TECHNICAL', icon: '💻', desc: '算法、系统设计、编程语言等', color: '#3b82f6' },
  { label: 'HR面试', value: 'HR', icon: '🤝', desc: '职业规划、沟通协作、综合素质', color: '#10b981' },
  { label: '综合面试', value: 'COMPREHENSIVE', icon: '🎯', desc: '技术 + 综合素质全面评估', color: '#8b5cf6' }
]

const session = ref<any>(null)
const report = ref<any>(null)
const loading = ref(false)
const sending = ref(false)
const jobTitle = ref('')
const interviewType = ref('COMPREHENSIVE')
const chatMessages = ref<any[]>([])

const currentType = computed(() => interviewTypes.find(t => t.value === interviewType.value))

const scoreLevel = computed(() => {
  const s = report.value?.totalScore || 0
  if (s >= 85) return 'excellent'
  if (s >= 70) return 'good'
  if (s >= 55) return 'average'
  return 'need'
})

const scoreEmoji = computed(() => {
  const s = report.value?.totalScore || 0
  if (s >= 85) return '🏆'
  if (s >= 70) return '🌟'
  if (s >= 55) return '📈'
  return '💪'
})

const scoreLevelText = computed(() => {
  const s = report.value?.totalScore || 0
  if (s >= 85) return '优秀'
  if (s >= 70) return '良好'
  if (s >= 55) return '中等'
  return '需提升'
})

const scoreColor = computed(() => {
  const s = report.value?.totalScore || 0
  if (s >= 85) return '#10b981'
  if (s >= 70) return '#3b82f6'
  if (s >= 55) return '#f59e0b'
  return '#ef4444'
})

function dimColor(v: number): string {
  if (v >= 85) return '#10b981'
  if (v >= 70) return '#3b82f6'
  if (v >= 55) return '#f59e0b'
  return '#ef4444'
}

function dimGradient(v: number): string {
  if (v >= 85) return 'linear-gradient(90deg, #10b981, #34d399)'
  if (v >= 70) return 'linear-gradient(90deg, #3b82f6, #60a5fa)'
  if (v >= 55) return 'linear-gradient(90deg, #f59e0b, #fbbf24)'
  return 'linear-gradient(90deg, #ef4444, #f87171)'
}

async function start() {
  loading.value = true
  try {
    const res: any = await startInterview({ jobTitle: jobTitle.value, interviewType: interviewType.value })
    session.value = res.data
    chatMessages.value = [{ role: 'assistant', content: res.data.question }]
  } finally { loading.value = false }
}

async function handleAnswer(text: string) {
  chatMessages.value.push({ role: 'user', content: text })
  sending.value = true
  try {
    const res: any = await submitAnswer(session.value.sessionId, text)
    const data = res.data
    if (data.finished) {
      chatMessages.value.push({ role: 'assistant', content: '面试结束，正在生成报告...' })
    } else {
      chatMessages.value.push({ role: 'assistant', content: data.question })
    }
    session.value = data
  } finally { sending.value = false }
}

async function endInterview() {
  const res: any = await endInterviewApi(session.value.sessionId)
  report.value = res.data
  session.value = null
}

function reset() { session.value = null; report.value = null; chatMessages.value = [] }
</script>

<style scoped lang="scss">
// ======== Hero ========
.iv-hero {
  position: relative;
  background: linear-gradient(135deg, #1e1b4b, #312e81, #4338ca);
  border-radius: 20px;
  padding: 36px;
  margin-bottom: 24px;
  text-align: center;
  overflow: hidden;
}
.ivh-glow {
  position: absolute;
  top: -50%; left: 50%;
  transform: translateX(-50%);
  width: 400px; height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2), transparent);
  pointer-events: none;
}
.ivh-content {
  position: relative; z-index: 1;
  .ivh-icon-wrap {
    width: 64px; height: 64px;
    border-radius: 18px;
    background: rgba(255,255,255,0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin: 0 auto 12px;
  }
  h1 { font-size: 28px; font-weight: 800; color: #fff; margin: 0 0 6px; }
  p { color: #c7d2fe; font-size: 14px; }
}

// ======== 面试类型选择 ========
.iv-setup { margin-bottom: 24px; }

.iv-type-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.iv-type-card {
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px 20px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  position: relative;
  &:hover { border-color: #c7d2fe; transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
  &.active {
    border-color: #6366f1;
    background: #eef2ff;
    box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
  }
  .ivtc-icon {
    width: 52px; height: 52px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin: 0 auto 12px;
  }
  .ivtc-info {
    h4 { font-size: 15px; font-weight: 700; color: #1f2937; margin: 0 0 4px; }
    p { font-size: 12px; color: #9ca3af; margin: 0; }
  }
  .ivtc-check {
    position: absolute;
    top: 12px; right: 12px;
    width: 24px; height: 24px;
    border-radius: 50%;
    background: #6366f1;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 700;
  }
}

.iv-form-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 20px;
  .ivf-row {
    flex: 1;
    label { display: block; font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 8px; }
  }
  .ivf-input { :deep(.el-input__inner) { border-radius: 10px; height: 44px; } }
}

.ivf-start-btn {
  height: 48px;
  padding: 0 36px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  flex-shrink: 0;
  &:hover { transform: translateY(-2px); }
}

// 面试提示
.iv-tips {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  .ivt-header { font-size: 16px; font-weight: 700; color: #1f2937; margin-bottom: 16px; }
}

.ivt-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  .ivt-item {
    display: flex;
    gap: 10px;
    .ivt-num {
      width: 26px; height: 26px;
      border-radius: 8px;
      background: #eff6ff;
      color: #3b82f6;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 700;
      flex-shrink: 0;
    }
    strong { font-size: 13px; color: #374151; }
    p { font-size: 11px; color: #9ca3af; margin: 2px 0 0; }
  }
}

// ======== 面试中 ========
.iva-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 16px 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  flex-wrap: wrap;
  gap: 12px;
  .iva-left {
    display: flex; align-items: center; gap: 14px;
    .iva-badge {
      padding: 5px 14px;
      border-radius: 8px;
      font-size: 13px;
      font-weight: 700;
    }
    .iva-progress { font-size: 14px; color: #6b7280; font-weight: 500; }
  }
  .iva-right {
    display: flex; align-items: center; gap: 12px;
    .iva-progress-bar {
      width: 160px; height: 6px;
      background: #e5e7eb;
      border-radius: 3px;
      overflow: hidden;
      .ivap-fill {
        height: 100%;
        background: linear-gradient(90deg, #6366f1, #8b5cf6);
        border-radius: 3px;
        transition: width 0.4s ease;
      }
    }
  }
}

.iva-chat {
  background: #fff;
  border-radius: 0 0 16px 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

// ======== 报告 ========
.iv-report {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.ivr-hero {
  text-align: center;
  padding: 32px 20px 20px;
  background: linear-gradient(180deg, #f8faff 0%, #fff 100%);
}

.ivrh-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
  &.score-excellent { background: #d1fae5; color: #065f46; }
  &.score-good { background: #dbeafe; color: #1e40af; }
  &.score-average { background: #fef3c7; color: #92400e; }
  &.score-need { background: #fee2e2; color: #991b1b; }
}

.ivr-hero h2 { font-size: 24px; font-weight: 800; color: #1f2937; margin: 0 0 4px; }
.ivr-hero p { color: #6b7280; font-size: 14px; }

.ivr-score-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.ivrs-ring {
  position: relative;
  width: 160px; height: 160px;
  .ivrs-animate { animation: ringFill 1.5s ease-out forwards; }
}

@keyframes ringFill {
  from { stroke-dasharray: 0 427; }
}

.ivrs-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .ivrs-value { font-size: 40px; font-weight: 800; color: #1f2937; line-height: 1; }
  .ivrs-label { font-size: 13px; color: #6b7280; margin-top: 4px; }
}

.ivr-dimensions {
  padding: 0 24px 20px;
  h3 { font-size: 16px; font-weight: 700; color: #1f2937; margin-bottom: 14px; }
}

.ivrd-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}
.ivrd-card {
  padding: 14px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  .ivrd-name { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 8px; }
  .ivrd-bar-wrap { display: flex; align-items: center; gap: 10px; }
  .ivrd-bar { flex: 1; height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; }
  .ivrd-fill { height: 100%; border-radius: 4px; transition: width 1s ease-out; }
  .ivrd-value { font-size: 15px; font-weight: 700; }
}

.ivr-feedback {
  padding: 0 24px 20px;
}

.ivrf-card {
  border-radius: 14px;
  padding: 20px 24px;
  &.highlight { background: #f0fdf4; border: 1px solid #86efac; }
  &.improve { background: #fefce8; border: 1px solid #fde68a; }
  .ivrf-header {
    font-size: 15px; font-weight: 700;
    color: #1f2937;
    display: flex; align-items: center; gap: 8px;
    margin-bottom: 12px;
    .ivrf-icon { font-size: 18px; }
  }
  ul { list-style: none; padding: 0; }
  li {
    padding: 8px 0;
    font-size: 13px;
    color: #374151;
    line-height: 1.5;
    border-bottom: 1px solid rgba(0,0,0,0.04);
    &:last-child { border-bottom: none; }
    &::before { content: '• '; color: #9ca3af; margin-right: 6px; }
  }
}

.ivr-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 0 24px 24px;
}
</style>
