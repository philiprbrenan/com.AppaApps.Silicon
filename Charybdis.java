//------------------------------------------------------------------------------
// A chip that transforms a register and then reloads it in a never ending cycle
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.
/*
 ÏÏÎ¯Ï Î¼á½²Î½ Î³á½°Ï ÏÎ±ÏÎ­ÎµÎ¹, ÏÏá½¶Ï Î´' á¼Î¾ÎµÎ¼Î­ÏÏÏÎ¹Î½ á¼ÏÎ±Î½ÏÎ±, Î´ÎµÎ¹Î½á½¸Î½ ÎºÏÎºÏÏÎ±ÏÎ±Â· Î¸Îµá½¸Ï ÏÎ­ÏÎ±Ï Îµá¼°ÏÎ¿ÏÎ¬Î±ÏÎ¸Î±Î¹.

Three times a day she sucks the dark water down, and three times she spits it up, a terror to behold.
*/

//D1 Construct                                                                  // A chip that transforms a register and then reloads it in a never ending cycle

public class Charybdis extends Chip                                             // A chip that transforms a register and then reloads it in a never ending cycle
 {final Register register;                                                      // The register that will be transformed
  final Pulse      pulse;                                                       // The pulse that will update the register at the end of the cycle
  final Bits       input;                                                       // Set this bit bus to load the register with its latest value
  final int        delay;                                                       // The time it takes for the register to reset
  final int        width;                                                       // Width of the register in bits
  final int           on;                                                       // How many steps we need to wait for the correct response

  Charybdis(String Name, int Width, int On)                                     // Create a new Charybdis chip
   {super(Name);
    width       = Width;
    on          = On;
    delay       = logTwo(width);
    pulse       = pulse       (n(Name, "pulse")).period(0).delay(delay).on(on).b();
    register    = new Register(n(Name, "register"), width, pulse).anneal();
    input       = register.inputBits;
   }

//D0                                                                            // Tests

  static void test_shift_left()
   {final int W = 4;
    Charybdis c = new Charybdis("c", W, W);
    c.shiftLeftConstant(c.input.name, c.register, 1);

    c.executionTrace = c.new ExecutionTrace()
     {String title() {return "p  Reg";}
      String trace() {return String.format("%s  %s  %s", c.pulse, c.input, c.register);}
     };

    c.simulationSteps(3*W);
    c.simulate();
    //r.ok(A);
    c.printExecutionTrace(); stop();
    c.executionTrace.ok("""
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_shift_left();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_shift_left();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
     }
   }
 }
