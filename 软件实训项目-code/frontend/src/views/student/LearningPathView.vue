<template>
  <div class="learning-page">
    <!-- ======== Hero + 路径管理 ======== -->
    <div class="lp-hero">
      <div class="lph-left">
        <div class="lph-badge">
          <span class="badge-pulse"></span>AI 个性化规划
        </div>
        <h1 class="lph-title">
          学习路径
          <template v-if="paths.length > 1">
            <span class="lph-target">· {{ currentPathTitle }}</span>
          </template>
          <template v-else-if="targetJobName">
            <span class="lph-target">· {{ targetJobName }}</span>
          </template>
        </h1>
        <p class="lph-desc" v-if="isMergedMode">
          综合差距分析生成的合并学习路径，短板技能优先
        </p>
        <p class="lph-desc" v-else-if="paths.length > 1">
          已为 {{ paths.length }} 个岗位生成独立学习路径，可分别管理
        </p>
        <p class="lph-desc" v-else>
          基于能力差距，AI为你生成四阶段学习路径
        </p>
      </div>
      <div class="lph-right">
        <el-button type="primary" size="large" class="generate-btn" @click="handleGenerate" :loading="loading">
          <el-icon><Refresh /></el-icon> {{ allTasks.length ? '更新学习路径' : '生成学习路径' }}
        </el-button>
      </div>
    </div>

    <!-- ======== 多路径标签页（SEPARATE 模式） ======== -->
    <div v-if="paths.length > 1" class="path-tabs">
      <div
        v-for="p in paths"
        :key="p.id"
        class="path-tab"
        :class="{ active: activePathId === p.id }"
        @click="switchPath(p.id)"
      >
        <span class="pt-icon">{{ p.targetJobId ? '🎯' : '📊' }}</span>
        <span class="pt-title">{{ getPathTitle(p) }}</span>
        <span class="pt-tasks">{{ getPathTaskCount(p.id) }} 任务</span>
      </div>
    </div>

    <!-- ======== 四阶段时间线 ======== -->
    <div v-if="allTasks.length" class="lp-timeline">
      <div
        v-for="(stage, idx) in stages"
        :key="stage"
        class="stage-block"
        :class="'stage-' + stage.toLowerCase()"
      >
        <!-- 阶段头部 -->
        <div class="stage-header">
          <div class="stage-marker" :style="{ background: stageColors[stage] }">
            <span class="stage-num">{{ idx + 1 }}</span>
          </div>
          <div class="stage-info">
            <div class="stage-badge" :style="{ background: stageColors[stage] + '20', color: stageColors[stage] }">
              {{ stageIcons[stage] }} {{ stageLabels[stage] }}
            </div>
            <div class="stage-meta">
              {{ stageDescs[stage] }} · {{ tasksByStage[stage]?.length || 0 }} 个任务
            </div>
          </div>
          <div class="stage-progress">
            <svg viewBox="0 0 50 50" class="sp-ring">
              <circle cx="25" cy="25" r="20" fill="none" stroke="#e5e7eb" stroke-width="4" />
              <circle cx="25" cy="25" r="20" fill="none" :stroke="stageColors[stage]"
                stroke-width="4" stroke-linecap="round"
                :stroke-dasharray="(stageProgress(stage) * 1.256) + ' ' + (125.6 - stageProgress(stage) * 1.256)"
                transform="rotate(-90 25 25)" />
            </svg>
            <span class="sp-text">{{ stageProgress(stage) }}%</span>
          </div>
        </div>

        <!-- 任务列表 -->
        <div class="stage-tasks" v-if="tasksByStage[stage]?.length">
          <div
            v-for="task in tasksByStage[stage]"
            :key="task.id"
            class="task-item"
            :class="'task-' + task.status.toLowerCase().replace(/_/g, '-')"
          >
            <div class="task-check">
              <span v-if="task.status === 'LEARNING_COMPLETED'" class="check-done">✓</span>
              <span v-else-if="task.status === 'IN_PROGRESS'" class="check-active"></span>
              <span v-else class="check-not-started"></span>
            </div>
            <div class="task-content">
              <div class="task-title-row">
                <span class="task-title">{{ task.title }}</span>
              </div>
              <div class="task-meta-row">
                <span class="task-due" v-if="task.dueDate">
                  <el-icon><Calendar /></el-icon> {{ task.dueDate }}
                </span>
                <span class="task-stage-label">{{ stageLabels[task.stage] || task.stage }}</span>
              </div>
            </div>
            <div class="task-link" v-if="task.resourceUrl">
              <el-button type="primary" link size="small" @click="openResource(task.resourceUrl)">
                🔗 跳转学习
              </el-button>
            </div>
            <div class="task-action">
              <el-select
                :model-value="task.status"
                @change="(v: string) => updateTask(task.id, v)"
                size="small"
                class="task-select"
              >
                <el-option label="未开始" value="NOT_STARTED" />
                <el-option label="学习中" value="IN_PROGRESS" />
                <el-option label="已完成" value="LEARNING_COMPLETED" />
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ======== 空状态 ======== -->
    <div v-else class="lp-empty">
      <div class="empty-art">
        <div class="art-stages">
          <div class="art-stage s1">📖</div>
          <div class="art-line"></div>
          <div class="art-stage s2">🔧</div>
          <div class="art-line"></div>
          <div class="art-stage s3">🚀</div>
          <div class="art-line"></div>
          <div class="art-stage s4">🎯</div>
        </div>
      </div>
      <h2>还没有学习路径</h2>
      <p>完成能力测评或差距分析后，AI将为你生成专属的四阶段学习路径<br/>从基础入门到面试冲刺，助你系统化成长</p>
      <div class="empty-actions">
        <el-button type="primary" size="large" @click="handleGenerate" :loading="loading">
          🤖 立即生成
        </el-button>
        <el-button size="large" @click="$router.push('/student/gap-analysis')">
          📊 差距分析
        </el-button>
        <el-button size="large" @click="$router.push('/student/assessment')">
          📝 先去测评
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Refresh, Calendar } from '@element-plus/icons-vue'
import {
  generateLearningPath,
  generateLearningPathMulti,
  getLearningTasks,
  getLearningPaths,
  updateTaskStatus,
  getJobDetail
} from '@/api/student'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const allTasks = ref<any[]>([])
const targetJobName = ref('')
const targetJobId = ref<number | null>(null)

