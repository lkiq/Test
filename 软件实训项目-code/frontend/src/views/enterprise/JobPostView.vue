<template>
  <div class="job-post-page">
    <!-- 页面标题区 -->
    <div class="jp-header">
      <div class="jp-header-left">
        <h2 class="jp-title">
          <el-icon :size="22"><Briefcase /></el-icon>
          岗位管理
        </h2>
        <span class="jp-subtitle">管理所有职位发布与状态</span>
      </div>
      <el-button type="primary" size="large" round @click="openDialog(null)">
        <el-icon style="margin-right:6px"><Plus /></el-icon>发布新岗位
      </el-button>
    </div>

    <!-- 岗位统计 -->
    <div class="jp-stats-row">
      <div class="jp-stat-item">
        <span class="jp-stat-num">{{ activeCount }}</span>
        <span class="jp-stat-label">招聘中</span>
        <span class="jp-stat-badge active"></span>
      </div>
      <div class="jp-stat-item">
        <span class="jp-stat-num">{{ draftCount }}</span>
        <span class="jp-stat-label">草稿</span>
        <span class="jp-stat-badge draft"></span>
      </div>
      <div class="jp-stat-item">
        <span class="jp-stat-num">{{ offlineCount }}</span>
        <span class="jp-stat-label">已下架</span>
        <span class="jp-stat-badge offline"></span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="jp-search-bar" v-if="jobs.length > 0 || keyword">
      <el-input
        v-model="keyword"
        placeholder="搜索岗位名称、公司、方向…"
        :prefix-icon="Search"
        clearable
        size="large"
        @clear="keyword = ''"
      />
    </div>

    <!-- 岗位列表 -->
    <div class="jp-body">
      <!-- 加载 -->
      <div v-if="loading" class="jp-loading">
        <el-skeleton v-for="i in 3" :key="i" :rows="2" animated style="margin-bottom:12px" />
      </div>

      <!-- 岗位卡片列表 -->
      <div v-else-if="filteredJobs.length > 0" class="jp-list">
        <div
          v-for="job in filteredJobs"
          :key="job.id"
          class="job-card"
          :class="{ 'is-offline': job.publishStatus === 2, 'is-draft': job.publishStatus === 0 }"
        >
          <!-- 左侧 Info -->
          <div class="jc-main">
            <div class="jc-logo">
              <img v-if="job.logoUrl" :src="job.logoUrl" alt="" />
              <el-icon v-else :size="24"><OfficeBuilding /></el-icon>
            </div>
            <div class="jc-info">
              <div class="jc-title-row">
                <span class="jc-title">{{ job.title }}</span>
                <el-tag
                  v-if="job.publishStatus === 1"
                  type="success"
                  size="small"
                  effect="light"
                  round
                >招聘中</el-tag>
                <el-tag
                  v-else-if="job.publishStatus === 0"
                  type="warning"
                  size="small"
                  effect="light"
                  round
                >草稿</el-tag>
                <el-tag
                  v-else
                  type="info"
                  size="small"
                  effect="light"
                  round
                >已下架</el-tag>
              </div>
              <div class="jc-meta">
                <span class="jc-meta-item">
                  <el-icon :size="14"><OfficeBuilding /></el-icon>
                  {{ job.companyName || '未设置公司' }}
                </span>
                <span class="jc-meta-item" v-if="job.direction">
                  <el-icon :size="14"><Collection /></el-icon>
                  {{ job.direction }}
                </span>
                <span class="jc-meta-item" v-if="job.city">
                  <el-icon :size="14"><Location /></el-icon>
                  {{ job.city }}
                </span>
                <span class="jc-meta-item salary" v-if="job.salaryRange">
                  <el-icon :size="14"><Money /></el-icon>
                  {{ job.salaryRange }}
                </span>
              </div>
              <div class="jc-tags">
                <span class="jc-tag">{{ job.jobType || '全职' }}</span>
                <span class="jc-tag" v-if="job.experienceRequired">{{ job.experienceRequired }}</span>
                <span class="jc-tag" v-if="job.educationRequired">{{ job.educationRequired }}</span>
              </div>
              <div class="jc-time" v-if="job.publishTime">
                <el-icon :size="12"><Clock /></el-icon>
                {{ formatTime(job.publishTime) }} 发布
              </div>
            </div>
          </div>

          <!-- 右侧操作 -->
          <div class="jc-actions">
            <el-button
              v-if="job.publishStatus === 1"
              type="primary"
              size="small"
              round
              plain
              @click="$router.push('/enterprise/candidates')"
            >
              查看候选人
            </el-button>
            <el-button
              size="small"
              round
              @click="openDialog(job)"
            >
              <el-icon style="margin-right:4px"><Edit /></el-icon>编辑
            </el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, job)">
              <el-button size="small" round>
                更多<el-icon style="margin-left:4px"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="job.publishStatus === 1" command="offline">
                    <span style="color:#FF7D00">下架岗位</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="job.publishStatus === 2" command="republish">
                    <span style="color:#00B42A">重新发布</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="job.publishStatus === 2" command="delete" divided>
                    <span style="color:#F53F3F">删除岗位</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 搜索结果为空 -->
      <div v-else-if="keyword.trim() && jobs.length > 0" class="jp-empty">
        <div class="jp-empty-icon">
          <el-icon :size="64" color="#c0c4cc"><Search /></el-icon>
        </div>
        <h3>未找到匹配岗位</h3>
        <p>没有找到包含"{{ keyword.trim() }}"的岗位</p>
        <el-button round @click="keyword = ''">清除搜索</el-button>
      </div>

      <!-- 空状态（无岗位） -->
      <div v-else class="jp-empty">
        <div class="jp-empty-icon">
          <el-icon :size="64" color="#c0c4cc"><FolderDelete /></el-icon>
        </div>
        <h3>暂无岗位</h3>
        <p>您还没有发布过岗位，点击"发布新岗位"开始招聘</p>
        <el-button type="primary" round @click="openDialog(null)">立即发布</el-button>
      </div>
    </div>

    <!-- 发布/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingJob ? '编辑岗位' : '发布新岗位'"
      width="720px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="dialog-body">
        <!-- 公司Logo -->
        <div class="dialog-section">
          <div class="dialog-section-label">公司Logo</div>
          <div class="logo-upload-row">
            <el-upload
              :show-file-list="false"
              :http-request="uploadLogo"
              accept="image/*"
              :before-upload="beforeLogoUpload"
            >
              <div v-if="form.logoUrl" class="logo-preview">
                <img :src="form.logoUrl" alt="Logo" />
                <div class="logo-mask"><el-icon :size="16"><Edit /></el-icon>更换</div>
              </div>
              <div v-else class="logo-upload-btn">
                <el-icon :size="24"><Plus /></el-icon>
                <span>上传</span>
              </div>
            </el-upload>
            <span class="logo-tip">建议 200×200px，PNG/JPG，≤3MB</span>
          </div>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="dialog-section">
          <div class="dialog-section-label">基本信息</div>
          <el-row :gutter="16">
            <el-col :span="16">
              <el-form-item label="岗位名称" prop="title" :rules="[{ required: true, message: '必填' }]">
                <el-input v-model="form.title" placeholder="如：Java后端开发工程师" maxlength="100" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="公司名称" prop="companyName" :rules="[{ required: true, message: '必填' }]">
                <el-input v-model="form.companyName" placeholder="如：腾讯" maxlength="100" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="岗位方向">
                <el-select v-model="form.direction" placeholder="选择方向" clearable style="width:100%">
                  <el-option v-for="d in directions" :key="d" :label="d" :value="d" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工作城市">
                <el-input v-model="form.city" placeholder="如：北京" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-divider />

        <!-- 薪资与要求 -->
        <div class="dialog-section">
          <div class="dialog-section-label">薪资与要求</div>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="薪资范围">
                <el-input v-model="form.salaryRange" placeholder="如：15K-30K" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="工作性质">
                <el-select v-model="form.jobType" style="width:100%">
                  <el-option v-for="t in jobTypes" :key="t" :label="t" :value="t" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="经验要求">
                <el-select v-model="form.experienceRequired" style="width:100%">
                  <el-option v-for="e in expLevels" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="学历要求">
                <el-select v-model="form.educationRequired" style="width:100%">
                  <el-option v-for="e in eduLevels" :key="e" :label="e" :value="e" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-divider />

        <!-- 岗位描述 -->
        <div class="dialog-section">
          <div class="dialog-section-label">岗位描述</div>
          <el-form-item prop="jd" label-width="0">
            <el-input
              v-model="form.jd"
              type="textarea"
              :rows="6"
              placeholder="请详细描述岗位职责、技术要求、任职资格等信息…"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false" size="default" round>取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm" size="default" round>
          {{ editingJob ? '保存修改' : '立即发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Briefcase, OfficeBuilding, Collection, Location,
  Money, Clock, Edit, ArrowDown, FolderDelete
} from '@element-plus/icons-vue'
import { listMyJobs, publishJob, updateJob, offlineJob, republishJob, deleteJob, uploadJobImage } from '@/api/enterprise'

const route = useRoute()
const jobs = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingJob = ref<any>(null)
const submitLoading = ref(false)
const keyword = ref('')

const filteredJobs = computed(() => {
  if (!keyword.value.trim()) return jobs.value
  const kw = keyword.value.trim().toLowerCase()
  return jobs.value.filter((j: any) =>
    (j.title || '').toLowerCase().includes(kw) ||
    (j.companyName || '').toLowerCase().includes(kw) ||
    (j.direction || '').toLowerCase().includes(kw)
  )
})

const activeCount = computed(() => jobs.value.filter(j => j.publishStatus === 1).length)
const draftCount = computed(() => jobs.value.filter(j => j.publishStatus === 0).length)
const offlineCount = computed(() => jobs.value.filter(j => j.publishStatus === 2).length)

const form = reactive({
  title: '', companyName: '', direction: '', city: '',
  salaryRange: '', jobType: '全职', experienceRequired: '经验不限',
  educationRequired: '本科', jd: '', logoUrl: ''
})

const directions = ['前端开发', '后端开发', '移动开发', '数据科学', '算法&AI', '测试开发', 'DevOps', '产品经理', '架构师', '其他']
const jobTypes = ['全职', '实习', '校招', '兼职']
const expLevels = ['经验不限', '应届生', '1-3年', '3-5年', '5-10年', '10年以上']
const eduLevels = ['大专', '本科', '硕士', '博士', '学历不限']

function formatTime(t: string | null) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - d.getTime()) / (1000 * 86400))
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 30) return diffDays + '天前'
  return d.toLocaleDateString('zh-CN')
}

