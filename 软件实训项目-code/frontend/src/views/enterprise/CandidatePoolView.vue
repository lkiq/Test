<template>
  <div class="candidate-pool">
    <!-- 页面标题区 -->
    <div class="cp-header">
      <div class="cp-header-left">
        <h2 class="cp-title">
          <el-icon :size="22"><UserFilled /></el-icon>
          候选人库
        </h2>
        <span class="cp-subtitle">管理和筛选所有岗位的投递候选人</span>
      </div>
      <el-button type="primary" round @click="$router.push('/enterprise/recommend')">
        <el-icon style="margin-right:6px"><MagicStick /></el-icon>AI 推荐人才
      </el-button>
    </div>

    <!-- 选择岗位 + 状态概览 -->
    <div class="cp-toolbar">
      <div class="cp-job-selector">
        <span class="cp-label">当前岗位</span>
        <el-select
          v-model="selectedJobId"
          placeholder="请选择要查看的岗位"
          size="large"
          class="cp-job-select"
          @change="fetchCandidates"
          :loading="loadingJobs"
          popper-class="cp-popper"
        >
          <el-option
            v-for="job in myJobs"
            :key="job.id"
            :label="job.title"
            :value="job.id"
          >
            <div class="cp-option">
              <span class="cp-option-title">{{ job.title }}</span>
              <span class="cp-option-extra">{{ job.city }} · {{ job.salaryRange }}</span>
            </div>
          </el-option>
        </el-select>
      </div>

      <div class="cp-stats-row" v-if="selectedJobId && !loading">
        <div class="cp-stat-badge" :class="{ active: filterStatus === '' }" @click="filterStatus = ''">
          <span class="cp-stat-num">{{ candidates.length }}</span>
          <span class="cp-stat-text">全部</span>
        </div>
        <div class="cp-stat-badge pending" :class="{ active: filterStatus === 'PENDING' }" @click="filterStatus = 'PENDING'">
          <span class="cp-stat-num">{{ statusCount('PENDING') }}</span>
          <span class="cp-stat-text">待筛选</span>
        </div>
        <div class="cp-stat-badge interview" :class="{ active: filterStatus === 'INTERVIEW' }" @click="filterStatus = 'INTERVIEW'">
          <span class="cp-stat-num">{{ statusCount('INTERVIEW') }}</span>
          <span class="cp-stat-text">面试中</span>
        </div>
        <div class="cp-stat-badge reject" :class="{ active: filterStatus === 'REJECT' }" @click="filterStatus = 'REJECT'">
          <span class="cp-stat-num">{{ statusCount('REJECT') }}</span>
          <span class="cp-stat-text">不合适</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="cp-search-bar" v-if="selectedJobId">
      <el-input
        v-model="keyword"
        placeholder="搜索候选人姓名、学校、专业..."
        :prefix-icon="Search"
        clearable
        size="large"
        class="cp-search-input"
      />
    </div>

    <!-- 候选人列表 -->
    <div class="cp-body" v-if="selectedJobId">
      <!-- 加载状态 -->
      <div v-if="loading" class="cp-loading">
        <div class="cp-skeleton" v-for="i in 3" :key="i">
          <el-skeleton :rows="3" animated />
        </div>
      </div>

      <!-- 候选人卡片列表 -->
      <div v-else-if="filteredCandidates.length > 0" class="cp-candidate-list">
        <div
          v-for="cand in filteredCandidates"
          :key="cand.id"
          class="candidate-card"
          :class="{ 'is-interview': cand.status === 'INTERVIEW', 'is-reject': cand.status === 'REJECT' }"
        >
          <div class="cc-main">
            <!-- 头像 -->
            <div class="cc-avatar">
              <el-avatar :size="48" :style="{ background: avatarColor(cand.realName) }">
                {{ (cand.realName || '?')[0] }}
              </el-avatar>
              <div class="cc-online-dot" v-if="cand.status === 'PENDING'"></div>
            </div>

            <!-- 信息 -->
            <div class="cc-info">
              <div class="cc-name-row">
                <span class="cc-name">{{ cand.realName || '未知' }}</span>
                <el-tag
                  :type="statusTagType(cand.status)"
                  size="small"
                  effect="light"
                  class="cc-status-tag"
                >
                  <span class="cc-status-dot" :class="cand.status"></span>
                  {{ statusText(cand.status) }}
                </el-tag>
                <span class="cc-degree">{{ eduText(cand.education) }}</span>
              </div>
              <div class="cc-meta">
                <span class="cc-meta-item">
                  <el-icon :size="12"><School /></el-icon>
                  {{ cand.school || '未知院校' }}
                </span>
                <span class="cc-meta-item">
                  <el-icon :size="12"><Notebook /></el-icon>
                  {{ cand.major || '未知专业' }}
                </span>
                <span class="cc-meta-item">
                  <el-icon :size="12"><Briefcase /></el-icon>
                  应聘：{{ cand.jobTitle || '-' }}
                </span>
              </div>
              <div class="cc-time">
                <el-icon :size="12"><Clock /></el-icon>
                投递时间：{{ formatTime(cand.createdAt) }}
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="cc-actions">
            <el-button
              v-if="cand.resumeId"
              size="small"
              round
              @click="viewResume(cand)"
            >
              <el-icon style="margin-right:4px"><Document /></el-icon>查看简历
            </el-button>
            <el-button
              v-if="cand.status === 'PENDING'"
              type="success"
              size="small"
              round
              @click="updateStatus(cand, 'INTERVIEW')"
            >
              <el-icon style="margin-right:4px"><ChatDotRound /></el-icon>邀约面试
            </el-button>
            <el-button
              v-if="cand.status === 'PENDING'"
              type="warning"
              size="small"
              round
              plain
              @click="updateStatus(cand, 'REJECT')"
            >
              不合适
            </el-button>
            <el-button
              v-if="cand.status === 'INTERVIEW'"
              type="primary"
              size="small"
              round
              plain
              @click="$router.push('/enterprise/interviews')"
            >
              查看面试
            </el-button>
            <el-button
              size="small"
              round
              plain
              type="danger"
              @click="handleDelete(cand)"
            >
              <el-icon style="margin-right:2px"><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="cp-empty">
        <div class="cp-empty-icon">
          <el-icon :size="64" color="#c0c4cc"><User /></el-icon>
        </div>
        <h3>暂无候选人</h3>
        <p v-if="filterStatus || keyword">当前筛选条件下没有找到候选人，试试调整筛选条件</p>
        <p v-else>该岗位暂时没有收到投递，去发布岗位或使用AI推荐匹配人才</p>
        <el-button type="primary" round @click="$router.push('/enterprise/recommend')">
          AI 推荐人才
        </el-button>
      </div>
    </div>

    <!-- 未选择岗位的引导 -->
    <div v-else class="cp-empty">
      <div class="cp-empty-icon">
        <el-icon :size="64" color="#c0c4cc"><DocumentChecked /></el-icon>
      </div>
      <h3>选择岗位查看候选人</h3>
      <p>从上方下拉列表中选择一个已发布的岗位，查看投递该岗位的候选人</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, UserFilled, School, Notebook, Briefcase,
  Clock, Document, ChatDotRound, Delete, MagicStick, User, DocumentChecked
} from '@element-plus/icons-vue'
import { listMyJobs, getJobApplications, updateApplicationStatus, deleteApplication, getResumeDownloadUrl } from '@/api/enterprise'
import axios from 'axios'

