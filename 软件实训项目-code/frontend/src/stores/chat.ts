import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Conversation, ChatMessage } from '@/types/chat'
import * as chatApi from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const conversations = ref<Conversation[]>([])
  const messages = ref<Map<number, ChatMessage[]>>(new Map())
  const activeConversationId = ref<number | null>(null)
  const unreadTotal = ref(0)
  const loading = ref(false)
  const sending = ref(false)

  // 活跃会话
  const activeConversation = computed(() =>
    conversations.value.find(c => c.id === activeConversationId.value) || null
  )

  // 活跃会话的消息列表
  const activeMessages = computed(() =>
    activeConversationId.value ? (messages.value.get(activeConversationId.value) || []) : []
  )

  /** 设置活跃会话 */
  function setActiveConversation(id: number | null) {
    activeConversationId.value = id
  }

  /** 加载会话列表 */
  async function fetchConversations() {
    loading.value = true
    try {
      const res: any = await chatApi.getConversations()
      conversations.value = res.data || []
      // 计算总未读数
      unreadTotal.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
    } finally {
      loading.value = false
    }
  }

  /** 加载历史消息 */
  async function fetchMessages(conversationId: number) {
    try {
      const res: any = await chatApi.getMessages(conversationId, 1, 50)
      messages.value.set(conversationId, res.data || [])
    } catch (e) {
      console.error('加载消息失败:', e)
    }
  }

  /** 创建或获取会话 */
  async function createConversation(data: { jobId?: number; hrId: number; jobTitle?: string; enterpriseName?: string }): Promise<Conversation | null> {
    try {
      const res: any = await chatApi.createConversation(data)
      const conv: Conversation = res.data
      await fetchConversations()
      return conv
    } catch (e) {
      console.error('创建会话失败:', e)
      return null
    }
  }

  /** 发送消息 */
  async function sendMessage(conversationId: number, content: string): Promise<boolean> {
    if (!content.trim()) return false
    sending.value = true
    try {
      const res: any = await chatApi.sendMessage({ conversationId, content })
      const msg: ChatMessage = res.data
      // 添加到本地消息
      const list = messages.value.get(conversationId) || []
      list.push(msg)
      messages.value.set(conversationId, list)
      // 更新会话列表的最后一条消息
      const conv = conversations.value.find(c => c.id === conversationId)
      if (conv) {
        conv.lastMessage = content.length > 50 ? content.substring(0, 50) + '...' : content
        conv.lastMessageAt = msg.createdAt
      }
      return true
    } catch (e) {
      console.error('发送消息失败:', e)
      return false
    } finally {
      sending.value = false
    }
  }

  /** 轮询新消息（替代WebSocket） */
  let pollTimer: ReturnType<typeof setInterval> | null = null

  function startPolling() {
    stopPolling()
    fetchUnreadCount()
    pollTimer = setInterval(async () => {
      // 轮询未读数
      await fetchUnreadCount()
      // 如果当前在聊天页面，也刷新消息
      if (activeConversationId.value) {
        await fetchMessages(activeConversationId.value)
      }
    }, 3000) // 每3秒轮询
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  async function fetchUnreadCount() {
    try {
      const res: any = await chatApi.getUnreadCount()
      unreadTotal.value = res.data?.total || 0
    } catch (e) {
      // 静默失败
    }
  }

  /** 标记已读 */
  async function markAsRead(conversationId: number) {
    try {
      await chatApi.markAsRead(conversationId)
      const conv = conversations.value.find(c => c.id === conversationId)
      if (conv) conv.unreadCount = 0
      unreadTotal.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
    } catch (e) {
      console.error('标记已读失败:', e)
    }
  }

  /** 关闭会话 */
  async function closeConversation(conversationId: number) {
    try {
      await chatApi.closeConversation(conversationId)
      await fetchConversations()
    } catch (e) {
      console.error('关闭会话失败:', e)
    }
  }

  return {
    conversations, messages, activeConversationId, unreadTotal, loading, sending,
    activeConversation, activeMessages,
    setActiveConversation, fetchConversations, fetchMessages, createConversation,
    sendMessage, startPolling, stopPolling, fetchUnreadCount, markAsRead, closeConversation
  }
})
