import { defineEventHandler, readBody, createError } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

interface SavedSearchRequest {
  variantId?: number
  submissionId?: number
  region: string
  firstRegistrationDate: {
    year: number
    month?: number
    monthUnknown?: boolean
  }
  label: string
}

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

  const body = await readBody<SavedSearchRequest>(event)

  if (!body.region || !body.firstRegistrationDate || !body.label) {
    throw createError({
      statusCode: 400,
      message: 'Region, registration date and label are required'
    })
  }

  if (!body.variantId && !body.submissionId) {
    throw createError({
      statusCode: 400,
      message: 'Either variantId or submissionId is required'
    })
  }

  try {
    const savedSearch = await backendFetch<SavedSearchResponse>('/saved-searches', {
      method: 'POST',
      body,
      token
    })

    return {
      data: savedSearch,
      message: 'Search saved successfully'
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to save search'
    })
  }
})
