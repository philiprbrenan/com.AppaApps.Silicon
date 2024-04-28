//------------------------------------------------------------------------------
// Design, simulate and layout a silicon chip made of basic gates.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

public class Chip                                                               // Describe a chip and emulate its operation.
 {final static int maxSimulationSteps = 100;                                    // Maximum simulation steps
  final static int          debugMask =   0;                                    // Adds a grid and fiber names to a mask to help debug fibers if true.
  final static int      pixelsPerCell =   4;                                    // Pixels per cell
  final static int     layersPerLevel =   4;                                    // There are 4 layers in each level: insulation, x cross bars, x-y connectors and insulation, y cross bars
  final static Stack<String>  gdsPerl = new Stack<>();                          // Perl code to create GDS2 output files
  final TreeSet<String>   outputGates = new TreeSet<>();                        // Output gates
  final TreeMap<String, TreeSet<Gate.DrivePin>>                                 // Pending gate definitions
                              pending = new TreeMap<>();
  int                         gateSeq =   0;                                    // Gate sequence number - this allows us to display the gates in the order they were defined to simplify the understanding of drawn layouts
  int                           steps =   0;                                    // Simulation step time
  int         maximumDistanceToOutput;                                          // Maximum distance from an output
  int      countAtMostCountedDistance;                                          // The distance at which we have the most gates from an output
  int             mostCountedDistance;                                          // The number of gates at the distance from the drives that has the most gates
  final TreeMap<Integer, TreeSet<String>>                                       // Classify gates by distance to output
                      distanceToOutput = new TreeMap<>();
  final TreeMap<Integer, Stack<String>>                                         // Each column of gates ordered by Y coordinate in layout
                               columns = new TreeMap<>();
  final static TreeSet<String>
                          layoutsDrawn = new TreeSet<>();                       // Avoid redrawing the same layout multiple times
  int                        gsx, gsy;                                          // The global scaling factors to be applied to the dimensions of the gates during layout
  int                layoutX, layoutY;                                          // Dimensions of chip
  Stack<Connection>       connections;                                          // Pairs of gates to be connected
  Diagram                     diagram;                                          // Diagram specifying the layout of the chip

  enum Operator                                                                 // Gate operations
   {And, Continue, FanOut, Gt, Input, Lt, Nand, Ngt, Nlt, Nor, Not, Nxor,
    One, Or, Output, Xor, Zero;
   }

  boolean commutative(Operator op)                                              // Whether the pin order matters on the gate or not
   {return op != Operator.Gt  && op != Operator.Lt &&
           op != Operator.Ngt && op != Operator.Nlt;
   }

  final String name;                                                            // Name of chip
  final Map<String, Gate>        gates = new TreeMap<>();                       // Gates by name
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

  static int powerTwo(int n)                                                    // Power of 2
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

  boolean definedGate(String name)                                              // Check whether a gate has been defined yet
   {final Gate g = gates.get(name);
    return g != null;
   }

  Gate getGate(String name)                                                     // Get details of named gate
   {if (name == null) stop("No gate name provided");
    final Gate g = gates.get(name);
    //if (g == null) stop("No such gate:", name, ". Gates must be created before they can be referenced.");
    return g;
   }

  public String toString()                                                      // Convert chip to string
   {final StringBuilder b = new StringBuilder();
    b.append("Chip: "+name+" # Gates: "+ gates.size()+"  Maximum distance: " + maximumDistanceToOutput);
    b.append("  mostCountedDistance: "            + mostCountedDistance);
    b.append("  countAtMostCountedDistance: "     + countAtMostCountedDistance);
    b.append("\n");
    b.append("Seq   Name      Operator  #  11111111-P=#  22222222-P=#  C Frst Last  Dist   Nearest  Px__,Py__  Drives these gates\n");
    for(Gate g : gates.values()) b.append(g);

    final int P = pending.size();                                               // Write pending gates
    if (P > 0)
     {b.append(""+P+" pending gates\n");
      b.append("Source__  Target__\n");
      for(String n : pending.keySet())
        for(Gate.DrivePin d : pending.get(n))
          b.append(String.format("%8s  %8s  %c\n",
            n, d.drives, d.pin == null ? ' ' : d.pin ? '1' : '2'));
     }
    return b.toString();                                                        // String describing chip
   }

  class Gate                                                                    // Description of a gate
   {final int               seq = nextGateNumber();                             // Sequence number for this gate
    final String           name;                                                // Name of the gate.  This is also the name of the output of the gate. FanOut gates have a second output which is the name suffixed by the secondary marker. The secondary marker is not normally used in a gate name so a name that ends in ! is easily recognized as the second output of a fan out gate.
    final Operator           op;                                                // Operation performed by gate
    Gate  iGate1,  iGate2;                                                      // Gates driving the inputs of this gate as during simulation but not during layout
    Gate soGate1, soGate2, tiGate1, tiGate2;                                    // Pin assignments on source and target gates used during layout but not during simulation
    TreeSet<DrivePin>    drives = new TreeSet<>();                              // The names of the gates that are driven by the output of this gate with a possible pin selection attached
    Integer    distanceToOutput;                                                // Distance to nearest output
    Boolean               value;                                                // Current output value of this gate
    boolean             changed;                                                // Changed on current simulation step
    int        firstStepChanged;                                                // First step at which we changed
    int         lastStepChanged;                                                // Last step at which we changed
    String        nearestOutput;                                                // The name of the nearest output so we can sort each layer to position each gate vertically so that it is on approximately the same Y value as its nearest output.
    int                  px, py;                                                // Position in x and y of gate in latest layout

    String drives()                                                             // Convert drives to a printable string
     {final StringBuilder b = new StringBuilder();
      for(DrivePin s : drives) b.append(s + ", ");
      if (b.length() > 0) b.delete(b.length() - 2, b.length());
      return b.toString();
     }

    class DrivePin implements Comparable<DrivePin>                              // Shows that this gate drives another gate either on a specific pin or on any pin if the gate is commutative
     {final String drives;                                                      // Drives this named gate
      final Boolean   pin;                                                      // Null can drive any pin on target, true - must drive input pin 1, false - must drive input pin 2

      DrivePin(String Drives, Boolean Pin) {drives = Drives; pin = Pin;}        // Construct a pin drive specification targeting a specified input pin
      DrivePin(String Drives)              {this(Drives, null);}                // Construct a pin drive specification targeting any available input pin
      Gate gate() {return getGate(drives);}                                     // Details of gate being driven

      public int compareTo(DrivePin a) {return drives.compareTo(a.drives);}     // So we can add and remove entries from the set of driving gates

      public String toString()                                                  // Convert drive to string
       {if (pin == null) return drives;
        if (pin)         return drives+">1";
                         return drives+">2";
       }
      boolean ok1() {return pin == null ||  pin;}                               // Can drive the first pin
      boolean ok2() {return pin == null || !pin;}                               // Can drive the second pin
     }

    public String toString()                                                    // Convert to string
     {final char v = value == null ? '.' : value ? '1' : '0';

      if (op == Operator.Input)
        return String.format("%4d  %8s  %8s  %c"+" ".repeat(59)+"%4d,%4d  ",
          seq, name, op, v, px, py) + drives() + "\n";

      final Boolean pin1 = iGate1 != null ? whichPinDrivesPin1() : null;
      final Boolean pin2 = iGate2 != null ? whichPinDrivesPin2() : null;

      return   String.format("%4d  %8s  %8s  %c  %8s-%c=%c  %8s-%c=%c  %c %4d %4d  %4d  %8s  %4d,%4d  ",
        seq, name, op, v,
        iGate1 == null ? ""  : iGate1.name,
        pin1   == null ? '.' : pin1  ? '1' : '2',
        iGate1 == null ? '.' : iGate1.value == null ? '.' : iGate1.value ? '1' : '0',
        iGate2 == null ? ""  : iGate2.name,
        pin2   == null ? '.' : pin2  ? '1' : '2',
        iGate2 == null ? '.' : iGate2.value == null ? '.' : iGate2.value ? '1' : '0',
        changed ? '1' : '0',
        firstStepChanged,
        lastStepChanged,
        distanceToOutput,
        nearestOutput != null ? nearestOutput : "",
        px, py
        ) + drives() + "\n";
     }

    private Gate(Operator Op)                                                   // System created gate of a specified type with a unique system generated name. As yet the inputs are unknonw.
     {op = Op;
      name = ""+seq;
      gates.put(name,  this);
     }

    public Gate(Operator Op, String Name, String Input1, String Input2)         // User created gate with a user supplied name and inputs
     {name = validateName(Name);
      op   = Op;
      gates.put(name, this);
      if (commutative(op))                                                      // Any input pin will do
       {if (Input1 != null) impinge(Input1);
        if (Input2 != null) impinge(Input2);
       }
      else                                                                      // Input pin order is important
       {if (Input1 == null || Input2 == null) stop("Non commutative gates must have two inputs", Name, Op, Input1, Input2);
        impinge(Input1, true);
        impinge(Input2, false);
       }
      final TreeSet<DrivePin> d = pending.get(name);                            // Add any pending gate references to this gate definition
      if (d != null)
       {pending.remove(name);
        for(DrivePin p : d)
         {drives.add(new DrivePin(p.drives, p.pin));
         }
       }
     }

    void impinge(String Input)                                                  // Go to the named gate (which must therefore already exist) and show that it drives this gate on any input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new DrivePin(name));                                       // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have nbot been defined yet so add the impinging definitions to pending
       {TreeSet<DrivePin>  d = pending.get(Input);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input, d);}
        d.add(new DrivePin(name));
       }
     }

    void impinge(String Input, Boolean pin)                                     // Go to the named gate (which must therefore already exist) and show that it drives this gate on the specified input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new DrivePin(name, pin));                                  // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have nbot been defined yet so add the impinging definitions to pending
       {TreeSet<DrivePin>  d = pending.get(Input);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input, d);}
        d.add(new DrivePin(name, pin));
       }
     }

    boolean whichPinDrivesPin(String driving)                                   // True if the driving gate is using pin 1 to drive this gate, else false meaning it is using pin 2
     {final Gate d = getGate(driving);                                          // Driving gate
      if (d == null) stop("Invalid gate name:", driving);                       // No driving gate
      if (d.drives.size() == 0)
       {stop("Gate name:", driving, "is referenced as driving gate:", name,
             "but does not drive it");
        return false;
       }
      return name.equals(d.drives.first());                                     // First/Second output pin drives first input pin
     }

    boolean  whichPinDrivesPin1()                                               // True if output pin 1 drives input pin 1, false if output pin 2 drives pin 1, else null if not known
     {return whichPinDrivesPin(iGate1.name);                                    // Gate driving pin 1
     }

    boolean  whichPinDrivesPin2()                                               // True if output pin 1 drives input pin 2, false if output pin 2 drives pin 2, else null if not known
     {return whichPinDrivesPin(iGate2.name);                                    // Gate driving pin 2
     }

    void compileGate()                                                          // Locate inputs for gate so we do not have to look them up every time
     {final int N = drives.size();

      if (N == 0)                                                               // An output gate does not drive any other gate
       {if (op == Operator.Output) return;
        stop("Gate", name, "does not drive any other gate");
        return;
       }

      if (N > 2) stop("Gate", name, "drives", N,                                // At this point each gate should have no more than two inputs
        "gates, but a gate can drive no more than 2 other gates");

      for (DrivePin t : drives)                                                 // Connect targeted gates back to this gate
       {final Gate T = t.gate();
        if      (T.iGate1 == this || T.iGate2 == this) {}                       // Already set
        else if (T.iGate1 == null && t.ok1()) T.iGate1 = this;                  // Use input pin 1
        else if (T.iGate2 == null && t.ok2()) T.iGate2 = this;                  // Use input pin 2
        else                                                                    // No input pin available
         {say(this);
          if (t.pin == null) stop("Gate:", T.name,
            "driven by too many other gates, including one from gate:", name);
          else               stop("Gate:", T.name,
            "does not have enough pins to be driven by:", t, "from", name);
         }
       }
     }

    String validateName(String name)                                            // Confirm that a component name looks like a variable name and has not already been used
     {final String[]words = name.split("_");
      for (int i = 0; i < words.length; i++)
       {final String w = words[i];
        if (!w.matches("\\A([a-zA-Z][a-zA-Z0-9_.:]*|\\d+)\\Z"))
          stop("Invalid gate name:", name, "in word", w);
       }
      if (gates.containsKey(name)) stop("Gate:", name, "has already been used");
      return name;
     }

    void updateValue(Boolean Value)                                             // Update the value of the gate
     {changed = value != Value;
      value   = Value;
     }

    void step()                                                                 // One step in the simulation
     {final Boolean g = iGate1 != null ? iGate1.value : null,
                    G = iGate2 != null ? iGate2.value : null;
      Boolean value = null;
      switch(op)                                                                // Gate operation
       {case And: if (g != null && G != null) updateValue(  g &&  G);  return;
        case Gt:  if (g != null && G != null) updateValue(  g && !G);  return;
        case Lt:  if (g != null && G != null) updateValue( !g &&  G);  return;
        case Nand:if (g != null && G != null) updateValue(!(g &&  G)); return;
        case Ngt: if (g != null && G != null) updateValue( !g ||  G);  return;
        case Nlt: if (g != null && G != null) updateValue(  g || !G);  return;
        case Nor: if (g != null && G != null) updateValue(!(g ||  G)); return;
        case Not: if (g != null)              updateValue( !g);        return;
        case Nxor:if (g != null && G != null) updateValue(!(g ^   G)); return;
        case One:                             updateValue(true);       return;
        case Or:  if (g != null && G != null) updateValue(  g ||  G);  return;
        case Xor: if (g != null && G != null) updateValue(  g ^   G);  return;
        case Zero:                            updateValue( false);     return;
        case Continue: case FanOut: case Output:
                                              updateValue(g);          return;
       }
     }

    void fanOut()                                                               // Fan out when more than two gates are driven by this gate
     {final int N = drives.size();
      if (op == Operator.Output) return;                                        // Output gates do not fan out
      if (N <= 2) return;                                                       // No fan out required

      final DrivePin[]D = drives.toArray(new DrivePin[drives.size()]);

      if (N % 2 == 1)                                                           // Odd number of gates driven
       {final Gate g = new Gate(Operator.FanOut);                               // The new fan out
        for(int i = 0; i < N-1; ++i)
         {final DrivePin d = D[i];
          g.drives.add (d);                                                     // Put the target to the newly created gate
          drives.remove(d);                                                     // Remove the target from the original gate
         }
        drives.add(new DrivePin(g.name));                                       // The old gate drives the new gate
        g.fanOut();                                                             // The even gate might need further fan put
        return;
       }

      final Gate g = new Gate(Operator.FanOut), f = new Gate(Operator.FanOut);  // Even and greater than 2
      for(int i = 0; i < N/2; ++i)                                              // Lower half
       {final DrivePin d = D[i];
        g.drives.add(d);
        drives.remove(d);
       }
      drives.add(new DrivePin(g.name));                                         // The old gate drives the new gate
      g.fanOut();                                                               // The lower half gate might need further fan out

      for(int i = N/2; i < N; ++i)                                              // Upper half
       {final DrivePin d = D[i];
        f.drives.add(d);
        drives.remove(d);
       }
      drives.add(new DrivePin(f.name));                                         // The old gate drives the new gate
      f.fanOut();                                                               // The upper half gate might need further fan out
     }
   } // Gate

  Gate FanIn(Operator Op, String Name, String...Input)                          // Normal gate - not a fan out gate
   {final int L = Input.length;
    if (L == 0) return new Gate(Op, Name, null,     null);                      // Input gates have no driving gates
    if (L == 1) return new Gate(Op, Name, Input[0], null);                      // Input gate have no driving gates
    if (L == 2) return new Gate(Op, Name, Input[0], Input[1]);                  // Input gate have no driving gates

    if (L % 2 == 1)                                                             // Odd fan in
     {final Gate f = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input,0,L-1));// Divisible by two
      return new Gate(Op, Name, f.name, Input[L-1]);                            // Consolidation of the two gates
     }

    final Gate f = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input, 0, L/2));// Even fan out
    final Gate g = FanIn(Op, nextGateName(), Arrays.copyOfRange(Input, L/2, L));
    return new Gate(Op, Name, f.name, g.name);                                  // Consolidation of the two gates
   }

  Gate Input    (String n)                       {return FanIn(Operator.Input,    n);}
  Gate One      (String n)                       {return FanIn(Operator.One,      n);}
  Gate Zero     (String n)                       {return FanIn(Operator.Zero,     n);}
  Gate Output   (String n, String i)             {return FanIn(Operator.Output,   n, i);}
  Gate Continue (String n, String i)             {return FanIn(Operator.Continue, n, i);}
  Gate Not      (String n, String i)             {return FanIn(Operator.Not,      n, i);}

  Gate Nxor     (String n, String i1, String i2) {return FanIn(Operator.Nxor, n, i1, i2);}
  Gate Xor      (String n, String i1, String i2) {return FanIn(Operator.Xor,  n, i1, i2);}
  Gate Gt       (String n, String i1, String i2) {return FanIn(Operator.Gt,   n, i1, i2);}
  Gate Ngt      (String n, String i1, String i2) {return FanIn(Operator.Ngt,  n, i1, i2);}
  Gate Lt       (String n, String i1, String i2) {return FanIn(Operator.Lt,   n, i1, i2);}
  Gate Nlt      (String n, String i1, String i2) {return FanIn(Operator.Nlt,  n, i1, i2);}

  Gate And      (String n, String...i)           {return FanIn(Operator.And,  n, i);}
  Gate Nand     (String n, String...i)           {return FanIn(Operator.Nand, n, i);}
  Gate Or       (String n, String...i)           {return FanIn(Operator.Or,   n, i);}
  Gate Nor      (String n, String...i)           {return FanIn(Operator.Nor,  n, i);}

  class WordBus                                                                 // Description of a word bus
   {final int bits;                                                             // Bits in each word of the bus
    final int words;                                                            // Words in bus
    WordBus(int Words, int Bits) {bits = Bits; words = Words;}                  // Create bus
   }

  void distanceToOutput()                                                       // Distance to the nearest output
   {outputGates.clear();
    for(Gate g : gates.values())                                                // Each gate
     {g.distanceToOutput = null;                                                // Reset distance
      if (g.op == Operator.Output)                                              // Locate drives
       {outputGates.add(g.name);
        g.nearestOutput = g.name;
       }
     }

    TreeSet<String> check = outputGates;                                        // Search from the drives
    for (int d = 0; d < gates.size() && check.size() > 0; ++d)                  // Set distance to nearest output
     {final TreeSet<String> next = new TreeSet<>();
      maximumDistanceToOutput    = d;                                           // Maximum distance from an output

      final TreeSet<String>   to = new TreeSet<>();                             // Gates at this distance from drives
      distanceToOutput.put(d, to);

      for(String name : check)                                                  // Expand search one level
       {final Gate g = getGate(name);
        if (g.distanceToOutput == null)                                         // New gate
         {to.add(name);                                                         // Classify gate by distance from gate
          g.distanceToOutput = d;                                               // Distance to nearest output
          if (g.iGate1 != null)
           {next.add(g.iGate1.name);
            g.iGate1.nearestOutput = g.nearestOutput;
           }
          if (g.iGate2 != null)
           {next.add(g.iGate2.name);
            g.iGate2.nearestOutput = g.nearestOutput;
           }
         }
       }
      check = next;
     }

    countAtMostCountedDistance = 0;                                             // The distance at which we have the most gates from an output
    mostCountedDistance   = 0;                                                  // The number of gates at the distance from the drives that has the most gates
    for (Integer s : distanceToOutput.keySet())
     {final TreeSet<String> d = distanceToOutput.get(s);
      if (d.size() > mostCountedDistance)
       {countAtMostCountedDistance = d.size();
        mostCountedDistance   = s;
       }
     }
   }

  void compileChip()                                                            // Check that an input value has been provided for every input pin on the chip.
   {final Gate[]G = gates.values().toArray(new Gate[0]);
    for(Gate g : G) g.fanOut();                                                 // Fan the output of each gate if necessary which might introduce more gates
    for(Gate g : gates.values()) g.compileGate();                               // Each gate on chip
    distanceToOutput();                                                         // Output gates
   }

  void noChangeGates()                                                          // Mark each gate as unchanged
   {for(Gate g : gates.values()) g.changed = false;
   }

  boolean changes()                                                             // Check whether any changes were made
   {for(Gate g : gates.values()) if (g.changed) return true;
    return false;
   }

  void initializeInputGates(Inputs inputs)                                      // Initialize the output of each input gate
   {noChangeGates();
    for(Gate g : gates.values())
     {g.value = null; g.lastStepChanged = 0;
      if (g.op == Operator.Input)                                               // Input gate
       {if (inputs != null)                                                     // Initialize
         {final Boolean v = inputs.get(g.name);
          if (v != null) g.value = v;
          else stop("No input value provided for gate:", g.name);
         }
        else stop("Input gate", g.name, "has no initial value");
       }
     }
   }

  class Inputs                                                                  // Set values on input gates prior to simulation
   {private final Map<String,Boolean> inputs = new TreeMap<>();

    void set(String s, boolean value)                                           // Set the value of an input
     {if (inputs.containsKey(s)) stop("Input", s, "already defined");
      inputs.put(s, value);
     }

    Boolean get(String s) {return inputs.get(s);}                               // Get the value of an input
   }

  Diagram singleLevelLayout()                                                   // Try different gate scaling factors in hopes of finding a single level wiring diagram.  Returns the wiring diagram with the fewest wiring levels found.
   {Diagram D = null;
    Integer L = null;
    for    (int s = 2; s < 10; ++s)                                             // Various gate scaling factors
     {for  (int y = 1; y < s;  ++y)                                             // Various gate scaling factors
       {for(int x = 1; x < s;  ++x)
         {if (x + y == s)                                                       // Order by overall scaling favoring x
           {final Diagram d = layout(x, y);
            final int l = d.levels();
            if (l == 1) return d;
            if (L == null || L > l) {L = l; D = d;}                             // Track smallest number of wiring levels
           }
         }
       }
     }
    return D;
   }

  Diagram simulate() {return simulate(null);}                                   // Simulate the operation of a chip with no input pins. If the chip has in fact got input pins an error will be reported.

  Diagram simulate(Inputs inputs)                                               // Simulate the operation of a chip
   {compileChip();                                                              // Check that the inputs to each gate are defined
    initializeInputGates(inputs);                                               // Set the drives of each input gate
    for(steps = 1; steps < maxSimulationSteps; ++steps)                         // Steps in time
     {for(Gate g : gates.values()) g.step();
      if (!changes())                                                           // No changes occurred
       {final Diagram d = singleLevelLayout();
        if (d.levels() > 1) say(String.format("%8s  %4d %4d  %2d",              // Print layouts that run into multiple levels
          name, d.gsx, d.gsy, d.levels()));
        return d;
       }
      noChangeGates();                                                          // Reset change indicators
     }

    stop("Out of time after", maxSimulationSteps, "steps");                     // Not enough steps available
    return null;
   }

