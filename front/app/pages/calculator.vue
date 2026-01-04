<script setup lang="ts">
import type { Region, BrandResponse, ModelResponse, VariantResponse } from '~/types/api'

const { t } = useI18n()

useSeoMeta({
  title: () => `${t('calculator.title')} - ${t('app.name')}`,
  description: () => t('calculator.subtitle')
})

const vehicles = useVehicles()
const tax = useTax()

// Form state
const selectedRegion = ref<Region | undefined>(undefined)
const selectedBrand = ref<BrandResponse | undefined>(undefined)
const selectedModel = ref<ModelResponse | undefined>(undefined)
const selectedVariant = ref<VariantResponse | undefined>(undefined)
const registrationYear = ref<number>(new Date().getFullYear())
const registrationMonth = ref<MonthOption | undefined>(undefined)
const monthUnknown = ref(false)

// Regions data
const regions: { value: Region; label: string; color: string }[] = [
  { value: 'wallonia', label: 'regions.wallonia', color: 'red' },
  { value: 'flanders', label: 'regions.flanders', color: 'amber' },
  { value: 'brussels', label: 'regions.brussels', color: 'blue' }
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

// Load brands on mount
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

// Form validation
const canCalculate = computed(() => {
  return selectedRegion.value &&
    selectedVariant.value &&
    registrationYear.value &&
    (registrationMonth.value || monthUnknown.value)
})

// Calculate
async function handleCalculate() {
  if (!canCalculate.value || !selectedRegion.value || !selectedVariant.value) return

  await tax.calculate({
    region: selectedRegion.value,
    variantId: selectedVariant.value.id,
    firstRegistrationDate: {
      year: registrationYear.value,
      month: monthUnknown.value ? undefined : registrationMonth.value?.value,
      monthUnknown: monthUnknown.value
    }
  })
}

// Format currency
function formatCurrency(amount: number) {
  return new Intl.NumberFormat('fr-BE', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2
  }).format(amount)
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

          <!-- Vehicle Selection -->
          <div class="bg-white dark:bg-stone-900 rounded-xl border border-stone-200 dark:border-stone-800 p-6">
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
                        {{ item.powerKw }}kW · {{ item.fiscalHp }}CV · {{ t(`fuel.${item.fuel}`) }}
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
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('vehicle.powerKw') }}</span>
                    <p class="font-medium">{{ selectedVariant.powerKw }} kW</p>
                  </div>
                  <div>
                    <span class="text-stone-500 dark:text-stone-400">{{ t('vehicle.fiscalHp') }}</span>
                    <p class="font-medium">{{ selectedVariant.fiscalHp }} CV</p>
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
            :label="tax.loading.value ? t('tax.calculating') : t('tax.calculate')"
            size="lg"
            block
            :loading="tax.loading.value"
            :disabled="!canCalculate"
            trailing-icon="i-lucide-calculator"
            @click="handleCalculate"
          />

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
                {{ t('compare.selectVehicle') }}
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

              <!-- Actions -->
              <div class="flex gap-2">
                <UButton
                  variant="outline"
                  color="neutral"
                  class="flex-1"
                  trailing-icon="i-lucide-bookmark"
                >
                  {{ t('common.save') }}
                </UButton>
                <UButton
                  variant="outline"
                  color="neutral"
                  class="flex-1"
                  trailing-icon="i-lucide-share"
                >
                  Partager
                </UButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
