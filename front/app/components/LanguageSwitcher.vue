<script setup lang="ts">
const { locale, locales, setLocale } = useI18n()

const availableLocales = computed(() => {
  if (!locales.value) return []
  return locales.value.filter(i => typeof i !== 'string') as Array<{ code: string; name: string }>
})

const currentLocale = computed(() => {
  return availableLocales.value.find(l => l.code === locale.value)
})

const items = computed(() => [
  availableLocales.value.map(loc => ({
    label: loc.name,
    onSelect: () => setLocale(loc.code as 'fr' | 'nl' | 'en')
  }))
])

const getLocaleLabel = (code: string) => {
  switch (code) {
    case 'fr':
      return 'FR'
    case 'nl':
      return 'NL'
    case 'en':
      return 'EN'
    default:
      return code.toUpperCase()
  }
}
</script>

<template>
  <UDropdownMenu
    :items="items"
    :content="{ align: 'end' }"
  >
    <UButton
      color="neutral"
      variant="ghost"
      size="sm"
      class="cursor-pointer"
    >
      <span class="text-xs font-bold">{{ getLocaleLabel(locale) }}</span>
      <span class="hidden sm:inline ml-1">{{ currentLocale?.name }}</span>
      <UIcon
        name="i-lucide-chevron-down"
        class="w-4 h-4 ml-1"
      />
    </UButton>
  </UDropdownMenu>
</template>
