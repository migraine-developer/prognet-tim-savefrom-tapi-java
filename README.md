
# Instalasi **yt-dlp**
---

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
  - `C:\tools\ffmpeg\bin` *(jika ffmpeg diletakkan di sana)*
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

### Masalah umum & solusi
- **Command not found / not recognized**  
  → Pastikan PATH benar atau sesi terminal baru dibuka setelah instalasi.
- **`ffmpeg not found` saat konversi MP3**  
  → Pastikan `ffmpeg` terinstall & ada di PATH.
- **Akses ditolak (Linux)**  
  → Pastikan binary memiliki permission eksekusi: `chmod +x /usr/local/bin/yt-dlp`.
- **SSL / sertifikat / jaringan**  
  → Coba jaringan lain, update sistem, atau set proxy sesuai lingkungan kampus/kantor.
- **Rate limit / throttling**  
  → Coba ulang beberapa saat kemudian; hindari unduhan paralel berlebihan.

---