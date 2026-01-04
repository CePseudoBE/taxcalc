import type {
  TaxCalculationRequest,
  TaxCalculationResponse,
  ManualTaxCalculationRequest
} from '~/types/api'

export function useTax() {
  const api = useApi()

  const loading = ref(false)
  const error = ref<string | null>(null)
  const result = ref<{ tmc: TaxCalculationResponse; annual: TaxCalculationResponse } | null>(null)

  async function calculate(request: TaxCalculationRequest) {
    loading.value = true
    error.value = null
    result.value = null

    try {
      const response = await api.post<{ tmc: TaxCalculationResponse; annual: TaxCalculationResponse }>(
        '/tax/calculate',
        request
      )
      result.value = response
      return response
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function calculateTmc(request: TaxCalculationRequest) {
    loading.value = true
    error.value = null

    try {
      return await api.post<TaxCalculationResponse>('/tax/tmc', request)
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function calculateAnnual(request: TaxCalculationRequest) {
    loading.value = true
    error.value = null

    try {
      return await api.post<TaxCalculationResponse>('/tax/annual', request)
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function calculateManual(request: ManualTaxCalculationRequest) {
    loading.value = true
    error.value = null
    result.value = null

    try {
      const response = await api.post<{ tmc: TaxCalculationResponse; annual: TaxCalculationResponse }>(
        '/tax/manual/calculate',
        request
      )
      result.value = response
      return response
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  function reset() {
    loading.value = false
    error.value = null
    result.value = null
  }

  return {
    loading,
    error,
    result,
    calculate,
    calculateTmc,
    calculateAnnual,
    calculateManual,
    reset
  }
}
