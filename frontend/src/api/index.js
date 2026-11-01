import request from '@/utils/request'

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const uploadFiles = (files) => {
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  return request.post('/upload/multiple', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 认证
export const register = (data) => request.post('/auth/register', data)
export const login = (data) => request.post('/auth/login', data)

// 用户信息
export const getMyInfo = () => request.get('/user/me')
export const updateMyInfo = (data) => request.put('/user/me', data)
export const getUserInfo = (id) => request.get(`/user/${id}`)

// 推荐
export const getRecommend = (tags, topN = 10) =>
  request.get('/recommend/photographers', { params: { tags, topN } })
export const getTags = () => request.get('/recommend/tags')

// 订单
export const createOrder = (data) => request.post('/order', data)
export const confirmOrder = (id) => request.post(`/order/${id}/confirm`)
export const finishOrder = (id) => request.post(`/order/${id}/finish`)
export const cancelOrder = (id) => request.post(`/order/${id}/cancel`)
export const getMyOrders = () => request.get('/order/my')

// 评价
export const evaluate = (orderId, data) => request.post(`/evaluation/${orderId}`, data)

// 私信
export const sendMessage = (data) => request.post('/message', data)
export const getConversation = (otherUserId) => request.get(`/message/conversation/${otherUserId}`)
export const getUnread = () => request.get('/message/unread')
export const getConversations = () => request.get('/message/conversations')

// 需求
export const createDemand = (data) => request.post('/demand', data)
export const getDemandList = (params) => request.get('/demand', { params })
export const getDemandDetail = (id) => request.get(`/demand/${id}`)
export const updateDemand = (id, data) => request.put(`/demand/${id}`, data)
export const closeDemand = (id) => request.post(`/demand/${id}/close`)
export const deleteDemand = (id) => request.delete(`/demand/${id}`)

// 作品
export const createWork = (data) => request.post('/work', data)
export const getWorkList = (params) => request.get('/work', { params })
export const getWorkDetail = (id) => request.get(`/work/${id}`)
export const updateWork = (id, data) => request.put(`/work/${id}`, data)
export const deleteWork = (id) => request.delete(`/work/${id}`)
export const likeWork = (id) => request.post(`/work/${id}/like`)

// 收藏
export const toggleFavorite = (workId) => request.post(`/favorite/toggle/${workId}`)
export const checkFavorite = (workId) => request.get(`/favorite/check/${workId}`)
export const getMyFavorites = () => request.get('/favorite/my')

// 摄影师认证
export const applyPhotographer = (data) => request.post('/photographer/apply', data)
export const getMyPhotographerStatus = () => request.get('/photographer/my-status')

// 管理端
export const getApplications = (auditStatus) => request.get('/admin/photographer/applications', { params: { auditStatus } })
export const auditPhotographer = (id, auditStatus) => request.post(`/admin/photographer/${id}/audit`, null, { params: { auditStatus } })
export const getAdminUsers = () => request.get('/admin/users')
export const changeUserStatus = (id, status) => request.post(`/admin/user/${id}/status`, null, { params: { status } })
export const getAdminWorks = () => request.get('/admin/works')
export const changeWorkStatus = (id, status) => request.post(`/admin/work/${id}/status`, null, { params: { status } })
export const getAdminDemands = () => request.get('/admin/demands')
export const changeDemandStatus = (id, status) => request.post(`/admin/demand/${id}/status`, null, { params: { status } })
export const getOverview = () => request.get('/admin/statistics/overview')
export const getTagStats = () => request.get('/admin/statistics/tags')
export const getOrderStatusStats = () => request.get('/admin/statistics/order-status')

// 管理员订单和投诉
export const getAdminOrders = () => request.get('/admin/orders')
export const getComplaints = (status) => request.get('/admin/complaints', { params: { status } })
export const resolveComplaint = (id, result) => request.post(`/admin/complaint/${id}/resolve`, null, { params: { result } })

// 用户投诉
export const submitComplaint = (data) => request.post('/complaint', data)
