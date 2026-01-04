import type { UserResponse, LoginRequest, UserRegistrationRequest } from '~/types/api'

export function useAuth() {
  const api = useApi()

  const user = useState<UserResponse | null>('auth-user', () => null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value)
  const isModerator = computed(() => user.value?.isModerator ?? false)

  async function checkAuth() {
    try {
      user.value = await api.get<UserResponse>('/auth/check')
    } catch {
      user.value = null
    }
  }

  async function login(credentials: LoginRequest) {
    loading.value = true
    error.value = null

    try {
      user.value = await api.post<UserResponse>('/auth/login', credentials)
      return user.value
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function register(data: UserRegistrationRequest) {
    loading.value = true
    error.value = null

    try {
      user.value = await api.post<UserResponse>('/auth/register', data)
      return user.value
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } finally {
      user.value = null
    }
  }

  async function fetchCurrentUser() {
    try {
      user.value = await api.get<UserResponse>('/users/me')
    } catch {
      user.value = null
    }
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    isModerator,
    checkAuth,
    login,
    register,
    logout,
    fetchCurrentUser
  }
}
