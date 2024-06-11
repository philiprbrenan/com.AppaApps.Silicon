//------------------------------------------------------------------------------
// RiscV driving Btree on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban                                                          // Create a chip that contains a Risc V processor extended with Btree instructcions
 {final static int InstructionWidthBits = 32;                                   // Instruction width in bits
  final static int XLEN                 =  4;                                   // Size of instructions
  final static boolean github_actions   =                                       // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final String name;                                                            // Name of chip
  Ban()                                                                         // Create a new program with the name of the test
   {name = Chip.currentTestName();                                              // Name of current test
   }

//D1 Logging                                                                    // Logging and tracing

//D2 Conversion                                                                 // Conversion utilities

  static String binaryString(int n, int width)                                  // Convert a integer to a binary string of specified width
   {final String b = "0".repeat(width)+Long.toBinaryString(n);
    return b.substring(b.length() - width);
   }

//D2 Printing                                                                   // Print log messages

  static void say (Object...O) {Chip.say(O);}                                   // Say something
  static StringBuilder say(StringBuilder b, Object...O) {return Chip.say(b, O);}// Say something in a string builder
  static void err (Object...O) {Chip.err(O);}                                   // Say something and provide an error trace.
  static void stop(Object...O) {Chip.stop(O);}                                  // Say something and stop

//D1 Testing                                                                    // Test expected output against got output

  void        ok(String expected)    {ok(toString(), expected);}                // Compare state of machine with expected results
  static void ok(Object a, Object b) {Chip.ok(a, b);}                           // Check test results match expected results.
  static void ok(String G, String E) {Chip.ok(G, E);}                           // Say something, provide an error trace and stop

//D0

  static void test_decode_addi()                                                // Decode an addi instruction
   {int       N = 4;
    var      C = new Chip();                                                    // Create a new chip
    var   addi = C.bits("addi", 32, 0xa00093);                                  // addi x1,x0,10
    var opCode = C.new SubBitBus("opCode", addi, 1, RiscV.Decode.p_rd);         // Extract op code
    var ignore = C.new SubBitBus("ignore", addi, 1+RiscV.Decode.p_rd, InstructionWidthBits - RiscV.Decode.p_rd); // Extract op code
    ignore.anneal();
    var    out = C.outputBits("out", opCode);
    C.simulate();
    out.ok(RiscV.Decode.opArithImm);
   }


  static void oldTests()                                                        // Tests thought to be in good shape
   {test_decode_addi();
   }

  static void newTests()                                                        // Tests being worked on
   {test_decode_addi();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      Chip.testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(Chip.traceBack(e));
     }
   }
 }