// 多路径支持
const paths = ref<any[]>([])
const activePathId = ref<number>(0)
const pathJobNames = ref<Record<number, string>>({})

const stages = ['BASIC', 'FRAMEWORK', 'PROJECT', 'INTERVIEW']
const stageLabels: Record<string, string> = {
  BASIC: '基础入门', FRAMEWORK: '框架进阶', PROJECT: '项目实战', INTERVIEW: '面试冲刺'
}
const stageIcons: Record<string, string> = {
  BASIC: '📖', FRAMEWORK: '🔧', PROJECT: '🚀', INTERVIEW: '🎯'
}
const stageDescs: Record<string, string> = {
  BASIC: '夯实基础', FRAMEWORK: '掌握框架', PROJECT: '积累经验', INTERVIEW: '冲刺Offer'
}
const stageColors: Record<string, string> = {
  BASIC: '#3b82f6', FRAMEWORK: '#8b5cf6', PROJECT: '#f59e0b', INTERVIEW: '#10b981'
}

// ---- 计算属性 ----
const isMergedMode = computed(() => {
  const activePath = paths.value.find(p => p.id === activePathId.value)
  return activePath && activePath.targetJobId === null
})

/** 当前活跃路径的岗位名称 */
const currentPathTitle = computed(() => {
  const active = paths.value.find(p => p.id === activePathId.value)
  if (!active) return ''
  return pathJobNames.value[active.id] || getPathTitle(active)
})

const tasksByStage = computed(() => {
  const map: Record<string, any[]> = {}
  stages.forEach(s => map[s] = allTasks.value.filter(t => t.stage === s))
  return map
})

function stageProgress(stage: string): number {
  const tasks = tasksByStage.value[stage]
  if (!tasks || tasks.length === 0) return 0
  const done = tasks.filter(t => t.status === 'LEARNING_COMPLETED').length
  return Math.round((done / tasks.length) * 100)
}

function getPathTitle(path: any): string {
  const name = pathJobNames.value[path.id]
  if (name) return name
  if (!path.targetJobId) return '综合学习路径'
  return `岗位 #${path.targetJobId}`
}

function getPathTaskCount(pathId: number): number {
  return allTasks.value.filter(t => t.pathId === pathId).length
}

// ---- 初始化 ----
onMounted(async () => {
  await loadPaths()

  // 解析路由参数
  const jobIdParam = route.query.jobId
  const jobIdsParam = route.query.jobIds as string | undefined
  const modeParam = route.query.mode as string | undefined

  if (jobIdsParam) {
    // 多岗位模式：自动生成
    const ids = jobIdsParam.split(',').map(Number).filter(Boolean)
    const mode = modeParam === 'separate' ? 'SEPARATE' : 'MERGED'
    if (ids.length > 0 && allTasks.value.length === 0) {
      await autoGenerateMulti(ids, mode)
    }
  } else if (jobIdParam) {
    targetJobId.value = Number(jobIdParam)
    try {
      const res: any = await getJobDetail(Number(jobIdParam))
      targetJobName.value = res.data?.title || ''
    } catch { targetJobName.value = '' }
    // 如果没有已有任务，自动生成
    if (allTasks.value.length === 0) {
      await autoGenerateSingle(Number(jobIdParam))
    }
  }
})

