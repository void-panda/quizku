# QuizKu — Presentasi Tugas Akhir

---

## 1. Pembuka

- **Judul:** QuizKu — Aplikasi Kuis Real-Time Berbasis Firebase Firestore
- **Mahasiswa:** [Nama]
- **NIM:** [NIM]
- **Dosen Pembimbing:** [Nama Dosen]
- **Program Studi:** [Prodi]

---

## 2. Latar Belakang

- Proses belajar mengajar di era digital membutuhkan alat evaluasi yang fleksibel
- Kuis konvensional (kertas) sulit diawasi dan butuh waktu lama untuk koreksi
- Kebutuhan aplikasi kuis real-time yang bisa diakses dari mana saja
- Siswa dan guru perlu umpan balik instan terhadap hasil kuis

---

## 3. Rumusan Masalah

1. Bagaimana membuat aplikasi kuis yang mendukung interaksi real-time antara guru dan siswa?
2. Bagaimana mengelola data kuis secara terdistribusi agar bisa diakses dari berbagai perangkat?
3. Bagaimana memastikan pengalaman pengguna yang intuitif untuk kedua peran (guru & siswa)?

---

## 4. Tujuan

- Mengembangkan aplikasi kuis Android dengan integrasi Firebase Firestore
- Menyediakan monitoring real-time bagi guru selama kuis berlangsung
- Memberikan pengalaman kuis yang interaktif bagi siswa dengan timer dan urutan soal acak
- Mengimplementasikan ekspor laporan hasil kuis ke Excel

---

## 5. Ruang Lingkup

### Yang Dikerjakan
- Aplikasi Android native (Kotlin)
- Autentikasi Firebase Anonymous (tanpa login rumit)
- Pembuatan ruang kuis oleh guru
- Upload soal dari file Excel (.xlsx)
- Kuis interaktif dengan timer
- Monitoring real-time jawaban siswa
- Detail jawaban per siswa (benar/salah + rincian)
- Ekspor laporan ke Excel
- Urutan soal acak per siswa
- Loading state di setiap screen

### Yang Tidak Dikerjakan
- Sistem login/registrasi (menggunakan akun demo)
- Notifikasi email/SMS
- Dashboard admin kompleks
- Integrasi payment gateway

---

## 6. Teknologi yang Digunakan

| Komponen | Teknologi |
|---|---|
| Bahasa Pemrograman | Kotlin |
| Backend Database | Firebase Firestore |
| Autentikasi | Firebase Auth (Anonymous) |
| Arsitektur | MVVM + Repository Pattern |
| UI Framework | Material Design 3 |
| Excel Processing | Apache POI 5.2.5 |
| Concurrency | Kotlin Coroutines + LiveData |
| IDE | Android Studio |

---

## 7. Arsitektur Sistem

### Diagram Komponen

```
┌──────────────────────────────────────────┐
│              Presentation                │
│   Activities + ViewModels (MVVM)         │
│   ┌────────────┐  ┌──────────────────┐   │
│   │   Views    │  │   ViewModels     │   │
│   │  (XML UI)  │←→│  (LiveData +     │   │
│   └────────────┘  │   Coroutines)    │   │
│                   └────────┬─────────┘   │
├────────────────────────────┼─────────────┤
│              Data Layer    │             │
│   ┌────────────────────────┼──────────┐  │
│   │    Repositories (object singletons)│ │
│   │    UserRepository                  │  │
│   │    QuizRoomRepository              │  │
│   │    QuestionRepository              │  │
│   │    ParticipantRepository           │  │
│   │    AnswerRepository                │  │
│   └─────────────────────┬──────────────┘ │
├─────────────────────────┼────────────────┤
│              Firebase   │                │
│   ┌─────────────────────┼──────────┐     │
│   │  Firestore          │          │     │
│   │  (Real-time DB)     │          │     │
│   │  Auth (Anonymous)   │          │     │
│   └─────────────────────┴──────────┘     │
└──────────────────────────────────────────┘
```

### Kenapa MVVM?
- **Separation of Concerns** — UI, business logic, dan data access terpisah
- **Testability** — ViewModel bisa diuji tanpa Android framework
- **Lifecycle-aware** — LiveData otomatis handle lifecycle Activity/Fragment

---

## 8. Skema Database (Firestore)

### Struktur Collections

