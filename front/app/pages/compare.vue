<script setup lang="ts">
import type { Region, BrandResponse, ModelResponse, VariantResponse, TaxCalculationResponse } from '~/types/api'

const { t, locale } = useI18n()

useSeoMeta({
  title: () => `${t('compare.title')} - ${t('app.name')}`,
  description: () => t('compare.subtitle')
})

const vehicles = useVehicles()
const tax = useTax()

// Form state
const selectedBrand = ref<BrandResponse | undefined>(undefined)
const selectedModel = ref<ModelResponse | undefined>(undefined)
const selectedVariant = ref<VariantResponse | undefined>(undefined)
const registrationYear = ref<number>(new Date().getFullYear())

// Results state
interface RegionResult {
  region: Region
  tmc: TaxCalculationResponse | null
  annual: TaxCalculationResponse | null
  loading: boolean
  error: string | null
}

const results = ref<RegionResult[]>([
  { region: 'wallonia', tmc: null, annual: null, loading: false, error: null },
  { region: 'flanders', tmc: null, annual: null, loading: false, error: null },
  { region: 'brussels', tmc: null, annual: null, loading: false, error: null }
])

const hasResults = computed(() => results.value.some(r => r.tmc !== null))

// Region display config
const regionConfig: Record<Region, { gradient: string; label: string }> = {
  wallonia: { gradient: 'from-red-500 to-red-600', label: 'regions.wallonia' },
  flanders: { gradient: 'from-amber-500 to-amber-600', label: 'regions.flanders' },
  brussels: { gradient: 'from-blue-500 to-blue-600', label: 'regions.brussels' }
}

// Years for select
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 30 }, (_, i) => currentYear - i)
})

// Load brands on mount
onMounted(() => {
  vehicles.fetchBrands()
})

// Watch brand changes -> load models
watch(selectedBrand, (brand) => {
  selectedModel.value = undefined
  selectedVariant.value = undefined
  clearResults()
  vehicles.clearModels()
  vehicles.clearVariants()
  if (brand) {
    vehicles.fetchModels(brand.id)
  }
})

// Watch model changes -> load variants
watch(selectedModel, (model) => {
  selectedVariant.value = undefined
  clearResults()
  vehicles.clearVariants()
  if (model) {
    vehicles.fetchVariants(model.id)
  }
})

// Clear all results
function clearResults() {
  results.value = results.value.map(r => ({
    ...r,
    tmc: null,
    annual: null,
    loading: false,
    error: null
  }))
}

// Check if we can compare
const canCompare = computed(() => {
  return selectedVariant.value && registrationYear.value
})

// Compare taxes across all regions
async function handleCompare() {
  if (!canCompare.value || !selectedVariant.value) return

  // Set all regions to loading
  results.value = results.value.map(r => ({
    ...r,
    loading: true,
    error: null,
    tmc: null,
    annual: null
  }))

  // Calculate for all regions in parallel
  const promises = results.value.map(async (result, index) => {
    try {
      const response = await tax.calculate({
        region: result.region,
        variantId: selectedVariant.value!.id,
        firstRegistrationDate: {
          year: registrationYear.value,
          monthUnknown: true
        }
      })

      results.value[index] = {
        region: result.region,
        tmc: response.tmc,
        annual: response.annual,
        loading: false,
        error: null
      }
    } catch (e: any) {
      results.value[index] = {
        region: result.region,
        tmc: null,
        annual: null,
        loading: false,
        error: e.message || 'Calculation failed'
      }
    }
  })

  await Promise.all(promises)
}

