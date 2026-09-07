import { reactive } from 'vue'
import { api } from './api'

export const session = reactive({
  user: null,
  ready: false
})

export async function bootstrapSession() {
  try {
    session.user = await api.me()
  } catch {
    session.user = null
  } finally {
    session.ready = true
  }
}

export async function login(username, password) {
  session.user = await api.login(username, password)
  return session.user
}

export async function logout() {
  try {
    await api.logout()
  } finally {
    session.user = null
  }
}

export function isInternal() {
  return session.user?.role === 'INTERNAL'
}
