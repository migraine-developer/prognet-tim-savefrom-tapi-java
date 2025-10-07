/**
 * @deprecated Progress dialog digantikan oleh indikator progress langsung di
 *             {@link DownloaderGUI}.
 *             Kelas ini dipertahankan hanya untuk kompatibilitas, namun tidak
 *             boleh dipakai lagi.
 */
@Deprecated
public final class ProgressGUI {

    private ProgressGUI() {
        throw new UnsupportedOperationException("Gunakan progress bawaan DownloaderGUI.");
    }
}
