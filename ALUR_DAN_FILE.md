# QuizKu — Alur Kerja & Struktur File

---

## 1. Arsitektur Umum

```
com/quizku/app/
├── QuizKuApplication.kt              # Entry point app, seed data
├── MainActivity.kt                   # Role selection (Guru/Siswa)
├── data/local/
│   ├── entity/                       # DTO Firestore (model data)
│   └── repository/                   # CRUD Firestore (akses data)
├── presentation/
│   ├── teacher/                      # Fitur guru
│   │   ├── adapter/                  # RecyclerView adapters
│   │   └── viewmodel/               # Business logic guru
│   └── student/                      # Fitur siswa
│       └── viewmodel/               # Business logic siswa
└── util/                             # Helper & konfigurasi
```

**Prinsip MVVM:**
```
Activity (View) ←observe→ ViewModel ←call→ Repository ←query→ Firestore
```

---

## 2. Alur Startup App

```
App Launch
    ↓
QuizKuApplication.onCreate()
    ├── Firebase Auth Anonymous (auto login)
    └── seedDefaultUsers()
        ├── Upsert user "guru_demo" (role: TEACHER)
        └── Upsert user "siswa_demo" (role: STUDENT)
    ↓
MainActivity
    └── Pilih Role → DashboardGuruActivity / MasukRoomActivity
```

| File | Fungsi |
|---|---|
| `QuizKuApplication.kt` | Inisialisasi Firebase Auth + seed akun demo ke Firestore |
| `MainActivity.kt` | Halaman awal, navigasi berdasarkan role |
| `ApiConfig.kt` | Singleton `FirebaseFirestore` + `FirebaseAuth` |
| `Constants.kt` | Konstanta: status, role, validasi |

---

## 3. Flow Guru — Buat Ruang Kuis

```
DashboardGuruActivity
    ↓ (tap "Buat Ruang")
BuatRoomActivity
    ├── Step 1: Isi nama kelas/mata pelajaran
    ├── Step 2: Upload file Excel (.xlsx)
    │   └── ExcelParser.parseExcel(inputStream)
    │       → validasi: minimal 10 soal, format kolom
    │       → return List<Question>
    ├── Step 3: Review soal + jumlah soal
    └── Tap "Simpan"
        ├── BuatRoomViewModel.createRoom()
        │   → QuizRoomRepository.insertRoom() → generate kode 6 char
        ├── BuatRoomViewModel.saveQuestions()
        │   → QuestionRepository.insertQuestions()
        └── BuatRoomViewModel.startQuiz()
            → QuizRoomRepository.updateStatus(ROOM_ID, ACTIVE)
        ↓
    Pindah ke MonitoringRoomActivity
```

| File | Fungsi |
|---|---|
| `DashboardGuruActivity.kt` | Dashboard guru, daftar semua ruang, navigasi ke buat room |
| `DashboardViewModel.kt` | Load rooms, delete room |
| `BuatRoomActivity.kt` | Step wizard buat ruang + upload Excel |
| `BuatRoomViewModel.kt` | Create room, save questions, start quiz |
| `QuizRoomRepository.kt` | CRUD rooms: insert, update status, get by ID, delete |
| `QuestionRepository.kt` | CRUD questions: insert batch, get by room |
| `ExcelParser.kt` | Parse file Excel (.xlsx) → List of Question |
| `ExcelTemplateGenerator.kt` | Generate template Excel kosong |
| `ExcelFileHelper.kt` | File picker & share intent |
| `activity_buat_room.xml` | Layout step wizard + loading overlay |
| `QuizRoomAdapter.kt` | RecyclerView adapter untuk daftar ruang |

---

## 4. Flow Guru — Monitor Kuis

