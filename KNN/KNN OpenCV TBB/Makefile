# I am a comment, and I want to say that the variable CC will be
# the compiler to use.
CC=g++
# Hey!, I am comment number 2. I want to say that CFLAGS will be the
# options I'll pass to the compiler.
CFLAGS=-c -Wall

all: knn

knn:
	$(CC) KNN.cpp -o knn `pkg-config --cflags --libs opencv`

clean:
	rm knn