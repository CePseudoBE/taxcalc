// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui',
    '@nuxt/image',
    '@nuxtjs/i18n',
    '@nuxtjs/sitemap',
    '@nuxtjs/robots'
  ],

  devtools: {
    enabled: true
  },

  css: ['~/assets/css/main.css'],

  app: {
    head: {
      charset: 'utf-8',
      viewport: 'width=device-width, initial-scale=1',
      meta: [
        { name: 'theme-color', content: '#f97316' },
        { name: 'msapplication-TileColor', content: '#f97316' },
        { name: 'apple-mobile-web-app-capable', content: 'yes' },
        { name: 'apple-mobile-web-app-status-bar-style', content: 'default' },
        { name: 'apple-mobile-web-app-title', content: 'CalcTax' },
        { name: 'format-detection', content: 'telephone=no' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' },
        { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/icons/icon-32x32.png' },
        { rel: 'icon', type: 'image/png', sizes: '16x16', href: '/icons/icon-16x16.png' },
        { rel: 'apple-touch-icon', sizes: '180x180', href: '/icons/apple-touch-icon.png' },
        { rel: 'manifest', href: '/manifest.json' }
      ],
      script: [
        { src: 'https://accounts.google.com/gsi/client', async: true, defer: true }
      ]
    }
  },

  runtimeConfig: {
    // Configuration privee (serveur uniquement)
    backendUrl: 'http://localhost:8080/api',
    sessionSecret: process.env.NUXT_SESSION_SECRET || 'dev-only-secret-change-in-production!!',

    // Configuration publique (client + serveur)
    public: {
      apiBaseUrl: 'http://localhost:8080/api',
      googleClientId: process.env.NUXT_PUBLIC_GOOGLE_CLIENT_ID || '',
      siteUrl: process.env.NUXT_PUBLIC_SITE_URL || 'https://calctax.be'
    }
  },

  i18n: {
    locales: [
      { code: 'fr', name: 'Français', file: 'fr.json', language: 'fr-BE' },
      { code: 'nl', name: 'Nederlands', file: 'nl.json', language: 'nl-BE' },
      { code: 'en', name: 'English', file: 'en.json', language: 'en-GB' }
    ],
    defaultLocale: 'fr',
    strategy: 'prefix_except_default',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_locale',
      fallbackLocale: 'fr',
      redirectOn: 'root'
    },
    baseUrl: process.env.NUXT_PUBLIC_SITE_URL || 'https://calctax.be'
  },

  site: {
    url: process.env.NUXT_PUBLIC_SITE_URL || 'https://calctax.be',
    name: 'CalcTax',
    description: 'Calculateur de taxes automobiles belges',
    defaultLocale: 'fr'
  },

  sitemap: {
    autoI18n: true,
    xslColumns: [
      { label: 'URL', width: '50%' },
      { label: 'Last Modified', select: 'sitemap:lastmod', width: '25%' },
      { label: 'Hreflang', select: 'count(xhtml:link)', width: '25%' }
    ]
  },

  robots: {
    allow: '/',
    disallow: ['/admin', '/account', '/api'],
    sitemap: '/sitemap.xml'
  },

  // Headers de sécurité
  routeRules: {
    '/**': {
      headers: {
        'X-Frame-Options': 'DENY',
        'X-Content-Type-Options': 'nosniff',
        'X-XSS-Protection': '1; mode=block',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
        'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
        // HSTS: Force HTTPS for 1 year, include subdomains
        'Strict-Transport-Security': 'max-age=31536000; includeSubDomains; preload',
        // CSP: Restrict resource loading
        'Content-Security-Policy': [
          "default-src 'self'",
          "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://accounts.google.com https://apis.google.com",
          "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
          "font-src 'self' https://fonts.gstatic.com",
          "img-src 'self' data: https: blob:",
          "connect-src 'self' https://accounts.google.com https://www.googleapis.com",
          "frame-src 'self' https://accounts.google.com",
          "object-src 'none'",
          "base-uri 'self'",
          "form-action 'self'",
          "frame-ancestors 'none'",
          "upgrade-insecure-requests"
        ].join('; ')
      }
    }
  },

  compatibilityDate: '2025-01-15',

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  }
})
