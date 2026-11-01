<template>
  <div>
    <el-card>
      <template #header>
        <div class="header-row">
          <span>作品展示</span>
          <div class="header-actions">
            <el-button v-if="isPhotographer" type="primary" @click="openCreate">上传作品</el-button>
            <el-button v-else type="primary" plain @click="openApply">申请成为摄影师</el-button>
          </div>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="query.keyword" placeholder="搜索作品标题" clearable style="width: 220px" @keyup.enter="load" />
        <el-input v-model="query.styleTag" placeholder="风格标签" clearable style="width: 180px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <div v-if="list.length" class="work-grid">
        <el-card v-for="item in list" :key="item.id" class="work-item" shadow="hover" :body-style="{ padding: '0' }">
          <div class="work-cover" :style="coverStyle(item)" @click="goDetail(item.id)">
            <span v-if="!item.cover" class="cover-placeholder">无封面</span>
          </div>
          <div class="work-body">
            <div class="work-title link" @click="goDetail(item.id)">{{ item.title }}</div>
            <div class="work-photographer">by {{ item.photographerName || '匿名' }}</div>
            <div class="tags">
              <el-tag v-for="tag in (item.styleTags || '').split(',').filter(Boolean)" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
            <div class="work-stats">
              <span>👁 {{ item.viewCount || 0 }}</span>
              <span>❤️ {{ item.likeCount || 0 }}</span>
              <el-button size="small" type="primary" text @click="handleLike(item)">点赞</el-button>
              <span class="work-actions" v-if="item.photographerUserId === myUserId">
                <el-button size="small" type="primary" text @click="openEdit(item)">编辑</el-button>
                <el-button size="small" type="danger" text @click="handleDelete(item)">删除</el-button>
              </span>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无作品" />

      <el-pagination
        v-if="total > 0"
        class="pagination"
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.size"
        :current-page="query.current"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 上传作品对话框 -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑作品' : '上传作品'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="作品标题" />
        </el-form-item>
        <el-form-item label="风格标签">
          <el-input v-model="form.styleTags" placeholder="逗号分隔，如：日系,校园" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <div v-if="form.cover" class="cover-preview" :style="{ backgroundImage: `url(${form.cover})` }">
              <span class="cover-hint">点击更换封面</span>
            </div>
            <el-button v-else>选择封面图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="作品图片">
          <el-upload
            :file-list="imageFileList"
            list-type="picture-card"
            :before-upload="beforeImageUpload"
            accept="image/*"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="作品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="作品介绍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">上传</el-button>
      </template>
    </el-dialog>

    <!-- 摄影师认证对话框 -->
    <el-dialog v-model="showApply" title="申请成为摄影师" width="520px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="个人简介" prop="intro">
          <el-input v-model="applyForm.intro" type="textarea" :rows="3" placeholder="介绍你的拍摄经历和擅长方向" />
        </el-form-item>
        <el-form-item label="风格标签" prop="styleTags">
          <el-input v-model="applyForm.styleTags" placeholder="逗号分隔，如：日系,校园,清新" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApply = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkList, createWork, updateWork, deleteWork, likeWork, applyPhotographer, getMyPhotographerStatus, uploadFile } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const myUserId = userStore.userInfo?.userId
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 12, keyword: '', styleTag: '' })
const showDialog = ref(false)
const showApply = ref(false)
const submitting = ref(false)
const applying = ref(false)
const formRef = ref()
const editingId = ref(null)
const applyFormRef = ref()
const myStatus = ref(null)

const form = reactive({ title: '', styleTags: '', cover: '', description: '' })
const applyForm = reactive({ intro: '', styleTags: '' })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }
const applyRules = {
  intro: [{ required: true, message: '请输入简介', trigger: 'blur' }],
  styleTags: [{ required: true, message: '请输入风格标签', trigger: 'blur' }]
}
// 多图上传：存 URL 列表
const imageUrls = ref([])
const imageFileList = ref([])

