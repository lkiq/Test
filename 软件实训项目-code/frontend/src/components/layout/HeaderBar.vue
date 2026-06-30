<template>
  <div class="header-wrapper">
    <header class="header-bar">
      <div class="header-inner">
        <!-- 左侧：Logo + 城市 -->
        <div class="header-left">
          <el-button v-if="isMobile" text :icon="Menu" class="menu-btn" @click="mobileDrawer = true" />
          <router-link to="/student/home" class="brand">
            <el-icon size="26" color="#2563eb"><Connection /></el-icon>
            <span class="brand-text">IT求职</span>
          </router-link>

          <el-dropdown v-if="!isMobile && userStore.role !== 'STUDENT' && userStore.role !== 'HR'" trigger="click" popper-class="city-dropdown">
            <span class="city-select">
              <span>{{ currentCity }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <div class="city-panel">
                <div class="city-section">
                  <div class="city-label">热门城市</div>
                  <div class="city-list">
                    <span
                      v-for="c in hotCities"
                      :key="c"
                      class="city-item"
                      :class="{ active: currentCity === c }"
                      @click="currentCity = c"
                    >{{ c }}</span>
                  </div>
                </div>
                <div class="city-section">
                  <div class="city-label">更多城市</div>
                  <div class="city-list">
                    <span
                      v-for="c in moreCities"
                      :key="c"
                      class="city-item"
                      :class="{ active: currentCity === c }"
                      @click="currentCity = c"
                    >{{ c }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-dropdown>
        </div>

        <!-- 中间：主导航 -->
        <nav v-if="!isMobile" class="main-nav">
          <router-link
            v-for="item in visibleNavs"
            :key="item.path || item.name"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item) }"
          >{{ item.name }}</router-link>

          <el-dropdown v-if="aiNavs.length" trigger="hover" popper-class="nav-dropdown">
            <span class="nav-item" :class="{ active: aiActive }">
              AI求职辅导
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="item in aiNavs" :key="item.path" @click="router.push(item.path)">
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.name }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <!-- 右侧：搜索 + 消息 + 用户 -->
        <div class="header-right">
          <!-- 管理员不显示搜索框 -->
          <div v-if="!isMobile && userStore.role !== 'ADMIN'" class="global-search">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              :placeholder="searchPlaceholder"
              @keyup.enter="handleGlobalSearch"
            />
            <el-button type="primary" class="search-btn" @click="handleGlobalSearch">搜索</el-button>
          </div>

          <el-badge :value="unreadCount" :hidden="!unreadCount" class="header-icon-btn" @click="router.push('/chat')">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>

          <el-dropdown @command="handleCommand" popper-class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ userStore.username }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-if="userStore.role === 'STUDENT'">
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon>求职画像</el-dropdown-item>
                  <el-dropdown-item command="profile-center"><el-icon><UserFilled /></el-icon>个人中心</el-dropdown-item>
                  <el-dropdown-item command="applications"><el-icon><DocumentChecked /></el-icon>我的投递</el-dropdown-item>
                  <el-dropdown-item command="favorites"><el-icon><Star /></el-icon>我的收藏</el-dropdown-item>
                  <el-dropdown-item command="chat"><el-icon><ChatDotRound /></el-icon>消息</el-dropdown-item>
                </template>
                <template v-if="userStore.role === 'HR'">
                  <el-dropdown-item command="enterprise-home"><el-icon><OfficeBuilding /></el-icon>企业后台</el-dropdown-item>
                  <el-dropdown-item command="enterprise-jobs"><el-icon><Briefcase /></el-icon>岗位管理</el-dropdown-item>
                  <el-dropdown-item command="enterprise-profile-center"><el-icon><UserFilled /></el-icon>个人中心</el-dropdown-item>
                  <el-dropdown-item command="chat"><el-icon><ChatDotRound /></el-icon>消息</el-dropdown-item>
                </template>
                <template v-if="userStore.role === 'ADMIN'">
                  <el-dropdown-item command="admin-profile-center"><el-icon><UserFilled /></el-icon>个人中心</el-dropdown-item>
                  <el-dropdown-item command="admin-users"><el-icon><Setting /></el-icon>用户管理</el-dropdown-item>
                  <el-dropdown-item command="admin-skills"><el-icon><Collection /></el-icon>技能词典</el-dropdown-item>
                </template>
                <el-dropdown-item command="customer-service"><el-icon><Service /></el-icon>智能客服</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 移动端抽屉菜单 -->
    <transition name="fade">
      <div v-if="mobileDrawer" class="mobile-overlay" @click="mobileDrawer = false" />
    </transition>
    <transition name="slide">
      <div v-if="mobileDrawer" class="mobile-drawer">
        <div class="drawer-header">
          <router-link to="/student/home" class="brand" @click="mobileDrawer = false">
            <el-icon size="26" color="#2563eb"><Connection /></el-icon>
            <span class="brand-text">IT求职</span>
          </router-link>
          <el-icon class="close-btn" @click="mobileDrawer = false"><Close /></el-icon>
        </div>
        <div class="drawer-body">
          <div class="drawer-section">
            <div class="drawer-title">主菜单</div>
            <router-link
              v-for="item in visibleNavs"
              :key="item.path"
              :to="item.path"
              class="drawer-link"
              :class="{ active: isActive(item) }"
              @click="mobileDrawer = false"
            >{{ item.name }}</router-link>
          </div>
          <div class="drawer-section">
            <div class="drawer-title">AI 求职辅导</div>
            <router-link
              v-for="item in aiNavs"
              :key="item.path"
              :to="item.path"
              class="drawer-link"
              :class="{ active: route.path === item.path }"
              @click="mobileDrawer = false"
            >{{ item.name }}</router-link>
          </div>
          <div class="drawer-section">
            <div class="drawer-title">个人中心</div>
            <router-link
              v-for="item in userNavs"
              :key="item.path"
              :to="item.path"
              class="drawer-link"
              :class="{ active: route.path === item.path }"
              @click="mobileDrawer = false"
            >{{ item.name }}</router-link>
            <router-link
              to="/customer-service"
              class="drawer-link"
              :class="{ active: route.path === '/customer-service' }"
              @click="mobileDrawer = false"
            >智能客服</router-link>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useJobStore } from '@/stores/job'
