<script setup lang="ts">
import type { FuelType, EuroNorm, BrandResponse, ModelResponse, VariantResponse } from '~/types/api'

const { t } = useI18n()

useSeoMeta({
  title: () => `${t('search.title')} - ${t('app.name')}`,
  description: () => t('app.description')
})

const vehicles = useVehicles()

// Search state
const searchQuery = ref('')
const selectedBrand = ref<BrandResponse | undefined>(undefined)
const selectedModel = ref<ModelResponse | undefined>(undefined)

// Filter state
const selectedFuelTypes = ref<FuelType[]>([])
const selectedEuroNorms = ref<EuroNorm[]>([])
const minPower = ref<number | undefined>(undefined)
const maxPower = ref<number | undefined>(undefined)

// Results state
const searchResults = ref<VariantResponse[]>([])
const loading = ref(false)
const hasSearched = ref(false)

// Available filter options
const fuelTypes: FuelType[] = ['petrol', 'diesel', 'electric', 'hybrid_petrol', 'hybrid_diesel', 'lpg', 'cng']
const euroNorms: EuroNorm[] = ['euro_7', 'euro_6d', 'euro_6', 'euro_5', 'euro_4', 'euro_3']

// Load brands on mount
onMounted(() => {
  vehicles.fetchBrands()
})

// Watch brand changes -> load models
watch(selectedBrand, (brand) => {
  selectedModel.value = undefined
  searchResults.value = []
  vehicles.clearModels()
  vehicles.clearVariants()
  if (brand) {
    vehicles.fetchModels(brand.id)
  }
})

// Watch model changes -> load variants (search results)
watch(selectedModel, (model) => {
  if (model) {
    performSearch()
  }
})

// Perform search
async function performSearch() {
  if (!selectedModel.value) return

  loading.value = true
  hasSearched.value = true

  try {
    await vehicles.fetchVariants(selectedModel.value.id)
    searchResults.value = filterVariants(vehicles.variants.value)
  } finally {
    loading.value = false
  }
}

// Filter variants based on selected filters
function filterVariants(variants: VariantResponse[]): VariantResponse[] {
  return variants.filter(v => {
    // Filter by fuel type
    if (selectedFuelTypes.value.length > 0 && !selectedFuelTypes.value.includes(v.fuel)) {
      return false
    }
    // Filter by euro norm
    if (selectedEuroNorms.value.length > 0 && !selectedEuroNorms.value.includes(v.euroNorm)) {
      return false
    }
    // Filter by power
    if (minPower.value && v.powerKw < minPower.value) {
      return false
    }
    if (maxPower.value && v.powerKw > maxPower.value) {
      return false
    }
    return true
  })
}

// Re-filter when filters change
watch([selectedFuelTypes, selectedEuroNorms, minPower, maxPower], () => {
  if (hasSearched.value && vehicles.variants.value.length > 0) {
    searchResults.value = filterVariants(vehicles.variants.value)
  }
}, { deep: true })

// Toggle fuel type filter
function toggleFuelType(fuel: FuelType) {
  const index = selectedFuelTypes.value.indexOf(fuel)
  if (index === -1) {
    selectedFuelTypes.value.push(fuel)
  } else {
    selectedFuelTypes.value.splice(index, 1)
  }
}

// Toggle euro norm filter
function toggleEuroNorm(norm: EuroNorm) {
  const index = selectedEuroNorms.value.indexOf(norm)
  if (index === -1) {
    selectedEuroNorms.value.push(norm)
  } else {
    selectedEuroNorms.value.splice(index, 1)
  }
}

// Clear all filters
function clearFilters() {
  selectedFuelTypes.value = []
  selectedEuroNorms.value = []
  minPower.value = undefined
  maxPower.value = undefined
}

