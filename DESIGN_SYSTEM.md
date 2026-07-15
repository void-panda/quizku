# QuizKu Flat Design System

## Color Palette

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#10B981` | Buttons, active states, accents |
| `primary_dark` | `#059669` | Status bar, dark accents |
| `primary_light` | `#6EE7B7` | Highlights, decorative dots |
| `primary_container` | `#D1FAE5` | Light green backgrounds |
| `background` | `#F5F5FA` | Screen background |
| `surface` | `#FFFFFF` | Cards, modals |
| `surface_container` | `#F0F0F5` | Card variants, secondary bg |
| `surface_container_low` | `#FAFAFC` | Input fields default fill |
| `surface_container_high` | `#E8E8EE` | Subtle backgrounds |
| `on_surface` | `#1C1C1E` | Primary text |
| `on_surface_variant` | `#6B6B76` | Secondary text, labels |
| `tertiary` | `#0D9488` | Success, teal accent |
| `error` | `#DC2626` | Errors, destructive |
| `outline` | `#C7C7CC` | Borders, dividers |
| `outline_variant` | `#E0E0E5` | Subtle borders |
| `scrim` | `#80000000` | Loading overlay |

## Typography (Poppins)

| Style | Weight | Size | Usage |
|---|---|---|---|
| `poppins_bold` | Bold | 24sp | Screen titles, hero numbers |
| `poppins_semibold` | SemiBold | 16-18sp | Section headers, card titles |
| `poppins_medium` | Medium | 12-15sp | Labels, buttons, nav |
| `poppins_regular` | Regular | 13-15sp | Body text, descriptions |

## Spacing System

### Screen Layout

| Element | Value |
|---|---|
| Screen horizontal padding | **24dp** |
| Screen top padding (below toolbar) | **16dp** |
| Screen bottom padding (last element) | **24dp** |
| Toolbar height | `?attr/actionBarSize` (56dp) |

### Cards

| Element | Value |
|---|---|
| Card corner radius | 12dp |
| Card elevation | 2dp |
| Content card padding | **20dp** |
| Card margin bottom (between sections) | **14dp** |

### Lists

| Element | Value |
|---|---|
| List item card padding | **16dp** |
| List item margin bottom | **10dp** |
| List item corner radius | 12dp |

### Typography Spacing

| Element | Value |
|---|---|
| Screen title margin bottom | **24dp** |
| Section header margin bottom | **14dp** |
| Subtitle margin bottom | **28dp** |
| Illustration margin bottom | **20dp** |

### Forms (Label + Input)

| Element | Value |
|---|---|
| Label to input gap | **6dp** |
| Input to next element gap | **16dp** |
| Last input to button gap | **24dp** |
| Input padding | 14dp horizontal, 14dp top, 12dp bottom |
| Input corner radius | 8dp |
| Input font | Poppins Regular 15sp |

### Buttons

| Element | Value |
|---|---|
| Primary button height | 64dp |
| Primary button corner radius | 8dp |
| Button to button gap | **12dp** |
| Button text | Poppins Medium, `textAllCaps=false` |

### Stats Cards (Dashboard)

| Element | Value |
|---|---|
| Card padding | **16dp** |
| Inter-card gap | **10dp** (symmetric) |
| Indicator dot size | 8dp |
| Indicator dot margin bottom | **10dp** |
| Number text | Poppins Bold 24sp |
| Label text | Poppins Regular 11sp |

## Components

### Flat Input Pattern
```xml
<!-- Label -->
<TextView
    style="@style/Theme.QuizKu.InputLabel"
    android:text="LABEL TEXT"
    android:layout_marginBottom="6dp" />

<!-- Input -->
<EditText
    style="@style/Theme.QuizKu.InputField"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Placeholder text" />
```

### Card Pattern
```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Theme.QuizKu.Card"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="20dp"
    android:layout_marginBottom="14dp">
    <!-- content -->
</com.google.android.material.card.MaterialCardView>
```

### Button Pattern
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Theme.QuizKu.Button"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    android:text="Button Text" />
```

## Illustrations

| Drawable | Used In |
|---|---|
| `illu_book.xml` | Main screen, Dashboard banner |
| `illu_student.xml` | Masuk room, Monitoring empty |
| `illu_clipboard.xml` | Buat room upload, Dashboard empty |
| `illu_trophy.xml` | Score screen |

## Do / Don't

- **DO** use 24dp screen padding consistently
- **DO** use 20dp card padding for content cards
- **DO** use 16dp card padding for list items
- **DO** use 14dp margin bottom between cards
- **DO** use 10dp gap between list items
- **DO** use uppercase labels with `poppins_medium` 12sp
- **DON'T** use outlined/bordered inputs — use flat fill + label
- **DON'T** use emoji for icons — use vector drawables
- **DON'T** mix padding values for the same component type
- **DON'T** use stroke borders on cards (flat = elevation only)
