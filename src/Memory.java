public class Memory {

    byte[] memory = new byte[1048576]; // 1 MiB in a byte array

    public void write(int address, int word, int bytes) {
        for (int i = 0; i < bytes; i++) {
            // Casting from int to byte cuts off all but the bottom 8 bits making this a
            // valid way to cut up the integer to bytes
            memory[address + i] = (byte) (word >> (i * 8));
        }
    }

    public void write(int address, byte[] data) {
        for (int i = 0; i < data.length; i++) {
            memory[address+i] = data[i];
        }
    }

    public byte readByte(int address) {
        return memory[address];
    }

    // Reading the byte as a word just means returning an integer which is 32 bits
    public int readByteAsWord(int address, boolean signExtend) {
        if (signExtend)
            return memory[address]; // When casting from a byte to an Int, Java sign-extends
        else
            return Byte.toUnsignedInt(memory[address]); // To avoid sign-extention
    }

    public short readHalfWord(int address) {
        // We need to convert the byte to an unsigned integer as java would otherwise
        // automatically convert it to a sign-extended one, making the 'or' operation bad
        return (short) ((memory[address + 1] << 8) | Byte.toUnsignedInt(memory[address]));
    }

    public int readHalfWordAsWord(int address, boolean signExtend) {
        //We reuse readByteAsWord as it then handles the sign extension problems
        int out = readByteAsWord(address+1, signExtend);
        return ((out << 8) | Byte.toUnsignedInt(memory[address]));
    }

    public int readWord(int address) {
        return (memory[address + 3] << 24) //We remember to use .toUnsignedInt so that it doesnt sign-extend the bytes before 'or'-ing
                | (Byte.toUnsignedInt(memory[address + 2]) << 16)
                | (Byte.toUnsignedInt(memory[address + 1]) << 8)
                | (Byte.toUnsignedInt(memory[address]));
    }
}
