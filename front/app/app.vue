<script setup lang="ts">
const { t } = useI18n()
const currentYear = new Date().getFullYear()

const navItems = computed(() => [
  { label: t('nav.calculator'), to: '/calculator', icon: 'i-lucide-calculator' },
  { label: t('nav.search'), to: '/search', icon: 'i-lucide-search' },
  { label: t('nav.compare'), to: '/compare', icon: 'i-lucide-git-compare' }
])
</script>

<template>
  <UApp>
    <UHeader>
      <template #left>
        <NuxtLink to="/">
          <AppLogo />
        </NuxtLink>
        <nav class="hidden md:flex items-center gap-1 ml-8">
          <NuxtLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="px-3 py-2 text-sm font-medium text-neutral-600 dark:text-neutral-400 hover:text-neutral-900 dark:hover:text-white transition-colors rounded-lg hover:bg-neutral-100 dark:hover:bg-neutral-800"
          >
            {{ item.label }}
          </NuxtLink>
        </nav>
      </template>

      <template #right>
        <NuxtLink
          to="/auth/login"
          class="hidden sm:inline-flex px-3 py-2 text-sm font-medium text-neutral-600 dark:text-neutral-400 hover:text-neutral-900 dark:hover:text-white transition-colors"
        >
          {{ t('nav.login') }}
        </NuxtLink>
        <LanguageSwitcher />
        <UColorModeButton />
      </template>
    </UHeader>

    <UMain>
      <NuxtPage />
    </UMain>

    <!-- Footer -->
    <footer class="border-t border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/50">
      <UContainer class="py-12">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <!-- About -->
          <div class="col-span-2 md:col-span-1">
            <AppLogo class="mb-4" />
            <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-4">
              {{ t('footer.aboutDesc') }}
            </p>
            <div class="flex items-center gap-1 text-sm text-neutral-500">
              <span>{{ t('footer.madeWith') }}</span>
              <UIcon name="i-lucide-heart" class="w-4 h-4 text-red-500" />
              <span>{{ t('footer.inBelgium') }}</span>
            </div>
          </div>

          <!-- Navigation -->
          <div>
            <h4 class="font-medium text-sm mb-4">{{ t('footer.navigation') }}</h4>
            <ul class="space-y-2">
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('nav.home') }}
                </NuxtLink>
              </li>
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('nav.calculator') }}
                </NuxtLink>
              </li>
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('nav.search') }}
                </NuxtLink>
              </li>
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('nav.compare') }}
                </NuxtLink>
              </li>
            </ul>
          </div>

          <!-- Regions -->
          <div>
            <h4 class="font-medium text-sm mb-4">{{ t('regions.title') }}</h4>
            <ul class="space-y-2">
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('regions.wallonia') }}
                </NuxtLink>
              </li>
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('regions.flanders') }}
                </NuxtLink>
              </li>
              <li>
                <NuxtLink to="/" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('regions.brussels') }}
                </NuxtLink>
              </li>
            </ul>
          </div>

          <!-- Resources -->
          <div>
            <h4 class="font-medium text-sm mb-4">{{ t('footer.resources') }}</h4>
            <ul class="space-y-2">
              <li>
                <a href="#" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('footer.documentation') }}
                </a>
              </li>
              <li>
                <a href="#" class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                  {{ t('footer.api') }}
                </a>
              </li>
              <li>
                <a
                  href="https://github.com"
                  target="_blank"
                  rel="noopener"
                  class="text-sm text-neutral-500 hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors inline-flex items-center gap-1"
                >
                  {{ t('footer.github') }}
                  <UIcon name="i-lucide-external-link" class="w-3 h-3" />
                </a>
              </li>
            </ul>
          </div>
        </div>

        <!-- Bottom bar -->
        <div class="mt-12 pt-8 border-t border-neutral-200 dark:border-neutral-800">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div class="flex flex-wrap items-center gap-4 text-sm text-neutral-500">
              <span>{{ t('footer.copyright', { year: currentYear }) }}</span>
              <span class="hidden md:inline">·</span>
              <a href="#" class="hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                {{ t('footer.legal') }}
              </a>
              <a href="#" class="hover:text-neutral-900 dark:hover:text-neutral-100 transition-colors">
                {{ t('footer.privacy') }}
              </a>
            </div>
            <p class="text-xs text-neutral-400 dark:text-neutral-500">
              {{ t('footer.disclaimer') }}
            </p>
          </div>
        </div>
      </UContainer>
    </footer>
  </UApp>
</template>
