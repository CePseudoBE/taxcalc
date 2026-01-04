<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => `${t('search.title')} - ${t('app.name')}`,
  description: () => t('app.description')
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

      <!-- Search Bar -->
      <div class="mb-8">
        <div class="relative">
          <UIcon name="i-lucide-search" class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-400" />
          <input
            type="text"
            :placeholder="t('search.placeholder')"
            class="w-full h-14 pl-12 pr-4 rounded-xl border border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-shadow"
          >
        </div>
      </div>

      <div class="grid lg:grid-cols-4 gap-8">
        <!-- Filters Sidebar -->
        <div class="lg:col-span-1">
          <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-5">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-medium">{{ t('search.filters') }}</h2>
              <button class="text-sm text-primary-600 dark:text-primary-400 hover:underline">
                {{ t('search.clearFilters') }}
              </button>
            </div>

            <!-- Filter: Fuel Type -->
            <div class="mb-6">
              <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-3">
                {{ t('fuel.title') }}
              </h3>
              <div class="space-y-2">
                <label v-for="fuel in ['petrol', 'diesel', 'electric', 'hybrid_petrol']" :key="fuel" class="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" class="rounded border-neutral-300 text-primary-600 focus:ring-primary-500">
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
                <label v-for="norm in ['euro_6d', 'euro_6', 'euro_5', 'euro_4']" :key="norm" class="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" class="rounded border-neutral-300 text-primary-600 focus:ring-primary-500">
                  <span class="text-sm">{{ t(`euroNorm.${norm}`) }}</span>
                </label>
              </div>
            </div>

            <!-- Filter: Power -->
            <div>
              <h3 class="text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-3">
                {{ t('vehicle.power') }}
              </h3>
              <div class="flex gap-2">
                <div class="h-10 flex-1 rounded-lg bg-neutral-100 dark:bg-neutral-800" />
                <div class="h-10 flex-1 rounded-lg bg-neutral-100 dark:bg-neutral-800" />
              </div>
            </div>
          </div>
        </div>

        <!-- Results -->
        <div class="lg:col-span-3">
          <div class="flex items-center justify-between mb-4">
            <p class="text-sm text-neutral-500">{{ t('search.results', { count: 0 }) }}</p>
          </div>

          <!-- Empty State -->
          <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-12 text-center">
            <UIcon name="i-lucide-car" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
            <p class="text-neutral-500 dark:text-neutral-400 mb-4">{{ t('search.noResults') }}</p>
            <p class="text-sm text-neutral-400">{{ t('search.placeholder') }}</p>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
