<template>
  <div class="profile-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>个人中心</h1>
        <p>查看您的账号信息与个人资料</p>
      </div>
      <el-button type="primary" :icon="Edit" @click="enterEdit">编辑信息</el-button>
    </div>

    <div class="content-grid">
      <!-- 账号信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon color="#2563eb"><User /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <el-form label-width="90px" :model="form" :disabled="!editing">
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag type="success">学生</el-tag>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="账号状态">
            <el-tag :type="form.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ form.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </el-form-item>
          <el-form-item label="注册时间">
            <span class="text-muted">{{ formatDate(form.createdAt) }}</span>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 教育信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon color="#2563eb"><School /></el-icon>
            <span>教育背景</span>
          </div>
        </template>

        <el-form label-width="90px" :model="form" :disabled="!editing">
          <el-form-item label="学历">
            <el-select v-model="form.education" placeholder="请选择学历" style="width:100%">
              <el-option label="大专" value="JUNIOR" />
              <el-option label="本科" value="BACHELOR" />
              <el-option label="硕士" value="MASTER" />
              <el-option label="博士" value="PHD" />
            </el-select>
          </el-form-item>
          <el-form-item label="毕业院校">
            <el-input v-model="form.school" placeholder="请输入毕业院校" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="form.major" placeholder="请输入专业" />
          </el-form-item>
        </el-form>

        <div v-if="editing" class="form-actions">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
        </div>
      </el-card>

      <!-- 修改密码卡片 -->
      <el-card class="info-card password-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon color="#f59e0b"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
        </template>

        <el-form label-width="90px" :model="pwdForm" :rules="pwdRules" ref="pwdFormRef">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="warning" :loading="changingPwd" @click="doChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" v-loading="loading" style="min-height:200px" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, School, Lock, Edit } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, changePassword } from '@/api/user'

interface ProfileForm {
  username: string
  realName: string
  phone: string
  email: string
  education: string
  school: string
  major: string
  status: string
  createdAt: string
}

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const form = ref<ProfileForm>({
  username: '',
  realName: '',
  phone: '',
  email: '',
  education: '',
  school: '',
  major: '',
  status: '',
  createdAt: ''
})

const originalForm = ref<ProfileForm>({ ...form.value })

onMounted(() => fetchProfile())

async function fetchProfile() {
  loading.value = true
  try {
    const res: any = await getUserProfile()
    const d = res.data || res
    form.value = {
      username: d.username || '',
      realName: d.realName || '',
      phone: d.phone || '',
      email: d.email || '',
      education: d.education || '',
      school: d.school || '',
      major: d.major || '',
      status: d.status || '',
      createdAt: d.createdAt || ''
    }
    originalForm.value = { ...form.value }
  } finally {
    loading.value = false
  }
}

function enterEdit() {
  originalForm.value = { ...form.value }
  editing.value = true
}

function cancelEdit() {
  form.value = { ...originalForm.value }
  editing.value = false
}

// 密码修改
const pwdFormRef = ref<FormInstance>()
const changingPwd = ref(false)
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function doChangePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    // handled by interceptor
  } finally {
    changingPwd.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await updateUserProfile({
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email,
      education: form.value.education,
      school: form.value.school,
      major: form.value.major
    })
    editing.value = false
    originalForm.value = { ...form.value }
    ElMessage.success('个人信息已更新')
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped lang="scss">
.profile-center {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  h1 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin: 0; }
  p { font-size: 14px; color: var(--text-tertiary); margin: 4px 0 0; }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.info-card {
  border-radius: 16px;
  :deep(.el-card__header) { padding: 16px 20px; border-bottom: 1px solid #f0f0f0; }
  :deep(.el-card__body) { padding: 20px; }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.text-muted { color: var(--text-tertiary); font-size: 14px; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.password-card {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .content-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
