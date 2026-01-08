<script setup lang="ts">
import type { SubmissionStatus, SubmissionResponse } from '~/types/api'

const { t, locale } = useI18n()
const auth = useAuth()
const router = useRouter()
const submissions = useSubmissions()

// Check authentication and moderator status
onMounted(async () => {
  await auth.checkAuth()
  if (!auth.isAuthenticated.value || !auth.isModerator.value) {
    router.push('/')
  } else {
    await loadSubmissions()
  }
})

useSeoMeta({
  title: () => `${t('admin.moderation')} - ${t('admin.title')}`,
  robots: 'noindex, nofollow'
})

// Filter state
const activeFilter = ref<SubmissionStatus | 'all'>('pending')

// Load submissions based on filter
async function loadSubmissions() {
  const status = activeFilter.value === 'all' ? undefined : activeFilter.value
  await submissions.fetchAllSubmissions(status).catch(() => {})
}

// Watch filter changes
watch(activeFilter, () => {
  loadSubmissions()
})

// Status filters with counts
const statusFilters = computed(() => [
  {
    key: 'pending' as const,
    label: t('status.pending'),
    count: submissions.adminSubmissions.value.filter(s => s.status === 'pending').length
  },
  {
    key: 'approved' as const,
    label: t('status.approved'),
    count: submissions.adminSubmissions.value.filter(s => s.status === 'approved').length
  },
  {
    key: 'rejected' as const,
    label: t('status.rejected'),
    count: submissions.adminSubmissions.value.filter(s => s.status === 'rejected').length
  },
  {
    key: 'all' as const,
    label: t('common.all'),
    count: submissions.adminSubmissions.value.length
  }
])

// Filtered submissions
const filteredSubmissions = computed(() => {
  if (activeFilter.value === 'all') {
    return submissions.adminSubmissions.value
  }
  return submissions.adminSubmissions.value.filter(s => s.status === activeFilter.value)
})

// Format date
function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat(locale.value, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// Relative time
function relativeTime(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffHours / 24)

  if (diffHours < 1) {
    return t('time.justNow')
  } else if (diffHours < 24) {
    return t('time.hoursAgo', { count: diffHours })
  } else if (diffDays < 7) {
    return t('time.daysAgo', { count: diffDays })
  } else {
    return formatDate(dateString)
  }
}

// Approve submission
async function handleApprove(id: number) {
  try {
    await submissions.approveSubmission(id)
  } catch (e) {
    // Error handled by composable
  }
}

// Reject submission with modal
const rejectModalOpen = ref(false)
const rejectingSubmission = ref<SubmissionResponse | null>(null)
const rejectFeedback = ref('')

function openRejectModal(submission: SubmissionResponse) {
  rejectingSubmission.value = submission
  rejectFeedback.value = ''
  rejectModalOpen.value = true
}

