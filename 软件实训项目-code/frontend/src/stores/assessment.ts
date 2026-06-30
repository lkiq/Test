import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** 测评状态管理 */
export const useAssessmentStore = defineStore('assessment', () => {
  const questions = ref<any[]>([])
  const currentIndex = ref(0)
  const answers = ref<Map<number, string>>(new Map())
  const result = ref<any>(null)

  // ===== 计时器状态 =====
  /** 开始时间戳 */
  const startTime = ref<number>(0)
  /** 限时秒数（默认25分钟） */
  const timeLimit = ref(1500)
  /** 剩余秒数 */
  const remainingTime = ref(1500)
  /** 定时器引用 */
  let timerInterval: ReturnType<typeof setInterval> | null = null

  /** 已答题目数量 */
  const answeredCount = computed(() => answers.value.size)

  /** 未答题目数量 */
  const unansweredCount = computed(() => questions.value.length - answers.value.size)

  /** 已答题目索引列表（用于导航圆点状态） */
  const answeredIndices = computed(() => {
    const indices = new Set<number>()
    answers.value.forEach((_, questionId) => {
      const idx = questions.value.findIndex(q => q.id === questionId)
      if (idx >= 0) indices.add(idx)
    })
    return indices
  })

  /** 剩余时间格式化 MM:SS */
  const formattedTime = computed(() => {
    const mins = Math.floor(remainingTime.value / 60)
    const secs = remainingTime.value % 60
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  })

  /** 倒计时进度百分比 */
  const timeProgress = computed(() => {
    if (timeLimit.value <= 0) return 100
    return Math.round((remainingTime.value / timeLimit.value) * 100)
  })

  /** 倒计时颜色（绿→黄→红） */
  const timeColor = computed(() => {
    const p = timeProgress.value
    if (p > 50) return '#10b981'  // 绿色
    if (p > 20) return '#f59e0b'  // 黄色
    return '#ef4444'                // 红色
  })

  function setQuestions(list: any[]) {
    questions.value = list
    currentIndex.value = 0
    answers.value = new Map()
    // 初始化计时器
    startTime.value = Date.now()
    remainingTime.value = timeLimit.value
    startTimer()
  }

  function setAnswer(questionId: number, answer: string) {
    answers.value.set(questionId, answer)
  }

  function setResult(data: any) {
    result.value = data
  }

  /** 启动倒计时 */
  function startTimer() {
    clearTimer()
    timerInterval = setInterval(() => {
      remainingTime.value--
      if (remainingTime.value <= 0) {
        remainingTime.value = 0
        clearTimer()
      }
    }, 1000)
  }

  /** 清除定时器 */
  function clearTimer() {
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }

  /** 是否时间到 */
  function isTimeUp(): boolean {
    return remainingTime.value <= 0
  }

  function reset() {
    clearTimer()
    questions.value = []
    currentIndex.value = 0
    answers.value = new Map()
    result.value = null
    startTime.value = 0
    remainingTime.value = timeLimit.value
  }

  return {
    questions, currentIndex, answers, result,
    startTime, timeLimit, remainingTime,
    answeredCount, unansweredCount, answeredIndices,
    formattedTime, timeProgress, timeColor,
    setQuestions, setAnswer, setResult,
    startTimer, clearTimer, isTimeUp, reset
  }
})
