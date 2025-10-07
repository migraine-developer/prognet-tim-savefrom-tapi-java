/**
 * Enumerates download error categories that the UI can reason about without
 * tying the implementation to any specific provider (e.g. YouTube).
 */
public enum DownloadError {
    NETWORK_INTERRUPTED,
    INSUFFICIENT_STORAGE,
    PERMISSION_DENIED,
    GENERAL_FAILURE;

    public String toUserMessage() {
        return switch (this) {
            case NETWORK_INTERRUPTED -> "Koneksi terputus saat mengunduh.";
            case INSUFFICIENT_STORAGE -> "Ruang penyimpanan tidak mencukupi.";
            case PERMISSION_DENIED -> "Tidak memiliki izin untuk menulis di lokasi tujuan.";
            case GENERAL_FAILURE -> "Gagal mengunduh dari URL.";
        };
    }
}
