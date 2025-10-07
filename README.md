# Universal Downloader - Aplikasi Pengunduh File

## Deskripsi Aplikasi

Universal Downloader adalah aplikasi desktop berbasis Java Swing yang dirancang khusus untuk mengunduh file dari berbagai sumber dengan antarmuka yang sederhana dan efisien. Aplikasi ini mendukung pengunduhan dari platform video seperti YouTube, Vimeo, serta direct links lainnya dengan fitur manajemen progres dan riwayat download yang komprehensif.

## Teknologi yang Digunakan

- **Bahasa Pemrograman**: Java 17+
- **GUI Framework**: Java Swing
- **Komponen UI Custom**: RoundedPanel, RoundedButton, GradientPanel
- **External Tools**: yt-dlp (untuk YouTube/video downloads)
- **Concurrency**: ExecutorService, CountDownLatch, AtomicReference
- **File I/O**: NIO2 (java.nio.file)

## Fitur Utama

### 1. Universal Download Engine
- Mendukung pengunduhan dari URL apapun (HTTP/HTTPS)
- Deteksi otomatis platform video (YouTube, Vimeo, dll.)
- Download buffered dengan progress tracking real-time
- Cancellation support dengan cleanup otomatis

### 2. YouTube & Video Platform Support
- Integrasi yt-dlp dengan auto-download capability
- Format selection (MP4 video / MP3 audio)
- Aggressive cancellation untuk menghentikan proses yt-dlp secara paksa
- Process tree termination untuk membunuh semua child processes

### 3. Modern User Interface
- Single-window design tanpa popup dialog
- Real-time progress bar dengan percentage dan size information
- Custom rounded components untuk aesthetic yang modern
- Gradient backgrounds dan smooth animations

### 4. History Management
- Riwayat download lengkap dengan timestamp
- Quick actions: buka file dan buka folder
- Status tracking (Berhasil, Dibatalkan, Gagal)
- Persistent storage untuk session berikutnya

### 5. Automatic Video Title Extraction
- Ekstraksi judul video otomatis untuk YouTube
- Sanitasi nama file untuk kompatibilitas filesystem
- Fallback ke timestamp jika ekstraksi gagal
- Update UI real-time dengan judul yang diekstrak

### 6. Error Handling & Recovery
- Comprehensive error classification system
- User-friendly error messages dalam Bahasa Indonesia
- Automatic retry mechanisms untuk network issues
- Graceful degradation jika dependencies tidak tersedia

## Arsitektur Aplikasi

### Class Structure

```
Main.java                   # Entry point aplikasi
├── DownloaderGUI.java     # Main UI controller dan event handling
├── FileDownloader.java    # Generic file download engine
├── YtDlpHelper.java       # YouTube/video platform handler
├── DownloadError.java     # Error classification enum
├── RoundedPanel.java      # Custom UI component
├── RoundedButton.java     # Custom button component
├── GradientPanel.java     # Background gradient component
└── ProgressGUI.java       # Progress display component
```

### Component Details

#### FileDownloader
- **Fungsi**: Universal HTTP/HTTPS file downloader
- **Features**: Buffered streaming, progress callbacks, cancellation support
- **Error Handling**: Network timeouts, file permission, disk space validation

#### YtDlpHelper
- **Fungsi**: Interface ke yt-dlp untuk video platform downloads
- **Features**: Auto-download yt-dlp binary, process management, format selection
- **Cancellation**: Aggressive process tree termination dengan timeout handling

#### DownloaderGUI
- **Fungsi**: Main application window dan UI logic
- **Features**: Input validation, progress display, history management
- **Threading**: Background downloads dengan SwingUtilities untuk UI updates

## Cara Kompilasi dan Menjalankan

### Prerequisites
- Java Development Kit (JDK) 17 atau lebih tinggi
- Koneksi internet (untuk auto-download yt-dlp)

### Langkah Kompilasi
```bash
# Clone atau download source code
cd prognet-tim-savefrom-tapi-java

# Compile semua file Java
javac *.java

# Jalankan aplikasi
java Main
```

### Struktur Directory Setelah Running
```
prognet-tim-savefrom-tapi-java/
├── bin/                    # Auto-downloaded executables
│   ├── yt-dlp.exe         # Windows executable
│   └── yt-dlp             # Linux/Mac executable
├── downloads/              # Default download directory
├── *.java                 # Source files
├── *.class                # Compiled bytecode
└── README.md              # Dokumentasi ini
```