function loadJobs() {
  loading.value = true
  listMyJobs()
    .then((res: any) => { jobs.value = res.data || [] })
    .catch(() => { jobs.value = [] })
    .finally(() => { loading.value = false })
}

function openDialog(job: any | null) {
  if (job) {
    editingJob.value = job
    Object.assign(form, {
      title: job.title || '', companyName: job.companyName || '',
      direction: job.direction || '', city: job.city || '',
      salaryRange: job.salaryRange || '', jobType: job.jobType || '全职',
      experienceRequired: job.experienceRequired || '经验不限',
      educationRequired: job.educationRequired || '本科',
      jd: job.jd || '', logoUrl: job.logoUrl || ''
    })
  } else {
    editingJob.value = null
    Object.assign(form, {
      title: '', companyName: '', direction: '', city: '',
      salaryRange: '', jobType: '全职', experienceRequired: '经验不限',
      educationRequired: '本科', jd: '', logoUrl: ''
    })
  }
  dialogVisible.value = true
}

function beforeLogoUpload(file: File) {
  const isValid = file.type.startsWith('image/')
  if (!isValid) ElMessage.error('仅支持图片文件')
  const isLt3M = file.size / 1024 / 1024 < 3
  if (!isLt3M) ElMessage.error('图片不能超过 3MB')
  return isValid && isLt3M
}

