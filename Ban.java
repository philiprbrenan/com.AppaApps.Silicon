//------------------------------------------------------------------------------
// RiscV 32I and Btree on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
/*
Io sono docile,    - son rispettosa,
sono ubbediente,   - dolce, amorosa;
mi lascio reggere, - mi fo guidar.
Ma se mi toccano   - dov'Ã¨ il mio debole,
sarÃ² una vipera    - e cento trappole
prima di cedere    - farÃ² giocar.
*/

package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban extends Chip                                             // Create a chip that contains a Risc V processor extended with Btree instructions
 {final static int XLEN                  = RiscV.XLEN;                          // Number of bits in a register
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

//D1 RV32I                                                                      // Risc V RV32I CPU.

  static class RV32I                                                            // Decode and execute an RV32I instruction
   {final String   out;                                                         // Name for this instruction processor
    final Bits    zero;                                                         // Constant zero
    final Bits     one;                                                         // Constant one

    final Bits      PC;                                                         // Next value for program counter forward declaration
    final Bits      pc;                                                         // Current program counter

    final Bits       x[];                                                       // Risc V registers
    final Bits       X[] = new Bits[XLEN];                                      // New value for registers

    final Bits  decode;                                                         // Instruction to decode and execute

    final Bits  opCode;                                                         // Opcode from instruction
    final Bits  funct3, funct5, funct7;                                         // Function codes
    final Bits      rd, rs1, rs2;                                               // Register numbers

    final Bits    immi, immu, immb, immj, imms;                                 // Imipac: the weapon that defends itself.
    final Bits    immB, immI, immJ, immS, immU, immB2, immI2, immJ2, immU2;     // Sign extend the immediate field

    final Eq[]      sv;                                                         // Get value of s1, s2 registers
    final Bits    rs1v, rs2v;                                                   // The value of the selected s1, s2 registers

    final Bits  f3_add,  f3_xor,  f3_or,  f3_and, f3_sll, f3_srl,               // Funct3 op codes
                f3_slt,  f3_sltu, f3_beq, f3_bne, f3_blt, f3_bge,
                f3_bltu, f3_bgeu;

    final Bits    addI, subI, xorI, orI, andI, sllI, srlI, sraI, sltI, sltuI;   // Immediate operation
    final Bit     cmpI, cmpuI;

    final Bits   rAddi, rSubi, rXori, rOri, rAndi, rSlli, rSrli, rSrai,         // Enable result of immediate operation
                 rSlti, rSltui;

    final Bits     iR0, iR2, iR;                                                // Choose the immediate operation
    final Eq      eiR0, eiR2;                                                   // Decode funct7 for immediate opcode

    final Bits    addD, subD, xorD, orD, andD, sllD, srlD, sraD, sltD, sltuD;   // Dyadic operation

    final Bit     cmpD, cmpuD;
    final Bits   rAddd, rSubd, rXord, rOrd, rAndd, rSlld, rSrld, rSrad,         // Enable result of dyadic operation
                 rSltd, rSltud;

    final Bits     dR0, dR2, dR;                                                // Choose the dyadic operation
    final Eq      edR0, edR2;                                                   // Decode funct7 for dyadic operation

    final Bit     eq12, lt12, ltu12;                                            // Comparison operation
    final Bits     pcb, pci, pcj;                                               // Pc plus sign extended immediate
    final Bits     pc4;                                                         // Pc plus instruction width
    final Bits pcImmU2;                                                         // Pc plus U format immediate
    final Bits    rBeq, rBne, rBlt, rBge, rBltu, rBgeu;                         // Jump targets
    final Bits    eBeq, eBne, eBlt, eBge, eBltu, eBgeu;                         // Enable result of branch operation
    final Bits  branch;
    final Eq  opBranch, opJal, opJalr;

    final Eq     eid13, eid33, eJal, eJalr, eLui, eAuipc;                       // Type of instruction

    final Bits mAL, mAS;                                                        // Memory load address, store address
    final Eq   eqLoad, eqStore;                                                 // Load requested, store requested

    final Bits    result;                                                       // Choose between immediate or dyadic operation

//D2 Memory                                                                     // Make a request to memory or receive data from memory.

   class MemoryControl                                                          // Interface between CPU and memory
     {Bit  loadRequested  = null;                                               // Load requested
      Bit  storeRequested = null;                                               // Store requested
      Bits type           = null;                                               // Type of load or store requested
      Bits address        = null;                                               // Address to load from or load to
      Bits register       = null;                                               // Register to be loaded or stored
      void anneal()                                                             // Anneal memory to avoid error messages during testing
       {loadRequested .anneal();                                                // Load requested
        storeRequested.anneal();                                                // Store requested
        type          .anneal();                                                // Type of load or store requested
        address       .anneal();                                                // Address to load from or load to
        register      .anneal();                                                // Register to be loaded or stored
       }
      public String toString()                                                  // Memory request represented as a string
       {final StringBuilder b = new StringBuilder();
        say(b, " loadRequested :", loadRequested);
        say(b,  "storeRequested:", storeRequested);
        say(b,  "          type:", type);
        say(b,  "       address:", address);
        say(b,  "      register:", register);
        return b.toString();
       }
     }

    final MemoryControl m = new MemoryControl();                                // Interface between cpu and memory

    String q(String n) {return Chip.n(out, n);}                                 // Prefix this block

    public String toString()                                                    // Show the differences between the input and output registers
     {final StringBuilder b = new StringBuilder();
      if (pc.Int() != PC.Int()) b.append("pc="+PC.Int()+" ");
      for (int i = 1; i < XLEN; i++)
        if (x[i].Int() != X[i].Int()) b.append("x"+i+"="+X[i].Int()+" ");
      return b.toString().substring(0, b.length()-1);
     }

    void ok(String expected)                                                    // Compare expected cpu state with actual
     {Chip.ok(toString(), expected);
     }

    void om(String expected)                                                    // Compare expected memory control request with actual
     {Chip.ok(m.toString(), expected);
     }

    RV32I(Chip C, String Out, Bits Decode, Bits pc, Bits[]x)                    // Decode the specified bits as a RV32I instruction and execute it
     {out     = Out;                                                            // Name for this area of silicon and the prefix for the gates therein
      decode  = Decode;                                                         // Instruction to decode
      this.pc = pc;                                                             // Program counter at start
      this.x  = x;                                                              // Registers at start
      one     = C.bits (q("one"),    XLEN, 1);                                  // Constant one
      zero    = C.bits (q("zero"),   XLEN, 0);                                  // Constant zero

      opCode  = C.subBitBus(q("opCode"), decode, p_opCode,     l_opCode);       // Decode the instruction converting the offsets from zero based to one based
      funct3  = C.subBitBus(q("funct3"), decode, p_funct3,     l_funct3);
      funct5  = C.subBitBus(q("funct5"), decode, p_funct5,     l_funct5);
      funct7  = C.subBitBus(q("funct7"), decode, p_funct7,     l_funct7);
      rd      = C.subBitBus(q("rd"),     decode, p_rd,         l_rd);
      rs1     = C.subBitBus(q("rs1"),    decode, p_rs1,        l_rs1);
      rs2     = C.subBitBus(q("rs2"),    decode, p_rs2,        l_rs2);
                                                                                // Decode immediate field
      immi    = C.subBitBus(q("immi"),   decode, ip_immediate, il_immediate);   // Imipac: the weapon that defends itself.
      immu    = C.subBitBus(q("immu"),   decode, up_immediate, ul_immediate);   // Immediate from U format
      immb    = C.conCatBits(q("immb"),                                         // Immediate operand from B format
          zero.b(1),                                                            // First bit known to be 0.
          decode.b( 9), decode.b(10), decode.b(11), decode.b(12),               // Field offsets are one based: in riscv-spec-20191213.pdf they are zero based.
          decode.b(26), decode.b(27), decode.b(28), decode.b(29),
          decode.b(30), decode.b(31),
          decode.b( 8),
          decode.b(32));

      immj    = C.conCatBits(q("immj"),                                         // Immediate operand from J format
          decode.b(22), decode.b(23), decode.b(24), decode.b(25), decode.b(26),
          decode.b(27), decode.b(28), decode.b(29), decode.b(30), decode.b(31),
          decode.b(21),
          decode.b(13), decode.b(14), decode.b(15), decode.b(16), decode.b(17),
          decode.b(18), decode.b(19), decode.b(20),
          decode.b(32));

      imms    = C.conCatBits(q("imms"),                                         // Immediate operand from S format
          decode.b( 8), decode.b( 9), decode.b(10), decode.b(11), decode.b(12),
          decode.b(26), decode.b(27), decode.b(28), decode.b(29), decode.b(30),
          decode.b(31), decode.b(32));

      immB    = C.binaryTCSignExtend(q("immB"),  immb, XLEN);                   // Sign extend the immediate field
      immI    = C.binaryTCSignExtend(q("immI"),  immi, XLEN);
      immJ    = C.binaryTCSignExtend(q("immJ"),  immj, XLEN);
      immS    = C.binaryTCSignExtend(q("immS"),  imms, XLEN);
      immU    = C.binaryTCSignExtend(q("immU"),  immu, XLEN);
      immB2   = C.shiftUp           (q("immB2"), immB);                         // Multiply by two to get the branch offset in bytes
      immI2   = C.shiftUp           (q("immI2"), immI);
      immJ2   = C.shiftUp           (q("immJ2"), immJ);
      immU2   = C.shiftLeftConstant (q("immU2"), immu, D.U.p_immediate);        // Place the 20 bits provided in the immediate operand in the high bits of the target register

      sv      = new Eq[XLEN];                                                   // Get values of s1 and s2
      sv[0]   = C.eq(C.bits(q("xv0"), D.l_rs1, 0), zero);                       // Get value of s1 register

      for (int i = 1; i < XLEN; i++)
       {sv[i] = C.eq(C.bits(q("xv")+i, D.l_rs1, i), x[i]);                      // Get value of s1 register
       }

      rs1v    = C.chooseEq(q("rs1v"),  rs1,  sv);                               // The value of the selected s1 register
      rs2v    = C.chooseEq(q("rs2v"),  rs2,  sv);                               // The value of the selected s2 register

      f3_add  = C.bits(q("f3_add"),  l_funct3, D.f3_add);                       // Funct3 op codes
      f3_xor  = C.bits(q("f3_xor"),  l_funct3, D.f3_xor);
      f3_or   = C.bits(q("f3_or"),   l_funct3, D.f3_or);
      f3_and  = C.bits(q("f3_and"),  l_funct3, D.f3_and);
      f3_sll  = C.bits(q("f3_sll"),  l_funct3, D.f3_sll);
      f3_srl  = C.bits(q("f3_srl"),  l_funct3, D.f3_srl);
      f3_slt  = C.bits(q("f3_slt"),  l_funct3, D.f3_slt);
      f3_sltu = C.bits(q("f3_sltu"), l_funct3, D.f3_sltu);

      f3_beq  = C.bits(q("f3_beq"),  l_funct3, D.f3_beq);                       // Decode a branch instruction
      f3_bne  = C.bits(q("f3_bne"),  l_funct3, D.f3_bne);
      f3_blt  = C.bits(q("f3_blt"),  l_funct3, D.f3_blt);
      f3_bge  = C.bits(q("f3_bge"),  l_funct3, D.f3_bge);
      f3_bltu = C.bits(q("f3_bltu"), l_funct3, D.f3_bltu);
      f3_bgeu = C.bits(q("f3_bgeu"), l_funct3, D.f3_bgeu);

      addI    = C.binaryTCAdd         (q("addI"),  rs1v, immI);                 // Immediate operation
      subI    = C.binaryTCSubtract    (q("subI"),  rs1v, immI);
      xorI    = C.xorBits             (q("xorI"),  rs1v, immI);
      orI     = C.orBits              (q("orI"),   rs1v, immI);
      andI    = C.andBits             (q("andI"),  rs1v, immI);
      sllI    = C.shiftLeftMultiple   (q("sllI"),  rs1v, immI);
      srlI    = C.shiftRightMultiple  (q("srlI"),  rs1v, immI);
      sraI    = C.shiftRightArithmetic(q("sraI"),  rs1v, immI);
      cmpI    = C.binaryTCCompareLt   (q("cmpI"),  rs1v, immI);
      cmpuI   = C.compareLt           (q("cmpuI"), rs1v, immI);
      sltI    = C.enableWord          (q("sltI"),  one,  cmpI);
      sltuI   = C.enableWord          (q("sltuI"), one,  cmpuI);

      rAddi   = C.enableWordIfEq(q("rAddi"),   addI, funct3, f3_add);           // Enable result of immediate operation
      rSubi   = C.enableWordIfEq(q("rSubi"),   subI, funct3, f3_add);
      rXori   = C.enableWordIfEq(q("rXori"),   xorI, funct3, f3_xor);
      rOri    = C.enableWordIfEq(q("rOri"),     orI, funct3, f3_or);
      rAndi   = C.enableWordIfEq(q("rAndi"),   andI, funct3, f3_and);
      rSlli   = C.enableWordIfEq(q("rSlli"),   sllI, funct3, f3_sll);
      rSrli   = C.enableWordIfEq(q("rSrli"),   srlI, funct3, f3_srl);
      rSrai   = C.enableWordIfEq(q("rSrai"),   sraI, funct3, f3_srl);
      rSlti   = C.enableWordIfEq(q("rSlti"),   sltI, funct3, f3_slt);
      rSltui  = C.enableWordIfEq(q("rSltui"), sltuI, funct3, f3_sltu);
      iR0     = C.orBits(q("iR0"), rAddi, rXori, rOri,  rAndi,
                                   rSlli, rSrli, rSlti, rSltui);
      iR2     = C.orBits(q("iR2"), rSubi, rSrai);

      eiR0    = C.eq(C.bits(q("if70"), l_funct7, 0), iR0);                      // Decode funct7 for immediate opcode
      eiR2    = C.eq(C.bits(q("if72"), l_funct7, 2), iR2);

      iR      = C.chooseEq(q("iR"), funct7, eiR0, eiR2);                        // Choose the dyadic operation

      addD    = C.binaryTCAdd         (q("addD"),  rs1v, rs2v);
      subD    = C.binaryTCSubtract    (q("subD"),  rs1v, rs2v);
      xorD    = C.xorBits             (q("xorD"),  rs1v, rs2v);
      orD     = C.orBits              (q("orD"),   rs1v, rs2v);
      andD    = C.andBits             (q("andD"),  rs1v, rs2v);
      sllD    = C.shiftLeftMultiple   (q("sllD"),  rs1v, rs2v);
      srlD    = C.shiftRightMultiple  (q("srlD"),  rs1v, rs2v);
      sraD    = C.shiftRightArithmetic(q("sraD"),  rs1v, rs2v);
      cmpD    = C.binaryTCCompareLt   (q("cmpD"),  rs1v, rs2v);
      cmpuD   = C.compareLt           (q("cmpuD"), rs1v, rs2v);
      sltD    = C.enableWord          (q("sltD"),  one,  cmpD);
      sltuD   = C.enableWord          (q("sltuD"), one,  cmpuD);

      rAddd   = C.enableWordIfEq(q("rAddd"),   addD, funct3, f3_add);           // Enable result of dyadic operation
      rSubd   = C.enableWordIfEq(q("rSubd"),   subD, funct3, f3_add);
      rXord   = C.enableWordIfEq(q("rXord"),   xorD, funct3, f3_xor);
      rOrd    = C.enableWordIfEq(q("rOrd"),     orD, funct3, f3_or);
      rAndd   = C.enableWordIfEq(q("rAndd"),   andD, funct3, f3_and);
      rSlld   = C.enableWordIfEq(q("rSlld"),   sllD, funct3, f3_sll);
      rSrld   = C.enableWordIfEq(q("rSrld"),   srlD, funct3, f3_srl);
      rSrad   = C.enableWordIfEq(q("rSrad"),   sraD, funct3, f3_srl);
      rSltd   = C.enableWordIfEq(q("rSltd"),   sltD, funct3, f3_slt);
      rSltud  = C.enableWordIfEq(q("rSltud"), sltuD, funct3, f3_sltu);
      dR0     = C.orBits(q("dR0"), rAddd, rXord, rOrd,  rAndd,
                                   rSlld, rSrld, rSltd, rSltud);
      dR2     = C.orBits(q("dR2"), rSubd, rSrad);

      edR0    = C.eq(C.bits(q("df70"), l_funct7, 0), dR0);                      // Decode funct7 for dyadic operation
      edR2    = C.eq(C.bits(q("df72"), l_funct7, 2), dR2);

      dR      = C.chooseEq (q("dR"), funct7, edR0, edR2);                       // Choose the dyadic operation

      eq12    = C.compareEq        (q("eq12"),  rs1v, rs2v);                    // Compare source1 with immediate
      lt12    = C.binaryTCCompareLt(q("lt12"),  rs1v, rs2v);
      ltu12   = C.compareLt        (q("ltu12"), rs1v, rs2v);
      pcb     = C.binaryTCAdd(q("pcb"),   pc, immB2);                           // Pc plus sign extended immediate
      pci     = C.binaryTCAdd(q("pci"), rs1v, immI2);                           // Jalr
      pcj     = C.binaryTCAdd(q("pcj"),   pc, immJ2);                           // Jal
      pc4     = C.binaryTCAdd(q("pc4"),   pc, RiscV.instructionBytes);          // Pc plus instruction width. Note should really determine if this is a 2 byte compressed instruction or not.

      rBeq    = C.chooseFromTwoWords(q("rBeq"),  pc4, pcb,  eq12);              // Jump targets
      rBne    = C.chooseFromTwoWords(q("rBne"),  pcb, pc4,  eq12);
      rBlt    = C.chooseFromTwoWords(q("rBlt"),  pc4, pcb,  lt12);
      rBge    = C.chooseFromTwoWords(q("rBge"),  pcb, pc4,  lt12);
      rBltu   = C.chooseFromTwoWords(q("rBltu"), pcb, pc4, ltu12);
      rBgeu   = C.chooseFromTwoWords(q("rBgeu"), pc4, pcb, ltu12);

      eBeq    = C.enableWordIfEq(q("eBeq"),  rBeq,  funct3, f3_beq);            // Enable result of branch operation
      eBne    = C.enableWordIfEq(q("eBne"),  rBne,  funct3, f3_bne);
      eBlt    = C.enableWordIfEq(q("eBlt"),  rBlt,  funct3, f3_blt);
      eBge    = C.enableWordIfEq(q("eBge"),  rBge,  funct3, f3_bge);
      eBltu   = C.enableWordIfEq(q("eBltu"), rBltu, funct3, f3_bltu);
      eBgeu   = C.enableWordIfEq(q("eBgeu"), rBgeu, funct3, f3_bgeu);
      branch  = C.orBits(q("branch"), eBeq, eBne, eBlt, eBge, eBltu, eBgeu);    // Next instruction as a result of branching

      opBranch= C.eq(C.bits(q("opBranch"), D.l_opCode, D.opBranch), branch);    // Branch
      opJal   = C.eq(C.bits(q("opJal"),    D.l_opCode, D.opJal   ), pcj);       // jal
      opJalr  = C.eq(C.bits(q("opJalr"),   D.l_opCode, D.opJalr  ), pci);       // jalr

      PC      = C.chooseEq(q("PC"), opCode, pc4, opBranch, opJal, opJalr);      // Advance normally by default, otherwise depending on a branch or as requested by a jump
      pcImmU2 = C.binaryTCAdd(q("pcImmU2"), pc, immU2);                         // Auipc

      eid13   = C.eq(C.bits(q("opCode13"),   l_opCode, opArithImm), iR     );   // Was it an arithmetic with immediate instruction?
      eid33   = C.eq(C.bits(q("opCode33"),   l_opCode, opArith   ), dR     );   // Was it an arithmetic with two source registers?
      eJal    = C.eq(C.bits(q("opCodeJal"),  l_opCode, D.opJal   ), pc4    );   // Jal
      eJalr   = C.eq(C.bits(q("opCodeJalr"), l_opCode, D.opJalr  ), pc4    );   // Jalr
      eLui    = C.eq(C.bits(q("opLui"),      l_opCode, D.opLui   ), immU2  );   // Lui
      eAuipc  = C.eq(C.bits(q("opAuipc"),    l_opCode, D.opAuiPc ), pcImmU2);   // Auipc

      result  = C.chooseEq(q("result"), opCode, eid13, eid33, eJal, eJalr,      // Choose operation to load target register
        eLui, eAuipc);

      m.loadRequested  = C.compareEq(q("memLoad"),  opCode, D.opLoad);          // Load from memory requested
      m.storeRequested = C.compareEq(q("memStore"), opCode, D.opStore);         // Store into memory requested
      m.type           = funct3;

      mAL              = C.binaryTCAdd(q("memAddressLoad"),  rs1v, immI);       // Load requested
      mAS              = C.binaryTCAdd(q("memAddressStore"), rs1v, immS);       // Store requested

      eqLoad           = C.eq(C.bits(q("opLoad"),  D.l_opCode, D.opLoad),  mAL);// Load address
      eqStore          = C.eq(C.bits(q("opStore"), D.l_opCode, D.opStore), mAS);// Store address
      m.address        = C.chooseEq(q("memAddress"), opCode, eqLoad, eqStore);  // Save address
      m.register       = rd;                                                    // Register to load or store

      X[0] = null;                                                              // X0
      for (int i = 1; i < XLEN; i++)                                            // Values to reload back into registers
        X[i]  = C.chooseThenElseIfEQ(q("X")+i, result, x[i], rd, i);            // Either the passed in register value or the newly computed one
     }
   }

  static RV32I rv32i(Chip chip, String out, Bits decode, Bits pc, Bits[]x)      // Decode the specified bits as a RV32I instruction and execute it
   {return new RV32I(chip, out, decode, pc, x);
   }

//D0

  static RV32I test_instruction(Integer instruction)                            // Test an instruction
   {final Chip          C = new Chip();
    final Bits         pc = C.bits("pc",  XLEN, 4);                             // Initialize pc
    final Bits []       x = new Bits[XLEN];
    for (int i = 1; i < XLEN; i++) x[i] = C.bits("x"+i, XLEN, i);               // Initialize registers

    final Bits     decode = C.bits("decode", XLEN, instruction);                // Instruction to decode and execute
    final RV32I         R = rv32i(C, "a", decode, pc, x);                       // Decode and execute the instruction
    for (int i = 1; i < XLEN; i++) R.X[i].anneal();                             // Anneal the outputs
    R.PC.anneal(); R.m.anneal();
    C.maxSimulationSteps(300);
    C.simulate();
    return R;
   }

  static void test_decode_addi()                                                // Addi
   {final RV32I R = test_instruction(0xa00093);

    R.ok("pc=8 x1=10");
    R.   opCode.ok(D.opArithImm);
    R.   funct3.ok(D.f3_add);
    R.       rd.ok(   1);
    R.      rs1.ok(   0);
    R.     immB.ok(2048);
    R.     immI.ok(  10);
    R.     immJ.ok(   5);
    R.     immS.ok(   1);
    R.     immU.ok(2560);
    R.       PC.ok(   8);
    R.       pc.ok(   4);
    R.      pcb.ok(4100);
    R.      pc4.ok(   8);
    R.     addI.ok(  10);
    R.      orI.ok(  10);
    R.    rAddi.ok(  10);
    R.    rXori.ok(   0);
    R.     rOri.ok(   0);
    R.    rAndi.ok(   0);
    R.   rSltui.ok(   0);
    R.     x[1].ok(   1);
    R.     x[2].ok(   2);
    R.     x[3].ok(   3);
    R.     X[1].ok(  10);
    R.     X[2].ok(   2);
    R.     X[3].ok(   3);
    R.   result.ok(  10);
   }

  static void test_decode_add1 () {test_instruction(0x310233).ok("pc=8 x4=5");}
  static void test_decode_add2 () {test_instruction(0x0201b3).ok("pc=8 x3=4");}
  static void test_decode_slt1 () {test_instruction(0x20afb3).ok("pc=8 x31=1");}
  static void test_decode_slt2 () {test_instruction(0x112f33).ok("pc=8 x30=0");}
  static void test_decode_jal  () {test_instruction(0x80016f).ok("pc=12 x2=8");}
// At start pc=4, x0=0, x2=2. Instruction decode: rd=2 rs1=0 imm=4. Should result in x2=0+4<<1 = 8 and pc=4+4 = 8
  static void test_decode_jalr () {test_instruction(0x400167).ok("pc=8 x2=8");}
  static void test_decode_lui  () {test_instruction(0x10b7)  .ok("pc=8 x1=4096");}
  static void test_decode_auipc() {test_instruction(0x4097)  .ok("pc=8 x1=16388");}

  static void test_decode_sb()
   {RV32I r = test_instruction(0x1000a3);
    r.om("""
 loadRequested : 0
 storeRequested: 1
           type: 000
        address: 0x1
       register: 00001
""");
   }

  static void test_decode_lh()
   {RV32I r = test_instruction(0x1103);
    r.om("""
 loadRequested : 1
 storeRequested: 0
           type: 001
        address: 0x0
       register: 00010
""");
   }

  static void test_fibonacci()                                                  // Test Risc V cpu by producing some Fibonacci numbers
   {final int       N = 300;  // 200
    final Chip      c = new Chip();
    final Pulse    xi = c.pulse("xi").period(N).on(N/2).start(1).b();           // Execute an instruction
    final Register pc = c.new Register("pc", XLEN, xi, 0);                      // Initialize pc
    final Register[]x = new Register[XLEN];
    final Bits   []xb = new Bits    [XLEN];

    for (int i = 1; i < XLEN; i++)  x[i] = c.new Register("x"+i, XLEN, xi, 0);  // Initialize registers
    for (int i = 1; i < XLEN; i++) xb[i] = x[i].anneal();

    long[]Code = {0xa00093, 0x293,   0x113,   0x100193, 0x228a23,               // Code for Fibonacci numbers in Risc V machine code
                  0x310233, 0x18133, 0x201b3, 0x128293, 0xfe12cbe3};

    Bits code = c.bits("code", XLEN, Code);

    Bits  pc4 = c.shiftRightArithmetic("pc4", pc, 2);                           // Address instruction in blocks of XLEN bits
    Bits  instruction = c.readMemory("instruction", code, pc4, XLEN);           // Latest instruction

    RV32I cpu = rv32i(c, "cpu", instruction, pc, xb);                           // Execute instruction

    for (int i = 1; i < XLEN; i++) x[i].load(cpu.X[i]);                         // Initialize registers

    c.continueBits(pc.load, cpu.PC);                                            // Update
//  OutputUnit oInstruction = c.new OutputUnit("oInstruction", instruction, xi);
    OutputUnit aVariable    = c.new OutputUnit("aVariable",    cpu.X[2],    xi) // Extract Fibonacci numbers as they are formed. Paul Halmos: Naive Set Theory, page 45: the object defined has some irrelevant structure, which seems to get in the way (but is in fact harmless).
     {void action()
       {final Integer p = pc.Int(), f = cpu.X[2].Int();                         // Program counter and variable 'a'
        if (p != null && p == 16) log.push(f);                                  // Extract latest fibonacci number and write it to the output channel
       }
     };

//  c.simulationSteps(4000);                                                    // Simulation
    c.simulationSteps(70*N);                                                    // Simulation
//  c.executionTrace("pc   instruction", "%8s   %8s   %8s", pc4, instruction, cpu.X[2]);

    instruction.anneal(); cpu.m.anneal();
    for (int i = 1; i < XLEN; i++) cpu.X[i].anneal();

//  c.executionTrace = c.new Trace("p    r", true)
//   {String trace()
//     {return String.format("%s    %s", cpu.pc, cpu.X[2]);
//     }
//   };

    c.simulate();

    say(aVariable.decimal());
    aVariable.ok(0, 1, 1, 2, 3, 5, 8, 13, 21, 34);
//  say(oInstruction);
//  oInstruction.ok(0xa00093, 0x293, 0x113, 0x100193, 0x228a23, 0x310233, 0x18133, 0x201b3, 0x128293, 0xfe12cbe3, 0x0);
    c.printExecutionTrace();
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_decode_addi();
    test_decode_add1();
    test_decode_slt1();
    test_decode_slt2();
    test_decode_jal();
    test_decode_jalr();
    test_decode_auipc();
    test_decode_sb();
    test_decode_lh();
    test_fibonacci();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_fibonacci();
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
