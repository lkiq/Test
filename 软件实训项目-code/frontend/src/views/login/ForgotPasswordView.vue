<template>
  <div class="auth-page">
    <!-- 左侧品牌区 -->
    <div class="auth-brand-side">
      <div class="brand-decoration">
        <div class="deco-circle circle-1"></div>
        <div class="deco-circle circle-2"></div>
        <div class="deco-circle circle-3"></div>
        <div class="deco-shape shape-1"></div>
        <div class="deco-shape shape-2"></div>
        <div class="tech-float tech-1">🔑</div>
        <div class="tech-float tech-2">📧</div>
        <div class="tech-float tech-3">🔒</div>
        <div class="dot-grid"></div>
      </div>

      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon-wrap">
            <el-icon size="36" color="#fff"><Connection /></el-icon>
          </div>
          <span class="logo-text">IT求职</span>
        </div>
        <h1 class="brand-slogan">忘记密码？</h1>
        <p class="brand-subtitle">通过注册邮箱验证码即可快速重置密码，请确保访问的是您注册时使用的邮箱</p>

        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon"><el-icon size="22" color="#fff"><Message /></el-icon></div>
            <div class="feature-text">
              <div class="feature-title">验证码发送</div>
              <div class="feature-desc">6位验证码将发送至您的注册邮箱，5分钟内有效</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon size="22" color="#fff"><Lock /></el-icon></div>
            <div class="feature-text">
              <div class="feature-title">安全重置</div>
              <div class="feature-desc">验证通过后设置新密码，全程加密保护</div>
            </div>
          </div>
        </div>
      </div>

      <div class="brand-footer">© 2026 IT求职平台 · 技术人才专属</div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-form-side">
      <div class="auth-card">
        <div class="card-logo">
          <el-icon size="32" color="#2563eb"><Connection /></el-icon>
          <span class="card-logo-text">IT求职平台</span>
        </div>

        <h2 class="card-welcome">重置密码</h2>
        <p class="card-hint">{{ step === 1 ? '请输入您的注册邮箱' : '请输入验证码和新密码' }}</p>

        <!-- 步骤指示器 -->
        <div class="step-bar">
          <div class="step-item" :class="{ active: step >= 1, current: step === 1 }">
            <div class="step-num">1</div>
            <div class="step-label">验证身份</div>
          </div>
          <div class="step-line" :class="{ active: step >= 2 }"></div>
          <div class="step-item" :class="{ active: step >= 2, current: step === 2 }">
            <div class="step-num">2</div>
            <div class="step-label">重置密码</div>
          </div>
        </div>

        <!-- 步骤一：输入邮箱 -->
        <template v-if="step === 1">
          <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="0" size="large" autocomplete="off">
            <el-form-item prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入注册邮箱" :prefix-icon="Message" clearable autocomplete="email" name="forgot-email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="auth-submit-btn" @click="sendCode" :loading="sending">
                {{ sending ? '发送中...' : '获取验证码' }}
              </el-button>
            </el-form-item>
          </el-form>
        </template>

        <!-- 步骤二：验证码 + 新密码 -->
        <template v-else>
          <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="0" size="large" autocomplete="off" :key="step2Key">
            <el-form-item>
              <div class="email-display">
                <el-icon><Message /></el-icon>
                <span>验证码已发送至 {{ emailForm.email }}</span>
              </div>
            </el-form-item>
            <el-form-item prop="code">
              <el-input v-model="resetForm.code" placeholder="请输入6位验证码" maxlength="6" clearable autocomplete="one-time-code" name="reset-code" />
            </el-form-item>
            <el-form-item prop="newPassword">
              <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" :prefix-icon="Lock" show-password @input="evaluatePassword" autocomplete="new-password" name="new-password" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请再次确认新密码" :prefix-icon="Lock" show-password autocomplete="new-password" name="confirm-new-password" />
            </el-form-item>

            <div class="password-strength" v-if="resetForm.newPassword">
              <div class="strength-header">
                <span class="strength-label">密码强度</span>
                <span class="strength-text" :style="{ color: strengthColor }">{{ strengthLabel }}</span>
              </div>
              <div class="strength-bar">
                <div class="strength-fill" :style="{ width: strengthPercent + '%', background: strengthColor }"></div>
              </div>
            </div>

            <el-form-item>
              <el-button type="primary" class="auth-submit-btn" @click="resetPassword" :loading="resetting">
                {{ resetting ? '重置中...' : '重置密码' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-back-row" @click="goBackToEmail">
            <el-icon><ArrowLeft /></el-icon>
            <span>更换邮箱</span>
          </div>
        </template>

        <div class="auth-footer">
          <span class="auth-tip">想起密码了？</span>
          <el-link type="primary" :underline="false" @click="$router.push('/login')">返回登录</el-link>
        </div>

        <div class="safe-tips">
          <el-icon size="12"><Lock /></el-icon>
          <span>重置密码过程全程加密保护</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { Message, Lock, Connection, ArrowLeft } from '@element-plus/icons-vue'
import { forgotPassword as forgotApi, resetPassword as resetApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const step = ref(1)
const step2Key = ref(0) // 步骤二强制重建 DOM 的 key
const sending = ref(false)
const resetting = ref(false)

const emailForm = reactive({ email: '' })
const resetForm = reactive({ code: '', newPassword: '', confirmPassword: '' })

const emailFormRef = ref()
const resetFormRef = ref()

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}
const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

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
  const pwd = resetForm.newPassword
  let score = 0
  if (pwd.length >= 6) score += 20
  if (pwd.length >= 10) score += 20
  if (/\d/.test(pwd)) score += 20
  if (/[a-zA-Z]/.test(pwd)) score += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) score += 20
  strengthScore.value = score
}

