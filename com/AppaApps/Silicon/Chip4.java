package AppaApps.Silicon.Chip;

import java.util.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

public class Chip                                                               // Describe a chip and emulate its operation.
 {final static int maxSimulationSteps = 100;                                    // Maximum simulation steps
  final static int          debugMask =   0;                                    // Adds a grid and fiber names to a mask to help debug fibers if true.
  final static int      pixelsPerCell =   4;                                    // Pixels per cell
  int                         gateSeq =   0;                                    // Gate sequence number - this allows us to display the gates in the order they were defined to simplify the understanding of drawn layouts
  int                           steps =   0;                                    // Simulation step time

  enum Operator                                                                 // Gate operations
   {And, Continue, FanOut, Gt, Input, Lt, Nand, Ngt, Nlt, Nor, Not, Nxor,
    One, Or, Output, Xor, Zero;
   }

  final String name;                                                            // Name of chip
  final Stack<Gate>              gates = new Stack<>();                         // Gates in chip
  final Map<String, Gate>  gatesByName = new TreeMap<>();                       // Gates by name
  final Map<String, Integer>  sizeBits = new TreeMap<>();                       // Sizes of bit buses
  final Map<String, WordBus> sizeWords = new TreeMap<>();                       // Sizes of word buses

  Chip(String Name)                                                             // Create a new L<chip>.
   {name = Name;                                                                // Name of chip
   }

  int nextPowerOfTwo(int n)                                                     // If this is a power of two return it, else return the next power of two greater than this number
   {int p = 1;
    for(int i = 0; i < 32; ++i, p *= 2) if (p >= n) return p;
    stop("Cannot find next power of two for", n);
    return -1;
   }

  int logTwo(int n)                                                             // Log 2 of containing power of 2
   {int p = 1;
    for(int i = 0; i < 32; ++i, p *= 2) if (p >= n) return i;
    stop("Cannot find log two for", n);
    return -1;
   }

  static int powerTwo(int n)                                                      // Power of 2
   {return 1 << n;
   }

  static String[]stackToStringArray(Stack<String> s)                            // Stack of string to array of string
   {final String[]a = new String[s.size()];
    for (int i = 0; i < s.size(); i++) a[i] = s.elementAt(i);
    return a;
   }

  int nextGateNumber()                                                          // Numbers for gates
   {return ++gateSeq;                                                           // Sequence number of gate
   }

  String nextGateName()                                                         // Create a numeric generated gate name
   {return ""+nextGateNumber();                                                 // Sequence number of gate
   }

  public String toString()                                                      // Convert chip to string
   {final StringBuilder b = new StringBuilder();
    b.append("Name      Operator  #  11111111  22222222  Input111-P=#  Input222-P=#  C Frst Last  Inputs\n");
    for(Gate g : gates) b.append(g);
    return b.toString();
   }

  class Gate                                                                    // Description of a gate
   {final int         seq = nextGateNumber();                                   // Sequence number for this gate
    final String     name;                                                      // Name of the gate.  This is also the name of the output of the gate. FanOut gates have a second output which is the name suffixed by the secondary marker. The secondary marker is not normally used in a gate name so a name that ends in ! is easily recognized as the second output of a fan out gate.
    final Operator     op;                                                      // Operation performed by gate
    String input1, input2;                                                      // Up to two inputs per gate. The name of the input is the output of another gate
    Gate         iGate1, iGate2;                                                // Gates driving the inputs of this gate
    Boolean                pin1;                                                // If true then we are being driven by output pin 1 of driving gate on input pin 1 else by output pin 2.
    Boolean                pin2;                                                // If true then we are being driven by output pin 1 of driving gate on input pin 2 else by output pin 2.
    Stack<String>       outputs = new Stack<>();                                // The names of the gates that are driven by the output of this gate
    Boolean               value;                                                // Current output value of this gate
    boolean             changed;                                                // Changed on current simulation step
    int        firstStepChanged;                                                // First step at which we changed
    int         lastStepChanged;                                                // Last step at which we changed

    String outputs()                                                            // Convert outputs to a printable string
     {final StringBuilder b = new StringBuilder();
      for(String s : outputs) b.append(s + ", ");
      if (b.length() > 0) b.delete(b.length() - 2, b.length());
      return b.toString();
     }

    public String toString()                                                    // Convert to string
     {final char v = value == null ? '.' : value ? '1' : '0';

      if (op == Operator.Input)
        return String.format("%8s  %8s  %c\n", name, op, v);

      return   String.format("%8s  %8s  %c  %8s  %8s  %8s-%c=%c  %8s-%c=%c  %c %4d %4d  ",
        name, op, v,
        input1 == null ? ""  : input1,
        input2 == null ? ""  : input2,
        iGate1 == null ? ""  : iGate1.name,
        iGate1 == null ? '.' : iGate1.pin1  == null ? '.' : iGate1.pin1  ? '1' : '2',
        iGate1 == null ? '.' : iGate1.value == null ? '.' : iGate1.value ? '1' : '0',
        iGate2 == null ? ""  : iGate2.name,
        iGate2 == null ? '.' : iGate2.pin1  == null ? '.' : iGate2.pin1  ? '1' : '2',
        iGate2 == null ? '.' : iGate2.value == null ? '.' : iGate2.value ? '1' : '0',
        changed ? '1' : '0',
        firstStepChanged,
        lastStepChanged
        ) + outputs() + "\n";
     }

    private Gate(Operator Op)                                                   // System created gate of a specified type with a unique system generated name. As yet the inputs are unknonw.
     {op = Op;
      name = ""+seq;
      gates.push(this);
      gatesByName.put(name,  this);
     }

    public Gate(Operator Op, String Name, String Input1, String Input2)         // User created gate with a user supplied name and inputs
     {name = validateName(Name);
      op = Op; input1 = Input1; input2 = Input2;
      gates.push(this);
      gatesByName.put(name, this);
     }

    void compileChip()                                                          // Check that an input value has been provided for every input pin on the gate
     {if (input1 != null && !gatesByName.containsKey(input1))                   // Input 1
        stop("No such input", input1, "for gate", name);
      if (input2 != null && !gatesByName.containsKey(input2))                   // Input 2
        stop("No such input", input1, "for gate", name);
      if (input1 != null)                                                       // Address gate driving first input
       {iGate1 = gatesByName.get(input1);
        iGate1.outputs.push(name);
       }
      if (input2 != null)                                                       // Address gate driving second gate
       {iGate2 = gatesByName.get(input2);
        iGate2.outputs.push(name);
       }
     }

    String validateName(String name)                                            // Confirm that a component name looks like a variable name and has not already been used
     {final String[]words = name.split("_");
      for (int i = 0; i < words.length; i++)
       {final String w = words[i];
        if (!w.matches("\\A([a-zA-Z][a-zA-Z0-9_.:]*|\\d+)\\Z"))
         {stop("Invalid gate name:", name, "in word", w);
         }
       }
      if (gatesByName.containsKey(name))
        stop("Gate:", name, "has already been used");
      return name;
     }

    void updateGateValue(Boolean Value)                                         // Update the value of the gate
     {changed = Value != this.value;
      this.value = Value;
     }

    void step()                                                                 // One step in the simulation
     {final Boolean g = iGate1 != null ? iGate1.value : null,
                    G = iGate2 != null ? iGate2.value : null;
      Boolean value = null;
      switch(op)                                                                // Gate operation
       {case And: if (g != null && G != null) updateGateValue(  g &&  G);  return;
        case Gt:  if (g != null && G != null) updateGateValue(  g && !G);  return;
        case Lt:  if (g != null && G != null) updateGateValue( !g &&  G);  return;
        case Nand:if (g != null && G != null) updateGateValue(!(g &&  G)); return;
        case Ngt: if (g != null && G != null) updateGateValue( !g ||  G);  return;
        case Nlt: if (g != null && G != null) updateGateValue(  g || !G);  return;
        case Nor: if (g != null && G != null) updateGateValue(!(g ||  G)); return;
        case Not: if (g != null)              updateGateValue( !g);        return;
        case Nxor:if (g != null && G != null) updateGateValue(!(g ^   G)); return;
        case One:                             updateGateValue(true);       return;
        case Or:  if (g != null && G != null) updateGateValue(  g ||  G);  return;
        case Xor: if (g != null && G != null) updateGateValue(  g ^   G);  return;
        case Zero:                            updateGateValue( false);     return;
        case Continue: case FanOut: case Output:
                                              updateGateValue(g);          return;
       }
     }

    void fanOut()                                                               // Fan out when more than two gates are driven by this gate
     {final int N = outputs.size();
      if (op == Operator.Output) return;                                        // Output gates do not fan out

      if (N == 0)                                                               // No gate references this gate
       {stop("Unused gate", name);
        return;
       }

      if (N == 1)                                                               // Only one reference to this gate, so we can use output pin 1 to satisfy it
       {final Gate f = gatesByName.get(outputs.firstElement());
        if (f.input1.equals(name)) f.pin1 = true; else f.pin2 = true;
        return;
       }

      if (N == 2)                                                               // Only one reference to this gate, so we can use output pin 1 to satisfy the first and output2 to satisfy the second
       {final Gate f = gatesByName.get(outputs.firstElement());
        final Gate l = gatesByName.get(outputs. lastElement());
        if (f.input1.equals(name)) f.pin1 = true;  else f.pin2 = true;
        if (l.input1.equals(name)) l.pin1 = false; else l.pin2 = false;
        return;
       }

      if (N % 2 == 1)                                                           // Odd number of gates driven
       {final Gate g = new Gate(Operator.FanOut);
        g.input1 = name;                                                        // The new gate refers to the original gate. It is like IMIPAK - the weapon that protects itself.
        for(int i = 0; i < N-1; ++i)
         {final Gate t = gatesByName.get(outputs.elementAt(i));
          if (t.input1 == name) t.input1 = g.name; else t.input2 = g.name;
          g.outputs.push(t.name);
         }
        g.fanOut();                                                             // The even gate might need further fan put
        return;
       }

      final Gate g = new Gate(Operator.FanOut), f = new Gate(Operator.FanOut);  // Even and greater than 2
      f.input1 = g.input1 = name;                                               // The new gates refer to the original gate
      for(int i = 0; i < N/2; ++i)                                              // Lower half
       {final Gate t = gatesByName.get(outputs.elementAt(i));
        if (t.input1.equals(name)) t.input1 = g.name; else t.input2 = g.name;
        g.outputs.push(t.name);
       }
      g.fanOut();                                                               // The lower half gate might need further fan out

      for(int i = N/2; i < N; ++i)                                              // Upper half
       {final Gate t = gatesByName.get(outputs.elementAt(i));
        if (t.input1.equals(name)) t.input1 = f.name; else t.input2 = f.name;
        f.outputs.push(t.name);
       }
      f.fanOut();                                                               // The upper half gate might need further fan out
     }
   } // Gate

  Gate FanIn(Operator Op, String Name, String...Input)                          // Normal gate - not a fan out gate
   {final int L = Input.length;
    if (L == 0)                                                                 // Input gate
      return new Gate(Op, Name, null, null);                                    // Input gates have no driving gates

    if (L == 1)                                                                 // One input
      return new Gate(Op, Name, Input[0], null);                                // Input gate have no driving gates

    if (L == 2)                                                                 // Two inputs
      return new Gate(Op, Name, Input[0], Input[1]);                            // Input gate have no driving gates

    if (L % 2 == 1)                                                             // Odd fan in
     {final Gate f = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input, 0, L-1)); // Divisible by two
      return new Gate(Op, Name, f.name, Input[L-1]);                            // Consolidation of the two gates
     }

    final Gate f = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input, 0, L/2));// Even fan out
    final Gate g = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input, L/2, L));
    return new Gate(Op, Name, f.name, g.name);                                  // Consolidation of the two gates
   }

  Gate Input    (String name)                               {return FanIn(Operator.Input,    name);}
  Gate One      (String name)                               {return FanIn(Operator.One,      name);}
  Gate Zero     (String name)                               {return FanIn(Operator.Zero,     name);}
  Gate Output   (String name, String input)                 {return FanIn(Operator.Output,   name, input);}
  Gate Continue (String name, String input)                 {return FanIn(Operator.Continue, name, input);}
  Gate Not      (String name, String input)                 {return FanIn(Operator.Not,      name, input);}

  Gate Nxor     (String name, String input1, String input2) {return FanIn(Operator.Nxor,     name, input1, input2);}
  Gate Xor      (String name, String input1, String input2) {return FanIn(Operator.Xor,      name, input1, input2);}
  Gate Gt       (String name, String input1, String input2) {return FanIn(Operator.Gt,       name, input1, input2);}
  Gate Ngt      (String name, String input1, String input2) {return FanIn(Operator.Ngt,      name, input1, input2);}
  Gate Lt       (String name, String input1, String input2) {return FanIn(Operator.Lt,       name, input1, input2);}
  Gate Nlt      (String name, String input1, String input2) {return FanIn(Operator.Nlt,      name, input1, input2);}

  Gate And      (String name, String...input)               {return FanIn(Operator.And,      name, input);}
  Gate Nand     (String name, String...input)               {return FanIn(Operator.Nand,     name, input);}
  Gate Or       (String name, String...input)               {return FanIn(Operator.Or,       name, input);}
  Gate Nor      (String name, String...input)               {return FanIn(Operator.Nor,      name, input);}

  class WordBus                                                                 // Description of a word bus
   {final int bits;                                                             // Bits in each word of the bus
    final int words;                                                            // Words in bus
    WordBus(int Words, int Bits)                                                // Create bus
     {bits = Bits; words = Words;
     }
   }

  void compileChip()                                                            // Check that an input value has been provided for every input pin on the chip.
   {for(Gate g : gates) g.outputs.clear();                                      // Clear forward references to pins that this gate drives
    for(Gate g : gates) g.compileChip();                                        // Each gate on chip
    final int N = gates.size();                                                 // Fan out will add more gates beyond the existing ones
    for(int i = 0; i < N; ++i) gates.elementAt(i).fanOut();                     // Fan the output of this gate if necessary
   }

  void noChangeGates()                                                          // Mark each gate as unchanged
   {for(Gate g : gates) g.changed = false;
   }

  boolean changes()                                                             // Check whether any changes were made
   {for(Gate g : gates) if (g.changed) return true;
    return false;
   }

  void initializeGates()                                                        // Initialize the gates ready for simulation
   {noChangeGates();
    for(Gate g : gates)
     {g.value = null; g.lastStepChanged = 0;
     }
   }

  void initializeInputGates(Inputs inputs)                                      // Initialize the output of each input gate
   {for(Gate g : gates)
     {if (g.op == Operator.Input)                                               // Input gate
       {if (inputs != null)                                                     // Initialize
         {final Boolean v = inputs.get(g.name);
          if (v != null) g.value = v;
          else stop("No input value provided for gate:", g.name);
         }
        else stop("Input gate", g.name, "has no initial value");
       }
     }
   }

  class Inputs
   {final Map<String,Boolean> inputs = new TreeMap<>();
    void set(String s, boolean value)                                               // Set the value of an input
     {if (inputs.containsKey(s)) stop("Input", s, "already defined");
      inputs.put(s, value);
     }
    Boolean get(String s)                                                       // Get the value of an input
     {if (inputs.containsKey(s)) return inputs.get(s);
      return null;
     }
   }

  void simulationStep()                                                         // One step in the simualtion
   {for(Gate g : gates)                                                        // Each gate in sub chip on definition order to get a repeatable order
     {g.step();
     }
   }

  void simulate()                                                               // Simulate the operation of a chip with no input pins. If the chip has in fact got input pins an error will be reported.
   {simulate(null);
   }

  void simulate(Inputs inputs)                                                  // Simulate the operation of a chip
   {compileChip();                                                              // Check that the inputs to each gate are defined
    initializeGates();                                                          // Set the output of each gate to null to represent unknown
    initializeInputGates(inputs);                                               // Set the outputs of each input gate
    for(steps = 1; steps < maxSimulationSteps; ++steps)                         // Steps in time
     {simulationStep();
      if (!changes())                                                           // No changes occurred
       {return;
       }
      noChangeGates();                                                          // Reset change indicators
     }

    stop("Out of time after", maxSimulationSteps, "steps");                     // Not enough steps available
   }