//D1 Buses                                                                      // A bus is an array of bits or an array of arrays of bits

  static String n(int i, String...C)                                            // Gate name from single index.
   {return String.join("_", C)+"_"+i;
   }

  static String nn(int i, int j, String...c)                                    // Gate name from double index.
   {return String.join("_", c)+"_"+i+"_"+j;
   }

//D2 Bits                                                                       // An array of bits that can be manipulated via one name.

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
      if (b.elementAt(i-1)) One(n(i, name)); else Zero(n(i, name));             // Set bit

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
    Nor(name, b);                                                               // Or all the bits in the bus
   }

  Integer bInt(String output)                                                   // Convert the bits represented by an output bus to an integer
   {final int B = sizeBits(output);                                             // Number of bits in bus
    int v = 0, p = 1;
    for (int i = 1; i <= B; i++)                                                // Each bit on bus
     {final String n = n(i, output);                                            // Name of gate supporting named bit
      final Gate g = getGate(n);                                                // Gate providing bit
      if (g.value == null) return null;                                         // Bit state not known
      if (g.value) v += p;
      p *= 2;
     }
    return v;
   }

//D2 Words                                                                      // An array of arrays of bits that can be manipulated via one name.

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
      And(n(b, name), words.toArray(new String[words.size()]));                 // Combine inputs using B<and> gates
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
      Or(n(b, name), words.toArray(new String[words.size()]));                  // Combine inputs using B<or> gates
     }
    setSizeBits(name, wb.bits);                                                 // Record bus size
   }

