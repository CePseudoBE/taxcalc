import { defineEventHandler, createError } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

interface UserResponse {
  id: number
  email: string
  isModerator: boolean
  createdAt: string
}

export default defineEventHandler(async (event) => {
  const session = await useSession<UserSession>(event, sessionConfig)

  const token = session.data.accessToken
  const expiresAt = session.data.expiresAt

  if (!token) {
    throw createError({
      statusCode: 401,
      message: 'Not authenticated'
    })
  }

  // Verifier si le token est expire localement
  if (expiresAt && Date.now() > expiresAt) {
    await session.clear()
    throw createError({
      statusCode: 401,
      message: 'Session expired'
    })
  }

  try {
    // Valider le token aupres du backend
    const user = await backendFetch<UserResponse>('/auth/check', {
      token
    })

    return {
      data: user
    }
  } catch (error: any) {
    // Token invalide sur le backend - supprimer la session
    await session.clear()
    throw createError({
      statusCode: 401,
      message: 'Invalid session'
    })
  }
})
