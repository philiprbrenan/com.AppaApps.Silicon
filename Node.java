//------------------------------------------------------------------------------
// Big machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.util.Stack;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Big extends RiscV                                            // Emulate the big machine that will be driven by a small Risc V machine to perform the vector instructions needed to build and maintain a binary tree
 {final Lane    []     lanes;                                                   // The lanes of execution
  final Register[] registers;                                                   // The registers
  final Register[] registersSaved;                                              // Save the registers before executing an operation
  boolean overflow;                                                             // Integer addition overflowed
  boolean lte, gte;                                                             // Integer addition overflowed

  enum Op {nop, add, sub, inc, dec, shiftLeft, shiftRight,                      // Possible operations performed by the big machine
    shiftRightArithmetic;}
                                                                                // Comparison was less than or equal, greater than or equal
  Big(int Registers, int Width, int Lanes)                                      // Create the big machine as a specified number of registers of specified width with a specified number of lanes
   {registers      = new Register[Registers];
    registersSaved = new Register[Registers];
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
     {name = Name; op = Op.nop; target = source1 = source2 = 0;
      execute = false;
     }

    Lane duplicate(int target)                                                  // Duplicate a lane to the indicated target
     {final Lane l = lanes[target];
      assert l.name == target;
      l.op       = op;
      l.target   = target;                                                      // Target register
      l.source1  = source1;                                                     // Source 1 register
      l.source2  = source2;                                                     // Source 2 register
      l.execute  = execute;                                                     // Execute this lane
      return l;
     }
   }

  void clearLanes()                                                             // Set all lanes not to execute
   {for (int i = 0; i < lanes.length; i++) lanes[i].execute = false;
   }

  class Register                                                                // Execution lane description
   {final int name;                                                             // Name/number of the register
    final Boolean[]value;                                                       // Value of register

    Register(int Name, int width)
     {name  = Name;
      value = new Boolean[width];
      for (int i = 0; i < width; i++) value[i] = null;
     }

    int width() {return value.length;}                                          // Width of register

    Register sameLength(Register Source)                                        // Check that two registers have the same length
     {if (Source.width() != width()) stop("Lengths differ", Source, width());
      return this;
     }

    Register zero()                                                             // Zero a register
     {for (int i = 0, w = width(); i < w; i++) value[i] = false;
      return this;
     }

    String reduceString(String BitString)                                       // Remove anything that is not a 0 or a 1 from the string
     {return BitString.replaceAll("[.]", "0").replaceAll("[^01]", "");
     }

    Register set(String BitString)                                              // Set the value of a register from an immediate value
     {final String bs = reduceString(BitString);                                // Edit input string down to 0 and 1's
      final int b = bs.length(), w = width();
      if (b > w) stop("Too long", bs, "versus", w);
      zero();
      for (int i = 0; i < b; i++) value[i] = bs.charAt(b-i-1) != '0';
      return this;
     }

    Register set(Register Source)                                               // Set the value of a register from another register
     {sameLength(Source);
      for (int i = 0, w = width(); i < w; i++) value[i] = Source.value[i];
      return this;
     }

    boolean eq(String BitString)                                                // Confirm that the register has the indicated value
     {final String bs = reduceString(BitString);                                // Edit input string down to 0 and 1's
      final int b = bs.length(), v = width();
      if (b > v) stop("Too long", b, v);
      for (int i = 0; i < b; i++) if (value[i] != (bs.charAt(b-i-1) != '0')) return false;
      return true;
     }

    Register add(Register Source1, Register Source2)                            // Add the specified source registers together and store in this register
     {final Register S1 = Source1, S2 = Source2;
      boolean c = false;
      for (int i = 0, w = width(); i < w; i++)
       {final boolean s1 = S1.value[i], s2 = S2.value[i];
        value[i] = (c && s1 &&  s2) ||
                   (c && !s1 && !s2) || (!c && s1 && !s2) || (!c && !s1 &&  s2);
        c = (c && s1) || (c && s2) || (s1 & s2);
       }
      return this;
     }

    Register inc()                                                              // Add the specified source registers together and store in this register
     {boolean c = true;
      for (int i = 0, w = width(); i < w; i++)
       {final boolean v = value[i];
        value[i] = (c && !v) || (!c && v);
        c = c && v;
       }
      return this;
     }

    Register twosComplement()                                                   // Twos complement of a register
     {for (int i = 0, w = width(); i < w; i++) value[i] = !value[i];            // Ones complement
      inc();                                                                    // Twos complement
      return this;
     }

    Register sub(Register Source1, Register Source2)                            // Subtract the second source register from the first source register and save in the target
     {final Register S1 = Source1, S2 = Source2;
      set(S2);
      twosComplement();
      add(Source1, this);
      return this;
     }

    public Register clone()                                                     // Clone a register
     {final Register r = new Register(name, value.length);
      for (int i = 0; i < value.length; i++) r.value[i] = value[i];
      return r;
     }

    int numberSize()                                                            // Width of number in register
     {for (int m = value.length; m > 0; m--) if (value[m-1]) return m;
      return 0;
     }

    public String toString()                                                    // Clone a register
     {final StringBuilder b = new StringBuilder();
      final int m = width();
      for (int i = m; i > 0; i--) b.append(value[i-1] ? '1' : '.');
      return b.toString();
     }
   }

  void add(Register target, Register source1, Register source2)                 // Add two source registers and store in target
   {target.add(source1, source2);
   }

  void sub(Register target, Register source1, Register source2)                 // Subtract source2 from source1 and save in target
   {target.sub(source1, source2);
   }

  void inc(Register target)                                                     // Increment the target
   {target.inc();
   }

  void execute()                                                                // Execute one step of the big machine
   {for (int i = 0; i < registers.length; i++)
     {registersSaved[i] = registers[i].clone();
     }

    for (Lane l: lanes)
     {if (!l.execute) continue;
      switch(l.op)
       {case add -> {add(registers[l.target], registersSaved[l.source1], registersSaved[l.source2]);}
        case sub -> {sub(registers[l.target], registersSaved[l.source1], registersSaved[l.source2]);}
        case inc -> {inc(registers[l.target]);}
        default  -> {stop("Implementation needed for", l.op);}
       }
     }
   }

  static void test_add()                                                        // Test parallel addition
   {Big b = new Big(8, 4, 4);
    b.clearLanes();
    b.registers[1].set("0111");
    b.registers[2].set("0001");
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.add;
    b.lanes[0].execute = true;

    b.lanes[0].duplicate(1);
    b.lanes[1].target  = 4;
    b.lanes[1].op      = Op.sub;
    b.lanes[1].execute = true;

    b.execute();
say("AAAA", b.registers[3]);
    ok(b.registers[3].eq("1000"));
    ok(b.registers[4].eq("0110"));

    b.clearLanes();
    b.lanes[0].op      = Op.inc;
    b.lanes[0].execute = true;
    b.execute();
    ok(b.registers[3].eq("1001"));
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
