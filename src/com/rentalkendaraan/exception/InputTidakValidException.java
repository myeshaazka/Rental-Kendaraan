package com.rentalkendaraan.exception;

/**
 * Exception yang dilempar ketika data masukan pengguna tidak valid
 * (kosong, negatif, nol, format salah, dan sebagainya).
 *
 * <p>Exception ini merupakan <i>unchecked exception</i> agar signature method
 * pada kontrak {@code hitungSewa(int)} dan {@code prosesSewa()} tetap bersih
 * dan bisa di-override tanpa menambahkan klausa {@code throws}.</p>
 */
public class InputTidakValidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Nama field/atribut yang menyebabkan kesalahan validasi. */
    private final String namaField;

    public InputTidakValidException(String pesan) {
        super(pesan);
        this.namaField = "-";
    }

    public InputTidakValidException(String namaField, String pesan) {
        super(pesan);
        this.namaField = namaField;
    }

    public String getNamaField() {
        return namaField;
    }
}
