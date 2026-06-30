<template>
  <div class="resume-page">
    <!-- Hero -->
    <div class="resume-hero">
      <div class="rh-shapes">
        <div class="rh-shape s1"></div><div class="rh-shape s2"></div><div class="rh-shape s3"></div>
      </div>
      <div class="rh-content">
        <div class="rh-icon-wrap">📄</div>
        <h1>简历智能优化</h1>
        <p>上传简历，AI将全方位分析并提供专业优化建议，助你脱颖而出</p>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：上传区 -->
      <el-col :lg="8" :md="10" :sm="24">
        <div class="upload-panel">
          <div class="up-header">
            <span class="up-step-badge">STEP 1</span>
            <h3>上传简历</h3>
          </div>

          <div class="upload-area" :class="{ 'has-file': fileUrl }">
            <template v-if="!fileUrl">
              <div class="upload-illustration">
                <div class="upload-icon-wrap">
                  <span class="upload-icon">📁</span>
                </div>
                <div class="upload-dashed"></div>
              </div>
              <p class="upload-title">点击或拖拽上传简历</p>
              <p class="upload-hint">支持 PDF、Word、TXT 格式</p>
              <div class="upload-divider"><span>或</span></div>
              <FileUpload @file-change="handleFile" />
            </template>
            <template v-else>
              <div class="upload-success">
                <div class="success-icon">✅</div>
                <p class="success-text">文件上传成功</p>
                <p class="success-hint">点击下方按钮开始AI智能分析</p>
              </div>
            </template>
          </div>

          <div class="up-step" v-if="fileUrl && !result">
            <span class="up-step-badge">STEP 2</span>
            <el-button type="primary" size="large" class="analyze-btn" @click="analyze" :loading="loading">
              <span class="ai-sparkle">✨</span> AI 智能分析
            </el-button>
          </div>

          <!-- 已保存的简历列表 -->
          <div class="resume-list" v-if="resumeList.length > 0">
            <h4 class="rl-title">
              <span>📋</span> 我的简历 <span class="rl-count">{{ resumeList.length }}份</span>
            </h4>
            <div class="rl-item" v-for="item in resumeList" :key="item.id"
                 :class="{ active: fileUrl === item.fileUrl }">
              <div class="rli-info" @click="selectResume(item)">
                <span class="rli-icon">{{ item.fileType === 'pdf' ? '📕' : '📘' }}</span>
                <div class="rli-text">
                  <span class="rli-name">{{ item.fileName }}</span>
                  <span class="rli-meta">{{ formatSize(item.fileSize) }} · {{ formatTime(item.createdAt) }}</span>
                </div>
              </div>
              <div class="rli-actions">
                <el-button link type="primary" size="small" @click="viewResumeFile(item)">
                  👁️ 查看
                </el-button>
                <el-popconfirm title="确定删除这份简历吗？" @confirm.stop="handleDelete(item.id)">
                  <template #reference>
                    <span class="rli-delete" @click.stop>🗑️</span>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>

          <!-- 快速特性 -->
          <div class="up-features">
            <div class="uf-item">
              <span class="uf-icon">🔍</span>
              <div><strong>深度分析</strong><p>AI读取简历全文</p></div>
            </div>
            <div class="uf-item">
              <span class="uf-icon">💡</span>
              <div><strong>精准建议</strong><p>逐项优化指导</p></div>
            </div>
            <div class="uf-item">
              <span class="uf-icon">⚡</span>
              <div><strong>秒级响应</strong><p>即时分析报告</p></div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：分析报告 -->
      <el-col :lg="16" :md="14" :sm="24">
        <div v-if="result" class="report-panel">
          <!-- 报告头部 -->
          <div class="rp-header">
            <div class="rph-left">
              <el-tag :type="result.source === 'AI' ? 'success' : ''" effect="dark" size="large">
                {{ result.source === 'AI' ? '🤖 AI分析' : '📋 系统分析' }}
              </el-tag>
              <h3>分析报告</h3>
            </div>
          </div>

          <!-- 评分仪表盘 -->
          <div class="rp-score-section">
            <div class="score-main">
              <svg class="score-big-ring" viewBox="0 0 180 180">
                <circle cx="90" cy="90" r="78" fill="none" stroke="#e5e7eb" stroke-width="14" />
                <circle cx="90" cy="90" r="78" fill="none" :stroke="scoreColor"
                  stroke-width="14" stroke-linecap="round"
                  :stroke-dasharray="((result.score || 0) * 4.9) + ' ' + (490 - (result.score || 0) * 4.9)"
                  transform="rotate(-90 90 90)"
                  class="score-ring-animate" />
              </svg>
              <div class="score-center">
                <span class="sc-value">{{ result.score || 0 }}</span>
                <span class="sc-label">综合评分</span>
              </div>
            </div>

            <!-- 维度分数 -->
            <div class="score-dimensions" v-if="result.dimensionScores">
              <div
                v-for="(v, k) in result.dimensionScores"
                :key="k"
                class="score-dim"
              >
                <div class="sd-header">
                  <span class="sd-name">{{ k }}</span>
                  <span class="sd-value" :style="{ color: dimColor(v) }">{{ v }}分</span>
                </div>
                <div class="sd-bar-wrap">
                  <div class="sd-bar">
                    <div class="sd-bar-fill" :style="{ width: v + '%', background: dimColor(v) }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 优化建议 -->
          <div class="rp-issues" v-if="result.issues?.length">
            <h4 class="rp-section-title">
              <span>🔧</span> 优化建议 <span class="issue-count">{{ result.issues.length }}项</span>
            </h4>
            <div class="issue-list">
              <div
                v-for="(issue, i) in result.issues"
                :key="i"
                class="issue-card"
                :class="'severity-' + (issue.severity || 'medium').toLowerCase()"
                :style="{ animationDelay: (i * 0.08) + 's' }"
              >
                <div class="ic-header">
                  <span class="ic-severity" :class="issue.severity">
                    {{ issue.severity }}
                  </span>
                  <span class="ic-desc">{{ issue.description }}</span>
                </div>
                <div class="ic-body">
                  <div class="ic-suggestion">
                    <span class="ic-label">💡 建议</span>
                    <p>{{ issue.suggestion }}</p>
                  </div>
                  <div class="ic-example" v-if="issue.exampleRewrite">
                    <span class="ic-label">✏️ 改写示例</span>
                    <p>{{ issue.exampleRewrite }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 总结 -->
          <div class="rp-summary" v-if="result.summary">
            <h4 class="rp-section-title"><span>📝</span> 综合总结</h4>
            <p>{{ result.summary }}</p>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="report-empty">
          <div class="re-illustration">
            <div class="re-doc-icon">📄</div>
            <div class="re-scan-line"></div>
          </div>
          <h3>等待简历分析</h3>
          <p>请先在左侧上传简历文件，AI将为你生成详细的分析报告</p>
          <div class="re-hints">
            <span class="re-hint">✓ 格式排版分析</span>
            <span class="re-hint">✓ 内容质量评估</span>
            <span class="re-hint">✓ 关键词优化</span>
            <span class="re-hint">✓ 技能呈现建议</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import FileUpload from '@/components/common/FileUpload.vue'
import { uploadResume, listResumes, deleteResume, analyzeResume as analyzeResumeApi, getResumeFileUrl } from '@/api/student'

const fileUrl = ref('')
const loading = ref(false)
const result = ref<any>(null)
const resumeList = ref<any[]>([])

const scoreColor = computed(() => {
  const s = result.value?.score || 0
  if (s >= 85) return '#10b981'
  if (s >= 70) return '#3b82f6'
  if (s >= 55) return '#f59e0b'
  return '#ef4444'
})

function dimColor(v: number): string {
  if (v >= 85) return '#10b981'
  if (v >= 70) return '#3b82f6'
  if (v >= 55) return '#f59e0b'
  return '#ef4444'
}

function formatSize(bytes: number): string {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function formatTime(time: string): string {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

onMounted(() => fetchList())

async function fetchList() {
  try {
    const res: any = await listResumes()
    resumeList.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function handleFile(file: File) {
  const res: any = await uploadResume(file)
  // 后端现在返回 ResumeFile 对象 { id, fileName, fileUrl, fileSize, fileType, createdAt }
  const resumeFile = res.data
  fileUrl.value = resumeFile.fileUrl
  // 刷新列表
  fetchList()
}

function selectResume(item: any) {
  fileUrl.value = item.fileUrl
  result.value = null
}

async function viewResumeFile(item: any) {
  if (!item.id) {
    ElMessage.warning('简历信息不完整')
    return
  }
  try {
    const token = localStorage.getItem('token')
    const url = getResumeFileUrl(item.id)
    const resp = await axios.get(url, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    })
    const contentType = String(resp.headers['content-type'] || 'application/pdf')
    const blob = new Blob([resp.data], { type: contentType })
    const blobUrl = URL.createObjectURL(blob)
    window.open(blobUrl, '_blank')
  } catch (e: any) {
    const msg = e?.response?.data
    if (msg instanceof Blob) {
      try {
        const text = await msg.text()
        const obj = JSON.parse(text)
        ElMessage.error(obj.message || '无法查看简历')
      } catch { ElMessage.error('无法查看简历') }
    } else {
      ElMessage.error('无法查看简历')
    }
  }
}

async function handleDelete(id: number) {
  try {
    await deleteResume(id)
    // 如果删除的是当前选中的，清空
    const deleted = resumeList.value.find(r => r.id === id)
    if (deleted && fileUrl.value === deleted.fileUrl) {
      fileUrl.value = ''
      result.value = null
    }
    fetchList()
  } catch (e) { /* ignore */ }
}

async function analyze() {
  loading.value = true
  try {
    const res: any = await analyzeResumeApi({ fileUrl: fileUrl.value })
    result.value = res.data
  } finally { loading.value = false }
}
</script>

<style scoped lang="scss">
// ======== Hero ========
.resume-hero {
  position: relative;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 30%, #dbeafe 60%, #ede9fe 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  text-align: center;
  overflow: hidden;
}

.rh-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .rh-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(59, 130, 246, 0.06);
    &.s1 { width: 180px; height: 180px; top: -60px; right: -40px; }
    &.s2 { width: 120px; height: 120px; bottom: -30px; left: 10%; }
    &.s3 { width: 80px; height: 80px; top: 20%; left: -20px; }
  }
}

.rh-content {
  position: relative;
  z-index: 1;
  .rh-icon-wrap {
    width: 60px; height: 60px;
    border-radius: 16px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin: 0 auto 12px;
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.12);
  }
  h1 { font-size: 26px; font-weight: 800; color: #1f2937; margin: 0 0 6px; }
  p { color: #6b7280; font-size: 14px; }
}

// ======== 上传面板 ========
.upload-panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.up-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  h3 { font-size: 17px; font-weight: 700; color: #1f2937; margin: 0; }
}

.up-step-badge {
  padding: 4px 12px;
  border-radius: 8px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 14px;
  padding: 36px 20px;
  text-align: center;
  transition: all 0.3s;
  margin-bottom: 20px;
  &.has-file {
    border-color: #10b981;
    border-style: solid;
    background: #f0fdf4;
  }
  .upload-illustration { margin-bottom: 16px; }
  .upload-icon-wrap {
    width: 64px; height: 64px;
    border-radius: 18px;
    background: #f0f9ff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    .upload-icon { font-size: 30px; }
  }
  .upload-title { font-size: 15px; font-weight: 600; color: #374151; margin-bottom: 4px; }
  .upload-hint { font-size: 12px; color: #9ca3af; }
  .upload-divider {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 16px 0;
    &::before, &::after {
      content: '';
      flex: 1; height: 1px;
      background: #e5e7eb;
    }
    span { font-size: 12px; color: #9ca3af; }
  }
}

.upload-success {
  .success-icon { font-size: 40px; margin-bottom: 8px; }
  .success-text { font-size: 15px; font-weight: 600; color: #065f46; }
  .success-hint { font-size: 12px; color: #6b7280; margin-top: 4px; }
}

.up-step {
  text-align: center;
  margin-bottom: 20px;
  .up-step-badge { margin-bottom: 12px; display: inline-block; }
}

.analyze-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border: none;
  box-shadow: 0 6px 24px rgba(59, 130, 246, 0.35);
  margin-top: 8px;
  &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(59, 130, 246, 0.45); }
  .ai-sparkle { margin-right: 6px; }
}

// ======== 简历列表 ========
.resume-list {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.rl-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  .rl-count { font-size: 11px; color: #9ca3af; font-weight: 400; }
}

.rl-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 6px;
  border: 1px solid #f3f4f6;
  &:hover { background: #f9fafb; border-color: #e5e7eb; }
  &.active { background: #eff6ff; border-color: #93c5fd; }
  .rli-info {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
    min-width: 0;
  }
  .rli-icon { font-size: 22px; flex-shrink: 0; }
  .rli-text {
    display: flex;
    flex-direction: column;
    min-width: 0;
    .rli-name { font-size: 13px; color: #374151; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .rli-meta { font-size: 11px; color: #9ca3af; margin-top: 2px; }
  }
  .rli-delete {
    font-size: 16px;
    cursor: pointer;
    opacity: 0.4;
    transition: opacity 0.2s;
    flex-shrink: 0;
    padding: 4px;
    &:hover { opacity: 1; }
  }
  .rli-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
  }
}

.up-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  .uf-item {
    display: flex;
    align-items: center;
    gap: 12px;
    .uf-icon { font-size: 22px; flex-shrink: 0; }
    strong { font-size: 13px; color: #374151; }
    p { font-size: 11px; color: #9ca3af; margin: 2px 0 0; }
  }
}

// ======== 报告面板 ========
.report-panel {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.rp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  .rph-left { display: flex; align-items: center; gap: 12px; h3 { font-size: 18px; font-weight: 700; margin: 0; } }
}

// 评分区
.rp-score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 24px;
  background: #f9fafb;
  border-radius: 16px;
  margin-bottom: 24px;
}

.score-main {
  flex-shrink: 0;
  position: relative;
  .score-big-ring { width: 180px; height: 180px; }
}

.score-ring-animate {
  animation: ringDraw 1.5s ease-out forwards;
}
@keyframes ringDraw {
  from { stroke-dasharray: 0 490; }
}

.score-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .sc-value { font-size: 44px; font-weight: 800; color: #1f2937; line-height: 1; }
  .sc-label { font-size: 13px; color: #6b7280; margin-top: 4px; }
}

.score-dimensions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.score-dim {
  .sd-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    .sd-name { font-size: 13px; color: #374151; font-weight: 500; }
    .sd-value { font-size: 14px; font-weight: 700; }
  }
  .sd-bar-wrap { .sd-bar { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; } }
  .sd-bar-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 1s ease-out;
  }
}

// ======== 问题列表 ========
.rp-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  .issue-count { font-size: 12px; color: #9ca3af; font-weight: 400; }
}

.issue-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.issue-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 18px 20px;
  border-left: 5px solid #f59e0b;
  transition: all 0.3s;
  animation: cardFadeIn 0.35s ease-out both;
  &:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
  &.severity-high, &.severity-高 { border-left-color: #ef4444; }
  &.severity-medium, &.severity-中 { border-left-color: #f59e0b; }
  &.severity-low, &.severity-低 { border-left-color: #3b82f6; }
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateX(-12px); }
  to { opacity: 1; transform: translateX(0); }
}

.ic-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  .ic-severity {
    flex-shrink: 0;
    padding: 2px 10px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 700;
    &.高, &.high { background: #fee2e2; color: #dc2626; }
    &.中, &.medium { background: #fef3c7; color: #d97706; }
    &.低, &.low { background: #dbeafe; color: #2563eb; }
  }
  .ic-desc { font-size: 14px; color: #374151; font-weight: 500; line-height: 1.5; }
}

.ic-body {
  padding-left: 0;
  .ic-suggestion, .ic-example {
    margin-bottom: 8px;
    .ic-label { font-size: 12px; font-weight: 600; color: #6b7280; margin-right: 8px; }
    p { font-size: 13px; color: #374151; line-height: 1.6; margin: 4px 0 0; padding: 10px 14px; background: #f9fafb; border-radius: 8px; }
  }
}

.rp-summary {
  margin-top: 20px;
  p {
    font-size: 14px;
    color: #374151;
    line-height: 1.7;
    padding: 16px 20px;
    background: #f9fafb;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
  }
}

// ======== 空状态 ========
.report-empty {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  .re-illustration {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    .re-doc-icon {
      width: 80px; height: 80px;
      border-radius: 20px;
      background: #f0f9ff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
    }
    .re-scan-line {
      position: absolute;
      top: 0; left: 5%; right: 5%;
      height: 3px;
      background: linear-gradient(90deg, transparent, #3b82f6, transparent);
      border-radius: 2px;
      animation: scanLine 2s ease-in-out infinite;
    }
  }
  h3 { font-size: 18px; font-weight: 700; color: #1f2937; }
  p { color: #6b7280; font-size: 14px; margin-bottom: 20px; }
  .re-hints {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
    .re-hint {
      padding: 6px 14px;
      background: #f9fafb;
      border-radius: 8px;
      font-size: 12px;
      color: #6b7280;
      border: 1px solid #e5e7eb;
    }
  }
}

@keyframes scanLine {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 3px); }
}
</style>
