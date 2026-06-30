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
        <div class="tech-float tech-1">☕</div>
        <div class="tech-float tech-2">🐍</div>
        <div class="tech-float tech-3">⚛️</div>
        <div class="tech-float tech-4">🤖</div>
        <div class="tech-float tech-5">🔷</div>
        <div class="dot-grid"></div>
      </div>

      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon-wrap">
            <el-icon size="36" color="#fff"><Connection /></el-icon>
          </div>
          <span class="logo-text">IT求职</span>
        </div>
        <h1 class="brand-slogan">IT求职<span class="slogan-highlight"> · 技术人才的专属平台</span></h1>
        <p class="brand-subtitle">智能推荐、简历优化、技术面试模拟，助你拿下理想技术岗位</p>

        <div class="brand-features">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon size="22" color="#fff"><Search /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-title">技术岗位精准匹配</div>
                <div class="feature-desc">基于AI分析你的技术栈与项目经验，精准匹配前端、后端、算法等IT岗位</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon size="22" color="#fff"><DocumentChecked /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-title">技术简历智能优化</div>
                <div class="feature-desc">AI诊断技术简历短板，优化项目经验描述，提升简历通过率</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon size="22" color="#fff"><ChatDotRound /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-title">技术面试模拟训练</div>
                <div class="feature-desc">算法题、系统设计、八股文全覆盖，真实面试场景模拟</div>
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
          <el-icon size="32" color="#2563eb"><Connection /></el-icon>
          <span class="card-logo-text">IT求职平台</span>
        </div>

        <h2 class="card-welcome">欢迎回来 👋</h2>
        <p class="card-hint">登录你的账号，开启求职之旅</p>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div class="login-tab" :class="{ active: loginMode === 'password' }" @click="switchMode('password')">密码登录</div>
          <div class="login-tab" :class="{ active: loginMode === 'code' }" @click="switchMode('code')">验证码登录</div>
        </div>

        <!-- 密码登录表单 -->
        <el-form v-show="loginMode === 'password'" :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" size="small">记住我</el-checkbox>
            <el-link type="primary" :underline="false" class="forgot-link" @click="$router.push('/forgot-password')">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              class="auth-submit-btn"
              @click="handleLogin"
              :loading="loading"
            >
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 验证码登录表单 -->
        <el-form v-show="loginMode === 'code'" :model="codeForm" :rules="codeRules" ref="codeFormRef" label-width="0" size="large">
          <el-form-item prop="email">
            <el-input
              v-model="codeForm.email"
              placeholder="请输入注册邮箱"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-input-row">
              <el-input
                v-model="codeForm.code"
                placeholder="请输入6位验证码"
                :prefix-icon="Key"
                maxlength="6"
                clearable
                @keyup.enter="handleCodeLogin"
              />
              <el-button
                class="send-code-btn"
                :type="countdown > 0 ? 'info' : 'primary'"
                :disabled="countdown > 0"
                @click="sendCode"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="auth-submit-btn"
              @click="handleCodeLogin"
              :loading="codeLoading"
            >
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <span class="auth-tip">还没有账号？</span>
          <el-link type="primary" :underline="false" @click="$router.push('/register')">
            立即注册
          </el-link>
        </div>

        <div class="safe-tips">
          <el-icon size="12"><Lock /></el-icon>
          <span>您的信息将受到严格保护，请放心登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  User, Lock, Message, Key, Search, DocumentChecked, ChatDotRound,
  Connection
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { sendLoginCode as sendLoginCodeApi, loginByCode } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const rememberMe = ref(false)

// 登录模式
const loginMode = ref<'password' | 'code'>('password')
function switchMode(mode: 'password' | 'code') {
  loginMode.value = mode
}

// 密码登录
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const formRef = ref<FormInstance>()

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res: any = await userStore.login(form)
    redirectByRole(res.data?.role)
  } catch (e: any) {
    if (e?.response?.data?.message || e?.message) {
      ElMessage.error(e.response?.data?.message || e.message)
    }
  } finally {
    loading.value = false
  }
}

// 验证码登录
const codeLoading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const codeForm = reactive({ email: '', code: '' })
const codeRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ]
}
const codeFormRef = ref<FormInstance>()

let countdownTimer: ReturnType<typeof setInterval> | null = null

async function sendCode() {
  const valid = await codeFormRef.value?.validateField('email').catch(() => false)
  if (!valid) return

  sendingCode.value = true
  try {
    await sendLoginCodeApi({ email: codeForm.email })
    ElMessage.success('验证码已发送至您的邮箱')
    // 60秒倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        if (countdownTimer) clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch {
    // 错误已由拦截器处理
  } finally {
    sendingCode.value = false
  }
}