// Format currency
function formatCurrency(amount: number) {
  return new Intl.NumberFormat(locale.value, {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

// Get total for region
function getTotal(result: RegionResult): number {
  const tmcAmount = result.tmc?.isExempt ? 0 : (result.tmc?.amount || 0)
  const annualAmount = result.annual?.isExempt ? 0 : (result.annual?.amount || 0)
  return tmcAmount + annualAmount
}

// Get cheapest region
const cheapestRegion = computed(() => {
  if (!hasResults.value) return null

  const validResults = results.value.filter(r => r.tmc !== null && !r.error)
  if (validResults.length === 0) return null

  return validResults.reduce((min, r) =>
    getTotal(r) < getTotal(min) ? r : min
  ).region
})
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="max-w-2xl mb-12">
        <h1 class="text-3xl font-semibold mb-3">{{ t('compare.title') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('compare.subtitle') }}</p>
      </div>

      <!-- Vehicle Selector -->
      <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6 mb-8">
        <h2 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 uppercase tracking-wide mb-4">
          {{ t('calculator.selectVehicle') }}
        </h2>
        <div class="grid md:grid-cols-4 gap-4">
          <!-- Brand -->
          <div>
            <label class="block text-sm font-medium mb-2">{{ t('vehicle.brand') }}</label>
            <USelectMenu
              v-model="selectedBrand"
              :items="vehicles.brands.value"
              :loading="vehicles.brandsLoading.value"
              placeholder="Marque"
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
              placeholder="Modèle"
              option-attribute="name"
              searchable
              :search-attributes="['name']"
              class="w-full"
            />
          </div>

          <!-- Variant -->
          <div>
            <label class="block text-sm font-medium mb-2">{{ t('vehicle.variant') }}</label>
            <USelectMenu
              v-model="selectedVariant"
              :items="vehicles.variants.value"
              :loading="vehicles.variantsLoading.value"
              :disabled="!selectedModel"
              placeholder="Variante"
              option-attribute="name"
              searchable
              :search-attributes="['name']"
              class="w-full"
            />
          </div>

          <!-- Year -->
          <div>
            <label class="block text-sm font-medium mb-2">{{ t('vehicle.year') }}</label>
            <USelectMenu
              v-model="registrationYear"
              :items="years"
              class="w-full"
            />
          </div>
        </div>

        <!-- Selected variant info -->
        <div
          v-if="selectedVariant"
          class="mt-4 p-4 rounded-lg bg-neutral-50 dark:bg-neutral-800/50 border border-neutral-200 dark:border-neutral-700"
        >
          <div class="flex flex-wrap gap-4 text-sm">
            <span class="text-neutral-500 dark:text-neutral-400">
              {{ t('vehicle.powerKw') }}: <span class="font-medium text-neutral-900 dark:text-white">{{ selectedVariant.powerKw }} kW</span>
            </span>
            <span class="text-neutral-500 dark:text-neutral-400">
              {{ t('vehicle.fiscalHp') }}: <span class="font-medium text-neutral-900 dark:text-white">{{ selectedVariant.fiscalHp }} CV</span>
            </span>
            <span class="text-neutral-500 dark:text-neutral-400">
              {{ t('fuel.title') }}: <span class="font-medium text-neutral-900 dark:text-white">{{ t(`fuel.${selectedVariant.fuel}`) }}</span>
            </span>
            <span class="text-neutral-500 dark:text-neutral-400">
              {{ t('euroNorm.title') }}: <span class="font-medium text-neutral-900 dark:text-white">{{ t(`euroNorm.${selectedVariant.euroNorm}`) }}</span>
            </span>
          </div>
        </div>

        <!-- Compare Button -->
        <div class="mt-6">
          <UButton
            :label="t('compare.compare')"
            size="lg"
            :disabled="!canCompare"
            :loading="results.some(r => r.loading)"
            trailing-icon="i-lucide-git-compare"
            @click="handleCompare"
          />
        </div>
      </div>

      <!-- Comparison Grid -->
      <div class="grid md:grid-cols-3 gap-6">
        <div
          v-for="result in results"
          :key="result.region"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 overflow-hidden"
          :class="{ 'ring-2 ring-green-500': cheapestRegion === result.region }"
        >
          <!-- Region Header -->
          <div
            class="p-6 text-white relative"
            :class="`bg-gradient-to-br ${regionConfig[result.region].gradient}`"
          >
            <h3 class="text-lg font-semibold">{{ t(regionConfig[result.region].label) }}</h3>
            <span
              v-if="cheapestRegion === result.region"
              class="absolute top-4 right-4 text-xs px-2 py-1 rounded-full bg-white/20"
            >
              {{ t('compare.cheapest') }}
            </span>
          </div>

          <!-- Tax Results -->
          <div class="p-6">
            <!-- Loading State -->
            <div v-if="result.loading" class="space-y-6">
              <div class="animate-pulse">
                <div class="h-4 w-16 bg-neutral-200 dark:bg-neutral-700 rounded mb-2"></div>
                <div class="h-8 w-24 bg-neutral-200 dark:bg-neutral-700 rounded"></div>
              </div>
              <div class="animate-pulse">
                <div class="h-4 w-16 bg-neutral-200 dark:bg-neutral-700 rounded mb-2"></div>
                <div class="h-8 w-24 bg-neutral-200 dark:bg-neutral-700 rounded"></div>
              </div>
              <div class="animate-pulse pt-4 border-t border-neutral-200 dark:border-neutral-800">
                <div class="h-4 w-20 bg-neutral-200 dark:bg-neutral-700 rounded mb-2"></div>
                <div class="h-10 w-32 bg-neutral-200 dark:bg-neutral-700 rounded"></div>
              </div>
            </div>

            <!-- Error State -->
            <div v-else-if="result.error" class="text-center py-8">
              <UIcon name="i-lucide-alert-circle" class="w-8 h-8 text-red-400 mx-auto mb-2" />
              <p class="text-sm text-red-500">{{ result.error }}</p>
            </div>

            <!-- Empty State -->
            <div v-else-if="!result.tmc" class="text-center py-8">
              <UIcon name="i-lucide-calculator" class="w-8 h-8 text-neutral-300 dark:text-neutral-600 mx-auto mb-2" />
              <p class="text-sm text-neutral-400">{{ t('compare.selectVehicle') }}</p>
            </div>

            <!-- Results -->
            <template v-else>
              <!-- TMC -->
              <div class="mb-6">
                <div class="flex items-center justify-between mb-1">
                  <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ t('tax.tmc') }}</p>
                  <span
                    v-if="result.tmc.isExempt"
                    class="text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300"
                  >
                    {{ t('tax.exempt') }}
                  </span>
                </div>
                <p class="text-2xl font-bold">
                  {{ result.tmc.isExempt ? '0 EUR' : formatCurrency(result.tmc.amount) }}
                </p>
              </div>

              <!-- Annual Tax -->
              <div class="mb-6">
                <div class="flex items-center justify-between mb-1">
                  <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ t('tax.annual') }}</p>
                  <span
                    v-if="result.annual?.isExempt"
                    class="text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300"
                  >
                    {{ t('tax.exempt') }}
                  </span>
                </div>
                <p class="text-2xl font-bold">
                  {{ result.annual?.isExempt ? '0 EUR' : formatCurrency(result.annual?.amount || 0) }}
                </p>
                <span class="text-xs text-neutral-400">{{ t('tax.perYear') }}</span>
              </div>

              <!-- Total First Year -->
              <div class="pt-4 border-t border-neutral-200 dark:border-neutral-800">
                <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-1">{{ t('tax.totalFirstYear') }}</p>
                <p
                  class="text-3xl font-bold"
                  :class="cheapestRegion === result.region ? 'text-green-600 dark:text-green-400' : ''"
                >
                  {{ formatCurrency(getTotal(result)) }}
                </p>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div
        v-if="!hasResults"
        class="text-center py-12 text-neutral-400 dark:text-neutral-500"
      >
        <UIcon name="i-lucide-git-compare" class="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p>{{ t('compare.selectVehicle') }}</p>
      </div>
    </UContainer>
  </div>
</template>
