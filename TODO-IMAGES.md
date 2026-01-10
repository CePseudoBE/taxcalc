# Images à créer pour CalcTax

## Outils recommandés

- **Favicons/PWA** : [RealFaviconGenerator](https://realfavicongenerator.net)
- **OG Images** : [Figma](https://figma.com) ou [Canva](https://canva.com)

---

## 1. Favicons & PWA Icons

Placer dans `front/public/icons/`

| Fichier | Taille | Format |
|---------|--------|--------|
| `icon-16x16.png` | 16x16 | PNG |
| `icon-32x32.png` | 32x32 | PNG |
| `icon-72x72.png` | 72x72 | PNG |
| `icon-96x96.png` | 96x96 | PNG |
| `icon-128x128.png` | 128x128 | PNG |
| `icon-144x144.png` | 144x144 | PNG |
| `icon-152x152.png` | 152x152 | PNG |
| `icon-192x192.png` | 192x192 | PNG |
| `icon-384x384.png` | 384x384 | PNG |
| `icon-512x512.png` | 512x512 | PNG |
| `apple-touch-icon.png` | 180x180 | PNG |

**Conseil** : Créer un logo 512x512 et utiliser RealFaviconGenerator pour générer toutes les tailles.

---

## 2. Images Open Graph (Partage Social)

Placer dans `front/public/`

| Fichier | Taille | Description |
|---------|--------|-------------|
| `og-image.png` | 1200x630 | Image par défaut |
| `og-image-fr.png` | 1200x630 | Version française |
| `og-image-nl.png` | 1200x630 | Version néerlandaise |
| `og-image-en.png` | 1200x630 | Version anglaise |

### Contenu suggéré pour les OG Images

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│     [Logo CalcTax]                                  │
│                                                     │
│     Calculez vos taxes auto                         │
│     en Belgique                                     │
│                                                     │
│     TMC • Taxe de circulation • 3 régions           │
│                                                     │
│     calctax.be                                      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

- **Fond** : Dégradé orange (#f97316) ou blanc
- **Texte** : Gros, lisible, contraste élevé
- **Logo** : En haut à gauche

---

## 3. Logo principal

Placer dans `front/public/`

| Fichier | Usage |
|---------|-------|
| `logo.png` | Logo principal (utilisé par JSON-LD) |
| `logo.svg` | Version vectorielle (optionnel) |

---

## 4. Couleur de marque

- **Primary** : `#f97316` (Orange 500)
- **Background** : `#ffffff` (Blanc)

---

## Checklist

- [ ] Créer logo 512x512
- [ ] Générer favicons via RealFaviconGenerator
- [ ] Placer les icônes dans `front/public/icons/`
- [ ] Créer OG image 1200x630 (FR, NL, EN)
- [ ] Placer les OG images dans `front/public/`
- [ ] Tester avec [Facebook Debugger](https://developers.facebook.com/tools/debug/)
- [ ] Tester avec [Twitter Card Validator](https://cards-dev.twitter.com/validator)
