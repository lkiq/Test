<template>
  <div class="recommend-page">
    <!-- Hero Banner -->
    <div class="rm-hero">
      <div class="rmh-glow"></div>
      <div class="rmh-content">
        <div class="rmh-icon-wrap">
          <div class="rmh-icon-circle">
            <el-icon :size="28"><MagicStick /></el-icon>
          </div>
        </div>
        <h1>AI 人才推荐</h1>
        <p>输入项目需求，AI 将智能分析并推荐最匹配的岗位方案和候选人</p>
        <div class="rmh-dots">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="rm-input-card">
      <div class="rmi-label">
        <span class="rmi-step">STEP 1</span>
        <h3>描述项目需求</h3>
        <el-radio-group v-model="inputMode" size="small" class="rmi-mode-switch">
          <el-radio-button value="text">文字描述</el-radio-button>
          <el-radio-button value="pdf">上传 PDF</el-radio-button>
        </el-radio-group>
        <el-button text type="primary" class="rmi-history-btn" @click="showHistory = true">
          <el-icon><Clock /></el-icon> 历史推荐
        </el-button>
      </div>

      <!-- 筛选条件（可折叠） -->
      <div class="rmi-filters">
        <el-button text type="primary" size="small" @click="filterExpanded = !filterExpanded">
          <el-icon style="margin-right:4px"><Filter /></el-icon>
          {{ filterExpanded ? '收起' : '展开' }}筛选条件（可选）
        </el-button>
        <el-collapse-transition>
          <div v-show="filterExpanded" class="rmi-filter-panel">
            <div class="rmi-filter-row">
              <span class="rmi-filter-label">学历要求</span>
              <el-select v-model="filters.education" placeholder="不限" clearable size="small" style="width:140px">
                <el-option label="不限" value="" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
                <el-option label="大专" value="大专" />
              </el-select>
            </div>
            <div class="rmi-filter-row">
              <span class="rmi-filter-label">期望城市</span>
              <el-input v-model="filters.city" placeholder="如：深圳" size="small" style="width:140px" clearable />
            </div>
            <div class="rmi-filter-row">
              <span class="rmi-filter-label">偏好技能</span>
              <el-input v-model="filters.skillPreference" placeholder="如：Java,Spring" size="small" style="width:200px" clearable />
            </div>
          </div>
        </el-collapse-transition>
      </div>

      <!-- 文字输入模式 -->
      <div class="rmi-body" v-if="inputMode === 'text'">
        <el-input
          v-model="projectDesc"
          type="textarea"
          :rows="4"
          placeholder="请详细描述您的项目需求（不少于20字），例如：我们需要开发一款面向大学生群体的IT求职平台，包含岗位推荐、能力测评、AI模拟面试等功能，前后端分离架构..."
          class="rmi-textarea"
        />
        <div class="rmi-footer">
          <span class="rmi-count" :class="{ valid: projectDesc.length >= 20 }">
            {{ projectDesc.length }} / 20 字 {{ projectDesc.length >= 20 ? '✓' : '(最少20字)' }}
          </span>
          <el-button
            type="primary"
            size="large"
            @click="submitRecommend"
            :loading="loading"
            :disabled="projectDesc.length < 20"
            class="rmi-submit"
          >
            <el-icon style="margin-right:6px"><MagicStick /></el-icon>AI 智能分析
          </el-button>
        </div>
      </div>

      <!-- PDF 上传模式 -->
      <div class="rmi-body" v-else>
        <div
          class="rmi-pdf-upload"
          :class="{ dragover: pdfDragOver, hasFile: !!pdfFile }"
          @click="triggerFileInput"
          @dragover.prevent="pdfDragOver = true"
          @dragleave.prevent="pdfDragOver = false"
          @drop.prevent="handlePdfDrop"
        >
          <input
            ref="fileInputRef"
            type="file"
            accept="application/pdf"
            style="display:none"
            @change="handlePdfSelect"
          />
          <template v-if="!pdfFile">
            <el-icon :size="40" color="#6366f1"><UploadFilled /></el-icon>
            <p class="rmi-pdf-title">点击上传或拖拽 PDF 文件到此处</p>
            <p class="rmi-pdf-hint">上传项目需求文档，AI 将自动提取文本并进行分析</p>
            <p class="rmi-pdf-limit">支持 .pdf 格式，文件大小不超过 10MB</p>
          </template>
          <template v-else>
            <div class="rmi-pdf-file">
              <el-icon :size="32" color="#ef4444"><Document /></el-icon>
              <div class="rmi-pdf-info">
                <span class="rmi-pdf-name">{{ pdfFile.name }}</span>
                <span class="rmi-pdf-size">{{ formatFileSize(pdfFile.size) }}</span>
              </div>
              <el-button
                text
                type="danger"
                size="small"
                @click.stop="removePdf"
                :disabled="loading"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </template>
        </div>
        <div class="rmi-footer">
          <span class="rmi-count" v-if="pdfFile" style="color:#6366f1">
            <el-icon style="vertical-align:middle"><Document /></el-icon>
            已选择 PDF 文件
          </span>
          <span class="rmi-count" v-else style="color:#9ca3af">请选择一个 PDF 文件</span>
          <el-button
            type="primary"
            size="large"
            @click="submitPdfRecommend"
            :loading="loading"
            :disabled="!pdfFile"
            class="rmi-submit"
          >
            <el-icon style="margin-right:6px"><MagicStick /></el-icon>上传并 AI 分析
          </el-button>
        </div>
      </div>
    </div>

    <!-- 加载状态（分步提示） -->
    <div v-if="loading" class="rm-loading">
      <div class="rm-loading-hint">
        <el-icon :size="36" color="#8b5cf6" class="rm-loading-icon"><Loading /></el-icon>
        <p class="rm-loading-step">{{ loadingStep }}</p>
        <div class="rm-loading-steps">
          <div class="rmls-dot" :class="{ active: loadingStepIndex >= 0 }">解析项目需求</div>
          <div class="rmls-line" :class="{ active: loadingStepIndex >= 1 }"></div>
          <div class="rmls-dot" :class="{ active: loadingStepIndex >= 1 }">匹配候选人</div>
          <div class="rmls-line" :class="{ active: loadingStepIndex >= 2 }"></div>
          <div class="rmls-dot" :class="{ active: loadingStepIndex >= 2 }">生成推荐结果</div>
        </div>
      </div>
      <div class="rm-skeleton-card" v-for="i in 2" :key="i">
        <el-skeleton :rows="4" animated />
      </div>
    </div>

    <!-- 推荐结果 -->
    <template v-if="result && !loading">
      <!-- 来源标记 -->
      <div class="rm-source-bar" v-if="result.source">
        <el-tag :type="result.source === 'AI' ? 'success' : 'warning'" size="small" effect="plain" round>
          {{ result.source === 'AI' ? '🤖 AI 智能解析' : '📋 本地匹配' }}
        </el-tag>
        <span class="rm-source-summary" v-if="result.projectSummary">{{ result.projectSummary }}</span>
      </div>

      <!-- 推荐岗位 -->
      <div class="rm-section" v-if="result.positions?.length">
        <div class="rm-section-header">
          <div class="rmsh-left">
            <div class="rmsh-icon positions">
              <el-icon :size="22"><Briefcase /></el-icon>
            </div>
            <div class="rmsh-text">
              <h3>推荐岗位方案</h3>
              <p>基于需求分析生成的岗位配置</p>
            </div>
          </div>
          <span class="rmsh-count">{{ result.positions.length }} 个岗位</span>
        </div>

        <div class="rm-pos-grid">
          <div v-for="(pos, idx) in result.positions" :key="idx" class="rm-pos-card">
            <div class="rmpc-top">
              <h4 class="rmpc-title">{{ pos.positionTitle }}</h4>
              <el-tag type="warning" size="small" effect="dark" round>需求 {{ pos.headcount || 1 }} 人</el-tag>
            </div>
            <div class="rmpc-skills" v-if="pos.skillRequirements?.length">
              <el-tag
                v-for="r in pos.skillRequirements"
                :key="r.skillName"
                size="small"
                effect="plain"
                round
                :type="reqTagType(r.requiredLevel)"
              >
                {{ r.skillName }}
                <span style="opacity:0.7;margin-left:2px">{{ r.requiredLevel }}</span>
              </el-tag>
            </div>
            <div class="rmpc-footer">
              <el-button size="small" type="primary" round plain @click="$router.push('/enterprise/jobs')">
                发布此岗位
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 候选人推荐 -->
      <div class="rm-section" v-if="result.candidates?.length">
        <div class="rm-section-header">
          <div class="rmsh-left">
            <div class="rmsh-icon candidates">
              <el-icon :size="22"><UserFilled /></el-icon>
            </div>
            <div class="rmsh-text">
              <h3>匹配候选人</h3>
              <p>AI 多维度评分推荐 · 按匹配分降序</p>
            </div>
          </div>
          <span class="rmsh-count">{{ result.candidates.length }} 位候选人</span>
        </div>

        <div class="rm-cand-list">
          <div v-for="cand in result.candidates" :key="cand.userId || cand.username" class="rm-cand-card">
            <div class="rmcc-header">
              <div class="rmcc-avatar" :style="{ background: avatarBg(cand.username) }">
                {{ (cand.username || '?')[0]?.toUpperCase() }}
              </div>
              <div class="rmcc-name-block">
                <div class="rmcc-name-line">
                  <span class="rmcc-name">{{ cand.username || '未知用户' }}</span>
                  <el-tag
                    :type="matchLevelType(cand.matchLevel)"
                    size="small"
                    effect="plain"
                    round
                    class="rmcc-level-tag"
                  >
                    {{ cand.matchLevel || '' }}
                  </el-tag>
                </div>
                <div class="rmcc-meta" v-if="cand.education || cand.school">
                  {{ [cand.education, cand.school, cand.major].filter(Boolean).join(' · ') || '' }}
                </div>
              </div>
              <div class="rmcc-right">
                <div class="rmcc-match-badge" :style="{ background: matchGradient(cand.matchScore) }">
                  {{ cand.matchScore || 0 }}% 匹配
                </div>
                <el-button type="primary" size="small" round @click="$router.push('/chat')">
                  <el-icon style="margin-right:4px"><ChatDotRound /></el-icon>沟通
                </el-button>
              </div>
            </div>

            <!-- 技能标签：已匹配 + 缺口 -->
            <div class="rmcc-skill-tags" v-if="cand.matchedSkills?.length || cand.gapSkills?.length">
              <el-tag
                v-for="s in cand.matchedSkills"
                :key="s"
                size="small"
                effect="plain"
                round
                class="rmcct-matched"
              >
                ✓ {{ s }}
              </el-tag>
              <el-tag
                v-for="s in cand.gapSkills"
                :key="s"
                size="small"
                effect="plain"
                round
                class="rmcct-gap"
              >
                ✗ {{ s }}
              </el-tag>
            </div>

            <p class="rmcc-reason" v-if="cand.recommendReason">{{ cand.recommendReason }}</p>

            <div class="rmcc-scores">
              <div class="rmcc-score-row">
                <span class="rmcc-score-label">技能匹配</span>
                <div class="rmcc-score-track">
                  <div class="rmcc-score-fill" :style="{ width: (cand.skillScore || 0) + '%', background: '#3b82f6' }"></div>
                </div>
                <span class="rmcc-score-pct">{{ cand.skillScore || 0 }}%</span>
              </div>
              <div class="rmcc-score-row">
                <span class="rmcc-score-label">测评适配</span>
                <div class="rmcc-score-track">
                  <div class="rmcc-score-fill" :style="{ width: (cand.assessmentScore || 0) + '%', background: '#8b5cf6' }"></div>
                </div>
                <span class="rmcc-score-pct">{{ cand.assessmentScore || 0 }}%</span>
              </div>
              <div class="rmcc-score-row">
                <span class="rmcc-score-label">学习进度</span>
                <div class="rmcc-score-track">
                  <div class="rmcc-score-fill" :style="{ width: (cand.learningScore || 0) + '%', background: '#10b981' }"></div>
                </div>
                <span class="rmcc-score-pct">{{ cand.learningScore || 0 }}%</span>
              </div>
              <div class="rmcc-score-row">
                <span class="rmcc-score-label">成果测评</span>
                <div class="rmcc-score-track">
                  <div class="rmcc-score-fill" :style="{ width: (cand.learningResultScore || 0) + '%', background: '#ec4899' }"></div>
                </div>
                <span class="rmcc-score-pct">{{ cand.learningResultScore || 0 }}%</span>
              </div>
              <div class="rmcc-score-row">
                <span class="rmcc-score-label">基础匹配</span>
                <div class="rmcc-score-track">
                  <div class="rmcc-score-fill" :style="{ width: (cand.basicScore || 0) + '%', background: '#f59e0b' }"></div>
                </div>
                <span class="rmcc-score-pct">{{ cand.basicScore || 0 }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态引导 -->
    <div v-else-if="!loading" class="rm-empty">
      <div class="rme-illustration">
        <div class="rme-ring r1"></div>
        <div class="rme-ring r2"></div>
        <div class="rme-icon-big">🔮</div>
      </div>
      <h3>AI 驱动的人才匹配</h3>
      <p>描述您的项目需求，让 AI 为您找到最合适的人才</p>
      <div class="rme-features">
        <div class="rme-f-card">
          <el-icon :size="18" color="#165DFF"><MagicStick /></el-icon>
          <span>智能需求分析</span>
        </div>
        <div class="rme-f-card">
          <el-icon :size="18" color="#00B42A"><Aim /></el-icon>
          <span>岗位自动匹配</span>
        </div>
        <div class="rme-f-card">
          <el-icon :size="18" color="#722ED1"><UserFilled /></el-icon>
          <span>候选人精准推荐</span>
        </div>
      </div>
    </div>

    <!-- ===== 推荐历史抽屉 ===== -->
    <el-drawer
      v-model="showHistory"
      title="历史推荐记录"
      size="420px"
      direction="rtl"
    >
      <div v-if="historyLoading" style="text-align:center;padding:40px">
        <el-icon :size="32" class="is-loading"><Loading /></el-icon>
        <p style="color:#9ca3af; margin-top:12px">加载中...</p>
      </div>
      <div v-else-if="!historyList.length" class="rmh-empty">
        <el-empty description="暂无推荐记录" :image-size="80" />
      </div>
      <div v-else class="rmh-list">
        <div
          v-for="item in historyList"
          :key="item.createdAt"
          class="rmh-item"
          @click="loadHistoryResult(item)"
        >
          <div class="rmhi-header">
            <el-tag :type="item.source === 'AI' ? 'success' : 'warning'" size="small" round>
              {{ item.source === 'AI' ? 'AI' : '本地' }}
            </el-tag>
            <span class="rmhi-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <p class="rmhi-preview">{{ truncate(item.projectSummary || '点击查看详情', 60) }}</p>
          <div class="rmhi-meta">
            <span>{{ item.positions?.length || 0 }} 个岗位</span>
            <span>·</span>
            <span>{{ item.candidates?.length || 0 }} 位候选人</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Briefcase, UserFilled, ChatDotRound, Aim, Clock, Filter, Loading, UploadFilled, Document, Close } from '@element-plus/icons-vue'
