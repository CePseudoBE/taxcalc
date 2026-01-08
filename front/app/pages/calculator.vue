<script setup lang="ts">
import type { Region, FuelType, EuroNorm, BrandResponse, ModelResponse, VariantResponse } from '~/types/api'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuth()
const savedSearches = useSavedSearches()

useSeoMeta({
  title: () => `${t('calculator.title')} - ${t('app.name')}`,
  description: () => t('calculator.subtitle')
})

const vehicles = useVehicles()
const tax = useTax()

// Input mode: 'manual' (direct specs) or 'catalog' (select from database)
// Default to catalog - manual is fallback if vehicle not in catalog
const inputMode = ref<'manual' | 'catalog'>('catalog')

// Pre-select region from URL query parameter
const regionFromQuery = computed(() => {
  const region = route.query.region as string
  if (region && ['wallonia', 'flanders', 'brussels'].includes(region)) {
    return region as Region
  }
  return undefined
})

// Form state - common
const selectedRegion = ref<Region | undefined>(regionFromQuery.value)
const registrationYear = ref<number>(new Date().getFullYear())
const registrationMonth = ref<MonthOption | undefined>(undefined)
const monthUnknown = ref(false)

// Form state - catalog mode
const selectedBrand = ref<BrandResponse | undefined>(undefined)
const selectedModel = ref<ModelResponse | undefined>(undefined)
const selectedVariant = ref<VariantResponse | undefined>(undefined)

// Form state - manual mode
const manualBrandName = ref<string>('')
const manualModelName = ref<string>('')
const manualVariantName = ref<string>('')
const manualFiscalHp = ref<number | undefined>(undefined)
const manualDisplacementCc = ref<number | undefined>(undefined)
const manualPowerKw = ref<number | undefined>(undefined)
const manualFuelOption = ref<{ value: FuelType; label: string } | undefined>(undefined)
const manualEuroNormOption = ref<{ value: EuroNorm; label: string } | undefined>(undefined)
const manualCo2Wltp = ref<number | undefined>(undefined)
const manualMmaKg = ref<number | undefined>(undefined)

// Computed values for fuel and euroNorm (extract value from option object)
const manualFuel = computed(() => manualFuelOption.value?.value)
const manualEuroNorm = computed(() => manualEuroNormOption.value?.value)

// Submissions composable for logged-in users
const submissions = useSubmissions()

// Fields visibility based on selected region
const showPowerKw = computed(() => {
  // Required for Brussels (max of fiscal HP / kW) and Wallonia (base amount)
  return !selectedRegion.value || selectedRegion.value === 'brussels' || selectedRegion.value === 'wallonia'
})

const showCo2 = computed(() => {
  // Required for Wallonia (CO2 factor) and Flanders (BIV formula)
  return !selectedRegion.value || selectedRegion.value === 'wallonia' || selectedRegion.value === 'flanders'
})

const showEuroNorm = computed(() => {
  // Required for Flanders (luchtcomponent)
  return !selectedRegion.value || selectedRegion.value === 'flanders'
})

const showMma = computed(() => {
  // Only for Wallonia (MMA factor)
  return !selectedRegion.value || selectedRegion.value === 'wallonia'
})

// Regions data
const regions: { value: Region; label: string; color: string }[] = [
  { value: 'wallonia', label: 'regions.wallonia', color: 'red' },
  { value: 'flanders', label: 'regions.flanders', color: 'amber' },
  { value: 'brussels', label: 'regions.brussels', color: 'blue' }
]

// Fuel types (computed to get translated labels)
const fuelTypes = computed(() => [
  { value: 'petrol', label: t('fuel.petrol') },
  { value: 'diesel', label: t('fuel.diesel') },
  { value: 'electric', label: t('fuel.electric') },
  { value: 'hybrid_petrol', label: t('fuel.hybrid_petrol') },
  { value: 'hybrid_diesel', label: t('fuel.hybrid_diesel') },
  { value: 'plug_in_hybrid_petrol', label: t('fuel.plug_in_hybrid_petrol') },
  { value: 'plug_in_hybrid_diesel', label: t('fuel.plug_in_hybrid_diesel') },
  { value: 'lpg', label: t('fuel.lpg') },
  { value: 'cng', label: t('fuel.cng') },
  { value: 'hydrogen', label: t('fuel.hydrogen') }
])

