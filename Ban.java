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

    Bits    decode = C.bits("addi", 32, 0xa00093);                              // addi x1,x0,10
    Bits    opCode = C.new SubBitBus("opCode", decode, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Extract op code
    Bits    funct3 = C.new SubBitBus("funct3", decode, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);      // Extract op type
//  Bits        rd = C.new SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,          RiscV.Decode.l_rd);          // Extract destination register
    Bits        rd = C.new SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,          N);                          // Extract destination register adapted to the width of the registers
    Bits       rs1 = C.new SubBitBus("rs1",    decode, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);         // Extract source1 register
//  Bits       imm = C.new SubBitBus("imm",    decode, 1 + RiscV.Decode.I.p_immediate, RiscV.Decode.I.l_immediate); // Extract immediate
    Bits       imm = C.new SubBitBus("imm",    decode, 1 + RiscV.Decode.I.p_immediate, N);                          // Extract immediate  adapted to the width of the registers

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

    decode.anneal();
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

  static void test_decode_I32()                                                 // Decode an immediate instruction
   {int          N = 4;
    Chip         C = new Chip();                                                // Create a new chip
    Pulse    start = C.pulse("start", 0, N);                                    // Start pulse
    Bits       one = C.bits ("one",   N, 1);                                    // Constant one
    Bits        x0 = C.bits ("X0",    N, 0);                                    // Define registers and zero them at the start
    Bits    result = C.new BitBus("result", N);                                 // Results of addition that will be sent to the destination register
    Register    x1 = C.register("x1", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(1, "tp"))))); // Initialize or reload each register
    Register    x2 = C.register("x2", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(2, "tp")))));
    Register    x3 = C.register("x3", new RegIn(x0, start), new RegIn(result, C.new Pulse(C.new Bit(n(3, "tp")))));

    Bits    decode = C.bits("decode", 32, 0xa00093);                            // addi x1,x0,10
    Bits    opCode = C.new SubBitBus("opCode", decode, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Extract op code
    Bits    funct3 = C.new SubBitBus("funct3", decode, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);      // Extract op type
    Bits    funct5 = C.new SubBitBus("funct5", decode, 1 + RiscV.Decode.p_funct5,      RiscV.Decode.l_funct5);      // Extract op type
    Bits    funct7 = C.new SubBitBus("funct7", decode, 1 + RiscV.Decode.p_funct7,      RiscV.Decode.l_funct7);      // Extract op type
//  Bits        rd = C.new SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,          RiscV.Decode.l_rd);          // Extract destination register
    Bits        rd = C.new SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,          N);                          // Extract destination register adapted to the width of the registers
    Bits       rs1 = C.new SubBitBus("rs1",    decode, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);         // Extract source 1 register
    Bits       rs2 = C.new SubBitBus("rs2",    decode, 1 + RiscV.Decode.p_rs2,         RiscV.Decode.l_rs2);         // Extract source 2 register