import { recommend, recommendByPdf, getRecommendHistory } from '@/api/enterprise'

const inputMode = ref<'text' | 'pdf'>('text')
const projectDesc = ref('')
const pdfFile = ref<File | null>(null)
const pdfDragOver = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const loading = ref(false)
const loadingStepIndex = ref(0)
const loadingStep = ref('正在解析项目需求...')
const result = ref<any>(null)
const filterExpanded = ref(false)
const filters = ref({ education: '', city: '', skillPreference: '' })

// 历史记录
const showHistory = ref(false)
const historyLoading = ref(false)
const historyList = ref<any[]>([])

// 分步加载提示
const stepTexts = ['正在解析项目需求...', '正在匹配候选人...', '正在生成推荐结果...']
let loadingTimer: any = null

function startLoadingSteps() {
  loadingStepIndex.value = 0
  loadingStep.value = stepTexts[0]
  let i = 0
  loadingTimer = setInterval(() => {
    i++
    if (i < stepTexts.length) {
      loadingStepIndex.value = i
      loadingStep.value = stepTexts[i]
    } else {
      clearInterval(loadingTimer)
    }
  }, 2500)
}

function stopLoadingSteps() {
  if (loadingTimer) {
    clearInterval(loadingTimer)
    loadingTimer = null
  }
}

