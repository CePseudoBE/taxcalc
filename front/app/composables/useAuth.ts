import type { UserResponse } from '~/types/api'

interface ApiResponse<T> {
  data: T
  message?: string
}

interface GoogleAuthRequest {
  idToken: string
}

/**
 * Composable pour la gestion de l'authentification.
 * Authentification uniquement via Google OAuth.
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
  const isAdmin = computed(() => user.value?.isAdmin ?? false)

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
   * Connecte un utilisateur via Google OAuth.
   */
  async function loginWithGoogle(idToken: string) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/google', {
        method: 'POST',
        body: { idToken } as GoogleAuthRequest
      })
      user.value = response.data
      return user.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Google login failed'
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
    isAdmin,
    checkAuth,
    loginWithGoogle,
    logout,
    fetchCurrentUser
  }
}
