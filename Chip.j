//------------------------------------------------------------------------------
// Design, simulate and layout a binary tree on a silicon chip.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
// pckage com.AppaApps.Silicon;
// Test all dyadic gates to see if there is any correlation between their outputs and any other pins indicating that the gate might be redundant. Use class Grouping to achieve this.

import java.io.*;
import java.util.*;
import java.util.stream.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

public class Chip                                                               // Describe a chip and emulate its operation.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final String                   name;                                          // Name of chip
  final int                clockWidth;                                          // Number of bits in system clock. Zero implies no clock.

  int                   layoutLTGates = 100;                                    // Always draw the layout if it has less than this many gates in it
  int              maxSimulationSteps = github_actions ? 1000 : 100;            // Maximum simulation steps
  int              minSimulationSteps =   0;                                     // Minimum simulation steps - we keep ggoing at least this long even if there have been no changes to allow clocked circuits to evolve.
  int          singleLevelLayoutLimit =  16;                                    // Limit on gate scaling dimensions during layout.

  final static boolean    makeSayStop = false;                                  // Turn say into stop if true which is occasionally useful for locating unlabeled say statements.
  final static int      pixelsPerCell =   4;                                    // Pixels per cell
  final static int     layersPerLevel =   4;                                    // There are 4 layers in each level: insulation, x cross bars, x-y connectors and insulation, y cross bars
  final static String          clock0 = "Clock0";                               // Negative clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.
  final static String          clock1 = "Clock1";                               // Positive clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.
  final static String      perlFolder = "perl", perlFile = "gds2.pl";           // Folder and file for Perl code to represent a layout in GDS2.
  final static Stack<String>  gdsPerl = new Stack<>();                          // Perl code to create GDS2 output files

  final Map<String, Gate>       gates = new TreeMap<>();                        // Gates by name
  final Map<String, Integer> sizeBits = new TreeMap<>();                        // Sizes of bit buses
  final Map<String, WordBus>sizeWords = new TreeMap<>();                        // Sizes of word buses
  final TreeSet<String>   outputGates = new TreeSet<>();                        // Output gates
  final TreeMap<String, TreeSet<Gate.WhichPin>>                                 // Pending gate definitions
                              pending = new TreeMap<>();
  final TreeMap<String, Gate>                                                   // Gates that are connected to an output gate
                    connectedToOutput = new TreeMap<>();
  final TreeMap<String, Pulse> pulses = new TreeMap<>();                        // Bits that are externally driven by periodic pulses of a specified duty cycle
  int                         gateSeq =   0;                                    // Gate sequence number - this allows us to display the gates in the order they were defined to simplify the understanding of drawn layouts
  int                           steps =   0;                                    // Simulation step time
  int         maximumDistanceToOutput;                                          // Maximum distance from an output
  int      countAtMostCountedDistance;                                          // The distance at which we have the most gates from an output
  int             mostCountedDistance;                                          // The number of gates at the distance from the drives that has the most gates
  final TreeMap<Integer, TreeSet<String>>                                       // Classify gates by distance to output
                      distanceToOutput = new TreeMap<>();
  final TreeMap<Integer, Stack<String>>                                         // Each column of gates ordered by Y coordinate in layout
                               columns = new TreeMap<>();
  final static TreeMap<String, Diagram>
                          diagramsDrawn = new TreeMap<>();                       // Avoid redrawing the same layout multiple times by only redrawing a new layout if it has a smaller number of levels or is closer to a square
  int                         gsx, gsy;                                         // The global scaling factors to be applied to the dimensions of the gates during layout
  int                 layoutX, layoutY;                                         // Dimensions of chip
  Stack<Connection>        connections;                                         // Pairs of gates to be connected
  Diagram                      diagram;                                         // Diagram specifying the layout of the chip

  Chip(String Name)                                                             // Create a new L<chip>.
   {this(Name, 0);                                                              // Name of chip
   }

  Chip(String Name, int ClockWidth)                                             // Create a new L<chip>.
   {name = Name;                                                                // Name of chip
    clockWidth = ClockWidth;                                                    // Clock width
    if (clockWidth > 0)                                                         // Create the system clock
     {inputBits(clock0, clockWidth);
      inputBits(clock1, clockWidth);
     }
    for (Gate g : gates.values()) g.systemGate = true;                          // Mark all gates produced so far as system gates
   }

  int          layoutLTGates(int          LayoutLTGates) {return          layoutLTGates =          LayoutLTGates;}  // Always draw the layout if it has less than this many gates in it
  int     maxSimulationSteps(int     MaxSimulationSteps) {return     maxSimulationSteps =     MaxSimulationSteps;}  // Maximum simulation steps
  int     minSimulationSteps(int     MinSimulationSteps) {return     minSimulationSteps =     MinSimulationSteps;}  // Minimum simulation steps
  int singleLevelLayoutLimit(int SingleLevelLayoutLimit) {return singleLevelLayoutLimit = SingleLevelLayoutLimit;}  // Limit on gate scaling dimensions during layout.

  enum Operator                                                                 // Gate operations
   {And, Continue, FanOut, Gt, Input, Lt, My, Nand, Ngt, Nlt, Nor, Not, Nxor,
    One, Or, Output, Xor, Zero;
   }

  boolean commutative(Operator op)                                              // Whether the pin order matters on the gate or not
   {return op != Operator.Gt  && op != Operator.Lt &&
           op != Operator.Ngt && op != Operator.Nlt;
   }

  boolean zerad(Operator op)                                                    // Whether the gate takes zero inputs
   {return op == Operator.Input || op == Operator.One || op == Operator.Zero;
   }

  boolean monad(Operator op)                                                    // Whether the gate takes a single input or not
   {return op == Operator.Continue || op == Operator.Not || op == Operator.Output;
   }

  boolean dyad(Operator op) {return !(zerad(op) || monad(op));}                 // Whether the gate takes two inputs or not

  int gates()           {return gates.size();}                                  // Number of gates in this chip
  int nextGateNumber()  {return ++gateSeq;}                                     // Numbers for gates
  String nextGateName() {return ""+nextGateNumber();}                           // Create a numeric generated gate name

  boolean definedGate(String name)                                              // Check whether a gate has been defined yet
   {final Gate g = gates.get(name);
    return g != null;
   }

  Gate getGate(String name)                                                     // Get details of named gate. Gates that have not been created yet will return null even though their details are pending.
   {if (name == null) stop("No gate name provided");
    final Gate g = gates.get(name);
    return g;
   }

  public String toString()                                                      // Convert chip to string
   {final StringBuilder b = new StringBuilder();
    b.append("Chip: "                             + name);
    b.append("  Step: "                           + (steps-1));
    b.append(" # Gates: "                         + gates.size());
    b.append("  Maximum distance: "               + maximumDistanceToOutput);
    b.append("  MostCountedDistance: "            + mostCountedDistance);
    b.append("  CountAtMostCountedDistance: "     + countAtMostCountedDistance);
    b.append("\n");
    b.append("Seq   Name____________________________ S  " +
     "Operator  #  11111111111111111111111111111111-P=#"+
                "  22222222222222222222222222222222-P=# "+
     " C Frst Last  Dist                           Nearest  Px__,Py__"+
     "  Drives these gates\n");
    for (Gate g : gates.values()) b.append(g.print());                          // Print each gate

    if (sizeBits.size() > 0)                                                    // Size of bit buses
     {b.append(""+sizeBits.size()+" Bit buses\n");
      b.append("Bits  Bus_____________________________  Value\n");
      for (String n : sizeBits.keySet())
       {final Integer v = bInt(n);
        b.append(String.format("%4d  %32s", sizeBits.get(n), n));
        if (v != null) b.append(String.format("  %d\n", v));
                       b.append(System.lineSeparator());
       }
     }

    if (sizeWords.size() > 0)                                                   // Size of word buses
     {b.append(""+sizeWords.size()+" Word buses\n");
      b.append("Words Bits  Bus_____________________________  Values\n");
      for (String n : sizeWords.keySet())
       {final WordBus w = sizeWords.get(n);
        b.append(String.format("%4d  %4d  %32s  ", w.words, w.bits, n));
        final Integer[]v = wInt(n);
        if (v != null) for(int i = 0; i < v.length; ++i) b.append(""+v[i]+", ");
        if (b.length() > 1) b.delete(b.length() - 2, b.length());
        b.append("\n");
       }
     }

    if (pending.size() > 0)                                                     // Write pending gates
     {b.append(""+pending.size()+" pending gates\n");
      b.append("Source__________________________  "+
               "Target__________________________\n");
      for   (String        n : pending.keySet())
        for (Gate.WhichPin d : pending.get(n))
          b.append(String.format("%32s  %32s  %c\n",
            n, d.drives, d.pin == null ? ' ' : d.pin ? '1' : '2'));
     }

    return b.toString();                                                        // String describing chip
   }

  class Gate                                                                    // Description of a gate
   {final int               seq = nextGateNumber();                             // Sequence number for this gate
    final String           name;                                                // Name of the gate.  This is also the name of the output of the gate. FanOut gates have a second output which is the name suffixed by the secondary marker. The secondary marker is not normally used in a gate name so a name that ends in ! is easily recognized as the second output of a fan out gate.
    final Operator           op;                                                // Operation performed by gate
    Gate                 iGate1,  iGate2;                                       // Gates driving the inputs of this gate as during simulation but not during layout
    Gate       soGate1, soGate2, tiGate1, tiGate2;                              // Pin assignments on source and target gates used during layout but not during simulation
    TreeSet<WhichPin>    drives = new TreeSet<>();                              // The names of the gates that are driven by the output of this gate with a possible pin selection attached
    boolean          systemGate = false;                                        // System gate if true
    Integer    distanceToOutput;                                                // Distance to nearest output
    Boolean               value;                                                // Current output value of this gate
    Boolean           nextValue;                                                // Next value to be assumed by the gate
    boolean             changed;                                                // Changed on current simulation step
    int        firstStepChanged;                                                // First step at which we changed
    int         lastStepChanged;                                                // Last step at which we changed
    String        nearestOutput;                                                // The name of the nearest output so we can sort each layer to position each gate vertically so that it is on approximately the same Y value as its nearest output.
    int                  px, py;                                                // Position in x and y of gate in latest layout

    private Gate(Operator Op)                                                   // System created gate of a specified type with a unique system generated name. As yet the inputs are unknown.
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
       {if (Input1 == null || Input2 == null)
          stop("Non commutative gates must have two inputs",
               Name, Op, Input1, Input2);
        impinge(Input1, true);
        impinge(Input2, false);
       }
      final TreeSet<WhichPin> d = pending.get(name);                            // Add any pending gate references to this gate definition
      if (d != null)
       {pending.remove(name);
        for (WhichPin p : d)
         {drives.add(new WhichPin(p.drives, p.pin));
         }
       }
     }

    String drives()                                                             // Convert drives to a printable string
     {final StringBuilder b = new StringBuilder();
      for (WhichPin s : drives) b.append(s + ", ");
      if (b.length() > 0) b.delete(b.length() - 2, b.length());
      return b.toString();
     }

    class WhichPin implements Comparable<WhichPin>                              // Shows that this gate drives another gate either on a specific pin or on any pin if the gate is commutative
     {final String drives;                                                      // Drives this named gate
      final Boolean   pin;                                                      // Null can drive any pin on target, true - must drive input pin 1, false - must drive input pin 2

      WhichPin(String Drives, Boolean Pin) {drives = Drives; pin = Pin;}        // Construct a pin drive specification targeting a specified input pin
      WhichPin(String Drives)              {this(Drives, null);}                // Construct a pin drive specification targeting any available input pin
      Gate gate() {return getGate(drives);}                                     // Details of gate being driven

      public int compareTo(WhichPin a) {return drives.compareTo(a.drives);}     // So we can add and remove entries from the set of driving gates

      public String toString()                                                  // Convert drive to string
       {if (pin == null) return drives;
        if (pin)         return drives+">1";
                         return drives+">2";
       }
      boolean ok1() {return pin == null ||  pin;}                               // Can drive the first pin
      boolean ok2() {return pin == null || !pin;}                               // Can drive the second pin
     }

    String print()                                                              // Print gate
     {final char v = value == null ? '.' : value ? '1' : '0';                   // Value of gate
      final char s = systemGate ? 's' : ' ';                                    // System gate

      if (op == Operator.Input)
        return String.format("%4d  %32s %c  %8s  %c"+" ".repeat(131)+"%4d,%4d  ",
          seq, name, s, op, v, px, py) + drives() + "\n";

      final Boolean pin1 = iGate1 != null ? whichPinDrivesPin1() : null;
      final Boolean pin2 = iGate2 != null ? whichPinDrivesPin2() : null;

      return   String.format("%4d  %32s %c  %8s  %c  %32s-%c=%c  %32s-%c=%c"+
                             "  %c %4d %4d  %4d  %32s  %4d,%4d  ",
        seq, name, s, op, v,
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

    public String toString()                                                    // Convert to string
     {return "" + (value == null ? 'x' : value ? '1' : '0');                    // Value of gate
     }

    void impinge(String Input)                                                  // Go to the named gate (which must therefore already exist) and show that it drives this gate on any input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new WhichPin(name));                                       // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin>  d = pending.get(Input);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input, d);}
        d.add(new WhichPin(name));
       }
     }

    void impinge(String Input, Boolean pin)                                     // Go to the named gate (which must therefore already exist) and show that it drives this gate on the specified input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new WhichPin(name, pin));                                  // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin>  d = pending.get(Input);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input, d);}
        d.add(new WhichPin(name, pin));
       }
     }

    boolean whichPinDrivesPin(String driving)                                   // True if the driving gate is using pin 1 to drive this gate, else false meaning it is using pin 2
     {final Gate d = getGate(driving);                                          // Driving gate
      if (d == null) stop("Invalid gate name:", driving);                       // No driving gate
      if (d.drives.size() == 0)
       {err("Gate name:", driving, "is referenced as driving gate:", name,
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

      if (N == 0 && !systemGate)                                                // Check for user defined gates that do not drive any other gate
       {if (op == Operator.Output) return;
        err("Gate", name, "does not drive any gate");
        return;
       }

      if (N > 2) stop("Gate", name, "drives", N,                                // At this point each gate should have no more than two inputs
        "gates, but a gate can drive no more than 2 other gates");

      for (WhichPin t : drives)                                                 // Connect targeted gates back to this gate
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

    void updateEdge()                                                           // Update a memory gate on a leading edge. The memory bit on pin 2 is loaded when the value on pin 1 goes from low to high.
     {if (op == Operator.My)
       {if (iGate1.value != null && !iGate1.value && iGate1.nextValue != null && iGate1.nextValue)
         {if (iGate2.value != null)
           {value = changed = iGate2.value != iGate2.nextValue;
            value = iGate2.nextValue;
           }
         }
       }
     }

    void updateValue()                                                          // Update the value of the gate
     {if (op != Operator.My)
       {changed = !systemGate && nextValue != value;
        value   = nextValue;
       }
     }

    void step()                                                                 // One step in the simulation
     {final Boolean g = iGate1 != null ? iGate1.value : null,
                    G = iGate2 != null ? iGate2.value : null;
      switch(op)                                                                // Gate operation
       {case And:   if (g != null && G != null)  nextValue =   g &&  G;  return;
        case Gt:    if (g != null && G != null)  nextValue =   g && !G;  return;
        case Lt:    if (g != null && G != null)  nextValue =  !g &&  G;  return;
        case Nand:  if (g != null && G != null)  nextValue = !(g &&  G); return;
        case Ngt:   if (g != null && G != null)  nextValue =  !g ||  G;  return;
        case Nlt:   if (g != null && G != null)  nextValue =   g || !G;  return;
        case Nor:   if (g != null && G != null)  nextValue = !(g ||  G); return;
        case Not:   if (g != null)               nextValue =  !g;        return;
        case Nxor:  if (g != null && G != null)  nextValue = !(g ^   G); return;
        case One:                                nextValue = true;       return;
        case Or:    if (g != null && G != null)  nextValue =   g ||  G;  return;
        case Xor:   if (g != null && G != null)  nextValue =   g ^   G;  return;
        case Zero:                               nextValue = false;      return;
        case Input:                              nextValue = value;      return;
        case Continue: case FanOut: case Output: nextValue =   g;        return;
       }
     }

    void fanOut()                                                               // Fan out when more than two gates are driven by this gate
     {final int N = drives.size();
      if (op == Operator.Output) return;                                        // Output gates do not fan out
      if (N <= 2) return;                                                       // No fan out required

      final WhichPin[]D = drives.toArray(new WhichPin[drives.size()]);

      if (N % 2 == 1)                                                           // Odd number of gates driven
       {final Gate g = new Gate(Operator.FanOut);                               // The new fan out
        for (int i = 0; i < N-1; ++i)
         {final WhichPin d = D[i];
          g.drives.add (d);                                                     // Put the target to the newly created gate
          drives.remove(d);                                                     // Remove the target from the original gate
         }
        drives.add(new WhichPin(g.name));                                       // The old gate drives the new gate
        g.fanOut();                                                             // The even gate might need further fan put
        return;
       }

      final Gate g = new Gate(Operator.FanOut), f = new Gate(Operator.FanOut);  // Even and greater than 2
      for (int i = 0; i < N/2; ++i)                                             // Lower half
       {final WhichPin d = D[i];
        g.drives.add(d);
        drives.remove(d);
       }
      drives.add(new WhichPin(g.name));                                         // The old gate drives the new gate
      g.fanOut();                                                               // The lower half gate might need further fan out

      for (int i = N/2; i < N; ++i)                                             // Upper half
       {final WhichPin d = D[i];
        f.drives.add(d);
        drives.remove(d);
       }
      drives.add(new WhichPin(f.name));                                         // The old gate drives the new gate
      f.fanOut();                                                               // The upper half gate might need further fan out
     }
   } // Gate

  Gate FanIn(Operator Op, String Name, String...Input)                          // Normal gate - not a fan out gate
   {final int L = Input.length;
    if (L == 0)                                                                 // Zerad
     {if (!zerad(Op)) stop(Op, "gate:", Name, "does not accept zero inputs");
      return new Gate(Op, Name, null,     null);
     }

    if (L == 1)                                                                 // Monad
     {if (!monad(Op)) stop(Op, "gate:", Name, "does not accept just one input");
      return new Gate(Op, Name, Input[0], null);
     }

    if (L == 2)                                                                 // Dyad
     {if (!dyad(Op)) stop(Op, "gate:", Name, "does not accept two inputs");
      return new Gate(Op, Name, Input[0], Input[1]);
     }

    final Operator Ao;                                                          // Only the last level of Not operators requires a not
    switch(Op)
     {case Nand: Ao = Operator.And; break;
      case Nor : Ao = Operator.Or; break;
      default:   Ao = Op;
     }

    if (L % 2 == 1)                                                             // Odd fan in
     {final Gate f = FanIn(Ao, nextGateName(), Arrays.copyOfRange(Input,0,L-1));// Divisible by two
      return new Gate(Op, Name, f.name, Input[L-1]);                            // Consolidation of the two gates
     }

    final Gate f = FanIn(Ao, nextGateName(), Arrays.copyOfRange(Input, 0, L/2));// Even fan out
    final Gate g = FanIn(Ao, nextGateName(), Arrays.copyOfRange(Input, L/2, L));
    return new Gate(Op, Name, f.name, g.name);                                  // Consolidation of the two gates
   }

  Gate Input    (String n)                     {return FanIn(Operator.Input,    n);}
  Gate One      (String n)                     {return FanIn(Operator.One,      n);}
  Gate Zero     (String n)                     {return FanIn(Operator.Zero,     n);}
  Gate Output   (String n, String i)           {return FanIn(Operator.Output,   n, i);}
  Gate Continue (String n, String i)           {return FanIn(Operator.Continue, n, i);}
  Gate Not      (String n, String i)           {return FanIn(Operator.Not,      n, i);}

  Gate Nxor     (String n, String i, String j) {return FanIn(Operator.Nxor, n, i, j);}
  Gate Xor      (String n, String i, String j) {return FanIn(Operator.Xor,  n, i, j);}
  Gate Gt       (String n, String i, String j) {return FanIn(Operator.Gt,   n, i, j);}
  Gate Ngt      (String n, String i, String j) {return FanIn(Operator.Ngt,  n, i, j);}
  Gate Lt       (String n, String i, String j) {return FanIn(Operator.Lt,   n, i, j);}
  Gate Nlt      (String n, String i, String j) {return FanIn(Operator.Nlt,  n, i, j);}
  Gate My       (String n, String i, String j) {return FanIn(Operator.My,   n, i, j);}

  Gate And      (String n, String...i)         {return FanIn(Operator.And,  n, i);}
  Gate Nand     (String n, String...i)         {return FanIn(Operator.Nand, n, i);}
  Gate Or       (String n, String...i)         {return FanIn(Operator.Or,   n, i);}
  Gate Nor      (String n, String...i)         {return FanIn(Operator.Nor,  n, i);}

  class WordBus                                                                 // Description of a word bus
   {final int bits;                                                             // Bits in each word of the bus
    final int words;                                                            // Words in bus
    WordBus(int Words, int Bits) {bits = Bits; words = Words;}                  // Create bus
   }

  void distanceToOutput()                                                       // Distance to the nearest output
   {outputGates.clear();
    for (Gate g : gates.values())                                               // Start at the output gates
     {g.distanceToOutput = null;                                                // Reset distance
      if (g.op == Operator.Output)                                              // Locate drives
       {outputGates.add(g.name);
        g.nearestOutput = g.name;
       }
     }

    connectedToOutput.clear();                                                  // Gates that are connected to an output gate

    TreeSet<String> check = outputGates;                                        // Search backwards from the output gates
    for (int d = 0; d < gates.size() && check.size() > 0; ++d)                  // Set distance to nearest output
     {final TreeSet<String> next = new TreeSet<>();
      maximumDistanceToOutput    = d;                                           // Maximum distance from an output

      final TreeSet<String>   to = new TreeSet<>();                             // Gates at this distance from drives
      distanceToOutput.put(d, to);

      for (String name : check)                                                 // Expand search one level
       {final Gate g = getGate(name);
        if (g.distanceToOutput == null)                                         // New gate
         {to.add(name);                                                         // Classify gate by distance from gate
          connectedToOutput.put(g.name, g);                                     // Gates that have connections to output gates
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
    mostCountedDistance        = 0;                                             // The number of gates at the distance from the drives that has the most gates
    for (Integer s : distanceToOutput.keySet())
     {final TreeSet<String> d = distanceToOutput.get(s);
      if (d.size() > countAtMostCountedDistance)
       {countAtMostCountedDistance = d.size();
        mostCountedDistance        = s;
       }
     }
   }

// Periodic Pulses                                                              // Periodic pulses that drive input buses.

  class Pulse                                                                   // A periodic pulse that drives an input bit
   {final String name;                                                          // Name of pulse - this becomes the input bit name
    final int  period;                                                          // Length of pulses in simulations steps
    final int      on;                                                          // How long the pulse is in the on state in each period in simulations steps
    final int   delay;                                                          // Offset of the on phase of the pulse in simulations steps
    final Gate   gate;                                                          // The corresponding input gate
    Pulse(String Name, int Period, int On, int Delay)                           // Pulse definition
     {name = Name; period = Period; on = On; delay = Delay;                     // Pulse details
      gate = Input(name);                                                       // Input gate for pulse
      gate.systemGate = true;                                                   // The input gate associated with this pulse. The gate will be driven by the simulator.
      if (on    > period) stop("On", on, "is greater than period", period);
      if (delay > period) stop("Delay", delay, "is greater than period", period);
      if (on + delay > period) stop("On + Delay", on, "+", delay, "is greater than period", period);
      pulses.put(name, this);                                                   // Save pulse
     }
    boolean setState()                                                          // Set gate to current state
     {final int i = steps % period;
      return gate.value = i > delay && i <= on+delay;                           // Set gate to current state
     }
    public String toString() {return ""+gate;}                                  // Pulse as state as a character
   }

  void loadPulses()                                                             // Load all the pulses for this chip
   {for (Pulse p : pulses.values()) p.setState();
   }

  Pulse pulse(String Name, int Period, int On, int Delay)                       // Create a pulse
   {return new Pulse(Name, Period, On, Delay);
   }

  Pulse pulse(String Name, int Period, int On)                                  // Create a pulse with no delay
   {return new Pulse(Name, Period, On, 0);
   }

  Pulse pulse(String Name, int Period)                                          // Create a single step pulse with no delay
   {return new Pulse(Name, Period, 1, 0);
   }

// Simulation                                                                   // Simulate the behavior of the chip

  void compileChip()                                                            // Check that an input value has been provided for every input pin on the chip.
   {final Gate[]G = gates.values().toArray(new Gate[0]);
    for (Gate g : G) g.fanOut();                                                // Fan the output of each gate if necessary which might introduce more gates
    for (Gate g : gates.values()) g.compileGate();                              // Each gate on chip
    distanceToOutput();                                                         // Output gates
   }

  void noChangeGates()                                                          // Mark each gate as unchanged
   {for (Gate g : gates.values()) g.changed = false;
   }

  boolean changes()                                                             // Check whether any changes were made
   {for (Gate g : gates.values()) if (g.changed) return true;
    return false;
   }

  void initializeGates(Inputs inputs)                                           // Initialize each gate
   {noChangeGates();

    for (Gate g : gates.values())                                               // Initialize each gate
     {g.value = null; g.lastStepChanged = 0;                                    // Make the value of the gate unknown.  An unknown value should not be used to compute a known value.
      if (g.op == Operator.Input && ! g.systemGate)                             // User input gate
       {if (inputs != null)                                                     // Initialize
         {final Boolean v = inputs.get(g.name);
          if (v != null) g.value = v;
          else stop("No input value provided for gate:", g.name);
         }
        else stop("Input gate", g.name, "has no initial value");
       }
     }
   }

  void loadClock()                                                              // Load the value of the clock into the clock input bus
   {if (clockWidth == 0) return;                                                // No clock
    final boolean[]b = bitStack(clockWidth, steps - 1);                         // Current step as bits. Start counting from zero so that we get a leading edge transition soonest.
    for (int i = 1; i <= b.length; i++)                                         // Set clock bits
     {getGate(n(i, clock0)).value =  b[i-1];
      getGate(n(i, clock1)).value = !b[i-1];
     }
   }

  class Inputs                                                                  // Set values on input gates prior to simulation
   {private final Map<String,Boolean> inputs = new TreeMap<>();

    void set(String s, boolean value)                                           // Set the value of an input
     {inputs.put(s, value);
     }

    void set(String input, int value)                                           // Set the value of an input bit bus
     {final int bits = sizeBits(input);                                         // Get the size of the input bit bus
      final boolean[]b = bitStack(bits, value);                                 // Bits to set
      for (int i = 1; i <= bits; i++) set(n(i, input), b[i-1]);                 // Load the bits into the input bit bus
     }

    void set(String input, int...values)                                        // Set the value of an input word bus
     {final WordBus wb = sizeWords(input);                                      // Get the size of the input word bus
      for   (int w = 1; w <= wb.words; w++)                                     // Each word
       {final boolean[]b = bitStack(wb.bits, values[w-1]);                      // Bits from current word
        for (int i = 1; i <= wb.bits; i++) set(nn(w, i, input), b[i-1]);        // Load the bits into the input bit bus
       }
     }

    Boolean get(String s) {return inputs.get(s);}                               // Get the value of an input
   }

  interface SimulationStep {void step();}                                       // Called each simulation step
  SimulationStep simulationStep = null;                                         // Called each simulation step

  Diagram simulate() {return simulate(null);}                                   // Simulate the operation of a chip with no input pins. If the chip has in fact got input pins an error will be reported.

  Diagram simulate(Inputs inputs)                                               // Simulate the operation of a chip
   {compileChip();                                                              // Check that the inputs to each gate are defined
    initializeGates(inputs);                                                    // Set the value of each input gate
    for (steps = 1; steps < maxSimulationSteps; ++steps)                        // Steps in time
     {loadClock();                                                              // Load the value of the clock into the clock input bus
      loadPulses();
      for (Gate g : gates.values()) g.nextValue = null;                         // Reset next values
      for (Gate g : gates.values()) g.step();                                   // Compute next value for  each gate
      for (Gate g : gates.values()) g.updateEdge();                             // Update each gate triggered by an edge transition
      for (Gate g : gates.values()) g.updateValue();                            // Update each gate
      if (simulationStep != null) simulationStep.step();                        // Call the simulation step
      if (steps > minSimulationSteps && !changes())                             // No changes occurred and we are beyond the minimum simulation time
       {return gates.size() < layoutLTGates ? drawSingleLevelLayout() : null;   // Draw the layout if it has less than the specified maximum number of gates for being drawn automatically with out a specific request.
       }
      noChangeGates();                                                          // Reset change indicators
     }

    err("Out of time after", maxSimulationSteps, "steps");                      // Not enough steps available
    return null;
   }

//D1 Circuits                                                                   // Some useful circuits

//D2 Buses                                                                      // A bus is an array of bits or an array of arrays of bits

  static String n(int i, String...C)                                            // Gate name from single index.
   {return concatenateNames(concatenateNames((Object[])C), i);
   }

  static String nn(int i, int j, String...c)                                    // Gate name from double index.
   {return concatenateNames(concatenateNames((Object[])c), i, j);
   }

  static String concatenateNames(Object...O)                                    // Concatenate names to construct a gate name
   {final StringBuilder b = new StringBuilder();
    for (Object o: O) {b.append("_"); b.append(o);}
    return O.length > 0 ? b.substring(1) : "";
   }

//D3 Bits                                                                       // An array of bits that can be manipulated via one name.

  int sizeBits(String name)                                                     // Get the size of a bits bus.
   {final Integer s = sizeBits.get(name);
    if (s == null) stop("No bit bus named", name);
    return s;
   }

  void setSizeBits(String name, int bits)                                       // Set the size of a bits bus.
   {final Integer s = sizeBits.get(name);

    if (s != null)
     {if (s != bits) stop("A bit bus with name:", name, "and width", s,
                          "has already been defined versus bits:", bits);
     }
    else sizeBits.put(name, bits);
   }

  void bit(String name, int value)                                              // Set an individual bit to true if the supplied number is non zero else false
   {if (value > 0) One(name); else Zero(name);
   }

  static boolean[]bitStack(int bits, int value)                                 // Create a stack of bits, padded with zeroes if necessary, representing an unsigned integer with the least significant bit lowest.
   {final boolean[]b = new boolean[bits];
    for (int i = 0; i < bits; ++i) b[i] = (value & (1 << i)) != 0;
    return b;
   }

  String bits(String n, int bits, int value)                                    // Create a bus set to a specified number.
   {final boolean[]b = bitStack(bits, value);                                   // Number as a stack of bits padded to specified width
    for(int i = 1; i <= bits; ++i) if (b[i-1]) One(n(i, n)); else Zero(n(i, n));// Generate constant
    setSizeBits(n, bits);                                                       // Record bus width
    return n;
   }

  String bits(int bits, int value)                                              // Create an unnamed bus set to a specified number.
   {final String n = nextGateName();                                            // Create a name for the bus
    bits(n, bits, value);                                                       // Create bus
    return n;                                                                   // Generated bus name
   }

  String inputBits(String name, int bits)                                       // Create an B<input> bus made of bits.
   {setSizeBits(name, bits);                                                    // Record bus width
    for (int b = 1; b <= bits; ++b) Input(n(b, name));                          // Bus of input gates
    return name;
   }

  String outputBits(String name, String input)                                  // Create an B<output> bus made of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    for (int i = 1; i <= bits; i++) Output(n(i, name), n(i, input));            // Bus of output gates
    setSizeBits(name, bits);                                                    // Record bus width
    return name;
   }

  String notBits(String name, String input)                                     // Create a B<not> bus made of bits.
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    for (int b = 1; b <= bits; ++b) Not(n(b, name), n(b, input));               // Bus of not gates
    return name;
   }

  Gate andBits(String name, String input)                                       // B<And> all the bits in a bus to get one bit
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = n(i, input);                       // Names of bits
    return And(name, b);                                                        // And all the bits in the bus
   }

  String andBits(String name, String input1, String input2)                     // B<And> two same sized bit buses together to make another bit bus of the same size
   {final int b1 = sizeBits(input1);                                            // Number of bits in input bus
    final int b2 = sizeBits(input2);                                            // Number of bits in input bus
    if (b1 != b2) stop("Buses have different sizes", b1, b2);
    for (int i = 1; i <= b1; ++i) And(n(i, name), n(i, input1), n(i, input2));  // And the two buses
    sizeBits.put(name, b1);
    return name;
   }

  Gate nandBits(String name, String input)                                      // B<Nand> all the bits in a bus to get one bit
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = n(i, input);                       // Names of bits
    return Nand(name, b);                                                       // Nand all the bits in the bus
   }

  Gate orBits(String name, String input)                                        // B<Or> all the bits in a bus to get one bit
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = n(i, input);                       // Names of bits
    return Or(name, b);                                                         // Or all the bits in the bus
   }

  Gate norBits(String name, String input)                                       // B<Nor> all the bits in a bus to get one bit
   {final int bits = sizeBits(input);                                           // Number of bits in input bus
    final String[]b = new String[bits];                                         // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = n(i, input);                       // Names of bits
    return Nor(name, b);                                                        // Or all the bits in the bus
   }

  Integer bInt(String output)                                                   // Convert the bits represented by an output bus to an integer
   {final int B = sizeBits(output);                                             // Number of bits in bus
    int v = 0, p = 1;
    for (int i = 1; i <= B; i++)                                                // Each bit on bus
     {final String n = n(i, output);                                            // Name of gate supporting named bit
      final Gate g = getGate(n);                                                // Gate providing bit
      if (g == null)
       {err("No such gate as:", n);
        return null;
       }
      if (g.value == null) return null;                                         // Bit state not known
      if (g.value) v += p;
      p *= 2;
     }
    return v;
   }

  Boolean getBit(String name)                                                   // Get the current value of the named gate.
   {final Gate g = getGate(name);                                               // Gate providing bit
    if (g == null) stop("No such gate named:", name);
    return g.value;                                                             // Bit state
   }