//D2 Buses                                                                      // A bus is an array of bits or an array of arrays of bits

  static String n(int i, String...C)                                            // Gate name from single index.
   {final String c = String.join("_", C);
    return c+"_"+i;
   }

  static String nn(int i, int j, String...C)                                    // Gate name from double index.
   {final String c = String.join("_", C);
    return c+"_"+i+"_"+j;
   }

//D3 Bits                                                                       // An array of bits that can be manipulated via one name.

  int sizeBits(String name)                                                     // Get the size of a bits bus.
   {final Integer s = sizeBits.get(name);
    if (s == null) stop("No bit bus named", name);
    return s;
   }

  void setSizeBits(String name, int bits)                                       // Set the size of a bits bus.
   {final Integer s = sizeBits.get(name);
    if (s != null) stop("A bit bus with name:", name, "has already been defined");
    sizeBits.put(name, bits);
   }

  void bit(String name, int value)                                              // Set an individual bit to true if the supplied number is non zero else false
   {if (value > 0) One(name); else Zero(name);
   }

  void bits(String name, int bits, int value)                                   // Create a bus set to a specified number.
   {final String         s = Integer.toBinaryString(value);                     // Bit in number
    final Stack<Boolean> b = new Stack<>();
    for(int i = s.length(); i > 0; --i)                                         // Stack of bits with least significant lowest
     {b.push(s.charAt(i-1) == '1' ? true : false);                              // Bits of the number
     }
    for(int i = b.size(); i <= bits; ++i) b.push(false);                        // Extend to the left with zeroes
    for(int i = 1; i <= bits; ++i)                                              // Generate constant
     {final boolean B = b.elementAt(i-1);                                       // Bit value
      if (B) One(n(i, name)); else Zero(n(i, name));                            // Set bit
     }
    setSizeBits(name, bits);                                                    // Record bus width
   }

  void inputBits(String name, int bits)                                         // Create an B<input> bus made of bits.
   {setSizeBits(name, bits);                                                    // Record bus width
    for (int b = 1; b <= bits; ++b) Input(n(b, name));                          // Bus of input gates
   }

  void outputBits(String name, String input)                                    // Create an B<output> bus made of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    for (int i = 1; i <= bits; i++) Output(n(i, name), n(i, input));            // Bus of output gates
    setSizeBits(name, bits);                                                    // Record bus width
   }

  void notBits(String name, String input)                                       // Create a B<not> bus made of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    for (int b = 1; b <= bits; ++b) Not(n(b, name), n(b, input));               // Bus of not gates
   }

  void andBits(String name, String input)                                       // B<And> and all the bits in a bus
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 0; i < bits; ++i) b[i] = n(i, input);                          // Names of bits
    And(name, b);                                                               // And all the bits in the bus
   }

  void nandBits(String name, String input)                                      // B<Nand> all the bits in a bus
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 0; i < bits; ++i) b[i] = n(i, input);                          // Names of bits
    Nand(name, b);                                                              // Nand all the bits in the bus
   }

  void orBits(String name, String input)                                        // B<Or> an input bus of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 0; i < bits; ++i) b[i] = n(i, input);                          // Names of bits
    Or(name, b);                                                                // Or all the bits in the bus
   }

  void norBits(String name, String input)                                       // B<Nor> an input bus of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 0; i < bits; ++i) b[i] = n(i, input);                          // Names of bits
    Nor(name, b);                                                                // Or all the bits in the bus
   }

  Integer bInt(String output)                                                   // Convert the bits represented by an output bus to an integer
   {final int B = sizeBits(output);                                             // Number of bits in bus
    int v = 0, p = 1;
    for (int i = 1; i <= B; i++)                                                // Each bit on bus
     {final String n = n(i, output);                                            // Name of gate supporting named bit
      final Gate g = gatesByName.get(n);                                        // Gate providing bit
      if (g.value == null) return null;                                         // Bit state not known
      if (g.value) v += p;
      p *= 2;
     }
    return v;
   }

