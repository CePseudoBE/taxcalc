import type { SavedSearchResponse, SavedSearchRequest } from '~/types/api'

interface ApiResponse<T> {
  data: T
  message?: string
}

/**
 * Composable for managing saved searches.
 * Uses Nuxt server routes that handle authentication via BFF pattern.
 */
export function useSavedSearches() {
  const savedSearches = ref<SavedSearchResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  /**
   * Fetch all saved searches for the current user.
   */
  async function fetchSavedSearches() {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SavedSearchResponse[]>>('/api/saved-searches')
      savedSearches.value = response.data
      return savedSearches.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to fetch saved searches'
      savedSearches.value = []
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Save a new search.
   */
  async function saveSearch(request: SavedSearchRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SavedSearchResponse>>('/api/saved-searches', {
        method: 'POST',
        body: request
      })
      savedSearches.value.push(response.data)
      return response.data
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to save search'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete a saved search.
   */
  async function deleteSavedSearch(id: number) {
    loading.value = true
    error.value = null

    try {
      await $fetch(`/api/saved-searches/${id}`, {
        method: 'DELETE'
      })
      savedSearches.value = savedSearches.value.filter(s => s.id !== id)
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to delete saved search'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Clear all saved searches from local state.
   */
  function clearSavedSearches() {
    savedSearches.value = []
    error.value = null
  }

  return {
    savedSearches,
    loading,
    error,
    fetchSavedSearches,
    saveSearch,
    deleteSavedSearch,
    clearSavedSearches
  }
}