```
MonitoringRoomActivity
    ├── loadRoom(roomId)
    │   └── MonitoringViewModel.loadRoom()
    │       ├── QuizRoomRepository.getRoomById()
    │       ├── QuestionRepository.getQuestionsByRoom()
    │       └── ParticipantRepository.getParticipantsByRoom()
    ├── Auto-refresh setiap 5 detik (refreshJob)
    │   └── loadParticipants() → ParticipantRepository.getParticipantsByRoom()
    ├── Pagination soal (5 per halaman)
    │   └── LinearLayout + inflate item_question_monitoring.xml
    ├── Tap siswa → StudentDetailBottomSheet
    │   ├── AnswerRepository.getAnswersByParticipant()
    │   └── Tampilkan: benar, salah, total, rincian salah
    ├── Tombol "Akhiri" → completeRoom()
    │   └── MonitoringViewModel.completeRoom()
    │       → QuizRoomRepository.updateStatus(ROOM_ID, COMPLETED)
    └── Tombol "Download" → exportToExcel()
        └── generateXlsx() → FileProvider → share
```

| File | Fungsi |
|---|---|
| `MonitoringRoomActivity.kt` | Monitoring real-time, pagination, export |
| `MonitoringViewModel.kt` | Load room, load participants, complete room, CSV export |
| `StudentDetailBottomSheet.kt` | Bottom sheet detail jawaban per siswa |
| `StudentAdapter.kt` | RecyclerView adapter daftar siswa |
| `QuestionAdapter.kt` | RecyclerView adapter daftar soal (monitoring) |
| `ParticipantRepository.kt` | CRUD participants: insert, get by room, get by room+user |
| `AnswerRepository.kt` | CRUD answers: insert, get by participant, get by room |
| `activity_monitoring_room.xml` | Layout monitoring + pagination row + loading overlay |
| `item_question_monitoring.xml` | Item layout nomor soal di monitoring |
| `bottom_sheet_student_detail.xml` | Layout bottom sheet detail siswa |
| `file_paths.xml` | FileProvider paths untuk export Excel |

---

## 5. Flow Siswa — Masuk Ruang

```
MasukRoomActivity
    ├── Input kode ruang (6 karakter)
    └── Tap "Masuk"
        └── MasukRoomViewModel.joinRoom(code)
            ├── QuizRoomRepository.getRoomByCode(code)
            │   → validasi: room harus DRAFT atau ACTIVE
            │   → validasi: jika ACTIVE, cek endsAt belum lewat
            ├── ParticipantRepository.insertParticipant()
            └── return roomId, roomStatus, roomEndsAt
        ↓
    Intent → KuisActivity (roomId, participantId, roomStatus, roomEndsAt)
```

| File | Fungsi |
|---|---|
| `MasukRoomActivity.kt` | Input kode ruang + validasi + loading overlay |
| `MasukRoomViewModel.kt` | Join room: cek status, cek waktu, insert participant |
| `activity_masuk_room.xml` | Layout input kode + loading overlay |

---

## 6. Flow Siswa — Kerjakan Kuis

```
KuisActivity
    ├── loadQuestions(roomId, participantId)
    │   └── KuisViewModel.loadQuestions()
    │       ├── QuestionRepository.getQuestionsByRoom()
    │       └── Shuffle dengan seed = participantId.hashCode()
    ├── Tampilkan soal pertama
    ├── Pilih jawaban (A-E)
    │   └── Simpan ke selectedAnswers HashMap (in-memory)
    ├── Navigasi soal via nomor soal (QuestionListBottomSheet)
    ├── Timer mundur berjalan
    │   └── autoCompleteRoom() jika waktu habis
    └── Tap "Selesai"
        ├── Dialog konfirmasi
        └── submitAnswers()
            └── KuisViewModel.submitAnswers()
                ├── AnswerRepository.insertAnswer() (per soal)
                ├── Calculate score: correct / total * 100
                └── Intent → SkorAkhirActivity
```