// 监听路由变化
watch(() => route.query, async (newQuery) => {
  const ids = newQuery.jobIds as string | undefined
  if (ids) {
    const idArr = ids.split(',').map(Number).filter(Boolean)
    const mode = (newQuery.mode as string) === 'separate' ? 'SEPARATE' : 'MERGED'
    if (idArr.length > 0 && allTasks.value.length === 0) {
      await autoGenerateMulti(idArr, mode)
    }
  }
})

async function loadPaths() {
  try {
    const res: any = await getLearningPaths()
    const list = res.data || []
    paths.value = list
    if (list.length > 0) {
      activePathId.value = list[0].id
      await loadPathNames(list)
      await fetchTasks()
    }
  } catch {
    // 降级：用旧接口
    try {
      const res: any = await getLearningTasks()
      allTasks.value = res.data || []
    } catch { /* silent */ }
  }
}

async function loadPathNames(pathList: any[]) {
  for (const p of pathList) {
    if (p.targetJobId) {
      try {
        const res: any = await getJobDetail(p.targetJobId)
        pathJobNames.value[p.id] = res.data?.title || `岗位 #${p.targetJobId}`
      } catch {
        pathJobNames.value[p.id] = `岗位 #${p.targetJobId}`
      }
    } else {
      pathJobNames.value[p.id] = '综合学习路径'
    }
  }
}

async function autoGenerateMulti(jobIds: number[], mode: string) {
  loading.value = true
  try {
    const titleRes = await Promise.all(
      jobIds.slice(0, 3).map(id => getJobDetail(id).catch(() => null))
    )
    targetJobName.value = titleRes
      .filter(Boolean)
      .map((r: any) => r?.data?.title || '')
      .filter(Boolean)
      .join(' + ') || '多岗位'

    await generateLearningPathMulti(jobIds, mode)
    await loadPaths()
  } catch (e) {
    console.error('自动生成多岗位路径失败:', e)
  } finally {
    loading.value = false
  }
}

async function autoGenerateSingle(jobId: number) {
  loading.value = true
  try {
    await generateLearningPath(jobId)
    await loadPaths()
  } catch (e) {
    console.error('自动生成路径失败:', e)
  } finally {
    loading.value = false
  }
}

// ---- 方法 ----
async function handleGenerate() {
  loading.value = true
  try {
    if (paths.value.length > 1) {
      // 多路径模式：重新生成所有
      const jobIds = paths.value
        .filter((p: any) => p.targetJobId)
        .map((p: any) => p.targetJobId)
      if (jobIds.length > 0) {
        const mode = paths.value.some((p: any) => !p.targetJobId) ? 'MERGED' : 'SEPARATE'
        await generateLearningPathMulti(jobIds, mode)
      }
    } else if (targetJobId.value) {
      await generateLearningPath(targetJobId.value)
    } else {
      // 无明确岗位 → 跳转去路径列表页让用户选择
      router.push('/student/learning-paths')
      return
    }
    await loadPaths()
  } catch (e) {
    console.error('生成路径失败:', e)
  } finally {
    loading.value = false
  }
}

async function switchPath(pathId: number) {
  activePathId.value = pathId
  // 后端按 pathId 过滤，前端不再手动过滤
  try {
    const res: any = await getLearningTasks(pathId)
    allTasks.value = res.data || []
  } catch {
    allTasks.value = []
  }
}

async function fetchTasks() {
  try {
    if (activePathId.value > 0 && paths.value.length > 1) {
      const res: any = await getLearningTasks(activePathId.value)
      allTasks.value = res.data || []
    } else {
      const res: any = await getLearningTasks()
      allTasks.value = res.data || []
    }
  } catch {
    allTasks.value = []
  }
}

async function updateTask(id: number, status: string) {
  try {
    await updateTaskStatus(id, status)
    // 同步更新本地 allTasks 中的对应任务状态
    const idx = allTasks.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      allTasks.value[idx] = { ...allTasks.value[idx], status }
    }
  } catch {
    console.error('更新任务状态失败')
  }
}

function openResource(url: string) {
  if (url) window.open(url, '_blank')
}
</script>

