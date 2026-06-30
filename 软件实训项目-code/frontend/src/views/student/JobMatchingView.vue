<template>
  <div>
    <h2 class="page-title">岗位匹配推荐</h2>

    <el-row :gutter="20" align="top">
      <!-- 左侧筛选 -->
      <el-col :lg="5" :md="6" :sm="24" class="filter-col">
        <JobFilter @search="handleSearch" />
      </el-col>

      <!-- 右侧列表 -->
      <el-col :lg="19" :md="18" :sm="24">
        <div class="page-card list-header">
          <div class="list-tabs">
            <div
              v-for="tab in tabs"
              :key="tab.value"
              class="tab-item"
              :class="{ active: activeTab === tab.value }"
              @click="switchTab(tab.value)"
            >
              {{ tab.label }}
            </div>
          </div>
          <div class="list-sort">
            <span class="result-count">共 {{ jobStore.total }} 个岗位</span>
            <el-select v-model="jobStore.sort" size="small" style="width: 130px;" @change="handleSearch">
              <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </div>
        </div>

        <LoadingSkeleton v-if="loading" type="list" :rows="4" />
        <ErrorRetry v-else-if="error" :message="error" :retry="handleSearch" />
        <template v-else>
          <div v-if="jobStore.pagedJobs.length" class="job-list">
            <JobCard v-for="job in jobStore.pagedJobs" :key="job.id" :job="job" />
          </div>
          <EmptyState
            v-else
            scene="search"
            title="未找到合适岗位"
            description="尝试调整筛选条件或搜索关键词"
            action-text="清空筛选"
            @action="clearFilters"
          />

          <div v-if="jobStore.total > jobStore.pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="jobStore.page"
              :page-size="jobStore.pageSize"
              :total="jobStore.total"
              layout="prev, pager, next, jumper"
              background
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useJobStore } from '@/stores/job'
import { recommendJobs, searchJobs } from '@/api/student'
import { enrichJob, filterJobs, sortJobs } from '@/utils/jobEnrich'
import JobFilter from '@/components/job/JobFilter.vue'
import JobCard from '@/components/job/JobCard.vue'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'
import ErrorRetry from '@/components/common/ErrorRetry.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()
const jobStore = useJobStore()
const loading = ref(false)
const error = ref('')
const activeTab = ref('all')

const tabs = [
  { label: '全部岗位', value: 'all' },
  { label: '智能推荐', value: 'recommend' }
]

const sortOptions = [
  { label: '综合排序', value: 'comprehensive' },
  { label: '最新发布', value: 'newest' },
  { label: '薪资从高到低', value: 'salaryDesc' },
  { label: '薪资从低到高', value: 'salaryAsc' },
  { label: '匹配度', value: 'match' }
]

async function handleSearch() {
  loading.value = true
  error.value = ''
  try {
    let raw: any[] = []
    if (activeTab.value === 'recommend') {
      const res: any = await recommendJobs()
      raw = res.data || []
    } else {
      const res: any = await searchJobs(jobStore.filters.keyword || undefined, jobStore.filters.city || undefined)
      raw = res.data || []
    }
    const enriched = raw.map(enrichJob)
    const filtered = filterJobs(enriched, jobStore.filters)
    const sorted = sortJobs(filtered, jobStore.sort)
    jobStore.setJobs(sorted)
  } catch (e: any) {
    error.value = e?.message || '加载岗位失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function switchTab(tab: string) {
  activeTab.value = tab
  jobStore.sort = tab === 'recommend' ? 'match' : 'comprehensive'
  handleSearch()
}

function handlePageChange() {
  document.querySelector('.content')?.scrollTo({ top: 0, behavior: 'smooth' })
}

function clearFilters() {
  jobStore.resetFilters()
  activeTab.value = 'all'
  handleSearch()
}

// 监听路由 query 参数变化，支持从 HeaderBar 或其他页面传入关键词搜索
// 即使组件被 keep-alive 缓存也能响应
watch(
  () => route.query._t,
  () => {
    const q = route.query.q as string
    if (q) {
      jobStore.filters.keyword = q
      activeTab.value = 'all'
      handleSearch()
    }
  }
)

onMounted(() => {
  // 如果有路由传来的关键词，同步到 store 然后搜索
  if (route.query.q) {
    jobStore.filters.keyword = route.query.q as string
    handleSearch()
  } else if (!jobStore.jobs.length) {
    // 没有数据时执行初始搜索
    handleSearch()
  }
})
</script>

<style scoped lang="scss">
.filter-col {
  margin-bottom: 16px;
}
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 20px;
}
.list-tabs {
  display: flex;
  gap: 24px;
}
.tab-item {
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  padding-bottom: 6px;
  transition: color 0.2s;
  &:hover { color: var(--primary-color); }
  &.active {
    color: var(--primary-color);
    font-weight: 600;
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: var(--primary-color);
      border-radius: 2px;
    }
  }
}
.list-sort {
  display: flex;
  align-items: center;
  gap: 12px;
}
.result-count {
  font-size: 13px;
  color: var(--text-tertiary);
}
.job-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
