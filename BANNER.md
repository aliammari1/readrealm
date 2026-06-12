# ReadRealm — Banner / Social-Preview Brief

A lightweight placeholder lives at [`assets/banner.svg`](assets/banner.svg) (so the
README never 404s). Replace it with final art generated from the **single prompt**
below, then drop the result in `assets/` as `assets/banner.png` (hero, 1600×500)
and set the 1280×640 crop as the repo **Social preview** (Settings → Social
preview). The README references `assets/banner.*`.

## The prompt (paste into your image generator)

> A premium, editorial GitHub banner for an open-source product called
> **"ReadRealm"** with the tagline **"Where books meet intelligence."**
> Central concept: a single open book on warm parchment whose right-hand pages
> dissolve into a fine, glowing **gold node-mesh / neural network** — the book and
> the network are literally the same object, pages becoming a graph of connected
> nodes. Calm, literary, intelligent — NOT a generic neon "AI gradient".
> Background is a deep **ink-navy** (`#1C2434`) shading to **deeper navy**
> (`#0F1626`). Paper and light type are **parchment** (`#F4ECD8`); the network
> spark, links and the single accent are **warm gold** (`#C9A24B`); secondary
> details in **muted slate** (`#5B6473`). Left third: the wordmark "ReadRealm" in a
> refined high-contrast serif (Fraunces / Playfair style) with the tagline beneath
> in a clean grotesque (Inter / Söhne style). Right two-thirds: the open-book →
> node-mesh motif. Optional subtle bottom strip: a row of three device silhouettes
> (Android phone, iPhone, a Flutter web/desktop dashboard) each faintly showing a
> book-chat bubble, tying together "one backend, three clients". Soft paper grain,
> gentle vignette, generous safe margins, no clutter, no stock-photo people, no
> lens flares. Wide hero composition. Render at **1600×500** for the README hero
> and also as a centered **1280×640** variant for the GitHub social preview.

## Palette (for consistency across docs/Mintlify)

| Token | Hex | Use |
|---|---|---|
| Ink navy | `#1C2434` | background, primary type |
| Deep navy | `#0F1626` | gradient base |
| Parchment | `#F4ECD8` | paper / light type |
| Warm gold | `#C9A24B` | accent (the "intelligence" spark / links) |
| Muted slate | `#5B6473` | secondary type |

These match the Mintlify theme in [`docs/docs.json`](docs/docs.json).
