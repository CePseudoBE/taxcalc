/**
 * Composable for JSON-LD structured data (Schema.org)
 * Improves rich snippets in search results
 */

interface OrganizationSchema {
  '@context': 'https://schema.org'
  '@type': 'Organization'
  name: string
  url: string
  logo: string
  description: string
  sameAs?: string[]
  contactPoint?: {
    '@type': 'ContactPoint'
    contactType: string
    availableLanguage: string[]
  }
}

interface WebSiteSchema {
  '@context': 'https://schema.org'
  '@type': 'WebSite'
  name: string
  url: string
  description: string
  inLanguage: string[]
  potentialAction?: {
    '@type': 'SearchAction'
    target: {
      '@type': 'EntryPoint'
      urlTemplate: string
    }
    'query-input': string
  }
}

interface SoftwareApplicationSchema {
  '@context': 'https://schema.org'
  '@type': 'SoftwareApplication'
  name: string
  description: string
  url: string
  applicationCategory: string
  operatingSystem: string
  offers: {
    '@type': 'Offer'
    price: string
    priceCurrency: string
  }
  aggregateRating?: {
    '@type': 'AggregateRating'
    ratingValue: string
    ratingCount: string
  }
}

interface BreadcrumbSchema {
  '@context': 'https://schema.org'
  '@type': 'BreadcrumbList'
  itemListElement: {
    '@type': 'ListItem'
    position: number
    name: string
    item?: string
  }[]
}

export function useJsonLd() {
  const config = useRuntimeConfig()
  const { t, locale } = useI18n()
  const route = useRoute()

  const siteUrl = config.public.siteUrl || 'https://calctax.be'

  /**
   * Add Organization schema (use on homepage)
   */
  function addOrganizationSchema() {
    const schema: OrganizationSchema = {
      '@context': 'https://schema.org',
      '@type': 'Organization',
      name: 'CalcTax',
      url: siteUrl,
      logo: `${siteUrl}/logo.png`,
      description: t('seo.home.description'),
      sameAs: [
        // Add social media URLs when available
      ],
      contactPoint: {
        '@type': 'ContactPoint',
        contactType: 'customer service',
        availableLanguage: ['French', 'Dutch', 'English']
      }
    }

    useHead({
      script: [
        {
          type: 'application/ld+json',
          innerHTML: JSON.stringify(schema)
        }
      ]
    })
  }

  /**
   * Add WebSite schema with search action (use on homepage)
   */
  function addWebSiteSchema() {
    const schema: WebSiteSchema = {
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      name: 'CalcTax',
      url: siteUrl,
      description: t('seo.home.description'),
      inLanguage: ['fr-BE', 'nl-BE', 'en-GB'],
      potentialAction: {
        '@type': 'SearchAction',
        target: {
          '@type': 'EntryPoint',
          urlTemplate: `${siteUrl}/search?q={search_term_string}`
        },
        'query-input': 'required name=search_term_string'
      }
    }

    useHead({
      script: [
        {
          type: 'application/ld+json',
          innerHTML: JSON.stringify(schema)
        }
      ]
    })
  }

  /**
   * Add SoftwareApplication schema (use on calculator page)
   */
  function addCalculatorSchema() {
    const schema: SoftwareApplicationSchema = {
      '@context': 'https://schema.org',
      '@type': 'SoftwareApplication',
      name: 'CalcTax - Calculateur TMC Belgique',
      description: t('seo.calculator.description'),
      url: `${siteUrl}/calculator`,
      applicationCategory: 'FinanceApplication',
      operatingSystem: 'Web Browser',
      offers: {
        '@type': 'Offer',
        price: '0',
        priceCurrency: 'EUR'
      }
    }

    useHead({
      script: [
        {
          type: 'application/ld+json',
          innerHTML: JSON.stringify(schema)
        }
      ]
    })
  }

  /**
   * Add Breadcrumb schema
   */
  function addBreadcrumbSchema(items: { name: string; url?: string }[]) {
    const schema: BreadcrumbSchema = {
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: items.map((item, index) => ({
        '@type': 'ListItem',
        position: index + 1,
        name: item.name,
        ...(item.url && { item: `${siteUrl}${item.url}` })
      }))
    }

    useHead({
      script: [
        {
          type: 'application/ld+json',
          innerHTML: JSON.stringify(schema)
        }
      ]
    })
  }

  /**
   * Add FAQ schema (for FAQ pages if added)
   */
  function addFaqSchema(faqs: { question: string; answer: string }[]) {
    const schema = {
      '@context': 'https://schema.org',
      '@type': 'FAQPage',
      mainEntity: faqs.map(faq => ({
        '@type': 'Question',
        name: faq.question,
        acceptedAnswer: {
          '@type': 'Answer',
          text: faq.answer
        }
      }))
    }

    useHead({
      script: [
        {
          type: 'application/ld+json',
          innerHTML: JSON.stringify(schema)
        }
      ]
    })
  }

  return {
    addOrganizationSchema,
    addWebSiteSchema,
    addCalculatorSchema,
    addBreadcrumbSchema,
    addFaqSchema
  }
}
