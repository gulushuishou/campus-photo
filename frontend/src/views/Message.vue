<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>私信</span>
        <el-tag v-if="totalUnread > 0" type="danger">{{ totalUnread }} 条未读</el-tag>
      </div>
    </template>

    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="conversation-panel">
        <div class="panel-header">
          <span>会话列表</span>
          <el-button size="small" type="primary" text @click="showNewChat = true">新会话</el-button>
        </div>
        <div class="conversation-list" v-loading="convLoading">
          <div
            v-for="conv in conversations"
            :key="conv.otherUserId"
            class="conversation-item"
            :class="{ active: currentUserId === conv.otherUserId }"
            @click="selectConversation(conv)"
          >
            <el-avatar :size="40">{{ conv.nickname?.[0] || 'U' }}</el-avatar>
            <div class="conv-info">
              <div class="conv-name">
                <span>{{ conv.nickname }}</span>
                <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" type="danger" />
              </div>
              <div class="conv-last">{{ conv.lastMessage }}</div>
            </div>
          </div>
          <el-empty v-if="!conversations.length && !convLoading" description="暂无会话" :image-size="60" />
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-panel">
        <template v-if="currentUserId">
          <div class="chat-header">
            <span>{{ currentNickname }}</span>
          </div>
          <div class="message-list" ref="messageListRef">
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ mine: msg.fromUserId === myUserId }">
              <div class="bubble">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <el-empty v-if="!messages.length" description="开始聊天吧" :image-size="60" />
          </div>
          <div class="send-area">
            <el-input v-model="inputText" placeholder="输入消息内容" @keyup.enter="handleSend" />
            <el-button type="primary" @click="handleSend">发送</el-button>
          </div>
        </template>
        <div v-else class="chat-empty">
          <el-empty description="选择左侧会话开始聊天，或发起新会话" />
        </div>
      </div>
    </div>

    <!-- 新会话对话框 -->
    <el-dialog v-model="showNewChat" title="发起新会话" width="400px">
      <el-form label-width="90px">
        <el-form-item label="对方用户ID">
          <el-input v-model.number="newUserId" placeholder="输入对方用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewChat = false">取消</el-button>
        <el-button type="primary" @click="startNewChat">开始聊天</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getConversation, sendMessage, getUnread, getConversations } from '@/api'
import { useUserStore } from '@/stores/user'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const userStore = useUserStore()
const myUserId = userStore.userInfo?.userId
const conversations = ref([])
const messages = ref([])
const currentUserId = ref(null)
const currentNickname = ref('')
const inputText = ref('')
const totalUnread = ref(0)
const convLoading = ref(false)
const showNewChat = ref(false)
const newUserId = ref(null)
const messageListRef = ref()
let stompClient = null

const formatTime = (t) => t ? t.replace('T', ' ').substring(0, 16) : ''

const loadConversations = async () => {
  convLoading.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } finally {
    convLoading.value = false
  }
}

const selectConversation = async (conv) => {
  currentUserId.value = conv.otherUserId
  currentNickname.value = conv.nickname
  const res = await getConversation(conv.otherUserId)
  messages.value = res.data || []
  scrollToBottom()
  loadConversations()
  loadUnread()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const handleSend = async () => {
  if (!inputText.value.trim()) return
  if (!currentUserId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  await sendMessage({ toUserId: currentUserId.value, content: inputText.value.trim() })
  inputText.value = ''
  const res = await getConversation(currentUserId.value)
  messages.value = res.data || []
  scrollToBottom()
  loadConversations()
}

const startNewChat = async () => {
  if (!newUserId.value) {
    ElMessage.warning('请输入对方用户ID')
    return
  }
  if (newUserId.value === myUserId) {
    ElMessage.warning('不能和自己聊天')
    return
  }
  currentUserId.value = newUserId.value
  currentNickname.value = '用户' + newUserId.value
  messages.value = []
  showNewChat.value = false
}

const loadUnread = async () => {
  try {
    const res = await getUnread()
    totalUnread.value = res.data || 0
  } catch (e) { /* 忽略 */ }
}

const connectWebSocket = () => {
  const token = localStorage.getItem('token')
  stompClient = new Client({
    // token 通过 URL 参数传给握手拦截器
    webSocketFactory: () => new SockJS(`/ws?token=${token}`),
    reconnectDelay: 5000,
    onConnect: () => {
      // 订阅自己的消息 topic
      stompClient.subscribe(`/topic/user/${myUserId}`, (message) => {
        const msg = JSON.parse(message.body)
        if (currentUserId.value === msg.fromUserId) {
          // 正在和该用户聊天，直接刷新消息
          getConversation(msg.fromUserId).then((res) => {
            messages.value = res.data || []
            scrollToBottom()
          })
        }
        loadConversations()
        loadUnread()
      })
    }
  })
  stompClient.activate()
}

onMounted(() => {
  loadConversations()
  loadUnread()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
  }
})
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.chat-layout { display: flex; height: 520px; border: 1px solid #eee; border-radius: 8px; overflow: hidden; }
.conversation-panel { width: 260px; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.panel-header { padding: 12px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; font-weight: 500; }
.conversation-list { flex: 1; overflow-y: auto; }
.conversation-item { display: flex; align-items: center; gap: 10px; padding: 12px; cursor: pointer; transition: background 0.2s; }
.conversation-item:hover { background: #f5f7fa; }
.conversation-item.active { background: #ecf5ff; }
.conv-info { flex: 1; overflow: hidden; }
.conv-name { display: flex; justify-content: space-between; align-items: center; font-size: 14px; }
.conv-last { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 2px; }
.chat-panel { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 12px; border-bottom: 1px solid #eee; font-weight: 500; }
.message-list { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.message-item { display: flex; flex-direction: column; max-width: 60%; }
.message-item.mine { align-self: flex-end; align-items: flex-end; }
.bubble { padding: 10px 14px; border-radius: 8px; background: #f0f2f5; font-size: 14px; word-break: break-word; }
.message-item.mine .bubble { background: #409eff; color: #fff; }
.msg-time { font-size: 11px; color: #999; margin-top: 4px; }
.send-area { display: flex; gap: 8px; padding: 12px; border-top: 1px solid #eee; }
.chat-empty { flex: 1; display: flex; align-items: center; justify-content: center; }
</style>