```
users/
  {userId}          → name, role, joinedAt

rooms/
  {roomId}          → name, teacherId, code, status, createdAt, startedAt, endsAt

questions/
  {questionId}      → roomId, text, optionA-E, correctAnswer, orderNumber

participants/
  {participantId}   → roomId, userId, userName, joinedAt

answers/
  {answerId}        → participantId, roomId, questionId, selectedAnswer, isCorrect, answeredAt
```

### Status Ruang

| Status | Deskripsi |
|---|---|
| `DRAFT` | Ruang dibuat, belum dimulai |
| `ACTIVE` | Kuis berlangsung |
| `LOCKED` | Kuis diakhiri guru |
| `COMPLETED` | Semua selesai / timer habis |

---

## 9. Alur Aplikasi

### Alur Guru

```
Pilih Role Guru
    ↓
Dashboard (lihat semua ruang)
    ↓
Buat Ruang Baru
    ↓
Step 1: Isi Nama Kelas/Mata Pelajaran
    ↓
Step 2: Upload File Excel Soal
    ↓
Step 3: Review & Simpan → Kode Ruang Auto-generated
    ↓
Monitoring Room:
  - Lihat daftar siswa bergabung
  - Real-time update status jawaban
  - Lihat detail jawaban per siswa
  - Pagination daftar soal
    ↓
Akhiri Kuis → Status LOCKED
    ↓
Export Laporan ke Excel (.xlsx)
```

### Alur Siswa

```
Pilih Role Siswa
    ↓
Masukkan Kode Ruang (6 karakter)
    ↓
Menunggu kuis dimulai oleh guru
    ↓
Kuis Dimulai (status ACTIVE):
  - Timer mundur berjalan
  - Soal diacak per siswa
  - Navigasi nomor soal
  - Pilihan ganda A-E
    ↓
Selesaikan Kuis / Timer Habis
    ↓
Lihat Hasil Akhir:
  - Skor (benar / total × 100)
  - Jumlah benar & salah
  - Rincian jawaban yang salah
```

---

## 10. Fitur Utama

### 10.1 Role Selection
- Halaman awal: pilih "Saya Guru" atau "Saya Siswa"
- Tidak ada login — menggunakan akun demo yang di-seed otomatis

### 10.2 Buat Ruang Kuis
- Step-by-step wizard (3 langkah)
- Upload Excel → validasi otomatis (minimal 10 soal, format kolom)
- Kode ruang 6 karakter auto-generated
- Download template Excel kosong untuk referensi

### 10.3 Monitoring Real-Time
- Daftar siswa bergabung dengan status jawaban
- Pagination daftar soal (5 soal per halaman)
- Tap siswa → detail jawaban (benar/salah + rincian)
- Tombol "Akhiri" untuk mengunci ruang
- Tombol "Salin" untuk copy kode ruang
- Export laporan ke Excel

### 10.4 Kuis Interaktif
- Timer mundur dari waktu yang ditentukan
- Soal diacak per siswa (deterministic shuffle)
- Navigasi soal via nomor soal (bottom sheet)
- Pilihan ganda dengan tombol outline (hijau saat dipilih)
- Loading overlay saat load soal
- Auto-submit saat timer habis

### 10.5 Hasil Akhir
- Lingkaran skor dengan warna
- Kartu benar & salah
- Rincian jawaban salah (soal, jawaban siswa, kunci)
- Jumlah tidak dijawab

---

## 11. Implementasi Teknis

### 11.1 Firebase Integration
- **Firebase Auth Anonymous** — tidak perlu registrasi, langsung bisa pakai
- **Firestore** — database real-time, data sinkron otomatis antar perangkat
- **Seed Data** — `QuizKuApplication` auto-create akun guru & siswa demo

### 11.2 Repository Pattern (Singleton)
- Setiap entitas punya Repository `object` (singleton)
- Mengakses Firestore langsung tanpa DAO/Room
- Sorting dilakukan di Kotlin (bukan Firestore query) untuk menghindari composite index

### 11.3 Loading State
- Setiap screen punya `loadingOverlay` (FrameLayout + CircularProgressIndicator)
- `isLoading` LiveData diobserve untuk show/hide overlay
- `withTimeout(15_000L)` pada setiap Firestore coroutine call

### 11.4 Urutan Soal Acak
- Shuffle menggunakan `participantId.hashCode()` sebagai seed
- Setiap siswa dapat urutan soal berbeda
- Deterministic — hasil shuffle konsisten untuk ID yang sama

### 11.5 Timeout & Auto-Complete
- Timer mundur di kuis (configurable per ruang)
- Ketika semua siswa selesai → status otomatis COMPLETED
- Timer habis → auto-submit jawaban → COMPLETED

