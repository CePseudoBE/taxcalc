import type { H3Event } from 'h3'

/**
 * Utilitaire pour communiquer avec le backend Spring Boot.
 * Gere l'ajout du token d'authentification dans les headers.
 */

interface BackendRequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  body?: unknown
  token?: string
}

interface BackendResponse<T> {
  data: T
  message?: string
  success: boolean
}

/**
 * Execute une requete vers le backend avec gestion automatique du token.
 *
 * @param endpoint - Endpoint relatif (ex: '/auth/login')
 * @param options - Options de la requete
 * @returns Les donnees de la reponse
 */
export async function backendFetch<T>(
  endpoint: string,
  options: BackendRequestOptions = {}
): Promise<T> {
  const backendUrl = process.env.NUXT_BACKEND_URL || 'http://localhost:8080/api'

  const { method = 'GET', body, token } = options

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Client-Name': 'nuxt-bff'
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${backendUrl}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  const json = await response.json() as BackendResponse<T>

  if (!response.ok) {
    const error = new Error((json as any).message || 'Backend error')
    ;(error as any).statusCode = response.status
    ;(error as any).data = json
    throw error
  }

  return json.data
}

/**
 * Configuration de session pour stocker le token.
 * Le secret doit etre au moins 32 caracteres.
 */
export const sessionConfig = {
  password: process.env.NUXT_SESSION_SECRET || 'development-secret-key-minimum-32-chars!!'
}

/**
 * Interface de la session utilisateur.
 */
export interface UserSession {
  accessToken?: string
  userId?: number
  expiresAt?: number
}
