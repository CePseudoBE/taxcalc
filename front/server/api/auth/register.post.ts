import { defineEventHandler, readBody, createError } from 'h3'
import { backendFetch, sessionConfig } from '../../utils/backend'

interface RegisterRequest {
  email: string
  password: string
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
  const body = await readBody<RegisterRequest>(event)

  if (!body.email || !body.password) {
    throw createError({
      statusCode: 400,
      message: 'Email and password are required'
    })
  }

  try {
    const authResponse = await backendFetch<AuthResponse>('/auth/register', {
      method: 'POST',
      body
    })

    // Stocker le token dans la session serveur
    const session = await useSession(event, sessionConfig)

    await session.update({
      accessToken: authResponse.accessToken,
      userId: authResponse.user.id,
      expiresAt: Date.now() + (authResponse.expiresIn * 1000)
    })

    // Retourner uniquement les infos utilisateur
    return {
      data: authResponse.user,
      message: 'Registration successful'
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 400,
      message: error.message || 'Registration failed'
    })
  }
})
