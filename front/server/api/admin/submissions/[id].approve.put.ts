import { defineEventHandler, createError, getRouterParam } from 'h3'
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

  const id = getRouterParam(event, 'id')

  if (!id || isNaN(Number(id))) {
    throw createError({
      statusCode: 400,
      message: 'Invalid submission ID'
    })
  }

  try {
    const submission = await backendFetch<SubmissionResponse>(`/admin/submissions/${id}/approve`, {
      method: 'PUT',
      token
    })

    return {
      data: submission,
      message: 'Submission approved successfully'
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
      message: error.message || 'Failed to approve submission'
    })
  }
})