// Euro norms
const euroNorms: { value: EuroNorm; label: string }[] = [
  { value: 'euro_7', label: 'Euro 7' },
  { value: 'euro_6d', label: 'Euro 6d' },
  { value: 'euro_6d_temp', label: 'Euro 6d-TEMP' },
  { value: 'euro_6', label: 'Euro 6' },
  { value: 'euro_5', label: 'Euro 5' },
  { value: 'euro_4', label: 'Euro 4' },
  { value: 'euro_3', label: 'Euro 3' },
  { value: 'euro_2', label: 'Euro 2' },
  { value: 'euro_1', label: 'Euro 1' }
]

// Years for select
const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 30 }, (_, i) => currentYear - i)
})

// Months for select
interface MonthOption {
  value: number
  label: string
}
const months = computed<MonthOption[]>(() => {
  return Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: t(`months.${i + 1}`)
  }))
})

// Load brands on mount (for catalog mode)
onMounted(() => {
  vehicles.fetchBrands()
})

// Watch brand changes -> load models
watch(selectedBrand, (brand) => {
  selectedModel.value = undefined
  selectedVariant.value = undefined
  vehicles.clearModels()
  vehicles.clearVariants()
  if (brand) {
    vehicles.fetchModels(brand.id)
  }
})

// Watch model changes -> load variants
watch(selectedModel, (model) => {
  selectedVariant.value = undefined
  vehicles.clearVariants()
  if (model) {
    vehicles.fetchVariants(model.id)
  }
})

// Reset results when switching mode
watch(inputMode, () => {
  tax.reset()
})

// Form validation
const canCalculate = computed(() => {
  const hasRegion = !!selectedRegion.value
  const hasDate = registrationYear.value && (registrationMonth.value || monthUnknown.value)

  if (inputMode.value === 'catalog') {
    return hasRegion && hasDate && !!selectedVariant.value
  } else {
    // Manual mode: (fiscalHp OR displacementCc) + fuel required
    const hasFiscalHpOrDisplacement = !!manualFiscalHp.value || !!manualDisplacementCc.value
    const hasBasicSpecs = hasFiscalHpOrDisplacement && !!manualFuel.value

    // Logged-in users must also provide brand/model/variant for catalog submission
    if (auth.isAuthenticated.value) {
      const hasVehicleInfo = manualBrandName.value.trim() && manualModelName.value.trim() && manualVariantName.value.trim()
      return hasRegion && hasDate && hasBasicSpecs && hasVehicleInfo
    }

    return hasRegion && hasDate && hasBasicSpecs
  }
})

// State for submission feedback
const submissionSuccess = ref(false)
const submissionError = ref<string | null>(null)

// Calculate
async function handleCalculate() {
  if (!canCalculate.value || !selectedRegion.value) return

  submissionSuccess.value = false
  submissionError.value = null

  if (inputMode.value === 'catalog' && selectedVariant.value) {
    await tax.calculate({
      region: selectedRegion.value,
      variantId: selectedVariant.value.id,
      firstRegistrationDate: {
        year: registrationYear.value,
        month: monthUnknown.value ? undefined : registrationMonth.value?.value,
        monthUnknown: monthUnknown.value
      }
    })
  } else if (inputMode.value === 'manual' && (manualFiscalHp.value || manualDisplacementCc.value) && manualFuel.value) {
    // Calculate taxes
    await tax.calculate({
      region: selectedRegion.value,
      fiscalHp: manualFiscalHp.value,
      displacementCc: manualDisplacementCc.value,
      powerKw: manualPowerKw.value,
      fuel: manualFuel.value,
      euroNorm: manualEuroNorm.value,
      co2Wltp: manualCo2Wltp.value,
      mmaKg: manualMmaKg.value,
      firstRegistrationDate: {
        year: registrationYear.value,
        month: monthUnknown.value ? undefined : registrationMonth.value?.value,
        monthUnknown: monthUnknown.value
      }
    })

    // If logged in, also submit to catalog for moderation
    if (auth.isAuthenticated.value && manualBrandName.value && manualModelName.value && manualVariantName.value) {
      try {
        await submissions.createSubmission({
          brandName: manualBrandName.value.trim(),
          modelName: manualModelName.value.trim(),
          variantName: manualVariantName.value.trim(),
          yearStart: registrationYear.value,
          powerKw: manualPowerKw.value || 0,
          fiscalHp: manualFiscalHp.value,
          fuel: manualFuel.value,
          euroNorm: manualEuroNorm.value || 'euro_6',
          co2Wltp: manualCo2Wltp.value,
          mmaKg: manualMmaKg.value
        })
        submissionSuccess.value = true
      } catch (e: any) {
        submissionError.value = e.message || t('common.error')
      }
    }
  }
}

