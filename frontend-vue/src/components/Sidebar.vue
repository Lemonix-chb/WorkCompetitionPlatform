<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': collapsed, 'sidebar-mobile-open': mobileOpen }">
    <!-- Academic Header -->
    <div class="sidebar-header">
      <div class="logo-container">
        <div class="academic-logo">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <!-- Academic Shield -->
            <path d="M20 2L36 8V20C36 30 28 36 20 38C12 36 4 30 4 20V8L20 2Z"
                  fill="var(--color-primary)"
                  stroke="var(--color-accent)"
                  stroke-width="1"/>
            <!-- Inner Star -->
            <path d="M20 10L21.5 14L26 14L22 17L23.5 21L20 18L16.5 21L18 17L14 14L18.5 14L20 10Z"
                  fill="var(--color-accent)"/>
            <!-- Laurels -->
            <path d="M12 28C12 28 14 24 16 26M24 26C24 26 26 24 28 28"
                  stroke="var(--color-accent)"
                  stroke-width="1"
                  stroke-linecap="round"/>
          </svg>
        </div>
        <div v-if="!collapsed" class="logo-text-container">
          <span class="logo-title">竞赛评审系统</span>
          <span class="logo-subtitle">Competition Review</span>
        </div>
      </div>
      <button class="sidebar-toggle" @click="toggleSidebar" v-if="!isMobile">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
          <path d="M5 5L13 13M13 5L5 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <!-- Navigation with Academic Sections -->
    <nav class="sidebar-nav">
      <!-- Main Navigation -->
      <div class="nav-section">
        <div v-if="!collapsed" class="nav-section-header">
          <span class="nav-section-title">主要功能</span>
          <div class="nav-section-divider"></div>
        </div>

        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'nav-item-active': isActive(item.path) }"
        >
          <div class="nav-item-icon">
            <component :is="getIconComponent(item.icon)" />
          </div>
          <span v-if="!collapsed" class="nav-item-text">{{ item.name }}</span>
          <span v-if="!collapsed && isActive(item.path)" class="nav-item-badge"></span>
        </router-link>
      </div>

      <!-- Footer Navigation -->
      <div class="nav-section nav-section-bottom">
        <div v-if="!collapsed" class="nav-section-divider"></div>

        <router-link to="/competitions" class="nav-item nav-item-public">
          <div class="nav-item-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <span v-if="!collapsed" class="nav-item-text">赛事浏览</span>
        </router-link>

        <div class="nav-item nav-item-logout" @click="logout">
          <div class="nav-item-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M7 17H5C4 17 3 16 3 15V5C3 4 4 3 5 3H7"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"/>
              <path d="M14 13L17 10L14 7M17 10H7"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"/>
            </svg>
          </div>
          <span v-if="!collapsed" class="nav-item-text">退出登录</span>
        </div>
      </div>
    </nav>

    <!-- Mobile Toggle Button -->
    <button
      class="sidebar-mobile-toggle"
      @click="toggleMobile"
      v-if="isMobile"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M4 6H20M4 12H20M4 18H20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
    </button>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  HomeIcon,
  TeamIcon,
  WorkIcon,
  InviteIcon,
  AwardIcon,
  SettingsIcon,
  DoneIcon,
  StatsIcon,
  CompIcon,
  UserIcon,
  ReviewIcon
} from './SidebarIcons.js'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)
const mobileOpen = ref(false)
const isMobile = ref(false)

const userRole = computed(() => localStorage.getItem('userRole'))

const menuItems = computed(() => {
  const role = userRole.value

  if (role === 'STUDENT') {
    return [
      { name: '个人中心', path: '/student', icon: 'HomeIcon' },
      { name: '赛事报名', path: '/student/registration', icon: 'CompIcon' },
      { name: '我的团队', path: '/student/teams', icon: 'TeamIcon' },
      { name: '我的作品', path: '/student/works', icon: 'WorkIcon' },
      { name: '获奖记录', path: '/student/results', icon: 'AwardIcon' },
      { name: '个人设置', path: '/student/profile', icon: 'SettingsIcon' }
    ]
  } else if (role === 'JUDGE') {
    return [
      { name: '评审中心', path: '/judge', icon: 'HomeIcon' },
      { name: '待评作品', path: '/judge/pending', icon: 'WorkIcon' },
      { name: '已评作品', path: '/judge/reviewed', icon: 'DoneIcon' },
      { name: '评审统计', path: '/judge/stats', icon: 'StatsIcon' },
      { name: '个人设置', path: '/judge/profile', icon: 'SettingsIcon' }
    ]
  } else if (role === 'ADMIN') {
    return [
      { name: '管理中心', path: '/admin', icon: 'HomeIcon' },
      { name: '赛事管理', path: '/admin/competitions', icon: 'CompIcon' },
      { name: '评审员管理', path: '/admin/judges', icon: 'UserIcon' },
      { name: '参赛者管理', path: '/admin/students', icon: 'UserIcon' },
      { name: '作品管理', path: '/admin/works', icon: 'WorkIcon' },
      { name: '评审管理', path: '/admin/reviews', icon: 'ReviewIcon' },
      { name: '数据统计', path: '/admin/stats', icon: 'StatsIcon' }
    ]
  }

  return []
})

