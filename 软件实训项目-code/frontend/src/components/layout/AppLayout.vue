<template>
  <div class="app-layout" v-if="isLoggedIn">
    <HeaderBar />
    <div class="content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <keep-alive include="JobMatching">
            <component :is="Component" />
          </keep-alive>
        </transition>
      </router-view>
    </div>
    <FloatCustomerService />
  </div>
  <router-view v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import HeaderBar from './HeaderBar.vue'
import FloatCustomerService from '@/components/customer-service/FloatCustomerService.vue'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
</script>

<style scoped>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>