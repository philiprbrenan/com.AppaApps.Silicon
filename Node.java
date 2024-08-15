//------------------------------------------------------------------------------
// Node machine driven by a RiscV machine
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.util.Stack;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Node extends RiscV                                           // Emulate the Node Machine that will be driven by a small Risc V machine to perform the vector instructions needed to build and maintain a binary tree
 {final Lane    []     lanes;                                                   // The lanes of execution
  final Register[] registers;                                                   // The registers
  final Register[] registersSaved;                                              // Save the registers before executing an operation

  enum Op {nop, add, sub, inc, dec, sll, slr, sar, le, ge};                     // Possible operations performed by the Node Machine
                                                                                // Comparison was less than or equal, greater than or equal
  Node(int Registers, int Width, int Lanes)                                     // Create the Node Machine as a specified number of registers of specified width with a specified number of lanes
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
      for (int i = 0; i < b; i++)
       {if (value[i] != (bs.charAt(b-i-1) != '0')) return false;
       }
      return true;
     }

    int intValue()                                                              // Get upto the first 31 bits of a register as an integer
     {int v = 0;
      for (int i = 0, w = width(); i < w; i++) v += value[i] ? powerTwo(i) : 0;
      return v;
     }

    Register add(Register Source1, Register Source2)                            // Add the specified source registers together and store in this register
     {final Register S1 = Source1, S2 = Source2;
      boolean c = false;
      for (int i = 0, w = width(); i < w; i++)
       {final boolean s1 = S1.value[i], s2 = S2.value[i];
        value[i] = (c &&  s1 &&  s2) ||
                   (c && !s1 && !s2) || (!c && s1 && !s2) || (!c && !s1 && s2);
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

    Register dec()                                                              // Decrement a register
     {boolean c = false;
      for (int i = 0, w = width(); i < w; i++)
       {final boolean t = value[i], s = true;
        value[i] = (c &&  t &&  s) ||
                   (c && !t && !s) || (!c && t && !s) || (!c && !t && s);
        c = (c && t) || (c && s) || (t & s);
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

    Register sll(Register Source1, Register Source2)                            // Shift left source 1 by the amount in source 2  and save in target
     {final Register S1 = Source1, S2 = Source2;
      final int s = S2.intValue();
      zero();
      for (int i = 0, w = width() - s; i < w; i++) value[i+s] = S1.value[i];
      return this;
     }

    Register slr(Register Source1, Register Source2)                            // Shift right source 1 by the amount in source 2  and save in target
     {final Register S1 = Source1, S2 = Source2;
      final int s = S2.intValue();
      zero();
      for (int i = s, w = width(); i < w; i++) value[i-s] = S1.value[i];
      return this;
     }

    Register sar(Register Source1, Register Source2)                            // Shift arithmetic right source 1 by the amount in source 2  and save in target
     {final Register S1 = Source1, S2 = Source2;
      final int s = S2.intValue();
      zero();
      for (int i = s, w = width(); i < w; i++) value[i-s]   = S1.value[i];      // Shift
      for (int i = 0, w = width(); i < s; i++) value[w-i-1] = S1.value[w-1];    // Shift sign bit
      return this;
     }

    Register le(Register Source1, Register Source2)                             // Compare source 1 to source 2 and set target to 1 if source 1 <= source 2 else 0
     {final Register S1 = Source1, S2 = Source2;
      zero();
      for (int i = width()-1; i >= 0; i--)                                      // Each bit starting with most significant
       {if (S1.value[i] == S2.value[i]) continue;                               // Equal on bits at this index
        value[0] = S2.value[i];                                                 // Less than
        return this;
       }

      value[0] = true;                                                          // Equal on all bits
      return this;
     }

    Register ge(Register Source1, Register Source2)                             // Compare source 1 to source 2 and set target to 1 if source 1 >= source 2 else 0
     {final Register S1 = Source1, S2 = Source2;
      zero();
      for (int i = width()-1; i >= 0; i--)                                      // Each bit starting with most significant
       {if (S1.value[i] == S2.value[i]) continue;                               // Equal on bits at this index
        value[0] = S1.value[i];                                                 // Greater than
        return this;
       }

      value[0] = true;                                                          // Equal on all bits
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

  void sll(Register target, Register source1, Register source2)                 // Shift left logical source1 by source2 filling in with zeroes and save in target
   {target.sll(source1, source2);
   }

  void slr(Register target, Register source1, Register source2)                 // Shift right logical source1 by source2 filling in with zeroes and save in target
   {target.slr(source1, source2);
   }

  void sar(Register target, Register source1, Register source2)                 // Shift arithmetic right source1 by source2 filling in with sign bit and save in target
   {target.sar(source1, source2);
   }

  void le(Register target, Register source1, Register source2)                  // Compare source 1 to source 2 and set target to 1 if source 1 <= source 2 else 0
   {target.le(source1, source2);
   }

  void ge(Register target, Register source1, Register source2)                  // Compare source 1 to source 2 and set target to 1 if source 1 >= source 2 else 0
   {target.ge(source1, source2);
   }

  void inc(Register target) {target.inc();}                                     // Increment the target
  void dec(Register target) {target.dec();}                                     // Decrement the target

  void execute()                                                                // Execute one step of the Node Machine
   {for (int i = 0; i < registers.length; i++)
     {registersSaved[i] = registers[i].clone();
     }

    for (Lane l: lanes)                                                         // Execute enabled lanes
     {if (!l.execute) continue;                                                 // Not enabled
      final Register[]r = registers;
      final Register[]s = registersSaved;
      switch(l.op)                                                              // Choose operation
       {case add -> {add(r[l.target], s[l.source1], s[l.source2]);}
        case sub -> {sub(r[l.target], s[l.source1], s[l.source2]);}
        case inc -> {inc(r[l.target]);}
        case dec -> {dec(r[l.target]);}
        case sll -> {sll(r[l.target], s[l.source1], s[l.source2]);}
        case slr -> {slr(r[l.target], s[l.source1], s[l.source2]);}
        case sar -> {sar(r[l.target], s[l.source1], s[l.source2]);}
        case le  -> {le (r[l.target], s[l.source1], s[l.source2]);}
        case ge  -> {ge (r[l.target], s[l.source1], s[l.source2]);}
        default  -> {stop("Implementation needed for", l.op);}
       }
     }
   }

//D1 Define a node

  static MemoryLayout defineLeaf(int keys, int bits)                            // Define a layout representing a node - an interior node of the tree
   {if (keys % 2 == 1) stop("Keys in leaf must be even, not", keys);
    RiscV      r = new RiscV();
    Variable   k = r.variable ("key",      bits);                               // Keys
    Variable   d = r.variable ("data",     bits);                               // Data corresponding to each key
    Variable   F = r.variable ("full",     keys);                               // A unary number that indicates how full the node is
    Array      K = r.array    ("keys",  k, keys);                               // Array of keys
    Array      D = r.array    ("datas", d, keys);                               // Array of matching data matching each key
    Structure  s = r.structure("leaf",  K, D, F);                               // Definition of a leaf
    s.layout();
    return s;
   }

  static MemoryLayout defineNode(int keys, int bits)                            // Define a layout representing a node - an interior node of the tree
   {if (keys % 2 == 0) stop("Keys in node must be odd, not", keys);
    RiscV      r = new RiscV();
    Variable   k = r.variable ("key",      bits);                               // Keys
    Variable   n = r.variable ("next",     bits);                               // Next nodes in lower layer
    Variable   T = r.variable ("top",      bits);                               // Top next node to lower layer
    Variable   F = r.variable ("full",     keys);                               // A unary number that indicates how full the node is
    Array      K = r.array    ("keys",  k, keys);                               // Array of keys
    Array      N = r.array    ("nexts", n, keys);                               // Array of corresponding nexts all of which are less than or equal to the corresponding keys
    Structure  s = r.structure("node",  K, N, T, F);
    s.layout();
    return s;
   }

//D1 Tests

  static void test_add()                                                        // Test parallel addition
   {Node b = new Node(8, 4, 4);
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
    ok(b.registers[3].eq("1000"));
    ok(b.registers[4].eq("0110"));

    b.clearLanes();
    b.lanes[0].op      = Op.inc;
    b.lanes[0].execute = true;
    b.lanes[1].op      = Op.dec;
    b.lanes[1].execute = true;
    b.execute();
    ok(b.registers[3].eq("1001"));
    ok(b.registers[4].eq("0101"));
   }

  static void test_sll()                                                        // Test shift logical left
   {Node b = new Node(8, 4, 4);
    b.clearLanes();
    b.registers[1].set("0101");
    b.registers[2].set("0011");
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.sll;
    b.lanes[0].execute = true;
    b.execute();
    ok(b.registers[3].eq("1000"));
   }

  static void test_slr()                                                        // Test shift logical right
   {Node b = new Node(8, 4, 4);
    b.clearLanes();
    b.registers[1].set("0101");
    b.registers[2].set("0010");
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.slr;
    b.lanes[0].execute = true;
    b.execute();
    ok(b.registers[3].eq("0001"));
   }

  static void test_sar()                                                        // Test shift arithmetic right
   {Node b = new Node(8, 4, 4);
    b.clearLanes();
    b.registers[1].set("1101");
    b.registers[2].set("0010");
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.sar;
    b.lanes[0].execute = true;
    b.execute();
    ok(b.registers[3].eq("1111"));
   }

  static void test_cmp()                                                        // Compare
   {Node b = new Node(8, 4, 4);
    b.clearLanes();
    b.registers[1].set("1101");
    b.registers[2].set("0010");
    b.lanes[0].source1 = 1;
    b.lanes[0].source2 = 2;
    b.lanes[0].target  = 3;
    b.lanes[0].op      = Op.le;
    b.lanes[0].execute = true;

    b.lanes[0].duplicate(1);
    b.lanes[1].target  = 4;
    b.lanes[1].op      = Op.ge;
    b.lanes[1].execute = true;

    b.execute();
    ok(b.registers[3].eq("0000"));
    ok(b.registers[4].eq("0001"));
   }

  static void test_defineLeaf()                                                 // Test define a leaf
   {final MemoryLayout s = defineLeaf(6, 4);
    ok(s.width, 54);
   }

  static void test_defineNode()                                                 // Test define a leaf
   {final MemoryLayout s = defineNode(5, 4);
    ok(s.width, 49);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_add();
    test_sll();
    test_slr();
    test_sar();
    test_cmp();
    test_defineLeaf();
    test_defineNode();
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