//  Bits       imm = C.new SubBitBus("imm",    decode, 1 + RiscV.Decode.I.p_immediate, RiscV.Decode.I.l_immediate); // Extract immediate
    Bits       imm = C.new SubBitBus("imm",    decode, 1 + RiscV.Decode.I.p_immediate, N);                          // Extract immediate  adapted to the width of the registers

    EnableWord s1v = new EnableWord(x1, C.bits("x1v", RiscV.Decode.l_rs1, 1));  // Get value of s1 register
    EnableWord s2v = new EnableWord(x2, C.bits("x2v", RiscV.Decode.l_rs1, 2));
    EnableWord s3v = new EnableWord(x3, C.bits("x3v", RiscV.Decode.l_rs1, 3));

    EnableWord S1v = new EnableWord(x1, C.bits("X1v", RiscV.Decode.l_rs2, 1));  // Get values of s2 register
    EnableWord S2v = new EnableWord(x2, C.bits("X2v", RiscV.Decode.l_rs2, 2));
    EnableWord S3v = new EnableWord(x3, C.bits("X3v", RiscV.Decode.l_rs2, 3));

    Bits    f3_add = C.bits("f3_add",  RiscV.Decode.l_funct3, 0x0);             // Funct3 op codes
    Bits    f3_xor = C.bits("f3_xor",  RiscV.Decode.l_funct3, 0x4);
    Bits     f3_or = C.bits("f3_or",   RiscV.Decode.l_funct3, 0x6);
    Bits    f3_and = C.bits("f3_and",  RiscV.Decode.l_funct3, 0x7);
    Bits    f3_sll = C.bits("f3_sll",  RiscV.Decode.l_funct3, 0x1);
    Bits    f3_srl = C.bits("f3_srl",  RiscV.Decode.l_funct3, 0x5);
    Bits    f3_slt = C.bits("f3_slt",  RiscV.Decode.l_funct3, 0x2);
    Bits   f3_sltu = C.bits("f3_sltu", RiscV.Decode.l_funct3, 0x3);

    Bits      rs1v = C.enableWord          ("rs1v",  rs1,  s1v, s2v, s3v);      // The value of the selected s1 register
    Bits      rs2v = C.enableWord          ("rs2v",  rs2,  S1v, S2v, S3v);      // The value of the selected s2 register

    Bits      addI = C.binaryTCAdd         ("addI",  rs1v, imm);                // Immediate operation
    Bits      subI = C.binaryTCSubtract    ("subI",  rs1v, imm);
    Bits      xorI = C.xorBits             ("xorI",  rs1v, imm);
    Bits       orI = C.orBits              ("orI",   rs1v, imm);
    Bits      andI = C.andBits             ("andI",  rs1v, imm);
    Bits      sllI = C.shiftLeftMultiple   ("sllI",  rs1v, imm);
    Bits      srlI = C.shiftRightMultiple  ("srlI",  rs1v, imm);
    Bits      sraI = C.shiftRightArithmetic("sraI",  rs1v, imm);
    Bit       cmpI = C.binaryTCCompareLt   ("cmpI",  rs1v, imm);
    Bit      cmpuI = C.compareLt           ("cmpuI", rs1v, imm);
    Bits      sltI = C.enableWord          ("sltI",  one,  cmpI);
    Bits     sltuI = C.enableWord          ("sltuI", one,  cmpuI);

    Bits     rAddi = C.enableWordIfEq("rAddi",   addI, funct3, f3_add);         // Enable result of immediate operation
    Bits     rSubi = C.enableWordIfEq("rSubi",   subI, funct3, f3_add);
    Bits     rXori = C.enableWordIfEq("rXori",   xorI, funct3, f3_xor);
    Bits      rOri = C.enableWordIfEq("rOri",     orI, funct3, f3_or);
    Bits     rAndi = C.enableWordIfEq("rAndi",   andI, funct3, f3_and);
    Bits     rSlli = C.enableWordIfEq("rSlli",   sllI, funct3, f3_sll);
    Bits     rSrli = C.enableWordIfEq("rSrli",   srlI, funct3, f3_srl);
    Bits     rSrai = C.enableWordIfEq("rSrai",   sraI, funct3, f3_srl);
    Bits     rSlti = C.enableWordIfEq("rSlti",   sltI, funct3, f3_slt);
    Bits    rSltui = C.enableWordIfEq("rSltui", sltuI, funct3, f3_sltu);
    Bits       iR0 = C.orBits("iR0", rAddi, rXori, rOri, rAndi, rSlli, rSrli, rSlti, rSltui);
    Bits       iR2 = C.orBits("iR2", rSubi, rSrai);

    EnableWord eiR0 = new EnableWord(iR0, C.bits("if70", RiscV.Decode.l_funct7, 0));  // Decode funct7 for immediate opcode
    EnableWord eiR2 = new EnableWord(iR2, C.bits("if72", RiscV.Decode.l_funct7, 2));

    Bits        iR = C.enableWord("iR", funct7, eiR0, eiR2);                    // Choose the dyadic operation

    Bits      addD = C.binaryTCAdd         ("addD",  rs1v, rs2v);
    Bits      subD = C.binaryTCSubtract    ("subD",  rs1v, rs2v);
    Bits      xorD = C.xorBits             ("xorD",  rs1v, rs2v);
    Bits       orD = C.orBits              ("orD",   rs1v, rs2v);
    Bits      andD = C.andBits             ("andD",  rs1v, rs2v);
    Bits      sllD = C.shiftLeftMultiple   ("sllD",  rs1v, rs2v);
    Bits      srlD = C.shiftRightMultiple  ("srlD",  rs1v, rs2v);
    Bits      sraD = C.shiftRightArithmetic("sraD",  rs1v, rs2v);
    Bit       cmpD = C.binaryTCCompareLt   ("cmpD",  rs1v, rs2v);
    Bit      cmpuD = C.compareLt           ("cmpuD", rs1v, rs2v);
    Bits      sltD = C.enableWord          ("sltD",  one,  cmpD);
    Bits     sltuD = C.enableWord          ("sltuD", one,  cmpuD);

    Bits     rAddd = C.enableWordIfEq("rAddd",   addD, funct3, f3_add);         // Enable result of dyadic operation
    Bits     rSubd = C.enableWordIfEq("rSubd",   subD, funct3, f3_add);
    Bits     rXord = C.enableWordIfEq("rXord",   xorD, funct3, f3_xor);
    Bits      rOrd = C.enableWordIfEq("rOrd",     orD, funct3, f3_or);
    Bits     rAndd = C.enableWordIfEq("rAndd",   andD, funct3, f3_and);
    Bits     rSlld = C.enableWordIfEq("rSlld",   sllD, funct3, f3_sll);
    Bits     rSrld = C.enableWordIfEq("rSrld",   srlD, funct3, f3_srl);
    Bits     rSrad = C.enableWordIfEq("rSrad",   sraD, funct3, f3_srl);
    Bits     rSltd = C.enableWordIfEq("rSltd",   sltD, funct3, f3_slt);
    Bits    rSltud = C.enableWordIfEq("rSltud", sltuD, funct3, f3_sltu);
    Bits       dR0 = C.orBits("dR0", rAddd, rXord, rOrd, rAndd, rSlld, rSrld, rSltd, rSltud);
    Bits       dR2 = C.orBits("dR2", rSubd, rSrad);

    EnableWord edR0 = new EnableWord(dR0, C.bits("df70", RiscV.Decode.l_funct7, 0));  // Decode funct7 for dyadic operation
    EnableWord edR2 = new EnableWord(dR2, C.bits("df72", RiscV.Decode.l_funct7, 2));

    Bits        dR = C.enableWord("dR", funct7, edR0, edR2);                    // Choose the dyadic operation

    EnableWord eid13 = new EnableWord(iR, C.bits("opCode13", RiscV.Decode.l_opCode, 0x13)); // Decode funct7 for dyadic operation
    EnableWord eid33 = new EnableWord(dR, C.bits("opCode33", RiscV.Decode.l_opCode, 0x33)); // Decode funct7 for dyadic operation

    Bits    Result = C.enableWord("result", opCode, eid13, eid33);              // Choose between immediate or dyadic operation

    Words  targets = C.words      ("targets", N, 1, 2, 3);                      // Target register to load result into
    Pulse      ltd = C.delay      ("ltd",  start, 28);                          // Wait this long before attempting to load the targets. Its cruclai thatthis be just long ewnough fo r all the computation to complete otherwise we get a partial answer which is usually wrong.
    Pulse[]     tp = C.choosePulse("tp", targets, rd, ltd);                     // Pulse loads target register

    decode.anneal();
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
    test_decode_I32();
   }

  static void newTests()                                                        // Tests being worked on
   {test_decode_I32();
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
