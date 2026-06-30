<template>
  <div class="progress-page">
    <!-- Hero -->
    <div class="pg-hero">
      <div class="pgh-bg"></div>
      <div class="pgh-content">
        <h1>📈 学习进度</h1>
        <p>追踪你的学习旅程，见证每一步成长</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="pg-stats">
      <div v-for="(stat, idx) in stats" :key="stat.label" class="pg-stat-card" :class="'stat-' + (idx + 1)">
        <div class="psc-icon-wrap">
          <span class="psc-icon">{{ statIcons[idx] }}</span>
        </div>
        <div class="psc-info">
          <span class="psc-value">{{ stat.value }}</span>
          <span class="psc-label">{{ stat.label }}</span>
        </div>
        <div class="psc-bar">
          <div class="psc-bar-fill" :style="{ width: statPercent(stat) + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 技能掌握度 -->
    <div class="pg-skills">
      <div class="pg-section-header">
        <h3>🎯 技能掌握度</h3>
        <span class="pg-section-hint">持续学习，每天进步</span>
      </div>

      <div class="pg-skills-grid" v-if="skillData?.skillScores">
        <div v-for="(v, k) in skillData.skillScores" :key="k" class="pg-skill-card">
          <div class="pskc-header">
            <div class="pskc-icon-wrap" :style="{ background: skillColor(v) + '18', color: skillColor(v) }">
              {{ getSkillIcon(k as unknown as string) }}
            </div>
            <div class="pskc-info">
              <span class="pskc-name">{{ k }}</span>
              <span class="pskc-level">{{ skillData?.skillLevels?.[k] || '-' }}</span>
            </div>
            <span class="pskc-score" :style="{ color: skillColor(v) }">{{ v }}分</span>
          </div>
          <div class="pskc-bar-wrap">
            <div class="pskc-bar">
              <div class="pskc-bar-fill"
                :style="{ width: v + '%', background: skillGradient(v) }"
              ></div>
            </div>
          </div>
          <div class="pskc-footer">
            <span class="pskc-tag" :class="levelClass(skillData?.skillLevels?.[k])">
              {{ levelLabel(skillData?.skillLevels?.[k]) }}
            </span>
          </div>
        </div>
      </div>

      <div v-else class="pg-empty-inline">
        <span>📭</span> 暂无技能数据，请先完成测评
      </div>
    </div>

    <!-- 学习建议 -->
    <div class="pg-advice">
      <div class="pg-advice-card">
        <div class="pac-header">
          <span class="pac-icon">💡</span>
          <div>
            <h4>学习建议</h4>
            <p>根据你的技能掌握情况，AI给你以下建议</p>
          </div>
        </div>
        <div class="pac-items">
          <div class="pac-item">
            <span class="paci-num">01</span>
            <span>优先补齐薄弱技能，均衡发展各项能力</span>
          </div>
          <div class="pac-item">
            <span class="paci-num">02</span>
            <span>针对目标岗位要求，重点提升关键技能</span>
          </div>
          <div class="pac-item">
            <span class="paci-num">03</span>
            <span>通过项目实战巩固理论知识，积累作品</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProgressOverview, getSkillProgress } from '@/api/student'

const stats = ref<any[]>([
  { label: '任务完成率', value: '0%' }, { label: '测评次数', value: 0 },
  { label: '面试次数', value: 0 }, { label: '简历分析', value: 0 }
])
const statIcons = ['✅', '📝', '🎤', '📄']
const skillData = ref<any>(null)

onMounted(async () => {
  try {
    const overview: any = await getProgressOverview()
    const d = overview.data
    stats.value = [
      { label: '任务完成率', value: `${d.completionRate || 0}%` },
      { label: '测评次数', value: d.totalAssessmentCount || 0 },
      { label: '面试次数', value: d.totalInterviewCount || 0 },
      { label: '简历分析', value: d.resumeAnalysisCount || 0 }
    ]
    const skill: any = await getSkillProgress()
    skillData.value = skill.data
  } catch {/* ignored */}
})

function statPercent(stat: any): number {
  if (stat.label === '任务完成率') return parseInt(stat.value) || 0
  return Math.min(Number(stat.value) * 10, 100)
}

function getSkillIcon(name: string): string {
  const map: Record<string, string> = { 'Java': '☕', 'Python': '🐍', 'JavaScript': '📜', 'TypeScript': '🔷',
    'Vue': '🟢', 'React': '⚛️', 'Spring': '🍃', 'Docker': '🐳', 'MySQL': '🗄️' }
  return map[name] || '🔧'
}

function skillColor(v: number): string {
  if (v >= 80) return '#10b981'; if (v >= 60) return '#3b82f6'; if (v >= 35) return '#f59e0b'; return '#ef4444'
}

function skillGradient(v: number): string {
  if (v >= 80) return 'linear-gradient(90deg, #10b981, #34d399)'
  if (v >= 60) return 'linear-gradient(90deg, #3b82f6, #60a5fa)'
  if (v >= 35) return 'linear-gradient(90deg, #f59e0b, #fbbf24)'
  return 'linear-gradient(90deg, #ef4444, #f87171)'
}