//D1 Comparisons                                                                // Compare unsigned binary integers of specified bit widths.

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

    for(int j = 2; j <= B; ++j)                                                 // More than on one bit and all preceding bits are equal
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
   {final int A = sizeBits(a), B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A, ", but second input bus", b, "has width", B);
    for (int i = 2; i <= B; i++) Nxor(n(i, output, "e"), n(i, a), n(i, b));     // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Lt  (n(i, output, "l"), n(i, a), n(i, b));     // Test each bit pair for more than

    for(int j = 2; j <= B; ++j)                                                 // More than on one bit and all preceding bits are equal
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
   {final int A = sizeBits(a), B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A, ", but second input bus", b, "has width", B);

    final String notChoose = nextGateName();                                    // Opposite of choice
    Not(notChoose, choose);                                                     // Invert choice

    for (int i = 1; i <= B; i++)                                                // Each bit
     {And(n(i, output, "a"), n(i, a), notChoose);                               // Choose first word if not choice
      And(n(i, output, "b"), n(i, b),    choose);                               // Choose second word if choice
      Or (n(i, output),      n(i, output, "a"), n(i, output, "b"));             // Or results of choice
     }
    setSizeBits(output, B);                                                     // Record bus size
   }

  void enableWord(String output, String a, String enable)                       // Output a word or zeros depending on a choice bit.  The first word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
   {final int A = sizeBits(a);
    for (int i = 1; i <= A; i++) And(n(i, output), n(i, a), enable);            // Choose each bit of input word
    setSizeBits(output, A);                                                     // Record bus size
   }

