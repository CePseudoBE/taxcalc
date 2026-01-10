<script setup lang="ts">
const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useAuth()

useSeo('auth')

// Redirect if already authenticated
onMounted(async () => {
  await auth.checkAuth()
  if (auth.isAuthenticated.value) {
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  }
})

const isLoading = ref(false)
const error = ref<string | null>(null)

async function handleGoogleLogin() {
  isLoading.value = true
  error.value = null

  try {
    // @ts-expect-error google is loaded from external script
    const google = window.google
    if (!google) {
      error.value = t('auth.googleNotLoaded')
      return
    }

    // Use Google Identity Services
    google.accounts.id.initialize({
      client_id: useRuntimeConfig().public.googleClientId,
      callback: handleGoogleCallback
    })

    google.accounts.id.prompt()
  } catch (e: any) {
    error.value = e.message || t('auth.googleError')
  } finally {
    isLoading.value = false
  }
}

async function handleGoogleCallback(response: { credential: string }) {
  isLoading.value = true
  error.value = null

  try {
    await auth.loginWithGoogle(response.credential)
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (e: any) {
    error.value = e.data?.message || e.message || t('auth.loginFailed')
  } finally {
    isLoading.value = false
  }
}

// Expose callback globally for Google
onMounted(() => {
  // @ts-expect-error global callback
  window.handleGoogleCallback = handleGoogleCallback
})

onUnmounted(() => {
  // @ts-expect-error global callback
  delete window.handleGoogleCallback
})
</script>

<template>
  <UContainer class="py-12">
    <div class="max-w-md mx-auto">
      <UCard>
        <template #header>
          <div class="text-center">
            <h1 class="text-2xl font-bold">{{ t('auth.loginTitle') }}</h1>
            <p class="text-neutral-500 dark:text-neutral-400 mt-2">
              {{ t('auth.loginSubtitle') }}
            </p>
          </div>
        </template>

        <div class="space-y-6">
          <UAlert
            v-if="error"
            color="error"
            :title="t('common.error')"
            :description="error"
            icon="i-lucide-alert-circle"
          />

          <!-- Google Sign In Button -->
          <div
            id="g_id_onload"
            :data-client_id="useRuntimeConfig().public.googleClientId"
            data-context="signin"
            data-ux_mode="popup"
            data-callback="handleGoogleCallback"
            data-auto_prompt="false"
          />

          <div
            class="g_id_signin"
            data-type="standard"
            data-shape="rectangular"
            data-theme="outline"
            data-text="signin_with"
            data-size="large"
            data-logo_alignment="left"
            data-width="100%"
          />

          <!-- Fallback button if GSI doesn't load -->
          <UButton
            block
            color="neutral"
            variant="outline"
            size="lg"
            :loading="isLoading"
            icon="i-simple-icons-google"
            @click="handleGoogleLogin"
          >
            {{ t('auth.continueWithGoogle') }}
          </UButton>
        </div>

        <template #footer>
          <p class="text-center text-sm text-neutral-500 dark:text-neutral-400">
            {{ t('auth.termsNotice') }}
          </p>
        </template>
      </UCard>
    </div>
  </UContainer>
</template>
