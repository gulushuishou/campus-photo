<template>
  <div>
    <!-- 概览统计 -->
    <el-row :gutter="16">
      <el-col :span="4" v-for="stat in statCards" :key="stat.label">
        <el-card>
          <div class="stat-title">{{ stat.label }}</div>
          <div class="stat-num">{{ stat.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header><span>热门风格标签统计</span></template>
          <div ref="tagChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>订单状态分布</span></template>
          <div ref="orderChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 摄影师认证审核 -->
    <el-card class="section-card">
      <template #header><span>摄影师认证审核</span></template>
      <el-table :data="applications" v-loading="appLoading">
        <el-table-column label="昵称" width="140">
          <template #default="{ row }">{{ row.nickname }}</template>
        </el-table-column>
        <el-table-column prop="intro" label="简介" />
        <el-table-column prop="styleTags" label="风格标签" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)" size="small">{{ auditText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 'PENDING'" size="small" type="success" @click="handleAudit(row, 'APPROVED')">通过</el-button>
            <el-button v-if="row.auditStatus === 'PENDING'" size="small" type="danger" @click="handleAudit(row, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 用户管理 -->
    <el-card class="section-card">
      <template #header><span>用户管理</span></template>
      <el-table :data="users" v-loading="userLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'PHOTOGRAPHER' ? 'success' : 'info'" size="small">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '正常' : '封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.role !== 'ADMIN'" size="small" :type="row.status === 0 ? 'danger' : 'success'" @click="handleUserStatus(row)">
              {{ row.status === 0 ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 需求管理 -->
    <el-card class="section-card">
      <template #header><span>需求管理</span></template>
      <el-table :data="demands" v-loading="demandLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="styleTags" label="风格标签" width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="demandType(row.status)" size="small">{{ demandText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'RECRUITING'" size="small" type="danger" @click="handleDemandStatus(row, 'CLOSED')">下架</el-button>
            <el-button v-else size="small" type="success" @click="handleDemandStatus(row, 'RECRUITING')">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订单管理 -->
    <el-card class="section-card">
      <template #header><span>订单管理</span></template>
      <el-table :data="orders" v-loading="orderLoading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="orderType(row.status)" size="small">{{ orderText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">{{ row.price || '面议' }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="下单用户ID" width="110" />
        <el-table-column prop="photographerId" label="摄影师ID" width="110" />
        <el-table-column label="拍摄时间" width="160">
          <template #default="{ row }">{{ row.shootTime ? row.shootTime.replace('T', ' ').substring(0, 16) : '待定' }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" />
      </el-table>
    </el-card>

    <!-- 投诉管理 -->
    <el-card class="section-card">
      <template #header><span>投诉管理</span></template>
      <el-table :data="complaints" v-loading="complaintLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="userId" label="投诉人ID" width="90" />
        <el-table-column prop="content" label="投诉内容" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'" size="small">{{ row.status === 'PENDING' ? '待处理' : '已处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="handleResolve(row)">处理</el-button>
            <span v-else class="muted">{{ row.result || '已处理' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getOverview, getTagStats, getOrderStatusStats,
  getApplications, auditPhotographer, getAdminUsers, changeUserStatus,
  getAdminDemands, changeDemandStatus, getAdminOrders, getComplaints, resolveComplaint
} from '@/api'

const overview = ref(null)
const applications = ref([])
const users = ref([])
const demands = ref([])
const orders = ref([])
const complaints = ref([])
const appLoading = ref(false)
const userLoading = ref(false)
const demandLoading = ref(false)
const orderLoading = ref(false)
const complaintLoading = ref(false)
const tagChartRef = ref()
const orderChartRef = ref()

const statCards = computed(() => [
  { label: '用户总数', value: overview.value?.userCount ?? '-' },
  { label: '摄影师', value: overview.value?.photographerCount ?? '-' },
  { label: '作品总数', value: overview.value?.workCount ?? '-' },
  { label: '需求总数', value: overview.value?.demandCount ?? '-' },
  { label: '订单总数', value: overview.value?.orderCount ?? '-' },
  { label: '评价总数', value: overview.value?.evaluationCount ?? '-' }
])

const auditType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')
const auditText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)
const roleText = (r) => ({ USER: '普通用户', PHOTOGRAPHER: '摄影师', ADMIN: '管理员' }[r] || r)
const demandType = (s) => ({ RECRUITING: 'success', DONE: 'info', CLOSED: 'danger' }[s] || 'info')
const demandText = (s) => ({ RECRUITING: '招募中', DONE: '已完成', CLOSED: '已关闭' }[s] || s)
const orderType = (s) => ({ PENDING: 'warning', CONFIRMED: 'success', FINISHED: 'primary', EVALUATED: 'info', CANCELLED: 'danger' }[s] || 'info')
const orderText = (s) => ({ PENDING: '待确认', CONFIRMED: '已确认', FINISHED: '拍摄完成', EVALUATED: '已评价', CANCELLED: '已取消' }[s] || s)

const loadOverview = async () => {
  const res = await getOverview()
  overview.value = res.data
}

const loadApplications = async () => {
  appLoading.value = true
  try {
    const res = await getApplications()
    applications.value = res.data || []
  } finally {
    appLoading.value = false
  }
}

const loadUsers = async () => {
  userLoading.value = true
  try {
    const res = await getAdminUsers()
    users.value = res.data || []
  } finally {
    userLoading.value = false
  }
}

const loadDemands = async () => {
  demandLoading.value = true
  try {
    const res = await getAdminDemands()
    demands.value = res.data || []
  } finally {
    demandLoading.value = false
  }
}

const handleDemandStatus = async (row, status) => {
  await changeDemandStatus(row.id, status)
  ElMessage.success(status === 'CLOSED' ? '已下架' : '已恢复')
  loadDemands()
}

const loadCharts = async () => {
  await nextTick()
  // 标签统计图
  const tagRes = await getTagStats()
  const tagData = tagRes.data || []
  if (tagChartRef.value) {
    const chart = echarts.init(tagChartRef.value)
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: tagData.map(t => t.tag) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: tagData.map(t => t.count), itemStyle: { color: '#409eff' } }]
    })
  }
  // 订单状态图
  const orderRes = await getOrderStatusStats()
  const orderData = orderRes.data || []
  if (orderChartRef.value) {
    const chart = echarts.init(orderChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: orderData.map(o => ({ name: o.status, value: o.count }))
      }]
    })
  }
}

