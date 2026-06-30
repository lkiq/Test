<template>
  <div class="job-card" :class="{ list: mode === 'list', hovered }" @click="goDetail" @mouseenter="hovered = true" @mouseleave="hovered = false">
    <!-- 顶部高亮条 -->
    <div v-if="job.matchScore && job.matchScore >= 80" class="job-highlight-bar"></div>
    <div class="job-main">
      <div class="company-logo">
        <el-avatar :size="52" shape="square" class="company-avatar-img" :style="{ background: getLogoBg(job.companyName) }">
          <span class="logo-letters">{{ getLogoText(job.companyName) }}</span>
        </el-avatar>
      </div>
      <div class="job-info">
        <div class="title-row">
          <h3 class="job-title">{{ job.title }}</h3>
          <span class="job-salary">{{ job.salaryRange }}</span>
        </div>
        <div class="company-row">
          <span class="company-name">{{ job.companyName }}</span>
          <span class="dot">·</span>
          <span>{{ job.companyIndustry }}</span>
          <span class="dot">·</span>
          <span>{{ job.companyScale }}</span>
          <el-tag v-if="job.matchScore && job.matchScore >= 85" size="small" type="danger" effect="dark" class="high-match-tag">高匹配</el-tag>
        </div>
        <div class="tag-row">
          <el-tag size="small" effect="plain" class="loc-tag">
            <el-icon><Location /></el-icon> {{ job.city }}
          </el-tag>
          <el-tag size="small" effect="plain" type="info">{{ job.experience }}</el-tag>
          <el-tag size="small" effect="plain" type="info">{{ job.education }}</el-tag>
          <el-tag size="small" effect="dark" :type="job.jobType === '实习' ? 'success' : job.jobType === '校招' ? 'primary' : 'warning'">{{ job.jobType }}</el-tag>
          <el-tag v-if="job.matchScore" size="small" type="success" effect="dark">匹配 {{ Math.round(job.matchScore) }}%</el-tag>
        </div>
        <div v-if="job.welfare.length" class="welfare-row">
          <span v-for="(w, i) in job.welfare.slice(0, 4)" :key="i" class="welfare-item">{{ w }}</span>
        </div>
      </div>
    </div>
    <div class="job-footer">
      <div class="hr-info">
        <el-avatar :size="22" :icon="UserFilled" style="background: #e5e7eb; color: #6b7280;" />
        <span class="hr-name">{{ job.hrName }}</span>
        <span class="hr-title">{{ job.hrTitle }}</span>
        <el-tag size="small" type="success" effect="light" class="reply-tag">回复率 {{ job.replyRate }}%</el-tag>
      </div>
      <div class="time">{{ job.publishTimeLabel }}</div>
    </div>
    <div class="job-actions" @click.stop>
      <el-button
        :type="isFavorite ? 'danger' : 'default'"
        :icon="isFavorite ? StarFilled : Star"
        size="small"
        :loading="jobStore.favoriteLoading"
        @click="toggleFav"
      >
        {{ isFavorite ? '已收藏' : '收藏' }}
      </el-button>
      <el-button type="primary" size="small" :icon="Position" :disabled="hasApplied" @click="apply">
        {{ hasApplied ? '已投递' : '投递简历' }}
      </el-button>
      <el-button type="success" size="small" plain :icon="ChatDotRound" @click="chat">聊一聊</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, UserFilled, Star, StarFilled, Position, ChatDotRound } from '@element-plus/icons-vue'
import { useJobStore } from '@/stores/job'
import type { EnrichedJob } from '@/utils/jobEnrich'

const props = defineProps<{
  job: EnrichedJob
  mode?: 'card' | 'list'
}>()

const router = useRouter()
const jobStore = useJobStore()
const hovered = ref(false)

const isFavorite = computed(() => jobStore.isFavorite(props.job.id))
const hasApplied = computed(() => jobStore.hasApplied(props.job.id))

const logoColors = [
  'linear-gradient(135deg, #2563eb, #1d4ed8)',
  'linear-gradient(135deg, #10b981, #059669)',
  'linear-gradient(135deg, #f59e0b, #d97706)',
  'linear-gradient(135deg, #ef4444, #dc2626)',
  'linear-gradient(135deg, #8b5cf6, #7c3aed)',
  'linear-gradient(135deg, #06b6d4, #0891b2)',
  'linear-gradient(135deg, #ec4899, #db2777)',
  'linear-gradient(135deg, #14b8a6, #0d9488)',
  'linear-gradient(135deg, #f97316, #ea580c)',
  'linear-gradient(135deg, #6366f1, #4f46e5)',
]

function getLogoBg(name: string): string {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return logoColors[Math.abs(hash) % logoColors.length]
}

function getLogoText(name: string): string {
  return name.length >= 2 ? name.slice(0, 2) : name.slice(0, 1)
}

function goDetail() {
  router.push(`/student/job/${props.job.id}`)
}

function toggleFav() {
  jobStore.toggleFavorite(props.job.id)
}

async function apply() {
  if (jobStore.hasApplied(props.job.id)) {
    ElMessage.warning('您已投递过该岗位')
    return
  }
  const ok = await jobStore.applyJob(props.job.id)
  if (ok) {
    ElMessage.success('简历投递成功')
  } else {
    ElMessage.error('投递失败，请稍后重试')
  }
}

function chat() {
  // 跳转到聊天页，附带岗位信息
  router.push({
    path: '/chat',
    query: {
      hrId: props.job.hrUserId,
      jobId: props.job.id,
      jobTitle: props.job.title,
      enterpriseName: props.job.companyName
    }
  })
}
</script>

<style scoped lang="scss">
.job-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 22px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;

  &:hover, &.hovered {
    box-shadow: var(--shadow-md);
    border-color: var(--primary-color);
    transform: translateY(-3px);
  }
}
.job-highlight-bar {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, #f56c6c, #f59e0b, #10b981);
}
.job-main {
  display: flex;
  gap: 16px;
}
.company-logo {
  flex-shrink: 0;
  padding-top: 2px;
}
.company-avatar-img {
  font-weight: 700;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.logo-letters {
  color: #fff;
  font-size: 16px;
}
.job-info {
  flex: 1;
  min-width: 0;
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.job-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  line-height: 1.3;
}
.job-salary {
  font-size: 17px;
  font-weight: 700;
  color: #f56c6c;
  white-space: nowrap;
  background: #fef2f2;
  padding: 2px 10px;
  border-radius: 6px;
}
.company-row {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  .company-name { font-weight: 600; color: var(--text-primary); }
  .dot { color: var(--text-tertiary); }
}
.high-match-tag {
  margin-left: 4px;
  animation: matchPulse 2s ease-in-out infinite;
}
@keyframes matchPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
.tag-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.loc-tag { display: inline-flex; align-items: center; gap: 4px; }
.welfare-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.welfare-item {
  font-size: 11px;
  color: #6b7280;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  padding: 3px 10px;
  border-radius: 10px;
}
.job-footer {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--divider-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hr-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.hr-name { color: var(--text-primary); font-weight: 500; }
.reply-tag { margin-left: 4px; }
.time { font-size: 12px; color: var(--text-tertiary); }
.job-actions {
  margin-top: 14px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .job-card { padding: 16px; }
  .title-row { flex-direction: column; align-items: flex-start; gap: 6px; }
  .job-actions { flex-wrap: wrap; }
}
</style>
