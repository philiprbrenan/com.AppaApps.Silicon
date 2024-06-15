//------------------------------------------------------------------------------
// RiscV driving Btree on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban extends Chip                                             // Create a chip that contains a Risc V processor extended with Btree instructcions
 {final static int InstructionWidthBits = 32;                                   // Instruction width in bits
  final static int XLEN                 =  4;                                   // Size of instructions
  final static boolean github_actions   =                                       // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final String name;                                                            // Name of chip
  Ban()                                                                         // Create a new program with the name of the test
   {name = Chip.currentTestName();                                              // Name of current test
   }

//D1 Logging                                                                    // Logging and tracing

//D2 Conversion                                                                 // Conversion utilities

  static String binaryString(int n, int width)                                  // Convert a integer to a binary string of specified width
   {final String b = "0".repeat(width)+Long.toBinaryString(n);
    return b.substring(b.length() - width);
   }

//D2 Printing                                                                   // Print log messages

  static void say (Object...O) {Chip.say(O);}                                   // Say something
  static StringBuilder say(StringBuilder b, Object...O) {return Chip.say(b, O);}// Say something in a string builder
  static void err (Object...O) {Chip.err(O);}                                   // Say something and provide an error trace.
  static void stop(Object...O) {Chip.stop(O);}                                  // Say something and stop

//D1 Testing                                                                    // Test expected output against got output

  void        ok(String expected)    {ok(toString(), expected);}                // Compare state of machine with expected results
  static void ok(Object a, Object b) {Chip.ok(a, b);}                           // Check test results match expected results.
  static void ok(String G, String E) {Chip.ok(G, E);}                           // Say something, provide an error trace and stop

//D0

  static void test_decode_addi()                                                // Decode an addi instruction
   {int          N = 4;
    Chip         C = new Chip();                                                // Create a new chip
    Pulse    start = C.pulse("start", 0, N);                                    // Start pulse
    Bits        x0 = C.bits("X0",     N, 0);                                    // Define registers and zero them at the start
    Bits    result = C.new BitBus("result", N);                                 // Results of addition that will be sent to the destination register
    Register    x1 = C.register("x1", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(1, "tp")))));
    Register    x2 = C.register("x2", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(2, "tp")))));
    Register    x3 = C.register("x3", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(3, "tp")))));

    Bits      addi = C.bits("addi", 32, 0xa00093);                              // addi x1,x0,10
    Bits    opCode = C.new SubBitBus("opCode", addi, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Extract op code
    Bits    funct3 = C.new SubBitBus("funct3", addi, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);      // Extract op type
//  Bits        rd = C.new SubBitBus("rd",     addi, 1 + RiscV.Decode.p_rd,          RiscV.Decode.l_rd);          // Extract destination register
    Bits        rd = C.new SubBitBus("rd",     addi, 1 + RiscV.Decode.p_rd,          N);                          // Extract destination register adapted to the width of the registers
    Bits       rs1 = C.new SubBitBus("rs1",    addi, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);         // Extract source1 register
//  Bits       imm = C.new SubBitBus("imm",    addi, 1 + RiscV.Decode.I.p_immediate, RiscV.Decode.I.l_immediate); // Extract immediate
    Bits       imm = C.new SubBitBus("imm",    addi, 1 + RiscV.Decode.I.p_immediate, N);                          // Extract immediate  adapted to the width of the registers

    EnableWord s1v = new EnableWord(x1, C.bits("x1v", RiscV.Decode.l_rs1, 1));  // Get values of registers
    EnableWord s2v = new EnableWord(x2, C.bits("x2v", RiscV.Decode.l_rs1, 2));
    EnableWord s3v = new EnableWord(x3, C.bits("x3v", RiscV.Decode.l_rs1, 3));

    Bits    f3_add = C.bits("f3_add",  RiscV.Decode.l_funct3, 0x0);             // Funct3 op codes
//  Bits    f3_xor = C.bits("f3_xor",  RiscV.Decode.l_funct3, 0x4);
//  Bits     f3_or = C.bits("f3_or",   RiscV.Decode.l_funct3, 0x6);
//  Bits    f3_and = C.bits("f3_and",  RiscV.Decode.l_funct3, 0x7);
//  Bits    f3_sll = C.bits("f3_sll",  RiscV.Decode.l_funct3, 0x1);
//  Bits    f3_srl = C.bits("f3_srl",  RiscV.Decode.l_funct3, 0x5);
//  Bits    f3_slt = C.bits("f3_slt",  RiscV.Decode.l_funct3, 0x2);
//  Bits   f3_sltu = C.bits("f3_sltu", RiscV.Decode.l_funct3, 0x3);

    Bits      rs1v = C.enableWord ("rs1v", rs1,  s1v, s2v, s3v);                // The value of the selected rs1 register
    Bits       add = C.binaryTCAdd("add",  rs1v, imm);                          // Add the selected register to the immediate value

    Bits        r1 = C.enableWordIfEq("r1", add, funct3, f3_add);               // Enable result of addition
//  Bits    Result = C.orBits("result", r1, r2);                                // Combine results of all immediate operations
    Bits    Result = C.continueBits("result", r1);                              // Connect the opcode output to the register ready to be loaded
    Words  targets = C.words      ("targets", N, 1, 2, 3);                      // Target register to load result into
    Pulse      ltd = C.delay      ("ltd",  start, 18);                          // Wait this long before attempting to load the targets
    Pulse[]     tp = C.choosePulse("tp", targets, rd, ltd);                     // Pulse loads target register

    addi.anneal();
    C.simulate();
    opCode.ok(RiscV.Decode.opArithImm);
    funct3.ok(RiscV.Decode.f3_add);
        rd.ok( 1);
       rs1.ok( 0);
       imm.ok(10);
       add.ok(10);
        r1.ok(10);
    Result.ok(10);
        x1.ok(10);                                                              // Target register set to expected value
   }

  static void test_decode_imm()                                                 // Decode an immediate instruction
   {int          N = 4;
    Chip         C = new Chip();                                                // Create a new chip
    Pulse    start = C.pulse("start", 0, N);                                    // Start pulse
    Bits       one = C.bits("one",    N, 1);                                    // Constant one
    Bits        x0 = C.bits("X0",     N, 0);                                    // Define registers and zero them at the start
    Bits    result = C.new BitBus("result", N);                                 // Results of addition that will be sent to the destination register
    Register    x1 = C.register("x1", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(1, "tp"))))); // Initialize or reload each register
    Register    x2 = C.register("x2", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(2, "tp")))));
    Register    x3 = C.register("x3", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(3, "tp")))));

    Bits      addi = C.bits("addi", 32, 0xa00093);                              // addi x1,x0,10
    Bits    opCode = C.new SubBitBus("opCode", addi, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Extract op code
    Bits    funct3 = C.new SubBitBus("funct3", addi, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);      // Extract op type
//  Bits        rd = C.new SubBitBus("rd",     addi, 1 + RiscV.Decode.p_rd,          RiscV.Decode.l_rd);          // Extract destination register
    Bits        rd = C.new SubBitBus("rd",     addi, 1 + RiscV.Decode.p_rd,          N);                          // Extract destination register adapted to the width of the registers
    Bits       rs1 = C.new SubBitBus("rs1",    addi, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);         // Extract source1 register
//  Bits       imm = C.new SubBitBus("imm",    addi, 1 + RiscV.Decode.I.p_immediate, RiscV.Decode.I.l_immediate); // Extract immediate
    Bits       imm = C.new SubBitBus("imm",    addi, 1 + RiscV.Decode.I.p_immediate, N);                          // Extract immediate  adapted to the width of the registers

    EnableWord s1v = new EnableWord(x1, C.bits("x1v", RiscV.Decode.l_rs1, 1));  // Get values of registers
    EnableWord s2v = new EnableWord(x2, C.bits("x2v", RiscV.Decode.l_rs1, 2));
    EnableWord s3v = new EnableWord(x3, C.bits("x3v", RiscV.Decode.l_rs1, 3));

    Bits    f3_add = C.bits("f3_add",  RiscV.Decode.l_funct3, 0x0);             // Funct3 op codes
    Bits    f3_xor = C.bits("f3_xor",  RiscV.Decode.l_funct3, 0x4);
    Bits     f3_or = C.bits("f3_or",   RiscV.Decode.l_funct3, 0x6);
    Bits    f3_and = C.bits("f3_and",  RiscV.Decode.l_funct3, 0x7);
//  Bits    f3_sll = C.bits("f3_sll",  RiscV.Decode.l_funct3, 0x1);
//  Bits    f3_srl = C.bits("f3_srl",  RiscV.Decode.l_funct3, 0x5);
//  Bits    f3_slt = C.bits("f3_slt",  RiscV.Decode.l_funct3, 0x2);
    Bits   f3_sltu = C.bits("f3_sltu", RiscV.Decode.l_funct3, 0x3);

    Bits      rs1v = C.enableWord ("rs1v",  rs1,  s1v, s2v, s3v);               // The value of the selected rs1 register
    Bits      addI = C.binaryTCAdd("addI",  rs1v, imm);                         // Add the selected register to the immediate value
    Bits      xorI = C.xorBits    ("xorI",  rs1v, imm);                         // Xor the selected register to the immediate value
    Bits       orI = C.orBits     ("orI",   rs1v, imm);                         // Or the selected register to the immediate value
    Bits      andI = C.andBits    ("andI",  rs1v, imm);                         // And the selected register to the immediate value
    Bit    compare = C.compareLt  ("comp",  rs1v, imm);                         // Compare
    Bits     sltui = C.enableWord ("sltui", one,  compare);                     // Enable a one if the register is less than the immediate value using unsigned arithmetic

    Bits     rAddi = C.enableWordIfEq("rAddi",   addI, funct3, f3_add);         // Enable result of addition
    Bits     rXori = C.enableWordIfEq("rXori",   xorI, funct3, f3_xor);         // Enable result of xor
    Bits      rOri = C.enableWordIfEq("rOri",     orI, funct3, f3_or);          // Enable result of or
    Bits     rAndi = C.enableWordIfEq("rAndi",   andI, funct3, f3_and);         // Enable result of and
    Bits    rSltui = C.enableWordIfEq("rSltui", sltui, funct3, f3_sltu);        // Enable a one if the register is less than the immediate value using unsigned arithmetic
    Bits    Result = C.orBits("result", rAddi, rXori, rOri, rAndi, rSltui);     // Combine results of all immediate operations

    Words  targets = C.words      ("targets", N, 1, 2, 3);                      // Target register to load result into
    Pulse      ltd = C.delay      ("ltd",  start, 22);                          // Wait this long before attempting to load the targets. Its cruclai thatthis be just long ewnough fo r all the computation to complete otherwise we get a partial answer which is usually wrong.
    Pulse[]     tp = C.choosePulse("tp", targets, rd, ltd);                     // Pulse loads target register

    addi.anneal();
    C.simulate();
    opCode.ok(RiscV.Decode.opArithImm);
    funct3.ok(RiscV.Decode.f3_add);
        rd.ok( 1);
       rs1.ok( 0);
       imm.ok(10);
      addI.ok(10);
       orI.ok(10);
     rAddi.ok(10);
     rXori.ok(0);
      rOri.ok(0);
     rAndi.ok(0);
    rSltui.ok(0);
    Result.ok(10);
        x1.ok(10);                                                              // Target register set to expected value
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_decode_addi();
   }

  static void newTests()                                                        // Tests being worked on
   {test_decode_imm();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      Chip.testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(Chip.traceBack(e));
     }
   }
 }
