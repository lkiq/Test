<template>
  <div>
    <h2 class="page-title">我的收藏</h2>
    <div v-if="favorites.length" class="job-list">
      <JobCard v-for="job in favorites" :key="job.id" :job="job" />
    </div>
    <div v-else class="page-card">
      <EmptyState scene="result" title="暂无收藏岗位" description="遇到喜欢的岗位点击收藏，方便随时查看" action-text="去找工作" @action="$router.push('/student/job-matching')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useJobStore } from '@/stores/job'
import JobCard from '@/components/job/JobCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const jobStore = useJobStore()
const favorites = computed(() => jobStore.favoriteJobDetails.length > 0 ? jobStore.favoriteJobDetails : jobStore.favoriteJobs)

onMounted(() => {
  jobStore.fetchFavoriteJobs()
})
</script>

<style scoped lang="scss">
.job-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>