<style scoped lang="scss">
// ======== Hero ========
.lp-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfeff 50%, #fef3c7 100%);
  border-radius: 20px;
  padding: 32px 36px;
  margin-bottom: 24px;
  border: 1px solid #e5e7eb;
}

.lph-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(99, 102, 241, 0.12);
  color: #4f46e5;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
  .badge-pulse {
    width: 7px; height: 7px;
    border-radius: 50%;
    background: #6366f1;
    animation: gentlePulse 1.5s ease-in-out infinite;
  }
}

@keyframes gentlePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.lph-title {
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
  margin: 0 0 6px;
  .lph-target {
    font-weight: 500;
    color: #6366f1;
    font-size: 20px;
  }
}

.lph-desc {
  color: #6b7280;
  font-size: 14px;
  max-width: 450px;
}

.generate-btn {
  height: 48px;
  padding: 0 28px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 28px rgba(99, 102, 241, 0.45); }
}

// ======== 路径标签页 ========
.path-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.path-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;

  &:hover {
    border-color: #a5b4fc;
    color: #4f46e5;
  }

  &.active {
    border-color: #6366f1;
    background: #eef2ff;
    color: #4f46e5;
    font-weight: 600;
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.08);
  }

  .pt-icon { font-size: 16px; }
  .pt-title { white-space: nowrap; }
  .pt-tasks {
    font-size: 11px;
    color: #9ca3af;
    background: #f3f4f6;
    padding: 2px 8px;
    border-radius: 8px;
  }
}

// ======== 阶段时间线 ========
.lp-timeline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stage-block {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.3s;
  &:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
}

.stage-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.stage-marker {
  width: 44px; height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  .stage-num {
    color: #fff;
    font-size: 18px;
    font-weight: 800;
  }
}

.stage-info {
  flex: 1;
  .stage-badge {
    display: inline-block;
    padding: 3px 14px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 700;
    margin-bottom: 4px;
  }
  .stage-meta {
    font-size: 12px;
    color: #9ca3af;
  }
}

.stage-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  .sp-ring { width: 50px; height: 50px; }
  .sp-text { font-size: 15px; font-weight: 700; color: #374151; }
}

// 任务列表
.stage-tasks {
  padding: 8px 24px 16px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  transition: all 0.25s;
  &:hover { background: #f9fafb; }
  &.task-learning-completed {
    .task-title { text-decoration: line-through; color: #9ca3af; }
  }
  &.task-in-progress {
    background: #fefce8;
    border: 1px solid #fde68a;
  }
}

.task-check {
  flex-shrink: 0;
  .check-done {
    width: 26px; height: 26px;
    border-radius: 50%;
    background: #10b981;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
  }
  .check-active {
    width: 26px; height: 26px;
    border-radius: 50%;
    border: 2px solid #f59e0b;
    position: relative;
    &::after {
      content: '';
      position: absolute; inset: 4px; border-radius: 50%;
      background: #f59e0b;
      animation: gentlePulse 1.2s ease-in-out infinite;
    }
  }
  .check-not-started {
    width: 26px; height: 26px;
    border-radius: 50%;
    border: 2px solid #d1d5db;
  }
}

.task-content {
  flex: 1;
  min-width: 0;
  .task-title-row {
    .task-title { font-size: 14px; color: #374151; font-weight: 500; }
  }
  .task-meta-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 3px;
  }
  .task-due {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #9ca3af;
  }
  .task-stage-label {
    font-size: 11px;
    color: #9ca3af;
    background: #f3f4f6;
    padding: 1px 8px;
    border-radius: 6px;
  }
}

.task-link {
  flex-shrink: 0;
}

.task-action {
  .task-select { width: 110px; }
}

// ======== 空状态 ========
.lp-empty {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.empty-art {
  margin-bottom: 24px;
  .art-stages {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
  }
  .art-stage {
    width: 64px; height: 64px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    &.s1 { background: #dbeafe; }
    &.s2 { background: #ede9fe; }
    &.s3 { background: #fef3c7; }
    &.s4 { background: #d1fae5; }
  }
  .art-line {
    width: 40px; height: 3px;
    background: linear-gradient(90deg, #d1d5db, #e5e7eb);
    border-radius: 2px;
  }
}

.lp-empty h2 {
  font-size: 22px; font-weight: 700; color: #1f2937; margin-bottom: 8px;
}
.lp-empty p {
  color: #6b7280; font-size: 14px; line-height: 1.7; margin-bottom: 28px;
}

.empty-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

@media (max-width: 768px) {
  .lp-hero { padding: 24px 20px; }
  .lph-title { font-size: 22px; }
  .path-tabs { flex-direction: column; }
}
</style>
