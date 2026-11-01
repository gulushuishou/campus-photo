<template>
  <div>
    <!-- 顶部 Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1>校园约拍平台</h1>
        <p>发现校园里的美好瞬间，让每一次拍摄都有迹可循</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="goDemand">发布约拍需求</el-button>
          <el-button size="large" plain @click="goWorks">浏览摄影师作品</el-button>
        </div>
      </div>
    </div>

    <!-- 核心功能亮点 -->
    <el-row :gutter="16" class="feature-row">
      <el-col :span="6" v-for="feature in features" :key="feature.title">
        <el-card class="feature-card" shadow="hover">
          <div class="feature-icon" :style="{ background: feature.color }">{{ feature.icon }}</div>
          <div class="feature-title">{{ feature.title }}</div>
          <div class="feature-desc">{{ feature.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 智能推荐 -->
    <el-card class="recommend-card">
      <template #header>
        <div class="card-header">
          <span>智能推荐 · 为你匹配摄影师</span>
          <span class="hint">基于余弦相似度 + 信用分权重</span>
        </div>
      </template>

      <div class="tag-select">
        <span class="label">选择你喜欢的风格：</span>
        <el-checkbox-group v-model="selectedTags">
          <el-checkbox v-for="tag in allTags" :key="tag" :value="tag">{{ tag }}</el-checkbox>
        </el-checkbox-group>
        <el-button type="primary" @click="handleRecommend">开始推荐</el-button>
      </div>

      <div v-if="recommendList.length" class="recommend-list">
        <el-card v-for="item in recommendList" :key="item.photographerId" class="photographer-item" shadow="hover">
          <div class="photographer-info">
            <el-avatar :size="56">{{ item.nickname?.[0] }}</el-avatar>
            <div class="info-text">
              <div class="name-row">
                <span class="name">{{ item.nickname }}</span>
                <el-tag size="small" type="success">信用分 {{ item.creditScore }}</el-tag>
              </div>
              <div class="intro">{{ item.intro || '暂无简介' }}</div>
              <div class="tags">
                <el-tag v-for="tag in (item.styleTags || '').split(',').filter(Boolean)" :key="tag" size="small" class="tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="score">
              <div class="score-num">{{ item.similarity }}</div>
              <div class="score-label">相似度</div>
            </div>
          </div>
        </el-card>
      </div>
      <el-empty v-else-if="recommended" description="暂无推荐结果，请选择风格标签" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTags, getRecommend } from '@/api'

const router = useRouter()
const allTags = ref([])
const selectedTags = ref([])
const recommendList = ref([])
const recommended = ref(false)

const features = [
  { icon: '📷', title: '作品展示', desc: '摄影师发布多图作品集，风格标签分类浏览', color: '#e6f1fb' },
  { icon: '🎯', title: '智能推荐', desc: '余弦相似度匹配，快速找到心仪摄影师', color: '#e1f5ee' },
  { icon: '📋', title: '订单管理', desc: '状态机规范约拍流程，杜绝违约纠纷', color: '#faeeda' },
  { icon: '💬', title: '实时沟通', desc: 'WebSocket 私信，闭环沟通拍摄细节', color: '#eeedfe' }
]

const goDemand = () => router.push('/demand')
const goWorks = () => router.push('/works')

onMounted(async () => {
  const res = await getTags()
  allTags.value = res.data
})

const handleRecommend = async () => {
  if (!selectedTags.value.length) {
    ElMessage.warning('请至少选择一个风格标签')
    return
  }
  const res = await getRecommend(selectedTags.value.join(','))
  recommendList.value = res.data
  recommended.value = true
}
</script>

<style scoped>
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 48px 40px;
  margin-bottom: 16px;
  color: #fff;
}
.hero-content h1 {
  font-size: 32px;
  font-weight: 500;
  margin-bottom: 12px;
}
.hero-content p {
  font-size: 15px;
  opacity: 0.9;
  margin-bottom: 24px;
}
.hero-actions { display: flex; gap: 12px; }
.feature-row { margin-bottom: 16px; }
.feature-card { text-align: center; }
.feature-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin: 0 auto 12px; }
.feature-title { font-size: 15px; font-weight: 500; margin-bottom: 6px; }
.feature-desc { font-size: 13px; color: #999; line-height: 1.5; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.hint { font-size: 12px; color: #999; }
.tag-select { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.label { font-size: 14px; color: #333; }
.recommend-list { display: flex; flex-direction: column; gap: 12px; }
.photographer-info { display: flex; align-items: center; gap: 16px; }
.info-text { flex: 1; }
.name-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.name { font-size: 16px; font-weight: 500; }
.intro { color: #666; font-size: 13px; margin-bottom: 6px; }
.tags { display: flex; gap: 6px; }
.score { text-align: center; }
.score-num { font-size: 24px; color: #409eff; font-weight: 500; }
.score-label { font-size: 12px; color: #999; }
</style>
