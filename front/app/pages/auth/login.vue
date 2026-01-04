<script setup lang="ts">
const { t } = useI18n()
const router = useRouter()
const auth = useAuth()

useSeoMeta({
  title: () => `${t('auth.login')} - ${t('app.name')}`,
  robots: 'noindex'
})

// Form state
const email = ref('')
const password = ref('')
const showPassword = ref(false)

// Form validation
const emailError = computed(() => {
  if (!email.value) return null
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    return t('errors.validation')
  }
  return null
})

const canSubmit = computed(() => {
  return email.value && password.value && !emailError.value && !auth.loading.value
})

// Submit
async function handleSubmit() {
  if (!canSubmit.value) return

  try {
    await auth.login({ email: email.value, password: password.value })
    router.push('/account')
  } catch {
    // Error is handled by the composable
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <AppLogo class="mx-auto mb-6 justify-center" />
        <h1 class="text-2xl font-semibold mb-2">{{ t('auth.login') }}</h1>
        <p class="text-neutral-500 dark:text-neutral-400">{{ t('app.tagline') }}</p>
      </div>

      <form
        class="bg-white dark:bg-neutral-900 rounded-xl border border-neutral-200 dark:border-neutral-800 p-6"
        @submit.prevent="handleSubmit"
      >
        <!-- Error message -->
        <div
          v-if="auth.error.value"
          class="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-sm"
        >
          {{ auth.error.value }}
        </div>

        <!-- Email -->
        <div class="mb-4">
          <label class="block text-sm font-medium mb-2">{{ t('auth.email') }}</label>
          <UInput
            v-model="email"
            type="email"
            size="lg"
            placeholder="email@example.com"
            :color="emailError ? 'error' : undefined"
          />
          <p v-if="emailError" class="mt-1 text-sm text-red-500">{{ emailError }}</p>
        </div>

        <!-- Password -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-2">
            <label class="block text-sm font-medium">{{ t('auth.password') }}</label>
            <a href="#" class="text-sm text-primary-600 dark:text-primary-400 hover:underline">
              {{ t('auth.forgotPassword') }}
            </a>
          </div>
          <UInput
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            size="lg"
            placeholder="••••••••"
          >
            <template #trailing>
              <UButton
                :icon="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                variant="ghost"
                color="neutral"
                size="xs"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                @click="showPassword = !showPassword"
              />
            </template>
          </UInput>
        </div>

        <!-- Submit -->
        <UButton
          type="submit"
          :label="auth.loading.value ? t('common.loading') : t('auth.loginButton')"
          size="lg"
          block
          :loading="auth.loading.value"
          :disabled="!canSubmit"
        />

        <!-- Register Link -->
        <p class="text-center text-sm text-neutral-500 dark:text-neutral-400 mt-6">
          {{ t('auth.noAccount') }}
          <NuxtLink to="/auth/register" class="text-primary-600 dark:text-primary-400 hover:underline">
            {{ t('auth.registerButton') }}
          </NuxtLink>
        </p>
      </form>
    </div>
  </div>
</template>