| File | Fungsi |
|---|---|
| `KuisActivity.kt` | Layar kuis utama: soal, opsi, timer, navigasi |
| `KuisViewModel.kt` | Load questions, shuffle, submit answers, auto-complete |
| `QuestionListBottomSheet.kt` | Bottom sheet navigasi nomor soal |
| `activity_kuis.xml` | Layout kuis: toolbar, opsi, navigasi, timer, loading overlay |
| `bottom_sheet_question_list.xml` | Layout grid nomor soal |

---

## 7. Flow Siswa — Hasil Akhir

```
SkorAkhirActivity
    ├── Tampilkan data dari Intent (score, correct, total)
    ├── loadResult(participantId, roomId)
    │   └── SkorAkhirViewModel.loadResult()
    │       ├── AnswerRepository.getAnswersByParticipant()
    │       ├── QuestionRepository.getQuestionsByRoom()
    │       └── Hitung: score, correct, wrong, unanswered
    ├── Tampilkan: skor, benar, salah, tidak dijawab
    ├── Tampilkan rincian jawaban salah
    │   └── inflate item_wrong_question.xml per item
    └── Tap "Kembali ke Dasbor"
        └── finish() → kembali ke MainActivity
```

| File | Fungsi |
|---|---|
| `SkorAkhirActivity.kt` | Hasil akhir: skor, statistik, rincian salah |
| `SkorAkhirViewModel.kt` | Load answers, hitung score, wrong details |
| `activity_skor_akhir.xml` | Layout hasil: lingkaran skor, kartu stats, rincian salah |
| `item_wrong_question.xml` | Item layout jawaban salah |

---

## 8. Data Layer — Entity & Repository

### Entity (DTO Firestore)

| File | Representasi | Field Utama |
|---|---|---|
| `User.kt` | `users/{id}` | name, role, joinedAt |
| `QuizRoom.kt` | `rooms/{id}` | name, teacherId, code, status, createdAt, startedAt, endsAt |
| `Question.kt` | `questions/{id}` | roomId, text, optionA-E, correctAnswer, orderNumber |
| `Participant.kt` | `participants/{id}` | roomId, userId, userName, joinedAt |
| `Answer.kt` | `answers/{id}` | participantId, roomId, questionId, selectedAnswer, isCorrect, answeredAt |

### Repository (Singleton Object)

| File | Operasi Firestore |
|---|---|
| `UserRepository.kt` | getUser, upsertUser, updateUserRole |
| `QuizRoomRepository.kt` | insertRoom, getRoomById, getRoomByCode, getRoomsByTeacher, updateStatus, deleteRoom |
| `QuestionRepository.kt` | insertQuestions, getQuestionsByRoom |
| `ParticipantRepository.kt` | insertParticipant, getParticipantsByRoom, getParticipantByRoomAndUser |
| `AnswerRepository.kt` | insertAnswer, getAnswersByParticipant, getAnswersByRoom |

---

## 9. Utilitas

| File | Fungsi |
|---|---|
| `ApiConfig.kt` | Singleton `FirebaseFirestore` + `FirebaseAuth` |
| `Constants.kt` | Status (`DRAFT/ACTIVE/LOCKED/COMPLETED`), role (`TEACHER/STUDENT`), validasi Excel |
| `ExcelParser.kt` | Parse `.xlsx` → `List<Question>`, validasi format kolom |
| `ExcelTemplateGenerator.kt` | Generate template Excel kosong dengan header yang benar |
| `ExcelFileHelper.kt` | File picker (ACTION_OPEN_DOCUMENT) + share (ACTION_SEND) via FileProvider |

---

## 10. Resource & Layout

### Layout Utama

| File | Screen |
|---|---|
| `activity_main.xml` | Role selection (Guru/Siswa) |
| `activity_dashboard_guru.xml` | Dashboard guru + loading overlay |
| `activity_buat_room.xml` | Step wizard buat ruang + loading overlay |
| `activity_monitoring_room.xml` | Monitoring + pagination + loading overlay |
| `activity_masuk_room.xml` | Masuk ruang + loading overlay |
| `activity_kuis.xml` | Kuis + timer + loading overlay |
| `activity_skor_akhir.xml` | Hasil akhir + loading overlay |