// 监听抽屉打开，加载历史
watch(showHistory, (v) => {
  if (v) loadHistory()
})

async function loadHistory() {
  historyLoading.value = true
  try {
    const res: any = await getRecommendHistory()
    historyList.value = res.data || []
  } finally {
    historyLoading.value = false
  }
}

function loadHistoryResult(item: any) {
  result.value = item
  showHistory.value = false
}

const avatarColors = ['#165DFF', '#722ED1', '#00B42A', '#FF7D00', '#F53F3F', '#7B61FF', '#16C8C8']
function avatarBg(name: string): string {
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

function reqTagType(level: string): string {
  if (level === '精通' || level === '熟练') return 'success'
  if (level === '掌握') return 'warning'
  return 'danger'
}

function matchLevelType(level: string): string {
  if (level === '非常匹配') return 'success'
  if (level === '比较匹配') return 'primary'
  if (level === '一般匹配') return 'warning'
  if (level === '部分匹配') return 'info'
  return 'danger'
}

function matchGradient(score: number): string {
  if (score >= 80) return 'linear-gradient(90deg, #10b981, #34d399)'
  if (score >= 60) return 'linear-gradient(90deg, #3b82f6, #60a5fa)'
  if (score >= 40) return 'linear-gradient(90deg, #f59e0b, #fbbf24)'
  return 'linear-gradient(90deg, #ef4444, #f87171)'
}

function formatTime(t: string): string {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function truncate(s: string, n: number): string {
  if (!s) return ''
  return s.length > n ? s.slice(0, n) + '...' : s
}

async function submitRecommend() {
  if (projectDesc.value.length < 20) return
  loading.value = true
  result.value = null
  startLoadingSteps()
  try {
    const body: any = { projectDescription: projectDesc.value }
    const f: any = {}
    if (filters.value.education) f.education = filters.value.education
    if (filters.value.city) f.city = filters.value.city
    if (filters.value.skillPreference) f.skillPreference = filters.value.skillPreference
    if (Object.keys(f).length > 0) body.filters = f

    const res: any = await recommend(body)
    result.value = res.data
  } finally {
    stopLoadingSteps()
    loading.value = false
  }
}

// ===== PDF 上传相关 =====
function triggerFileInput() {
  fileInputRef.value?.click()
}

function handlePdfDrop(e: DragEvent) {
  pdfDragOver.value = false
  const files = e.dataTransfer?.files
  if (files?.length) setPdfFile(files[0])
}

function handlePdfSelect(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files?.length) setPdfFile(target.files[0])
}

function setPdfFile(file: File) {
  if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
    ElMessage.warning('仅支持 PDF 格式文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB')
    return
  }
  pdfFile.value = file
}

