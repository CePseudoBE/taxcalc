// Enums
export type Region = 'wallonia' | 'flanders' | 'brussels'

export type FuelType =
  | 'petrol'
  | 'diesel'
  | 'electric'
  | 'hybrid_petrol'
  | 'hybrid_diesel'
  | 'plug_in_hybrid_petrol'
  | 'plug_in_hybrid_diesel'
  | 'lpg'
  | 'cng'
  | 'hydrogen'

export type EuroNorm =
  | 'euro_1'
  | 'euro_2'
  | 'euro_3'
  | 'euro_4'
  | 'euro_5'
  | 'euro_5b'
  | 'euro_6'
  | 'euro_6d_temp'
  | 'euro_6d'
  | 'euro_7'

export type TaxType = 'tmc' | 'annual'

export type SubmissionStatus = 'pending' | 'approved' | 'rejected'

// API Response wrapper
export interface ApiResponse<T> {
  data?: T
  message?: string
  error?: {
    code: string
    details?: string[]
  }
}

// Brand, Model, Variant
export interface BrandResponse {
  id: number
  name: string
}

export interface ModelResponse {
  id: number
  name: string
  brand: BrandResponse
}

export interface VariantResponse {
  id: number
  name: string
  yearStart: number
  yearEnd: number | null
  powerKw: number
  fiscalHp: number
  fuel: FuelType
  euroNorm: EuroNorm
  co2Wltp: number | null
}

export interface VariantDetailResponse extends VariantResponse {
  model: ModelResponse
  co2Nedc: number | null
  displacementCc: number | null
  mmaKg: number | null
  hasParticleFilter: boolean | null
}

// Tax Calculation
export interface FirstRegistrationDate {
  year: number
  month?: number
  monthUnknown?: boolean
}

export interface TaxCalculationRequest {
  variantId?: number
  submissionId?: number
  region: Region
  firstRegistrationDate: FirstRegistrationDate
}

export interface TaxCalculationResponse {
  region: Region
  taxType: TaxType
  amount: number
  isExempt: boolean
  exemptionReason?: string
  breakdown: Record<string, unknown>
}

export interface TaxCalculationResult {
  tmc: TaxCalculationResponse
  annual: TaxCalculationResponse
}

// Manual Tax Calculation
export interface ManualTaxCalculationRequest {
  region: Region
  firstRegistrationDate: FirstRegistrationDate
  powerKw?: number
  fiscalHp?: number
  co2Wltp?: number
  co2Nedc?: number
  fuelType?: FuelType
  euroNorm?: EuroNorm
  mmaKg?: number
  displacementCc?: number
  hasParticleFilter?: boolean
}

// User / Auth
export interface UserResponse {
  id: number
  email: string
  isModerator: boolean
  createdAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface UserRegistrationRequest {
  email: string
  password: string
}

// Saved Search
export interface SavedSearchRequest {
  variantId?: number
  submissionId?: number
  region: Region
  firstRegistrationDate: FirstRegistrationDate
  label: string
}

export interface SavedSearchResponse {
  id: number
  label: string
  region: Region
  firstRegistrationDate: FirstRegistrationDate
  vehicleSummary: {
    brand: string
    model: string
    variant: string
    powerKw: number
    fuel: FuelType
  }
  createdAt: string
}

// Vehicle Submission
export interface VehicleSubmissionRequest {
  brandName: string
  modelName: string
  variantName: string
  yearStart: number
  yearEnd?: number
  powerKw: number
  fiscalHp?: number
  fuel: FuelType
  euroNorm: EuroNorm
  co2Wltp?: number
  co2Nedc?: number
  displacementCc?: number
  mmaKg?: number
  hasParticleFilter?: boolean
}

export interface SubmissionResponse {
  id: number
  status: SubmissionStatus
  vehicleData: VehicleSubmissionRequest
  submitterId: number
  submittedAt: string
  reviewedById?: number
  reviewedAt?: string
  feedback?: string
}
