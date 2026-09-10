const STATUS = {
  PENDING: '待处理',
  IN_PROGRESS: '处理中',
  RESOLVED: '已解决',
  CLOSED: '已关闭'
}

const PRIORITY = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急'
}

const CATEGORY = {
  INTEGRATION: '对接',
  TRANSACTION: '交易',
  SETTLEMENT: '结算',
  OTHER: '其他'
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  })
  if (response.status === 204) {
    return null
  }
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    const error = new Error(data?.message || '请求失败')
    error.status = response.status
    throw error
  }
  return data
}

export const api = {
  login: (username, password) => request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  me: () => request('/api/auth/me'),
  dashboard: () => request('/api/dashboard/summary'),
  handlers: () => request('/api/handlers'),
  tickets: (params) => {
    const query = new URLSearchParams()
    Object.entries(params || {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        query.set(key, value)
      }
    })
    const suffix = query.toString() ? `?${query}` : ''
    return request(`/api/tickets${suffix}`)
  },
  ticket: (id) => request(`/api/tickets/${id}`),
  createTicket: (payload) => request('/api/tickets', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  updateTicket: (id, payload) => request(`/api/tickets/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload)
  }),
  addRemark: (id, content) => request(`/api/tickets/${id}/remarks`, {
    method: 'POST',
    body: JSON.stringify({ content })
  })
}

export { STATUS, PRIORITY, CATEGORY }

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value.replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    hour12: false,
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
