/** 聊天消息类型 */
export interface ChatMessage {
  id: number
  conversationId: number
  senderId: number
  senderName: string
  senderRole: 'STUDENT' | 'HR'
  receiverId: number
  content: string
  msgType: 'TEXT' | 'IMAGE' | 'FILE'
  fileUrl?: string
  isRead: number
  createdAt: string
}

/** 会话类型 */
export interface Conversation {
  id: number
  studentId: number
  studentName?: string
  hrId: number
  hrName?: string
  jobId: number | null
  jobTitle: string
  enterpriseName: string
  lastMessage: string
  lastMessageAt: string
  unreadCount: number
  status: number
  createdAt: string
}

/** 未读数 */
export interface UnreadCount {
  total: number
}
