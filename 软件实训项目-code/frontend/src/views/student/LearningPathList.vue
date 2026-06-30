<template>
  <div class="path-list-page">
    <!-- ======== 顶部 ======== -->
    <div class="pl-hero">
      <div class="plh-left">
        <div class="plh-badge"><span class="badge-pulse"></span>AI 个性化规划</div>
        <h1 class="plh-title">我的学习路径</h1>
        <p class="plh-desc">基于能力差距分析，AI 为你生成专属学习计划</p>
      </div>
      <div class="plh-right">
        <el-button type="primary" size="large" class="create-btn" @click="showCreateDialog = true">
          + 生成新路径
        </el-button>
      </div>
    </div>

    <!-- ======== 已掌握技能区域 ======== -->
    <div v-if="masteredSkills.length > 0" class="mastered-section">
      <div class="section-header">
        <h3>🏆 已掌握的技能</h3>
        <span class="mastered-count">{{ masteredSkills.length }} 项</span>
      </div>
      <div class="mastered-tags">
        <el-tag
          v-for="skill in masteredSkills"
          :key="skill.id"
          :type="masteryTagType(skill.level)"
          effect="plain"
          class="mastered-tag"
          @click="handleReview(skill.skillId)"
        >
          {{ skill.skillName }}
          <span class="tag-level">· {{ masteryLevelLabel(skill.level) }}</span>
        </el-tag>
      </div>
    </div>

    <!-- ======== 路径卡片列表 ======== -->
    <div v-if="pathStats.length > 0" class="path-cards">
      <div
        v-for="stat in pathStats"
        :key="stat.pathId"
        class="path-card"
        @click="goToDetail(stat.pathId)"
      >
        <div class="pc-top">
          <div class="pc-info">
            <span class="pc-icon">{{ stat.mode === 'MERGED' ? '📊' : '🎯' }}</span>
            <div class="pc-title-group">
              <h3 class="pc-title">{{ stat.jobTitle || '未命名路径' }}</h3>
              <span class="pc-mode">{{ stat.mode === 'MERGED' ? '综合路径' : '专属路径' }}</span>
            </div>
          </div>
          <div class="pc-progress-circle">
            <svg viewBox="0 0 60 60" class="pc-ring">
              <circle cx="30" cy="30" r="26" fill="none" stroke="#e5e7eb" stroke-width="5" />
              <circle cx="30" cy="30" r="26" fill="none" stroke="#6366f1"
                stroke-width="5" stroke-linecap="round"
                :stroke-dasharray="(stat.overallProgress * 1.634) + ' ' + (163.4 - stat.overallProgress * 1.634)"
                transform="rotate(-90 30 30)" />
            </svg>
            <span class="pc-progress-text">{{ stat.overallProgress }}%</span>
          </div>
        </div>

        <div class="pc-stages">
          <div
            v-for="(pct, stage) in stat.stageProgress"
            :key="stage"
            class="pc-stage-bar"
          >
            <span class="pcs-label">{{ stageLabel(stage as string) }}</span>
            <div class="pcs-track">
              <div class="pcs-fill" :style="{ width: pct + '%', background: stageColor(stage as string) }"></div>
            </div>
            <span class="pcs-pct">{{ pct }}%</span>
          </div>
        </div>

        <div class="pc-footer">
          <span>{{ stat.completedTasks }}/{{ stat.totalTasks }} 任务完成</span>
          <el-button type="primary" link size="small">
            进入学习 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- ======== 空状态 ======== -->
    <div v-else-if="!loading" class="pl-empty">
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
      <p>完成能力测评后，AI 将为你生成专属的四阶段学习路径<br />从基础入门到面试冲刺，助你系统化成长</p>
      <div class="empty-actions">
        <el-button type="primary" size="large" @click="showCreateDialog = true">
          🤖 生成学习路径
        </el-button>
        <el-button size="large" @click="$router.push('/student/gap-analysis')">
          📊 差距分析
        </el-button>
        <el-button size="large" @click="$router.push('/student/assessment')">
          📝 能力测评
        </el-button>
      </div>
    </div>

    <!-- ======== 生成新路径弹窗 ======== -->
    <el-dialog v-model="showCreateDialog" title="生成学习路径" width="580px" destroy-on-close>
      <div class="dialog-body">
        <div class="dialog-section">
          <label class="dialog-label">选择目标岗位（可多选）</label>
          <template v-if="availableJobs.length > 0">
            <el-select
              v-model="selectedJobIds"
              multiple
              filterable
              placeholder="从收藏的岗位中选择..."
              style="width: 100%"
              :loading="jobsLoading"
            >
              <el-option
                v-for="job in availableJobs"
                :key="job.id"
                :label="job.title"
                :value="job.id"
              />
            </el-select>
          </template>
          <div v-else class="no-fav-hint">
            <el-icon><Folder /></el-icon>
            <span>暂无收藏的岗位，请先收藏感兴趣的岗位</span>
          </div>
        </div>

        <div class="dialog-section">
          <label class="dialog-label">生成模式</label>
          <el-radio-group v-model="generateMode" class="mode-group">
            <el-radio-button value="MERGED">
              <div class="mode-option">
                <span class="mo-icon">📊</span>
                <div class="mo-text">
                  <strong>合并综合路径</strong>
                  <small>多岗位技能去重合并，一条路径覆盖所有</small>
                </div>
              </div>
            </el-radio-button>
            <el-radio-button value="SEPARATE">
              <div class="mode-option">
                <span class="mo-icon">🎯</span>
                <div class="mo-text">
                  <strong>各岗位独立路径</strong>
                  <small>每个岗位一条独立路径，分别管理</small>
                </div>
              </div>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate" :disabled="selectedJobIds.length === 0">
          开始生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowRight, Folder } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getPathListWithStats,
  generateLearningPath,
  generateLearningPathMulti,
  getMasteredSkills,
  recordSkillReview,
  getJobDetail,
  getFavorites
} from '@/api/student'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const generating = ref(false)
const pathStats = ref<any[]>([])
const masteredSkills = ref<any[]>([])
const showCreateDialog = ref(false)
const selectedJobIds = ref<number[]>([])
const generateMode = ref('MERGED')
const availableJobs = ref<any[]>([])
const jobsLoading = ref(false)

