/**
 * Class TugasTransaksi ini implements interface Runnable
 * Jadi ini adalah "tugas" yang nanti bakal dijalankan sama Thread
 * - Thread itu kayak jalur eksekusi terpisah dalam program
 * - Beberapa thread bisa jalan bersamaan (concurrent/paralel)
 * - Interface Runnable punya method run() yang isinya kode yang mau dijalankan thread
 * 
 * Jadi class ini merepresentasikan satu tugas transaksi (tarik atau setor) yang bakal dijalankan oleh thread tertentu
 */
public class TugasTransaksi implements Runnable {
    private final AkunBank akun;    // Akun mana yang mau ditransaksikan
    private final double jumlah;     // Berapa nominalnya
    private final String jenis;      // Jenisnya apa: "tarik" atau "setor"

    public TugasTransaksi(AkunBank akun, double jumlah, String jenis) {
        this.akun = akun;
        this.jumlah = jumlah;
        this.jenis = jenis;
    }

    /**
     * Method run() ini dari interface Runnable
     * Otomatis dipanggil pas kita panggil thread.start()
     * 
     * Jadi prosesnya:
     * 1. Thread cek dulu jenis transaksinya apa
     * 2. Kalau "tarik", panggil method tarik() dari AkunBank
     * 3. Kalau "setor", panggil method setor() dari AkunBank
     * 4. Method-method itu synchronized, jadi cuma satu thread yang bisa masuk
     */
    @Override
    public void run() {
        // equalsIgnoreCase itu biar case-insensitive (TARIK, tarik, Tarik, semua dianggap sama)
        if ("tarik".equalsIgnoreCase(jenis)) {
            akun.tarik(jumlah);  // Panggil method synchronized tarik()
        } else if ("setor".equalsIgnoreCase(jenis)) {
            akun.setor(jumlah);  // Panggil method synchronized setor()
        } else {
            // Kalau jenis transaksi ga valid
            System.out.println("[" + Thread.currentThread().getName() 
                + "] Jenis transaksi tidak dikenal: " + jenis);
        }
    }
}