const isPhotographer = computed(() =>
  userStore.role === 'PHOTOGRAPHER' || (myStatus.value && myStatus.value.auditStatus === 'APPROVED')
)

const coverStyle = (item) => item.cover
  ? { backgroundImage: `url(${item.cover})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  : {}

// 封面上传：上传成功后将返回的 URL 存入 form.cover
const beforeCoverUpload = async (file) => {
  const res = await uploadFile(file)
  form.cover = res.data
  ElMessage.success('封面上传成功')
  return false // 阻止 el-upload 自动上传
}

// 多图上传
const beforeImageUpload = async (file) => {
  const res = await uploadFile(file)
  imageUrls.value.push(res.data)
  imageFileList.value.push({ name: file.name, url: res.data })
  return false
}

const handleImageRemove = (file) => {
  const idx = imageFileList.value.findIndex((f) => f.url === file.url)
  if (idx > -1) {
    imageFileList.value.splice(idx, 1)
    imageUrls.value.splice(idx, 1)
  }
}

const load = async () => {
  const res = await getWorkList(query)
  list.value = res.data.records || []
  total.value = res.data.total || 0
}

const loadMyStatus = async () => {
  try {
    const res = await getMyPhotographerStatus()
    myStatus.value = res.data
  } catch (e) { /* 未认证忽略 */ }
}

const handlePageChange = (page) => {
  query.current = page
  load()
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, { title: '', styleTags: '', cover: '', description: '' })
  imageUrls.value = []
  imageFileList.value = []
  showDialog.value = true
}

const openEdit = (item) => {
  editingId.value = item.id
  Object.assign(form, {
    title: item.title,
    styleTags: item.styleTags,
    cover: item.cover,
    description: item.description
  })
  imageUrls.value = item.images ? [...item.images] : []
  imageFileList.value = imageUrls.value.map((url, i) => ({ name: '图片' + (i + 1), url }))
  showDialog.value = true
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form, images: imageUrls.value }
    if (editingId.value) {
      await updateWork(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createWork(payload)
      ElMessage.success('上传成功')
    }
    showDialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (item) => {
  await ElMessageBox.confirm('确认删除该作品？此操作不可恢复', '警告', { type: 'warning' })
  await deleteWork(item.id)
  ElMessage.success('已删除')
  load()
}

const handleLike = async (item) => {
  await likeWork(item.id)
  ElMessage.success('点赞成功')
  load()
}

const goDetail = (id) => {
  router.push(`/work/${id}`)
}

const openApply = () => {
  Object.assign(applyForm, { intro: '', styleTags: '' })
  showApply.value = true
}

const submitApply = async () => {
  await applyFormRef.value.validate()
  applying.value = true
  try {
    await applyPhotographer(applyForm)
    ElMessage.success('申请已提交，等待管理员审核')
    showApply.value = false
    loadMyStatus()
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  load()
  loadMyStatus()
})
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; }
.filter-row { display: flex; gap: 12px; margin-bottom: 16px; }
.work-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.work-cover { height: 160px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; color: #999; cursor: pointer; }
.cover-placeholder { font-size: 14px; }
.work-body { padding: 12px; }
.work-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.work-title.link { cursor: pointer; }
.work-title.link:hover { color: #409eff; }
.work-photographer { font-size: 12px; color: #999; margin-bottom: 8px; }
.tags { display: flex; gap: 6px; margin-bottom: 8px; flex-wrap: wrap; }
.work-stats { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #666; }
.work-actions { display: flex; gap: 2px; margin-left: auto; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.cover-preview { width: 160px; height: 100px; background-size: cover; background-position: center; border-radius: 4px; display: flex; align-items: flex-end; justify-content: center; }
.cover-hint { background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; padding: 2px 8px; border-radius: 0 0 4px 4px; width: 100%; text-align: center; }
</style>
