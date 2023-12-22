CXXFLAGS:=-Wall -W -O3 -g -march=native

hilbert-checker : hilbert-checker.o hilbert.o
	g++ hilbert-checker.o hilbert.o -o $@ -lstdc++
hilbert.o : hilbert.cpp hilbert.hpp
hilbert-checker.o : hilbert.cpp hilbert.hpp
