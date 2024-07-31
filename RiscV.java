//------------------------------------------------------------------------------
// Execute Risc V machine code. Little endian RV32I.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
// Make it possible to automatically compare the results of RiscV with Ban.
package com.AppaApps.Silicon;                                                   // Design, emulate and layout digital a binary tree on a silicon chip.
/*
A beginning is the time for taking the most delicate care that the balances are
correct. This every sister of the Bene Gesserit knows. To begin your study of
the life of Muad'Dib, then, take care that you first place him in his time:
born in the 57th year of the Padishah Emperor, Shaddam V. And take the most
special care that you locate Muad'Dib in his place: the planet Arrakis. Do not
be deceived by the fact that he was born on Caladan and lived his first fifteen
years there. Arrakis, the planet known as Dune, is forever his place.

- from "Manual of Muad'Dib" by the Princess Irulan
*/
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

public class RiscV extends Chip                                                 // Load and execute a program written in RiscV machine code
 {final static int               XLEN = 32;                                     // Size of instructions
  final static int   instructionBytes =  4;                                     // Bytes per instruction - yes there are some two byte instructions but we are not using them yet.
  final static boolean    makeSayStop = false;                                  // Turn say into stop if true which is occasionally useful for locating unlabeled say statements.

  final String                   name;                                          // Name of program
  final int defaultMaxSimulationSteps = github_actions ? 1000 : 100;            // Default maximum simulation steps
  final int defaultMinSimulationSteps =    0;                                   // Default minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.
  Integer          maxSimulationSteps = null;                                   // Maximum simulation steps
  Integer          minSimulationSteps = null;                                   // Minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.

  final Stack<Encode>            code = new Stack<>();                          // Encoded instructions
  final Register[]                  x = new Register[32];                       // General purpose registers
  final Register x0,  x1,  x2,   x3,  x4,  x5,  x6,  x7,  x8,  x9,
                 x10, x11, x12, x13, x14, x15, x16, x17, x18, x19,
                 x20, x21, x22, x23, x24, x25, x26, x27, x28, x29, x30, x31, z;
  final byte[]                 memory = new byte[100];                          // Memory
  int                              pc = 0;                                      // Program counter expressed in bytes
  int                           steps = 0;                                      // Number of steps taken so far in executing the program

  final Stack<String>           ecall = new Stack<>();                          // Ecall executed

  TreeMap<String, Label>       labels = new TreeMap<>();                        // Labels in assembler code
  TreeMap<String, Variable> variables = new TreeMap<>();                        // Variables in assembler code
  int                      pVariables = 0;                                      // Position of variables in memory
  Stack<Sub>                     subs = new Stack<>();                          // Subroutines created

  final Stack<Integer>             in = new Stack<>();                          // Stdin
  final Stack<Integer>            out = new Stack<>();                          // Stdout
  final Stack<Integer>            err = new Stack<>();                          // Stderr
  final Stack<ReturnAddress>
                        returnAddress = new Stack<>();                          // Return address stack
  final int      returnAddressMaximum = 256;                                    // The return address stack is implemented in hardware and so is of fixed size
  final Register returnAddressRegister;                                         // The return address stack only applies to jalr instructions using this register as source and target
  final Stack<String>      traceBacks = new Stack<>();                          // Tracebacks printed during program execution so we can see them for testing purposes
  boolean     writeTraceBacksToStderr = true;                                   // Normally tracebacks are written to stderr so the user can see them, but sometimes for testing we want to save them for analysis

  RiscV(String Name)                                                            // Create a new program
   {name = Name;                                                                // Name of chip
    for (int i = 0; i < x.length; i++) x[i] = new Register(i);                  // Create the registers
    x0  = x[ 0];  x1 = x[ 1]; x2  = x[ 2]; x3  = x[ 3]; x4  = x[ 4];
    x5  = x[ 5];  x6 = x[ 6]; x7  = x[ 7]; x8  = x[ 8]; x9  = x[ 9];
    x10 = x[10]; x11 = x[11]; x12 = x[12]; x13 = x[13]; x14 = x[14];
    x15 = x[15]; x16 = x[16]; x17 = x[17]; x18 = x[18]; x19 = x[19];
    x20 = x[20]; x21 = x[21]; x22 = x[22]; x23 = x[23]; x24 = x[24];
    x25 = x[25]; x26 = x[26]; x27 = x[27]; x28 = x[28]; x29 = x[29];
    x30 = x[30]; x31 = x[31]; z   = x0;
    returnAddressRegister = x31;                                                // The return address stack only applies to jalr instructions using this register as source and target
   }

  RiscV()                                                                       // Create a new program with the name of the test
   {this(Chip.currentTestNameSuffix());
   }

  int maxSimulationSteps(int MaxSimulationSteps) {return maxSimulationSteps = MaxSimulationSteps;}  // Maximum simulation steps
  int minSimulationSteps(int MinSimulationSteps) {return minSimulationSteps = MinSimulationSteps;}  // Minimum simulation steps

  void simulationSteps(int min, int max) {minSimulationSteps(min);   maxSimulationSteps(max);}      // Stop cleanly between the specified minimum and maximum number of steps
  void simulationSteps(int steps)        {minSimulationSteps(steps); maxSimulationSteps(steps);}    // Stop cleanly at this number of steps

  public String toString()                                                      // Convert state chip to string
   {final StringBuilder b = new StringBuilder();
    b.append("RiscV      : " + name  + "\n");
    b.append("Step       : " + steps + "\n");
    b.append("Instruction: " + pc    + "\n");
    int m = 0, r = 0;                                                           // Number of memory bytes and registers that are not zero
    for (int i = 0; i < x.length;      i++) if (x[i].value != 0) ++r;           // Print non zero registers
    for (int i = 0; i < memory.length; i++) if (memory[i]  != 0) ++m;           // Print non zero memory

    if (r > 0)                                                                  // Print non zero registers
     {b.append("Registers  : ");
      for (int i = 0; i < x.length; i++)
       {final int v = x[i].value;
        if (v != 0) b.append(" x"+i+"="+v);
       }
      b.append("\n");
     }
    if (m > 0)                                                                  // Print non zero memory
     {b.append("Memory     : ");
      for (int i = 0; i < memory.length; i++)
       {final int v = memory[i];
        if (v != 0) b.append(" "+i+"="+v);
       }
      b.append("\n");
     }

    if (out.size() > 0)                                                         // Print out
     {b.append("Out        : ");
      final int N = out.size();
      for (int i = 0; i < N; i++) b.append(""+out.elementAt(i)+", ");
      b.setLength(b.length()-2);
      b.append("\n");
     }

    if (err.size() > 0)                                                         // Print err
     {b.append("Err        : ");
      final int N = err.size();
      for (int i = 0; i < N; i++) b.append(""+err.elementAt(i)+", ");
      b.setLength(b.length()-2);
      b.append("\n");
     }

    return b.toString();                                                        // String describing chip
   }

  public String printCode()                                                     // Print the program being run by the chip
   {final StringBuilder b = new StringBuilder();
    b.append("RiscV Hex Code: " + name  + "\n");
    final int N = code.size();
    b.append("Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value\n");
    for (int i = 0; i < N; i++)                                                 // Print each instruction
      b.append(String.format("%04x  %s", i, code.elementAt(i).toString()));
    return b.toString();                                                        // String describing chip
   }

  public String printCodeSequence()                                             // Print the program code in a form where it can be used by Ban
   {final StringBuilder b = new StringBuilder();
    final int N = code.size();
    for (int i = 0; i < N; i++)                                                 // Print each instruction
      b.append(String.format("0x%x, ", code.elementAt(i).instruction));
    if (N > 0) b.setLength(b.length()-2);
    return "long[]code = {"+b.toString()+"};";                                  // String describing program
   }

  public void ok(String expected)                                               // Confirm the current state of the Risc V chip
   {ok(toString(), expected);
   }

  class Register                                                                // Description of a register
   {final int x;                                                                // Number of register
    int value;                                                                  // Current value of the register

    Register(int register)                                                      // Describe a register
     {x = register;
      if (x < 0 || x >= XLEN) stop("Register must be 0 to", XLEN, "not", x);
     }

    public String toString() {return ""+value;}                                 // Print the value of a register
   }

//D1 Emulate                                                                    // Emulate the execution of a Risc V program

  void setLabels()                                                              // Set labels so that they address the targets of branch instructions
   {final int N  = code.size();
    for (int i = 0; i < N; i++) code.elementAt(i).setLabel(i);
   }

  void emulate()                                                                // Emulate the execution of the program
   {final int actualMaxSimulationSteps =                                        // Actual limit on number of steps
      maxSimulationSteps != null ? maxSimulationSteps : defaultMaxSimulationSteps;
    final boolean miss = minSimulationSteps != null;                            // Minimum simulation steps set

    pc = 0;                                                                     // Reset
    for (int i = 0; i < x.length; i++) x[i].value = 0;                          // Clear registers
    setLabels();                                                                // Set branch instructions so they address their targets

    for (steps = 1; steps <= actualMaxSimulationSteps; ++steps)                 // Steps in time
     {if (pc >= instructionBytes * code.size()) return;                         // Off the end of the code
      final Encode e = code.elementAt(pc / instructionBytes);
      final Decode.Executable d = decode(e);
      if (d == null) stop("Need data for instruction at byte:", pc);
      try {d.action();} catch(Stop x) {return;}                                 // Execute the instruction and respond to any exceptions
      eachEmulationStep();                                                      // Called at each step of the emulation
     }
    if (maxSimulationSteps == null)                                             // Not enough steps available by default
     {err("Out of time after", actualMaxSimulationSteps, "steps");
     }
   }

  void eachEmulationStep() {}                                                   // Called at each step of the emulation

// D1 Trace back                                                                // Produce a trace back showing where we are in a program in terms of subroutines called

  record ReturnAddress                                                          // Record calls and returns so we can print a trace back of subroutine calls
   (int returnTo,                                                               // The absolute address in the program code that we are going to return to
    int called                                                                  // The absolute address of the subroutine we called
   ){}

  String locateContainingSubName(int offset, String def)                        // Locate the subroutine that contains a specified offset
   {for (Sub s: subs)                                                           // Each subroutine
     {final int o = offset / instructionBytes;
      if (o >= s.start.offset && o <= s.end.offset) return s.name;              // Found a plausible subroutine
     }
    return def;                                                                 // Return a default value indicating we could not find the subroutine
   }

  void writeTraceBack()                                                         // Write a trace back
   {final StringBuilder b = new StringBuilder();
    b.append(String.format("%4s  %4s  %16s\n","To", "From", "Subroutine"));

    final TreeMap<Integer,String> t = new TreeMap<>();                          // Instruction addresses to label names
    for (Label l : labels.values()) t.put(l.offset * instructionBytes, l.name); // Assumes fixed instruction size

    for (int i = returnAddress.size()-1; i >= 0; --i)                           // Trace back
     {final ReturnAddress r = returnAddress.elementAt(i);
      final int called = r.called, returnTo = r.returnTo;
      final String c = locateContainingSubName(called, "");
      say(b, String.format("%4d  %4d  %16s", called, returnTo, c));             // Traceback entry
     }
    if (writeTraceBacksToStderr) System.err.print(b.toString());                // Save or write traceback
    else traceBacks.push(b.toString());
   }

  void trace()                                                                  // Request a trace back
   {addi(x1, x0, Decode.eCall_trace_back);
    ecall();
   }

//D1 Encode and Decode                                                          // Encode and decode instructions to and from their binary formats in machine code

  class Encode                                                                  // Encode an instruction
   {int instruction;                                                            // Resulting instruction
    final Label label;                                                          // Label describing target of branch instruction

    Encode(int opCode, Register rd, Register rs1, Register rs2,                 // Encode an instruction
      int funct3, int funct5, int funct7, int subType, int immediate,
      int aq, int rl, Label Label
     )
     {opCode  &= Decode.m_opCode;                                               // Mask input values to desired ranges
      funct3  &= Decode.m_funct3;
      funct5  &= Decode.m_funct5;
      funct7  &= Decode.m_funct7;
      subType &= Decode.m_subType;
      rl      &= Decode.m_rl;
      aq      &= Decode.m_aq;
      instruction = opCode | immediate                                          // Overlay the fields - zero fields will have no effect so this safe.
                  | (rd  != null ? rd.x  <<  7 : 0)
                  | (rs1 != null ? rs1.x << 15 : 0)
                  | (rs2 != null ? rs2.x << 20 : 0)
                  | (funct3  << 12)
                  | (subType << 12)
                  | (funct7  << 25)
                  | (rl      << 25)
                  | (aq      << 26)
                  | (funct5  << 27);

      label = Label;
      code.push(this);
     }

    Encode(int opCode, Register rd, Register rs1, Register rs2,                 // Encode an instruction without aq or rl or a label
      int funct3, int funct5, int funct7, int subType, int immediate
     )
     {this(opCode, rd, rs1, rs2, funct3, funct5, funct7, subType, immediate,
           0, 0, null);
     }

    Encode(int opCode, Register rd, Register rs1, Register rs2,                 // Encode an instruction without aq or rl but with a label
      int funct3, int funct5, int funct7, int subType, int immediate,
      Label label
     )
     {this(opCode, rd, rs1, rs2, funct3, funct5, funct7, subType, immediate,
           0, 0, label);
     }

    void setLabel(int offset) throws Stop                                       // Set immediate field from any label referenced by this instruction
     {if (label == null) return;                                                // No label
      final Decode d = decode(this).details();                                  // Decode this instruction so we can reassemble it with the current immediate value
      final int    o = label.fixed ? label.offset : label.offset - offset;      // Offset to target instruction from current instruction in blocks of 2 bytes either as a fixed or relative address
      final int    i = switch(d.format)                                         // Choose encoding format for immediate operand
       {case "B" -> encodeBImmediate(o << 1);                                   // Offset to target instruction from current instruction in signed blocks of 2 bytes
        case "I" -> encodeIImmediate(o << 1);                                   // Offset to target instruction from current instruction in signed blocks of 2 bytes
        case "J" -> encodeJImmediate(o << 1);                                   // Offset to target instruction from current instruction in signed blocks of 2 bytes
        case "U" -> encodeUImmediate(o << 1);                                   // Offset to target instruction from current instruction in signed blocks of 2 bytes
        default  -> {stop("Cannot use format", d.format, "in a jump"); yield 0;}
       };
      final Encode e = new Encode(d.opCode, x[d.rd], x[d.rs1], x[d.rs2],        // Encode an instruction without aq or rl or a label
        d.funct3, d.funct5, d.funct7, d.subType, i);
      code.pop();                                                               // Remove unwanted instruction just added as part of re-encoding.  This is safe to do as we have updated the existing instruction that we want to keep.
      instruction = e.instruction;                                              // Update current instruction with intermediate value showing offset to the target instruction
     }

    public String toString()                                                    // Instruction as string
     {final Decode d = decode(this).details();
      return String.format
       ("%8s  %4x  %2x %2x %2x  %2x  %2x %2x %2x  %x %x   %8x %8x\n",
        d.name, d.opCode, d.rd,  d.rs1,  d.rs2, d.subType, d.funct3, d.funct5,
        d.funct7, d.aq ? 1 : 0, d.rl ? 1 : 0, d.immediate, instruction);
     }
   }

  static class Decode                                                           // Decode an instruction. The class is static to allow the constants to be easily accessed elsewhere
   {final Encode instruction;                                                   // Instruction to be decoded
    String format = null;                                                       // Format of instruction
    String   name = null;                                                       // Name of instruction
    int immediate = 0;                                                          // Immediate value
    boolean    rl = false;                                                      // rl
    boolean    aq = false;                                                      // aq

    final int rd;                                                               // Destination register
    final int opCode;                                                           // Operation code
    final int funct3;                                                           // Sub function
    final int funct5;                                                           // Sub function
    final int funct7;                                                           // Sub function
    final int rs1;                                                              // Source 1 register
    final int rs2;                                                              // Source 2 register
    final int subType;                                                          // Sub type

    final static int   p_opCode =  0, l_opCode  =  7;                           // Encoded position of op code - zero based
    final static int       p_rd =  7, l_rd      =  5;                           // Encoded position of destination register
    final static int   p_funct3 = 12, l_funct3  =  3;                           // Encoded position of sub function
    final static int  p_subType = 12, l_subType =  3;                           // Encoded position of sub type
    final static int      p_rs1 = 15, l_rs1     =  5;                           // Encoded position of source register 1
    final static int      p_rs2 = 20, l_rs2     =  5;                           // Encoded position of source register 2
    final static int   p_funct7 = 25, l_funct7  =  7;                           // Encoded position of sub function
    final static int       p_rl = 25, l_rl      =  1;                           // Encoded position of rl
    final static int       p_aq = 26, l_aq      =  1;                           // Encoded position of aq
    final static int   p_funct5 = 27, l_funct5  =  5;                           // Encoded position of sub function

    final static int   m_opCode = 0b111_1111;                                   // Mask for op code
    final static int       m_rd = 0b001_1111;                                   // Mask for destination register
    final static int      m_rs1 = 0b001_1111;                                   // Mask for source register 1
    final static int      m_rs2 = 0b001_1111;                                   // Mask for source register 2
    final static int   m_funct3 = 0b000_0111;                                   // Mask for sub function 3
    final static int   m_funct5 = 0b001_1111;                                   // Mask for sub function 5
    final static int   m_funct7 = 0b111_1111;                                   // Mask for sub function 7
    final static int  m_subType = 0b000_0111;                                   // Mask for sub type
    final static int       m_rl = 0b000_0001;                                   // Mask for rl
    final static int       m_aq = 0b000_0001;                                   // Mask for aq

    final static int     opLoad = 0b000_0011;                                   // Opcodes - load
    final static int opArithImm = 0b001_0011;
    final static int    opAuiPc = 0b001_0111;
    final static int    opStore = 0b010_0011;
    final static int    opArith = 0b011_0011;
    final static int      opLui = 0b011_0111;
    final static int   opBranch = 0b110_0011;
    final static int    opEcall = 0b111_0011;
    final static int      opJal = 0b110_1111;
    final static int     opJalr = 0b110_0111;

    final static int     f3_add = 0;                                            // Funct3 op codes
    final static int     f3_xor = 4;
    final static int      f3_or = 6;
    final static int     f3_and = 7;
    final static int     f3_sll = 1;
    final static int     f3_srl = 5;
    final static int     f3_slt = 2;
    final static int    f3_sltu = 3;

    final static int     f3_beq = 0;
    final static int     f3_bne = 1;
    final static int     f3_blt = 4;
    final static int     f3_bge = 5;
    final static int    f3_bltu = 6;
    final static int    f3_bgeu = 7;

    final static int     f3_sb  = 0;
    final static int     f3_sh  = 1;
    final static int     f3_sw  = 2;
    final static int     f3_lb  = 0;
    final static int     f3_lbu = 4;
    final static int     f3_lh  = 1;
    final static int     f3_lhu = 5;
    final static int     f3_lw  = 2;

    final static int    f3_jalr = 0;

    final static int  eCall_stop         = 0;                                   // Stop requested
    final static int  eCall_read_stdin   = 1;                                   // Read an integer from stdin
    final static int  eCall_write_stdout = 2;                                   // Write an integer to stdout
    final static int  eCall_write_stderr = 3;                                   // Write an integer to stdout
    final static int  eCall_trace_back   = 4;                                   // Writes a trace back of the current call stac to stderr

    Decode(String Name, Encode Instruction)                                     // Decode an instruction
     {instruction = Instruction;
      name        = Name;
      final int i = instruction.instruction;
      opCode      = (i >> p_opCode)  & m_opCode;
      rd          = (i >> p_rd     ) & m_rd;                                    // Destination register
      rs1         = (i >> p_rs1    ) & m_rs1;                                   // Source register 1
      rs2         = (i >> p_rs2    ) & m_rs2;                                   // Source register 2
      funct3      = (i >> p_funct3 ) & m_funct3;                                // Sub function
      funct5      = (i >> p_funct5 ) & m_funct5;                                // Sub function
      funct7      = (i >> p_funct7 ) & m_funct7;                                // Sub function
      subType     = (i >> p_subType) & m_subType;                               // Sub type
     }

    Decode(Encode Instruction)                                                  // Decode an instruction
     {this(null, Instruction);
     }

    public void action() {}                                                     // Action required to implement an instruction

    public int instructionSize() {return instructionBytes;}                     // Size of decoded instruction
    public int advance(int pc)   {return pc + instructionSize();}               // Advance the program counter over this instruction

    public String toString()                                                    // Print instruction
     {return name
       + " rd"          + rd
       + " opCode"      + opCode
       + " funct3"      + funct3
       + " funct5"      + funct5
       + " funct7"      + funct7
       + " rs1"         + rs1
       + " rs2"         + rs2
       + " subType"     + subType;
     }

    interface Executable                                                        // An instruction that can be executed
     {public default void action() {stop("Implementation needed");}             // Action performed by instruction
      Decode details();                                                         // Decoded instruction details
     }

    class B implements Executable                                               // Decode a B format instruction
     {final static int immWidth = 12;                                           // Immediate width. Twice the signed immediate value specifies the offset in bytes of the next instruction relative to the current instruction.
      final static int m_31_31  = 0b10000000000000000000000000000000;
      final static int m_30_25  = 0b01111110000000000000000000000000;
      final static int m_11_08  = 0b00000000000000000000111100000000;
      final static int m_07_07  = 0b00000000000000000000000010000000;

      static int immediate(int i)                                               // Decode a B format immediate operand
       {final int a = ((i & m_31_31) >>> 31) << 11;
        final int b = ((i & m_30_25) >>> 25) <<  4;
        final int c = ((i & m_11_08) >>>  8) <<  0;
        final int d = ((i & m_07_07) >>>  7) << 10;

        return (((a|b|c|d)<<20)>>20)<<1;                                        // The 2*immediate field encodes the offset in bytes, but we need the offset in blocks of 4 bytes because we do not use the 2 byte instructions in this implementations.
       }

      B(String Name)                                                            // Decode B format instruction immediate field
       {format = "B"; name = Name; immediate = immediate(instruction.instruction);
       }
      public Decode details() {return Decode.this;}                             // Decoded instruction details

      public String toString()                                                  // Print instruction
       {return String.format("B %7s %8s rs1=%2d rs2=%2d imm=0x%x, %d",
          binaryString(opCode, 7), name, rs1, rs2, immediate, immediate);
       }
     }

    class I implements Executable                                               // Decode an I format instruction
     {final static int p_immediate = 20, l_immediate = 12, p_sr = 5;            // Position of immediate value

      I(String Name)                                                            // Decode instruction
       {format = "I"; name = Name;
        immediate = instruction.instruction >> p_immediate;                     // Immediate value
       }

      public Decode details() {return Decode.this;}                             // Decoded instruction details
      public String toString()                                                  // Print instruction
       {return String.format
         ("I %7s %8s rd=%2d rs1=%2d        funct3=%2x imm=0x%x, %d",
          binaryString(opCode, 7), name, rd, rs1, funct3, immediate, immediate);
       }
     }

    class J implements Executable                                               // Decode a J format instruction
     {final static int m_31_31 = 0b10000000000000000000000000000000;            // Masks for areas of the intermediate value
      final static int m_30_21 = 0b01111111111000000000000000000000;
      final static int m_20_20 = 0b00000000000100000000000000000000;
      final static int m_19_12 = 0b00000000000011111111000000000000;

      static int immediate(int i)                                               // Decode J immediate
       {final int a = ((i & m_31_31) >>> 31) << 19;                             // 20
        final int b = ((i & m_30_21) >>> 21) <<  0;                             // 10-1
        final int c = ((i & m_20_20) >>> 20) << 10;                             // 11
        final int d = ((i & m_19_12) >>>  1) <<  0;                             // 19-12

        return ((a|b|c|d)<<12)>>12;
       }

      J(String Name)                                                            // Decode a J format instruction
       {format = "J"; name = Name;
        immediate = immediate(instruction.instruction);                         // Immediate value
       }

      public Decode details() {return Decode.this;}                             // Decoded instruction details
      public String toString()                                                  // Print instruction
       {return String.format("J %7s %8s rd=%2d imm=0x%x, %d",
          binaryString(opCode, 7), rd, name, immediate, immediate);
       }
     }


    class R implements Executable                                               // Decode a R format instruction
     {R(String Name) {name = Name; format = "R"; }                              // Decode instruction
      public Decode details() {return Decode.this;}                             // Decoded instruction details

      public String toString()                                                  // Print instruction
       {return String.format
         ("R %7s %8s rd=%2d rs1=%2d rs2=%2d funct3=%2x funct7=%2x",
          binaryString(opCode, 7), name, rd, rs1, rs2, funct3, funct7);
       }
     }


    class Ra implements Executable                                              // Decode a R atomic format instruction
     {Ra(String Name)                                                           // Decode instruction
       {name = Name; format = "Ra";
        final int i = instruction.instruction;
        rl          = ((i >>> 25) & 0b1) == 0b1;                                // rl
        aq          = ((i >>> 26) & 0b1) == 0b1;                                // aq
       }

      public Decode details() {return Decode.this;}                             // Decoded instruction details
      public String toString()                                                  // Print instruction
       {return String.format
         ("Ra %7s %8s rd=%2d rs1=%2d rs2=%2d funct3=%2x funct5=%2x aq=%d rl=%d",
          binaryString(opCode, 7), name, rd, rs1, rs2, funct3, funct5,  aq, rl);
       }
     }

    class S implements Executable                                               // Decode a S format instruction
     {final static int m_31_25 = 0b11111110000000000000000000000000;
      final static int m_11_07 = 0b00000000000000000000111110000000;

      static int immediate(int i)                                               // Decode the immediate field of an S format instruction
       {final int a = ((i & m_31_25) >>> 25) << 5;
        final int b = ((i & m_11_07) >>>  7) << 0;

        return ((a|b)<<20)>>20;
       }

      S(String Name)                                                            // Decode instruction
       {format = "S"; name = Name;
        immediate = immediate(instruction.instruction);
       }

      public Decode details() {return Decode.this;}                             // Decoded instruction details
      public String toString()                                                  // Print instruction
       {return String.format
         ("S %7s %8s rd=%2d rs1=%2d  rs2=%2d funct3=%2x imm=0x%x, %d",
          binaryString(opCode, 7), name, rd, rs1, rs2, funct3,
          immediate, immediate);
       }
     }


    class U implements Executable                                               // Decode a U format instruction
     {final static int p_immediate = 12, l_immediate = 20;                      // Position of immediate value

      U(String Name)                                                            // Decode instruction
       {format = "U"; name = Name;
        immediate = instruction.instruction >>> p_immediate;                    // Immediate value
       }

      public Decode details() {return Decode.this;}                             // Decoded instruction details
      public String toString()                                                  // Print instruction
       {return String.format("U %7s %8s rd=%2d imm=0x%x, %d",
          binaryString(opCode, 7), rd, name, immediate, immediate);
       }
     }
   }

  Decode.Executable decode(Encode e) throws Stop                                // Decode an instruction
   {final Decode d = new Decode(e);
    switch(d.opCode)
     {case Decode.opArith ->                                                    // Arithmetic
       {if (d.funct7 == 0)
         {switch(d.funct3)
           {case Decode.f3_add : return d.new R("add")  {public void action() {x[d.rd].value = x[d.rs1].value +   x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_xor : return d.new R("xor")  {public void action() {x[d.rd].value = x[d.rs1].value ^   x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_or  : return d.new R("or")   {public void action() {x[d.rd].value = x[d.rs1].value |   x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_and : return d.new R("and")  {public void action() {x[d.rd].value = x[d.rs1].value &   x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_sll : return d.new R("sll")  {public void action() {x[d.rd].value = x[d.rs1].value <<  x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_srl : return d.new R("srl")  {public void action() {x[d.rd].value = x[d.rs1].value >>> x[d.rs2].value;         pc = d.advance(pc);}};
            case Decode.f3_slt : return d.new R("slt")  {public void action() {x[d.rd].value = x[d.rs1].value <   x[d.rs2].value ? 1 : 0; pc = d.advance(pc);}};
            case Decode.f3_sltu: return d.new R("sltu") {public void action() {x[d.rd].value = x[d.rs1].value <   x[d.rs2].value ? 1 : 0; pc = d.advance(pc);}};
            default            : return null;
           }
         }
        else if (d.funct7 == 2)
         {switch(d.funct3)
           {case Decode.f3_add : return d.new I("sub")  {public void action() {x[d.rd].value = x[d.rs1].value -   d.immediate; pc = d.advance(pc);}};
            case Decode.f3_srl : return d.new I("sra")  {public void action() {x[d.rd].value = x[d.rs1].value >>  d.immediate; pc = d.advance(pc);}};
            default            : return null;
           }
         }
        else return null;
       }

      case Decode.opArithImm ->                                                 // Arithmetic with immediate operands
       {switch(d.funct3)
         {case Decode.f3_add:  return d.new I("addi")   {public void action() {x[d.rd].value = x[d.rs1].value +   d.immediate;         pc = d.advance(pc);}};
          case Decode.f3_xor:  return d.new I("xori")   {public void action() {x[d.rd].value = x[d.rs1].value ^   d.immediate;         pc = d.advance(pc);}};
          case Decode. f3_or:  return d.new I("ori")    {public void action() {x[d.rd].value = x[d.rs1].value |   d.immediate;         pc = d.advance(pc);}};
          case Decode.f3_and:  return d.new I("andi")   {public void action() {x[d.rd].value = x[d.rs1].value &   d.immediate;         pc = d.advance(pc);}};
          case Decode.f3_sll:  return d.new I("slli")   {public void action() {x[d.rd].value = x[d.rs1].value <<  d.immediate;         pc = d.advance(pc);}};
          case Decode.f3_srl:
            final Decode dI = d.new I(null).details();
            switch((dI.immediate >> Decode.I.p_sr) & 0b111_1111)
             {case 0:  return d.new I("srli")           {public void action() {x[d.rd].value = x[d.rs1].value >>> d.immediate;         pc = d.advance(pc);}};
              case 2:  return d.new I("srai")           {public void action() {x[d.rd].value = x[d.rs1].value >>  d.immediate;         pc = d.advance(pc);}};
              default: return null;
             }
          case Decode.f3_slt : return d.new I("slti" )  {public void action() {x[d.rd].value = x[d.rs1].value <   d.immediate ? 1 : 0; pc = d.advance(pc);}};
          case Decode.f3_sltu: return d.new I("sltiu")  {public void action() {x[d.rd].value = x[d.rs1].value <   d.immediate ? 1 : 0; pc = d.advance(pc);}};
          default            : return null;
         }
       }

      case Decode.opBranch ->                                                   // Branch
       {switch(d.funct3)
         {case Decode. f3_beq: return d.new B("beq")    {public void action() {if (x[d.rs1].value == x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          case Decode. f3_bne: return d.new B("bne")    {public void action() {if (x[d.rs1].value != x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          case Decode. f3_blt: return d.new B("blt")    {public void action() {if (x[d.rs1].value <  x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          case Decode. f3_bge: return d.new B("bge")    {public void action() {if (x[d.rs1].value >= x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          case Decode.f3_bltu: return d.new B("bltu")   {public void action() {if (x[d.rs1].value <  x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          case Decode.f3_bgeu: return d.new B("bgeu")   {public void action() {if (x[d.rs1].value >= x[d.rs2].value) pc += d.immediate<<1; else pc = d.advance(pc);}};
          default:             return null;
         }
       }

      case Decode.opStore ->                                                    // Store
       {switch(d.funct3)
         {case Decode.f3_sb:      return d.new S("sb")
           {public void action()
             {pc = d.advance(pc);
              memory[x[d.rs1].value+d.immediate]   = (byte) (x[d.rs2].value>>>0  & 0xff);
             }
           };
          case Decode.f3_sh:      return d.new S("sh")
           {public void action()
             {pc = d.advance(pc);
              memory[x[d.rs1].value+d.immediate]   = (byte) (x[d.rs2].value>>>0  & 0xff);
              memory[x[d.rs1].value+d.immediate+1] = (byte)((x[d.rs2].value>>>8) & 0xff);
             }
           };
          case Decode.f3_sw:      return d.new S("sw")
          {public void action()
             {pc = d.advance(pc);
              memory[x[d.rs1].value+d.immediate+0] = (byte)((x[d.rs2].value>>> 0) & 0xff);
              memory[x[d.rs1].value+d.immediate+1] = (byte)((x[d.rs2].value>>> 8) & 0xff);
              memory[x[d.rs1].value+d.immediate+2] = (byte)((x[d.rs2].value>>>16) & 0xff);
              memory[x[d.rs1].value+d.immediate+3] = (byte)((x[d.rs2].value>>>24) & 0xff);
             }
           };
          default:                return null;
         }
       }

      case Decode.opLoad ->                                                     // Load
       {switch(d.funct3)
         {case Decode.f3_lb:      return d.new I("lb")
           {public void action()
             {pc = d.advance(pc);
              x[d.rd].value = memory[x[d.rs1].value+d.immediate];
             }
           };
          case Decode.f3_lbu:     return d.new I("lbu")
           {public void action()
             {pc = d.advance(pc);
              x[d.rd].value = (memory[x[d.rs1].value+d.immediate]<<24)>>24;
             }
           };
          case Decode.f3_lh:      return d.new I("lh")
           {public void action()
             {pc = d.advance(pc);
              x[d.rd].value = memory[x[d.rs1].value+d.immediate] |
                             (memory[x[d.rs1].value+d.immediate+1] << 8);
             }
           };
          case Decode.f3_lhu:     return d.new I("lhu")
           {public void action()
             {pc = d.advance(pc);
              x[d.rd].value = (memory[x[d.rs1].value+d.immediate] |
                              (memory[x[d.rs1].value+d.immediate+1] << 8)<<16)>>16;
             }
           };
          case Decode.f3_lw:      return d.new I("lw")
          {public void action()
             {pc = d.advance(pc);
              x[d.rd].value = memory[x[d.rs1].value+d.immediate+0]
                            | memory[x[d.rs1].value+d.immediate+1] <<  8
                            | memory[x[d.rs1].value+d.immediate+2] << 16
                            | memory[x[d.rs1].value+d.immediate+3] << 24;
             }
           };
          default:     return null;
         }
       }

      case Decode.opJal -> {return d.new J("jal")                               // Jump and Link
       {public void action()
         {if (d.rd > 0) x[d.rd].value = pc + d.instructionSize();               // Byte address of return to next instruction
          pc += d.immediate << 1;                                               // Jump to this address
         }
       };}

      case Decode.opJalr ->                                                     // Jump and Link register
       {switch(d.funct3)
         {case Decode.f3_jalr: return d.new I("jalr")
           {public void action()
             {final int source = x[d.rs1].value;                                // Possibly the source and target registers are the same
              final int retReg = returnAddressRegister.x;                       // Return address register to check
              if (d.rd > 0)                                                     // Save byte address to return to for next instruction
               {final int ret = x[d.rd].value = pc + d.instructionSize();       // Return address
                pc = (d.rs1 > 0 ? source : 0) + d.immediate<<1;                 // Jump to this address
                if (d.rd == retReg && d.rs1 == retReg && d.immediate == 0)      // Call: jump and return on register one so we also push the return address on the return stack as writing a return address into memory makes it vulnerable to being over written.
                 {if (returnAddress.size() < returnAddressMaximum)              // Push the return address on to the return address stack for safe keeping during the execution of the called method
                   {returnAddress.push(new ReturnAddress(ret, pc));             // Record return to , called procedure entry point
                   }
                  else                                                          // The return address stack is not infinite
                   {stop("Call requested, yet return stack is full");
                   }
                 }
               }
              else if (d.rs1 == retReg && d.immediate == 0)                     // Jumping to the address in register one with the immediate field at zero indicates a return
               {if (returnAddress.size() > 0)                                   // Return
                 {final ReturnAddress ra = returnAddress.pop();                 // Expected return address
                  final int era = ra.returnTo;                                  // This is what the return address should be according to the return address stack
                  final int gra = returnAddressRegister.value;                  // Offered return address
                  if (era != gra)                                               // Expected return address differs from one offered
                   {stop("Expected return address of:", era, "but got", gra);   // Complain about corrupted return address
                   }
                  pc = gra;                                                     // Continue with acceptable return address
                 }
                else
                 {stop("Return requested, yet return stack is empty");
                 }
               }
              else                                                              // Normal jump with no intention of returning as target register is register zero
               {pc = (d.rs1 > 0 ? source : 0) + d.immediate<<1;                 // Jump to this address
               }
             }
           };
          default: return null;
         }
       }

      case Decode.opLui ->                                                      // Load upper immediate
       {return d.new U("lui")
         {public void action()
           {pc = d.advance(pc);
            if (d.rd > 0) x[d.rd].value = d.immediate << 12;
           }
         };
       }

      case Decode.opAuiPc ->                                                    // Add upper immediate to program counter
       {return d.new U("auipc")
         {public void action()
           {pc = d.advance(pc);
            x[d.rd].value = pc + d.immediate << 12;
           }
         };
       }

      case Decode.opEcall ->                                                    // Transfer call to operating system
       {return d.new I("ecall")
         {public void action()
           {pc = d.advance(pc);
            ecall.push(RiscV.this.toString());
            final int svc = x[1].value;                                         // Supervisor request
            switch(svc)                                                         // Decode supervisor request
             {case Decode.eCall_stop         -> {throw new Stop();}             // Stop requested
              case Decode.eCall_read_stdin   -> {x[1].value = in.remove(0);}    // Read an integer from stdin
              case Decode.eCall_write_stdout -> {out.push(x[2].value);}         // Write an integer to stdout
              case Decode.eCall_write_stderr -> {err.push(x[2].value);}         // Write an integer to stderr
              case Decode.eCall_trace_back   -> {writeTraceBack();}             // Write trace back
              default -> stop("Unknown supervisorcode:", svc);
             }
           }
         };
       }

      default ->  {return null;}
     }
   }

  static int encodeBImmediate(int Immediate)                                    // Encode a B format immediate operand.
   {final int i = Immediate >> 1;                                               // The immediate parameter gives us the number of bytes in the offset, but this is guaranteed to be a multiple of two see page 22, riscv-spec-20191213.pdf.

    final int a = (((i >>> 11) << 31) & Decode.B.m_31_31);
    final int b = (((i >>>  4) << 25) & Decode.B.m_30_25);
    final int c = (((i >>>  0) <<  8) & Decode.B.m_11_08);
    final int d = (((i >>> 10) <<  7) & Decode.B.m_07_07);

    return a|b|c|d;
   }

  Encode encodeB(int opCode, Register rs1, Register rs2, int subType, Label label) // Encode a B format instruction
   {return new Encode(opCode, null, rs1, rs2, 0, 0, 0, subType, 0, label);
   }

  Encode encodeI(int opCode, Register rd, Register rs1, int funct3, int Immediate) // Encode an I format instruction
   {final int i = encodeIImmediate(Immediate);
    return new Encode(opCode, rd, rs1, null, funct3, 0, 0, 0, i);
   }

  static int encodeIImmediate(int Immediate)                                    // Encode the immediate field of a I format instruction
   {return Immediate << Decode.I.p_immediate;
   }

  Encode encodeI(int opCode, Register rd, Register rs1, int funct3, Label label)// Encode an I format instruction
   {return new Encode(opCode, rd, rs1, null, funct3, 0, 0, 0, 0, label);
   }

  static int encodeJImmediate(int Immediate)                                    // Encode the immediate field of a J format instruction
   {final int i = Immediate;
    final int a = ((i >>> 19) << 31) & Decode.J.m_31_31;
    final int b =  (i >>>  0) << 21  & Decode.J.m_30_21;
    final int c = ((i >>> 10) << 20) & Decode.J.m_20_20;
    final int d = ((i >>>  0) <<  1) & Decode.J.m_19_12;
    return a|b|c|d;
   }

  Encode encodeJ(int opCode, Register rd, Label label)                          // Encode a J format instruction
   {return new Encode(opCode, rd, null, null, 0, 0, 0, 0, 0, label);
   }

  Encode encodeR(int opCode, Register rd, Register rs1, Register rs2, int funct3, int funct7)
   {return new Encode(opCode, rd, rs1, rs2, funct3, 0, funct7, 0, 0);
   }

  Encode encodeRa(int opCode, int funct5, Register rd, Register rs1, Register rs2, int funct3, int aq, int rl)
   {return new Encode(opCode, rd, rs1, rs2, funct3, funct5, 0, 0, 0, aq, rl, null);
   }

  static int encodeSImmediate(int Immediate)                                    // Encode the immediate field of an S format instruction
   {final int i = Immediate;
    final int a = ((i >>> 5) << 25) & Decode.S.m_31_25;
    final int b = ((i >>> 0) <<  7) & Decode.S.m_11_07;

    return a|b;
   }

  Encode encodeS(int opCode, Register rs1, Register rs2, int funct3, int Immediate) // Encode an S format instruction
   {final int i = encodeSImmediate(Immediate);
    return new Encode(opCode, null, rs1, rs2, funct3, 0, 0, 0, i);
   }

  Encode encodeU(int opCode, Register rd, int Immediate)                        // Encode a U format instruction
   {return new Encode(opCode, rd, null, null, 0, 0, 0, 0,
      Immediate << Decode.U.p_immediate);
   }

  Encode encodeU(int opCode, Register rd, Label label)                          // Encode a U format instruction
   {return new Encode(opCode, rd, null, null, 0, 0, 0, 0, 0, label);
   }

  static int encodeUImmediate(int Immediate)                                    // Encode the immediate field of a J format instruction
   {return Immediate << Decode.U.p_immediate;
   }

//D1 Instructions                                                               // Instructions

//D2 RV32I                                                                      // Base integer instruction set v2.1

/*
https://www.cs.sfu.ca/~ashriram/Courses/CS295/assets/notebooks/RISCV/RISCV_CARD.pdf

Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note-
add    ADD                     R 0110011 0x0    0x00        rd = rs1 + rs2
sub    SUB                     R 0110011 0x0    0x20        rd = rs1 - rs2
xor    XOR                     R 0110011 0x4    0x00        rd = rs1 Ë rs2
or     OR                      R 0110011 0x6    0x00        rd = rs1 | rs2
and    AND                     R 0110011 0x7    0x00        rd = rs1 & rs2
sll    Shift Left Logical      R 0110011 0x1    0x00        rd = rs1 << rs2
srl    Shift Right Logical     R 0110011 0x5    0x00        rd = rs1 >> rs2
sra    Shift Right Arith*      R 0110011 0x5    0x20        rd = rs1 >> rs2 msb-extends
slt    Set Less Than           R 0110011 0x2    0x00        rd = (rs1 < rs2)?1:0
sltu   Set Less Than (U)       R 0110011 0x3    0x00        rd = (rs1 < rs2)?1:0 zero-extends
*/

  Encode   add(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 0, 0);}
  Encode   sub(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 0, 2);}
  Encode   xor(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 4, 0);}
  Encode    or(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 6, 0);}
  Encode   and(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 7, 0);}
  Encode   sll(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 1, 0);}
  Encode   srl(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 5, 0);}
  Encode   sra(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 5, 2);}
  Encode   slt(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 2, 0);}
  Encode  sltu(Register rd, Register rs1, Register rs2) {return encodeR(0b011_0011, rd, rs1, rs2, 3, 0);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
addi   ADD Immediate           I 0010011 0x0                rd = rs1 + imm
xori   XOR Immediate           I 0010011 0x4                rd = rs1 Ë imm
ori    OR Immediate            I 0010011 0x6                rd = rs1 | imm
andi   AND Immediate           I 0010011 0x7                rd = rs1 & imm
slli   Shift Left Logical Imm  I 0010011 0x1 imm[5:11]=0x00 rd = rs1 << imm[0:4]
srli   Shift Right Logical Imm I 0010011 0x5 imm[5:11]=0x00 rd = rs1 >> imm[0:4]
srai   Shift Right Arith Imm   I 0010011 0x5 imm[5:11]=0x20 rd = rs1 >> imm[0:4] msb-extends
slti   Set Less Than Imm       I 0010011 0x2                rd = (rs1 < imm)?1:0
sltiu  Set Less Than Imm (U)   I 0010011 0x3                rd = (rs1 < imm)?1:0 zero-extends
*/

  Encode  addi(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x0,  immediate & 0xfff);}
  Encode  xori(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x4,  immediate & 0xfff);}
  Encode   ori(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x6,  immediate & 0xfff);}
  Encode  andi(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x7,  immediate & 0xfff);}
  Encode  slli(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x1,  immediate & 0b1_1111);}
  Encode  srli(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x5,  immediate & 0b1_1111);}
  Encode  srai(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x5, (immediate & 0b1_1111) & (2<<5));}
  Encode  slti(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x2,  immediate & 0xfff);}
  Encode sltiu(Register rd, Register rs1, int immediate) {return encodeI(0b001_0011, rd, rs1, 0x3,  immediate & 0xfff);}

  Encode  addi(Register rd, Register rs1, Label label)                          // This variant allows us to load the absolute address of a subroutine
   {if (rs1 != x0) stop("Source register must be register zero");
    label.fixed();
    if (rd != returnAddressRegister)
      stop("Target must be register:", "x"+returnAddressRegister.x);
    return encodeI(0b001_0011, rd, rs1, 0x0, label);
   }
/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
lb     Load Byte               I 0000011 0x0                rd = M[rs1+imm][0:7]
lh     Load Half               I 0000011 0x1                rd = M[rs1+imm][0:15]
lw     Load Word               I 0000011 0x2                rd = M[rs1+imm][0:31]
lbu    Load Byte (U)           I 0000011 0x4                rd = M[rs1+imm][0:7] zero-extends
lhu    Load Half (U)           I 0000011 0x5                rd = M[rs1+imm][0:15] zero-extends
*/
  Encode    lb(Register rd, Register rs1, int immediate) {return encodeI(0b000_0011, rd, rs1, 0x0, immediate & 0xff);}
  Encode    lh(Register rd, Register rs1, int immediate) {return encodeI(0b000_0011, rd, rs1, 0x1, immediate & 0xffff);}
  Encode    lw(Register rd, Register rs1, int immediate) {return encodeI(0b000_0011, rd, rs1, 0x2, immediate & 0xffff_ffff);}
  Encode   lbu(Register rd, Register rs1, int immediate) {return encodeI(0b000_0011, rd, rs1, 0x4, immediate & 0xff);}
  Encode   lhu(Register rd, Register rs1, int immediate) {return encodeI(0b000_0011, rd, rs1, 0x5, immediate & 0xffff);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
sb     Store Byte              S 0100011 0x0                M[rs1+imm][0:7] = rs2[0:7]
sh     Store Half              S 0100011 0x1                M[rs1+imm][0:15] = rs2[0:15]
sw     Store Word              S 0100011 0x2                M[rs1+imm][0:31] = rs2[0:31]
*/

  Encode    sb(Register rs1, Register rs2, int immediate) {return encodeS(0b010_0011, rs1, rs2, 0, immediate);}
  Encode    sh(Register rs1, Register rs2, int immediate) {return encodeS(0b010_0011, rs1, rs2, 1, immediate);}
  Encode    sw(Register rs1, Register rs2, int immediate) {return encodeS(0b010_0011, rs1, rs2, 2, immediate);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
beq    Branch ==               B 1100011 0x0                if(rs1 == rs2) PC += imm
bne    Branch !=               B 1100011 0x1                if(rs1 != rs2) PC += imm
blt    Branch <                B 1100011 0x4                if(rs1 < rs2)  PC += imm
bge    Branch â¥                B 1100011 0x5                if(rs1 >= rs2) PC += imm
bltu   Branch < (U)            B 1100011 0x6                if(rs1 < rs2)  PC += imm zero-extends
bgeu   Branch â¥ (U)            B 1100011 0x7                if(rs1 >= rs2) PC += imm zero-extends
*/

  Encode   beq(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 0, l);}
  Encode   bne(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 1, l);}
  Encode   blt(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 4, l);}
  Encode   bge(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 5, l);}
  Encode  bltu(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 6, l);}
  Encode  bgeu(Register rs1, Register rs2, Label l) {return encodeB(0b110_0011, rs1, rs2, 7, l);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
jal    Jump And Link           J 1101111                    rd = PC+4; PC += imm
jalr   Jump And Link Reg       I 1100111 0x0                rd = PC+4; PC  = rs1 + imm
*/

  Encode  jal (Register rd, Label l)
   {l.relative();
    return encodeJ(0b110_1111, rd,           l);
   }
  Encode  jalr(Register rd, Register rs1, Label l)
   {l.fixed();
    return encodeI(0b110_0111, rd,  rs1,  0, l);
   }
  Encode  call()                                   {return encodeI(0b110_0111, x31, x31,  0, 0);}  // The special case of jalr interpreted as "call" with a return via the subroutine return address stack
  Encode  ret ()                                   {return encodeI(0b110_0111, x0,  x31,  0, 0);}  // The special case of jalr interpreted as "return" via the subroutine return address stack

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
lui    Load Upper Imm          U 0110111                    rd = imm << 12
*/

  Encode   lui(Register rd, int immediate) {return encodeU(0b011_0111, rd, immediate & 0xfff);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
auipc  Add Upper Imm to PC     U 0010111                    rd = PC + (imm << 12)
*/

  Encode auipc(Register rd, int immediate) {return encodeU(0b001_0111, rd, immediate & 0xfff);}
  Encode auipc(Register rd, Label l)       {return encodeU(0b001_0111, rd, l);}

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
ecall  Environment Call        I 1110011 0x0 imm=0x0 Transfer control to OS
ebreak Environment Break       I 1110011 0x0 imm=0x1 Transfer control to debug
*/

  Encode  ecall() {return encodeI(0b111_0011, null, null, 0, 0);}
  Encode ebreak() {return encodeI(0b111_0011, null, null, 0, 1);}

//D1 Utility routines                                                           // Utility routines

//D1 Labels                                                                     // Labels are used to define locations in code

  class Label                                                                   // Label defining a location in code relative to the program counter register allowing for relative addressing if a relative lable, else a fixed location in program memory from the start of the program
   {final String   name;                                                        // Name of label
    final boolean fixed;                                                        // A fixed address if true
    int           offset = 0;                                                   // Address of instruction following label

    Label(String Name, boolean Fixed)                                           // Create label assumed to be just after the current instruction
     {fixed  = Fixed;
      offset = code.size();
      name   = Name+"_"+offset;                                                 // Make sure the label is unique
      labels.put(name, this);                                                   // Index label
     }

    void set() {offset = code.size();}                                          // Set a label to the current point in the code
    void fixed()    {if (!fixed) stop("Fixed label required");}
    void relative() {if ( fixed) stop("Relative label required");}
   }

  Label fixedLabel   (String name) {return new Label(name, true);}              // New fixed label
  Label relativeLabel(String name) {return new Label(name, false);}             // New label re,lative to the program counter

//D1 Variables                                                                  // Variables are symbolic names for fixed locations in memory

  class Variable                                                                // Variable/Array definition. If there is only room for one element it is an element, otherwise it is an array.
   {final String name;                                                          // Name of
    final int at;                                                               // Offset of variable either from start of memory or from start of a structure
    final int width;                                                            // Width of variable
    final int size;                                                             // Number of elements in array variable
    final int bytes;                                                            // Number of bytes in array

    Variable(String Name, int Width, int Size)                                  // Create variable
     {name = Name; width = Width; size = Size;
      pVariables = (at = pVariables) + width * size;                            // Offset of this variable
      variables.put(name, this);                                                // Save variable
      bytes = width * size;                                                     // Size of the array in bytes
     }

    int at(int i)  {return at + i * width;}                                     // Offset to an element in an array
    int at()       {return at(0);}                                              // Offset to start of variable
   }

//D1 Structured programming                                                     // Structured programming features.

  abstract class IfEq                                                           // If equal execute the "then" statement else the "else" statement
   {final Label el;
    final Label ed;

    IfEq(Register r1, Register r2)
     {el = relativeLabel("ifeq_else");
      ed = relativeLabel("ifeq_end");
      bne(r1, r2, el);
        Then();
        jal(x[0], ed);
      el.set();
        Else();
      ed.set();
     }

    abstract void Then();
    void          Else() {}
   }

  abstract class IfNe                                                           // If not equal execute the "then" statement else the "else" statement
   {final Label el;
    final Label ed;

    IfNe(Register r1, Register r2)
     {el = relativeLabel("ifne_else");
      ed = relativeLabel("ifne_end");
      beq(r1, r2, el);
        Then();
        jal(x[0], ed);
      el.set();
        Else();
      ed.set();
     }

    abstract void Then();
    void          Else() {}
   }

  abstract class IfLt                                                           // If less than execute the "then" statement else the "else" statement
   {final Label el;
    final Label ed;

    IfLt(Register r1, Register r2)
     {el = relativeLabel("iflt_else");
      ed = relativeLabel("iflt_end");
      bge(r1, r2, el);
        Then();
        jal(x[0], ed);
      el.set();
        Else();
      ed.set();
     }

    abstract void Then();
    void          Else() {}
   }

  abstract class IfGe                                                           // If greater than or equal execute the "then" statement else the "else" statement
   {final Label el;
    final Label ed;

    IfGe(Register r1, Register r2)
     {el = relativeLabel("ifge_else");
      ed = relativeLabel("ifge_end");
      blt(r1, r2, el);
        Then();
        jal(x[0], ed);
      el.set();
        Else();
      ed.set();
     }

    abstract void Then();
    void          Else() {}
   }

  abstract class Up                                                             // Increment a register from zero to a specified limit executing a body each time
   {final Label start;                                                          // Start of the for loop
    final Label next;                                                           // Next iteration of for loop
    final Label end;                                                            // End of for loop
    final Register z = x0;
    final Register count;                                                       // Count register
    final Register limit;                                                       // Limit register
    final int      step;                                                        // Step

    Up(Register Count, Register Limit, int Step)                                // Create up for loop with specified step which should be positive else +1 is used
     {count = Count; limit = Limit; step = max(1, Step);
      addi(count, z, 0);
      start = relativeLabel("upStart");
      end   = relativeLabel("upEnd");

      bge(count, limit, end);
        body();
        next = relativeLabel("upNext");
        addi(count, count, step);
        jal(z, start);
      end.set();
     }
    Up(Register Count, Register Limit)                                          // Create up for loop with step of 1
     {this(Count, Limit, 1);
     }

    abstract void body();                                                       // Body
    void Continue() {jal(z, start);}                                            // Restart this iteration
    void Next    () {jal(z, next);}                                             // Start next iteration
    void Break   () {jal(z, end);}                                              // Break out unconditionally
    void breakGe (Register b) {bge(count, b, end);}                             // Break out if the count is greater than or equal to that of the specified register
   }

  abstract class Down                                                           // Reverse for loop
   {final Label start;                                                          // Start of the for loop
    final Label next;                                                           // Next iteration of for loop
    final Label end;                                                            // End of for loop
    final Register z = x0;
    final Register count;                                                       // Count register
    final Register limit;                                                       // Limit register
    final int      step;                                                        // Step

    Down(Register Count, Register Limit, int Step)                              // Create down for loop with specified step which should be negative else -1 was is used
     {count = Count; limit = Limit; step = min(-1, Step);
      add(count, z, limit);
      addi(count, count, step);
      start = relativeLabel("downStart");
      end   = relativeLabel("downEnd");

      blt(count, z, end);
        body();
        next = relativeLabel("downNext");
        addi(count, count, step);
        jal(z, start);
      end.set();
     }

    Down(Register Count, Register Limit)                                        // Create down for loop
     {this(Count, Limit, -1);
     }

    abstract void body();                                                       // Body
    void Continue() {jal(z, start);}                                            // Restart this iteration
    void Next    () {jal(z, next);}                                             // Start next iteration
    void Break   () {jal(z, end);}                                              // Break out unconditionally
    void breakLt (Register b) {blt(count, b, end);}                             // Break out if count is less than that of the specified register
   }

  abstract class Sub                                                            // Sub routine
   {final String name;                                                          // Name of subroutine
    final Label start;                                                          // Start of subroutine
    final Label end;                                                            // End of subroutine
    final Register z = x0;

    Sub(String Name)                                                            // Create subroutine with the specified name
     {name = Name;
      end  = fixedLabel(name+"_end");
      jump(end);                                                                // Jump over subroutine code
        start = fixedLabel(name+"_start");                                      // Start of subroutine
        body();                                                                 // Body of subroutine
        ret();                                                                  // Return to caller
      end.set();                                                                // End of subroutine code
      subs.push(this);                                                          // Subroutines created
     }

    abstract void body();                                                       // Body
    final void Return() {ret();}                                                // Return from subroutine
    final void call  () {addi(x31, z, start); RiscV.this.call();}               // Call subroutine using x31 as the link register
   }

//D1 Psuedo instructions                                                        // Useful instruction variants

  void jump(Label label)                                                        // Goto the specified fixed label
   {label.fixed();
    jalr(z, z, label);
   }

  void copy(Register a, Register b)                                             // Copy register b to register a
   {add(a, z, b);
   }

  void stop()                                                                   // Stop
   {addi(x1, x0, Decode.eCall_stop);
    ecall();
   }

  void out(Register a)                                                          // Write a register to stdout
   {if (a == x1) stop("cannot use x1");
    if (a != x2) add (x2, x0, a);
    addi(x1, x0, Decode.eCall_write_stdout);
    ecall();
   }

  void err(Register a)                                                          // Write a register to stderr
   {if (a == x1) stop("cannot use x1");
    if (a != x2) add (x2, x0, a);
    addi(x1, x0, Decode.eCall_write_stderr);
    ecall();
   }

  void set(Register a, int n) {addi(a, x0, n);}                                 // Set a register
  void inc(Register a) {addi(a, a, +1);}                                        // Increment a register
  void dec(Register a) {addi(a, a, -1);}                                        // Decrement a register

//D0

  static void test_immediate_b()                                                // Immediate b
   {final int N = powerTwo(10);
    if(true)
     {final int i = 3;
      final int e = encodeBImmediate(+i*2);
      final int d = Decode.B.immediate(e);
      ok(d, +i*2);
     }

    for (int i = 0; i < N; i++)
     {final int e = encodeBImmediate(+i*2);
      final int d = Decode.B.immediate(e);
      ok(d, +i*2);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeBImmediate(-i*2);
      final int d = Decode.B.immediate(e);
      ok(d, -i*2);
     }
    ok(encodeBImmediate(-2), Decode.B.m_31_31|Decode.B.m_30_25|Decode.B.m_11_08|Decode.B.m_07_07);
   }

  static void test_immediate_j()                                                // Immediate j
   {final int N = powerTwo(19);
    for (int i = 1; i < N; i++)
     {final int e = encodeJImmediate(+i);
      final int d = Decode.J.immediate(e);
      ok(d, +i);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeJImmediate(-i);
      final int d = Decode.J.immediate(e);
      ok(d, -i);
     }
    ok(encodeJImmediate(-1), Decode.J.m_31_31|Decode.J.m_30_21|Decode.J.m_20_20|Decode.J.m_19_12);
   }

  static void test_immediate_s()                                                // Immediate s
   {final int N = powerTwo(11);
    for (int i = 1; i < N; i++)
     {final int e = encodeSImmediate(+i);
      final int d = Decode.S.immediate(e);
      ok(d, +i);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeSImmediate(-i);
      final int d = Decode.S.immediate(e);
      ok(d, -i);
     }
    ok(encodeSImmediate(-1), Decode.S.m_31_25 | Decode.S.m_11_07);
   }

  static void test_add()                                                        // Add
   {RiscV r = new RiscV();
    r.addi(r.x31, r.x0, 2);
    r.add (r.x30, r.x31, r.x31);
    ok(r.decode(r.code.elementAt(0)), "I 0010011     addi rd=31 rs1= 0        funct3= 0 imm=0x2, 2");
    ok(r.decode(r.code.elementAt(1)), "R 0110011      add rd=30 rs1=31 rs2=31 funct3= 0 funct7= 0");
    r.emulate();
    ok(r.x31, 2);
    ok(r.x30, 4);
   }

  static void test_slt()                                                        // Fibonacci
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x1;                                                          // A
    Register b = r.x2;                                                          // B
    Register c = r.x31;                                                         // C = A <  B
    Register d = r.x30;                                                         // D = A >= B

    r.addi(a, z, 10);
    r.addi(b, z, 201);
    r.slt (c, a, b);                                                            // a <  b
    r.slt (d, b, a);                                                            // a >= b
    r.emulate();                                                                // Run the program
    r.ok("""
RiscV      : slt
Step       : 5
Instruction: 16
Registers  :  x1=10 x2=201 x31=1
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: slt
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      addi    13   1  0  a   0   0  0  0  0 0          a   a00093
0001      addi    13   2  0  9   0   0  1  6  0 0         c9  c900113
0002       slt    33  1f  1  2   2   2  0  0  0 0          0   20afb3
0003       slt    33  1e  2  1   2   2  0  0  0 0          0   112f33
""");
   }

  static void test_fibonacci()                                                  // Fibonacci
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register N = r.x3;                                                          // Number of Fibonacci numbers to produce
    Register a = r.x4;                                                          // A
    Register b = r.x5;                                                          // B
    Register c = r.x6;                                                          // C = A + B
    Register i = r.x7;                                                          // Loop counter

    Variable p = r.new Variable("p", 2, 10);                                    // Variable declarations
    Variable o = r.new Variable("a", 4, 10);                                    // Block of 40 bytes starting at byte address 20
    r.addi(N, z, 10);                                                           // N = 10
    r.addi(i, z, 0);                                                            // i =  0
    r.addi(a, z, 0);                                                            // a =  0
    r.addi(b, z, 1);                                                            // b =  1
    Label start = r.relativeLabel("start");                                         // Start of for loop
    r.sb  (i, a, o.at());                                                       // Save latest result in memory
    r.addi(r.x1, z, Decode.eCall_write_stdout);                                 // Write to stdout
    r.add (r.x2, z, a);                                                         // Write to stdout variable a
    r.ecall();
    r.add (c, a, b);                                                            // Latest Fibonacci number
    r.add (a, b, z);                                                            // Move b to a
    r.add (b, c, z);                                                            // Move c to b
    r.addi(i, i, 1);                                                            // Increment loop count
    r.blt (i, N, start);                                                        // Loop

    r.addi(r.x1, z, Decode.eCall_stop);  r.ecall();                             // Request exit with x1 = 0

    r.emulate();                                                                // Run the program
    //stop(r);
    r.ok("""
RiscV      : fibonacci
Step       : 96
Instruction: 60
Registers  :  x2=34 x3=10 x4=55 x5=89 x6=89 x7=10
Memory     :  21=1 22=1 23=2 24=3 25=5 26=8 27=13 28=21 29=34
Out        : 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: fibonacci
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      addi    13   3  0  a   0   0  0  0  0 0          a   a00193
0001      addi    13   7  0  0   0   0  0  0  0 0          0      393
0002      addi    13   4  0  0   0   0  0  0  0 0          0      213
0003      addi    13   5  0  1   0   0  0  0  0 0          1   100293
0004        sb    23  14  7  4   0   0  0  0  0 0         14   438a23
0005      addi    13   1  0  2   0   0  0  0  0 0          2   200093
0006       add    33   2  0  4   0   0  0  0  0 0          0   400133
0007     ecall    73   0  0  0   0   0  0  0  0 0          0       73
0008       add    33   6  4  5   0   0  0  0  0 0          0   520333
0009       add    33   4  5  0   0   0  0  0  0 0          0    28233
000a       add    33   5  6  0   0   0  0  0  0 0          0    302b3
000b      addi    13   7  7  1   0   0  0  0  0 0          1   138393
000c       blt    63  11  7  3   4   4 1f 7f  0 0   fffffff0 fe33c8e3
000d      addi    13   1  0  0   0   0  0  0  0 0          0       93
000e     ecall    73   0  0  0   0   0  0  0  0 0          0       73
""");
    ok(r.out, "[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]");
    //ok(r.printCodeSequence(), "long[]code = {0xa00193, 0x393, 0x213, 0x100293, 0x438a23, 0x200093, 0x400133, 0x73, 0x520333, 0x28233, 0x302b3, 0x138393, 0xfe33c8e3, 0xb3, 0x73};");
   } // test_fibonacci

  static void test_lui()                                                        // Load upper immediate
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    r.lui(i, 1);
    r.sw (z, i, 0);
    r.emulate();
    r.ok("""
RiscV      : lui
Step       : 3
Instruction: 8
Registers  :  x1=4096
Memory     :  1=16
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: lui
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000       lui    37   1  0  0   1   1  0  0  0 0          1     10b7
0001        sw    23   0  0  1   2   2  0  0  0 0          0   102023
""");
   }

  static void test_auipc()                                                      // Load upper immediate plus PC
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    Label    j = r.relativeLabel("jump");

    r.add(z, z, z);
    r.auipc(i, j);
    r.sw (z, i, 0);
    j.set();
    r.emulate();
    r.ok("""
RiscV      : auipc
Step       : 4
Instruction: 12
Registers  :  x1=49152
Memory     :  1=-64
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: auipc
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000       add    33   0  0  0   0   0  0  0  0 0          0       33
0001     auipc    17   1  0  0   4   4  0  0  0 0          4     4097
0002        sw    23   0  0  1   2   2  0  0  0 0          0   102023
""");
   }

  static void test_jal()                                                        // Jump and link
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    Register j = r.x2;
    Label jump = r.relativeLabel("jump");

    r.add(z, z, z);
    r.jal (j, jump);
    r.addi(i, z, 2);                                                            // i = 2
    jump.set();
    r.emulate();                                                                // Run the program
    r.ok("""
RiscV      : jal
Step       : 3
Instruction: 12
Registers  :  x2=8
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: jal
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000       add    33   0  0  0   0   0  0  0  0 0          0       33
0001       jal    6f   2  0  8   0   0  0  0  0 0          4   80016f
0002      addi    13   1  0  2   0   0  0  0  0 0          2   200093
""");
   }                                                                            // Immediate offset is 2 x 4 bytes instructions which is stored in the immediate field as 4 = 2 x 2 bytes, which is finally resolved as an offset of 8 bytes from the start of the jal instruction

  static void test_jalr()                                                       // Jump and link register
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    Register j = r.x2;
    Label end  = r.fixedLabel("end");

    r.jalr (j, z, end);
    r.addi (i, z, 2);
    end.set();
    r.emulate();                                                                // Run the program
    r.ok("""
RiscV      : jalr
Step       : 2
Instruction: 8
Registers  :  x2=4
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: jalr
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      jalr    67   2  0  4   0   0  0  0  0 0          4   400167
0001      addi    13   1  0  2   0   0  0  0  0 0          2   200093
""");
   }

  static void test_call()                                                       // Call a subroutine
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    Register j = r.x2;
    Register c = r.x31;

    Label end   = r.relativeLabel("end");
    r.jal(z, end);
    Label start = r.fixedLabel("start");
      r.addi(j, z, 42);
      r.out(j);
      r.ret();
    end.set();

    r.addi(c, z, start);                                                        // Absolute address of subroutine
    r.call();
    r.addi(c, z, start);
    r.call();
    r.emulate();                                                                // Run the program
    //say(r.printCode());
    ok(r.out, "[42, 42]");
   }

  static void test_call_call()
   {RiscV    r = new RiscV();
    r.writeTraceBacksToStderr = false;
    Register a = r.x3;
    Register b = r.x4;

    r.set(a, 42);

    Sub s = r.new Sub("s")
     {void body()
       {r.trace();
        r.out(a);
        r.inc(a);
       }
     };

    Sub t = r.new Sub("t")
     {void body()
       {r.trace();
        r.copy(b, r.x31);
        s.call();
        r.copy(r.x31, b);
        r.trace();
        r.inc(a);
        r.out(a);
       }
     };

    t.call();
    r.emulate();

    ok(r.out, "[42, 44]");
    ok(r.traceBacks.elementAt(0), """
  To  From        Subroutine
  40   100                 t
""");
    ok(r.traceBacks.elementAt(1), """
  To  From        Subroutine
   8    60                 s
  40   100                 t
""");
    ok(r.traceBacks.elementAt(2), """
  To  From        Subroutine
  40   100                 t
""");
   }

  static void test_set_inc_dec()
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register a = r.x3;

    r.set(a, 42);
    r.out(a);
    r.inc(a);
    r.out(a);
    r.dec(a);
    r.out(a);
    r.emulate();                                                                // Run the program
    //say(r.printCode());
    ok(r.out, "[42, 43, 42]");
   }

  static void test_store_load()                                                 // Store and then load
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;
    Register j = r.x2;

    r.addi (i, z, 2);
    r.sb   (z, i, 0);
    r.sb   (z, i, 1);
    r.lh   (j, z, 0);
    r.emulate();                                                                // Run the program
    //say(r);
    r.ok("""
RiscV      : store_load
Step       : 5
Instruction: 16
Registers  :  x1=2 x2=514
Memory     :  0=2 1=2
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: store_load
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      addi    13   1  0  2   0   0  0  0  0 0          2   200093
0001        sb    23   0  0  1   0   0  0  0  0 0          0   100023
0002        sb    23   1  0  1   0   0  0  0  0 0          1   1000a3
0003        lh     3   2  0  0   1   1  0  0  0 0          0     1103
""");
   }

  static void test_ecall()                                                      // Supervisor call
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register i = r.x1;

    r.addi (i, z, 2);
    r.ecall();
    r.add  (i, i, i);
    r.emulate();
    r.ok("""
RiscV      : ecall
Step       : 4
Instruction: 12
Registers  :  x1=4
Out        : 0
""");
   //stop(r.printCode());
   ok(r.printCode(), """
RiscV Hex Code: ecall
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      addi    13   1  0  2   0   0  0  0  0 0          2   200093
0001     ecall    73   0  0  0   0   0  0  0  0 0          0       73
0002       add    33   1  1  1   0   0  0  0  0 0          0   1080b3
""");

   ok(r.ecall.firstElement(), """
RiscV      : ecall
Step       : 2
Instruction: 8
Registers  :  x1=2
""");
   }

  static void test_bubble_sort()                                                // Bubble sort an array of integers
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register q = r.x3;                                                          // Upper limit of array to be sorted
    Register p = r.x4;                                                          // Current position in array
    Register a = r.x5;                                                          // Lower element
    Register b = r.x6;                                                          // Upper element

    final int[]array = {9,3,1,2,8,6,4,7,5};                                     // Array to sort
    Variable A = r.new Variable("a", 4, array.length);                          // Array in memory
    for (int j = 0; j < array.length; j++)                                      // Load array into memory
     {r.addi(a, z, array[j]);
      r.sw  (z, a, A.at(j));
     }

    r.addi(q, z, A.bytes);                                                      // Size of array
    Label outerStart = r.relativeLabel("outerStart");                           // Outer loop
    Label outerEnd   = r.relativeLabel("outerEnd");
      r.addi(q, q, -A.width);                                                   // Each pass shortens the number of elements that still need to be sorted
      r.blt (q, z, outerEnd);                                                   // Finished pouter loop

      r.add (p, z, z);                                                          // p = 0
      Label inner = r.relativeLabel("inner");                                   // Swap loop
        r.lw  (a, p, 0);                                                        // Lower element
        r.lw  (b, p, A.width);                                                  // Upper width

        Label inOrder = r.relativeLabel("inOrder");                             // Elements in order
        r.blt(a, b, inOrder);                                                   // Jump if elements are in order
          r.sw(p, b, 0);                                                        // Swap elements
          r.sw(p, a, A.width);

        inOrder.set();
        r.addi(p, p, A.width);                                                  // Increment pointer
        r.blt (p, q, inner);                                                    // Inner loop

    r.jal (z, outerStart);                                                      // Next swap pass
    outerEnd.set();

    r.add (p, z, z);                                                            // Write sorted array
    for (int i = 0; i < array.length; i++)
     {r.lw  (r.x2, z, A.at(i));
      r.addi(r.x1, z, Decode.eCall_write_stdout);
      r.ecall();
     }

    r.addi (r.x1, z, Decode.eCall_stop);  r.ecall();                            // Stop

    r.simulationSteps(1_000_000);
    r.emulate();                                                                // Run the program
    //say(r.stdout);
    ok(r.out, "[1, 2, 3, 4, 5, 6, 7, 8, 9]");
    //say(r.printCode());                                                       // Code table
    //say(r.printCodeSequence());                                               // Code instructions
    //say(r);                                                                   // Cpu state
   } // test_bubble_sort

  static void test_insertion_sort()                                             // Insertion sort an array of integers
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register o = r.x3;                                                          // Current search position in sorted prefix
    Register p = r.x4;                                                          // Current position in array being sorted
    Register q = r.x5;                                                          // Upper limit of array to be sorted
    Register a = r.x6;                                                          // Lower element
    Register b = r.x7;                                                          // Upper element

    final int[]array = {9,3,1,2,8,6,4,7,5};                                     // Array to sort
    Variable A = r.new Variable("a", 4, array.length);                          // Array in memory
    for (int j = 0; j < array.length; j++)                                      // Load array into memory
     {r.addi(a, z, array[j]);
      r.sw  (z, a, A.at(j));
     }

    r.addi(q, z, A.bytes);                                                      // Size of array
    r.new Up(p, q, A.width)
     {void body()                                                               // Lengthen the sorted suffix
       {r.new Down(o, p, -A.width)
         {void body()                                                           // Down through sorted prefix
           {r.lw(a, o, 0);
            r.lw(b, o, A.width);
            final Down down = this;
            r.new IfLt(b, a)                                                    // Swap
             {void Then()
               {r.sw(o, b, 0);
                r.sw(o, a, A.width);
               }
              void Else()                                                       // In order - so finished
               {down.Break();
               }
             };
           }
         };
       }
     };

    r.new Up(p, q, A.width)                                                     // Write sorted array
     {void body()
       {r.lw (a, p, 0);
        r.out(a);
       }
     };

    r.stop();
    r.simulationSteps(1_000);
    r.emulate();                                                                // Run the program
    //say(r.stdout);
    ok(r.out, "[1, 2, 3, 4, 5, 6, 7, 8, 9]");
    //say(r.printCode());                                                       // Code table
    //say(r.printCodeSequence());                                               // Code instructions
    //say(r);                                                                   // Cpu state
   } // test_insertion_sort

  static void test_if_eq()                                                      // Test if equal
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x3;
    Register b = r.x4;

    r.addi(a, z, 2);
    r.addi(b, z, 2);
    r.new IfEq(a, b)
     {void Then()
       {r.addi(a, z, 11);
        r.addi(b, z, 22);
       }
      void Else()
       {r.addi(a, z, 33);
        r.addi(b, z, 44);
       }
     };

    r.addi (r.x1, z, Decode.eCall_stop);  r.ecall();                            // Stop

    r.simulationSteps(1_000_000);
    r.emulate();                                                                // Run the program
    //say(r.printCodeSequence());                                                 // Code instructions
    //say(r);                                                                     // Cpu state
    r.ok("""
RiscV      : if_eq
Step       : 8
Instruction: 40
Registers  :  x3=11 x4=22
""");
   }

  static void test_if_ne()                                                      // Test if not equal
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x3;
    Register b = r.x4;

    r.addi(a, z, 2);
    r.addi(b, z, 2);
    r.new IfNe(a, b)
     {void Then()
       {r.addi(a, z, 11);
        r.addi(b, z, 22);
       }
      void Else()
       {r.addi(a, z, 33);
        r.addi(b, z, 44);
       }
     };

    r.addi (r.x1, z, Decode.eCall_stop);  r.ecall();                            // Stop

    r.simulationSteps(1_000_000);
    r.emulate();                                                                // Run the program
    //say(r.printCodeSequence());                                                 // Code instructions
    //say(r);                                                                     // Cpu state
    r.ok("""
RiscV      : if_ne
Step       : 7
Instruction: 40
Registers  :  x3=33 x4=44
""");
   }

  static void test_if_lt()                                                      // Test if less than
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x3;
    Register b = r.x4;

    r.addi(a, z, 2);
    r.addi(b, z, 2);
    r.new IfLt(a, b)
     {void Then()
       {r.addi(a, z, 11);
        r.addi(b, z, 22);
       }
      void Else()
       {r.addi(a, z, 33);
        r.addi(b, z, 44);
       }
     };

    r.addi (r.x1, z, Decode.eCall_stop);  r.ecall();                            // Stop

    r.simulationSteps(1_000_000);
    r.emulate();                                                                // Run the program
    //say(r.printCodeSequence());                                                 // Code instructions
    //say(r);                                                                     // Cpu state
    r.ok("""
RiscV      : if_lt
Step       : 7
Instruction: 40
Registers  :  x3=33 x4=44
""");
   }

  static void test_if_ge()                                                      // Test if greater than or equal
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x3;
    Register b = r.x4;

    r.addi(a, z, 2);
    r.addi(b, z, 1);
    r.new IfGe(a, b)
     {void Then()
       {r.addi(a, z, 11);
        r.addi(b, z, 22);
       }
      void Else()
       {r.addi(a, z, 33);
        r.addi(b, z, 44);
       }
     };

    r.addi (r.x1, z, Decode.eCall_stop);  r.ecall();                            // Stop

    r.simulationSteps(1_000_000);
    r.emulate();                                                                // Run the program
    //say(r.printCodeSequence());                                                 // Code instructions
    //say(r);                                                                     // Cpu state
    r.ok("""
RiscV      : if_ge
Step       : 8
Instruction: 40
Registers  :  x3=11 x4=22
""");
   }

  static void test_up()                                                         // For loop
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register a = r.x3;
    Register b = r.x4;

    r.addi(b, z, 10);
    r.new Up(a, b)
     {void body()
       {r.out(a);
       }
     };

    r.stop();

    r.emulate();
    //stop(r.printCodeSequence());
    //stop(r.stdout);
    ok(r.out, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]");
   }

  static void test_down()                                                       // Down loop
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register a = r.x3;
    Register b = r.x4;

    r.addi(b, z, 10);
    r.new Down(a, b)
     {void body()
       {r.out(a);
       }
     };

    r.stop();

    r.emulate();
    //stop(r.printCodeSequence());
    //stop(r.stdout);
    ok(r.out, "[9, 8, 7, 6, 5, 4, 3, 2, 1, 0]");
   }

  static void test_down_break()                                                 // Down with break
   {RiscV    r = new RiscV();
    Register z = r.x0;
    Register a = r.x3;
    Register b = r.x4;
    Register c = r.x5;

    r.addi(b, z, 10);
    r.addi(c, z,  5);
    r.new Down(a, b)
     {void body()
       {r.out(a);
        breakLt(c);
       }
     };

    r.emulate();
    //stop(r.printCodeSequence());
    //stop(r.stdout);
    ok(r.out, "[9, 8, 7, 6, 5, 4]");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {if (github_actions) test_immediate_b();
    if (github_actions) test_immediate_j();
    if (github_actions) test_immediate_s();
    test_add();
    test_slt();
    test_lui();
    test_auipc();
    test_jal();
    test_jalr();
    test_call();
    test_call_call();
    test_store_load();
    test_ecall();
    test_fibonacci();
    test_bubble_sort();
    test_insertion_sort();
    test_if_eq();
    test_if_ne();
    test_if_lt();
    test_if_ge();
    test_up();
    test_down();
    test_down_break();
    test_set_inc_dec();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
