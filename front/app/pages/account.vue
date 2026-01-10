<script setup lang="ts">
import type { SavedSearchResponse, SubmissionResponse } from '~/types/api'

const { t, locale } = useI18n()
const auth = useAuth()
const router = useRouter()
const savedSearches = useSavedSearches()
const submissions = useSubmissions()

// Redirect if not authenticated
onMounted(async () => {
  await auth.checkAuth()
  if (!auth.isAuthenticated.value) {
    router.push('/auth')
  } else {
    // Load data
    await Promise.all([
      savedSearches.fetchSavedSearches().catch(() => {}),
      submissions.fetchMySubmissions().catch(() => {})
    ])
  }
})

useSeo('account')

const tabs = [
  { key: 'searches', label: 'account.savedSearches', icon: 'i-lucide-bookmark' },
  { key: 'submissions', label: 'account.submissions', icon: 'i-lucide-send' },
  { key: 'profile', label: 'account.profile', icon: 'i-lucide-user' }
]

const activeTab = ref('searches')

// Format member since date
function formatMemberSince(dateString: string | undefined): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  return new Intl.DateTimeFormat(locale.value, { month: 'long', year: 'numeric' }).format(date)
}

// Format date
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat(locale.value, {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(date)
}

// Handle logout
async function handleLogout() {
  await auth.logout()
  router.push('/')
}

// Handle delete saved search
async function handleDeleteSearch(id: number) {
  try {
    await savedSearches.deleteSavedSearch(id)
  } catch (e) {
    // Error handled by composable
  }
}

// Get status badge color
function getStatusColor(status: string): string {
  switch (status) {
    case 'approved':
      return 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300'
    case 'rejected':
      return 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300'
    default:
      return 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300'
  }
}

// Delete account confirmation
const showDeleteConfirm = ref(false)
const deleteLoading = ref(false)

async function handleDeleteAccount() {
  deleteLoading.value = true
  try {
    await $fetch('/api/users/me', { method: 'DELETE' })
    await auth.logout()
    router.push('/')
  } catch (e) {
    // Handle error
  } finally {
    deleteLoading.value = false
    showDeleteConfirm.value = false
  }
}
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-semibold mb-2">{{ t('account.title') }}</h1>
          <p class="text-neutral-500 dark:text-neutral-400">{{ auth.user.value?.email }}</p>
        </div>
        <UButton
          :label="t('auth.logoutButton')"
          color="neutral"
          variant="outline"
          trailing-icon="i-lucide-log-out"
          @click="handleLogout"
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
          <span
            v-if="tab.key === 'searches' && savedSearches.savedSearches.value.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400"
          >
            {{ savedSearches.savedSearches.value.length }}
          </span>
          <span
            v-if="tab.key === 'submissions' && submissions.submissions.value.length > 0"
            class="text-xs px-1.5 py-0.5 rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400"
          >
            {{ submissions.submissions.value.length }}
          </span>
        </button>
      </div>

      <!-- Tab Content: Saved Searches -->
      <div v-if="activeTab === 'searches'">
        <!-- Loading -->
        <div v-if="savedSearches.loading.value" class="space-y-4">
          <div
            v-for="i in 3"
            :key="i"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
          >
            <div class="animate-pulse">
              <div class="h-5 bg-neutral-200 dark:bg-neutral-700 rounded w-1/3 mb-3"></div>
              <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-2/3"></div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          v-else-if="savedSearches.savedSearches.value.length === 0"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-8 text-center"
        >
          <UIcon name="i-lucide-bookmark" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
          <p class="text-neutral-500 dark:text-neutral-400 mb-4">{{ t('account.noSavedSearches') }}</p>
          <UButton
            :label="t('home.getStarted')"
            to="/calculator"
            variant="outline"
          />
        </div>

        <!-- Saved Searches List -->
        <div v-else class="space-y-4">
          <div
            v-for="search in savedSearches.savedSearches.value"
            :key="search.id"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
          >
            <div class="flex items-start justify-between">
              <div>
                <h3 class="font-semibold mb-1">{{ search.label }}</h3>
                <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-2">
                  {{ search.vehicleSummary.brand }} {{ search.vehicleSummary.model }} - {{ search.vehicleSummary.variant }}
                </p>
                <div class="flex flex-wrap gap-2 text-xs text-neutral-400">
                  <span class="flex items-center gap-1">
                    <UIcon name="i-lucide-map-pin" class="w-3 h-3" />
                    {{ t(`regions.${search.region}`) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <UIcon name="i-lucide-calendar" class="w-3 h-3" />
                    {{ search.firstRegistrationDate.year }}
                  </span>
                  <span class="flex items-center gap-1">
                    <UIcon name="i-lucide-zap" class="w-3 h-3" />
                    {{ search.vehicleSummary.powerKw }} kW
                  </span>
                </div>
              </div>
              <div class="flex gap-2">
                <UButton
                  :to="`/calculator?region=${search.region}&year=${search.firstRegistrationDate.year}`"
                  color="primary"
                  variant="soft"
                  size="sm"
                  trailing-icon="i-lucide-arrow-right"
                >
                  {{ t('common.view') }}
                </UButton>
                <UButton
                  color="error"
                  variant="ghost"
                  size="sm"
                  icon="i-lucide-trash-2"
                  @click="handleDeleteSearch(search.id)"
                />
              </div>
            </div>
          </div>
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

        <!-- Loading -->
        <div v-if="submissions.loading.value" class="space-y-4">
          <div
            v-for="i in 3"
            :key="i"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
          >
            <div class="animate-pulse">
              <div class="h-5 bg-neutral-200 dark:bg-neutral-700 rounded w-1/3 mb-3"></div>
              <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-2/3"></div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          v-else-if="submissions.submissions.value.length === 0"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-8 text-center"
        >
          <UIcon name="i-lucide-send" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
          <p class="text-neutral-500 dark:text-neutral-400">{{ t('account.noSubmissions') }}</p>
        </div>

        <!-- Submissions List -->
        <div v-else class="space-y-4">
          <div
            v-for="submission in submissions.submissions.value"
            :key="submission.id"
            class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
          >
            <div class="flex items-start justify-between">
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <h3 class="font-semibold">
                    {{ submission.vehicleData.brandName }} {{ submission.vehicleData.modelName }}
                  </h3>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full"
                    :class="getStatusColor(submission.status)"
                  >
                    {{ t(`status.${submission.status}`) }}
                  </span>
                </div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-2">
                  {{ submission.vehicleData.variantName }}
                </p>
                <div class="flex flex-wrap gap-2 text-xs text-neutral-400">
                  <span>{{ submission.vehicleData.powerKw }} kW</span>
                  <span>{{ t(`fuel.${submission.vehicleData.fuel}`) }}</span>
                  <span>{{ t(`euroNorm.${submission.vehicleData.euroNorm}`) }}</span>
                  <span>{{ submission.vehicleData.yearStart }}-{{ submission.vehicleData.yearEnd || t('common.present') }}</span>
                </div>
                <p class="text-xs text-neutral-400 mt-2">
                  {{ t('account.submittedOn') }} {{ formatDate(submission.submittedAt) }}
                </p>
                <p
                  v-if="submission.feedback"
                  class="text-sm text-red-500 mt-2"
                >
                  {{ t('account.feedback') }}: {{ submission.feedback }}
                </p>
              </div>
            </div>
          </div>
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
              <p class="font-medium">{{ auth.user.value?.email }}</p>
            </div>

            <!-- Member Since -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-2">
                {{ t('account.memberSince') }}
              </label>
              <p class="font-medium">{{ formatMemberSince(auth.user.value?.createdAt) }}</p>
            </div>

            <!-- Role -->
            <div v-if="auth.user.value?.isModerator" class="mb-6">
              <label class="block text-sm font-medium text-neutral-500 dark:text-neutral-400 mb-2">
                {{ t('account.role') }}
              </label>
              <span class="inline-flex items-center gap-1 text-sm px-2 py-1 rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300">
                <UIcon name="i-lucide-shield" class="w-4 h-4" />
                {{ t('account.moderator') }}
              </span>
            </div>

            <!-- Danger Zone -->
            <div class="pt-6 border-t border-neutral-200 dark:border-neutral-800">
              <h3 class="text-sm font-medium text-red-600 dark:text-red-400 mb-4">{{ t('account.dangerZone') }}</h3>

              <div v-if="!showDeleteConfirm">
                <UButton
                  :label="t('account.deleteAccount')"
                  color="error"
                  variant="outline"
                  @click="showDeleteConfirm = true"
                />
              </div>

              <div v-else class="p-4 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
                <p class="text-sm text-red-700 dark:text-red-300 mb-4">
                  {{ t('account.deleteConfirm') }}
                </p>
                <div class="flex gap-2">
                  <UButton
                    :label="t('account.confirmDelete')"
                    color="error"
                    :loading="deleteLoading"
                    @click="handleDeleteAccount"
                  />
                  <UButton
                    :label="t('common.cancel')"
                    color="neutral"
                    variant="ghost"
                    @click="showDeleteConfirm = false"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
