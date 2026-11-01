<template>
  <div v-loading="loading">
    <el-card v-if="demand">
      <template #header>
        <div class="detail-header">
          <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
        </div>
      </template>

      <div class="detail-title-row">
        <h2 class="title">{{ demand.title }}</h2>
        <el-tag :type="statusType(demand.status)" size="small">{{ statusText(demand.status) }}</el-tag>
      </div>

      <div class="publisher-info">
        <el-avatar :size="48">{{ demand.nickname?.[0] || 'U' }}</el-avatar>
        <div class="publisher-text">
          <div class="name">{{ demand.nickname || '匿名用户' }}</div>
          <div class="meta">
            <span>{{ demand.campus || '校区未设置' }}</span>
            <span>信用分 {{ demand.creditScore }}</span>
            <span>{{ formatTime(demand.createTime) }} 发布</span>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="detail-content">
        <h3>需求详情</h3>
        <p class="content-text">{{ demand.content || '暂无详细说明' }}</p>
      </div>

      <div class="detail-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="风格标签">
            <el-tag v-for="tag in (demand.styleTags || '').split(',').filter(Boolean)" :key="tag" size="small" type="info" class="tag">{{ tag }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="拍摄时间">{{ demand.shootTime ? formatTime(demand.shootTime) : '待定' }}</el-descriptions-item>
          <el-descriptions-item label="拍摄地点">{{ demand.location || '待定' }}</el-descriptions-item>
          <el-descriptions-item label="预算">{{ demand.budget ? '¥' + demand.budget : '面议' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="detail-actions" v-if="canOperate">
        <el-button type="primary" @click="showOrder = true">发起预约</el-button>
        <el-button v-if="isOwner" type="warning" plain @click="handleClose">关闭需求</el-button>
      </div>
    </el-card>

    <!-- 发起预约对话框 -->
    <el-dialog v-model="showOrder" title="发起预约" width="460px">
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="价格(元)">
          <el-input-number v-model="orderForm.price" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="拍摄时间">
          <el-date-picker v-model="orderForm.shootTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrder = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDemandDetail, closeDemand, createOrder } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const demand = ref(null)
const showOrder = ref(false)
const orderForm = reactive({ price: 0, shootTime: null, remark: '' })

const isOwner = computed(() => demand.value && demand.value.userId === userStore.userInfo?.userId)
const canOperate = computed(() => demand.value && demand.value.status === 'RECRUITING')

const statusType = (s) => ({ RECRUITING: 'success', DONE: 'info', CLOSED: 'info' }[s] || 'info')
const statusText = (s) => ({ RECRUITING: '招募中', DONE: '已完成', CLOSED: '已关闭' }[s] || s)
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const load = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    demand.value = res.data
  } finally {
    loading.value = false
  }
}

const handleClose = async () => {
  await ElMessageBox.confirm('确认关闭该需求？', '提示')
  await closeDemand(demand.value.id)
  ElMessage.success('需求已关闭')
  load()
}

const submitOrder = async () => {
  await createOrder({
    demandId: demand.value.id,
    photographerId: userStore.userInfo?.userId, // 当前用户作为摄影师接单
    price: orderForm.price,
    shootTime: orderForm.shootTime ? new Date(orderForm.shootTime).toISOString().slice(0, 19) : null,
    location: demand.value.location,
    remark: orderForm.remark
  })
  ElMessage.success('预约已提交')
  showOrder.value = false
  router.push('/order')
}

onMounted(load)
</script>

<style scoped>
.detail-header { display: flex; align-items: center; }
.detail-title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.title { font-size: 22px; font-weight: 500; }
.publisher-info { display: flex; align-items: center; gap: 12px; }
.publisher-text .name { font-size: 15px; font-weight: 500; }
.publisher-text .meta { display: flex; gap: 12px; font-size: 13px; color: #999; margin-top: 4px; }
.detail-content h3 { font-size: 15px; margin-bottom: 12px; }
.content-text { color: #555; line-height: 1.8; font-size: 14px; margin-bottom: 20px; }
.detail-info { margin-bottom: 20px; }
.tag { margin-right: 6px; }
.detail-actions { display: flex; gap: 12px; }
</style>
