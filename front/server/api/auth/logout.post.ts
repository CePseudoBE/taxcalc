import { defineEventHandler } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

export default defineEventHandler(async (event) => {
  const session = await useSession<UserSession>(event, sessionConfig)

  const token = session.data.accessToken

  if (token) {
    try {
      // Revoquer le token sur le backend
      await backendFetch('/auth/logout', {
        method: 'POST',
        token
      })
    } catch {
      // Ignorer les erreurs backend pendant le logout
      // Le token sera de toute facon supprime de la session
    }
  }

  // Supprimer la session
  await session.clear()

  return {
    message: 'Logout successful'
  }
})