// 发送验证码
async function sendCode() {
  const valid = await emailFormRef.value?.validate().catch(() => false)
  if (!valid) return

  sending.value = true
  try {
    await forgotApi({ email: emailForm.email })
    ElMessage.success('验证码已发送至您的邮箱')
    // 进入步骤二前清空表单数据
    clearResetForm()
    step.value = 2
  } catch {
    // 错误已由拦截器处理
  } finally {
    sending.value = false
  }
}

// 返回邮箱输入步骤
function goBackToEmail() {
  clearResetForm()
  step.value = 1
}

// 清空重置密码表单
function clearResetForm() {
  resetForm.code = ''
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  strengthScore.value = 0
  // 递增 key 强制下次渲染时重建整个步骤二 DOM
  step2Key.value++
  nextTick(() => {
    resetFormRef.value?.clearValidate()
  })
}

// 组件挂载时重置所有表单状态（防止页面切换残留 + 浏览器自动填充）
onMounted(() => {
  emailForm.email = ''
  resetForm.code = ''
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  strengthScore.value = 0
  step.value = 1
  step2Key.value++
  nextTick(() => {
    emailFormRef.value?.clearValidate()
    resetFormRef.value?.clearValidate()
  })
})

// 重置密码（重置成功后返回登录页）
async function resetPassword() {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid) return

  resetting.value = true
  try {
    await resetApi({
      email: emailForm.email,
      code: resetForm.code,
      newPassword: resetForm.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    // 返回登录页面
    router.push('/login')
  } catch {
    // 错误已由拦截器处理
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

.auth-brand-side {
  position: relative;
  width: 46%;
  min-height: 100vh;
  background: linear-gradient(145deg, #7c3aed 0%, #6d28d9 50%, #5b21b6 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 48px;
  overflow: hidden;
}

.brand-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.08); }
.circle-1 { width: 420px; height: 420px; top: -120px; right: -120px; }
.circle-2 { width: 280px; height: 280px; bottom: 80px; left: -80px; }
.circle-3 { width: 160px; height: 160px; top: 40%; right: 15%; background: rgba(255, 255, 255, 0.12); }
.deco-shape { position: absolute; border-radius: 24px; background: rgba(255, 255, 255, 0.1); transform: rotate(-15deg); }
.shape-1 { width: 140px; height: 140px; bottom: 25%; right: 10%; }
.shape-2 { width: 80px; height: 80px; top: 20%; left: 12%; background: rgba(255, 255, 255, 0.06); }

.tech-float { position: absolute; font-size: 32px; opacity: 0.2; animation: techFloat 4s ease-in-out infinite; }
.tech-1 { left: 10%; top: 18%; }
.tech-2 { right: 20%; top: 25%; animation-delay: 1s; }
.tech-3 { left: 20%; bottom: 30%; animation-delay: 2s; }
@keyframes techFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-12px) rotate(3deg); }
  66% { transform: translateY(6px) rotate(-3deg); }
}
.dot-grid {
  position: absolute; inset: 0;
  background-image: radial-gradient(rgba(255,255,255,0.08) 1px, transparent 1px);
  background-size: 24px 24px;
}

