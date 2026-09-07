<template>
  <div v-if="ticket" class="detail">
    <div class="hero card">
      <div class="hero-top">
        <div>
          <div class="eyebrow">{{ ticket.ticketNo }}</div>
          <h3>{{ ticket.title }}</h3>
        </div>
        <div class="hero-tags">
          <span class="pill" :class="ticket.status">{{ STATUS[ticket.status] }}</span>
          <span class="pill" :class="ticket.priority">{{ PRIORITY[ticket.priority] }}</span>
          <span class="pill">{{ CATEGORY[ticket.category] }}</span>
        </div>
      </div>
      <div class="meta">
        <div><span>发起人</span><strong>{{ ticket.initiator.displayName }}</strong></div>
        <div><span>对接企业</span><strong>{{ ticket.initiator.company }}</strong></div>
        <div><span>处理人</span><strong>{{ ticket.handler?.displayName || '未指派' }}</strong></div>
        <div><span>商户号</span><strong>{{ ticket.merchantId || '—' }}</strong></div>
        <div><span>联系电话</span><strong>{{ ticket.contactPhone || '—' }}</strong></div>
        <div><span>联系邮箱</span><strong>{{ ticket.contactEmail || '—' }}</strong></div>
        <div><span>创建时间</span><strong>{{ formatDateTime(ticket.createdAt) }}</strong></div>
        <div><span>解决时间</span><strong>{{ formatDateTime(ticket.resolvedAt) }}</strong></div>
      </div>
    </div>

    <div class="layout">
      <section class="card block">
        <div class="eyebrow">DESCRIPTION</div>
        <h4>问题详情</h4>
        <pre class="desc">{{ ticket.description }}</pre>

        <div class="eyebrow">TIMELINE</div>
        <h4>处理轨迹</h4>
        <ol class="timeline">
          <li v-for="event in ticket.timeline" :key="event.id">
            <div class="dot" :class="event.eventType"></div>
            <div>
              <div class="when">{{ formatDateTime(event.createdAt) }} · {{ event.authorName }}（{{ event.authorCompany }}）</div>
              <div class="what">{{ event.content }}</div>
              <div class="etype">{{ eventLabel(event.eventType) }}</div>
            </div>
          </li>
        </ol>

        <form class="remark" @submit.prevent="sendRemark">
          <label class="field">
            <span>追加备注</span>
            <textarea v-model="remark" placeholder="补充排查进展、渠道回复或对商户的说明"></textarea>
          </label>
          <button class="btn" :disabled="saving">写入备注</button>
        </form>
      </section>

      <aside class="card block" v-if="internal">
        <div class="eyebrow">INTERNAL</div>
        <h4>处理操作</h4>
        <p class="muted">仅盛迪嘉内部处理人可变更状态与指派。</p>
        <label class="field">
          <span>工单状态</span>
          <select v-model="edit.status">
            <option v-for="(label, key) in STATUS" :key="key" :value="key">{{ label }}</option>
          </select>
        </label>
        <label class="field">
          <span>处理人</span>
          <select v-model="edit.handlerId">
            <option value="">保持不变</option>
            <option v-for="h in handlers" :key="h.id" :value="String(h.id)">{{ h.displayName }}</option>
          </select>
        </label>
        <label class="field">
          <span>操作说明（可选）</span>
          <textarea v-model="edit.remark" placeholder="同步给对接方的处理说明"></textarea>
        </label>
        <button class="btn" :disabled="saving" @click="saveHandle">保存处理结果</button>
      </aside>
    </div>
    <p class="error" v-if="error">{{ error }}</p>
  </div>
  <div v-else class="empty">{{ error || '正在载入工单…' }}</div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api, STATUS, PRIORITY, CATEGORY, formatDateTime } from '../api'
import { isInternal } from '../session'

const route = useRoute()
const ticket = ref(null)
const handlers = ref([])
const remark = ref('')
const error = ref('')
const saving = ref(false)
const internal = isInternal()
const edit = reactive({
  status: 'PENDING',
  handlerId: '',
  remark: ''
})

function eventLabel(type) {
  return {
    CREATED: '创建',
    REMARK: '备注',
    STATUS_CHANGE: '状态',
    HANDLER_CHANGE: '指派'
  }[type] || type
}

function applyTicket(data) {
  ticket.value = data
  edit.status = data.status
  edit.handlerId = data.handler ? String(data.handler.id) : ''
  edit.remark = ''
}

async function load() {
  ticket.value = null
  error.value = ''
  try {
    const data = await api.ticket(route.params.id)
    applyTicket(data)
    if (internal) {
      handlers.value = await api.handlers()
    }
  } catch (e) {
    error.value = e.message
  }
}

async function sendRemark() {
  if (!remark.value.trim()) return
  saving.value = true
  error.value = ''
  try {
    applyTicket(await api.addRemark(ticket.value.id, remark.value.trim()))
    remark.value = ''
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

async function saveHandle() {
  saving.value = true
  error.value = ''
  try {
    const payload = {
      status: edit.status,
      remark: edit.remark || null
    }
    if (edit.handlerId) {
      payload.handlerId = Number(edit.handlerId)
    }
    applyTicket(await api.updateTicket(ticket.value.id, payload))
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

watch(() => route.params.id, load, { immediate: true })
</script>

<style scoped>
.hero { padding: 24px; margin-bottom: 18px; }
.hero-top { display: flex; justify-content: space-between; gap: 16px; align-items: start; }
h3, h4 { font-family: var(--font-serif); margin: 8px 0 12px; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.meta {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 20px;
}
.meta span { display: block; color: var(--muted); font-size: 12px; margin-bottom: 6px; }
.layout { display: grid; grid-template-columns: 1.4fr 0.8fr; gap: 16px; }
.block { padding: 22px; }
.desc {
  white-space: pre-wrap;
  font-family: var(--font-sans);
  color: var(--ivory-dim);
  line-height: 1.7;
  margin: 0 0 28px;
}
.timeline { list-style: none; padding: 0; margin: 0 0 24px; }
.timeline li {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  padding: 0 0 18px;
  position: relative;
}
.timeline li::before {
  content: "";
  position: absolute;
  left: 8px;
  top: 14px;
  bottom: 0;
  width: 1px;
  background: var(--line);
}
.dot {
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border: 1px solid var(--gold-500);
  background: var(--navy-900);
  transform: rotate(45deg);
}
.when { color: var(--muted); font-size: 12px; }
.what { margin: 6px 0; }
.etype { color: var(--gold-400); font-size: 12px; letter-spacing: 0.12em; }
.remark .field { margin-bottom: 10px; }
@media (max-width: 960px) {
  .layout, .meta { grid-template-columns: 1fr; }
  .hero-top { flex-direction: column; }
}
</style>
