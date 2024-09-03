//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

class Unary extends Chip                                                        // Unary arithmetic
 {final int max;                                                                // Maximum size of unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max) {max = Max;}                                                   // Create a unary number of specified size

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number of specified size

  int max() {return max;}                                                       // The maximum value of the unary number

  void ok(Memory.Layout ml, int n) {ml.ok(n);}                                  // Check that a unary number has the expected value

  class Layout extends Memory.Variable                                          // Memory layout for a stuck stack
   {Layout(String name)                                                         // Create the a memory layout for a unary number
     {super(name, max);
      layout();                                                                 // Layout memory.  If this layout is contained in another layout, then laying out that layout will make this layout refer the the memory of the containing layout.  The memory being supplied here is useful only when this lay out is used by itself.
     }
   }

//D1 Set and get                                                                // Set and get a unary number

  void set(Memory.Layout ml, int n)                                             // Set the unary number
   {ml.memory.zero();
    if (n > 0) ml.memory.shiftLeftFillWithOnes(n);
   }

  int get(Memory.Layout ml)                                                     // Get the unary number
   {return ml.memory.countTrailingOnes();
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc(Memory.Layout ml) {return get(ml) < max();}                    // Can we increment the unary number
  boolean canDec(Memory.Layout ml) {return get(ml) > 0;}                        // Can we decrement the unary number

  void inc(Memory.Layout ml)                                                    // Increment the unary number
   {if (!canInc(ml)) stop(ml.get(), "Unary number is too big to be incremented");
    ml.memory.shiftLeftFillWithOnes(1);
   }

  void dec(Memory.Layout ml)                                                    // Decrement the unary number
   {if (!canDec(ml))
     {stop(ml.get(), "Unary number is too small to be decremented");
     }
    ml.memory.shiftRightFillWithSign(1);
   }

//D1 Print                                                                      // Print a unary number

  public String toString(Memory.Layout ml) {return ""+get(ml);}                 // Print a unary number

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {Unary         u = unary(32);
    Memory.Layout l = u.new Layout("Unary");
                 u.set(l,  0);
                  u.ok(l,  0);
    u.inc(l);     u.ok(l,  1);
    u.inc(l);     u.ok(l,  3);
    u.inc(l);     u.ok(l,  7);
    u.inc(l);     u.ok(l, 15);
    u.set(l, 21);
    u.inc(l);
    u.ok(l, (1<<22)-1);
    u.set(l, 23);
    u.dec(l);
    u.ok(l, (1<<22)-1);
    u.set(l, 31); ok( u.canInc(l));
    u.set(l, 32); ok(!u.canInc(l));

    u.set(l, 1); ok( u.canDec(l));
    u.set(l, 0); ok(!u.canDec(l));
    u.ok(l, 0);
   }

  static void test_preset()
   {Unary         u = unary(4);
    Memory.Layout l = u.new Layout("Unary");
    u.set(l, 1); u.ok(l, 1);
    u.dec(l);  u.ok(l,  0); ok( u.canInc(l));
    u.inc(l);  u.ok(l,  1); ok( u.canInc(l));
    u.inc(l);  u.ok(l,  3); ok( u.canInc(l));
    u.inc(l);  u.ok(l,  7); ok( u.canInc(l));
    u.inc(l);  u.ok(l, 15); ok(!u.canInc(l));
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
