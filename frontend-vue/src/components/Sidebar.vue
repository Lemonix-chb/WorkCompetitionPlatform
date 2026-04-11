<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': collapsed, 'sidebar-mobile-open': mobileOpen }">
    <!-- Logo Section -->
    <div class="sidebar-header">
      <div class="logo-container">
        <div class="logo-icon">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#5a7fa8"/>
            <path d="M8 16H24M16 8V24" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span v-if="!collapsed" class="logo-text">竞赛管理系统</span>
      </div>
      <button class="sidebar-toggle" @click="toggleSidebar" v-if="!isMobile">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M6 6L14 14M14 6L6 14" stroke="white" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <div class="nav-section">
        <div v-if="!collapsed" class="nav-section-title">导航菜单</div>

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
        </router-link>
      </div>

      <div class="nav-section nav-section-bottom">
        <router-link to="/competitions" class="nav-item">
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
              <path d="M7 17H5C4 17 3 16 3 15V5C3 4 4 3 5 3H7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M14 13L17 10L14 7M17 10H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span v-if="!collapsed" class="nav-item-text">退出登录</span>
        </div>
      </div>
    </nav>

    <!-- Mobile Toggle -->
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

// Import icon components from JS file
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
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-header {
  padding: 24px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-white);
  white-space: nowrap;
  opacity: 1;
  transition: opacity var(--transition-fast);
}

.sidebar-collapsed .logo-text {
  opacity: 0;
  width: 0;
}

.sidebar-toggle {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: var(--radius-sm);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background var(--transition-fast);
  color: white;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 8px;
  overflow-y: auto;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-section-title {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.4);
  padding: 8px 8px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  position: relative;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text-white);
}

.nav-item-active {
  background: rgba(90, 127, 168, 0.15);
  color: var(--color-accent-light);
}

.nav-item-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: var(--color-accent-light);
  border-radius: 0 3px 3px 0;
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
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  opacity: 1;
  transition: opacity var(--transition-fast);
}

.sidebar-collapsed .nav-item-text {
  opacity: 0;
  width: 0;
}

.sidebar-collapsed .nav-item {
  justify-content: center;
}

.nav-section-bottom {
  margin-top: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 16px;
}

.nav-item-logout {
  color: rgba(255, 255, 255, 0.6);
}

.nav-item-logout:hover {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
}

.sidebar-mobile-toggle {
  position: fixed;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  background: var(--color-bg-sidebar);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  color: white;
  box-shadow: var(--shadow-card);
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