//D1 Masks                                                                      // Point masks and monotone masks. A point mask has a single B<1> in a sea of B<0>s as in B<00100>.  A monotone mask has zero or more B<0>s followed by all B<1>s as in: B<00111>.

  void monotoneMaskToPointMask(String output, String input)                     // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
   {final int B = sizeBits(input);                                              // Number of bits in input monotone mask

    for (int i = 1; i <= B;  i++)                                               // Each bit in each possible output number
      if (i > 1) Lt(n(i, output), n(i-1, input), n(i, input));                  // Look for a step from 0 to 1
      else Continue(n(i, output),                n(i, input));                  // First bit is 1 so point is in the first bit

    setSizeBits(output, B);                                                     // Size of resulting bus representing the chosen integer
   }

  void chooseWordUnderMask(String output, String input, String mask)            // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
   {final WordBus wb = sizeWords(input);
    final int mi     = sizeBits (mask);
    if (mi != wb.words)
      stop("Mask width", mi, "does not match number of words ", wb.words);

    for   (int w = 1; w <= wb.words; ++w)                                       // And each bit of each word with the mask
      for (int b = 1; b <= wb.bits;  ++b)                                       // Bits in each word
        And(nn(w, b, output, "a"), n(w, mask), nn(w, b, input));

    for   (int b = 1; b <= wb.bits; ++b)                                        // Bits in each word
     {final Stack<String> or = new Stack<>();
      for (int w = 1; w <= wb.words; ++w) or.push(nn(w, b, output, "a"));       // And each bit of each word with the mask
      Or(n(b, output), stackToStringArray(or));
     }
    setSizeBits(output, wb.bits);
   }

