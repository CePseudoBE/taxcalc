<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: () => `${t('app.name')} - ${t('app.tagline')}`,
  description: () => t('app.description'),
  ogTitle: () => `${t('app.name')} - ${t('app.tagline')}`,
  ogDescription: () => t('app.description')
})
</script>

<template>
  <div>
    <!-- Hero -->
    <div class="relative isolate overflow-hidden bg-orange-50 dark:bg-stone-950">
      <!-- Decorative circles - contained within hero bounds -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none" aria-hidden="true">
        <div class="absolute -top-12 -right-12 w-72 h-72 rounded-full bg-orange-200 dark:bg-orange-900/30 blur-2xl" />
        <div class="absolute top-8 left-1/3 w-40 h-40 rounded-full bg-amber-200/70 dark:bg-amber-900/20 blur-2xl" />
        <div class="absolute top-1/3 -left-12 w-48 h-48 rounded-full bg-amber-200 dark:bg-orange-900/20 blur-2xl" />
        <div class="absolute top-1/4 right-1/4 w-32 h-32 rounded-full bg-orange-300/50 dark:bg-orange-800/20 blur-xl" />
        <div class="absolute top-1/2 right-12 w-24 h-24 rounded-full bg-amber-300/60 dark:bg-amber-800/20 blur-lg" />
      </div>

      <UContainer class="relative py-28 md:py-40">
        <div class="max-w-2xl">
          <!-- Badge -->
          <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-orange-100 dark:bg-orange-900/40 text-orange-700 dark:text-orange-300 text-sm font-medium mb-8 border border-orange-200 dark:border-orange-800/50">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-500 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-orange-600"></span>
            </span>
            <span>{{ t('home.heroSubtitle') }}</span>
          </div>

          <!-- Title -->
          <h1 class="text-4xl md:text-6xl font-bold tracking-tight mb-6 text-stone-900 dark:text-white">
            {{ t('home.heroTitle') }}
          </h1>

          <p class="text-lg md:text-xl text-stone-600 dark:text-stone-400 mb-10 leading-relaxed">
            {{ t('app.description') }}
          </p>

          <div class="flex flex-wrap gap-4">
            <UButton
              :label="t('home.getStarted')"
              size="lg"
              trailing-icon="i-lucide-arrow-right"
            />
            <UButton
              :label="t('home.viewCatalog')"
              size="lg"
              color="neutral"
              variant="outline"
              trailing-icon="i-lucide-search"
            />
          </div>
        </div>
      </UContainer>
    </div>

    <!-- Regions as cards -->
    <div class="relative bg-white dark:bg-stone-950">
      <UContainer class="py-16">
      <h2 class="text-sm font-medium text-primary-600 dark:text-primary-400 uppercase tracking-wide mb-2">
        {{ t('regions.title') }}
      </h2>
      <p class="text-2xl font-semibold mb-8">{{ t('home.step1Desc') }}</p>
      <div class="grid md:grid-cols-3 gap-6">
        <NuxtLink
          to="/calculator?region=wallonia"
          class="group p-6 rounded-xl border border-neutral-200 dark:border-neutral-800 hover:border-primary-300 dark:hover:border-primary-700 hover:shadow-lg hover:shadow-primary-500/10 transition-all duration-300 cursor-pointer bg-white dark:bg-neutral-900"
        >
          <div class="flex items-center gap-4 mb-4">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-red-400 to-red-600 flex items-center justify-center shadow-lg shadow-red-500/25">
              <UIcon name="i-lucide-map-pin" class="w-6 h-6 text-white" />
            </div>
            <h3 class="text-lg font-semibold group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {{ t('regions.wallonia') }}
            </h3>
          </div>
          <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-4">
            {{ t('home.walDesc') }}
          </p>
          <div class="flex items-center text-sm text-primary-600 dark:text-primary-400 font-medium">
            <span>{{ t('tax.calculate') }}</span>
            <UIcon name="i-lucide-arrow-right" class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </NuxtLink>

        <NuxtLink
          to="/calculator?region=flanders"
          class="group p-6 rounded-xl border border-neutral-200 dark:border-neutral-800 hover:border-primary-300 dark:hover:border-primary-700 hover:shadow-lg hover:shadow-primary-500/10 transition-all duration-300 cursor-pointer bg-white dark:bg-neutral-900"
        >
          <div class="flex items-center gap-4 mb-4">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center shadow-lg shadow-amber-500/25">
              <UIcon name="i-lucide-map-pin" class="w-6 h-6 text-white" />
            </div>
            <h3 class="text-lg font-semibold group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {{ t('regions.flanders') }}
            </h3>
          </div>
          <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-4">
            {{ t('home.flaDesc') }}
          </p>
          <div class="flex items-center text-sm text-primary-600 dark:text-primary-400 font-medium">
            <span>{{ t('tax.calculate') }}</span>
            <UIcon name="i-lucide-arrow-right" class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </NuxtLink>

        <NuxtLink
          to="/calculator?region=brussels"
          class="group p-6 rounded-xl border border-neutral-200 dark:border-neutral-800 hover:border-primary-300 dark:hover:border-primary-700 hover:shadow-lg hover:shadow-primary-500/10 transition-all duration-300 cursor-pointer bg-white dark:bg-neutral-900"
        >
          <div class="flex items-center gap-4 mb-4">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center shadow-lg shadow-blue-500/25">
              <UIcon name="i-lucide-map-pin" class="w-6 h-6 text-white" />
            </div>
            <h3 class="text-lg font-semibold group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
              {{ t('regions.brussels') }}
            </h3>
          </div>
          <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-4">
            {{ t('home.bruDesc') }}
          </p>
          <div class="flex items-center text-sm text-primary-600 dark:text-primary-400 font-medium">
            <span>{{ t('tax.calculate') }}</span>
            <UIcon name="i-lucide-arrow-right" class="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </NuxtLink>
      </div>
      </UContainer>
    </div>

    <!-- How it works with colored icons -->
    <div class="bg-gradient-to-b from-neutral-50 to-white dark:from-neutral-900/50 dark:to-neutral-950">
      <UContainer class="py-20">
        <div class="text-center mb-12">
          <h2 class="text-sm font-medium text-primary-600 dark:text-primary-400 uppercase tracking-wide mb-2">
            {{ t('home.howItWorks') }}
          </h2>
          <p class="text-2xl font-semibold">{{ t('calculator.subtitle') }}</p>
        </div>
        <div class="grid md:grid-cols-3 gap-8">
          <div class="text-center">
            <div class="w-16 h-16 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center shadow-lg shadow-primary-500/30">
              <UIcon name="i-lucide-map-pin" class="w-8 h-8 text-white" />
            </div>
            <h3 class="font-semibold text-lg mb-2">{{ t('home.step1Title') }}</h3>
            <p class="text-neutral-500 dark:text-neutral-400">{{ t('home.step1Desc') }}</p>
          </div>
          <div class="text-center">
            <div class="w-16 h-16 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center shadow-lg shadow-primary-500/30">
              <UIcon name="i-lucide-car" class="w-8 h-8 text-white" />
            </div>
            <h3 class="font-semibold text-lg mb-2">{{ t('home.step2Title') }}</h3>
            <p class="text-neutral-500 dark:text-neutral-400">{{ t('home.step2Desc') }}</p>
          </div>
          <div class="text-center">
            <div class="w-16 h-16 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center shadow-lg shadow-primary-500/30">
              <UIcon name="i-lucide-receipt" class="w-8 h-8 text-white" />
            </div>
            <h3 class="font-semibold text-lg mb-2">{{ t('home.step3Title') }}</h3>
            <p class="text-neutral-500 dark:text-neutral-400">{{ t('home.step3Desc') }}</p>
          </div>
        </div>
      </UContainer>
    </div>

    <!-- CTA with colored background -->
    <UContainer class="py-16">
      <div class="relative overflow-hidden rounded-2xl bg-gradient-to-r from-primary-600 to-primary-700 dark:from-primary-700 dark:to-primary-800 p-8 md:p-12">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2" />
        <div class="absolute bottom-0 left-0 w-48 h-48 bg-white/5 rounded-full translate-y-1/2 -translate-x-1/2" />
        <div class="relative flex flex-col md:flex-row md:items-center md:justify-between gap-6">
          <div class="text-white">
            <h2 class="text-2xl font-semibold mb-2">{{ t('home.ctaTitle') }}</h2>
            <p class="text-primary-100">{{ t('home.ctaSubtitle') }}</p>
          </div>
          <UButton
            :label="t('home.ctaButton')"
            size="lg"
            trailing-icon="i-lucide-arrow-right"
            class="shrink-0 bg-white text-orange-600 hover:bg-orange-50"
          />
        </div>
      </div>
    </UContainer>
  </div>
</template>