### 11.6 Excel Import/Export
- **Import:** Apache POI membaca .xlsx → parsing kolom → validasi
- **Export:** Generate .xlsx dengan kolom Nama, Benar, Salah, Tidak Dijawab, Total Soal
- **Template:** Download template kosong untuk referensi format

---

## 12. Design System

### Prinsip Desain
- **Flat Design** — cards tanpa stroke, elevation rendah (2dp)
- **Warna Emerald** — Primary `#10B981`, Tertiary `#0D9488`
- **Font Poppins** — Regular, Medium, SemiBold, Bold
- **Padding Konsisten** — Screen 24dp, cards 20dp, list items 16dp

### Komponen UI
- **Input Flat** — Label `TextView` + `EditText` dengan background fill (bukan outlined)
- **Tombol** — Height 64dp, `textAllCaps=false`, corner 8dp
- **Cards** — Elevation 2dp, corner 12dp, tanpa stroke
- **Loading Overlay** — Semi-transparent scrim + CircularProgressIndicator
- **Illustrasi** — 4 vector drawables XML (buku, clipboard, siswa, piala)

---

## 13. Pengujian

### Skenario yang Diuji

| No | Skenario | Hasil |
|---|---|---|
| 1 | Guru buat ruang + upload Excel | Berhasil |
| 2 | Siswa masuk via kode ruang | Berhasil |
| 3 | Siswa kerjakan kuis + submit | Berhasil |
| 4 | Guru monitor real-time | Berhasil |
| 5 | Guru akhiri kuis → LOCKED | Berhasil |
| 6 | Export laporan ke Excel | Berhasil |
| 7 | Timer habis → auto-submit | Berhasil |
| 8 | Soal acak per siswa | Berhasil |
| 9 | Detail jawaban salah per siswa | Berhasil |
| 10 | Loading state di setiap screen | Berhasil |

---

## 14. Kelebihan & Keterbatasan

### Kelebihan
- Real-time tanpa refresh manual
- Multi-device — data tersinkron via Firestore
- Tidak perlu server backend sendiri
- Desain UI bersih dan intuitif
- Urutan soal acak per siswa (anti kecurangan)
- Ekspor laporan otomatis

### Keterbatasan
- Tidak ada sistem login/registrasi (demo only)
- Tidak ada enkripsi data end-to-end
- Kapasitas soal maksimal 100 per ruang
- Bergantung pada koneksi internet (Firestore)

---

## 15. Penutup

- QuizKu berhasil mengimplementasikan aplikasi kuis real-time dengan Firebase Firestore
- Fitur lengkap: buat ruang, upload soal, kuis interaktif, monitoring, ekspor laporan
- Arsitektur MVVM memudahkan maintenance dan pengembangan lebih lanjut
- **Potensi pengembangan:** login/register, push notification, mode offline, bank soal

---

## 16. Demo Langsung

1. **Buka aplikasi** → Pilih "Saya Guru"
2. **Buat ruang** → Isi nama, upload Excel → Dapat kode ruang
3. **Buka aplikasi di device lain** → Pilih "Saya Siswa" → Masukkan kode
4. **Guru mulai kuis** → Status ACTIVE
5. **Siswa kerjakan kuis** → Pilih jawaban, submit
6. **Guru monitor** → Lihat real-time update
7. **Guru akhiri kuis** → Tombol "Akhiri"
8. **Guru export** → Download laporan Excel
9. **Siswa lihat hasil** → Skor, benar, salah, rincian

---

## Pertanyaan yang Mungkin Ditanyakan

**Q: Kenapa Firebase Auth Anonymous?**
> Karena fokus proyek adalah alur kuis real-time, bukan sistem autentikasi. Anonymous auth cukup untuk demonstrasi.

**Q: Kenapa sorting di Kotlin bukan di Firestore?**
> Menghindari kebutuhan composite index yang menambah kompleksitas konfigurasi Firestore.

**Q: Bagaimana jika koneksi hilang saat kuis?**
> Firestore SDK memiliki offline persistence. Data akan disinkronkan otomatis saat koneksi kembali.

**Q: Bagaimana mencegah siswa curang (buka Google)?**
> Fitur ini belum diimplementasikan. Potensi pengembangan: fullscreen mode, deteksi tab switch.

**Q: Berapa kapasitas maksimal siswa dalam satu ruang?**
> Tidak ada batasan teknis dari Firestore. Kapasitas tergantung pada performa aplikasi dan koneksi.
