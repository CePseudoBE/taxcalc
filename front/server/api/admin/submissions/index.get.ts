import { defineEventHandler, createError, getQuery } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../../utils/backend'

interface SubmissionResponse {
  id: number
  status: string
  vehicleData: {
    brandName: string
    modelName: string
    variantName: string
    yearStart: number
    yearEnd?: number
    powerKw: number
    fiscalHp?: number
    fuel: string
    euroNorm: string
    co2Wltp?: number
    co2Nedc?: number
    displacementCc?: number
    mmaKg?: number
    hasParticleFilter?: boolean
  }
  submitterId: number
  submitterEmail?: string
  submittedAt: string
  reviewedById?: number
  reviewedAt?: string
  feedback?: string
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

  const query = getQuery(event)
  const status = query.status as string | undefined

  try {
    const endpoint = status ? `/admin/submissions?status=${status}` : '/admin/submissions'
    const submissions = await backendFetch<SubmissionResponse[]>(endpoint, {
      token
    })

    return {
      data: submissions
    }
  } catch (error: any) {
    if (error.statusCode === 403) {
      throw createError({
        statusCode: 403,
        message: 'Admin or moderator access required'
      })
    }
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to fetch submissions'
    })
  }
})
