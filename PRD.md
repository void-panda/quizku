# Product Requirements Document (PRD): Obsidian Quiz

## 1. Pendahuluan

**Nama Produk:** Obsidian Quiz
**Deskripsi:** Aplikasi Android quiz online dengan tema dark UI "Precision in Darkness" yang memungkinkan guru membuat kuis, mengunggah soal dari Excel, mengelola durasi kuis, dan memantau hasil siswa secara langsung.

**Tujuan MVP:**
- Guru bisa buat room kuis
- Guru upload soal dari Excel (10-100 soal)
- Guru share kode room ke siswa
- Siswa join dan jawab soal
- Guru lihat hasil kuis real-time
- Auto-lock saat waktu habis

---

## 2. Tech Stack

| Komponen | Teknologi | Keterangan |
|----------|-----------|------------|
| **Language** | Kotlin | Android native |
| **UI** | XML Layout + Material Design | Dark theme "Obsidian" |
| **Database** | Room (SQLite) | Local storage |
| **Architecture** | MVVM | ViewModel + LiveData |
| **Excel** | Apache POI | Parse .xlsx/.xls |
| **Min SDK** | API 24 | Android 7.0+ |
| **Target SDK** | API 34 | Android 14 |

---

## 3. Design System: Obsidian Theme

### 3.1 Color Palette

| Name | Hex | Keterangan |
|------|-----|------------|
| Primary | `#A78BFA` | Soft violet - interactive elements |
| Primary Container | `#7C3AED` | Button backgrounds |
| Background | `#09090B` | True near-black |
| Surface | `#0C0C0F` | Card backgrounds |
| Surface Container | `#121215` | Elevated surfaces |
| On Surface | `#FAFAFA` | Primary text |
| On Surface Variant | `#A1A1AA` | Secondary text |
| Tertiary | `#34D399` | Emerald green - success |
| Error | `#EF4444` | Red - errors only |
| Outline Variant | `#27272A` | Borders |

### 3.2 Typography

- **Font:** Geist (modern, clean, developer-friendly)
- **Headings:** Bold, tight letter-spacing (-0.02em)
- **Body:** Regular weight
- **Labels:** Uppercase, wide tracking (0.2em)

### 3.3 Elevation

- Minimal shadows
- Border-based separation: `1px solid #27272A`
- Focus rings: `2px solid #A78BFA`
- Active states: subtle background shifts

---

## 4. Database Schema

