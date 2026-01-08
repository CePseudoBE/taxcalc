<script setup lang="ts">
import type { FuelType, EuroNorm, VehicleSubmissionRequest } from '~/types/api'

const { t } = useI18n()
const router = useRouter()
const auth = useAuth()
const submissions = useSubmissions()

// Redirect if not authenticated
onMounted(async () => {
  await auth.checkAuth()
  if (!auth.isAuthenticated.value) {
    router.push('/auth/login?redirect=/submit')
  }
})

useSeoMeta({
  title: () => `${t('submission.title')} - ${t('app.name')}`,
  description: () => t('submission.subtitle')
})

// Form state
const form = ref<VehicleSubmissionRequest>({
  brandName: '',
  modelName: '',
  variantName: '',
  yearStart: new Date().getFullYear(),
  yearEnd: undefined,
  powerKw: 0,
  fiscalHp: undefined,
  fuel: 'petrol',
  euroNorm: 'euro_6d',
  co2Wltp: undefined,
  co2Nedc: undefined,
  displacementCc: undefined,
  mmaKg: undefined,
  hasParticleFilter: undefined
})

// Available options
const fuelTypes: { value: FuelType; label: string }[] = [
  { value: 'petrol', label: 'fuel.petrol' },
  { value: 'diesel', label: 'fuel.diesel' },
  { value: 'electric', label: 'fuel.electric' },
  { value: 'hybrid_petrol', label: 'fuel.hybrid_petrol' },
  { value: 'hybrid_diesel', label: 'fuel.hybrid_diesel' },
  { value: 'plug_in_hybrid_petrol', label: 'fuel.plug_in_hybrid_petrol' },
  { value: 'plug_in_hybrid_diesel', label: 'fuel.plug_in_hybrid_diesel' },
  { value: 'lpg', label: 'fuel.lpg' },
  { value: 'cng', label: 'fuel.cng' },
  { value: 'hydrogen', label: 'fuel.hydrogen' }
]

const euroNorms: { value: EuroNorm; label: string }[] = [
  { value: 'euro_7', label: 'euroNorm.euro_7' },
  { value: 'euro_6d', label: 'euroNorm.euro_6d' },
  { value: 'euro_6d_temp', label: 'euroNorm.euro_6d_temp' },
  { value: 'euro_6', label: 'euroNorm.euro_6' },
  { value: 'euro_5b', label: 'euroNorm.euro_5b' },
  { value: 'euro_5', label: 'euroNorm.euro_5' },
  { value: 'euro_4', label: 'euroNorm.euro_4' },
  { value: 'euro_3', label: 'euroNorm.euro_3' },
  { value: 'euro_2', label: 'euroNorm.euro_2' },
  { value: 'euro_1', label: 'euroNorm.euro_1' }
]

// Translated items for selects
const fuelTypesTranslated = computed(() =>
  fuelTypes.map(f => ({ value: f.value, label: t(f.label) }))
)

const euroNormsTranslated = computed(() =>
  euroNorms.map(n => ({ value: n.value, label: t(n.label) }))
)

// Selected objects for USelectMenu (synced with form values)
const selectedFuel = computed({
  get: () => fuelTypesTranslated.value.find(f => f.value === form.value.fuel),
  set: (val) => { if (val) form.value.fuel = val.value }
})

const selectedEuroNorm = computed({
  get: () => euroNormsTranslated.value.find(n => n.value === form.value.euroNorm),
  set: (val) => { if (val) form.value.euroNorm = val.value }
})

const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 50 }, (_, i) => currentYear - i)
})

// Validation
const errors = ref<Record<string, string>>({})

function validate(): boolean {
  errors.value = {}

  if (!form.value.brandName.trim()) {
    errors.value.brandName = t('validation.required')
  }
  if (!form.value.modelName.trim()) {
    errors.value.modelName = t('validation.required')
  }
  if (!form.value.variantName.trim()) {
    errors.value.variantName = t('validation.required')
  }
  if (!form.value.yearStart) {
    errors.value.yearStart = t('validation.required')
  }
  if (!form.value.powerKw || form.value.powerKw <= 0) {
    errors.value.powerKw = t('validation.positiveNumber')
  }
  if (!form.value.fuel) {
    errors.value.fuel = t('validation.required')
  }
  if (!form.value.euroNorm) {
    errors.value.euroNorm = t('validation.required')
  }

  return Object.keys(errors.value).length === 0
}

// Submit
const successMessage = ref<string | null>(null)

