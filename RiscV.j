//------------------------------------------------------------------------------
// Execute Risc V machine code
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class RiscV                                                        // Load and execute a program written in RiscV machine code
 {final static boolean github_actions =                                         // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));
  final static boolean    makeSayStop = false;                                  // Turn say into stop if true which is occasionally useful for locating unlabeled say statements.

  final String                   name;                                          // Name of program
  final int defaultMaxSimulationSteps = github_actions ? 1000 : 100;            // Default maximum simulation steps
  final int defaultMinSimulationSteps =    0;                                   // Default minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.
  Integer          maxSimulationSteps = null;                                   // Maximum simulation steps
  Integer          minSimulationSteps = null;                                   // Minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.

  final Stack<Encode>            code = new Stack<>();                          // Encoded instructions
  final long[]              registers = new long[32];                           // General purpose registers
  final int                        pc = 0;                                      // Program counter

  RiscV(String Name)                                                            // Create a new program
   {name = Name;                                                                // Name of chip
   }

  RiscV()                                                                       // Create a new program with the name of the test
   {this(currentTestName());
   }

  int maxSimulationSteps(int MaxSimulationSteps) {return maxSimulationSteps = MaxSimulationSteps;}  // Maximum simulation steps
  int minSimulationSteps(int MinSimulationSteps) {return minSimulationSteps = MinSimulationSteps;}  // Minimum simulation steps

  void simulationSteps(int min, int max) {minSimulationSteps(min);   maxSimulationSteps(max);}      // Stop cleanly between the specified minimum and maximum number of steps
  void simulationSteps(int steps)        {minSimulationSteps(steps); maxSimulationSteps(steps);}    // Stop cleanly at this number of steps

  public String toString()                                                      // Convert chip to string
   {final StringBuilder b = new StringBuilder();
    b.append("RiscV      : " + name);

    b.append("Instruction: "+pc);
    b.append("Registers  : ");
    for (int i = 0; i < registers.length; i++) b.append(" "+registers[i]);      // Print registers
    return b.toString();                                                        // String describing chip
   }

  class Encode                                                                  // Encode an instruction
   {final int instruction;                                                      // Resulting instruction

    Encode(int opCode)                                                          // Encode an instruction
     {instruction = opCode;
      code.push(this);
     }
   }

  class Decode                                                                  // Decode an instruction
   {final Encode instruction;                                                   // Instruction to be decoded
    final int rd;                                                               // Destination register
    final int opCode;                                                           // Operation code
    final int funct3;                                                           // Sub function
    final int rs1;                                                              // Source 1 register
    final int rs2;                                                              // Source 2 register
    final String name;                                                          // Name of operation code

    Decode(Encode Instruction)                                                  // Decode an instruction
     {instruction = Instruction;
      final int i = instruction.instruction;
      rd          = (i >>  7) & 0b1_1111;                                       // Destination register
      rs1         = (i >> 15) & 0b1_1111;                                       // Source register 1
      rs2         = (i >> 20) & 0b1_1111;                                       // Source register 1
      funct3      = (i >> 12) & 0b111;                                          // Sub function
      name        = switch(opCode = i & 0b111_1111)                             // Op code
       {case 1  -> {yield "add";}
        default -> {yield "";   }
       };
     }

    public String toString()                                                    // Print instruction
     {return name + " rd="+rd;
     }
   }

  class DecodeI extends Decode                                                  // Decode an I format instruction
   {final int immediate;                                                        // Immediate value

    DecodeI(Encode Instruction)                                                 // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      immediate   = (i >> 20);                                                  // Immediate value
     }

    public String toString()                                                    // Print instruction
     {return name + "=I rd="+rd+" funct3="+funct3+" rs1="+rs1+" imm="+immediate;
     }
   }

  class DecodeJ extends Decode                                                  // Decode a J format instruction
   {final int immediate;                                                        // Immediate value

    DecodeJ(Encode Instruction)                                                 // Decode an instruction
     {super(Instruction);
      final int i = instruction.instruction;
      immediate   = immediate();                                                // Immediate value
     }

    int immediate()                                                             // Immediate value
     {final int i = instruction.instruction;
      final int b_31_31 = i & 0b10000000000000000000000000000000;
      final int b_30_21 = i & 0b01111111111000000000000000000000;
      final int b_20_20 = i & 0b00000000000100000000000000000000;
      final int b_19_12 = i & 0b00000000000011111111000000000000;

      final int B_20_20 = (b_31_31 >> 31) << 20;
      final int B_10_01 = (b_30_21 >> 21) <<  1;
      final int B_11_11 = (b_20_20 >> 20) << 11;
      final int B_19_12 = (b_19_12 >> 12) << 12;

      return B_20_20 | B_19_12 | B_11_11 | B_10_01;
     }

    public String toString()                                                    // Print instruction
     {return name + "=J rd="+rd+" imm="+immediate;
     }
   }

  class DecodeU extends Decode                                                  // Decode a U format instruction
   {final int immediate;                                                        // Immediate value

    DecodeU(Encode Instruction)                                                 // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      immediate   = i >> 12;                                                    // Immediate value
     }
    public String toString()                                                    // Print instruction
     {return name + "=U rd="+rd+" imm="+immediate;
     }
   }

  class DecodeB extends Decode                                                  // Decode a B format instruction
   {final int immediate;                                                        // Immediate value
    final int subType;                                                          // Instruction sub type

    DecodeB(Encode Instruction)                                                 // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      subType     = (i >> 12) & 0b111;                                          // Sub type
      immediate   = immediate();                                                // Immediate operand
     }

    int  immediate()                                                            // Immediate value
     {final int i       = instruction.instruction;
      final int b_31_31 = i & 0b10000000000000000000000000000000;
      final int b_30_25 = i & 0b01111110000000000000000000000000;
      final int b_11_08 = i & 0b00000000000000000000111100000000;
      final int b_07_07 = i & 0b00000000000000000000000010000000;

      final int B_12_12 = (b_31_31 >> 31) << 12;
      final int B_10_05 = (b_30_25 >> 25) <<  5;
      final int B_04_01 = (b_11_08 >>  8) <<  1;
      final int B_11_11 = (b_07_07 <<  7) << 11;

      return B_12_12 | B_11_11 | B_10_05 | B_04_01;
     }

    public String toString()                                                    // Print instruction
     {return name + "=B rd="+rd+" subType="+subType+" rs1="+rs1+" rs2="+rs2+" imm="+immediate;
     }
   }

  class DecodeS extends Decode                                                  // Decode a S format instruction
   {final int immediate;                                                        // Immediate value

    DecodeS(Encode Instruction)                                                 // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      immediate   = immediate();                                                // Immediate operand
     }

    int  immediate()                                                            // Immediate value
     {final int i       = instruction.instruction;

      final int b_31_25 = i & 0b11111110000000000000000000000000;
      final int b_11_07 = i & 0b00000000000000000000111110000000;

      final int B_11_05 = (b_31_25 >> 25) << 5;
      final int B_04_00 = (b_11_07 >>  7) << 0;

      return B_11_05 | B_04_00;
     }

    public String toString()                                                    // Print instruction
     {return name + "=S rd="+rd+" funct3="+funct3+" rs1="+rs1+" rs2="+rs2+" imm="+immediate;
     }
   }

  class DecodeR extends Decode                                                  // Decode a R format instruction
   {final int funct7;                                                           // Sub function

    DecodeR(Encode Instruction)                                                 // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      funct7      = (i >> 25) & 0b111_1111;                                     // Sub type
     }

    public String toString()                                                    // Print instruction
     {return name + "=R rd="+rd+" funct3="+funct3+" funct7="+funct7+" rs1="+rs1+" rs2="+rs2;
     }
   }

  class DecodeRa extends Decode                                                 // Decode a R atomic format instruction
   {final int funct5;                                                           // Sub function
    final boolean rl;                                                           // rl
    final boolean aq;                                                           // aq

    DecodeRa(Encode Instruction)                                                // Decode instruction
     {super(Instruction);
      final int i = instruction.instruction;
      rl          = ((i >> 25) & 0b1) == 0b1;                                   // rl
      aq          = ((i >> 26) & 0b1) == 0b1;                                   // aq
      funct5      =  (i >> 27) & 0b1_1111;                                      // Sub type
     }

    public String toString()                                                    // Print instruction
     {return name + "=Ra rd="+rd+" funct3="+funct3+" funct5="+funct5+" rl="+rl+" aq="+aq+" rs1="+rs1+" rs2="+rs2;
     }
   }

