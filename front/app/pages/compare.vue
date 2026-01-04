<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => `${t('compare.title')} - ${t('app.name')}`,
  description: () => t('compare.subtitle')
})

const regions = ['wallonia', 'flanders', 'brussels'] as const
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
          <div class="h-12 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
          <div class="h-12 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
          <div class="h-12 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
          <div class="h-12 rounded-lg bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
        </div>
      </div>

      <!-- Comparison Grid -->
      <div class="grid md:grid-cols-3 gap-6">
        <div
          v-for="(region, index) in regions"
          :key="region"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 overflow-hidden"
        >
          <!-- Region Header -->
          <div
            class="p-6 text-white"
            :class="{
              'bg-gradient-to-br from-red-500 to-red-600': index === 0,
              'bg-gradient-to-br from-amber-500 to-amber-600': index === 1,
              'bg-gradient-to-br from-blue-500 to-blue-600': index === 2
            }"
          >
            <h3 class="text-lg font-semibold">{{ t(`regions.${region}`) }}</h3>
          </div>

          <!-- Tax Results -->
          <div class="p-6">
            <!-- TMC -->
            <div class="mb-6">
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-1">{{ t('tax.tmc') }}</p>
              <div class="h-8 w-24 rounded bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>

            <!-- Annual Tax -->
            <div class="mb-6">
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-1">{{ t('tax.annual') }}</p>
              <div class="h-8 w-24 rounded bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>

            <!-- Total First Year -->
            <div class="pt-4 border-t border-neutral-200 dark:border-neutral-800">
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-1">{{ t('tax.totalFirstYear') }}</p>
              <div class="h-10 w-32 rounded bg-neutral-100 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="text-center py-12 text-neutral-400 dark:text-neutral-500">
        <UIcon name="i-lucide-git-compare" class="w-12 h-12 mx-auto mb-4 opacity-50" />
        <p>{{ t('compare.selectVehicle') }}</p>
      </div>
    </UContainer>
  </div>
</template>