//D3 Words                                                                      // An array of arrays of bits that can be manipulated via one name.

  WordBus sizeWords(String name)                                                // Size of a words bus.
   {final WordBus s = sizeWords.get(name);
    if (s == null)
      stop( "No words width specified or defaulted for word bus:", name);
    return s;
   }

  void setSizeWords(String name, int words, int bits)                           // Set the size of a bits bus.
   {final WordBus w = sizeWords.get(name);                                      // Chip, bits bus name, words, bits per word, options
    if (w != null)
      stop("A word bus with name:", name, "has already been defined");
    sizeWords.put(name, new WordBus(words, bits));
    for (int b = 1; b <= words; ++b) setSizeBits(n(b, name), bits);             // Size of bit bus for each word in the word bus
   }

  void words(String name, int bits, int[]values)                                // Create a word bus set to specified numbers.
   {for (int w = 1; w <= values.length; ++w)                                    // Each value to put on the bus
     {final int value = values[w-1];                                            // Each value to put on the bus
      final String  s = Integer.toBinaryString(value);                          // Bit in number
      final Stack<Boolean> b = new Stack<>();
      for (int i = s.length(); i > 0; --i)                                      // Stack of bits with least significant lowest
       {b.push(s.charAt(i-1) == '1' ? true : false);
       }
      for (int i = b.size(); i <= bits; ++i) b.push(false);                     // Extend to requested bits
      for (int i = 1; i <= bits; ++i)                                           // Generate constant
       {final boolean B = b.elementAt(i-1);                                     // Bit value
        if (B) One(nn(w, i, name)); else Zero(nn(w, i, name));                  // Set bit
       }
     }
    setSizeWords(name, values.length, bits);                                    // Record bus width
   }

  String words(int bits, int[]values)                                           // Create an unnamed word bus set to specified numbers.
   {final String n = nextGateName();                                            // Create a name for the bus
    words(n, bits, values);
    return n;                                                                   // Generated bus name
   }

  String inputWords(String name, int words, int bits)                           // Create an B<input> bus made of words.
   {for   (int w = 1; w <= words; ++w)                                          // Each word on the bus
      for (int b = 1; b <= bits;  ++b) Input(nn(w, b, name));                   // Each word on the bus

    setSizeWords(name, words, bits);                                            // Record bus size
    return name;
   }

  String outputWords(String name, String input)                                 // Create an B<output> bus made of words.
   {final WordBus wb = sizeWords(input);
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {for (int b = 1; b <= wb.bits;  ++b)                                       // Each word on the bus
       {Output(nn(wb.words, wb.bits, name), nn(wb.words, wb.bits, input));      // Bus of output gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
    return name;
   }

  String notWords(String name, String input)                                    // Create a B<not> bus made of words.
   {final WordBus wb = sizeWords(input);
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {for (int b = 1; b <= wb.bits;  ++b)                                       // Each word on the bus
       {Not(nn(wb.words, wb.bits, name), nn(wb.words, wb.bits, input));         // Bus of not gates
       }
     }
    setSizeWords(name, wb.words, wb.bits);                                      // Record bus size
    return name;
   }

  String andWordsX(String name, String input)                                   // Create a bit bus of width equal to the number of words in a word bus by and-ing the bits in each word to make the bits of the resulting word.
   {final WordBus wb = sizeWords(input);
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {final Stack<String> bits = new Stack<>();
      for (int b = 1; b <= wb.bits; ++b) bits.push(nn(w, b, input));            // Bits to and
      And(n(w, name), stackToStringArray(bits));                                // And bits
     }
    setSizeBits(name, wb.words);                                                // Record bus size
    return name;
   }

  String andWords(String name, String input)                                    // Create a bit bus of the same width as each word in a word bus by and-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final WordBus wb = sizeWords(input);
    for   (int b = 1; b <= wb.bits;  ++b)                                       // Each bit in the words on the bus
     {final Stack<String> words = new Stack<>();
      for (int w = 1; w <= wb.words; ++w) words.push(nn(w, b, input));          // Each word on the bus
      And(n(b, name), words.toArray(new String[words.size()]));                 // Combine inputs using B<and> gates
     }
    setSizeBits(name, wb.bits);                                                 // Record number of bits in bit bus
    return name;
   }

  String orWordsX(String name, String input)                                    // Create a bit bus of width equal to the number of words in a word bus by or-ing the bits in each word to make the bits of the resulting word.
   {final WordBus wb = sizeWords(input);
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {final Stack<String> bits = new Stack<>();
      for (int b = 1; b <= wb.bits; ++b) bits.push(nn(w, b, input));            // Bits to or
      Or(n(w, name), stackToStringArray(bits));                                 // Or bits
     }
    setSizeBits(name, wb.words);                                                // Record number of bits in bit bus
    return name;
   }

  String orWords(String name, String input)                                     // Create a bit bus of the same width as each word in a word bus by or-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final WordBus wb = sizeWords(input);
    for   (int b = 1; b <= wb.bits;  ++b)                                       // Each bit in the words on the bus
     {final Stack<String> words = new Stack<>();
      for (int w = 1; w <= wb.words; ++w) words.push(nn(w, b, input));          // Each word on the bus
      Or(n(b, name), words.toArray(new String[words.size()]));                  // Combine inputs using B<or> gates
     }
    setSizeBits(name, wb.bits);                                                 // Record bus size
    return name;
   }

  Integer[]wInt(String name)                                                    // Convert the words on a word bus into integers
   {final WordBus W = sizeWords(name);                                          // Size of bus
    final Integer[]r = new Integer[W.words];                                    // Words on bus
    loop: for (int j = 1; j <= W.words; j++)                                    // Each word on bus
     {r[j-1] = null;                                                            // Value not known
      int v = 0, p = 1;
      for (int i = 1; i <= W.bits; i++)                                         // Each bit on bus
       {final String n = nn(j, i, name);                                        // Name of gate supporting named bit
        final Gate g = getGate(n);                                              // Gate providing bit
        if (g == null)
         {say("No such word as:", name);
          return null;
         }
        if (g.value == null) continue loop;                                     // Bit state not known
        if (g.value) v += p;
        p *= 2;
       }
      r[j-1] = v;                                                               // Save value of this word
     }
    return r;
   }