import { useChatStore } from '@/stores/chat'
import {
  UserFilled, ArrowDown, Search, Bell, User, DocumentChecked, Star,
  SwitchButton, Menu, Connection, Close, EditPen, Compass, DataAnalysis, List,
  Document, ChatDotRound, TrendCharts, Service, OfficeBuilding, Briefcase, Setting, Collection
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const jobStore = useJobStore()
const chatStore = useChatStore()

const searchKeyword = ref('')
const unreadCount = computed(() => chatStore.unreadTotal)

const searchPlaceholder = computed(() => {
  if (userStore.role === 'HR') return '搜索我的岗位…'
  return '搜索职位、公司'
})
const isMobile = ref(false)
const mobileDrawer = ref(false)
const currentCity = ref('深圳')

const hotCities = ['全国', '北京', '上海', '广州', '深圳', '杭州', '成都', '武汉']
const moreCities = ['南京', '西安', '苏州', '重庆', '天津', '长沙', '郑州', '东莞', '青岛', '厦门', '宁波', '合肥']

const visibleNavs = computed(() => {
  if (userStore.role === 'STUDENT') {
    return [
      { name: '首页', path: '/student/home' },
      { name: '技术职位', path: '/student/job-matching' },
      { name: '名企内推', path: '/student/job-matching' },
      { name: '校招实习', path: '/student/job-matching' },
      { name: '消息', path: '/student/messages' }
    ]
  }
  if (userStore.role === 'HR') {
    return [
      { name: '企业首页', path: '/enterprise/home' },
      { name: '职位管理', path: '/enterprise/jobs' },
      { name: '人才推荐', path: '/enterprise/recommend' },
      { name: '候选人', path: '/enterprise/candidates' },
      { name: '消息', path: '/chat' }
    ]
  }
  return [
    { name: '管理首页', path: '/admin/home' },
    { name: '用户管理', path: '/admin/users' },
    { name: '技能词典', path: '/admin/skills' }
  ]
})

const aiNavs = computed(() => {
  if (userStore.role !== 'STUDENT') return []
  return [
    { name: '求职画像', path: '/student/profile', icon: markRaw(User) },
    { name: '能力测评', path: '/student/assessment', icon: markRaw(EditPen) },
    { name: '方向探索', path: '/student/career-exploration', icon: markRaw(Compass) },
    { name: '差距分析', path: '/student/gap-analysis', icon: markRaw(DataAnalysis) },
    { name: '学习路径', path: '/student/learning-path', icon: markRaw(List) },
    { name: '简历优化', path: '/student/resume-optimize', icon: markRaw(Document) },
    { name: '模拟面试', path: '/student/interview', icon: markRaw(ChatDotRound) },
    { name: '学习进度', path: '/student/learning-progress', icon: markRaw(TrendCharts) }
  ]
})

const userNavs = computed(() => {
  if (userStore.role !== 'STUDENT') return []
  return [
    { name: '求职画像', path: '/student/profile' },
    { name: '我的投递', path: '/student/applications' },
    { name: '我的收藏', path: '/student/favorites' },
    { name: '消息中心', path: '/student/messages' }
  ]
})

const aiActive = computed(() => aiNavs.value.some(item => route.path === item.path))

function isActive(item: { path: string }) {
  return route.path === item.path || route.path.startsWith(item.path + '/')
}

function handleGlobalSearch() {
  if (!searchKeyword.value.trim()) return
  const keyword = searchKeyword.value.trim()
  if (userStore.role === 'HR') {
    // HR搜索自己的岗位，跳转岗位管理页并传关键词
    router.push({ path: '/enterprise/jobs', query: { keyword, _t: Date.now() } })
  } else {
    // 用 query 参数触发 JobMatchingView 的 watch，即使已在同一页面也能响应
    router.push({ path: '/student/job-matching', query: { q: keyword, _t: Date.now() } })
  }
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') userStore.logout()
  else if (cmd === 'profile') router.push('/student/profile')
  else if (cmd === 'profile-center') router.push('/student/profile-center')
  else if (cmd === 'applications') router.push('/student/applications')
  else if (cmd === 'favorites') router.push('/student/favorites')
  else if (cmd === 'chat') router.push('/chat')
  else if (cmd === 'enterprise-home') router.push('/enterprise/home')
  else if (cmd === 'enterprise-jobs') router.push('/enterprise/jobs')
  else if (cmd === 'enterprise-profile-center') router.push('/enterprise/profile-center')
  else if (cmd === 'admin-profile-center') router.push('/admin/profile-center')
  else if (cmd === 'admin-users') router.push('/admin/users')
  else if (cmd === 'admin-skills') router.push('/admin/skills')
  else if (cmd === 'customer-service') router.push('/customer-service')
}

function updateMobile() {
  isMobile.value = window.innerWidth <= 992
  if (!isMobile.value) mobileDrawer.value = false
}

onMounted(() => {
  updateMobile()
  window.addEventListener('resize', updateMobile)
  // 启动聊天未读数轮询
  chatStore.fetchUnreadCount()
})
onUnmounted(() => window.removeEventListener('resize', updateMobile))
</script>

<style scoped lang="scss">
.header-wrapper {
  width: 100%;
  flex-shrink: 0;
}

.header-bar {
  width: 100%;
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.menu-btn {
  font-size: 20px;
  padding: 8px;
  margin-left: -8px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  .brand-text {
    font-size: 20px;
    font-weight: 800;
    color: var(--text-primary);
    letter-spacing: -0.5px;
  }
}

.city-select {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background 0.2s;
  &:hover { background: #f3f4f6; }
  .arrow { font-size: 12px; color: var(--text-tertiary); }
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 18px;
  font-size: 15px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
  font-weight: 500;
  cursor: pointer;
  .arrow { font-size: 12px; transition: transform 0.2s; }
  &:hover {
    color: var(--primary-color);
    background: var(--primary-light);
  }
  &.active {
    color: var(--primary-color);
    background: var(--primary-light);
    font-weight: 600;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}

.global-search {
  position: relative;
  width: 260px;
  display: flex;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  overflow: hidden;
  background: #f9fafb;
  transition: all 0.2s;
  &:focus-within {
    border-color: var(--primary-color);
    background: #fff;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  }
  input {
    flex: 1;
    height: 36px;
    border: none;
    padding: 0 12px 0 32px;
    font-size: 14px;
    outline: none;
    background: transparent;
  }
  .search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-tertiary);
  }
  .search-btn {
    height: 36px;
    border-radius: 0;
    padding: 0 16px;
  }
}

.header-icon-btn {
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.2s;
  &:hover { color: var(--primary-color); }
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
  &:hover { background: #f3f4f6; }
  .arrow { font-size: 12px; color: var(--text-tertiary); }
}
.username {
  font-size: 14px;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 移动端抽屉
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 998;
}
.mobile-drawer {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  background: #fff;
  z-index: 999;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
}
.drawer-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
  .close-btn {
    font-size: 20px;
    color: var(--text-secondary);
    cursor: pointer;
    &:hover { color: var(--text-primary); }
  }
}
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.drawer-section {
  margin-bottom: 20px;
}
.drawer-title {
  padding: 0 20px 8px;
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}
.drawer-link {
  display: block;
  padding: 12px 20px;
  font-size: 15px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s;
  &:hover {
    color: var(--primary-color);
    background: var(--primary-light);
  }
  &.active {
    color: var(--primary-color);
    background: var(--primary-light);
    font-weight: 600;
  }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-enter-active, .slide-leave-active { transition: transform 0.25s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(-100%); }

@media (max-width: 1200px) {
  .global-search { width: 200px; }
}

@media (max-width: 992px) {
  .header-inner { padding: 0 16px; gap: 12px; }
  .main-nav { display: none; }
  .global-search { width: 180px; }
  .username { display: none; }
}

@media (max-width: 768px) {
  .header-inner { padding: 0 12px; }
  .global-search { display: none; }
  .brand-text { font-size: 18px; }
}
</style>

<style lang="scss">
.city-dropdown {
  .el-dropdown__wrapper { padding: 0; }
}
.city-panel {
  width: 400px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}
.city-section { margin-bottom: 16px; }
.city-section:last-child { margin-bottom: 0; }
.city-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 10px;
}
.city-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.city-item {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--text-secondary);
  background: #f9fafb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    color: var(--primary-color);
    background: var(--primary-light);
  }
  &.active {
    color: #fff;
    background: var(--primary-color);
  }
}

.nav-dropdown .el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
}
</style>