function levelClass(level: string): string {
  if (level === '精通' || level === '熟练') return 'lvl-high'
  if (level === '掌握') return 'lvl-mid'
  return 'lvl-low'
}

function levelLabel(level: string): string {
  if (level === '精通') return '⭐ 精通'; if (level === '熟练') return '✅ 熟练'
  if (level === '掌握') return '📖 掌握'; if (level === '了解') return '📌 了解'
  return '🆕 入门'
}
</script>

<style scoped lang="scss">
// ======== Hero ========
.pg-hero {
  position: relative;
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 30%, #ecfeff 70%, #eff6ff 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  text-align: center;
  overflow: hidden;
  border: 1px solid #d1fae5;
  .pgh-bg {
    position: absolute; inset: 0;
    background-image: radial-gradient(circle, rgba(16,185,129,0.06) 1px, transparent 1px);
    background-size: 24px 24px;
  }
  .pgh-content { position: relative; z-index: 1; }
  h1 { font-size: 26px; font-weight: 800; color: #065f46; margin: 0 0 6px; }
  p { color: #047857; font-size: 14px; opacity: 0.8; }
}

// ======== 统计卡片 ========
.pg-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.pg-stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.3s;
  &:hover { transform: translateY(-3px); box-shadow: 0 6px 24px rgba(0,0,0,0.08); }
  .psc-icon-wrap {
    width: 44px; height: 44px;
    border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 12px;
    .psc-icon { font-size: 22px; }
  }
  &.stat-1 .psc-icon-wrap { background: #d1fae5; }
  &.stat-2 .psc-icon-wrap { background: #dbeafe; }
  &.stat-3 .psc-icon-wrap { background: #ede9fe; }
  &.stat-4 .psc-icon-wrap { background: #fef3c7; }
  .psc-info {
    display: flex; flex-direction: column; gap: 2px; margin-bottom: 12px;
    .psc-value { font-size: 24px; font-weight: 800; color: #1f2937; }
    .psc-label { font-size: 12px; color: #6b7280; }
  }
  .psc-bar { height: 4px; background: #e5e7eb; border-radius: 2px; overflow: hidden; }
  .psc-bar-fill {
    height: 100%;
    border-radius: 2px;
    transition: width 1s ease-out;
    &.stat-1 & { background: #10b981; }
    &.stat-2 & { background: #3b82f6; }
    &.stat-3 & { background: #8b5cf6; }
    &.stat-4 & { background: #f59e0b; }
  }
  &.stat-1 .psc-bar-fill { background: #10b981; }
  &.stat-2 .psc-bar-fill { background: #3b82f6; }
  &.stat-3 .psc-bar-fill { background: #8b5cf6; }
  &.stat-4 .psc-bar-fill { background: #f59e0b; }
}

// ======== 技能掌握 ========
.pg-skills {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

.pg-section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
  h3 { font-size: 17px; font-weight: 700; color: #1f2937; margin: 0; }
  .pg-section-hint { font-size: 12px; color: #9ca3af; }
}

.pg-skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.pg-skill-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 18px 20px;
  transition: all 0.3s;
  &:hover { border-color: #d1d5db; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
}

.pskc-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
  .pskc-icon-wrap {
    width: 36px; height: 36px;
    border-radius: 10px;
    display: flex; align-items: center; justify-content: center;
    font-size: 18px; flex-shrink: 0;
  }
  .pskc-info { flex: 1; display: flex; flex-direction: column; }
  .pskc-name { font-size: 14px; font-weight: 600; color: #374151; }
  .pskc-level { font-size: 11px; color: #9ca3af; }
  .pskc-score { font-size: 18px; font-weight: 800; }
}

.pskc-bar-wrap { .pskc-bar { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; } }
.pskc-bar-fill { height: 100%; border-radius: 4px; transition: width 1s ease-out; }

.pskc-footer { margin-top: 8px; }
.pskc-tag {
  display: inline-block;
  padding: 2px 10px; border-radius: 6px; font-size: 11px; font-weight: 600;
  &.lvl-high { background: #d1fae5; color: #065f46; }
  &.lvl-mid { background: #fef3c7; color: #92400e; }
  &.lvl-low { background: #fee2e2; color: #991b1b; }
}

.pg-empty-inline {
  text-align: center; padding: 40px; color: #9ca3af; font-size: 14px;
}

// ======== 学习建议 ========
.pg-advice-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.pac-header {
  display: flex; align-items: center; gap: 14px; margin-bottom: 20px;
  .pac-icon { font-size: 28px; }
  h4 { font-size: 16px; font-weight: 700; color: #1f2937; margin: 0; }
  p { font-size: 12px; color: #6b7280; margin: 2px 0 0; }
}

.pac-items {
  display: flex; flex-direction: column; gap: 10px;
  .pac-item {
    display: flex; align-items: center; gap: 12px;
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 10px;
    font-size: 13px; color: #374151;
    .paci-num {
      width: 28px; height: 28px;
      border-radius: 8px;
      background: #10b981;
      color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700; flex-shrink: 0;
    }
  }
}
</style>
