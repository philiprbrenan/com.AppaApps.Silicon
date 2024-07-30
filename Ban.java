//------------------------------------------------------------------------------
// RiscV 32I Cpu on a silicon chip
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.
/*
Io sono docile,    - son rispettosa,
sono ubbediente,   - dolce, amorosa;
mi lascio reggere, - mi fo guidar.
Ma se mi toccano   - dov'e il mio debole,
saro una vipera    - e cento trappole
prima di cedere    - faro giocar.
*/
import java.util.Stack;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class Ban extends Chip                                             // Create a chip that contains a Risc V processor extended with Btree instructions
 {final static int XLEN = RiscV.XLEN;                                           // Number of bits in a register

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
    up_immediate = 1 + D.U.p_immediate, ul_immediate = D.U.l_immediate;

//D1 RV32I Cpu                                                                  // Risc V RV32I CPU.

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
    final Bit     immI7;                                                        // The bit in the immediate value that differentiates between the possible types of right shift

    final Eq[]      sv;                                                         // Get value of s1, s2 registers
    final Bits    rs1v, rs2v;                                                   // The value of the selected s1, s2 registers

    final Bits  f3_add,  f3_xor,  f3_or,  f3_and, f3_sll, f3_srl,               // Funct3 op codes
                f3_slt,  f3_sltu, f3_beq, f3_bne, f3_blt, f3_bge,
                f3_bltu, f3_bgeu;

    final Bits    addI, xorI, orI, andI, sllI, srlI, sraI, sltI, sltuI;         // Immediate operation
    final Bit     cmpI, cmpuI;

    final Bits   rAddi, rXori, rOri, rAndi, rSlli, rSrli, rSrai, rSral,         // Enable result of immediate operation
                 rSlti, rSltui;

    final Bits    iR;                                                           // Choose the immediate operation

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
    final Bit  modifyRd;                                                        // This instruction modifies the target register

    final Bits   result;                                                        // Choose between immediate or dyadic operation

//D2 Memory                                                                     // Make a request to memory or receive data from memory.

   class MemoryControl                                                          // Interface between CPU and memory
     {Bit  loadRequested;                                                       // Load requested
      Bit  storeRequested;                                                      // Store requested
      Bits type;                                                                // Type of load or store requested
      Bits address;                                                             // Address to load from or load to
      Bits sourceRegister;                                                      // Store content of rs2 into memory indexed by rd1 and immediate
      Bits targetRegister;                                                      // Load register rd from memory indexed by rs1 and immediate

      void anneal()                                                             // Anneal memory to avoid error messages during testing
       {loadRequested .anneal();                                                // Load requested
        storeRequested.anneal();                                                // Store requested
        type          .anneal();                                                // Type of load or store requested
        address       .anneal();                                                // Address to load from or load to
        sourceRegister.anneal();                                                // Register to be stored
        targetRegister.anneal();                                                // Register to be loaded
       }
      public String toString()                                                  // Memory request represented as a string
       {final StringBuilder b = new StringBuilder();
        say(b,  "loadRequested :", loadRequested);
        say(b,  "storeRequested:", storeRequested);
        say(b,  "          type:", type);
        say(b,  "       address:", address);
        say(b,  "sourceRegister:", sourceRegister);
        say(b,  "targetRegister:", targetRegister);
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
      immI7   = immi.b(D.I.p_sr+2);                                             // The bit that differentiates the type of right shift
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
      rXori   = C.enableWordIfEq(q("rXori"),   xorI, funct3, f3_xor);
      rOri    = C.enableWordIfEq(q("rOri"),     orI, funct3, f3_or);
      rAndi   = C.enableWordIfEq(q("rAndi"),   andI, funct3, f3_and);
      rSlli   = C.enableWordIfEq(q("rSlli"),   sllI, funct3, f3_sll);

      rSrli   = C.enableWordIfEq(q("rSrli"),   srlI, funct3, f3_srl);
      rSrai   = C.enableWordIfEq(q("rSrai"),   sraI, funct3, f3_srl);
      rSral   = C.chooseFromTwoWords(q("rSral"), rSrli, rSrai, immI7);          // Particular case of shift right

      rSlti   = C.enableWordIfEq(q("rSlti"),   sltI, funct3, f3_slt);
      rSltui  = C.enableWordIfEq(q("rSltui"), sltuI, funct3, f3_sltu);

      iR      = C.orBits(q("iR"),  rAddi, rXori, rOri,  rAndi,                  // Select the immediate operation as only the required value can be non zero
                                   rSlli, rSral, rSlti, rSltui);

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

      eq12    = C.compareEq        (q("eq12"),  rs1v, rs2v);                    // Compare source with immediate
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

      eid13   = C.eq(C.bits(q("opCode13"),   l_opCode, D.opArithImm), iR   );   // Was it an arithmetic with immediate instruction?
      eid33   = C.eq(C.bits(q("opCode33"),   l_opCode, D.opArith   ), dR   );   // Was it an arithmetic with two source registers?
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
      m.targetRegister = rd;                                                    // Load register rd from memory indexed by rs1 and immediate
      m.sourceRegister = rs2;                                                   // Store content of rs2 into memory indexed by rd1 and immediate

      modifyRd = C.checkIn(q("modifyRd"), opCode,                               // Is this an operation code that modifies the target register?
       D.opArithImm, D.opArith, D.opJal, D.opJalr, D.opLui, D.opAuiPc);

      X[0] = null;                                                              // X0
      for (int i = 1; i < XLEN; i++)                                            // Values to reload back into registers
       {final Bit eqReg = C.compareEq(q("eqReg_")       +i, rd,   i);           // Is this the target register I see before me?
        final Bit eqRegMod =    C.And(q("eqRegMod_")    +i, modifyRd, eqReg);   // Is this the target register and we have an instruction that modifies it?
        X[i] = C.chooseFromTwoWords  (q("modTargetReg_")+i, x[i], result, eqRegMod);// Either the passed in register value or the newly computed one
       }
     }
   }

  static RV32I rv32i(Chip chip, String out, Bits decode, Bits pc, Bits[]x)      // Decode the specified bits as a RV32I instruction and execute it
   {return new RV32I(chip, out, decode, pc, x);
   }

  static class Cpu extends Chip                                                 // RiscV cpu on a chip
   {final int       N = 110;                                                    // The number of steps to execute an instruction
    Pulse          xi;                                                          // Execute an instruction
    Register       pc;                                                          // Initialize pc
    Register[]      x;                                                          // The registers of the RiscV architecture
    final long[] Code;                                                          // The code to be executed by this Cpu
    Bits         code;                                                          // Instructions as numbers
    Bits          pc4;                                                          // Address instruction in blocks of XLEN bits spread across 8 bit bytes
    Bits  instruction;                                                          // Latest instruction
    RV32I         cpu;                                                          // Execute instruction

    final byte[]memory;                                                         // Memory for this chip
    final Stack<Integer>stdin  = new Stack<>();                                 // Stdin
    final Stack<Integer>stdout = new Stack<>();                                 // Stdout
    final Stack<Integer>stderr = new Stack<>();                                 // Stderr

    Cpu(int Memory, long...Code)                                                // Create CPU with the specified amount of memory and a preloaded program
     {this.Code = Code;                                                         // Code to be executed - prepare using RiscV.java
      memory = new byte[Memory];                                                // Allocate memory
      xi = pulse("xi").period(  N).on(N/2).start(1).b();                        // Execute an instruction
      pc = new Register("pc", XLEN, xi, 0);                                     // Initialize program counter
       x = new Register[XLEN];                                                  // The registers of the RiscV architecture

      for (int i = 1; i < XLEN; i++) x[i] = new Register("x"+i, XLEN, xi, 0);   // Initialize registers

      code = bits("code", XLEN, Code);                                          // Instructions as numbers

      pc4  = shiftRightArithmetic("pc4", pc, 2);                                // Address instruction in blocks of XLEN bits spread across 8 bit bytes
      instruction = readMemory("instruction", code, pc4, XLEN);                 // Latest instruction

      cpu  = rv32i(this, "cpu", instruction, pc, x);                            // Execute instruction

      for (int i = 1; i < XLEN; i++) x[i].load(cpu.X[i]);                       // Update registers

      continueBits(pc.load, cpu.PC);                                            // Update the program counter

      cpu.m.anneal();                                                           // Anneal potentially unused gates
      for (int i = 1; i < XLEN; i++) cpu.X[i].anneal();
     }

    final public void eachStep()                                                // Implement load and store instructions, ecall instruction as these  instructions interact with the outside world
     {final Integer opCode = cpu.opCode.Int();                                  // Opcode
      if (opCode == null) return;
      switch(opCode)                                                            // Switch on opcode
       {case RiscV.Decode.opStore ->                                            // Store instruction
         {if (xi.fellStep == steps)
           {final int a = cpu.m.address.Int();
            final int r = cpu.m.sourceRegister.Int();
            final int v = cpu.x[r].Int();

            memory[a] = (byte) (v & 0xff);                                      // Store at least a byte
            switch(cpu.funct3.Int())                                            // Decode type of store
             {case RiscV.Decode.f3_sb -> {}                                     // Byte
              case RiscV.Decode.f3_sh ->                                        // Two bytes == half word
               {memory[a+1] = (byte) (v >> 8 & 0xff);
               }
              case RiscV.Decode.f3_sw ->                                        // Full word
               {memory[a+1] = (byte) (v >> 8 & 0xff);
                memory[a+2] = (byte) (v >>16 & 0xff);
                memory[a+3] = (byte) (v >>24 & 0xff);
               }
              default -> stop("Unknown funct3", cpu.funct3.Int(), "for store operation");
             }
           }
         }
        case RiscV.Decode.opLoad ->                                             // Load instruction
         {if (xi.fellStep >= steps - 4)                                         // The target bits have to be set for several steps to make them stick. It is not apparent why 3 steps are needed, but for 32 bit wide operands it seems to work so we go with it.
           {final int a = cpu.m.address.Int();
            final int r = cpu.m.targetRegister.Int();
            int A = 0, B = 0, C = 0, D = 0, v = 0;
            switch(cpu.funct3.Int())
             {case RiscV.Decode.f3_lb  -> {v = memory[a];                                                                    v <<= 24; v >>= 24;}
              case RiscV.Decode.f3_lbu -> {v = memory[a];                                                                                       }
              case RiscV.Decode.f3_lh  -> {A = memory[a]; B = memory[a];                                     v = A | (B<<8); v <<= 16; v >>= 16;}
              case RiscV.Decode.f3_lhu -> {A = memory[a]; B = memory[a];                                     v = A | (B<<8);                    }
              case RiscV.Decode.f3_lw  -> {A = memory[a]; B = memory[a+1]; C = memory[a+2]; D = memory[a+3]; v = A | (B<<8) | (C<<16) | (D<<24);}
              default -> stop("Unknown funct3", cpu.funct3.Int(), "for load operation");
             }
            x[r].set(v);
           }
         }
        case RiscV.Decode.opEcall ->                                            // Supervisor call
         {if (xi.fellStep == steps)                                             // Instruction has just executed
           {final Integer src = cpu.x[1].Int();                                 // Supervisor service code as requested in x1
            switch(src)                                                         // Decode supervisor service as requested in x1
             {case RiscV.Decode.eCall_stop         -> {throw new Stop();}              // Stop: brings the emulation to an end
              case RiscV.Decode.eCall_read_stdin   -> {cpu.x[1].set(stdin.remove(0));} // Read a 32 bit integer from stdin channel
              case RiscV.Decode.eCall_write_stdout -> {stdout.push(cpu.x[2].Int());}   // Write a 32 bit integer to stdout channel
              case RiscV.Decode.eCall_write_stderr -> {stderr.push(cpu.x[2].Int());}   // Write a 32 bit integer to stderr channel
              default -> stop("Unknown supervisor request code:", src);
             }
           }
         }
        default -> {}                                                           // Not an instruction that requires interaction with the outside world
       }
     }

    public String toString()                                                    // Print the state of the cpu
     {final StringBuilder M = new StringBuilder();
      for (int i = 0; i < memory.length; i++)                                   // Non zero memory
       {if (memory[i] > 0) M.append(""+i+"="+memory[i]+", ");
       }
      if (M.length() > 0) M.setLength(M.length()-2);

      final StringBuilder R = new StringBuilder();                              // Non zero registers
      for (int i = 1; i < XLEN; i++)
       {final Integer r = x[i].Int();
        if (r != null && r != 0) R.append(""+i+"="+r+", ");
       }
      if (R.length() > 0) R.setLength(R.length()-2);                            // Non zero registers

      final StringBuilder b = new StringBuilder();                              // Format results
      if (M.length() > 0) b.append("Memory   : "+ M.toString()+"\n");
      if (R.length() > 0) b.append("Registers: "+ R.toString()+"\n");
      return b.toString();
     }
   } // Cpu

//D0 Tests                                                                      // Test the CPU

  static RV32I test_instruction(Integer instruction)                            // Test an instruction
   {final Chip          C = new Chip();
    final Bits         pc = C.bits("pc",  XLEN, 4);                             // Initialize pc
    final Bits []       x = new Bits[XLEN];
    for (int i = 1; i < XLEN; i++) x[i] = C.bits("x"+i, XLEN, i);               // Initialize registers

    final Bits     decode = C.bits("decode", XLEN, instruction);                // Instruction to decode and execute
    final RV32I         R = rv32i(C, "a", decode, pc, x);                       // Decode and execute the instruction
    for (int i = 1; i < XLEN; i++) R.X[i].anneal();                             // Anneal the outputs
    R.PC.anneal(); R.m.anneal();
    C.simulate();                                                               // 61 steps
    return R;
   }

  static void test_decode_addi()                                                // Addi
   {final RV32I R = test_instruction(0xa00093);                                 // 0000 1 001 0011

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

  static void test_decode_add1 () {RV32I i = test_instruction(0x310233);  i.ok("pc=8 x4=5");     i.rd.ok(4);}
  static void test_decode_add2 () {RV32I i = test_instruction(0x0201b3);  i.ok("pc=8 x3=4");     i.rd.ok(2);}
  static void test_decode_slt1 () {RV32I i = test_instruction(0x20afb3);  i.ok("pc=8 x31=1");    i.rd.ok(31);}
  static void test_decode_slt2 () {RV32I i = test_instruction(0x112f33);  i.ok("pc=8 x30=0");    i.rd.ok(30);}
  static void test_decode_jal  () {RV32I i = test_instruction(0x80016f);  i.ok("pc=12 x2=8");    i.rd.ok(2);}
// At start pc=4, x0=0, x2=2. Instruction decode: rd=2 rs1=0 imm=4. Should result in x2=0+4<<1 = 8 and pc=4+4 = 8
  static void test_decode_jalr () {RV32I i = test_instruction(0x400167);  i.ok("pc=8 x2=8");     i.rd.ok(2);}
  static void test_decode_lui  () {RV32I i = test_instruction(0x10b7);    i.ok("pc=8 x1=4096");  i.rd.ok(1);}
  static void test_decode_auipc() {RV32I i = test_instruction(0x4097);    i.ok("pc=8 x1=16388"); i.rd.ok(1);}
  static void test_decode_i31()   {RV32I i = test_instruction(0x1f00293); i.ok("pc=8 x5=31"); i.immi.ok(31);   i.immI.ok(31);}
  static void test_decode_i33()   {RV32I i = test_instruction(0x2100293); i.ok("pc=8 x5=33"); i.immi.ok(33);   i.immI.ok(33); i.addI.ok(33); i.iR.ok(33); i.result.ok(33);}
  static void test_decode_subi()  {RV32I i = test_instruction(0xffe18193);i.ok("pc=8 x3=1");  i.immi.ok(4094); i.immI.ok(-2); i.addI.ok(1); i.iR.ok(1); i.result.ok(1);}

  static void test_decode_sb()                                                  // Store instruction
   {RV32I r = test_instruction(0x1000a3);
    r.om("""
loadRequested : 0
storeRequested: 1
          type: 000
       address: 0x1
sourceRegister: 00001
targetRegister: 00001
""");
   r.rd.ok(1);
   r.rs1.ok(0);
   r.rs2.ok(1);
   }

  static void test_decode_lh()                                                  // Load instruction
   {RV32I r = test_instruction(0x1103);
    r.om("""
loadRequested : 1
storeRequested: 0
          type: 001
       address: 0x0
sourceRegister: 00000
targetRegister: 00010
""");
   r.rd.ok(2);
   r.rs1.ok(0);
   r.rs2.ok(0);
   }

  static void test_fibonacci()                                                  // Implement memory operations in Risc V cpu producing Fibonacci numbers
   {Cpu c = new Cpu(64, 0xa00193, 0x393, 0x213, 0x100293, 0x438a23, 0x200093, 0x400133, 0x73, 0x520333, 0x28233, 0x302b3, 0x138393, 0xfe33c8e3, 0xb3, 0x73)
     {public void run()                                                         // Run the simulation
       {simulationSteps(1000*N);                                                // Simulation steps: we need to set it to something, but on the other and the code has an eCall to exit so we do not have to be accurate just big enough.
        simulate();
        ok(stdout.toString(), "[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]");
        ok(memory[20], 0 );
        ok(memory[21], 1 );
        ok(memory[22], 1 );
        ok(memory[23], 2 );
        ok(memory[24], 3 );
        ok(memory[25], 5 );
        ok(memory[26], 8 );
        ok(memory[27], 13);
        ok(memory[28], 21);
        ok(memory[29], 34);
       }
     };
    c.run();                                                                    // Run the RiscV computer
   }

  static void test_bubble_sort()                                                // Bubble sort
   {long[]code = {0x900293, 0x502023, 0x300293, 0x502223, 0x100293, 0x502423, 0x200293, 0x502623, 0x800293, 0x502823, 0x600293, 0x502a23, 0x400293, 0x502c23, 0x700293, 0x502e23, 0x500293, 0x2502023, 0x2400193, 0xffc18193, 0x1ca63, 0x233, 0x22283, 0x422303, 0x62c363, 0x622023, 0x522223, 0x420213, 0xfe324ae3, 0xfd9ff06f, 0x233, 0x2103, 0x200093, 0x73, 0x402103, 0x200093, 0x73, 0x802103, 0x200093, 0x73, 0xc02103, 0x200093, 0x73, 0x1002103, 0x200093, 0x73, 0x1402103, 0x200093, 0x73, 0x1802103, 0x200093, 0x73, 0x1c02103, 0x200093, 0x73, 0x2002103, 0x200093, 0x73, 0x93, 0x73};

    Cpu c = new Cpu(64, code)
     {public void run()                                                         // Run the simulation
       {simulationSteps(1000*N);                                                // Simulation steps: we need to set it to sopmething, but on the other and the code has an eCall to exit so we do not have to be accurate just big enough.
        simulate();
        //say(stdout);
        ok(""+stdout, "[1, 2, 3, 4, 5, 6, 7, 8, 9]");
       }
     };
    c.run();                                                                    // Run the RiscV computer
   }

  static void test_insertion_sort()                                             // Insertion sort
   {long[]code = {0x900313, 0x602023, 0x300313, 0x602223, 0x100313, 0x602423, 0x200313, 0x602623, 0x800313, 0x602823, 0x600313, 0x602a23, 0x400313, 0x602c23, 0x700313, 0x602e23, 0x500313, 0x2602023, 0x2400293, 0x213, 0x525f63, 0x4001b3, 0xffc18193, 0x1ca63, 0x1a303, 0x41a383, 0x63d463, 0x71a023, 0x61a223, 0x80006f, 0xc0006f, 0xffc18193, 0xfddff06f, 0x420213, 0xfc9ff06f, 0x213, 0x525763, 0x22303, 0x600133, 0x200093, 0x73, 0x420213, 0xfe9ff06f, 0x93, 0x73};

    Cpu c = new Cpu(64, code)
     {public void run()                                                         // Run the simulation
       {simulationSteps(1000*N);                                                // Simulation steps: we need to set it to sopmething, but on the other and the code has an eCall to exit so we do not have to be accurate just big enough.
        simulate();
        //say(stdout);
        ok(""+stdout, "[1, 2, 3, 4, 5, 6, 7, 8, 9]");
       }
     };
    c.run();                                                                    // Run the RiscV computer
   }

  static void test_up()                                                         // Up for loop
   {long[]code = {0xa00213, 0x193, 0x41d663, 0x300133, 0x200093, 0x73, 0x118193, 0xfedff06f, 0x93, 0x73};
    Cpu c = new Cpu(64, code)
     {public void run()                                                         // Run the simulation
       {simulationSteps(100*N);
        simulate();
        //stop(stdout);
        ok(stdout, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]");
       }
     };
    c.run();                                                                    // Run the RiscV computer
   }

  static void test_down()                                                       // Down for loop
   {long[]code = {0xa00213, 0x4001b3, 0x18663, 0x300133, 0x200093, 0x73, 0xfff18193, 0xfedff06f, 0x93, 0x73};
    Cpu c = new Cpu(64, code)
     {public void run()                                                         // Run the simulation
       {simulationSteps(100*N);
        simulate();
        //stop(stdout);
        ok(stdout, "[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]");
       }
     };
    c.run();                                                                    // Run the RiscV computer
   }

  static void test_down_break()                                                 // Down for loop
   {long[]code = {0xa00213, 0x500293, 0x4001b3, 0x18763, 0x300133, 0x200093, 0x73, 0x51c363, 0xfff18193, 0xfe9ff06f, 0x93, 0x73};
    Cpu c = new Cpu(64, code)
     {public void run()                                                         // Run the simulation
       {simulationSteps(100*N);
        simulate();
        //stop(stdout);
        ok(stdout, "[10, 9, 8, 7, 6, 5, 4]");
       }
     };
    c.run();                                                                    // Run the RiscV computer
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
    test_decode_i31();
    test_decode_i33();
    test_fibonacci();
    test_bubble_sort();
    test_insertion_sort();
    test_up();
    test_down();
    test_down_break();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    //test_insertion_sort();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      Chip.testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(Chip.fullTraceBack(e));
     }
   }
 }
