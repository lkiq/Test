<template>
  <div class="lr-page">
    <!-- 顶部导航 -->
    <div class="lr-topbar">
      <el-button text @click="goBack" :icon="ArrowLeft" :disabled="testing">返回</el-button>
      <h2>学习成果测评</h2>
      <div></div>
    </div>

    <!-- ===== 阶段选择 ===== -->
    <div v-if="!testing && !result" class="lr-start">
      <div class="lrs-card">
        <div class="lrs-icon">📝</div>
        <h3>选择测评阶段</h3>
        <p>选择您已完成学习的阶段进行测评，检验学习成果</p>
        <div class="lrs-stages">
          <div
            v-for="s in stages"
            :key="s.value"
            class="lrs-stage-card"
            :class="{ active: selectedStage === s.value }"
            @click="selectedStage = s.value"
          >
            <div class="lrss-num">{{ s.num }}</div>
            <div class="lrss-info">
              <span class="lrss-name">{{ s.label }}</span>
              <span class="lrss-desc">{{ s.desc }}</span>
            </div>
          </div>
        </div>
        <el-button
          type="primary"
          size="large"
          :disabled="!selectedStage"
          @click="startTest"
          :loading="starting"
          class="lrs-btn"
        >
          开始测评
        </el-button>
      </div>
    </div>

    <!-- ===== 答题区 ===== -->
    <div v-if="testing && !result" class="lr-test">
      <!-- 进度条 -->
      <div class="lrt-header">
        <div class="lrth-info">
          <el-tag type="primary" effect="plain" round>{{ currentStageLabel }}</el-tag>
          <span class="lrth-progress">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
        </div>
        <div class="lrth-bar">
          <div class="lrth-fill" :style="{ width: ((currentIndex + 1) / questions.length * 100) + '%' }"></div>
        </div>
      </div>

      <!-- 题目区 -->
      <div class="lrt-question" v-if="questions.length">
        <div class="lrtq-header">
          <span class="lrtq-type">{{ currentQ.type === 'MULTI' ? '多选题' : '单选题' }}</span>
          <span class="lrtq-score">{{ currentQ.score || 5 }} 分</span>
        </div>
        <p class="lrtq-content">{{ currentQ.content }}</p>
        <div class="lrtq-options">
          <div
            v-for="(opt, oi) in currentQ.options"
            :key="oi"
            class="lrtq-opt"
            :class="{ selected: isSelected(oi) }"
            @click="toggleOption(oi)"
          >
            <div class="lrtq-opt-radio" :class="{ checked: isSelected(oi) }">
              <span v-if="isSelected(oi)">✓</span>
            </div>
            <span class="lrtq-opt-text">{{ opt }}</span>
          </div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="lrt-footer">
        <el-button
          v-if="currentIndex > 0"
          @click="currentIndex--"
          :icon="ArrowLeft"
        >上一题</el-button>
        <div class="lrtf-spacer"></div>
        <el-button
          v-if="currentIndex < questions.length - 1"
          type="primary"
          @click="currentIndex++"
        >下一题 <el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
        <el-button
          v-else
          type="success"
          @click="submitTest"
          :loading="submitting"
          :disabled="unansweredCount > 0"
        >
          提交答卷（{{ unansweredCount }} 题未答）
        </el-button>
      </div>

      <!-- 题号导航 -->
      <div class="lrt-nav">
        <div
          v-for="(q, qi) in questions"
          :key="qi"
          class="lrtn-dot"
          :class="{ active: qi === currentIndex, answered: answers[qi] }"
          @click="currentIndex = qi"
        >{{ qi + 1 }}</div>
      </div>
    </div>

    <!-- ===== 结果区 ===== -->
    <div v-if="result" class="lr-result">
      <div class="lrr-card">
        <div class="lrr-score-circle" :style="{ borderColor: scoreColor }">
          <span class="lrr-score-num" :style="{ color: scoreColor }">{{ result.totalScore }}分</span>
          <span class="lrr-score-level">{{ result.level }}</span>
        </div>
        <div class="lrr-stats">
          <div class="lrr-stat">
            <span class="lrrs-label">正确</span>
            <span class="lrrs-value green">{{ result.correctCount }}</span>
          </div>
          <div class="lrr-stat">
            <span class="lrrs-label">总计</span>
            <span class="lrrs-value">{{ result.totalCount }}</span>
          </div>
          <div class="lrr-stat">
            <span class="lrrs-label">正确率</span>
            <span class="lrrs-value" :style="{ color: scoreColor }">{{ result.totalCount ? Math.round(result.correctCount / result.totalCount * 100) : 0 }}%</span>
          </div>
        </div>

        <div class="lrr-pass" v-if="result.passed">
          <span>🎉 恭喜通过测评！</span>
        </div>
        <div class="lrr-fail" v-else>
          <span>📖 继续加油，建议复习相关知识后再试一次</span>
        </div>

        <!-- 优势与不足 -->
        <div class="lrr-detail" v-if="result.strengths || result.weaknesses">
          <div class="lrrd-item" v-if="result.strengths">
            <span class="lrrd-tag green">优势</span>
            <p>{{ result.strengths }}</p>
          </div>
          <div class="lrrd-item" v-if="result.weaknesses">
            <span class="lrrd-tag orange">薄弱</span>
            <p>{{ result.weaknesses }}</p>
          </div>
        </div>
      </div>

      <!-- 题目详情 -->
      <div class="lrr-questions" v-if="result.questions?.length">
        <h3>答题详情</h3>
        <div v-for="(q, qi) in result.questions" :key="qi" class="lrrq-card" :class="{ correct: q.isCorrect, wrong: !q.isCorrect }">
          <div class="lrrq-header">
            <span class="lrrq-num">{{ qi + 1 }}</span>
            <span class="lrrq-status">{{ q.isCorrect ? '✓ 正确' : '✗ 错误' }}</span>
          </div>
          <p class="lrrq-content">{{ q.content }}</p>
          <div class="lrrq-answers">
            <span class="lrrqa-label">你的答案：</span>
            <span :class="q.isCorrect ? 'lrrqa-correct' : 'lrrqa-wrong'">{{ q.userAnswer || '未作答' }}</span>
            <span v-if="!q.isCorrect" class="lrrqa-right">正确答案：{{ q.correctAnswer }}</span>
          </div>
          <p class="lrrq-explanation" v-if="!q.isCorrect && q.explanation">{{ q.explanation }}</p>
        </div>
      </div>

      <div class="lrr-actions">
        <el-button @click="resetTest" :icon="RefreshRight">重新测评</el-button>
        <el-button type="primary" @click="goBack">返回选择</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, RefreshRight } from '@element-plus/icons-vue'
