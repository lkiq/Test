<template>
  <div>
    <h2 class="page-title">我的投递</h2>
    <div class="page-card">
      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px; color: var(--text-secondary);">加载中...</p>
      </div>
      <el-table v-else-if="jobStore.applications.length" :data="jobStore.applications" stripe>
        <el-table-column label="岗位">
          <template #default="scope">
            <el-link type="primary" @click="$router.push(`/student/job/${scope.row.jobId}`)">{{ scope.row.jobTitle || '未知岗位' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="公司" />
        <el-table-column label="投递时间">
          <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push(`/student/job/${scope.row.jobId}`)">查看</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else scene="document" title="暂无投递记录" description="去岗位匹配看看心仪的职位吧" action-text="去找工作" @action="$router.push('/student/job-matching')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useJobStore } from '@/stores/job'
import EmptyState from '@/components/common/EmptyState.vue'

const jobStore = useJobStore()
const loading = ref(false)

function formatTime(iso: string) {
  if (!iso) return ''
  return new Date(iso).toLocaleString()
}

function statusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info',
    INTERVIEW: 'success',
    REJECT: 'danger',
    CANCELLED: 'warning'
  }
  return map[status] || 'info'
}

function statusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待筛选',
    INTERVIEW: '邀约面试',
    REJECT: '不合适',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm('确定要取消该投递吗？', '确认', { type: 'warning' })
    const ok = await jobStore.cancelApplicationById(row.id)
    if (ok) {
      ElMessage.success('已取消投递')
    } else {
      ElMessage.error('取消失败')
    }
  } catch {
    // 用户取消操作
  }
}

onMounted(async () => {
  loading.value = true
  await jobStore.fetchApplications()
  loading.value = false
})
</script>
