<template>
  <div>
    <el-card>
      <template #header>
        <div class="header-row">
          <span>约拍需求</span>
          <el-button type="primary" @click="openCreate">发布需求</el-button>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="query.keyword" placeholder="搜索需求标题" clearable style="width: 200px" @keyup.enter="load" />
        <el-input v-model="query.styleTag" placeholder="风格标签，如：日系" clearable style="width: 160px" @keyup.enter="load" />
        <el-input v-model="query.location" placeholder="校区/地点，如：本部" clearable style="width: 160px" @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
          <el-option label="招募中" value="RECRUITING" />
          <el-option label="已完成" value="DONE" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <div v-if="list.length" class="demand-list">
        <el-card v-for="item in list" :key="item.id" class="demand-item" shadow="hover">
          <div class="demand-top">
            <div class="demand-title-row">
              <span class="title link" @click="goDetail(item.id)">{{ item.title }}</span>
              <el-tag :type="statusType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
            </div>
            <div class="publisher">
              <span>{{ item.nickname || '匿名' }}</span>
              <span class="muted">{{ item.campus || '' }}</span>
              <span class="muted">信用 {{ item.creditScore }}</span>
            </div>
          </div>
          <div class="demand-content">{{ item.content || '暂无详细说明' }}</div>
          <div class="tags">
            <el-tag v-for="tag in (item.styleTags || '').split(',').filter(Boolean)" :key="tag" size="small" type="info">{{ tag }}</el-tag>
          </div>
          <div class="demand-meta">
            <span>时间：{{ item.shootTime ? formatTime(item.shootTime) : '待定' }}</span>
            <span>地点：{{ item.location || '待定' }}</span>
            <span>预算：{{ item.budget ? '¥' + item.budget : '面议' }}</span>
            <span class="muted">{{ formatTime(item.createTime) }} 发布</span>
          </div>
          <div class="demand-actions" v-if="item.userId === myUserId">
            <el-button size="small" type="primary" text @click.stop="openEdit(item)">编辑</el-button>
            <el-button size="small" type="danger" text @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无需求，快来发布第一个吧" />

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

    <el-dialog v-model="showDialog" :title="editingId ? '编辑约拍需求' : '发布约拍需求'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="如：想找摄影师拍毕业写真" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="描述你的拍摄需求" />
        </el-form-item>
        <el-form-item label="风格标签">
          <el-input v-model="form.styleTags" placeholder="逗号分隔，如：日系,校园,清新" />
        </el-form-item>
        <el-form-item label="拍摄时间">
          <el-date-picker v-model="form.shootTime" type="datetime" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="拍摄地点">
          <el-input v-model="form.location" placeholder="如：校园图书馆前" />
        </el-form-item>
        <el-form-item label="预算(元)">
          <el-input-number v-model="form.budget" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDemandList, createDemand, updateDemand, deleteDemand } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const myUserId = userStore.userInfo?.userId
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '', styleTag: '', location: '', status: '' })
const showDialog = ref(false)
const submitting = ref(false)
const formRef = ref()
const editingId = ref(null)

const form = reactive({ title: '', content: '', styleTags: '', shootTime: null, location: '', budget: 0 })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const statusType = (s) => ({ RECRUITING: 'success', DONE: 'info', CLOSED: 'info' }[s] || 'info')
const statusText = (s) => ({ RECRUITING: '招募中', DONE: '已完成', CLOSED: '已关闭' }[s] || s)
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const load = async () => {
  const res = await getDemandList(query)
  list.value = res.data.records || []
  total.value = res.data.total || 0
}

const handlePageChange = (page) => {
  query.current = page
  load()
}

const goDetail = (id) => {
  router.push(`/demand/${id}`)
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, { title: '', content: '', styleTags: '', shootTime: null, location: '', budget: 0 })
  showDialog.value = true
}

const openEdit = (item) => {
  editingId.value = item.id
  Object.assign(form, {
    title: item.title,
    content: item.content,
    styleTags: item.styleTags,
    shootTime: item.shootTime ? item.shootTime : null,
    location: item.location,
    budget: item.budget
  })
  showDialog.value = true
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      ...form,
      shootTime: form.shootTime ? new Date(form.shootTime).toISOString().slice(0, 19) : null
    }
    if (editingId.value) {
      await updateDemand(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createDemand(payload)
      ElMessage.success('发布成功')
    }
    showDialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (item) => {
  await ElMessageBox.confirm('确认删除该需求？此操作不可恢复', '警告', { type: 'warning' })
  await deleteDemand(item.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.filter-row { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.demand-list { display: flex; flex-direction: column; gap: 12px; }
.demand-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.demand-title-row { display: flex; align-items: center; gap: 8px; }
.title { font-size: 16px; font-weight: 500; }
.title.link { cursor: pointer; color: #333; }
.title.link:hover { color: #409eff; }
.publisher { display: flex; gap: 8px; font-size: 13px; color: #666; }
.muted { color: #999; }
.demand-content { color: #666; font-size: 14px; margin-bottom: 8px; }
.tags { display: flex; gap: 6px; margin-bottom: 8px; }
.demand-meta { display: flex; gap: 16px; font-size: 13px; color: #666; flex-wrap: wrap; }
.demand-actions { display: flex; gap: 4px; margin-top: 8px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
