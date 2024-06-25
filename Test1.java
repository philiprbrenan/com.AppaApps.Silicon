//------------------------------------------------------------------------------
// Test Chip.j
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

final class Test1 extends Chip                                                  // Describe a chip and emulate its operation.
 {static void test_fibonacci()                                                  // First few fibonacci numbers
   {final int N = 8, D = 22;                                                    // Number of bits in number, wait time to allow latest number to be computed from prior two
    Chip        C = new Chip();                                                 // Create a new chip
    Bits     zero = C.bits ("zero", N, 0);                                      // Zero - the first element of the sequence
    Bits      one = C.bits ("one",  N, 1);                                      // One - the second element of the sequence
    Pulse      ia = C.pulse("ia", 0,   D);                                      // Initialize the registers to their starting values
    Pulse      ib = C.pulse("ib", 0, 2*D);
    Pulse      la = C.pulse("la", 3*D, D, 0*D);                                 // Each pair sum is calculated on a rotating basis
    Pulse      lb = C.pulse("lb", 3*D, D, 1*D);
    Pulse      lc = C.pulse("lc", 3*D, D, 2*D);

    Bits       ab = C.bits("ab", N);                                            // Pre-declare the output of the pair sums so that we can use these buses to drive the registers holding the latest fibonacci numbers
    Bits       ac = C.bits("ac", N);
    Bits       bc = C.bits("bc", N);

    Register    a = C.register("a", bc, la);                                    // Registers holding the latest fibonacci number
    Register    b = C.register("b", ac, lb);
    Register    c = C.register("c", ab, lc);

    OutputUnit fa = C.new OutputUnit("fa", "f", a, la);                         // Log latest number
    OutputUnit fb = C.new OutputUnit("fb", "f", b, lb);
    OutputUnit fc = C.new OutputUnit("fc", "f", c, lc);

    BinaryAdd sbc = C.binaryAdd("sbc", b, c);                                   // a
    BinaryAdd sac = C.binaryAdd("sac", a, c);                                   // b
    BinaryAdd sab = C.binaryAdd("sab", a, b);                                   // c
    sab.carry().anneal(); sac.carry().anneal(); sbc.carry().anneal();                 // Ignore the carry bits

    Bits       BC = C.chooseFromTwoWords("bc", sbc.sum(), zero, ia);            // a
    Bits       AC = C.chooseFromTwoWords("ac", sac.sum(), one,  ib);            // b
    Bits       AB = C.continueBits      ("ab", sab.sum());                      // c

    C.executionTrace(
      "ia la ib lb lc   a         b         c",
      "%s  %s  %s  %s  %s    %s  %s  %s",
      ia, la, ib, lb, lc, a, b, c);
    C.simulationSteps(380);
    C.simulate();
    fa.ok(null, null, null, 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233);
    //C.printExecutionTrace(); stop();
    C.ok("""
Step  ia la ib lb lc   a         b         c
   1  1  1  1  0  0    ........  ........  ........
  23  0  0  1  1  0    ........  ........  ........
  25  0  0  1  1  0    00000000  ........  ........
  45  0  0  0  0  1    00000000  ........  ........
  47  0  0  0  0  1    00000000  00000001  ........
  67  0  1  0  0  0    00000000  00000001  ........
  69  0  1  0  0  0    00000000  00000001  00000001
  89  0  0  0  1  0    00000000  00000001  00000001
  91  0  0  0  1  0    00000010  00000001  00000001
 111  0  0  0  0  1    00000010  00000001  00000001
 113  0  0  0  0  1    00000010  00000011  00000001
 133  0  1  0  0  0    00000010  00000011  00000001
 135  0  1  0  0  0    00000010  00000011  00000101
 155  0  0  0  1  0    00000010  00000011  00000101
 157  0  0  0  1  0    00001000  00000011  00000101
 177  0  0  0  0  1    00001000  00000011  00000101
 179  0  0  0  0  1    00001000  00001101  00000101
 199  0  1  0  0  0    00001000  00001101  00000101
 201  0  1  0  0  0    00001000  00001101  00010101
 221  0  0  0  1  0    00001000  00001101  00010101
 223  0  0  0  1  0    00100010  00001101  00010101
 243  0  0  0  0  1    00100010  00001101  00010101
 245  0  0  0  0  1    00100010  00110111  00010101
 265  0  1  0  0  0    00100010  00110111  00010101
 267  0  1  0  0  0    00100010  00110111  01011001
 287  0  0  0  1  0    00100010  00110111  01011001
 289  0  0  0  1  0    10010000  00110111  01011001
 309  0  0  0  0  1    10010000  00110111  01011001
 311  0  0  0  0  1    10010000  11101001  01011001
 331  0  1  0  0  0    10010000  11101001  01011001
 333  0  1  0  0  0    10010000  11101001  11111001
 353  0  0  0  1  0    10010000  11101001  11111001
 355  0  0  0  1  0    11100010  11101001  11111001
 375  0  0  0  0  1    11100010  11101001  11111001
 377  0  0  0  0  1    11100010  11011011  11111001
""");
   }

  public static void main(String[] args)                                        // Test if called as a program
   {test_fibonacci();
    Chip.testSummary();
   }
 }
