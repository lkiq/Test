<template>
  <div class="interview-page">
    <!-- 页面标题区 -->
    <div class="iv-header">
      <div class="iv-header-left">
        <h2 class="iv-title">
          <el-icon :size="22"><Calendar /></el-icon>
          面试日程
        </h2>
        <span class="iv-subtitle">安排和跟踪候选人面试</span>
      </div>
      <div class="iv-header-right">
        <el-button-group>
          <el-button
            v-for="v in views"
            :key="v.key"
            :type="currentView === v.key ? 'primary' : 'default'"
            size="default"
            round
            @click="currentView = v.key"
          >
            <el-icon style="margin-right:4px"><component :is="v.icon" /></el-icon>
            {{ v.label }}
          </el-button>
        </el-button-group>
        <el-button type="primary" size="default" round @click="dialogVisible = true">
          <el-icon style="margin-right:6px"><Plus /></el-icon>安排面试
        </el-button>
      </div>
    </div>

    <!-- 面试统计卡片 -->
    <div class="iv-stats">
      <div class="iv-stat-card today">
        <div class="iv-stat-icon"><el-icon :size="20"><Sunny /></el-icon></div>
        <div class="iv-stat-body">
          <span class="iv-stat-num">{{ todayInterviews }}</span>
          <span class="iv-stat-label">今日面试</span>
        </div>
      </div>
      <div class="iv-stat-card upcoming">
        <div class="iv-stat-icon"><el-icon :size="20"><Clock /></el-icon></div>
        <div class="iv-stat-body">
          <span class="iv-stat-num">{{ upcomingInterviews }}</span>
          <span class="iv-stat-label">即将进行</span>
        </div>
      </div>
      <div class="iv-stat-card done">
        <div class="iv-stat-icon"><el-icon :size="20"><CircleCheck /></el-icon></div>
        <div class="iv-stat-body">
          <span class="iv-stat-num">{{ completedInterviews }}</span>
          <span class="iv-stat-label">已完成</span>
        </div>
      </div>
    </div>

    <!-- 面试列表 -->
    <div class="iv-list-container">
      <div v-if="filteredInterviews.length > 0" class="iv-list">
        <div
          v-for="item in filteredInterviews"
          :key="item.id"
          class="iv-card"
          :class="{ 'is-today': isToday(item.interviewTime), 'is-done': item.status === 'COMPLETED' }"
        >
          <!-- 时间线 -->
          <div class="ivc-timeline">
            <div class="ivc-timeline-dot" :class="item.status"></div>
            <div class="ivc-timeline-line"></div>
          </div>

          <!-- 内容 -->
          <div class="ivc-body">
            <div class="ivc-time-badge" :class="{ today: isToday(item.interviewTime) }">
              <el-icon :size="14"><Clock /></el-icon>
              {{ formatTime(item.interviewTime) }}
              <el-tag v-if="isToday(item.interviewTime)" size="small" type="danger" effect="dark" class="today-tag">今天</el-tag>
            </div>

            <div class="ivc-info">
              <div class="ivc-avatar">
                <el-avatar :size="44" :style="{ background: avatarColor(item.candidateName) }">
                  {{ (item.candidateName || '?')[0] }}
                </el-avatar>
              </div>
              <div class="ivc-detail">
                <h4 class="ivc-name">{{ item.candidateName }}</h4>
                <p class="ivc-position">
                  <el-icon :size="12"><Briefcase /></el-icon>
                  {{ item.positionTitle }}
                </p>
              </div>
            </div>

            <div class="ivc-meta">
              <div class="ivc-meta-item">
                <el-icon :size="14"><Timer /></el-icon>
                <span>{{ item.round || '技术面' }}</span>
              </div>
              <div class="ivc-meta-item">
                <el-icon :size="14"><User /></el-icon>
                <span>面试官：{{ item.interviewer || 'HR' }}</span>
              </div>
              <div class="ivc-meta-item" v-if="item.location">
                <el-icon :size="14"><Location /></el-icon>
                <span>{{ item.location }}</span>
              </div>
            </div>

            <div class="ivc-actions">
              <el-button
                v-if="item.status === 'SCHEDULED'"
                type="primary"
                size="small"
                round
                @click="updateStatus(item, 'COMPLETED')"
              >
                <el-icon style="margin-right:4px"><Check /></el-icon>标记完成
              </el-button>
              <el-button
                v-if="item.status === 'SCHEDULED'"
                size="small"
                round
                plain
                @click="openReschedule(item)"
              >
                修改时间
              </el-button>
              <el-button
                v-if="item.status === 'SCHEDULED'"
                size="small"
                round
                plain
                type="danger"
                @click="updateStatus(item, 'CANCELLED')"
              >
                取消
              </el-button>
              <el-button
                v-if="item.status === 'COMPLETED'"
                type="success"
                size="small"
                round
                plain
              >
                <el-icon style="margin-right:4px"><CircleCheck /></el-icon>已完成
              </el-button>
              <el-button
                v-if="item.candidateId"
                size="small"
                round
                @click="$router.push('/chat')"
              >
                联系候选人
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="iv-empty">
        <div class="iv-empty-icon">
          <el-icon :size="64" color="#c0c4cc"><Calendar /></el-icon>
        </div>
        <h3>暂无{suffix}面试安排</h3>
        <p>点击"安排面试"按钮为候选人发起面试邀约</p>
        <el-button type="primary" round @click="dialogVisible = true">安排面试</el-button>
      </div>
    </div>

    <!-- 安排面试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="安排面试"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="newInterview" label-width="90px" label-position="right">
        <el-form-item label="候选人" required>
          <el-input v-model="newInterview.candidateName" placeholder="请输入候选人姓名" />
        </el-form-item>
        <el-form-item label="应聘岗位">
          <el-input v-model="newInterview.positionTitle" placeholder="岗位名称" />
        </el-form-item>
        <el-form-item label="面试时间" required>
          <el-date-picker
            v-model="newInterview.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="面试轮次">
          <el-select v-model="newInterview.round" style="width:100%">
            <el-option label="技术一面" value="技术一面" />
            <el-option label="技术二面" value="技术二面" />
            <el-option label="HR面" value="HR面" />
            <el-option label="综合面" value="综合面" />
            <el-option label="终面" value="终面" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试官">
          <el-input v-model="newInterview.interviewer" placeholder="面试官姓名" />
        </el-form-item>
        <el-form-item label="面试方式">
          <el-radio-group v-model="newInterview.mode">
            <el-radio value="线上">线上</el-radio>
            <el-radio value="线下">线下</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地点/链接">
          <el-input v-model="newInterview.location" :placeholder="newInterview.mode === '线上' ? '视频会议链接' : '面试地点'" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="newInterview.remark" type="textarea" :rows="3" placeholder="面试备注信息..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="scheduleInterview">确认安排</el-button>
      </template>
    </el-dialog>

    <!-- 修改时间对话框 -->
    <el-dialog v-model="rescheduleVisible" title="修改面试时间" width="420px">
      <el-date-picker
        v-model="rescheduleTime"
        type="datetime"
        placeholder="选择新的面试时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width:100%"
      />
      <template #footer>
        <el-button @click="rescheduleVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReschedule">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Plus, Sunny, Clock, CircleCheck, Check, Timer,
  User, Location, Briefcase
} from '@element-plus/icons-vue'

