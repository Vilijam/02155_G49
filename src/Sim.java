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
        byte[] resReg; //Byte array to hold the expected register data
        Registers reg = new Registers(); //We create objects for the register and memory abstractions
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

            //We declare the different possible fields of the instruction to later populate with the correct bits
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
                opcode = (instruction) & 0x7f;          // matching 0000 0000 0000 0000 0000 0000 0111 1111
                rd = (instruction >> 7) & 0x1f;         // matching 0000 0000 0000 0000 0000 1111 1000 0000
                funct3 = (instruction >> 12) & 0x7;     // matching 0000 0000 0000 0000 0111 0000 0000 0000
                rs1 = (instruction >> 15) & 0x1f;       // matching 0000 0000 0000 1111 1000 0000 0000 0000
                rs2 = (instruction >> 20) & 0x1f;       // matching 0000 0001 1111 0000 0000 0000 0000 0000
                funct7 = (instruction >> 25) & 0x7f;    // matching 1111 1110 0000 0000 0000 0000 0000 0000

                //The different immediate formats get constructed
                iFormat_Imm = (instruction >> 20);
                
                uFormat_Imm = (instruction & 0xFFFFF000); //No need to shift the bits as this is for the upper immediate
                
                sFormat_Imm = rd | (((instruction & 0xFE000000) >> 20)); //We reuse the rd value as it is equal to part of the immediate we need
                
                //We grab the bits from the different parts of the instruction, gather them in one place to the left and then shift the final value right
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

                /*
                 * The Switch Statement
                 */
                switch (opcode) {
                    case 0b0000011: // Load instructions
                        switch (funct3) {
                            case 0b000:
                                reg.writeWord(rd, memory.readByteAsWord(reg.read(rs1) + iFormat_Imm, true)); //lb
                                break;
                            case 0b001:
                                reg.writeWord(rd, memory.readHalfWordAsWord(reg.read(rs1) + iFormat_Imm, true)); //lh
                                break;
                            case 0b010:
                                reg.writeWord(rd, memory.readWord(reg.read(rs1) + iFormat_Imm)); //lw
                                break;
                            case 0b100:
                                reg.writeWord(rd, memory.readByteAsWord(reg.read(rs1) + iFormat_Imm, false)); //lbu
                                break;
                            case 0b101:
                                reg.writeWord(rd, memory.readHalfWordAsWord(reg.read(rs1) + iFormat_Imm, false)); //lhu
                                break;
                        }
                        break;

                    case 0b0010011: // Immediate instructions
                        switch (funct3) {
                            case 0b000:
                                reg.writeWord(rd, reg.read(rs1) + iFormat_Imm); //addi
                                break;
                            case 0b001:
                                reg.writeWord(rd, reg.read(rs1) << iFormat_Imm); //slli
                                break;
                            case 0b010:
                                reg.writeWord(rd, (reg.read(rs1) < iFormat_Imm) ? 1 : 0); //slti
                                break;
                            case 0b011:
                                reg.writeWord(rd,
                                        (Integer.compareUnsigned(reg.read(rs1), iFormat_Imm) < 0) ? 1 : 0); //sltiu
                                break;
                            case 0b100:
                                reg.writeWord(rd, reg.read(rs1) ^ iFormat_Imm); //xori
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        reg.writeWord(rd, reg.read(rs1) >>> iFormat_Imm); //srli
                                        break;
                                    case 0b0100000:
                                        reg.writeWord(rd, reg.read(rs1) >> iFormat_Imm); //srai
                                        break;
                                }
                                break;
                            case 0b110:
                                reg.writeWord(rd, reg.read(rs1) | iFormat_Imm); //ori
                                break;
                            case 0b111:
                                reg.writeWord(rd, reg.read(rs1) & iFormat_Imm); //andi
                                break;
                        }
                        break;

                    case 0b0010111: // U-type instruction
                        reg.writeWord(rd, pc + uFormat_Imm); //auipc
                        break;

                    case 0b0100011: // Store instructions
                        switch (funct3) {
                            case 0b000:
                                memory.write(reg.read(rs1) + sFormat_Imm, reg.read(rs2), 1); //sb
                                break;
                            case 0b001:
                                memory.write(reg.read(rs1) + sFormat_Imm, reg.read(rs2), 2); //sh
                                break;
                            case 0b010:
                               memory.write(reg.read(rs1) + sFormat_Imm, reg.read(rs2), 4); //sw
                                break;
                        }
                        break;

                    case 0b0110011: // R-type instructions
                        switch (funct3) {
                            case 0b000:
                                switch (funct7) {
                                    case 0b0000000:
                                        reg.writeWord(rd, reg.read(rs1) + reg.read(rs2)); //add
                                        break;
                                    case 0b0100000:
                                        reg.writeWord(rd, reg.read(rs1) - reg.read(rs2)); //sub
                                        break;
                                }
                                break;
                            case 0b001:
                                reg.writeWord(rd, reg.read(rs1) << reg.read(rs2)); //sll
                                break;
                            case 0b010:
                                reg.writeWord(rd, (reg.read(rs1) < reg.read(rs2)) ? 1 : 0); //slt
                                break;
                            case 0b011:
                                reg.writeWord(rd,
                                        (Integer.compareUnsigned(reg.read(rs1), reg.read(rs2)) < 0) ? 1 : 0); //sltu
                                break;
                            case 0b100:
                                reg.writeWord(rd, reg.read(rs1) ^ reg.read(rs2)); //xor
                                break;
                            case 0b101:
                                switch (funct7) {
                                    case 0b0000000:
                                        reg.writeWord(rd, reg.read(rs1) >>> reg.read(rs2)); //sal
                                        break;
                                    case 0b0100000:
                                        reg.writeWord(rd, reg.read(rs1) >> reg.read(rs2)); //sra
                                        break;
                                }
                                break;
                            case 0b110:
                                reg.writeWord(rd, reg.read(rs1) | reg.read(rs2)); //or
                                break;
                            case 0b111:
                                reg.writeWord(rd, reg.read(rs1) & reg.read(rs2)); //and
                                break;
                        }
                        break;

                    case 0b0110111: // U-type instruction
                        reg.writeWord(rd, (uFormat_Imm)); //lui
                        break;

                    case 0b1100011: // Branch instructions
                        switch (funct3) {
                            case 0b000:
                                if (reg.read(rs1) == reg.read(rs2)) { //beq
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b001:
                                if (reg.read(rs1) != reg.read(rs2)) { //bne
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                } 
                                break;
                            case 0b100:
                                if (reg.read(rs1) < reg.read(rs2)) { //blt
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b101:
                                if (reg.read(rs1) >= reg.read(rs2)) { //bge
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b110:
                                if (Integer.compareUnsigned(reg.read(rs1), reg.read(rs2)) < 0) { //bltu
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                            case 0b111:
                                if (Integer.compareUnsigned(reg.read(rs1), reg.read(rs2)) >= 0) { //bgeu
                                    jumping = true; 
                                    pc = pc + sbFormat_Imm; 
                                }
                                break;
                        }
                        break;

                    case 0b1100111: // JALR
                        switch (funct3) {
                            case 0b000:
                                reg.writeWord(rd, pc + 4); 
                                pc = reg.read(rs1) + iFormat_Imm;
                                jumping = true;
                                break;
                        }
                        break;

                    case 0b1101111: // JAL
                        reg.writeWord(rd, pc + 4);
                        //System.out.println(ujFormat_Imm);
                        pc = pc + ujFormat_Imm;
                        jumping = true;
                        break;

                    case 0b1110011: // System instructions
                        if (funct3 == 0b000 && funct7 == 0) { //ecall
                            running = false;
                        }
                        break;
                }
                if(jumping) jumping = false;
                 else pc += 4;
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
                
                System.out.println(((Arrays.compare(res1, res2) == 0)) ? "Comparison SUCCESS" : "Comparison FAIL");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }
}