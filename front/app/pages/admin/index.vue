<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => t('admin.title'),
  robots: 'noindex, nofollow'
})

const stats = [
  { label: 'Soumissions en attente', value: 0, icon: 'i-lucide-clock', color: 'amber' },
  { label: 'Véhicules', value: '12,345', icon: 'i-lucide-car', color: 'blue' },
  { label: 'Utilisateurs', value: '1,234', icon: 'i-lucide-users', color: 'green' },
  { label: 'Calculs ce mois', value: '45,678', icon: 'i-lucide-calculator', color: 'purple' }
]

const menuItems = [
  { label: 'Modération', description: 'Gérer les soumissions de véhicules', icon: 'i-lucide-shield-check', to: '/admin/submissions' },
  { label: 'Tranches de taxes', description: 'Configurer les tranches TMC et annuelles', icon: 'i-lucide-layers', to: '/admin/tax-brackets' },
  { label: 'Paramètres de taxes', description: 'Coefficients et paramètres de calcul', icon: 'i-lucide-settings', to: '/admin/tax-parameters' },
  { label: 'Indexation', description: 'Appliquer une indexation annuelle', icon: 'i-lucide-trending-up', to: '/admin/indexation' }
]
</script>

<template>
  <div>
    <UContainer class="py-12">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-semibold mb-2">{{ t('admin.title') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">Tableau de bord administrateur</p>
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
      <h2 class="text-lg font-semibold mb-4">Gestion</h2>
      <div class="grid sm:grid-cols-2 gap-4">
        <NuxtLink
          v-for="item in menuItems"
          :key="item.label"
          :to="item.to"
          class="group bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-5 hover:border-primary-300 dark:hover:border-primary-700 transition-colors"
        >
          <div class="flex items-start gap-4">
            <div class="w-10 h-10 rounded-lg bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 flex items-center justify-center shrink-0">
              <UIcon :name="item.icon" class="w-5 h-5" />
            </div>
            <div>
              <h3 class="font-medium group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                {{ item.label }}
              </h3>
              <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ item.description }}</p>
            </div>
          </div>
        </NuxtLink>
      </div>
    </UContainer>
  </div>
</template>
