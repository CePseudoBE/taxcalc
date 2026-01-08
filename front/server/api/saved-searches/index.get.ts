import { defineEventHandler, createError } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

interface SavedSearchResponse {
  id: number
  label: string
  region: string
  firstRegistrationDate: {
    year: number
    month?: number
    monthUnknown?: boolean
  }
  vehicleSummary: {
    brand: string
    model: string
    variant: string
    powerKw: number
    fuel: string
  }
  createdAt: string
}

export default defineEventHandler(async (event) => {
  const session = await useSession<UserSession>(event, sessionConfig)

  const token = session.data.accessToken

  if (!token) {
    throw createError({
      statusCode: 401,
      message: 'Authentication required'
    })
  }

  try {
    const savedSearches = await backendFetch<SavedSearchResponse[]>('/saved-searches', {
      token
    })

    return {
      data: savedSearches
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to fetch saved searches'
    })
  }
})
