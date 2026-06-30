<template>
  <div v-if="loading" class="page-card">
    <LoadingSkeleton type="detail" />
  </div>
  <ErrorRetry v-else-if="error" :message="error" :retry="fetchJob" />
  <div v-else-if="job" class="job-detail">
    <!-- 岗位头部 -->
    <div class="page-card detail-header">
      <div class="header-top">
        <el-button :icon="ArrowLeft" text @click="$router.back()">返回</el-button>
      </div>
      <div class="header-main">
        <div class="company-logo-large">
          <el-avatar :size="64" shape="square" style="background: var(--primary-light); color: var(--primary-color); font-size: 24px; font-weight: 700;">
            {{ job.companyName.slice(0, 1) }}
          </el-avatar>
        </div>
        <div class="header-info">
          <h1 class="detail-title">{{ job.title }}</h1>
          <div class="detail-meta">
            <span class="salary">{{ job.salaryRange }}</span>
            <span class="dot">·</span>
            <span>{{ job.city }}</span>
            <span class="dot">·</span>
            <span>{{ job.experience }}</span>
            <span class="dot">·</span>
            <span>{{ job.education }}</span>
            <span class="dot">·</span>
            <span>{{ job.jobType }}</span>
          </div>
          <div class="tag-row" style="margin-top: 10px;">
            <el-tag v-for="(w, i) in job.welfare" :key="i" type="info" effect="plain" size="small">{{ w }}</el-tag>
          </div>
        </div>
        <div class="header-actions">
          <el-button :type="isFavorite ? 'danger' : 'default'" :icon="isFavorite ? StarFilled : Star" size="large" :loading="jobStore.favoriteLoading" @click="toggleFav">
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="primary" size="large" :icon="Position" :disabled="hasApplied" @click="apply">
            {{ hasApplied ? '已投递' : '投递简历' }}
          </el-button>
          <el-button type="success" size="large" :icon="ChatDotRound" @click="chat">聊一聊</el-button>
          <el-button type="warning" size="large" @click="goToGapAnalysis">
            📊 分析能力差距
          </el-button>
        </div>
      </div>
      <div class="header-bottom">
        <div class="hr-box">
          <el-avatar :size="28" :icon="UserFilled" />
          <span>{{ job.hrName }}</span>
          <span class="hr-title">{{ job.hrTitle }}</span>
          <el-tag size="small" type="success">回复率 {{ job.replyRate }}%</el-tag>
        </div>
        <span class="publish-time">{{ job.publishTimeLabel }}发布</span>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :lg="16" :md="16" :sm="24">
        <div class="page-card">
          <h2 class="section-title">职位描述</h2>
          <p class="job-desc">{{ job.jd }}</p>
          <h2 class="section-title" style="margin-top: 24px;">任职要求</h2>
          <ul class="require-list">
            <li>熟悉 {{ job.direction }} 相关技术栈，具备扎实的专业基础。</li>
            <li>{{ job.experience }} 相关工作经验，{{ job.education }} 及以上学历。</li>
            <li>具备良好的沟通能力、团队协作精神和抗压能力。</li>
            <li>对技术有热情，愿意持续学习成长。</li>
          </ul>
          <div v-if="job.skillTags?.length">
            <h2 class="section-title" style="margin-top: 24px;">技能要求</h2>
            <div class="skill-row">
              <SkillTag v-for="(t, i) in job.skillTags" :key="i" :name="typeof t === 'string' ? t : t.skillName" :status="t.status" />
            </div>
          </div>
        </div>
      </el-col>
      <el-col :lg="8" :md="8" :sm="24">
        <div class="page-card company-card">
          <h2 class="section-title">公司信息</h2>
          <div class="company-name-large">{{ job.companyName }}</div>
          <div class="company-meta">
            <p><el-icon><OfficeBuilding /></el-icon> {{ job.companyIndustry }}</p>
            <p><el-icon><User /></el-icon> {{ job.companyScale }}</p>
            <p><el-icon><Location /></el-icon> {{ job.city }}</p>
          </div>
          <el-button type="primary" plain style="width: 100%; margin-top: 12px;" @click="chat">咨询 HR</el-button>
        </div>
        <div class="page-card">
          <h2 class="section-title">相似推荐</h2>
          <div v-if="similarJobs.length">
            <div v-for="j in similarJobs" :key="j.id" class="similar-job" @click="$router.push(`/student/job/${j.id}`)">
              <div class="similar-title">{{ j.title }}</div>
              <div class="similar-info">
                <span class="similar-salary">{{ j.salaryRange }}</span>
                <span>{{ j.companyName }}</span>
              </div>
            </div>
          </div>
          <EmptyState v-else scene="search" compact title="暂无相似岗位" />
        </div>
      </el-col>
    </el-row>
  </div>
  <EmptyState v-else scene="search" title="岗位不存在或已下架" action-text="返回岗位列表" @action="$router.push('/student/job-matching')" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, StarFilled, Position, ChatDotRound, UserFilled, OfficeBuilding, User, Location } from '@element-plus/icons-vue'
