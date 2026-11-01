<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">校园约拍平台</h2>
      <p class="subtitle">发现校园里的美好瞬间</p>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
        <div class="register-tip">
          还没有账号？<el-link type="primary" @click="showRegister = true">立即注册</el-link>
        </div>
      </el-form>
    </el-card>

    <el-dialog v-model="showRegister" title="注册账号" width="400px">
      <el-form :model="regForm" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="regForm.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="regForm.password" type="password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="regForm.nickname" /></el-form-item>
        <el-form-item label="校区"><el-input v-model="regForm.campus" placeholder="如：本部校区" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const showRegister = ref(false)

const form = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', campus: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  await register(regForm)
  ElMessage.success('注册成功，请登录')
  showRegister.value = false
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}
.subtitle {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-bottom: 24px;
}
.login-btn {
  width: 100%;
}
.register-tip {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #999;
}
</style>
