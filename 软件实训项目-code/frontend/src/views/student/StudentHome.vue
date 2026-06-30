<template>
  <div class="home-page">
    <!-- Hero 搜索区 -->
    <div class="hero-section">
      <!-- 装饰元素 -->
      <div class="hero-bg-decor">
        <div class="hero-float-icon icon-java" style="left: 8%; top: 15%; animation-delay: 0s;">☕</div>
        <div class="hero-float-icon icon-py" style="left: 15%; top: 65%; animation-delay: 1.2s;">🐍</div>
        <div class="hero-float-icon icon-go" style="right: 10%; top: 20%; animation-delay: 0.6s;">🔷</div>
        <div class="hero-float-icon icon-ai" style="right: 18%; top: 70%; animation-delay: 1.8s;">🤖</div>
        <div class="hero-float-icon icon-cloud" style="left: 50%; top: 10%; animation-delay: 0.3s;">☁️</div>
        <div class="hero-dot-grid"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span> AI 驱动的智能求职平台
        </div>
        <h1 class="hero-title">IT行业求职<span class="hero-highlight">，从这里开始</span></h1>
        <p class="hero-subtitle">Java · Python · 前端 · 算法 · AI · Go，精准找到你的技术舞台</p>
        <div class="hero-search">
          <el-icon class="hero-search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索技术职位、公司、技术栈"
            @keyup.enter="goSearch"
          />
          <el-button type="primary" size="large" class="hero-search-btn" @click="goSearch">
            <el-icon><Search /></el-icon> 搜索职位
          </el-button>
        </div>
        <div class="hot-keywords">
          <span class="hot-label">🔥 热门搜索：</span>
          <span
            v-for="kw in hotKeywords"
            :key="kw"
            class="hot-keyword"
            @click="searchKeyword = kw; goSearch()"
          >{{ kw }}</span>
        </div>
      </div>
    </div>

    <!-- 平台数据统计 -->
    <div class="stats-section">
      <div class="stat-item">
        <div class="stat-icon-wrapper" style="background: #eff6ff; color: #2563eb;">
          <el-icon size="28"><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ animatedStats.companies }}</span>
          <span class="stat-label">合作企业</span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon-wrapper" style="background: #ecfdf5; color: #10b981;">
          <el-icon size="28"><Briefcase /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ animatedStats.jobs }}</span>
          <span class="stat-label">IT 岗位</span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon-wrapper" style="background: #fef2f2; color: #ef4444;">
          <el-icon size="28"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ animatedStats.offers }}</span>
          <span class="stat-label">已发 Offer</span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon-wrapper" style="background: #f5f3ff; color: #8b5cf6;">
          <el-icon size="28"><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ animatedStats.matches }}</span>
          <span class="stat-label">精准匹配</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口横向滑动 -->
    <div class="quick-channel">
      <div class="channel-header">
        <h2 class="channel-title">🔥 热门技术方向</h2>
        <span class="channel-subtitle">点击探索你感兴趣的IT岗位</span>
      </div>
      <el-scrollbar>
        <div class="channel-list">
          <div
            v-for="channel in channels"
            :key="channel.name"
            class="channel-item"
            :style="{ '--ch-color': channel.color, '--ch-bg': channel.bg }"
            @click="goChannel(channel)"
          >
            <div class="ch-icon-wrap">
              <el-icon :size="28" :color="channel.color"><component :is="channel.icon" /></el-icon>
            </div>
            <span class="channel-name">{{ channel.name }}</span>
            <span class="channel-desc">{{ getChannelDesc(channel.name) }}</span>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 为什么选择我们 -->
    <div class="why-section">
      <h2 class="section-main-title">为什么选择 IT求职？</h2>
      <div class="why-cards">
        <div class="why-card">
          <div class="why-icon" style="background: linear-gradient(135deg, #2563eb, #1d4ed8);">
            <el-icon size="32" color="#fff"><Search /></el-icon>
          </div>
          <h3>AI 智能匹配</h3>
          <p>基于你的技术栈和学习经历，AI精准推荐最适合的IT岗位</p>
        </div>
        <div class="why-card">
          <div class="why-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
            <el-icon size="32" color="#fff"><DocumentChecked /></el-icon>
          </div>
          <h3>简历智能优化</h3>
          <p>AI诊断简历短板，优化项目经验描述，提升大厂面试邀请率</p>
        </div>
        <div class="why-card">
          <div class="why-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
            <el-icon size="32" color="#fff"><ChatDotRound /></el-icon>
          </div>
          <h3>模拟面试训练</h3>
          <p>算法题+系统设计+八股文全覆盖，真实面试官模拟对话</p>
        </div>
        <div class="why-card">
          <div class="why-icon" style="background: linear-gradient(135deg, #8b5cf6, #7c3aed);">
            <el-icon size="32" color="#fff"><DataLine /></el-icon>
          </div>
          <h3>学习路径规划</h3>
          <p>根据目标岗位技能要求，生成个性化学习路径和资源推荐</p>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="home-main">
      <!-- 左侧职位列表 -->
      <div class="home-left">
        <!-- 筛选 Tab -->
        <div class="filter-tabs">
          <div class="filter-left">
            <span
              v-for="tab in filterTabs"
              :key="tab.value"
              class="filter-tab"
              :class="{ active: activeTab === tab.value }"
              @click="activeTab = tab.value"
            >{{ tab.icon }} {{ tab.label }}</span>
          </div>
          <div class="filter-right">
            <el-checkbox v-model="onlyNear" size="small">附近</el-checkbox>
            <el-checkbox v-model="onlyNew" size="small">最新</el-checkbox>
          </div>
        </div>

        <!-- 职位列表 -->
        <LoadingSkeleton v-if="jobLoading" type="list" :rows="4" />
        <template v-else-if="displayJobs.length">
          <JobCard
            v-for="job in displayJobs"
            :key="job.id"
            :job="job"
            class="home-job-card"
            @click="$router.push(`/student/job/${job.id}`)"
          />
          <div class="load-more">
            <el-button type="primary" plain size="large" @click="$router.push('/student/job-matching')">
              查看更多职位 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <EmptyState v-else scene="search" title="暂无职位" description="换个条件试试吧" />
      </div>

      <!-- 右侧边栏 -->
      <div class="home-right">
        <!-- 我的求职 -->
        <div class="right-card user-card">
          <div class="user-card-decor"></div>
          <div class="user-header">
            <el-avatar :size="56" :icon="UserFilled" class="user-avatar" />
            <div class="user-meta">
              <div class="user-name">{{ userStore.username || '求职者' }}</div>
              <div class="user-desc">完善技术栈，获得更多IT岗位机会</div>
            </div>
          </div>
          <div class="user-stats">
            <div class="user-stat" @click="$router.push('/student/applications')">
              <div class="stat-num">{{ jobStore.applications.length }}</div>
              <div class="stat-label">投递</div>
            </div>
            <div class="user-stat center-stat" @click="$router.push('/student/favorites')">
              <div class="stat-num">{{ jobStore.favorites.size }}</div>
              <div class="stat-label">收藏</div>
            </div>
            <div class="user-stat" @click="$router.push('/student/messages')">
              <div class="stat-num">3</div>
              <div class="stat-label">消息</div>
            </div>
          </div>
          <el-button type="primary" class="resume-btn" @click="$router.push('/student/profile')">
            <el-icon><Edit /></el-icon> 完善求职画像
          </el-button>
        </div>

        <!-- 简历评分 -->
        <div class="right-card score-card">
          <div class="section-title"><el-icon><TrendCharts /></el-icon> 简历评分</div>
          <div class="resume-score">
            <el-progress type="dashboard" :percentage="78" :stroke-width="10" color="#2563eb">
              <template #default>
                <div class="score-num">78<span class="score-unit">分</span></div>
              </template>
            </el-progress>
            <div class="score-desc">简历完整度良好，继续优化可提升匹配度</div>
          </div>
          <el-button text type="primary" class="score-action" @click="$router.push('/student/resume-optimize')">
            AI 简历优化 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <!-- 热门公司 -->
        <div class="right-card company-card">
          <div class="section-title"><el-icon><OfficeBuilding /></el-icon> 热门公司</div>
          <div class="company-list">
            <div v-for="(company, idx) in hotCompanies" :key="company.name" class="company-item" :style="{ animationDelay: idx * 0.05 + 's' }">
              <el-avatar :size="44" shape="square" class="company-avatar" :style="{ background: company.color }">
                {{ company.name.slice(0, 2) }}
              </el-avatar>
              <div class="company-info">
                <div class="company-name">{{ company.name }}</div>
                <div class="company-tags">{{ company.industry }} · {{ company.jobs }}个职位</div>
              </div>
              <el-tag size="small" type="danger" effect="light">热招</el-tag>
            </div>
          </div>
        </div>

        <!-- 求职攻略 -->
        <div class="right-card guide-card">
          <div class="section-title"><el-icon><Reading /></el-icon> 求职攻略</div>
          <ul class="guide-list">
            <li v-for="(guide, i) in guides" :key="i" @click="$router.push(guide.path)">
              <span class="guide-num">{{ String(i + 1).padStart(2, '0') }}</span>
              <div class="guide-content">
                <el-tag size="small" :type="guide.type" class="guide-tag">{{ guide.tag }}</el-tag>
                <span class="guide-title">{{ guide.title }}</span>
              </div>
              <el-icon class="guide-arrow"><ArrowRight /></el-icon>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, ArrowRight, UserFilled, DocumentChecked, Star, ChatDotRound,
  Briefcase, OfficeBuilding, School, Monitor, FirstAidKit, Wallet,
  DataLine, Brush, Setting, Trophy, Edit, TrendCharts, Reading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useJobStore } from '@/stores/job'
