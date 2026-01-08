import type { ApiResponse } from '~/types/api'

/**
 * Composable pour les appels API publics (taxes, vehicules, etc.).
 *
 * Note: Les endpoints authentifies (submissions, saved-searches)
 * doivent passer par les routes serveur Nuxt qui gerent le token.
 */
export function useApi() {
  const config = useRuntimeConfig()
  const baseUrl = config.public.apiBaseUrl as string

  async function request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${baseUrl}${endpoint}`

    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    })

    const json: ApiResponse<T> = await response.json()

    if (!response.ok || json.error) {
      const error = new Error(json.message || 'Une erreur est survenue')
      ;(error as any).code = json.error?.code
      ;(error as any).details = json.error?.details
      throw error
    }

    return json.data as T
  }

  function get<T>(endpoint: string) {
    return request<T>(endpoint, { method: 'GET' })
  }

  function post<T>(endpoint: string, body?: unknown) {
    return request<T>(endpoint, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined
    })
  }

  function put<T>(endpoint: string, body?: unknown) {
    return request<T>(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined
    })
  }

  function del<T>(endpoint: string) {
    return request<T>(endpoint, { method: 'DELETE' })
  }

  return { get, post, put, del, request }
}
