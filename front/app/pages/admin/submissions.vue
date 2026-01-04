<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => `${t('admin.moderation')} - ${t('admin.title')}`,
  robots: 'noindex, nofollow'
})

const statusFilters = [
  { key: 'pending', label: 'En attente', count: 0 },
  { key: 'approved', label: 'Approuvées', count: 0 },
  { key: 'rejected', label: 'Rejetées', count: 0 }
]

const activeFilter = ref('pending')
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="flex items-center gap-4 mb-8">
        <NuxtLink to="/admin" class="text-neutral-400 hover:text-neutral-600 dark:hover:text-neutral-300">
          <UIcon name="i-lucide-arrow-left" class="w-5 h-5" />
        </NuxtLink>
        <div>
          <h1 class="text-3xl font-semibold">{{ t('admin.moderation') }}</h1>
          <p class="text-neutral-500 dark:text-neutral-400">{{ t('admin.pendingSubmissions') }}</p>
        </div>
      </div>

      <!-- Status Filters -->
      <div class="flex gap-2 mb-6">
        <button
          v-for="filter in statusFilters"
          :key="filter.key"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="activeFilter === filter.key
            ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300'
            : 'bg-neutral-100 dark:bg-neutral-800 text-neutral-600 dark:text-neutral-400 hover:bg-neutral-200 dark:hover:bg-neutral-700'"
          @click="activeFilter = filter.key"
        >
          {{ filter.label }}
          <span class="ml-1 text-xs opacity-60">({{ filter.count }})</span>
        </button>
      </div>

      <!-- Empty State -->
      <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-12 text-center">
        <UIcon name="i-lucide-inbox" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('admin.noSubmissions') }}</p>
      </div>

      <!-- Submission Card Example (hidden) -->
      <div class="hidden bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h3 class="font-medium mb-1">Volkswagen Golf 8 GTI</h3>
            <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-2">
              180kW · Essence · Euro 6d · 2020-2024
            </p>
            <p class="text-xs text-neutral-400">
              Soumis par user@example.com · Il y a 2 heures
            </p>
          </div>
          <div class="flex gap-2">
            <UButton
              :label="t('admin.approve')"
              color="success"
              size="sm"
            />
            <UButton
              :label="t('admin.reject')"
              color="error"
              variant="outline"
              size="sm"
            />
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