import { recommendJobs as recommendJobsApi } from '@/api/student'
import { enrichJob, type EnrichedJob } from '@/utils/jobEnrich'
import JobCard from '@/components/job/JobCard.vue'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()
const jobStore = useJobStore()

const searchKeyword = ref('')
const activeTab = ref('recommend')
const onlyNear = ref(false)
const onlyNew = ref(false)
const jobLoading = ref(false)
const allJobs = ref<EnrichedJob[]>([])

// 动画数字
const animatedStats = reactive({ companies: 0, jobs: 0, offers: 0, matches: 0 })
const statTargets = { companies: 2000, jobs: 50000, offers: 15000, matches: 80000 }

function animateStats() {
  const duration = 2000
  const start = performance.now()
  function tick(now: number) {
    const progress = Math.min((now - start) / duration, 1)
    const ease = 1 - Math.pow(1 - progress, 3)
    animatedStats.companies = Math.floor(statTargets.companies * ease)
    animatedStats.jobs = Math.floor(statTargets.jobs * ease)
    animatedStats.offers = Math.floor(statTargets.offers * ease)
    animatedStats.matches = Math.floor(statTargets.matches * ease)
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

const hotKeywords = ['Java', 'Python', 'Go', '前端工程师', '后端开发', '算法工程师', '数据分析', 'AI工程师', '架构师']

const channels = [
  { name: '前端开发', icon: Monitor, color: '#2563eb', bg: '#eff6ff', keyword: '前端', direction: '前端开发', jobType: '' },
  { name: '后端开发', icon: DataLine, color: '#10b981', bg: '#ecfdf5', keyword: '后端', direction: '后端开发', jobType: '' },
  { name: '移动端', icon: Briefcase, color: '#f59e0b', bg: '#fff7ed', keyword: '移动端', direction: '移动开发', jobType: '' },
  { name: '算法&AI', icon: Trophy, color: '#8b5cf6', bg: '#f5f3ff', keyword: '算法', direction: '', jobType: '' },
  { name: '测试开发', icon: Setting, color: '#ef4444', bg: '#fef2f2', keyword: '测试', direction: '测试', jobType: '' },
  { name: '数据科学', icon: Brush, color: '#06b6d4', bg: '#ecfeff', keyword: '数据', direction: '数据', jobType: '' },
  { name: 'DevOps', icon: Star, color: '#ec4899', bg: '#fdf2f8', keyword: '运维', direction: '运维', jobType: '' },
  { name: '架构师', icon: Wallet, color: '#14b8a6', bg: '#f0fdfa', keyword: '架构', direction: '', jobType: '' },
  { name: '名企IT', icon: OfficeBuilding, color: '#f97316', bg: '#fff7ed', keyword: '', direction: '', jobType: '' },
  { name: '实习', icon: School, color: '#6366f1', bg: '#eef2ff', keyword: '实习', direction: '', jobType: '实习' },
  { name: '校招', icon: DocumentChecked, color: '#84cc16', bg: '#f7fee7', keyword: '校招', direction: '', jobType: '校招' },
  { name: '高薪', icon: Star, color: '#d946ef', bg: '#fdf4ff', keyword: '', direction: '', jobType: '' }
]

function getChannelDesc(name: string) {
  const map: Record<string, string> = {
    '前端开发': 'React/Vue', '后端开发': 'Java/Go', '移动端': 'iOS/Android',
    '算法&AI': '深度学习', '测试开发': '自动化', '数据科学': '大数据分析',
    'DevOps': 'K8s/Docker', '架构师': '系统架构', '名企IT': '大厂直招',
    '实习': '在校可投', '校招': '应届专属', '高薪': '20K+'
  }
  return map[name] || ''
}

const filterTabs = [
  { label: '推荐', value: 'recommend', icon: '⭐' },
  { label: '最新', value: 'latest', icon: '🆕' },
  { label: '高薪', value: 'high-salary', icon: '💰' },
  { label: '附近', value: 'nearby', icon: '📍' },
  { label: '实习', value: 'intern', icon: '🎓' }
]

const companyColors = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4']
const hotCompanies = [
  { name: '腾讯科技', industry: '互联网/社交', jobs: 385, color: companyColors[0] },
  { name: '字节跳动', industry: '互联网/AI', jobs: 520, color: companyColors[1] },
  { name: '阿里巴巴', industry: '电商/云计算', jobs: 296, color: companyColors[2] },
  { name: '华为技术', industry: '通信/芯片', jobs: 412, color: companyColors[3] },
  { name: '美团', industry: '生活服务', jobs: 178, color: companyColors[4] },
  { name: '百度', industry: 'AI/自动驾驶', jobs: 230, color: companyColors[5] }
]

const guides = [
  { tag: '简历', type: 'success', title: '技术岗简历怎么写？HR 最看重这5点', path: '/student/resume-optimize' },
  { tag: '面试', type: 'warning', title: '大厂技术面试必刷的算法题', path: '/student/interview' },
  { tag: '技术', type: 'primary', title: 'Java/Go/前端 哪个方向更有前景？', path: '/student/assessment' },
  { tag: '规划', type: 'info', title: 'IT行业技术路线与职业发展规划', path: '/student/career-exploration' }
]

const displayJobs = computed(() => {
  let list = [...allJobs.value]
  if (activeTab.value === 'latest') {
    list.sort((a, b) => b.id - a.id)
  } else if (activeTab.value === 'high-salary') {
    list.sort((a, b) => (b.salaryMax || 0) - (a.salaryMax || 0))
  } else if (activeTab.value === 'intern') {
    list = list.filter(j => j.jobType === '实习')
  }
  return list.slice(0, 8)
})

function goSearch() {
  if (!searchKeyword.value.trim()) return
  jobStore.filters.keyword = searchKeyword.value.trim()
  router.push('/student/job-matching')
}

function goChannel(channel: typeof channels[0]) {
  jobStore.setJobs([])
  jobStore.resetFilters()
  if (channel.keyword) jobStore.filters.keyword = channel.keyword
  if (channel.direction) jobStore.filters.direction = [channel.direction]
  if (channel.jobType) jobStore.filters.jobType = channel.jobType
  if (channel.name === '高薪') {
    jobStore.filters.salaryRange = [20, 999]
    jobStore.sort = 'salaryDesc'
  }
  router.push('/student/job-matching')
}

async function loadJobs() {
  jobLoading.value = true
  try {
    const res: any = await recommendJobsApi()
    allJobs.value = (res.data || []).map(enrichJob)
  } catch {
    allJobs.value = []
  } finally {
    jobLoading.value = false
  }
}

onMounted(() => {
  loadJobs()
  animateStats()
})
</script>

<style scoped lang="scss">
.home-page {
  margin: -20px;
}

/* ========== Hero 搜索区 ========== */
.hero-section {
  position: relative;
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 40%, #1e3a8a 100%);
  padding: 64px 24px 72px;
  color: #fff;
  overflow: hidden;
}
.hero-bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.hero-float-icon {
  position: absolute;
  font-size: 36px;
  opacity: 0.25;
  animation: heroFloat 4s ease-in-out infinite;
}
@keyframes heroFloat {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-16px) rotate(5deg); }
}
.hero-dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.5;
}
.hero-content {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.25);
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
  letter-spacing: 0.5px;
}
.badge-dot {
  width: 8px;
  height: 8px;
  background: #34d399;
  border-radius: 50%;
  animation: badgePulse 1.5s ease-in-out infinite;
}
@keyframes badgePulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}
.hero-title {
  font-size: 40px;
  font-weight: 800;
  margin-bottom: 14px;
  letter-spacing: -0.5px;
  line-height: 1.2;
}
.hero-highlight {
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-subtitle {
  font-size: 17px;
  opacity: 0.85;
  margin-bottom: 32px;
  letter-spacing: 0.5px;
}
.hero-search {
  position: relative;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  padding: 6px;
  transition: box-shadow 0.3s;
  &:focus-within {
    box-shadow: 0 12px 48px rgba(37, 99, 235, 0.35);
  }
}
.hero-search-icon {
  position: absolute;
  left: 20px;
  color: #9ca3af;
  font-size: 20px;
  z-index: 1;
}
.hero-search input {
  flex: 1;
  height: 54px;
  border: none;
  padding: 0 20px 0 52px;
  font-size: 16px;
  outline: none;
  color: #1f2937;
  background: transparent;
  &::placeholder { color: #9ca3af; }
}
.hero-search-btn {
  height: 54px;
  border-radius: 28px;
  padding: 0 36px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.hot-keywords {
  margin-top: 20px;
  font-size: 13px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}
.hot-label {
  opacity: 0.8;
}
.hot-keyword {
  opacity: 0.85;
  cursor: pointer;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(255,255,255,0.12);
  transition: all 0.2s;
  &:hover {
    opacity: 1;
    background: rgba(255,255,255,0.25);
    transform: translateY(-1px);
  }
}

/* ========== 数据统计 ========== */
.stats-section {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  gap: 0;
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 2;
  margin-top: -4px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 40px;
}
.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.stat-divider {
  width: 1px;
  height: 48px;
  background: #e5e7eb;
}

/* ========== 快捷入口 ========== */
.quick-channel {
  background: #fff;
  padding: 28px 24px 24px;
  border-bottom: 1px solid var(--border-color);
}
.channel-header {
  margin-bottom: 18px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.channel-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}
.channel-subtitle {
  font-size: 13px;
  color: var(--text-tertiary);
}
.channel-list {
  display: flex;
  gap: 16px;
  padding-bottom: 4px;
}
.channel-item {
  flex-shrink: 0;
  width: 110px;
  padding: 18px 12px 14px;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  background: var(--ch-bg, #f9fafb);
  border: 1.5px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: var(--ch-color, #2563eb);
    opacity: 0;
    transition: opacity 0.3s;
    border-radius: var(--radius-lg);
  }
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
    border-color: var(--ch-color, #2563eb);
    &::after { opacity: 0.04; }
  }
}
.ch-icon-wrap {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255,255,255,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.channel-name {
  position: relative;
  z-index: 1;
  font-size: 14px;
  font-weight: 700;
  color: var(--ch-color, #374151);
}
.channel-desc {
  position: relative;
  z-index: 1;
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
}

/* ========== 为什么选择我们 ========== */
.why-section {
  padding: 48px 24px;
  background: #f8fafc;
  text-align: center;
}
.section-main-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 36px;
  letter-spacing: -0.5px;
}
.why-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.why-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 36px 24px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s;
  cursor: default;
  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-md);
  }
  .why-icon {
    width: 64px;
    height: 64px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 18px;
  }
  h3 {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 10px;
  }
  p {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

/* ========== 主体 ========== */
.home-main {
  display: flex;
  gap: 20px;
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.home-left {
  flex: 1;
  min-width: 0;
}
.home-right {
  width: 330px;
  flex-shrink: 0;
}

/* ========== 筛选 Tab ========== */
.filter-tabs {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
}
.filter-left {
  display: flex;
  gap: 6px;
}
.filter-tab {
  padding: 7px 18px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 16px;
  transition: all 0.25s;
  font-weight: 500;
  &:hover {
    color: var(--primary-color);
    background: var(--primary-light);
  }
  &.active {
    color: #fff;
    background: var(--primary-color);
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(37,99,235,0.3);
  }
}
.filter-right {
  display: flex;
  gap: 12px;
}
.home-job-card {
  margin-bottom: 14px;
}
.load-more {
  text-align: center;
  padding: 28px 0 12px;
}

/* ========== 右侧卡片 ========== */
.right-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.3s;
  &:hover { box-shadow: var(--shadow-md); }
}
.user-card {
  text-align: center;
  position: relative;
  overflow: hidden;
}
.user-card-decor {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 64px;
  background: linear-gradient(135deg, var(--primary-color), #1d4ed8);
  opacity: 0.06;
}
.user-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  position: relative;
}
.user-avatar {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(37,99,235,0.3);
}
.user-meta { text-align: left; }
.user-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}
.user-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.user-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
  padding: 14px 0;
  border-top: 1px solid var(--divider-color);
  border-bottom: 1px solid var(--divider-color);
}
.user-stat {
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  &:hover {
    background: #f9fafb;
    .stat-num { color: var(--primary-color); }
  }
}
.user-stat .stat-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  transition: color 0.2s;
}
.user-stat .stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.center-stat {
  border-left: 1px solid var(--divider-color);
  border-right: 1px solid var(--divider-color);
  padding: 4px 20px;
}
.resume-btn {
  width: 100%;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.score-card, .company-card, .guide-card {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 16px;
  }
}
.resume-score {
  text-align: center;
  padding: 6px 0;
}
.score-num {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
}
.score-unit {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}
.score-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 10px;
  line-height: 1.5;
}
.score-action {
  margin-top: 4px;
  font-weight: 500;
}

