<template>
  <div class="admin-dashboard">
    <!-- 欢迎区 -->
    <div class="dashboard-welcome">
      <div class="welcome-text">
        <h2>数据看板</h2>
        <p>实时监控平台核心数据指标，掌握整体运营状况</p>
      </div>
      <div class="welcome-time">{{ currentTime }}</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="item in dashboardCards" :key="item.label" :class="item.colorClass">
        <div class="stat-card-inner">
          <div class="stat-icon-box">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">
              <span class="stat-number">{{ item.formattedValue }}</span>
              <span class="stat-unit">{{ item.unit }}</span>
            </div>
            <div class="stat-sub">{{ item.description }}</div>
          </div>
        </div>
        <div class="stat-card-bg"></div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="dashboard-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="quick-actions">
        <div class="action-item" @click="$router.push('/admin/users')">
          <el-icon :size="22"><UserFilled /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/skills')">
          <el-icon :size="22"><Collection /></el-icon>
          <span>技能词典</span>
        </div>
        <div class="action-item" @click="refreshData">
          <el-icon :size="22"><Refresh /></el-icon>
          <span>刷新数据</span>
        </div>
      </div>
    </div>

    <!-- 最近趋势简述 -->
    <div class="dashboard-section">
      <h3 class="section-title">平台概况</h3>
      <div class="platform-overview">
        <div class="overview-item">
          <div class="overview-dot dot-blue"></div>
          <div class="overview-info">
            <strong>{{ dashboardCards[0]?.value || 0 }}</strong> 名注册用户
          </div>
        </div>
        <div class="overview-item">
          <div class="overview-dot dot-green"></div>
          <div class="overview-info">
            <strong>{{ dashboardCards[1]?.value || 0 }}</strong> 次能力测评
          </div>
        </div>
        <div class="overview-item">
          <div class="overview-dot dot-orange"></div>
          <div class="overview-info">
            <strong>{{ dashboardCards[2]?.value || 0 }}</strong> 次岗位匹配
          </div>
        </div>
        <div class="overview-item">
          <div class="overview-dot dot-purple"></div>
          <div class="overview-info">
            已累计服务 <strong>{{ dashboardCards[0]?.value || 0 }}</strong> 名用户，
            完成 <strong>{{ dashboardCards[1]?.value || 0 }}</strong> 次测评分析
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { UserFilled, Collection, Refresh, DataLine, TrendCharts,
  Connection, DocumentChecked, ChatDotSquare, Monitor } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/admin'

interface StatCard {
  label: string
  value: number
  icon: any
  colorClass: string
  description: string
  unit: string
  formattedValue: string
}

const dashboardCards = ref<StatCard[]>([
  { label: '用户总数', value: 0, icon: UserFilled, colorClass: 'card-blue', description: '平台注册用户总量', unit: '人', formattedValue: '0' },
  { label: '测评次数', value: 0, icon: DataLine, colorClass: 'card-green', description: '累计能力测评次数', unit: '次', formattedValue: '0' },
  { label: '匹配次数', value: 0, icon: Connection, colorClass: 'card-orange', description: '岗位匹配调用次数', unit: '次', formattedValue: '0' },
  { label: '简历分析', value: 0, icon: DocumentChecked, colorClass: 'card-purple', description: '简历优化分析次数', unit: '次', formattedValue: '0' },
  { label: '面试次数', value: 0, icon: ChatDotSquare, colorClass: 'card-teal', description: '模拟面试练习次数', unit: '次', formattedValue: '0' },
  { label: '企业推荐', value: 0, icon: Monitor, colorClass: 'card-pink', description: '人才推荐调用次数', unit: '次', formattedValue: '0' }
])

async function refreshData() {
  await loadData()
}

async function loadData() {
  try {
    const res: any = await getDashboard()
    const d = res.data || {}
    const rawValues = [
      { val: d.totalUsers || 0, unit: '人' },
      { val: d.totalAssessments || 0, unit: '次' },
      { val: d.totalMatches || 0, unit: '次' },
      { val: d.totalResumeAnalysis || 0, unit: '次' },
      { val: d.totalInterviews || 0, unit: '次' },
      { val: d.totalEnterpriseRecommendations || 0, unit: '次' }
    ]
    dashboardCards.value.forEach((card, i) => {
      card.value = rawValues[i].val
      card.unit = rawValues[i].unit
      card.formattedValue = formatNumber(rawValues[i].val)
    })
  } catch {
    // 数据加载失败时保持默认值
  }
}

function formatNumber(num: number): string {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString('zh-CN')
}

// 实时时间
const currentTime = ref('')
let timer: ReturnType<typeof setInterval>
function updateTime() {
  const now = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  currentTime.value = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekDays[now.getDay()]} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadData()
  updateTime()
  timer = setInterval(updateTime, 60000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped lang="scss">
.admin-dashboard {
  max-width: 1280px;
  margin: 0 auto;
  padding: 4px 0;
}

/* 欢迎区 */
.dashboard-welcome {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
  padding: 28px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.welcome-text h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}
.welcome-text p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
.welcome-time {
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  padding: 8px 16px;
  border-radius: 8px;
  white-space: nowrap;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.25s ease;
  cursor: default;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  }
}

.stat-card-inner {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20px;
}

.stat-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-blue .stat-icon-box { background: #e8f0fe; color: #2563eb; }
.card-green .stat-icon-box { background: #e6f7e6; color: #16a34a; }
.card-orange .stat-icon-box { background: #fff7ed; color: #ea580c; }
.card-purple .stat-icon-box { background: #f3e8ff; color: #9333ea; }
.card-teal .stat-icon-box { background: #e6fffa; color: #0d9488; }
.card-pink .stat-icon-box { background: #fce7f3; color: #db2777; }

.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 4px;
}
.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}
.stat-unit {
  font-size: 13px;
  color: #909399;
}
.stat-sub {
  font-size: 12px;
  color: #b0b3bb;
}

.stat-card-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.06;
  background: currentColor;
}
.card-blue .stat-card-bg { background: #2563eb; }
.card-green .stat-card-bg { background: #16a34a; }
.card-orange .stat-card-bg { background: #ea580c; }
.card-purple .stat-card-bg { background: #9333ea; }
.card-teal .stat-card-bg { background: #0d9488; }
.card-pink .stat-card-bg { background: #db2777; }

/* 区块标题 */
.dashboard-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.section-title {
  margin: 0 0 20px;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 16px;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s ease;
  &:hover {
    background: #e8f0fe;
    color: #2563eb;
  }
}

/* 平台概况 */
.platform-overview {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.overview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
  strong {
    color: #1a1a2e;
  }
}
.overview-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-blue { background: #2563eb; }
.dot-green { background: #16a34a; }
.dot-orange { background: #ea580c; }
.dot-purple { background: #9333ea; }

/* 响应式 */
@media (max-width: 992px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .stat-cards { grid-template-columns: 1fr; }
  .dashboard-welcome { flex-direction: column; gap: 12px; }
  .platform-overview { flex-direction: column; }
}
</style>
