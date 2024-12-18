import java.nio.file.Files;
import java.nio.file.Paths;

class Registers {

    byte[] reg;

    public Registers() {
        this.reg = new byte[128];

        this.reg[0] = 0;
        this.reg[1] = 0;
        this.reg[2] = 0;
        this.reg[3] = 0;
    }

    public int read(int index) {
        // Multiplying by 4 to get the word address
        // e.g. x5 is the address for the 5th word but the 20th byte
        return bytesToWord(index * 4, reg);
    }

    public void writeWord(int index, int value) {
        if (index != 0) { // x0 is always 0
            index = index * 4;
            reg[index] = (byte) value;
            reg[index + 1] = (byte) (value >> 8);
            reg[index + 2] = (byte) (value >> 16);
            reg[index + 3] = (byte) (value >> 24);
        }
    }


        /*
         * Collects the byte at given index in
         * given memory and the 3 subsequent bytes into a word.
         * Also, flips endianness.
         * The first byte in memory ends as least significant byte.
         * 
         * The & operator converts byte to int and extends sign.
         * Sign extension is counteracted.
         */

    static int bytesToWord(int index, byte[] mem) {
        int word = 0;

        word = (mem[index] & 0x000000FF)
                | ((mem[index + 1] << 8) & 0x0000FF00)
                | ((mem[index + 2] << 16) & 0x00FF0000)
                | ((mem[index + 3] << 24) & 0xFF000000);

        return word;
    }

    /*
     * Prints register contents to out.res file. Deletes previous contents
     */
    void writeRes() {
        try {
            Files.write(Paths.get("out.res"), reg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}