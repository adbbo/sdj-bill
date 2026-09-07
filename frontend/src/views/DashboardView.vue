<template>
  <div>
    <p class="lead">欢迎回到盛迪嘉支付商户对接支持平台。以下为当前权限范围内的工单概览。</p>
    <div class="grid-stats" v-if="data">
      <article class="stat card" v-for="item in stats" :key="item.key" @click="$router.push(`/tickets?status=${item.key}`)">
        <div class="eyebrow">{{ item.label }}</div>
        <div class="num">{{ item.value }}</div>
        <div class="muted">点击进入对应列表</div>
      </article>
    </div>
    <div class="toolbar">
      <div>
        <div class="eyebrow">RECENT CASES</div>
        <h3>最近更新</h3>
      </div>
      <router-link class="btn" to="/tickets/new">提交新工单</router-link>
    </div>
    <div class="card table-wrap" v-if="data">
      <table class="data" v-if="data.recentTickets.length">
        <thead>
          <tr>
            <th>工单编号</th>
            <th>摘要</th>
            <th>对接方</th>
            <th>状态</th>
            <th>影响</th>
            <th>处理人</th>
            <th>更新时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in data.recentTickets" :key="row.id" @click="$router.push(`/tickets/${row.id}`)">
            <td class="mono">{{ row.ticketNo }}</td>
            <td>{{ row.title }}</td>
            <td>{{ row.initiatorCompany }}</td>
            <td><span class="pill" :class="row.status">{{ STATUS[row.status] }}</span></td>
            <td><span class="pill" :class="row.priority">{{ PRIORITY[row.priority] }}</span></td>
            <td>{{ row.handlerName || '未指派' }}</td>
            <td class="muted">{{ formatDateTime(row.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div class="empty" v-else>暂无工单</div>
    </div>
    <p class="error" v-if="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api, STATUS, PRIORITY, formatDateTime } from '../api'
const data = ref(null)
const error = ref('')

const stats = computed(() => {
  const counts = data.value?.statusCounts || {}
  return [
    { key: 'PENDING', label: '待处理', value: counts.PENDING || 0 },
    { key: 'IN_PROGRESS', label: '处理中', value: counts.IN_PROGRESS || 0 },
    { key: 'RESOLVED', label: '已解决', value: counts.RESOLVED || 0 },
    { key: 'CLOSED', label: '已关闭', value: counts.CLOSED || 0 }
  ]
})

onMounted(async () => {
  try {
    data.value = await api.dashboard()
  } catch (e) {
    error.value = e.message
  }
})
</script>

<style scoped>
.lead { color: var(--ivory-dim); margin: 0 0 24px; max-width: 720px; }
.stat { padding: 20px 22px; cursor: pointer; }
.stat:hover { border-color: var(--gold-500); }
.num {
  font-family: var(--font-serif);
  font-size: 40px;
  margin: 10px 0 8px;
  color: var(--gold-200);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: end;
  margin: 32px 0 14px;
}
h3 { margin: 8px 0 0; font-family: var(--font-serif); font-weight: 600; }
.mono { font-variant-numeric: tabular-nums; color: var(--gold-200); }
.card { padding: 8px 8px 0; }
</style>
