<template>
  <div class="dashboard">
    <!-- 顶部欢迎横幅 -->
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-left">
          <div class="company-avatar">
            <el-icon :size="32"><OfficeBuilding /></el-icon>
          </div>
          <div class="hero-text">
            <h1>欢迎回来，{{ userStore.realName || userStore.username || 'HR' }}</h1>
            <p>高效管理招聘流程，发现更多优秀人才</p>
          </div>
        </div>
        <div class="hero-right">
          <el-button type="primary" size="large" round @click="$router.push('/enterprise/jobs')">
            <el-icon style="margin-right:6px"><Plus /></el-icon>发布新岗位
          </el-button>
        </div>
      </div>
      <div class="hero-decor">
        <div class="decor-circle c1"></div>
        <div class="decor-circle c2"></div>
        <div class="decor-dots"></div>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card theme-blue" @click="$router.push('/enterprise/jobs')">
        <div class="stat-top">
          <div class="stat-icon">
            <el-icon :size="20"><Briefcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeJobs }}</div>
            <div class="stat-label">在招岗位</div>
          </div>
        </div>
        <div class="stat-footer">
          <span>共 {{ stats.totalJobs }} 个岗位，{{ stats.offlineJobs }} 个已下架</span>
          <el-icon :size="14" class="stat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="stat-indicator blue"></div>
      </div>

      <div class="stat-card theme-orange" @click="$router.push('/enterprise/candidates')">
        <div class="stat-top">
          <div class="stat-icon">
            <el-icon :size="20"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCandidates }}</div>
            <div class="stat-label">待筛选简历</div>
          </div>
        </div>
        <div class="stat-footer">
          <span>总投递 {{ stats.totalCandidates }} 份</span>
          <el-icon :size="14" class="stat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="stat-indicator orange"></div>
      </div>

      <div class="stat-card theme-green" @click="$router.push('/enterprise/interviews')">
        <div class="stat-top">
          <div class="stat-icon">
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.interviewCandidates }}</div>
            <div class="stat-label">面试邀约中</div>
          </div>
        </div>
        <div class="stat-footer">
          <span>{{ stats.rejectedCandidates }} 人已婉拒</span>
          <el-icon :size="14" class="stat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="stat-indicator green"></div>
      </div>

      <div class="stat-card theme-purple" @click="$router.push('/enterprise/recommend')">
        <div class="stat-top">
          <div class="stat-icon">
            <el-icon :size="20"><MagicStick /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeJobs }}</div>
            <div class="stat-label">AI推荐匹配</div>
          </div>
        </div>
        <div class="stat-footer">
          <span>智能匹配候选人</span>
          <el-icon :size="14" class="stat-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="stat-indicator purple"></div>
      </div>
    </div>

    <!-- 招聘进程概览 -->
    <div class="recruitment-overview">
      <div class="overview-header">
        <h3 class="section-title">
          <el-icon :size="18"><TrendCharts /></el-icon>
          <span>招聘进程概览</span>
        </h3>
      </div>
      <div class="overview-pipeline">
        <div class="pipeline-step">
          <div class="pipeline-dot pending"></div>
          <div class="pipeline-line" :style="{ width: pipelineWidth('PENDING') }"></div>
          <span class="pipeline-label">待筛选</span>
          <strong class="pipeline-num">{{ stats.pendingCandidates || 0 }}</strong>
        </div>
        <div class="pipeline-step">
          <div class="pipeline-dot interview"></div>
          <div class="pipeline-line" :style="{ width: pipelineWidth('INTERVIEW') }"></div>
          <span class="pipeline-label">面试中</span>
          <strong class="pipeline-num">{{ stats.interviewCandidates || 0 }}</strong>
        </div>
        <div class="pipeline-step">
          <div class="pipeline-dot hired"></div>
          <span class="pipeline-label">待入职</span>
          <strong class="pipeline-num">0</strong>
        </div>
      </div>
    </div>

    <!-- 快捷功能 + 最近岗位 + 动态 -->
    <div class="content-grid">
      <!-- 快捷功能 -->
      <div class="quick-actions">
        <h3 class="section-title">
          <el-icon :size="16"><Grid /></el-icon>
          <span>快捷入口</span>
        </h3>
        <div class="action-list">
          <div class="action-item" @click="$router.push('/enterprise/jobs')">
            <div class="action-icon-circle blue">
              <el-icon :size="18"><Edit /></el-icon>
            </div>
            <div class="action-item-text">
              <span class="action-name">岗位管理</span>
              <span class="action-desc">发布与编辑岗位</span>
            </div>
          </div>
          <div class="action-item" @click="$router.push('/enterprise/recommend')">
            <div class="action-icon-circle purple">
              <el-icon :size="18"><MagicStick /></el-icon>
            </div>
            <div class="action-item-text">
              <span class="action-name">AI推荐</span>
              <span class="action-desc">智能匹配人才</span>
            </div>
          </div>
          <div class="action-item" @click="$router.push('/enterprise/candidates')">
            <div class="action-icon-circle green">
              <el-icon :size="18"><User /></el-icon>
            </div>
            <div class="action-item-text">
              <span class="action-name">候选人库</span>
              <span class="action-desc">筛选投递简历</span>
            </div>
          </div>
          <div class="action-item" @click="$router.push('/enterprise/interviews')">
            <div class="action-icon-circle orange">
              <el-icon :size="18"><Calendar /></el-icon>
            </div>
            <div class="action-item-text">
              <span class="action-name">面试日程</span>
              <span class="action-desc">安排与管理面试</span>
            </div>
          </div>
          <div class="action-item" @click="$router.push('/chat')">
            <div class="action-icon-circle teal">
              <el-icon :size="18"><ChatLineSquare /></el-icon>
            </div>
            <div class="action-item-text">
              <span class="action-name">在线沟通</span>
              <span class="action-desc">联系候选人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近岗位 + 今日待办 -->
      <div class="right-column">
        <!-- 今日面试提醒 -->
        <div class="today-schedule" v-if="todaySchedules.length > 0">
          <h3 class="section-title">
            <el-icon :size="16"><Sunny /></el-icon>
            <span>今日面试提醒</span>
            <el-tag size="small" type="danger" effect="dark" round class="today-badge">{{ todaySchedules.length }}</el-tag>
          </h3>
          <div class="schedule-list">
            <div v-for="s in todaySchedules" :key="s.id" class="schedule-item">
              <div class="schedule-avatar">
                <el-avatar :size="36" :style="{ background: avatarColor(s.candidate) }">{{ s.candidate[0] }}</el-avatar>
              </div>
              <div class="schedule-info">
                <span class="schedule-name">{{ s.candidate }}</span>
                <span class="schedule-position">{{ s.position }}</span>
              </div>
              <span class="schedule-time">{{ s.time }}</span>
            </div>
          </div>
        </div>

        <!-- 最近发布的岗位 -->
        <div class="recent-jobs">
          <h3 class="section-title">
            <el-icon :size="16"><Clock /></el-icon>
            <span>最近岗位</span>
            <el-button link type="primary" class="view-all" @click="$router.push('/enterprise/jobs')">
              全部 <el-icon :size="12"><ArrowRight /></el-icon>
            </el-button>
          </h3>
          <div v-if="recentJobs.length > 0" class="jobs-list">
            <div
              v-for="job in recentJobs"
              :key="job.id"
              class="job-item"
              @click="$router.push('/enterprise/jobs')"
            >
              <div class="job-item-left">
                <div class="job-logo">
                  <img v-if="job.logoUrl" :src="job.logoUrl" alt="" />
                  <el-icon v-else :size="22"><OfficeBuilding /></el-icon>
                </div>
                <div class="job-info">
                  <div class="job-title">{{ job.title }}</div>
                  <div class="job-meta">
                    <span v-if="job.city"><el-icon :size="11"><Location /></el-icon> {{ job.city }}</span>
                    <span v-if="job.salaryRange" class="salary"><el-icon :size="11"><Money /></el-icon> {{ job.salaryRange }}</span>
                  </div>
                </div>
              </div>
              <el-tag :type="job.publishStatus === 1 ? 'success' : 'info'" size="small" round>
                {{ job.publishStatus === 1 ? '发布中' : '已下架' }}
              </el-tag>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon :size="36" color="#c0c4cc"><Folder /></el-icon>
            <p>还没有发布岗位</p>
            <el-button type="primary" size="small" round @click="$router.push('/enterprise/jobs')">立即发布</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStats } from '@/api/enterprise'

