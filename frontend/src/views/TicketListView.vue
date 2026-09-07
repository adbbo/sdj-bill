<template>
  <div>
    <form class="filters card" @submit.prevent="load(0)">
      <label class="field">
        <span>关键词</span>
        <input v-model="filters.keyword" placeholder="编号 / 摘要 / 发起人" />
      </label>
      <label class="field">
        <span>状态</span>
        <select v-model="filters.status">
          <option value="">全部</option>
          <option v-for="(label, key) in STATUS" :key="key" :value="key">{{ label }}</option>
        </select>
      </label>
      <label class="field" v-if="internal">
        <span>对接方企业</span>
        <input v-model="filters.company" placeholder="企业名称" />
      </label>
      <label class="field" v-if="internal">
        <span>处理人</span>
        <select v-model="filters.handlerId">
          <option value="">全部</option>
          <option v-for="h in handlers" :key="h.id" :value="String(h.id)">{{ h.displayName }}</option>
        </select>
      </label>
      <label class="field">
        <span>起始日期</span>
        <input type="date" v-model="filters.from" />
      </label>
      <label class="field">
        <span>结束日期</span>
        <input type="date" v-model="filters.to" />
      </label>
      <div class="actions">
        <button class="btn" type="submit">筛选</button>
        <button class="btn btn-ghost" type="button" @click="reset">重置</button>
      </div>
    </form>

    <div class="card table-wrap">
      <table class="data" v-if="page && page.content.length">
        <thead>
          <tr>
            <th>工单编号</th>
            <th>摘要</th>
            <th>分类</th>
            <th>对接方</th>
            <th>状态</th>
            <th>影响</th>
            <th>处理人</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in page.content" :key="row.id" @click="$router.push(`/tickets/${row.id}`)">
            <td class="mono">{{ row.ticketNo }}</td>
            <td>{{ row.title }}</td>
            <td>{{ CATEGORY[row.category] }}</td>
            <td>{{ row.initiatorCompany }}</td>
            <td><span class="pill" :class="row.status">{{ STATUS[row.status] }}</span></td>
            <td><span class="pill" :class="row.priority">{{ PRIORITY[row.priority] }}</span></td>
            <td>{{ row.handlerName || '未指派' }}</td>
            <td class="muted">{{ formatDateTime(row.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div class="empty" v-else-if="!loading">没有符合条件的工单</div>
    </div>
    <div class="pager" v-if="page">
      <span class="muted">共 {{ page.totalElements }} 条</span>
      <button class="btn btn-ghost" :disabled="page.page <= 0" @click="load(page.page - 1)">上一页</button>
      <button class="btn btn-ghost" :disabled="page.page + 1 >= page.totalPages" @click="load(page.page + 1)">下一页</button>
    </div>
    <p class="error" v-if="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, STATUS, PRIORITY, CATEGORY, formatDateTime } from '../api'
import { isInternal } from '../session'

const route = useRoute()
const internal = isInternal()
const handlers = ref([])
const page = ref(null)
const loading = ref(false)
const error = ref('')
const filters = reactive({
  keyword: '',
  status: route.query.status || '',
  company: '',
  handlerId: '',
  from: '',
  to: ''
})

async function load(p = 0) {
  loading.value = true
  error.value = ''
  try {
    page.value = await api.tickets({ ...filters, page: p, size: 12 })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function reset() {
  filters.keyword = ''
  filters.status = ''
  filters.company = ''
  filters.handlerId = ''
  filters.from = ''
  filters.to = ''
  load(0)
}

onMounted(async () => {
  if (internal) {
    handlers.value = await api.handlers()
  }
  if (route.query.status) {
    filters.status = String(route.query.status)
  }
  await load(0)
})
</script>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  padding: 18px;
  margin-bottom: 16px;
}
.actions { display: flex; align-items: end; gap: 8px; }
.mono { color: var(--gold-200); }
.pager { display: flex; gap: 10px; align-items: center; margin-top: 16px; }
@media (max-width: 900px) {
  .filters { grid-template-columns: 1fr; }
}
</style>
