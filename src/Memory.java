public class Memory {

    byte[] memory = new byte[1048576]; // 1 MiB

    public void write(int address, int word, int bytes) {
        for (int i = 0; i < bytes; i++) {
            memory[address + i] = (byte) (word >> (i * 8));
        }
    }

    public byte readByte(int address) {
        return memory[address];
    }

    public int readByteAsWord(int address, boolean signExtend) {
        if (signExtend) {
            return memory[address];
        } else {
            return Byte.toUnsignedInt(memory[address]);
        }
    }

    public short readHalfWord(int address) {
        return (short) ((memory[address + 1] << 8) | Byte.toUnsignedInt(memory[address]));
    }

    public int readHalfWordAsWord(int address, boolean signExtend) {
        int out;
        if (signExtend) {
            out = memory[address + 1];

        } else {
            out = Byte.toUnsignedInt(memory[address + 1]);
        }

        return ((out << 8) | Byte.toUnsignedInt(memory[address]));
    }

    public int readWord(int address) {
        return (memory[address + 3] << 24)
                | (Byte.toUnsignedInt(memory[address + 2]) << 16)
                | (Byte.toUnsignedInt(memory[address + 1]) << 8)
                | (Byte.toUnsignedInt(memory[address]));
    }

    public void lb(int regAddrs, int memAddrs) {
        if (memory[memAddrs] < 0) {
            // writeWord(regAddrs, (0xFFFFFF00 | memory[memAddrs]));
        } else {
            // writeWord(regAddrs, (0x000000FF & memory[memAddrs]));
        }
    }

    public void lh(int regAddrs, int memAddrs) {
        if (memory[memAddrs] < 0) {
            // writeWord(regAddrs,
            // (0xFFFF0000 | ((0x000000FF & memory[memAddrs]) |
            // (0x0000FF00 & memory[memAddrs + 1] << 8))));
        } else {
            // writeWord(regAddrs,
            // (0x000000FF & memory[memAddrs]) |
            // (0x0000FF00 & (memory[memAddrs + 1] << 8)));
        }
    }

    public void lw(int regAddrs, int memAddrs) {
        // writeWord(regAddrs, bytesToWord(memAddrs, memory));
    }

    public void lbu(int regAddrs, int memAddrs) {
        // writeWord(regAddrs, (0x000000FF & memory[memAddrs]));
    }

    public void lhu(int regAddrs, int memAddrs) {

        // writeWord(regAddrs,
        // (0x000000FF & memory[memAddrs]) |
        // (0x0000FF00 & (memory[memAddrs + 1] << 8)));
    }
}
