import { createRouter, createWebHistory } from 'vue-router'
import { bootstrapSession, session } from './session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('./views/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('./layouts/Shell.vue'),
      children: [
        { path: '', name: 'dashboard', component: () => import('./views/DashboardView.vue') },
        { path: 'tickets', name: 'tickets', component: () => import('./views/TicketListView.vue') },
        { path: 'tickets/new', name: 'ticket-create', component: () => import('./views/TicketCreateView.vue') },
        { path: 'tickets/:id', name: 'ticket-detail', component: () => import('./views/TicketDetailView.vue') }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  if (!session.ready) {
    await bootstrapSession()
  }
  if (!to.meta.public && !session.user) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && session.user) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
