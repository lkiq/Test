<template>
  <div class="auth-page">
    <!-- 左侧品牌区 -->
    <div class="auth-brand-side register-brand">
      <div class="brand-decoration">
        <div class="deco-circle circle-1"></div>
        <div class="deco-circle circle-2"></div>
        <div class="deco-circle circle-3"></div>
        <div class="deco-shape shape-1"></div>
        <div class="deco-shape shape-2"></div>
      </div>

      <div class="brand-content">
        <div class="brand-logo">
          <el-icon size="40" color="#fff"><Connection /></el-icon>
          <span class="logo-text">IT求职</span>
        </div>
        <h1 class="brand-slogan">加入 IT求职平台</h1>
        <p class="brand-subtitle">技术人才专属招聘平台，AI 助你高效匹配理想技术岗位</p>

        <div class="brand-features">
          <template v-if="selectedRole === 'STUDENT'">
            <div class="feature-item">
              <div class="feature-icon"><el-icon size="22" color="#fff"><UserFilled /></el-icon></div>
              <div class="feature-text">
                <div class="feature-title">技术人才专属</div>
                <div class="feature-desc">AI 智能匹配前端/后端/算法等IT岗位，技术简历优化与模拟面试</div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="feature-item">
              <div class="feature-icon"><el-icon size="22" color="#fff"><OfficeBuilding /></el-icon></div>
              <div class="feature-text">
                <div class="feature-title">专属企业HR</div>
                <div class="feature-desc">精准匹配优质人才，高效管理招聘流程，智能招聘助手</div>
              </div>
            </div>
          </template>
          <div class="feature-item">
            <div class="feature-icon"><el-icon size="22" color="#fff"><DataLine /></el-icon></div>
            <div class="feature-text">
              <div class="feature-title">数据驱动决策</div>
              <div class="feature-desc">全方位能力评估与岗位匹配分析，让选择更科学</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon size="22" color="#fff"><Opportunity /></el-icon></div>
            <div class="feature-text">
              <div class="feature-title">持续职业成长</div>
              <div class="feature-desc">个性化学习路径，技能提升建议，陪伴整个职业生涯</div>
            </div>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        © 2026 IT求职平台 · 技术人才专属
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-form-side">
      <div class="auth-card">
        <!-- 顶部 Logo -->
        <div class="card-logo">
          <el-icon size="32" color="#4f46e5"><Connection /></el-icon>
          <span class="card-logo-text">IT求职平台</span>
        </div>

        <!-- 步骤指示器 -->
        <div class="step-bar">
          <div class="step-item" :class="{ active: step >= 1, current: step === 1 }">
            <div class="step-num">1</div>
            <div class="step-label">选择身份</div>
          </div>
          <div class="step-line" :class="{ active: step >= 2 }"></div>
          <div class="step-item" :class="{ active: step >= 2, current: step === 2 }">
            <div class="step-num">2</div>
            <div class="step-label">填写信息</div>
          </div>
        </div>

        <!-- 步骤一：选择角色 -->
        <div v-if="step === 1" class="role-step">
          <div class="auth-header">
            <h2 class="auth-title">选择你的身份</h2>
            <p class="auth-subtitle">我们将为你提供专属的求职/招聘服务</p>
          </div>

          <div class="role-options">
            <div
              class="role-card"
              :class="{ active: tempRole === 'STUDENT' }"
              @click="tempRole = 'STUDENT'"
            >
              <div class="role-header">
                <div class="role-icon student-icon">
                  <el-icon size="28" color="#fff"><UserFilled /></el-icon>
                </div>
                <div class="role-check" v-if="tempRole === 'STUDENT'">
                  <el-icon size="14" color="#fff"><Check /></el-icon>
                </div>
              </div>
              <div class="role-title">我要找工作</div>
              <div class="role-desc">适合IT技术人才寻找前端、后端、算法、数据等理想岗位</div>
              <div class="role-tags">
                <span class="role-tag">技术岗位匹配</span>
                <span class="role-tag">简历优化</span>
                <span class="role-tag">技术面试模拟</span>
              </div>
            </div>

            <div
              class="role-card"
              :class="{ active: tempRole === 'HR' }"
              @click="tempRole = 'HR'"
            >
              <div class="role-header">
                <div class="role-icon hr-icon">
                  <el-icon size="28" color="#fff"><OfficeBuilding /></el-icon>
                </div>
                <div class="role-check" v-if="tempRole === 'HR'">
                  <el-icon size="14" color="#fff"><Check /></el-icon>
                </div>
              </div>
              <div class="role-title">我要招人</div>
              <div class="role-desc">适合企业HR、招聘负责人发布职位、筛选人才</div>
              <div class="role-tags">
                <span class="role-tag">人才匹配</span>
                <span class="role-tag">招聘管理</span>
                <span class="role-tag">智能助手</span>
              </div>
            </div>
          </div>

          <el-button
            type="primary"
            class="auth-submit-btn"
            :disabled="!tempRole"
            @click="confirmRole"
          >
            下一步
          </el-button>

          <div class="auth-footer">
            <span class="auth-tip">已有账号？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/login')">
              直接登录
            </el-link>
          </div>
        </div>

        <!-- 步骤二：填写注册信息 -->
        <div v-else class="form-step">
          <div class="auth-header">
            <h2 class="auth-title">{{ selectedRole === 'STUDENT' ? '注册求职者账号' : '注册企业HR账号' }}</h2>
            <p class="auth-subtitle">填写基本信息，开启你的{{ selectedRole === 'STUDENT' ? '求职' : '招聘' }}之旅</p>
          </div>

          <el-form :model="form" :rules="dynamicRules" ref="formRef" label-width="0" size="large">
            <!-- 账号区域 -->
            <div class="form-section">
              <div class="section-label">账号信息</div>
              <el-form-item prop="username">
                <el-input v-model="form.username" placeholder="用户名（3-20位字母数字）" :prefix-icon="User" clearable />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" :prefix-icon="Lock" show-password @input="evaluatePassword" />
              </el-form-item>

              <div class="password-strength" v-if="form.password">
                <div class="strength-header">
                  <span class="strength-label">密码强度</span>
                  <span class="strength-text" :style="{ color: strengthColor }">{{ strengthLabel }}</span>
                </div>
                <div class="strength-bar">
                  <div class="strength-fill" :style="{ width: strengthPercent + '%', background: strengthColor }"></div>
                </div>
                <div class="strength-tips">建议包含字母、数字，长度6-20位</div>
              </div>
            </div>

            <!-- 个人信息区域 -->
            <div class="form-section">
              <div class="section-label">个人信息</div>
              <el-form-item prop="realName">
                <el-input v-model="form.realName" placeholder="真实姓名" :prefix-icon="EditPen" clearable />
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item prop="phone">
                    <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Phone" clearable maxlength="11" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="email">
                    <el-input v-model="form.email" placeholder="邮箱（用于找回密码）" :prefix-icon="Message" clearable />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 学生额外信息 -->
            <div v-if="selectedRole === 'STUDENT'" class="form-section">
              <div class="section-label">教育背景 <span class="section-optional">（选填）</span></div>
              <el-form-item prop="education">
                <el-select v-model="form.education" placeholder="最高学历" clearable style="width:100%">
                  <el-option label="专科" value="JUNIOR" />
                  <el-option label="本科" value="BACHELOR" />
                  <el-option label="硕士" value="MASTER" />
                  <el-option label="博士" value="PHD" />
                </el-select>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item prop="school">
                    <el-input v-model="form.school" placeholder="毕业院校" :prefix-icon="School" clearable />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="major">
                    <el-input v-model="form.major" placeholder="专业" :prefix-icon="EditPen" clearable />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- HR企业信息 -->
            <div v-if="selectedRole === 'HR'" class="form-section">
              <div class="section-label">企业信息 <span class="section-required">（必填）</span></div>
              <el-form-item prop="companyName">
                <el-input v-model="form.companyName" placeholder="公司全称" :prefix-icon="OfficeBuilding" clearable />
              </el-form-item>
              <el-form-item prop="companyIndustry">
                <el-select v-model="form.companyIndustry" placeholder="所属行业" clearable style="width:100%">
                  <el-option label="互联网/IT" value="互联网/IT" />
                  <el-option label="金融" value="金融" />
                  <el-option label="教育" value="教育" />
                  <el-option label="医疗健康" value="医疗健康" />
                  <el-option label="制造业" value="制造业" />
                  <el-option label="零售/电商" value="零售/电商" />
                  <el-option label="房地产/建筑" value="房地产/建筑" />
                  <el-option label="传媒/娱乐" value="传媒/娱乐" />
                  <el-option label="交通/物流" value="交通/物流" />
                  <el-option label="能源/环保" value="能源/环保" />
                  <el-option label="政府/事业单位" value="政府/事业单位" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item prop="companySize">
                    <el-select v-model="form.companySize" placeholder="公司规模" clearable style="width:100%">
                      <el-option label="1-50人" value="1-50" />
                      <el-option label="51-200人" value="51-200" />
                      <el-option label="201-500人" value="201-500" />
                      <el-option label="501-1000人" value="501-1000" />
                      <el-option label="1000+人" value="1000+" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="contactPosition">
                    <el-input v-model="form.contactPosition" placeholder="您的职位" :prefix-icon="EditPen" clearable />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item prop="companyAddress">
                <el-input v-model="form.companyAddress" placeholder="公司地址" :prefix-icon="School" clearable />
              </el-form-item>
            </div>

            <!-- 服务协议 -->
            <div class="agreement-row">
              <el-checkbox v-model="agreed" size="small">
                <span class="agreement-text">
                  我已阅读并同意
                  <el-link type="primary" :underline="false" class="agreement-link">《用户服务协议》</el-link>
                  和
                  <el-link type="primary" :underline="false" class="agreement-link">《隐私政策》</el-link>
                </span>
              </el-checkbox>
            </div>

            <el-form-item>
              <el-button type="primary" class="auth-submit-btn" @click="handleRegister" :loading="loading" :disabled="!agreed">
                立即注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-back-row" @click="step = 1">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回选择身份</span>
          </div>

          <div class="auth-footer">
            <span class="auth-tip">已有账号？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/login')">直接登录</el-link>
          </div>
        </div>

        <div class="safe-tips">
          <el-icon size="12"><Lock /></el-icon>
          <span>信息加密保护，仅用于招聘求职服务</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import {
  User, Lock, UserFilled, OfficeBuilding,
  Connection, DataLine, Opportunity, ArrowLeft, Check,
  Phone, Message, School, EditPen
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const step = ref(1)
const tempRole = ref('')
const selectedRole = ref('STUDENT')
const agreed = ref(false)

const form = reactive({
  username: '',
  realName: '',
  password: '',
  phone: '',
  email: '',
  education: '',
  school: '',
  major: '',
  role: 'STUDENT',
  // HR企业字段
  companyName: '',
  companyIndustry: '',
  companySize: '',
  companyAddress: '',
  contactPosition: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  // HR必填
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ],
  companyIndustry: [
    { required: true, message: '请选择所属行业', trigger: 'change' }
  ]
}

// 动态校验规则 - HR时公司名和行业必填
const dynamicRules = computed(() => {
  if (selectedRole.value === 'HR') {
    return {
      ...rules,
      companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
      companyIndustry: [{ required: true, message: '请选择所属行业', trigger: 'change' }]
    }
  }
  return rules
})

const formRef = ref()

// 监听角色变化，切换HR时清空学生字段，反之亦然
watch(tempRole, (val) => {
  if (val === 'HR') {
    form.education = ''
    form.school = ''
    form.major = ''
  } else {
    form.companyName = ''
    form.companyIndustry = ''
    form.companySize = ''
    form.companyAddress = ''
    form.contactPosition = ''
  }
})

// 密码强度
const strengthScore = ref(0)
const strengthLabel = computed(() => {
  if (strengthScore.value < 30) return '弱'
  if (strengthScore.value < 60) return '中'
  return '强'
})
const strengthColor = computed(() => {
  if (strengthScore.value < 30) return '#F56C6C'
  if (strengthScore.value < 60) return '#E6A23C'
  return '#67C23A'
})
const strengthPercent = computed(() => Math.min(strengthScore.value, 100))

function evaluatePassword() {
  const pwd = form.password
  let score = 0
  if (pwd.length >= 6) score += 20
  if (pwd.length >= 10) score += 20
  if (/\d/.test(pwd)) score += 20
  if (/[a-zA-Z]/.test(pwd)) score += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) score += 20
  strengthScore.value = score
}