const getIconComponent = (iconName) => {
  const iconMap = {
    HomeIcon,
    TeamIcon,
    WorkIcon,
    InviteIcon,
    AwardIcon,
    SettingsIcon,
    DoneIcon,
    StatsIcon,
    CompIcon,
    UserIcon,
    ReviewIcon
  }
  return iconMap[iconName] || HomeIcon
}

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

const toggleMobile = () => {
  mobileOpen.value = !mobileOpen.value
}

const logout = () => {
  localStorage.clear()
  ElMessage.success('已退出登录')
  router.push('/')
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileOpen.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background: var(--color-bg-sidebar);
  transition: width var(--transition-base), transform var(--transition-base);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(13, 33, 55, 0.15);
  border-right: 2px solid rgba(52, 152, 219, 0.1);
}

.sidebar-collapsed {
  width: var(--sidebar-collapsed);
}

.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid rgba(52, 152, 219, 0.15);
  background: rgba(13, 33, 55, 0.02);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.academic-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  filter: drop-shadow(0 0 6px rgba(52, 152, 219, 0.4));
}

.logo-text-container {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-white);
  white-space: nowrap;
  letter-spacing: 0.01em;
  line-height: 1.2;
}

.logo-subtitle {
  font-family: var(--font-body);
  font-size: 11px;
  color: rgba(52, 152, 219, 0.8);
  white-space: nowrap;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-collapsed .logo-text-container {
  display: none;
}

.sidebar-toggle {
  background: rgba(52, 152, 219, 0.12);
  border: 1px solid rgba(52, 152, 219, 0.2);
  border-radius: var(--radius-md);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-accent);
}

.sidebar-toggle:hover {
  background: rgba(52, 152, 219, 0.18);
  border-color: var(--color-accent);
  color: var(--color-accent-light);
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 8px;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: rgba(52, 152, 219, 0.3) transparent;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(52, 152, 219, 0.3);
  border-radius: 2px;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-section-header {
  margin-bottom: 8px;
}

.nav-section-title {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 600;
  color: rgba(52, 152, 219, 0.6);
  padding: 8px 8px 4px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  display: block;
}

.nav-section-divider {
  height: 1px;
  background: linear-gradient(90deg, rgba(52, 152, 219, 0.3) 0%, transparent 100%);
  margin: 4px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 10px;
  border-radius: var(--radius-md);
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  position: relative;
  border: 1px solid transparent;
}

.nav-item:hover {
  background: rgba(52, 152, 219, 0.08);
  color: var(--color-text-white);
  border-color: rgba(52, 152, 219, 0.15);
}

.nav-item-active {
  background: rgba(52, 152, 219, 0.12);
  color: var(--color-accent-light);
  border-color: rgba(52, 152, 219, 0.25);
  box-shadow: 0 0 8px rgba(52, 152, 219, 0.2);
}

.nav-item-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: var(--color-accent);
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 6px rgba(52, 152, 219, 0.5);
}

.nav-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-item-text {
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  letter-spacing: 0.01em;
}

.nav-item-badge {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-accent);
  box-shadow: 0 0 6px rgba(52, 152, 219, 0.6);
}

.sidebar-collapsed .nav-item-text,
.sidebar-collapsed .nav-item-badge {
  display: none;
}

.sidebar-collapsed .nav-item {
  justify-content: center;
  padding: 12px 6px;
}

.nav-section-bottom {
  margin-top: auto;
  padding-top: 16px;
}

.nav-item-public {
  color: rgba(255, 255, 255, 0.6);
}

.nav-item-public:hover {
  color: var(--color-accent-light);
}

.nav-item-logout {
  color: rgba(255, 255, 255, 0.5);
}

.nav-item-logout:hover {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.12);
  border-color: rgba(231, 76, 60, 0.25);
}

.sidebar-mobile-toggle {
  position: fixed;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  background: var(--color-bg-sidebar);
  border: 2px solid rgba(52, 152, 219, 0.2);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  color: var(--color-accent);
  box-shadow: 0 4px 12px rgba(13, 33, 55, 0.2);
}

.sidebar-mobile-toggle:hover {
  background: rgba(52, 152, 219, 0.12);
  border-color: var(--color-accent);
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    width: var(--sidebar-width);
  }

  .sidebar-mobile-open {
    transform: translateX(0);
  }

  .sidebar-collapsed {
    width: var(--sidebar-width);
  }

  .sidebar-toggle {
    display: none;
  }
}
</style>