### Item Layout

| File | Digunakan di |
|---|---|
| `item_room.xml` | Dashboard guru — daftar ruang |
| `item_student.xml` | Monitoring — daftar siswa |
| `item_question_monitoring.xml` | Monitoring — daftar soal (pagination) |
| `item_question_number.xml` | Question list bottom sheet — grid nomor soal |
| `item_wrong_question.xml` | Bottom sheet detail siswa + hasil akhir — rincian salah |

### Bottom Sheet

| File | Screen |
|---|---|
| `bottom_sheet_student_detail.xml` | Monitoring — detail jawaban siswa |
| `bottom_sheet_question_list.xml` | Kuis — navigasi nomor soal |

### Drawable & Resource

| File | Fungsi |
|---|---|
| `illu_book.xml` | Ilustrasi buku (main screen, dashboard) |
| `illu_student.xml` | Ilustrasi siswa (masuk room, monitoring empty) |
| `illu_clipboard.xml` | Ilustrasi clipboard (buat room, dashboard empty) |
| `illu_trophy.xml` | Ilustrasi piala (hasil akhir) |
| `ic_share.xml` | Ikon share (export Excel) |
| `ic_arrow_left.xml` | Ikon panah kiri (pagination) |
| `ic_arrow_right.xml` | Ikon panah kanan (pagination) |
| `progress_bar_primary.xml` | Custom progress bar (kuis timer) |
| `bg_input_fill.xml` | Background flat input |
| `bg_option_button.xml` | Background tombol opsi |
| `bg_option_selected.xml` | Background tombol opsi saat dipilih |

### Font

| File | Weight |
|---|---|
| `poppins_regular.ttf` | Regular — body text |
| `poppins_medium.ttf` | Medium — labels, buttons |
| `poppins_semibold.ttf` | SemiBold — section headers |
| `poppins_bold.ttf` | Bold — titles, numbers |

### Values

| File | Isi |
|---|---|
| `colors.xml` | Warna tema: primary, tertiary, background, surface, error |
| `themes.xml` | Theme.QuizKu, Theme.QuizKu.Dialog, Theme.QuizKu.Button.Option |
| `strings.xml` | String resources (termasuk status_active = "BERLANGSUNG") |
| `dimens.xml` | Spacing, corner radius, elevation |
| `file_paths.xml` | FileProvider paths untuk export Excel |

---

## 11. Konfigurasi Firebase

| File | Fungsi |
|---|---|
| `google-services.json` | Konfigurasi Firebase project `quisku-655b2` |
| `app/build.gradle` | Plugin google-services, Firebase BoM 32.7.4, dependencies |
| `build.gradle` (project) | Classpath google-services 4.4.2 |

---

## 12. Dependensi Build

| Dependency | Versi | Fungsi |
|---|---|---|
| `firebase-bom` | 32.7.4 | Firebase version management |
| `firebase-firestore-ktx` | (BoM) | Firestore database |
| `firebase-auth-ktx` | (BoM) | Firebase Auth anonymous |
| `material` | 1.11.0 | Material Design components |
| `activity-ktx` | 1.8.2 | viewModels() delegate |
| `lifecycle-viewmodel-ktx` | 2.7.0 | ViewModel + Coroutines |
| `lifecycle-livedata-ktx` | 2.7.0 | LiveData |
| `poi` | 5.2.5 | Apache POI Excel read |
| `poi-ooxml` | 5.2.5 | Apache POI Excel write |
| `kotlinx-coroutines-android` | 1.7.3 | Kotlin Coroutines |
| `recyclerview` | 1.3.2 | RecyclerView |
| `cardview` | 1.0.0 | CardView |
| `constraintlayout` | 2.1.4 | ConstraintLayout |
