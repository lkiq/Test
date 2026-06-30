<template>
  <div>
    <h2 class="page-title">消息中心</h2>
    <div class="page-card">
      <div v-if="messages.length" class="message-list">
        <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ unread: !msg.read }">
          <div class="message-header">
            <span class="message-title">{{ msg.title }}</span>
            <span class="message-time">{{ msg.time }}</span>
          </div>
          <p class="message-content">{{ msg.content }}</p>
        </div>
      </div>
      <EmptyState v-else scene="message" title="暂无消息" description="HR 回复、面试邀约、系统通知都会出现在这里" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'

const messages = ref([
  { id: 1, title: '系统通知', content: '欢迎来到 IT求职平台，完善技术栈信息可以获得更精准的IT岗位推荐。', time: '10分钟前', read: false },
  { id: 2, title: '简历优化提醒', content: '您的简历综合评分已达到 85 分，继续保持！', time: '1小时前', read: false },
  { id: 3, title: '面试邀请', content: '您投递的 Java后端开发工程师 岗位邀请您参加明天下午 2 点的面试。', time: '昨天', read: true }
])
</script>

<style scoped lang="scss">
.message-list {
  display: flex;
  flex-direction: column;
}
.message-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--divider-color);
  &:last-child { border-bottom: none; }
  &.unread .message-title {
    color: var(--primary-color);
    font-weight: 600;
    &::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--danger-color);
      margin-right: 8px;
      vertical-align: middle;
    }
  }
}
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.message-title {
  font-size: 15px;
  color: var(--text-primary);
}
.message-time {
  font-size: 12px;
  color: var(--text-tertiary);
}
.message-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
