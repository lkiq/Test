import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { EnrichedJob } from '@/utils/jobEnrich'
import { applyJob as apiApply, getApplications, cancelApplication, addFavorite, removeFavorite, getFavorites, getJobDetail } from '@/api/student'
import { enrichJob } from '@/utils/jobEnrich'
import { ElMessage } from 'element-plus'

export interface ApplicationRecord {
  id: number
  jobId: number
  userId: number
  resumeId: number | null
  status: string
  createdAt: string
  jobTitle?: string
  companyName?: string
}

export const useJobStore = defineStore('job', () => {
  // 岗位列表与加载状态
  const jobs = ref<EnrichedJob[]>([])
  const loading = ref(false)
  const error = ref('')

  // 筛选条件
  const filters = ref({
    keyword: '',
    city: '',
    direction: [] as string[],
    salaryRange: null as null | [number, number],
    experience: '',
    education: '',
    jobType: '',
    skill: [] as string[]
  })

  // 排序
  const sort = ref('comprehensive')

  // 分页
  const page = ref(1)
  const pageSize = ref(10)

  // 收藏（后端持久化）
  const favorites = ref<Set<number>>(new Set())
  const favoriteLoading = ref(false)
  const favoriteJobDetails = ref<EnrichedJob[]>([])

  // 投递记录（从后端同步）
  const applications = ref<ApplicationRecord[]>([])
  const appliedJobIds = computed(() => new Set(applications.value.map(a => a.jobId)))

  const total = computed(() => jobs.value.length)
  const pagedJobs = computed(() => {
    const start = (page.value - 1) * pageSize.value
    return jobs.value.slice(start, start + pageSize.value)
  })

  function setJobs(list: EnrichedJob[]) {
    jobs.value = list
    page.value = 1
  }

  function resetFilters() {
    filters.value = {
      keyword: '',
      city: '',
      direction: [],
      salaryRange: null,
      experience: '',
      education: '',
      jobType: '',
      skill: []
    }
    sort.value = 'comprehensive'
    page.value = 1
  }

  // ========== 收藏功能（后端持久化） ==========

  /** 从后端加载收藏列表（ID列表） */
  async function fetchFavorites() {
    favoriteLoading.value = true
    try {
      const res: any = await getFavorites()
      const ids: number[] = res.data || []
      favorites.value = new Set(ids)
    } catch {
      // 后端接口不可用时保持本地状态
    } finally {
      favoriteLoading.value = false
    }
  }

  /** 从后端加载收藏岗位完整详情（独立于 jobStore.jobs） */
  async function fetchFavoriteJobs() {
    favoriteLoading.value = true
    try {
      const res: any = await getFavorites()
      const ids: number[] = res.data || []
      favorites.value = new Set(ids)
      if (ids.length > 0) {
        const details = await Promise.all(
          ids.map(id => getJobDetail(id).then(r => enrichJob(r.data)).catch(() => null))
        )
        favoriteJobDetails.value = details.filter((d): d is EnrichedJob => d !== null)
      } else {
        favoriteJobDetails.value = []
      }
    } catch {
      favoriteJobDetails.value = []
    } finally {
      favoriteLoading.value = false
    }
  }

  /** 切换收藏状态（调用后端API，带 loading 锁） */
  let favoriteToggleLock = false
  async function toggleFavorite(jobId: number) {
    if (favoriteToggleLock) return
    favoriteToggleLock = true
    favoriteLoading.value = true
    const wasFavorite = favorites.value.has(jobId)
    // 乐观更新
    if (wasFavorite) {
      favorites.value.delete(jobId)
    } else {
      favorites.value.add(jobId)
    }
    try {
      if (wasFavorite) {
        await removeFavorite(jobId)
        ElMessage.success('已取消收藏')
      } else {
        await addFavorite(jobId)
        ElMessage.success('已收藏该岗位')
      }
      // 同步 favoriteJobDetails
      if (favoriteJobDetails.value.length > 0) {
        if (wasFavorite) {
          favoriteJobDetails.value = favoriteJobDetails.value.filter(j => j.id !== jobId)
        } else {
          try {
            const detailRes: any = await getJobDetail(jobId)
            const enriched = enrichJob(detailRes.data)
            favoriteJobDetails.value.push(enriched)
          } catch { /* 降级：不追加详情 */ }
        }
      }
    } catch {
      // 后端失败时回退本地状态
      if (wasFavorite) {
        favorites.value.add(jobId)
      } else {
        favorites.value.delete(jobId)
      }
      ElMessage.warning('操作失败，请稍后重试')
    } finally {
      favoriteLoading.value = false
      favoriteToggleLock = false
    }
  }

  function isFavorite(jobId: number) {
    return favorites.value.has(jobId)
  }

  /** 已收藏岗位列表 */
  const favoriteJobs = computed(() => {
    if (favoriteJobDetails.value.length > 0) return favoriteJobDetails.value
    return jobs.value.filter(j => favorites.value.has(j.id))
  })

  // ========== 投递功能（后端持久化） ==========

  /** 从后端加载投递记录 */
  async function fetchApplications() {
    try {
      const res: any = await getApplications()
      applications.value = res.data || []
    } catch {
      // 静默失败，不阻塞UI
    }
  }

  /** 投递简历（调用后端API，自动附带最新简历） */
  async function applyJob(jobId: number, resumeId?: number): Promise<boolean> {
    if (appliedJobIds.value.has(jobId)) return false
    try {
      let finalResumeId = resumeId
      if (!finalResumeId) {
        try {
          const { listResumes } = await import('@/api/student')
          const res: any = await listResumes()
          if (res.data && res.data.length > 0) {
            finalResumeId = res.data[0].id
          }
        } catch {
          // 没有简历就不传
        }
      }
      await apiApply(jobId, finalResumeId)
      await fetchApplications()
      return true
    } catch {
      return false
    }
  }

  /** 取消投递 */
  async function cancelApplicationById(applicationId: number): Promise<boolean> {
    try {
      await cancelApplication(applicationId)
      applications.value = applications.value.filter(a => a.id !== applicationId)
      return true
    } catch {
      return false
    }
  }

  function hasApplied(jobId: number) {
    return appliedJobIds.value.has(jobId)
  }

  return {
    // 岗位列表
    jobs,
    loading,
    error,
    filters,
    sort,
    page,
    pageSize,
    total,
    pagedJobs,
    setJobs,
    resetFilters,
    // 收藏
    favorites,
    favoriteLoading,
    favoriteJobDetails,
    favoriteJobs,
    toggleFavorite,
    isFavorite,
    fetchFavorites,
    fetchFavoriteJobs,
    // 投递
    applications,
    appliedJobIds,
    fetchApplications,
    applyJob,
    cancelApplicationById,
    hasApplied
  }
})
