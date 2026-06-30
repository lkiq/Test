<template>
  <span :class="['skill-tag', status]">
    {{ name }}
  </span>
</template>

<script setup lang="ts">
defineProps<{ name: string; status: 'mastered' | 'learning' | 'missing' }>()
</script>
