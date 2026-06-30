import request from './request'
import type { Conversation, ChatMessage, UnreadCount } from '@/types/chat'

/** 创建/获取会话 */
export const createConversation = (data: { jobId?: number; hrId: number; jobTitle?: string; enterpriseName?: string }) =>
  request.post('/chat/conversations', data)

/** 发送消息 */
export const sendMessage = (data: { conversationId: number; content: string; msgType?: string }) =>
  request.post('/chat/messages', data)

/** 获取会话列表 */
export const getConversations = () =>
  request.get('/chat/conversations')

/** 获取会话历史消息 */
export const getMessages = (conversationId: number, page = 1, size = 50) =>
  request.get(`/chat/conversations/${conversationId}/messages`, { params: { page, size } })

/** 标记会话已读 */
export const markAsRead = (conversationId: number) =>
  request.put(`/chat/conversations/${conversationId}/read`)

/** 获取未读消息总数 */
export const getUnreadCount = () =>
  request.get('/chat/unread-count')

/** 关闭会话 */
export const closeConversation = (conversationId: number) =>
  request.put(`/chat/conversations/${conversationId}/close`)
