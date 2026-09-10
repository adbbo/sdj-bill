<template>
  <div class="shell">
    <aside class="side">
      <div class="side-brand">
        <div class="seal">盛</div>
        <div>
          <div class="brand-name">盛迪嘉支付</div>
          <div class="eyebrow">商户对接支持</div>
        </div>
      </div>
      <nav>
        <router-link to="/" exact-active-class="active" :class="{ active: $route.name === 'dashboard' }">总览</router-link>
        <router-link to="/tickets" :class="{ active: $route.path.startsWith('/tickets') && $route.name !== 'ticket-create' }">工单中心</router-link>
        <router-link to="/tickets/new" :class="{ active: $route.name === 'ticket-create' }">提交工单</router-link>
      </nav>
      <div class="side-foot">
        <div class="muted">持牌支付机构</div>
        <div class="tiny">Payment Institution · China</div>
      </div>
    </aside>
    <div class="main">
      <header class="top">
        <div>
          <div class="eyebrow">SHENGDIJIA PAYMENT</div>
          <h1>{{ title }}</h1>
        </div>
        <div class="userbox" v-if="session.user">
          <div class="who">
            <strong>{{ session.user.displayName }}</strong>
            <span>{{ session.user.company }} · {{ session.user.role === 'INTERNAL' ? '内部处理人' : '对接方' }}</span>
          </div>
          <button class="btn btn-ghost" @click="onLogout">退出</button>
        </div>
      </header>
      <section class="page">
        <router-view />
      </section>
      <footer class="foot">
        © {{ year }} 盛迪嘉支付  商户对接支持平台  工单记录仅供授权合作机构使用
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout, session } from '../session'

const route = useRoute()
const router = useRouter()
const year = new Date().getFullYear()

const title = computed(() => {
  if (route.name === 'dashboard') return '业务总览'
  if (route.name === 'tickets') return '工单中心'
  if (route.name === 'ticket-create') return '提交对接工单'
  if (route.name === 'ticket-detail') return '工单详情'
  return '盛迪嘉支付'
})

async function onLogout() {
  await logout()
  router.push('/login')
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
}
.side {
  background:
    radial-gradient(circle at top, rgba(196,163,90,0.12), transparent 42%),
    linear-gradient(180deg, #102038, #070d18);
  border-right: 1px solid var(--line);
  padding: 28px 22px;
  display: flex;
  flex-direction: column;
  gap: 36px;
}
.side-brand { display: flex; gap: 14px; align-items: center; }
.side-brand .brand-name { font-size: 17px; letter-spacing: 0.08em; white-space: nowrap; }
nav { display: flex; flex-direction: column; gap: 8px; }
nav a {
  padding: 12px 14px;
  color: var(--ivory-dim);
  border: 1px solid transparent;
  letter-spacing: 0.12em;
}
nav a.active, nav a:hover {
  color: var(--gold-200);
  border-color: var(--line);
  background: rgba(196,163,90,0.08);
}
.side-foot { margin-top: auto; }
.tiny { color: var(--slate); font-size: 11px; letter-spacing: 0.18em; margin-top: 6px; }
.main { display: flex; flex-direction: column; min-width: 0; }
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 32px 16px;
  border-bottom: 1px solid var(--line);
  background: rgba(11,22,40,0.7);
  backdrop-filter: blur(12px);
}
.top h1 {
  margin: 6px 0 0;
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 600;
}
.userbox { display: flex; align-items: center; gap: 16px; }
.who { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.who span { color: var(--muted); font-size: 12px; }
.page { padding: 28px 32px 16px; flex: 1; }
.foot {
  padding: 16px 32px 28px;
  color: var(--slate);
  font-size: 12px;
  letter-spacing: 0.08em;
}
@media (max-width: 860px) {
  .shell { grid-template-columns: 1fr; }
  .side { flex-direction: row; align-items: center; gap: 18px; padding: 16px; }
  nav { flex-direction: row; flex-wrap: wrap; }
  .side-foot { display: none; }
  .top, .page, .foot { padding-left: 16px; padding-right: 16px; }
  .userbox { flex-direction: column; align-items: flex-end; gap: 8px; }
  .top h1 { font-size: 22px; }
}
</style>
