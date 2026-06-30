import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/student/home'
  },
  // 登录/注册/忘记密码
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginView.vue'), meta: { noAuth: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/register/RegisterView.vue'), meta: { noAuth: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/login/ForgotPasswordView.vue'), meta: { noAuth: true } },

  // 学生端
  { path: '/student/home', name: 'StudentHome', component: () => import('@/views/student/StudentHome.vue') },
  { path: '/student/profile', name: 'Profile', component: () => import('@/views/student/ProfileView.vue') },
  { path: '/student/assessment', name: 'Assessment', component: () => import('@/views/student/AssessmentView.vue') },
  { path: '/student/career-exploration', name: 'CareerExploration', component: () => import('@/views/student/CareerExplorationView.vue') },
  { path: '/student/job-matching', name: 'JobMatching', component: () => import('@/views/student/JobMatchingView.vue'), meta: { keepAlive: true } },
  { path: '/student/job/:id', name: 'JobDetail', component: () => import('@/views/student/JobDetailView.vue') },
  { path: '/student/gap-analysis', name: 'GapAnalysis', component: () => import('@/views/student/GapAnalysisView.vue') },
  { path: '/student/learning-path', name: 'LearningPathList', component: () => import('@/views/student/LearningPathList.vue') },
  { path: '/student/learning-path/detail/:pathId', name: 'LearningPathDetail', component: () => import('@/views/student/LearningPathDetailView.vue') },
  { path: '/student/learning-progress', name: 'LearningProgress', component: () => import('@/views/student/LearningProgressView.vue') },
  { path: '/student/learning-result', name: 'LearningResult', component: () => import('@/views/student/LearningResultView.vue') },
  { path: '/student/resume-optimize', name: 'ResumeOptimize', component: () => import('@/views/student/ResumeOptimizeView.vue') },
  { path: '/student/interview', name: 'Interview', component: () => import('@/views/student/InterviewView.vue') },
  { path: '/student/applications', name: 'Applications', component: () => import('@/views/student/ApplicationsView.vue') },
  { path: '/student/favorites', name: 'Favorites', component: () => import('@/views/student/FavoritesView.vue') },
  { path: '/student/messages', name: 'Messages', component: () => import('@/views/student/MessagesView.vue') },
  { path: '/student/profile-center', name: 'StudentProfileCenter', component: () => import('@/views/student/ProfileCenterView.vue') },

  // 企业端
  { path: '/enterprise/home', name: 'EnterpriseHome', component: () => import('@/views/enterprise/EnterpriseHome.vue') },
  { path: '/enterprise/recommend', name: 'Recommend', component: () => import('@/views/enterprise/RecommendView.vue') },
  { path: '/enterprise/jobs', name: 'JobPost', component: () => import('@/views/enterprise/JobPostView.vue') },
  { path: '/enterprise/candidates', name: 'CandidatePool', component: () => import('@/views/enterprise/CandidatePoolView.vue') },
  { path: '/enterprise/interviews', name: 'InterviewSchedule', component: () => import('@/views/enterprise/InterviewScheduleView.vue') },
  { path: '/enterprise/profile-center', name: 'EnterpriseProfileCenter', component: () => import('@/views/enterprise/ProfileCenterView.vue') },

  // 管理端
  { path: '/admin/home', name: 'AdminHome', component: () => import('@/views/admin/AdminHome.vue'), meta: { role: 'ADMIN' } },
  { path: '/admin/users', name: 'UserManagement', component: () => import('@/views/admin/UserManagement.vue'), meta: { role: 'ADMIN' } },
  { path: '/admin/skills', name: 'SkillDictionary', component: () => import('@/views/admin/SkillDictionary.vue'), meta: { role: 'ADMIN' } },
  { path: '/admin/profile-center', name: 'AdminProfileCenter', component: () => import('@/views/admin/ProfileCenterView.vue'), meta: { role: 'ADMIN' } },

  // 聊天（学生-HR 私信）
  { path: '/chat', name: 'Chat', component: () => import('@/views/chat/ChatView.vue') },

  // 智能客服
  { path: '/customer-service', name: 'CustomerService', component: () => import('@/views/customer/CustomerServiceView.vue') },

  // 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：未登录 → 登录页；角色不符 → 403
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 无需认证的页面
  if (to.meta.noAuth) {
    return next()
  }

  // 未登录跳转登录页
  if (!token) {
    return next('/login')
  }

  // 角色校验
  if (to.meta.role && role !== to.meta.role) {
    return next({ name: role === 'STUDENT' ? 'StudentHome' : role === 'HR' ? 'EnterpriseHome' : 'AdminHome' })
  }

  next()
})

export default router
