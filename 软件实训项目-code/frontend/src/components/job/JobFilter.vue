<template>
  <div class="job-filter">
    <el-input
      v-model="filters.keyword"
      placeholder="搜索职位、公司"
      clearable
      :prefix-icon="Search"
      @keyup.enter="emitSearch"
    />

    <el-form label-position="top" size="small" class="filter-form">
      <el-form-item label="城市">
        <el-select v-model="filters.city" placeholder="不限" clearable @change="emitSearch">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>

      <el-form-item label="岗位方向">
        <el-checkbox-group v-model="filters.direction" @change="emitSearch">
          <el-checkbox v-for="d in directions" :key="d" :value="d">{{ d }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="薪资范围">
        <el-select v-model="salaryLabel" placeholder="不限" clearable @change="onSalaryChange">
          <el-option v-for="s in salaryOptions" :key="s.label" :label="s.label" :value="s.label" />
        </el-select>
      </el-form-item>

      <el-form-item label="经验要求">
        <el-select v-model="filters.experience" placeholder="不限" clearable @change="emitSearch">
          <el-option v-for="e in experiences" :key="e" :label="e" :value="e" />
        </el-select>
      </el-form-item>

      <el-form-item label="学历要求">
        <el-select v-model="filters.education" placeholder="不限" clearable @change="emitSearch">
          <el-option v-for="e in educations" :key="e" :label="e" :value="e" />
        </el-select>
      </el-form-item>

      <el-form-item label="工作性质">
        <el-select v-model="filters.jobType" placeholder="不限" clearable @change="emitSearch">
          <el-option v-for="t in jobTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-button type="primary" plain style="width: 100%; margin-top: 8px;" @click="emitSearch">
      <el-icon class="el-icon--left"><Search /></el-icon>搜索岗位
    </el-button>
    <el-button style="width: 100%; margin-top: 8px; margin-left: 0;" @click="reset">
      清空筛选
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useJobStore } from '@/stores/job'

const emit = defineEmits(['search'])
const jobStore = useJobStore()
const filters = jobStore.filters

const cities = ['北京', '上海', '深圳', '广州', '杭州', '成都', '武汉']
const directions = ['后端开发', '前端开发', '数据', '产品', '测试', '移动开发', '运维', '安全', '设计', '管理', '嵌入式']
const experiences = ['经验不限', '1年以内', '1-3年', '3-5年', '5-10年', '10年以上']
const educations = ['学历不限', '大专', '本科', '硕士', '博士']
const jobTypes = ['全职', '实习', '兼职', '校招']
const salaryOptions = [
  { label: '不限', value: null },
  { label: '10K以下', value: [0, 10] },
  { label: '10K-20K', value: [10, 20] },
  { label: '20K-30K', value: [20, 30] },
  { label: '30K-50K', value: [30, 50] },
  { label: '50K以上', value: [50, 999] }
]

const salaryLabel = ref('不限')

function onSalaryChange(label: string) {
  const option = salaryOptions.find(s => s.label === label)
  filters.salaryRange = option?.value ? (option.value as [number, number]) : null
  emitSearch()
}

function emitSearch() {
  jobStore.page = 1
  emit('search')
}

function reset() {
  salaryLabel.value = '不限'
  jobStore.resetFilters()
  emit('search')
}

// 同步外部筛选变化
watch(() => jobStore.filters.salaryRange, (val) => {
  const option = salaryOptions.find(s => JSON.stringify(s.value) === JSON.stringify(val))
  salaryLabel.value = option ? option.label : '不限'
})
</script>

<style scoped lang="scss">
.job-filter {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}
.filter-form {
  margin-top: 12px;
}
:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}
:deep(.el-checkbox) {
  margin-right: 12px;
}
</style>
