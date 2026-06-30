<template>
  <div class="profile-page">
    <!-- Hero -->
    <div class="pf-hero">
      <div class="pfh-decor">
        <div class="pfh-circle c1"></div><div class="pfh-circle c2"></div>
      </div>
      <div class="pfh-content">
        <div class="pfh-avatar">
          <span>👤</span>
        </div>
        <h1>求职画像</h1>
        <p>完善你的个人资料，让AI更精准地为你匹配岗位</p>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 表单区 -->
      <el-col :lg="16" :md="24">
        <div class="pf-form-card">
          <div class="pff-section">
            <div class="pff-section-title">
              <span class="pff-st-icon">🎓</span> 教育背景
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <label class="pf-label">学校</label>
                <el-input v-model="form.school" placeholder="请输入学校名称" size="large" />
              </el-col>
              <el-col :span="12">
                <label class="pf-label">专业</label>
                <el-input v-model="form.major" placeholder="如：计算机科学、软件工程" size="large" />
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 16px;">
              <el-col :span="12">
                <label class="pf-label">学历</label>
                <el-select v-model="form.education" size="large" style="width:100%">
                  <el-option label="专科" value="专科" /><el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" /><el-option label="博士" value="博士" />
                </el-select>
              </el-col>
              <el-col :span="12">
                <label class="pf-label">求职状态</label>
                <el-select v-model="form.jobStatus" size="large" style="width:100%">
                  <el-option label="在校" value="在校" /><el-option label="应届" value="应届" />
                  <el-option label="已毕业" value="已毕业" />
                </el-select>
              </el-col>
            </el-row>
          </div>

          <div class="pff-divider"></div>

          <div class="pff-section">
            <div class="pff-section-title">
              <span class="pff-st-icon">💼</span> 求职意向
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <label class="pf-label">期望城市</label>
                <el-input v-model="form.expectedCity" placeholder="如：深圳、北京、杭州" size="large" />
              </el-col>
              <el-col :span="12">
                <label class="pf-label">期望薪资</label>
                <el-input v-model="form.expectedSalary" placeholder="如：20K-40K" size="large" />
              </el-col>
            </el-row>
          </div>

          <div class="pff-divider"></div>

          <div class="pff-section">
            <div class="pff-section-title">
              <span class="pff-st-icon">⚡</span> 技能标签
            </div>
            <div class="pf-skills-input">
              <el-input
                v-model="skillInput"
                placeholder="输入技能后按回车添加（如：Java、Python、Vue、Spring Boot）"
                size="large"
                @keyup.enter="addSkill"
              />
              <div class="pf-skills-cloud" v-if="skills.length">
                <el-tag
                  v-for="s in skills"
                  :key="s"
                  closable
                  @close="removeSkill(s)"
                  class="pf-skill-tag"
                  :color="tagColor(s)"
                  effect="dark"
                >
                  {{ getSkillIcon(s) }} {{ s }}
                </el-tag>
              </div>
              <div v-else class="pf-skills-empty">
                💡 添加你的核心技能，帮助我们更精准推荐
              </div>
            </div>
          </div>

          <div class="pff-divider"></div>

          <div class="pff-section">
            <div class="pff-section-title">
              <span class="pff-st-icon">📝</span> 个人总结
            </div>
            <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="简要介绍你的技术栈、项目经验和个人特点..." />
          </div>

          <div class="pff-actions">
            <el-button type="primary" size="large" @click="save" :loading="loading" class="pff-save-btn">
              💾 保存画像
            </el-button>
          </div>
        </div>
      </el-col>

      <!-- 右侧提示 -->
      <el-col :lg="8" :md="24">
        <div class="pf-sidebar">
          <div class="pfs-card fill-guide">
            <div class="pfs-card-icon">📋</div>
            <h4>填写指南</h4>
            <div class="pfs-guide-list">
              <div class="pfs-guide-item">
                <span class="pfs-gi-check">✓</span>
                <span>学校专业信息越详细，越容易被发现</span>
              </div>
              <div class="pfs-guide-item">
                <span class="pfs-gi-check">✓</span>
                <span>技能标签至少添加 3 个核心技能</span>
              </div>
              <div class="pfs-guide-item">
                <span class="pfs-gi-check">✓</span>
                <span>个人总结突出项目经验和亮点</span>
              </div>
              <div class="pfs-guide-item">
                <span class="pfs-gi-check">✓</span>
                <span>完整填写可提升 AI 推荐准确度 60%+</span>
              </div>
            </div>
          </div>

          <div class="pfs-card completion-card">
            <div class="pfs-card-icon">🎯</div>
            <h4>资料完整度</h4>
            <div class="pfs-completion-ring">
              <svg viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="40" fill="none" stroke="#e5e7eb" stroke-width="8" />
                <circle cx="50" cy="50" r="40" fill="none" stroke="#6366f1"
                  stroke-width="8" stroke-linecap="round"
                  :stroke-dasharray="(completionPercent * 2.51) + ' ' + (251 - completionPercent * 2.51)"
                  transform="rotate(-90 50 50)" />
              </svg>
              <div class="pfs-comp-center">
                <span class="pfs-comp-value">{{ completionPercent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useProfileStore } from '@/stores/profile'
import { ElMessage } from 'element-plus'

const profileStore = useProfileStore()
const loading = ref(false)
const skillInput = ref('')
const skills = ref<string[]>([])
const form = reactive({
  school: '', major: '', education: '', grade: '',
  expectedCity: '', expectedSalary: '', jobStatus: '', summary: ''
})

const completionPercent = computed(() => {
  let filled = 0; const total = 7
  if (form.school) filled++
  if (form.major) filled++
  if (form.education) filled++
  if (form.expectedCity) filled++
  if (form.expectedSalary) filled++
  if (form.jobStatus) filled++
  if (skills.value.length) filled++
  return Math.round((filled / total) * 100)
})

function getSkillIcon(name: string): string {
  const map: Record<string, string> = { 'Java': '☕', 'Python': '🐍', 'JavaScript': '📜', 'TypeScript': '🔷',
    'Vue': '🟢', 'React': '⚛️', 'Spring': '🍃', 'Docker': '🐳', 'MySQL': '🗄️', 'Go': '🔹',
    'C++': '⚙️', 'Node': '💚', 'CSS': '🎨', 'HTML': '📄' }
  for (const [k, v] of Object.entries(map)) { if (name.toLowerCase().includes(k.toLowerCase())) return v }
  return '🔧'
}

const tagColors = ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#6366f1']
function tagColor(skill: string): string {
  let hash = 0
  for (let i = 0; i < skill.length; i++) hash = skill.charCodeAt(i) + ((hash << 5) - hash)
  return tagColors[Math.abs(hash) % tagColors.length]
}

onMounted(async () => {
  await profileStore.fetchProfile()
  if (profileStore.profile) Object.assign(form, profileStore.profile)
  try { skills.value = JSON.parse(profileStore.profile?.skillTags || '[]') } catch {/* ignore */}
})

function addSkill() {
  const s = skillInput.value.trim()
  if (s && !skills.value.includes(s)) skills.value.push(s)
  skillInput.value = ''
}

function removeSkill(s: string) { skills.value = skills.value.filter(v => v !== s) }

async function save() {
  loading.value = true
  try {
    await profileStore.saveProfile({ ...form, skillTags: JSON.stringify(skills.value) })
    ElMessage.success('保存成功')
  } catch {/* handled */}
  finally { loading.value = false }
}
</script>

<style scoped lang="scss">
// ======== Hero ========
.pf-hero {
  position: relative;
  background: linear-gradient(135deg, #eff6ff 0%, #e0f2fe 30%, #f0f9ff 60%, #ede9fe 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  text-align: center;
  overflow: hidden;
}
.pfh-decor {
  position: absolute; inset: 0; pointer-events: none;
  .pfh-circle {
    position: absolute; border-radius: 50%;
    background: rgba(59,130,246,0.06);
    &.c1 { width: 160px; height: 160px; top: -40px; right: -30px; }
    &.c2 { width: 100px; height: 100px; bottom: -20px; left: 15%; }
  }
}
.pfh-content { position: relative; z-index: 1; }
.pfh-avatar {
  width: 64px; height: 64px;
  border-radius: 20px;
  background: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px;
  margin: 0 auto 12px;
  box-shadow: 0 4px 16px rgba(59,130,246,0.1);
}
.pf-hero h1 { font-size: 26px; font-weight: 800; color: #1f2937; margin: 0 0 6px; }
.pf-hero p { color: #6b7280; font-size: 14px; }

// ======== 表单卡片 ========
.pf-form-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

.pff-section {
  margin-bottom: 8px;
}

.pff-section-title {
  font-size: 16px; font-weight: 700; color: #1f2937;
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 18px;
  .pff-st-icon { font-size: 20px; }
}

.pf-label {
  display: block;
  font-size: 13px; font-weight: 600; color: #374151;
  margin-bottom: 6px;
}

.pff-divider {
  height: 1px; background: #f3f4f6;
  margin: 24px 0;
}

.pf-skills-input {
  .pf-skills-cloud {
    display: flex; flex-wrap: wrap; gap: 8px; margin-top: 14px;
    .pf-skill-tag {
      padding: 6px 14px;
      border-radius: 10px;
      font-size: 13px; font-weight: 600;
      border: none;
    }
  }
  .pf-skills-empty {
    margin-top: 12px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 10px;
    font-size: 13px; color: #9ca3af;
    text-align: center;
  }
}

.pff-actions {
  margin-top: 8px;
}

.pff-save-btn {
  height: 48px;
  padding: 0 40px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border: none;
  box-shadow: 0 4px 20px rgba(59,130,246,0.3);
  &:hover { transform: translateY(-2px); }
}

// ======== 侧边栏 ========
.pf-sidebar {
  .pfs-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    margin-bottom: 16px;
    .pfs-card-icon { font-size: 28px; margin-bottom: 8px; }
    h4 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0 0 16px; }
  }
}

.pfs-guide-list {
  display: flex; flex-direction: column; gap: 10px;
  .pfs-guide-item {
    display: flex; align-items: flex-start; gap: 8px;
    font-size: 13px; color: #374151; line-height: 1.4;
    .pfs-gi-check {
      width: 20px; height: 20px;
      border-radius: 50%;
      background: #d1fae5;
      color: #065f46;
      display: flex; align-items: center; justify-content: center;
      font-size: 11px; font-weight: 700; flex-shrink: 0; margin-top: 1px;
    }
  }
}

.pfs-completion-ring {
  display: flex; justify-content: center;
  position: relative;
  width: 100px; height: 100px;
  margin: 0 auto;
  svg { width: 100px; height: 100px; }
  .pfs-comp-center {
    position: absolute; inset: 0;
    display: flex; align-items: center; justify-content: center;
    .pfs-comp-value { font-size: 24px; font-weight: 800; color: #6366f1; }
  }
}
</style>
