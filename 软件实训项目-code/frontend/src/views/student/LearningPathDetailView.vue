<template>
  <div class="learning-detail-page">
    <!-- ======== 返回 + 标题栏 ======== -->
    <div class="ld-topbar">
      <el-button link @click="$router.push('/student/learning-path')">
        <el-icon><ArrowLeft /></el-icon> 返回路径列表
      </el-button>
    </div>

    <!-- ======== Hero ======== -->
    <div class="ld-hero">
      <div class="ldh-left">
        <div class="ldh-badge">
          <span class="badge-pulse"></span>AI 个性化规划
        </div>
        <h1 class="ldh-title">
          学习路径<span class="ldh-target">· {{ pathTitle }}</span>
        </h1>
        <p class="ldh-desc" v-if="pathMode === 'MERGED'">
          综合差距分析生成的合并学习路径，短板技能优先
        </p>
        <p class="ldh-desc" v-else>
          基于能力差距，AI 为你生成四阶段学习路径
        </p>
      </div>
      <div class="ldh-right">
        <el-dropdown trigger="click" @command="handleDropdown">
          <el-button type="primary" size="large" class="action-btn" :loading="loading">
            {{ allTasks.length ? '更新学习路径' : '生成学习路径' }} <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="update">
                <el-icon><Refresh /></el-icon> 更新学习路径（保留进度）
              </el-dropdown-item>
              <el-dropdown-item command="regenerate" divided>
                <el-icon><Delete /></el-icon> 重新生成（清空进度）
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- ======== 四阶段时间线 ======== -->
    <div v-if="allTasks.length" class="ld-timeline">
      <div
        v-for="(stage, idx) in stages"
        :key="stage"
        class="stage-block"
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
            :class="taskClass(task.status)"
          >
            <div class="task-check">
              <span v-if="task.status === 'TEST_PASSED'" class="check-mastered">🏆</span>
              <span v-else-if="task.status === 'LEARNING_COMPLETED'" class="check-done">✓</span>
              <span v-else-if="task.status === 'IN_PROGRESS'" class="check-active"></span>
              <span v-else class="check-not-started"></span>
            </div>
            <div class="task-content">
              <div class="task-title-row">
                <span class="task-title">{{ task.title }}</span>
                <el-tag v-if="task.status === 'TEST_PASSED'" size="small" type="success" class="task-status-tag">已掌握</el-tag>
                <el-tag v-else-if="isMastered(task.skillId)" size="small" type="warning" class="task-status-tag">已掌握</el-tag>
              </div>
              <div class="task-meta-row">
                <span class="task-due" v-if="task.dueDate">
                  <el-icon><Calendar /></el-icon> {{ task.dueDate }}
                </span>
                <span class="task-stage-label">{{ stageLabels[task.stage] || task.stage }}</span>
                <!-- V5 新增：技能来源岗位标签 -->
                <template v-if="skillSourceJobs[task.skillId]?.length">
                  <el-tag
                    v-for="job in skillSourceJobs[task.skillId].slice(0, 2)"
                    :key="job"
                    size="small"
                    effect="plain"
                    type="info"
                    class="source-job-tag"
                  >
                    📌 {{ job }}
                  </el-tag>
                  <el-tag v-if="skillSourceJobs[task.skillId].length > 2" size="small" effect="plain" type="info">
                    +{{ skillSourceJobs[task.skillId].length - 2 }}
                  </el-tag>
                </template>
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
              <!-- V5 新增：技能测试按钮 -->
              <el-button
                v-if="task.status === 'LEARNING_COMPLETED' || task.status === 'TEST_PASSED'"
                type="warning"
                size="small"
                link
                class="test-btn"
                @click="startTest(task)"
              >
                {{ task.status === 'TEST_PASSED' ? '重新测试' : '技能测试' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ======== 空状态 ======== -->
    <div v-else-if="!loading" class="ld-empty">
      <h2>该路径下暂无学习任务</h2>
      <p>点击"生成学习路径"按钮来创建你的学习计划</p>
    </div>

    <!-- ======== 测试弹窗 ======== -->
    <el-dialog v-model="showTestDialog" title="技能测试" width="600px" :close-on-click-modal="false">
      <div v-if="testQuestions.length > 0" class="test-body">
        <div class="test-header">
          <span>共 {{ testQuestions.length }} 题 · 每题 1 分</span>
        </div>
        <div
          v-for="(q, qi) in testQuestions"
          :key="qi"
          class="test-question"
        >
          <p class="tq-title">{{ qi + 1 }}. {{ q.question }}</p>
          <el-radio-group v-model="testAnswers[qi]" class="tq-options">
            <el-radio
              v-for="opt in q.options"
              :key="opt"
              :value="opt.charAt(0)"
              class="tq-option"
            >
              {{ opt }}
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <div v-else class="test-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>AI 正在生成测试题...</p>
      </div>

      <template #footer>
        <el-button @click="showTestDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingTest" @click="submitTest" :disabled="testQuestions.length === 0">
          提交测试
        </el-button>
      </template>
    </el-dialog>

    <!-- ======== 测试结果弹窗 ======== -->
    <el-dialog v-model="showResultDialog" title="测试结果" width="400px">
      <div class="result-body" v-if="testResult">
        <div class="result-score-ring">
          <svg viewBox="0 0 100 100" class="rsr-svg">
            <circle cx="50" cy="50" r="42" fill="none" stroke="#e5e7eb" stroke-width="8" />
            <circle cx="50" cy="50" r="42" fill="none"
              :stroke="testResult.passed ? '#10b981' : '#ef4444'"
              stroke-width="8" stroke-linecap="round"
              :stroke-dasharray="(testResult.score * 2.639) + ' ' + (263.9 - testResult.score * 2.639)"
              transform="rotate(-90 50 50)" />
          </svg>
          <span class="rsr-text">{{ testResult.score }}分</span>
        </div>
        <p class="result-msg">
          {{ testResult.passed ? '🎉 恭喜通过测试！' : '😔 未通过，继续加油！' }}
        </p>
        <p class="result-detail">
          答对 {{ testResult.correctCount }}/{{ testResult.totalCount }} 题
          <template v-if="testResult.passed">
            · 技能等级已升级为 <strong>{{ testResult.newLevel }}</strong>
          </template>
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showResultDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowDown, Refresh, Delete, Calendar, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getLearningTasks,
  getLearningPathMeta,
  getPathSkillsMatrix,
  updateTaskStatus,
  generateLearningPath,
  regenerateLearningPathMulti,
  startTaskTest,
  submitTaskTest,
  getMasteredSkills
} from '@/api/student'

const route = useRoute()
const router = useRouter()
const pathId = computed(() => Number(route.params.pathId))

const loading = ref(false)
const allTasks = ref<any[]>([])
const pathTitle = ref('')
const pathMode = ref('SINGLE')
const targetJobId = ref<number | null>(null)

// V5 新增：技能-岗位映射 + 已掌握技能
const skillSourceJobs = ref<Record<number, string[]>>({})
const masteredSkillIds = ref<Set<number>>(new Set())
const showTestDialog = ref(false)
const showResultDialog = ref(false)
const testQuestions = ref<any[]>([])
const testAnswers = ref<Record<number, string>>({})
const currentTestTaskId = ref<number>(0)
const submittingTest = ref(false)
const testResult = ref<any>(null)

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

const tasksByStage = computed(() => {
  const map: Record<string, any[]> = {}
  stages.forEach(s => map[s] = allTasks.value.filter(t => t.stage === s))
  return map
})

function stageProgress(stage: string): number {
  const tasks = tasksByStage.value[stage]
  if (!tasks || tasks.length === 0) return 0
  const done = tasks.filter(t => t.status === 'LEARNING_COMPLETED' || t.status === 'TEST_PASSED').length
  return Math.round((done / tasks.length) * 100)
}

function taskClass(status: string): string {
  const map: Record<string, string> = {
    'NOT_STARTED': '',
    'IN_PROGRESS': 'task-in-progress',
    'LEARNING_COMPLETED': 'task-learning-completed',
    'TEST_PASSED': 'task-test-passed'
  }
  return map[status] || ''
}

function isMastered(skillId: number): boolean {
  return masteredSkillIds.value.has(skillId)
}

// ======== 初始化 ========

onMounted(async () => {
  await loadAll()
})

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([
      loadMeta(),
      loadSkillsMatrix(),
      fetchTasks(),
      loadMasteredSkills()
    ])
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  try {
    const res: any = await getLearningPathMeta(pathId.value)
    const meta = res.data || {}
    pathTitle.value = meta.jobTitle || '学习路径'
    pathMode.value = meta.mode || 'SINGLE'
  } catch {
    pathTitle.value = '学习路径'
  }
}

