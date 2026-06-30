<template>
  <div class="cs-page">
    <!-- Hero -->
    <div class="cs-hero">
      <div class="csh-bg-grid"></div>
      <div class="csh-content">
        <div class="csh-icon-wrap">🤖</div>
        <h1>智能客服</h1>
        <p>AI驱动的智能客服，7x24小时为你解答疑问</p>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧FAQ -->
      <el-col :lg="8" :md="10" :sm="24">
        <div class="cs-faq-panel">
          <div class="cs-faq-header">
            <span class="csfh-icon">📋</span>
            <div>
              <h4>常见问题</h4>
              <p>点击问题快速提问</p>
            </div>
          </div>

          <div class="cs-faq-list" v-if="faqs.length">
            <div v-for="faq in faqs" :key="faq.id" class="cs-faq-item" @click="askFAQ(faq.question)">
              <div class="csfi-top">
                <span class="csfi-q">Q</span>
                <span class="csfi-question">{{ faq.question }}</span>
                <span class="csfi-arrow">→</span>
              </div>
              <div class="csfi-answer" v-if="faq.answer">
                {{ faq.answer }}
              </div>
            </div>
          </div>
          <div v-else class="cs-faq-empty">
            <span>📭</span> 暂无常见问题
          </div>
        </div>
      </el-col>

      <!-- 右侧聊天 -->
      <el-col :lg="16" :md="14" :sm="24">
        <div class="cs-chat-panel">
          <div class="cs-chat-header">
            <div class="csch-left">
              <div class="csch-avatar">🤖</div>
              <div>
                <div class="csch-name">AI 智能助手</div>
                <div class="csch-status">
                  <span class="csch-dot" :class="{ thinking: loading }"></span>
                  {{ loading ? '思考中...' : '在线' }}
                </div>
              </div>
            </div>
            <div class="csch-tags">
              <span class="csch-tag" @click="askFAQ('如何投递简历？')">投递简历</span>
              <span class="csch-tag" @click="askFAQ('如何进行能力测评？')">能力测评</span>
              <span class="csch-tag" @click="askFAQ('如何与HR沟通？')">HR沟通</span>
            </div>
          </div>
          <div class="cs-chat-body">
            <ChatWindow :messages="messages" :loading="loading" placeholder="请输入你的问题..." @send="handleSend" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ChatWindow from '@/components/common/ChatWindow.vue'
import { chat, getFAQs } from '@/api/customerService'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const faqs = ref<any[]>([])
const messages = ref<any[]>([{ role: 'assistant', content: '你好！我是平台智能客服，有什么可以帮助你的吗？' }])
const loading = ref(false)

onMounted(async () => {
  const res: any = await getFAQs()
  faqs.value = res.data || []
})

function askFAQ(question: string) { handleSend(question) }

async function handleSend(text: string) {
  messages.value.push({ role: 'user', content: text })
  loading.value = true
  try {
    const res: any = await chat({ question: text, userRole: userStore.role })
    const data = res.data
    messages.value.push({ role: 'assistant', content: data.answer })
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '网络异常，请稍后重试'
    messages.value.push({ role: 'system', content: `抱歉，客服暂时不可用（${msg}）` })
  } finally { loading.value = false }
}
</script>

<style scoped lang="scss">
// ======== Hero ========
.cs-hero {
  position: relative;
  background: linear-gradient(135deg, #312e81, #4338ca, #6366f1);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  text-align: center;
  overflow: hidden;
}
.csh-bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}
.csh-content { position: relative; z-index: 1; }
.csh-icon-wrap {
  width: 60px; height: 60px;
  border-radius: 18px;
  background: rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  font-size: 30px;
  margin: 0 auto 12px;
}
.cs-hero h1 { font-size: 26px; font-weight: 800; color: #fff; margin: 0 0 6px; }
.cs-hero p { color: #c7d2fe; font-size: 14px; }

// ======== FAQ面板 ========
.cs-faq-panel {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.cs-faq-header {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
  .csfh-icon { font-size: 26px; }
  h4 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0; }
  p { font-size: 12px; color: #9ca3af; margin: 2px 0 0; }
}

.cs-faq-list {
  max-height: 520px;
  overflow-y: auto;
  padding: 8px;
}

.cs-faq-item {
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  &:hover {
    background: #f5f3ff;
    .csfi-arrow { opacity: 1; transform: translateX(4px); }
  }
  .csfi-top {
    display: flex; align-items: center; gap: 10px;
    .csfi-q {
      width: 24px; height: 24px;
      border-radius: 7px;
      background: #6366f1;
      color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700; flex-shrink: 0;
    }
    .csfi-question { font-size: 14px; color: #374151; font-weight: 500; flex: 1; }
    .csfi-arrow { color: #6366f1; opacity: 0; transition: all 0.25s; font-weight: 700; }
  }
  .csfi-answer {
    margin-top: 8px; margin-left: 34px;
    font-size: 13px; color: #6b7280; line-height: 1.5;
    padding: 10px 14px; background: #f9fafb; border-radius: 8px;
  }
}

.cs-faq-empty {
  text-align: center; padding: 60px; color: #9ca3af; font-size: 14px;
}

// ======== 聊天面板 ========
.cs-chat-panel {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.cs-chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;
  flex-wrap: wrap; gap: 12px;
  .csch-left {
    display: flex; align-items: center; gap: 12px;
    .csch-avatar {
      width: 40px; height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      font-size: 20px;
    }
    .csch-name { font-size: 15px; font-weight: 700; color: #1f2937; }
    .csch-status {
      font-size: 12px; color: #6b7280;
      display: flex; align-items: center; gap: 6px;
    }
    .csch-dot {
      width: 7px; height: 7px;
      border-radius: 50%;
      background: #10b981;
      &.thinking { background: #f59e0b; animation: gentlePulse 1s ease-in-out infinite; }
    }
  }
  .csch-tags { display: flex; gap: 6px; flex-wrap: wrap; }
  .csch-tag {
    padding: 4px 12px;
    border-radius: 8px;
    background: #f3f4f6;
    font-size: 12px; color: #374151;
    cursor: pointer;
    transition: all 0.2s;
    &:hover { background: #eef2ff; color: #4f46e5; }
  }
}

.cs-chat-body {
  height: 540px;
}
</style>
