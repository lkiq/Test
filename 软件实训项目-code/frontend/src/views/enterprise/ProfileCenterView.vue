<template>
  <div class="profile-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>个人中心</h1>
        <p>管理企业信息与个人资料</p>
      </div>
      <el-button type="primary" :icon="Edit" @click="enterEdit">编辑信息</el-button>
    </div>

    <div class="content-grid">
      <!-- 账号信息 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon color="#2563eb"><User /></el-icon>
            <span>个人资料</span>
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
            <el-tag type="warning">HR / 招聘经理</el-tag>
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

      <!-- 企业信息 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon color="#2563eb"><OfficeBuilding /></el-icon>
            <span>企业信息</span>
            <el-tag v-if="entForm.verifyStatus === 'APPROVED'" type="success" size="small">已认证</el-tag>
            <el-tag v-else-if="entForm.verifyStatus === 'PENDING'" type="warning" size="small">待审核</el-tag>
            <el-tag v-else-if="entForm.verifyStatus === 'REJECTED'" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else type="info" size="small">未认证</el-tag>
          </div>
        </template>

        <el-form label-width="100px" :model="entForm" :disabled="!editing">
          <el-form-item label="公司名称">
            <el-input v-model="entForm.companyName" placeholder="请输入公司名称" />
          </el-form-item>
          <el-form-item label="所属行业">
            <el-select v-model="entForm.companyIndustry" placeholder="请选择行业" style="width:100%">
              <el-option label="互联网/IT" value="互联网/IT" />
              <el-option label="金融" value="金融" />
              <el-option label="教育" value="教育" />
              <el-option label="医疗健康" value="医疗健康" />
              <el-option label="制造业" value="制造业" />
              <el-option label="房地产" value="房地产" />
              <el-option label="零售/电商" value="零售/电商" />
              <el-option label="文化传媒" value="文化传媒" />
              <el-option label="其它" value="其它" />
            </el-select>
          </el-form-item>
          <el-form-item label="公司规模">
            <el-select v-model="entForm.companySize" placeholder="请选择规模" style="width:100%">
              <el-option label="1-50人" value="1-50人" />
              <el-option label="50-200人" value="50-200人" />
              <el-option label="200-500人" value="200-500人" />
              <el-option label="500-1000人" value="500-1000人" />
              <el-option label="1000人以上" value="1000人以上" />
            </el-select>
          </el-form-item>
          <el-form-item label="公司地址">
            <el-input v-model="entForm.companyAddress" placeholder="请输入公司地址" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="entForm.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item label="职位">
            <el-input v-model="entForm.contactPosition" placeholder="请输入您的职位" />
          </el-form-item>
          <el-form-item label="公司简介">
            <el-input
              v-model="entForm.companyDescription"
              type="textarea"
              :rows="3"
              placeholder="请输入公司简介"
            />
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
import { User, OfficeBuilding, Lock, Edit } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, changePassword } from '@/api/user'

interface ProfileForm {
  username: string
  realName: string
  phone: string
  email: string
  status: string
  createdAt: string
}

interface EnterpriseForm {
  companyName: string
  companyIndustry: string
  companySize: string
  companyAddress: string
  companyDescription: string
  contactName: string
  contactPosition: string
  verifyStatus: string
}

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)

const form = ref<ProfileForm>({
  username: '', realName: '', phone: '', email: '',
  status: '', createdAt: ''
})

const entForm = ref<EnterpriseForm>({
  companyName: '', companyIndustry: '', companySize: '',
  companyAddress: '', companyDescription: '',
  contactName: '', contactPosition: '', verifyStatus: ''
})

const originalForm = ref<ProfileForm>({ ...form.value })
const originalEnt = ref<EnterpriseForm>({ ...entForm.value })

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
      status: d.status || '',
      createdAt: d.createdAt || ''
    }
    if (d.enterprise) {
      entForm.value = {
        companyName: d.enterprise.companyName || '',
        companyIndustry: d.enterprise.companyIndustry || '',
        companySize: d.enterprise.companySize || '',
        companyAddress: d.enterprise.companyAddress || '',
        companyDescription: d.enterprise.companyDescription || '',
        contactName: d.enterprise.contactName || '',
        contactPosition: d.enterprise.contactPosition || '',
        verifyStatus: d.enterprise.verifyStatus || ''
      }
    }
    originalForm.value = { ...form.value }
    originalEnt.value = { ...entForm.value }
  } finally {
    loading.value = false
  }
}

function enterEdit() {
  originalForm.value = { ...form.value }
  originalEnt.value = { ...entForm.value }
  editing.value = true
}

function cancelEdit() {
  form.value = { ...originalForm.value }
  entForm.value = { ...originalEnt.value }
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
      enterprise: {
        companyName: entForm.value.companyName,
        companyIndustry: entForm.value.companyIndustry,
        companySize: entForm.value.companySize,
        companyAddress: entForm.value.companyAddress,
        companyDescription: entForm.value.companyDescription,
        contactName: entForm.value.contactName,
        contactPosition: entForm.value.contactPosition
      }
    })
    editing.value = false
    originalForm.value = { ...form.value }
    originalEnt.value = { ...entForm.value }
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
  max-width: 1060px;
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