async function loadSkillsMatrix() {
  try {
    const res: any = await getPathSkillsMatrix(pathId.value)
    const matrix = res.data || []
    const map: Record<number, string[]> = {}
    matrix.forEach((item: any) => {
      if (item.skillId && item.sourceJobs?.length) {
        map[item.skillId] = item.sourceJobs
      }
    })
    skillSourceJobs.value = map
  } catch {
    skillSourceJobs.value = {}
  }
}

async function loadMasteredSkills() {
  try {
    const res: any = await getMasteredSkills()
    const list = res.data || []
    masteredSkillIds.value = new Set(list.map((s: any) => s.skillId))
  } catch {
    masteredSkillIds.value = new Set()
  }
}

async function fetchTasks() {
  try {
    const res: any = await getLearningTasks(pathId.value)
    allTasks.value = res.data || []
  } catch {
    allTasks.value = []
  }
}

// ======== 操作 ========

async function handleDropdown(command: string) {
  if (command === 'update') {
    await handleUpdate()
  } else if (command === 'regenerate') {
    await handleRegenerate()
  }
}

async function handleUpdate() {
  loading.value = true
  try {
    await generateLearningPath(targetJobId.value || undefined)
    ElMessage.success('学习路径已更新（学习进度已保留）')
    await loadAll()
  } catch (e: any) {
    ElMessage.error('更新失败：' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

async function handleRegenerate() {
  try {
    await ElMessageBox.confirm(
      '重新生成将清空当前路径的所有学习进度，此操作不可撤销。是否继续？',
      '确认重新生成',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
    )
  } catch {
    return // 用户取消
  }

  loading.value = true
  try {
    await regenerateLearningPathMulti(
      targetJobId.value ? [targetJobId.value] : [],
      pathMode.value
    )
    ElMessage.success('学习路径已重新生成')
    await loadAll()
  } catch (e: any) {
    ElMessage.error('重新生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

async function updateTask(id: number, status: string) {
  try {
    await updateTaskStatus(id, status)
    const idx = allTasks.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      allTasks.value[idx] = { ...allTasks.value[idx], status }
    }
    // 更新已掌握技能列表
    if (status === 'LEARNING_COMPLETED') {
      await loadMasteredSkills()
    }
  } catch {
    ElMessage.error('更新任务状态失败')
  }
}

function openResource(url: string) {
  if (url) window.open(url, '_blank')
}

// ======== 测试相关 ========

async function startTest(task: any) {
  currentTestTaskId.value = task.id
  testQuestions.value = []
  testAnswers.value = {}
  showTestDialog.value = true

  try {
    const res: any = await startTaskTest(task.id)
    testQuestions.value = res.data || []
  } catch (e: any) {
    ElMessage.error('生成测试题失败：' + (e.response?.data?.message || e.message))
    showTestDialog.value = false
  }
}

async function submitTest() {
  if (testQuestions.value.length === 0) return

  submittingTest.value = true
  try {
    const res: any = await submitTaskTest(currentTestTaskId.value, testAnswers.value)
    testResult.value = res.data

    showTestDialog.value = false
    showResultDialog.value = true

    // 刷新任务列表
    await fetchTasks()
    await loadMasteredSkills()
  } catch (e: any) {
    ElMessage.error('提交测试失败：' + (e.response?.data?.message || e.message))
  } finally {
    submittingTest.value = false
  }
}
</script>

<style scoped lang="scss">
.learning-detail-page { max-width: 900px; margin: 0 auto; }

// ======== 顶栏 ========
.ld-topbar { margin-bottom: 16px; }

// ======== Hero ========
.ld-hero {
  display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 20px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfeff 50%, #fef3c7 100%);
  border-radius: 20px; padding: 32px 36px; margin-bottom: 24px; border: 1px solid #e5e7eb;
}
.ldh-badge {
  display: inline-flex; align-items: center; gap: 8px; padding: 6px 16px; border-radius: 20px;
  background: rgba(99, 102, 241, 0.12); color: #4f46e5; font-size: 13px; font-weight: 600; margin-bottom: 10px;
  .badge-pulse {
    width: 7px; height: 7px; border-radius: 50%; background: #6366f1;
    animation: gentlePulse 1.5s ease-in-out infinite;
  }
}
@keyframes gentlePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.ldh-title { font-size: 28px; font-weight: 800; color: #1f2937; margin: 0 0 6px;
  .ldh-target { font-weight: 500; color: #6366f1; font-size: 20px; }
}
.ldh-desc { color: #6b7280; font-size: 14px; max-width: 450px; }

.action-btn {
  height: 48px; padding: 0 28px; border-radius: 14px; font-size: 16px; font-weight: 600;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 28px rgba(99, 102, 241, 0.45); }
}

// ======== 阶段时间线 ========
.ld-timeline { display: flex; flex-direction: column; gap: 20px; }
.stage-block {
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: all 0.3s;
  &:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
}
.stage-header { display: flex; align-items: center; gap: 16px; padding: 20px 24px; border-bottom: 1px solid #f3f4f6; }
.stage-marker {
  width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center;
  justify-content: center; flex-shrink: 0;
  .stage-num { color: #fff; font-size: 18px; font-weight: 800; }
}
.stage-info { flex: 1;
  .stage-badge { display: inline-block; padding: 3px 14px; border-radius: 8px; font-size: 14px; font-weight: 700; margin-bottom: 4px; }
  .stage-meta { font-size: 12px; color: #9ca3af; }
}
.stage-progress { display: flex; align-items: center; gap: 8px;
  .sp-ring { width: 50px; height: 50px; }
  .sp-text { font-size: 15px; font-weight: 700; color: #374151; }
}

.stage-tasks { padding: 8px 24px 16px; }

.task-item {
  display: flex; align-items: center; gap: 14px; padding: 14px 16px;
  border-radius: 10px; transition: all 0.25s;
  &:hover { background: #f9fafb; }
  &.task-learning-completed { .task-title { text-decoration: line-through; color: #9ca3af; } }
  &.task-test-passed { background: #f0fdf4; border: 1px solid #bbf7d0; }
  &.task-in-progress { background: #fefce8; border: 1px solid #fde68a; }
}

.task-check { flex-shrink: 0;
  .check-mastered { font-size: 20px; }
  .check-done {
    width: 26px; height: 26px; border-radius: 50%; background: #10b981; color: #fff;
    display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700;
  }
  .check-active {
    width: 26px; height: 26px; border-radius: 50%; border: 2px solid #f59e0b; position: relative;
    &::after {
      content: ''; position: absolute; inset: 4px; border-radius: 50%; background: #f59e0b;
      animation: gentlePulse 1.2s ease-in-out infinite;
    }
  }
  .check-not-started { width: 26px; height: 26px; border-radius: 50%; border: 2px solid #d1d5db; }
}

.task-content { flex: 1; min-width: 0;
  .task-title-row { display: flex; align-items: center; gap: 8px;
    .task-title { font-size: 14px; color: #374151; font-weight: 500; }
  }
  .task-meta-row { display: flex; align-items: center; gap: 8px; margin-top: 3px; flex-wrap: wrap; }
  .task-due { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #9ca3af; }
  .task-stage-label { font-size: 11px; color: #9ca3af; background: #f3f4f6; padding: 1px 8px; border-radius: 6px; }
  .source-job-tag { font-size: 11px; }
  .task-status-tag { font-size: 11px; }
}

.task-link { flex-shrink: 0; }

.task-action { display: flex; align-items: center; gap: 6px;
  .task-select { width: 110px; }
  .test-btn { font-size: 12px; white-space: nowrap; }
}

// ======== 空状态 ========
.ld-empty {
  text-align: center; padding: 80px 20px; background: #fff;
  border-radius: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  h2 { font-size: 20px; font-weight: 700; color: #1f2937; }
  p { color: #6b7280; font-size: 14px; }
}

// ======== 测试弹窗 ========
.test-header { margin-bottom: 16px; font-size: 14px; color: #6b7280; }
.test-question { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f3f4f6; }
.tq-title { font-size: 14px; font-weight: 600; color: #1f2937; margin: 0 0 10px; }
.tq-options { display: flex; flex-direction: column; gap: 6px; }
.tq-option { margin-right: 0 !important; }

.test-loading { text-align: center; padding: 40px; color: #6b7280; }

// ======== 结果弹窗 ========
.result-body { text-align: center; padding: 20px; }
.result-score-ring { position: relative; width: 100px; height: 100px; margin: 0 auto 16px; }
.rsr-svg { width: 100px; height: 100px; }
.rsr-text { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 800; color: #1f2937; }
.result-msg { font-size: 18px; font-weight: 600; color: #1f2937; margin-bottom: 8px; }
.result-detail { font-size: 14px; color: #6b7280; }

@media (max-width: 768px) {
  .ld-hero { padding: 24px 20px; }
  .ldh-title { font-size: 22px; }
}
</style>