// Check if any filters are active
const hasActiveFilters = computed(() => {
  return selectedFuelTypes.value.length > 0 ||
    selectedEuroNorms.value.length > 0 ||
    minPower.value !== undefined ||
    maxPower.value !== undefined
})
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="max-w-2xl mb-8">
        <h1 class="text-3xl font-semibold mb-3">{{ t('search.title') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('app.description') }}</p>
      </div>

      <!-- Vehicle Selection -->
      <div class="mb-8">
        <div class="grid md:grid-cols-2 gap-4">
          <!-- Brand -->
          <div>
            <label class="block text-sm font-medium mb-2">{{ t('vehicle.brand') }}</label>
            <USelectMenu
              v-model="selectedBrand"
              :items="vehicles.brands.value"
              :loading="vehicles.brandsLoading.value"
              :placeholder="t('search.selectBrand')"
              option-attribute="name"
              searchable
              :search-attributes="['name']"
              class="w-full"
            />
          </div>

          <!-- Model -->
          <div>
            <label class="block text-sm font-medium mb-2">{{ t('vehicle.model') }}</label>
            <USelectMenu
              v-model="selectedModel"
              :items="vehicles.models.value"
              :loading="vehicles.modelsLoading.value"
              :disabled="!selectedBrand"
              :placeholder="t('search.selectModel')"
              option-attribute="name"
              searchable
              :search-attributes="['name']"
              class="w-full"
            />
          </div>
        </div>
      </div>

      <div class="grid lg:grid-cols-4 gap-8">
        <!-- Filters Sidebar -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-5">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-medium">{{ t('search.filters') }}</h2>
              <button
                v-if="hasActiveFilters"
                class="text-sm text-primary-600 dark:text-primary-400 hover:underline"
                @click="clearFilters"
              >
                {{ t('search.clearFilters') }}
              </button>
            </div>

            <!-- Filter: Fuel Type -->
            <div class="mb-6">
              <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-3">
                {{ t('fuel.title') }}
              </h3>
              <div class="space-y-2">
                <label
                  v-for="fuel in fuelTypes"
                  :key="fuel"
                  class="flex items-center gap-2 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    class="rounded border-neutral-300 text-primary-600 focus:ring-primary-500"
                    :checked="selectedFuelTypes.includes(fuel)"
                    @change="toggleFuelType(fuel)"
                  >
                  <span class="text-sm">{{ t(`fuel.${fuel}`) }}</span>
                </label>
              </div>
            </div>

            <!-- Filter: Euro Norm -->
            <div class="mb-6">
              <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-3">
                {{ t('euroNorm.title') }}
              </h3>
              <div class="space-y-2">
                <label
                  v-for="norm in euroNorms"
                  :key="norm"
                  class="flex items-center gap-2 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    class="rounded border-neutral-300 text-primary-600 focus:ring-primary-500"
                    :checked="selectedEuroNorms.includes(norm)"
                    @change="toggleEuroNorm(norm)"
                  >
                  <span class="text-sm">{{ t(`euroNorm.${norm}`) }}</span>
                </label>
              </div>
            </div>

            <!-- Filter: Power -->
            <div>
              <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-3">
                {{ t('vehicle.power') }} (kW)
              </h3>
              <div class="flex gap-2">
                <UInput
                  v-model="minPower"
                  type="number"
                  placeholder="Min"
                  class="flex-1"
                />
                <UInput
                  v-model="maxPower"
                  type="number"
                  placeholder="Max"
                  class="flex-1"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Results -->
        <div class="lg:col-span-3">
          <div class="flex items-center justify-between mb-4">
            <p class="text-sm text-neutral-500">
              {{ t('search.results', { count: searchResults.length }) }}
            </p>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="space-y-4">
            <div
              v-for="i in 3"
              :key="i"
              class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
            >
              <div class="animate-pulse">
                <div class="h-5 bg-neutral-200 dark:bg-neutral-700 rounded w-1/3 mb-3"></div>
                <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-2/3 mb-2"></div>
                <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-1/2"></div>
              </div>
            </div>
          </div>

          <!-- Empty State - No search yet -->
          <div
            v-else-if="!hasSearched"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-12 text-center"
          >
            <UIcon name="i-lucide-car" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
            <p class="text-neutral-500 dark:text-neutral-400 mb-4">{{ t('search.selectToStart') }}</p>
            <p class="text-sm text-neutral-400">{{ t('search.placeholder') }}</p>
          </div>

          <!-- Empty State - No results -->
          <div
            v-else-if="searchResults.length === 0"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-12 text-center"
          >
            <UIcon name="i-lucide-search-x" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
            <p class="text-neutral-500 dark:text-neutral-400 mb-4">{{ t('search.noResults') }}</p>
            <UButton
              v-if="hasActiveFilters"
              :label="t('search.clearFilters')"
              variant="outline"
              @click="clearFilters"
            />
          </div>

          <!-- Results List -->
          <div v-else class="space-y-4">
            <NuxtLink
              v-for="variant in searchResults"
              :key="variant.id"
              :to="`/calculator?variantId=${variant.id}`"
              class="block bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6 hover:border-primary-300 dark:hover:border-primary-700 hover:shadow-md transition-all"
            >
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-semibold text-lg mb-2">{{ variant.name }}</h3>
                  <div class="flex flex-wrap gap-2 text-sm text-neutral-500 dark:text-neutral-400">
                    <span class="flex items-center gap-1">
                      <UIcon name="i-lucide-zap" class="w-4 h-4" />
                      {{ variant.powerKw }} kW ({{ variant.fiscalHp }} CV)
                    </span>
                    <span class="flex items-center gap-1">
                      <UIcon name="i-lucide-fuel" class="w-4 h-4" />
                      {{ t(`fuel.${variant.fuel}`) }}
                    </span>
                    <span class="flex items-center gap-1">
                      <UIcon name="i-lucide-shield-check" class="w-4 h-4" />
                      {{ t(`euroNorm.${variant.euroNorm}`) }}
                    </span>
                    <span v-if="variant.co2Wltp" class="flex items-center gap-1">
                      <UIcon name="i-lucide-cloud" class="w-4 h-4" />
                      {{ variant.co2Wltp }} g/km
                    </span>
                  </div>
                  <p class="text-xs text-neutral-400 mt-2">
                    {{ variant.yearStart }} - {{ variant.yearEnd || t('common.present') }}
                  </p>
                </div>
                <UIcon name="i-lucide-chevron-right" class="w-5 h-5 text-neutral-400" />
              </div>
            </NuxtLink>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
