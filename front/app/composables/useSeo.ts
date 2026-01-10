/**
 * Composable for setting up SEO meta tags with i18n support
 * Automatically handles hreflang tags, canonical URLs, and translated meta
 */
export function useSeo(pageKey: string, options?: { noindex?: boolean }) {
  const { t, locale } = useI18n()
  const route = useRoute()
  const config = useRuntimeConfig()

  const siteUrl = config.public.siteUrl || 'https://calctax.be'
  const siteName = 'CalcTax - Calculateur de Taxes Auto Belges'

  // Build canonical URL (without locale prefix for default locale)
  const canonicalUrl = computed(() => {
    const path = route.path
    return `${siteUrl}${path}`
  })

  // OG Image URL (locale-specific with fallback)
  const ogImageUrl = computed(() => {
    // Use locale-specific image, fallback to default
    return `${siteUrl}/og-image-${locale.value}.png`
  })

  // Fallback OG image if locale-specific doesn't exist
  const ogImageFallback = `${siteUrl}/og-image.png`

  const i18nHead = useLocaleHead({
    addSeoAttributes: true,
    addDirAttribute: true
  })

  useHead({
    htmlAttrs: {
      lang: i18nHead.value.htmlAttrs?.lang
    },
    link: [
      ...(i18nHead.value.link || []),
      { rel: 'canonical', href: canonicalUrl.value }
    ],
    meta: [...(i18nHead.value.meta || [])]
  })

  useSeoMeta({
    title: () => t(`seo.${pageKey}.title`),
    description: () => t(`seo.${pageKey}.description`),
    robots: options?.noindex ? 'noindex, nofollow' : 'index, follow',
    author: 'CalcTax',
    ogTitle: () => t(`seo.${pageKey}.title`),
    ogDescription: () => t(`seo.${pageKey}.description`),
    ogType: 'website',
    ogSiteName: siteName,
    ogUrl: canonicalUrl.value,
    ogImage: ogImageUrl.value,
    ogImageWidth: 1200,
    ogImageHeight: 630,
    ogLocale: () => locale.value === 'fr' ? 'fr_BE' : locale.value === 'nl' ? 'nl_BE' : 'en_GB',
    twitterCard: 'summary_large_image',
    twitterSite: '@calctax_be',
    twitterTitle: () => t(`seo.${pageKey}.title`),
    twitterDescription: () => t(`seo.${pageKey}.description`),
    twitterImage: ogImageUrl.value
  })
}
