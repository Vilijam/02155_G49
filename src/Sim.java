import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class Sim {

    static boolean DEBUG = false;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide input file");
            System.exit(0);
        }

        String inputFile = args[0];
        byte[] resReg;
        Registers reg = new Registers();
        Memory memory = new Memory();

        try {
            //Load the binary from disk
            byte[] program = Files.readAllBytes(Paths.get(inputFile + ".bin"));
            
            //We write the program to memory
            memory.write(0, program); //Programs start at address 0 to work with test, we dont have an operating system

            resReg = Files.readAllBytes(Paths.get(inputFile + ".res"));

        } catch (IOException ex) {
            ex.printStackTrace();
            return; //No reason to continue
        }

            boolean running = true;
            boolean jumping = false;

            int instruction;
            int opcode;
            int rd;
            int funct3;
            int rs1;
            int rs2;
            int funct7;

            int iFormat_Imm;
            int sFormat_Imm;
            int sbFormat_Imm;
            int uFormat_Imm;
            int ujFormat_Imm;
            int pc = 0;

            while (running) {

                /*
                 * Fetch Instruction from memory
                 */
                instruction = memory.readWord(pc);

                /*
                 * Decode Instruction
                 */
                opcode = (instruction) & 0x7f; // matching 0000 0000 0000 0000 0000 0000 0111 1111
                rd = (instruction >> 7) & 0x1f; // matching 0000 0000 0000 0000 0000 1111 1000 0000
                funct3 = (instruction >> 12) & 0x7; // matching 0000 0000 0000 0000 0111 0000 0000 0000
                rs1 = (instruction >> 15) & 0x1f; // matching 0000 0000 0000 1111 1000 0000 0000 0000
                rs2 = (instruction >> 20) & 0x1f; // matching 0000 0001 1111 0000 0000 0000 0000 0000
                funct7 = (instruction >> 25) & 0x7f; // matching 1111 1110 0000 0000 0000 0000 0000 0000

                iFormat_Imm = (instruction >> 20);
                uFormat_Imm = (instruction & 0xFFFFF000); //No need to shift the bits as this is for the upper immediate
                sFormat_Imm = rd | (((instruction & 0xFE000000) >> 20)); //We reuse the rd value as it is equal to part of the immediate we need
                sbFormat_Imm = (0x80000000 & instruction) // Grab bit 31
                        | (0x40000000 & (instruction << 23)) // Then bit 7
                        | (0x3F000000 & (instruction >> 1)) // Then bits 25-30
                        | (0x00F00000 & (instruction << 12)); // then 8-11
                sbFormat_Imm = sbFormat_Imm >> 19; // shift the whole thing over to the right, while signextending and
                                                   // preserving that the rightmost bit is always zero


                ujFormat_Imm = (0x80000000 & instruction) // Grab bit 31
                        | (0x7F800000 & (instruction << 11)) // Then bits 12-19
                        | (0x00400000 & (instruction << 2)) // Then bits 20
                        | (0x003FF000 & (instruction >> 9)); // then 30-21
                ujFormat_Imm = ujFormat_Imm >> 11; // shift the whole thing over to the right, while signextending and
                                                   // preserving that the rightmost bit is always zero
//System.out.println(Integer.toBinaryString(ujFormat_Imm));

                /*
                 * The Switch Statement
                 */
                switch (opcode) {
                    case 0b0000011: // Load instructions
                        switch (funct3) {
                            case 0b000:
                                printDebug("lb");
                                reg.writeWord(rd, memory.readByteAsWord(reg.readWord(rs1) + iFormat_Imm, true)); //lb
                                break;
                            case 0b001:
                                printDebug("lh");
                                reg.writeWord(rd, memory.readHalfWordAsWord(reg.readWord(rs1) + iFormat_Imm, true)); //lh
                                break;
                            case 0b010:
                                printDebug("lw, Reg#"+ rd + "= address:" +"Reg#" + rs1 + " :" + (reg.readWord(rs1) + iFormat_Imm));
                                reg.writeWord(rd, memory.readWord(reg.readWord(rs1) + iFormat_Imm)); //lw
                                printDebug("FROM LOAD: " + reg.readWord(rd));

                                break;
                            case 0b100:
                                printDebug("lbu");
                                reg.writeWord(rd, memory.readByteAsWord(reg.readWord(rs1) + iFormat_Imm, false)); //lbu
                                break;
                            case 0b101:
                                printDebug("lhu");
                                reg.writeWord(rd, memory.readHalfWordAsWord(reg.readWord(rs1) + iFormat_Imm, false)); //lhu
                                break;
                            default:
                                printDebug("Unknown load instruction");
                                break;
                        }
                        break;

                    case 0b0010011: // Immediate instructions
                        switch (funct3) {
                            case 0b000:
                                printDebug("addi, reg# " + rd + " = reg#" + rs1 + ": " + (reg.readWord(rs1) + iFormat_Imm));
                                reg.writeWord(rd, reg.readWord(rs1) + iFormat_Imm);
                                break;
                            case 0b001:
                                printDebug("slli");
                                reg.writeWord(rd, reg.readWord(rs1) << iFormat_Imm);
                                break;
                            case 0b010:
                                printDebug("slti");
                                reg.writeWord(rd, (reg.readWord(rs1) < iFormat_Imm) ? 1 : 0);
                                break;
                            case 0b011:
                                printDebug("sltiu");
                                reg.writeWord(rd,
                                        (Integer.compareUnsigned(reg.readWord(rs1), iFormat_Imm) < 0) ? 1 : 0);
                                break;
                            case 0b100:
                                printDebug("xori");
                                reg.writeWord(rd, reg.readWord(rs1) ^ iFormat_Imm);
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        printDebug("srli");
                                        reg.writeWord(rd, reg.readWord(rs1) >>> iFormat_Imm);
                                        break;
                                    case 0b0100000:
                                        printDebug("srai");
                                        reg.writeWord(rd, reg.readWord(rs1) >> iFormat_Imm);
                                        break;
                                    default:
                                        printDebug("Unknown shift immediate");
                                        break;
                                }
                                break;
                            case 0b110:
                                printDebug("ori");
                                reg.writeWord(rd, reg.readWord(rs1) | iFormat_Imm);
                                break;
                            case 0b111:
                                printDebug("andi");
                                reg.writeWord(rd, reg.readWord(rs1) & iFormat_Imm);
                                break;
                            default:
                                printDebug("Unknown immediate instruction");
                                break;
                        }
                        break;

                    case 0b0010111: // U-type instruction
                        printDebug("auipc");
                        reg.writeWord(rd, pc + uFormat_Imm);
                        break;

                    case 0b0100011: // Store instructions
                        switch (funct3) {
                            case 0b000:
                                printDebug("sb");
                                memory.write(reg.readWord(rs1) + sFormat_Imm, reg.readWord(rs2), 1); //sb
                                break;
                            case 0b001:
                                printDebug("sh");
                                memory.write(reg.readWord(rs1) + sFormat_Imm, reg.readWord(rs2), 2); //sh
                                break;
                            case 0b010:
                                printDebug("sw, Reg#"+ rs2 + ":" + reg.readWord(rs2) + " -> address:" +"Reg#" + rs1 + " :" + (reg.readWord(rs1) + sFormat_Imm));
                                memory.write(reg.readWord(rs1) + sFormat_Imm, reg.readWord(rs2), 4); //sw
                                break;
                            default:
                                printDebug("Unknown store instruction");
                                break;
                        }
                        break;

                    case 0b0110011: // R-type instructions
                        switch (funct3) {
                            case 0b000:
                                switch (funct7) {
                                    case 0b0000000:
                                        printDebug("add");
                                        reg.writeWord(rd, reg.readWord(rs1) + reg.readWord(rs2));
                                        break;
                                    case 0b0100000:
                                        printDebug("sub");
                                        reg.writeWord(rd, reg.readWord(rs1) - reg.readWord(rs2));
                                        break;
                                    default:
                                        printDebug("Unknown arithmetic operation");
                                        break;
                                }
                                break;
                            case 0b001:
                                printDebug("sll");
                                reg.writeWord(rd, reg.readWord(rs1) << reg.readWord(rs2));
                                break;
                            case 0b010:
                                printDebug("slt");
                                reg.writeWord(rd, (reg.readWord(rs1) < reg.readWord(rs2)) ? 1 : 0);
                                break;
                            case 0b011:
                                printDebug("sltu");
                                reg.writeWord(rd,
                                        (Integer.compareUnsigned(reg.readWord(rs1), reg.readWord(rs2)) < 0) ? 1 : 0);
                                break;
                            case 0b100:
                                printDebug("xor");
                                reg.writeWord(rd, reg.readWord(rs1) ^ reg.readWord(rs2));
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        printDebug("srl");
                                        reg.writeWord(rd, reg.readWord(rs1) >>> reg.readWord(rs2));
                                        break;
                                    case 0b0100000:
                                        printDebug("sra");
                                        reg.writeWord(rd, reg.readWord(rs1) >> reg.readWord(rs2));
                                        break;
                                    default:
                                        printDebug("Unknown shift operation");
                                        break;
                                }
                                break;
                            case 0b110:
                                printDebug("or");
                                reg.writeWord(rd, reg.readWord(rs1) | reg.readWord(rs2));
                                break;
                            case 0b111:
                                printDebug("and");
                                reg.writeWord(rd, reg.readWord(rs1) & reg.readWord(rs2));
                                break;
                            default:
                                printDebug("Unknown R-type instruction");
                                break;
                        }
                        break;

                    case 0b0110111: // U-type instruction
                        printDebug("lui");
                        reg.writeWord(rd, (uFormat_Imm));
                        break;

                    case 0b1100011: // Branch instructions
                        switch (funct3) {
                            case 0b000:
                                printDebug("beq");
                                if (reg.readWord(rs1) == reg.readWord(rs2)) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b001:
                                printDebug("bne");
                                if (reg.readWord(rs1) != reg.readWord(rs2)) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                } 
                                break;
                            case 0b100:
                                printDebug("blt");
                                if (reg.readWord(rs1) < reg.readWord(rs2)) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b101:
                                printDebug("bge");
                                if (reg.readWord(rs1) >= reg.readWord(rs2)) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b110:
                                printDebug("bltu");
                                if (Integer.compareUnsigned(reg.readWord(rs1), reg.readWord(rs2)) < 0) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b111:
                                printDebug("bgeu");
                                if (Integer.compareUnsigned(reg.readWord(rs1), reg.readWord(rs2)) >= 0) {
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            default:
                                printDebug("Unknown branch instruction");
                                break;
                        }
                        break;

                    case 0b1100111: // JALR
                        switch (funct3) {
                            case 0b000:
                                printDebug("jalr, reg# " + rd + " = pc:" + pc + ", pc=reg#"+ rs1 + ":" + reg.readWord(rs1) + " + iformat:" + iFormat_Imm);
                                reg.writeWord(rd, pc + 4);
                                pc = reg.readWord(rs1) + iFormat_Imm;
                                jumping = true;
                                break;
                            default:
                                printDebug("Unknown jalr instruction");
                                break;
                        }
                        break;

                    case 0b1101111: // JAL
                        printDebug("jal");
                        reg.writeWord(rd, pc + 4);
                        //System.out.println(ujFormat_Imm);
                        pc = pc + ujFormat_Imm;
                        jumping = true;
                        break;

                    case 0b1110011: // System instructions
                        if (funct3 == 0b000 && funct7 == 0) {
                            printDebug("ecall");
                            running = false;
                        } else {
                            printDebug("Unknown system instruction");
                        }
                        break;

                    default:
                        printDebug("Unknown opcode");
                        break;
                }

                if (jumping == false) {
                    pc = pc + 4; 
                } else {
                    jumping = false;
                }

            }

            /*
             * Termination
             */

            // Check register contents
            reg.writeRes();

            byte[] res1; 
            byte[] res2; 
            try {
                res1 = Files.readAllBytes(Paths.get("out.res"));
                res2 = Files.readAllBytes(Paths.get(inputFile + ".res"));
                
                if (Arrays.compare(res1, res2) == 0) {
                    System.out.println("Comparison SUCCESS");
                    
                } else {
                    System.out.println("Comparison FAILURE");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (DEBUG) {
                for (int i = 0; i < 32; ++i) {
                    if (reg.readWord(i) == bytesToWord(i * 4, resReg)) {

                    } else {
                        System.out.println("Error at address " + i + ":");
                        System.out.println("Our: " + reg.readWord(i));
                        System.out.println("Res: " + bytesToWord(i * 4, resReg));
                    }
                }
            }
    }

    static void printDebug(String s){
        if(DEBUG){
            System.out.println(s);
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
}