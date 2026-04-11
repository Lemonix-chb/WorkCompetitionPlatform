import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/competitions'
  },
  {
    path: '/competitions',
    name: 'Competitions',
    component: () => import('../views/Competitions.vue')
  },
  {
    path: '/competitions/:id/tracks',
    name: 'Tracks',
    component: () => import('../views/Tracks.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  // Student routes with layout
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        name: 'StudentHome',
        component: () => import('../views/student/Home.vue')
      },
      {
        path: 'registration',
        name: 'Registration',
        component: () => import('../views/student/Registration.vue')
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('../views/student/Teams.vue')
      },
      {
        path: 'teams/:id',
        name: 'TeamDetail',
        component: () => import('../views/student/TeamDetail.vue')
      },
      {
        path: 'works',
        name: 'Works',
        component: () => import('../views/student/Works.vue')
      },
      {
        path: 'results',
        name: 'Results',
        component: () => import('../views/student/Results.vue')
      },
      {
        path: 'invitations',
        name: 'Invitations',
        component: () => import('../views/student/Invitations.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/student/Profile.vue')
      }
    ]
  },
  // Judge routes with layout
  {
    path: '/judge',
    component: () => import('../layouts/JudgeLayout.vue'),
    meta: { requiresAuth: true, role: 'JUDGE' },
    children: [
      {
        path: '',
        name: 'JudgeHome',
        component: () => import('../views/judge/Home.vue')
      },
      {
        path: 'pending',
        name: 'JudgePending',
        component: () => import('../views/judge/Pending.vue')
      },
      {
        path: 'reviewed',
        name: 'JudgeReviewed',
        component: () => import('../views/judge/Reviewed.vue')
      },
      {
        path: 'stats',
        name: 'JudgeStats',
        component: () => import('../views/judge/Stats.vue')
      },
      {
        path: 'profile',
        name: 'JudgeProfile',
        component: () => import('../views/judge/Profile.vue')
      }
    ]
  },

  // Admin routes with layout
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('../views/admin/Home.vue')
      },
      {
        path: 'competitions',
        name: 'AdminCompetitions',
        component: () => import('../views/admin/Competitions.vue')
      },
      {
        path: 'judges',
        name: 'AdminJudges',
        component: () => import('../views/admin/Judges.vue')
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: () => import('../views/admin/Students.vue')
      },
      {
        path: 'works',
        name: 'AdminWorks',
        component: () => import('../views/admin/Works.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('../views/admin/Reviews.vue')
      },
      {
        path: 'stats',
        name: 'AdminStats',
        component: () => import('../views/admin/Stats.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== userRole) {
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router