### 4.1 Entity: User

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val role: String, // "TEACHER" or "STUDENT"
    val createdAt: Long = System.currentTimeMillis()
)
```

### 4.2 Entity: QuizRoom

```kotlin
@Entity(tableName = "quiz_rooms")
data class QuizRoom(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomCode: String, // 6 karakter unik
    val teacherId: String,
    val title: String,
    val status: String, // "DRAFT", "ACTIVE", "LOCKED", "COMPLETED"
    val durationMinutes: Int,
    val startedAt: Long? = null,
    val endsAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 4.3 Entity: Question

```kotlin
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val optionE: String,
    val correctAnswer: String, // "A", "B", "C", "D", "E"
    val orderNumber: Int
)
```

### 4.4 Entity: Participant

```kotlin
@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val userId: String,
    val joinedAt: Long = System.currentTimeMillis()
)
```

### 4.5 Entity: Answer

```kotlin
@Entity(tableName = "answers")
data class Answer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val participantId: String,
    val questionId: String,
    val selectedAnswer: String, // "A", "B", "C", "D", "E"
    val isCorrect: Boolean,
    val answeredAt: Long = System.currentTimeMillis()
)
```

---

## 5. Screens & Activities

### 5.1 Screen Flow

```
MainActivity (Role Selection)
├── Teacher Flow
│   ├── DashboardGuruActivity
│   │   ├── BuatRoomActivity
│   │   │   └── MonitoringRoomActivity
│   │   └── MonitoringRoomActivity (via list)
└── Student Flow
    ├── MasukRoomActivity
    │   └── KuisActivity
    │       └── SkorAkhirActivity
```

### 5.2 Screen Details

#### Screen 1: BuatRoomActivity

**Fungsi:** Guru membuat room kuis baru

**Components:**
- EditText: Judul Kuis
- NumberPicker: Durasi (menit)
- Button: Upload Excel → Open file picker
- Button: Buat Room → Generate room code
- TextView: Room code (6 karakter)
- Button: Copy to clipboard
- Button: Mulai Kuis → Start timer

**Layout:**
```
┌─────────────────────────────┐
│  Header: Sesi Baru          │
├─────────────────────────────┤
│  Judul Kuis                 │
│  ┌─────────────────────┐    │
│  │                     │    │
│  └─────────────────────┘    │
│                             │
│  Durasi (menit)             │
│  ┌─────────────────────┐    │
│  │ 30                  │    │
│  └─────────────────────┘    │
│                             │
│  Upload Excel               │
│  ┌─────────────────────┐    │
│  │  📁 Unggah File     │    │
│  │  XLSX, XLS          │    │
│  └─────────────────────┘    │
│                             │
│  Ringkasan                  │
│  ┌─────────────────────┐    │
│  │ Soal: 10            │    │
│  │ Status: DRAFT       │    │
│  └─────────────────────┘    │
│                             │
│  [BUAT KODE RUANGAN]        │
│                             │
│  Kode: ABC123 [Copy]        │
│                             │
│  [MULAI KUIS]               │
└─────────────────────────────┘
```

---

#### Screen 2: DashboardGuruActivity

**Fungsi:** Menampilkan daftar kuis guru

**Components:**
- RecyclerView: List kuis
- Item: Judul, tanggal, jumlah siswa, status
- FAB: Buat kuis baru

**Layout:**
```
┌─────────────────────────────┐
│  Header: Dasbor Guru        │
├─────────────────────────────┤
│  [Buat Ruang Baru]          │
├─────────────────────────────┤
│  Kuis Terbaru               │
│  ┌─────────────────────┐    │
│  │ 📝 Kuis CS-101      │    │
│  │ 12 Mei 2024 | 42 siswa│   │
│  │ Status: ACTIVE       │    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ 📝 Kalkulus II      │    │
│  │ 10 Mei 2024 | 28 siswa│   │
│  │ Status: COMPLETED    │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

---

#### Screen 3: MonitoringRoomActivity

**Fungsi:** Monitoring real-time siswa

**Components:**
- Header: Kode room, timer countdown
- RecyclerView: List siswa aktif (LiveData observer)
- Item: Nama, progress (X/10 terjawab)
- Progress bar: Persentase selesai

**Layout:**
```
┌─────────────────────────────┐
│  Kode Ruang: OBS-404        │
│  [Mulai Kuis] [Tautan]      │
├─────────────────────────────┤
│  Hitung Mundur              │
│  ┌─────────────────────┐    │
│  │     15:00           │    │
│  └─────────────────────┘    │
├─────────────────────────────┤
│  Siswa Aktif                │
│  ┌─────────────────────┐    │
│  │ AS Ahmad Syah       │    │
│  │ Kemajuan: 5/10      │    │
│  │ ████████░░░░░░░░ 50%│    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ SN Siti Noor        │    │
│  │ Kemajuan: 8/10      │    │
│  │ ██████████████░░ 80%│    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

---

#### Screen 4: MasukRoomActivity

**Fungsi:** Siswa join room

**Components:**
- EditText: Username
- EditText: Kode Room (6 karakter)
- Button: Ikuti Kuis

**Layout:**
```
┌─────────────────────────────┐
│                             │
│      Gabung Ruangan         │
│                             │
│  ┌─────────────────────┐    │
│  │ 👤 Nama Pengguna    │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │ 🔑 Kode Ruangan     │    │
│  └─────────────────────┘    │
│                             │
│  [Ikuti KUIS →]             │
│                             │
└─────────────────────────────┘
```

---

#### Screen 5: KuisActivity

**Fungsi:** Siswa menjawab soal

**Components:**
- Top: Timer countdown (sisa waktu)
- Center: Soal saat ini
- Bottom: Pilihan A-E (buttons)
- Navigation: Next/Prev buttons
- Drawer: List nomor soal (tap to jump)
- Auto-save jawaban saat dipilih

**Layout:**
```
┌─────────────────────────────┐
│  Progres: 3/10    ⏱ 08:42  │
├─────────────────────────────┤
│                             │
│  [Ilmu Komputer II]        │
│                             │
│  Manakah dari struktur data │
│  berikut yang beroperasi    │
│  pada prinsip FIFO?         │
│                             │
├─────────────────────────────┤
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ A │ │ B │ │ C │ │ D │  │
│  └───┘ └───┘ └───┘ └───┘  │
│  ┌───┐                     │
│  │ E │                     │
│  └───┘                     │
├─────────────────────────────┤
│  [← Prev]    [Next →]      │
└─────────────────────────────┘

Drawer (Bottom Sheet):
┌─────────────────────────────┐
│  Daftar Soal                │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ 1 │ │ 2 │ │ 3*│ │ 4 │  │
│  └───┘ └───┘ └───┘ └───┘  │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ 5 │ │ 6 │ │ 7 │ │ 8 │  │
│  └───┘ └───┘ └───┘ └───┘  │
│  ┌───┐ ┌───┐               │
│  │ 9 │ │10 │               │
│  └───┘ └───┘               │
└─────────────────────────────┘
```

---

#### Screen 6: SkorAkhirActivity

**Fungsi:** Menampilkan hasil akhir

**Components:**
- Circular progress: Skor (0-100)
- Stats: Benar/X, Waktu tempuh
- Button: Kembali ke Dashboard

**Layout:**
```
┌─────────────────────────────┐
│  Header: Hasil Akhir        │
├─────────────────────────────┤
│                             │
│       ┌─────────┐          │
│      │   850    │          │
│      │   POIN   │          │
│       └─────────┘          │
│                             │
│  Kerja Bagus!               │
│                             │
├─────────────────────────────┤
│  ┌───────┐ ┌───────┐       │
│  │ 8/10  │ │12:45  │       │
│  │ Benar │ │ Waktu │       │
│  └───────┘ └───────┘       │
│                             │
│  Rincian Performa           │
│  Logika: 100% ████████████ │
│  Sintaks: 70% ███████░░░░░ │
│                             │
├─────────────────────────────┤
│  [Kembali ke Dashboard]     │
└─────────────────────────────┘
```

---

## 6. Excel Format

### 6.1 Format Kolom

| Kolom A | Kolom B | Kolom C | Kolom D | Kolom E | Kolom F | Kolom G |
|---------|---------|---------|---------|---------|---------|---------|
| **SOAL** | **OPSI_A** | **OPSI_B** | **OPSI_C** | **OPSI_D** | **OPSI_E** | **JAWABAN** |
| Apa ibukota Indonesia? | Jakarta | Bandung | Surabaya | Yogyakarta | Semarang | A |
| 2 + 2 = ? | 3 | 4 | 5 | 6 | 7 | B |

### 6.2 Validasi

- **Minimal soal:** 10 soal
- **Maksimal soal:** 100 soal
- **Header wajib:** Baris pertama harus nama kolom: `SOAL`, `OPSI_A`, `OPSI_B`, `OPSI_C`, `OPSI_D`, `OPSI_E`, `JAWABAN`
- **Format file:** `.xlsx` atau `.xls`
- **Jawaban:** Harus salah satu dari: `A`, `B`, `C`, `D`, `E` (huruf kapital)
- **Karakter soal:** Maksimal 500 karakter per soal
- **Karakter opsi:** Maksimal 100 karakter per opsi
- **Tidak ada baris kosong:** Semua baris harus terisi

---

## 7. Key Implementation Details

### 7.1 Room Code Generation

```kotlin
fun generateRoomCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}
```

### 7.2 LiveData Observer (Monitoring)

```kotlin
// MonitoringRoomViewModel
val participants: LiveData<List<Participant>> = repository.getParticipants(roomId)

// MonitoringRoomActivity
viewModel.participants.observe(this) { participants ->
    adapter.submitList(participants)
}
```

### 7.3 Copy to Clipboard

```kotlin
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Room Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Kode room disalin!", Toast.LENGTH_SHORT).show()
}
```

### 7.4 Question Navigation Drawer

```kotlin
// KuisActivity
private fun setupQuestionDrawer() {
    // BottomSheetDialog with GridLayoutManager
    // Show question numbers (1-10)
    // Tap to jump to question
    // Highlight current question
}
```

### 7.5 Auto-Lock Mechanism

```kotlin
// CountdownTimer
val timer = object : CountDownTimer(durationMillis, 1000) {
    override fun onTick(millisUntilFinished: Long) {
        updateTimerDisplay(millisUntilFinished)
    }
    
    override fun onFinish() {
        lockRoom(roomId) // ACTIVE → LOCKED
        disableAnswering()
        calculateScore()
    }
}
```

---

## 8. Implementation Plan

### Phase 1: Project Setup (Day 1-2)
- [ ] Create Android project Kotlin
- [ ] Configure Gradle dependencies
- [ ] Setup colors.xml (Obsidian theme)
- [ ] Create data models & database

### Phase 2: Database Layer (Day 2-3)
- [ ] Create User entity & DAO
- [ ] Create QuizRoom entity & DAO
- [ ] Create Question entity & DAO
- [ ] Create Participant entity & DAO
- [ ] Create Answer entity & DAO
- [ ] Setup AppDatabase

### Phase 3: Excel Parser (Day 3-4)
- [ ] Implement ExcelParser.kt
- [ ] Add validation rules
- [ ] Test with sample Excel files

### Phase 4: Teacher Flow (Day 4-7)
- [ ] BuatRoomActivity (form + upload Excel)
- [ ] DashboardGuruActivity (list kuis)
- [ ] MonitoringRoomActivity (real-time monitoring)
- [ ] BuatRoomViewModel, DashboardViewModel, MonitoringViewModel

### Phase 5: Student Flow (Day 7-9)
- [ ] MasukRoomActivity (join room)
- [ ] KuisActivity (jawab soal + timer)
- [ ] SkorAkhirActivity (hasil)
- [ ] MasukRoomViewModel, KuisViewModel, SkorAkhirViewModel

### Phase 6: Auto-Lock & Polish (Day 9-10)
- [ ] Implementasi auto-lock
- [ ] Hitung skor otomatis
- [ ] UI polish sesuai design
- [ ] Testing

---

## 9. Dependencies (build.gradle app)

```gradle
dependencies {
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    annotationProcessor "androidx.room:room-compiler:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    
    // ViewModel & LiveData
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    
    // Apache POI (Excel)
    implementation "org.apache.poi:poi:5.2.5"
    implementation "org.apache.poi:poi-ooxml:5.2.5"
    
    // Material Design
    implementation "com.google.android.material:material:1.11.0"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}
```

---

## 10. Scope Management

### ✅ DOs (Harus Dilakukan)
- Buat room dengan kode unik (6 karakter)
- Upload Excel soal (10-100 soal)
- Mulai kuis dengan durasi
- Join kuis dengan username + kode
- Jawab soal A-E
- Next/Prev navigation dengan drawer
- Monitoring real-time via LiveData
- Auto-lock saat waktu habis
- Hitung skor otomatis
- Copy to clipboard untuk share kode

### ❌ DON'Ts (Tidak Perlu MVP)
- Login/Auth kompleks
- Multiple room aktif
- Leaderboard
- Export hasil
- Notifikasi push
- Chat antar user
- Upload gambar soal
- Kuis essay
- Backend server
- Firebase

---

## 11. References

- Design files: `./smart_quis_design/`
- Theme: "Obsidian - High-Contrast Dark"
- Color palette: See Section 3.1
- Typography: Geist font family