const myJobs = ref<any[]>([])
const candidates = ref<any[]>([])
const selectedJobId = ref<number | null>(null)
const keyword = ref('')
const filterStatus = ref('')
const loading = ref(false)
const loadingJobs = ref(false)

const avatarColors = ['#165DFF', '#722ED1', '#00B42A', '#FF7D00', '#F53F3F', '#7B61FF', '#16C8C8']

function avatarColor(name: string): string {
  if (!name) return avatarColors[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

function statusCount(status: string): number {
  return candidates.value.filter(c => c.status === status).length
}

const filteredCandidates = computed(() => {
  return candidates.value.filter(c => {
    if (keyword.value) {
      const kw = keyword.value.toLowerCase()
      const matchName = (c.realName || '').toLowerCase().includes(kw)
      const matchSchool = (c.school || '').toLowerCase().includes(kw)
      const matchMajor = (c.major || '').toLowerCase().includes(kw)
      if (!matchName && !matchSchool && !matchMajor) return false
    }
    if (filterStatus.value && c.status !== filterStatus.value) return false
    return true
  })
})

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天 ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (days === 1) return '昨天'
  if (days < 7) return days + '天前'
  return d.toLocaleDateString('zh-CN')
}

function statusTagType(status: string) {
  const map: Record<string, any> = { PENDING: 'info', INTERVIEW: 'success', REJECT: 'danger', CANCELLED: 'warning' }
  return map[status] || 'info'
}

function statusText(status: string) {
  const map: Record<string, string> = { PENDING: '待筛选', INTERVIEW: '面试中', REJECT: '不合适', CANCELLED: '已取消' }
  return map[status] || status
}

function eduText(edu: string) {
  const map: Record<string, string> = {
    HIGH_SCHOOL: '高中', JUNIOR: '大专', BACHELOR: '本科',
    MASTER: '硕士', PHD: '博士'
  }
  return map[edu] || edu || '-'
}

async function fetchMyJobs() {
  loadingJobs.value = true
  try {
    const res: any = await listMyJobs()
    myJobs.value = (res.data || []).filter((j: any) => j.publishStatus === 1)
  } finally {
    loadingJobs.value = false
  }
}

async function fetchCandidates() {
  if (!selectedJobId.value) return
  loading.value = true
  filterStatus.value = ''
  keyword.value = ''
  try {
    const res: any = await getJobApplications(selectedJobId.value)
    candidates.value = res.data || []
  } catch {
    candidates.value = []
  } finally {
    loading.value = false
  }
}

async function updateStatus(row: any, newStatus: string) {
  const actionText = newStatus === 'INTERVIEW' ? '邀约面试' : '标记为不合适'
  try {
    await ElMessageBox.confirm(
      `确定将 <strong>${row.realName || '该候选人'}</strong> ${actionText}吗？`,
      '确认操作',
      { type: 'warning', dangerouslyUseHTMLString: true }
    )
    const res: any = await updateApplicationStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`已${actionText}`)
      await fetchCandidates()
    }
  } catch { /* 取消 */ }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定删除 <strong>${row.realName || '该候选人'}</strong> 的投递记录吗？`,
      '确认删除',
      { type: 'warning', dangerouslyUseHTMLString: true }
    )
    const res: any = await deleteApplication(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      await fetchCandidates()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch { /* 取消 */ }
}

async function viewResume(row: any) {
  if (!row.resumeId) { ElMessage.warning('未上传简历'); return }
  try {
    const token = localStorage.getItem('token')
    const url = getResumeDownloadUrl(row.id)
    const resp = await axios.get(url, { responseType: 'blob', headers: { Authorization: `Bearer ${token}` } })
    const blob = new Blob([resp.data], { type: String(resp.headers['content-type'] || 'application/pdf') })
    window.open(URL.createObjectURL(blob), '_blank')
  } catch {
    ElMessage.error('查看失败')
  }
}

onMounted(() => fetchMyJobs())
</script>

<style scoped>
.candidate-pool { max-width: 1100px; margin: 0 auto; padding-bottom: 32px; }

/* ===== 页头 ===== */
.cp-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.cp-header-left { display: flex; align-items: baseline; gap: 12px; }
.cp-title { font-size: 20px; font-weight: 700; color: #1d2129; margin: 0; display: flex; align-items: center; gap: 8px; }
.cp-subtitle { font-size: 13px; color: #86909c; }

/* ===== 工具栏 ===== */
.cp-toolbar {
  background: #fff; border-radius: 14px; padding: 20px 24px;
  border: 1px solid #f0f0f0; margin-bottom: 16px;
  display: flex; align-items: center; justify-content: space-between; gap: 24px; flex-wrap: wrap;
}
.cp-job-selector { display: flex; align-items: center; gap: 12px; }
.cp-label { font-size: 13px; font-weight: 600; color: #4e5969; white-space: nowrap; }
.cp-job-select { width: 320px; }
.cp-option { display: flex; justify-content: space-between; width: 100%; }
.cp-option-title { font-weight: 500; color: #1d2129; flex: 1; }
.cp-option-extra { font-size: 12px; color: #86909c; margin-left: 12px; flex-shrink: 0; }

/* 状态统计 */
.cp-stats-row { display: flex; gap: 8px; }
.cp-stat-badge {
  display: flex; flex-direction: column; align-items: center;
  padding: 6px 16px; border-radius: 10px; cursor: pointer;
  transition: all 0.2s; background: #f7f8fa; min-width: 64px;
}
.cp-stat-badge:hover { background: #e8f1ff; }
.cp-stat-badge.active { background: #e8f1ff; box-shadow: 0 0 0 2px #165DFF; }
.cp-stat-badge.pending.active { background: #fff7e8; box-shadow: 0 0 0 2px #FF7D00; }
.cp-stat-badge.interview.active { background: #e8ffea; box-shadow: 0 0 0 2px #00B42A; }
.cp-stat-badge.reject.active { background: #ffecec; box-shadow: 0 0 0 2px #F53F3F; }
.cp-stat-num { font-size: 20px; font-weight: 700; color: #1d2129; line-height: 1.2; }
.cp-stat-text { font-size: 11px; color: #86909c; margin-top: 2px; }

/* ===== 搜索 ===== */
.cp-search-bar { margin-bottom: 16px; }
.cp-search-input :deep(.el-input__wrapper) { border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }

/* ===== 骨架屏 ===== */
.cp-loading { display: flex; flex-direction: column; gap: 12px; }
.cp-skeleton { background: #fff; border-radius: 12px; padding: 20px 24px; border: 1px solid #f0f0f0; }

/* ===== 候选人卡片列表 ===== */
.cp-candidate-list { display: flex; flex-direction: column; gap: 8px; }

.candidate-card {
  background: #fff; border-radius: 12px; padding: 18px 24px;
  border: 1px solid #f0f0f0; display: flex; align-items: center;
  justify-content: space-between; transition: all 0.2s;
}
.candidate-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(0,0,0,0.06); transform: translateY(-1px); }
.candidate-card.is-interview { border-left: 3px solid #00B42A; }
.candidate-card.is-reject { border-left: 3px solid #e5e6eb; opacity: 0.7; }

.cc-main { display: flex; align-items: center; gap: 16px; flex: 1; min-width: 0; }

.cc-avatar { position: relative; flex-shrink: 0; }
.cc-online-dot {
  position: absolute; bottom: 2px; right: 2px;
  width: 10px; height: 10px; border-radius: 50%;
  background: #00B42A; border: 2px solid #fff;
}

.cc-info { flex: 1; min-width: 0; }
.cc-name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.cc-name { font-size: 16px; font-weight: 600; color: #1d2129; }
.cc-status-tag { font-size: 11px; }
.cc-status-dot {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; vertical-align: middle;
}
.cc-status-dot.PENDING { background: #165DFF; }
.cc-status-dot.INTERVIEW { background: #00B42A; }
.cc-status-dot.REJECT { background: #F53F3F; }

.cc-degree {
  font-size: 12px; padding: 2px 8px; border-radius: 4px;
  background: #f2f3f5; color: #4e5969;
}

.cc-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 4px; }
.cc-meta-item {
  font-size: 13px; color: #4e5969; display: flex; align-items: center; gap: 4px;
}
.cc-time { font-size: 12px; color: #86909c; display: flex; align-items: center; gap: 4px; }

.cc-actions { display: flex; gap: 6px; flex-shrink: 0; flex-wrap: wrap; justify-content: flex-end; }

/* ===== 空状态 ===== */
.cp-empty {
  text-align: center; padding: 80px 40px;
  background: #fff; border-radius: 14px; border: 1px solid #f0f0f0;
}
.cp-empty-icon { margin-bottom: 16px; }
.cp-empty h3 { font-size: 17px; font-weight: 600; color: #1d2129; margin: 0 0 8px; }
.cp-empty p { font-size: 14px; color: #86909c; margin: 0 0 20px; }

/* 响应式 */
@media (max-width: 768px) {
  .cp-toolbar { flex-direction: column; align-items: flex-start; }
  .cp-job-select { width: 100% !important; }
  .candidate-card { flex-direction: column; align-items: flex-start; gap: 12px; }
  .cc-actions { width: 100%; justify-content: flex-start; }
}
</style>
