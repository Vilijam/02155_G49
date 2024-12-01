public class Memory {

    byte[] memory;

    public void lb(int regAddrs, int memAddrs) {
        if (dataMem[memAddrs] < 0) {
            writeWord(regAddrs, (0xFFFFFF00 | dataMem[memAddrs]));
        } else {
            writeWord(regAddrs, (0x000000FF & dataMem[memAddrs]));
        }
    }

    public void lh(int regAddrs, int memAddrs) {
        if (dataMem[memAddrs] < 0) {
            writeWord(regAddrs,
                    (0xFFFF0000 | ((0x000000FF & dataMem[memAddrs]) |
                            (0x0000FF00 & dataMem[memAddrs + 1] << 8))));
        } else {
            writeWord(regAddrs,
                    (0x000000FF & dataMem[memAddrs]) |
                            (0x0000FF00 & (dataMem[memAddrs + 1] << 8)));
        }
    }

    public void lw(int regAddrs, int memAddrs) {
        writeWord(regAddrs, bytesToWord(memAddrs, dataMem));
    }

    public void lbu(int regAddrs, int memAddrs) {
        writeWord(regAddrs, (0x000000FF & dataMem[memAddrs]));
    }

    public void lhu(int regAddrs, int memAddrs) {

        writeWord(regAddrs,
                (0x000000FF & dataMem[memAddrs]) |
                        (0x0000FF00 & (dataMem[memAddrs + 1] << 8)));
    }
}
