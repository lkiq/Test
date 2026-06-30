<template>
  <div class="loading-skeleton">
    <!-- 列表骨架 -->
    <template v-if="type === 'list'">
      <div v-for="i in rows" :key="i" class="skeleton-row">
        <el-skeleton animated>
          <template #template>
            <div style="display: flex; gap: 16px; align-items: center; padding: 16px 0;">
              <el-skeleton-item variant="circle" style="width: 48px; height: 48px; flex-shrink: 0;" />
              <div style="flex: 1;">
                <el-skeleton-item variant="text" style="width: 40%; height: 18px; margin-bottom: 10px;" />
                <el-skeleton-item variant="text" style="width: 70%; height: 14px;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </template>

    <!-- 卡片骨架 -->
    <template v-else-if="type === 'card'">
      <el-row :gutter="16">
        <el-col v-for="i in count" :key="i" :span="span" style="margin-bottom: 16px;">
          <el-skeleton animated>
            <template #template>
              <div style="padding: 16px;">
                <el-skeleton-item variant="text" style="width: 60%; height: 18px; margin-bottom: 12px;" />
                <el-skeleton-item variant="text" style="width: 80%; height: 14px; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 50%; height: 14px;" />
              </div>
            </template>
          </el-skeleton>
        </el-col>
      </el-row>
    </template>

    <!-- 详情骨架 -->
    <template v-else-if="type === 'detail'">
      <el-skeleton animated>
        <template #template>
          <div style="padding: 20px;">
            <el-skeleton-item variant="text" style="width: 50%; height: 24px; margin-bottom: 16px;" />
            <el-skeleton-item variant="text" style="width: 30%; height: 16px; margin-bottom: 12px;" />
            <el-skeleton-item variant="p" style="width: 100%; height: 80px; margin-bottom: 16px;" />
            <el-skeleton-item variant="text" style="width: 80%; height: 14px; margin-bottom: 8px;" />
            <el-skeleton-item variant="text" style="width: 60%; height: 14px;" />
          </div>
        </template>
      </el-skeleton>
    </template>

    <!-- 通用段落骨架 -->
    <template v-else>
      <el-skeleton :rows="rows" animated />
    </template>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'list' | 'card' | 'detail' | 'text'
  rows?: number
  count?: number
  span?: number
}>(), {
  type: 'text',
  rows: 4,
  count: 6,
  span: 8
})
</script>

<style scoped lang="scss">
.loading-skeleton {
  width: 100%;
}
.skeleton-row {
  border-bottom: 1px solid var(--divider-color);
  &:last-child { border-bottom: none; }
}
</style>