## Cara Penggunaan

### 1. Download File Biasa
1. Paste URL file ke field "Download Link"
2. Klik "Browse" untuk memilih folder tujuan
3. Masukkan nama file (tanpa extension)
4. Pilih format jika diperlukan
5. Klik "Download" untuk memulai

### 2. Download Video YouTube
1. Paste URL YouTube (youtube.com atau youtu.be)
2. Pilih folder tujuan
3. **Nama file opsional** - kosongkan untuk menggunakan judul video otomatis
4. Pilih format: MP4 (video) atau MP3 (audio saja)
5. Aplikasi akan otomatis download yt-dlp jika belum ada
6. Jika nama kosong, aplikasi akan mengekstrak judul video dan menggunakannya sebagai nama file
7. Progress akan ditampilkan real-time

### 3. Manajemen Download
- **Cancel**: Klik tombol "Cancel" untuk menghentikan download
- **History**: Lihat riwayat download di panel bawah
- **Open File**: Double-click atau gunakan tombol "Buka File"
- **Open Folder**: Gunakan tombol "Buka Folder" untuk navigasi

## Fitur Keamanan

### Process Management
- Isolated process execution untuk yt-dlp
- Proper resource cleanup pada cancellation
- Process tree termination untuk mencegah orphaned processes

### Input Validation
- URL format validation
- File path sanitization
- Extension validation untuk security

### Error Recovery
- Graceful fallback mechanisms
- Partial file cleanup pada cancellation
- Network error retry dengan exponential backoff

---

## Instalasi Manual yt-dlp (Optional)

> **Catatan**: Aplikasi sudah memiliki fitur auto-download yt-dlp. Bagian ini hanya diperlukan jika auto-download gagal.

Jika auto-download tidak bekerja, Anda bisa install manual:

## 1) Ubuntu (22.04/24.04 dan turunan)

### Opsi A — Paket Resmi

```bash
sudo apt update
sudo apt install -y yt-dlp ffmpeg
```

**Cek versi:**

```bash
yt-dlp --version
ffmpeg -version
```

> Catatan: Paket `yt-dlp` di repo Ubuntu cukup baru untuk kebanyakan kasus. Bila Anda butuh versi **terbaru sekali**, gunakan Opsi B/C di bawah.

### Opsi B — `pipx`

**Install pipx (sekali saja):**

```bash
sudo apt update
sudo apt install -y pipx
pipx ensurepath
# Tutup & buka terminal lagi, atau run:
source ~/.bashrc 2>/dev/null || true
```

**Install yt-dlp via pipx:**

```bash
pipx install yt-dlp
```

**Install ffmpeg via apt:**

```bash
sudo apt install -y ffmpeg
```

**Cek versi:**

```bash
~/.local/bin/yt-dlp --version   # bila PATH belum ter-reload
yt-dlp --version                # setelah PATH aktif
ffmpeg -version
```

### Opsi C — Binary Standalone

Unduh binary terbaru dan taruh di `/usr/local/bin`:

```bash
sudo curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp   -o /usr/local/bin/yt-dlp
sudo chmod a+rx /usr/local/bin/yt-dlp
```

**Install ffmpeg:**

```bash
sudo apt install -y ffmpeg
```

**Cek versi:**

```bash
yt-dlp --version
ffmpeg -version
```

### Update ke versi terbaru

- **apt**: `sudo apt update && sudo apt install yt-dlp`
- **pipx**: `pipx upgrade yt-dlp`
- **binary**: jalankan `sudo yt-dlp -U` (self-update), atau ulangi perintah **Opsi C**.

### Uninstall

- **apt**: `sudo apt remove yt-dlp`
- **pipx**: `pipx uninstall yt-dlp`
- **binary**: `sudo rm /usr/local/bin/yt-dlp`

---

## 2) Windows 10/11

> Gunakan salah satu dari paket manajer berikut (disarankan), atau metode portable.

### Opsi A — **winget** (built-in pada Windows modern)

Buka **PowerShell** sebagai user biasa:

```powershell
winget install --id=yt-dlp.yt-dlp -e
winget install --id=Gyan.FFmpeg -e
```

Jika paket FFmpeg di atas tidak tersedia, coba:

```powershell
winget install --id=FFmpeg.FFmpeg -e
```

**Cek versi:**

```powershell
yt-dlp --version
ffmpeg -version
```

### Opsi B — **Chocolatey**

