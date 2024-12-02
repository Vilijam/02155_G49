
all:
	javac -d ./ src/Sim.java src/Memory.java src/Registers.java

testall: test1 test2 test3 test4

test1: all
	java Sim tests/task1/addlarge
	java Sim tests/task1/addneg
	java Sim tests/task1/addpos
	java Sim tests/task1/bool
	java Sim tests/task1/set
	java Sim tests/task1/shift
	java Sim tests/task1/shift2

test2: all
	java Sim tests/task2/branchcnt
	java Sim tests/task2/branchmany
	java Sim tests/task2/branchtrap

test3: all
	java Sim tests/task3/loop
	java Sim tests/task3/recursive
	java Sim tests/task3/string
	java Sim tests/task3/width

test4: all
	java Sim tests/task4/t1
	java Sim tests/task4/t2
	java Sim tests/task4/t3
	java Sim tests/task4/t4
	java Sim tests/task4/t5
	java Sim tests/task4/t6
	java Sim tests/task4/t7
	java Sim tests/task4/t8
	java Sim tests/task4/t9
	java Sim tests/task4/t10
	java Sim tests/task4/t11
	java Sim tests/task4/t12
	java Sim tests/task4/t13
	java Sim tests/task4/t14
	java Sim tests/task4/t15

clean:
	rm *.class
