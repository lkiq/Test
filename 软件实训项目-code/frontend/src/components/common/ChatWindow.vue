<template>
  <div class="chat-window">
    <div class="chat-messages" ref="msgContainer">
      <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
        <div class="msg-avatar" v-if="msg.role !== 'user'">
          {{ msg.role === 'assistant' ? '🤖' : 'ℹ️' }}
        </div>
        <div class="msg-body">
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
        <div class="msg-avatar user-avatar" v-if="msg.role === 'user'">
          👤
        </div>
      </div>
      <div v-if="loading" class="message assistant">
        <div class="msg-avatar">🤖</div>
        <div class="msg-body">
          <div class="msg-bubble typing-indicator">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>
      </div>
    </div>
    <div class="chat-input" v-if="!readonly">
      <div class="chat-input-row">
        <el-input
          v-model="inputText"
          :placeholder="placeholder"
          @keyup.enter="send"
          type="textarea"
          :rows="2"
          class="chat-textarea"
        />
        <el-button type="primary" @click="send" :loading="loading" class="chat-send-btn">
          <el-icon><Promotion /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { Promotion } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  messages: { role: string; content: string }[];
  loading?: boolean;
  placeholder?: string;
  readonly?: boolean;
}>(), { loading: false, placeholder: '请输入...', readonly: false })

const emit = defineEmits<{ send: [text: string] }>()
const inputText = ref('')
const msgContainer = ref<HTMLElement>()

watch(() => props.messages.length, () => {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
})

function send() {
  if (!inputText.value.trim()) return
  emit('send', inputText.value.trim())
  inputText.value = ''
}
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 500px;
  background: #f8fafc;
  border-radius: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chat-messages::-webkit-scrollbar {
  width: 5px;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 90%;
  animation: msgSlideIn 0.3s ease-out;
}

@keyframes msgSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  align-self: flex-end;
}

.message.assistant, .message.system {
  align-self: flex-start;
}

.msg-avatar {
  width: 34px; height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(99,102,241,0.15);
}

.user-avatar {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  box-shadow: 0 2px 8px rgba(59,130,246,0.15);
}

.msg-body {
  max-width: 100%;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message.user .msg-bubble {
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message.assistant .msg-bubble {
  background: #fff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.message.system .msg-bubble {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  border-bottom-left-radius: 4px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px 20px !important;
}

.typing-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* 输入区 */
.chat-input {
  padding: 14px 16px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}

.chat-input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-textarea {
  flex: 1;
}

.chat-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #e5e7eb;
  font-size: 14px;
  padding: 10px 14px;
  line-height: 1.5;
  resize: none;
  transition: all 0.25s;
}

.chat-textarea :deep(.el-textarea__inner:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

.chat-send-btn {
  width: 42px; height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(99,102,241,0.3);
  transition: all 0.25s;
}

.chat-send-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(99,102,241,0.4);
}

.chat-send-btn:active {
  transform: scale(0.95);
}
</style>
