<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar" :class="{ collapsed: mobileShowChat }">
      <div class="sidebar-header">
        <h3>消息</h3>
      </div>
      <div class="conversation-list">
        <div
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: chatStore.activeConversationId === conv.id }"
          @click="selectConversation(conv)"
        >
          <div class="conv-avatar">
            <el-avatar :size="40" :icon="UserFilled" />
          </div>
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">
                {{ userStore.role === 'STUDENT' ? (conv.hrName || conv.enterpriseName || 'HR') : (conv.studentName || '学生') }}
              </span>
              <span class="conv-time">{{ formatTime(conv.lastMessageAt) }}</span>
            </div>
            <div class="conv-bottom">
              <span class="conv-msg">{{ conv.lastMessage || '暂无消息' }}</span>
              <el-badge v-if="conv.unreadCount" :value="conv.unreadCount" :max="99" class="conv-badge" />
            </div>
            <div class="conv-job" v-if="conv.jobTitle">{{ conv.jobTitle }}</div>
          </div>
        </div>
        <div v-if="!chatStore.conversations.length && !chatStore.loading" class="empty-conv">
          <span>暂无会话</span>
          <p>浏览岗位列表，点击"聊一聊"与HR开始沟通</p>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-main" :class="{ 'no-conv': !chatStore.activeConversation }">
      <!-- 空状态 -->
      <div v-if="!chatStore.activeConversation" class="empty-chat">
        <el-icon :size="64" color="#d1d5db"><ChatDotRound /></el-icon>
        <p>选择一个会话开始聊天</p>
      </div>

      <!-- 聊天头部 -->
      <template v-else>
        <div class="chat-header">
          <el-button v-if="mobileShowChat" text :icon="ArrowLeft" @click="mobileShowChat = false" class="back-btn" />
          <el-avatar :size="36" :icon="UserFilled" />
          <div class="chat-partner-info">
            <span class="partner-name">
              {{ userStore.role === 'STUDENT' ? (chatStore.activeConversation?.hrName || chatStore.activeConversation?.enterpriseName || 'HR') : (chatStore.activeConversation?.studentName || '学生') }}
            </span>
            <span class="partner-job" v-if="chatStore.activeConversation?.jobTitle">
              {{ chatStore.activeConversation.jobTitle }}
            </span>
          </div>
          <el-button text type="danger" size="small" @click="handleClose">关闭会话</el-button>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="msgListRef">
          <div
            v-for="msg in chatStore.activeMessages"
            :key="msg.id"
            class="msg-item"
            :class="{ mine: msg.senderId === userStore.userId }"
          >
            <div class="msg-wrapper" :class="{ mine: msg.senderId === userStore.userId }">
              <!-- 发送者身份标签 -->
              <div class="msg-sender-label" v-if="msg.senderId !== userStore.userId">
                <span class="sender-tag" :class="msg.senderRole === 'STUDENT' ? 'tag-student' : 'tag-hr'">
                  {{ msg.senderRole === 'STUDENT' ? '学生' : 'HR' }}
                </span>
                <span class="sender-name">{{ msg.senderName || (msg.senderRole === 'STUDENT' ? '学生' : 'HR') }}</span>
              </div>
              <div class="msg-bubble" :class="{ mine: msg.senderId === userStore.userId }">
                <div class="msg-text">{{ msg.content }}</div>
                <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
              </div>
              <!-- 自己的消息也显示角色标签 -->
              <div class="msg-sender-label self-label" v-if="msg.senderId === userStore.userId">
                <span class="sender-name">{{ msg.senderName || '我' }}</span>
                <span class="sender-tag" :class="msg.senderRole === 'STUDENT' ? 'tag-student' : 'tag-hr'">
                  {{ msg.senderRole === 'STUDENT' ? '学生' : 'HR' }}
                </span>
              </div>
            </div>
          </div>
          <div v-if="chatStore.activeMessages.length === 0" class="empty-msg">
            <span>暂无消息，发送第一条消息打个招呼吧</span>
          </div>
          <div ref="msgEndRef" />
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
          <div class="input-footer">
            <span class="input-hint">按 Enter 发送</span>
            <el-button type="primary" :disabled="!inputText.trim() || chatStore.sending" @click="handleSend">
              发送
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ChatDotRound, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import type { Conversation } from '@/types/chat'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const inputText = ref('')
const msgListRef = ref<HTMLElement | null>(null)
const msgEndRef = ref<HTMLElement | null>(null)
const mobileShowChat = ref(false)

/** 选择会话 */
async function selectConversation(conv: Conversation) {
  chatStore.setActiveConversation(conv.id)
  await chatStore.fetchMessages(conv.id)
  await chatStore.markAsRead(conv.id)
  mobileShowChat.value = true
  scrollToBottom()
}

