import type { UserResponse, LoginRequest, UserRegistrationRequest } from '~/types/api'

interface ApiResponse<T> {
  data: T
  message?: string
}

/**
 * Composable pour la gestion de l'authentification.
 *
 * Les appels passent par les routes serveur Nuxt qui gerent
 * le stockage securise du token (BFF pattern).
 */
export function useAuth() {
  const user = useState<UserResponse | null>('auth-user', () => null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value)
  const isModerator = computed(() => user.value?.isModerator ?? false)

  /**
   * Verifie l'etat de l'authentification au chargement.
   */
  async function checkAuth() {
    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/check')
      user.value = response.data
    } catch {
      user.value = null
    }
  }

  /**
   * Connecte un utilisateur.
   */
  async function login(credentials: LoginRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/login', {
        method: 'POST',
        body: credentials
      })
      user.value = response.data
      return user.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Login failed'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Inscrit un nouvel utilisateur.
   */
  async function register(data: UserRegistrationRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/register', {
        method: 'POST',
        body: data
      })
      user.value = response.data
      return user.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Registration failed'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Deconnecte l'utilisateur.
   */
  async function logout() {
    try {
      await $fetch('/api/auth/logout', { method: 'POST' })
    } finally {
      user.value = null
    }
  }

  /**
   * Recupere les informations de l'utilisateur courant.
   */
  async function fetchCurrentUser() {
    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/check')
      user.value = response.data
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
