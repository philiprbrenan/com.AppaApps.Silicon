//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

class Unary extends Chip                                                        // Unary arithmetic
 {final boolean[]u;                                                             // The unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max)                                                                // Create a unary number
   {u = new boolean[Max+1];                                                     // Create the unary number
    for (int i = 0; i < Max; i++) u[i] = false;                                 // Zero the unary number
   }

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number

  static Unary unary(int max, int value)                                        // Create a unary number set to a specified value
   {final Unary u = new Unary(max);
    u.set(value);
    return u;
   }

  public Unary clone() {return unary(max(), get());}                            // Clone a unary number

  int max() {return u.length-1;}                                                // The maximum value of the unary number

  void ok(int n) {ok(get(), n);}                                                // Check that a unary number has the expected value

//D1 Set and get                                                                // Set and get a unary numnber

  void set(int n)                                                               // Set the unary number
   {if (n <= u.length)
     {for (int i = 0; i < u.length; i++) u[i] = false;
      u[n] = true;
     }
    else stop(n, "too big");
   }

  int get()                                                                     // Get the unary number
   {for (int i = 0; i < u.length; i++) if (u[i]) return i;
    return 0;
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc() {return get() < max();}                                      // Can we increment the unary number
  boolean canDec() {return get() > 0;}                                          // Can we decrement the unary number

  void inc()                                                                    // Increment the unary number
   {final int n = get();
    if (!canInc()) stop(n, "is too big to be incremented");
    set(n+1);
   }

  void dec()                                                                    // Decrement the unary number
   {final int n = get();
    if (!canDec()) stop(n, "is too small to be decremented");
    set(n-1);
   }

//D1 Print                                                                      // Print a unary number

  public String toString() {return ""+get();}                                   // Print a unary number

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {var u = unary(32);
               u.ok(0);
    u.inc();   u.ok(1);
    u.set(21); u.inc(); u.ok(22);
    u.set(23); u.dec(); u.ok(22);
    u.set(31); ok( u.canInc());
    u.set(32);
    ok(!u.canInc());

    u.set(1);  ok( u.canDec());
    u.set(0);  ok(!u.canDec());
   }

  static void test_preset()
   {var u = unary(4, 1);
             u.ok(1);
    u.dec(); u.ok(0); ok( u.canInc());
    u.inc(); u.ok(1); ok( u.canInc());
    u.inc(); u.ok(2); ok( u.canInc());
    u.inc(); u.ok(3); ok( u.canInc());
    u.inc(); u.ok(4); ok(!u.canInc());
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
