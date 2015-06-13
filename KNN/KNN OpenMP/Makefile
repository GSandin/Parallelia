# I am a comment, and I want to say that the variable CC will be
# the compiler to use.
CC=g++
# Hey!, I am comment number 2. I want to say that CFLAGS will be the
# options I'll pass to the compiler.
CFLAGS=-c -Wall -ansi -pedantic -Dcimg_use_vt100
CMP=-I/usr/X11R6/include  -lm -L/usr/X11R6/lib -lpthread -lX11 -fopenmp 
all: knn

knn: main.o knn.o KNNVector.o
	$(CC) main.o knn.o KNNVector.o -o knn $(CMP) `pkg-config --cflags --libs opencv`

main.o: main.cpp
	$(CC) $(CFLAGS) main.cpp

knn.o: knn.cpp
	$(CC) $(CFLAGS) $(CMP) knn.cpp

KNNVector.o: KNNVector.cpp
	$(CC) $(CFLAGS) KNNVector.cpp

clean:
	rm *o knn