<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      class="sidebar-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <!-- Logo -->
      <div class="sidebar-logo">
        <span v-if="!appStore.sidebarCollapsed" class="logo-text">零售分析系统</span>
        <span v-else class="logo-text-mini">零售</span>
      </div>

      <!-- 菜单项 -->
      <el-menu-item index="/dashboard">
        <el-icon><DataAnalysis /></el-icon>
        <span>可视化仪表盘</span>
      </el-menu-item>

      <template v-if="authStore.isAdmin() || authStore.isManager()">
        <el-sub-menu index="store-group">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>门店管理</span>
          </template>
          <el-menu-item index="/stores">门店列表</el-menu-item>
        </el-sub-menu>
      </template>

      <el-menu-item index="/behaviors">
        <el-icon><Document /></el-icon>
        <span>行为数据</span>
      </el-menu-item>

      <el-sub-menu index="analytics-group">
        <template #title>
          <el-icon><TrendCharts /></el-icon>
          <span>数据分析</span>
        </template>
        <el-menu-item index="/analytics/traffic">客流统计</el-menu-item>
        <el-menu-item index="/analytics/heatmap">热力图分析</el-menu-item>
        <el-menu-item index="/analytics/duration">停留时长分析</el-menu-item>
        <el-menu-item index="/analytics/conversion">购买转化分析</el-menu-item>
      </el-sub-menu>

      <el-menu-item v-if="authStore.isAdmin()" index="/users">
        <el-icon><UserFilled /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
    </el-menu>

    <!-- 主体区域 -->
    <div class="main-container">
      <!-- 顶栏 -->
      <div class="topbar">
        <div class="topbar-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar()">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <span class="page-title-text">{{ currentPageTitle }}</span>
        </div>
        <div class="topbar-right">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ authStore.user?.username || '未知用户' }}
          </span>
          <el-tag size="small" :type="roleTagType">{{ roleLabel }}</el-tag>
          <el-button type="danger" link @click="authStore.logout()">退出登录</el-button>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import {
  DataAnalysis, Shop, Document, TrendCharts, UserFilled,
  Fold, Expand, User
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => route.meta?.title || '')

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '店长', analyst: '数据分析师' }
  return map[authStore.user?.role] || '未知'
})

const roleTagType = computed(() => {
  const map = { admin: 'danger', manager: 'warning', analyst: 'info' }
  return map[authStore.user?.role] || 'info'
})
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar-menu {
  width: 220px;
  min-height: 100vh;
  overflow-y: auto;
  transition: width 0.3s;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.el-menu--collapse {
  width: 64px;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text-mini {
  font-size: 14px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: #409EFF;
}

.page-title-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.page-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}
</style>
