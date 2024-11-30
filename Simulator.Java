import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
/**
 * Copy one file to another using low level byte streams, one byte at a time.
 * @author www.codejava.net
 */
public class Simulator {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide input file");
            System.exit(0);
        }

        String inputFile = args[0];
        byte[] programMem;

        try {
            programMem = Files.readAllBytes(Paths.get(inputFile));
            int memLen = programMem.length;
            
            int instr;
            int opcode;
            int rd;
            int funct3;
            int rs1;
            int rs2;
            int funct7;

            /*
                Det bliver besværligt at sikre fortegn 
            */
            int iFormat_Imm;
            int sFormat_Imm;
            int sbFormat_Imm;
            int uFormat_Imm;
            int ujFormat_Imm;
            int pc = 0; 

  
            while (true) {

                /* 
                    Fetch Instruction
                */
                instr = bytesToWord(pc, programMem);

                /*
                    Decode Instruction
                */
                opcode = (instr) & 0x7f;              // matching 0000 0000 0000 0000  0000 0000 0111 1111
                rd = (instr >> 7) & 0x1f;             // matching 0000 0000 0000 0000  0000 1111 1000 0000
                funct3 = (instr >> 12) & 0x7;         // matching 0000 0000 0000 0000  0111 0000 0000 0000
                rs1 = (instr >> 15) & 0x1f;           // matching 0000 0000 0000 1111  1000 0000 0000 0000
                rs2 = (instr >> 20) & 0x1f;           // matching 0000 0001 1111 0000  0000 0000 0000 0000
                funct7 = (instr >> 25 ) & 0x7f;       // matching 1111 1110 0000 0000  0000 0000 0000 0000
                // missing immediates
                iFormat_Imm = (instr >> 20 ) & 0xfff; // matching 1111 1111 1111 0000  0000 0000 0000 0000

                //printInstruction(opcode, rd, funct3, rs1, rs2, funct7, iFormat_Imm);

                /*
                    The Switch Statement
                */
                if (opcode == 0x73) {
                    System.out.println("ecall found POG");
                    break;
                }
                pc = pc + 4;
            
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    static int bytesToWord(int index, byte[] mem) {
        int word = 0;

        word = (mem[index] & 0x000000FF) | ((mem[index + 1] << 8) & 0x0000FF00) | ((mem[index + 2] << 16) & 0x00FF0000) | ((mem[index + 3] << 24) & 0xFF000000);

        return word;
    }

    static void printInstruction(int opcode, int rd, int funct3, int rs1, int rs2, int funct7, int iFormat_Imm) {
        System.out.println("Leading zeroes not printed!");
        
        System.out.println("opcode: \n" +       Integer.toBinaryString(opcode));
        System.out.println("rd: \n" +           Integer.toBinaryString(rd));
        System.out.println("funct3: \n" +       Integer.toBinaryString(funct3));
        System.out.println("rs1: \n" +          Integer.toBinaryString(rs1));
        System.out.println("rs2: \n" +          Integer.toBinaryString(rs2));
        System.out.println("funct7: \n" +       Integer.toBinaryString(funct7));
        System.out.println("iFormat_Imm: \n" +  Integer.toBinaryString(iFormat_Imm));
    }

}