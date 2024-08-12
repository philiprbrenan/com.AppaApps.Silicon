//------------------------------------------------------------------------------
// Big machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.util.Stack;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Big extends Chip                                             // Emulate the big machine that will be driven by a small Risc V machine to perform the vector instructions needed to build and maintain a binary tree
 {final Lane    []     lanes;                                                   // The lanes of execution
  final Register[] registers;                                                   // The lanes of execution
  boolean overflow;                                                             // Integer addition overflowed
  boolean lte, gte;                                                             // Integer addition overflowed

  enum Op {nop, add, sub, inc, dec, shiftLeft, shiftRight,                      // Possible operations performed by the big machine
    shiftRightArithmetic;}
                                                                                // Comparison was less than or equal, greater than or equal
  Big(int Registers, int Width, int Lanes)                                      // Create the big machine as a specified number of registers of specified width with a specified number of lanes
   {registers = new Register[Registers];
    lanes     = new Lane[Lanes];
    for (int i = 0; i < Registers; i++) registers[i] = new Register(i, Width);  // Make the registers
    for (int i = 0; i < Lanes;     i++)     lanes[i] = new Lane(i);             // Make the lanes
   }

  class Lane                                                                    // Execution lane description
   {int name;
    Op  op;                                                                     // Op code
    int target;                                                                 // Target register
    int source1;                                                                // Source 1 register
    int source2;                                                                // Source 2 register
    boolean execute;                                                            // Execute this lane

    Lane(int Name)                                                              // Lane
     {name = name; op = Op.nop; target = source1 = source2 = 0;
      execute = false;
     }

    Lane duplicate(int target)                                                  // Duplicate a lane to the indicated target
     {final Lane l = lanes[target];
      l.op       = op;
      l.target   = target;                                                      // Target register
      l.source1  = source1;                                                     // Source 1 register
      l.source2  = source2;                                                     // Source 2 register
      l.execute  = execute;                                                     // Execute this lane
      return l;
     }
   }

  class Register                                                                // Execution lane description
   {final int name;                                                             // Name/number of the register
    final byte[]value;                                                          // Value of register

    Register(int Name, int width)
     {name = Name; value = new byte[width];
      for (int i = 0; i < width; i++) value[i] = 0;
     }
   }

  void add(int target, int source1, int source2)                                // Add two source registers and store in target
   {final int s1 = registers[source1].value[0], s2 = registers[source2].value[0];
    final int t = s1 + s2;
    registers[target].value[0] = (byte)t;
   }

// Need to retire registers in a separate step afterwards so that outputs do not overwrite inputs

  void execute()                                                                // Execute one step of the big machine
   {for (Lane l: lanes)
     {if (!l.execute) continue;
      switch(l.op)
       {case add -> {add(l.target, l.source1, l.source2);}
       }
     }
   }

  static void test_add()                                                        // Test parallel addition
   {Big b = new Big(6, 4, 4);
    b.registers[1].value[0] = 1;
    b.registers[2].value[0] = 2;
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.add;
    b.lanes[0].execute = true;
    b.lanes[0].duplicate(2);
    b.lanes[2].target  = 4;
    b.execute();
    ok(b.registers[3].value[0], 3);
    ok(b.registers[4].value[0], 3);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_add();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      distanceSummary();
      testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(Chip.fullTraceBack(e));
     }
   }
 }
