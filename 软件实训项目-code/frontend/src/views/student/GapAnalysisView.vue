<template>
  <div class="gap-page">
    <!-- 模式选择器 (始终可见，有报告时显示模式切换) -->
    <div class="mode-selector" v-if="!report || report.mode">
      <div class="mode-tabs">
        <button
          class="mode-tab"
          :class="{ active: analysisMode === 'single' }"
          @click="switchMode('single')"
        >
          🎯 单岗位分析
        </button>
        <button
          class="mode-tab"
          :class="{ active: analysisMode === 'multi' }"
          @click="switchMode('multi')"
        >
          📊 多岗位合并分析
        </button>
      </div>
      <span class="mode-hint" v-if="analysisMode === 'multi'">最多选择 3 个岗位进行合并差距分析</span>
    </div>

    <!-- ============ 选择面板（无报告时显示） ============ -->
    <div v-if="!report" class="selection-panel">
      <!-- 有收藏时 -->
      <template v-if="favJobList.length > 0">
        <h3 class="panel-title">
          {{ analysisMode === 'single' ? '从收藏中选择一个岗位' : '从收藏中选择岗位（最多3个）' }}
        </h3>
        <div class="fav-job-list">
          <div
            v-for="job in favJobList"
            :key="job.id"
            class="fav-job-card"
            :class="{
              selected: analysisMode === 'single'
                ? selectedJobId === job.id
                : selectedJobIds.has(job.id)
            }"
            @click="toggleJobSelect(job.id)"
          >
            <div class="fjc-check">
              <el-icon :size="20" v-if="analysisMode === 'single'">
                <CircleCheckFilled v-if="selectedJobId === job.id" />
                <CircleCheck v-else />
              </el-icon>
              <el-checkbox
                v-else
                :model-value="selectedJobIds.has(job.id)"
                :disabled="!selectedJobIds.has(job.id) && selectedJobIds.size >= 3"
                @click.stop
                @change="toggleJobSelect(job.id)"
              />
            </div>
            <div class="fjc-body">
              <div class="fjc-title">{{ job.title }}</div>
              <div class="fjc-meta">
                <span>{{ job.companyName }}</span>
                <span class="dot">·</span>
                <span>{{ job.city }}</span>
                <span class="dot">·</span>
                <span class="fjc-salary">{{ job.salaryRange }}</span>
              </div>
              <div class="fjc-tags">
                <el-tag size="small" type="info">{{ job.direction }}</el-tag>
                <el-tag size="small" v-if="job.experience !== '经验不限'" type="info">{{ job.experience }}</el-tag>
              </div>
            </div>
          </div>
        </div>
        <div class="panel-actions">
          <el-button
            type="primary"
            size="large"
            :disabled="!canAnalyze"
            :loading="analyzing"
            @click="startAnalysis"
          >
            📊 开始差距分析
          </el-button>
        </div>
      </template>

      <!-- 无收藏时 -->
      <div v-else class="gap-empty">
        <div class="empty-visual">
          <div class="empty-icon-bg">🔍</div>
          <div class="empty-ripple r1"></div>
          <div class="empty-ripple r2"></div>
        </div>
        <h2>还没有收藏岗位</h2>
        <p>先去岗位匹配页面收藏感兴趣的岗位，再进行差距分析</p>
        <div class="empty-features">
          <div class="ef-item">
            <span class="ef-icon">⭐</span>
            <span>收藏岗位</span>
          </div>
          <div class="ef-item">
            <span class="ef-icon">📊</span>
            <span>差距精准定位</span>
          </div>
          <div class="ef-item">
            <span class="ef-icon">📚</span>
            <span>学习路径推荐</span>
          </div>
        </div>
        <el-button type="primary" size="large" @click="$router.push('/student/job-matching')">
          前往岗位匹配
        </el-button>
      </div>
    </div>

    <!-- ============ 报告面板（有报告时显示） ============ -->
    <template v-if="report">
      <!-- 顶部匹配概览 -->
      <div class="gap-hero" :style="{ '--match-color': gapColor(report.overallMatch || 0) }">
        <div class="gap-hero-bg">
          <div class="gh-circle c1"></div>
          <div class="gh-circle c2"></div>
        </div>
        <div class="gap-hero-content">
          <div class="gh-left">
            <div class="gh-badges">
              <el-tag effect="dark" class="gh-badge">🎯 目标岗位</el-tag>
              <el-tag v-if="report.mode === 'multi'" effect="dark" type="warning" class="gh-badge">
                多岗位合并
              </el-tag>
            </div>
            <h1 class="gh-title">{{ reportTitle }}</h1>
            <!-- 多岗位模式下显示来源岗位标签 -->
            <div v-if="report.mode === 'multi' && report.sourceJobs" class="gh-source-jobs">
              <el-tag
                v-for="title in getSourceJobTitles()"
                :key="title"
                size="small"
                effect="plain"
                type="warning"
              >
                {{ title }}
              </el-tag>
            </div>
            <p class="gh-desc">以下是你的能力与岗位要求的差距分析</p>
            <el-button type="primary" size="large" class="gh-action" @click="goToLearningPath">
              📚 生成学习路径 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="gh-right">
            <div class="match-gauge">
              <svg viewBox="0 0 140 140">
                <circle cx="70" cy="70" r="60" fill="none" stroke="#e5e7eb" stroke-width="10" />
                <circle cx="70" cy="70" r="60" fill="none" :stroke="gapColor(report.overallMatch || 0)"
                  stroke-width="10" stroke-linecap="round"
                  :stroke-dasharray="(report.overallMatch || 0) * 3.77 + ' ' + (377 - (report.overallMatch || 0) * 3.77)"
                  transform="rotate(-90 70 70)"
                  class="gauge-animate" />
              </svg>
              <div class="gauge-center">
                <span class="gauge-value">{{ report.overallMatch || 0 }}</span>
                <span class="gauge-label">综合匹配度</span>
                <span class="gauge-sub" :style="{ color: gapColor(report.overallMatch || 0) }">
                  {{ matchLevel(report.overallMatch || 0) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 技能差距矩阵 -->
      <div class="gap-matrix-section">
        <h3 class="section-title-big">
          <span class="st-icon">📋</span> 技能差距矩阵
          <span class="st-sub">紫色=岗位要求 · 蓝色=你的水平</span>
        </h3>

        <div class="gap-cards">
          <div
            v-for="gap in report.gaps || []"
            :key="gap.skillName"
            class="gap-card"
            :class="'gap-' + gapClass(gap.gapDegree)"
          >
            <div class="gc-header">
              <span class="gc-skill">{{ gap.skillName }}</span>
              <div class="gc-header-right">
                <!-- 多岗位来源标注 -->
                <template v-if="report.mode === 'multi' && gap.sourceJobs?.length">
                  <el-tag
                    v-for="src in gap.sourceJobs"
                    :key="src"
                    size="small"
                    effect="plain"
                    type="warning"
                    class="gc-source-tag"
                  >
                    {{ getJobTitleById(src) }}
                  </el-tag>
                </template>
                <el-tag
                  :type="gap.gapDegree === '严重不足' ? 'danger' : gap.gapDegree === '需要提升' ? 'warning' : 'success'"
                  size="small"
                  effect="dark"
                >
                  {{ gap.gapDegree }}
                </el-tag>
              </div>
            </div>

            <div class="gc-compare">
              <div class="gc-bar-row">
                <span class="gc-bar-label">你的水平</span>
                <div class="gc-bar-wrap">
                  <div class="gc-bar gc-bar-user" :style="{ width: levelPercent(gap.userLevel) + '%' }">
                    <span class="gc-bar-text">{{ gap.userLevel }}</span>
                  </div>
                </div>
              </div>
              <div class="gc-bar-row">
                <span class="gc-bar-label">岗位要求</span>
                <div class="gc-bar-wrap">
                  <div class="gc-bar gc-bar-req" :style="{ width: levelPercent(gap.requiredLevel) + '%' }">
                    <span class="gc-bar-text">{{ gap.requiredLevel }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 雷达图 -->
      <div v-if="report.radarChart" class="gap-radar-section">
        <h3 class="section-title-big">
          <span class="st-icon">📊</span> 能力雷达图对比
        </h3>
        <div class="radar-card">
          <RadarChart :data="report.radarChart" />
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="gap-actions">
        <el-button size="large" @click="resetAnalysis">
          <el-icon><ArrowLeft /></el-icon> 重新选择岗位
        </el-button>
        <el-button type="primary" size="large" @click="goToLearningPath">
          📚 生成学习路径 <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button type="warning" size="large" @click="$router.push('/student/interview')">
          🎤 模拟面试练习
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight, ArrowLeft, CircleCheck, CircleCheckFilled } from '@element-plus/icons-vue'
import { analyzeGap, analyzeMultipleGap } from '@/api/student'
import { useJobStore } from '@/stores/job'
import { type EnrichedJob } from '@/utils/jobEnrich'
import RadarChart from '@/components/charts/RadarChart.vue'

const route = useRoute()
const router = useRouter()
const jobStore = useJobStore()

// ---- 状态 ----
const report = ref<any>(null)
const analysisMode = ref<'single' | 'multi'>('single')
const selectedJobId = ref<number>(0)
const selectedJobIds = ref<Set<number>>(new Set())
const analyzing = ref(false)
const favJobDetails = ref<EnrichedJob[]>([])

// ---- 计算 ----
/** 收藏岗位列表（优先用 store 独立加载的详情，否则按 ID 构造简单对象） */
const favJobList = computed(() => {
  const details = jobStore.favoriteJobDetails
  if (details.length > 0) return details.slice(0, 10)
  const storeJobs = jobStore.favoriteJobs
  if (storeJobs.length > 0) return storeJobs.slice(0, 10)
  return favJobDetails.value
})

/** 可否开始分析 */
const canAnalyze = computed(() => {
  if (analysisMode.value === 'single') return selectedJobId.value > 0
  return selectedJobIds.value.size >= 1 && selectedJobIds.value.size <= 3
})

/** 报告标题 */
const reportTitle = computed(() => {
  if (!report.value) return ''
  if (report.value.mode === 'multi') {
    const titles = getSourceJobTitles()
    return titles.join(' + ') + ' 综合差距分析'
  }
  return report.value.jobTitle || ''
})

// ---- 初始化 ----
onMounted(async () => {
  await jobStore.fetchFavorites()

  const jobIdParam = route.query.jobId

  // 如果从岗位详情跳转过来，自动单岗位分析
  if (jobIdParam) {
    const jid = Number(jobIdParam)
    selectedJobId.value = jid
    analysisMode.value = 'single'
    await analyzeSingleJob(jid)
  } else {
    // 否则加载收藏岗位详情用于展示
    await loadFavJobDetails()
  }
})

// ---- 方法 ----
async function loadFavJobDetails() {
  const favIds = Array.from(jobStore.favorites)
  if (favIds.length === 0) return

  // 通过 store 独立加载收藏岗位详情（不依赖 jobStore.jobs）
  await jobStore.fetchFavoriteJobs()
}

function switchMode(mode: 'single' | 'multi') {
  analysisMode.value = mode
  // 切换模式时清空之前的选择
  if (mode === 'single') {
    selectedJobIds.value = new Set()
  } else {
    selectedJobId.value = 0
  }
}

function toggleJobSelect(jobId: number) {
  if (analysisMode.value === 'single') {
    selectedJobId.value = selectedJobId.value === jobId ? 0 : jobId
  } else {
    const newSet = new Set(selectedJobIds.value)
    if (newSet.has(jobId)) {
      newSet.delete(jobId)
    } else if (newSet.size < 3) {
      newSet.add(jobId)
    }
    selectedJobIds.value = newSet
  }
}

async function startAnalysis() {
  analyzing.value = true
  try {
    if (analysisMode.value === 'single' && selectedJobId.value > 0) {
      await analyzeSingleJob(selectedJobId.value)
    } else if (analysisMode.value === 'multi' && selectedJobIds.value.size > 0) {
      const res: any = await analyzeMultipleGap(Array.from(selectedJobIds.value))
      report.value = res.data
    }
  } catch (e: any) {
    console.error('差距分析失败:', e)
  } finally {
    analyzing.value = false
  }
}

async function analyzeSingleJob(jobId: number) {
  try {
    const res: any = await analyzeGap(jobId)
    report.value = res.data
  } catch (e: any) {
    console.error('差距分析失败:', e)
  }
}

function resetAnalysis() {
  report.value = null
  selectedJobId.value = 0
  selectedJobIds.value = new Set()
}

function getSourceJobTitles(): string[] {
  if (!report.value?.sourceJobs) return []
  const jobMap = report.value.sourceJobs as Record<string, string[]>
  return Object.keys(jobMap)
}

function getJobTitleById(idOrTitle: string): string {
  // sourceJobs value 可能是岗位名数组或岗位ID
  const job = favJobList.value.find(j => j.id === Number(idOrTitle) || j.title === idOrTitle)
  return job?.title || idOrTitle
}

function goToLearningPath() {
  if (!report.value) return
  if (report.value.mode === 'multi') {
    const ids = Array.from(selectedJobIds.value).join(',')
    router.push(`/student/learning-path?jobIds=${ids}&mode=merged`)
  } else {
    router.push(`/student/learning-path?jobId=${report.value.jobId || selectedJobId.value}`)
  }
}

// ---- 工具函数 ----
function gapColor(percentage: number) {
  if (percentage >= 80) return '#10b981'
  if (percentage >= 60) return '#3b82f6'
  if (percentage >= 40) return '#f59e0b'
  return '#ef4444'
}

function matchLevel(pct: number): string {
  if (pct >= 80) return '高度匹配'
  if (pct >= 60) return '基本匹配'
  if (pct >= 40) return '差距较大'
  return '差距显著'
}

function gapClass(degree: string): string {
  if (degree === '严重不足') return 'critical'
  if (degree === '需要提升') return 'warning'
  return 'ok'
}

function levelPercent(level: string): number {
  const map: Record<string, number> = {
    '精通': 95, '熟练': 80, '掌握': 55, '了解': 30, '入门': 15,
    '未掌握': 5,
    'EXPERT': 95, 'PROFICIENT': 80, 'MASTER': 55, 'BASIC': 30, 'NOT_MASTERED': 5
  }
  return map[level] || 40
}
</script>

<style scoped lang="scss">
// ======== 模式选择器 ========
.mode-selector {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.mode-tabs {
  display: flex;
  background: #f3f4f6;
  border-radius: 10px;
  padding: 4px;
}

.mode-tab {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.25s;
  white-space: nowrap;

  &:hover {
    color: #374151;
  }

  &.active {
    background: #fff;
    color: #6366f1;
    font-weight: 600;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  }
}

.mode-hint {
  font-size: 12px;
  color: #9ca3af;
}

// ======== 选择面板 ========
.selection-panel {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 20px;
}

.fav-job-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.fav-job-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #6366f1;
    background: #faf5ff;
  }

  &.selected {
    border-color: #6366f1;
    background: #f0ebff;
    box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
  }
}

.fjc-check {
  padding-top: 2px;
  color: #6366f1;
  flex-shrink: 0;
}

.fjc-body {
  flex: 1;
  min-width: 0;
}

.fjc-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.fjc-meta {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;

  .dot { color: #d1d5db; }
  .fjc-salary { color: #f56c6c; font-weight: 500; }
}

.fjc-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.panel-actions {
  display: flex;
  justify-content: center;
}

// ======== Hero ========
.gap-hero {
  position: relative;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 20px;
}

.gap-hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .gh-circle {
    position: absolute;
    border-radius: 50%;
    border: 1px solid rgba(255,255,255,0.06);
    &.c1 { width: 260px; height: 260px; top: -80px; right: -40px; }
    &.c2 { width: 180px; height: 180px; bottom: -50px; left: 20%; }
  }
}

.gap-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 36px 40px;
  gap: 40px;
}

.gh-left {
  flex: 1;
  .gh-badges { display: flex; gap: 8px; margin-bottom: 12px; }
  .gh-badge { font-weight: 600; }
  .gh-title { font-size: 30px; font-weight: 800; color: #fff; margin: 0 0 8px; }
  .gh-source-jobs {
    display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px;
  }
  .gh-desc { color: #94a3b8; font-size: 14px; margin-bottom: 20px; }
  .gh-action {
    height: 44px;
    border-radius: 12px;
    font-weight: 600;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
    &:hover { transform: translateY(-2px); }
  }
}

.gh-right {
  flex-shrink: 0;
}

.match-gauge {
  position: relative;
  width: 140px; height: 140px;
  .gauge-animate {
    animation: gaugeFill 1.5s ease-out forwards;
    stroke-dasharray: 0 377;
  }
  .gauge-center {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .gauge-value { font-size: 36px; font-weight: 800; color: #fff; line-height: 1; }
    .gauge-label { font-size: 11px; color: #94a3b8; margin-top: 2px; }
    .gauge-sub { font-size: 12px; font-weight: 600; margin-top: 2px; }
  }
}

@keyframes gaugeFill {
  to { stroke-dasharray: var(--target-dash) 377; }
}

// ======== 矩阵 ========
.gap-matrix-section {
  margin-bottom: 20px;
}

.section-title-big {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  .st-icon { font-size: 22px; }
  .st-sub {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 400;
    margin-left: auto;
  }
}

.gap-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 14px;
}

.gap-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border-left: 5px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.3s;
  &:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.08); transform: translateY(-2px); }
  &.gap-critical { border-left-color: #ef4444; }
  &.gap-warning { border-left-color: #f59e0b; }
  &.gap-ok { border-left-color: #10b981; }
}

.gc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;

  .gc-skill { font-size: 15px; font-weight: 700; color: #1f2937; }
  .gc-header-right {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }
  .gc-source-tag {
    opacity: 0.85;
  }
}

.gc-compare {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.gc-bar-row {
  display: flex;
  align-items: center;
  gap: 12px;
  .gc-bar-label {
    width: 60px;
    font-size: 12px;
    color: #6b7280;
    text-align: right;
    flex-shrink: 0;
  }
  .gc-bar-wrap {
    flex: 1;
    height: 24px;
    background: #f3f4f6;
    border-radius: 12px;
    overflow: hidden;
  }
  .gc-bar {
    height: 100%;
    border-radius: 12px;
    display: flex;
    align-items: center;
    padding-left: 12px;
    transition: width 1s ease-out;
    .gc-bar-text {
      font-size: 12px;
      font-weight: 600;
      color: #fff;
    }
  }
  .gc-bar-user {
    background: linear-gradient(90deg, #3b82f6, #60a5fa);
  }
  .gc-bar-req {
    background: linear-gradient(90deg, #8b5cf6, #a78bfa);
  }
}

// ======== 雷达图 ========
.gap-radar-section {
  margin-bottom: 20px;
  .radar-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  }
}

// ======== 操作 ========
.gap-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}

// ======== 空状态 ========
.gap-empty {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.empty-visual {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
  .empty-icon-bg {
    width: 100px; height: 100px;
    border-radius: 28px;
    background: linear-gradient(135deg, #fef2f2, #fef3c7, #ecfdf5);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 46px;
  }
  .empty-ripple {
    position: absolute;
    border-radius: 50%;
    border: 2px solid #e5e7eb;
    &.r1 {
      inset: -12px;
      animation: ripple 2.5s ease-out infinite;
    }
    &.r2 {
      inset: -24px;
      animation: ripple 2.5s ease-out 0.5s infinite;
    }
  }
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(1.5); opacity: 0; }
}

.gap-empty h2 {
  font-size: 22px; font-weight: 700; color: #1f2937; margin-bottom: 8px;
}
.gap-empty p {
  color: #6b7280; font-size: 14px; max-width: 400px; margin: 0 auto 28px;
}

.empty-features {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
  .ef-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: #f9fafb;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    font-size: 14px;
    color: #374151;
    .ef-icon { font-size: 18px; }
  }
}

@media (max-width: 768px) {
  .gap-hero-content { flex-direction: column; padding: 24px; }
  .gh-left { .gh-title { font-size: 22px; } }
  .gap-cards { grid-template-columns: 1fr; }
}
</style>