const views = [
  { key: 'upcoming', label: '即将进行', icon: 'Clock' },
  { key: 'all', label: '全部日程', icon: 'Calendar' },
  { key: 'done', label: '已完成', icon: 'CircleCheck' },
]
const currentView = ref('upcoming')

const dialogVisible = ref(false)
const rescheduleVisible = ref(false)
const rescheduleItem = ref<any>(null)
const rescheduleTime = ref('')

const newInterview = ref({
  candidateName: '', positionTitle: '', interviewTime: '',
  round: '技术一面', interviewer: 'HR', mode: '线上',
  location: '', remark: ''
})

const interviews = ref([
  {
    id: 1, candidateName: '张明远', candidateId: 1,
    positionTitle: 'Java后端开发工程师', interviewTime: '2026-06-29 10:00:00',
    round: '技术一面', interviewer: '王工', mode: '线上',
    location: '腾讯会议：888-123-456', status: 'SCHEDULED'
  },
  {
    id: 2, candidateName: '李晓婷', candidateId: 2,
    positionTitle: '前端开发工程师', interviewTime: '2026-06-29 14:30:00',
    round: '技术一面', interviewer: '赵工', mode: '线上',
    location: '腾讯会议：888-789-012', status: 'SCHEDULED'
  },
  {
    id: 3, candidateName: '王子涵', candidateId: 3,
    positionTitle: '算法工程师', interviewTime: '2026-06-30 09:30:00',
    round: '技术二面', interviewer: '李工', mode: '线下',
    location: '公司3楼面试室B', status: 'SCHEDULED'
  },
  {
    id: 4, candidateName: '陈思远', candidateId: 4,
    positionTitle: '产品经理', interviewTime: '2026-06-28 15:00:00',
    round: 'HR面', interviewer: 'HR', mode: '线上',
    location: '腾讯会议：888-345-678', status: 'COMPLETED'
  },
  {
    id: 5, candidateName: '刘雨晴', candidateId: 5,
    positionTitle: '测试开发工程师', interviewTime: '2026-06-28 10:00:00',
    round: '技术一面', interviewer: '孙工', mode: '线下',
    location: '公司5楼会议室A', status: 'COMPLETED'
  },
])