// Format currency using current locale
function formatCurrency(amount: number) {
  return new Intl.NumberFormat(locale.value, {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2
  }).format(amount)
}

// Save search functionality (only for catalog mode)
const saveModalOpen = ref(false)
const saveLabel = ref('')
const savingSearch = ref(false)
const saveError = ref<string | null>(null)
const saveSuccess = ref(false)

function openSaveModal() {
  if (!auth.isAuthenticated.value) {
    router.push('/auth/login?redirect=/calculator')
    return
  }
  if (inputMode.value === 'manual') {
    // Can't save manual searches
    return
  }
  saveLabel.value = selectedBrand.value && selectedModel.value
    ? `${selectedBrand.value.name} ${selectedModel.value.name}`
    : ''
  saveError.value = null
  saveSuccess.value = false
  saveModalOpen.value = true
}

async function handleSaveSearch() {
  if (!selectedVariant.value || !selectedRegion.value) return

  savingSearch.value = true
  saveError.value = null

  try {
    await savedSearches.saveSearch({
      variantId: selectedVariant.value.id,
      region: selectedRegion.value,
      firstRegistrationDate: {
        year: registrationYear.value,
        month: monthUnknown.value ? undefined : registrationMonth.value?.value,
        monthUnknown: monthUnknown.value
      },
      label: saveLabel.value || `${selectedBrand.value?.name} ${selectedModel.value?.name}`
    })
    saveSuccess.value = true
    setTimeout(() => {
      saveModalOpen.value = false
      saveSuccess.value = false
    }, 1500)
  } catch (e: any) {
    saveError.value = e.data?.message || e.message || t('common.error')
  } finally {
    savingSearch.value = false
  }
}

// Share functionality
const shareSuccess = ref(false)

