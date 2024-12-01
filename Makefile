
all:
	javac src/Simulator.java src/Memory.java src/Registers.java
	java Simulator

clean:
	rm *.class
