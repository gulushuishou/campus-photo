<template>
  <div v-loading="loading">
    <el-card v-if="work">
      <template #header>
        <div class="detail-header">
          <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
        </div>
      </template>

      <div class="work-detail-layout">
        <!-- 左侧：图片展示 -->
        <div class="image-section">
          <div class="main-image" v-if="work.cover" :style="{ backgroundImage: `url(${work.cover})` }"></div>
          <div class="main-image placeholder" v-else>暂无图片</div>
          <!-- 多图缩略 -->
          <div class="thumb-list" v-if="work.images && work.images.length">
            <img
              v-for="img in work.images"
              :key="img"
              :src="img"
              class="thumb"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            />
          </div>
        </div>

        <!-- 右侧：信息 -->
        <div class="info-section">
          <h2 class="title">{{ work.title }}</h2>
          <div class="photographer-row">
            <el-avatar :size="48" :src="work.photographerAvatar || undefined">{{ work.photographerName?.[0] || 'P' }}</el-avatar>
            <div class="photographer-text">
              <div class="name-row">
                <span class="name">{{ work.photographerName || '匿名摄影师' }}</span>
                <el-tag size="small" type="success">信用 {{ work.photographerCreditScore }}</el-tag>
              </div>
              <div class="intro">{{ work.photographerIntro || '暂无简介' }}</div>
            </div>
          </div>

          <div class="tags">
            <el-tag v-for="tag in (work.styleTags || '').split(',').filter(Boolean)" :key="tag" size="small" type="info">{{ tag }}</el-tag>
          </div>

          <div class="desc">
            <h3>作品介绍</h3>
            <p>{{ work.description || '暂无介绍' }}</p>
          </div>

          <div class="stats">
            <span>👁 {{ work.viewCount || 0 }} 浏览</span>
            <span>❤️ {{ work.likeCount || 0 }} 点赞</span>
            <span>{{ formatTime(work.createTime) }} 发布</span>
          </div>

          <div class="actions">
            <el-button type="primary" @click="handleLike">点赞</el-button>
            <el-button :type="favorited ? 'warning' : 'default'" @click="handleFavorite">{{ favorited ? '已收藏' : '收藏' }}</el-button>
            <el-button type="success" plain @click="showOrder = true">预约拍摄</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 预约对话框 -->
    <el-dialog v-model="showOrder" title="预约拍摄" width="460px">
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkDetail, likeWork, createOrder, toggleFavorite, checkFavorite } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const work = ref(null)
const currentImage = ref('')
const showOrder = ref(false)
const favorited = ref(false)
const orderForm = reactive({ price: 0, shootTime: null, remark: '' })

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const load = async () => {
  loading.value = true
  try {
    const res = await getWorkDetail(route.params.id)
    work.value = res.data
    currentImage.value = res.data.cover || (res.data.images?.[0] || '')
    // 查询收藏状态
    const favRes = await checkFavorite(route.params.id)
    favorited.value = favRes.data.favorited
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  await likeWork(work.value.id)
  ElMessage.success('点赞成功')
  load()
}

const handleFavorite = async () => {
  const res = await toggleFavorite(work.value.id)
  favorited.value = res.data.favorited
  ElMessage.success(favorited.value ? '收藏成功' : '已取消收藏')
}

const submitOrder = async () => {
  if (!work.value.photographerUserId) {
    ElMessage.warning('无法获取摄影师信息')
    return
  }
  await createOrder({
    photographerId: work.value.photographerUserId,
    price: orderForm.price,
    shootTime: orderForm.shootTime ? new Date(orderForm.shootTime).toISOString().slice(0, 19) : null,
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
.work-detail-layout { display: flex; gap: 32px; }
.image-section { flex: 1; }
.main-image { width: 100%; height: 360px; background-size: cover; background-position: center; border-radius: 8px; }
.main-image.placeholder { display: flex; align-items: center; justify-content: center; color: #999; background: #f5f7fa; }
.thumb-list { display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
.thumb { width: 80px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 2px solid transparent; }
.thumb.active { border-color: #409eff; }
.info-section { flex: 1; }
.title { font-size: 22px; font-weight: 500; margin-bottom: 16px; }
.photographer-row { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.photographer-text .name-row { display: flex; align-items: center; gap: 8px; }
.photographer-text .name { font-size: 16px; font-weight: 500; }
.photographer-text .intro { font-size: 13px; color: #999; margin-top: 4px; }
.tags { display: flex; gap: 6px; margin-bottom: 16px; }
.desc h3 { font-size: 15px; margin-bottom: 8px; }
.desc p { color: #555; line-height: 1.8; font-size: 14px; }
.stats { display: flex; gap: 16px; color: #666; font-size: 13px; margin: 16px 0; }
.actions { display: flex; gap: 12px; }
</style>
