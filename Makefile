JCC = javac

server: class1

client: class1 

broker: class1

crawler: class1

class1: *.java
	$(JCC) *.java

clean:
	$(RM) *.class