//D1 Instructions                                                               // Instructions

  Encode add() {return new Encode(1);}

//D1 Utility routines                                                           // Utility routines

//D2 Numeric routines                                                           // Numeric routines

  static double max(double n, double...rest)                                    // Maximum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = rest[i] > m ? rest[i] : m;
    return m;
   }

  static double min(double n, double...rest)                                    // Minimum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = rest[i] < m ? rest[i] : m;
    return m;
   }

  int nextPowerOfTwo(int n)                                                     // If this is a power of two return it, else return the next power of two greater than this number
   {int p = 1;
    for (int i = 0; i < 32; ++i, p *= 2) if (p >= n) return p;
    stop("Cannot find next power of two for", n);
    return -1;
   }

  int logTwo(int n)                                                             // Log 2 of containing power of 2
   {int p = 1;
    for (int i = 0; i < 32; ++i, p *= 2) if (p >= n) return i;
    stop("Cannot find log two for", n);
    return -1;
   }

  static int powerTwo(int n) {return 1 << n;}                                   // Power of 2
  static int powerOf (int a, int b)                                             // Raise a to the power b
   {int v = 1; for (int i = 0; i < b; ++i) v *= a; return v;
   }

//D1 Logging                                                                    // Logging and tracing

//D2 Traceback                                                                  // Trace back so we know where we are

  static String traceBack(Exception e)                                          // Get a stack trace that we can use in Geany
   {final StackTraceElement[]  t = e.getStackTrace();
    final StringBuilder        b = new StringBuilder();
    if (e.getMessage() != null)b.append(e.getMessage()+'\n');

    for(StackTraceElement s : t)
     {final String f = s.getFileName();
      final String c = s.getClassName();
      final String m = s.getMethodName();
      final String l = String.format("%04d", s.getLineNumber());
      if (f.equals("Main.java") || f.equals("Method.java") || f.equals("DirectMethodHandleAccessor.java")) {}
      else b.append("  "+f+":"+l+":"+m+'\n');
     }
    return b.toString();
   }

  static String traceBack() {return traceBack(new Exception());}                // Get a stack trace that we can use in Geany

  static String currentTestName(String test_name)                               // Remove prefix from test names
   {return test_name.replaceFirst("^test_", "");
   }

  static String currentTestName()                                               // Name of the current test
   {final StackTraceElement[] T = Thread.currentThread().getStackTrace();       // Current stack trace
    if (T.length >= 4)                                                          // Perhaps called from a constructor
     {final String c = T[2].getMethodName();
      final String C = T[3].getMethodName();
      return currentTestName(!c.equals("<init>") ? c : C);                      // Remove test marker
     }
    if (T.length == 3) return currentTestName(T[2].getMethodName());            // Not called from a constructor
    return null;
   }

  static String sourceFileName()                                                // Name of source file containing this method
   {final StackTraceElement e = Thread.currentThread().getStackTrace()[2];      // 0 is getStackTrace, 1 is this routine, 2 is calling method
    return e.getFileName();
   }