//D2 Comparisons                                                                // Compare unsigned binary integers of specified bit widths.

  void compareEq(String output, String a, String b)                             // Compare two unsigned binary integers of a specified width returning B<1> if they are equal else B<0>.  Each integer is supplied as a bus.
   {final int A = sizeBits(a);                                                  // Width of first bus
    final int B = sizeBits(b);                                                  // Width of second bus
    if (A != B)                                                                 // Check buses match in size
      stop("Input",  a, "has width", A, "but input", b, "has width", B);
    final String eq = nextGateName();                                           // Set of gates to test equality of corresponding bits
    final String[]n = new String[B];                                            // Equal bit names
    for (int i = 1; i <= B; i++) n[i-1] = n(i, eq);                             // Load array with equal bit names
    for (int i = 1; i <= B; i++) Nxor(n[i-1], n(i, a), n(i, b));                // Test each bit pair for equality
    And(output, n);                                                             // All bits must be equal
   }

  void compareGt(String output, String a, String b)                             // Compare two unsigned binary integers for greater than.
   {final int A = sizeBits(a);
    final int B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A,
                     ", but second input bus", b, "has width", B);
    for (int i = 2; i <= B; i++) Nxor(n(i, output, "e"), n(i, a), n(i, b));     // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Gt  (n(i, output, "g"), n(i, a), n(i, b));     // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
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
    if (A != B) stop("First input bus", a, "has width", A,
                     ", but second input bus", b, "has width", B);
    for (int i = 2; i <= B; i++) Nxor(n(i, output, "e"), n(i, a), n(i, b));     // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Lt  (n(i, output, "l"), n(i, a), n(i, b));     // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
     {final Stack<String> and = new Stack<>();
      and.push(n(j-1, output, "l"));
      for (int i = j; i <= B; i++) and.push(n(i, output, "e"));
      And(n(j, output, "c"), stackToStringArray(and));
     }

    final Stack<String> or = new Stack<>();
    for (int i = 2; i <= B; i++) or.push(n(i, output, "c"));                    // Equals followed by less than
                                 or.push(n(B, output, "l"));
    Or(output, stackToStringArray(or));                                         // Any set bit indicates that first is less than second
   }

  void chooseFromTwoWords(String output, String a, String b, String choose)     // Choose one of two words depending on a choice bit.  The first word is chosen if the bit is B<0> otherwise the second word is chosen.
   {final int A = sizeBits(a), B = sizeBits(b);
    if (A != B) stop("First input bus", a, "has width", A,
                     ", but second input bus", b, "has width", B);

    final String notChoose = nextGateName();                                    // Opposite of choice
    Not(notChoose, choose);                                                     // Invert choice

    for (int i = 1; i <= B; i++)                                                // Each bit
     {And(n(i, output, "a"), n(i, a), notChoose);                               // Choose first word if not choice
      And(n(i, output, "b"), n(i, b),    choose);                               // Choose second word if choice
      Or (n(i, output),      n(i, output, "a"), n(i, output, "b"));             // Or results of choice
     }
    setSizeBits(output, B);                                                     // Record bus size
   }

  void enableWord(String output, String a, String enable)                       // Output a word or zeros depending on a choice bit.  The word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
   {final int A = sizeBits(a);
    for (int i = 1; i <= A; i++) And(n(i, output), n(i, a), enable);            // Choose each bit of input word
    setSizeBits(output, A);                                                     // Record bus size
   }