.brand-content { position: relative; z-index: 1; max-width: 480px; }
.brand-logo { display: flex; align-items: center; gap: 14px; margin-bottom: 48px; }
.logo-icon-wrap {
  width: 56px; height: 56px; background: rgba(255,255,255,0.15);
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.2);
}
.logo-text { font-size: 30px; font-weight: 800; letter-spacing: 1px; }
.brand-slogan { font-size: 38px; font-weight: 700; line-height: 1.3; margin-bottom: 18px; }
.brand-subtitle { font-size: 16px; line-height: 1.6; opacity: 0.9; margin-bottom: 56px; }

.brand-features { display: flex; flex-direction: column; gap: 24px; }
.feature-item {
  display: flex; align-items: flex-start; gap: 16px; padding: 12px;
  border-radius: 12px; transition: background 0.3s;
  &:hover { background: rgba(255,255,255,0.08); }
}
.feature-icon {
  width: 44px; height: 44px; border-radius: 12px; background: rgba(255, 255, 255, 0.18);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; backdrop-filter: blur(4px);
}
.feature-title { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.feature-desc { font-size: 14px; opacity: 0.8; line-height: 1.5; }
.brand-footer { position: relative; z-index: 1; font-size: 13px; opacity: 0.7; }

/* 右侧表单 */
.auth-form-side {
  flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px 24px;
}
.auth-card {
  width: 100%; max-width: 440px; background: #fff;
  border-radius: 20px; padding: 48px 44px; box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}
.card-logo { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 12px; }
.card-logo-text { font-size: 22px; font-weight: 700; color: #1f2937; }
.card-welcome { text-align: center; font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 6px; }
.card-hint { text-align: center; font-size: 14px; color: #9ca3af; margin: 0 0 24px; }

.step-bar { display: flex; align-items: center; justify-content: center; margin-bottom: 28px; }
.step-item { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.step-num {
  width: 28px; height: 28px; border-radius: 50%; background: #e5e7eb; color: #9ca3af;
  font-size: 14px; font-weight: 600; display: flex; align-items: center; justify-content: center;
}
.step-item.active .step-num { background: #7c3aed; color: #fff; }
.step-item.current .step-num { box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.15); }
.step-label { font-size: 13px; color: #9ca3af; }
.step-item.active .step-label { color: #7c3aed; font-weight: 500; }
.step-line { width: 60px; height: 2px; background: #e5e7eb; margin: 0 12px 20px; }
.step-line.active { background: #7c3aed; }

.email-display {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: #f0fdf4; border: 1px solid #86efac; border-radius: 8px;
  font-size: 14px; color: #166534;
}

::deep(.el-input__inner) { height: 46px; }
::deep(.el-input__prefix-inner) { color: #9ca3af; }

.auth-submit-btn { width: 100%; height: 46px; border-radius: 8px; font-size: 16px; font-weight: 600; }

/* 密码强度 */
.password-strength { margin: -8px 0 16px; }
.strength-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.strength-label { font-size: 13px; color: #4b5563; }
.strength-text { font-size: 13px; font-weight: 600; }
.strength-bar { height: 6px; background: #e5e7eb; border-radius: 3px; overflow: hidden; margin-bottom: 6px; }
.strength-fill { height: 100%; border-radius: 3px; transition: all 0.3s ease; }

.form-back-row {
  display: flex; align-items: center; justify-content: center; gap: 4px;
  color: #6b7280; font-size: 13px; cursor: pointer; margin: 16px 0; transition: color 0.2s;
  &:hover { color: #7c3aed; }
}

.auth-footer { margin-top: 24px; text-align: center; font-size: 14px; color: #6b7280; }
.auth-tip { margin-right: 4px; }

.safe-tips {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  margin-top: 20px; padding-top: 20px; border-top: 1px solid #f3f4f6;
  font-size: 12px; color: #9ca3af;
}

@media (max-width: 992px) {
  .auth-page { flex-direction: column; }
  .auth-brand-side { width: 100%; min-height: auto; padding: 40px 24px; text-align: center; }
  .brand-logo { justify-content: center; margin-bottom: 24px; }
  .brand-slogan { font-size: 28px; }
  .brand-subtitle { margin-bottom: 32px; }
  .brand-features { display: none; }
  .brand-footer { display: none; }
  .auth-form-side { padding: 32px 20px; }
  .auth-card { padding: 32px 24px; box-shadow: none; }
  .step-line { width: 40px; }
}
</style>
