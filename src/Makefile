JFLAGS = -g
JC = javac
JV = java

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = UDPClient.java UDPServer.java Solver.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

server:
	$(JV) UDPServer

client:
	$(JV) UDPClient