import { getQuestions, submitTest as submitApi } from '@/api/learning-result'

const router = useRouter()

const stages = [
  { value: 'BASIC', label: '基础阶段', desc: '编程语言、数据结构、网络基础', num: '01' },
  { value: 'FRAMEWORK', label: '框架阶段', desc: 'Spring Boot、Vue.js、数据库设计', num: '02' },
  { value: 'PROJECT', label: '项目阶段', desc: '项目架构、CI/CD、代码规范', num: '03' },
  { value: 'INTERVIEW', label: '面试阶段', desc: '算法、系统设计、行为面试', num: '04' },
  { value: 'FINAL', label: '综合阶段', desc: '架构设计、职业素养、综合能力', num: '05' },
]

const selectedStage = ref('')
const starting = ref(false)
const testing = ref(false)
const submitting = ref(false)
const result = ref<any>(null)
const questions = ref<any[]>([])
const currentIndex = ref(0)
const answers = ref<Record<number, string>>({})

const currentStageLabel = computed(() => {
  return stages.find(s => s.value === selectedStage.value)?.label || ''
})

const currentQ = computed(() => {
  return questions.value[currentIndex.value] || { options: [] }
})

const unansweredCount = computed(() => {
  let count = 0
  for (let i = 0; i < questions.value.length; i++) {
    if (!answers.value[i]) count++
  }
  return count
})

const scoreColor = computed(() => {
  if (!result.value) return '#10b981'
  const s = result.value.totalScore
  if (s >= 90) return '#10b981'
  if (s >= 75) return '#3b82f6'
  if (s >= 60) return '#f59e0b'
  return '#ef4444'
})

function isSelected(optionIndex: number) {
  const ans = answers.value[currentIndex.value]
  if (!ans) return false
  return ans.split(',').includes(String.fromCharCode(65 + optionIndex))
}