function confirmRole() {
  if (!tempRole.value) return
  selectedRole.value = tempRole.value
  form.role = tempRole.value
  step.value = 2
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!agreed.value) {
    ElMessage.warning('请阅读并同意用户服务协议和隐私政策')
    return
  }

  if (form.password.length < 6 || form.password.length > 20) {
    ElMessage.warning('密码长度需在6-20位之间')
    return
  }

  // 构建请求数据
  const data: any = {
    username: form.username,
    realName: form.realName,
    password: form.password,
    phone: form.phone,
    email: form.email,
    role: form.role
  }

  if (selectedRole.value === 'STUDENT') {
    data.education = form.education || undefined
    data.school = form.school || undefined
    data.major = form.major || undefined
  } else {
    data.companyName = form.companyName
    data.companyIndustry = form.companyIndustry
    data.companySize = form.companySize || undefined
    data.companyAddress = form.companyAddress || undefined
    data.contactPosition = form.contactPosition || undefined
  }

  loading.value = true
  try {
    await userStore.register(data)
    ElMessage.success('注册成功')
    window.location.hash = selectedRole.value === 'STUDENT' ? '#/student/home' : '#/enterprise/home'
  } catch {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

/* 左侧品牌区 */
.auth-brand-side {
  position: relative;
  width: 46%;
  min-height: 100vh;
  background: linear-gradient(145deg, #2563eb 0%, #1d4ed8 50%, #1e40af 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 48px;
  overflow: hidden;
}

.register-brand {
  background: linear-gradient(145deg, #4f46e5 0%, #4338ca 50%, #3730a3 100%);
}

.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.circle-1 {
  width: 420px;
  height: 420px;
  top: -120px;
  right: -120px;
}

.circle-2 {
  width: 280px;
  height: 280px;
  bottom: 80px;
  left: -80px;
}

.circle-3 {
  width: 160px;
  height: 160px;
  top: 40%;
  right: 15%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-shape {
  position: absolute;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(-15deg);
}

.shape-1 {
  width: 140px;
  height: 140px;
  bottom: 25%;
  right: 10%;
}

.shape-2 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 12%;
  background: rgba(255, 255, 255, 0.06);
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-slogan {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 16px;
}

.brand-subtitle {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  margin-bottom: 56px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.5;
}

.brand-footer {
  position: relative;
  z-index: 1;
  font-size: 13px;
  opacity: 0.7;
}

/* 右侧表单区 */
.auth-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.auth-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 42px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.card-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.card-logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

/* 步骤条 */
.step-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #9ca3af;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-item.active .step-num {
  background: #2563eb;
  color: #fff;
}

.step-item.current .step-num {
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.15);
}

.step-label {
  font-size: 13px;
  color: #9ca3af;
}

.step-item.active .step-label {
  color: #2563eb;
  font-weight: 500;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #e5e7eb;
  margin: 0 12px;
  margin-bottom: 20px;
}

.step-line.active {
  background: #2563eb;
}

.auth-header {
  margin-bottom: 24px;
  text-align: center;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.auth-subtitle {
  font-size: 13px;
  color: #6b7280;
}

:deep(.el-input__inner) {
  height: 46px;
}

:deep(.el-input__prefix-inner) {
  color: #9ca3af;
}

.auth-submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.auth-tip {
  margin-right: 4px;
}

/* 角色选择卡片 */
.role-options {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 22px;
}

.role-card {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.role-card:hover {
  border-color: #c7d2fe;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.1);
}

.role-card.active {
  border-color: #4f46e5;
  background: #eef2ff;
  box-shadow: 0 4px 16px rgba(79, 70, 229, 0.12);
}

.role-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.role-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.student-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.hr-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.role-check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.role-title {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.role-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 12px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.role-tag {
  font-size: 12px;
  color: #4f46e5;
  background: #e0e7ff;
  padding: 4px 10px;
  border-radius: 4px;
}

/* 密码强度 */
.password-strength {
  margin: -8px 0 16px;
}

.strength-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.strength-label {
  font-size: 13px;
  color: #4b5563;
}

.strength-text {
  font-size: 13px;
  font-weight: 600;
}

.strength-bar {
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 6px;
}

.strength-fill {
  height: 100%;
  border-radius: 3px;
  transition: all 0.3s ease;
}

.strength-tips {
  font-size: 12px;
  color: #9ca3af;
}

/* 服务协议 */
.agreement-row {
  margin: 8px 0 18px;
}

.agreement-text {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

.agreement-link {
  font-size: 12px;
}

.form-back-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #6b7280;
  font-size: 13px;
  cursor: pointer;
  margin: 16px 0;
  transition: color 0.2s;
}

.form-back-row:hover {
  color: #4f46e5;
}

/* 表单分区 */
.form-section {
  margin-bottom: 8px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.section-optional {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.section-required {
  font-size: 12px;
  color: #ef4444;
  font-weight: 400;
}

.safe-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #f3f4f6;
  font-size: 12px;
  color: #9ca3af;
}

/* 响应式 */
@media (max-width: 992px) {
  .auth-page {
    flex-direction: column;
  }

  .auth-brand-side {
    width: 100%;
    min-height: auto;
    padding: 40px 24px;
    text-align: center;
  }

  .brand-logo {
    justify-content: center;
    margin-bottom: 24px;
  }

  .brand-slogan {
    font-size: 28px;
  }

  .brand-subtitle {
    margin-bottom: 32px;
  }

  .brand-features {
    display: none;
  }

  .brand-footer {
    display: none;
  }

  .auth-form-side {
    padding: 32px 20px;
  }

  .auth-card {
    padding: 32px 24px;
    box-shadow: none;
  }

  .step-line {
    width: 40px;
  }
}
</style>