//D1 Layout                                                                     // Layout the gates and connect them with wires

  Stack<String> sortIntoOutputGateOrder(int Distance)                           // Order the gates in this column to match the order of the output gates
   {final Stack<String> r = new Stack<>();                                      // Order of gates

    if (Distance == 0)                                                          // Start at the output gates
     {for(String o : outputGates) r.push(o);                                    // Load output gates in name order
      columns.put(Distance, r);                                                 // Save the gates in this column ordered by y position
      return r;                                                                 // Return output gates
     }

    final TreeMap<String, TreeSet<String>> order = new TreeMap<>();             // Gates connected to nearest gates in previous column
    for(String G : columns.get(Distance-1))
     {final TreeSet<String> p = new TreeSet<>();                                // Gates nearest
      order.put(G, p);
      final Gate g = getGate(G);                                                // Gate details
      if (g.iGate1 != null) p.add(g.iGate1.name);                               // Connect the current layer to previous layer
      if (g.iGate2 != null) p.add(g.iGate2.name);
     }

    for(String o: order.keySet()) for(String g: order.get(o)) r.push(g);        // Stack the gates in this column in output gate order
    columns.put(Distance, r);                                                   // The gates in this column ordered by y position
    return r;                                                                   // The gates in this column ordered by y position
   }

  class Connection                                                              // Pairs of gates which we wish to connect
   {final Gate source, target;                                                  // Source and target gates for connection
    Connection(Gate Source, Gate Target)                                        // Pair of gates between which we wish to connect
     {source = Source;
      target = Target;
      if (source.drives.size() == 1)                                            // One drive specification on source
       {final Gate.DrivePin p = source.drives.first();
        if (p.pin != null)
         {if (p.pin) target.tiGate1 = source; else target.tiGate2 = source;     // Layout as much of the connection as we can at this point
         }
       }
      else if (source.drives.size() == 2)                                       // Two drive specifications on source
       {final Gate.DrivePin f = source.drives.first();
        final Gate.DrivePin l = source.drives. last();
        final Gate.DrivePin p = f.drives.equals(target.name) ? f : l;           // The drive between the two gates
        if (p.pin != null)
         {if (p.pin) target.tiGate1 = source; else target.tiGate2 = source;     // Layout as much of the connection as we can at this point
         }
       }
     }
   }

  Diagram layout(int Gsx, int Gsy)                                              // Layout with gates scaled by gsx and gsy.  Normally gates are 2 by 2 in size, unless globally magnified by these factors.
   {gsx = Gsx; gsy = Gsy;

    final int sx =  2 * gsx, sy = 2 * gsy;                                      // Size of gate
//FIX need better estimate of size
    layoutX = sx * 2 * maximumDistanceToOutput;                                 // X dimension of chip
    layoutY = sy * 2 * countAtMostCountedDistance;                              // Y dimension of chip

    compileChip();

    for (Gate g : gates.values()) g.px = g.distanceToOutput * sx;               // Locate gate in x

    final TreeSet<String> drawn = new TreeSet<>();                              // Gates that have already been drawn and so do not need to be redrawn

    for   (Integer D : distanceToOutput.keySet())                               // Gates at each distance from the drives
     {final TreeSet<String> d = distanceToOutput.get(D);                        // Gates in this column
      final int             N = d.size();                                       // Number of gates in this column
      final float          dy = layoutY/ (float)N - sy;                         // Extra space available to spread gates down column
      float             extra = 0;                                              // Extra space accumulated as we can only use it in increments of gsy
      int                   y = gsy;                                            // Current y position in column.

      for (String name : sortIntoOutputGateOrder(D))                            // Gates at each distance from the drives
       {final Gate g = getGate(name);
        if (drawn.contains(name)) continue;                                     // Only draw each named gate once
        drawn.add(name);                                                        // Show that we have drawn this gate already
        g.py = y; y += sy; extra += dy;
        for(; extra >= gsy; extra -= gsy) y += gsy;                             // Enough extra has accumulated to be distributed
       }
     }

    connections = new Stack<>();                                                // Connections required

    for (Gate g : gates.values()) g.soGate1 = g.soGate2 = g.tiGate1 = g.tiGate2 = null;

    for (Gate s : gates.values())                                               // Gates
      for (Gate.DrivePin p : s.drives)                                          // Gates driven by this gate
        connections.push(new Connection(s, p.gate()));                          // Connection needed

    for (Connection c : connections)                                            // Each possible connection
     {final Gate s = c.source, t = c.target;

      if (t.py < s.py)                                                          // Target is lower than source
       {if      ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate1 == null) {t.tiGate2 = s; s.soGate1 = t;}
        else if ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate1 == null) {t.tiGate1 = s; s.soGate1 = t;}
        else if ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate2 == null) {t.tiGate2 = s; s.soGate2 = t;}
        else if ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate2 == null) {t.tiGate1 = s; s.soGate2 = t;}
       }
      else if (t.py > s.py)                                                     // Target is higher than source
       {if      ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate2 == null) {t.tiGate1 = s; s.soGate2 = t;}
        else if ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate1 == null) {t.tiGate1 = s; s.soGate1 = t;}
        else if ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate2 == null) {t.tiGate2 = s; s.soGate2 = t;}
        else if ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate1 == null) {t.tiGate1 = s; s.soGate1 = t;}
       }
      else                                                                      // Target is same height as source
       {if      ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate1 == null) {t.tiGate1 = s; s.soGate1 = t;}
        else if ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate2 == null) {t.tiGate2 = s; s.soGate2 = t;}
        else if ((t.tiGate1 == null || t.tiGate1 == s) && s.soGate2 == null) {t.tiGate1 = s; s.soGate2 = t;}
        else if ((t.tiGate2 == null || t.tiGate2 == s) && s.soGate1 == null) {t.tiGate2 = s; s.soGate1 = t;}
       }
     }

    diagram = new Diagram(layoutX, layoutY, gsx, gsy);                          // Layout the chip as a wiring diagram

    for (Connection c : connections)                                            // Draw connections
     {final Gate s = c.source, t = c.target;
      diagram.new Wire(s.px,     s.py + (s.soGate1 == t ? 0 : gsy),
                       t.px+gsx, t.py + (t.tiGate1 == s ? 0 : gsy));
     }

    if (!layoutsDrawn.contains(name))                                           // Only draw each named chip once
     {layoutsDrawn.add(name);                                                   // Show that we have drawn this chip already
      diagram.gds2();                                                           // Draw the layout diagram as GDS2
     }
    return diagram;
   }

  class Diagram                                                                 // Wiring diagram
   {final int           width;                                                  // Width of diagram
    final int          height;                                                  // Height of diagram
    final Stack<Level> levels = new Stack<>();                                  // Wires levels in the diagram
    final Stack<Wire>   wires = new Stack<>();                                  // Wires in the diagram after they have been routed
    final int   pixelsPerCell = 4;                                              // Number of pixels along one side of a cell
    final int  crossBarOffset = 2;                                              // Offset of the pixels in each cross bar from the edge of the cell
    final int             gsx;                                                  // Gate scale factor x - number of cells between pins in X
    final int             gsy;                                                  // Gate scale factor y - number of cells between pins in Y
    final int       interViaX;                                                  // Number of pixels between pins in X
    final int       interViaY;                                                  // Number of pixels between pins in Y

    public Diagram(int Width, int Height, int Gsx, int Gsy)                     // Diagram
     {width = p(Width); height = p(Height);
      gsx   = Gsx;   gsy    = Gsy;
      interViaX = gsx * pixelsPerCell;
      interViaY = gsy * pixelsPerCell;
      new Level();                                                              // A diagram has at least one level
     }

    int p(int coord) {return pixelsPerCell * coord;}                            // Convert a gate coordinate into a pixel coordinate
    int levels() {return levels.size();}                                        // Number of wiring levels in this diagram

    class Level                                                                 // A level within the diagram
     {final boolean[][]ix = new boolean[width][height];                         // Moves in x permitted
      final boolean[][]iy = new boolean[width][height];                         // Moves in y permitted

      public Level()                                                            // Diagram
       {for   (int i = 0; i < width;  ++i)                                      // The initial moves allowed
         {for (int j = 0; j < height; ++j)
           {if (j % pixelsPerCell == crossBarOffset) ix[i][j] = true;           // This arrangement leaves room for the vertical vias that connect the levels to the sea of gates on level 0
            if (i % pixelsPerCell == crossBarOffset) iy[i][j] = true;
           }
         }
        levels.push(this);                                                      // Add level to diagram
       }

      public String toString()                                                  // Display a level as a string
       {final StringBuilder s = new StringBuilder();
        s.append("      0----+----1----+----2----+----3----+----4----+----5----+----6----+----7----+----8----+----9----+----0\n");
        for  (int j = 0; j < height; ++j)                                       // Each y cross bar
         {s.append(String.format("%4d  ", j));
          for(int i = 0; i < width;  ++i)                                       // Each x cross bar
           {final boolean x = ix[i][j], y = iy[i][j];
            final char c = x && y ? '3' : y ? '2' : x ? '1' : ' ';              // Only used for debugging so these values have no long term meaning
            s.append(c);
           }
          s.append(System.lineSeparator());
         }
        return s.toString();                                                    // String representation of level
       }
     }

    class Pixel                                                                 // Pixel on the diagram
     {final int x, y;
      public Pixel(int X, int Y) {x = X; y = Y;}
      public String toString() {return "["+x+","+y+"]";}
     }

    class Segment                                                               // Segment containing some pixels
     {Pixel corner  = null, last   = null;                                      // Left most, upper most corner; last pixel placed
      Integer width = null, height = null;                                      // Width and height of segment, the segment is always either 1 wide or 1 high.
      Boolean onX   = null;                                                     // The segment is on the x cross bar level if true else on the Y cross bar level

      Segment(Pixel p)                                                          // Start a new segment
       {corner = p;
       }

      int X() {return corner.x + (width  != null ? width  : 1);}
      int Y() {return corner.y + (height != null ? height : 1);}

      boolean add(Pixel p)                                                      // Add the next pixel to the segment if possible
       {if (corner == null)                  {corner = p;             last = p; return true;}
        else if (width == null && height == null)
         {if      (p.x == corner.x - 1)      {corner = p; width  = 2; last = p; return true;}
          else if (p.x == corner.x + 1)      {            width  = 2; last = p; return true;}
          else if (p.y == corner.y - 1)      {corner = p; height = 2; last = p; return true;}
          else if (p.y == corner.y + 1)      {            height = 2; last = p; return true;}
         }
        else if (width != null)
         {if      (p.x == corner.x - 1)      {corner = p; width++;    last = p; return true;}
          else if (p.x == corner.x + width)  {            width++;    last = p; return true;}
         }
        else if (height != null)
         {if      (p.y == corner.y - 1)      {corner = p; height++;   last = p; return true;}
          else if (p.y == corner.y + height) {            height++;   last = p; return true;}
         }
        return false;                                                           // Cannot add this pixel to the this segment
       }

      void removeFromCrossBars(Level level)                                     // Remove pixel from crossbars. The metal around the via has already been removed
       {final int w = width  != null ? width  : 0;
        final int h = height != null ? height : 0;
        final Pixel c = corner;
        final boolean F = false;
        for   (int x = 0; x <= w; ++x)                                          // We have to go one beyond the end of the segment to ensure that we do not inadvertently combine with a neighboring segment in the same cross bar.
         {for (int y = 0; y <= h; ++y)
           {if      (w > 1) level.ix[c.x+x][c.y] = F;
            else if (h > 1) level.iy[c.x][c.y+y] = F;
            else stop("Empty segment");
           }
         }
       }

      public String toString()                                                  // String representation in Perl format
       {final String level = onX == null ? ", onX=>null" : onX ? ", onX=>1" : ", onX=>0";
        if (corner      == null)                   return "{}";
        else if (width  == null && height == null) return "{x=>"+corner.x+", y=>"+corner.y+level+"}";
        else if (width  != null)                   return "{x=>"+corner.x+", y=>"+corner.y+", width=>" +width +level+"}";
        else                                       return "{x=>"+corner.x+", y=>"+corner.y+", height=>"+height+level+"}";
       }
     }

    class Search                                                                // Find a shortest path between two points in this level
     {final Level    level;                                                     // The level we are on
      final Pixel    start;                                                     // Start of desired path
      final Pixel   finish;                                                     // Finish of desired path
      Stack<Pixel>    path = new Stack<>();                                     // Path from start to finish
      Stack<Pixel>       o = new Stack<>();                                     // Cells at current edge of search
      Stack<Pixel>       n = new Stack<>();                                     // Cells at new edge of search
      short[][]          d = new short[width][height];                          // Distance at each cell
      Integer        turns = null;                                              // Number of turns along path
      short          depth = 1;                                                 // Length of path
      boolean       found;                                                      // Whether a connection was found or not

      void printD()                                                             // Print state of current search
       {final StringBuilder b = new StringBuilder();
        b.append("    ");
        for (int x = 0; x < width; ++x) b.append(String.format("%2d ", x));     // Print column headers
        b.append("\n");

        for  (int y = 0; y < height; ++y)                                       // Print body
         {b.append(String.format("%3d ", y));
          for(int x = 0; x < width;  ++x) b.append(String.format("%2d ", d[x][y]));
          say(b);
         }
       }

      boolean around(int x, int y)                                              // Check around the specified point
       {if (x < 0 || y < 0 || x >= width || y >= height) return false;          // Trying to move off the board
         if ((level.ix[x][y] || level.iy[x][y]) && d[x][y] == 0)                // Located a new unclassified cell
         {d[x][y] = depth;                                                      // Set depth for cell and record is as being at that depth
          n.push(new Pixel(x, y));                                              // Set depth for cell and record is as being at that depth
          return x == finish.x && y == finish.y;                                // Reached target
         }
        return false;
       }

      boolean search()                                                          // Breadth first search
       {for(depth = 2; depth < 999999; ++depth)                                 // Depth of search
         {if (o.size() == 0) break;                                             // Keep going until we cannot go any further

          n = new Stack<>();                                                    // Cells at new edge of search

          for (Pixel p : o)                                                     // Check cells adjacent to the current border
           {if (around(p.x,   p.y))    return true;
            if (around(p.x-1, p.y))    return true;
            if (around(p.x+1, p.y))    return true;
            if (around(p.x,   p.y-1))  return true;
            if (around(p.x,   p.y+1))  return true;
           }
          o = n;                                                                // The new frontier becomes the settled frontier
         }
        return false;                                                           // Unable to place wire
       }

      boolean step(int x, int y, int D)                                         // Step back along path from finish to start
       {if (x <  0     || y <  0)      return false;                            // Preference for step in X
        if (x >= width || y >= height) return false;                            // Step is viable?
        return d[x][y] == D;
       }

      void path(boolean favorX)                                                 // Finds a shortest path and returns the number of changes of direction and the path itself
       {int x = finish.x, y = finish.y;                                         // Work back from end point
        final short N = d[x][y];                                                // Length of path
        final Stack<Pixel> p = new Stack<>();                                   // Path
        p.push(finish);
        Integer s = null, S = null;                                             // Direction of last step
        int c = 0;                                                              // Number of changes
        for(int D = N-1; D >= 1; --D)                                           // Work backwards
         {final boolean f = favorX ? s != null && s == 0 : s == null || s == 0; // Preferred direction
          if (f)                                                                // Preference for step in X
           {if      (step(x-1, y, D)) {x--; S = 0;}
            else if (step(x+1, y, D)) {x++; S = 0;}
            else if (step(x, y-1, D)) {y--; S = 1;}
            else if (step(x, y+1, D)) {y++; S = 1;}
            else stop("Cannot retrace");
           }
          else
           {if      (step(x, y-1, D)) {y--; S = 1;}                             // Preference for step in y
            else if (step(x, y+1, D)) {y++; S = 1;}
            else if (step(x-1, y, D)) {x--; S = 0;}
            else if (step(x+1, y, D)) {x++; S = 0;}
            else stop("Cannot retrace");
           }
          p.push(new Pixel(x, y));
          if (s != null && S != null && s != S) ++c;                            // Count changes of direction
          s = S;                                                                // Continue in the indicated direction
         }
        if (turns == null || c < turns) {path = p; turns = c;}                  // Record path with fewest turns so far
       }

      boolean findShortestPath()                                                // Shortest path
       {final int x = start.x, y  = start.y;

        o.push(start);                                                          // Start
        d[x][y] = 1;                                                            // Visited start
        return search();                                                        // True if search for shortest path was successful
       }

      void setIx(int x, int y, boolean v)                                       // Set a temporarily possible position
       {if (x < 0 || y < 0 || x >= width || y >= height) return;
        level.ix[x][y] = v;
       }

      void setIy(int x, int y, boolean v)                                       // Set a temporarily possible position
       {if (x < 0 || y < 0 || x >= width || y >= height) return;
        level.iy[x][y] = v;
       }

      Search(Level Level, Pixel Start, Pixel Finish)                            // Search for path along which to place wire
       {level = Level; start = Start; finish = Finish;
        final int x = start.x, y = start.y, X = finish.x, Y = finish.y;

        if (x < 0 || y < 0)                                                     // Validate start and finish
          stop("Start out side of diagram", "x", x, "y", y);

        if (x >= width || y >= height)
          stop("Start out side of diagram", "x", x, "y", y, "width", width, "height", height);

        if (X < 0 || Y < 0)
          stop("Finish out side of diagram", "X", X, "Y", Y);

        if (X >= width || Y >= height)
          stop("Finish out side of diagram", "X", X, "Y", Y, width, height);

        if (x % interViaX > 0 || y % interViaY > 0)
          stop("Start not on a via", "x", x, "y", y, "gsx", gsx, "gsy", gsy);

        if (X % interViaX > 0 || Y % interViaY > 0)
          stop("Finish not on a via", "X", X, "Y", Y, "gsx", gsx, "gsy", gsy);

        for   (int i = 0; i < width;  ++i)                                      // Clear the searched space
          for (int j = 0; j < height; ++j)
            d[i][j] = 0;

        for  (int i = -crossBarOffset; i <= interViaX - crossBarOffset; ++i)    // Add metal around via
         {for(int j = -crossBarOffset; j <= interViaY - crossBarOffset; ++j)
           {setIx(x+i, y, true); setIx(X+i, Y, true);
            setIy(x, y+j, true); setIy(X, Y+j, true);
           }
         }

        found = findShortestPath();                                             // Shortest path
        for  (int i = -crossBarOffset; i <= interViaX - crossBarOffset; ++i)    // Remove metal around via
         {for(int j = -crossBarOffset; j <= interViaY - crossBarOffset; ++j)
           {setIx(x+i, y, false); setIx(X+i, Y, false);
            setIy(x, y+j, false); setIy(X, Y+j, false);
           }
         }

        if (found)                                                              // The found path will be from finish to start so we reverse it and remove the pixels used from further consideration.
         {path(false);  path(true);                                             // Find path with fewer turns by choosing to favour steps in y over x

          final Stack<Pixel> r = new Stack<>();
          Pixel p = path.pop(); r.push(p);                                      // Start point

          for(int i = 0; i < 999999 && path.size() > 0; ++i)                    // Reverse along path
           {final Pixel q = path.pop();                                         // Current pixel
            r.push(p = q);                                                      // Save pixel in path running from start to finish instead of from finish to start
           }
          path = r;
         }
       }
     }

    class Wire                                                                  // A wired connection on the diagram
     {final Pixel             start;                                            // Start pixel
      final Pixel            finish;                                            // End pixel
      final Stack<Pixel>       path;                                            // Path from start to finish
      final Stack<Segment> segments = new Stack<>();                            // Wires represented as a series of rectangles
      final int               level;                                            // The 1 - based  index of the level in the diagram
      final int               turns;                                            // Number of turns along path
      final boolean          placed;                                            // Whether the wire was place on the diagram or not
      final Stack<Pixel>
              crossBarInterConnects = new Stack<>();                            // The pixels at which segments on different layers in each level connect

      public String toString()                                                  // Print wire
       {return String.format("%4d %4d  %4d %4d %4d\n",
          start.x, start.y, finish.x, finish.y, level);
       }

      Wire(int x, int y, int X, int Y)                                          // Create a wire and place it
       {start = new Pixel(p(x), p(y)); finish = new Pixel(p(X), p(Y));
        Search S = null;
        for (Level l : levels)                                                  // Search each existing level for a placement
         {final Search s = new Search(l, start, finish);                        // Search
          if (s.found) {S = s; break;}                                          // Found a level on which we can connect this wire
         }
        if (S == null)                                                          // Create a new level on which we are bound to succeed of thre is no room on any existing level
         {final Level l = new Level();
          S = new Search(l, start, finish);                                     // Search
         }

        placed = S.found;                                                       // Save details of shortest path
        path   = S.path;
        turns  = S.turns != null ? S.turns : -1;
        wires.push(this);
        level  = 1 + levels.indexOf(S.level);                                   // Levels are based from 1

        collapsePixelsIntoSegments();                                           // Place pixels into segments

        for(Segment s : segments) s.onX = s.width != null;                      // Crossbar

        if (segments.size() == 1)                                               // Set levels - direct connection
         {final Segment s = segments.firstElement();
          s.onX = s.height == null;
         }
        else if (segments.size() > 1)                                           // Set levels - runs along crossbars
         {final Segment b = segments.firstElement();
          final Segment B = segments.elementAt(1);
          b.onX = B.onX;

          final Segment e = segments.lastElement();
          final Segment E = segments.elementAt(segments.size()-2);
          e.onX = E.onX;
         }

        for(Segment s : segments) s.removeFromCrossBars(S.level);               // Remove segments from crossbars
        crossBarInterConnects();                                                // Connect between cross bars
       }

      void collapsePixelsIntoSegments()                                         // Collapse pixels into segments
       {Segment s = new Segment(path.firstElement());
        segments.add(s);
        for(Pixel q : path)
         {if (s.corner != q && !s.add(q))
           {final Segment t = new Segment(s.last);
            segments.add(t);
            if (t.add(q)) s = t; else stop("Cannot add next pixel to new segment:", q);
           }
         }
       }

      void crossBarInterConnects()                                              // Connects between x and y cross bars when a wore changes layer within a level
       {Segment q = segments.firstElement();
        for(Segment p : segments)
         {if (q.onX != p.onX)                                                   // Changing levels with previous layer
           {final int qx = q.corner.x, qy = q.corner.y, px = p.corner.x, py = p.corner.y;
            final int qX = q.X(), qY = q.Y(), pX = p.X(), pY = p.Y();
            if      (qx == px && qy == py) crossBarInterConnects.push(new Pixel(qx,   qy));
            else if (qX == pX && qy == py) crossBarInterConnects.push(new Pixel(qX-1, qy));
            else if (qX == pX && qY == pY) crossBarInterConnects.push(new Pixel(qX-1, qY-1));
            else if (qx == px && qY == pY) crossBarInterConnects.push(new Pixel(qx,   qY-1));
            else stop ("No intersection between adjacent segments");
           }
          q = p;
         }
       }
     }

    public void gds2()                                                          // Represent as Graphic Design System 2 via Perl
     {final Stack<String> p = gdsPerl;
      if (p.size() == 0)
       {p.push("use v5.34;\nuse Data::Table::Text qw(:all);\nuse Data::Dump qw(dump);\nuse GDS2;");
        p.push("clearFolder(\"gds/\", 999);");
        p.push("my $debug = 0;");
       }

      p.push("if (1)");                                                         // Header for this chip
      p.push(" {my $gdsOut = \""+name+"\";");
      p.push("  my $f = \"gds/$gdsOut.gds\";");
      p.push("  say STDERR \"Chip: $gdsOut\" if $debug;");
      p.push("  createEmptyFile($f);");
      p.push("  my $g = new GDS2(-fileName=>\">$f\");");
      p.push("  $g->printInitLib(-name=>$gdsOut);");
      p.push("  $g->printBgnstr (-name=>$gdsOut);");

      for  (Gate g : gates.values())                                            // Each gate
       {p.push("# Gate: "+g);
        p.push("  if (1)");
        p.push("   {my $gsx = "+gsx+";");
        p.push("    my $gsy = "+gsy+";");
        p.push("    my $x   = "+g.px +" * 4;");
        p.push("    my $y   = "+g.py +" * 4;");
        p.push("    my $X   = $x + 6 * "+gsx+";");
        p.push("    my $Y   = $y + 6 * "+gsy+";");
        p.push("    say STDERR dump(\"Gate\", $x, $y, $X, $Y) if $debug;");
        p.push("    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);");
        p.push("    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>\""+g.name+" "+g.op+"\");");
        p.push("   }");
       }

      for  (Wire    w : wires)                                                  // Each wire
       {p.push("# Wire: "+w);
        p.push("  if (1)");
        p.push("   {my $L = "   + w.level * layersPerLevel+";");
        for(Segment s : w.segments)                                             // Each segment of each wire
         {p.push("    if (1)");
          p.push("     {my $x = "   +s.corner.x                        +";");
          p.push("      my $y = "   +s.corner.y                        +";");
          p.push("      my $X = $x+"+(s.width  == null ? 1 : s.width)  +";");
          p.push("      my $Y = $y+"+(s.height == null ? 1 : s.height) +";");
          p.push("      say STDERR dump(\"Wire\", $x, $y, $X, $Y, $L) if $debug;");
          p.push("      $g->printBoundary(-layer=>$L+"+ (s.onX ? 1 : 3)+", -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);");
          p.push("     }");
         }
        for(Pixel P : w.crossBarInterConnects)                                  // Each connection between X and Y cross bars between adjacent segments
         {p.push("    if (1)");
          p.push("     {my $x = "   + P.x +";");
          p.push("      my $y = "   + P.y +";");
          p.push("      say STDERR dump(\"Interconnect\", $x, $y, $L) if $debug;");
          p.push("      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);");
          p.push("     }");
         }
        p.push("    if (1)");                                                   // Each wire starts at a unique location and so the via can drop down from the previous layer all the way down to the gates.
        p.push("     {my $x = "   + w.start.x                          +";");
        p.push("      my $y = "   + w.start.y                          +";");
        p.push("      my $X = "   + w.finish.x                         +";");
        p.push("      my $Y = "   + w.finish.y                         +";");
        p.push("      my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];");
        p.push("      my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];");
// Levels 1,2,3 have nothing in them so that we can start each new level on a multiple of 4
        for (int i = layersPerLevel; i < w.level * layersPerLevel + (w.segments.firstElement().onX ? 0 : 2); i++)
         {p.push("      $g->printBoundary(-layer=>"+i+", -xy=>$xy);");
         }
        for (int i = layersPerLevel; i < w.level * layersPerLevel + (w.segments. lastElement().onX ? 0 : 2); i++)
         {p.push("      $g->printBoundary(-layer=>"+i+", -xy=>$XY);");
         }
        p.push("   }");
        p.push(" }");
       }
      p.push("  $g->printEndstr;");                                             // End of this chip
      p.push("  $g->printEndlib;");
      p.push(" }");
     }
   }

  static void gds2Finish()                                                      // Finish representation as Graphic Design System 2 via Perl. All the chips are written out nto one Perl file which is executed to generate the corresponding GDS2 files.
   {final Stack<String> p = gdsPerl;
    final StringBuilder b = new StringBuilder();
    for(String s : p) b.append(s+"\n");

    final String file = "perl/gds2.pl";
    try (BufferedWriter w = new BufferedWriter(new FileWriter(file)))
     {w.write(b.toString());
     }
    catch(Exception e)
     {say("Error writing to file: " + e.getMessage());
     }

    try                                                                         // Execute the file as a Perl script to create the GDS2 output - following code courtesey of Mike Limberger.
     {final var pb = new ProcessBuilder("perl", file);
      pb.redirectErrorStream(false);                                            // STDERR will be captured and returned to the caller
      final var P = pb.start();

      final var E = P.getErrorStream();                                         // Read and print STDERR
      for(int c = E.read(); c > -1; c = E.read()) System.err.print((char)c);
      E.close();

      final int rc = P.waitFor();                                               // Wait for process to finish and close it
      if (rc != 0) say("Perl script exited with code: " + rc);
     }
    catch(Exception E)
     {say("An error occurred while executing Perl script: "+file+" error: "+ E.getMessage());
      System.exit(1);
     }
   }