async function handleReject() {
  if (!rejectingSubmission.value) return

  try {
    await submissions.rejectSubmission(rejectingSubmission.value.id, rejectFeedback.value)
    rejectModalOpen.value = false
    rejectingSubmission.value = null
    rejectFeedback.value = ''
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
          <p class="text-neutral-500 dark:text-neutral-400">{{ t('admin.moderationDesc') }}</p>
        </div>
      </div>

      <!-- Status Filters -->
      <div class="flex gap-2 mb-6 overflow-x-auto pb-2">
        <button
          v-for="filter in statusFilters"
          :key="filter.key"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors whitespace-nowrap"
          :class="activeFilter === filter.key
            ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300'
            : 'bg-neutral-100 dark:bg-neutral-800 text-neutral-600 dark:text-neutral-400 hover:bg-neutral-200 dark:hover:bg-neutral-700'"
          @click="activeFilter = filter.key"
        >
          {{ filter.label }}
          <span class="ml-1 text-xs opacity-60">({{ filter.count }})</span>
        </button>
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
            <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-2/3 mb-2"></div>
            <div class="h-4 bg-neutral-200 dark:bg-neutral-700 rounded w-1/2"></div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="filteredSubmissions.length === 0"
        class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-12 text-center"
      >
        <UIcon name="i-lucide-inbox" class="w-12 h-12 text-neutral-300 dark:text-neutral-700 mx-auto mb-4" />
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('admin.noSubmissions') }}</p>
      </div>

      <!-- Submissions List -->
      <div v-else class="space-y-4">
        <div
          v-for="submission in filteredSubmissions"
          :key="submission.id"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <h3 class="font-semibold text-lg">
                  {{ submission.vehicleData.brandName }} {{ submission.vehicleData.modelName }}
                </h3>
                <span
                  class="text-xs px-2 py-0.5 rounded-full"
                  :class="getStatusColor(submission.status)"
                >
                  {{ t(`status.${submission.status}`) }}
                </span>
              </div>

              <p class="text-neutral-600 dark:text-neutral-400 mb-3">
                {{ submission.vehicleData.variantName }}
              </p>

              <div class="flex flex-wrap gap-3 text-sm text-neutral-500 dark:text-neutral-400 mb-3">
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-zap" class="w-4 h-4" />
                  {{ submission.vehicleData.powerKw }} kW
                  <span v-if="submission.vehicleData.fiscalHp">({{ submission.vehicleData.fiscalHp }} CV)</span>
                </span>
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-fuel" class="w-4 h-4" />
                  {{ t(`fuel.${submission.vehicleData.fuel}`) }}
                </span>
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-shield-check" class="w-4 h-4" />
                  {{ t(`euroNorm.${submission.vehicleData.euroNorm}`) }}
                </span>
                <span class="flex items-center gap-1">
                  <UIcon name="i-lucide-calendar" class="w-4 h-4" />
                  {{ submission.vehicleData.yearStart }}-{{ submission.vehicleData.yearEnd || t('common.present') }}
                </span>
                <span v-if="submission.vehicleData.co2Wltp" class="flex items-center gap-1">
                  <UIcon name="i-lucide-cloud" class="w-4 h-4" />
                  {{ submission.vehicleData.co2Wltp }} g/km
                </span>
              </div>

              <p class="text-xs text-neutral-400">
                {{ t('admin.submittedBy') }} #{{ submission.submitterId }} - {{ relativeTime(submission.submittedAt) }}
              </p>

              <p
                v-if="submission.feedback && submission.status === 'rejected'"
                class="mt-3 text-sm text-red-600 dark:text-red-400 p-2 rounded bg-red-50 dark:bg-red-900/20"
              >
                <strong>{{ t('account.feedback') }}:</strong> {{ submission.feedback }}
              </p>
            </div>

            <div v-if="submission.status === 'pending'" class="flex gap-2 shrink-0">
              <UButton
                :label="t('admin.approve')"
                color="success"
                size="sm"
                :loading="submissions.loading.value"
                @click="handleApprove(submission.id)"
              />
              <UButton
                :label="t('admin.reject')"
                color="error"
                variant="outline"
                size="sm"
                @click="openRejectModal(submission)"
              />
            </div>
          </div>
        </div>
      </div>
    </UContainer>

    <!-- Reject Modal -->
    <UModal v-model:open="rejectModalOpen">
      <template #content>
        <div class="p-6">
          <h3 class="text-lg font-semibold mb-4">{{ t('admin.rejectSubmission') }}</h3>

          <div v-if="rejectingSubmission" class="mb-4 p-3 rounded-lg bg-neutral-50 dark:bg-neutral-800">
            <p class="font-medium">
              {{ rejectingSubmission.vehicleData.brandName }} {{ rejectingSubmission.vehicleData.modelName }}
            </p>
            <p class="text-sm text-neutral-500">{{ rejectingSubmission.vehicleData.variantName }}</p>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium mb-2">{{ t('admin.feedbackOptional') }}</label>
            <UTextarea
              v-model="rejectFeedback"
              :placeholder="t('admin.feedbackPlaceholder')"
              :rows="3"
            />
          </div>

          <div class="flex justify-end gap-2">
            <UButton
              :label="t('common.cancel')"
              color="neutral"
              variant="ghost"
              @click="rejectModalOpen = false"
            />
            <UButton
              :label="t('admin.confirmReject')"
              color="error"
              :loading="submissions.loading.value"
              @click="handleReject"
            />
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>
