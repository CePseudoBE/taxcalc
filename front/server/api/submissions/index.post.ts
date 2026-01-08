import { defineEventHandler, readBody, createError } from 'h3'
import { backendFetch, sessionConfig, type UserSession } from '../../utils/backend'

interface VehicleSubmissionRequest {
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

interface SubmissionResponse {
  id: number
  status: string
  vehicleData: VehicleSubmissionRequest
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

  const body = await readBody<VehicleSubmissionRequest>(event)

  // Validate required fields
  if (!body.brandName || !body.modelName || !body.variantName) {
    throw createError({
      statusCode: 400,
      message: 'Brand, model and variant names are required'
    })
  }

  if (!body.yearStart || !body.powerKw) {
    throw createError({
      statusCode: 400,
      message: 'Year and power are required'
    })
  }

  if (!body.fuel || !body.euroNorm) {
    throw createError({
      statusCode: 400,
      message: 'Fuel type and Euro norm are required'
    })
  }

  try {
    const submission = await backendFetch<SubmissionResponse>('/submissions', {
      method: 'POST',
      body,
      token
    })

    return {
      data: submission,
      message: 'Submission created successfully'
    }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to create submission'
    })
  }
})