const handleAudit = async (row, status) => {
  await auditPhotographer(row.id, status)
  ElMessage.success(status === 'APPROVED' ? '已通过' : '已拒绝')
  loadApplications()
  loadOverview()
}

const handleUserStatus = async (row) => {
  const newStatus = row.status === 0 ? 1 : 0
  await changeUserStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已封禁' : '已解封')
  loadUsers()
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const res = await getAdminOrders()
    orders.value = res.data || []
  } finally {
    orderLoading.value = false
  }
}

const loadComplaints = async () => {
  complaintLoading.value = true
  try {
    const res = await getComplaints()
    complaints.value = res.data || []
  } finally {
    complaintLoading.value = false
  }
}

const handleResolve = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入处理结果', '投诉仲裁', {
    confirmButtonText: '确定', cancelButtonText: '取消'
  })
  await resolveComplaint(row.id, value)
  ElMessage.success('已处理')
  loadComplaints()
}

onMounted(() => {
  loadOverview()
  loadApplications()
  loadUsers()
  loadDemands()
  loadOrders()
  loadComplaints()
  loadCharts()
})
</script>

<style scoped>
.stat-title { color: #999; font-size: 14px; }
.stat-num { font-size: 28px; font-weight: 500; margin-top: 8px; }
.chart-row { margin-top: 16px; }
.section-card { margin-top: 16px; }
</style>