async function uploadLogo(options: any) {
  try {
    const res: any = await uploadJobImage(options.file)
    form.logoUrl = res.data
    ElMessage.success('Logo上传成功')
  } catch {
    ElMessage.error('Logo上传失败')
  }
}

async function submitForm() {
  if (!form.title || !form.companyName) {
    ElMessage.warning('请填写岗位名称和公司名称')
    return
  }
  submitLoading.value = true
  try {
    if (editingJob.value) {
      await updateJob(editingJob.value.id, { ...form })
      ElMessage.success('岗位更新成功')
    } else {
      await publishJob({ ...form })
      ElMessage.success('岗位发布成功')
    }
    dialogVisible.value = false
    loadJobs()
  } catch {
    // 拦截器处理
  } finally {
    submitLoading.value = false
  }
}

async function handleCommand(cmd: string, job: any) {
  if (cmd === 'offline') {
    try {
      await ElMessageBox.confirm(`确定下架「${job.title}」吗？`, '下架确认', {
        confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning'
      })
      await offlineJob(job.id)
      ElMessage.success('已下架')
      loadJobs()
    } catch {}
  } else if (cmd === 'republish') {
    await republishJob(job.id)
    ElMessage.success('已重新发布')
    loadJobs()
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(
        `确定删除「${job.title}」吗？此操作不可恢复。`, '确认删除',
        { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
      )
      const res: any = await deleteJob(job.id)
      if (res.code === 200) {
        ElMessage.success('已删除')
        loadJobs()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {}
  }
}

onMounted(() => {
  // 从URL读取搜索关键词
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
  }
  loadJobs()
})

// 监听路由变化（HeaderBar搜索触发时更新关键词）
watch(() => route.query.keyword, (val) => {
  if (val) keyword.value = val as string
})
</script>

<style scoped>
.job-post-page { max-width: 1100px; margin: 0 auto; padding-bottom: 32px; }

/* ===== 页头 ===== */
.jp-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; flex-wrap: wrap; gap: 12px;
}
.jp-header-left { display: flex; align-items: baseline; gap: 12px; }
.jp-title { font-size: 20px; font-weight: 700; color: #1d2129; margin: 0; display: flex; align-items: center; gap: 8px; }
.jp-subtitle { font-size: 13px; color: #86909c; }

/* ===== 统计 ===== */
.jp-stats-row {
  display: flex; gap: 12px; margin-bottom: 20px;
}

/* ===== 搜索栏 ===== */
.jp-search-bar {
  margin-bottom: 16px;
}
.jp-search-bar :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid #e5e6eb;
}
.jp-search-bar :deep(.el-input__wrapper):hover {
  border-color: #165DFF;
}
.jp-stat-item {
  background: #fff; border-radius: 12px; padding: 12px 24px;
  border: 1px solid #f0f0f0; display: flex; align-items: center; gap: 10px;
  min-width: 130px; transition: transform 0.2s;
}
.jp-stat-item:hover { transform: translateY(-2px); }
.jp-stat-num { font-size: 26px; font-weight: 700; color: #1d2129; }
.jp-stat-label { font-size: 13px; color: #86909c; }
.jp-stat-badge { width: 8px; height: 8px; border-radius: 50%; margin-left: auto; }
.jp-stat-badge.active { background: #00B42A; box-shadow: 0 0 0 3px rgba(0,180,42,0.15); }
.jp-stat-badge.draft { background: #FF7D00; box-shadow: 0 0 0 3px rgba(255,125,0,0.15); }
.jp-stat-badge.offline { background: #86909c; box-shadow: 0 0 0 3px rgba(134,144,156,0.15); }

/* ===== 岗位列表 ===== */
.jp-body { min-height: 300px; }
.jp-loading { background: #fff; border-radius: 14px; padding: 24px; border: 1px solid #f0f0f0; }

.jp-list { display: flex; flex-direction: column; gap: 8px; }

.job-card {
  background: #fff; border-radius: 14px; padding: 18px 24px;
  border: 1px solid #f0f0f0; display: flex; align-items: center;
  justify-content: space-between; transition: all 0.2s; gap: 16px;
}
.job-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(0,0,0,0.06); transform: translateY(-1px); }
.job-card.is-offline { opacity: 0.6; }
.job-card.is-draft { border-style: dashed; border-color: #e5e6eb; }

.jc-main { display: flex; align-items: center; gap: 16px; flex: 1; min-width: 0; }
.jc-logo {
  width: 48px; height: 48px; border-radius: 10px;
  background: #f0f5ff; display: flex; align-items: center;
  justify-content: center; color: #165DFF; overflow: hidden; flex-shrink: 0;
}
.jc-logo img { width: 100%; height: 100%; object-fit: cover; }

.jc-info { flex: 1; min-width: 0; }
.jc-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.jc-title { font-size: 16px; font-weight: 600; color: #1d2129; }
.jc-meta { display: flex; gap: 14px; flex-wrap: wrap; margin-bottom: 6px; }
.jc-meta-item {
  font-size: 13px; color: #4e5969; display: flex; align-items: center; gap: 4px;
}
.jc-meta-item.salary { color: #F53F3F; font-weight: 500; }
.jc-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 4px; }
.jc-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
  background: #f2f3f5; color: #4e5969;
}
.jc-time { font-size: 12px; color: #86909c; display: flex; align-items: center; gap: 4px; }

.jc-actions { display: flex; gap: 6px; flex-shrink: 0; }

/* ===== 空状态 ===== */
.jp-empty {
  text-align: center; padding: 80px 40px;
  background: #fff; border-radius: 14px; border: 1px solid #f0f0f0;
}
.jp-empty-icon { margin-bottom: 16px; }
.jp-empty h3 { font-size: 17px; font-weight: 600; color: #1d2129; margin: 0 0 8px; }
.jp-empty p { font-size: 14px; color: #86909c; margin: 0 0 20px; }

/* ===== 对话框 ===== */
.dialog-body { padding: 0 8px; }
.dialog-section { margin-bottom: 4px; }
.dialog-section-label {
  font-size: 14px; font-weight: 600; color: #1d2129; margin-bottom: 14px;
  padding-left: 10px; border-left: 3px solid #165DFF;
}
.dialog-body :deep(.el-divider) { margin: 12px 0 16px; }

.logo-upload-row { display: flex; align-items: center; gap: 16px; }
.logo-preview {
  width: 72px; height: 72px; border-radius: 10px; overflow: hidden;
  position: relative; cursor: pointer; border: 2px solid #e5e6eb;
}
.logo-preview img { width: 100%; height: 100%; object-fit: cover; }
.logo-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.4); color: #fff;
  display: flex; align-items: center; justify-content: center;
  gap: 4px; font-size: 12px; opacity: 0; transition: opacity 0.2s;
}
.logo-preview:hover .logo-mask { opacity: 1; }
.logo-upload-btn {
  width: 72px; height: 72px; border: 2px dashed #d9d9d9;
  border-radius: 10px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; cursor: pointer;
  color: #8c8c8c; gap: 4px; font-size: 11px; transition: all 0.2s;
}
.logo-upload-btn:hover { border-color: #165DFF; color: #165DFF; }
.logo-tip { font-size: 12px; color: #86909c; }

:deep(.el-dialog__body) { padding-top: 12px; }

@media (max-width: 768px) {
  .jp-stats-row { flex-wrap: wrap; }
  .job-card { flex-direction: column; align-items: flex-start; }
  .jc-actions { width: 100%; justify-content: flex-start; }
}
</style>
