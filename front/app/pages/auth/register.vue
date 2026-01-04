<script setup lang="ts">
const { t } = useI18n()
const router = useRouter()
const auth = useAuth()

useSeoMeta({
  title: () => `${t('auth.register')} - ${t('app.name')}`,
  robots: 'noindex'
})

// Form state
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// Form validation
const emailError = computed(() => {
  if (!email.value) return null
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    return t('errors.validation')
  }
  return null
})

const passwordError = computed(() => {
  if (!password.value) return null
  if (password.value.length < 8) {
    return 'Le mot de passe doit contenir au moins 8 caractères'
  }
  return null
})

const confirmPasswordError = computed(() => {
  if (!confirmPassword.value) return null
  if (confirmPassword.value !== password.value) {
    return 'Les mots de passe ne correspondent pas'
  }
  return null
})

const canSubmit = computed(() => {
  return email.value &&
    password.value &&
    confirmPassword.value &&
    !emailError.value &&
    !passwordError.value &&
    !confirmPasswordError.value &&
    !auth.loading.value
})

// Submit
async function handleSubmit() {
  if (!canSubmit.value) return

  try {
    await auth.register({ email: email.value, password: password.value })
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
        <h1 class="text-2xl font-semibold mb-2">{{ t('auth.register') }}</h1>
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
        <div class="mb-4">
          <label class="block text-sm font-medium mb-2">{{ t('auth.password') }}</label>
          <UInput
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            size="lg"
            placeholder="••••••••"
            :color="passwordError ? 'error' : undefined"
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
          <p v-if="passwordError" class="mt-1 text-sm text-red-500">{{ passwordError }}</p>
        </div>

        <!-- Confirm Password -->
        <div class="mb-6">
          <label class="block text-sm font-medium mb-2">{{ t('auth.confirmPassword') }}</label>
          <UInput
            v-model="confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            size="lg"
            placeholder="••••••••"
            :color="confirmPasswordError ? 'error' : undefined"
          >
            <template #trailing>
              <UButton
                :icon="showConfirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                variant="ghost"
                color="neutral"
                size="xs"
                :aria-label="showConfirmPassword ? 'Hide password' : 'Show password'"
                @click="showConfirmPassword = !showConfirmPassword"
              />
            </template>
          </UInput>
          <p v-if="confirmPasswordError" class="mt-1 text-sm text-red-500">{{ confirmPasswordError }}</p>
        </div>

        <!-- Submit -->
        <UButton
          type="submit"
          :label="auth.loading.value ? t('common.loading') : t('auth.registerButton')"
          size="lg"
          block
          :loading="auth.loading.value"
          :disabled="!canSubmit"
        />

        <!-- Login Link -->
        <p class="text-center text-sm text-neutral-500 dark:text-neutral-400 mt-6">
          {{ t('auth.hasAccount') }}
          <NuxtLink to="/auth/login" class="text-primary-600 dark:text-primary-400 hover:underline">
            {{ t('auth.loginButton') }}
          </NuxtLink>
        </p>
      </form>
    </div>
  </div>
</template>
