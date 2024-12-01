
all:
	javac Simulator.java Memory.java Registers.java
	java Simulator

clean:
	rm *.class
