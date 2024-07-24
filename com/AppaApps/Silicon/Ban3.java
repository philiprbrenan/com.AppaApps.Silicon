//------------------------------------------------------------------------------
// RiscV driving Btree on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban extends Chip                                             // Create a chip that contains a Risc V processor extended with Btree instructcions
 {final static int InstructionWidthBytes =  4;                                  // Instruction width in bytes
  final static int XLEN                  = 32;                                  // Number of bits in a register
  final static boolean github_actions    =                                      // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final static class D extends RiscV.Decode {D(){super(null);}}                 // Easier access to static constants

  final static int                                                              // Constants used to decode an instruction
    p_opCode     = 1 + D.p_opCode,      l_opCode     = D.l_opCode,
    p_funct3     = 1 + D.p_funct3,      l_funct3     = D.l_funct3,
    p_funct5     = 1 + D.p_funct5,      l_funct5     = D.l_funct5,
    p_funct7     = 1 + D.p_funct7,      l_funct7     = D.l_funct7,
    p_rd         = 1 + D.p_rd,          l_rd         = D.l_rd,
    p_rs1        = 1 + D.p_rs1,         l_rs1        = D.l_rs1,
    p_rs2        = 1 + D.p_rs2,         l_rs2        = D.l_rs2,

    ip_immediate = 1 + D.I.p_immediate, il_immediate = D.I.l_immediate,
    up_immediate = 1 + D.U.p_immediate, ul_immediate = D.U.l_immediate,

    opArithImm   = D.opArithImm,        opArith      = D.opArith;

//D0

  static Chip RV32I()                                                           // Decode and execute an RV32I instruction
   {Chip       C = new Chip(currentTestName());                                 // Create a new chip
    Pulse  start = C.pulse("start",  0, 2);                                     // Start pulse has to be wide enough to load the registers
    Pulse update = C.pulse("update", 0, 6, 34);                                 // Update registers pulse at end of decode/execute cycle.
    Bits     one = C.bits ("one",    XLEN, 1);                                  // Constant one

    Bits      x0 = C.bits       ("X0",  XLEN, 0);                               // Define registers and zero them at the start
    Bits      PC = C.collectBits("PC",  XLEN);                                  // Next value for program counter forward declaration
    Register  pc = C.register("pc", C.regIn(x0, start), C.regIn(PC, update));   // Risc V registers

    Bits     X[] = new Bits    [XLEN];                                          // New value for register
    Register x[] = new Register[XLEN];                                          // Registers
    for (int i = 1; i < XLEN; i++)                                              // Load registers. Initially from x0 later with instruction results
     {X[i] = C.collectBits("X"+i, XLEN);
      x[i] = C.register   ("x"+i, C.regIn(x0, start), C.regIn(X[i], update));
     }

    Bits decode = C.inputBits("decode", XLEN);                                  // Instruction to decode and execute

    Bits opCode = C.subBitBus("opCode", decode, p_opCode,     l_opCode);        // Decode the instruction
    Bits funct3 = C.subBitBus("funct3", decode, p_funct3,     l_funct3);
    Bits funct5 = C.subBitBus("funct5", decode, p_funct5,     l_funct5);
    Bits funct7 = C.subBitBus("funct7", decode, p_funct7,     l_funct7);
    Bits     rd = C.subBitBus("rd",     decode, p_rd,         l_rd);
    Bits    rs1 = C.subBitBus("rs1",    decode, p_rs1,        l_rs1);
    Bits    rs2 = C.subBitBus("rs2",    decode, p_rs2,        l_rs2);
                                                                                // Decode immediate field
    Bits   immi = C.subBitBus("immi",   decode, ip_immediate, il_immediate);    // Imipac: the weapon that defends itself.
    Bits   immu = C.subBitBus("immu",   decode, up_immediate, ul_immediate);    // Immediate from U format
    Bits   immb = C.conCatBits("immb",                                          // Immediate operand from B format
      x0.b(1),                                                                  // First bit known to be 0.
      decode.b( 9), decode.b(10), decode.b(11), decode.b(12),                   // Field offsets are one based: in riscv-spec-20191213.pdf they are zero based.
      decode.b(26), decode.b(27), decode.b(28), decode.b(29),
      decode.b(30), decode.b(31),
      decode.b( 8),
      decode.b(32));

    Bits      immj = C.conCatBits("immj",                                       // Immediate operand from J format
       decode.b(22), decode.b(23), decode.b(24), decode.b(25), decode.b(26),
       decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31),
       decode.b(21),
       decode.b(13), decode.b(14), decode.b(15), decode.b(16), decode.b(17),
       decode.b(18), decode.b(19), decode.b(20),
       decode.b(32));

    Bits      imms = C.conCatBits("imms",                                       // Immediate operand from S format
       decode.b( 8), decode.b( 9), decode.b(10), decode.b(11), decode.b(12),
       decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30),
       decode.b(31), decode.b(32));

    Bits      immB = C.binaryTCSignExtend("immB", immb, XLEN);                  // Sign extend the immediate field
    Bits      immI = C.binaryTCSignExtend("immI", immi, XLEN);
    Bits      immJ = C.binaryTCSignExtend("immJ", immj, XLEN);
    Bits      immS = C.binaryTCSignExtend("immS", imms, XLEN);
    Bits      immU = C.binaryTCSignExtend("immU", immu, XLEN);

    EnableWord s1v = C.enableWord(x[1], C.bits("x1v", D.l_rs1, 1));             // Get value of s1 register
    EnableWord s2v = C.enableWord(x[2], C.bits("x2v", D.l_rs1, 2));
    EnableWord s3v = C.enableWord(x[3], C.bits("x3v", D.l_rs1, 3));

    EnableWord S1v = C.enableWord(x[1], C.bits("X1v", D.l_rs2, 1));             // Get values of s2 register
    EnableWord S2v = C.enableWord(x[2], C.bits("X2v", D.l_rs2, 2));
    EnableWord S3v = C.enableWord(x[3], C.bits("X3v", D.l_rs2, 3));

    Bits    f3_add = C.bits("f3_add",  l_funct3, D.f3_add);                     // Funct3 op codes
    Bits    f3_xor = C.bits("f3_xor",  l_funct3, D.f3_xor);
    Bits     f3_or = C.bits("f3_or",   l_funct3, D.f3_or);
    Bits    f3_and = C.bits("f3_and",  l_funct3, D.f3_and);
    Bits    f3_sll = C.bits("f3_sll",  l_funct3, D.f3_sll);
    Bits    f3_srl = C.bits("f3_srl",  l_funct3, D.f3_srl);
    Bits    f3_slt = C.bits("f3_slt",  l_funct3, D.f3_slt);
    Bits   f3_sltu = C.bits("f3_sltu", l_funct3, D.f3_sltu);
    Bits    f3_beq = C.bits("f3_beq",  l_funct3, D.f3_beq);
    Bits    f3_bne = C.bits("f3_bne",  l_funct3, D.f3_bne);
    Bits    f3_blt = C.bits("f3_blt",  l_funct3, D.f3_blt);
    Bits    f3_bge = C.bits("f3_bge",  l_funct3, D.f3_bge);
    Bits   f3_bltu = C.bits("f3_bltu", l_funct3, D.f3_bltu);
    Bits   f3_bgeu = C.bits("f3_bgeu", l_funct3, D.f3_bgeu);

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
    Bits       iR0 = C.orBits("iR0", rAddi, rXori, rOri,  rAndi,
                                     rSlli, rSrli, rSlti, rSltui);
    Bits       iR2 = C.orBits("iR2", rSubi, rSrai);

    var       eiR0 = new EnableWord(iR0, C.bits("if70", l_funct7, 0));          // Decode funct7 for immediate opcode
    var       eiR2 = new EnableWord(iR2, C.bits("if72", l_funct7, 2));

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
    Bits       dR0 = C.orBits("dR0", rAddd, rXord, rOrd,  rAndd,
                                     rSlld, rSrld, rSltd, rSltud);
    Bits       dR2 = C.orBits("dR2", rSubd, rSrad);

    var       edR0 = C.enableWord(dR0, C.bits("df70", l_funct7, 0));            // Decode funct7 for dyadic operation
    var       edR2 = C.enableWord(dR2, C.bits("df72", l_funct7, 2));

    Bits        dR = C.enableWord("dR", funct7, edR0, edR2);                    // Choose the dyadic operation

    Bit       eq12 = C.compareEq        ("eq12",  rs1v, rs2v);
    Bit       lt12 = C.binaryTCCompareLt("lt12",  rs1v, rs2v);
    Bit      ltu12 = C.compareLt        ("ltu12", rs1v, rs2v);
    Bits       pci = C.binaryTCAdd("pci", pc, immB);                            // Pc plus sign extended immediate
    Bits       pc4 = C.binaryTCAdd("pc4", pc, InstructionWidthBytes);           // Pc plus instruction width

    Bits      rBeq = C.chooseFromTwoWords("rBeq",  pc4, pci,  eq12);            // Jump targets
    Bits      rBne = C.chooseFromTwoWords("rBne",  pci, pc4,  eq12);
    Bits      rBlt = C.chooseFromTwoWords("rBlt",  pc4, pci,  lt12);
    Bits      rBge = C.chooseFromTwoWords("rBge",  pci, pc4,  lt12);
    Bits     rBltu = C.chooseFromTwoWords("rBltu", pci, pc4, ltu12);
    Bits     rBgeu = C.chooseFromTwoWords("rBgeu", pc4, pci, ltu12);

    Bits      eBeq = C.enableWordIfEq("eBeq",  rBeq,  funct3, f3_beq);          // Enable result of branch operation
    Bits      eBne = C.enableWordIfEq("eBne",  rBne,  funct3, f3_bne);
    Bits      eBlt = C.enableWordIfEq("eBlt",  rBlt,  funct3, f3_blt);
    Bits      eBge = C.enableWordIfEq("eBge",  rBge,  funct3, f3_bge);
    Bits     eBltu = C.enableWordIfEq("eBltu", rBltu, funct3, f3_bltu);
    Bits     eBgeu = C.enableWordIfEq("eBgeu", rBgeu, funct3, f3_bgeu);
    Bits    branch = C.orBits("branch", eBeq, eBne, eBlt, eBge, eBltu, eBgeu);
    Bit  branchIns = C.compareEq("branchIns", opCode, D.opBranch);              // True if we are on a branch instruction
    Bits  brOrStep = C.chooseFromTwoWords("PC", pc4, branch, branchIns);        // Advance normally or jump by branch instruction immediate amount

    var      eid13 = C.enableWord(iR, C.bits("opCode13", l_opCode, opArithImm));// Decode funct7 for immediate operation
    var      eid33 = C.enableWord(dR, C.bits("opCode33", l_opCode, opArith   ));// Decode funct7 for dyadic operation

    Bits    result = C.enableWord("result", opCode, eid13, eid33);              // Choose between immediate or dyadic operation

    for (int i = 1; i < XLEN; i++)                                              // Values to reload back into registers
      C.chooseThenElseIfEQ("X"+i, result, x[i], rd, i);

    return C;                                                                   // A chip designed to code a Risc V 32I instruction
   }

//D0

  static void test_decode_RV32I ()                                              // Decode an immediate instruction
   {final Chip C = RV32I();

     , 0x00a00093
//  C.executionTrack(
//    "pc    pci   pc4   PC    start update  e   l",
//    "%s  %s  %s  %s  %s   %s     %s %s",
//     pc, pci, pc4, PC, start, update, C.collectBits("pc_e", XLEN), C.getGate("pc_l"));

//  C.executionTrack(
//    "S U pc                   pc4              pci",
//    "%s %s %s  %s  %s",
//     start, update, pc, pc4, pci);

    C.simulationSteps(68);
    C.simulate();
//  C.printExecutionTrace(); //stop();
    opCode.ok(D.opArithImm);
    funct3.ok(D.f3_add);
        rd.ok(   1);
       rs1.ok(   0);
 branchIns.ok(false);
      immB.ok(2048);
      immI.ok(  10);
      immJ.ok(   5);
      immS.ok(   1);
      immU.ok(2560);
        PC.ok(   8);
        pc.ok(   4);
       pci.ok(2052);
       pc4.ok(   8);
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
   {test_decode_RV32I();
   }

  static void newTests()                                                        // Tests being worked on
   {test_decode_RV32I();
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
