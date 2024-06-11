//------------------------------------------------------------------------------
// Test Chip.j
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;
import java.util.stream.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

final class Test1                                                               // Describe a chip and emulate its operation.
 {static void test_choose_pulse()                                               // Choose a pulse
   {int              N = 4;
    Chip             c = new Chip();                                            // Create a new chip
    Chip.Bits      two = c.bits        ("two",   N,       2);
    Chip.Words   words = c.words       ("words", N, 4, 3, 2, 1);

    Chip.Pulse       p = c.pulse       ("s1",    0,       4);
    Chip.Pulse[]     P = c.choosePulse ("P", words, two,  p);
    Chip.Bit        P1 = c.Output      ("P1", P[0]);
    Chip.Bit        P2 = c.Output      ("P2", P[1]);
    Chip.Bit        P3 = c.Output      ("P3", P[2]);
    Chip.Bit        P4 = c.Output      ("P4", P[3]);
    c.executionTrack(
      "p   4 3 2 1",
      "%s   %s %s %s %s",
       p, P[0], P[1], P[2], P[3]);
    c.simulationSteps(8);
    c.simulate();

    //c.printExecutionTrace(); Chip.stop();
    c.ok("""
Step  p   4 3 2 1
   1  1   . . . .
   2  1   . . . .
   3  1   . . . .
   4  1   . . . .
   5  0   . . . .
   6  0   0 0 1 0
   7  0   0 0 0 0
   8  0   0 0 0 0
""");
   }

  public static void main(String[] args)                                        // Test if called as a program
   {test_choose_pulse();
    Chip.testSummary();
   }
 }
