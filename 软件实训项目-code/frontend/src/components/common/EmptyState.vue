<template>
  <div class="empty-state" :class="{ compact }">
    <div class="empty-icon-wrapper" :style="{ background: iconBg }">
      <el-icon :size="compact ? 32 : 48" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <el-button v-if="actionText" type="primary" @click="$emit('action')" class="empty-action">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Search, FolderOpened, Document, ChatLineRound, Bell, DataLine } from '@element-plus/icons-vue'

type Scene = 'search' | 'data' | 'document' | 'message' | 'notification' | 'result'

const props = defineProps<{
  scene?: Scene
  title?: string
  description?: string
  actionText?: string
  compact?: boolean
}>()

defineEmits(['action'])

const sceneMap: Record<Scene, { icon: any; color: string; bg: string; defaultTitle: string }> = {
  search: { icon: Search, color: '#2563eb', bg: '#eff6ff', defaultTitle: '没有找到相关结果' },
  data: { icon: DataLine, color: '#10b981', bg: '#ecfdf5', defaultTitle: '暂无数据' },
  document: { icon: Document, color: '#f59e0b', bg: '#fff7ed', defaultTitle: '暂无文档' },
  message: { icon: ChatLineRound, color: '#8b5cf6', bg: '#f5f3ff', defaultTitle: '暂无消息' },
  notification: { icon: Bell, color: '#ef4444', bg: '#fef2f2', defaultTitle: '暂无通知' },
  result: { icon: FolderOpened, color: '#06b6d4', bg: '#ecfeff', defaultTitle: '暂无内容' }
}

const icon = computed(() => sceneMap[props.scene || 'result'].icon)
const iconColor = computed(() => sceneMap[props.scene || 'result'].color)
const iconBg = computed(() => sceneMap[props.scene || 'result'].bg)
const title = computed(() => props.title ?? sceneMap[props.scene || 'result'].defaultTitle)
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-secondary);
  text-align: center;
  animation: fadeInUp 0.5s ease;

  &.compact {
    padding: 32px 16px;
    .empty-title { font-size: 15px; margin-top: 10px; }
    .empty-desc { font-size: 13px; }
  }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
.empty-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  &.compact & {
    width: 56px;
    height: 56px;
    border-radius: 14px;
  }
}
.empty-title {
  margin-top: 16px;
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}
.empty-desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 400px;
  line-height: 1.5;
}
.empty-action {
  margin-top: 16px;
}
</style>
