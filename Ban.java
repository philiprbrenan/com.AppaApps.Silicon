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

//D1 RV32I                                                                      // Decode and execute an RV32I instruction

  static class RV32I                                                            // Decode and execute an RV32I instruction
   {final String   out;                                                         // Name for this instruction processor
    final Pulse  start;                                                         // Start pulse has to be wide enough to load the registers
    final Pulse update;                                                         // Update registers pulse at end of decode/execute cycle.
    final Bits     one;                                                         // Constant one

    final Bits      x0;                                                         // Define registers and zero them at the start
    final Bits      PC;                                                         // Next value for program counter forward declaration
    final Register  pc;                                                         // Risc V registers

    final Bits     X[] = new Bits    [XLEN];                                    // New value for register
    final Register x[] = new Register[XLEN];                                    // Registers

    final Bits decode;                                                          // Instruction to decode and execute

    final Bits opCode;                                                          // Decode the instruction
    final Bits funct3;
    final Bits funct5;
    final Bits funct7;
    final Bits     rd;
    final Bits    rs1;
    final Bits    rs2;
                                                                                // Decode immediate field
    final Bits   immi;                                                          // Imipac: the weapon that defends itself.
    final Bits   immu;                                                          // Immediate from U format
    final Bits   immb;                                                          // Immediate operand from B format
    final Bits   immj;                                                          // Immediate operand from J format
    final Bits   imms;                                                          // Immediate operand from S format

    final Bits   immB;                                                          // Sign extend the immediate field
    final Bits   immI;
    final Bits   immJ;
    final Bits   immS;
    final Bits   immU;

    final EnableWord s1v;                                                       // Get value of s1 register
    final EnableWord s2v;
    final EnableWord s3v;
                  ;
    final EnableWord S1v;                                                       // Get values of s2 register
    final EnableWord S2v;
    final EnableWord S3v;
                  ;
    final Bits    f3_add;                                                       // Funct3 op codes
    final Bits    f3_xor;
    final Bits     f3_or;
    final Bits    f3_and;
    final Bits    f3_sll;
    final Bits    f3_srl;
    final Bits    f3_slt;
    final Bits   f3_sltu;
    final Bits    f3_beq;
    final Bits    f3_bne;
    final Bits    f3_blt;
    final Bits    f3_bge;
    final Bits   f3_bltu;
    final Bits   f3_bgeu;

    final Bits      rs1v;                                                       // The value of the selected s1 register
    final Bits      rs2v;                                                       // The value of the selected s2 register

    final Bits      addI;                                                       // Immediate operation
    final Bits      subI;
    final Bits      xorI;
    final Bits       orI;
    final Bits      andI;
    final Bits      sllI;
    final Bits      srlI;
    final Bits      sraI;
    final Bit       cmpI;
    final Bit      cmpuI;
    final Bits      sltI;
    final Bits     sltuI;

    final Bits     rAddi;                                                       // Enable result of immediate operation
    final Bits     rSubi;
    final Bits     rXori;
    final Bits      rOri;
    final Bits     rAndi;
    final Bits     rSlli;
    final Bits     rSrli;
    final Bits     rSrai;
    final Bits     rSlti;
    final Bits    rSltui;
    final Bits       iR0;

    final Bits       iR2;

    final EnableWord eiR0;                                                      // Decode funct7 for immediate opcode
    final EnableWord eiR2;

    final Bits        iR;                                                       // Choose the dyadic operation

    final Bits      addD;
    final Bits      subD;
    final Bits      xorD;
    final Bits       orD;
    final Bits      andD;
    final Bits      sllD;
    final Bits      srlD;
    final Bits      sraD;
    final Bit       cmpD;
    final Bit      cmpuD;
    final Bits      sltD;
    final Bits     sltuD;

    final Bits     rAddd;                                                       // Enable result of dyadic operation
    final Bits     rSubd;
    final Bits     rXord;
    final Bits      rOrd;
    final Bits     rAndd;
    final Bits     rSlld;
    final Bits     rSrld;
    final Bits     rSrad;
    final Bits     rSltd;
    final Bits    rSltud;
    final Bits       dR0;

    final Bits       dR2;

    final EnableWord edR0;                                                      // Decode funct7 for dyadic operation
    final EnableWord edR2;

    final Bits        dR;                                                       // Choose the dyadic operation

    final Bit       eq12;
    final Bit       lt12;
    final Bit      ltu12;
    final Bits       pci;                                                       // Pc plus sign extended immediate
    final Bits       pc4;                                                       // Pc plus instruction width

    final Bits      rBeq;                                                       // Jump targets
    final Bits      rBne;
    final Bits      rBlt;
    final Bits      rBge;
    final Bits     rBltu;
    final Bits     rBgeu;

    final Bits      eBeq;                                                       // Enable result of branch operation
    final Bits      eBne;
    final Bits      eBlt;
    final Bits      eBge;
    final Bits     eBltu;
    final Bits     eBgeu;
    final Bits    branch;
    final Bit  branchIns;                                                       // True if we are on a branch instruction
    final Bits  brOrStep;                                                       // Advance normally or jump by branch instruction immediate amount

    final EnableWord eid13;                                                     // Decode funct7 for immediate operation
    final EnableWord eid33;                                                     // Decode funct7 for dyadic operation

    final Bits    result;                                                       // Choose between immediate or dyadic operation

    String q(String n) {return Chip.n(out, n);}                                 // Prefix this block

    RV32I(Chip C, String Out, Bits Decode)                                      // Decode the specified bits as a RV32I instruction and execute it
     {   out = Out;                                                             // Name for this area of silicon and the prefix for the gates therein
      decode = Decode;                                                          // Instruction to decode
       start = C.pulse(q("start"),  0, 2);                                      // Start pulse has to be wide enough to load the registers
      update = C.pulse(q("update"), 0, 6, 34);                                  // Update registers pulse at end of decode/execute cycle.
         one = C.bits (q("one"),    XLEN, 1);                                   // Constant one

          x0 = C.bits    (q("X0"),  XLEN, 0);                                   // Define registers and zero them at the start
          PC = C.bits    (q("PC"),  XLEN);                                      // Next value for program counter forward declaration
          pc = C.register(q("pc"), C.regIn(x0, start), C.regIn(PC, update));    // Risc V registers

      for (int i = 1; i < XLEN; i++)                                            // Load registers. Initially from x0 later with instruction results
       {X[i] = C.bits    (q("X")+i, XLEN);
        x[i] = C.register(q("x")+i, C.regIn(x0, start), C.regIn(X[i], update));
       }

      opCode = C.subBitBus(q("opCode"), decode, p_opCode,     l_opCode);        // Decode the instruction
      funct3 = C.subBitBus(q("funct3"), decode, p_funct3,     l_funct3);
      funct5 = C.subBitBus(q("funct5"), decode, p_funct5,     l_funct5);
      funct7 = C.subBitBus(q("funct7"), decode, p_funct7,     l_funct7);
          rd = C.subBitBus(q("rd"),     decode, p_rd,         l_rd);
         rs1 = C.subBitBus(q("rs1"),    decode, p_rs1,        l_rs1);
         rs2 = C.subBitBus(q("rs2"),    decode, p_rs2,        l_rs2);
                                                                                // Decode immediate field
        immi = C.subBitBus(q("immi"),   decode, ip_immediate, il_immediate);    // Imipac: the weapon that defends itself.
        immu = C.subBitBus(q("immu"),   decode, up_immediate, ul_immediate);    // Immediate from U format
        immb = C.conCatBits(q("immb"),                                          // Immediate operand from B format
          x0.b(1),                                                              // First bit known to be 0.
          decode.b( 9), decode.b(10), decode.b(11), decode.b(12),               // Field offsets are one based: in riscv-spec-20191213.pdf they are zero based.
          decode.b(26), decode.b(27), decode.b(28), decode.b(29),
          decode.b(30), decode.b(31),
          decode.b( 8),
          decode.b(32));

        immj = C.conCatBits(q("immj"),                                          // Immediate operand from J format
          decode.b(22), decode.b(23), decode.b(24), decode.b(25), decode.b(26),
          decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31),
          decode.b(21),
          decode.b(13), decode.b(14), decode.b(15), decode.b(16), decode.b(17),
          decode.b(18), decode.b(19), decode.b(20),
          decode.b(32));

        imms = C.conCatBits(q("imms"),                                          // Immediate operand from S format
          decode.b( 8), decode.b( 9), decode.b(10), decode.b(11), decode.b(12),
          decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30),
          decode.b(31), decode.b(32));

        immB = C.binaryTCSignExtend(q("immB"), immb, XLEN);                     // Sign extend the immediate field
        immI = C.binaryTCSignExtend(q("immI"), immi, XLEN);
        immJ = C.binaryTCSignExtend(q("immJ"), immj, XLEN);
        immS = C.binaryTCSignExtend(q("immS"), imms, XLEN);
        immU = C.binaryTCSignExtend(q("immU"), immu, XLEN);

         s1v = C.enableWord(x[1], C.bits(q("x1v"), D.l_rs1, 1));                // Get value of s1 register
         s2v = C.enableWord(x[2], C.bits(q("x2v"), D.l_rs1, 2));
         s3v = C.enableWord(x[3], C.bits(q("x3v"), D.l_rs1, 3));

         S1v = C.enableWord(x[1], C.bits(q("X1v"), D.l_rs2, 1));                // Get values of s2 register
         S2v = C.enableWord(x[2], C.bits(q("X2v"), D.l_rs2, 2));
         S3v = C.enableWord(x[3], C.bits(q("X3v"), D.l_rs2, 3));

         f3_add = C.bits(q("f3_add"),  l_funct3, D.f3_add);                     // Funct3 op codes
         f3_xor = C.bits(q("f3_xor"),  l_funct3, D.f3_xor);
          f3_or = C.bits(q("f3_or"),   l_funct3, D.f3_or);
         f3_and = C.bits(q("f3_and"),  l_funct3, D.f3_and);
         f3_sll = C.bits(q("f3_sll"),  l_funct3, D.f3_sll);
         f3_srl = C.bits(q("f3_srl"),  l_funct3, D.f3_srl);
         f3_slt = C.bits(q("f3_slt"),  l_funct3, D.f3_slt);
        f3_sltu = C.bits(q("f3_sltu"), l_funct3, D.f3_sltu);
         f3_beq = C.bits(q("f3_beq"),  l_funct3, D.f3_beq);
         f3_bne = C.bits(q("f3_bne"),  l_funct3, D.f3_bne);
         f3_blt = C.bits(q("f3_blt"),  l_funct3, D.f3_blt);
         f3_bge = C.bits(q("f3_bge"),  l_funct3, D.f3_bge);
        f3_bltu = C.bits(q("f3_bltu"), l_funct3, D.f3_bltu);
        f3_bgeu = C.bits(q("f3_bgeu"), l_funct3, D.f3_bgeu);

           rs1v = C.enableWord          (q("rs1v"),  rs1,  s1v, s2v, s3v);      // The value of the selected s1 register
           rs2v = C.enableWord          (q("rs2v"),  rs2,  S1v, S2v, S3v);      // The value of the selected s2 register

           addI = C.binaryTCAdd         (q("addI"),  rs1v, immI);               // Immediate operation
           subI = C.binaryTCSubtract    (q("subI"),  rs1v, immI);
           xorI = C.xorBits             (q("xorI"),  rs1v, immI);
            orI = C.orBits              (q("orI"),   rs1v, immI);
           andI = C.andBits             (q("andI"),  rs1v, immI);
           sllI = C.shiftLeftMultiple   (q("sllI"),  rs1v, immI);
           srlI = C.shiftRightMultiple  (q("srlI"),  rs1v, immI);
           sraI = C.shiftRightArithmetic(q("sraI"),  rs1v, immI);
           cmpI = C.binaryTCCompareLt   (q("cmpI"),  rs1v, immI);
          cmpuI = C.compareLt           (q("cmpuI"), rs1v, immI);
           sltI = C.enableWord          (q("sltI"),  one,  cmpI);
          sltuI = C.enableWord          (q("sltuI"), one,  cmpuI);

          rAddi = C.enableWordIfEq(q("rAddi"),   addI, funct3, f3_add);         // Enable result of immediate operation
          rSubi = C.enableWordIfEq(q("rSubi"),   subI, funct3, f3_add);
          rXori = C.enableWordIfEq(q("rXori"),   xorI, funct3, f3_xor);
           rOri = C.enableWordIfEq(q("rOri"),     orI, funct3, f3_or);
          rAndi = C.enableWordIfEq(q("rAndi"),   andI, funct3, f3_and);
          rSlli = C.enableWordIfEq(q("rSlli"),   sllI, funct3, f3_sll);
          rSrli = C.enableWordIfEq(q("rSrli"),   srlI, funct3, f3_srl);
          rSrai = C.enableWordIfEq(q("rSrai"),   sraI, funct3, f3_srl);
          rSlti = C.enableWordIfEq(q("rSlti"),   sltI, funct3, f3_slt);
         rSltui = C.enableWordIfEq(q("rSltui"), sltuI, funct3, f3_sltu);
            iR0 = C.orBits(q("iR0"), rAddi, rXori, rOri,  rAndi,
                                  rSlli, rSrli, rSlti, rSltui);
            iR2 = C.orBits(q("iR2"), rSubi, rSrai);

           eiR0 = new EnableWord(iR0, C.bits(q("if70"), l_funct7, 0));          // Decode funct7 for immediate opcode
           eiR2 = new EnableWord(iR2, C.bits(q("if72"), l_funct7, 2));

             iR = C.enableWord(q("iR"), funct7, eiR0, eiR2);                    // Choose the dyadic operation

           addD = C.binaryTCAdd         (q("addD"),  rs1v, rs2v);
           subD = C.binaryTCSubtract    (q("subD"),  rs1v, rs2v);
           xorD = C.xorBits             (q("xorD"),  rs1v, rs2v);
            orD = C.orBits              (q("orD"),   rs1v, rs2v);
           andD = C.andBits             (q("andD"),  rs1v, rs2v);
           sllD = C.shiftLeftMultiple   (q("sllD"),  rs1v, rs2v);
           srlD = C.shiftRightMultiple  (q("srlD"),  rs1v, rs2v);
           sraD = C.shiftRightArithmetic(q("sraD"),  rs1v, rs2v);
           cmpD = C.binaryTCCompareLt   (q("cmpD"),  rs1v, rs2v);
          cmpuD = C.compareLt           (q("cmpuD"), rs1v, rs2v);
           sltD = C.enableWord          (q("sltD"),  one,  cmpD);
          sltuD = C.enableWord          (q("sltuD"), one,  cmpuD);

          rAddd = C.enableWordIfEq(q("rAddd"),   addD, funct3, f3_add);         // Enable result of dyadic operation
          rSubd = C.enableWordIfEq(q("rSubd"),   subD, funct3, f3_add);
          rXord = C.enableWordIfEq(q("rXord"),   xorD, funct3, f3_xor);
           rOrd = C.enableWordIfEq(q("rOrd"),     orD, funct3, f3_or);
          rAndd = C.enableWordIfEq(q("rAndd"),   andD, funct3, f3_and);
          rSlld = C.enableWordIfEq(q("rSlld"),   sllD, funct3, f3_sll);
          rSrld = C.enableWordIfEq(q("rSrld"),   srlD, funct3, f3_srl);
          rSrad = C.enableWordIfEq(q("rSrad"),   sraD, funct3, f3_srl);
          rSltd = C.enableWordIfEq(q("rSltd"),   sltD, funct3, f3_slt);
         rSltud = C.enableWordIfEq(q("rSltud"), sltuD, funct3, f3_sltu);
            dR0 = C.orBits(q("dR0"), rAddd, rXord, rOrd,  rAndd,
                                  rSlld, rSrld, rSltd, rSltud);
            dR2 = C.orBits(q("dR2"), rSubd, rSrad);

           edR0 = C.enableWord(dR0, C.bits(q("df70"), l_funct7, 0));            // Decode funct7 for dyadic operation
           edR2 = C.enableWord(dR2, C.bits(q("df72"), l_funct7, 2));

             dR = C.enableWord(q("dR"), funct7, edR0, edR2);                    // Choose the dyadic operation

           eq12 = C.compareEq        (q("eq12"),  rs1v, rs2v);
           lt12 = C.binaryTCCompareLt(q("lt12"),  rs1v, rs2v);
          ltu12 = C.compareLt        (q("ltu12"), rs1v, rs2v);
            pci = C.binaryTCAdd(q("pci"), pc, immB);                            // Pc plus sign extended immediate
            pc4 = C.binaryTCAdd(q("pc4"), pc, InstructionWidthBytes);           // Pc plus instruction width

           rBeq = C.chooseFromTwoWords(q("rBeq"),  pc4, pci,  eq12);            // Jump targets
           rBne = C.chooseFromTwoWords(q("rBne"),  pci, pc4,  eq12);
           rBlt = C.chooseFromTwoWords(q("rBlt"),  pc4, pci,  lt12);
           rBge = C.chooseFromTwoWords(q("rBge"),  pci, pc4,  lt12);
          rBltu = C.chooseFromTwoWords(q("rBltu"), pci, pc4, ltu12);
          rBgeu = C.chooseFromTwoWords(q("rBgeu"), pc4, pci, ltu12);

           eBeq = C.enableWordIfEq(q("eBeq"),  rBeq,  funct3, f3_beq);          // Enable result of branch operation
           eBne = C.enableWordIfEq(q("eBne"),  rBne,  funct3, f3_bne);
           eBlt = C.enableWordIfEq(q("eBlt"),  rBlt,  funct3, f3_blt);
           eBge = C.enableWordIfEq(q("eBge"),  rBge,  funct3, f3_bge);
          eBltu = C.enableWordIfEq(q("eBltu"), rBltu, funct3, f3_bltu);
          eBgeu = C.enableWordIfEq(q("eBgeu"), rBgeu, funct3, f3_bgeu);
         branch = C.orBits(q("branch"), eBeq, eBne, eBlt, eBge, eBltu, eBgeu);
      branchIns = C.compareEq(q("branchIns"), opCode, D.opBranch);              // True if we are on a branch instruction
       brOrStep = C.chooseFromTwoWords(q("PC"), pc4, branch, branchIns);        // Advance normally or jump by branch instruction immediate amount

          eid13 = C.enableWord(iR, C.bits(q("opCode13"), l_opCode, opArithImm));// Decode funct7 for immediate operation
          eid33 = C.enableWord(dR, C.bits(q("opCode33"), l_opCode, opArith   ));// Decode funct7 for dyadic operation

         result = C.enableWord(q("result"), opCode, eid13, eid33);              // Choose between immediate or dyadic operation

      for (int i = 1; i < XLEN; i++)                                            // Values to reload back into registers
        C.chooseThenElseIfEQ(q("X")+i, result, x[i], rd, i);
     }
   }

//D0

  static void test_decode_RV32I ()                                              // Decode an immediate instruction
   {final Chip  C = new Chip();
    final Bits  decode = C.bits("decode", XLEN, 0xa00093);                      // Instruction to decode and execute
    final RV32I R = new RV32I(C, "a", decode);

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
    R.   opCode.ok(D.opArithImm);
    R.   funct3.ok(D.f3_add);
    R.       rd.ok(   1);
    R.      rs1.ok(   0);
    R.branchIns.ok(false);
    R.     immB.ok(2048);
    R.     immI.ok(  10);
    R.     immJ.ok(   5);
    R.     immS.ok(   1);
    R.     immU.ok(2560);
    R.       PC.ok(   8);
    R.       pc.ok(   4);
    R.      pci.ok(2052);
    R.      pc4.ok(   8);
    R.     addI.ok(  10);
    R.      orI.ok(  10);
    R.    rAddi.ok(  10);
    R.    rXori.ok(   0);
    R.     rOri.ok(   0);
    R.    rAndi.ok(   0);
    R.   rSltui.ok(   0);
    R.     x[1].ok(  10);
    R.     x[2].ok(   0);
    R.     x[3].ok(   0);
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
