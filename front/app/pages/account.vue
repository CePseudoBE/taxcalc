<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => `${t('account.title')} - ${t('app.name')}`,
  robots: 'noindex'
})

const tabs = [
  { key: 'searches', label: 'account.savedSearches', icon: 'i-lucide-bookmark' },
  { key: 'submissions', label: 'account.submissions', icon: 'i-lucide-send' },
  { key: 'profile', label: 'account.profile', icon: 'i-lucide-user' }
]

const activeTab = ref('searches')
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-semibold mb-2">{{ t('account.title') }}</h1>
          <p class="text-neutral-500 dark:text-neutral-400">user@example.com</p>
        </div>
        <UButton
          :label="t('auth.logoutButton')"
          color="neutral"
          variant="outline"
          trailing-icon="i-lucide-log-out"
        />
      </div>

      <!-- Tabs -->
      <div class="flex gap-2 mb-8 border-b border-neutral-200 dark:border-neutral-800">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors"
          :class="activeTab === tab.key
            ? 'border-primary-500 text-primary-600 dark:text-primary-400'
            : 'border-transparent text-neutral-500 hover:text-neutral-700 dark:hover:text-neutral-300'"
          @click="activeTab = tab.key"
        >
          <UIcon :name="tab.icon" class="w-4 h-4" />
          {{ t(tab.label) }}
        </button>
      </div>

      <!-- Tab Content: Saved Searches -->
      <div v-if="activeTab === 'searches'">
        <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-8 text-center">
          <UIcon name="i-lucide-bookmark" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
          <p class="text-neutral-500 dark:text-neutral-400 mb-4">{{ t('account.noSavedSearches') }}</p>
          <UButton
            :label="t('home.getStarted')"
            to="/calculator"
            variant="outline"
          />
        </div>
      </div>

      <!-- Tab Content: Submissions -->
      <div v-if="activeTab === 'submissions'">
        <div class="flex justify-end mb-4">
          <UButton
            :label="t('submission.title')"
            to="/submit"
            trailing-icon="i-lucide-plus"
          />
        </div>
        <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-8 text-center">
          <UIcon name="i-lucide-send" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
          <p class="text-neutral-500 dark:text-neutral-400">{{ t('account.noSubmissions') }}</p>
        </div>
      </div>

      <!-- Tab Content: Profile -->
      <div v-if="activeTab === 'profile'">
        <div class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6">
          <div class="max-w-md">
            <!-- Email -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-2">
                {{ t('auth.email') }}
              </label>
              <p class="font-medium">user@example.com</p>
            </div>

            <!-- Member Since -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-2">
                Membre depuis
              </label>
              <p class="font-medium">Janvier 2025</p>
            </div>

            <!-- Danger Zone -->
            <div class="pt-6 border-t border-neutral-200 dark:border-neutral-800">
              <h3 class="text-sm font-medium text-red-600 dark:text-red-400 mb-4">Zone de danger</h3>
              <UButton
                label="Supprimer mon compte"
                color="error"
                variant="outline"
              />
            </div>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
