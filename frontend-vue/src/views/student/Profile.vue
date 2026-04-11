<template>
  <div class="profile-page">
    <!-- Hero -->
    <section class="section-dark">
      <div class="container">
        <h1 class="hero-title">个人设置</h1>
        <h2 class="subheading">管理个人信息</h2>
      </div>
    </section>

    <!-- Profile Form -->
    <section class="section-light">
      <div class="container">
        <div class="profile-card card">
          <form @submit.prevent="handleUpdate" class="profile-form">
            <!-- Username (readonly) -->
            <div class="form-group mb-md">
              <label class="form-label caption">用户名</label>
              <input v-model="profile.username" type="text" class="input" readonly />
            </div>

            <!-- Student No (readonly) -->
            <div class="form-group mb-md">
              <label class="form-label caption">学号</label>
              <input v-model="profile.studentNo" type="text" class="input" readonly />
            </div>

            <!-- Real Name -->
            <div class="form-group mb-md">
              <label class="form-label caption">真实姓名</label>
              <input v-model="profile.realName" type="text" class="input" required />
            </div>

            <!-- Email -->
            <div class="form-group mb-md">
              <label class="form-label caption">邮箱</label>
              <input v-model="profile.email" type="email" class="input" required />
            </div>

            <!-- Phone -->
            <div class="form-group mb-md">
              <label class="form-label caption">手机号</label>
              <input v-model="profile.phone" type="text" class="input" />
            </div>

            <!-- College -->
            <div class="form-group mb-md">
              <label class="form-label caption">学院</label>
              <input v-model="profile.college" type="text" class="input" />
            </div>

            <!-- Major -->
            <div class="form-group mb-lg">
              <label class="form-label caption">专业</label>
              <input v-model="profile.major" type="text" class="input" />
            </div>

            <!-- Submit -->
            <button type="submit" class="btn-primary" style="width: 100%;" :disabled="loading">
              {{ loading ? '保存中...' : '保存修改' }}
            </button>
          </form>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const profile = ref({
  username: '',
  studentNo: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  major: ''
})

const loading = ref(false)

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
  }

  // 从localStorage加载用户信息
  profile.value.username = localStorage.getItem('userName') || ''
  profile.value.studentNo = localStorage.getItem('studentNo') || ''
  profile.value.realName = localStorage.getItem('userName') || ''
  profile.value.email = localStorage.getItem('email') || ''

  // TODO: 从后端获取完整用户信息
  // await fetchProfile()
})

const handleUpdate = async () => {
  loading.value = true

  try {
    // TODO: 实现更新用户信息API
    ElMessage.success('个人信息更新成功（功能开发中）')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const fetchProfile = async () => {
  try {
    const response = await fetch('/api/user/profile')
    const data = await response.json()
    if (data.code === 200) {
      profile.value = data.data
    }
  } catch (error) {
    console.error('获取个人信息失败', error)
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}

.profile-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  color: var(--color-near-black);
}
</style>