//D2 Printing                                                                   // Print log messages

  static void say(Object...O)                                                   // Say something
   {final StringBuilder b = new StringBuilder();
    for (Object o: O) {b.append(" "); b.append(o);}
    System.err.println((O.length > 0 ? b.substring(1) : ""));
    if (makeSayStop)
     {System.err.println(traceBack());
      System.exit(1);
     }
   }

  static StringBuilder say(StringBuilder b, Object...O)                         // Say something in a string builder
   {for (Object o: O)
     {if (b.length() > 0) b.append(" ");
      b.append(o);
     }
    b.append('\n');
    return b;
   }

  static void err(Object...O)                                                   // Say something and provide an error trace.
   {say(O);
    System.err.println(traceBack());
   }

  static void stop(Object...O)                                                  // Say something. provide an error trace and stop,
   {say(O);
    System.err.println(traceBack());
    System.exit(1);
   }

//D1 Testing                                                                    // Test expected output against got output

  static int testsPassed = 0, testsFailed = 0;                                  // Number of tests passed and failed

  static void ok(Object a, Object b)                                            // Check test results match expected results.
   {if (a.equals(b)) {++testsPassed; return;}
    final boolean n = b.toString().contains("\n");
    testsFailed++;
    if (n) err("Test failed. Got:\n"+b+"\n");
    else   err(a, "does not equal", b);
   }

  static void okIntegers(Integer[]E, Integer[]G)                                // Check that two integer arrays are are equal
   {final StringBuilder b = new StringBuilder();
    final int lg = G.length, le = E.length;

    if (le != lg)
     {err("Mismatched length, got", lg, "expected", le, "got:\n"+G);
      return;
     }

    int fails = 0, passes = 0;
    for (int i = 1; i <= lg; i++)
     {final Integer e = E[i-1], g = G[i-1];
      if (false)                       {}
      else if (e == null && g == null) {}
      else if (e != null && g == null) {b.append(String.format("Index %d expected %d, but got null\n", i, e   )); ++fails;}
      else if (e == null && g != null) {b.append(String.format("Index %d expected null, but got %d\n", i, g   )); ++fails;}
      else if (e != g)                 {b.append(String.format("Index %d expected %d, but got %d\n",   i, e, g)); ++fails;}
      else ++passes;
     }
    if (fails > 0) err(b);
    testsPassed += passes; testsFailed += fails;                                // Passes and fails
   }

//D0

  static void test_add()                                                        // Add
   {RiscV r = new RiscV();
    r.add();
    say(r.new Decode(r.code.elementAt(0)));
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_add();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      if (false) {}                                                             // Analyze results of tests
      else if (testsPassed == 0 && testsFailed == 0) say("No tests runs");
      else if (testsFailed == 0)   say("PASSed ALL", testsPassed, "tests");
      else say("Passed "+testsPassed+",    FAILed:", testsFailed, "tests.");
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(traceBack(e));
     }
   }
 }
