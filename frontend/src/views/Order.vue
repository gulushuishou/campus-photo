<template>
  <div>
    <el-card>
      <template #header>
        <div class="header-row">
          <span>我的订单</span>
          <el-button type="primary" @click="openCreate">创建订单</el-button>
        </div>
      </template>

      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元)" width="100">
          <template #default="{ row }">{{ row.price || '面议' }}</template>
        </el-table-column>
        <el-table-column label="拍摄时间" width="160">
          <template #default="{ row }">{{ row.shootTime ? formatTime(row.shootTime) : '待定' }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING' && isPhotographer(row)" size="small" type="success" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status === 'CONFIRMED' && isPhotographer(row)" size="small" type="primary" @click="handleFinish(row)">完成</el-button>
            <el-button v-if="['PENDING', 'CONFIRMED'].includes(row.status)" size="small" type="danger" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status === 'FINISHED'" size="small" type="warning" @click="openEvaluate(row)">评价</el-button>
            <el-button v-if="['CONFIRMED', 'FINISHED'].includes(row.status)" size="small" type="info" text @click="openComplaint(row)">投诉</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!list.length && !loading" description="暂无订单" />
    </el-card>

    <!-- 创建订单对话框 -->
    <el-dialog v-model="showCreate" title="创建预约订单" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="摄影师ID" prop="photographerId">
          <el-input v-model.number="form.photographerId" placeholder="输入摄影师用户ID" />
        </el-form-item>
        <el-form-item label="价格(元)">
          <el-input-number v-model="form.price" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="拍摄时间">
          <el-date-picker v-model="form.shootTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="拍摄地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">创建</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="showEvaluate" title="评价订单" width="460px">
      <div class="eval-rate">
        <span>评分：</span>
        <el-rate v-model="evalForm.score" />
      </div>
      <el-input v-model="evalForm.content" type="textarea" :rows="3" placeholder="写下你的评价" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="showEvaluate = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluate">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 投诉对话框 -->
    <el-dialog v-model="showComplaint" title="投诉订单" width="460px">
      <el-input v-model="complaintContent" type="textarea" :rows="4" placeholder="请描述投诉原因" />
      <template #footer>
        <el-button @click="showComplaint = false">取消</el-button>
        <el-button type="danger" @click="submitComplaintAction">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, createOrder, confirmOrder, finishOrder, cancelOrder, evaluate, submitComplaint } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)
const showCreate = ref(false)
const showEvaluate = ref(false)
const showComplaint = ref(false)
const complaintContent = ref('')
const complaintOrderId = ref(null)
const submitting = ref(false)
const formRef = ref()
const evalOrderId = ref(null)

const form = reactive({ photographerId: null, price: 0, shootTime: null, location: '', remark: '' })
const evalForm = reactive({ score: 5, content: '' })
const rules = { photographerId: [{ required: true, message: '请输入摄影师ID', trigger: 'blur' }] }

const statusType = (s) => ({ PENDING: 'warning', CONFIRMED: 'success', FINISHED: 'primary', EVALUATED: 'info', CANCELLED: 'danger' }[s] || 'info')
const statusText = (s) => ({ PENDING: '待确认', CONFIRMED: '已确认', FINISHED: '拍摄完成', EVALUATED: '已评价', CANCELLED: '已取消' }[s] || s)
const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

// 判断当前用户是否是订单里的摄影师
const isPhotographer = (row) => row.photographerId === userStore.userInfo?.userId

const load = async () => {
  loading.value = true
  try {
    const res = await getMyOrders()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { photographerId: null, price: 0, shootTime: null, location: '', remark: '' })
  showCreate.value = true
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createOrder({
      ...form,
      shootTime: form.shootTime ? new Date(form.shootTime).toISOString().slice(0, 19) : null
    })
    ElMessage.success('订单创建成功')
    showCreate.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const handleConfirm = async (row) => {
  await ElMessageBox.confirm('确认接受该订单？', '提示')
  await confirmOrder(row.id)
  ElMessage.success('已确认')
  load()
}

const handleFinish = async (row) => {
  await ElMessageBox.confirm('确认拍摄已完成？', '提示')
  await finishOrder(row.id)
  ElMessage.success('已标记完成')
  load()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该订单？', '提示')
  await cancelOrder(row.id)
  ElMessage.success('已取消')
  load()
}

const openEvaluate = (row) => {
  evalOrderId.value = row.id
  Object.assign(evalForm, { score: 5, content: '' })
  showEvaluate.value = true
}

const submitEvaluate = async () => {
  await evaluate(evalOrderId.value, evalForm)
  ElMessage.success('评价成功')
  showEvaluate.value = false
  load()
}

const openComplaint = (row) => {
  complaintOrderId.value = row.id
  complaintContent.value = ''
  showComplaint.value = true
}

const submitComplaintAction = async () => {
  if (!complaintContent.value.trim()) {
    ElMessage.warning('请填写投诉内容')
    return
  }
  await submitComplaint({ orderId: complaintOrderId.value, content: complaintContent.value.trim() })
  ElMessage.success('投诉已提交，管理员会尽快处理')
  showComplaint.value = false
}

onMounted(load)
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.eval-rate { display: flex; align-items: center; }
</style>
