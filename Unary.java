//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout a binary tree on a silicon chip.

class Unary extends Memory.Structure                                            // Unary arithmetic
 {final Memory.Variable notZero;                                                // Not zero if true
  final Memory.Variable atMax;                                                  // At the maximum value
  final Memory.Variable value;                                                  // The value of the unary number
  final int max;                                                                // Maximum size of unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max)                                                                // Create a unary number of specified size
   {super("Unary");                                                             // Structure definition
    if (Max <= 0) stop("Unary size must be at least one, not", Max);            // Size check
    notZero = variable("notZero", 1);                                           // Not zero if true
    atMax   = variable("atMax",   1);                                           // At the maximum value
    value   = variable("value", Max) ;                                          // The value of the unary number
    addField(notZero);                                                          // Not zero if true
    addField(atMax);                                                            // At the maximum value
    addField(value);                                                            // The value of the unary number
    layout();
    max = Max;
   }

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number of specified size

  int max() {return max;}                                                       // The maximum value of the unary number

  void ok(int n) {ok(get(), n);}                                                // Check that a unary number has the expected value

//D1 Set and get                                                                // Set and get a unary number

  void set(int n)                                                               // Set the unary number
   {if (n < 0)   stop("Cannot set to a value less than zero", n);
    if (n > max) stop("Cannot set to a value greater than max", n, max);
    zero();                                                                     // Zero out the number
    if (n > 0)                                                                  // Set a non zero value
     {value.shiftLeftFillWithOnes(n);                                           // Non zero value
      notZero.set(1);
      atMax.set(n < max ? 0 : 1);
     }
   }

  int get()                                                                     // Get the unary number
   {return value.countTrailingOnes();
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc() {return get() < max();}                                      // Can we increment the unary number
  boolean canDec() {return get() > 0;}                                          // Can we decrement the unary number

  void inc()                                                                    // Increment the unary number
   {if (atMax.toInt() == 1) stop(get(), "Unary number is too big to be incremented");
    set(get()+1);
   }

  void dec()                                                                    // Decrement the unary number
   {if (notZero.toInt() == 0) stop(get(), "Unary number is too small to be decremented");
    set(get()-1);
   }

//D1 Print                                                                      // Print a unary number

  public String toString()
   {return "Unary(notZero:"+notZero.toInt()+                                    // Print a unary number
                 ", atMax:"+atMax.toInt()+
                 ", value:"+get()+
                   ", max:"+max+")";
   }

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {Unary u = unary(32);
              u.set(0);
              u.ok(0);
    u.inc();  u.ok(1);
    u.inc();  u.ok(2);
    u.inc();  u.ok(3);
    u.inc();  u.ok(4);
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
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_unary();
    test_preset();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
