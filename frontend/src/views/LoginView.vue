<template>
  <div class="login">
    <div class="panel brand">
      <div class="watermark">盛</div>
      <div class="seal large">盛</div>
      <h1 class="brand-name">盛迪嘉支付</h1>
      <p class="tag">持牌支付机构 · 商户对接支持中心</p>
      <div class="gold-line"></div>
      <ul>
        <li>对接、交易、结算问题统一归档</li>
        <li>处理人、状态与备注全程可追溯</li>
        <li>取代即时通讯群，保障服务连续性</li>
      </ul>
    </div>
    <div class="panel form-panel">
      <div class="eyebrow">SECURE ACCESS</div>
      <h2>登录支持平台</h2>
      <p class="muted intro">请使用贵司开通的对接账号进入。演示环境账号见页面下方。</p>
      <form @submit.prevent="submit">
        <label class="field">
          <span>账号</span>
          <input v-model="username" autocomplete="username" placeholder="请输入账号" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" />
        </label>
        <p class="error" v-if="error">{{ error }}</p>
        <button class="btn" :disabled="loading">{{ loading ? '正在验证…' : '进入平台' }}</button>
      </form>
      <div class="demos">
        <div class="demo" @click="fill('admin', 'Sdj@Admin2026')">内部管理员 · admin</div>
        <div class="demo" @click="fill('handler', 'Sdj@Handler2026')">内部处理人 · handler</div>
        <div class="demo" @click="fill('xinghui', 'Sdj@Partner2026')">星辉科技 · xinghui</div>
        <div class="demo" @click="fill('yuntu', 'Sdj@Partner2026')">云途电商 · yuntu</div>
      </div>
      <p class="legal">© {{ year }} 盛迪嘉支付  演示密码见 README</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../session'

const router = useRouter()
const route = useRoute()
const username = ref('admin')
const password = ref('Sdj@Admin2026')
const loading = ref(false)
const error = ref('')
const year = new Date().getFullYear()

function fill(u, p) {
  username.value = u
  password.value = p
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await login(username.value.trim(), password.value)
    router.replace(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background:
    radial-gradient(circle at 20% 20%, rgba(196,163,90,0.16), transparent 32%),
    radial-gradient(circle at 80% 90%, rgba(30,58,95,0.7), transparent 40%),
    var(--navy-950);
}
.panel { padding: 72px 64px; position: relative; overflow: hidden; }
.brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-right: 1px solid var(--line);
}
.watermark {
  position: absolute;
  right: -20px;
  bottom: -80px;
  font-size: 320px;
  font-family: var(--font-serif);
  color: rgba(196,163,90,0.05);
  pointer-events: none;
}
.seal.large { width: 72px; height: 72px; font-size: 34px; margin-bottom: 24px; }
h1 { font-size: 42px; margin: 0 0 12px; }
.tag { color: var(--gold-200); letter-spacing: 0.22em; margin: 0 0 28px; }
ul { padding: 0; margin: 28px 0 0; list-style: none; color: var(--ivory-dim); }
ul li { padding: 10px 0 10px 18px; position: relative; }
ul li::before {
  content: "";
  width: 7px; height: 7px;
  border: 1px solid var(--gold-500);
  position: absolute;
  left: 0; top: 16px;
  transform: rotate(45deg);
}
.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 520px;
}
h2 { font-family: var(--font-serif); font-size: 32px; margin: 10px 0 8px; }
.intro { margin-bottom: 28px; }
form { display: flex; flex-direction: column; gap: 16px; }
.btn { margin-top: 8px; }
.demos {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 28px;
}
.demo {
  border: 1px solid var(--line);
  padding: 10px 12px;
  font-size: 12px;
  color: var(--ivory-dim);
  cursor: pointer;
}
.demo:hover { border-color: var(--gold-500); color: var(--gold-200); }
.legal { margin-top: 28px; color: var(--slate); font-size: 12px; }
@media (max-width: 900px) {
  .login { grid-template-columns: 1fr; }
  .brand { display: none; }
  .panel { padding: 36px 20px; }
  .demos { grid-template-columns: 1fr; }
}
</style>