function removePdf() {
  pdfFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function submitPdfRecommend() {
  if (!pdfFile.value) return
  loading.value = true
  result.value = null
  startLoadingSteps()
  try {
    const f: any = {}
    if (filters.value.education) f.education = filters.value.education
    if (filters.value.city) f.city = filters.value.city
    if (filters.value.skillPreference) f.skillPreference = filters.value.skillPreference
    const filterParam = Object.keys(f).length > 0 ? f : undefined

    const res: any = await recommendByPdf(pdfFile.value, filterParam)
    result.value = res.data
    ElMessage.success('PDF 分析完成，已生成推荐结果')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || 'PDF 分析失败，请重试')
  } finally {
    stopLoadingSteps()
    loading.value = false
  }
}
</script>

<style scoped>
.recommend-page { max-width: 1000px; margin: 0 auto; padding-bottom: 32px; }

/* ===== Hero ===== */
.rm-hero {
  position: relative;
  background: linear-gradient(135deg, #0f172a, #1e1b4b, #312e81);
  border-radius: 18px; padding: 36px 40px;
  margin-bottom: 24px; overflow: hidden; text-align: center;
}
.rmh-glow {
  position: absolute; top: -40%; left: 50%; transform: translateX(-50%);
  width: 400px; height: 400px; border-radius: 50%;
  background: radial-gradient(circle, rgba(139,92,246,0.2), transparent);
  pointer-events: none;
}
.rmh-content { position: relative; z-index: 1; }
.rmh-icon-wrap { margin-bottom: 12px; }
.rmh-icon-circle {
  width: 56px; height: 56px; border-radius: 16px;
  background: rgba(255,255,255,0.12);
  display: inline-flex; align-items: center; justify-content: center;
  color: #c7d2fe; backdrop-filter: blur(10px);
}
.rm-hero h1 { font-size: 24px; font-weight: 800; color: #fff; margin: 0 0 6px; }
.rm-hero p { color: #c7d2fe; font-size: 14px; margin: 0; }
.rmh-dots { display: flex; gap: 6px; justify-content: center; margin-top: 14px; }
.rmh-dots .dot {
  width: 6px; height: 6px; border-radius: 50%; background: rgba(255,255,255,0.3);
}

/* ===== 输入区 ===== */
.rm-input-card {
  background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0; margin-bottom: 24px;
}
.rmi-label { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.rmi-step {
  padding: 4px 12px; border-radius: 8px;
  background: #eef2ff; color: #4f46e5;
  font-size: 12px; font-weight: 700;
}
.rmi-label h3 { font-size: 17px; font-weight: 700; color: #1f2937; margin: 0; flex: 1; }
.rmi-mode-switch { flex-shrink: 0; margin-right: 8px; }
.rmi-history-btn { font-size: 13px; font-weight: 500; }
.rmi-filters { margin-bottom: 12px; }
.rmi-filter-panel {
  display: flex; flex-wrap: wrap; gap: 14px; margin-top: 10px;
  padding: 14px 16px; background: #f9fafb; border-radius: 10px;
}
.rmi-filter-row { display: flex; align-items: center; gap: 8px; }
.rmi-filter-label { font-size: 13px; color: #6b7280; width: 56px; }

.rmi-textarea :deep(.el-textarea__inner) {
  border-radius: 12px; font-size: 14px; line-height: 1.7;
}
/* PDF 上传区域 */
.rmi-pdf-upload {
  border: 2px dashed #d1d5db;
  border-radius: 14px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafbfc;
}
.rmi-pdf-upload:hover,
.rmi-pdf-upload.dragover {
  border-color: #818cf8;
  background: #eef2ff;
}
.rmi-pdf-upload.hasFile {
  border-style: solid;
  border-color: #6366f1;
  background: #f5f3ff;
  padding: 24px 20px;
}
.rmi-pdf-title { font-size: 16px; font-weight: 600; color: #374151; margin: 12px 0 6px; }
.rmi-pdf-hint { font-size: 13px; color: #9ca3af; margin: 0 0 8px; }
.rmi-pdf-limit { font-size: 12px; color: #d1d5db; margin: 0; }
.rmi-pdf-file {
  display: flex; align-items: center; gap: 14px;
}
.rmi-pdf-info { display: flex; flex-direction: column; flex: 1; text-align: left; }
.rmi-pdf-name { font-size: 14px; font-weight: 600; color: #1f2937; }
.rmi-pdf-size { font-size: 12px; color: #9ca3af; }
.rmi-footer {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 16px; flex-wrap: wrap; gap: 12px;
}
.rmi-count { font-size: 13px; color: #9ca3af; }
.rmi-count.valid { color: #10b981; }
.rmi-submit {
  height: 44px; border-radius: 12px; font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #8b5cf6) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(99,102,241,0.35);
}
.rmi-submit:hover {
  box-shadow: 0 6px 20px rgba(99,102,241,0.45);
  transform: translateY(-1px);
}

/* ===== 来源标记 ===== */
.rm-source-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; background: #f9fafb; border-radius: 10px;
  margin-bottom: 16px; font-size: 13px;
}
.rm-source-summary { color: #6b7280; }

/* ===== 加载（分步提示） ===== */
.rm-loading { display: flex; flex-direction: column; gap: 12px; }
.rm-loading-hint {
  text-align: center; padding: 24px;
  background: #fff; border-radius: 14px; border: 1px solid #f0f0f0;
}
.rm-loading-icon { animation: spin 1.2s linear infinite; margin-bottom: 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
.rm-loading-step { font-size: 14px; color: #6b7280; margin: 0 0 16px; }
.rm-loading-steps {
  display: flex; align-items: center; justify-content: center; gap: 0; font-size: 12px;
}
.rmls-dot {
  padding: 4px 12px; border-radius: 12px;
  background: #f3f4f6; color: #9ca3af; transition: all 0.4s;
}
.rmls-dot.active { background: #eef2ff; color: #4f46e5; font-weight: 600; }
.rmls-line {
  width: 32px; height: 2px; background: #e5e7eb; margin: 0 2px; transition: all 0.4s;
}
.rmls-line.active { background: #818cf8; }

.rm-skeleton-card {
  background: #fff; border-radius: 14px; padding: 20px 24px;
  border: 1px solid #f0f0f0;
}

/* ===== 区块 ===== */
.rm-section { margin-bottom: 24px; }
.rm-section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.rmsh-left { display: flex; align-items: center; gap: 14px; }
.rmsh-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.rmsh-icon.positions { background: #e8f1ff; color: #165DFF; }
.rmsh-icon.candidates { background: #f5e8ff; color: #722ED1; }
.rmsh-text h3 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0; }
.rmsh-text p { font-size: 12px; color: #9ca3af; margin: 2px 0 0; }
.rmsh-count {
  padding: 4px 14px; border-radius: 20px;
  background: #f3f4f6; font-size: 13px; color: #374151; font-weight: 600;
}

/* ===== 岗位卡片 ===== */
.rm-pos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.rm-pos-card {
  background: #fff; border-radius: 14px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}
.rm-pos-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(99,102,241,0.1); }
.rmpc-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.rmpc-title { font-size: 15px; font-weight: 700; color: #1f2937; margin: 0; }
.rmpc-skills { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.rmpc-footer { display: flex; justify-content: flex-end; }

/* ===== 候选人卡片 ===== */
.rm-cand-list { display: flex; flex-direction: column; gap: 10px; }
.rm-cand-card {
  background: #fff; border-radius: 14px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}
.rm-cand-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.rmcc-header { display: flex; align-items: center; gap: 14px; margin-bottom: 8px; }
.rmcc-avatar {
  width: 44px; height: 44px; border-radius: 14px; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.rmcc-name-block { flex: 1; }
.rmcc-name-line { display: flex; align-items: center; gap: 8px; }
.rmcc-name { font-size: 15px; font-weight: 700; color: #1f2937; }
.rmcc-level-tag { font-size: 11px; }
.rmcc-meta { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.rmcc-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.rmcc-match-badge {
  padding: 3px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 700; color: #fff; white-space: nowrap;
}

/* 技能标签 */
.rmcc-skill-tags { display: flex; flex-wrap: wrap; gap: 5px; margin-bottom: 10px; }
.rmcct-matched { color: #059669 !important; background: #ecfdf5 !important; border-color: #a7f3d0 !important; }
.rmcct-gap { color: #d97706 !important; background: #fffbeb !important; border-color: #fde68a !important; }

.rmcc-reason { font-size: 13px; color: #6b7280; margin: 0 0 12px; line-height: 1.5; }

.rmcc-scores { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px 16px; }
.rmcc-score-row { display: flex; align-items: center; gap: 8px; }
.rmcc-score-label { font-size: 12px; color: #9ca3af; width: 60px; flex-shrink: 0; }
.rmcc-score-track {
  flex: 1; height: 6px; background: #e5e7eb; border-radius: 3px; overflow: hidden;
}
.rmcc-score-fill { height: 100%; border-radius: 3px; transition: width 1.2s ease-out; }
.rmcc-score-pct { font-size: 12px; font-weight: 700; color: #374151; width: 34px; text-align: right; }

/* ===== 空状态 ===== */
.rm-empty {
  text-align: center; padding: 80px 40px;
  background: #fff; border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid #f0f0f0;
}
.rme-illustration {
  position: relative; display: inline-block; margin-bottom: 24px;
}
.rme-icon-big { width: 80px; height: 80px; border-radius: 22px; background: linear-gradient(135deg, #eef2ff, #ede9fe); display: flex; align-items: center; justify-content: center; font-size: 36px; }
.rme-ring { position: absolute; inset: -12px; border-radius: 34px; border: 2px solid #c7d2fe; animation: ringPulse 2.5s ease-out infinite; }
.rme-ring.r2 { inset: -24px; animation-delay: 0.5s; }
@keyframes ringPulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.2); opacity: 0; }
}
.rm-empty h3 { font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.rm-empty p { color: #6b7280; font-size: 14px; margin-bottom: 20px; }
.rme-features { display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; }
.rme-f-card {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #f9fafb; border-radius: 10px;
  border: 1px solid #e5e7eb; font-size: 13px; color: #374151;
  transition: all 0.2s;
}
.rme-f-card:hover { border-color: #c7d2fe; background: #eef2ff; }

/* ===== 历史抽屉 ===== */
.rmh-empty { padding: 40px; }
.rmh-list { display: flex; flex-direction: column; gap: 8px; }
.rmh-item {
  padding: 14px 16px; border-radius: 10px;
  border: 1px solid #e5e7eb; cursor: pointer;
  transition: all 0.2s;
}
.rmh-item:hover { border-color: #818cf8; background: #fafaff; }
.rmhi-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.rmhi-time { font-size: 12px; color: #9ca3af; }
.rmhi-preview { font-size: 13px; color: #374151; margin: 0 0 4px; line-height: 1.4; }
.rmhi-meta { font-size: 12px; color: #9ca3af; display: flex; gap: 6px; }

@media (max-width: 768px) {
  .rm-hero { padding: 28px 20px; }
  .rm-pos-grid { grid-template-columns: 1fr; }
  .rmcc-scores { grid-template-columns: 1fr; }
  .rmi-filter-panel { flex-direction: column; gap: 10px; }
}
</style>
