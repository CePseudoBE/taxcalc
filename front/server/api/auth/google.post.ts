import { defineEventHandler, readBody, createError } from 'h3'
import { backendFetch, sessionConfig } from '../../utils/backend'

interface GoogleAuthRequest {
  idToken: string
}

interface UserResponse {
  id: number
  email: string
  isModerator: boolean
  createdAt: string
}

interface AuthResponse {
  user: UserResponse
  accessToken: string
  expiresIn: number
}

export default defineEventHandler(async (event) => {
  const body = await readBody<GoogleAuthRequest>(event)

  if (!body.idToken) {
    throw createError({
      statusCode: 400,
      message: 'Google ID token is required'
    })
  }

  try {
    const authResponse = await backendFetch<AuthResponse>('/auth/google', {
      method: 'POST',
      body
    })

    // Stocker le token dans la session serveur (jamais expose au client)
    const session = await useSession(event, sessionConfig)

    await session.update({
      accessToken: authResponse.accessToken,
      userId: authResponse.user.id,
      expiresAt: Date.now() + (authResponse.expiresIn * 1000)
    })

    // Retourner uniquement les infos utilisateur (sans le token)
    return {
      data: authResponse.user,
      message: 'Google login successful'
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 401,
      message: error.message || 'Google authentication failed'
    })
  }
})