const stageLabel = (stage: string) =>
  ({ BASIC: '基础', FRAMEWORK: '框架', PROJECT: '项目', INTERVIEW: '面试' } as any)[stage] || stage

const stageColor = (stage: string) =>
  ({ BASIC: '#3b82f6', FRAMEWORK: '#8b5cf6', PROJECT: '#f59e0b', INTERVIEW: '#10b981' } as any)[stage] || '#6b7280'

const masteryTagType = (level: string): any =>
  ({ BASIC: '', INTERMEDIATE: 'success', ADVANCED: 'warning', EXPERT: 'danger' } as any)[level] || ''

const masteryLevelLabel = (level: string) =>
  ({ BASIC: '了解', INTERMEDIATE: '掌握', ADVANCED: '熟练', EXPERT: '精通' } as any)[level] || level

function goToDetail(pathId: number) {
  router.push(`/student/learning-path/detail/${pathId}`)
}

// ======== 数据加载 ========

async function loadData() {
  loading.value = true
  try {
    const [statsRes, masteryRes] = await Promise.all([
      getPathListWithStats(),
      getMasteredSkills()
    ])
    pathStats.value = (statsRes as any).data || []
    masteredSkills.value = (masteryRes as any).data || []
  } catch {
    // 降级
    pathStats.value = []
    masteredSkills.value = []
  }
  loading.value = false
}

async function loadFavoriteJobs() {
  jobsLoading.value = true
  try {
    const favRes: any = await getFavorites()
    const jobIds: number[] = favRes.data || []
    if (jobIds.length > 0) {
      const detailPromises = jobIds.map(id => getJobDetail(id))
      const details = await Promise.all(detailPromises)
      availableJobs.value = details.map((r: any) => r.data).filter(Boolean)
    } else {
      availableJobs.value = []
    }
  } catch {
    availableJobs.value = []
  }
  jobsLoading.value = false
}

// ======== 操作 ========

