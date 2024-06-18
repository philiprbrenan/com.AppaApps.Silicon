//------------------------------------------------------------------------------
// RiscV driving Btree on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban extends Chip                                             // Create a chip that contains a Risc V processor extended with Btree instructcions
 {final static int InstructionWidthBits  = 32;                                  // Instruction width in bits
  final static int InstructionWidthBytes =  4;                                  // Instruction width in bytes
  final static int XLEN                  = 32;                                  // Number of bits in a register
  final static boolean github_actions    =                                      // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final String name;                                                            // Name of chip
  Ban()                                                                         // Create a new program with the name of the test
   {name = Chip.currentTestName();                                              // Name of current test
   }

//D1 Numerics                                                                   // Numeric facilitites

  static int min(int...a)                                                       // Minimum of some numbers
   {if (a.length == 0) stop("Need some integers");
    int m = a[0];
    for (int i = 0; i < a.length; i++) m = m > a[i] ? a[i] : m;
    return m;
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
    Register    x1 = C.register("x1", new RegIn(x0, start), new RegIn(result, C.pulse(n(1, "tp"))));
    Register    x2 = C.register("x2", new RegIn(x0, start), new RegIn(result, C.pulse(n(2, "tp"))));
    Register    x3 = C.register("x3", new RegIn(x0, start), new RegIn(result, C.pulse(n(3, "tp"))));

    Bits    decode = C.bits("addi", 32, 0xa00093);                              // addi x1,x0,10
    Bits    opCode = C.new SubBitBus("opCode", decode, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Extract op code
    Bits    funct3 = C.new SubBitBus("funct3", decode, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);      // Extract op type
    Bits        rd = C.new SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,   min(N, RiscV.Decode.l_rd));         // Extract destination register adapted to the width of the registers
    Bits       rs1 = C.new SubBitBus("rs1",    decode, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);         // Extract source1 register
    Bits       imm = C.new SubBitBus("imm",    decode, 1 + RiscV.Decode.I.p_immediate, min(N, RiscV.Decode.I.l_immediate)); // Extract immediate

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

  static void test_decode_RV32I ()                                              // Decode an immediate instruction
   {final int    N = 4;                                                         // Register widths in bits
    final int  LF3 = RiscV.Decode.l_funct3;                                     // Length of function codes
    final int  LF5 = RiscV.Decode.l_funct5;
    final int  LF7 = RiscV.Decode.l_funct7;

    Chip         C = new Chip();                                                // Create a new chip
    Pulse    start = C.pulse("start",  0, N);                                   // Start pulse
    Pulse   update = C.pulse("update", 0, N, 48);                               // Update registers at end of decode/execute cycle.
    Bits       one = C.bits ("one",    N, 1);                                   // Constant one
    Bits      insw = C.bits ("insw",   N, InstructionWidthBytes);               // Instruction width

    Bits        x0 = C.bits ("X0",     N, 0);                                   // Define registers and zero them at the start
    Bits        PC = C.collectBits("PC", N);                                    // Next value for program counter forward declaration
    Register    pc = C.register("pc", C.regIn(x0, start), C.regIn(PC, update)); // Risc V registers

    Bits     X[] = new Bits    [N];                                             // New value for register
    Register x[] = new Register[N];                                             // Registers
    for (int i = 1; i < N; i++)
     {X[i] = C.collectBits("X"+i, N);
      x[i] = C.register   ("x"+i, new RegIn(x0, start), new RegIn(X[i], update));
     }

    Bits    decode = C.bits("decode", 32, 0xa00093);                            // addi x1,x0,10

    Bits    opCode = C.new  SubBitBus("opCode", decode, 1 + RiscV.Decode.p_opCode,      RiscV.Decode.l_opCode);      // Decode the instruction
    Bits    funct3 = C.new  SubBitBus("funct3", decode, 1 + RiscV.Decode.p_funct3,      RiscV.Decode.l_funct3);
    Bits    funct5 = C.new  SubBitBus("funct5", decode, 1 + RiscV.Decode.p_funct5,      RiscV.Decode.l_funct5);
    Bits    funct7 = C.new  SubBitBus("funct7", decode, 1 + RiscV.Decode.p_funct7,      RiscV.Decode.l_funct7);
    Bits        rd = C.new  SubBitBus("rd",     decode, 1 + RiscV.Decode.p_rd,   min(N, RiscV.Decode.l_rd));
    Bits       rs1 = C.new  SubBitBus("rs1",    decode, 1 + RiscV.Decode.p_rs1,         RiscV.Decode.l_rs1);
    Bits       rs2 = C.new  SubBitBus("rs2",    decode, 1 + RiscV.Decode.p_rs2,         RiscV.Decode.l_rs2);

                                                                                // Decode immediate field
    Bits      immi = C.new  SubBitBus("immi",   decode, 1 + RiscV.Decode.I.p_immediate, min(N, RiscV.Decode.I.l_immediate)); // Imipac: the weapon that defends itself.
    Bits      immu = C.new  SubBitBus("immu",   decode, 1 + RiscV.Decode.U.p_immediate, min(N, RiscV.Decode.U.l_immediate));
    Bits      immb = C.new ConCatBits("immb",                                   // Immediate operand from B format
      x0.b(1),                                                                  // First bit known to be 0.
      decode.b( 9), decode.b(10), decode.b(11), decode.b(12),                   // Field offsets are one based: in riscv-spec-20191213.pdf they are zero based.
      decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31),
      decode.b( 8),
      decode.b(32));

    Bits      immj = C.new ConCatBits("immj",                                   // Immediate operand from J format
       decode.b(22), decode.b(23), decode.b(24), decode.b(25), decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31),
       decode.b(21),
       decode.b(13), decode.b(14), decode.b(15), decode.b(16), decode.b(17), decode.b(18), decode.b(19), decode.b(20),
       decode.b(32));

    Bits      imms = C.new ConCatBits("imms",                                   // Immediate operand from S format
       decode.b( 8), decode.b( 9), decode.b(10), decode.b(11), decode.b(12),
       decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31), decode.b(32));

    Bits      immB = C.binaryTCSignExtend("immB", immb,  N);                    // Sign extend the immediate field
    Bits      immI = C.binaryTCSignExtend("immI", immi,  N);
    Bits      immJ = C.binaryTCSignExtend("immJ", immj,  N);
    Bits      immS = C.binaryTCSignExtend("immS", imms,  N);
    Bits      immU = C.binaryTCSignExtend("immU", immu,  N);


    EnableWord s1v = new EnableWord(x[1], C.bits("x1v", RiscV.Decode.l_rs1, 1));// Get value of s1 register
    EnableWord s2v = new EnableWord(x[2], C.bits("x2v", RiscV.Decode.l_rs1, 2));
    EnableWord s3v = new EnableWord(x[3], C.bits("x3v", RiscV.Decode.l_rs1, 3));

    EnableWord S1v = new EnableWord(x[1], C.bits("X1v", RiscV.Decode.l_rs2, 1));// Get values of s2 register
    EnableWord S2v = new EnableWord(x[2], C.bits("X2v", RiscV.Decode.l_rs2, 2));
    EnableWord S3v = new EnableWord(x[3], C.bits("X3v", RiscV.Decode.l_rs2, 3));

    Bits    f3_add = C.bits("f3_add",  LF3, RiscV.Decode.f3_add);               // Funct3 op codes
    Bits    f3_xor = C.bits("f3_xor",  LF3, 4);
    Bits     f3_or = C.bits("f3_or",   LF3, 6);
    Bits    f3_and = C.bits("f3_and",  LF3, 7);
    Bits    f3_sll = C.bits("f3_sll",  LF3, 1);
    Bits    f3_srl = C.bits("f3_srl",  LF3, 5);
    Bits    f3_slt = C.bits("f3_slt",  LF3, 2);
    Bits   f3_sltu = C.bits("f3_sltu", LF3, 3);
    Bits    f3_beq = C.bits("f3_beq",  LF3, 0);
    Bits    f3_bne = C.bits("f3_bne",  LF3, 1);
    Bits    f3_blt = C.bits("f3_blt",  LF3, 4);
    Bits    f3_bge = C.bits("f3_bge",  LF3, 5);
    Bits   f3_bltu = C.bits("f3_bltu", LF3, 6);
    Bits   f3_bgeu = C.bits("f3_bgeu", LF3, 7);

    Bits      rs1v = C.enableWord          ("rs1v",  rs1,  s1v, s2v, s3v);      // The value of the selected s1 register
    Bits      rs2v = C.enableWord          ("rs2v",  rs2,  S1v, S2v, S3v);      // The value of the selected s2 register

    Bits      addI = C.binaryTCAdd         ("addI",  rs1v, immI);               // Immediate operation
    Bits      subI = C.binaryTCSubtract    ("subI",  rs1v, immI);
    Bits      xorI = C.xorBits             ("xorI",  rs1v, immI);
    Bits       orI = C.orBits              ("orI",   rs1v, immI);
    Bits      andI = C.andBits             ("andI",  rs1v, immI);
    Bits      sllI = C.shiftLeftMultiple   ("sllI",  rs1v, immI);
    Bits      srlI = C.shiftRightMultiple  ("srlI",  rs1v, immI);
    Bits      sraI = C.shiftRightArithmetic("sraI",  rs1v, immI);
    Bit       cmpI = C.binaryTCCompareLt   ("cmpI",  rs1v, immI);
    Bit      cmpuI = C.compareLt           ("cmpuI", rs1v, immI);
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

    EnableWord eiR0 = new EnableWord(iR0, C.bits("if70", LF7, 0));              // Decode funct7 for immediate opcode
    EnableWord eiR2 = new EnableWord(iR2, C.bits("if72", LF7, 2));

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

    EnableWord edR0 = new EnableWord(dR0, C.bits("df70", LF7, 0));              // Decode funct7 for dyadic operation
    EnableWord edR2 = new EnableWord(dR2, C.bits("df72", LF7, 2));

    Bits        dR = C.enableWord("dR", funct7, edR0, edR2);                    // Choose the dyadic operation

    Bit         eq12 = C.compareEq        ("eq12",  rs1v, rs2v);
    Bit         lt12 = C.binaryTCCompareLt("lt12",  rs1v, rs2v);
    Bit        ltu12 = C.compareLt        ("ltu12", rs1v, rs2v);
    Bits         pci = C.binaryTCAdd("pci", pc, immB);                          // Pc plus sign extended immediate
    Bits         pc4 = C.binaryTCAdd("pc4", pc, insw);                          // Pc plus instruction width

    Bits        rBeq = C.chooseFromTwoWords("rBeq",  pc4, pci,  eq12);          // Jump targets
    Bits        rBne = C.chooseFromTwoWords("rBne",  pci, pc4,  eq12);
    Bits        rBlt = C.chooseFromTwoWords("rBlt",  pc4, pci,  lt12);
    Bits        rBge = C.chooseFromTwoWords("rBge",  pci, pc4,  lt12);
    Bits       rBltu = C.chooseFromTwoWords("rBltu", pci, pc4, ltu12);
    Bits       rBgeu = C.chooseFromTwoWords("rBgeu", pc4, pci, ltu12);

    Bits        eBeq = C.enableWordIfEq("eBeq",  rBeq,  funct3, f3_beq);        // Enable result of branch operation
    Bits        eBne = C.enableWordIfEq("eBne",  rBne,  funct3, f3_bne);
    Bits        eBlt = C.enableWordIfEq("eBlt",  rBlt,  funct3, f3_blt);
    Bits        eBge = C.enableWordIfEq("eBge",  rBge,  funct3, f3_bge);
    Bits       eBltu = C.enableWordIfEq("eBltu", rBltu, funct3, f3_bltu);
    Bits       eBgeu = C.enableWordIfEq("eBgeu", rBgeu, funct3, f3_bgeu);
    Bits      branch = C.orBits("branch", eBeq, eBne, eBlt, eBge, eBltu, eBgeu);
    Bit  branchInstr = C.compareEq("branchInstr", opCode,    RiscV.Decode.opBranch); // True if we are on a branch instruction
                       C.chooseFromTwoWords("PC", pc4, branch, branchInstr);    // Advance normally or jump by branch instruction immediate amount

    EnableWord eid13 = new EnableWord(iR, C.bits("opCode13", RiscV.Decode.l_opCode, RiscV.Decode.opArithImm)); // Decode funct7 for immediate operation
    EnableWord eid33 = new EnableWord(dR, C.bits("opCode33", RiscV.Decode.l_opCode, RiscV.Decode.opArith   )); // Decode funct7 for dyadic operation

    Bits      result = C.enableWord("result", opCode, eid13, eid33);            // Choose between immediate or dyadic operation

    for (int i = 1; i < N; i++)                                                 // Values to reload back into registers
      C.chooseThenElseIfEQ("X"+i, result, x[i], rd, i);

     decode.anneal();
     f3_beq.anneal();
     f3_bne.anneal();
     f3_blt.anneal();
     f3_bge.anneal();
    f3_bltu.anneal();
    f3_bgeu.anneal();
       eq12.anneal();
       lt12.anneal();
      ltu12.anneal();
        pci.anneal();
        pc4.anneal();
    C.executionTrack(
      "pc    pci   pc4   PC    start update  e   l",
      "%s  %s  %s  %s  %s   %s     %s %s",
       pc, pci, pc4, PC, start, update, C.collectBits("pc_e", N), C.getGate("pc_l"));

    C.simulationSteps(64);
    C.simulate();
    C.printExecutionTrace(); //stop();
    opCode.ok(RiscV.Decode.opArithImm);
    funct3.ok(RiscV.Decode.f3_add);
        rd.ok(   1);
       rs1.ok(   0);
      immB.ok(   0);
      immI.ok(  10);
      immJ.ok(   5);
      immS.ok(   1);
      immU.ok(   0);
        pc.ok(   4);
       pci.ok(   4);
       pc4.ok(   4);
      addI.ok(  10);
       orI.ok(  10);
     rAddi.ok(  10);
     rXori.ok(   0);
      rOri.ok(   0);
     rAndi.ok(   0);
    rSltui.ok(   0);
      x[1].ok(  10);
      x[2].ok(   0);
      x[3].ok(   0);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_decode_addi();
    test_decode_RV32I();
   }

  static void newTests()                                                        // Tests being worked on
   {//test_decode_addi();
    test_decode_RV32I();
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
