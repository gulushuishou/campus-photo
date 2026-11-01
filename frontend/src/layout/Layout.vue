<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">校园约拍平台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/home"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>
        <el-menu-item index="/demand"><el-icon><Document /></el-icon>约拍需求</el-menu-item>
        <el-menu-item index="/works"><el-icon><Picture /></el-icon>作品展示</el-menu-item>
        <el-menu-item index="/order"><el-icon><List /></el-icon>我的订单</el-menu-item>
        <el-menu-item index="/message"><el-icon><ChatDotRound /></el-icon>私信</el-menu-item>
        <el-menu-item index="/profile"><el-icon><User /></el-icon>个人中心</el-menu-item>
        <el-menu-item v-if="userStore.role === 'ADMIN'" index="/admin"><el-icon><Setting /></el-icon>管理后台</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '校园约拍平台' }}</div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info" @click="goProfile">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">{{ userStore.userInfo?.nickname?.[0] || 'U' }}</el-avatar>
              <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const goProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: 500;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}
.nickname {
  font-size: 14px;
  color: #333;
}
.main {
  background: #f5f7fa;
}
</style>