async function handleCodeLogin() {
  const valid = await codeFormRef.value?.validate().catch(() => false)
  if (!valid) return

  codeLoading.value = true
  try {
    const res: any = await loginByCode({ email: codeForm.email, code: codeForm.code })
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    redirectByRole(res.data?.role)
  } catch (e: any) {
    if (e?.response?.data?.message || e?.message) {
      ElMessage.error(e.response?.data?.message || e.message)
    }
  } finally {
    codeLoading.value = false
  }
}

function redirectByRole(role: string) {
  if (role === 'STUDENT') window.location.hash = '#/student/home'
  else if (role === 'HR') window.location.hash = '#/enterprise/home'
  else if (role === 'ADMIN') window.location.hash = '#/admin/home'
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

.brand-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.08); }
.circle-1 { width: 420px; height: 420px; top: -120px; right: -120px; }
.circle-2 { width: 280px; height: 280px; bottom: 80px; left: -80px; }
.circle-3 { width: 160px; height: 160px; top: 40%; right: 15%; background: rgba(255, 255, 255, 0.12); }
.deco-shape { position: absolute; border-radius: 24px; background: rgba(255, 255, 255, 0.1); transform: rotate(-15deg); }
.shape-1 { width: 140px; height: 140px; bottom: 25%; right: 10%; }
.shape-2 { width: 80px; height: 80px; top: 20%; left: 12%; background: rgba(255, 255, 255, 0.06); }

/* 浮动技术图标 */
.tech-float { position: absolute; font-size: 32px; opacity: 0.2; animation: techFloat 4s ease-in-out infinite; }
.tech-1 { left: 10%; top: 18%; animation-delay: 0s; }
.tech-2 { right: 20%; top: 25%; animation-delay: 1s; }
.tech-3 { left: 20%; bottom: 30%; animation-delay: 2s; }
.tech-4 { right: 15%; bottom: 20%; animation-delay: 0.5s; }
.tech-5 { left: 60%; top: 55%; animation-delay: 1.5s; }
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

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 480px;
}

.brand-logo { display: flex; align-items: center; gap: 14px; margin-bottom: 48px; }
.logo-icon-wrap {
  width: 56px; height: 56px; background: rgba(255,255,255,0.15);
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.2);
}
.logo-text { font-size: 30px; font-weight: 800; letter-spacing: 1px; }
.brand-slogan { font-size: 38px; font-weight: 700; line-height: 1.3; margin-bottom: 18px; }
.slogan-highlight {
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
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
  display: flex; align-items: flex-start; gap: 16px; padding: 12px;
  border-radius: 12px; transition: background 0.3s;
  &:hover { background: rgba(255,255,255,0.08); }
}
.feature-icon {
  width: 44px; height: 44px; border-radius: 12px; background: rgba(255, 255, 255, 0.18);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; backdrop-filter: blur(4px);
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
  width: 100%; max-width: 440px; background: #fff;
  border-radius: 20px; padding: 48px 44px; box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}

.card-logo { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 12px; }
.card-logo-text { font-size: 22px; font-weight: 700; color: #1f2937; }
.card-welcome { text-align: center; font-size: 22px; font-weight: 700; color: #1f2937; margin: 0 0 6px; }
.card-hint { text-align: center; font-size: 14px; color: #9ca3af; margin: 0 0 24px; }

/* 登录方式 Tab */
.login-tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 28px;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 16px;
  color: #6b7280;
  cursor: pointer;
  position: relative;
  transition: color 0.25s;
}

.login-tab:hover {
  color: #2563eb;
}

.login-tab.active {
  color: #2563eb;
  font-weight: 600;
}

.login-tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #2563eb;
  border-radius: 2px;
}

:deep(.el-input__inner) {
  height: 46px;
}

:deep(.el-input__prefix-inner) {
  color: #9ca3af;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 20px;
}

.forgot-link {
  font-size: 13px;
}

.auth-submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}

/* 验证码输入行 */
.code-input-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-input-row .el-input {
  flex: 1;
}

.send-code-btn {
  width: 120px;
  height: 46px;
  flex-shrink: 0;
  font-size: 13px;
  border-radius: 8px;
}

/* 其他登录方式 */
.other-login {
  margin-top: 24px;
}

:deep(.el-divider__text) {
  color: #9ca3af;
  font-size: 12px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 16px;
}

.social-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #fff;
}

.social-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.social-btn.wechat {
  background: #07c160;
}

.social-btn.qq {
  background: #12b7f5;
}

.social-btn.dingtalk {
  background: #0089ff;
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.auth-tip {
  margin-right: 4px;
}

.safe-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  padding-top: 20px;
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
}
</style>
