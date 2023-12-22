#include <cstdlib>
#include <cstdio>
#include <fstream>
#include <iostream>
#include <iomanip>
#include <string>

#include "hilbert.hpp"

union double_bits
{
    double d;
    unsigned long bits;
};

bool check_bits(double d, unsigned long dbits)
{
    union double_bits dd;
    dd.d = d;
    return dd.bits == dbits;
}

void bit_error(int line, double d, unsigned long dbits)
{
    std::cerr << std::hex;
    std::cerr << "Bit error on line = " << line
              << " double = " << d << " bits = " << dbits << ' ';
    union double_bits dd;
    dd.d = d;
    std::cerr << "expected = " << dd.bits << '\n';
    abort();
}

int main(void)
{
    std::ifstream inp("hilbert-trace.log");
    int line = 0;
    while (!inp.eof())
    {
        line++;
        std::string str;
        std::getline(inp, str);
        double p1[2], p2[2];
        unsigned long p1bits[2], p2bits[2];
        int result;
        sscanf (str.c_str(), "%lg %lx %lg %lx %lg %lx %lg %lx %d",
                &p1[0], &p1bits[0], &p1[1], &p1bits[1],
                &p2[0], &p2bits[0], &p2[1], &p2bits[1], &result);
//        inp >> p1[0];
//        inp >> p1bits[0];
//        inp >> p1[1];
//        inp >> p1bits[1];
//        inp >> p2[0];
//        inp >> p2bits[0];
//        inp >> p2[1];
//        inp >> p2bits[1];
//        inp >> result;
        if (!check_bits (p1[0], p1bits[0]))
            bit_error (line, p1[0], p1bits[0]);
        if (!check_bits (p1[1], p1bits[1]))
            bit_error (line, p1[2], p1bits[1]);
        if (!check_bits (p2[0], p2bits[0]))
            bit_error (line, p2[0], p2bits[0]);
        if (!check_bits (p2[1], p2bits[1]))
            bit_error (line, p2[1], p2bits[1]);
        int correct_result = hilbert_ieee_cmp (2, p1, p2);
        if (correct_result != result)
        {
            std::cerr << "Hilbert comparisson error, line = " << line << '\n';
            std::cerr << "p1 = (" << p1[0] << ", " << p1[1] << ")\n";
            std::cerr << "p2 = (" << p2[0] << ", " << p2[1] << ")\n";
            std::cerr << "Result = " << result << '\n';
            std::cerr << "Expected result = " << correct_result << '\n';
            abort();
        }
    }
    std::cout << "Total lines = " << line << '\n';
    std::cout << "OK\n";
    return 0;
}
