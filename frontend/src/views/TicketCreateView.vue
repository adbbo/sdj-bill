<template>
  <form class="card form" @submit.prevent="submit">
    <div class="eyebrow">NEW CASE</div>
    <h3>登记对接问题</h3>
    <p class="muted">请尽量提供可复现信息（接口路径、商户号、发生时间、报文摘要）。盛迪嘉内部处理人将按影响程度排队跟进。</p>
    <label class="field">
      <span>问题摘要</span>
      <input v-model="form.title" maxlength="200" placeholder="例如：生产环境异步通知签名校验失败" />
    </label>
    <div class="row">
      <label class="field">
        <span>问题分类</span>
        <select v-model="form.category">
          <option v-for="(label, key) in CATEGORY" :key="key" :value="key">{{ label }}</option>
        </select>
      </label>
      <label class="field">
        <span>影响程度</span>
        <select v-model="form.priority">
          <option v-for="(label, key) in PRIORITY" :key="key" :value="key">{{ label }}</option>
        </select>
      </label>
    </div>
    <label class="field">
      <span>详细描述</span>
      <textarea v-model="form.description" placeholder="现象、影响范围、已尝试的排查步骤、相关单号"></textarea>
    </label>
    <div class="row">
      <label class="field">
        <span>联系电话</span>
        <input v-model="form.contactPhone" placeholder="选填" />
      </label>
      <label class="field">
        <span>联系邮箱</span>
        <input v-model="form.contactEmail" placeholder="选填" />
      </label>
      <label class="field">
        <span>商户号</span>
        <input v-model="form.merchantId" placeholder="选填，如 MCH8001001" />
      </label>
    </div>
    <p class="error" v-if="error">{{ error }}</p>
    <div class="actions">
      <button class="btn" :disabled="loading">{{ loading ? '提交中…' : '提交工单' }}</button>
      <router-link class="btn btn-ghost" to="/tickets">返回列表</router-link>
    </div>
  </form>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, CATEGORY, PRIORITY } from '../api'
import { session } from '../session'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({
  title: '',
  description: '',
  category: 'INTEGRATION',
  priority: 'MEDIUM',
  contactPhone: session.user?.phone || '',
  contactEmail: session.user?.email || '',
  merchantId: session.user?.merchantId || ''
})

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const created = await api.createTicket(form)
    router.push(`/tickets/${created.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form { padding: 28px; max-width: 880px; }
h3 { font-family: var(--font-serif); margin: 8px 0 8px; }
.row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.field { margin: 14px 0; }
.actions { display: flex; gap: 10px; margin-top: 8px; }
@media (max-width: 800px) {
  .row { grid-template-columns: 1fr; }
}
</style>
