<template>
  <div class="error-retry">
    <el-icon :size="56" color="#F56C6C"><WarningFilled /></el-icon>
    <h3 class="error-title">{{ title }}</h3>
    <p v-if="message" class="error-message">{{ message }}</p>
    <el-button type="primary" :loading="loading" @click="handleRetry">
      <el-icon class="el-icon--left"><RefreshRight /></el-icon>
      重新加载
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { WarningFilled, RefreshRight } from '@element-plus/icons-vue'

const props = defineProps<{
  title?: string
  message?: string
  retry?: () => Promise<void> | void
}>()

const loading = ref(false)

async function handleRetry() {
  if (!props.retry) return
  loading.value = true
  try {
    await props.retry()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.error-retry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}
.error-title {
  margin-top: 16px;
  font-size: 17px;
  font-weight: 500;
  color: var(--text-primary);
}
.error-message {
  margin: 8px 0 18px;
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 420px;
}
</style>