async function handleGenerate() {
  if (selectedJobIds.value.length === 0) {
    ElMessage.warning('请至少选择一个岗位')
    return
  }

  generating.value = true
  try {
    const mode = selectedJobIds.value.length === 1 ? 'SEPARATE' : generateMode.value
    const res: any = await generateLearningPathMulti(selectedJobIds.value, mode)
    ElMessage.success('学习路径生成成功！')
    showCreateDialog.value = false
    await loadData()

    // 自动跳转到第一条路径的详情页
    if (pathStats.value.length > 0) {
      goToDetail(pathStats.value[0].pathId)
    }
  } catch (e: any) {
    ElMessage.error('生成失败：' + (e.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

async function handleReview(skillId: number) {
  try {
    await recordSkillReview(skillId)
    ElMessage.success('已记录复习')
    // 打开复习相关资源
    window.open(`https://search.bilibili.com/all?keyword=${encodeURIComponent(skillId.toString())}`, '_blank')
  } catch {
    // silent
  }
}

// ======== 初始化 ========

onMounted(async () => {
  await Promise.all([loadData(), loadFavoriteJobs()])

  // 处理从差距分析跳转过来的自动生成
  const jobIdParam = route.query.jobId
  const jobIdsParam = route.query.jobIds as string | undefined
  const modeParam = route.query.mode as string | undefined

  if (jobIdsParam) {
    const ids = jobIdsParam.split(',').map(Number).filter(Boolean)
    if (ids.length > 0) {
      selectedJobIds.value = ids
      generateMode.value = modeParam === 'separate' ? 'SEPARATE' : 'MERGED'
      showCreateDialog.value = true
    }
  } else if (jobIdParam) {
    const jobId = Number(jobIdParam)
    selectedJobIds.value = [jobId]
    generateMode.value = 'SEPARATE'
    showCreateDialog.value = true
  }
})
</script>

<style scoped lang="scss">
.path-list-page {
  max-width: 900px;
  margin: 0 auto;
}

// ======== Hero ========
.pl-hero {
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

.plh-badge {
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

.plh-title { font-size: 28px; font-weight: 800; color: #1f2937; margin: 0 0 6px; }
.plh-desc { color: #6b7280; font-size: 14px; }

.create-btn {
  height: 48px; padding: 0 28px; border-radius: 14px; font-size: 16px; font-weight: 600;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 28px rgba(99, 102, 241, 0.45); }
}

// ======== 已掌握技能 ========
.mastered-section {
  background: #fff; border-radius: 16px; padding: 20px 24px;
  margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;
  h3 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0; }
}
.mastered-count { font-size: 13px; color: #9ca3af; }
.mastered-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.mastered-tag {
  cursor: pointer; font-size: 13px; padding: 4px 12px;
  &:hover { transform: scale(1.05); }
  .tag-level { opacity: 0.7; font-size: 11px; }
}

// ======== 路径卡片 ========
.path-cards { display: flex; flex-direction: column; gap: 16px; }

.path-card {
  background: #fff; border-radius: 16px; padding: 24px; cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04); border: 2px solid transparent;
  transition: all 0.25s;
  &:hover { border-color: #a5b4fc; box-shadow: 0 4px 20px rgba(0,0,0,0.08); transform: translateY(-2px); }
}

.pc-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.pc-info { display: flex; align-items: center; gap: 14px; }
.pc-icon { font-size: 28px; }
.pc-title { font-size: 18px; font-weight: 700; color: #1f2937; margin: 0; }
.pc-mode { font-size: 12px; color: #9ca3af; }

.pc-progress-circle { position: relative; width: 60px; height: 60px; flex-shrink: 0; }
.pc-ring { width: 60px; height: 60px; }
.pc-progress-text {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700; color: #4f46e5;
}

.pc-stages { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; margin-bottom: 14px; }
.pc-stage-bar { display: flex; align-items: center; gap: 8px; }
.pcs-label { font-size: 12px; color: #6b7280; width: 36px; }
.pcs-track { flex: 1; height: 6px; background: #f3f4f6; border-radius: 3px; overflow: hidden; }
.pcs-fill { height: 100%; border-radius: 3px; transition: width 0.5s; }
.pcs-pct { font-size: 12px; color: #9ca3af; width: 32px; text-align: right; }

.pc-footer { display: flex; align-items: center; justify-content: space-between; font-size: 13px; color: #6b7280; padding-top: 12px; border-top: 1px solid #f3f4f6; }

// ======== 空状态 ========
.pl-empty {
  text-align: center; padding: 80px 20px; background: #fff;
  border-radius: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.empty-art { margin-bottom: 24px;
  .art-stages { display: flex; align-items: center; justify-content: center; gap: 0; }
  .art-stage {
    width: 64px; height: 64px; border-radius: 18px; display: flex;
    align-items: center; justify-content: center; font-size: 28px;
    &.s1 { background: #dbeafe; } &.s2 { background: #ede9fe; }
    &.s3 { background: #fef3c7; } &.s4 { background: #d1fae5; }
  }
  .art-line { width: 40px; height: 3px; background: linear-gradient(90deg, #d1d5db, #e5e7eb); border-radius: 2px; }
}
.pl-empty h2 { font-size: 22px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.pl-empty p { color: #6b7280; font-size: 14px; line-height: 1.7; margin-bottom: 28px; }
.empty-actions { display: flex; gap: 12px; justify-content: center; }

// ======== 对话框 ========
.dialog-section { margin-bottom: 20px; }
.dialog-label { display: block; font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 8px; }

.mode-group { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.mode-option { display: flex; align-items: center; gap: 12px; padding: 4px 0; text-align: left; }
.mo-icon { font-size: 22px; flex-shrink: 0; }
.mo-text strong { display: block; font-size: 14px; }
.mo-text small { display: block; font-size: 12px; color: #9ca3af; margin-top: 2px; }

.no-fav-hint {
  display: flex; align-items: center; gap: 8px;
  padding: 20px; background: #f9fafb; border-radius: 8px; border: 1px dashed #d1d5db;
  color: #9ca3af; font-size: 14px; justify-content: center;
}

@media (max-width: 768px) {
  .pl-hero { padding: 24px 20px; }
  .plh-title { font-size: 22px; }
  .pc-stages { grid-template-columns: 1fr; }
}
</style>