import { useJobStore } from '@/stores/job'
import { getJobDetail } from '@/api/student'
import { enrichJob, type EnrichedJob } from '@/utils/jobEnrich'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import ErrorRetry from '@/components/common/ErrorRetry.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SkillTag from '@/components/common/SkillTag.vue'

const route = useRoute()
const router = useRouter()
const jobStore = useJobStore()

const job = ref<EnrichedJob | null>(null)
const loading = ref(false)
const error = ref('')

const isFavorite = computed(() => job.value ? jobStore.isFavorite(job.value.id) : false)
const hasApplied = computed(() => job.value ? jobStore.hasApplied(job.value.id) : false)
const similarJobs = computed(() => {
  if (!job.value) return []
  return jobStore.jobs
    .filter(j => j.id !== job.value!.id && j.direction === job.value!.direction)
    .slice(0, 4)
})

async function fetchJob() {
  const id = Number(route.params.id)
  if (!id) {
    error.value = '岗位 ID 无效'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res: any = await getJobDetail(id)
    job.value = enrichJob(res.data)
  } catch (e: any) {
    error.value = e?.message || '加载岗位详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function toggleFav() {
  if (!job.value) return
  jobStore.toggleFavorite(job.value.id)
}

async function apply() {
  if (!job.value) return
  if (jobStore.hasApplied(job.value.id)) {
    ElMessage.warning('您已投递过该岗位')
    return
  }
  const ok = await jobStore.applyJob(job.value.id)
  if (ok) {
    ElMessage.success('简历投递成功')
  } else {
    ElMessage.error('投递失败，请稍后重试')
  }
}

function chat() {
  if (!job.value) return
  router.push({
    path: '/chat',
    query: {
      hrId: job.value.hrUserId,
      jobId: job.value.id,
      jobTitle: job.value.title,
      enterpriseName: job.value.companyName
    }
  })
}

function goToGapAnalysis() {
  if (job.value) {
    router.push(`/student/gap-analysis?jobId=${job.value.id}`)
  }
}

onMounted(fetchJob)
</script>

<style scoped lang="scss">
.detail-header {
  padding: 16px 20px 20px;
}
.header-top {
  margin-bottom: 12px;
}
.header-main {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}
.header-info {
  flex: 1;
  min-width: 0;
}
.detail-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 10px;
}
.detail-meta {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.salary {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}
.header-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 130px;
}
.header-bottom {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--divider-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.hr-box {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.hr-title {
  color: var(--text-tertiary);
}
.publish-time {
  font-size: 13px;
  color: var(--text-tertiary);
}
.job-desc {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-line;
}
.require-list {
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
}
.skill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.company-card {
  .company-name-large {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 12px;
  }
  .company-meta p {
    font-size: 14px;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 8px 0;
  }
}
.similar-job {
  padding: 12px 0;
  border-bottom: 1px solid var(--divider-color);
  cursor: pointer;
  &:last-child { border-bottom: none; }
  &:hover .similar-title { color: var(--primary-color); }
}
.similar-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
.similar-info {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  justify-content: space-between;
}
.similar-salary { color: #f56c6c; font-weight: 500; }

@media (max-width: 768px) {
  .header-main { flex-direction: column; }
  .header-actions { width: 100%; flex-direction: row; }
}
</style>
