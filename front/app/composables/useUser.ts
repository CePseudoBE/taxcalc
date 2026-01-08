import type { UserResponse } from '~/types/api'

interface ApiResponse<T> {
  data: T
  message?: string
}

interface UpdateProfileRequest {
  email?: string
  currentPassword?: string
  newPassword?: string
}

/**
 * Composable for user profile management.
 * Uses Nuxt server routes that handle authentication via BFF pattern.
 */
export function useUser() {
  const profile = ref<UserResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  /**
   * Fetch the current user's profile.
   */
  async function fetchUserProfile() {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/auth/check')
      profile.value = response.data
      return profile.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to fetch profile'
      profile.value = null
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Update the current user's profile.
   */
  async function updateProfile(request: UpdateProfileRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<UserResponse>>('/api/users/me', {
        method: 'PUT',
        body: request
      })
      profile.value = response.data
      return profile.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to update profile'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete the current user's account.
   */
  async function deleteAccount() {
    loading.value = true
    error.value = null

    try {
      await $fetch('/api/users/me', {
        method: 'DELETE'
      })
      profile.value = null
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to delete account'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Clear user profile from local state.
   */
  function clearProfile() {
    profile.value = null
    error.value = null
  }

  return {
    profile,
    loading,
    error,
    fetchUserProfile,
    updateProfile,
    deleteAccount,
    clearProfile
  }
}