const avatarColors = ['#165DFF', '#722ED1', '#00B42A', '#FF7D00', '#F53F3F', '#7B61FF']

const todayInterviews = computed(() => interviews.value.filter(i => isToday(i.interviewTime) && i.status === 'SCHEDULED').length)
const upcomingInterviews = computed(() => interviews.value.filter(i => i.status === 'SCHEDULED').length)
const completedInterviews = computed(() => interviews.value.filter(i => i.status === 'COMPLETED').length)

const filteredInterviews = computed(() => {
  let list = interviews.value
  if (currentView.value === 'upcoming') list = list.filter(i => i.status === 'SCHEDULED')
  else if (currentView.value === 'done') list = list.filter(i => i.status === 'COMPLETED')
  return list.sort((a, b) => new Date(a.interviewTime).getTime() - new Date(b.interviewTime).getTime())
})

function isToday(timeStr: string): boolean {
  const d = new Date(timeStr)
  const now = new Date()
  return d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return d.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }) +
    ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function avatarColor(name: string): string {
  if (!name) return avatarColors[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

function openReschedule(item: any) {
  rescheduleItem.value = item
  rescheduleTime.value = item.interviewTime
  rescheduleVisible.value = true
}

function confirmReschedule() {
  if (rescheduleItem.value && rescheduleTime.value) {
    rescheduleItem.value.interviewTime = rescheduleTime.value
    ElMessage.success('面试时间已更新')
  }
  rescheduleVisible.value = false
  rescheduleItem.value = null
}

function updateStatus(item: any, status: string) {
  const text = status === 'COMPLETED' ? '标记为已完成' : '取消面试'
  ElMessageBox.confirm(`确定要${text}吗？`, '确认', { type: 'warning' })
    .then(() => {
      item.status = status
      ElMessage.success(status === 'COMPLETED' ? '面试已完成' : '面试已取消')
    })
    .catch(() => {})
}

function scheduleInterview() {
  if (!newInterview.value.candidateName || !newInterview.value.interviewTime) {
    ElMessage.warning('请填写候选人姓名和面试时间')
    return
  }
  interviews.value.unshift({
    id: Date.now(),
    candidateName: newInterview.value.candidateName,
    candidateId: 0,
    positionTitle: newInterview.value.positionTitle || '未指定岗位',
    interviewTime: newInterview.value.interviewTime,
    round: newInterview.value.round,
    interviewer: newInterview.value.interviewer,
    mode: newInterview.value.mode,
    location: newInterview.value.location,
    status: 'SCHEDULED'
  })
  ElMessage.success('面试已安排')
  dialogVisible.value = false
  newInterview.value = {
    candidateName: '', positionTitle: '', interviewTime: '',
    round: '技术一面', interviewer: 'HR', mode: '线上',
    location: '', remark: ''
  }
}
</script>

<style scoped>
.interview-page { max-width: 980px; margin: 0 auto; padding-bottom: 32px; }

/* ===== 页头 ===== */
.iv-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; flex-wrap: wrap; gap: 12px;
}
.iv-header-left { display: flex; align-items: baseline; gap: 12px; }
.iv-title { font-size: 20px; font-weight: 700; color: #1d2129; margin: 0; display: flex; align-items: center; gap: 8px; }
.iv-subtitle { font-size: 13px; color: #86909c; }
.iv-header-right { display: flex; align-items: center; gap: 12px; }

/* ===== 统计 ===== */
.iv-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 24px; }
.iv-stat-card {
  background: #fff; border-radius: 12px; padding: 16px 20px;
  border: 1px solid #f0f0f0; display: flex; align-items: center; gap: 14px;
  transition: transform 0.2s;
}
.iv-stat-card:hover { transform: translateY(-2px); }
.iv-stat-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.iv-stat-card.today .iv-stat-icon { background: #fff7e8; color: #FF7D00; }
.iv-stat-card.upcoming .iv-stat-icon { background: #e8f1ff; color: #165DFF; }
.iv-stat-card.done .iv-stat-icon { background: #e8ffea; color: #00B42A; }
.iv-stat-body { display: flex; flex-direction: column; }
.iv-stat-num { font-size: 24px; font-weight: 700; color: #1d2129; line-height: 1.2; }
.iv-stat-label { font-size: 12px; color: #86909c; }

/* ===== 面试列表 ===== */
.iv-list { display: flex; flex-direction: column; gap: 0; }

.iv-card {
  display: flex; gap: 16px; padding: 0 0 20px 0;
  position: relative;
}
.iv-card.is-done { opacity: 0.65; }

/* 时间线 */
.ivc-timeline { display: flex; flex-direction: column; align-items: center; width: 32px; flex-shrink: 0; padding-top: 6px; }
.ivc-timeline-dot {
  width: 12px; height: 12px; border-radius: 50%;
  border: 3px solid #fff; flex-shrink: 0;
  box-shadow: 0 0 0 2px #e5e6eb;
}
.ivc-timeline-dot.SCHEDULED { background: #165DFF; box-shadow: 0 0 0 2px #165DFF; }
.ivc-timeline-dot.COMPLETED { background: #00B42A; box-shadow: 0 0 0 2px #00B42A; }
.ivc-timeline-dot.CANCELLED { background: #F53F3F; box-shadow: 0 0 0 2px #F53F3F; }
.ivc-timeline-line {
  flex: 1; width: 2px; background: #e5e6eb; margin-top: 6px;
}

/* 卡片内容 */
.ivc-body {
  flex: 1; background: #fff; border-radius: 14px; padding: 18px 24px;
  border: 1px solid #f0f0f0; transition: all 0.2s;
}
.iv-card:not(.is-done) .ivc-body:hover {
  border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

.ivc-time-badge {
  font-size: 13px; font-weight: 600; color: #4e5969;
  display: flex; align-items: center; gap: 6px; margin-bottom: 14px;
}
.ivc-time-badge.today { color: #165DFF; }
.today-tag { margin-left: 8px; font-size: 10px; }

.ivc-info { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; }
.ivc-detail { flex: 1; }
.ivc-name { font-size: 16px; font-weight: 700; color: #1d2129; margin: 0 0 4px; }
.ivc-position { font-size: 13px; color: #4e5969; margin: 0; display: flex; align-items: center; gap: 4px; }

.ivc-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 14px; }
.ivc-meta-item {
  font-size: 13px; color: #4e5969; display: flex; align-items: center; gap: 4px;
  padding: 4px 12px; background: #f7f8fa; border-radius: 8px;
}

.ivc-actions { display: flex; gap: 8px; flex-wrap: wrap; }

/* 空状态 */
.iv-empty {
  text-align: center; padding: 80px 40px;
  background: #fff; border-radius: 14px; border: 1px solid #f0f0f0;
}
.iv-empty-icon { margin-bottom: 16px; }
.iv-empty h3 { font-size: 17px; font-weight: 600; color: #1d2129; margin: 0 0 8px; }
.iv-empty p { font-size: 14px; color: #86909c; margin: 0 0 20px; }

@media (max-width: 768px) {
  .iv-stats { grid-template-columns: repeat(2, 1fr); }
  .iv-header-right { flex-direction: column; width: 100%; }
  .iv-header-right .el-button-group { width: 100%; }
  .iv-header-right .el-button { flex: 1; }
  .ivc-meta { flex-direction: column; gap: 6px; }
}
</style>
