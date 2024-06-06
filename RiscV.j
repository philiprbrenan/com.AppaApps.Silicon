//------------------------------------------------------------------------------
// Execute Risc V machine code. Little endian RV32I.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

final public class RiscV                                                        // Load and execute a program written in RiscV machine code
 {final static int XLEN               = 32;                                     // Size of instructions
  final static boolean github_actions =                                         // Whether we are on a github or not
    "true".equals(System.getenv("GITHUB_ACTIONS"));
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
                 x20, x21, x22, x23, x24, x25, x26, x27, x28, x29, x30, x31;
  final byte[]                 memory = new byte[100];                          // Memory
  int                              pc = 0;                                      // Program counter
  int                           steps = 0;                                      // Number of steps taken so far in executing the program

  TreeMap<String, Label>       labels = new TreeMap<>();                       // Labels in assembler code

  RiscV(String Name)                                                            // Create a new program
   {name = Name;                                                                // Name of chip
    for (int i = 0; i < x.length; i++) x[i] = new Register(i);                  // Create the registers
    x0  = x[ 0];  x1 = x[ 1]; x2  = x[ 2]; x3  = x[ 3]; x4  = x[ 4];
    x5  = x[ 5];  x6 = x[ 6]; x7  = x[ 7]; x8  = x[ 8]; x9  = x[ 9];
    x10 = x[10]; x11 = x[11]; x12 = x[12]; x13 = x[13]; x14 = x[14];
    x15 = x[15]; x16 = x[16]; x17 = x[17]; x18 = x[18]; x19 = x[19];
    x20 = x[20]; x21 = x[21]; x22 = x[22]; x23 = x[23]; x24 = x[24];
    x25 = x[25]; x26 = x[26]; x27 = x[27]; x28 = x[28]; x29 = x[29];
    x30 = x[30]; x31 = x[31];
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
    b.append("RiscV      : " + name  + "\n");
    b.append("Step       : " + steps + "\n");
    b.append("Instruction: " + pc    + "\n");
    b.append("Registers  : ");
    for (int i = 0; i < x.length; i++)                                          // Print non zero registers
     {final int v = x[i].value;
      if (v != 0) b.append(" x"+i+"="+v);
     }
    b.append("\n");
    b.append("Memory     : ");
    for (int i = 0; i < memory.length; i++)                                     // Print non zero memory
     {final int v = memory[i];
      if (v != 0) b.append(" "+i+"="+v);
     }
    b.append("\n");
    return b.toString();                                                        // String describing chip
   }

  class Register                                                                // Description of a register
   {final int x;                                                                // Number of register
    int value;                                                                  // Current value of the register

    Register(int register)                                                      // Describe a register
     {x = register;
      if (x < 0 || x >=XLEN) stop("Register must be 0 to", XLEN,"not", x);
     }

    public String toString() {return ""+value;}                                 // Print the value of a register
   }

//D1 Simulate                                                                   // Simulate the execution of a Risc V program

  void setLabels()                                                              // Set labels so that they address the targets of branch instructions
   {final int N  = code.size();
    for (int i = 0; i < N; i++) code.elementAt(i).setLabel(i);
   }

  void simulate()                                                               // Simulate the execution of the program
   {final int actualMaxSimulationSteps =                                        // Actual limit on number of steps
      maxSimulationSteps != null ? maxSimulationSteps : defaultMaxSimulationSteps;
    final boolean miss = minSimulationSteps != null;                            // Minimum simulation steps set

    pc = 0;                                                                     // Reset
    for (int i = 0; i < x.length; i++) x[i].value = 0;                          // Clear registers
    setLabels();                                                                // Set branch instructions so they address their targets

    for (steps = 1; steps <= actualMaxSimulationSteps; ++steps)                 // Steps in time
     {if (pc >= code.size()) return;                                            // Off the end of the code
      final Encode e = code.elementAt(pc);
      final Decode d = decode(e);
      if (d == null) stop("Need data for instruction", pc);
      d.action();
     }
    if (maxSimulationSteps == null)                                             // Not enough steps available by default
     {err("Out of time after", actualMaxSimulationSteps, "steps");
     }
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

    void setLabel(int offset)                                                   // Set immediate field from any label referenced by this instruction
     {if (label == null) return;                                                // No label
      final Decode d = decode(this);                                            // Decode this instruction so we can reassemble it with the current immediate value
      final int    i = encodeBImmediate(label.offset - offset);                 // Offset to target from current instruction
      final Encode e = new Encode(d.opCode, x[d.rd], x[d.rs1], x[d.rs2],        // Encode an instruction without aq or rl or a label
        d.funct3, d.funct5, d.funct7, d.subType, i);
      instruction = e.instruction;                                              // Update current instruction with intermediate value showing offset to the target instruction
     }
   }

  class Decode                                                                  // Decode an instruction
   {final Encode instruction;                                                   // Instruction to be decoded
    final String name;                                                          // Name of instruction
    final int rd;                                                               // Destination register
    final int opCode;                                                           // Operation code
    final int funct3;                                                           // Sub function
    final int funct5;                                                           // Sub function
    final int funct7;                                                           // Sub function
    final int rs1;                                                              // Source 1 register
    final int rs2;                                                              // Source 2 register
    final int subType;                                                          // Sub type

    final static int p_opCode  =  0;                                            // Encoded position of op code
    final static int p_rd      =  7;                                            // Encoded position of destination register
    final static int p_rs1     = 15;                                            // Encoded position of source register 1
    final static int p_rs2     = 20;                                            // Encoded position of source register 2
    final static int p_funct3  = 12;                                            // Encoded position of sub function
    final static int p_funct5  = 27;                                            // Encoded position of sub function
    final static int p_funct7  = 25;                                            // Encoded position of sub function
    final static int p_subType = 12;                                            // Encoded position of sub type
    final static int p_rl      = 25;                                            // Encoded position of rl
    final static int p_aq      = 26;                                            // Encoded position of aq

    final static int m_opCode  = 0b111_1111;                                    // Mask for op code
    final static int m_rd      = 0b001_1111;                                    // Mask for destination register
    final static int m_rs1     = 0b001_1111;                                    // Mask for source register 1
    final static int m_rs2     = 0b001_1111;                                    // Mask for source register 2
    final static int m_funct3  = 0b000_0111;                                    // Mask for sub function
    final static int m_funct5  = 0b001_1111;                                    // Mask for sub function
    final static int m_funct7  = 0b111_1111;                                    // Mask for sub function
    final static int m_subType = 0b000_0111;                                    // Mask for sub type
    final static int m_rl      = 0b000_0001;                                    // Mask for rl
    final static int m_aq      = 0b000_0001;                                    // Mask for aq

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

    public void action()                                                        // Action required to implement an instruction
     {
     }

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
   }

  Decode decode(Encode e)                                                       // Decode an instruction
   {final Decode d = new Decode(null, e);
     {switch(d.opCode)
       {case 0b011_0011 ->
         {if (d.funct7 == 0)
           {switch(d.funct3)
             {case 0x0:    return new DecodeR("add",   e) {public void action() {x[rd].value = x[rs1].value +   x[rs2].value; pc++;}};
              case 0x4:    return new DecodeR("xor",   e) {public void action() {x[rd].value = x[rs1].value ^   x[rs2].value; pc++;}};
              case 0x6:    return new DecodeR("or",    e) {public void action() {x[rd].value = x[rs1].value |   x[rs2].value; pc++;}};
              case 0x7:    return new DecodeR("and",   e) {public void action() {x[rd].value = x[rs1].value &   x[rs2].value; pc++;}};
              case 0x1:    return new DecodeR("sll",   e) {public void action() {x[rd].value = x[rs1].value <<  x[rs2].value; pc++;}};
              case 0x5:    return new DecodeR("srl",   e) {public void action() {x[rd].value = x[rs1].value >>> x[rs2].value; pc++;}};
              case 0x2:    return new DecodeR("slt",   e) {public void action() {x[rd].value = x[rs1].value <   x[rs2].value ? 1 : 0; pc++;}};
              case 0x3:    return new DecodeR("sltu",  e) {public void action() {x[rd].value = x[rs1].value <   x[rs2].value ? 1 : 0; pc++;}};
              default :    return null;
             }
           }
          else if (d.funct7 == 2)
           {switch(d.funct3)
             {case 0x0:    return new DecodeI("sub",   e) {public void action() {x[rd].value = x[rs1].value -   immediate; pc++;}};
              case 0x5:    return new DecodeI("sra",   e) {public void action() {x[rd].value = x[rs1].value >>  immediate; pc++;}};
              default:     return null;
             }
           }
          else             return null;
         }

        case 0b001_0011 ->
         {switch(d.funct3)
           {case 0:      return new DecodeI("addi",  e) {public void action() {x[rd].value = x[rs1].value +   immediate; pc++;}};
            case 4:      return new DecodeI("xori",  e) {public void action() {x[rd].value = x[rs1].value ^   immediate; pc++;}};
            case 6:      return new DecodeI("ori",   e) {public void action() {x[rd].value = x[rs1].value |   immediate; pc++;}};
            case 7:      return new DecodeI("andi",  e) {public void action() {x[rd].value = x[rs1].value &   immediate; pc++;}};
            case 1:      return new DecodeI("slli",  e) {public void action() {x[rd].value = x[rs1].value <<  immediate; pc++;}};
            case 5:
              final DecodeI dI = new DecodeI(null,   e);
              switch((dI.immediate >> 5) & 0b111_1111)
               {case 0:  return new DecodeI("srli",  e) {public void action() {x[rd].value = x[rs1].value >>> immediate; pc++;}};
                case 2:  return new DecodeI("srai",  e) {public void action() {x[rd].value = x[rs1].value >>  immediate; pc++;}};
                default: return null;
               }
            case 2:      return new DecodeI("slti",  e) {public void action() {x[rd].value = x[rs1].value <   immediate ? 1 : 0; pc++;}};
            case 3:      return new DecodeI("sltiu", e) {public void action() {x[rd].value = x[rs1].value <   immediate ? 1 : 0; pc++;}};
            default:     return null;
           }
         }

        case 0b110_0011 ->
         {switch(d.funct3)
           {case 0:      return new DecodeB("beq",   e) {public void action() {if (x[rs1].value == x[rs2].value) pc += immediate; else pc++;}};
            case 1:      return new DecodeB("bne",   e) {public void action() {if (x[rs1].value != x[rs2].value) pc += immediate; else pc++;}};
            case 4:      return new DecodeB("blt",   e) {public void action() {if (x[rs1].value <  x[rs2].value) pc += immediate; else pc++;}};
            case 5:      return new DecodeB("bge",   e) {public void action() {if (x[rs1].value >= x[rs2].value) pc += immediate; else pc++;}};
            case 6:      return new DecodeB("bltu",  e) {public void action() {if (x[rs1].value <  x[rs2].value) pc += immediate; else pc++;}};
            case 7:      return new DecodeB("bgeu",  e) {public void action() {if (x[rs1].value >= x[rs2].value) pc += immediate; else pc++;}};
            default:     return null;
           }
         }

        case 0b010_0011 ->
         {switch(d.funct3)
           {case 0:      return new DecodeB("sb",    e)
             {public void action()
               {pc++;
                memory[x[rs1].value+immediate]   = (byte) (x[rs2].value & 0xff);
               }
             };
            case 1:      return new DecodeB("sh",    e)
             {public void action()
               {pc++;
                memory[x[rs1].value+immediate]   = (byte) (x[rs2].value & 0xff);
                memory[x[rs1].value+immediate+1] = (byte)((x[rs2].value>>>8) & 0xff);
               }
             };
            case 2:      return new DecodeB("sw",    e)
            {public void action()
               {pc++;
                memory[x[rs1].value+immediate+0] = (byte)((x[rs2].value>>> 0) & 0xff);
                memory[x[rs1].value+immediate+1] = (byte)((x[rs2].value>>> 8) & 0xff);
                memory[x[rs1].value+immediate+2] = (byte)((x[rs2].value>>>16) & 0xff);
                memory[x[rs1].value+immediate+3] = (byte)((x[rs2].value>>>24) & 0xff);
               }
             };
            default:     return null;
           }
         }

        case 0b000_0011 ->
         {switch(d.funct3)
           {case 0:      return new DecodeI("lb",    e)
             {public void action()
               {pc++;
                x[rd].value = memory[x[rs1].value+immediate];
               }
             };
            case 4:      return new DecodeI("lbu",   e)
             {public void action()
               {pc++;
                x[rd].value = (memory[x[rs1].value+immediate]<<24)>>24;
               }
             };
            case 1:      return new DecodeI("lh",    e)
             {public void action()
               {pc++;
                x[rd].value = memory[x[rs1].value+immediate] |
                             (memory[x[rs1].value+immediate+1] << 8);
               }
             };
            case 5:      return new DecodeI("lhu",   e)
             {public void action()
               {pc++;
                x[rd].value = (memory[x[rs1].value+immediate] |
                              (memory[x[rs1].value+immediate+1] << 8)<<16)>>16;
               }
             };
            case 2:      return new DecodeI("lw",    e)
            {public void action()
               {pc++;
                x[rd].value = memory[x[rs1].value+immediate+0]
                            | memory[x[rs1].value+immediate+1] <<  8
                            | memory[x[rs1].value+immediate+2] << 16
                            | memory[x[rs1].value+immediate+3] << 24;
               }
             };
            default:     return null;
           }
         }
/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
lb     Load Byte               I 0000011 0x0                rd = M[rs1+imm][0:7]
lh     Load Half               I 0000011 0x1                rd = M[rs1+imm][0:15]
lw     Load Word               I 0000011 0x2                rd = M[rs1+imm][0:31]
lbu    Load Byte (U)           I 0000011 0x4                rd = M[rs1+imm][0:7] zero-extends
lhu    Load Half (U)           I 0000011 0x5                rd = M[rs1+imm][0:15] zero-extends
*/

        default ->      {return null;}
       }
     }
   }

  class DecodeB extends Decode                                                  // Decode a B format instruction
   {final        int immediate;                                                 // Immediate value
    final static int m_31_31 = 0b10000000000000000000000000000000;
    final static int m_30_25 = 0b01111110000000000000000000000000;
    final static int m_11_08 = 0b00000000000000000000111100000000;
    final static int m_07_07 = 0b00000000000000000000000010000000;

    static int immediate(int Immediate)                                         // Decode a B format immediate operand
     {final int i = Immediate;

      final int a = ((i & DecodeB.m_31_31) >>> 31) << 11;
      final int b = ((i & DecodeB.m_30_25) >>> 25) <<  4;
      final int c = ((i & DecodeB.m_11_08) >>>  8) <<  0;
      final int d = ((i & DecodeB.m_07_07) >>>  7) << 10;

      return ((a|b|c|d)<<20)>>20;
     }

    DecodeB(String Name, Encode Instruction)                                    // Decode instruction
     {super(Name, Instruction);
      immediate = DecodeB.immediate(Instruction.instruction);                    // Immediate operand
     }

    public String toString()                                                    // Print instruction
     {return String.format("B %7s %8s rs1=%2d rs2=%2d imm=0x%x, %d",
        binaryString(opCode, 7), name, rs1, rs2, immediate, immediate);
     }
   }

  static int encodeBImmediate(int Immediate)                                    // Encode a B format immediate operand
   {final int i = Immediate;

    final int a = (((i >>> 11) << 31) & DecodeB.m_31_31);
    final int b = (((i >>>  4) << 25) & DecodeB.m_30_25);
    final int c = (((i >>>  0) <<  8) & DecodeB.m_11_08);
    final int d = (((i >>> 10) <<  7) & DecodeB.m_07_07);

    return a|b|c|d;
   }

  Encode encodeB(int opCode, Register rs1, Register rs2, int subType, Label label) // Encode a B format instruction
   {return new Encode(opCode, null, rs1, rs2, 0, 0, 0, subType, 0, label);
   }

  class DecodeI extends Decode                                                  // Decode an I format instruction
   {final int immediate;                                                        // Immediate value
    final static int p_immediate = 20;                                          // Position of immediate value

    DecodeI(String name, Encode Instruction)                                    // Decode instruction
     {super(name, Instruction);
      immediate = instruction.instruction >> p_immediate;                       // Immediate value
     }

    public String toString()                                                    // Print instruction
     {return String.format("I %7s %8s rd=%2d rs1=%2d        funct3=%2x imm=0x%x, %d",
        binaryString(opCode, 7), name, rd, rs1, funct3, immediate, immediate);
     }
   }

  Encode encodeI(int opCode, Register rd, Register rs1, int funct3, int Immediate) // Encode an I format instruction
   {final int i = Immediate << DecodeI.p_immediate;
    return new Encode(opCode, rd, rs1, null, funct3, 0, 0, 0, i);
   }

  class DecodeJ extends Decode                                                  // Decode a J format instruction
   {final        int immediate;                                                 // Immediate value
    final static int m_31_31 = 0b10000000000000000000000000000000;              // Masks for areas of the intermediate value
    final static int m_30_21 = 0b01111111111000000000000000000000;
    final static int m_20_20 = 0b00000000000100000000000000000000;
    final static int m_19_12 = 0b00000000000011111111000000000000;

    static int immediate(int Immediate)                                         // Decode J immediate
     {final int i = Immediate;
      final int a = ((i & DecodeJ.m_31_31) >>> 31) << 19;
      final int b = ((i & DecodeJ.m_30_21) >>> 21) <<  0;
      final int c = ((i & DecodeJ.m_20_20) >>> 20) << 10;
      final int d = ((i & DecodeJ.m_19_12) >>>  1) <<  0;

      return ((a|b|c|d)<<12)>>12;
     }

    DecodeJ(String Name, Encode Instruction)                                    // Decode an instruction
     {super(Name, Instruction);
      immediate = DecodeJ.immediate(Instruction.instruction);                    // Immediate value
     }

    public String toString()                                                    // Print instruction
     {return String.format("J %7s %8s rd=%2d imm=0x%x, %d",
        binaryString(opCode, 7), rd, name, immediate, immediate);
     }
   }

  static int encodeJImmediate(int Immediate)                                    // Encode the immediate field of a J format instruction
   {final int i = Immediate;
    final int a = ((i >>> 19) << 31) & DecodeJ.m_31_31;
    final int b =  (i >>>  0) << 21  & DecodeJ.m_30_21;
    final int c = ((i >>> 10) << 20) & DecodeJ.m_20_20;
    final int d = ((i >>>  0) <<  1) & DecodeJ.m_19_12;

    return a|b|c|d;
   }

  Encode encodeJ(int opCode, Register rd, int Immediate)                        // Encode a J format instruction
   {final int i = encodeJImmediate(Immediate);
    return new Encode(opCode, rd, null, null, 0, 0, 0, 0, i);
   }
  class DecodeR extends Decode                                                  // Decode a R format instruction
   {DecodeR(String Name, Encode Instruction)                                    // Decode instruction
     {super(Name, Instruction);
     }

    public String toString()                                                    // Print instruction
     {return String.format("R %7s %8s rd=%2d rs1=%2d rs2=%2d funct3=%2x funct7=%2x",
        binaryString(opCode, 7), name, rd, rs1, rs2, funct3, funct7);
     }
   }

  Encode encodeR(int opCode, Register rd, Register rs1, Register rs2, int funct3, int funct7)
   {return new Encode(opCode, rd, rs1, rs2, funct3, 0, funct7, 0, 0);
   }

  class DecodeRa extends Decode                                                 // Decode a R atomic format instruction
   {final boolean rl;                                                           // rl
    final boolean aq;                                                           // aq

    DecodeRa(String Name, Encode Instruction)                                   // Decode instruction
     {super(Name, Instruction);
      final int i = instruction.instruction;
      rl          = ((i >>> 25) & 0b1) == 0b1;                                  // rl
      aq          = ((i >>> 26) & 0b1) == 0b1;                                  // aq
     }

    public String toString()                                                    // Print instruction
     {return String.format("Ra %7s %8s rd=%2d rs1=%2d rs2=%2d funct3=%2x funct5=%2x aq=%d rl=%d",
        binaryString(opCode, 7), name, rd, rs1, rs2, funct3, funct5,  aq, rl);
     }
   }

  Encode encodeRa(int opCode, int funct5, Register rd, Register rs1, Register rs2, int funct3, int aq, int rl)
   {return new Encode(opCode, rd, rs1, rs2, funct3, funct5, 0, 0, 0, aq, rl, null);
   }

  class DecodeS extends Decode                                                  // Decode a S format instruction
   {final        int immediate;                                                 // Immediate value
    final static int m_31_25 = 0b11111110000000000000000000000000;
    final static int m_11_07 = 0b00000000000000000000111110000000;

    static int immediate(int Immediate)                                         // Decode the immediate field of an S format instruction
     {final int i = Immediate;
      final int a = ((i & DecodeS.m_31_25) >>> 25) << 5;
      final int b = ((i & DecodeS.m_11_07) >>>  7) << 0;

      return ((a|b)<<20)>>20;
     }

    DecodeS(String Name, Encode Instruction)                                    // Decode instruction
     {super(Name, Instruction);
      immediate = DecodeS.immediate(Instruction.instruction);                   // Immediate operand
     }

    public String toString()                                                    // Print instruction
     {return String.format("S %7s %8s rd=%2d rs1=%2d  rs2=%2d funct3=%2x imm=0x%x, %d",
        binaryString(opCode, 7), name, rd, rs1, rs2, funct3, immediate, immediate);
     }
   }

  static int encodeSImmediate(int Immediate)                                    // Encode the immediate field of an S format instruction
   {final int i = Immediate;
    final int a = ((i >>> 5) << 25) & DecodeS.m_31_25;
    final int b = ((i >>> 0) <<  7) & DecodeS.m_11_07;

    return a|b;
   }

  Encode encodeS(int opCode, Register rs1, Register rs2, int funct3, int Immediate) // Encode an S format instruction
   {final int i = encodeSImmediate(Immediate);
    return new Encode(opCode, null, rs1, rs2, funct3, 0, 0, 0, i);
   }

  class DecodeU extends Decode                                                  // Decode a U format instruction
   {final int immediate;                                                        // Immediate value

    DecodeU(String Name, Encode Instruction)                                    // Decode instruction
     {super(Name, Instruction);
      immediate = instruction.instruction >>> 12;                               // Immediate value
     }

    public String toString()                                                    // Print instruction
     {return String.format("U %7s %8s rd=%2d imm=0x%x, %d",
        binaryString(opCode, 7), rd, name, immediate, immediate);
     }
   }

  Encode encodeU(int opCode, Register rd, int Immediate)                        // Encode a U format instruction
   {return new Encode(opCode, rd, null, null, 0, 0, 0, 0, Immediate);
   }

//D1 Instructions                                                               // Instructions

//D2 RV32I                                                                      // Base integer instruction set v2.1

/*
https://www.cs.sfu.ca/~ashriram/Courses/CS295/assets/notebooks/RISCV/RISCV_CARD.pdf

Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
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

  Encode  jal (Register rd, int immediate) {return encodeJ(0b110_1111, rd, immediate);}
  Encode  jalr(Register rd, Register rs1, int immediate) {return encodeI(0b110_0111, rd, rs1, 0, immediate & 0xfff);}

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

/*
Inst   Name                  FMT Opcode  funct3 funct7      Description (C) Note
ecall  Environment Call        I 1110011 0x0 imm=0x0 Transfer control to OS
ebreak Environment Break       I 1110011 0x0 imm=0x1 Transfer control to debug
*/

  Encode  ecall() {return encodeI(0b111_0011, null, null, 0, 0);}
  Encode ebreak() {return encodeI(0b111_0011, null, null, 0, 1);}

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

//D1 Labels                                                                     // Labels are used to define locations in code

  class Label                                                                   // Label defining a location in code
   {final String name;                                                          // Name of label
    int        offset = 0;                                                      // Instruction following label if known
    Label(String Name)                                                          // Create label assumed to be just after the current instruction
     {name   = Name;
      offset = code.size();
      labels.put(name, this);                                                   // Index label
     }
    void set() {offset = code.size();}                                          // Set a label to the current point in the code
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

//D2 Conversion                                                                 // Conversion utilities

  static String binaryString(int n, int width)                                  // Convert a integer to a binary string of specified width
   {final String b = "0".repeat(width)+Long.toBinaryString(n);
    return b.substring(b.length() - width);
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
   {if (a.toString().equals(b.toString())) {++testsPassed; return;}
    final boolean n = b.toString().contains("\n");
    testsFailed++;
    if (n) err("Test failed. Got:\n"+a+"\n");
    else   err(""+'"'+a+'"', "\ndoes not equal\n"+'"'+b+'"');
    if (testsFailed > 10) stop("Failed", testsFailed, "tests");
   }

  void ok(String expected)                                                      // Compare state of machine with expected results
   {ok(toString(), expected);
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

  static void test_immediate_b()                                                // Immediate b
   {final int N = powerTwo(11);
    for (int i = 0; i < N; i++)
     {final int e = encodeBImmediate(+i);
      final int d = DecodeB.immediate(e);
      ok(d, +i);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeBImmediate(-i);
      final int d = DecodeB.immediate(e);
      ok(d, -i);
     }
    ok(encodeBImmediate(-1), DecodeB.m_31_31|DecodeB.m_30_25|DecodeB.m_11_08|DecodeB.m_07_07);
   }

  static void test_immediate_j()                                                // Immediate j
   {final int N = powerTwo(19);
    for (int i = 1; i < N; i++)
     {final int e = encodeJImmediate(+i);
      final int d = DecodeJ.immediate(e);
      ok(d, +i);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeJImmediate(-i);
      final int d = DecodeJ.immediate(e);
      ok(d, -i);
     }
    ok(encodeJImmediate(-1), DecodeJ.m_31_31|DecodeJ.m_30_21|DecodeJ.m_20_20|DecodeJ.m_19_12);
   }

  static void test_immediate_s()                                                // Immediate s
   {final int N = powerTwo(11);
    for (int i = 1; i < N; i++)
     {final int e = encodeSImmediate(+i);
      final int d = DecodeS.immediate(e);
      ok(d, +i);
     }
    for (int i = 0; i < N; i++)
     {final int e = encodeSImmediate(-i);
      final int d = DecodeS.immediate(e);
      ok(d, -i);
     }
    ok(encodeSImmediate(-1), DecodeS.m_31_25 | DecodeS.m_11_07);
   }

  static void test_add()                                                        // Add
   {RiscV r = new RiscV();
    r.addi(r.x31, r.x0, 2);
    r.add (r.x30, r.x31, r.x31);
    ok(r.decode(r.code.elementAt(0)), "I 0010011     addi rd=31 rs1= 0        funct3= 0 imm=0x2, 2");
    ok(r.decode(r.code.elementAt(1)), "R 0110011      add rd=30 rs1=31 rs2=31 funct3= 0 funct7= 0");
    r.simulate();
    ok(r.x31, 2);
    ok(r.x30, 4);
   }

  static void test_fibonacci()                                                  // Fibonacci
   {RiscV    r = new RiscV();                                                   // New Risc V machine and program
    Register z = r.x0;                                                          // Zero
    Register a = r.x1;                                                          // A
    Register b = r.x2;                                                          // B
    Register c = r.x3;                                                          // C = A + B
    Register i = r.x4;                                                          // Loop counter
    Register N = r.x5;                                                          // Number of Fibonacci numbers to produce

    r.addi(N, z, 10);                                                           // N = 10
    r.addi(i, z, 0);                                                            // i =  0
    r.addi(a, z, 0);                                                            // a =  0
    r.addi(b, z, 1);                                                            // b =  1
    Label start = r.new Label("start");                                         // Start of for loop
    r.sw  (i, a, 0);                                                            // Save latest result in memory
    r.add (c, a, b);                                                            // Latest Fibonacci number
    r.add (a, b, z);                                                            // Move b to a
    r.add (b, c, z);                                                            // Move c to b
    r.addi(i, i, 1);                                                            // Increment loop count
    r.blt (i, N, start);                                                        // Loop
    r.simulate();                                                               // Run the program
    r.ok("""
RiscV      : fibonacci
Step       : 66
Instruction: 11
Registers  :  x1=55 x2=89 x3=89 x4=10 x5=10
Memory     :  1=1 2=1 3=2 4=3 5=5 6=8 7=13 8=21 9=34
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {if (github_actions) test_immediate_b();
    if (github_actions) test_immediate_j();
    if (github_actions) test_immediate_s();
    test_add();
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