.company-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.company-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
  animation: fadeInUp 0.4s ease both;
  &:hover {
    background: #f9fafb;
    .company-name { color: var(--primary-color); }
  }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.company-avatar {
  font-weight: 700;
  font-size: 16px;
  color: #fff;
}
.company-info {
  flex: 1;
  min-width: 0;
}
.company-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.2s;
}
.company-tags {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 3px;
}

.guide-list {
  list-style: none;
  padding: 0;
  margin: 0;
  li {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    &:hover {
      background: #f9fafb;
      .guide-title { color: var(--primary-color); }
      .guide-arrow { opacity: 1; transform: translateX(0); }
    }
  }
}
.guide-num {
  font-size: 18px;
  font-weight: 800;
  color: #d1d5db;
  flex-shrink: 0;
  width: 28px;
}
.guide-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.guide-tag { flex-shrink: 0; width: fit-content; }
.guide-title {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.4;
  transition: color 0.2s;
}
.guide-arrow {
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.2s;
  color: var(--primary-color);
  flex-shrink: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 14px;
}

/* ========== 响应式 ========== */
@media (max-width: 1100px) {
  .why-cards { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 992px) {
  .home-main { flex-direction: column; }
  .home-right { width: 100%; }
  .hero-title { font-size: 30px; }
  .stats-section { flex-wrap: wrap; gap: 16px 0; padding: 24px 16px; }
  .stat-item { padding: 0 20px; }
  .stat-divider:nth-child(2) { display: none; }
}
@media (max-width: 768px) {
  .home-page { margin: -12px; }
  .hero-section { padding: 40px 16px 48px; }
  .hero-title { font-size: 22px; }
  .hero-search-btn { padding: 0 16px; font-size: 14px; }
  .hero-float-icon { display: none; }
  .home-main { padding: 12px; }
  .filter-tabs { flex-direction: column; gap: 12px; align-items: flex-start; }
  .why-cards { grid-template-columns: 1fr; }
  .stats-section { flex-direction: column; gap: 16px; }
  .stat-divider { width: 60%; height: 1px; }
  .channel-item { width: 96px; padding: 14px 10px 12px; }
}
@media (max-width: 480px) {
  .hero-search { flex-direction: column; border-radius: 16px; padding: 8px; gap: 8px; }
  .hero-search-btn { width: 100%; border-radius: 12px; }
  .hero-search input { height: 44px; }
  .hero-title { font-size: 19px; }
}
</style>
