<template>
  <div>
    <el-card class="profile-card">
      <template #header><span>个人中心</span></template>
      <div class="profile-header">
        <el-upload
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          accept="image/*"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="80" :src="profile.avatar || undefined">{{ profile.nickname?.[0] || 'U' }}</el-avatar>
            <div class="avatar-edit">更换头像</div>
          </div>
        </el-upload>
        <div class="profile-info">
          <div class="profile-name">
            <span class="name">{{ profile.nickname }}</span>
            <el-tag :type="roleType(profile.role)" size="small">{{ roleText(profile.role) }}</el-tag>
          </div>
          <div class="profile-meta">
            <span>用户名：{{ profile.username }}</span>
            <span>信用分：<b class="credit">{{ profile.creditScore }}</b></span>
            <span>校区：{{ profile.campus || '未设置' }}</span>
            <span>手机：{{ profile.phone || '未设置' }}</span>
          </div>
        </div>
        <el-button type="primary" plain @click="showEdit = true">编辑资料</el-button>
      </div>
    </el-card>

    <!-- 摄影师认证状态 -->
    <el-card class="section-card" v-if="userStore.role !== 'ADMIN'">
      <template #header><span>摄影师认证</span></template>
      <div v-if="photographerStatus">
        <el-alert
          v-if="photographerStatus.auditStatus === 'APPROVED'"
          title="已认证摄影师"
          type="success"
          :description="'风格标签：' + (photographerStatus.styleTags || '')"
          show-icon
        />
        <el-alert
          v-else-if="photographerStatus.auditStatus === 'PENDING'"
          title="认证申请审核中"
          type="warning"
          description="请耐心等待管理员审核"
          show-icon
        />
        <el-alert
          v-else-if="photographerStatus.auditStatus === 'REJECTED'"
          title="认证申请被拒绝"
          type="error"
          description="可重新提交认证申请"
          show-icon
        />
      </div>
      <div v-else class="apply-empty">
        <span>你还没有申请成为摄影师</span>
        <el-button type="primary" size="small" @click="showApply = true">申请认证</el-button>
      </div>
    </el-card>

    <!-- 我的收藏 -->
    <el-card class="section-card">
      <template #header><span>我的收藏（{{ favorites.length }}）</span></template>
      <div v-if="favorites.length" class="favorite-grid">
        <div v-for="item in favorites" :key="item.id" class="favorite-item" @click="goWorkDetail(item.id)">
          <div class="fav-cover" :style="item.cover ? { backgroundImage: `url(${item.cover})` } : {}">
            <span v-if="!item.cover">无封面</span>
          </div>
          <div class="fav-title">{{ item.title }}</div>
        </div>
      </div>
      <el-empty v-else description="还没有收藏任何作品" :image-size="60" />
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEdit" title="编辑资料" width="440px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="校区"><el-input v-model="editForm.campus" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 认证申请对话框 -->
    <el-dialog v-model="showApply" title="申请成为摄影师" width="440px">
      <el-form :model="applyForm" label-width="90px">
        <el-form-item label="个人简介">
          <el-input v-model="applyForm.intro" type="textarea" :rows="3" placeholder="介绍你的拍摄经历" />
        </el-form-item>
        <el-form-item label="风格标签">
          <el-input v-model="applyForm.styleTags" placeholder="逗号分隔，如：日系,校园" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyInfo, updateMyInfo, uploadFile, getMyPhotographerStatus, applyPhotographer, getMyFavorites } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const profile = ref({})
const photographerStatus = ref(null)
const favorites = ref([])
const showEdit = ref(false)
const showApply = ref(false)
const editForm = reactive({ nickname: '', campus: '', phone: '' })
const applyForm = reactive({ intro: '', styleTags: '' })

const roleType = (r) => ({ USER: 'info', PHOTOGRAPHER: 'success', ADMIN: 'danger' }[r] || 'info')
const roleText = (r) => ({ USER: '普通用户', PHOTOGRAPHER: '摄影师', ADMIN: '管理员' }[r] || r)

const loadProfile = async () => {
  const res = await getMyInfo()
  profile.value = res.data
  Object.assign(editForm, {
    nickname: res.data.nickname,
    campus: res.data.campus,
    phone: res.data.phone
  })
}

const loadPhotographerStatus = async () => {
  try {
    const res = await getMyPhotographerStatus()
    photographerStatus.value = res.data
  } catch (e) { /* 未认证 */ }
}

const loadFavorites = async () => {
  try {
    const res = await getMyFavorites()
    favorites.value = res.data || []
  } catch (e) { /* 忽略 */ }
}

const goWorkDetail = (id) => {
  router.push(`/work/${id}`)
}

const beforeAvatarUpload = async (file) => {
  const res = await uploadFile(file)
  await updateMyInfo({ avatar: res.data })
  ElMessage.success('头像更新成功')
  loadProfile()
  // 同步更新导航栏头像
  userStore.updateUserInfo({ avatar: res.data })
  return false
}

const submitEdit = async () => {
  await updateMyInfo(editForm)
  ElMessage.success('资料已更新')
  showEdit.value = false
  loadProfile()
  // 同步更新导航栏昵称
  userStore.updateUserInfo({ nickname: editForm.nickname })
}

const submitApply = async () => {
  if (!applyForm.intro || !applyForm.styleTags) {
    ElMessage.warning('请填写完整')
    return
  }
  await applyPhotographer(applyForm)
  ElMessage.success('申请已提交')
  showApply.value = false
  loadPhotographerStatus()
}

onMounted(() => {
  loadProfile()
  loadPhotographerStatus()
  loadFavorites()
})
</script>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 24px; }
.avatar-wrapper { position: relative; cursor: pointer; }
.avatar-edit { position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%); font-size: 11px; color: #409eff; background: #fff; padding: 1px 6px; border-radius: 8px; border: 1px solid #409eff; }
.profile-info { flex: 1; }
.profile-name { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.name { font-size: 20px; font-weight: 500; }
.profile-meta { display: flex; gap: 16px; color: #666; font-size: 14px; flex-wrap: wrap; }
.credit { color: #e6a23c; }
.section-card { margin-top: 16px; }
.apply-empty { display: flex; align-items: center; justify-content: space-between; color: #666; }
.favorite-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
.favorite-item { cursor: pointer; }
.fav-cover { height: 100px; background: #f0f2f5; background-size: cover; background-position: center; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px; }
.fav-title { font-size: 13px; margin-top: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
