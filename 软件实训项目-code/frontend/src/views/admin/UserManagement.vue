<template>
  <div class="admin-user-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2>用户管理</h2>
        <p>管理平台所有注册用户，支持按角色筛选、账号启停及密码重置</p>
      </div>
      <div class="page-header-right">
        <el-button @click="fetchUsers" :loading="loading" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="user-stats">
      <div class="user-stat-item stat-total">
        <div class="stat-num">{{ totalUsers }}</div>
        <div class="stat-label">用户总数</div>
      </div>
      <div class="user-stat-item stat-active">
        <div class="stat-num">{{ activeUsers }}</div>
        <div class="stat-label">正常用户</div>
      </div>
      <div class="user-stat-item stat-disabled">
        <div class="stat-num">{{ disabledUsers }}</div>
        <div class="stat-label">已禁用</div>
      </div>
      <div class="user-stat-item stat-students">
        <div class="stat-num">{{ studentCount }}</div>
        <div class="stat-label">学生</div>
      </div>
      <div class="user-stat-item stat-hr">
        <div class="stat-num">{{ hrCount }}</div>
        <div class="stat-label">企业HR</div>
      </div>
      <div class="user-stat-item stat-admins">
        <div class="stat-num">{{ adminCount }}</div>
        <div class="stat-label">管理员</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名或邮箱…"
          :prefix-icon="Search"
          clearable
          size="large"
          class="filter-input"
          @keyup.enter="fetchUsers"
          @clear="fetchUsers"
        />
        <el-select v-model="filterRole" placeholder="角色筛选" clearable size="large" class="filter-select" @change="fetchUsers">
          <el-option label="全部角色" value="" />
          <el-option label="学生" value="STUDENT" />
          <el-option label="企业HR" value="HR" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </div>
      <div class="filter-right">
        <el-button type="primary" size="large" @click="fetchUsers" :icon="Search">搜索</el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table :data="users" stripe v-loading="loading" size="large" empty-text="暂无用户数据">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="140">
          <template #default="scope">
            <div class="user-cell">
              <div class="user-avatar">{{ scope.row.username?.charAt(0)?.toUpperCase() }}</div>
              <span class="user-name">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.role)" effect="light" size="small">
              {{ roleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" align="center">
          <template #default="scope">
            <span :class="{ 'text-muted': !scope.row.phone }">{{ scope.row.phone || '未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="scope">
            <span :class="{ 'text-muted': !scope.row.email }">{{ scope.row.email || '未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="scope">
            <span class="status-dot" :class="scope.row.status === 'ACTIVE' ? 'dot-active' : 'dot-disabled'"></span>
            {{ scope.row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
              plain
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="resetPwd(scope.row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > size">
        <el-pagination
          :total="total"
          v-model:current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchUsers"
          @size-change="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getUsers, updateUserStatus, resetUserPassword } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const filterRole = ref('')
const loading = ref(false)

// 统计
const totalUsers = computed(() => total.value)
const activeUsers = computed(() => users.value.filter(u => u.status === 'ACTIVE').length)
const disabledUsers = computed(() => users.value.filter(u => u.status !== 'ACTIVE').length)
const studentCount = computed(() => users.value.filter(u => u.role === 'STUDENT').length)
const hrCount = computed(() => users.value.filter(u => u.role === 'HR').length)
const adminCount = computed(() => users.value.filter(u => u.role === 'ADMIN').length)

function roleLabel(role: string) {
  const map: Record<string, string> = { STUDENT: '学生', HR: '企业HR', ADMIN: '管理员' }
  return map[role] || role
}

function roleTagType(role: string) {
  const map: Record<string, string> = { STUDENT: '', HR: 'warning', ADMIN: 'danger' }
  return map[role] || 'info'
}

async function fetchUsers() {
  loading.value = true
  try {
    const res: any = await getUsers({ page: page.value, size: size.value, keyword: keyword.value, role: filterRole.value })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

function onSizeChange(val: number) {
  size.value = val
  page.value = 1
  fetchUsers()
}

async function toggleStatus(user: any) {
  const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = newStatus === 'DISABLED' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户「${user.username}」吗？`, `${actionText}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    fetchUsers()
  } catch {
    // 取消操作或错误
  }
}

/**
 * 重置用户密码，弹窗让管理员输入新密码
 */
async function resetPwd(user: any) {
  try {
    const { value } = await ElMessageBox.prompt(`为「${user.username}」输入新密码`, '重置密码', {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /.{6,}/,
      inputErrorMessage: '密码长度至少6位'
    })
    await resetUserPassword(user.id, value!)
    ElMessage.success(`用户「${user.username}」密码已重置`)
  } catch {
    // 取消操作或错误
  }
}

onMounted(fetchUsers)
</script>

<style scoped lang="scss">
.admin-user-page {
  max-width: 1280px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px 28px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.page-header-left h2 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}
.page-header-left p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* 统计概览 */
.user-stats {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.user-stat-item {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}
.stat-total .stat-num { color: #2563eb; }
.stat-active .stat-num { color: #16a34a; }
.stat-disabled .stat-num { color: #dc2626; }
.stat-students .stat-num { color: #2563eb; }
.stat-hr .stat-num { color: #ea580c; }
.stat-admins .stat-num { color: #9333ea; }

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}
.filter-left {
  display: flex;
  gap: 12px;
  flex: 1;
}
.filter-input { width: 280px; }
.filter-select { width: 160px; }

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

/* 用户单元格 */
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.user-name {
  font-weight: 500;
  color: #1a1a2e;
}

.text-muted {
  color: #c0c4cc;
}

/* 状态点 */
.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 5px;
}
.dot-active { background: #16a34a; }
.dot-disabled { background: #dc2626; }

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
}
:deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  border-bottom: none;
}
:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f5f5f5;
}
:deep(.el-table__body tr:hover > td) {
  background: #f8faff;
}

@media (max-width: 992px) {
  .user-stats { grid-template-columns: repeat(3, 1fr); }
  .filter-left { flex-direction: column; }
  .filter-input, .filter-select { width: 100%; }
}
@media (max-width: 640px) {
  .user-stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
