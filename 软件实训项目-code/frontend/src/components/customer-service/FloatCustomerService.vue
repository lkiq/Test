<template>
  <div class="float-cs">
    <!-- 聊天窗口 -->
    <transition name="chat-fade">
      <div v-if="visible" class="chat-window" :style="windowStyle">
        <div class="chat-header">
          <div class="header-title">
            <el-icon size="18"><Service /></el-icon>
            <span>AI 智能客服</span>
          </div>
          <div class="header-actions">
            <el-icon class="close-icon" @click="visible = false"><Close /></el-icon>
          </div>
        </div>

        <div ref="chatBodyRef" class="chat-body">
          <div class="welcome-msg">
            <div class="avatar bot"><el-icon><Service /></el-icon></div>
            <div class="bubble">你好！我是 AI 求职助手，专注于 IT 行业求职，随时为你解答技术岗位、简历、面试等问题～</div>
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-row"
            :class="msg.role"
          >
            <div class="avatar" :class="msg.role">
              <el-icon v-if="msg.role === 'bot'"><Service /></el-icon>
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="bubble" v-html="formatMsg(msg.content)"></div>
          </div>
          <div v-if="loading" class="message-row bot">
            <div class="avatar bot"><el-icon><Service /></el-icon></div>
            <div class="bubble loading">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <div class="quick-tags">
            <span
              v-for="tag in quickTags"
              :key="tag"
              class="quick-tag"
              @click="sendQuick(tag)"
            >{{ tag }}</span>
          </div>
          <div class="input-row">
            <el-input
              v-model="input"
              placeholder="请输入你的问题..."
              maxlength="200"
              show-word-limit
              @keyup.enter="send"
            />
            <el-button type="primary" :loading="loading" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 悬浮球 -->
    <div
      ref="ballRef"
      class="float-ball"
      :class="{ active: visible }"
      :style="ballStyle"
      @mousedown.prevent="startDrag"
    >
      <el-icon :size="22"><Service /></el-icon>
      <span class="ball-text">客服</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { Service, User, Close } from '@element-plus/icons-vue'
import { chat } from '@/api/customerService'

interface ChatMessage {
  role: 'user' | 'bot'
  content: string
}

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref<ChatMessage[]>([])
const chatBodyRef = ref<HTMLElement>()

const quickTags = ['前端面试', '后端面试', '算法刷题', '简历优化', '技术栈规划']

// 悬浮球位置
const ballSize = 56
const gap = 20

function defaultBallPos() {
  return {
    x: window.innerWidth - ballSize - gap - 12,
    y: window.innerHeight - ballSize - gap - 120
  }
}

const ballPos = ref(defaultBallPos())
const ballRef = ref<HTMLElement>()

onMounted(() => {
  ballPos.value = defaultBallPos()
})

const ballStyle = computed(() => ({
  left: `${ballPos.value.x}px`,
  top: `${ballPos.value.y}px`
}))

const windowStyle = computed(() => ({
  right: '16px',
  bottom: '100px'
}))

// ---- 拖拽逻辑 ----
// 使用普通变量而非 ref，避免 Vue 响应式时序问题
let dragHasMoved = false
let dragStartX = 0
let dragStartY = 0
let dragStartBallX = 0
let dragStartBallY = 0

function startDrag(e: MouseEvent) {
  // 如果聊天窗已打开，不拖拽（避免误触）
  dragHasMoved = false
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartBallX = ballPos.value.x
  dragStartBallY = ballPos.value.y

  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragUp)
}

function onDragMove(e: MouseEvent) {
  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY

  // 移动超过 2px 才算拖拽
  if (!dragHasMoved && (Math.abs(dx) > 2 || Math.abs(dy) > 2)) {
    dragHasMoved = true
  }
  if (!dragHasMoved) return

  ballPos.value = {
    x: Math.max(gap, Math.min(window.innerWidth - ballSize - gap, dragStartBallX + dx)),
    y: Math.max(gap, Math.min(window.innerHeight - ballSize - gap, dragStartBallY + dy))
  }
}

function onDragUp() {
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragUp)

  if (dragHasMoved) {
    // 保持当前位置，不进行吸附
    // 可以添加一些边界检查，但不要强制吸附到边缘
    // 确保悬浮球不会超出屏幕边界
    ballPos.value = {
      x: Math.max(gap, Math.min(window.innerWidth - ballSize - gap, ballPos.value.x)),
      y: Math.max(gap, Math.min(window.innerHeight - ballSize - gap, ballPos.value.y))
    }
  } else {
    // 没有移动 = 点击，切换聊天窗
    visible.value = !visible.value
  }
}

function sendQuick(tag: string) {
  input.value = tag
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await chat({ question: text })
    const reply = res.data?.reply || res.data?.answer || res.data?.data?.reply || res.data?.data?.answer || '收到你的问题，我会尽快帮你解答。'
    messages.value.push({ role: 'bot', content: reply })
  } catch (err) {
    messages.value.push({
      role: 'bot',
      content: '网络有点问题，你可以稍后再试，或者拨打客服热线 400-xxx-xxxx。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatBodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function formatMsg(text: string) {
  return text.replace(/\n/g, '<br>')
}

watch(messages, scrollToBottom, { deep: true })
</script>

<style scoped lang="scss">
.float-cs {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1000;
}

.float-ball {
  position: fixed;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.35);
  cursor: grab;
  pointer-events: auto;
  transition: transform 0.2s, box-shadow 0.2s;
  user-select: none;
  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 22px rgba(37, 99, 235, 0.45);
  }
  &:active {
    cursor: grabbing;
    transform: scale(0.95);
  }
  &.active {
    background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  }
  .ball-text {
    font-size: 11px;
    margin-top: 2px;
  }
}

.chat-window {
  position: fixed;
  width: 360px;
  height: 480px;
  right: 20px;
  bottom: 100px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  pointer-events: auto;
}

.chat-header {
  height: 56px;
  padding: 0 16px;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    font-size: 15px;
  }
  .close-icon {
    cursor: pointer;
    font-size: 18px;
    padding: 4px;
    border-radius: 4px;
    transition: background 0.2s;
    &:hover { background: rgba(255, 255, 255, 0.2); }
  }
}

.chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f8fafc;
}

.welcome-msg,
.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  &.user {
    flex-direction: row-reverse;
    .bubble {
      background: #2563eb;
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }
  }
  &.bot .bubble {
    background: #fff;
    color: var(--text-primary);
    border-radius: 16px 16px 16px 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  }
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  &.bot {
    background: #eff6ff;
    color: #2563eb;
  }
  &.user {
    background: #e0e7ff;
    color: #4f46e5;
  }
}

.bubble {
  max-width: 220px;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  &.loading {
    display: flex;
    align-items: center;
    gap: 4px;
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #cbd5e1;
      animation: bounce 1.4s infinite ease-in-out both;
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-footer {
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.quick-tag {
  padding: 4px 10px;
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: #2563eb;
    color: #fff;
  }
}

.input-row {
  display: flex;
  gap: 8px;
  .el-input { flex: 1; }
  .el-button { flex-shrink: 0; }
}

.chat-fade-enter-active,
.chat-fade-leave-active {
  transition: all 0.25s ease;
}
.chat-fade-enter-from,
.chat-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

@media (max-width: 768px) {
  .chat-window {
    width: calc(100vw - 40px);
    height: 60vh;
    right: 20px;
    left: 20px;
  }
}
</style>