async function handleSubmit() {
  if (!validate()) return

  try {
    await submissions.createSubmission(form.value)
    successMessage.value = t('submission.success')

    // Reset form after a delay and redirect
    setTimeout(() => {
      router.push('/account')
    }, 2000)
  } catch (e) {
    // Error is handled by the composable
  }
}
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="max-w-2xl mb-8">
        <h1 class="text-3xl font-semibold mb-3">{{ t('submission.title') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('submission.subtitle') }}</p>
      </div>

      <div class="max-w-2xl">
        <!-- Success Message -->
        <div
          v-if="successMessage"
          class="mb-6 p-4 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-300"
        >
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-check-circle" class="w-5 h-5" />
            {{ successMessage }}
          </div>
        </div>

        <!-- Error Message -->
        <div
          v-if="submissions.error.value"
          class="mb-6 p-4 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300"
        >
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-alert-circle" class="w-5 h-5" />
            {{ submissions.error.value }}
          </div>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6">
            <!-- Brand / Model / Variant -->
            <div class="grid md:grid-cols-3 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.brand') }} *</label>
                <UInput
                  v-model="form.brandName"
                  :placeholder="t('submission.brandPlaceholder')"
                  :color="errors.brandName ? 'error' : undefined"
                />
                <p v-if="errors.brandName" class="text-xs text-red-500 mt-1">{{ errors.brandName }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.model') }} *</label>
                <UInput
                  v-model="form.modelName"
                  :placeholder="t('submission.modelPlaceholder')"
                  :color="errors.modelName ? 'error' : undefined"
                />
                <p v-if="errors.modelName" class="text-xs text-red-500 mt-1">{{ errors.modelName }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.variant') }} *</label>
                <UInput
                  v-model="form.variantName"
                  :placeholder="t('submission.variantPlaceholder')"
                  :color="errors.variantName ? 'error' : undefined"
                />
                <p v-if="errors.variantName" class="text-xs text-red-500 mt-1">{{ errors.variantName }}</p>
              </div>
            </div>

            <!-- Years -->
            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.yearStart') }} *</label>
                <USelectMenu
                  v-model="form.yearStart"
                  :items="years"
                  class="w-full"
                />
                <p v-if="errors.yearStart" class="text-xs text-red-500 mt-1">{{ errors.yearStart }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.yearEnd') }}</label>
                <USelectMenu
                  v-model="form.yearEnd"
                  :items="[...years]"
                  :placeholder="t('submission.ongoing')"
                  class="w-full"
                />
              </div>
            </div>

            <!-- Technical Specs -->
            <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 uppercase tracking-wide mb-4">
              {{ t('submission.technicalSpecs') }}
            </h3>

            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.powerKw') }} *</label>
                <UInput
                  v-model="form.powerKw"
                  type="number"
                  placeholder="100"
                  :color="errors.powerKw ? 'error' : undefined"
                />
                <p v-if="errors.powerKw" class="text-xs text-red-500 mt-1">{{ errors.powerKw }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.fiscalHp') }}</label>
                <UInput
                  v-model="form.fiscalHp"
                  type="number"
                  placeholder="10"
                />
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('fuel.title') }} *</label>
                <USelectMenu
                  v-model="selectedFuel"
                  :items="fuelTypesTranslated"
                  option-attribute="label"
                  class="w-full"
                />
                <p v-if="errors.fuel" class="text-xs text-red-500 mt-1">{{ errors.fuel }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('euroNorm.title') }} *</label>
                <USelectMenu
                  v-model="selectedEuroNorm"
                  :items="euroNormsTranslated"
                  option-attribute="label"
                  class="w-full"
                />
                <p v-if="errors.euroNorm" class="text-xs text-red-500 mt-1">{{ errors.euroNorm }}</p>
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.co2Wltp') }}</label>
                <UInput
                  v-model="form.co2Wltp"
                  type="number"
                  placeholder="150"
                />
                <p class="text-xs text-neutral-400 mt-1">g/km (WLTP)</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.displacement') }}</label>
                <UInput
                  v-model="form.displacementCc"
                  type="number"
                  placeholder="1998"
                />
                <p class="text-xs text-neutral-400 mt-1">cm3</p>
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium mb-2">{{ t('vehicle.mmaKg') }}</label>
                <UInput
                  v-model="form.mmaKg"
                  type="number"
                  placeholder="1500"
                />
                <p class="text-xs text-neutral-400 mt-1">kg</p>
              </div>
              <div class="flex items-center pt-8">
                <label class="flex items-center gap-2 cursor-pointer">
                  <UCheckbox v-model="form.hasParticleFilter" />
                  <span class="text-sm">{{ t('vehicle.hasParticleFilter') }}</span>
                </label>
              </div>
            </div>

            <!-- Submit -->
            <div class="flex gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-800">
              <UButton
                type="submit"
                :label="t('submission.submit')"
                size="lg"
                :loading="submissions.loading.value"
                trailing-icon="i-lucide-send"
              />
              <UButton
                :label="t('common.cancel')"
                size="lg"
                color="neutral"
                variant="ghost"
                to="/account"
              />
            </div>
          </div>
        </form>
      </div>
    </UContainer>
  </div>
</template>
