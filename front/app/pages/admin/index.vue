<script setup lang="ts">
const { t } = useI18n()
const auth = useAuth()
const router = useRouter()
const submissions = useSubmissions()
const NuxtLink = resolveComponent('NuxtLink')

// Check authentication and moderator status
onMounted(async () => {
  await auth.checkAuth()
  if (!auth.isAuthenticated.value || !auth.isModerator.value) {
    router.push('/')
  } else {
    // Load pending submissions count
    await submissions.fetchAllSubmissions('pending').catch(() => {})
  }
})

useSeoMeta({
  title: () => t('admin.title'),
  robots: 'noindex, nofollow'
})

// Stats computed from data
const stats = computed(() => [
  {
    label: t('admin.pendingSubmissions'),
    value: submissions.adminSubmissions.value.filter(s => s.status === 'pending').length,
    icon: 'i-lucide-clock',
    color: 'amber'
  },
  {
    label: t('admin.approvedSubmissions'),
    value: submissions.adminSubmissions.value.filter(s => s.status === 'approved').length,
    icon: 'i-lucide-check-circle',
    color: 'green'
  },
  {
    label: t('admin.rejectedSubmissions'),
    value: submissions.adminSubmissions.value.filter(s => s.status === 'rejected').length,
    icon: 'i-lucide-x-circle',
    color: 'red'
  },
  {
    label: t('admin.totalSubmissions'),
    value: submissions.adminSubmissions.value.length,
    icon: 'i-lucide-inbox',
    color: 'blue'
  }
])

const menuItems = [
  {
    label: t('admin.moderation'),
    description: t('admin.moderationDesc'),
    icon: 'i-lucide-shield-check',
    to: '/admin/submissions'
  },
  {
    label: t('admin.taxBrackets'),
    description: t('admin.taxBracketsDesc'),
    icon: 'i-lucide-layers',
    to: '/admin/tax-brackets',
    disabled: true
  },
  {
    label: t('admin.taxParameters'),
    description: t('admin.taxParametersDesc'),
    icon: 'i-lucide-settings',
    to: '/admin/tax-parameters',
    disabled: true
  },
  {
    label: t('admin.indexation'),
    description: t('admin.indexationDesc'),
    icon: 'i-lucide-trending-up',
    to: '/admin/indexation',
    disabled: true
  }
]
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-semibold mb-2">{{ t('admin.title') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('admin.dashboard') }}</p>
      </div>

      <!-- Stats -->
      <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-12">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-5"
        >
          <div class="flex items-center gap-3 mb-3">
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center"
              :class="{
                'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400': stat.color === 'amber',
                'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400': stat.color === 'blue',
                'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400': stat.color === 'green',
                'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400': stat.color === 'red',
                'bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400': stat.color === 'purple'
              }"
            >
              <UIcon :name="stat.icon" class="w-5 h-5" />
            </div>
            <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ stat.label }}</p>
          </div>
          <p class="text-2xl font-semibold">{{ stat.value }}</p>
        </div>
      </div>

      <!-- Menu -->
      <h2 class="text-lg font-semibold mb-4">{{ t('admin.management') }}</h2>
      <div class="grid sm:grid-cols-2 gap-4">
        <component
          :is="item.disabled ? 'div' : NuxtLink"
          v-for="item in menuItems"
          :key="item.label"
          :to="item.disabled ? undefined : item.to"
          class="group bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-5 transition-colors"
          :class="item.disabled
            ? 'opacity-50 cursor-not-allowed'
            : 'hover:border-primary-300 dark:hover:border-primary-700 cursor-pointer'"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 rounded-lg bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 flex items-center justify-center shrink-0">
              <UIcon :name="item.icon" class="w-5 h-5" />
            </div>
            <div>
              <div class="flex items-center gap-2">
                <h3 class="font-medium transition-colors" :class="!item.disabled && 'group-hover:text-primary-600 dark:group-hover:text-primary-400'">
                  {{ item.label }}
                </h3>
                <span
                  v-if="item.disabled"
                  class="text-xs px-2 py-0.5 rounded-full bg-neutral-100 dark:bg-neutral-800 text-neutral-500"
                >
                  {{ t('common.comingSoon') }}
                </span>
              </div>
              <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ item.description }}</p>
            </div>
          </div>
        </component>
      </div>
    </UContainer>
  </div>
</template>