//D2 Masks                                                                      // Point masks and monotone masks. A point mask has a single B<1> in a sea of B<0>s as in B<00100>.  A monotone mask has zero or more B<0>s followed by all B<1>s as in: B<00111>.

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

//D2 Registers                                                                  // Create registers

  class Register                                                                // Description of a register
   {final String output;                                                        // Name of register and corresponding output bit bus
    final String input;                                                         // Bus from which the register is loaded
    final String load;                                                          // Load bit: when this bit goes from low to high the register is loaded from the input bus.
    final int width;                                                            // Width of register
    final int loadPulseWidth;                                                   // Width of pulse needed to load the register

    Register(String Output, String Input, String Load)                          // Create register
     {final int w = sizeBits(Input);
      output = Output; input = Input; load = Load; width = w;
      loadPulseWidth = logTwo(w);
      for (int i = 1; i <= w; i++) My(n(i, Output), n(i, Input), load);            // Create the memory bits
      setSizeBits(Output, w);
     }

    public String toString()                                                    // Convert the bits on the register to a string.
     {final StringBuilder b = new StringBuilder();
      for (int i = 1; i <= width; i++)
       {final Gate g = getGate(n(i, output));
        final Boolean j = g.value;
        b.append(j == null ? 'x' : j ? '1' : '0');
       }
      return b.toString();
     }
   }

  Register register(String name, String input, String load)                     // Create a loadable register out of memory bits
   {return new Register(name, input, load);
   }

//D2 B-tree                                                                     // Circuits useful in the construction and traversal of B-trees.

  class BtreeNode                                                               // Description of a node in a binary tree
   {final String  Output;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final int         Id;                                                       // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
    final int          B;                                                       // Width of each word in the node.
    final int          K;                                                       // Number of keys == number of data words each B bits wide in the node.
    final boolean   Leaf;                                                       // Width of each word in the node.
    final String  Enable;                                                       // B bit wide bus naming the currently enabled node by its id.
    final String    Find;                                                       // B bit wide bus naming the key to be found
    final String    Keys;                                                       // Keys in this node, an array of N B bit wide words.
    final String    Data;                                                       // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
    final String    Next;                                                       // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
    final String     Top;                                                       // The top next link making N+1 next links in all.
    final String   Found;                                                       // Found the search key
    final String OutData;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final String OutNext;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final String      id;                                                       // Id for this node
    final String      df;                                                       // Data found before application of enable
    final String      en;                                                       // Whether this node is enabled for searching
    final String      f2;                                                       // Whether the key was found or not but before application of enable
    final String      me;                                                       // Point mask showing key equal to search key
    final String      mm;                                                       // Monotone mask showing keys more than the search key
    final String      mf;                                                       // The next link for the first key greater than the search key is such a key is present int the node
    final String      nf;                                                       // True if we did not find the key
    final String      n2;
    final String      n3;
    final String      n4;
    final String      nm;                                                       // No key in the node is greater than the search key
    final String      pm;                                                       // Point mask showing the first key in the node greater than the search key
    final String      pt;                                                       // A single bit that tells us whether the top link is the next link
    final String      pn;                                                       // Top is the next link, but only if the key was not found
    int level, index;                                                           // Level and position in  level for this node

    BtreeNode                                                                   // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
     (String  Output,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      int         Id,                                                           // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
      int          B,                                                           // Width of each word in the node.
      int          K,                                                           // Number of keys == number of data words each B bits wide in the node.
      boolean   Leaf,                                                           // Width of each word in the node.
      String  Enable,                                                           // B bit wide bus naming the currently enabled node by its id.
      String    Find,                                                           // B bit wide bus naming the key to be found
      String    Keys,                                                           // Keys in this node, an array of N B bit wide words.
      String    Data,                                                           // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
      String    Next,                                                           // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
      String     Top,                                                           // The top next link making N+1 next links in all.
      String   Found,                                                           // Found the search key
      String OutData,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      String OutNext)                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
     {if (K     <= 2) stop("The number of keys the node can hold must be greater than 2, not", K);
      if (K % 2 == 0) stop("The number of keys the node can hold must be odd, not even:",      K);
      this.Output = Output;
      this.Id     = Id;     this.B       = B;       this.K       = K; this.Leaf = Leaf;
      this.Enable = Enable; this.Find    = Find;    this.Keys    = Keys;
      this.Data   = Data;   this.Next    = Next;    this.Top     = Top;
      this.Found  = Found;  this.OutData = OutData; this.OutNext = OutNext;

      id = concatenateNames(Output, "id");                                      // Id for this node
      df = concatenateNames(Output, "dataFound");                               // Data found before application of enable
      en = concatenateNames(Output, "enabled");                                 // Whether this node is enabled for searching
      f2 = concatenateNames(Output, "foundBeforeEnable");                       // Whether the key was found or not but before application of enable
      me = concatenateNames(Output, "maskEqual");                               // Point mask showing key equal to search key
      mm = concatenateNames(Output, "maskMore");                                // Monotone mask showing keys more than the search key
      mf = concatenateNames(Output, "moreFound");                               // The next link for the first key greater than the search key if such a key is present int the node
      nf = concatenateNames(Output, "notFound");                                // True if we did not find the key
      n2 = concatenateNames(Output, "nextLink2");
      n3 = concatenateNames(Output, "nextLink3");
      n4 = concatenateNames(Output, "nextLink4");
      nm = concatenateNames(Output, "noMore");                                  // No key in the node is greater than the search key
      pm = concatenateNames(Output, "pointMore");                               // Point mask showing the first key in the node greater than the search key
      pt = concatenateNames(Output, "pointMoreTop");                            // A single bit that tells us whether the top link is the next link
      pn = concatenateNames(Output, "pointMoreTop_notFound");                   // Top is the next link, but only if the key was not found

      bits(id, B, Id);                                                          // Save id of node

      compareEq(en, id, Enable);                                                // Check whether this node is enabled

      for (int i = 1; i <= K; i++)
       {compareEq(n(i, me), n(i, Keys), Find);                                  // Compare equal point mask
        if (!Leaf) compareGt(n(i, mm), n(i, Keys), Find);                       // Compare more  monotone mask
       }

      setSizeBits(me, K);

      chooseWordUnderMask       (df, Data, me);                                 // Choose data under equals mask
      orBits                    (f2,       me);                                 // Show whether key was found
      enableWord           (OutData, df,   en);                                 // Enable data found
      And                    (Found, f2,   en);                                 // Enable found flag

      if (!Leaf)
       {setSizeBits             (mm, K);                                        // Interior nodes have next links
        norBits                 (nm, mm);                                       // True if the more monotone mask is all zero indicating that all of the keys in the node are less than or equal to the search key
        monotoneMaskToPointMask (pm, mm);                                       // Convert monotone more mask to point mask
        chooseWordUnderMask     (mf, Next, pm);                                 // Choose next link using point mask from the more monotone mask created
        chooseFromTwoWords      (n4, mf,   Top, nm);                            // Show whether key was found
        norBits                 (pt, pm);                                       // The top link is the next link
        Not                     (nf, Found);                                    // Not found
        enableWord              (n3, n4,   nf);                                 // Disable next link if we found the key
        And                     (pn, pt,   nf);                                 // Top is the next link, but only if the key was not found
        chooseFromTwoWords      (n2, n3,   Top, pn);                            // Either the next link or the top link
        enableWord         (OutNext, n2,   en);                                 // Next link only if this node is enabled
       }
     }

    static void Test                                                            // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
     (Chip         c,                                                           // Chip to contain node
      String  Output,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      int         Id,                                                           // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
      int          B,                                                           // Width of each word in the node.
      int          N,                                                           // Number of keys == number of data words each B bits wide in the node.
      int     enable,                                                           // B bit wide bus naming the currently enabled node by its id.
      int       find,                                                           // B bit wide bus naming the key to be found
      int[]     keys,                                                           // Keys in this node, an array of N B bit wide words.
      int[]     data,                                                           // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
      int[]     next,                                                           // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
      int        top,                                                           // The top next link making N+1 next links in all.
      String   Found,                                                           // Found the search key
      String    Data,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      String    Next                                                            // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
     )
     {if (                N != keys.length) stop("Wrong number of keys, need", N, "got", keys.length);
      if (                N != data.length) stop("Wrong number of data, need", N, "got", data.length);
      if (next != null && N != next.length) stop("Wrong number of next, need", N, "got", next.length);

      final String e = c.bits (B, enable);
      final String f = c.bits (B, find);
      final String k = c.words(B, keys);
      final String d = c.words(B, data);
      final String n = next != null ? c.words(B, next) : null;
      final String t = next != null ? c.bits (B, top)  : null;

      c.new BtreeNode(Output, Id, B, N, next == null, e, f, k, d, n, t, Found, Data, Next);
     }
   }

  class Btree                                                                   // Construct and search a Btree.
   {final int      bits;                                                        // Number of bits in a key, datum, or next link
    final int      keys;                                                        // Number of keys in a node
    final int    levels;                                                        // Number of levels in the tree
    final String output;                                                        // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
    final String   find;                                                        //i The search key bus
    final String  found;                                                        //o A bit indicating whether the key was found in the tree or not
    final String   data;                                                        //o The data corresponding to the search key if the found bit is set, else all zeros
    final String     id;                                                        // Input data
    final String     ik;                                                        // Input keys
    final String     in;                                                        // Input next links
    final String     it;                                                        // Input top link
    final String     ld;                                                        // The data out from a level
    final String     lf;                                                        // Find status for a level
    final String     ln;                                                        // Next link to search
    final String     nd;                                                        // The data out from a node
    final String     nf;                                                        // The found flag output by each node
    final String     nn;                                                        // The next link output by this node
    final TreeMap<Integer, TreeMap<Integer, BtreeNode>> tree = new TreeMap<>(); // Nodes within tree by level and position in level
    final TreeMap<Integer,                  BtreeNode> nodes = new TreeMap<>(); // Nodes within tree by id number

    Btree                                                                       // Construct a Btree.
     (String output,                                                            // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
      String   find,                                                            //i The search key bus
      String  found,                                                            //o A bit indicating whether the key was found in the tree or not
      String   data,                                                            //o The data corresponding to the search key if the found bit is set, else all zeros
      int      keys,                                                            // Number of keys in a node
      int    levels,                                                            // Number of levels in the tree
      int      bits                                                             // Number of bits in each key, data, node identifier
     )
     {this.  bits = bits;                                                       // Number of bits in a key, datum, or next link
      this.  keys = keys;                                                       // Number of keys in a node
      this.levels = levels;                                                     // Number of levels in the tree
      this.output = output;                                                     // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
      this.  find = find;                                                       //i The search key bus
      this. found = found;                                                      //o A bit indicating whether the key was found in the tree or not
      this.  data = data;                                                       //o The data corresponding to the search key if the found bit is set, else all zeros
      id = concatenateNames(output, "inputData");                               // Input data
      ik = concatenateNames(output, "inputKeys");                               // Input keys
      in = concatenateNames(output, "inputNext");                               // Input next links
      it = concatenateNames(output, "inputTop");                                // Input top link
      ld = concatenateNames(output, "levelData");                               // The data out from a level
      lf = concatenateNames(output, "levelFound");                              // Find status for a level
      ln = concatenateNames(output, "levelNext");                               // Next link to search
      nd = concatenateNames(output, "nodeData");                                // The data out from a node
      nf = concatenateNames(output, "nodeFound");                               // The found flag output by each node
      nn = concatenateNames(output, "nodeNext");                                // The next link output by this node
      int nodeId = 0;                                                           // Gives each node in the tree a different id

      if (sizeBits(find) != bits) stop("Find bus must be", bits, "wide, not", sizeBits(find));
      if (sizeBits(find) != bits) stop("Find bus must be", bits, "wide, not", sizeBits(find));

      for (int l = 1; l <= levels; l++)                                         // Each level in the bTree
       {final int         N = powerOf(keys+1, l-1);                             // Number of nodes at this level
        final boolean  root = l == 1;                                           // Root node
        final boolean  leaf = l == levels;                                      // Final level is made of leaf nodes
        final String enable = concatenateNames(output, "nextLink");             // Next link to search
        final TreeMap<Integer, BtreeNode> level = new TreeMap<>();              // Current level in tree

        tree.put(l, level);                                                     // Add the level ot the tree

        for (int n = 1; n <= N; n++)                                            // Each node at this level
         {++nodeId;                                                             // Number of this node
          final String eI = root ? bits(n(0, ln), bits, nodeId) : n(l-1, ln);   // Id of node in this level to activate. The root is always active with a hard coded value of 1 as its enabling id
          final String iK = inputWords(nn(l, n, ik), keys, bits);               // Bus of input words representing the keys in this node
          final String iD = inputWords(nn(l, n, id), keys, bits);               // Bus of input words representing the data in this node
          final String iN = leaf ? null : inputWords(nn(l, n, in), keys, bits); // Bus of input words representing the next links in this node
          final String iT = leaf ? null : inputBits (nn(l, n, it),       bits); // Bus representing the top next link
          final String oF = root ? n(l, lf) : nn(l, n, nf);                     // On the root we do not need to combine the found flags for each node - on other levels we do
          final String oD = root ? n(l, ld) : nn(l, n, nd);                     // Output data element if found
          final String oN = root ? n(l, ln) : nn(l, n, nn);                     // Next link if node is not a leaf

          final BtreeNode node = new BtreeNode(nn(l, n, output, "node"),        // Create the node
            nodeId, bits, keys, leaf, eI, find, iK, iD, iN, iT, oF, oD, oN);

          level.put(n,      node);                                              // Add the node to this level
          nodes.put(nodeId, node);                                              // Index the node
          node.level = l; node.index = n;                                       // Position of node in tree
         }

        if (!root)                                                              // Or the found flags together for this level. Not necessary on the root because there is only one node.
         {setSizeBits   (          n(l, nf), N);                                // Found bits for this level
          orBits        (n(l, lf), n(l, nf));                                   // Collect all the find output fields in this level and Or them together to see if any node found the key. At most one node will find the key if the data has been correctly structured.
         }

        if (!root)                                                              // Or the data elements together for this level. Not necessary on the root because there is only one node.
         {setSizeWords  (          n(l, nd), N, bits);                          // Data found on this level.  all the data fields will be zero unless a key matched in which case it will have the value matching the key
          orWords       (n(l, ld), n(l, nd));                                   // Collect all the data output fields from this level and Or them together as they will all be zero except for possible to see if any node found the key. At most one node will find the key if the data has been correctly structured.
         }

        if (!root && !leaf)                                                     // Next link found on this level so we can place it into the next level
         {setSizeWords(n(l,           nn), N, bits);                            // Next link found on this level.  All the next link fields will be zero except that from the enable node unless a key matched in which case it will have the value matching the key
          orWords     (n(l, ln), n(l, nn));                                     // Collect all next links nodes on this level
         }
       }

      setSizeWords    (ld, levels, bits);                                       // Collect all the data output fields from this level and Or them together as they will all be zero except for possible to see if any node found the key. At most one node will find the key if the data has been correctly structured.
      orWords         (data,      ld);                                          // Data found over all layers
      setSizeBits     (lf,    levels);                                          // Collect all the data output fields from this level and Or them together as they will all be zero except for possible to see if any node found the key. At most one node will find the key if the data has been correctly structured.
      orBits          (found,     lf);                                          // Or of found status over all layers
     }
   }