//D3 Words                                                                      // An array of arrays of bits that can be manipulated via one name.

  WordBus sizeWords(String name)                                                // Size of a words bus.
   {final WordBus s = sizeWords.get(name);
    if (s == null) stop( "No words width specified or defaulted for word bus:", name);
    return s;
   }

  void setSizeWords(String name, int words, int bits)                           // Set the size of a bits bus.
   {final WordBus w = sizeWords.get(name);                                      // Chip, bits bus name, words, bits per word, options
    if (w != null) stop("A word bus with name:", name, "has already been defined");
    sizeWords.put(name, new WordBus(words, bits));
    for(int b = 1; b <= words; ++b) setSizeBits(n(b, name), bits);              // Size of bit bus for each word in the word bus
   }

  void words(String name, int bits, int[]values)                                // Create a word bus set to specified numbers.
   {for(int w = 1; w <= values.length; ++w)                                     // Each value to put on the bus
     {final int value = values[w-1];                                            // Each value to put on the bus
      final String  s = Integer.toBinaryString(value);                          // Bit in number
      final Stack<Boolean> b = new Stack<>();
      for(int i = s.length(); i > 0; --i)                                       // Stack of bits with least significant lowest
       {b.push(s.charAt(i-1) == '1' ? true : false);
       }
      for(int i = b.size(); i <= bits; ++i) b.push(false);                      // Extend to requested bits
      for(int i = 1; i <= bits; ++i)                                            // Generate constant
       {final boolean B = b.elementAt(i-1);                                     // Bit value
        if (B) One(nn(w, i, name)); else Zero(nn(w, i, name));                  // Set bit
       }
     }
    setSizeWords(name, values.length, bits);                                    // Record bus width
   }

  void inputWords(String name, int words, int bits)                             // Create an B<input> bus made of words.
   {for  (int w = 1; w <= words; ++w)                                           // Each word on the bus
     {for(int b = 1; b <= bits;  ++b)                                           // Each word on the bus
       {Input(nn(w, b, name));                                                  // Bus of input gates
       }
     }
    setSizeWords(name, words, bits);                                            // Record bus size
   }

  void outputWords(String name, String input)                                   // Create an B<output> bus made of words.
   {final WordBus wb = sizeWords(input);
    for  (int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
     {for(int b = 1; b <= wb.bits;  ++b)                                        // Each word on the bus
       {Output(nn(wb.words, wb.bits, name), nn(wb.words, wb.bits, input));      // Bus of output gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
   }

  void notWords(String name, String input)                                      // Create a B<not> bus made of words.
   {final WordBus wb = sizeWords(input);
    for  (int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
     {for(int b = 1; b <= wb.bits;  ++b)                                        // Each word on the bus
       {Not(nn(wb.words, wb.bits, name), nn(wb.words, wb.bits, input));         // Bus of not gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
   }

  void andWords(String name, String input)                                      // Create an B<and> bus made of words.
   {final WordBus wb = sizeWords(input);
    for  (int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
     {for(int b = 1; b <= wb.bits;  ++b)                                        // Each word on the bus
       {And(nn(w, b, name), nn(w, b, input));                                   // Bus of and gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
   }

  void andWordsX(String name, String input)                                     // B<and> a bus made of words by and-ing the corresponding bits in each word to make a single word.
   {final WordBus wb = sizeWords(input);
    for  (int b = 1; b <= wb.bits;  ++b)                                        // Each bit in the words on the bus
     {final Stack<String> words = new Stack<>();
      for(int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
       {words.push(nn(w, b, input));
       }
      final String[]inputs = words.toArray(new String[words.size()]);
      And(n(b, name), inputs);                                                  // Combine inputs using B<and> gates
     }
    setSizeBits(name, wb.bits);                                                 // Record bus size
   }

  void orWords(String name, String input)                                       // Create an B<or> bus made of words.
   {final WordBus wb = sizeWords(input);
    for  (int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
     {for(int b = 1; b <= wb.bits;  ++b)                                        // Each word on the bus
       {Or(nn(w, b, name), nn(w, b, input));                                    // Bus of or gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
   }

  void orWordsX(String name, String input)                                      // B<or> a bus made of words by or-ing the corresponding bits in each word to make a single word.
   {final WordBus wb = sizeWords(input);
    for  (int b = 1; b <= wb.bits;  ++b)                                        // Each bit in the words on the bus
     {final Stack<String> words = new Stack<>();
      for(int w = 1; w <= wb.words; ++w)                                        // Each word on the bus
       {words.push(nn(w, b, input));
       }
      final String[]inputs = words.toArray(new String[words.size()]);
      Or(n(b, name), inputs);                                                   // Combine inputs using B<or> gates
     }
    setSizeBits(name, wb.bits);                                                    // Record bus size
   }

//D1 Basic Circuits                                                             // Some well known basic circuits.

//D2 Comparisons                                                                // Compare unsigned binary integers of specified bit widths.

  void compareEq(String output, String a, String b)                             // Compare two unsigned binary integers of a specified width returning B<1> if they are equal else B<0>.  Each integer is supplied as a bus.
   {final int A = sizeBits(a);                                                  // Width of first bus
    final int B = sizeBits(b);                                                  // Width of second bus
    if (A != B) stop("Input",  a, "has width", A, "but input", b, "has width", B); // Check buses match in size
    final String eq = nextGateName();                                           // Set of gates to test equality of corresponding bits
    final String[]n = new String[B];                                            // Equal bit names
    for (int i = 0; i <  B; i++) n[i] = n(i, eq);                               // Load array with equal bit names
    for (int i = 1; i <= B; i++) Nxor(n[i-1], n(i, a), n(i, b));                // Test each bit pair for equality
    And(output, n);                                                             // All bits must be equal
   }

  void compareGt(String output, String a, String b)                             // Compare two unsigned binary integers for greater than.
   {final int A = sizeBits(a);
    final int B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A, ", but second input bus", b, "has width", B);
    for (int i = 2; i <= B; i++) Nxor(n(i, output, "e"), n(i, a), n(i, b));     // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Gt  (n(i, output, "g"), n(i, a), n(i, b));     // Test each bit pair for more than

    for(int j = 2; j <= B; ++j)                                                  // More than on one bit and all preceding bits are equal
     {final Stack<String> and = new Stack<>();
      and.push(n(j-1, output, "g"));
      for (int i = j; i <= B; i++) and.push(n(i, output, "e"));
      And(n(j, output, "c"), stackToStringArray(and));
     }

    final Stack<String> or = new Stack<>();
    for (int i = 2; i <= B; i++) or.push(n(i, output, "c"));                    // Equals  followed by greater than
                                 or.push(n(B, output, "g"));
    Or(output, stackToStringArray(or));                                         // Any set bit indicates that first is greater then second
   }

  void compareLt(String output, String a, String b)                             // Compare two unsigned binary integers for less than.
   {final int A = sizeBits(a);
    final int B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A, ", but second input bus", b, "has width", B);
    for (int i = 2; i <= B; i++) Nxor(n(i, output, "e"), n(i, a), n(i, b));     // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Lt  (n(i, output, "l"), n(i, a), n(i, b));     // Test each bit pair for more than

    for(int j = 2; j <= B; ++j)                                                  // More than on one bit and all preceding bits are equal
     {final Stack<String> and = new Stack<>();
      and.push(n(j-1, output, "l"));
      for (int i = j; i <= B; i++) and.push(n(i, output, "e"));
      And(n(j, output, "c"), stackToStringArray(and));
     }

    final Stack<String> or = new Stack<>();
    for (int i = 2; i <= B; i++) or.push(n(i, output, "c"));                    // Equals  followed by less than
                                 or.push(n(B, output, "l"));
    Or(output, stackToStringArray(or));                                         // Any set bit indicates that first is less than second
   }

  void chooseFromTwoWords(String output, String a, String b, String choose)     // Choose one of two words based on a bit.  The first word is chosen if the bit is B<0> otherwise the second word is chosen.
   {final int A = sizeBits(a);
    final int B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A, ", but second input bus", b, "has width", B);

    final String notChoose = nextGateName();                                    // Opposite of choice
    Not(notChoose, choose);                                                     // Invert choice

    for (int i = 1; i <= B; i++)                                                // Each bit
     {And(n(i, output, "a"), n(i, a), notChoose);                               // Choose first word if not choice
      And(n(i, output, "b"), n(i, b),    choose);                                // Choose second word if choice
      Or (n(i, output),      n(i, output, "a"), n(i, output, "b"));             // Or results of choice
     }
    setSizeBits(output, B);                                                     // Record bus size
   }

  void enableWord(String output, String a, String enable)                       // Output a word or zeros depending on a choice bit.  The first word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
   {final int A = sizeBits(a);

    for (int i = 1; i <= A; i++)                                                // Each bit
     {And(n(i, output), n(i, a), enable);                                       // Choose each bit of input word
     }
    setSizeBits(output, A);                                                     // Record bus size
   }

//D2 Masks                                                                      // Point masks and monotone masks. A point mask has a single B<1> in a sea of B<0>s as in B<00100>.  A monotone mask has zero or more B<0>s followed by all B<1>s as in: B<00111>.

  void monotoneMaskToPointMask(String output, String input)                     // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
   {final int B = sizeBits(input);                                              // Number of bits in input monotone mask

    for (int i = 1; i <= B;  i++)                                               // Each bit in each possible output number
     {if (i > 1) Lt(n(i, output), n(i-1, input), n(i, input));                  // Look for a step from 0 to 1
      else Continue(n(i, output),                n(i, input));                  // First bit is 1 so point is in the first bit
     }

    setSizeBits(output, B);                                                     // Size of resulting bus representing the chosen integer
   }

  void chooseWordUnderMask(String output, String input, String mask)            // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
   {final WordBus wb = sizeWords(input);
    final int mi     = sizeBits (mask);
    if (mi != wb.words) stop("Mask width", mi, "does not match number of words ", wb.words);

    for   (int w = 1; w <= wb.words; ++w)                                       // And each bit of each word with the mask
     {for (int b = 1; b <= wb.bits;  ++b)                                       // Bits in each word
       {And(nn(w, b, output, "a"), n(w, mask), nn(w, b, input));
       }
     }

    for   (int b = 1; b <= wb.bits; ++b)                                           // Bits in each word
     {final Stack<String> or = new Stack<>();
      for (int w = 1; w <= wb.words; ++w) or.push(nn(w, b, output, "a"));                                       // And each bit of each word with the mask
      Or(n(b, output), stackToStringArray(or));
     }
    setSizeBits(output, wb.bits);
   }

  static void say(Object...O)
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) {b.append(" "); b.append(o);}
    System.err.println((O.length > 0 ? b.substring(1) : ""));
   }

  static void out(Object...O)
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) {b.append(" "); b.append(o);}
    System.out.println((O.length > 0 ? b.substring(1) : ""));
   }

  static void stop(Object...O)
   {say(O);
    new Exception().printStackTrace();
    System.exit(1);
   }

  public static void main(String[] args)                                        // Test if called as a program
   {test_and();
    test_or();
    test_zero();
    test_one();
    test_andOr();
    test_expand();
    test_expand2();
    test_outputBits();
    test_gt(); test_gt2();
    test_lt(); test_lt2();
    test_compareEq();
    test_compareGt();
    test_compareLt();
    test_chooseFromTwoWords();
    test_enableWord();
    test_monotoneMaskToPointMask();
    test_chooseWordUnderMask();
    say("Passed ALL", testsPassed, "tests");
   }

  static void test_and()
   {final Chip c   = new Chip("And");
    final Gate i1  = c.Input ("i1");
    final Gate i2  = c.Input ("i2");
    final Gate and = c.And   ("and", "i1", "i2");
    final Gate o   = c.Output("o", "and");
    final Inputs inputs = c.new Inputs();
    inputs.set("i1", true);
    inputs.set("i2", false);
    c.simulate(inputs);
    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, false);
    ok(  o.value, false);
    ok(  c.steps ,     2);
   }

  static void test_or()
   {final Chip c = new Chip("Or");
    final Gate i1  = c.Input ("i1");
    final Gate i2  = c.Input ("i2");
    final Gate and = c.Or    ("or", "i1", "i2");
    final Gate o   = c.Output("o", "or");
    final Inputs inputs = c.new Inputs();
    inputs.set("i1", true);
    inputs.set("i2", false);
    c.simulate(inputs);
    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, true);
    ok(  o.value, true);
    ok(  c.steps,     2);
   }

  static void test_zero()
   {final Chip c = new Chip("Zero");
    c.Zero("z");
    final Gate o = c.Output("o", "z");
    c.simulate();
    ok(c.steps,  2);
    ok(o.value, false);
   }

  static void test_one()
   {final Chip c = new Chip("One");
    c.One ("O");
    final Gate o = c.Output("o", "O");
    c.simulate();
    ok(c.steps  , 2);
    ok(o.value , true);
   }

  static void test_and3a(boolean A, boolean B, boolean C, boolean D)
   {final Chip c = new Chip("And3");
    c.Input( "i11");
    c.Input( "i12");
    c.And(  "and1", "i11", "i12");
    c.Input( "i21");
    c.Input( "i22");
    c.And(  "and2", "i21", "i22");
    c.Or(     "or", "and1", "and2");
    final Gate o = c.Output( "o", "or");

    final Inputs i = c.new Inputs();
    i.set("i11", A);
    i.set("i12", B);
    i.set("i21", C);
    i.set("i22", D);

    c.simulate(i);

    ok(c.steps,  2);
    ok(o.value, (A && B) || (C && D));
   }

  static void test_andOr()
   {final boolean t = true, f = false;
     test_and3a(t, t, t, t);
     test_and3a(t, t, t, f);
     test_and3a(t, t, f, t);
     test_and3a(t, t, f, f);
     test_and3a(t, f, t, t);
     test_and3a(t, f, t, f);
     test_and3a(t, f, f, t);
     test_and3a(t, f, f, f);

     test_and3a(f, t, t, t);
     test_and3a(f, t, t, f);
     test_and3a(f, t, f, t);
     test_and3a(f, t, f, f);
     test_and3a(f, f, t, t);
     test_and3a(f, f, t, f);
     test_and3a(f, f, f, t);
     test_and3a(f, f, f, f);
   }

  static void test_expand()
   {final Chip c = new Chip("Expand");
    c.One ("one");
    c.Zero("zero");
    c.Or  ("or",   "one", "zero");
    c.And ("and",  "one", "zero");
    final Gate o1 = c.Output("o1", "or");
    final Gate o2 = c.Output("o2", "and");
    c.simulate();
    ok(c.steps,   2);
    ok(o1.value, true);
    ok(o2.value, false);
   }

  static void test_expand2()
   {final Chip c = new Chip("Expand");
    c.One ("one");
    c.Zero("zero");
    c.Or  ("or",   "one", "zero");
    c.And ("and",  "one", "zero");
    c.Xor ("xor",  "one", "zero");
    final Gate o1 = c.Output("o1", "or");
    final Gate o2 = c.Output("o2", "and");
    final Gate o3 = c.Output("o3", "xor");
    c.simulate();
    ok(c.steps,   2);
    ok(o1.value, true);
    ok(o2.value, false);
    ok(o3.value, true);
   }

  static void test_outputBits()
   {final int N = 4, N2 = powerTwo(N);
    for  (int i = 0; i < N2; i++)
     {final Chip c = new Chip("Output Bits");
      c.bits("c", N, i);
      c.outputBits("o", "c");
      c.simulate();
      ok(c.steps,      2);
      ok(c.bInt("o"), i);
     }
   }

  static void test_gt()
   {final Chip c = new Chip("Gt");
    c.One ("o");
    c.Zero("z");
    c.Gt("gt", "o", "z");
    final Gate o = c.Output("O", "gt");
    c.simulate();
    ok(c.steps,     2);
    ok(o.value, true);
   }

  static void test_gt2()
   {final Chip c = new Chip("Gt");
    c.One ("o");
    c.Zero("z");
    c.Gt("gt", "z", "o");
    final Gate o = c.Output("O", "gt");
    c.simulate();
    ok(c.steps,     2);
    ok(o.value, false);
   }

  static void test_lt()
   {final Chip c = new Chip("Lt");
    c.One ("o");
    c.Zero("z");
    c.Lt("lt", "o", "z");
    final Gate o = c.Output("O", "lt");
    c.simulate();
    ok(c.steps,     2);
    ok(o.value, false);
   }

  static void test_lt2()
   {final Chip c = new Chip("Lt");
    c.One ("o");
    c.Zero("z");
    c.Lt("lt", "z", "o");
    final Gate o = c.Output("O", "lt");
    c.simulate();
    ok(c.steps,     2);
    ok(o.value, true);
   }

  static void test_compareEq()
   {for(int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B)-1;
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareEq");
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareEq("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, 2);
          ok(o.value, i == j);
         }
       }
     }
   }

  static void test_compareGt()
   {final  int B = 2, B2 = powerTwo(B)-1;
    for   (int i = 0; i < B2; ++i)
     {for (int j = 0; j < B2; ++j)
       {final var c = new Chip("CompareGt");
        c.bits("i", B, i);
        c.bits("j", B, j);
        c.compareGt("o", "i", "j");
        final Gate o = c.Output("O", "o");
        c.simulate();
        ok(c.steps, 2);
        ok(o.value, i > j);
       }
     }
   }

  static void test_compareLt()
   {final  int B = 2, B2 = powerTwo(B)-1;
    for   (int i = 0; i < B2; ++i)
     {for (int j = 0; j < B2; ++j)
       {final var c = new Chip("CompareLt");
        c.bits("i", B, i);
        c.bits("j", B, j);
        c.compareLt("o", "i", "j");
        final Gate o = c.Output("O", "o");
        c.simulate();
        ok(c.steps, 2);
        ok(o.value, i < j);
       }
     }
   }

  static void test_chooseFromTwoWords()
   {for (int i = 0; i < 2; i++)
     {final int B = 4;
      final var c = new Chip("CompareLt");
      c.bits("a", B,  3);
      c.bits("b", B, 12);
      c.bit ("c", i);
      c.chooseFromTwoWords("o", "a", "b", "c");
      c.outputBits("out",  "o");
      c.simulate();
      ok(c.steps, 2);
      ok(c.bInt("out"), i == 0 ? 3 : 12);
     }
   }

  static void test_enableWord()
   {for (int i = 0; i < 2; i++)
     {final int B = 4;
      final var c = new Chip("EnableWord");
      c.bits("a", B,  3);
      c.bit ("e", i);
      c.enableWord("o", "a", "e");
      c.outputBits("out",  "o");
      c.simulate();
      ok(c.steps, 2);
      ok(c.bInt("out"), i == 0 ? 0 : 3);
     }
   }

  static void test_monotoneMaskToPointMask()
   {for(int B = 2; B <= 4; ++B)
     {final int N = powerTwo(B);
      for (int i = 1; i <= N; i++)
       {final var c = new Chip("monotoneMaskToPointMask");
        c.setSizeBits("i", N);
        for (int j = 1; j <= N; j++) c.bit(n(j, "i"), j < i ? 0 : 1);
        c.monotoneMaskToPointMask("o", "i");
        c.outputBits("out", "o");
        c.simulate();
        //say(B, i, String.format("%x", c.bInt("i")), String.format("%x", c.bInt("out")));
        ok(c.bInt("out"), powerTwo(i-1));
       }
     }
   }

  static void test_chooseWordUnderMask()
   {final int B = 3, B2 = powerTwo(B);
    final int[]numbers =  {4, 3, 2, 1, 0, 1, 2, 3};
    for (int i = 0; i < B2; i++)
     {final var c = new Chip("chooseWordUnderMask");
      c.words("i", B,  numbers);
      c.bits ("m", B2, powerTwo(i));
      c.chooseWordUnderMask("o", "i", "m");
      c.outputBits("out", "o");
      c.simulate();
      ok(c.bInt("out"), numbers[i]);
     }
   }

  static int testsPassed = 0;

  static void ok(Object a, Object b)
   {if (a.equals(b)) ++testsPassed;
    else
     {stop(a, "does not equal", b);
     }
   }
 }
