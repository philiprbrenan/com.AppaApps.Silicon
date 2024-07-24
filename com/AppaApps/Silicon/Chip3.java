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

    void checkInputs()                                                          // Check that an input value has been provided for every input pin on the gate
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
       {say("Unused gate", name);
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

  void checkInputs()                                                            // Check that an input value has been provided for every input pin on the chip.
   {for(Gate g : gates) g.outputs.clear();                                      // Clear forward references to pins that this gate drives
    for(Gate g : gates) g.checkInputs();                                        // Each gate on chip
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
   {checkInputs();                                                              // Check that the inputs to each gate are defined
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
      for(int i = b.size(); i <= bits; ++i) b.push(false);
      for(int i = 1; i <= b.size(); ++i)                                        // Extend to requested bits
       {b.push(s.charAt(i-1) == '1' ? true : false);
       }
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

//  void pointMaskToInteger(String output, String input)                          // Convert a mask B<i> known to have at a single bit on - also known as a B<point mask> - to an output number B<a> representing the one based location in the mask of the bit set to B<1>.  If the mask is
//   {final int B = sizeBits(input), B2 = logTwo(B);                              // Number of bits in mask and resulting integer
//
//    for (int j = 1; j <= B; j++) bits(n(j, output, "n"), B2, j-1);              // Each possible output number
//
//    for (int i = 1; i <= B2;  i++)                                              // Each bit in each possible output number
//     {final Stack<String> or = new Stack<>();
//      for (int j = 1; j <= B; j++)
//       {And(nn(i, j, output, "a"), n(j, input), nn(j, i, output, "n"));         // And horizontally with selection bit
//        or.push(nn(i, j, output, "a"));                                         // Or and results to produce selected number
//       }
//      Or(n(i, output), stackToStringArray(or));
//     }
//    setSizeBits(output, B2);                                                    // Size of resulting bus representing the chosen integer
//   }
//
//  void integerToPointMask(String output, String input)                          // Convert an integer B<i> of specified width to a point mask B<m>. If the input integer is B<0> then the mask is all zeroes as well.
//   {final int B = sizeBits(input), B2 = powerTwo(B);                            // Number of bits in integer, in resulting mask
//
//    for (int j = 1; j <= B2; j++)                                               // Each possible output number
//     {bits(n(j, output, "n"), B, j-1);                                          // Generate possible number
//      compareEq(n(j, output), n(j, output, "n"), input);                        // Compare each possible number with the input
//     }
//    setSizeBits(output, B2);                                                    // Size of output bus
//   }
//
//  void monotoneMaskToInteger(String output, String input)                       // Convert a monotone mask B<i> to an output number B<r> representing the location in the mask of the lowest bit set to B<1>. If no such bit exists in the point then output in B<r> is B<0>. A monotone mask starts at the lowest bit and proceeds to the highest bit. A monotone mask is created by setting all the bits in a bus to zero, then choosing a bit and making that bit and all the more significant bits B<1>.
//   {final int B = sizeBits(input), B2 = logTwo(B);                              // Number of bits in mask, in resulting integer
//
//    for (int j = 1; j <= B; j++) bits(n(j, output, "n"), B2, j-1);              // Each possible output number
//
//    for (int i = 1; i <= B2;  i++)                                              // Each bit in each possible output number
//     {final Stack<String> or = new Stack<>();
//      for (int j = 1; j <= B; j++)
//       {if (j > 1) Lt(nn(i, j, output, "p"), n(j-1, input), n(j, input));       // Look for a step from 0 to 1
//        else Continue(nn(i, j, output, "p"),                n(j, input));       // First bit is 1 so point is in the first bit
//
//        And(nn(i, j, output, "a"), nn(i, j, output, "p"), nn(j, i, output, "n"));// And horizontally with selection bit
//        or.push(nn(i, j, output, "a"));                                         // Or and results to produce selected number
//       }
//      Or(n(i, output), stackToStringArray(or));
//     }
//    setSizeBits(output, B2);                                                    // Size of resulting bus representing the chosen integer
//   }

  void monotoneMaskToPointMask(String output, String input)                     // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
   {final int B = sizeBits(input);                                              // Number of bits in input monotone mask

    for (int i = 1; i <= B;  i++)                                               // Each bit in each possible output number
     {if (i > 1) Lt(n(i, output), n(i-1, input), n(i, input));                  // Look for a step from 0 to 1
      else Continue(n(i, output),                n(i, input));                  // First bit is 1 so point is in the first bit
     }

    setSizeBits(output, B);                                                     // Size of resulting bus representing the chosen integer
   }

//  void integerToMonotoneMask(String output, String input)                       // Convert an integer B<i> of specified width to a monotone mask B<m>. If the input integer is B<0> then the mask is all zeroes.  Otherwise the mask has B<i-1> leading zeroes followed by all ones thereafter.
//   {final int B = sizeBits(input), B2 = powerTwo(B);                            // Number of bits in integer, in resulting mask
//
//    for (int j = 1; j <= B2; j++)                                               // Each possible output number
//     {bits(n(j, output, "n"), B, j);                                            // Generate possible number
//      if (j < B2) compareGt(n(j, output), n(j, output, "n"), input);            // Compare each possible number with the input
//      else              One(n(j, output));                                      // Final one
//     }
//    setSizeBits(output, B2);                                                    // Size of output bus
//   }

/*

  Chip chooseWordUnderMask(%)                                                  // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
 {final String (chip, output, input, mask, %options) = @_;                            // Chip, output, inputs, mask, options
  @_ >= 3 or confess "Three or more parameters";
  final String o = output;

  final String (words, bits) = sizeWords(chip, input);
  final String (mi)           = sizeBits (chip, mask);
  mi == words or confess <<"END" =~ s/\n(.)/ 1/gsr;
Mask width mi does not match number of words words.
END

  for   final String w(1..words)                                                        // And each bit of each word with the mask
   {for final String b(1..bits)                                                         // Bits in each word
     {chip.and(nn("o.a", w, b), [n(mask, w), nn(input, w, b)]);
     }
   }

  for   final String b(1..bits)                                                         // Bits in each word
   {chip.or(n(o, b), [map {nn("o.a", _, b)} 1..words]);
   }
  setSizeBits(chip, o, bits);

  chip
 }

  Chip findWord(%)                                                             // Choose one of a specified number of words B<w>, each of a specified width, using a key B<k>.  Return a point mask B<o> indicating the locations of the key if found or or a mask equal to all zeroes if the key is not present.
 {final String (chip, output, key, words, %options) = @_;                             // Chip, found point mask, key, words to search, options
  @_ >= 4 or confess "Four or more parameters";
  final String o = output;
  final String (W, B) = sizeWords(chip, words);
  final String bits    = sizeBits (chip, key);
  B == bits or confess <<"END" =~ s/\n(.)/ 1/gsr;
Number of bits in each word B differs from words in key bits.
END
  for   final String w(1..W)                                                            // Input words
   {chip.compareEq(n(o, w), n(words, w), key);                           // Compare each input word with the key to make a mask
   }
  setSizeBits(chip, o, W);

  chip
 }
*/

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
    //test_pointMaskToInteger();
    //test_integerToPointMask();
    //test_monotoneMaskToInteger();
    test_monotoneMaskToPointMask();
    //test_integerToMonotoneMask();
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

  //static void test_pointMaskToInteger()
  // {for(int B = 2; B <= 4; ++B)
  //   {final int N = powerTwo(B);
  //    for (int i = 1; i <= N; i++)
  //     {final var c = new Chip("pointMaskToInteger");
  //      c.setSizeBits("i", N);
  //      for (int j = 1; j <= N; j++) c.bit(n(j, "i"), j == i ? 1 : 0);
  //      c.pointMaskToInteger("o", "i");
  //      c.outputBits("out", "o");
  //      c.simulate();
  //      ok(c.bInt("out"), i-1);
  //     }
  //   }
  // }
  //
  //static void test_integerToPointMask()
  // {for(int B = 2; B <= 4; ++B)
  //   {final int N = powerTwo(B);
  //    for (int i = 1; i <= N; i++)
  //     {final var c = new Chip("integerToPointMask");
  //      c.bits("i", B, i-1);
  //      c.integerToPointMask("o", "i");
  //      c.outputBits("out", "o");
  //      c.simulate();
  //     }
  //   }
  // }
  //
  //static void test_monotoneMaskToInteger()
  // {for(int B = 2; B <= 4; ++B)
  //   {final int N = powerTwo(B);
  //    for (int i = 1; i <= N; i++)
  //     {final var c = new Chip("pointMaskToInteger");
  //      c.setSizeBits("i", N);
  //      for (int j = 1; j <= N; j++) c.bit(n(j, "i"), j < i ? 0 : 1);
  //      c.monotoneMaskToInteger("o", "i");
  //      c.outputBits("out", "o");
  //      c.simulate();
  //      ok(c.bInt("out"), i-1);
  //     }
  //   }
  // }

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
        say(B, i, String.format("%x", c.bInt("i")), String.format("%x", c.bInt("out")));
        ok(c.bInt("out"), powerTwo(i-1));
       }
     }
   }

//  static void test_integerToMonotoneMask()
//   {for(int B = 2; B <= 4; ++B)
//     {final int N = powerTwo(B);
//      for (int i = 1; i <= N; i++)
//       {final var c = new Chip("integerToMonotoneMask");
//        c.bits("i", B, i-1);
//        c.integerToMonotoneMask("o", "i");
//        c.outputBits("out", "o");
//        c.simulate();
//        say(B, i, c.bInt("i"), c.bInt("out"));
//       }
//     }
//   }

  static int testsPassed = 0;

  static void ok(Object a, Object b)
   {if (a.equals(b)) ++testsPassed;
    else
     {stop(a, "does not equal", b);
     }
   }
 }

