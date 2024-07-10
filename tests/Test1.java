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
   {final int N = 8, D = 24;                                                    // Number of bits in number, wait time to allow latest number to be computed from prior two
    Chip        C = chip();                                                     // Create a new chip
    Bits     zero = C.bits ("zero", N, 0);                                      // Zero - the first element of the sequence
    Bits      one = C.bits ("one",  N, 1);                                      // One - the second element of the sequence
    Pulse      ia = C.pulse("ia", 0,   D);                                      // Initialize the registers to their starting values
    Pulse      ib = C.pulse("ib", 0, 2*D);
    Pulse      la = C.pulse("la", 3*D, D, 0*D);                                 // Each pair sum is calculated on a rotating basis
    Pulse      lb = C.pulse("lb", 3*D, D, 1*D);
    Pulse      lc = C.pulse("lc", 3*D, D, 2*D);

    Pulse      La = C.pulse("La", la).start(1).b();                             // Delay for first value to be computed
    Pulse      Lb = C.pulse("Lb", lb).start(1).b();
    Pulse      Lc = C.pulse("Lc", lc).start(1).b();

    Register    a = C.register("a", N, la);                                     // Registers holding the latest fibonacci number
    Register    b = C.register("b", N, lb);
    Register    c = C.register("c", N, lc);

    OutputUnit fa = C.new OutputUnit("fa", "f", a, La);                         // Log latest number
    OutputUnit fb = C.new OutputUnit("fb", "f", b, Lb);
    OutputUnit fc = C.new OutputUnit("fc", "f", c, Lc);

    BinaryAdd sbc = C.binaryAdd("sbc", b, c);                                   // a
    BinaryAdd sac = C.binaryAdd("sac", a, c);                                   // b
    BinaryAdd sab = C.binaryAdd("sab", a, b);                                   // c
    sab.carry().anneal(); sac.carry().anneal(); sbc.carry().anneal();           // Ignore the carry bits

    Bits       BC = C.chooseFromTwoWords(a.load, sbc.sum(), zero, ia);          // a
    Bits       AC = C.chooseFromTwoWords(b.load, sac.sum(), one,  ib);          // b
    Bits       AB = C.continueBits      (c.load, sab.sum());                    // c

    C.executionTrace = C.new Trace("ia la ib lb lc   a         b         c")
     {String trace()
       {return String.format("%s  %s  %s  %s  %s    %s  %s  %s",
          ia, la, ib, lb, lc, a, b, c);
       }
     };

    C.simulationSteps(420);
    C.simulate();
    //stop(fa.decimal());
    fa.ok(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233);
    //C.printExecutionTrace(); stop();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {test_fibonacci();
    Chip.testSummary();
   }
 }
