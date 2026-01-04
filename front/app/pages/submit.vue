<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => t('submission.title'),
  description: () => t('submission.subtitle')
})
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
        <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6">
          <!-- Brand / Model / Variant -->
          <div class="grid md:grid-cols-3 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.brand') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.model') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.variant') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>

          <!-- Years -->
          <div class="grid md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.yearStart') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.yearEnd') }}</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>

          <!-- Technical Specs -->
          <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 uppercase tracking-wide mb-4">
            Spécifications techniques
          </h3>

          <div class="grid md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.powerKw') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.fiscalHp') }}</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>

          <div class="grid md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('fuel.title') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('euroNorm.title') }} *</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>

          <div class="grid md:grid-cols-2 gap-4 mb-6">
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.co2Wltp') }}</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">{{ t('vehicle.displacement') }}</label>
              <div class="h-11 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>

          <!-- Submit -->
          <div class="flex gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-800">
            <UButton
              :label="t('submission.submit')"
              size="lg"
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
      </div>
    </UContainer>
  </div>
</template>