/** 发送消息 */
async function handleSend() {
  if (!inputText.value.trim() || !chatStore.activeConversationId) return
  const text = inputText.value.trim()
  inputText.value = ''
  const ok = await chatStore.sendMessage(chatStore.activeConversationId, text)
  if (ok) {
    await nextTick()
    scrollToBottom()
  } else {
    ElMessage.error('发送失败')
  }
}

/** 关闭会话 */
async function handleClose() {
  if (!chatStore.activeConversationId) return
  try {
    await ElMessageBox.confirm('确定要关闭该会话吗？关闭后将无法发送新消息。', '关闭会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chatStore.closeConversation(chatStore.activeConversationId)
    chatStore.setActiveConversation(null)
    ElMessage.success('会话已关闭')
  } catch {
    // 取消
  }
}

/** 滚动到底部 */
function scrollToBottom() {
  nextTick(() => {
    msgEndRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

/** 格式化时间 */
function formatTime(t: string | null): string {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60_000) return '刚刚'
  if (diff < 3_600_000) return Math.floor(diff / 60_000) + '分钟前'
  if (diff < 86_400_000 && d.getDate() === now.getDate()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

// 监听活跃会话变化，滚动到底部
watch(() => chatStore.activeMessages.length, () => scrollToBottom())

onMounted(async () => {
  await chatStore.fetchConversations()
  chatStore.startPolling()

  // 处理从岗位页跳转过来的参数
  const convId = route.query.conversationId
  if (convId) {
    const id = Number(convId)
    const conv = chatStore.conversations.find(c => c.id === id)
    if (conv) await selectConversation(conv)
  }

  // 从岗位卡片"聊一聊"跳转：自动创建/进入会话
  const hrId = route.query.hrId
  const jobId = route.query.jobId
  const jobTitle = route.query.jobTitle as string | undefined
  const enterpriseName = route.query.enterpriseName as string | undefined

  if (hrId && userStore.role === 'STUDENT') {
    const conv = await chatStore.createConversation({
      hrId: Number(hrId),
      jobId: jobId ? Number(jobId) : undefined,
      jobTitle,
      enterpriseName
    })
    if (conv) {
      await selectConversation(conv)
      // 清除路由 query 参数，避免刷新后重复创建
      router.replace({ path: '/chat', query: { conversationId: conv.id } })
    }
  }
})

onUnmounted(() => {
  chatStore.stopPolling()
})
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  height: calc(100vh - var(--header-height) - 32px);
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

// 左侧会话列表
.chat-sidebar {
  width: 320px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  h3 { margin: 0; font-size: 16px; color: var(--text-primary); }
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
}
.conv-item {
  display: flex;
  padding: 14px 20px;
  gap: 12px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f3f4f6;
  &:hover { background: #f9fafb; }
  &.active { background: var(--primary-light); }
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.conv-time {
  font-size: 11px;
  color: var(--text-tertiary);
  white-space: nowrap;
}
.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-msg {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}
.conv-job {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 3px;
}
.empty-conv {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-tertiary);
  p { font-size: 12px; margin-top: 8px; }
}

// 右侧聊天区
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  &.no-conv {
    justify-content: center;
    align-items: center;
  }
}
.empty-chat {
  text-align: center;
  color: var(--text-tertiary);
  p { margin-top: 16px; font-size: 15px; }
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-color);
  .back-btn { margin-right: -8px; display: none; }
}
.chat-partner-info {
  flex: 1;
}
.partner-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.partner-job {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: 10px;
}
.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f9fafb;
}
.msg-item {
  display: flex;
  margin-bottom: 18px;
  &.mine { justify-content: flex-end; }
}
.msg-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  &.mine { align-items: flex-end; }
}
.msg-sender-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
  padding: 0 4px;
  &.self-label {
    flex-direction: row;
    margin-top: 3px;
    margin-bottom: 0;
  }
}
.sender-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 600;
  line-height: 1.6;
  &.tag-student {
    background: #e6f7ff;
    color: #1890ff;
  }
  &.tag-hr {
    background: #fff7e6;
    color: #fa8c16;
  }
}
.sender-name {
  font-size: 12px;
  color: var(--text-secondary);
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  &.mine {
    background: var(--primary-color);
    .msg-text { color: #fff; }
    .msg-time { color: rgba(255,255,255,0.7); }
  }
}
.msg-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-time {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: right;
  margin-top: 4px;
}
.empty-msg {
  text-align: center;
  padding: 40px;
  color: var(--text-tertiary);
  font-size: 13px;
}
.chat-input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid var(--border-color);
  background: #fff;
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    background: #f9fafb;
    &:focus { background: #fff; }
  }
}
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.input-hint {
  font-size: 12px;
  color: var(--text-tertiary);
}

// 响应式
@media (max-width: 768px) {
  .chat-sidebar {
    width: 100%;
    &.collapsed { display: none; }
  }
  .chat-main {
    min-width: 100%;
  }
  .chat-main.no-conv { display: none; }
  .chat-header .back-btn { display: block; }
  .conv-item { padding: 12px 16px; }
}
</style>