/*
//D2 Connect                                                                     // Connect input buses to other buses.

sub connectInput($$$%)                                                          // Connect a previously defined input gate to the output of another gate on the same chip. This allows us to define a set of gates on the chip without having to know, first, all the names of the gates that will provide input to these gates.
 {my ($chip, $in, $to, %options) = @_;                                          // Chip, input gate, gate to connect input gate to, options
  @_ >= 3 or confess "Three or more parameters";
  my $gates = $chip->gates;
  my $i = $$gates{$in};
  defined($i) or confess "No definition of input gate $in";
  $i->type =~ m(\Ainput\Z) or confess "No definition of input gate $in";
  $i->inputs = {1=>$to};
  $chip
 }

sub connectInputBits($$$%)                                                      // Connect a previously defined input bit bus to another bit bus provided the two buses have the same size.
 {my ($chip, $in, $to, %options) = @_;                                          // Chip, input gate, gate to connect input gate to, options
  @_ >= 3 or confess "Three or more parameters";
  my $I = sizeBits($chip, $in);
  my $T = sizeBits($chip, $to);
  $I == $T or confess <<"END" =~ s/\n(.)/ $1/gsr;
Mismatched bits bus width: input has $I bits but output has $T bits.
END
  connectInput($chip, n($in, $_), n($to, $_)) for 1..$I;
  $chip
 }

sub connectInputWords($$$%)                                                     // Connect a previously defined input word bus to another word bus provided the two buses have the same size.
 {my ($chip, $in, $to, %options) = @_;                                          // Chip, input gate, gate to connect input gate to, options
  @_ >= 3 or confess "Three or more parameters";
  my ($iw, $ib) = sizeWords($chip, $in);
  my ($tw, $tb) = sizeWords($chip, $to);
  $iw == $tw or confess <<"END" =~ s/\n(.)/ $1/gsr;
Mismatched words bus: input has $iw words but output has $tw words
END
  $ib == $tb or confess <<"END" =~ s/\n(.)/ $1/gsr;
Mismatched bits width of words bus: input has $ib bits but output has $tb bits
END
  connectInputBits($chip, n($in, $_), n($to, $_)) for 1..$iw;
  $chip
 }

//D1 Visualize                                                                   // Visualize the L<chip> in various ways.

my sub orderGates($%)                                                           // Order the L<lgs> on a L<chip> so that input L<lg> are first, the output L<lgs> are last and the non io L<lgs> are in between. All L<lgs> are first ordered alphabetically. The non io L<lgs> are then ordered by the step number at which they last changed during simulation of the L<chip>.  This has the effect of placing all the buses in the upper right corner as there are no loops.
 {my ($chip, %options) = @_;                                                    // Chip, options

  my $gates = $chip->gates;                                                     // Gates on chip
  my @i; my @n; my @o;

  for my $G(sort {$$gates{$a}->seq <=> $$gates{$b}->seq} keys %$gates)          // Dump each gate one per line in definition order
   {my $g = $$gates{$G};
    push @i, $G if $g->type =~ m(\Ainput\Z)i;
    push @n, $G if $g->type !~ m(\A(in|out)put\Z)i;
    push @o, $G if $g->type =~ m(\Aoutput\Z)i;
   }

  if (my $c = $options{changed})                                                // Order non IO gates by last change time during simulation if possible
   {@n = sort {($$c{$a}//0) <=> ($$c{$b}//0)} @n;
   }

  (\@i, \@n, \@o)
 }

sub print($%)                                                                   // Dump the L<lgs> present on a L<chip>.
 {my ($chip, %options) = @_;                                                    // Chip, gates, options
  my $gates  = $chip->gates;                                                    // Gates on chip
  my $values = $options{values};                                                // Values of each gate if known
  my @s;
  my ($i, $n, $o) = orderGates $chip, %options;                                 // Gates by type
  for my $G(@$i, @$n, @$o)                                                      // Dump each gate one per line
   {my $g = $$gates{$G};
    my %i = $g->inputs ? $g->inputs->%* : ();

    my $p = sub                                                                 // Instruction name and type
     {my $v = $$values{$G};                                                     // Value if known for this gate
      my $o = $g->name;
      my $t = $g->type;
      return sprintf "%-32s: %3d %-32s", $o, $v, $t if defined($v);             // With value
      return sprintf "%-32s:     %-32s", $o,     $t;                            // Without value
     }->();

    if (my @i = map {$i{$_}} sort keys %i)                                      // Add actual inputs in same line sorted in input pin name
     {$p .= join " ", @i;
     }
    push @s, $p;
   }
  my $s = join "\n", @s, '';                                                    // Representation of gates as text
  owf fpe($options{print}, q(txt)), $s if $options{print};                      // Write representation of gates as text to the named file
  $s
 }

sub Silicon::Chip::Simulation::print($%)                                        // Print simulation results as text.
 {my ($sim, %options) = @_;                                                     // Simulation, options
  $sim->chip->print(%options, values=>$sim->values);
 }

my sub paca(&$$%)                                                               // Arrange the second array to minimize the average value of the function between the two arrays
 {my ($metric, $A, $B, %options) = @_;                                          // Metric function between a pair of elements drawn from each array, the first array, the second array, options
  my $N = @$B / @$A;                                                            // Amount of duplication of A required to reach B
  my @C = map {int($_ / $N)} keys @$B;                                          // Spread out A to match B
  my @p = (undef) x @$B;                                                        // Proposed ordering for B
  for  my $j(keys @$B)                                                          // Place each element of B
   {my $b = $$B[$j];                                                            // Element of B
    my $c; my $D;                                                               // Index of best position
    for my $i(keys @C)                                                          // Each indexed position
     {next if defined $p[$i];                                                   // Position already filled
      my $d = &$metric($$A[$C[$i]], $b);                                        // Distance from b to a
      next unless defined $d;                                                   // Infinity if the metric function returns L<undef>
      next if defined($c) and $d > $D;                                          // Worse then the current best
      $c = $i; $D = $d;                                                         // Closest entry in A for this B so far
     }
    $p[$c] = $j;                                                                // Position as closely as possible
   }
  map {$$B[$_]} @p                                                              // Reorder B to make its entries match those of A as closely as possible
 }

my sub distance($$$$)                                                           // Manhattan distance between two points
 {my ($x, $y, $X, $Y) = @_;                                                     // Start x, start y, end x, end y
  abs($X - $x) + abs($Y - $y)
 }

my sub layoutSquare($%)                                                         // Layout the gates approximately a a square relying on multiple layers abovethe gates to carry the connections.
 {my ($chip, %options) = @_;                                                    // Chip, options
  my $gates      = $chip->gates;                                                // Gates on chip
  my $changed    = $options{changed};                                           // Step at which gate last changed in simulation
  my $values     = $options{values};                                            // Values of each gate if known
  my $route      = $options{route}    // '';                                    // Routing methodology
  my $minHeight  = $options{minHeight};                                         // Minimum Y dimension of chip
  my $newChange  = $options{newChange};                                         // Start a new column every time we reach a new change level
  my $dx         = $options{spaceDx}  //= 0;                                    // Spacing in x between gates
  my $dy         = $options{spaceDy}  //= 1;                                    // Spacing in y between gates
  my $borderDx   = $options{borderDx} //= 0;                                    // Make sure there is some space at the edge of the chip so that gates are not squashed up against it and made difficult to route as a consequence
  my $borderDy   = $options{borderDy} //= 2;                                    // Make sure there is some space at the edge of the chip so that gates are not squashed up against it and made difficult to route as a consequence
  my $gsx        = $options{gsx}      //= 2;                                    // Integer scale factor to apply to x dimension of gates on the chip - by spreading out the pins we make them more accessible.
  my $gsy        = $options{gsy}      //= 2;                                    // Integer scale factor to apply to y dimension of gates on the chip - by spreading out the pins we make them more accessible.

  $dx *= $gsx * pixelsPerCell;                                                  // Intergate spacing in steps related to gate size
  $dy *= $gsy * pixelsPerCell;

  $borderDx *= $gsx * pixelsPerCell;                                            // Border spacing in steps related to gate size
  $borderDy *= $gsy * pixelsPerCell;

  my $GATE_WIDTH = 2 * $gsx*pixelsPerCell;   ### Improve!  We assume this is the width of all gates but this unlikely to be true in general.
  my $DX         = $GATE_WIDTH + $dx;                                           // Width of each column of gates

  my sub position(%)                                                            // Position of a gate
   {my (%options) = @_;                                                         // Options
    genHash(__PACKAGE__."::LayoutSquarePosition",                               // Details of position
      changed    => $options{changed},                                          // Step at which gate first changed
      x          => $options{x},                                                // X position of left upper corner of gate
      y          => $options{y},                                                // Y position of left upper corner of gate
      h          => $options{height},                                           // Height of gate
      w          => $options{width},                                            // Width of gate
      Gate       => $options{Gate},                                             // Definition of gate. Capital G because of a slight default in L<Data::Table::Text::updateDocumentation> that reports a duplicate attribute if we use lowercase g.
      changeStep => 0,                                                          // True if we are stepping from one change level to the next
      bottomUp   => 0,                                                          // Normally the gates are laid out top down but sometimes it is better to lay them out bottom up
     );
   }

  my @positions;                                                                // Gate positions
  for my $G(sort keys %$gates)                                                  // Gates
   {my $g = $$gates{$G};                                                        // Gate
    my $c = $$changed{$G} // -1;                                                // Change time. Set change time of input gates to -1 to show they were changed before the simulation started.
    my $h = max(scalar(keys $g->inputs->%*), scalar(keys $g->output->@*)) || 1; // Every gate occupies at least one cell vertically
    push @positions, my $p = position(Gate=>$g, changed=>$c,                    // Gates are assumed to be two wide so that the inputs do overlay the outputs
      width=>$GATE_WIDTH, height=>$h*$gsy*pixelsPerCell);
   }

  @positions = sort {$a->changed <=> $b->changed} @positions;                   // Sort so that gates are in temporal order

  my $dimension  = 0;                                                           // Count of all the pins to get an idea of how big the chip will be
     $dimension += ($_->w+$dx) * ($_->h + $dy) for @positions;                  // Each pin
  my $dimensionY = sqrt($dimension);                                            // Approximate Y dimension of the chip assuming we can extend as needed in X which is more conveniebt to work with because monitors tend to have more pixels in X than in Y.
     $dimensionY = int($dimensionY+1);                                          // Round to a convenient integer greater than zero
     $dimensionY = ($minHeight // $dimensionY) + 2 * $borderDy;                 // Use user supplied width if available else estimate a reasonable size.

  my ($width, $height) = sub                                                    // Position each gate and return the dimensions of the chip in cells
   {my $ni;                                                                     // First non input gate
    for my $p(@positions)                                                       // Place each gate
     {$ni = $p unless defined($ni) or $p->Gate->type =~ m(\Ainput\Z);           // First non input gate
     }

    for my $i(1..$#positions)                                                   // First gate at each new change level
     {my $p = $positions[$i];
      $p->changeStep = $p->changed != $positions[$i-1]->changed;
     }

    my sub nextX($)                                                             // Next position that can contain a via along an x crossbar
     {my ($x) = @_;                                                             // x
      for my $i(0..$gsx*&pixelsPerCell)
       {my $X = $x + $i;
        return $X if $X % ($gsx*&pixelsPerCell) == 0;                            // Next via in X
       }
     }

    my sub nextY($)                                                             // Next position that can contain a via along a y crossbar
     {my ($y) = @_;                                                             // y
      for my $i(0..$gsy*&pixelsPerCell)
       {my $Y = $y + $i;
        return $Y if $Y % ($gsy*&pixelsPerCell) == 0;                            // Next via in Y
       }
     }

    my $X = nextX($borderDx); my $Y = nextY($borderDy); my $mY;                 // Current x,y position, maximum Y. I tried starting a new column every time the "changed" field changes thinking this would induce fewer wiring levels, it did for simple chips but made things slightly worse for Btree.

    for my $p(@positions)                                                       // Place each gate
     {my $g = $p->Gate;
      if ($Y + $p->h > $dimensionY or $ni && $ni == $p or                       // Next column if last column would be overly full.  Gates are assumed to require two columns so we can separate their inputs and outputs.  This is not required for input and output pins so some further improvements might be possible here.
        $newChange && $p->changeStep)                                           // Start a new column every time we reach a new change level
       {$X = nextX($X + $DX); $Y = nextY($borderDy);                            // Position of new column
       }
      $p->x = $X; $p->y = $Y;                                                   // Position gate
      $Y += nextY($p->h + $dy);                                                 // Position of next gate
      $mY = maximum $Y, $mY                                                     // Maximum Y position
     }
   ($X + $GATE_WIDTH + $borderDx, $mY + $gsy*&pixelsPerCell - 1)                // Chip dimensions
   }->();

#  for my $x(map {$DX * $_} 1..int($width/$DX))                                 // Reorder each column of gates to make each gate as close as possible to the preceding gate from which it derives its inputs
#   {my @a; my @b;                                                              // Preceding column and current column
#    for my $p(@positions)                                                      // Each position
#     {push @a, $p if $p->x == $x - $DX;                                        // Gate is in preceding column
#      push @b, $p if $p->x == $x;                                              // Gate is current column
#     }
#    next unless @a and @b;
#
#    my @c = paca                                                                // Reorder the current column to reduce the wiring needed to reach the next set of gates
#     {my ($p, $q) = @_;
#      my %i = map {$_=>1} values $q->Gate->inputs->%*;                          // Gates driving this gate
#      exists($i{$p->Gate->name}) ? 1 : 0;                                       // Gate is being driven by a gate in the preceding column
#     } \@a, \@b;
#
#    my $Y = 0;
#    for my $p(@c)                                                               // Reposition gates in column to locate them as closely as possible to their driving gates
#     {$p->y = $Y;                                                               // Gate in column
#      $Y += $p->h + $dy;                                                        // Reposition gate
#     }
#   }

  my $l = sub                                                                   // Position each gate on the chip
   {my $l = Silicon::Chip::Layout::new(width=>$width, height=>$height);         // Create chip

    for my $p(@positions)                                                       // Place each gate
     {$l->gate(%$p, w=>$p->w, h=>$p->h, t=>$p->Gate->type, l=>$p->Gate->name);
     }

    $l->svg(%options, file=>$options{svg}) if $options{svg};                    // Draw an svg representation of the gates

    $l
   }->();

  my %outPins = outPins $chip, %options;                                        // Map the output pins to gates

  my @wires = sub                                                               // The wires we need to route
   {my @W;                                                                      // Wire positions and lengths
    my %positions = map {$positions[$_]->Gate->name => $_} keys @positions;     // Index of positioned gates

    for my $p(@positions)                                                       // Place each gate
     {next if $p->Gate->type eq "input";                                        // Draw from inputs to outputs
      my %i = $p->Gate->inputs->%*;                                             // Inputs
      my @i = sort keys %i;                                                     // Inputs

      my @w1; my @w2;                                                           // Two ways of wiring things up
      for my $I(keys @i)                                                        // Each input
       {my $dp = $i{$i[$I]};                                                    // Name of pin driving this input
        my ($dg, $do) = $outPins{$dp}->@*;                                      // Gate and number of driving pin
        my $i  = $positions[$positions{$dg->name}];                             // Position of driving gate
#       my $x  = $i->x + $i->w - 1;                                             // Position of output pin
        my $x  = $i->x + $gsx*pixelsPerCell;                                    // Position of output pin
        my $y  = $i->y + $do*$gsy*pixelsPerCell;                                // Position of driving pin
        my $X  = $p->x;                                                         // Input pin X position
#       my $Y1 = $p->y+$p->h-$I*$gsy - 1;                                       // Connect this gates inputs bottom up
#       my $Y1 = $p->y+$p->h-$I*$gsy*pixelsPerCell;                             // Connect this gates inputs bottom up
        my $Y2 = $p->y+      $I*$gsy*pixelsPerCell;                             // Connect this gates inputs top  down
#say STDERR "AAAA ", dump($X, $Y1);
#say STDERR "BBBB ", dump($X, $Y2);
#        push @w1, [$x, $y, $X, $Y1, abs($X-$x)+abs($Y1-$y)];                    // Wire details bottom up because all our gates are commutative
        push @w2, [$x, $y, $X, $Y2, abs($X-$x)+abs($Y2-$y)];                    // Wire details top down  because all our gates are commutative
       }
#      my $l1 = 0; $l1 += $$_[4] for @w1;                                        // Length of wires bottom up
      my $l2 = 0; $l2 += $$_[4] for @w2;                                        // Length of wires top down
#      my $bu = $p->bottomUp = $l1 < $l2;                                        // Bottom up is shorter
#     push @W, $bu ? @w1 : @w2;
      push @W, @w2;
     }
    sort {$$b[4] <=> $$a[4]} @W                                                 // Wires ordered longest to shortest on the basis that short wires are more routable. Run time for short to long routing takes 4 times longer long to short using the tests in this file.
#   sort {$$a[4] <=> $$b[4]} @W                                                 // Wires ordered shortest to longest
   }->();

  my $w = Silicon::Chip::Wiring::new(width=>$width, height=>$height, %options); // Wiring

  for my $p(@wires)                                                             // Place each gate
   {my ($x, $y, $X, $Y) = @$p;                                                  // Draw from inputs to outputs
    my $c = $w->wire($x, $y, $X, $Y, %options);                                         # Create the wire
   }
  $w->layout(%options);

  $w->svg(%options, file=>$options{svg})  if $options{svg};                     // Draw an svg representation of the wiring between the gates
  $chip->gds2($l, $w, svg=>$options{svg}) if $options{svg};                     // Draw as GDS2 for transmission to fab.

  genHash(__PACKAGE__."::LayoutSquare",                                         // Details of layout and wiring
    chip           => $chip,                                                    // Chip being masked
    positions      => \@positions,                                              // Positions array
    layout         => $l,                                                       // Layout
    wiring         => $w,                                                       // Wiring
    changed        => $changed,                                                 // Changed at step in simulation
    values         => $values,                                                  // Final values
    height         => $height,                                                  // Height of layout
    width          => $width,                                                   // Width of layout
   );
 }

//D2 GDS2                                                                        // Graphic Design System Layout for a chip which can be visualized using KLayout.

sub gds2($$$%)                                                                  //P Draw the chip using GDS2
 {my ($chip, $layout, $wiring, %options) = @_;                                  // Chip, layout, wiring, options
  my $delta  = 1/4;                                                             // Offset from edge of each gate cell
  my $wireWidth  = 1/4;                                                         // Width of a wire

  confess "gdsOut required" unless my $outGds = $options{svg};                  // Output file required
  my $outFile = createEmptyFile(fpe qw(gds), $outGds, qw(gds));                 // Create output file to make any folders needed

  my $g = new GDS2(-fileName=>">$outFile");                                     // Draw as Graphics Design System 2
  $g->printInitLib(-name=>$outGds);
  $g->printBgnstr (-name=>$outGds);

# This code to layout the gates ought to be moved to Silicon::Chip::Layout
  my @l = $layout->gates->@*;                                                   // Layout the gates level
  for my $l(@l)
   {my ($x, $y, $h, $w, $n) = @$l{qw(x y h w l)};
    $g->printBoundary(-layer=>0, -xy=>[$x, $y,  $x+$w-3/4, $y,  $x+$w-3/4, $y+$h-3/4, $x, $y + $h-3/4]);
    $g->printText(-string=>$n,   -xy=>[$x+$w/2, $y+$h/2], -font=>3) if $n;      // Y coordinate
   }

  $wiring->gds2(block=>$g);                                                     // Draw wiring

  $g->printEndstr;
  $g->printEndlib();                                                            // Close the library
 }

//D1 Basic Circuits                                                              // Some well known basic circuits.

sub n(*$)                                                                       // Gate name from single index.
 {my ($c, $i) = @_;                                                             // Gate name, bit number
  !@_ or !ref($_[0]) or confess <<"END";
Call as a sub not as a method
END
  "${c}_$i"
 }

sub nn(*$$)                                                                     // Gate name from double index.
 {my ($c, $i, $j) = @_;                                                         // Gate name, word number, bit number
  !@_ or !ref($_[0]) or confess confess <<"END";
Call as a sub not as a method
END
 "${c}_${i}_$j"
 }

//D2 Comparisons                                                                 // Compare unsigned binary integers of specified bit widths.

sub compareEq($$$$%)                                                            // Compare two unsigned binary integers of a specified width returning B<1> if they are equal else B<0>.
 {my ($chip, $output, $a, $b, %options) = @_;                                   // Chip, name of component also the output bus, first integer, second integer, options
  @_ >= 4 or confess "Four or more parameters";
  my $o  = $output;
  my $A = sizeBits($chip, $a);
  my $B = sizeBits($chip, $b);
  $A == $B or confess <<"END" =~ s/\n(.)/ $1/gsr;
Input $a has width $A but input $b has width $B
END
  $chip->nxor(n("$o.e", $_), n($a, $_), n($b, $_)) for 1..$B;                   // Test each bit pair for equality
  $chip->andBits($o, "$o.e", bits=>$B);                                         // All bits must be equal

  $chip
 }

sub compareGt($$$$%)                                                            // Compare two unsigned binary integers and return B<1> if the first integer is more than B<b> else B<0>.
 {my ($chip, $output, $a, $b, %options) = @_;                                   // Chip, name of component also the output bus, first integer, second integer, options
  @_ >= 4 or confess "Four or more parameters";
  my $o  = $output;
  my $A = sizeBits($chip, $a);
  my $B = sizeBits($chip, $b);
  $A == $B or confess <<"END" =~ s/\n(.)/ $1/gsr;
Input $a has width $A but input $b has width $B
END
  $chip->nxor (n("$o.e", $_), n($a, $_), n($b, $_)) for 2..$B;                  // Test all but the lowest bit pair for equality
  $chip->gt   (n("$o.g", $_), n($a, $_), n($b, $_)) for 1..$B;                  // Test each bit pair for more than

  for my $b(2..$B)                                                              // More than on one bit and all preceding bits are equal
   {$chip->and(n("$o.c", $b),
     {(map {$_=>n("$o.e", $_)} $b..$B), ($b-1)=>n("$o.g", $b-1)});
   }

  $chip->or   ($o, {$B=>n("$o.g", $B),  (map {($_-1)=>n("$o.c", $_)} 2..$B)});  // Any set bit indicates that B<a> is more than B<b>

  $chip
 }

sub compareLt($$$$%)                                                            // Compare two unsigned binary integers B<a>, B<b> of a specified width. Output B<out> is B<1> if B<a> is less than B<b> else B<0>.
 {my ($chip, $output, $a, $b, %options) = @_;                                   // Chip, name of component also the output bus, first integer, second integer, options
  @_ >= 4 or confess "Four or more parameters";

  my $A = sizeBits($chip, $a);
  my $B = sizeBits($chip, $b);
  $A == $B or confess <<"END" =~ s/\n(.)/ $1/gsr;
Input $a has width $A but input $b has width $B
END

  my $o = $output;

  $chip->nxor (n("$o.e", $_), n($a, $_), n($b, $_)) for 2..$B;                  // Test all but the lowest bit pair for equality
  $chip->lt   (n("$o.l", $_), n($a, $_), n($b, $_)) for 1..$B;                  // Test each bit pair for less than

  for my $b(2..$B)                                                              // More than on one bit and all preceding bits are equal
   {$chip->and(n("$o.c", $b),
     {(map {$_=>n("$o.e", $_)} $b..$B), ($b-1)=>n("$o.l", $b-1)});
   }

  $chip->or   ($o, {$B=>n("$o.l", $B),  (map {($_-1)=>n("$o.c", $_)} 2..$B)});  // Any set bit indicates that B<a> is less than B<b>

  $chip
 }

sub enableWord($$$$%)                                                           // Output a word or zeros depending on a choice bit.  The first word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
 {my ($chip, $output, $a, $enable, %options) = @_;                              // Chip, name of component also the chosen word, the first word, the second word, the choosing bit, options
  @_ >= 4 or confess "Four or more parameters";
  my $o = $output;
  my $B = sizeBits($chip, $a);

  for my $i(1..$B)                                                              // Choose each bit of input word
   {$chip->and(n($o, $i), [n($a, $i), $enable]);
   }
  setSizeBits($chip, $o, $B);                                                   // Record bus size
  $chip
 }

//D2 Masks                                                                       // Point masks and monotone masks. A point mask has a single B<1> in a sea of B<0>s as in B<00100>.  A monotone mask has zero or more B<0>s followed by all B<1>s as in: B<00111>.

sub pointMaskToInteger($$$%)                                                    // Convert a mask B<i> known to have at most a single bit on - also known as a B<point mask> - to an output number B<a> representing the location in the mask of the bit set to B<1>. If no such bit exists in the point mask then output number B<a> is B<0>.
 {my ($chip, $output, $input, %options) = @_;                                   // Chip, output name, input mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $B = sizeBits($chip, $input);                                              // Bits in input bus
  my $I = containingPowerOfTwo($B);                                             // Bits in integer
  my $i = $input;                                                               // The bits in the input mask
  my $o = $output;                                                              // The name of the output bus

  my %b;
  for my $b(1..$B)                                                              // Bits in mask to bits in resulting number
   {my $s = sprintf "%b", $b;
    for my $p(1..length($s))
     {$b{$p}{$b}++ if substr($s, -$p, 1);
     }
   }

  for my $b(sort keys %b)
   {$chip->or(n($o, $b), {map {$_=>n($i, $_)} sort keys $b{$b}->%*});           // Bits needed to drive a bit in the resulting number
   }
  setSizeBits $chip, $o, $I;                                                    // Size of resulting integer
  $chip
 }

sub integerToPointMask($$$%)                                                    // Convert an integer B<i> of specified width to a point mask B<m>. If the input integer is B<0> then the mask is all zeroes as well.
 {my ($chip, $output, $input, %options) = @_;                                   // Chip, output name, input mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $bits = sizeBits($chip, $input);                                           // Size of input integer in bits
  my $B = 2**$bits-1;
  my $o = $output;                                                              // Output mask

  $chip->notBits("$o.n", $input);                                               // Not of each input

  for my $b(1..$B)                                                              // Each bit of the mask
   {my @s = reverse split //, sprintf "%0${bits}b", $b;                         // Bits for this point in the mask
    my %a;
    for my $i(1..@s)
     {$a{$i} = n($s[$i-1] ? 'i' : "$o.n", $i);                                  // Combination of bits to enable this mask bit
     }
    $chip->and(n($output, $b), {%a});                                           // And to set this point in the mask
   }
  setSizeBits($chip, $output, $B);                                              // Size of output bus

  $chip
 }

sub monotoneMaskToInteger($$$%)                                                 // Convert a monotone mask B<i> to an output number B<r> representing the location in the mask of the bit set to B<1>. If no such bit exists in the point then output in B<r> is B<0>.
 {my ($chip, $output, $input, %options) = @_;                                   // Chip, output name, input mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $B = sizeBits($chip, $input);
  my $I = containingPowerOfTwo($B);
  my $o = $output;

  my %b;
  for my $b(1..$B)
   {my $s = sprintf "%b", $b;
    for my $p(1..length($s))
     {$b{$p}{$b}++ if substr($s, -$p, 1);
     }
   }
  $chip->not     (n("$o.n", $_), n($input, $_)) for 1..$B-1;                    // Not of each input
  $chip->continue(n("$o.a", 1),  n($input, 1));
  $chip->and     (n("$o.a", $_), [n("$o.n", $_-1), n('i', $_)]) for 2..$B;      // Look for trailing edge

  for my $b(sort keys %b)
   {$chip->or    (n($o, $b), [map {n("$o.a", $_)} sort keys $b{$b}->%*]);       // Bits needed to drive a bit in the resulting number
   }
  setSizeBits($chip, $o, $I);

  $chip
 }

sub monotoneMaskToPointMask($$$%)                                               // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
 {my ($chip, $output, $input, %options) = @_;                                   // Chip, output name, input mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $o = $output;
  my $bits = sizeBits($chip, $input);
  $chip->continue(n($o, 1), n($input, 1));                                      // The first bit in the monotone mask matches the first bit of the point mask
  for my $b(2..$bits)
   {$chip->xor(n($o, $b), n($input, $b-1), n($input, $b));                      // Detect transition
   }
  setSizeBits($chip, $o, $bits);

  $chip
 }

sub integerToMonotoneMask($$$%)                                                 // Convert an integer B<i> of specified width to a monotone mask B<m>. If the input integer is B<0> then the mask is all zeroes.  Otherwise the mask has B<i-1> leading zeroes followed by all ones thereafter.
 {my ($chip, $output, $input, %options) = @_;                                   // Chip, output name, input mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $I = sizeBits($chip, $input);
  my $B = 2**$I-1;
  my $o = $output;

  $chip->notBits("$o.n", $input);                                               // Not of each input

  for my $b(1..$B)                                                              // Each bit of the mask
   {my @s = (reverse split //, sprintf "%0${I}b", $b);                          // Bits for this point in the mask
    my %a;
    for  my $i(1..@s)
     {$a{$i} = n($s[$i-1] ? $input : "$o.n", $i);                               // Choose either the input bit or the not of the input but depending on the number being converted to binary
     }
    $chip->and(n("$o.a", $b), {%a});                                            // Set at this point and beyond
    $chip-> or(n($o, $b), [map {n("$o.a", $_)} 1..$b]);                         // Set mask
   }
  setSizeBits($chip, $o, $B);
  $chip
 }

sub chooseWordUnderMask($$$$%)                                                  // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
 {my ($chip, $output, $input, $mask, %options) = @_;                            // Chip, output, inputs, mask, options
  @_ >= 3 or confess "Three or more parameters";
  my $o = $output;

  my ($words, $bits) = sizeWords($chip, $input);
  my ($mi)           = sizeBits ($chip, $mask);
  $mi == $words or confess <<"END" =~ s/\n(.)/ $1/gsr;
Mask width $mi does not match number of words $words.
END

  for   my $w(1..$words)                                                        // And each bit of each word with the mask
   {for my $b(1..$bits)                                                         // Bits in each word
     {$chip->and(nn("$o.a", $w, $b), [n($mask, $w), nn($input, $w, $b)]);
     }
   }

  for   my $b(1..$bits)                                                         // Bits in each word
   {$chip->or(n($o, $b), [map {nn("$o.a", $_, $b)} 1..$words]);
   }
  setSizeBits($chip, $o, $bits);

  $chip
 }

sub findWord($$$$%)                                                             // Choose one of a specified number of words B<w>, each of a specified width, using a key B<k>.  Return a point mask B<o> indicating the locations of the key if found or or a mask equal to all zeroes if the key is not present.
 {my ($chip, $output, $key, $words, %options) = @_;                             // Chip, found point mask, key, words to search, options
  @_ >= 4 or confess "Four or more parameters";
  my $o = $output;
  my ($W, $B) = sizeWords($chip, $words);
  my $bits    = sizeBits ($chip, $key);
  $B == $bits or confess <<"END" =~ s/\n(.)/ $1/gsr;
Number of bits in each word $B differs from words in key $bits.
END
  for   my $w(1..$W)                                                            // Input words
   {$chip->compareEq(n($o, $w), n($words, $w), $key);                           // Compare each input word with the key to make a mask
   }
  setSizeBits($chip, $o, $W);

  $chip
 }

//D1 Simulate                                                                    // Simulate the behavior of the L<chip> given a set of values on its input gates.

my sub setBit($*$%)                                                             // Set a single bit
 {my ($chip, $name, $value, %options) = @_;                                     // Chip, name of input gates, number to set to, options
  @_ >= 3 or confess "Three or more parameters";
  my $g = getGate($chip, $name);
  my $t = $g->type;
  $g->io == gateOuterInput or confess <<"END" =~ s/\n(.)/ $1/gsr;
Only outer input gates are setable. Gate $name is of type $t
END
  my %i = ($name => $value ? $value : 0);
  %i
 }

sub setBits($*$%)                                                               // Set an array of input gates to a number prior to running a simulation.
 {my ($chip, $name, $value, %options) = @_;                                     // Chip, name of input gates, number to set to, options
  @_ >= 3 or confess "Three or more parameters";
  my $bits = sizeBits($chip, $name);                                            // Size of bus
  my $W = 2**$bits;
  $value >= 0 or confess <<"END";
Value $value is less than 0
END
  $value < $W or confess <<"END";
Value $value is greater then or equal to $W
END
  my @b = reverse split //,  sprintf "%0${bits}b", $value;
  my %i = map {n($name, $_) => $b[$_-1]} 1..$bits;
  %i
 }

sub setWords($$@)                                                               // Set an array of arrays of gates to an array of numbers prior to running a simulation.
 {my ($chip, $name, @values) = @_;                                              // Chip, name of input gates, number of bits in each array element, numbers to set to
  @_ >= 3 or confess "Three or more parameters";
  my ($words, $bits) = sizeWords($chip, $name);
  my %i;
  my $M = 2**$bits-1;                                                           // Maximum we can store in a word of this many bits
  for   my $w(1..$words)                                                        // Each word
   {my $n = shift @values;
    $n >= 0 or confess <<"END";
Value $n is less than 0
END
    $n <= $M or confess <<"END";
 "Value $n is greater then or equal to $M";
END
    my @b = split //,  sprintf "%0${bits}b", $n;
    for my $b(1..$bits)                                                         // Each bit
     {$i{nn($name, $w, $b)} = $b[-$b];
     }
   }
  %i
 }

sub connectBits($*$*%)                                                          // Create a connection list connecting a set of output bits on the one chip to a set of input bits on another chip.
 {my ($oc, $o, $ic, $i, %options) = @_;                                         // First chip, name of gates on first chip, second chip, names of gates on second chip, options
  @_ >= 4 or confess "Four or more parameters";
  my %c;
  my $O = sizeBits($oc, $o);
  my $I = sizeBits($ic, $i);
  $O == $I or confess <<"END";
Mismatch between size of bus $o at $O and bus $i at $I
END

  for my $b(1..$I)                                                              // Bit to connect
   {$c{n($o, $b)} = n($i, $b);                                                  // Connect bits
   }
  %c                                                                            // Connection list
 }

sub connectWords($*$*$$%)                                                       // Create a connection list connecting a set of words on the outer chip to a set of words on the inner chip.
 {my ($oc, $o, $ic, $i, $words, $bits, %options) = @_;                          // First chip, name of gates on first chip, second chip, names of gates on second chip, number of words to connect, options
  @_ >= 6 or confess "Six or more parameters";
  my %c;
  for   my $w(1..$bits)                                                         // Word to connect
   {for my $b(1..$bits)                                                         // Bit to connect
     {$c{nn($o, $w, $b)} = nn($i, $w, $b);                                      // Connection list
     }
   }
  %c                                                                            // Connection list
 }

my sub merge($%)                                                                // Merge a L<chip> and all its sub L<chips> to make a single L<chip>.
 {my ($chip, %options) = @_;                                                    // Chip, options

  my $gates = getGates $chip;                                                   // Gates implementing the chip and all of its sub chips

  setOuterGates ($chip, $gates);                                                // Set the outer gates which are to be connected to in the real word
  removeExcessIO($chip, $gates);                                                // By pass and then remove all interior IO gates as they are no longer needed

  my $c = newChip %$chip, %options, gates=>$gates, installs=>[];                // Create the new chip with all installs expanded
  #print($c, %options)     if $options{print};                                  // Print the gates
  #printSvg ($c, %options) if $options{svg};                                    // Draw the gates using svg
  checkIO $c;                                                                   // Check all inputs are connected to valid gates and that all outputs are used

  addFans $c;                                                                   // Add any fanOut gates needed to ensure that all wires start and end on unique points to simplify the wiring diagram
  $c
 }

sub Silicon::Chip::Simulation::value($$%)                                       // Get the value of a gate as seen in a simulation.
 {my ($simulation, $name, %options) = @_;                                       // Chip, gate, options
  @_ >= 2 or confess "Two or more parameters";
  $simulation->values->{$name}                                                  // Value of gate
 }

my sub checkInputs($$%)                                                         // Check that an input value has been provided for every input pin on the chip.
 {my ($chip, $inputs, %options) = @_;                                           // Chip, inputs, hash of final values for each gate, options

  for my $g(values $chip->gates->%*)                                            // Each gate on chip
   {if   ($g->io == gateOuterInput)                                             // Outer input gate
     {my ($i) = values $g->inputs->%*;                                          // Inputs
      if (!defined($$inputs{$i}))                                               // Check we have a corresponding input
       {my $n = $g->name;
        confess "No input value for input gate: $n\n";
       }
     }
   }
 }

sub Silicon::Chip::Simulation::bInt($$%)                                        // Represent the state of bits in the simulation results as an unsigned binary integer.
 {my ($simulation, $output, %options) = @_;                                     // Chip, name of gates on bus, options
  @_ >= 2 or confess "Two or more parameters";
  my $B = sizeBits($simulation->chip, $output);
  my %v = $simulation->values->%*;
  my @b;
  for my $b(1..$B)                                                              // Bits
   {push @b, $v{n $output, $b};
   }

  eval join '', '0b', reverse @b;                                               // Convert to number
 }

sub Silicon::Chip::Simulation::wInt($$%)                                        // Represent the state of words in the simulation results as an array of unsigned binary integer.
 {my ($simulation, $output, %options) = @_;                                     // Chip, name of gates on bus, options
  @_ >= 2 or confess "Two or more parameters";
  my ($words, $bits) = sizeWords($simulation->chip, $output);
  my %v = $simulation->values->%*;
  my @w;
  for my $w(1..$words)                                                          // Words
   {my @b;
    for my $b(1..$bits)                                                         // Bits
     {push @b, $v{nn $output, $w, $b};
     }

    push @w,  eval join '', '0b', reverse @b;                                   // Convert to number
   }
  @w
 }

sub Silicon::Chip::Simulation::wordXToInteger($$%)                              // Represent the state of words in the simulation results as an array of unsigned binary integer.
 {my ($simulation, $output, %options) = @_;                                     // Chip, name of gates on bus, options
  @_ >= 2 or confess "Two or more parameters";
  my ($words, $bits) = sizeWords($simulation->chip, $output);
  my %v = $simulation->values->%*;
  my @w;
  for my $b(1..$bits)                                                           // Bits
   {my @b;
    for my $w(1..$words)                                                        // Words
     {push @b, $v{nn $output, $w, $b};
     }

    push @w,  eval join '', '0b', reverse @b;                                   // Convert to number
   }
  @w
 }

my sub simulationStep($$%)                                                      // One step in the simulation of the L<chip> after expansion of inner L<chips>.
 {my ($chip, $values, %options) = @_;                                           // Chip, current value of each gate, options
  my $gates = $chip->gates;                                                     // Gates on chip
  my %changes;                                                                  // Changes made

  for my $G(sort {$$gates{$a}->seq <=> $$gates{$b}->seq} keys %$gates)          // Each gate in sub chip on definition order to get a repeatable order
   {my $g = $$gates{$G};                                                        // Address gate
    my $t = $g->type;                                                           // Gate type
    my $n = $g->name;                                                           // Gate name
    my %i = $g->inputs->%*;                                                     // Inputs to gate
    my @i = map {$$values{$i{$_}}} sort keys %i;                                // Values of inputs to gates in input pin name order

    my $u = 0;                                                                  // Number of undefined inputs
    for my $i(@i)
     {++$u unless defined $i;
     }

    if ($u == 0)                                                                // All inputs defined
     {my $r;                                                                    // Result of gate operation
      if ($t =~ m(\Aand|nand\Z)i)                                               // Elaborate and B<and> and B<nand> gates
       {my $z = grep {!$_} @i;                                                  // Count zero inputs
        $r = $z ? 0 : 1;
        $r = !$r if $t =~ m(\Anand\Z)i;
       }
      elsif ($t =~ m(\A(input)\Z)i)                                             // An B<input> gate takes its value from the list of inputs or from an output gate in an inner chip
       {if (my @i = values $g->inputs->%*)                                      // Get the value of the input gate from the current values
         {my $n = $i[0];
             $r = $$values{$n};
         }
        else
         {confess "No driver for input gate: $n\n";
         }
       }
      elsif ($t =~ m(\A(continue|nor|not|or|output)\Z)i)                        // Elaborate B<not>, B<or> or B<output> gate. A B<continue> gate places its single input unchanged on its output
       {my $o = grep {$_} @i;                                                   // Count one inputs
        $r = $o ? 1 : 0;
        $r = $r ? 0 : 1 if $t =~ m(\Anor|not\Z)i;
       }
      elsif ($t =~ m(\A(nxor|xor)\Z)i)                                          // Elaborate B<xor>
       {@i == 2 or confess "$t gate: '$n' must have exactly two inputs\n";
        $r = $i[0] ^ $i[1] ? 1 : 0;
        $r = $r ? 0 : 1 if $t =~ m(\Anxor\Z)i;
       }
      elsif ($t =~ m(\A(gt|ngt)\Z)i)                                            // Elaborate B<a> more than B<b> - the input pins are assumed to be sorted by name with the first pin as B<a> and the second as B<b>
       {@i == 2 or confess "$t gate: '$n' must have exactly two inputs\n";
        $r = $i[0] > $i[1] ? 1 : 0;
        $r = $r ? 0 : 1 if $t =~ m(\Angt\Z)i;
       }
      elsif ($t =~ m(\A(lt|nlt)\Z)i)                                            // Elaborate B<a> less than B<b> - the input pins are assumed to be sorted by name with the first pin as B<a> and the second as B<b>
       {@i == 2 or confess "$t gate: '$n' must have exactly two inputs\n";
        $r = $i[0] < $i[1] ? 1 : 0;
        $r = $r ? 0 : 1 if $t =~ m(\Anlt\Z)i;
       }
      elsif ($t =~ m(\Aone\Z)i)                                                 // One
       {@i == 0 or confess "$t gate: '$n' must have no inputs\n";
        $r = 1;
       }
      elsif ($t =~ m(\Azero\Z)i)                                                // Zero
       {@i == 0 or confess "$t gate: '$n' must have no inputs\n";
        $r = 0;
       }
      elsif ($t =~ m(\AfanOut\Z)i)                                              // Fanout
       {$r = $i[0];
        for my $o($g->output->@*)                                               // Fan onput to each output pin if not already set
         {$changes{$o} = $r unless defined($$values{$o}) and $$values{$o} == $r;
         }
       }
      else                                                                      // Unknown gate type
       {confess "Need implementation for '$t' gates";
       }
      $changes{$G} = $r unless defined($$values{$G}) and $$values{$G} == $r;    // Value computed by this gate
     }
   }
  %changes
 }

sub Silicon::Chip::Simulation::checkLevelsMatch($%)                             // Check that the specified number of levels matches that specified by the pngs parameter.  The pngs paramneter tells us howmany drawings to make - one per level - ideally this should match the actual number of levels.
 {my ($simulation, %options) = @_;                                              // Simulation, options
  my $log    = $options{log} // 0;                                              // Logging required
  my $levels = $simulation->levels // 0;                                        // Levels actual
  my $pngs   = $simulation->options->{pngs} // 0;                               // Levels expected
  my $T = $levels == $pngs;                                                     // Confirm pngs is levels
  cluck "Levels=>$levels does not match pngs=>$pngs" unless $T;                 // Complain if requested
  $T                                                                            // Return pngs is levels
 }

sub simulate($$%)                                                               // Simulate the action of the L<lgs> on a L<chip> for a given set of inputs until the output value of each L<lg> stabilizes.  Draw the simulated circuit using the timeing information to determine the layout of the gates.
 {my ($chip, $inputs, %options) = @_;                                           // Chip, Hash of input names to values, options
  my $svg  = $options{svg}  // 0;                                               // Mask requested
  my $pngs = $options{pngs} // 0;                                               // Number of png images requested
  my $log  = $options{log}  // 0;                                               // Logging level
  @_ >= 2 or confess "Two or more parameters";

  lll "Simulate\n", dump(\%options) if $log;                                    // Log options used

  my $c = merge($chip, %options);                                               // Merge all the sub chips to make one chip with no sub chips
  checkInputs($c, $inputs);                                                     // Confirm that there is an input value for every input to the chip
  my %values = %$inputs;                                                        // The current set of values contains just the inputs at the start of the simulation
  my %changed;                                                                  // Last step on which this gate changed.  We use this to order the gates on layout

  my $T = maxSimulationSteps;                                                   // Maximum steps
  for my $t(0..$T)                                                              // Steps in time
   {my %changes = simulationStep $c, \%values;                                  // Changes made

    if (!keys %changes)                                                         // Keep going until nothing changes
     {my $mask;                                                                 // Create mask from simulation if requested
      if ($options{svg})                                                        // Layout the gates
       {$mask = drawMask $c, values=>\%values, changed=>\%changed,
          steps=>$t, %options;
       }

      my ($area, $height, $length, $levels, $width) = sub                       // Details of layout if known
       {return (undef) x 4 unless $mask && ref($mask);                          // Svg contains details about dimensions of chip
        my $length = $mask->wiring->totalLength // 0;                           // Total length of wiring on chip in cells
        my $levels = $mask->wiring->levels      // 0;                           // Number of levels of wiring
        my $width  = $mask->layout->width       // 0;                           // Width of chip in cells
        my $height = $mask->layout->height      // 0;                           // Height of chip in cells
        ($height*$width, $height, $length, $levels, $width);                    // Dimensions of chip
       }->();

      my $s = genHash(__PACKAGE__."::Simulation",                               // Kept going until nothing changed in the simulation
        chip    => $chip,                                                       // Chip being simulated
        changed => \%changed,                                                   // Last time this gate changed
        steps   => $t,                                                          // Number of steps to reach stability
        values  => \%values,                                                    // Values of every output at point of stability
        options => \%options,                                                   // Options used to perform simulation
        length  => $length,                                                     // Total length of wiring on chip in cells
        levels  => $levels,                                                     // Number of levels of wiring
        width   => $width,                                                      // Width of chip in cells
        height  => $height,                                                     // Height of chip in cells
        area    => $area,                                                       // Area of chip
        mask    => $mask,                                                       // Masks for chip if they have been produced
      );

      storeFile(fpe(q(save), $svg, q(data)), $s) if $svg;                       // Save simulation/mask details

      return $s;                                                                // Finished now that no more changes are occurring
     }

    for my $c(keys %changes)                                                    // Update state of circuit
     {$values{$c} = $changes{$c};
      if ($options{latest})
       {$changed{$c} = $t;                                                      // Latest time we changed this gate
       }
      else
       {$changed{$c} = $t unless defined($changed{$c});                         // Earliest time we changed this gate
       }
     }
   }

  confess "Out of time after $T steps";                                         // Not enough steps available
 }



//D0 Tests                                                                       # Tests and examples
goto finish if caller;                                                          # Skip testing if we are being called as a module
clearFolder($_, 999) for qw(gds save svg);                                      # Clear the output folders
my sub gitHub {!-e q(/home/phil/)}                                              # On Github

my $start = time;
eval "use Test::More";
eval "Test::More->builder->output('/dev/null')" unless gitHub;
eval {goto latest}                              unless gitHub;

#svg https://vanina-andrea.s3.us-east-2.amazonaws.com/SiliconChip/lib/Silicon/Chip/svg/

#latest:;
if (1)                                                                          #Tdistance
 {is_deeply(distance(2,1,  1, 2), 2);
 }

if (1)                                                                          #Tn #Tnn
 {is_deeply( n(a,1),   "a_1");
  is_deeply(nn(a,1,2), "a_1_2");
 }

if (1)                                                                          # Unused output
 {my $c = Silicon::Chip::newChip;
  $c->input( "i1");
  eval {$c->simulate({i1=>1})};
  ok($@ =~ m(Output from gate 'i1' is never used)i);
 }

if (1)                                                                          # Gate already specified
 {my $c = Silicon::Chip::newChip;
        $c->input("i1");
  eval {$c->input("i1")};
  ok($@ =~ m(Gate: 'i1' has already been specified));
 }

#latest:;
if (1)                                                                          # Check all inputs have values
 {my $c = Silicon::Chip::newChip;
  $c->input ("i1");
  $c->input ("i2");
  $c->and   ("and", [qw(i1 i2)]);
  $c->output("o",   q(and));
  eval {$c->simulate({i1=>1, i22=>1})};
  ok($@ =~ m(No input value for input gate: i2)i);
 }

#latest:;
if (1)                                                                          # Check each input to each gate receives output from another gate
 {my $c = Silicon::Chip::newChip;
  $c->input("i1");
  $c->input("i2");
  $c->and  ("and1", [qw(i1 i2)]);
  $c->output( "o", q(an1));
  eval {$c->simulate({i1=>1, i2=>1})};
  ok($@ =~ m(No output driving input 'an1' on 'output' gate 'o')i);
 }

#latest:;
if (1)                                                                          #TnewChip
 {my $c = Silicon::Chip::newChip(expand=>1);
  $c->one ("one");
  $c->zero("zero");
  $c->or  ("or",   [qw(one zero)]);
  $c->and ("and",  [qw(one zero)]);
  $c->output("o1", "or");
  $c->output("o2", "and");
  my $s = $c->simulate({}, svg=>q(oneZero), pngs=>1, gsx=>2, gsy=>2);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps      , 4);
  is_deeply($s->value("o1"), 1);
  is_deeply($s->value("o2"), 0);
 }

#latest:;
if (1)                                                                          #Tbits
 {my $N = 4;
  for my $i(0..2**$N-1)
   {my $c = Silicon::Chip::newChip;
    $c->bits      ("c", $N, $i);
    $c->outputBits("o", "c");
    my @T = $i == 3 ? (svg=>q(bits), pngs=>1) : ();
    my $s = $c->simulate({}, @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->steps, 2);
    is_deeply($s->bInt("o"), $i);
   }
 }

#latest:;
if (1)                                                                          #TnewChip # Single AND gate
 {my $c = Silicon::Chip::newChip;
  $c->input ("i1");
  $c->input ("i2");
  $c->and   ("and1", [qw(i1 i2)]);
  $c->output("o", "and1");
  my $s = $c->simulate({i1=>1, i2=>1}, svg=>q(and), pngs=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps         , 2);
  is_deeply($s->value("and1") , 1);
 }

#latest:;
if (1)                                                                          #TSilicon::Chip::Simulation::print #Tprint
 {my $c = Silicon::Chip::newChip(title=>"And gate");
  $c->input ("i1");
  $c->input ("i2");
  $c->and   ("and1", [qw(i1 i2)]);
  $c->output("o", "and1");
  my $s = $c->simulate({i1=>1, i2=>1}, svg=>"andGate", pngs=>1, gsx=>1, gsy=>1);
  ok($s->checkLevelsMatch);

  is_deeply($c->print, <<END);
i1                              :     input                           i1
i2                              :     input                           i2
and1                            :     and                             i1 i2
o                               :     output                          and1
END

  is_deeply($s->print, <<END);
i1                              :   1 input                           i1
i2                              :   1 input                           i2
and1                            :   1 and                             i1 i2
o                               :   1 output                          and1
END
 }

#latest:;
if (1)                                                                          # Three AND gates in a tree
 {my $c = Silicon::Chip::newChip;
  $c->input( "i11");
  $c->input( "i12");
  $c->and(    "and1", [qw(i11 i12)]);
  $c->input( "i21");
  $c->input( "i22");
  $c->and(    "and2", [qw(i21   i22)]);
  $c->and(    "and",  [qw(and1 and2)]);
  $c->output( "o", "and");
  my $s = $c->simulate({i11=>1, i12=>1, i21=>1, i22=>1}, svg=>q(and3), pngs=>1);
  ok($s->checkLevelsMatch);

  is_deeply($s->steps        , 3);
  is_deeply($s->value("and") , 1);
  $s = $c->simulate({i11=>1, i12=>0, i21=>1, i22=>1});

  is_deeply($s->steps        , 3);
  is_deeply($s->value("and") , 0);
 }

#latest:;
if (1)                                                                          #Tgate # Two AND gates driving an OR gate
 {my $c = newChip;
  $c->input ("i11");
  $c->input ("i12");
  $c->and   ("and1", [qw(i11   i12)]);
  $c->input ("i21");
  $c->input ("i22");
  $c->and   ("and2", [qw(i21   i22 )]);
  $c->or    ("or",   [qw(and1  and2)]);
  $c->output( "o", "or");
  my $s = $c->simulate({i11=>1, i12=>1, i21=>1, i22=>1}, svg=>q(andOr), pngs=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps        , 3);
  is_deeply($s->value("or")  , 1);
     $s  = $c->simulate({i11=>1, i12=>0, i21=>1, i22=>1});
  is_deeply($s->steps        , 3);
  is_deeply($s->value("or")  , 1);
     $s  = $c->simulate({i11=>1, i12=>0, i21=>1, i22=>0});
  is_deeply($s->steps        , 3);
  is_deeply($s->value("o")   , 0);
 }

#latest:;
if (1)                                                                          #Tsimulate # 4 bit equal #TnewChip
 {my $B = 4;                                                                    # Number of bits

  my $c = Silicon::Chip::newChip(title=><<"END");                               # Create chip
$B Bit Equals
END
  $c->input ("a$_")                 for 1..$B;                                  # First number
  $c->input ("b$_")                 for 1..$B;                                  # Second number

  $c->nxor  ("e$_", "a$_", "b$_")   for 1..$B;                                  # Test each bit for equality
  $c->and   ("and", {map{$_=>"e$_"}     1..$B});                                # And tests together to get total equality

  $c->output("out", "and");                                                     # Output gate

  my $s = $c->simulate({a1=>1, a2=>0, a3=>1, a4=>0,                             # Input gate values
                        b1=>1, b2=>0, b3=>1, b4=>0},
                        svg=>q(Equals), pngs=>1, spaceDy=>1);                   # Svg drawing of layout
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,        4);                                               # Steps
  is_deeply($s->value("out"), 1);                                               # Out is 1 for equals

  my $t = $c->simulate({a1=>1, a2=>1, a3=>1, a4=>0,
                        b1=>1, b2=>0, b3=>1, b4=>0});
  is_deeply($t->value("out"), 0);                                               # Out is 0 for not equals
 }

#latest:;
if (1)                                                                          #TsetBits # Compare two 4 bit unsigned integers 'a' > 'b' - the pins used to input 'a' must be alphabetically less than those used for 'b'
 {my $B = 4;                                                                    # Number of bits
  my $c = Silicon::Chip::newChip(title=><<"END", expand=>1);
$B Bit Compare Greater Than
END
  $c->inputBits("a", $B);                                                       # First number
  $c->inputBits("b", $B);                                                       # Second number
  $c->nxor (n(e,$_), n(a,$_), n(b,$_)) for 1..$B-1;                             # Test each bit for equality
  $c->gt   (n(g,$_), n(a,$_), n(b,$_)) for 1..$B;                               # Test each bit pair for greater

  for my $b(2..$B)
   {$c->and(n(c,$b), [(map {n(e, $_)} 1..$b-1), n(g,$b)], undef, debug=>1);     # Greater on one bit and all preceding bits are equal
   }

  $c->or    ("or",  [n(g,1), (map {n(c, $_)} 2..$B)]);                          # Any set bit indicates that 'a' is more than 'b'
  $c->output("out", "or");                                                      # Output 1 if a > b else 0

  my %a = $c->setBits('a', 0);                                                  # Number a
  my %b = $c->setBits('b', 0);                                                  # Number b

  my $s = $c->simulate({%a, %b, n(a,2)=>1, n(b,2)=>1}, svg=>q(gt), pngs=>2,
    gsx=>2, gsy=>2, spaceDx=>1, borderDy=>1, minHeight=>18, newChange=>1);      # Compare two numbers a, b to see if a > b
  ok($s->checkLevelsMatch);
  is_deeply($s->value("out"), 0);

  my $t = $c->simulate({%a, %b, n(a,2)=>1});
  is_deeply($t->value("out"), 1);
 }

#latest:;
if (1)                                                                           #TcompareEq # Compare unsigned integers
 {my $B = 2;

  my $c = Silicon::Chip::newChip(title=><<"END");
$B Bit Compare Equal
END
  $c->inputBits($_, $B) for qw(a b);                                            # First and second numbers
  $c->compareEq(qw(o a b));                                                     # Compare equals
  $c->output   (qw(out o));                                                     # Comparison result

  for   my $i(0..2**$B-1)                                                       # Each possible number
   {for my $j(0..2**$B-1)                                                       # Each possible number
     {my %a = $c->setBits('a', $i);                                             # Number a
      my %b = $c->setBits('b', $j);                                             # Number b

      my @T = $i == 1 && $j==1 ? (svg=>q(CompareEq), pngs=>1) : ();
      my $s = $c->simulate({%a, %b}, @T);                                       # Svg drawing of layout
      ok($s->checkLevelsMatch) if @T;
      is_deeply($s->value("out"), $i == $j ? 1 : 0);                            # Equal
      is_deeply($s->steps, 3);                                                  # Number of steps to stability
     }
   }
 }

#latest:;
if (gitHub)                                                                     #TcompareGt # Compare 8 bit unsigned integers 'a' > 'b' - the pins used to input 'a' must be alphabetically less than those used for 'b'
 {my $B = 3;
  my $c = Silicon::Chip::newChip(title=><<END);
$B Bit Compare more than
END
  $c->inputBits($_, $B) for qw(a b);                                            # First and second numbers
  $c->compareGt(qw(o a b));                                                     # Compare more than
  $c->output   (qw(out o));                                                     # Comparison result

  for   my $i(0..2**$B-1)                                                       # Each possible number
   {for my $j(0..2**$B-1)                                                       # Each possible number
     {#$i = 2; $j = 1;
      my %a = $c->setBits('a', $i);                                             # Number a
      my %b = $c->setBits('b', $j);                                             # Number b
      my $T = $i==2&&$j==1;
      my @T = $T ? (svg=>q(CompareGt), pngs=>1, spaceDx=>1, borderDy=>2) : ();  # Svg drawing of layout
      my $s = $c->simulate({%a, %b}, @T);                                       # Svg drawing of layout
      ok($s->checkLevelsMatch) if @T;
      is_deeply($s->value("out"), $i > $j ? 1 : 0);                             # More than
      is_deeply($s->steps, 8);                                                  # Number of steps to stability
     }
   }
 }

#latest:;
if (1)                                                                          #TcompareLt # Compare 8 bit unsigned integers 'a' < 'b' - the pins used to input 'a' must be alphabetically less than those used for 'b'
 {my $B = 3;
  my $c = Silicon::Chip::newChip(title=><<"END");
$B Bit Compare Less Than
END
  $c->inputBits($_, $B) for qw(a b);                                            # First and second numbers
  $c->compareLt(qw(o a b));                                                     # Compare less than
  $c->output   (qw(out o));                                                     # Comparison result

  for   my $i(0..2**$B-1)                                                       # Each possible number
   {for my $j(0..2**$B-1)                                                       # Each possible number
     {my %a = $c->setBits('a', $i);                                             # Number a
      my %b = $c->setBits('b', $j);                                             # Number b
      my $T = $i==2&&$j==1;
      my @T = $T ? (svg=>q(CompareLt), pngs=>1, spaceDx=>1, borderDy=>2) : ();  # Svg drawing of layout
      my $s = $c->simulate({%a, %b}, @T);                                       # Svg drawing of layout
      ok($s->checkLevelsMatch) if @T;
      is_deeply($s->value("out"), $i < $j ? 1 : 0);                             # More than
      is_deeply($s->steps, 8);                                                  # Number of steps to stability
     }
   }
 }

#latest:;
if (1)                                                                          # Masked multiplexer: copy B bit word selected by mask from W possible locations
 {my $B = 4; my $W = 4;
  my $c = newChip;
  for my $w(1..$W)                                                              # Input words
   {$c->input("s$w");                                                           # Selection mask
    for my $b(1..$B)                                                            # Bits of input word
     {$c->input("i$w$b");
      $c->and(  "s$w$b", ["i$w$b", "s$w"]);
     }
   }
  for my $b(1..$B)                                                              # Or selected bits together to make output
   {$c->or    ("c$b", [map {"s$b$_"} 1..$W]);                                   # Combine the selected bits to make a word
    $c->output("o$b", "c$b");                                                   # Output the word selected
   }
  my $s = $c->simulate(
   {s1 =>0, s2 =>0, s3 =>1, s4 =>0,                                             # Input switch
    i11=>0, i12=>0, i13=>0, i14=>1,                                             # Inputs
    i21=>0, i22=>0, i23=>1, i24=>0,
    i31=>0, i32=>1, i33=>0, i34=>0,
    i41=>1, i42=>0, i43=>0, i44=>0}, svg=>q(maskedMultiplexor), pngs=>1,
    newChange=>1, borderDy=>4, placeFirst=>1);
    ok($s->checkLevelsMatch);

  is_deeply([@{$s->values}{qw(o1 o2 o3 o4)}], [qw(0 0 1 0)]);                   # Number selected by mask
  is_deeply($s->steps,     6);
  #say STDERR $s->mask->wiring->printCode;
 }

#latest:;
if (1)                                                                          # Rename a gate
 {my $i = newChip(name=>"inner");
          $i->input ("i");
  my $n = $i->not   ("n",  "i");
          $i->name("io", "n");

  my $ci = cloneGate $i, $n;
  renameGate $i, $ci, "aaa";
  is_deeply($ci->inputs,   { n => "i" });
  is_deeply($ci->name,  "(aaa n)");
  is_deeply($ci->io, 0);
 }

#latest:;
if (1)                                                                          #Tinstall #TconnectBits # Install one chip inside another chip, specifically one chip that performs NOT is installed once to flip a value
 {my $i = newChip(name=>"not");
     $i-> inputBits('i',     1);
     $i->   notBits(qw(n i));
     $i->outputBits(qw(o n));

  my $o = newChip(name=>"outer");
     $o->inputBits('i', 1); $o->outputBits(qw(n i));
     $o->inputBits('I', 1); $o->outputBits(qw(N I));

  my %i = connectBits($i, 'i', $o, 'n');
  my %o = connectBits($i, 'o', $o, 'I');
  $o->install($i, {%i}, {%o});
  my %d = $o->setBits('i', 1);
  my $s = $o->simulate({%d}, svg=>q(notb1), pngs=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  2);
  is_deeply($s->values, {"(not 1 n_1)"=>0, "i_1"=>1, "N_1"=>0 });
 }

#latest:;
if (1)                                                                          #TconnectWords # Install one chip inside another chip, specifically one chip that performs NOT is installed three times sequentially to flip a value
 {my $i = newChip(name=>"not");
     $i-> inputWords('i', 1, 1);
     $i->   notWords(qw(n i));
     $i->outputWords(qw(o n));

  my $o = newChip(name=>"outer");
     $o->inputWords('i', 1, 1); $o->output(nn('n', 1, 1), nn('i', 1, 1));
     $o->inputWords('I', 1, 1); $o->output(nn('N', 1, 1), nn('I', 1, 1));

  my %i = connectWords($i, 'i', $o, 'n', 1, 1);
  my %o = connectWords($i, 'o', $o, 'I', 1, 1);
  $o->install($i, {%i}, {%o});
  my %d = $o->setWords('i', 1);
  my $s = $o->simulate({%d}, svg=>q(notw1), pngs=>1);
  ok($s->checkLevelsMatch);

  is_deeply($s->steps,  2);
  is_deeply($s->values, { "(not 1 n_1_1)" => 0, "i_1_1" => 1, "N_1_1" => 0 });
 }

#latest:;
if (1)
 {my $i = newChip(name=>"inner");
     $i->input ("Ii");
     $i->not   ("In", "Ii");
     $i->output( "Io", "In");

  my $o = newChip(name=>"outer");
     $o->input ("Oi1");
     $o->output("Oo1", "Oi1");
     $o->input ("Oi2");
     $o->output("Oo2", "Oi2");
     $o->input ("Oi3");
     $o->output("Oo3", "Oi3");
     $o->input ("Oi4");
     $o->output("Oo",  "Oi4");

  $o->install($i, {Ii=>"Oo1"}, {Io=>"Oi2"});
  $o->install($i, {Ii=>"Oo2"}, {Io=>"Oi3"});
  $o->install($i, {Ii=>"Oo3"}, {Io=>"Oi4"});

  my $s = $o->simulate({Oi1=>1}, svg=>q(not3), pngs=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->value("Oo"), 0);
  is_deeply($s->steps,       4);

  my $t = $o->simulate({Oi1=>0});
  is_deeply($t->value("Oo"), 1);
  is_deeply($t->steps,       4);
 }

#latest:;
if (1)                                                                          #TchooseWordUnderMask #TsetBits #TsetWords
 {my $B = 3; my $W = 4;

  my $c = Silicon::Chip::newChip(title=><<"END");
Choose one of $W words of $B bits
END
     $c->inputWords         ('w',       $W, $B);
     $c->inputBits          ('m',       $W);
     $c->chooseWordUnderMask(qw(W w m));
     $c->outputBits         (qw(o W));

  my %i = setWords($c, 'w', 0b000, 0b001, 0b010, 0b0100);
  my %m = setBits ($c, 'm', 1<<2);                                              # Choose the third word

  my $s = $c->simulate({%i, %m}, svg=>q(choose), pngs=>2, gsx=>2, gsy=>2, newChange=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  6);
  is_deeply($s->bInt('o'), 0b010);
 }

#latest:;
if (gitHub)                                                                     #TpointMaskToInteger
 {my $B = 4;
  my $N = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<"END");
$B bits point mask to integer
END
  $c->inputBits         (qw(    i), $N);                                        # Mask with no more than one bit on
  $c->pointMaskToInteger(qw(o   i));                                            # Convert
  $c->outputBits        (qw(out o));                                            # Mask with no more than one bit on

 for my $i(0..$N)                                                               # Each position of mask
   {my %i = setBits($c, 'i', $i ? 1<<($i-1) : 0);                               # Point in each position with zero representing no position
    my $T = $i == 5;
    my @T = $T ? (svg=>q(pointMaskToInteger), pngs=>1,
      newChange=>1, gsx=>3, gsy=>3,
      spaceDx=>2, spaceDy=>2, newChange=>1, borderDy=>4) : ();
    my $s = $c->simulate(\%i, @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->steps, 6);
    my %o = $s->values->%*;                                                     # Output bits
    my $n = eval join '', '0b', map {$o{n(o,$_)}} reverse 1..$B;                # Output bits as number
    is_deeply($n, $i);
    is_deeply($s->levels, 1) if $T;
   }
 }

#latest:;
if (gitHub)                                                                     #TintegerToPointMask
 {my $B = 3;
  my $N = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<"END");
$B bit integer to $N bit monotone mask.
END
     $c->inputBits         (qw(  i), $B);                                       # Input bus
     $c->integerToPointMask(qw(m i));
     $c->outputBits        (qw(o m));

  for my $i(0..$N)                                                              # Each position of mask
   {my %i = setBits($c, 'i', $i);
    my @T = $i == 5 ? (svg=>q(integerToPointMask), pngs=>2, placeFirst=>1):();
    my $s = $c->simulate(\%i, @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->steps, 9);

    my $r = $s->bInt('o');                                                      # Mask values
    is_deeply($r, $i ? 1<<($i-1) : 0);                                          # Expected mask
   }
 }

#latest:;
if (gitHub)                                                                     #TmonotoneMaskToInteger
 {my $B = 4;
  my $N = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<"END");
$N bit monotone mask to $B bit integer
END
     $c->inputBits            ('i',     $N);
     $c->monotoneMaskToInteger(qw(m i));
     $c->outputBits           (qw(o m));

  for my $i(0..$N-1)                                                            # Each monotone mask
   {my %i = setBits($c, 'i', $i > 0 ? 1<<$i-1 : 0);
    my @T = $i == 5 ? (svg=>q(monotoneMaskToInteger), pngs=>2, placeFirst=>1) : ();
    my $s = $c->simulate(\%i, @T);
    ok($s->checkLevelsMatch) if @T;

    is_deeply($s->steps, 9);
    is_deeply($s->bInt('m'), $i);
   }
 }

#latest:;
if (gitHub)                                                                     #TintegerToMonotoneMask
 {my $B = 4;
  my $N = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<"END");
Convert $B bit integer to $N bit monotone mask
END
     $c->inputBits            ('i', $B);                                        # Input gates
     $c->integerToMonotoneMask(qw(m i));
     $c->outputBits           (qw(o m));                                        # Output gates

  for my $i(0..$N)                                                              # Each position of mask
   {my %i = setBits($c, 'i', $i);                                               # The number to convert
    my $s = $c->simulate(\%i);
    is_deeply($s->steps, 19);
    is_deeply($s->bInt('o'), $i > 0 ? ((1<<$N)-1)>>($i-1)<<($i-1) : 0);         # Expected mask
   }
 }

#latest:;
if (1)
 {my $B = 4;
  my $N = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<"END");
Convert $B bit integer to $N bit monotone mask
END
     $c->inputBits            ('i', $B);                                        # Input gates
     $c->integerToMonotoneMask(qw(m i));
     $c->outputBits           (qw(o m));                                        # Output gates

  for my $i(2)                                                                  # Each position of mask
   {my %i = setBits($c, 'i', $i);                                               # The number to convert
    my @T = (svg=>q(integerToMontoneMask), pngs=>2, newChange=>1, spaceDx=>4, spaceDy=>4, borderDy=>12, placeFirst=>1);
    my $s = $c->simulate(\%i, @T);
    ok($s->checkLevelsMatch);
    is_deeply($s->steps, 19);
    is_deeply($s->bInt('o'), $i > 0 ? ((1<<$N)-1)>>($i-1)<<($i-1) : 0);         # Expected mask
   }
 }

#latest:;
if (gitHub)                                                                     #TfindWord
 {my $B = 3; my $W = 2**$B-1;

  my $c = Silicon::Chip::newChip(title=><<END);
Search $W words of $B bits
END
     $c->inputBits ('k',       $B);                                             # Search key
     $c->inputWords('w',       2**$B-1, $B);                                    # Words to search
     $c->findWord  (qw(m k w));                                                 # Find the word
     $c->outputBits(qw(M m));                                                   # Output mask

  my %w = setWords($c, 'w', reverse 1..$W);

  for my $k(0..$W)                                                              # Each possible key
   {my %k = setBits($c, 'k', $k);
    my @T = $k == 3 ? (svg=>q(findWord), pngs=>2) : ();
    my $s = $c->simulate({%k, %w}, @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->steps,  7);
    is_deeply($s->bInt('M'),$k ? 2**($W-$k) : 0);
   }
 }

#latest:;
if (1)                                                                          #TinputBits #ToutputBits #TnotBits #TSilicon::Chip::Simulation::bInt
 {my $W = 8;
  my $i = newChip();
     $i->inputBits('i', $W);
     $i->notBits   (qw(n i));
     $i->outputBits(qw(o n));

  my $o = newChip(name=>"outer");
     $o->inputBits ('a', $W);
     $o->outputBits(qw(A a));
     $o->inputBits ('b', $W);
     $o->outputBits(qw(B b));

  my %i = connectBits($i, 'i', $o, 'A');
  my %o = connectBits($i, 'o', $o, 'b');
  $o->install($i, {%i}, {%o});

  my %d = setBits($o, 'a', 0b10110);
  my $s = $o->simulate({%d}, svg=>q(not), pngs=>1, gsx=>1, gsy=>1, minHeight=>2*$W, newChange=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->bInt('B'), 0b11101001);
 }

#latest:;                                                                       # Good for wiring testing
if (gitHub)                                                                          #TandBits #TorBits #TnandBits #TnorBits
 {for my $W(2..8)
   {my $c = newChip(expand=>1);
       $c-> inputBits('i', $W);
       $c->   andBits(qw(and  i));
       $c->    orBits(qw(or   i));
       $c->  nandBits(qw(nand i));
       $c->   norBits(qw(nor  i));
       $c->output    (qw(And  and));
       $c->output    (qw(Or   or));
       $c->output    (qw(nAnd nand));
       $c->output    (qw(nOr  nor));

    my %d = setBits($c, 'i', 0b1);
    my @T = $W == 8 ? (svg=>q(andOrBits), pngs=>2, spaceDx=>2, spaceDy=>1, minHeight=>20) : ();
    my $s = $c->simulate({%d}, @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->value("And"),  0);
    is_deeply($s->value("Or"),   1);
    is_deeply($s->value("nAnd"), 1);
    is_deeply($s->value("nOr"),  0);
   }
 }

#latest:;
if (1)                                                                          #TandWords #TandWordsX #TorWords #TorWordsX #ToutputBits #TnotWords
 {my @B = ((my $W = 4), (my $B = 2));

  my $c = newChip();
     $c->inputWords ('i', @B);
     $c->andWords   (qw(and  i));
     $c->andWordsX  (qw(andX i));
     $c-> orWords   (qw( or  i));
     $c-> orWordsX  (qw( orX i));
     $c->notWords   (qw(n    i));
     $c->outputBits (qw(And  and));
     $c->outputBits (qw(AndX andX));
     $c->outputBits (qw(Or   or));
     $c->outputBits (qw(OrX  orX));
     $c->outputWords(qw(N    n));
  my %d = setWords($c, 'i', 0b00, 0b01, 0b10, 0b11);
  my $s = $c->simulate({%d}, svg=>q(andOrWords), pngs=>1, newChange=>1, placeFirst=>1, spaceDx=>4, spaceDy=>4, borderDy=>18);
  ok($s->checkLevelsMatch);

  is_deeply($s->bInt('And'),  0b1000);
  is_deeply($s->bInt('AndX'), 0b0000);

  is_deeply($s->bInt('Or'),  0b1110);
  is_deeply($s->bInt('OrX'), 0b11);
  is_deeply([$s->wInt('N')], [3, 2, 1, 0]);
 }

#latest:;
if (1)                                                                          #TandWords #TorWords #TSilicon::Chip::Simulation::wordXToInteger #TSilicon::Chip::Simulation::wInt  #TinputWords #ToutputWords
 {my @b = ((my $W = 4), (my $B = 3));

  my $c = newChip();
     $c->inputWords ('i',      @b);
     $c->outputWords(qw(o i));

  my %d = setWords($c, 'i', 0b000, 0b001, 0b010, 0b011);
  my $s = $c->simulate({%d}, svg=>q(words2), pngs=>1, newChange=>1, minHeight=>12);
  ok($s->checkLevelsMatch);
  is_deeply([$s->wInt('o')], [0..3]);
  is_deeply([$s->wordXToInteger('o')], [10, 12, 0]);
 }


#latest:;
if (1)
 {my $B = 4;

  my $c = newChip();
     $c->inputBits('i', 4);
     $c->not   (n('n',  1), n('i', 2));  $c->output(n('o',  1), n('n', 1));
     $c->not   (n('n',  2), n('i', 3));  $c->output(n('o',  2), n('n', 2));
     $c->not   (n('n',  3), n('i', 4));  $c->output(n('o',  3), n('n', 3));
     $c->not   (n('n',  4), n('i', 1));  $c->output(n('o',  4), n('n', 4));

  my %a = setBits($c, 'i', 0b0011);

  my $s = $c->simulate({%a}, svg=>q(collapseLeftSimple), pngs=>1, newChange=>1, spaceDy=>4, minHeight=>10);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps, 2);
 }

#latest:;
if (1)                                                                          #TchooseFromTwoWords
 {my $B = 4;

  my $c = newChip();
     $c->inputBits('a', $B);                                                    # First word
     $c->inputBits('b', $B);                                                    # Second word
     $c->input    ('c');                                                        # Chooser
     $c->chooseFromTwoWords(qw(o a b c));                                       # Generate gates
     $c->outputBits('out', 'o');                                                # Result

  my %a = setBits($c, 'a', 0b0011);
  my %b = setBits($c, 'b', 0b1100);

  my $s = $c->simulate({%a, %b, c=>1}, svg=>q(chooseFromTwoWords), pngs=>1, minHeight=>18, spaceDy=>2);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  9);
  is_deeply($s->bInt('out'), 0b1100);

  my $t = $c->simulate({%a, %b, c=>0});
  is_deeply($t->steps,               9);
  is_deeply($t->bInt('out'), 0b0011);
 }

#latest:;
if (1)                                                                          #TenableWord
 {my $B = 4;

  my $c = newChip();
     $c->inputBits ('a', $B);                                                   # Word
     $c->input     ('c');                                                       # Choice bit
     $c->enableWord(qw(o a c));                                                 # Generate gates
     $c->outputBits(qw(out o));                                                 # Result

  my %a = setBits($c, 'a', 3);

  my $s = $c->simulate({%a, c=>1}, svg=>q(enableWord), pngs=>1, spaceDy=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,       4);
  is_deeply($s->bInt('out'), 3);

  my $t = $c->simulate({%a, c=>0});
  is_deeply($t->steps,       4);
  is_deeply($t->bInt('out'), 0);
 }

#latest:;
if (gitHub)                                                                     #TmonotoneMaskToPointMask
 {my $B = 4;

  my $c = newChip();
     $c->inputBits('m', $B);                                                    # Monotone mask
     $c->monotoneMaskToPointMask(qw(o m));                                      # Generate gates
     $c->outputBits('out', 'o');                                                # Point mask

  for my $i(0..$B)
   {my %m = $c->setBits('m', eval '0b'.(1 x $i).('0' x ($B-$i)));
    my @T = $i == 2 ? (svg=>q(monotoneMaskToPointMask), pngs=>1) : ();
    my $s = $c->simulate({%m},  @T);
    ok($s->checkLevelsMatch) if @T;
    is_deeply($s->steps,  3) if @T;
    is_deeply($s->bInt('out'), $i ? (1<<($B-1)) / (1<<($i-1)) : 0);
   }
 }

#latest:;
if (1)                                                                          # Internal input gate  #TconnectInput #TSilicon::Chip::Simulation::value
 {my $c = newChip();
     $c->input ('i');                                                           # Input
     $c->input ('j');                                                           # Internal input which we will connect to later
     $c->output(qw(o j));                                                       # Output

     $c->connectInput(qw(j i));

  my $s = $c->simulate({i=>1}, svg=>q(connectInput), pngs=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  1);
  is_deeply($s->value("j"), undef);
  is_deeply($s->value("o"), 1);
 }

#latest:;
if (1)                                                                          #Twords # Internal input gate  #TconnectInput #TSilicon::Chip::Simulation::value
 {my @n = qw(3 2 1 2 3);
  my $c = newChip();
     $c->words('i', 2, @n);                                                     # Input
     $c->outputWords(qw(o i));                                                  # Output
  my $s = $c->simulate({}, svg=>q(words), pngs=>1, gsx=>1, gsy=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  2);
  is_deeply([$s->wInt("i")], [@n]);
 }

#latest:;
if (1)                                                                          #TsetSizeBits #TsetSizeWords
 {my $c = newChip();
  $c->setSizeBits ('i', 2);
  $c->setSizeWords('j', 3, 2);
  is_deeply($c->sizeBits,  {i => 2, j_1 => 2, j_2 => 2, j_3 => 2});
  is_deeply($c->sizeWords, {j => [3, 2]});
 }

#latest:;
if (1)                                                                          #TconnectInputBits
 {my $N = 5; my $B = 5;
  my $c = newChip();
  $c->bits      ('a', $B, $N);
  $c->inputBits ('i', $N);
  $c->outputBits(qw(o i));
  $c->connectInputBits(qw(i a));                                                # The input gates will get squeezed out
  my $s = $c->simulate({}, svg=>q(connectInputBits), pngs=>1, newChange=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps,  2);
  is_deeply($s->levels, 1);
  is_deeply($s->bInt("o"), $N);
 }

#latest:;
if (1)                                                                          #TconnectInputWords
 {my $W = 6; my $B = 5;
  my $c = newChip();
  $c->words      ('a',     $B, 1..$W);
  $c->inputWords ('i', $W, $B);
  $c->outputWords(qw(o i));
  $c->connectInputWords(qw(i a));
  my $s = $c->simulate({}, svg=>q(connectInputWords), pngs=>1, newChange=>1, borderDy=>16);
  ok($s->checkLevelsMatch);
  is_deeply($s->steps, 2);
  is_deeply([$s->wInt("o")], [1..$W]);
 }

#latest:;
if (1)                                                                          # Collapse left
 {my $c = Silicon::Chip::newChip;
  $c->input ('a');
  $c->input (             n('ia', $_)) for 1..8;
  $c->and   ('aa',  [map {n('ia', $_)}     1..8]);
  $c->output('oa', 'aa');
  $c->not   ('n1', 'a'); $c->output('o1', 'n1');
  $c->input (             n('ib', $_)) for 1..8;
  $c->and   ('ab',  [map {n('ib', $_)}     1..8]);
  $c->output('ob', 'ab');
  $c->not   ('n2', 'a'); $c->output('o2', 'n2');
  my %a = map {(n('ia', $_)=>1)} 1..8;
  my %b = map {(n('ib', $_)=>1)} 1..8;
  my $s = $c->simulate({%a, %b, a=>0}, svg=>q(collapseLeft), pngs=>1,
       gsx=>3, gsy=>3, spaceDx=>0, spaceDy=>1, newChange=>1, borderDy=>4);
  ok($s->checkLevelsMatch);
 }

#latest:;
if (1)                                                                          #Tsimulate
 {my $N = 2;
  my $c = Silicon::Chip::newChip;
  $c->input (n('i', $_))                              for 1..$N;
  $c->xor   (n("x", $_),   n('i', $_-1), n('i', $_))  for 2..$N;
  $c->and   ('a',    [map {n('x', $_)}                    2..$N]);
  $c->output('o',  'a');
  my %a = map {(n('i', $_)=> $_ % 2)} 1..$N;
  my $s = $c->simulate({%a}, svg=>q(square1), pngs=>1);
  ok($s->checkLevelsMatch);
 }

#latest:;
if (1)                                                                          #TfanOut
 {my $N = 2;
  my $c = Silicon::Chip::newChip;
  $c->input ('i');
  $c->fanOut(     [qw(f1 f2)], 'i');
  $c->and   ('a', [qw(f1 f2)]);
  $c->output('o',  'a');
  my $s = $c->simulate({i=>1}, svg=>q(fan2), pngs=>1, newChange=>1);
  ok($s->checkLevelsMatch);
  is_deeply($s->value("o"), 1);
 }

#latest:;
if (1)                                                                          #
 {my $N = 2;
  my $c = Silicon::Chip::newChip(expand=>1);
  $c->input ('i');
  $c->continue($_, q(i)) for qw(a b c d);
  $c->and   (      'A',     [qw(a b c d)]);
  $c->output('o',  'A');
  my $s = $c->simulate({i=>1}, svg=>q(addfan2), pngs=>1, spaceDy=>1, newChange=>1, minHeight=>20, gsx=>2, gsy=>2);
  ok($s->checkLevelsMatch);
  is_deeply($s->value("o"), 1);
 }

#latest:;
if (1)                                                                          #TSilicon::Chip::Simulation::checkLevelsMatch
 {my $N = 2;
  my $c = Silicon::Chip::newChip;
  $c->input (qw(  i));
  $c->not   (qw(n i));
  $c->output(qw(o n));
  my $s = $c->simulate({i=>1}, svg=>q(gdsTest), pngs=>1, gsx=>4, gsy=>4);
  ok($s->checkLevelsMatch);
 }

done_testing();
say STDERR sprintf "Finished in %9.4f seconds", time - $start;
finish: 1;
*/
