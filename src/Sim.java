import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class Sim {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide input file");
            System.exit(0);
        }

        String inputFile = args[0];
        byte[] programMem;
        byte[] dataMem = new byte[1024];
        Registers reg = new Registers(dataMem);

        try {
            programMem = Files.readAllBytes(Paths.get(inputFile));
            boolean notEOF = true;

            int instr;
            int opcode;
            int rd;
            int funct3;
            int rs1;
            int rs2;
            int funct7;

            /*
             * Det bliver besværligt at sikre fortegn
             */
            int iFormat_Imm;
            int sFormat_Imm;
            int sbFormat_Imm;
            int uFormat_Imm;
            int ujFormat_Imm;
            int pc = 0;

            while (notEOF) {

                /*
                 * Fetch Instruction
                 */
                instr = bytesToWord(pc, programMem);

                /*
                 * Decode Instruction
                 */
                opcode = (instr) & 0x7f; // matching 0000 0000 0000 0000 0000 0000 0111 1111
                rd = (instr >> 7) & 0x1f; // matching 0000 0000 0000 0000 0000 1111 1000 0000
                funct3 = (instr >> 12) & 0x7; // matching 0000 0000 0000 0000 0111 0000 0000 0000
                rs1 = (instr >> 15) & 0x1f; // matching 0000 0000 0000 1111 1000 0000 0000 0000
                rs2 = (instr >> 20) & 0x1f; // matching 0000 0001 1111 0000 0000 0000 0000 0000
                funct7 = (instr >> 25) & 0x7f; // matching 1111 1110 0000 0000 0000 0000 0000 0000
                // missing immediates

                // What is Java's default sign extension? casting byte to int does ext. sign.

                if (instr < 0) { // extending sign
                    iFormat_Imm = (instr >> 20) | 0xfffff000;
                } else {
                    iFormat_Imm = (instr >> 20) & 0xfff;
                }

                if (instr < 0) { // extending sign
                    uFormat_Imm = (instr >> 12) | 0xfff00000;
                } else {
                    uFormat_Imm = (instr >> 12) & 0xfffff;
                }

                // printInstruction(opcode, rd, funct3, rs1, rs2, funct7, iFormat_Imm,
                // uFormat_Imm);

                /*
                 * The Switch Statement
                 */
                switch (opcode) {
                    case 0b0000011: // Load instructions
                        switch (funct3) {
                            case 0b000:
                                System.out.println("lb");
                                reg.lb(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b001:
                                System.out.println("lh");
                                reg.lh(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b010:
                                System.out.println("lw");
                                reg.lw(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b100:
                                System.out.println("lbu");
                                reg.lbu(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b101:
                                System.out.println("lhu");
                                reg.lhu(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            default:
                                System.out.println("Unknown load instruction");
                                break;
                        }
                        break;

                    case 0b0010011: // Immediate instructions
                        switch (funct3) {
                            case 0b000:
                                System.out.println("addi");
                                reg.writeWord(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b001:
                                System.out.println("slli");
                                break;
                            case 0b010:
                                System.out.println("slti");
                                break;
                            case 0b011:
                                System.out.println("sltiu");
                                break;
                            case 0b100:
                                System.out.println("xori");
                                reg.writeWord(rd, reg.readWord(rs1) ^ iFormat_Imm);
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        System.out.println("srli");
                                        break;
                                    case 0b0100000:
                                        System.out.println("srai");
                                        break;
                                    default:
                                        System.out.println("Unknown shift immediate");
                                        break;
                                }
                                break;
                            case 0b110:
                                System.out.println("ori");
                                reg.writeWord(rd, reg.readWord(rs1) | iFormat_Imm);
                                break;
                            case 0b111:
                                System.out.println("andi");
                                reg.writeWord(rd, reg.readWord(rs1) & iFormat_Imm);
                                break;
                            default:
                                System.out.println("Unknown immediate instruction");
                                break;
                        }
                        break;

                    case 0b0010111: // U-type instruction
                        System.out.println("auipc");
                        break;

                    case 0b0100011: // Store instructions
                        switch (funct3) {
                            case 0b000:
                                System.out.println("sb");
                                break;
                            case 0b001:
                                System.out.println("sh");
                                break;
                            case 0b010:
                                System.out.println("sw");
                                break;
                            default:
                                System.out.println("Unknown store instruction");
                                break;
                        }
                        break;

                    case 0b0110011: // R-type instructions
                        switch (funct3) {
                            case 0b000:
                                switch (funct7) {
                                    case 0b0000000:
                                        System.out.println("add");
                                        reg.writeWord(rd, reg.readWord(rs1) + reg.readWord(rs2));
                                        break;
                                    case 0b0100000:
                                        System.out.println("sub");
                                        reg.writeWord(rd, reg.readWord(rs1) - reg.readWord(rs2));
                                        break;
                                    default:
                                        System.out.println("Unknown arithmetic operation");
                                        break;
                                }
                                break;
                            case 0b001:
                                System.out.println("sll");
                                reg.writeWord(rd, reg.readWord(rs1) << reg.readWord(rs2));
                                break;
                            case 0b010:
                                System.out.println("slt");
                                break;
                            case 0b011:
                                System.out.println("sltu");
                                break;
                            case 0b100:
                                System.out.println("xor");
                                reg.writeWord(rd, reg.readWord(rs1) ^ reg.readWord(rs2));
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        System.out.println("srl");
                                        break;
                                    case 0b0100000:
                                        System.out.println("sra");
                                        break;
                                    default:
                                        System.out.println("Unknown shift operation");
                                        break;
                                }
                                break;
                            case 0b110:
                                System.out.println("or");
                                reg.writeWord(rd, reg.readWord(rs1) | reg.readWord(rs2));
                                break;
                            case 0b111:
                                System.out.println("and");
                                reg.writeWord(rd, reg.readWord(rs1) & reg.readWord(rs2));
                                break;
                            default:
                                System.out.println("Unknown R-type instruction");
                                break;
                        }
                        break;

                    case 0b0110111: // U-type instruction
                        System.out.println("lui");
                        reg.writeWord(rd, (uFormat_Imm << 12));
                        break;

                    case 0b1100011: // Branch instructions
                        switch (funct3) {
                            case 0b000:
                                System.out.println("beq");
                                break;
                            case 0b001:
                                System.out.println("bne");
                                break;
                            case 0b100:
                                System.out.println("blt");
                                break;
                            case 0b101:
                                System.out.println("bge");
                                break;
                            case 0b110:
                                System.out.println("bltu");
                                break;
                            case 0b111:
                                System.out.println("bgeu");
                                break;
                            default:
                                System.out.println("Unknown branch instruction");
                                break;
                        }
                        break;

                    case 0b1100111: // JALR
                        switch (funct3) {
                            case 0b000:
                                System.out.println("jalr");
                                break;
                            default:
                                System.out.println("Unknown jalr instruction");
                                break;
                        }
                        break;

                    case 0b1101111: // JAL
                        System.out.println("jal");
                        break;

                    case 0b1110011: // System instructions
                        if (funct3 == 0b000 && funct7 == 0) {
                            System.out.println("ecall");
                            notEOF = false;
                        } else {
                            System.out.println("Unknown system instruction");
                        }
                        break;

                    default:
                        System.out.println("Unknown opcode");
                        break;
                }

                pc = pc + 4;

            }

            /*
             * Termination
             */

            // Print register contents
            for (int i = 0; i < 32; ++i) {
                System.out.println("Value at adress " + i + ":");
                System.out.println(reg.readWord(i));
                // System.out.println(Integer.toBinaryString(reg.readWord(i)));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    static int bytesToWord(int index, byte[] mem) {
        /*
         * Collects the byte at given index in
         * given memory and the 3 subsequent bytes into a word.
         * Also, flips endianness.
         * The first byte in memory ends as least significant byte.
         * 
         * The & operator converts byte to int and extends sign.
         * Sign extension is counteracted.
         */

        int word = 0;

        word = (mem[index] & 0x000000FF)
                | ((mem[index + 1] << 8) & 0x0000FF00)
                | ((mem[index + 2] << 16) & 0x00FF0000)
                | ((mem[index + 3] << 24) & 0xFF000000);

        return word;
    }

    static void printInstruction(int opcode, int rd, int funct3, int rs1, int rs2, int funct7, int iFormat_Imm,
            int uFormat_Imm) {
        System.out.println("Leading zeroes not printed!");

        System.out.println("opcode: \n" + Integer.toBinaryString(opcode));
        System.out.println("rd: \n" + Integer.toBinaryString(rd));
        System.out.println("funct3: \n" + Integer.toBinaryString(funct3));
        System.out.println("rs1: \n" + Integer.toBinaryString(rs1));
        System.out.println("rs2: \n" + Integer.toBinaryString(rs2));
        System.out.println("funct7: \n" + Integer.toBinaryString(funct7));
        System.out.println("iFormat_Imm: \n" + Integer.toBinaryString(iFormat_Imm));
        System.out.println("uFormat_Imm: \n" + Integer.toBinaryString(uFormat_Imm));
    }

}