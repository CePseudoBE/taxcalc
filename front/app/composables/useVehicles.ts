import type { BrandResponse, ModelResponse, VariantResponse, VariantDetailResponse } from '~/types/api'

export function useVehicles() {
  const api = useApi()

  // Brands
  const brands = ref<BrandResponse[]>([])
  const brandsLoading = ref(false)
  const brandsError = ref<string | null>(null)

  async function fetchBrands() {
    brandsLoading.value = true
    brandsError.value = null
    try {
      brands.value = await api.get<BrandResponse[]>('/brands')
    } catch (e: any) {
      brandsError.value = e.message
    } finally {
      brandsLoading.value = false
    }
  }

  // Models
  const models = ref<ModelResponse[]>([])
  const modelsLoading = ref(false)

  async function fetchModels(brandId: number, search?: string) {
    modelsLoading.value = true
    try {
      const query = search ? `?search=${encodeURIComponent(search)}` : ''
      models.value = await api.get<ModelResponse[]>(`/brands/${brandId}/models${query}`)
    } catch (e: any) {
      models.value = []
    } finally {
      modelsLoading.value = false
    }
  }

  function clearModels() {
    models.value = []
  }

  // Variants
  const variants = ref<VariantResponse[]>([])
  const variantsLoading = ref(false)

  async function fetchVariants(modelId: number) {
    variantsLoading.value = true
    try {
      variants.value = await api.get<VariantResponse[]>(`/models/${modelId}/variants`)
    } catch (e: any) {
      variants.value = []
    } finally {
      variantsLoading.value = false
    }
  }

  function clearVariants() {
    variants.value = []
  }

  // Variant detail
  async function fetchVariantDetail(variantId: number): Promise<VariantDetailResponse | null> {
    try {
      return await api.get<VariantDetailResponse>(`/variants/${variantId}`)
    } catch {
      return null
    }
  }

  return {
    // Brands
    brands,
    brandsLoading,
    brandsError,
    fetchBrands,
    // Models
    models,
    modelsLoading,
    fetchModels,
    clearModels,
    // Variants
    variants,
    variantsLoading,
    fetchVariants,
    clearVariants,
    // Detail
    fetchVariantDetail
  }
}