function toggleOption(optionIndex: number) {
  const letter = String.fromCharCode(65 + optionIndex)
  const current = answers.value[currentIndex.value] || ''
  if (currentQ.value.type === 'MULTI') {
    const selected = current ? current.split(',') : []
    const idx = selected.indexOf(letter)
    if (idx >= 0) selected.splice(idx, 1)
    else selected.push(letter)
    answers.value[currentIndex.value] = selected.join(',')
  } else {
    answers.value[currentIndex.value] = letter
  }
}

async function startTest() {
  starting.value = true
  try {
    const res: any = await getQuestions(selectedStage.value)
    const rawQuestions = res.data || []
    if (!rawQuestions.length) {
      ElMessage.warning('该阶段暂无测评题目')
      return
    }
    questions.value = rawQuestions
    currentIndex.value = 0
    answers.value = {}
    testing.value = true
    result.value = null
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载题目失败')
  } finally {
    starting.value = false
  }
}

async function submitTest() {
  submitting.value = true
  try {
    const answersMap: Record<string, string> = {}
    for (const [k, v] of Object.entries(answers.value)) {
      answersMap[String(k)] = v
    }
    const res: any = await submitApi({
      stage: selectedStage.value,
      answers: answersMap,
    })
    result.value = res.data
    testing.value = false
    ElMessage.success(res.data.passed ? '恭喜通过测评！' : '测评完成，继续加油！')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function resetTest() {
  result.value = null
  testing.value = true
  answers.value = {}
  currentIndex.value = 0
}

function goBack() {
  if (testing.value) {
    testing.value = false
    return
  }
  if (result.value) {
    result.value = null
    return
  }
  router.back()
}
</script>

<style scoped>
.lr-page {
  max-width: 760px; margin: 0 auto; padding: 16px 20px 40px;
  min-height: 100vh; background: #f8fafc;
}

/* 顶部 */
.lr-topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0; margin-bottom: 20px;
}
.lr-topbar h2 { font-size: 18px; font-weight: 700; color: #1f2937; margin: 0; }

/* ===== 阶段选择 ===== */
.lr-start { padding-top: 20px; }
.lrs-card {
  background: #fff; border-radius: 18px; padding: 36px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); text-align: center;
  border: 1px solid #f0f0f0;
}
.lrs-icon { font-size: 48px; margin-bottom: 12px; }
.lrs-card h3 { font-size: 20px; font-weight: 700; color: #1f2937; margin: 0 0 6px; }
.lrs-card > p { font-size: 14px; color: #9ca3af; margin: 0 0 24px; }
.lrs-stages { display: flex; flex-direction: column; gap: 8px; margin-bottom: 24px; }
.lrs-stage-card {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 18px; border-radius: 12px;
  border: 2px solid #e5e7eb; cursor: pointer;
  transition: all 0.2s; text-align: left;
}
.lrs-stage-card:hover { border-color: #818cf8; background: #fafaff; }
.lrs-stage-card.active { border-color: #6366f1; background: #eef2ff; }
.lrss-num {
  width: 40px; height: 40px; border-radius: 10px;
  background: #f3f4f6; display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700; color: #9ca3af; flex-shrink: 0;
}
.lrs-stage-card.active .lrss-num { background: #6366f1; color: #fff; }
.lrss-info { display: flex; flex-direction: column; gap: 2px; }
.lrss-name { font-size: 15px; font-weight: 600; color: #1f2937; }
.lrss-desc { font-size: 12px; color: #9ca3af; }
.lrs-btn { width: 100%; height: 46px; border-radius: 12px; font-weight: 700; }

/* ===== 答题区 ===== */
.lr-test { padding-top: 8px; }
.lrt-header { margin-bottom: 24px; }
.lrth-info { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.lrth-progress { font-size: 14px; color: #6b7280; font-weight: 500; }
.lrth-bar { height: 6px; background: #e5e7eb; border-radius: 3px; overflow: hidden; }
.lrth-fill { height: 100%; background: linear-gradient(90deg, #6366f1, #8b5cf6); border-radius: 3px; transition: width 0.4s; }

.lrt-question {
  background: #fff; border-radius: 16px; padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid #f0f0f0;
  margin-bottom: 20px;
}
.lrtq-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.lrtq-type { font-size: 13px; color: #6366f1; font-weight: 600; background: #eef2ff; padding: 4px 12px; border-radius: 8px; }
.lrtq-score { font-size: 13px; color: #9ca3af; }
.lrtq-content { font-size: 16px; line-height: 1.7; color: #1f2937; margin: 0 0 24px; }

.lrtq-options { display: flex; flex-direction: column; gap: 10px; }
.lrtq-opt {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; border-radius: 12px;
  border: 2px solid #e5e7eb; cursor: pointer;
  transition: all 0.2s;
}
.lrtq-opt:hover { border-color: #a5b4fc; }
.lrtq-opt.selected { border-color: #6366f1; background: #eef2ff; }
.lrtq-opt-radio {
  width: 26px; height: 26px; border-radius: 50%;
  border: 2px solid #d1d5db; display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #fff; flex-shrink: 0; transition: all 0.2s;
}
.lrtq-opt-radio.checked { border-color: #6366f1; background: #6366f1; }
.lrtq-opt-text { font-size: 14px; color: #374151; }

.lrt-footer { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.lrtf-spacer { flex: 1; }

.lrt-nav { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.lrtn-dot {
  width: 36px; height: 36px; border-radius: 10px;
  background: #f3f4f6; display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; color: #9ca3af; cursor: pointer; transition: all 0.2s;
}
.lrtn-dot.active { background: #6366f1; color: #fff; }
.lrtn-dot.answered { background: #dbeafe; color: #3b82f6; }

/* ===== 结果区 ===== */
.lr-result { padding-top: 10px; }
.lrr-card {
  background: #fff; border-radius: 18px; padding: 36px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); text-align: center;
  border: 1px solid #f0f0f0; margin-bottom: 20px;
}
.lrr-score-circle {
  width: 140px; height: 140px; border-radius: 50%;
  border: 6px solid #10b981; margin: 0 auto 24px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.lrr-score-num { font-size: 32px; font-weight: 800; }
.lrr-score-level { font-size: 14px; color: #9ca3af; font-weight: 500; }
.lrr-stats { display: flex; justify-content: center; gap: 36px; margin-bottom: 20px; }
.lrr-stat { display: flex; flex-direction: column; gap: 2px; }
.lrrs-label { font-size: 12px; color: #9ca3af; }
.lrrs-value { font-size: 22px; font-weight: 700; color: #1f2937; }
.lrrs-value.green { color: #10b981; }
.lrr-pass { padding: 10px; background: #ecfdf5; border-radius: 10px; color: #059669; font-weight: 600; margin-bottom: 16px; }
.lrr-fail { padding: 10px; background: #fef3c7; border-radius: 10px; color: #d97706; font-weight: 600; margin-bottom: 16px; }
.lrr-detail { text-align: left; display: flex; flex-direction: column; gap: 10px; }
.lrrd-item { padding: 12px 16px; border-radius: 10px; border: 1px solid #e5e7eb; }
.lrrd-item p { font-size: 13px; color: #6b7280; margin: 6px 0 0; }
.lrrd-tag { font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 6px; }
.lrrd-tag.green { background: #ecfdf5; color: #059669; }
.lrrd-tag.orange { background: #fef3c7; color: #d97706; }

/* 题目详情 */
.lrr-questions { margin-bottom: 24px; }
.lrr-questions h3 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0 0 12px; }
.lrrq-card {
  background: #fff; border-radius: 14px; padding: 18px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.03); border: 1px solid #f0f0f0;
  margin-bottom: 8px;
}
.lrrq-card.correct { border-left: 4px solid #10b981; }
.lrrq-card.wrong { border-left: 4px solid #ef4444; }
.lrrq-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.lrrq-num {
  width: 26px; height: 26px; border-radius: 8px; background: #f3f4f6;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #6b7280;
}
.lrrq-status { font-size: 12px; font-weight: 600; }
.lrrq-card.correct .lrrq-status { color: #059669; }
.lrrq-card.wrong .lrrq-status { color: #dc2626; }
.lrrq-content { font-size: 14px; color: #374151; line-height: 1.6; margin: 0 0 10px; }
.lrrq-answers { display: flex; flex-wrap: wrap; gap: 8px; font-size: 13px; align-items: center; }
.lrrqa-label { color: #9ca3af; }
.lrrqa-correct { color: #059669; font-weight: 600; }
.lrrqa-wrong { color: #dc2626; font-weight: 600; text-decoration: line-through; }
.lrrqa-right { color: #10b981; font-weight: 600; }
.lrrq-explanation { font-size: 13px; color: #6366f1; background: #eef2ff; padding: 10px 14px; border-radius: 8px; margin: 10px 0 0; line-height: 1.6; }

.lrr-actions { display: flex; justify-content: center; gap: 12px; padding-bottom: 40px; }
</style>
