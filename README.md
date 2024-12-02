# 02155_G49
Den besdte rics5 simulatår

# Install and run
In order to compile and run the code:

### Linux:
Use the makefile. As shown here:

    make all
    java Sim [inputfile]

### Windows:
If you have [make for windows](https://gnuwin32.sourceforge.net/packages/make.htm) installed, you can use make as with Linux. 
Else these commands also work:
    
    javac -d ./ src/Sim.java src/Memory.java src/Registers.java
    java Sim [inputfile]





<br><br><br><br><br>

Used https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java as a template. Copied

MNEMOMIC    OPDOCE      FUNCT3      FUNC7
lb          0000011     000
lh          0000011     001
lw          0000011     010

lbu         0000011     100
lhu         0000011     101

addi        0010011     000
slli        0010011     001
slti        0010011     010
sltiu       0010011     011
xori        0010011     100
srli        0010011     101         0000000
srai        0010011     101         0100000
ori         0010011     110
andi        0010011     111
auipc       0010111     

sb          0100011     000
sh          0100011     001
sw          0100011     010

add         0110011     000         0000000
sub         0110011     000         0100000
sll         0110011     001
slt         0110011     010
sltu        0110011     011
xor         0110011     100
srl         0110011     101         0000000
sra         0110011     101         0100000
or          0110011     110
and         0110011     111
lui         0110111


beq         1100011     000
bne         1100011     001
blt         1100011     100
bge         1100011     101
bltu        1100011     110
bgeu        1100011     111
jalr        1100111     000
jal         1101111
ecall       1110011     000         0
