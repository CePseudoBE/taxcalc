import { defineEventHandler, createError, getRouterParam } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

export default defineEventHandler(async (event) => {
  const session = await useSession<UserSession>(event, sessionConfig)

  const token = session.data.accessToken

  if (!token) {
    throw createError({
      statusCode: 401,
      message: 'Authentication required'
    })
  }

  const id = getRouterParam(event, 'id')

  if (!id || isNaN(Number(id))) {
    throw createError({
      statusCode: 400,
      message: 'Invalid saved search ID'
    })
  }

  try {
    await backendFetch(`/saved-searches/${id}`, {
      method: 'DELETE',
      token
    })

    return {
      message: 'Saved search deleted successfully'
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to delete saved search'
    })
  }
})