//D0

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
    ok(  c.steps ,    2);
   }

  static void test_delayedDefinitions()
   {final Chip c   = new Chip("And");
    final Gate o   = c.Output("o", "and");
    final Gate and = c.And   ("and", "i1", "i2");
    final Gate i1  = c.Input ("i1");
    final Gate i2  = c.Input ("i2");
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
    ok(  c.steps,    3);
   }

  static void test_zero()
   {final Chip c = new Chip("Zero");
    c.Zero("z");
    final Gate o = c.Output("o", "z");
    c.simulate();
    ok(c.steps,  3);
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

    ok(c.steps,  3);
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
    ok(c.steps,  4);
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
    ok(c.steps,  4);
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
    ok(c.steps, 4);
    ok(o.value, true);
   }

  static void test_gt2()
   {final Chip c = new Chip("Gt");
    c.One ("o");
    c.Zero("z");
    c.Gt("gt", "z", "o");
    final Gate o = c.Output("O", "gt");
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, false);
   }

  static void test_lt()
   {final Chip c = new Chip("Lt");
    c.One ("o");
    c.Zero("z");
    c.Lt("lt", "o", "z");
    final Gate o = c.Output("O", "lt");
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, false);
   }

  static void test_lt2()
   {final Chip c = new Chip("Lt");
    c.One ("o");
    c.Zero("z");
    c.Lt("lt", "z", "o");
    final Gate o = c.Output("O", "lt");
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, true);
   }

  static void test_compareEq()
   {for(int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B)-1;
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareEq "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareEq("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 ? 4 : 5);
          ok(o.value, i == j);
         }
       }
     }
   }

  static void test_compareGt()
   {for(int B = 2; B <= 4; ++B)
     {final int B2 = powerTwo(B)-1;
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareGt "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareGt("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 || B == 3 ? 5 : 6);
          ok(o.value, i > j);
         }
       }
     }
   }

  static void test_compareLt()
   {for(int B = 2; B <= 4; ++B)
     {final int B2 = powerTwo(B)-1;
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareLt "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareLt("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 || B == 3 ? 5 : 6);
          ok(o.value, i < j);
         }
       }
     }
   }

  static void test_chooseFromTwoWords()
   {for (int i = 0; i < 2; i++)
     {final int B = 4;
      final var c = new Chip("ChooseFromTwoWords "+B+" "+i);
      c.bits("a", B,  3);
      c.bits("b", B, 12);
      c.bit ("c", i);
      c.chooseFromTwoWords("o", "a", "b", "c");
      c.outputBits("out",  "o");
      c.simulate();
      ok(c.steps, 5);
      ok(c.bInt("out"), i == 0 ? 3 : 12);
     }
   }

  static void test_enableWord()
   {for (int i = 0; i < 2; i++)
     {final int B = 4;
      final var c = new Chip("EnableWord "+i);
      c.bits("a", B,  3);
      c.bit ("e", i);
      c.enableWord("o", "a", "e");
      c.outputBits("out",  "o");
      c.simulate();
      ok(c.steps, 3);
      ok(c.bInt("out"), i == 0 ? 0 : 3);
     }
   }

  static void test_monotoneMaskToPointMask()
   {for(int B = 2; B <= 4; ++B)
     {final int N = powerTwo(B);
      for (int i = 1; i <= N; i++)
       {final var c = new Chip("monotoneMaskToPointMask "+B);
        c.setSizeBits("i", N);
        for (int j = 1; j <= N; j++) c.bit(n(j, "i"), j < i ? 0 : 1);
        c.monotoneMaskToPointMask("o", "i");
        c.outputBits("out", "o");
        c.simulate();
        ok(c.bInt("out"), powerTwo(i-1));
       }
     }
   }

  static void test_chooseWordUnderMask()
   {final int B = 3, B2 = powerTwo(B);
    final int[]numbers =  {4, 3, 2, 1, 0, 1, 2, 3};
    for (int i = 0; i < B2; i++)
     {final var c = new Chip("chooseWordUnderMask "+B);
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

  public static void main(String[] args)                                        // Test if called as a program
   {test_and();
    test_or();
    test_zero();
    test_one();
    test_andOr();
    test_delayedDefinitions();
    test_expand();
    test_expand2();
    test_outputBits();
    test_gt();
    test_gt2();
    test_lt();
    test_lt2();
    test_compareEq();
    test_compareGt();
    test_compareLt();
    test_chooseFromTwoWords();
    test_enableWord();
    test_monotoneMaskToPointMask();
    test_chooseWordUnderMask();

    gds2Finish();                                                               // Execute resulting Perl code to create GDS2 files
    say("Passed ALL", testsPassed, "tests");
   }
 }
