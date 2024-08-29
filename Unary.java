//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

class Unary extends RiscV                                                       // Unary arithmetic
 {final int max;                                                                // Maximum size of unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max) {max = Max;}                                                   // Create a unary number

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number

  Memory memory() {return new Memory(max);}                                     // Create somememory for a unary number

  int max() {return max;}                                                       // The maximum value of the unary number

  void ok(Memory memory, int n) {ok(get(memory), n);}                           // Check that a unary number has the expected value

//D1 Set and get                                                                // Set and get a unary number

  void checkMemory(Memory memory)                                               // Set the unary number
   {final int m = memory.size();
    if (m != max)
      stop("Memory size is different from expected", m, "but expected", max);
   }

  void set(Memory memory, int n)                                                // Set the unary number
   {checkMemory(memory);
    memory.zero();
    if (n > 0) memory.shiftLeftFillWithOnes(n);
   }

  int get(Memory memory)                                                        // Get the unary number
   {checkMemory(memory);
    return memory.countTrailingOnes();
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc(Memory memory) {return get(memory) < max();}                   // Can we increment the unary number
  boolean canDec(Memory memory) {return get(memory) > 0;}                       // Can we decrement the unary number

  void inc(Memory memory)                                                       // Increment the unary number
   {if (!canInc(memory)) stop(memory.getInt(), "is too big to be incremented");
    memory.shiftLeftFillWithOnes(1);
   }

  void dec(Memory memory)                                                       // Decrement the unary number
   {if (!canDec(memory)) stop(memory.getInt(), "is too small to be decremented");
    memory.shiftRightFillWithSign(1);
   }

//D1 Print                                                                      // Print a unary number

  String toString(Memory memory) {return ""+get(memory);}                       // Print a unary number

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {Unary  u = unary(32);
    Memory m = u.memory();

                  u.ok(m, 0);
    u.inc(m);     u.ok(m, 1);
    u.set(m, 21); u.inc(m); u.ok(m, 22);
    u.set(m, 23); u.dec(m); u.ok(m, 22);
    u.set(m, 31); ok( u.canInc(m));
    u.set(m, 32); ok(!u.canInc(m));

    u.set(m, 1);  ok( u.canDec(m));
    u.set(m, 0);  ok(!u.canDec(m));
   }

  static void test_preset()
   {Unary  u = unary(4);
    Memory m = u.memory();
    u.set(m, 1); u.ok(m, 1);
    u.dec(m); u.ok(m, 0); ok( u.canInc(m));
    u.inc(m); u.ok(m, 1); ok( u.canInc(m));
    u.inc(m); u.ok(m, 2); ok( u.canInc(m));
    u.inc(m); u.ok(m, 3); ok( u.canInc(m));
    u.inc(m); u.ok(m, 4); ok(!u.canInc(m));
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_unary();
    test_preset();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
     }
   }
 }
