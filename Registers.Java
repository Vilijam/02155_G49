class Registers {

    byte[] reg;
    byte[] dataMem;

    public Registers(byte[] dataMem) {
        this.reg = new byte[128];
        this.dataMem = dataMem;
        this.reg[0] = 0;
        this.reg[1] = 0;
        this.reg[2] = 0;
        this.reg[3] = 0;
    }

    public int readWord(int index) {
        return bytesToWord(index * 4, reg);
    }

    public void writeWord(int index, int value) {
        if (index != 0) { // x0 is always 0
        index = index * 4; // e.g. x5 is the adress for the 5th word but the 20th byte
            reg[index]      = (byte) value;
            reg[index + 1]  = (byte) (value >> 8);
            reg[index + 2]  = (byte) (value >> 16);
            reg[index + 3]  = (byte) (value >> 24);
        }
    }

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
            (0xFFFF0000 | (
                (0x000000FF & dataMem[memAddrs]) | 
                (0x0000FF00 & dataMem[memAddrs + 1] << 8)
                )
            ));
        } else {
            writeWord(regAddrs, 
                ( 0x000000FF & dataMem[memAddrs] ) | 
                ( 0x0000FF00 & (dataMem[memAddrs + 1] << 8) )
            );
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
            ( 0x000000FF & dataMem[memAddrs] ) | 
            ( 0x0000FF00 & (dataMem[memAddrs + 1] << 8) )
        );
    }
    
    public void lui(int regAddrs, int value) {
        writeWord(regAddrs, value << 12);
    }

    public void addi(int regAddrs, int value) {
        writeWord(regAddrs, value);
    }

    public void add(int regAddrs, int value) {
        writeWord(regAddrs, value);
    }

    static int bytesToWord(int index, byte[] mem) {
        int word = 0;

        word = (mem[index] & 0x000000FF) | ((mem[index + 1] << 8) & 0x0000FF00) | ((mem[index + 2] << 16) & 0x00FF0000) | ((mem[index + 3] << 24) & 0xFF000000);

        return word;
    }


}