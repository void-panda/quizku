# QuizKu

Aplikasi kuis Android real-time untuk guru dan siswa. Guru membuat ruang kuis, mengunggah soal dari Excel, dan memantau jawaban siswa secara langsung. Siswa bergabung via kode ruang, mengerjakan soal, dan melihat hasil akhir.

## Fitur

### Guru
- **Buat Ruang Kuis** — isi nama kelas/mata pelajaran, upload file Excel soal, auto-generate kode 6 karakter
- **Monitoring Real-Time** — lihat daftar siswa yang bergabung, status jawaban (benar/salah), durasi pengerjaan
- **Detail Siswa** — tap nama siswa untuk melihat rincian jawaban salah per soal
- **Kunci Ruang** — tombol "Akhiri" untuk menutup ruang (status LOCKED), siswa baru tidak bisa masuk
- **Ekspor Laporan** — unduh hasil kuis sebagai file Excel (.xlsx) per siswa
- **Daftar Soal** — lihat semua soal dalam ruang dengan pagination (5 soal/halaman)

### Siswa
- **Masuk Ruang** — masukkan kode 6 karakter untuk bergabung
- **Kuis Interaktif** — pilihan ganda (A-E), timer mundur, navigasi soal via nomor soal
- **Urutan Acak** — soal diacak per siswa menggunakan seed unik
- **Hasil Akhir** — skor, jumlah benar/salah, rincian jawaban yang salah
- **Deteksi Waktu Habis** — otomatis menyelesaikan kuis saat timer habis

## Arsitektur

```
┌─────────────────────────────────────┐
│           Presentation              │
│  Activities + ViewModels (MVVM)     │
├─────────────────────────────────────┤
│             Data Layer              │
│  Repositories (Firestore CRUD)      │
├─────────────────────────────────────┤
│           Firebase                  │
│  Firestore (real-time DB)           │
│  Firebase Auth (anonymous)          │
└─────────────────────────────────────┘
```

**Pola Desain:** MVVM (Model-View-ViewModel), Repository Pattern

## Tech Stack

| Komponen | Teknologi |
|---|---|
| Bahasa | Kotlin |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |
| Backend DB | Firebase Firestore |
| Autentikasi | Firebase Auth (Anonymous) |
| UI | Material Design 3 + Custom Flat Design |
| Font | Poppins (Regular, Medium, SemiBold, Bold) |
| Excel | Apache POI 5.2.5 |
| Arsitektur | MVVM + Coroutines + LiveData |

## Struktur Proyek

```
app/src/main/java/com/quizku/app/
├── QuizKuApplication.kt          # Inisialisasi Firebase Auth + seed data
├── MainActivity.kt               # Role selection (Guru/Siswa)
├── data/local/
│   ├── entity/                   # DTO Firestore (User, QuizRoom, Question, Participant, Answer)
│   └── repository/               # CRUD Firestore (singleton object)
├── presentation/
│   ├── teacher/
│   │   ├── DashboardGuruActivity.kt      # Dashboard guru, daftar ruang
│   │   ├── BuatRoomActivity.kt           # Step form buat ruang + upload Excel
│   │   ├── MonitoringRoomActivity.kt     # Monitoring real-time + export
│   │   ├── StudentDetailBottomSheet.kt   # Detail jawaban per siswa
│   │   ├── adapter/                      # QuizRoomAdapter, StudentAdapter, QuestionAdapter
│   │   └── viewmodel/                    # DashboardViewModel, BuatRoomViewModel, MonitoringViewModel
│   └── student/
│       ├── MasukRoomActivity.kt          # Masuk ruang via kode
│       ├── KuisActivity.kt              # Layar kuis utama
│       ├── SkorAkhirActivity.kt         # Hasil akhir kuis
│       ├── QuestionListBottomSheet.kt    # Daftar nomor soal
│       └── viewmodel/                    # MasukRoomViewModel, KuisViewModel, SkorAkhirViewModel
└── util/
    ├── ApiConfig.kt              # Firebase Firestore + Auth singleton
    ├── Constants.kt              # Status, role, validasi konstanta
    ├── ExcelParser.kt            # Baca file Excel jadi list soal
    ├── ExcelTemplateGenerator.kt # Generate template Excel kosong
    └── ExcelFileHelper.kt        # File picker & share
```

## Skema Firestore

| Collection | Field Utama |
|---|---|
| `users` | `name`, `role` (TEACHER/STUDENT), `joinedAt` |
| `rooms` | `name`, `teacherId`, `code`, `status` (DRAFT/ACTIVE/LOCKED/COMPLETED), `createdAt`, `startedAt`, `endsAt` |
| `questions` | `roomId`, `text`, `optionA`-`optionE`, `correctAnswer`, `orderNumber` |
| `participants` | `roomId`, `userId`, `userName`, `joinedAt` |
| `answers` | `participantId`, `roomId`, `questionId`, `selectedAnswer`, `isCorrect`, `answeredAt` |

### Status Ruang

| Status | Arti |
|---|---|
| `DRAFT` | Ruang dibuat, belum dimulai. Siswa bisa masuk. |
| `ACTIVE` | Kuis berlangsung. Siswa mengerjakan soal. |
| `LOCKED` | Kuis diakhiri guru. Siswa tidak bisa masuk. |
| `COMPLETED` | Semua siswa selesai / timer habis. |

## Template Excel

Format kolom yang diperlukan:

| SOAL | OPSI_A | OPSI_B | OPSI_C | OPSI_D | OPSI_E | JAWABAN |
|---|---|---|---|---|---|---|
| 2 + 2 = ? | 1 | 2 | 4 | 5 | 6 | C |

- Minimal 10 soal, maksimal 100 soal
- Panjang maks soal: 500 karakter
- Panjang maks opsi: 100 karakter
- Kolom `JAWABAN` berisi huruf A-E

## Instalasi

1. Clone repositori
2. Buka project di Android Studio
3. Pastikan file `google-services.json` ada di folder `app/` (Firebase project `quisku-655b2`)
4. Build & run ke device atau emulator (minSdk 26)

```bash
./gradlew assembleDebug
```

APK debug akan dihasilkan di `app/build/outputs/apk/debug/`

## Firebase Setup

- **Project ID:** `quisku-655b2`
- **Package:** `com.quizku.app`
- **Firestore Rules (development):**
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if true;
      }
    }
  }
  ```

## Akun Demo

| Role | User ID |
|---|---|
| Guru | `teacher_demo_001` |
| Siswa | `guru_demo` (TEACHER), `siswa_demo` (STUDENT) |

Data demo di-seed otomatis saat aplikasi pertama kali dijalankan.

## Design System

Lihat [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) untuk detail lengkap:
- **Warna:** Primary `#10B981` (Emerald), Tertiary `#0D9488` (Teal), Background `#F5F5FA`
- **Font:** Poppins Regular/Medium/SemiBold/Bold
- **Desain:** Flat — cards tanpa stroke, elevation rendah (2dp), input flat fill
- **Padding:** Screen 24dp, cards 20dp, list items 16dp

## License

Proyek ini untuk keperluan tugas akhir. Tidak tersedia untuk distribusi publik.
