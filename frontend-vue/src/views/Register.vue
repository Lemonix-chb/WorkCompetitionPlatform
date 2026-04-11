<template>
  <div class="register-page">
    <!-- Register Section (浅色背景) -->
    <section class="section-light register-section">
      <div class="register-container">
        <h1 class="hero-title" style="color: var(--color-near-black);">注册账号</h1>

        <div class="register-card card">
          <form @submit.prevent="handleRegister" class="register-form">
            <!-- Username -->
            <div class="form-group mb-md">
              <label class="form-label caption">用户名 *</label>
              <input v-model="form.username" type="text" class="input" required />
            </div>

            <!-- Password -->
            <div class="form-group mb-md">
              <label class="form-label caption">密码 *</label>
              <input v-model="form.password" type="password" class="input" required />
            </div>

            <!-- Real Name -->
            <div class="form-group mb-md">
              <label class="form-label caption">真实姓名 *</label>
              <input v-model="form.realName" type="text" class="input" required />
            </div>

            <!-- Email -->
            <div class="form-group mb-md">
              <label class="form-label caption">邮箱 *</label>
              <input v-model="form.email" type="email" class="input" required />
            </div>

            <!-- Phone -->
            <div class="form-group mb-md">
              <label class="form-label caption">手机号</label>
              <input v-model="form.phone" type="text" class="input" />
            </div>

            <!-- College -->
            <div class="form-group mb-md">
              <label class="form-label caption">学院</label>
              <input v-model="form.college" type="text" class="input" />
            </div>

            <!-- Major -->
            <div class="form-group mb-md">
              <label class="form-label caption">专业</label>
              <input v-model="form.major" type="text" class="input" />
            </div>

            <!-- Student No / Teacher No -->
            <div class="form-group mb-md">
              <label class="form-label caption">
                {{ form.role === 'STUDENT' ? '学号' : '工号' }} *
              </label>
              <input
                v-model="form.studentNo"
                type="text"
                class="input"
                :placeholder="form.role === 'STUDENT' ? '请输入学号' : '请输入工号'"
                required
              />
            </div>

            <!-- Role Selection -->
            <div class="form-group mb-lg">
              <label class="form-label caption">用户类型</label>
              <select v-model="form.role" class="input" required>
                <option value="STUDENT">学生</option>
                <option value="JUDGE">教师/评委</option>
              </select>
            </div>

            <!-- Submit -->
            <button type="submit" class="btn-primary" style="width: 100%;" :disabled="loading">
              {{ loading ? '注册中...' : '提交注册' }}
            </button>
          </form>

          <!-- Login Link -->
          <div class="register-footer text-center mt-lg">
            <router-link to="/login" class="link link-learn-more">
              已有账号？立即登录
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = ref({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  college: '',
  major: '',
  studentNo: '',
  role: 'STUDENT'
})

const loading = ref(false)

const handleRegister = async () => {
  loading.value = true

  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })

    const data = await response.json()

    if (data.code === 200) {
      ElMessage.success('注册成功，请等待管理员审核')
      router.push('/login')
    } else {
      ElMessage.error(data.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
}

.register-section {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.register-container {
  width: 100%;
  max-width: 480px;
  padding: 0 24px;
}

.register-card {
  margin-top: 40px;
}

.register-form {
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