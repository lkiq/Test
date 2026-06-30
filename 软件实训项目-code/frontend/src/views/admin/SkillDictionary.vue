<template>
  <div class="admin-skill-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2>技能词典</h2>
        <p>管理平台技能标签库，用于能力测评、岗位匹配和简历分析</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" size="large" @click="openAddDialog" :icon="Plus">新增技能</el-button>
        <el-button @click="fetchSkills" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- 分类统计 -->
    <div class="category-stats" v-if="categoryStats.length">
      <div
        class="cate-stat"
        v-for="cat in categoryStats"
        :key="cat.name"
        :class="{ active: activeCategory === cat.name }"
        @click="filterByCategory(cat.name)"
      >
        <div class="cate-num">{{ cat.count }}</div>
        <div class="cate-label">{{ cat.name }}</div>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="table-card">
      <!-- 顶部搜索 -->
      <div class="table-toolbar">
        <el-input
          v-model="searchWord"
          placeholder="搜索技能名称或描述…"
          :prefix-icon="Search"
          clearable
          size="large"
          class="toolbar-search"
          @input="onSearchInput"
          @clear="fetchSkills"
        />
        <span class="toolbar-total" v-if="skills.length">共 {{ skills.length }} 项技能</span>
      </div>

      <el-table :data="skills" stripe v-loading="loading" size="large" empty-text="暂无技能数据">
        <el-table-column prop="name" label="技能名称" min-width="160">
          <template #default="scope">
            <div class="skill-name-cell">
              <span class="skill-tag-icon">#</span>
              <strong>{{ scope.row.name }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="130" align="center">
          <template #default="scope">
            <el-tag :type="categoryTagType(scope.row.category)" effect="light" size="small">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="技能描述" min-width="280">
          <template #default="scope">
            <span :class="{ 'text-muted': !scope.row.description }">
              {{ scope.row.description || '暂无描述' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="editSkill(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="removeSkill(scope.row.id)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingSkill ? '编辑技能' : '新增技能'"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form label-width="80px" label-position="right">
        <el-form-item label="技能名称" required>
          <el-input v-model="skillForm.name" placeholder="例如：Java、Spring Boot、MySQL" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="技能类别" required>
          <el-select v-model="skillForm.category" style="width: 100%;">
            <el-option label="编程语言" value="编程语言">
              <span>💻 编程语言</span>
            </el-option>
            <el-option label="框架" value="框架">
              <span>🏗 框架</span>
            </el-option>
            <el-option label="数据库" value="数据库">
              <span>🗄 数据库</span>
            </el-option>
            <el-option label="工具" value="工具">
              <span>🛠 工具</span>
            </el-option>
            <el-option label="软技能" value="软技能">
              <span>🤝 软技能</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="技能描述">
          <el-input
            v-model="skillForm.description"
            type="textarea"
            placeholder="请输入该技能的简要描述…"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSkill" :loading="saveLoading">
          {{ editingSkill ? '更新技能' : '添加技能' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { getSkills, addSkill as addSkillApi, updateSkill as updateSkillApi, deleteSkill } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const skills = ref<any[]>([])
const searchWord = ref('')
const showAddDialog = ref(false)
const editingSkill = ref<any>(null)
const loading = ref(false)
const saveLoading = ref(false)
const activeCategory = ref('')
const skillForm = reactive({ name: '', category: '编程语言', description: '' })

// 搜索防抖
let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(fetchSkills, 400)
}

// 分类统计
const categoryStats = computed(() => {
  const map: Record<string, number> = {}
  skills.value.forEach(s => {
    const c = s.category || '其他'
    map[c] = (map[c] || 0) + 1
  })
  return Object.entries(map).map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
})

function filterByCategory(cat: string) {
  if (activeCategory.value === cat) {
    activeCategory.value = ''
    fetchSkills()
  } else {
    activeCategory.value = cat
    fetchSkills()
  }
}

function categoryTagType(cat: string) {
  const map: Record<string, string> = {
    '编程语言': '', '框架': 'success', '数据库': 'warning', '工具': 'info', '软技能': 'danger'
  }
  return map[cat] || 'info'
}

async function fetchSkills() {
  loading.value = true
  try {
    const res: any = await getSkills({
      keyword: searchWord.value,
      category: activeCategory.value || undefined,
      page: 1,
      size: 200
    })
    skills.value = res.data?.records || []
  } catch {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  editingSkill.value = null
  Object.assign(skillForm, { name: '', category: '编程语言', description: '' })
  showAddDialog.value = true
}

function editSkill(skill: any) {
  editingSkill.value = skill
  Object.assign(skillForm, {
    name: skill.name,
    category: skill.category,
    description: skill.description || ''
  })
  showAddDialog.value = true
}

async function saveSkill() {
  if (!skillForm.name.trim()) {
    ElMessage.warning('请输入技能名称')
    return
  }
  saveLoading.value = true
  try {
    if (editingSkill.value) {
      await updateSkillApi(editingSkill.value.id, {
        name: skillForm.name.trim(),
        category: skillForm.category,
        description: skillForm.description.trim()
      })
      ElMessage.success('技能更新成功')
    } else {
      await addSkillApi({
        name: skillForm.name.trim(),
        category: skillForm.category,
        description: skillForm.description.trim()
      })
      ElMessage.success('技能添加成功')
    }
    showAddDialog.value = false
    editingSkill.value = null
    fetchSkills()
  } catch {
    // 错误由拦截器处理
  } finally {
    saveLoading.value = false
  }
}

async function removeSkill(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该技能吗？删除后相关岗位和测评将不再显示此技能。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSkill(id)
    ElMessage.success('技能已删除')
    fetchSkills()
  } catch {
    // 取消或错误
  }
}

onMounted(fetchSkills)
</script>

<style scoped lang="scss">
.admin-skill-page {
  max-width: 1280px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
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
.page-header-right {
  display: flex;
  gap: 10px;
}

/* 分类统计 */
.category-stats {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.cate-stat {
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  text-align: center;
  cursor: pointer;
  min-width: 90px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 2px solid transparent;
  transition: all 0.2s ease;
  &:hover {
    border-color: #e0e7ff;
    background: #f8faff;
  }
  &.active {
    border-color: #2563eb;
    background: #e8f0fe;
    .cate-label { color: #2563eb; font-weight: 600; }
  }
}
.cate-num {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.cate-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.toolbar-search { width: 320px; }
.toolbar-total {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}

/* 表格内部 */
.skill-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.skill-tag-icon {
  color: #2563eb;
  font-weight: 700;
  font-size: 16px;
}

.text-muted { color: #c0c4cc; }

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

@media (max-width: 768px) {
  .page-header { flex-direction: column; gap: 12px; }
  .toolbar-search { width: 100%; }
}
</style>