const userStore: any = useUserStore()

const stats = ref({
  totalJobs: 0, activeJobs: 0, offlineJobs: 0,
  totalCandidates: 0, pendingCandidates: 0,
  interviewCandidates: 0, rejectedCandidates: 0,
})
const recentJobs = ref<any[]>([])

const avatarColors = ['#165DFF', '#722ED1', '#00B42A', '#FF7D00', '#F53F3F']
function avatarColor(name: string): string {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

// Mock 今日面试
const todaySchedules = computed(() => {
  if (stats.value.interviewCandidates === 0) return []
  return [
    { id: 1, candidate: '张三', position: 'Java后端开发', time: '14:00' },
    { id: 2, candidate: '李四', position: '前端开发工程师', time: '16:30' },
  ]
})

function pipelineWidth(stage: string): string {
  const total = stats.value.totalCandidates || 1
  if (stage === 'PENDING') return Math.max(20, (stats.value.pendingCandidates / total) * 100) + '%'
  if (stage === 'INTERVIEW') return Math.max(20, (stats.value.interviewCandidates / total) * 100) + '%'
  return '0%'
}

async function loadStats() {
  try {
    const res: any = await getDashboardStats()
    if (res.code === 200 && res.data) {
      stats.value = res.data
      recentJobs.value = res.data.recentJobs || []
    }
  } catch { /* silent */ }
}

onMounted(() => { loadStats() })
</script>

<style scoped>
.dashboard { max-width: 1100px; margin: 0 auto; padding-bottom: 32px; }

/* ===== 顶部横幅 ===== */
.hero-banner {
  position: relative;
  background: linear-gradient(135deg, #165DFF 0%, #3D7EFF 40%, #6AA1FF 100%);
  border-radius: 16px; padding: 32px 40px; margin-bottom: 24px;
  overflow: hidden; color: #fff;
}
.hero-content { display: flex; align-items: center; justify-content: space-between; position: relative; z-index: 2; }
.hero-left { display: flex; align-items: center; gap: 16px; }
.company-avatar {
  width: 60px; height: 60px; background: rgba(255,255,255,0.2);
  border-radius: 16px; display: flex; align-items: center;
  justify-content: center; backdrop-filter: blur(10px); flex-shrink: 0;
}
.hero-text h1 { font-size: 22px; font-weight: 600; margin: 0 0 6px; line-height: 1.3; }
.hero-text p { font-size: 14px; opacity: 0.85; margin: 0; }
.hero-right .el-button {
  --el-button-bg-color: #fff; --el-button-text-color: #165DFF;
  --el-button-hover-bg-color: #f0f5ff; border: none;
  font-weight: 600; padding: 14px 28px; font-size: 15px;
}
.hero-decor .decor-circle { position: absolute; border-radius: 50%; }
.hero-decor .c1 { width: 200px; height: 200px; background: rgba(255,255,255,0.06); top: -60px; right: -40px; }
.hero-decor .c2 { width: 120px; height: 120px; background: rgba(255,255,255,0.08); bottom: -30px; right: 140px; }
.hero-decor .decor-dots {
  position: absolute; top: 20px; right: 220px; width: 80px; height: 80px;
  background-image: radial-gradient(rgba(255,255,255,0.15) 2px, transparent 2px);
  background-size: 16px 16px;
}

/* ===== 统计卡片 ===== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 18px 20px; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #f0f0f0; position: relative; overflow: hidden;
}
.stat-indicator {
  position: absolute; top: 0; left: 0; width: 100%; height: 3px;
  border-radius: 3px 3px 0 0;
}
.stat-indicator.blue { background: #165DFF; }
.stat-indicator.orange { background: #FF7D00; }
.stat-indicator.green { background: #00B42A; }
.stat-indicator.purple { background: #722ED1; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.stat-top { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 12px; }
.stat-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.theme-blue .stat-icon { background: #e8f1ff; color: #165DFF; }
.theme-orange .stat-icon { background: #fff7e8; color: #FF7D00; }
.theme-green .stat-icon { background: #e8ffea; color: #00B42A; }
.theme-purple .stat-icon { background: #f5e8ff; color: #722ED1; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #1d2129; line-height: 1.2; }
.stat-label { font-size: 13px; color: #86909c; margin-top: 2px; }
.stat-footer {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 11px; color: #86909c; padding-top: 10px; border-top: 1px solid #f2f3f5;
}
.stat-arrow { color: #c0c4cc; transition: transform 0.2s; }
.stat-card:hover .stat-arrow { transform: translateX(3px); color: #165DFF; }

/* ===== 招聘进程 ===== */
.recruitment-overview {
  background: #fff; border-radius: 12px; padding: 20px 24px;
  border: 1px solid #f0f0f0; margin-bottom: 20px;
}
.overview-header { margin-bottom: 16px; }
.overview-pipeline { display: flex; align-items: flex-start; gap: 0; }
.pipeline-step {
  display: flex; flex-direction: column; align-items: center; flex: 1;
  position: relative;
}
.pipeline-dot {
  width: 12px; height: 12px; border-radius: 50%; margin-bottom: 8px;
  border: 3px solid #fff; box-shadow: 0 0 0 2px #e5e6eb; z-index: 1;
}
.pipeline-dot.pending { background: #165DFF; box-shadow: 0 0 0 2px #165DFF; }
.pipeline-dot.interview { background: #00B42A; box-shadow: 0 0 0 2px #00B42A; }
.pipeline-dot.hired { background: #722ED1; box-shadow: 0 0 0 2px #722ED1; }
.pipeline-line {
  position: absolute; top: 4px; left: 50%; height: 3px;
  background: #e8f1ff; border-radius: 2px; z-index: 0;
}
.pipeline-label { font-size: 13px; color: #86909c; margin-bottom: 4px; }
.pipeline-num { font-size: 20px; font-weight: 700; color: #1d2129; }

/* ===== 内容网格 ===== */
.content-grid { display: grid; grid-template-columns: 280px 1fr; gap: 16px; }
.section-title {
  font-size: 15px; font-weight: 600; color: #1d2129;
  margin: 0 0 14px; display: flex; align-items: center; gap: 8px;
}

/* ===== 快捷入口 ===== */
.quick-actions {
  background: #fff; border-radius: 12px; padding: 20px; border: 1px solid #f0f0f0;
}
.action-list { display: flex; flex-direction: column; gap: 4px; }
.action-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 10px; cursor: pointer;
  transition: background 0.2s;
}
.action-item:hover { background: #f0f5ff; }
.action-icon-circle {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.action-icon-circle.blue { background: #e8f1ff; color: #165DFF; }
.action-icon-circle.purple { background: #f5e8ff; color: #722ED1; }
.action-icon-circle.green { background: #e8ffea; color: #00B42A; }
.action-icon-circle.orange { background: #fff7e8; color: #FF7D00; }
.action-icon-circle.teal { background: #e8fffa; color: #16C8C8; }
.action-item-text { display: flex; flex-direction: column; }
.action-name { font-size: 14px; font-weight: 500; color: #1d2129; }
.action-desc { font-size: 11px; color: #86909c; margin-top: 1px; }

/* ===== 右侧列 ===== */
.right-column { display: flex; flex-direction: column; gap: 16px; }

/* 今日面试提醒 */
.today-schedule {
  background: #fff; border-radius: 12px; padding: 20px; border: 1px solid #f0f0f0;
  position: relative;
}
.today-badge { margin-left: auto; }
.schedule-list { display: flex; flex-direction: column; gap: 8px; }
.schedule-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px; border-radius: 10px;
  background: #fafbfc; transition: background 0.2s;
}
.schedule-item:hover { background: #fff7e8; }
.schedule-avatar { flex-shrink: 0; }
.schedule-info { flex: 1; min-width: 0; }
.schedule-name { font-size: 14px; font-weight: 600; color: #1d2129; display: block; }
.schedule-position { font-size: 12px; color: #86909c; display: block; }
.schedule-time { font-size: 14px; font-weight: 600; color: #FF7D00; flex-shrink: 0; }

/* 最近岗位 */
.recent-jobs {
  background: #fff; border-radius: 12px; padding: 20px; border: 1px solid #f0f0f0;
  flex: 1;
}
.view-all { margin-left: auto; font-size: 13px; }
.jobs-list { display: flex; flex-direction: column; gap: 2px; }
.job-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px; border-radius: 10px; cursor: pointer;
  transition: background 0.2s;
}
.job-item:hover { background: #f7f8fa; }
.job-item-left { display: flex; align-items: center; gap: 12px; }
.job-logo {
  width: 42px; height: 42px; border-radius: 10px;
  background: #f0f5ff; display: flex; align-items: center;
  justify-content: center; color: #165DFF; overflow: hidden; flex-shrink: 0;
}
.job-logo img { width: 100%; height: 100%; object-fit: cover; }
.job-title { font-size: 15px; font-weight: 600; color: #1d2129; margin-bottom: 3px; }
.job-meta { font-size: 12px; color: #86909c; display: flex; gap: 12px; align-items: center; }
.job-meta span { display: flex; align-items: center; gap: 2px; }
.job-meta .salary { color: #F53F3F; font-weight: 500; }
.empty-state { text-align: center; padding: 40px 20px; color: #86909c; }
.empty-state p { margin: 10px 0 14px; font-size: 14px; }

/* 响应式 */
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
  .hero-banner { padding: 24px 20px; }
  .hero-content { flex-direction: column; gap: 16px; align-items: flex-start; }
  .hero-right { width: 100%; }
  .hero-right .el-button { width: 100%; }
}
@media (max-width: 500px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