Buka **PowerShell As Administrator**, lalu:

```powershell
choco install yt-dlp -y
choco install ffmpeg -y
```

**Cek versi:**

```powershell
yt-dlp --version
ffmpeg -version
```

### Opsi C — **Scoop**

Di **PowerShell** (user biasa), pasang Scoop (jika belum):

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex
```

Lalu install:

```powershell
scoop install yt-dlp ffmpeg
```

**Cek versi:**

```powershell
yt-dlp --version
ffmpeg -version
```

### Opsi D — **Portable (tanpa installer)**

1. Buat folder, misal: `C:\tools\yt-dlp`
2. Unduh **yt-dlp.exe** (release terbaru) ke folder tersebut.
3. Unduh **FFmpeg** build untuk Windows (zip), ekstrak `ffmpeg.exe` (dan dependency) ke folder yang sama atau ke `C:\tools\ffmpeg\bin`.

**Tambahkan ke PATH** (agar bisa dipanggil dari PowerShell/CMD):

- Buka **Start** → cari **"Edit the system environment variables"** → **Environment Variables**.
- Pada **User variables** pilih **Path** → **Edit** → **New**:
  - `C:\tools\yt-dlp`
  - `C:\tools\ffmpeg\bin` _(jika ffmpeg diletakkan di sana)_
- **OK** semua dialog, **tutup** dan **buka ulang** PowerShell/CMD.

**Cek versi:**

```powershell
yt-dlp --version
ffmpeg -version
```

### Update & Uninstall (Windows)

- **winget**: `winget upgrade yt-dlp.yt-dlp` / `winget upgrade FFmpeg.FFmpeg`  
  Uninstall: `winget uninstall yt-dlp.yt-dlp`
- **choco**: `choco upgrade yt-dlp` / `choco upgrade ffmpeg`  
  Uninstall: `choco uninstall yt-dlp`
- **scoop**: `scoop update yt-dlp ffmpeg`  
  Uninstall: `scoop uninstall yt-dlp ffmpeg`
- **portable**: ganti file `.exe` dengan yang baru / hapus folder dari PATH.

---

## 3) Verifikasi & Troubleshooting

### Verifikasi cepat

```bash
yt-dlp --version
ffmpeg -version
```

- Keduanya harus menampilkan versi (bukan “command not found”/“is not recognized”).

## Troubleshooting & FAQ

### Masalah Umum

**Q: Aplikasi tidak bisa download YouTube**
- A: Aplikasi akan otomatis download yt-dlp pada penggunaan pertama. Pastikan koneksi internet stabil.

**Q: Download terhenti/gagal**
- A: Gunakan tombol "Cancel" lalu coba kembali. Periksa koneksi internet dan space disk.

**Q: File tidak bisa dibuka dari history**
- A: Pastikan file masih ada di lokasi yang sama. Gunakan "Buka Folder" untuk navigasi manual.

**Q: Error "cannot run program"**
- A: yt-dlp belum terinstall dengan benar. Hapus folder `bin/` dan coba download YouTube lagi.

### Performance Tips

- Untuk download besar, pastikan ada cukup space disk
- Cancel download yang tidak diperlukan untuk menghemat bandwidth
- Gunakan format MP3 untuk audio-only downloads (ukuran lebih kecil)

### Limitasi

- Memerlukan Java 17+ untuk pattern matching features
- Beberapa platform video mungkin memerlukan cookies atau authentication
- Rate limiting dari server target dapat membatasi kecepatan download

## Lisensi & Kontribusi

Aplikasi ini dikembangkan untuk keperluan edukasi dan penggunaan personal. Untuk kontribusi:

1. Fork repository ini
2. Buat feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## Changelog

### v1.1.0 (Current)
- ✅ **NEW**: Optional filename untuk YouTube - otomatis gunakan judul video
- ✅ **NEW**: Automatic video title extraction dengan sanitasi nama file
- ✅ **NEW**: Real-time UI update saat judul diekstrak
- ✅ Universal file downloader dengan progress tracking
- ✅ YouTube download support dengan yt-dlp integration
- ✅ Auto-download yt-dlp capability
- ✅ Modern Swing UI dengan custom components
- ✅ Download history dengan quick actions
- ✅ Aggressive cancellation untuk reliable stop functionality
- ✅ Comprehensive error handling dengan Indonesian messages

---

**Dikembangkan dengan ❤️ menggunakan Java Swing**
