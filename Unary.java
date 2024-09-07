//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a binary tree on a silicon chip.

class Unary extends Memory.Variable                                             // Unary arithmetic
 {final int max;                                                                // Maximum size of unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max)                                                                // Create a unary number of specified size
   {super("Unary", Max);
    layout();
    max = Max;
   }

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number of specified size

  int max() {return max;}                                                       // The maximum value of the unary number

  void ok(int n) {ok(this, n);}                                                 // Check that a unary number has the expected value

//D1 Set and get                                                                // Set and get a unary number

  void set(int n)                                                               // Set the unary number
   {zero();
    if (n > 0) shiftLeftFillWithOnes(n);
   }

  int get()                                                                     // Get the unary number
   {return countTrailingOnes();
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc() {return get() < max();}                                      // Can we increment the unary number
  boolean canDec() {return get() > 0;}                                          // Can we decrement the unary number

  void inc()                                                                    // Increment the unary number
   {if (!canInc()) stop(get(), "Unary number is too big to be incremented");
    shiftLeftFillWithOnes(1);
   }

  void dec()                                                                    // Decrement the unary number
   {if (!canDec())
     {stop(get(), "Unary number is too small to be decremented");
     }
    shiftRightFillWithSign(1);
   }

//D1 Print                                                                      // Print a unary number

  public String toString() {return ""+get();}                                   // Print a unary number

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {Unary u = unary(32);
                u.set(0);
                 u.ok(0);
    u.inc();     u.ok(1);
    u.inc();     u.ok(2);
    u.inc();     u.ok(3);
    u.inc();     u.ok(4);
    u.set(21);
    u.inc();
    u.ok (22);
    u.set(23);
    u.dec();
    u.ok( 22);
    u.set(31); ok( u.canInc());
    u.set(32); ok(!u.canInc());

    u.set(1);  ok( u.canDec());
    u.set(0);  ok(!u.canDec());
    u.ok (0);
   }

  static void test_preset()
   {Unary     u = unary(4);
    u.set(1); u.ok(1);
    u.dec();  u.ok(0); ok( u.canInc());
    u.inc();  u.ok(1); ok( u.canInc());
    u.inc();  u.ok(2); ok( u.canInc());
    u.inc();  u.ok(3); ok( u.canInc());
    u.inc();  u.ok(4); ok(!u.canInc());
   }

  static void test_sub_unary()
   {Variable  a = variable ("a", 4);
    Unary     u = unary(4);
    Structure s = structure("s", a, u);
    s.layout();
    s.set(0);
    u.set(2);
    u.ok (2);
    s.ok("00110000");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_unary();
    test_preset();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_sub_unary();
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