//D1 Layout                                                                     // Layout the gates and connect them with wires

  Diagram drawLayout(Diagram d)                                                 // Draw unless we have already drawn it at a lower number of levels or we are forced to draw regardless
   {final Diagram D = diagramsDrawn.get(name);                                  // Have we already drawn this diagram
    if (D == null || d.betterThan(D)) d.gds2();                                 // Never drawn or this drawing is better
    return d;
   }

  Diagram drawSingleLevelLayout(int GlobalScaleX, int GlobalScaleY)             // Try different gate scaling factors in hopes of finding a single level wiring diagram.  Returns the wiring diagram with the fewest wiring levels found.
   {Diagram D = null;
    Integer L = null;
    for (int s = 1; s < singleLevelLayoutLimit; ++s)                            // Various gate scaling factors
     {final Diagram d = layout(GlobalScaleX*s, GlobalScaleY*s);                 // Layout chip as a diagram
      final int l = d.levels();                                                 // Number of levels in diagram
      if (l == 1) return drawLayout(d);                                         // Draw the layout diagram as GDS2
      if (L == null || L > l) {L = l; D = d;}                                   // Track smallest number of wiring levels
     }
    return drawLayout(D);
   }

  Diagram drawSingleLevelLayout() {return drawSingleLevelLayout(1,1);}          // Try different gate scaling factors in hopes of finding a single level wiring diagram.  Returns the wiring diagram with the fewest wiring levels found.

  Stack<String> sortIntoOutputGateOrder(int Distance)                           // Order the gates in this column to match the order of the output gates
   {final Stack<String> r = new Stack<>();                                      // Order of gates

    if (Distance == 0)                                                          // Start at the output gates
     {for (String o : outputGates) r.push(o);                                   // Load output gates in name order
      columns.put(Distance, r);                                                 // Save the gates in this column ordered by y position
      return r;                                                                 // Return output gates
     }

    final TreeMap<String, TreeSet<String>> order = new TreeMap<>();             // Gates connected to nearest gates in previous column
    for (String G : columns.get(Distance-1))                                    // Find gates in current column that connect to gates in the previous column
     {final TreeSet<String> p = new TreeSet<>();                                // Gates nearest
      order.put(G, p);
      final Gate g = getGate(G);                                                // Gate details
      if (g.iGate1 != null) p.add(g.iGate1.name);                               // Connect the current layer to previous layer
      if (g.iGate2 != null) p.add(g.iGate2.name);
     }

    for (String o: order.keySet()) for (String g: order.get(o)) r.push(g);      // Stack the gates in this column in output gate order
    columns.put(Distance, r);                                                   // The gates in this column ordered by y position
    return r;                                                                   // The gates in this column ordered by y position
   }

  class Connection                                                              // Pairs of gates which we wish to connect
   {final Gate source, target;                                                  // Source and target gates for connection
    Connection(Gate Source, Gate Target)                                        // Pair of gates between which we wish to connect
     {source = Source;
      target = Target;
      if (source.drives.size() == 1)                                            // One drive specification on source
       {final Gate.WhichPin p = source.drives.first();
        if (p.pin != null)
         {if (p.pin) target.tiGate1 = source; else target.tiGate2 = source;     // Layout as much of the connection as we can at this point
         }
       }
      else if (source.drives.size() == 2)                                       // Two drive specifications on source
       {final Gate.WhichPin f = source.drives.first();
        final Gate.WhichPin l = source.drives. last();
        final Gate.WhichPin p = f.drives.equals(target.name) ? f : l;           // The drive between the two gates
        if (p.pin != null)                                                      // If the pin is null then either pin on the target is acceptable
         {if (p.pin) target.tiGate1 = source; else target.tiGate2 = source;     // Layout as much of the connection as we can at this point
         }
       }
     }
   }

  Diagram layout(int Gsx, int Gsy)                                              // Layout with gates scaled by in x and y.  Normally gates are 2 by 2 in size, unless globally magnified by these factors.
   {gsx = Gsx; gsy = Gsy;

    final int sx =  2 * gsx, sy = 2 * gsy;                                      // Size of gate

    layoutX = sx * (2 + maximumDistanceToOutput);                               // X dimension of chip with a bit of border
    layoutY = sy * (2 + countAtMostCountedDistance);                            // Y dimension of chip with a bit of border

    compileChip();
    for (Gate g : connectedToOutput.values())  g.px = g.distanceToOutput * sx;  // Position each gate in x as long as it is eventually connected to an output, or it is an output that has a gate

    final TreeSet<String> drawn = new TreeSet<>();                              // Gates that have already been drawn and so do not need to be redrawn

    for   (Integer D : distanceToOutput.keySet())                               // Gates at each distance from the drives
     {final TreeSet<String> d = distanceToOutput.get(D);                        // Gates in this column
      final int             N = d.size();                                       // Number of gates in this column
      final float          dy = layoutY/ (float)N - 2 * sy;                     // Extra space available to spread gates down column
      float             extra = 0;                                              // Extra space accumulated as we can only use it in increments of gsy
      int                   y = gsy;                                            // Current y position in column.

      int i = 0;
      for (String name : sortIntoOutputGateOrder(D))                            // Gates at each distance from the drives
       {final Gate g = getGate(name);
        if (drawn.contains(name)) continue;                                     // Only draw each named gate once
        drawn.add(name);                                                        // Show that we have drawn this gate already
        g.py = y; y += sy; extra += dy;
        for (; extra >= gsy; extra -= gsy) y += gsy;                            // Enough extra has accumulated to be distributed
       }
     }

    connections = new Stack<>();                                                // Connections required

    for (Gate g : gates.values())
      g.soGate1 = g.soGate2 = g.tiGate1 = g.tiGate2 = null;

    for (Gate s : connectedToOutput.values())                                   // Gates connected to outputs
      for (Gate.WhichPin p : s.drives)                                          // Gates driven by this gate
        connections.push(new Connection(s, p.gate()));                          // Connection needed

    for (Connection c : connections)                                            // Each possible connection
     {final Gate s = c.source, t = c.target;
      final boolean S1 = s.soGate1 == t || s.soGate1 == null,
                    S2 = s.soGate2 == t || s.soGate2 == null;
      final boolean T1 = t.tiGate1 == s || t.tiGate1 == null,
                    T2 = t.tiGate2 == s || t.tiGate2 == null;

      if      (t.py < s.py)                                                     // Lower
       {if      (false) {}
        else if (S1 && T2) {s.soGate1 = t; t.tiGate2 = s;}
        else if (S2 && T2) {s.soGate2 = t; t.tiGate2 = s;}
        else if (S2 && T1) {s.soGate2 = t; t.tiGate1 = s;}
        else if (S1 && T1) {s.soGate1 = t; t.tiGate1 = s;}
        else stop("Failed to connect lower");
       }
      else if (t.py > s.py)                                                     // Higher
       {if      (false) {}
        else if (S2 && T1) {s.soGate2 = t; t.tiGate1 = s;}
        else if (S2 && T2) {s.soGate2 = t; t.tiGate2 = s;}
        else if (S1 && T1) {s.soGate1 = t; t.tiGate1 = s;}
        else if (S1 && T2) {s.soGate1 = t; t.tiGate2 = s;}
        else stop("Failed to connect upper");
       }
      else                                                                      // Same
       {if      (false) {}
        else if (S1 && T1) {s.soGate1 = t; t.tiGate1 = s;}
        else if (S2 && T2) {s.soGate2 = t; t.tiGate2 = s;}
        else if (S2 && T1) {s.soGate2 = t; t.tiGate1 = s;}
        else if (S1 && T2) {s.soGate1 = t; t.tiGate2 = s;}
        else stop("Failed to connect same");
       }
     }

    diagram = new Diagram(layoutX, layoutY, gsx, gsy);                          // Layout the chip as a wiring diagram

    for (Connection c : connections)                                            // Draw connections
     {final Gate s = c.source, t = c.target;
      diagram.new Wire(s, t,
                       s.px,     s.py + (s.soGate1 == t ? 0 : gsy),
                       t.px+gsx, t.py + (t.tiGate1 == s ? 0 : gsy));
     }

    return diagram;                                                             // Resulting diagram
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

    Chip chip() {return Chip.this;}                                             // Chip for this diagram

    boolean betterThan(Diagram D)                                               // Whether this diagram is better then some other diagram in terms of having fewer levels or being more square
     {if (levels() < D.levels()) return true;                                   // Fewer levels
      final double s = min(  width,   height) / max(  width,   height);
      final double S = min(D.width, D.height) / max(D.width, D.height);
      return s > S;                                                             // Closer to square
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
        for   (int j = 0; j < height; ++j)                                      // Each y cross bar
         {s.append(String.format("%4d  ", j));
          for (int i = 0; i < width;  ++i)                                      // Each x cross bar
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
        if      (w > 1 && c.x-1 >= 0) level.ix[c.x-1][c.y] = F;                 // Create space at start of segment
        else if (h > 1 && c.y-1 >= 0) level.iy[c.x][c.y-1] = F;
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
          for (int x = 0; x < width;  ++x)
            b.append(String.format("%2d ", d[x][y]));
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
       {for (depth = 2; depth < 999999; ++depth)                                // Depth of search
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

      void path(boolean favorX)                                                 // Shortest path and returns the number of changes of direction and the path itself
       {int x = finish.x, y = finish.y;                                         // Work back from end point
        final short N = d[x][y];                                                // Length of path
        final Stack<Pixel> p = new Stack<>();                                   // Path
        p.push(finish);
        Integer s = null, S = null;                                             // Direction of last step
        int c = 0;                                                              // Number of changes
        for (int D = N-1; D >= 1; --D)                                          // Work backwards
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

        if (x < 0      || y < 0)                    stop("Start out side of diagram",  "x", x, "y", y);
        if (x >= width || y >= height)              stop("Start out side of diagram",  "x", x, "y", y, "width", width, "height", height);
        if (X < 0 || Y < 0)                         stop("Finish out side of diagram", "X", X, "Y", Y);
        if (X >= width || Y >= height)              stop("Finish out side of diagram", "X", X, "Y", Y, width,   height);
        if (x % interViaX > 0 || y % interViaY > 0) stop("Start not on a via",         "x", x, "y", y, "gsx",   gsx,   "gsy",    gsy);
        if (X % interViaX > 0 || Y % interViaY > 0) stop("Finish not on a via",        "X", X, "Y", Y, "gsx",   gsx,   "gsy",    gsy);

        for   (int i = 0; i < width;  ++i)                                      // Clear the searched space
          for (int j = 0; j < height; ++j)
            d[i][j] = 0;

        for   (int i = -crossBarOffset; i <= interViaX - crossBarOffset; ++i)   // Add metal around via
         {for (int j = -crossBarOffset; j <= interViaY - crossBarOffset; ++j)
           {setIx(x+i, y, true); setIx(X+i, Y, true);
            setIy(x, y+j, true); setIy(X, Y+j, true);
           }
         }

        found = findShortestPath();                                             // Shortest path
        for   (int i = -crossBarOffset; i <= interViaX - crossBarOffset; ++i)   // Remove metal around via
         {for (int j = -crossBarOffset; j <= interViaY - crossBarOffset; ++j)
           {setIx(x+i, y, false); setIx(X+i, Y, false);
            setIy(x, y+j, false); setIy(X, Y+j, false);
           }
         }

        if (found)                                                              // The found path will be from finish to start so we reverse it and remove the pixels used from further consideration.
         {path(false);  path(true);                                             // Find path with fewer turns by choosing to favour steps in y over x

          final Stack<Pixel> r = new Stack<>();
          Pixel p = path.pop(); r.push(p);                                      // Start point

          for (int i = 0; i < 999999 && path.size() > 0; ++i)                   // Reverse along path
           {final Pixel q = path.pop();                                         // Current pixel
            r.push(p = q);                                                      // Save pixel in path running from start to finish instead of from finish to start
           }
          path = r;
         }
       }
     }

    class Wire                                                                  // A wired connection on the diagram
     {final Gate         sourceGate;                                            // Source gate
      final Gate         targetGate;                                            // Target gate
      final Pixel             start;                                            // Start pixel
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

      Wire(Gate SourceGate, Gate TargetGate, int x, int y, int X, int Y)        // Create a wire and place it
       {sourceGate = SourceGate; targetGate = TargetGate;
        start = new Pixel(p(x), p(y)); finish = new Pixel(p(X), p(Y));
        Search S = null;
        for (Level l : levels)                                                  // Search each existing level for a placement
         {final Search s = new Search(l, start, finish);                        // Search
          if (s.found) {S = s; break;}                                          // Found a level on which we can connect this wire
         }
        if (S == null)                                                          // Create a new level on which we are bound to succeed of there is no room on any existing level
         {final Level l = new Level();
          S = new Search(l, start, finish);                                     // Search
         }

        placed = S.found;                                                       // Save details of shortest path
        path   = S.path;
        turns  = S.turns != null ? S.turns : -1;
        wires.push(this);
        level  = 1 + levels.indexOf(S.level);                                   // Levels are based from 1

        collapsePixelsIntoSegments();                                           // Place pixels into segments

        for (Segment s : segments) s.onX = s.width != null;                     // Crossbar

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

        for (Segment s : segments) s.removeFromCrossBars(S.level);              // Remove segments from crossbars
        crossBarInterConnects();                                                // Connect between cross bars
       }

      void collapsePixelsIntoSegments()                                         // Collapse pixels into segments
       {Segment s = new Segment(path.firstElement());
        segments.add(s);
        for (Pixel q : path)
         {if (s.corner != q && !s.add(q))
           {final Segment t = new Segment(s.last);
            segments.add(t);
            if (t.add(q)) s = t;
            else stop("Cannot add next pixel to new segment:", q);
           }
         }
       }

      void crossBarInterConnects()                                              // Connects between x and y cross bars when a wore changes layer within a level
       {Segment q = segments.firstElement();
        for (Segment p : segments)
         {if (q.onX != p.onX)                                                   // Changing levels with previous layer
           {final int qx = q.corner.x, qy = q.corner.y,
                      px = p.corner.x, py = p.corner.y;
            final int qX = q.X(), qY = q.Y(), pX = p.X(), pY = p.Y();
            final var s = crossBarInterConnects;
            if      (qx == px && qy == py) s.push(new Pixel(qx,   qy));
            else if (qX == pX && qy == py) s.push(new Pixel(qX-1, qy));
            else if (qX == pX && qY == pY) s.push(new Pixel(qX-1, qY-1));
            else if (qx == px && qY == pY) s.push(new Pixel(qx,   qY-1));
            else stop ("No intersection between adjacent segments");
           }
          q = p;
         }
       }
     }

    public void gds2()                                                          // Represent as Graphic Design System 2 via Perl
     {final Stack<String> p = gdsPerl;
      final Diagram bestDiagramYet = diagramsDrawn.get(Chip.this.name);         // Best levels so far
      if (bestDiagramYet == null || betterThan(bestDiagramYet))                 // Record best level so far
       {diagramsDrawn.put(Chip.this.name, this);                                // Show that we have drawn this chip at this smallest number of level
       }

      if (p.size() == 0)
       {p.push("use v5.34;");
        p.push("use Data::Table::Text qw(:all);");
        p.push("use Data::Dump qw(dump);");
        p.push("use GDS2;");
        p.push("clearFolder(\"gds/\", 999);");
       }

      p.push("if (1)");                                                         // Header for this chip
      p.push(" {my $gdsOut = \""+name+"\";");
      p.push("  my @debug; my $debug = 0;");
      p.push("  my $f = \"gds/$gdsOut.gds\";");
      p.push("  push @debug, \"Chip: $gdsOut\" if $debug;");
      p.push("  createEmptyFile($f);");
      p.push("  my $g = new GDS2(-fileName=>\">$f\");");
      p.push("  $g->printInitLib(-name=>$gdsOut);");
      p.push("  $g->printBgnstr (-name=>$gdsOut);");

      final String title = name+                                                // Title of the piece
        ", gsx="+gsx+", gsy="+gsy+", gates="+gates()+
        ", levels="+levels();
      final int tx = 8*gsx, ty = height;                                        // Title position
      p.push("  $g->printText(-layer=>102, -xy=>["+tx+", "+ty+"], "+
             " -string=>\""+title+"\");");

      for  (Gate g : connectedToOutput.values())                                // Each gate that is connected to an output
       {p.push("# Gate: "+g);
        p.push("  if (1)");
        p.push("   {my $gsx = "+gsx+";");
        p.push("    my $gsy = "+gsy+";");
        p.push("    my $x   = "+g.px +" * 4;");
        p.push("    my $y   = "+g.py +" * 4;");
        p.push("    my $X   = $x + 6 * "+gsx+";");
        p.push("    my $Y   = $y + 6 * "+gsy+";");
        p.push("    my $n   = \""+g.name+"\";");
        p.push("    my $o   = \""+g.op  +"\";");
        p.push("    push @debug, sprintf(\"Gate         %4d %4d %4d %4d  %8s  %s\", $x, $y, $X, $Y, $o, $n) if $debug;");
        p.push("    $g->printBoundary(-layer=>0,"+
               " -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);");
        p.push("    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2],"+
               " -string=>\""+g.name+" "+g.op+"\");");

        if (g.value != null &&  g.value)                                        // Show a one value
         {p.push("    $g->printBoundary(-layer=>101,"+
                 " -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);");
         }
        if (g.value != null && !g.value)                                        // Show a zero value
         {p.push("    $g->printBoundary(-layer=>100, "+
                 "-xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);");
         }
        p.push("   }");
       }

      for  (Wire    w : wires)                                                  // Each wire
       {p.push("# Wire: "+w);
        p.push("  if (1)");
        p.push("   {my $L = "       + w.level * layersPerLevel         +";");
        p.push("    my $x = "       + w.start.x                        +";");
        p.push("    my $y = "       + w.start.y                        +";");
        p.push("    my $X = "       + w.finish.x                       +";");
        p.push("    my $Y = "       + w.finish.y                       +";");
        p.push("    my $s = \""     + w.sourceGate.name              +"\";");
        p.push("    my $t = \""     + w.targetGate.name              +"\";");
        p.push("    push @debug, sprintf(\"Wire         %4d %4d %4d %4d %4d  %32s=>%s\", $x, $y, $X, $Y, $L, $s, $t) if $debug;");
        p.push("    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];");
        p.push("    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];");

        final int ll = w.level * layersPerLevel;                                // Each wire starts at a unique location and so the via can drop down from the previous layer all the way down to the gates.
        final int nx = ll + (w.segments.firstElement().onX ? 0 : 2);
        final int ny = ll + (w.segments. lastElement().onX ? 0 : 2);

        for (int i = layersPerLevel; i <= nx; i++)                              // Levels 1,2,3 have nothing in them so that we can start each new level on a multiple of 4
         {p.push("    $g->printBoundary(-layer=>"+i+", -xy=>$xy);");
         }

        for (int i = layersPerLevel; i <= ny; i++)
         {p.push("    $g->printBoundary(-layer=>"+i+", -xy=>$XY);");
         }

        for (Segment s : w.segments)                                            // Each segment of each wire
         {p.push("    if (1)");
          p.push("     {my $x = "   +s.corner.x                        +";");
          p.push("      my $y = "   +s.corner.y                        +";");
          p.push("      my $X = $x+"+(s.width  == null ? 1 : s.width)  +";");
          p.push("      my $Y = $y+"+(s.height == null ? 1 : s.height) +";");
          p.push("      push @debug, sprintf(\"Segment      %4d %4d %4d %4d %4d\", $x, $y, $X, $Y, $L) if $debug;");
          p.push("      $g->printBoundary(-layer=>$L+"+ (s.onX ? 1 : 3)+
                 ", -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);");
          p.push("     }");
         }
        for (Pixel P : w.crossBarInterConnects)                                 // Each connection between X and Y cross bars between adjacent segments
         {p.push("    if (1)");
          p.push("     {my $x = "   + P.x +";");
          p.push("      my $y = "   + P.y +";");
          p.push("      push @debug,  sprintf(\"Interconnect %4d %4d           %4d\", $x, $y, $L) if $debug;");
          p.push("      $g->printBoundary(-layer=>$L+2,"+
                 " -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);");
          p.push("     }");
         }
        p.push(" }");
       }
      p.push("  $g->printEndstr;");                                             // End of this chip
      p.push("  $g->printEndlib;");
      p.push("  owf(\"gds/$gdsOut.txt\", join \"\\n\", @debug) if $debug;");
      p.push(" }");
     }
   }

  static void gds2Finish()                                                      // Finish representation as Graphic Design System 2 via Perl. All the chips are written out into one Perl file which is executed to generate the corresponding GDS2 files.
   {final Stack<String> p = gdsPerl;
    final StringBuilder b = new StringBuilder();
    for (String s : p) b.append(s+"\n");

    new File(perlFolder).mkdirs();                                              // Create folder

    final String pf = new File(perlFolder, perlFile).toString();                // Write Perl to represent the layout in GDS2
    try (BufferedWriter w = new BufferedWriter(new FileWriter(pf)))
     {w.write(b.toString());
     }
    catch(Exception e)
     {say("Error writing to file: " + e.getMessage());
     }

    try                                                                         // Execute the file as a Perl script to create the GDS2 output - following code courtesy of Mike Limberger.
     {final var pb = new ProcessBuilder("perl", pf);
      pb.redirectErrorStream(false);                                            // STDERR will be captured and returned to the caller
      final var P = pb.start();

      final var E = P.getErrorStream();                                         // Read and print STDERR
      for (int c = E.read(); c > -1; c = E.read()) System.err.print((char)c);
      E.close();

      final int rc = P.waitFor();                                               // Wait for process to finish and close it
      if (rc != 0) say("Perl script exited with code: " + rc);
     }
    catch(Exception E)
     {say("An error occurred while executing Perl script: "+pf+
          " error: "+ E.getMessage());
      System.exit(1);
     }
   }

//D2 Groupings                                                                  // Group chips into two classes and see if any single bit predicts the classification.  Finding a bit that does predict a classification can help resolve edge cases.

  static class Grouping                                                         // Group multiple runs into two classes and then see if any bits predict the grouping. (Grouping, bit name, bit value)
   {final TreeMap<Boolean, TreeMap<String, Boolean>> grouping = new TreeMap<>();

    Grouping()                                                                  // All of Gaul is divided into two parts
     {for (int i = 0; i < 2; i++)
        grouping.put(i == 0, new TreeMap<String, Boolean>());
     }

    void put(Boolean group, Chip chip)                                          // Add the gates in a chip to the grouping
     {for (Gate g: chip.gates.values())                                         // Each gate
       {final String   name = g.name;
        final Boolean value = g.value;
        if (value != null)                                                      // Gate has a value
         {final TreeMap<String, Boolean> t = grouping.get(group);               // Part this chip belongs too.
          if (!t.containsKey(name)) t.put(name, value);                         // Initial placement
          else                                                                  // Nullify unless it matches existing key
           {final Boolean b = t.get(g.name);
            if (b != null && b != value) t.put(name, null);
           }
         }
        else  for (int i = 0; i < 2; i++) grouping.get(i == 0).put(name, null); // Nullify in all parts because the gate has no value on this chip
       }
     }

    TreeMap<String, Boolean> analyze()                                          // Locate the names of any bits that predict the grouping. The associated boolean will be true if the relationship is positive else false if negative
     {final TreeMap<String, Boolean> t = grouping.get(true);
      final TreeMap<String, Boolean> f = grouping.get(false);
      final String[]tt = t.keySet().toArray(new String[0]);
      final String[]ff = f.keySet().toArray(new String[0]);

      for (String n : tt)                                                       // Remove from true
       {final Boolean T = t.get(n), F = f.get(n);
        if (T == null || F == null || F == T) t.remove(n);
       }

      for (String n : ff)                                                       // Remove from false
       {final Boolean T = t.get(n), F = f.get(n);
        if (T == null || F == null || F == T) t.remove(n);
       }

      return t;                                                                 // Now the true set only has keys that are true for the classification while the false set contains the opposite
     }
   }

//D1 Utility routines                                                           // Utility routines

//D2 String routines                                                            // String routines

  static String[]stackToStringArray(Stack<String> s)                            // Stack of string to array of string
   {final String[]a = new String[s.size()];
    for (int i = 0; i < s.size(); i++) a[i] = s.elementAt(i);
    return a;
   }

  static String s(Stack<String> S)                                              // Stack of string to string of lines
   {final StringBuilder b = new StringBuilder();
    for (String s : S) b.append(s.replaceAll("\\s+$", "")+"\n");
    return b.toString();
   }

  static String[] s(String...S)                                                 // Convert strings to words -  sometimes helpful for specifying a list of constant gate names.
   {final Stack<String> w = new Stack<>();
    for (String s : S) for (String t : s.split("\\s+")) w.push(t);
    return stackToStringArray(w);
   }

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

//D1 Logging                                                                    // Logging and tracing

  static void say(Object...O)                                                   // Say something
   {final StringBuilder b = new StringBuilder();
    for (Object o: O) {b.append(" "); b.append(o);}
    System.err.println((O.length > 0 ? b.substring(1) : ""));
    if (makeSayStop)
     {new Exception().printStackTrace();
      System.exit(1);
     }
   }

  static void err(Object...O)                                                   // Say something and provide an error trace.
   {say(O);
    new Exception().printStackTrace();
   }

  static void stop(Object...O)                                                  // Say something. provide an error trace and stop,
   {say(O);
    new Exception().printStackTrace();
    System.exit(1);
   }

//D0

  static void test_max_min()
   {ok(min(3, 2, 1), 1d);
    ok(max(1, 2, 3), 3d);
   }

  static void test_and()
   {final Chip   c = new Chip("And");
    final Gate  i1 = c.Input ("i1");
    final Gate  i2 = c.Input ("i2");
    final Gate and = c.And   ("and", "i1", "i2");
    final Gate   o = c.Output("o", "and");

    final Inputs inputs = c.new Inputs();
    inputs.set("i1", true);
    inputs.set("i2", false);
    c.simulate(inputs);

    c.drawSingleLevelLayout();

    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, false);
    ok(  o.value, false);
    ok(  c.steps ,   3);
   }

  static Chip test_and_grouping(Boolean i1, Boolean i2)
   {final Chip c = new Chip("And Grouping");
    c.Input ("i1");
    c.Input ("i2");
    c.And   ("and", "i1", "i2");
    c.Output("o", "and");
    final Inputs inputs = c.new Inputs();
    inputs.set("i1", i1);
    inputs.set("i2", i2);
    c.simulate(inputs);
    ok(c.getBit("o"), i1 && i2);
    return c;
   }

  static void test_and_grouping()
   {Grouping g = new Grouping();
    g.put(false, test_and_grouping(true, false));
    g.put( true, test_and_grouping(true, true));
    final var a = g.analyze();
    ok(a.size(), 3);
    ok(a.get("and"), true);
    ok(a.get( "i2"), true);
    ok(a.get(  "o"), true);
   }

  static Chip test_or_grouping(Boolean i1, Boolean i2)
   {final Chip c = new Chip("Or Grouping");
    c.Input ("i1");
    c.Input ("i2");
    c.Or    ("or", "i1", "i2");
    c.Output("o", "or");
    final Inputs inputs = c.new Inputs();
    inputs.set("i1", i1);
    inputs.set("i2", i2);
    c.simulate(inputs);
    ok(c.getBit("o"), i1 || i2);
    return c;
   }

  static void test_or_grouping()
   {Grouping g = new Grouping();
    g.put(false, test_or_grouping(true, false));
    g.put( true, test_or_grouping(true, true));
    final var a = g.analyze();
    ok(a.size(), 1);
    ok(a.get( "i2"), true);
   }

  static void test_delayedDefinitions()
   {final Chip   c = new Chip("And");
    final Gate   o = c.Output("o", "and");
    final Gate and = c.And   ("and", "i1", "i2");
    final Gate  i1 = c.Input ("i1");
    final Gate  i2 = c.Input ("i2");
    final Inputs inputs = c.new Inputs();
    inputs.set("i1", true);
    inputs.set("i2", false);
    c.simulate(inputs);
    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, false);
    ok(  o.value, false);
    ok(  c.steps ,     3);
   }

  static void test_or()
   {final Chip c = new Chip("Or");
    final Gate  i1 = c.Input ("i1");
    final Gate  i2 = c.Input ("i2");
    final Gate and = c.Or    ("or", "i1", "i2");
    final Gate   o = c.Output("o", "or");
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

  static void test_notGates()
   {final Chip c = new Chip("NotGates");
    c.bits("b", 5, 21);
    final Gate   a = c. andBits(  "a",  "b");
    final Gate   o = c.  orBits(  "o",  "b");
    final Gate  na = c.nandBits( "na",  "b");
    final Gate  no = c.norBits ( "no",  "b");
    final Gate  oa = c.Output  ( "oa",  "a");
    final Gate  oo = c.Output  ( "oo",  "o");
    final Gate ona = c.Output  ("ona", "na");
    final Gate ono = c.Output  ("ono", "no");
    c.simulate();
    ok( a.value, false);
    ok(na.value, true);
    ok( o.value, true);
    ok(no.value, false);
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
    ok(c.steps  , 3);
    ok(o.value , true);
   }

  static Chip test_and3(boolean A, boolean B, boolean C, boolean D)
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

    ok(c.steps,  4);
    ok(o.value, (A && B) || (C && D));
    return c;
   }

  static void test_andOr()
   {final boolean t = true, f = false;
    Grouping g = new Grouping();
    g.put(false,  test_and3(t, t, t, t));
    g.put(false,  test_and3(t, t, t, f));
    g.put(false,  test_and3(t, t, f, t));
    g.put(false,  test_and3(t, t, f, f));
    g.put(false,  test_and3(t, f, t, t));
    g.put(false,  test_and3(t, f, t, f));
    g.put(false,  test_and3(t, f, f, t));
    g.put(false,  test_and3(t, f, f, f));

    g.put( true,  test_and3(f, t, t, t));
    g.put( true,  test_and3(f, t, t, f));
    g.put( true,  test_and3(f, t, f, t));
    g.put( true,  test_and3(f, t, f, f));
    g.put( true,  test_and3(f, f, t, t));
    g.put( true,  test_and3(f, f, t, f));
    g.put( true,  test_and3(f, f, f, t));
    g.put( true,  test_and3(f, f, f, f));

    final var a = g.analyze();
    ok(a.size(), 1);
    ok(a.containsKey("i11"), true);
    ok(a.get("i11"),         false);
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
    ok(c.steps,  5);
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
      ok(c.steps,     3);
      ok(c.bInt("o"), i);
     }
   }

  static void test_and2Bits()
   {for (int N = 3; N <= 4; ++N)
     {final int N2 = powerTwo(N);
      for  (int i = 0; i < N2; i++)
       {final Chip c = new Chip("OutputBits");
        c.bits("i1", N, 5);
        c.bits("i2", N, i);
        c.andBits("o", "i1", "i2");
        c.outputBits("out", "o");
        c.simulate();
        ok(c.steps,     4);
        ok(c.bInt("o"), 5 & i);
       }
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

/*
  101   0 AndX horizontally  1 OrX horizontally
  100   0                    1
  111   1                    1
  110   0                    1
  111   1                    1

  100 And words vertically
  111  Or words vertically
 */

  static void test_aix()
   {final int B = 3;
    final int[]bits =  {5, 4, 7, 6, 7};
    final var c = new Chip("Aix "+B);
    c.     words("i", B, bits);
    c. andWords ("a", "i"); c.outputBits("oa", "a");
    c.  orWords ("o", "i"); c.outputBits("oo", "o");
    c. andWordsX("A", "i"); c.outputBits("oA", "A");
    c.  orWordsX("O", "i"); c.outputBits("oO", "O");
    c.simulate();
    ok(c.bInt("oa"),  4);
    ok(c.bInt("oo"),  7);
    ok(c.bInt("oA"), 20);
    ok(c.bInt("oO"), 31);
   }

  static void test_compareEq()
   {for (int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareEq "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareEq("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 ? 5 : 6);
          ok(o.value, i == j);
         }
       }
     }
   }

  static void test_compareGt()
   {for     (int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareGt "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareGt("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 ? 6 : B == 3 ? 8 : 9);
          ok(o.value, i > j);
          if (B == 4 && i == 1 && j == 2) c.drawSingleLevelLayout(3, 2);
         }
       }
     }
   }

  static void test_compareLt()
   {for     (int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var c = new Chip("CompareLt "+B);
          c.bits("i", B, i);
          c.bits("j", B, j);
          c.compareLt("o", "i", "j");
          final Gate o = c.Output("O", "o");
          c.simulate();
          ok(c.steps, B == 2 ? 6 : B == 3 ? 8 : 9);
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
      ok(c.steps, 9);
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
      ok(c.steps, 5);
      ok(c.bInt("out"), i == 0 ? 0 : 3);
     }
   }

  static void test_monotoneMaskToPointMask()
   {for    (int B = 2; B <= 4; ++B)
     {final int N = powerTwo(B);
      for  (int i = 1; i <= N; i++)
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

  static void test_chooseWordUnderMask(int B, int i)
   {final int[]numbers =  {2, 3, 2, 1,  0, 1, 2, 3,  2, 3, 2, 1,  0, 1, 2, 3};
    final int B2 = powerTwo(B);
    final var c = new Chip("chooseWordUnderMask "+B);
    c.words("i", B, Arrays.copyOfRange(numbers, 0, B2));
    c.bits ("m", B2, powerTwo(i));
    c.chooseWordUnderMask("o", "i", "m");
    c.outputBits("out", "o");
    c.simulate();
    ok(c.bInt("out"), numbers[i]);
    c.drawSingleLevelLayout(3, 1);
   }

  static void test_chooseWordUnderMask()
   {final int N = github_actions ? 4 : 3;
    for   (int B = 2; B <= N;          B++)
      for (int i = 1; i < powerTwo(B); i++)
        test_chooseWordUnderMask(B, i);
   }

  static Chip test_BtreeNode(int find, int enable, boolean Found, int Data, int Next)
   {final int[]keys = {2, 4, 6};
    final int[]data = {1, 3, 5};
    final int[]next = {1, 3, 5};
    final int   top = 7;
    final int     B = 3;
    final int     N = 3;
    final int    id = 7;
    final var     c = new Chip("BtreeNode "+B);

    BtreeNode.Test(c, "node", id, B, N, enable, find, keys, data, next, top, "found", "data", "next");                                                                  // Create a Btree node"out_found" , "out_dataFound", "out_nextLink"),
    c.outputBits("d", "data"); // Anneal the node
    c.outputBits("n", "next");
    c.Output    ("f", "found");
    c.simulate();
    ok(c.steps,              23);
    ok(c.getBit("found"), Found);
    ok(c.bInt  ("data"),   Data);
    ok(c.bInt  ("next"),   Next);
    return c;
   }

  static void test_BtreeNode()
   {test_BtreeNode(1, 7, false, 0, 1);
    test_BtreeNode(2, 7,  true, 1, 0);
    test_BtreeNode(3, 7, false, 0, 3);
    test_BtreeNode(4, 7,  true, 3, 0);
    test_BtreeNode(5, 7, false, 0, 5);
    test_BtreeNode(6, 7,  true, 5, 0);
    test_BtreeNode(7, 7, false, 0, 7);

    test_BtreeNode(1, 1, false, 0, 0);
    test_BtreeNode(2, 1, false, 0, 0);
    test_BtreeNode(3, 1, false, 0, 0);
    test_BtreeNode(4, 1, false, 0, 0);
    test_BtreeNode(5, 1, false, 0, 0);
    test_BtreeNode(6, 1, false, 0, 0);
    test_BtreeNode(7, 1, false, 0, 0);
   }

  static Chip test_BtreeLeafCompare(int find, int enable, boolean Found, int Data, int Next)
   {final int[]keys = {2, 4, 6};
    final int[]data = {1, 3, 5};
    final int[]next = null;
    final int   top = 7;
    final int     B = 3;
    final int     N = 3;
    final int    id = 7;
    final var     c = new Chip("BtreeLeafCompare "+B);

    BtreeNode.Test(c, "node", id, B, N, enable, find, keys, data, next, top, "found", "data", "next");                                                                  // Create a Btree node"out_found" , "out_dataFound", "out_nextLink"),
    c.outputBits("d", "data"); // Anneal the node
    c.Output    ("f", "found");
    c.simulate();
    ok(c.steps,              12);
    ok(c.getBit("found"), Found);
    ok(c.bInt  ("data"),   Data);
    return c;
   }

  static void test_BtreeLeafCompare()
   {test_BtreeLeafCompare(1, 7, false, 0, 1);
    test_BtreeLeafCompare(2, 7,  true, 1, 0);
    test_BtreeLeafCompare(3, 7, false, 0, 3);
    test_BtreeLeafCompare(4, 7,  true, 3, 0);
    test_BtreeLeafCompare(5, 7, false, 0, 5);
    test_BtreeLeafCompare(6, 7,  true, 5, 0);
    test_BtreeLeafCompare(7, 7, false, 0, 7);

    test_BtreeLeafCompare(1, 1, false, 0, 0);
    test_BtreeLeafCompare(2, 1, false, 0, 0);
    test_BtreeLeafCompare(3, 1, false, 0, 0);
    test_BtreeLeafCompare(4, 1, false, 0, 0);
    test_BtreeLeafCompare(5, 1, false, 0, 0);
    test_BtreeLeafCompare(6, 1, false, 0, 0);
    test_BtreeLeafCompare(7, 1, false, 0, 0);
   }

  static void test_Btree(Chip c, Inputs i, int find)
   {i.set("find", find);
    c.simulate(i);
   }

  static void test_Btree(Chip c, Inputs i, int find, int found)
   {test_Btree(c, i, find);
    ok(c.steps,              46);
    ok(c.getBit("found"),   true);
    ok(c.bInt  ("data"),   found);
   }

  static void test_Btree(Chip c, Inputs i, int find, int found, boolean layout)
   {test_Btree(c, i, find, found);

    if (layout) c.drawSingleLevelLayout(6, 1);
   }

  static void test_Btree()
   {final int B = 8, K = 3, L = 2;

    final var c = new Chip("Btree");
    c.inputBits("find", B);
    final Btree  b = c.new Btree("tree", "find", "found", "data", K, L, B);
    c.outputBits("d", "data"); // Anneal the tree
    c.Output    ("f", "found");

    final Inputs i = c.new Inputs();
    final BtreeNode r = b.tree.get(1).get(1);
    i.set(r.Keys,   10,  20, 30);
    i.set(r.Data,   11,  22, 33);
    i.set(r.Next,  2,  3,  4);
    i.set(r.Top,               5);
    final BtreeNode l1 = b.tree.get(2).get(1);
    i.set(l1.Keys,   2,  4,  6);
    i.set(l1.Data,  22, 44, 66);
    final BtreeNode l2 = b.tree.get(2).get(2);
    i.set(l2.Keys,  13, 15, 17);
    i.set(l2.Data,  31, 51, 71);
    final BtreeNode l3 = b.tree.get(2).get(3);
    i.set(l3.Keys,  22, 24, 26);
    i.set(l3.Data,  22, 42, 62);
    final BtreeNode l4 = b.tree.get(2).get(4);
    i.set(l4.Keys,  33, 35, 37);
    i.set(l4.Data,  33, 53, 73);

    test_Btree(c, i, 10, 11);
    test_Btree(c, i, 20, 22);
    test_Btree(c, i, 30, 33);

    test_Btree(c, i,  2, 22, true);
    test_Btree(c, i,  4, 44);
    test_Btree(c, i,  6, 66);

    test_Btree(c, i, 13, 31);
    test_Btree(c, i, 15, 51);
    test_Btree(c, i, 17, 71);

    test_Btree(c, i, 22, 22);
    test_Btree(c, i, 24, 42);
    test_Btree(c, i, 26, 62);

    test_Btree(c, i, 33, 33);
    test_Btree(c, i, 35, 53);
    test_Btree(c, i, 37, 73);

    final int[]skip = {10, 20, 30,  2,4,6,  13,15,17, 22,24,26, 33,35,37};
    for (final int f : IntStream.rangeClosed(0, 100).toArray())
     {if (Arrays.stream(skip).noneMatch(x -> x == f)) test_Btree(c, i, f);
     }
   }

  static void test_simulationStep()
   {final var c = new Chip("Simulation Step");
    final Stack<Integer> s = new Stack<>();
    c.simulationStep = ()->{s.push(c.steps);};
    c.One("i");
    c.Not("n9", "i");
    c.Not("n8", "n9");
    c.Not("n7", "n8");
    c.Not("n6", "n7");
    c.Not("n5", "n6");
    c.Not("n4", "n5");
    c.Not("n3", "n4");
    c.Not("n2", "n3");
    c.Not("n1", "n2");
    c.Output("o", "n1");

    final Inputs i = c.new Inputs();
    i.set("i", true);
    c.simulate();

    ok(c.steps,  12);
    ok(s.size(), 12);
   }

  static void test_clock()
   {final var c = new Chip("Clock", 8);
    c.minSimulationSteps(16);
    final Stack<Boolean> s = new Stack<>();
    c.simulationStep = ()->{s.push(c.getBit("a"));};
    c.And ("a", n(1, clock0), n(2, clock0));
    c.Output("o", "a");
    c.simulate();
    ok(c.steps,  19);
    for (int i = 0; i < s.size(); i++) ok(s.elementAt(i), (i+1) % 4 == 0);
   }

  static void test_pulse()
   {final Stack<String> s = new Stack<>();
    final var c = new Chip("Pulse");
    c.minSimulationSteps(32);

    Pulse p1 = c.pulse("p1", 16);
    Pulse p2 = c.pulse("p2", 16, 1, 3);
    Pulse p3 = c.pulse("p3", 16, 2, 5);
    Pulse p4 = c.pulse("p4",  4);
    Gate  a  = c.   Or("a", s("p1 p2 p3"));
    Gate  m  = c.   My("m", "a", "p4");
    c.Output("o", "m");

    c.simulationStep = ()->{s.push(String.format("%4d  %s %s %s %s   %s %s", c.steps-1, p1, p2, p3, p4, a, m));};
    c.simulate();
    //stop(s(s));
    ok(STR."""
   0  1 0 0 1   x x
   1  0 0 0 0   1 x
   2  0 0 0 0   0 x
   3  0 1 0 0   0 x
   4  0 0 0 1   1 1
   5  0 0 1 0   1 1
   6  0 0 1 0   1 1
   7  0 0 0 0   0 1
   8  0 0 0 1   0 1
   9  0 0 0 0   0 1
  10  0 0 0 0   0 1
  11  0 0 0 0   0 1
  12  0 0 0 1   0 1
  13  0 0 0 0   0 1
  14  0 0 0 0   0 1
  15  0 0 0 0   0 1
  16  1 0 0 1   0 1
  17  0 0 0 0   1 0
  18  0 0 0 0   0 0
  19  0 1 0 0   0 0
  20  0 0 0 1   1 1
  21  0 0 1 0   1 1
  22  0 0 1 0   1 1
  23  0 0 0 0   0 1
  24  0 0 0 1   0 1
  25  0 0 0 0   0 1
  26  0 0 0 0   0 1
  27  0 0 0 0   0 1
  28  0 0 0 1   0 1
  29  0 0 0 0   0 1
  30  0 0 0 0   0 1
  31  0 0 0 0   0 1
  32  1 0 0 1   0 1
  33  0 0 0 0   1 0
  34  0 0 0 0   0 0
  35  0 1 0 0   0 0
  36  0 0 0 1   1 1
  37  0 0 1 0   1 1
  38  0 0 1 0   1 1
""", s(s));
    ok(c.steps,  39);
   }

  static void test_register()
   {final Stack<String> s = new Stack<>();
    final var c = new Chip("Pulse");
    c.bits("i1", 8, 9);
    c.bits("i2", 8, 6);
    Pulse   pc = c.pulse("choose", 32, 16, 16);
    Pulse   pl = c.pulse("load",    8,  1,  1);
    c.chooseFromTwoWords("I", "i1", "i2", "choose");
    Register r = c.register("reg",  "I", "load");
    c.outputBits("out", "reg");

    c.simulationStep = ()->{s.push(String.format("%4d  %s %s %8s", c.steps-1, pc, pl, r));};
    c.minSimulationSteps(32);
    c.simulate();
    //stop(s(s));
    ok(STR."""
   0  0 0 xxxxxxxx
   1  0 1 xxxxxxxx
   2  0 0 xxxxxxxx
   3  0 0 xxxxxxxx
   4  0 0 xxxxxxxx
   5  0 0 xxxxxxxx
   6  0 0 xxxxxxxx
   7  0 0 xxxxxxxx
   8  0 0 xxxxxxxx
   9  0 1 xxxxxxxx
  10  0 0 10010000
  11  0 0 10010000
  12  0 0 10010000
  13  0 0 10010000
  14  0 0 10010000
  15  0 0 10010000
  16  1 0 10010000
  17  1 1 10010000
  18  1 0 10010000
  19  1 0 10010000
  20  1 0 10010000
  21  1 0 10010000
  22  1 0 10010000
  23  1 0 10010000
  24  1 0 10010000
  25  1 1 10010000
  26  1 0 01100000
  27  1 0 01100000
  28  1 0 01100000
  29  1 0 01100000
  30  1 0 01100000
  31  0 0 01100000
  32  0 0 01100000
  33  0 1 01100000
  34  0 0 01100000
  35  0 0 01100000
  36  0 0 01100000
  37  0 0 01100000
  38  0 0 01100000
  39  0 0 01100000
""", s(s));
    ok(c.steps,  40);
   }

  static int testsPassed = 0, testsFailed = 0;                                  // Number of tests passed and failed

  static void ok(Object a, Object b)                                            // Check test results match expected results.
   {if (a.equals(b)) ++testsPassed;
    else {testsFailed++; err(a, "does not equal", b);}
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_max_min();
    test_and2Bits();
    test_and();
    test_and_grouping();
    test_or_grouping();
    test_or();
    test_notGates();
    test_zero();
    test_one();
    test_andOr();
    test_clock();
    test_delayedDefinitions();
    test_simulationStep();
    test_expand();
    test_expand2();
    test_outputBits();
    test_gt();
    test_gt2();
    test_lt();
    test_lt2();
    test_aix();
    test_pulse();
    test_compareEq();
    test_compareGt();
    test_compareLt();
    test_chooseFromTwoWords();
    test_enableWord();
    test_monotoneMaskToPointMask();
    test_chooseWordUnderMask();
    test_BtreeNode();
    test_BtreeLeafCompare();
    test_register();
    if (github_actions) test_Btree();
   }

  static void newTests()                                                        // Tests being worked on
   {if (github_actions) return;
    test_register();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {oldTests();
    newTests();
    gds2Finish();                                                               // Execute resulting Perl code to create GDS2 files
    if (testsFailed == 0) say("PASSed ALL", testsPassed, "tests");
    else say("Passed "+testsPassed+",    FAILed:", testsFailed, "tests.");
   }
 }
