//------------------------------------------------------------------------------
// Unary arithmetic using boolean arrays.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

class Unary extends RiscV                                                       // Unary arithmetic
 {final Layout layout;                                                          // Layout of memory used by a unary number
  final int max;                                                                // Maximum size of unary number
  final java.util.Stack<Memory> memory = new java.util.Stack<>();               // A memory containing a unary number

//D1 Construction                                                               // Create a unary number

  Unary(int Max) {max = Max; memory(); layout = new Layout("unary");}           // Create a unary number of specified size

  static Unary unary(int max) {return new Unary(max);}                          // Create a unary number of specified size

  void memory() {memoryOpen(new Memory(max));}                                  // Assign some memory for a unary number

  void memoryOpen(Memory Memory)                                                // Set memory for a unary number
   {final int m = Memory.size();
    if (m != max)
      stop("Memory size is different from expected", max, "but got", m);
    memory.push(Memory);
   }

  void memoryClose()                                                            // Pop the current memory to restore the previous memory
   {if (memory.size() == 0) stop("Memory stack underflow");
    memory.pop();
   }

  int max() {return max;}                                                       // The maximum value of the unary number

  void ok(int n) {ok(get(), n);}                                                // Check that a unary number has the expected value

  class Layout extends Variable                                                 // Memory layout for a stuck stack
   {Layout(String name)                                                         // Create the a memory layout for a unary number
     {super(name, max);
      layout();                                                                 // Layout memory
     }
   }

//D1 Set and get                                                                // Set and get a unary number

  void set(int n)                                                               // Set the unary number
   {memory.lastElement().zero();
    if (n > 0) memory.lastElement().shiftLeftFillWithOnes(n);
   }

  int get()                                                                     // Get the unary number
   {return memory.lastElement().countTrailingOnes();
   }

//D1 Arithmetic                                                                 // Arithmetic using unary numbers

  boolean canInc() {return get() < max();}                                      // Can we increment the unary number
  boolean canDec() {return get() > 0;}                                          // Can we decrement the unary number

  void inc()                                                                    // Increment the unary number
   {if (!canInc()) stop(memory.lastElement().getInt(), "is too big to be incremented");
    memory.lastElement().shiftLeftFillWithOnes(1);
   }

  void dec()                                                                    // Decrement the unary number
   {if (!canDec())
     {stop(memory.lastElement().getInt(), "is too small to be decremented");
     }
    memory.lastElement().shiftRightFillWithSign(1);
   }

//D1 Print                                                                      // Print a unary number

  public String toString() {return ""+get();}                                   // Print a unary number

//D0 Tests                                                                      // Test unary numbers

  static void test_unary()
   {Unary  u = unary(32);
           u.set(4);
           u.memory();
               u.ok(0);
    u.inc();   u.ok(1);
    u.set(21); u.inc(); u.ok(22);
    u.set(23); u.dec(); u.ok(22);
    u.set(31); ok( u.canInc());
    u.set(32); ok(!u.canInc());

    u.set( 1); ok( u.canDec());
    u.set( 0); ok(!u.canDec());
    u.memoryClose(); u.ok(4);
   }

  static void test_preset()
   {Unary  u = unary(4);
    u.set(1); u.ok(1);
    u.dec();  u.ok(0); ok( u.canInc());
    u.inc();  u.ok(1); ok( u.canInc());
    u.inc();  u.ok(2); ok( u.canInc());
    u.inc();  u.ok(3); ok( u.canInc());
    u.inc();  u.ok(4); ok(!u.canInc());
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