async function handleShare() {
  if (!selectedRegion.value) return

  const url = new URL(window.location.href)
  url.searchParams.set('region', selectedRegion.value)

  if (inputMode.value === 'catalog' && selectedVariant.value) {
    url.searchParams.set('variantId', selectedVariant.value.id.toString())
  } else if (inputMode.value === 'manual') {
    if (manualFiscalHp.value) url.searchParams.set('fiscalHp', manualFiscalHp.value.toString())
    if (manualFuel.value) url.searchParams.set('fuel', manualFuel.value)
    if (manualPowerKw.value) url.searchParams.set('powerKw', manualPowerKw.value.toString())
    if (manualCo2Wltp.value) url.searchParams.set('co2', manualCo2Wltp.value.toString())
  }

  url.searchParams.set('year', registrationYear.value.toString())
  if (registrationMonth.value && !monthUnknown.value) {
    url.searchParams.set('month', registrationMonth.value.value.toString())
  }

  try {
    if (navigator.share) {
      await navigator.share({
        title: t('calculator.shareTitle'),
        text: inputMode.value === 'catalog' && selectedBrand.value
          ? `${selectedBrand.value.name} ${selectedModel.value?.name} - ${t(`regions.${selectedRegion.value}`)}`
          : `${manualFiscalHp.value} CV ${t(`fuel.${manualFuel.value}`)} - ${t(`regions.${selectedRegion.value}`)}`,
        url: url.toString()
      })
    } else {
      await navigator.clipboard.writeText(url.toString())
      shareSuccess.value = true
      setTimeout(() => {
        shareSuccess.value = false
      }, 2000)
    }
  } catch (e) {
    // User cancelled share or clipboard failed
  }
}
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="max-w-2xl mb-12">
        <h1 class="text-3xl font-bold mb-3">{{ t('calculator.title') }}</h1>
        <p class="text-stone-500 dark:text-stone-400">{{ t('calculator.subtitle') }}</p>
      </div>

      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Calculator Form -->
        <div class="lg:col-span-2 space-y-8">
          <!-- Region Selection -->
          <div class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-4">
              {{ t('regions.title') }}
            </h2>
            <div class="grid grid-cols-3 gap-3">
              <button
                v-for="region in regions"
                :key="region.value"
                class="relative p-4 rounded-xl border-2 transition-all duration-200 text-center"
                :class="selectedRegion === region.value
                  ? 'border-orange-500 bg-orange-50 dark:bg-orange-900/20'
                  : 'border-stone-200 dark:border-stone-700 hover:border-stone-300 dark:hover:border-stone-600'"
                @click="selectedRegion = region.value"
              >
                <div
                  v-if="selectedRegion === region.value"
                  class="absolute top-2 right-2 w-2 h-2 rounded-full bg-orange-500"
                />
                <span class="font-medium">{{ t(region.label) }}</span>
              </button>
            </div>
          </div>

          <!-- Input Mode Toggle -->
          <div class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-4">
              {{ t('calculator.inputMode') }}
            </h2>
            <div class="flex gap-2">
              <UButton
                :label="t('calculator.fromCatalog')"
                :variant="inputMode === 'catalog' ? 'solid' : 'outline'"
                :color="inputMode === 'catalog' ? 'primary' : 'neutral'"
                @click="inputMode = 'catalog'"
              />
              <UButton
                :label="t('calculator.manualEntry')"
                :variant="inputMode === 'manual' ? 'solid' : 'outline'"
                :color="inputMode === 'manual' ? 'primary' : 'neutral'"
                @click="inputMode = 'manual'"
              />
            </div>
          </div>

          <!-- Manual Entry Form -->
          <div v-if="inputMode === 'manual'" class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-4">
              {{ t('calculator.vehicleSpecs') }}
            </h2>

            <!-- Vehicle identification fields (only for logged-in users) -->
            <div v-if="auth.isAuthenticated.value" class="grid md:grid-cols-3 gap-4 mb-6 pb-6 border-b border-stone-200 dark:border-stone-700">
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('vehicle.brand') }} <span class="text-red-500">*</span>
                </label>
                <UInput
                  v-model="manualBrandName"
                  :placeholder="t('vehicle.brandPlaceholder')"
                  class="w-full"
                />
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('vehicle.model') }} <span class="text-red-500">*</span>
                </label>
                <UInput
                  v-model="manualModelName"
                  :placeholder="t('vehicle.modelPlaceholder')"
                  class="w-full"
                />
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('vehicle.variant') }} <span class="text-red-500">*</span>
                </label>
                <UInput
                  v-model="manualVariantName"
                  :placeholder="t('vehicle.variantPlaceholder')"
                  class="w-full"
                />
              </div>
              <div class="md:col-span-3">
                <p class="text-xs text-blue-600 dark:text-blue-400">
                  {{ t('calculator.loggedInSubmissionNotice') }}
                </p>
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-4">
              <!-- Fiscal HP (required, OR displacement) -->
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('vehicle.fiscalHp') }} <span v-if="!manualDisplacementCc" class="text-red-500">*</span>
                </label>
                <UInput
                  v-model.number="manualFiscalHp"
                  type="number"
                  :placeholder="t('vehicle.fiscalHpPlaceholder')"
                  min="1"
                  max="100"
                  class="w-full"
                />
                <p class="text-xs text-stone-500 mt-1">{{ t('vehicle.fiscalHpHelp') }}</p>
              </div>

              <!-- Displacement (alternative to fiscal HP) -->
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('vehicle.displacementCc') }} <span v-if="!manualFiscalHp" class="text-red-500">*</span>
                </label>
                <UInput
                  v-model.number="manualDisplacementCc"
                  type="number"
                  :placeholder="t('vehicle.displacementCcPlaceholder')"
                  min="50"
                  max="10000"
                  class="w-full"
                />
                <p class="text-xs text-stone-500 mt-1">{{ t('vehicle.displacementCcHelp') }}</p>
              </div>

              <!-- Fuel Type (required) -->
              <div>
                <label class="block text-sm font-medium mb-2">
                  {{ t('fuel.title') }} <span class="text-red-500">*</span>
                </label>
                <USelectMenu
                  v-model="manualFuelOption"
                  :items="fuelTypes"
                  option-attribute="label"
                  :placeholder="t('fuel.select')"
                  class="w-full"
                />
              </div>

              <!-- Power kW (Brussels, Wallonia) -->
              <div v-if="showPowerKw">
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.powerKw') }}</label>
                <UInput
                  v-model.number="manualPowerKw"
                  type="number"
                  :placeholder="t('vehicle.powerKwPlaceholder')"
                  min="1"
                  max="1000"
                  class="w-full"
                />
              </div>

              <!-- CO2 WLTP (Wallonia, Flanders) -->
              <div v-if="showCo2">
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.co2Wltp') }}</label>
                <UInput
                  v-model.number="manualCo2Wltp"
                  type="number"
                  :placeholder="t('vehicle.co2WltpPlaceholder')"
                  min="0"
                  max="500"
                  class="w-full"
                />
              </div>

              <!-- Euro Norm (Flanders only) -->
              <div v-if="showEuroNorm">
                <label class="block text-sm font-medium mb-2">{{ t('euroNorm.title') }}</label>
                <USelectMenu
                  v-model="manualEuroNormOption"
                  :items="euroNorms"
                  option-attribute="label"
                  :placeholder="t('euroNorm.select')"
                  class="w-full"
                />
              </div>

              <!-- MMA (Wallonia only) -->
              <div v-if="showMma">
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.mmaKg') }}</label>
                <UInput
                  v-model.number="manualMmaKg"
                  type="number"
                  :placeholder="t('vehicle.mmaKgPlaceholder')"
                  min="500"
                  max="10000"
                  class="w-full"
                />
              </div>
            </div>
          </div>

          <!-- Catalog Selection Form -->
          <div v-else class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-4">
              {{ t('calculator.selectVehicle') }}
            </h2>

            <div class="space-y-4">
              <!-- Brand -->
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.brand') }}</label>
                <USelectMenu
                  v-model="selectedBrand"
                  :items="vehicles.brands.value"
                  :loading="vehicles.brandsLoading.value"
                  placeholder="Sélectionnez une marque"
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
                  placeholder="Sélectionnez un modèle"
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
                  placeholder="Sélectionnez une variante"
                  option-attribute="name"
                  searchable
                  :search-attributes="['name']"
                  class="w-full"
                >
                  <template #item="{ item }">
                    <div class="flex flex-col">
                      <span>{{ item.name }}</span>
                      <span class="text-xs text-stone-500">
                        {{ item.powerKw }}kW · {{ item.fiscalHp }}CV
                        <template v-if="item.displacementCc"> · {{ item.displacementCc }}cm³</template>
                        · {{ t(`fuel.${item.fuel}`) }}
                      </span>
                    </div>
                  </template>
                </USelectMenu>
              </div>

              <!-- Selected variant info -->
              <div
                v-if="selectedVariant"
                class="p-4 rounded-lg bg-stone-50 dark:bg-stone-800/50 border border-stone-200 dark:border-stone-700"
              >
                <div class="grid grid-cols-2 md:grid-cols-5 gap-4 text-sm">
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('vehicle.powerKw') }}</span>
                    <p class="font-medium">{{ selectedVariant.powerKw }} kW</p>
                  </div>
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('vehicle.fiscalHp') }}</span>
                    <p class="font-medium">{{ selectedVariant.fiscalHp }} CV</p>
                  </div>
                  <div v-if="selectedVariant.displacementCc">
                    <span class="text-stone-500 dark:text-stone-400">{{ t('vehicle.displacementCc') }}</span>
                    <p class="font-medium">{{ selectedVariant.displacementCc }} cm³</p>
                  </div>
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('fuel.title') }}</span>
                    <p class="font-medium">{{ t(`fuel.${selectedVariant.fuel}`) }}</p>
                  </div>
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('euroNorm.title') }}</span>
                    <p class="font-medium">{{ t(`euroNorm.${selectedVariant.euroNorm}`) }}</p>
                  </div>
                </div>
              </div>

              <!-- Empty catalog notice -->
              <div
                v-if="vehicles.brands.value.length === 0 && !vehicles.brandsLoading.value"
                class="p-4 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800"
              >
                <p class="text-sm text-amber-700 dark:text-amber-300">
                  {{ t('calculator.noCatalogData') }}
                </p>
              </div>
            </div>
          </div>

          <!-- Registration Date -->
          <div class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-4">
              {{ t('vehicle.firstRegistration') }}
            </h2>

            <div class="grid md:grid-cols-2 gap-4">
              <!-- Year -->
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.year') }}</label>
                <USelectMenu
                  v-model="registrationYear"
                  :items="years"
                  class="w-full"
                />
              </div>

              <!-- Month -->
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.month') }}</label>
                <USelectMenu
                  v-model="registrationMonth"
                  :items="months"
                  :disabled="monthUnknown"
                  :placeholder="t('vehicle.selectMonth')"
                  option-attribute="label"
                  class="w-full"
                />
              </div>
            </div>

            <!-- Month unknown checkbox -->
            <label class="flex items-center gap-2 mt-4 cursor-pointer">
              <UCheckbox v-model="monthUnknown" />
              <span class="text-sm text-stone-600 dark:text-stone-400">{{ t('vehicle.monthUnknown') }}</span>
            </label>
          </div>

          <!-- Calculate Button -->
          <UButton
            size="lg"
            block
            :loading="tax.loading.value"
            :disabled="!canCalculate"
            class="justify-center"
            @click="handleCalculate"
          >
            <UIcon name="i-lucide-calculator" class="w-5 h-5 mr-2" />
            {{ tax.loading.value ? t('tax.calculating') : t('tax.calculate') }}
          </UButton>

          <!-- Error -->
          <div
            v-if="tax.error.value"
            class="p-4 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300"
          >
            {{ tax.error.value }}
          </div>
        </div>

        <!-- Results Panel -->
        <div class="lg:col-span-1">
          <div class="bg-stone-50 dark:bg-stone-900/50 rounded-xl border border-stone-200 dark:border-stone-800 p-6 sticky top-24">
            <h2 class="text-sm font-medium text-stone-500 dark:text-stone-400 uppercase tracking-wide mb-6">
              {{ t('tax.results') }}
            </h2>

            <!-- Empty state -->
            <div v-if="!tax.result.value" class="text-center py-12">
              <UIcon name="i-lucide-calculator" class="w-12 h-12 text-stone-300 dark:text-stone-700 mx-auto mb-4" />
              <p class="text-stone-400 dark:text-stone-500">
                {{ t('calculator.fillForm') }}
              </p>
            </div>

            <!-- Results -->
            <div v-else class="space-y-6">
              <!-- TMC -->
              <div>
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-stone-500 dark:text-stone-400">{{ t('tax.tmc') }}</span>
                  <span
                    v-if="tax.result.value.tmc.isExempt"
                    class="text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300"
                  >
                    {{ t('tax.exempt') }}
                  </span>
                </div>
                <p class="text-3xl font-bold text-stone-900 dark:text-white">
                  {{ tax.result.value.tmc.isExempt ? '0 €' : formatCurrency(tax.result.value.tmc.amount) }}
                </p>
                <p v-if="tax.result.value.tmc.exemptionReason" class="text-sm text-green-600 dark:text-green-400 mt-1">
                  {{ tax.result.value.tmc.exemptionReason }}
                </p>
              </div>

              <hr class="border-stone-200 dark:border-stone-700">

              <!-- Annual -->
              <div>
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm text-stone-500 dark:text-stone-400">{{ t('tax.annual') }}</span>
                  <span
                    v-if="tax.result.value.annual.isExempt"
                    class="text-xs px-2 py-0.5 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300"
                  >
                    {{ t('tax.exempt') }}
                  </span>
                </div>
                <p class="text-3xl font-bold text-stone-900 dark:text-white">
                  {{ tax.result.value.annual.isExempt ? '0 €' : formatCurrency(tax.result.value.annual.amount) }}
                </p>
                <span class="text-sm text-stone-500">{{ t('tax.perYear') }}</span>
                <p v-if="tax.result.value.annual.exemptionReason" class="text-sm text-green-600 dark:text-green-400 mt-1">
                  {{ tax.result.value.annual.exemptionReason }}
                </p>
              </div>

              <hr class="border-stone-200 dark:border-stone-700">

              <!-- Total first year -->
              <div class="p-4 rounded-lg bg-orange-50 dark:bg-orange-900/20 border border-orange-200 dark:border-orange-800">
                <span class="text-sm text-orange-700 dark:text-orange-300">{{ t('tax.totalFirstYear') }}</span>
                <p class="text-2xl font-bold text-orange-600 dark:text-orange-400">
                  {{ formatCurrency(
                    (tax.result.value.tmc.isExempt ? 0 : tax.result.value.tmc.amount) +
                    (tax.result.value.annual.isExempt ? 0 : tax.result.value.annual.amount)
                  ) }}
                </p>
              </div>

              <!-- Submission success notice (logged-in manual mode) -->
              <div
                v-if="submissionSuccess"
                class="p-3 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800"
              >
                <p class="text-sm text-green-700 dark:text-green-300 flex items-center gap-2">
                  <UIcon name="i-lucide-check-circle" class="w-4 h-4" />
                  {{ t('calculator.submissionSuccess') }}
                </p>
              </div>

              <!-- Submission error notice -->
              <div
                v-if="submissionError"
                class="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800"
              >
                <p class="text-sm text-red-700 dark:text-red-300">{{ submissionError }}</p>
              </div>

              <!-- Actions -->
              <div class="flex gap-2">
                <UButton
                  v-if="inputMode === 'catalog'"
                  variant="outline"
                  color="neutral"
                  class="flex-1"
                  trailing-icon="i-lucide-bookmark"
                  @click="openSaveModal"
                >
                  {{ t('common.save') }}
                </UButton>
                <UButton
                  variant="outline"
                  color="neutral"
                  :class="inputMode === 'catalog' ? 'flex-1' : 'w-full'"
                  :trailing-icon="shareSuccess ? 'i-lucide-check' : 'i-lucide-share'"
                  @click="handleShare"
                >
                  {{ shareSuccess ? t('common.copied') : t('common.share') }}
                </UButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </UContainer>

    <!-- Save Search Modal -->
    <UModal v-model:open="saveModalOpen">
      <template #content>
        <div class="p-6">
          <h3 class="text-lg font-semibold mb-4">{{ t('calculator.saveSearch') }}</h3>

          <!-- Success State -->
          <div v-if="saveSuccess" class="text-center py-8">
            <div class="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/30 mx-auto mb-4 flex items-center justify-center">
              <UIcon name="i-lucide-check" class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <p class="text-green-600 dark:text-green-400 font-medium">{{ t('calculator.searchSaved') }}</p>
          </div>

          <!-- Form -->
          <template v-else>
            <div class="mb-4 p-3 rounded-lg bg-neutral-50 dark:bg-neutral-800">
              <p class="font-medium">{{ selectedBrand?.name }} {{ selectedModel?.name }}</p>
              <p class="text-sm text-neutral-500">
                {{ selectedVariant?.name }} - {{ t(`regions.${selectedRegion}`) }}
              </p>
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium mb-2">{{ t('calculator.searchLabel') }}</label>
              <UInput
                v-model="saveLabel"
                :placeholder="t('calculator.searchLabelPlaceholder')"
              />
            </div>

            <div
              v-if="saveError"
              class="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm"
            >
              {{ saveError }}
            </div>

            <div class="flex justify-end gap-2">
              <UButton
                :label="t('common.cancel')"
                color="neutral"
                variant="ghost"
                @click="saveModalOpen = false"
              />
              <UButton
                :label="t('common.save')"
                :loading="savingSearch"
                trailing-icon="i-lucide-bookmark"
                @click="handleSaveSearch"
              />
            </div>
          </template>
        </div>
      </template>
    </UModal>
  </div>
</template>
