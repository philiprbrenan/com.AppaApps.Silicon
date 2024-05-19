//------------------------------------------------------------------------------
// Design, simulate and layout a binary tree on a silicon chip.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
// pckage com.AppaApps.Silicon;
// Test all dyadic gates to see if there is any correlation between their outputs and any other pins indicating that the gate might be redundant. Use class Grouping to achieve this.
// Gate set method
// concatenateNames() ->n()

import java.io.*;
import java.util.*;
import java.util.stream.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

public class Chip                                                               // Describe a chip and emulate its operation.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));

  final String                   name;                                          // Name of chip
  final int                clockWidth;                                          // Number of bits in system clock. Zero implies no clock.

  int                   layoutLTGates =  100;                                   // Always draw the layout if it has less than this many gates in it
  final int defaultMaxSimulationSteps = github_actions ? 1000 : 100;            // Default maximum simulation steps
  final int defaultMinSimulationSteps =    0;                                   // Default minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.
  Integer          maxSimulationSteps = null;                                   // Maximum simulation steps
  Integer          minSimulationSteps = null;                                   // Minimum simulation steps - we keep going at least this long even if there have been no changes to allow clocked circuits to evolve.
  int          singleLevelLayoutLimit =   16;                                   // Limit on gate scaling dimensions during layout.

  final static boolean    makeSayStop = false;                                  // Turn say into stop if true which is occasionally useful for locating unlabeled say statements.
  final static int      pixelsPerCell =    4;                                   // Pixels per cell
  final static int     layersPerLevel =    4;                                   // There are 4 layers in each level: insulation, x cross bars, x-y connectors and insulation, y cross bars
  final static String      perlFolder = "perl", perlFile = "gds2.pl";           // Folder and file for Perl code to represent a layout in GDS2.
  final static Stack<String>  gdsPerl = new Stack<>();                          // Perl code to create GDS2 output files
  final static TreeSet<String> errors = new TreeSet<>();                        // Unique error messages during the compilation of a chip

  final Map<String, Bit>         bits = new TreeMap<>();                        // Bits by name
  final Map<String, Gate>       gates = new TreeMap<>();                        // Gates by name
  final Map<String, BitBus>  bitBuses = new TreeMap<>();                        // Bit buses
  final Map<String, WordBus>wordBuses = new TreeMap<>();                        // Sizes of word buses
  final TreeSet<String>   outputGates = new TreeSet<>();                        // Output gates
  final TreeMap<String, TreeSet<Gate.WhichPin>>                                 // Pending gate definitions
                              pending = new TreeMap<>();
  final TreeMap<String, Gate>                                                   // Gates that are connected to an output gate
                    connectedToOutput = new TreeMap<>();
  final TreeMap<String, Pulse> pulses = new TreeMap<>();                        // Bits that are externally driven by periodic pulses of a specified duty cycle
  final BitBus                 clock0;                                          // Negative clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.
  final BitBus                 clock1;                                          // Positive clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.

  ExecutionTrace       executionTrace = null;                                   // Execution trace goes here

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
                          diagramsDrawn = new TreeMap<>();                      // Avoid redrawing the same layout multiple times by only redrawing a new layout if it has a smaller number of levels or is closer to a square
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
     {clock0 = inputBits("Clock0", clockWidth);
      clock1 = inputBits("Clock1", clockWidth);
     }
    else
     {clock0 = null;
      clock1 = null;
     }
    for (Gate g : gates.values()) g.systemGate = true;                          // Mark all gates produced so far as system gates
   }

  int          layoutLTGates(int          LayoutLTGates) {return          layoutLTGates =          LayoutLTGates;}  // Always draw the layout if it has less than this many gates in it
  int     maxSimulationSteps(int     MaxSimulationSteps) {return     maxSimulationSteps =     MaxSimulationSteps;}  // Maximum simulation steps
  int     minSimulationSteps(int     MinSimulationSteps) {return     minSimulationSteps =     MinSimulationSteps;}  // Minimum simulation steps
  int singleLevelLayoutLimit(int SingleLevelLayoutLimit) {return singleLevelLayoutLimit = SingleLevelLayoutLimit;}  // Limit on gate scaling dimensions during layout.

  void simulationSteps(int min, int max) {minSimulationSteps(min);   maxSimulationSteps(max);}                      // Stop cleanly between the specified minimum and maximum number of steps
  void simulationSteps(int steps)        {minSimulationSteps(steps); maxSimulationSteps(steps);}                    // Stop cleanly at this number of steps

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

  int gates              () {return gates.size();}                              // Number of gates in this chip
  int nextGateNumber     () {return ++gateSeq;}                                 // Numbers for gates
  String nextGateName    () {return ""+nextGateNumber();}                       // Create a numeric generated gate name

  String nextGateName(String...name)                                            // Create a named, numeric generated gate name
   {return concatenateNames(concatenateNames((Object[])name), nextGateNumber());
   }

  boolean definedBit(String name)                                               // Check whether a bit has been defined yet
   {final Gate g = gates.get(name);
    return g != null;
   }

  boolean definedGate(String name)                                              // Check whether a gate has been defined yet
   {final Gate g = gates.get(name);
    return g != null;
   }

  boolean definedGate(Bit bit) {return definedGate(bit.name);}                  // Check whether a gate has been defined yet

  Gate getGate(String name)                                                     // Get details of named gate. Gates that have not been created yet will return null even though their details are pending.
   {if (name == null) stop("No gate name provided");
    final Gate g = gates.get(name);
    return g;
   }
  Gate getGate(Bit bit) {return getGate(bit.name);}                             // Get details of named gate. Gates that have not been created yet will return null even though their details are pending.

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

    if (bitBuses.size() > 0)                                                    // Size of bit buses
     {b.append(""+bitBuses.size()+" Bit buses\n");
      b.append("Bits  Bus_____________________________  Value\n");
      for (String n : bitBuses.keySet())
       {final BitBus  B = bitBuses.get(n);
        final Integer v = B.Int();
        b.append(String.format("%4d  %32s", bitBuses.get(n).bits, n));
        if (v != null) b.append(String.format("  %d\n", v));
                       b.append(System.lineSeparator());
       }
     }

    if (wordBuses.size() > 0)                                                   // Size of word buses
     {b.append(""+wordBuses.size()+" Word buses\n");
      b.append("Words Bits  Bus_____________________________  Values\n");
      for (String n : wordBuses.keySet())
       {final WordBus w = wordBuses.get(n);
        b.append(String.format("%4d  %4d  %32s  ", w.words, w.bits, n));
        final Integer[]v = w.Int();
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

  class Bit                                                                     // Description of a bit that will eventually be generated by a gate
   {final int seq;                                                              // Sequence number for this bit
    final String name;                                                          // Name of the bit.  This is also the name of the gate and the output of the gate. FanOut gates have a second output which is the name suffixed by the secondary marker. The secondary marker is not normally used in a gate name so a name that ends in ! is easily recognized as the second output of a fan out gate.
    Bit()                                                                       // Unnamed bit
     {seq  = nextGateNumber();
      name = ""+seq;
      putBit();
     }
    Bit(String Name)                                                            // Named bit
     {name = validateName(Name);                                                // Validate name
      final Bit b = getBit();                                                   // Get details of bit
      seq = b == null ? nextGateNumber() : b.seq;                               // Sequence number for gate
      if (b == null) putBit();                                                  // Tree of gates
     }

    String validateName(String name)                                            // Confirm that a component name looks like a variable name and has not already been used
     {final String[]words = name.split("_");
      for (int i = 0; i < words.length; i++)
       {final String w = words[i];
        if (!w.matches("\\A([a-zA-Z][a-zA-Z0-9_.:]*|\\d+)\\Z"))
          stop("Invalid gate name:", name, "in word", w);
       }
      return name;
     }
    void putBit() {bits.put(name, this);}                                       // Index the bit
    Bit  getBit() {return bits.get(name);}                                      // Get the  bit
    public String toString() {return name;}                                     // Name of bit
   }

  class Gate extends Bit                                                        // Description of a gate
   {final int               seq = nextGateNumber();                             // Sequence number for this gate
    final Operator           op;                                                // Operation performed by gate
    Gate                 iGate1,  iGate2;                                       // Gates driving the inputs of this gate as during simulation but not during layout
    Gate       soGate1, soGate2, tiGate1, tiGate2;                              // Pin assignments on source and target gates used during layout but not during simulation
    final TreeSet<WhichPin>
                         drives = new TreeSet<>();                              // The names of the gates that are driven by the output of this gate with a possible pin selection attached
    boolean          systemGate = false;                                        // System gate if true
    Integer    distanceToOutput;                                                // Distance to nearest output
    Boolean               value;                                                // Current output value of this gate
    Boolean           nextValue;                                                // Next value to be assumed by the gate
    boolean             changed;                                                // Changed on current simulation step
    int        firstStepChanged;                                                // First step at which we changed
    int         lastStepChanged;                                                // Last step at which we changed
    String        nearestOutput;                                                // The name of the nearest output so we can sort each layer to position each gate vertically so that it is on approximately the same Y value as its nearest output.
    int                  px, py;                                                // Position in x and y of gate in latest layout

    void indexGate() {gates.put(name, this);}                                   // Index the gate

    private Gate(Operator Op)                                                   // System created gate of a specified type with a unique system generated name. As yet the inputs are unknown.
     {super();
      op = Op;
      indexGate();
     }

    public Gate(Operator Op, String Name, Bit Input1, Bit Input2)               // User created gate with a user supplied name and inputs
     {super(Name);
      op   = Op;
      indexGate();
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
     {return "" + (value == null ? '.' : value ? '1' : '0');                    // Value of gate
     }

    void impinge(Bit Input)                                                     // Go to the named gate (which must therefore already exist) and show that it drives this gate on any input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new WhichPin(name));                                       // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin> d = pending.get(Input.name);
        if (d == null)   {d = new TreeSet<>(); pending.put(Input.name, d);}
        d.add(new WhichPin(name));
       }
     }

    void impinge(Bit Input, Boolean pin)                                        // Go to the named gate (which must therefore already exist) and show that it drives this gate on the specified input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new WhichPin(name, pin));                                  // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin>  d = pending.get(Input.name);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input.name, d);}
        d.add(new WhichPin(name, pin));
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
     {final int N = drives.size();                                              // Size of actual drives
      if (N == 0 && !systemGate)                                                // Check for user defined gates that do not drive any other gate
       {if (op == Operator.Output || op == Operator.FanOut) return;
        stop(op, "gate", name, "does not drive any gate");
        return;
       }

      if (N > 2) stop("Gate", name, "drives", N,                                // At this point each gate should have no more than two inputs
        "gates, but a gate can drive no more than 2 other gates");

      for (WhichPin t : drives)                                                 // Connect targeted gates back to this gate
       {if (t == null) continue;                                                // Fan out is woder than strictly necessary but tolerated to produce a complete tree.
        final Gate T = t.gate();
        if      (T.iGate1 == this || T.iGate2 == this) {}                       // Already set
        else if (T.iGate1 == null && t.ok1()) T.iGate1 = this;                  // Use input pin 1
        else if (T.iGate2 == null && t.ok2()) T.iGate2 = this;                  // Use input pin 2
        else                                                                    // No input pin available
         {if (t.pin == null) stop("Gate:", T.name,
            "driven by too many other gates, including one from gate:", name);
          else               stop("Gate:", T.name,
            "does not have enough pins to be driven by:", t, "from", name);
         }
       }
     }

    void updateEdge()                                                           // Update a memory gate on a leading edge. The memory bit on pin 2 is loaded when the value on pin 1 goes from high to low because reasoning about trailing edges seems to be easier than reasoning about leading edges
     {if (op == Operator.My)
       {if (iGate1.value != null && iGate1.value && iGate1.nextValue != null && !iGate1.nextValue)
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
      nextValue = null;                                                         // Null means we do not kow whatteh vlaue is.  In some cases involving dyadic commutative gates we only need to know one input to be able to deduce the output.  However, if the gate ouput cannot be computed then its result must be null meaning "could be true or  false".
      switch(op)                                                                // Gate operation
       {case And:   if (g != null && G != null)  nextValue =   g &&  G;  else if ((g != null && !g) || (G != null && !G)) nextValue = false; return;
        case Gt:    if (g != null && G != null)  nextValue =   g && !G;  return;
        case Lt:    if (g != null && G != null)  nextValue =  !g &&  G;  return;
        case Nand:  if (g != null && G != null)  nextValue = !(g &&  G); else if ((g != null && !g) || (G != null && !G)) nextValue = true; return;
        case Ngt:   if (g != null && G != null)  nextValue =  !g ||  G;  return;
        case Nlt:   if (g != null && G != null)  nextValue =   g || !G;  return;
        case Nor:   if (g != null && G != null)  nextValue = !(g ||  G); else if ((g != null &&  g) || (G != null &&  G)) nextValue = false; return;
        case Not:   if (g != null)               nextValue =  !g;        return;
        case Nxor:  if (g != null && G != null)  nextValue = !(g ^   G); return;
        case One:                                nextValue = true;       return;
        case Or:    if (g != null && G != null)  nextValue =   g ||  G;  else if ((g != null &&  g) || (G != null &&  G)) nextValue = true; return;
        case Xor:   if (g != null && G != null)  nextValue =   g ^   G;  return;
        case Zero:                               nextValue = false;      return;
        case Input:                              nextValue = value;      return;
        case Continue: case FanOut: case Output: nextValue =   g;        return;
       }
     }

    void fanOut()                                                               // Fan out when more than two gates are driven by this gate
     {final int N = drives.size(), N2 = N / 2;                                  // Number of pins driven by this gate
      if (op == Operator.Output) return;                                        // Output gates do not fan out
      if (N <= 2) return;                                                       // No fan out required because we have maintained the rule that no gate may drive more than two gates directly.

      final WhichPin[]D = drives.toArray(new WhichPin[N]);                      // The input pins driven by this output spread across the fan

      final Gate g = new Gate(Operator.FanOut), f = new Gate(Operator.FanOut);  // Even and greater than 2
      final int P = logTwo(N);                                                  // Length of fan out path - we want to make all the fanout paths the same length to simplify timing issues
      final int Q = powerTwo(P-1);                                              // Size of a full half
      for (int i = 0; i < Q; ++i)                                               // Lower half full tree
       {final WhichPin d = D[i];
        g.drives.add(d);                                                        // Transfer drive to the new lower gate
        drives.remove(d);                                                       // Remove drive from current gate
       }
      drives.add(new WhichPin(g.name));                                         // The old gate drives the new gate
      g.fanOut();                                                               // The lower half gate might need further fan out

      for (int i = Q; i < N; ++i)                                               // Upper half - might not be a complete tree
       {final WhichPin d = D[i];
        f.drives.add(d);                                                        // Transfer drive to new lower gate
        drives.remove(d);                                                       // Remove drive from current gate
       }

      if (2 * N >= 3 * Q)                                                       // Can fill out most of the second half of the tree
       {drives.add(new WhichPin(f.name));                                       // The old gate drives the new gate
        f.fanOut();                                                             // Fan out lower gate
       }
      else                                                                      // To few entries to fill more than half of the leaves so push the sub tree down one level
       {final Gate e = new Gate(Operator.FanOut);                               // Extend the path
        drives.add(new WhichPin(e.name));                                       // The old gate drives the extension gate
        e.drives.add(new WhichPin(f.name));                                     // The extension gate drives the new gate
        f.fanOut();                                                             // Fanout the smaller sub stree
       }
     }
   } // Gate

  Gate FanIn(Operator Op, String Name, Bit...Input)                             // Normal gate - not a fan out gate
   {final int N = Input.length;
    if (N == 0)                                                                 // Zerad
     {if (!zerad(Op)) stop(Op, "gate:", Name, "does not accept zero inputs");
      return new Gate(Op, Name, null, null);
     }

    if (N == 1)                                                                 // Monad
     {if (!monad(Op)) stop(Op, "gate:", Name, "does not accept just one input");
      return new Gate(Op, Name, Input[0], null);
     }

    if (N == 2)                                                                 // Dyad
     {if (!dyad(Op)) stop(Op, "gate:", Name, "does not accept two inputs");
      return new Gate(Op, Name, Input[0], Input[1]);
     }

    final int P = logTwo(N);                                                    // Length of fan out path - we want to make all the fanout paths the same length to simplify timing issues
    final int Q = powerTwo(P-1);                                                // Size of a full half

    final Operator l;                                                           // Lower operator
    switch(Op)
     {case Nand: l = Operator.And; break;
      case Nor : l = Operator.Or;  break;
      default  : l = Op;
     }

    final Operator u;                                                           // Upper operator
    switch(Op)
     {case And: case Nand: u = N > Q + 1 ? Operator.And : Operator.Continue; break;
      case Or : case Nor : u = N > Q + 1 ? Operator.Or  : Operator.Continue; break;
      default :            u = Op;
     }

    final Gate f = FanIn(l, nextGateName(), Arrays.copyOfRange(Input, 0, Q));   // Lower half is a full sub tree
    final Gate g = FanIn(u, nextGateName(), Arrays.copyOfRange(Input, Q, N));   // Upper half might not be full

    if (2 * N >= 3 * Q) return new Gate(Op, Name, f, g);                        // No need to extend path to balance it

    final Gate e = FanIn(Operator.Continue, nextGateName(), g);                 // Extension gate to make the upper half paths the same length as the lower half paths
    return new Gate(Op, Name, f, e);
   }

  Gate Input   (String n)               {return FanIn(Operator.Input,    n);}
  Gate One     (String n)               {return FanIn(Operator.One,      n);}
  Gate Zero    (String n)               {return FanIn(Operator.Zero,     n);}
  Gate Output  (String n, Bit i)        {return FanIn(Operator.Output,   n, i);}
  Gate Continue(String n, Bit i)        {return FanIn(Operator.Continue, n, i);}
  Gate Not     (String n, Bit i)        {return FanIn(Operator.Not,      n, i);}

  Gate Nxor    (String n, Bit i, Bit j) {return FanIn(Operator.Nxor, n, i, j);}
  Gate Xor     (String n, Bit i, Bit j) {return FanIn(Operator.Xor,  n, i, j);}
  Gate Gt      (String n, Bit i, Bit j) {return FanIn(Operator.Gt,   n, i, j);}
  Gate Ngt     (String n, Bit i, Bit j) {return FanIn(Operator.Ngt,  n, i, j);}
  Gate Lt      (String n, Bit i, Bit j) {return FanIn(Operator.Lt,   n, i, j);}
  Gate Nlt     (String n, Bit i, Bit j) {return FanIn(Operator.Nlt,  n, i, j);}
  Gate My      (String n, Bit i, Bit j) {return FanIn(Operator.My,   n, i, j);} // When pin 1 falls from 1 to 0 record the value of pin 2

  Gate And     (String n, Bit...i)      {return FanIn(Operator.And,  n, i);}
  Gate Nand    (String n, Bit...i)      {return FanIn(Operator.Nand, n, i);}
  Gate Or      (String n, Bit...i)      {return FanIn(Operator.Or,   n, i);}
  Gate Nor     (String n, Bit...i)      {return FanIn(Operator.Nor,  n, i);}

  Gate And     (String n, Stack<Bit> i) {return FanIn(Operator.And,  n, stackToBitArray(i));}
  Gate Nand    (String n, Stack<Bit> i) {return FanIn(Operator.Nand, n, stackToBitArray(i));}
  Gate Or      (String n, Stack<Bit> i) {return FanIn(Operator.Or,   n, stackToBitArray(i));}
  Gate Nor     (String n, Stack<Bit> i) {return FanIn(Operator.Nor,  n, stackToBitArray(i));}

  void anneal(Bit   ...inputs) {for (Bit    i: inputs) Output(nextGateName(),     i);} // Throw away a bit by annealing it to a bogus output gate so it does not reported as not driving any gate.  This is useful in testing but should be avoided on production chip as a waste of space.
  void anneal(BitBus...inputs) {for (BitBus i: inputs) outputBits(nextGateName(), i);} // Throw away all the bits in a but bus by annealing it to a bogus output gate so it does not reported as not driving any gate.  This is useful in testing but should be avoided on production chip as a waste of space.

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

// Simulation                                                                   // Simulate the behavior of the chip

  void printErrors()                                                            // Print any errors and stop if thre are any
   {if (errors.size() > 0)                                                      // Print any recorded errors and stop.
     {say(errors.size(), "errors during compilation of chip:");
      for (String s : errors) say(s);
      stop("Stopping because of chip compile errors");
     }
   }

  void compileChip()                                                            // Check that an input value has been provided for every input pin on the chip.
   {final Gate[]G = gates.values().toArray(new Gate[0]);
    for (Gate g : G) g.fanOut();                                                // Fan the output of each gate if necessary which might introduce more gates
    for (Gate g : gates.values()) g.compileGate();                              // Each gate on chip
    printErrors();
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
         {final Boolean v = inputs.get(g);
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
     {getGate(clock0.b(i)).value =  b[i-1];
      getGate(clock1.b(i)).value = !b[i-1];
     }
   }

  class Inputs                                                                  // Set values on input gates prior to simulation
   {private final Map<String,Boolean> inputs = new TreeMap<>();

    void set(Bit s, boolean value)                                              // Set the value of an input
     {inputs.put(s.name, value);
     }

    void set(BitBus input, int value)                                           // Set the value of an input bit bus
     {final int bits = input.bits;                                              // Get the size of the input bit bus
      final boolean[]b = bitStack(bits, value);                                 // Bits to set
      for (int i = 1; i <= bits; i++) set(input.b(i), b[i-1]);                  // Load the bits into the input bit bus
     }

    void set(Bit input, int value)                                              // Set the value of an input bit bus
     {set(input, value);
     }

    void set(WordBus wb, int...values)                                          // Set the value of an input word bus
     {for   (int w = 1; w <= wb.words; w++)                                     // Each word
       {final boolean[]b = bitStack(wb.bits, values[w-1]);                      // Bits from current word
        for (int i = 1; i <= wb.bits; i++) set(wb.b(w, i), b[i-1]);             // Load the bits into the input bit bus
       }
     }

    Boolean get(Bit s) {return inputs.get(s.name);}                             // Get the value of an input
   }

  interface SimulationStep {void step();}                                       // Called each simulation step
  SimulationStep simulationStep = null;                                         // Called each simulation step

  class ExecutionTrace                                                          // Trace the values of named bits and bit buses as the execution proceeds
   {final Object[]objects;                                                      // Objects to trace
    final String title;                                                         // Title
    final String format;                                                        // Format string
    final Stack<String> trace = new Stack<>();                                  // Execution trace
    ExecutionTrace(String Title, String Format, Object...Objects)               // Detail of objects to be traced
     {title = Title; format = Format; objects = Objects;
     }
    void trace()                                                                // Trace
     {final String s = String.format(format, objects);
      trace.push(s);
     }

    public String toString()                                                    // Print execution trace
     {final StringBuilder b = new StringBuilder();
      b.append("Step  "+title); b.append('\n');
      b.append(String.format("%4d  %s\n", 1, trace.firstElement()));
      for(int i = 1; i < trace.size(); ++i)
       {final String s = trace.elementAt(i-1);
        final String t = trace.elementAt(i);
        if (!s.equals(t)) b.append(String.format("%4d  %s\n", i+1, trace.elementAt(i)));
       }
      return b.toString();
     }
   }

  void executionTrace(String Title, String Format, Object...Objects)            // Request an execution trace
   {   executionTrace = new ExecutionTrace(Title, Format, Objects);
   }

  void printExecutionTrace() {if (executionTrace != null) say(executionTrace);} // Detail of objects to be traced

  void ok(String expected)                                                      // Confirm execution trace matches expected output
   {if (executionTrace == null) return;
    final String G = executionTrace.toString(), E = expected;
    final int lg = G.length(), le = E.length();
    final StringBuilder b = new StringBuilder();

    int fails = 0;
    if (le != lg)                                                               // Failed on length
     {++fails;
      say(b, "Mismatched length, got", lg, "expected", le, "got:\n"+G);
     }
    else                                                                        // Check characters in equal lengths strings
     {int l = 1, c = 1;
      for (int i = 0; i < lg; i++)
       {final int  e = E.charAt(i), g = G.charAt(i);
        if (e != g)                                                             // Character mismatch
         {say(b, "Differs at line:"+String.format("%04d", l),
              "character", c, ", expected:", ((char)e),
                              ", got:",      ((char)g));
          say(b, "      0----+----1----+----2----+----3----+----4----+----5----+----6");
          final String[]t = G.split("\\n");
          for(int k = 0; k < t.length; k++)                                     // Details of  mismatch
           {say(b, String.format("%04d  ", k+1)+t[k]);
            if (l == k+1) say(b, " ".repeat(6+c)+'^');
           }
          ++fails;
          break;
         }
        if (e == '\n') {++l; c = 0;} else c++;
       }
     }
    if (fails > 0)                                                              // Failed this test
     {++testsFailed;
      err(b);                                                                   // Show error location with a traceback so we know where the failure occurred
     }
    else ++testsPassed;                                                         // Passed this test
   }

  Diagram simulate() {return simulate(null);}                                   // Simulate the operation of a chip with no input pins. If the chip has in fact got input pins an error will be reported.

  Diagram simulate(Inputs inputs)                                               // Simulate the operation of a chip
   {compileChip();                                                              // Check that the inputs to each gate are defined
    initializeGates(inputs);                                                    // Set the value of each input gate

    final int actualMaxSimulationSteps =                                        // Actual limit on number of steps
      maxSimulationSteps != null ? maxSimulationSteps : defaultMaxSimulationSteps;
    final boolean miss = minSimulationSteps != null;                            // Minimum simulation steps set

    for (steps = 1; steps <= actualMaxSimulationSteps; ++steps)                 // Steps in time
     {loadClock();                                                              // Load the value of the clock into the clock input bus
      loadPulses();
      for (Gate g : gates.values()) g.step();                                   // Compute next value for  each gate
      for (Gate g : gates.values()) g.updateEdge();                             // Update each gate triggered by an edge transition
      for (Gate g : gates.values()) g.updateValue();                            // Update each gate
      if (executionTrace != null) executionTrace.trace();                       // Trace requested

      if (!changes())                                                           // No changes occurred
       {if (!miss || steps >= minSimulationSteps)                               // No changes occurred and we are beyond the minimum simulation time or no such time was set
         {return gates.size() <= layoutLTGates ? drawSingleLevelLayout() : null;// Draw the layout if it has less than the specified maximum number of gates for being drawn automatically with out a specific request.
         }
       }
      noChangeGates();                                                          // Reset change indicators
     }
    if (maxSimulationSteps == null)                                             // Not enough steps available by default
     {err("Out of time after", actualMaxSimulationSteps, "steps");
     }
    return null;
   }

//D1 Circuits                                                                   // Some useful circuits

//D2 Bits                                                                       // Operations on bits

  Gate bit(String name, int value)                                              // Create a constant bit as true if the supplied number is non zero else false
   {return value > 0 ? One(name) : Zero(name);
   }

  static boolean[]bitStack(int bits, int value)                                 // Create a stack of bits, padded with zeroes if necessary, representing an unsigned integer with the least significant bit lowest.
   {final boolean[]b = new boolean[bits];
    for (int i = 0; i < bits; ++i) b[i] = (value & (1 << i)) != 0;
    return b;
   }

  Boolean getBit(String name)                                                   // Get the current value of the named gate.
   {final Gate g = getGate(name);                                               // Gate providing bit
    if (g == null) stop("No such gate named:", name);
    return g.value;                                                             // Bit state
   }

//D2 Buses                                                                      // A bus is an array of bits or an array of arrays of bits

  class BitBus                                                                  // The width of the bit bus
   {final String name;                                                          // Name of bus
    final int    bits;                                                          // Bits of bus
    final Stack<Bit> bit = new Stack<>();
    BitBus(String Name, int Bits)                                               // Create a new bit bus
     {name = Name;
      bits = Bits;
      if (bitBuses.containsKey(name)) stop("BitBus", name, "has already been defined");
      bitBuses.put(name, this);
      for (int i = 1; i <= bits; i++) bit.push(new Bit(b(i).name));             // Define the individual bits in the bit bus
     }

    void sameSize(BitBus b)                                                     // Check two buses are the same size and stop if they are not
     {final int A = bits, B = b.bits;
      if (A != B) stop("Input",  name, "has width", A, "but input", b.name, "has width", B);
     }

    Bit b(int i) {return new Bit(n(i, name));}                                  // Name of a bit in the bus

    Integer Int()                                                               // Convert the bits represented by an output bus to an integer
     {int v = 0, p = 1;
      for (int i = 1; i <= bits; i++)                                           // Each bit on bus
       {final Bit n = b(i);                                                     // Name of gate supporting named bit
        final Gate g = getGate(n);                                              // Gate providing bit
        if (g == null)
         {say("No such gate as:", n);
          return null;
         }
        if (g.value == null) return null;                                       // Bit state not known
        if (g.value) v += p;
        p *= 2;
       }
      return v;
     }

    public String toString()                                                    // Convert the bits represented by an output bus to a string
     {final StringBuilder b = new StringBuilder();
      for (int i = 1; i <= bits; i++)
       {final Gate g = getGate(b(i));
        if (g != null)
         {final Boolean j = g.value;
          b.append(j == null ? '.' : j ? '1' : '0');
         }
        else stop("No such gate as:", b(i));
       }
      return b.reverse().toString();
     }

    void ok(Integer e)                                                          // Confirm the expected values of the bit bus. Write a message describing any unexpected values
     {final Integer g = Int();                                                  // The values we actually got
      final StringBuilder b = new StringBuilder();
      int fails = 0, passes = 0;
      if (false)                       {}
      else if (e == null && g == null) {}
      else if (e != null && g == null) {b.append(String.format("Expected %d, but got null\n", e   )); ++fails;}
      else if (e == null && g != null) {b.append(String.format("Expected null, but got %d\n", g   )); ++fails;}
      else if (e != g)                 {b.append(String.format("Expected %d, but got %d\n",   e, g)); ++fails;}
      else ++passes;
      if (fails > 0) err(b);
      testsPassed += passes; testsFailed += fails;                              // Passes and fails
     }
   }

  BitBus findBits(String name)                                                  // Find a bit bus by name
   {final BitBus b = bitBuses.get(name);
    if (b == null) stop("No such bit bus as", name);
    return b;
   }

  static String n(String...C)                                                   // Gate name from no index probably to make a bus connected to a name
   {return concatenateNames(concatenateNames((Object[])C));
   }

  static String n(int i, String...C)                                            // Gate name from single index.
   {return concatenateNames(concatenateNames((Object[])C), i);
   }

  static String n(int i, int j, String...c)                                     // Gate name from double index.
   {return concatenateNames(concatenateNames((Object[])c), i, j);
   }

  static String concatenateNames(Object...O)                                    // Concatenate names to construct a gate name
   {final StringBuilder b = new StringBuilder();
    for (Object o: O) {b.append("_"); b.append(o);}
    return O.length > 0 ? b.substring(1) : "";
   }

//D3 Bits                                                                       // An array of bits that can be manipulated via one name.

  int sizeBits(String name)                                                     // Get the size of a bits bus.
   {final BitBus s = bitBuses.get(name);
    if (s == null) stop("No bit bus named", name);
    return s.bits;
   }

  BitBus collectBits(String name, int bits)                                     // Set the size of a bits bus.
   {final BitBus b = bitBuses.get(name);

    if (b != null)                                                              // Bus already exists
     {if (b.bits != bits) stop("A bit bus with name:", name, "and bits", b.bits,
                        "has already been defined versus bits requested:", bits);
      return b;                                                                 // Treat as reuse if a bus of the same name and size already exists
     }
    return new BitBus(name, bits);                                              // Create new bit bus
   }

  BitBus bits(String n, int bits, int value)                                    // Create a bus set to a specified number.
   {final boolean[]b = bitStack(bits, value);                                   // Number as a stack of bits padded to specified width
    final BitBus   B = new BitBus(n, bits);
    for(int i = 1; i <= bits; ++i)                                              // Generate constant
      if (b[i-1]) One(B.b(i).name); else Zero(B.b(i).name);
    return B;
   }

  BitBus bits(int bits, int value)                                              // Create an unnamed bus set to a specified number.
   {return bits(nextGateName(), bits, value);                                   // Create bus
   }

  BitBus inputBits(String name, int bits)                                       // Create an B<input> bus made of bits.
   {final BitBus B = new BitBus(name, bits);                                    // Record bus width
    for (int b = 1; b <= bits; ++b) Input(B.b(b).name);                         // Bus of input gates
    return B;
   }

  BitBus outputBits(String name, BitBus input)                                  // Create an B<output> bus made of bits.
   {final int bits = input.bits;                                                // Number of bits in input bus
    final BitBus o = new BitBus(name, bits);                                    // Resulting bit bus
    for (int i = 1; i <= bits; i++) Output(o.b(i).name, input.b(i));            // Bus of output gates
    return o;                                                                   // Resulting bit bus
   }

  BitBus outputBits(String name, String input)                                  // Create an B<output> bus made of bits.
   {return outputBits(name, findBits(input));
   }

  BitBus notBits(String name, BitBus input)                                     // Create a B<not> bus made of bits.
   {final int bits = input.bits;                                                // Number of bits in input bus
    final BitBus o = new BitBus(name, bits);                                    // Resulting bit bus
    for (int b = 1; b <= bits; ++b) Not(o.b(b).name, input.b(b));               // Bus of not gates
    return o;                                                                   // Resulting bit bus
   }

  BitBus notBits(String name, String input)                                     // Create a B<not> bus made of bits.
   {return notBits(name, findBits(input));
   }

  Gate andBits(String name, BitBus input)                                       // B<And> all the bits in a bus to get one bit
   {final int bits = input.bits;                                                // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return And(name, b);                                                        // And all the bits in the bus
   }

  Gate andBits(String name, String input)                                       // B<And> all the bits in a bus to get one bit
   {return andBits(name, findBits(input));
   }

  BitBus andBits(String output, BitBus input1, BitBus input2)                   // B<And> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus 1
    final int c = input2.bits;                                                  // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check sizes match
    final BitBus o = new BitBus(output, b);                                     // Size of resulting bus
    for (int i = 1; i <= b; ++i) And(o.b(i).name, input1.b(i), input2.b(i));    // And each pair of bits
    return o;
   }

  Gate nandBits(String name, BitBus input)                                      // B<Nand> all the bits in a bus to get one bit
   {final int bits = input.bits;                                                // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Nand(name, b);                                                       // Nand all the bits in the bus
   }

  Gate nandBits(String name, String input)                                      // B<Nand> all the bits in a bus to get one bit
   {return nandBits(name, findBits(input));                                     // Number of bits in input bus
   }

  BitBus nandBits(String output, BitBus input1, BitBus input2)                  // B<Nand> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus 1
    final int c = input2.bits;                                                  // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final BitBus o = new BitBus(output, b);                                     // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nand(o.b(i).name, input1.b(i), input2.b(i));   // Nand each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  Gate orBits(String name, BitBus input)                                        // B<Or> all the bits in a bus to get one bit
   {final int bits = input.bits;                                                // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Or(name, b);                                                         // Or all the bits in the bus
   }

  Gate orBits(String name, String input)                                        // B<Or> all the bits in a bus to get one bit
   {return orBits(name, findBits(input));
   }

  BitBus orBits(String output, BitBus input1, BitBus input2)                    // B<Or> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus
    final int c = input2.bits;                                                  // Number of bits in input bus
    input1.sameSize(input2);                                                    // Check bus sizes match
    final BitBus o = new BitBus(output, b);                                     // Resulting bit bus
    for (int i = 1; i <= b; ++i) Or(o.b(i).name, input1.b(i), input2.b(i));     // Or each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  Gate norBits(String name, BitBus input)                                       // B<Nor> all the bits in a bus to get one bit
   {final int bits = input.bits;                                                // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Nor(name, b);                                                        // Or all the bits in the bus
   }

  Gate norBits(String name, String input)                                       // B<Nor> all the bits in a bus to get one bit
   {return norBits(name, findBits(input));
   }

  BitBus norBits(String output, BitBus input1, BitBus input2)                   // B<Nor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus 1
    final int c = input2.bits;                                                  // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final BitBus o = new BitBus(output, b);                                     // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nor(o.b(i).name, input1.b(i), input2.b(i));    // Nor each pair of bits
    return o;                                                                   // Resulting bus
   }

  BitBus xorBits(String output, BitBus input1, BitBus input2)                   // B<Xor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus 1
    final int c = input2.bits;                                                  // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final BitBus o = new BitBus(output, b);                                     // Resulting bit bus
    for (int i = 1; i <= b; ++i) Xor(o.b(i).name, input1.b(i), input2.b(i));    // Xor each pair of bits
    return o;                                                                   // Resulting bus
   }

  BitBus nxorBits(String output, BitBus input1, BitBus input2)                  // B<Nxor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits;                                                  // Number of bits in input bus 1
    final int c = input2.bits;                                                  // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final BitBus o = new BitBus(output, b);                                     // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nxor(o.b(i).name, input1.b(i), input2.b(i));   // Nxor each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  void connect(BitBus input1, BitBus input2)                                    // Connect two bit buses together.
   {input1.sameSize(input2);                                                    // Check the buses are the same size
    final int bits = input1.bits;                                               // Number of bits in input bus
    for (int b = 1; b <= bits; ++b) Continue(input1.b(b).name, input2.b(b));    // Connect the buses
   }

  BitBus orBitBuses(String output, BitBus...input)                              // B<Or> several equal size bit buses together to make an equal sized bit bus
   {final int N = input.length;
    if (N < 1) stop("Need at least one input bit bus");
    for (int i = 1; i < N; ++i) input[0].sameSize(input[i]);                    // Make sure all the bit buses have the same length
    final int b = input[0].bits;                                                // Number of bits in input bus
    final BitBus o = collectBits(output, b);                                    // Resulting bit bus
    for   (int i = 1; i <= b; ++i)                                              // Each bit
     {Stack<Bit> or = new Stack<>();                                            // Corresponding bits
      for (int j = 0; j <  N; ++j) or.push(input[j].b(i));                      // Each corresponding bit in each bus
      Or(o.b(i).name, stackToBitArray(or));                                     // Or across buses
     }
    return o;                                                                   // Size of resulting bus
   }

  BitBus andBitBuses(String output, BitBus...input)                             // B<And> several equal size bit buses together to make an equal sized bit bus
   {final int N = input.length;
    if (N < 1) stop("Need at least one input bit bus");
    for (int i = 1; i < N; ++i) input[0].sameSize(input[i]);                    // Make sure all the bit buses have the same length
    final int b = input[0].bits;                                                // Number of bits in input bus
    final BitBus o = collectBits(output, b);                                    // Resulting bit bus
    for   (int i = 1; i <= b; ++i)                                              // Each bit
     {Stack<Bit> or = new Stack<>();                                            // Corresponding bits
      for (int j = 0; j <  N; ++j) or.push(input[j].b(i));                      // Each corresponding bit in each bus
      And(o.b(i).name, stackToBitArray(or));                                    // And across buses
     }
    return o;                                                                   // Size of resulting bus
   }

//D3 Words                                                                      // An array of arrays of bits that can be manipulated via one name.

  class WordBus                                                                 // Description of a word bus
   {final String name;                                                          // Name of the word bus
    final int bits;                                                             // Bits in each word of the bus
    final int words;                                                            // Words in bus
    final Stack<BitBus> bitBus = new Stack<>();
    WordBus(String Name, int Words, int Bits)                                   // Create bus
     {name = Name; bits = Bits; words = Words;
      final WordBus w = wordBuses.get(name);                                    // Chip, bits bus name, words, bits per word, options
      if (w != null)
        stop("A word bus with name:", name, "has already been defined");
      wordBuses.put(name, this);
      for (int b = 1; b <= words; ++b)                                          // Size of bit bus for each word in the word bus
       {final String s = n(b, name);
        BitBus B = bitBuses.get(s);                                             // Check whether the component bitBus exists or not
        if (B == null) B = new BitBus(s, bits);                                 // Create component bitBus if necessary
        bitBus.push(B);                                                         // Bit buses associated with this word bus
       }
     }
    WordBus(String Name, WordBus Words) {this(Name, Words.words, Words.bits);}  // Create a word bus with the same dimensions as the specified word bus.

    Integer[]Int()                                                              // Convert the words on a word bus into integers
     {final Integer[]r = new Integer[words];                                    // Words on bus
      loop: for (int j = 1; j <= words; j++)                                    // Each word on bus
       {r[j-1] = null;                                                          // Value not known
        int v = 0, p = 1;
        for (int i = 1; i <= bits; i++)                                         // Each bit on bus
         {final Gate g = getGate(Chip.this.n(j, i, name));                      // Gate providing bit
          if (g == null)
           {say("No such word as:", name);
            return null;
           }
          if (g.value == null) continue loop;                                   // Bit state not known
          if (g.value) v += p;
          p *= 2;
         }
        r[j-1] = v;                                                             // Save value of this word
       }
      return r;
     }

    BitBus w(int i)        {return bitBuses.get(Chip.this.n(i,    name));}      // Get a bit bus in the word bus
    Bit    b(int i, int j) {return new Bit     (Chip.this.n(i, j, name));}      // Get a bit from a bit bus in the word bus

    void ok(Integer...E)                                                        // Confirm the expected values of the word bus. Write a message describing any unexpected values
     {final Integer[]G = Int();                                                 // The values we actually got
      final StringBuilder b = new StringBuilder();
      final int lg = G.length, le = E.length;
      if (le != lg) stop("Mismatched length, got", lg, "expected", le, "got:\n"+G);
      int fails = 0, passes = 0;
      for (int i = 0; i < lg; i++)
       {final Integer e = E[i], g = G[i];
        if (false)                       {}
        else if (e == null && g == null) {}
        else if (e != null && g == null) {b.append(String.format("Index %d expected %d, but got null\n", i, e   )); ++fails;}
        else if (e == null && g != null) {b.append(String.format("Index %d expected null, but got %d\n", i, g   )); ++fails;}
        else if (e != g)                 {b.append(String.format("Index %d expected %d, but got %d\n",   i, e, g)); ++fails;}
        else ++passes;
       }
      if (fails > 0) err(b);
      testsPassed += passes; testsFailed += fails;                              // Passes and fails
     }
   }

  WordBus findWords(String name)                                                // Find a word bus by name
   {final WordBus w = wordBuses.get(name);
    if (w == null) stop("No such word bus as", name);
    return w;
   }

  WordBus collectWords(String name, int words, int bits)                        // Set the size of a bits bus.
   {final WordBus w = wordBuses.get(name);                                      // Chip, bits bus name, words, bits per word, options
    if (w != null)
      stop("A word bus with name:", name, "has already been defined");
    final WordBus wb = new WordBus(name, words, bits);
    wordBuses.put(name, wb);
    for (int b = 1; b <= words; ++b) collectBits(n(b, name), bits);             // Size of bit bus for each word in the word bus
    return wb;
   }

  WordBus words(String name, int bits, int...values)                            // Create a word bus set to specified numbers.
   {final WordBus wb = new WordBus(name, values.length, bits);                  // Record bus width

    for (int w = 1; w <= values.length; ++w)                                    // Each value to put on the bus
     {final int value = values[w-1];                                            // Each value to put on the bus
      final String  s = Integer.toBinaryString(value);                          // Bit in number
      final Stack<Boolean> b = new Stack<>();
      for (int i = s.length(); i > 0; --i)                                      // Stack of bits with least significant lowest
       {b.push(s.charAt(i-1) == '1' ? true : false);
       }
      for (int i = b.size(); i <= bits; ++i) b.push(false);                     // Extend to requested bits
      for (int i = 1; i <= bits; ++i)                                           // Generate constant
       {final boolean B = b.elementAt(i-1);                                     // Bit value
        if (B) One(wb.b(w, i).name); else Zero(wb.b(w, i).name);                // Set bit
       }
     }
    return wb;
   }

  WordBus words(int bits, int[]values)                                          // Create an unnamed word bus set to specified numbers.
   {return words(nextGateName(), bits, values);
   }

  WordBus inputWords(String name, int words, int bits)                          // Create an B<input> bus made of words.
   {final WordBus wb = new WordBus(name, words, bits);
    for   (int w = 1; w <= words; ++w)                                          // Each word on the bus
      for (int b = 1; b <= bits;  ++b) Input(wb.b(w, b).name);                  // Each word on the bus

    return wb;
   }

  WordBus outputWords(String name, WordBus wb)                                  // Create an B<output> bus made of words.
   {final WordBus o = new WordBus(name, wb.words, wb.bits);                     // Record bus width
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {for (int b = 1; b <= wb.bits;  ++b) Output(o.b(w, b).name, wb.b(w, b));   // Each bit on each word on the bus
     }
    return o;
   }

  WordBus notWords(String name, WordBus wb)                                     // Create a B<not> bus made of words.
   {final WordBus o = new WordBus(name, wb.words, wb.bits);                     // Record bus width
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {for (int b = 1; b <= wb.bits;  ++b) Not(o.b(w, b).name, wb.b(w, b));      // Each bit of eacvh word on the bus
     }
    return o;
   }

  BitBus andWordsX(String name, WordBus wb)                                     // Create a bit bus of width equal to the number of words in a word bus by and-ing the bits in each word to make the bits of the resulting word.
   {final BitBus B = new BitBus(name, wb.words);                                // One bit for each word
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {final Stack<Bit> bits = new Stack<>();                                    // Select bits
      for (int b = 1; b <= wb.bits; ++b) bits.push(wb.b(w, b));                 // Bits to and
      And(B.b(w).name, bits);                                                   // And bits
     }
    return B;
   }

  BitBus andWords(String name, WordBus wb)                                      // Create a bit bus of the same width as each word in a word bus by and-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final BitBus B = new BitBus(name, wb.bits);                                 // One bit for each word
    for   (int b = 1; b <= wb.bits;  ++b)                                       // Each bit in the words on the bus
     {final Stack<Bit> words = new Stack<>();                                   // Select bits
      for (int w = 1; w <= wb.words; ++w) words.push(wb.b(w, b));               // The current bit in each word in the bus
      And(B.b(b).name, words.toArray(new Bit[words.size()]));                   // Combine inputs using B<and> gates
     }
    return B;
   }

  BitBus orWordsX(String name, WordBus wb)                                      // Create a bit bus of width equal to the number of words in a word bus by or-ing the bits in each word to make the bits of the resulting word.
   {final BitBus B = new BitBus(name, wb.words);                                // One bit for each word
    for   (int w = 1; w <= wb.words; ++w)                                       // Each word on the bus
     {final Stack<Bit> bits = new Stack<>();                                    // Select bits
      for (int b = 1; b <= wb.bits; ++b) bits.push(wb.b(w, b));                 // Bits to or
      Or(B.b(w).name, bits);                                                    // Or bits
     }
    return B;
   }

  BitBus orWords(String name, WordBus wb)                                       // Create a bit bus of the same width as each word in a word bus by or-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final BitBus B = new BitBus(name, wb.bits);                                 // One bit for each word
    for   (int b = 1; b <= wb.bits;  ++b)                                       // Each bit in the words on the bus
     {final Stack<Bit> words = new Stack<>();                                   // Select bits
      for (int w = 1; w <= wb.words; ++w) words.push(wb.b(w, b));               // Each word on the bus
      Or(B.b(b).name, words.toArray(new Bit[words.size()]));                    // Combine inputs using B<or> gates
     }
    return B;
   }

//D2 Comparisons                                                                // Compare unsigned binary integers of specified bit widths.

  Gate compareEq(String output, BitBus a, BitBus b)                             // Compare two unsigned binary integers of a specified width returning B<1> if they are equal else B<0>.  Each integer is supplied as a bus.
   {final int A = a.bits;                                                       // Width of first bus
    final int B = b.bits;                                                       // Width of second bus
    a.sameSize(b);                                                              // Check buses match in size
    final BitBus eq = new BitBus(n(output, "eq"), A);                           // Compare each pair of bits
    for (int i = 1; i <= B; i++) Nxor(eq.b(i).name, a.b(i), b.b(i));            // Test each bit pair for equality
    return andBits(output, eq);                                                 // All bits must be equal
   }

  Gate compareGt(String output, BitBus a, BitBus b)                             // Compare two unsigned binary integers for greater than.
   {final int A = a.bits;
    final int B = b.bits;
    a.sameSize(b);

    final BitBus oe = new BitBus(n(output, "e"), A);                            // Bits equal in input buses
    final BitBus og = new BitBus(n(output, "g"), A);                            // Bits greater than
    final BitBus oc = new BitBus(n(output, "c"), A);                            // Bit greater than with all other bits equal

    for (int i = 2; i <= B; i++) Nxor(oe.b(i).name, a.b(i), b.b(i));            // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Gt  (og.b(i).name, a.b(i), b.b(i));            // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
     {final Stack<Bit> and = new Stack<>(); and.push(og.b(j-1));
      for (int i = j; i <= B; i++) and.push(oe.b(i));
      And(oc.b(j).name, stackToBitArray(and));
     }

    final Stack<Bit> or = new Stack<>();
    for (int i = 2; i <= B; i++) or.push(oc.b(i));                              // Equals  followed by greater than
                                 or.push(og.b(B));
    return Or(output, or);                                                      // Any set bit indicates that first is greater then second
   }

  Gate compareLt(String output, BitBus a, BitBus b)                             // Compare two unsigned binary integers for less than.
   {final int A = a.bits, B = b.bits;
    a.sameSize(b);

    final BitBus oe = new BitBus(n(output, "e"), A);                            // Bits equal in input buses
    final BitBus ol = new BitBus(n(output, "l"), A);                            // Bits less than
    final BitBus oc = new BitBus(n(output, "c"), A);                            // Bit less than with all other bits equal

    for (int i = 2; i <= B; i++) Nxor(oe.b(i).name, a.b(i), b.b(i));            // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Lt  (ol.b(i).name, a.b(i), b.b(i));            // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
     {final Stack<Bit> and = new Stack<>(); and.push(ol.b(j-1));
      for (int i = j; i <= B; i++) and.push(oe.b(i));
      And(oc.b(j).name, and);
     }

    final Stack<Bit> or = new Stack<>();
    for (int i = 2; i <= B; i++) or.push(oc.b(i));                              // Equals followed by less than
                                 or.push(ol.b(B));
    return Or(output, or);                                                      // Any set bit indicates that first is less than second
   }

  BitBus chooseFromTwoWords(String output, BitBus a, BitBus b, Bit choose)      // Choose one of two words depending on a choice bit.  The first word is chosen if the bit is B<0> otherwise the second word is chosen.
   {final int A = a.bits, B = b.bits;
    if (A != B) stop("First input bus", a, "has width", A,
                     ", but second input bus", b, "has width", B);

    final BitBus         o = new BitBus(output, B);                             // Output bus
    final String notChoose = nextGateName(output);                              // Opposite of choice
    final Gate           n = Not(notChoose, choose);                            // Invert choice

    final BitBus oa = new BitBus(n(output, "a"), A);                            // Choose first word
    final BitBus ob = new BitBus(n(output, "b"), A);                            // Choose second word

    for (int i = 1; i <= B; i++)                                                // Each bit
     {final Gate ga = And(oa.b(i).name, a.b(i), n);                             // Choose first word if not choice
      final Gate gb = And(ob.b(i).name, b.b(i), choose);                        // Choose second word if choice
      Or (o.b(i).name, ga, gb);                                                 // Or results of choice
     }
    return o;                                                                   // Record bus size
   }

  BitBus enableWord(String output, BitBus a, Bit enable)                        // Output a word or zeros depending on a choice bit.  The word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
   {final int A = a.bits;
    final BitBus o = new BitBus(output, A);                                     // Record bus size
    for (int i = 1; i <= A; i++) And(o.b(i).name, a.b(i), enable);              // Choose each bit of input word
    return o;
   }

//D2 Masks                                                                      // Point masks and monotone masks. A point mask has a single B<1> in a sea of B<0>s as in B<00100>.  A monotone mask has zero or more B<0>s followed by all B<1>s as in: B<00111>.

  BitBus monotoneMaskToPointMask(String output, BitBus input)                   // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
   {final int B = input.bits;                                                   // Number of bits in input monotone mask

    final BitBus o = new BitBus(output, B);                                     // Size of resulting bus representing the chosen integer
    for (int i = 1; i <= B;  i++)                                               // Each bit in each possible output number
      if (i > 1) Lt(o.b(i).name, input.b(i-1), input.b(i));                     // Look for a step from 0 to 1
      else Continue(o.b(i).name,               input.b(i));                     // First bit is 1 so point is in the first bit

    return o;
   }

  BitBus chooseWordUnderMask(String output, WordBus input, BitBus mask)         // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
   {final WordBus wb = input;
    final BitBus   o = new BitBus(output, wb.bits);
    final int     mi = mask.bits;
    if (mi != wb.words)
      stop("Mask width", mi, "does not match number of words ", wb.words);

    final WordBus a = new WordBus(n(output, "a"), wb.words, wb.bits);
    for   (int w = 1; w <= wb.words; ++w)                                       // And each bit of each word with the mask
      for (int b = 1; b <= wb.bits;  ++b)                                       // Bits in each word
        And(a.b(w, b).name, mask.b(w), input.b(w, b));

    for   (int b = 1; b <= wb.bits; ++b)                                        // Bits in each word
     {final Stack<Bit> or = new Stack<>();
      for (int w = 1; w <= wb.words; ++w) or.push(a.b(w, b));                   // And each bit of each word with the mask
      Or(o.b(b).name, or);
     }
    return o;
   }

  WordBus insertIntoArray(String Output,                                        // Shift the words selected by the monotone mask up one position.
    WordBus Input, BitBus Mask, BitBus Insert)
   {final int words = Input.words, bits = Input.bits;
    if (bits      != Insert.bits) stop("Insert is", Insert.bits, "bits, but the input words are", bits, "wide");
    if (Mask.bits != words) stop("Mask has", Mask.bits, "bits to select", words, "words");

    final BitBus  M = Mask;                                                     // The monotone mask
    final BitBus  P = monotoneMaskToPointMask(n(Output, "pm"), M);              // Make a point mask from the input monotone mask
    final BitBus  N = notBits                (n(Output, "np"), M);              // Invert the monotone mask

    final WordBus u = new WordBus(n(Output, "upper"),  words-1, bits);          // Shifted words  to fill upper part
    final WordBus l = new WordBus(n(Output, "lower"),  words,   bits);          // Un-shifted words to fill lower part
    final WordBus o = new WordBus(  Output,            Input);                  // Resulting array of shifted words
    final WordBus I = new WordBus(n(Output, "Insert"), Input);                  // The first word - insertion

    for (int b = 1; b <= bits;  ++b)                                            // Bits in each word in shift area
     {And(l.b(1, b).name, Input.b(1, b), N.b(1));                               // Select lower words
      And(I.b(1, b).name, Insert.b(b),   P.b(1));                               // Select lower words
      Or (o.b(1, b).name, l.b(1, b),     I.b(1, b));                            // First word of output is the corresponding input word or inserted word depending on the mask
     }

    for   (int w = 2; w <= words; ++w)                                          // Words in upper shifted area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(u.b(w-1, b).name, Input.b(w-1, b), M.b(w-1));                       // Shifted upper bits

    for   (int w = 2; w <= words; ++w)                                          // Words in shift area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
       {And(l.b(w, b).name, Input.b(w,   b), N.b(w));                           // Un-shifted lower bits
        And(I.b(w, b).name, Insert.b(b),     P.b(w));                           // Select lower words
        Or (o.b(w, b).name, u.b(w-1, b),     l.b(w, b), I.b(w, b));             // Combine un-shifted, insert, shifted
       }
    return o;                                                                   // Copy of input with word inserted at the indicated position
   }

  class RemoveFromArray                                                         // Results of removing a word from a word bus
   {final WordBus    out;                                                       // Resulting word bus with indicated word removed and a new value shifted in at the high end
    final BitBus removed;                                                       // The removed word

    RemoveFromArray(WordBus Out, BitBus Removed)                                // Construct
     {out = Out; removed = Removed;
     }
   }

  RemoveFromArray removeFromArray(String Output,                                // Remove a word from an array, slide the array down to cover the gap and insert a new word at the top to cover the resulting gap there.
    WordBus Input, BitBus Mask, BitBus Insert)
   {final int words = Input.words, bits = Input.bits;
    if (bits      != Insert.bits) stop("Insert is", Insert.bits, "bits, but the input words are", bits, "wide");
    if (Mask.bits != words) stop("Mask has", Mask.bits, "bits to select", words, "words");

    final BitBus  P = monotoneMaskToPointMask(n(Output, "pm"), Mask);           // Make a point mask from the input monotone mask          0100
    final BitBus  p = notBits                (n(Output, "ip"), P);              // Invert the point mask                                   1011
    final BitBus  M = andBits                (n(Output, "sm"), Mask, p);        // This is the original monotone mask minus its first bit  1000
    final BitBus  N = notBits                (n(Output, "np"), Mask);           // Invert the monotone mask                                0011

    final WordBus u = new WordBus(n(Output, "upper"),  words-1, bits);          // Shifted words   to fill upper part
    final WordBus l = new WordBus(n(Output, "lower"),  words,   bits);          // Un-shifted words to fill lower part
    final WordBus o = new WordBus(  Output,            Input);                  // Resulting array of shifted words
    final WordBus S = new WordBus(n(Output, "select"), Input);                  // Select the word to remove

    for   (int w = 1; w <  words; ++w)                                          // Words in upper shifted area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(u.b(w, b).name, Input.b(w+1, b), M.b(w+1));                         // Shifted upper bits

    final Gate   nz = orBits(n(Output, "zm"), Mask);                            // True if the mask is not all zeros
    final BitBus  U = chooseFromTwoWords(n(Output, "U"),                        // If the mask is zero we want the output to equal the input
                        Input.w(words), Insert, nz);
    connect(o.w(words), U);                                                     // Inserted word if non zero mask else no change on input
//  connect(o.w(words), Insert);                                                // Word inserted into highest word of output

    for   (int w = 1; w <  words; ++w)                                          // Combine shifted upper and lower
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word
       {And(l.b(w, b).name, Input.b(w, b), N.b(w));                             // Lower words
        Or (o.b(w, b).name,     u.b(w, b), l.b(w, b));                          // Combine shifted upper and lower
       }

    for   (int w = 1; w <=  words; ++w)                                         // Removed word
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(S.b(w, b).name, Input.b(w,   b), P.b(w));                           // Select removed word

    anneal(N.b(words), M.b(words), M.b(1));                                     // No selection required here because the final word is added regardless
    final BitBus R = orWords(n(Output, "remove"), S);                           // Removed word
    return new RemoveFromArray(o, R);                                           // Resulting word bus and removed word
   }

//D2 Registers                                                                  // Create registers

  class RegIn                                                                   // A BitBus, bit pair representing one possible input to a register
   {final BitBus  input;                                                        // Input bus from which the register is loaded
    final Bit      load;                                                        // Load bit: when this bit goes from high to low the register is loaded from the first input bus.

    RegIn(BitBus Input, Bit Load)                                               // Register input
     {input = Input; load = Load;
     }
   }

  class Register extends BitBus                                                 // Description of a register
   {final RegIn    []  I;                                                       // Inputs
    final BitBus   []  E;                                                       // Enabled inputs
    final BitBus       e;                                                       // Combined enabled input
    final Bit       load;                                                       // Combined load - the register gets loaded after a short delay after this bit falls from 1 to 0.
    final Bit     loaded;                                                       // Falls from 1 to 0 once the register has been loaded

    Register(String Output, BitBus Input, Bit Load)                             // Create register from single input
     {super(Output, Input.bits);
      I = new RegIn[1]; I[0] = new RegIn(Input, Load);
      final int w = Input.bits;
      for (int i = 1; i <= w; i++) My(b(i).name, Input.b(i), Load);             // Create the memory bits
      e = null; E = null; load = Load;
      loaded = Continue(n(Output, "loaded"), load);                             // Show register has been loaded
     }

    Register(String Output, BitBus Input1, Bit Load1, BitBus Input2, Bit Load2) // Create register from two inputs
     {super(Output, Input1.bits);
      I    = new RegIn[2];
      I[0] = new RegIn(Input1, Load1);
      I[1] = new RegIn(Input2, Load2);
      Input1.sameSize(Input2);
      E    = new BitBus[2];
      E[0] = enableWord(n(Output, "e1"), Input1, Load1);                        // Enable input 1
      E[1] = enableWord(n(Output, "e2"), Input2, Load2);                        // Enable input 2
      e    = orBitBuses(n(Output, "e"),  E);                                    // Combined input
      load = Or    (n(Output, "l"), Load1, Load2);                              // Combined load
      loaded = Continue(n(Output, "loaded"), load);                             // Show register has been loaded
      final int N = Input1.bits, Q = logTwo(N);
      for (int i = 1; i <= N; i++)                                              // Create the memory bits
        My(b(i).name, load, delay(n(i, Output, "d"), e.b(i), Q));               // The delay loads the register when the fan out arrives
     }

    Register(String Output, RegIn...in)                                         // Create register from multiple inputs
     {super(Output, in[0].input.bits);
      if (in.length == 0) stop("Need at least one register input specification");
      final int N = in.length, P = in[0].input.bits, Q = logTwo(P);
      I = in;
      for (int i = 1; i < N; i++) in[0].input.sameSize(in[i].input);            // Check sizes
      E = new BitBus[N];                                                        // Enabled inputs
      final Bit[]L = new Bit[N];                                                // Or load bits
      for (int i = 0; i < N; i++)                                               // Enable each bus
       {final String n = n(Output, "e"+i);                                      // Generate a name for the enabled version of the input bus
        final RegIn R = I[i];                                                   // Register input specification
        E[i] = enableWord(n, R.input, R.load);                                  // Enable input
        L[i] = R.load;                                                          // Load with no delay - possibly useful on pulses
       }
      e    = orBitBuses(n(Output, "e"), E);                                     // Combined input
      load = Or        (n(Output, "l"), L);                                     // Combined load
      loaded = Continue(n(Output, "loaded"), load);                             // Show register has been loaded
      for (int i = 1; i <= P; i++)                                              // Create the memory bits
        My(b(i).name, load, delay(n(i, Output, "d"), e.b(i), Q));               // The delay loads the register when the fan out arrives
     }
   }

  Register register(String name, BitBus input, Bit load)                        // Create a register loaded from one source
   {return new Register(name, input, load);
   }

  Register register(String name, BitBus input1, Bit load1, BitBus input2, Bit load2)  // Create a register loaded from two sources
   {return new Register(name, input1, load1, input2, load2);
   }

  Register register(String name, RegIn...in)                                    // Create a register loaded from multiple sources
   {return new Register(name, in);
   }

  Gate delay(String Output, Bit input, int delay)                               // Create a delay chain so that one leading edge can trigger another later on as work is performed
   {Bit  p = input;
    Gate g = getGate(input);                                                    // Start at the input gate
    for (int i = 1; i <= delay; i++)                                            // All along the delay line
     {final Bit q = new Bit(i < delay ? nextGateName(Output) : Output);         // Make a chain along to the end gate
      g = Continue(q.name, p);
      p = q;
     }
    return g;
   }

//D2 Periodic Pulses                                                            // Periodic pulses that drive input buses.

  class Pulse extends Gate                                                      // A periodic pulse that drives an input bit
   {final int  period;                                                          // Length of pulses in simulations steps
    final int      on;                                                          // How long the pulse is in the on state in each period in simulations steps
    final int   delay;                                                          // Offset of the on phase of the pulse in simulations steps
    final int   start;                                                          // Number of cycles to wait before starting this pulse
    Pulse(String Name, int Period, int On, int Delay, int Start)                // Pulse definition
     {super(Operator.Input, Name, null, null);                                  // Pulse name
      period = Period; on = On; delay = Delay; start = Start;                   // Pulse details
      systemGate = true;                                                        // The input gate associated with this pulse. The gate will be driven by the simulator.
      if (on    > period) stop("On", on, "is greater than period", period);
      if (delay > period) stop("Delay", delay, "is greater than period", period);
      if (on + delay > period) stop("On + Delay", on, "+", delay, "is greater than period", period);
      pulses.put(name, this);                                                   // Save pulse
     }
    void setState()                                                             // Set gate to current state
     {final int i = (steps-1) % period;
      final boolean v = i >= delay && i < on+delay;                             // Pulse value
      if (steps-1 >= start*period) value = v; else value = false;
     }
   }

  void loadPulses()                                                             // Load all the pulses for this chip
   {for (Pulse p : pulses.values()) p.setState();
   }

  Pulse pulse(String Name, int Period, int On, int Delay, int Start)            // Create a pulse
   {return new Pulse(Name, Period, On, Delay, Start);
   }

  Pulse pulse(String Name, int Period, int On, int Delay)                       // Create a pulse
   {return new Pulse(Name, Period, On, Delay, 0);
   }

  Pulse pulse(String Name, int Period, int On)                                  // Create a pulse with no delay
   {return new Pulse(Name, Period, On, 0, 0);
   }

  Pulse pulse(String Name, int Period)                                          // Create a single step pulse with no delay
   {return new Pulse(Name, Period, 1, 0, 0);
   }

//D3 Select                                                                     // Send a pulse one way or another depending on a bit allowing us to execute one branch of an if statement or the other and receive a pulse notifying us when the execution of the different length paths are complete.

  class Select                                                                  // Select a direction for a pulse from two possibilities depending on the setting of a bit. The pulse is transmitted along the selecetd path for as long as the control bit is true, thereafter both paths revert to false.
   {final String    name;                                                       // Name of decision. The output along the first path selected when the control bot is true has "_then" appended to it, the other "_else";
    final Gate      Then;                                                       // The output along the then path
    final Gate      Else;                                                       // The output along the else path
    final Bit    control;                                                       // The control bit
    final Bit      pulse;                                                       // The pulse that determines when the selection is made.
    Select(String Name, Bit Control, Bit Pulse)                                 // Define the selection
     {name = Name; control = Control; pulse = Pulse;                            // Selection details
      Gate c = Continue(nextGateName(name), control);                           // Match the length of the else path
      Then   = And     (nextGateName(name), c, pulse);                          // Then path
      Gate n = Not     (nextGateName(name), control);                           // Not of control
      Else   = And     (nextGateName(name), n, pulse);                          // Else path
     }
   }

  Select select(String Name, Bit Control, Bit Pulse)                            // Create a select element
   {return new Select(Name, Control, Pulse);                                    // Define the selection
   }

//D2 Arithmetic Base 1                                                          // Arithmetic in base 1

  BitBus shiftUp(String output, BitBus input)                                   // Shift an input bus up one place to add 1 in base 1 or multiply by two in base 2
   {final int    b = input.bits;                                                // Number of bits in input monotone mask
    final BitBus o = new BitBus(output, b+1);                                   // Shifted result is a bus one bit wider
    final Gate   z = Zero(o.b(1).name);                                         // The lowest but will be zero after the shift
    for (int i = 1; i <= b; i++) Continue(o.b(1+i).name, input.b(i));           // Create the remaining bits of the shifted result
    return o;
   }

  BitBus shiftDown(String output, BitBus input)                                 // Shift an input bus down one place to subtract 1 in base 1 or divide by two in base 2
   {final int    b = input.bits;                                                // Number of bits in input monotone mask
    final BitBus o = new BitBus(output, b-1);                                   // Shifted result is a bus one bit narrower
    anneal(input.b(1));                                                         // Remove the lowest bit in such away that it will not be report as failing to drive anything
    for (int i = 2; i <= b; i++) Continue(o.b(i-1).name, input.b(i));           // Create the remaining bits of the shifted result
    return o;
   }

//D2 Arithmetic Base 2                                                          // Arithmetic in base 2

  class BinaryAdd                                                               // Results of a binary add
   {final Gate carry;                                                           // Carry out gate
    final BitBus sum;                                                           // Sum
    BinaryAdd(Gate Carry, BitBus Sum) {carry = Carry; sum = Sum;}
   }

  BinaryAdd binaryAdd(String CarryOut, String output, BitBus in1, BitBus in2)   // Add two bit buses of the same size to make a bit bus one bit wider
   {final int b = in1.bits;                                                     // Number of bits in input monotone mask
    final int B = in2.bits;                                                     // Number of bits in input monotone mask
    if (b != B) stop("Input bit buses must have the same size, not", b, B);     // Check sizes match
    final BitBus c = new BitBus(nextGateName(output, "carry"),   b);            // Carry bits
    final BitBus C = notBits(nextGateName(output,  "not_carry"), c);            // Not of carry bits
    final BitBus n = notBits(nextGateName(output,  "not_in1"), in1);            // Not of input 1
    final BitBus N = notBits(nextGateName(output,  "not_in2"), in2);            // Not of input 2
    Xor(n(1, output), in1.b(1), in2.b(1));                                      // Low order bit has no carry in
    And(c.b(1).name,  in1.b(1), in2.b(1));                                      // Low order bit carry out
    anneal(n.b(1), N.b(1), C.b(b));                                             // These bits are not needed, but get defined, so we anneal them off to prevent error messages
// #  c 1 2  R C
// 1  0 0 0  0 0
// 2  0 0 1  1 0                                                                // We only need 1 bits so do not define a full bus as we would have to anneal a lot of unused gates which would be wasteful.
// 3  0 1 0  1 0
// 4  0 1 1  0 1
// 5  1 0 0  1 0
// 6  1 0 1  0 1
// 7  1 1 0  0 1
// 8  1 1 1  1 1
    final String r = output;                                                    // Result bit bus name
    final BitBus i = in1;                                                       // Input 1
    final BitBus I = in2;                                                       // Input 2
    for (int j = 2; j <= b; j++)                                                // Create the remaining bits of the shifted result
     {final Stack<Bit>rs = new Stack<>();                                       // Result
      Gate r2 = And(nextGateName(output), C.b(j-1), n.b(j), I.b(j)); rs.push(r2);
      Gate r3 = And(nextGateName(output), C.b(j-1), i.b(j), N.b(j)); rs.push(r3);
      Gate r5 = And(nextGateName(output), c.b(j-1), n.b(j), N.b(j)); rs.push(r5);
      Gate r8 = And(nextGateName(output), c.b(j-1), i.b(j), I.b(j)); rs.push(r8);
      Or(n(j, r), rs);

      final Stack<Bit>cs = new Stack<>();                                       // Carry
      Gate c4 = And(nextGateName(output), C.b(j-1), i.b(j), I.b(j)); cs.push(c4);
      Gate c6 = And(nextGateName(output), c.b(j-1), n.b(j), I.b(j)); cs.push(c6);
      Gate c7 = And(nextGateName(output), c.b(j-1), i.b(j), N.b(j)); cs.push(c7);
      Gate c8 = And(nextGateName(output), c.b(j-1), i.b(j), I.b(j)); cs.push(c8);
      Or(c.b(j).name, cs);
     }

    return new BinaryAdd(Continue(CarryOut, c.b(b)), collectBits(output, b));   // Carry, result
   }

//D2 B-tree                                                                     // Circuits useful in the construction and traversal of B-trees.

  class BtreeNode                                                               // Description of a node in a binary tree
   {final String  Output;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final int         Id;                                                       // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
    final int          B;                                                       // Width of each word in the node.
    final int          K;                                                       // Number of keys == number of data words each B bits wide in the node.
    final boolean   Leaf;                                                       // Width of each word in the node.
    final BitBus  Enable;                                                       // B bit wide bus naming the currently enabled node by its id.
    final BitBus    Find;                                                       // B bit wide bus naming the key to be found
    final WordBus    Keys;                                                      // Keys in this node, an array of N B bit wide words.
    final WordBus    Data;                                                      // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
    final WordBus    Next;                                                      // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
    final BitBus     Top;                                                       // The top next link making N+1 next links in all.
    final Bit      found;                                                       // Found the search key
    final BitBus outData;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final BitBus outNext;                                                       // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    int     level, index;                                                       // Level and position in  level for this node
    final BitBus  nodeId;                                                       // Save id of node
    final Bit     enable;                                                       // Check whether this node is enabled
    final BitBus  matches;                                                      // Bitbus showing whether each key is equal to the search key
    final BitBus  selectedData;                                                 // Choose data under equals mask
    final Bit     keyWasFound;                                                  // Show whether key was found

    BtreeNode                                                                   // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
     (String  Output,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      int         Id,                                                           // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
      int          B,                                                           // Width of each word in the node.
      int          K,                                                           // Number of keys == number of data words each B bits wide in the node.
      boolean   Leaf,                                                           // Width of each word in the node.
      BitBus  Enable,                                                           // B bit wide bus naming the currently enabled node by its id.
      BitBus    Find,                                                           // B bit wide bus naming the key to be found
      WordBus   Keys,                                                           // Keys in this node, an array of N B bit wide words.
      WordBus   Data,                                                           // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
      WordBus   Next,                                                           // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
      BitBus     Top,                                                           // The top next link making N+1 next links in all.
      String   Found,                                                           // Found the search key
      String OutData,                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      String OutNext)                                                           // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
     {if (K     <= 2) stop("The number of keys the node can hold must be greater than 2, not", K);
      if (K % 2 == 0) stop("The number of keys the node can hold must be odd, not even:",      K);
      this.Output = Output;
      this.Id     = Id;     this.B       = B;       this.K       = K; this.Leaf = Leaf;
      this.Enable = Enable; this.Find    = Find;    this.Keys    = Keys;
      this.Data   = Data;   this.Next    = Next;    this.Top     = Top;

      final String id = n(Output, "id");                                        // Id for this node
      final String df = n(Output, "dataFound");                                 // Data found before application of enable
      final String en = n(Output, "enabled");                                   // Whether this node is enabled for searching
      final String f2 = n(Output, "foundBeforeEnable");                         // Whether the key was found or not but before application of enable
      final String me = n(Output, "maskEqual");                                 // Point mask showing key equal to search key
      final String mm = n(Output, "maskMore");                                  // Monotone mask showing keys more than the search key
      final String mf = n(Output, "moreFound");                                 // The next link for the first key greater than the search key if such a key is present int the node
      final String nf = n(Output, "notFound");                                  // True if we did not find the key
      final String n2 = n(Output, "nextLink2");
      final String n3 = n(Output, "nextLink3");
      final String n4 = n(Output, "nextLink4");
      final String nm = n(Output, "noMore");                                    // No key in the node is greater than the search key
      final String pm = n(Output, "pointMore");                                 // Point mask showing the first key in the node greater than the search key
      final String pt = n(Output, "pointMoreTop");                              // A single bit that tells us whether the top link is the next link
      final String pn = n(Output, "pointMoreTop_notFound");                     // Top is the next link, but only if the key was not found

      nodeId = bits     (id, B, Id);                                            // Save id of node
      enable = compareEq(en, nodeId, Enable);                                   // Check whether this node is enabled

      for (int i = 1; i <= K; i++)
       {compareEq(n(i, me), Keys.w(i), Find);                                   // Compare equal point mask
        if (!Leaf) compareGt(n(i, mm), Keys.w(i), Find);                        // Compare more  monotone mask
       }

      matches = collectBits(me, K);                                             // Equal bit bus for each key

      selectedData = chooseWordUnderMask(df, Data, matches);                    // Choose data under equals mask
      keyWasFound  = orBits             (f2,       matches);                    // Show whether key was found
      outData      = enableWord    (OutData, selectedData, enable);             // Enable data found
      found        = And             (Found, keyWasFound,  enable);             // Enable found flag

      if (!Leaf)                                                                // Find next link with which to enable next layer
       {final BitBus Mm = collectBits            (mm, K);                       // Interior nodes have next links
        final Bit    Nm = norBits                (nm, Mm);                      // True if the more monotone mask is all zero indicating that all of the keys in the node are less than or equal to the search key
        final BitBus Pm = monotoneMaskToPointMask(pm, Mm);                      // Convert monotone more mask to point mask
        final BitBus Mf = chooseWordUnderMask    (mf, Next, Pm);                // Choose next link using point mask from the more monotone mask created
        final BitBus N4 = chooseFromTwoWords     (n4, Mf,   Top, Nm);           // Choose top or next link
        final Bit    Pt = norBits                (pt, Pm);                      // The top link is the next link
        final Bit    Nf = Not                    (nf, found);                   // Not found
        final BitBus N3 = enableWord             (n3, N4,   Nf);                // Disable next link if we found the key
        final Bit    Pn = And                    (pn, Pt,   Nf);                // Top is the next link, but only if the key was not found
        final BitBus N2 = chooseFromTwoWords     (n2, N3,   Top, Pn);           // Either the next link or the top link
        outNext = enableWord                     (OutNext,  N2,  enable);       // Next link only if this node is enabled
       }
      else outNext = null;                                                      // Not relevant in a leaf
     }

    static BtreeNode Test                                                       // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
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

      final BitBus  e = c.bits (B, enable);
      final BitBus  f = c.bits (B, find);
      final WordBus k = c.words(B, keys);
      final WordBus d = c.words(B, data);
      final WordBus n = next != null ? c.words(B, next) : null;
      final BitBus  t = next != null ? c.bits (B, top)  : null;

      return c.new BtreeNode(Output, Id, B, N, next == null, e, f, k, d, n, t, Found, Data, Next);
     }
   }

  class Btree                                                                   // Construct and search a Btree.
   {final int      bits;                                                        // Number of bits in a key, datum, or next link
    final int      keys;                                                        // Number of keys in a node
    final int    levels;                                                        // Number of levels in the tree
    final String output;                                                        // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
    final BitBus   find;                                                        //i The search key bus
    final Gate    found;                                                        //o A bit indicating whether the key was found in the tree or not
    final BitBus   data;                                                        //o The data corresponding to the search key if the found bit is set, else all zeros
    final TreeMap<Integer, Level>      tree = new TreeMap<>();                  // Levels within the tree
    final TreeMap<Integer, BtreeNode> nodes = new TreeMap<>();                  // Nodes within tree by id number

    class Level
     {final int     level;                                                      // Number of level
      final int         N;                                                      // Number of nodes at this level
      final boolean  root;                                                      // Root node
      final boolean  leaf;                                                      // Final level is made of leaf nodes
      final BitBus enable;                                                      // Node selector for this level
      final TreeMap<Integer, BtreeNode> nodes;                                  // Nodes in this level
      Level(int l, int n, boolean Root, boolean Leaf, BitBus Enable)            // Create a level description
       {nodes = new TreeMap<>();
        level = l; N = n; root = Root; leaf = Leaf; enable = Enable;
        tree.put(l, this);                                                      // Record details of the level to enable debugging
       }
     }

    Btree                                                                       // Construct a Btree.
     (String output,                                                            // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
      BitBus   find,                                                            //i The search key bus
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
      final String id = n(output, "inputData");                                 // Input data
      final String ik = n(output, "inputKeys");                                 // Input keys
      final String in = n(output, "inputNext");                                 // Input next links
      final String it = n(output, "inputTop");                                  // Input top link
      final String ld = n(output, "levelData");                                 // The data out from a level
      final String lf = n(output, "levelFound");                                // Find status for a level
      final String ln = n(output, "levelNext");                                 // Next link to search
      final String nd = n(output, "nodeData");                                  // The data out from a node
      final String nf = n(output, "nodeFound");                                 // The found flag output by each node
      final String nn = n(output, "nodeNext");                                  // The next link output by this node
      int nodeId = 0;                                                           // Gives each node in the tree a different id

      if (find.bits != bits) stop("Find bus must be", bits, "wide, not", find.bits);

      BitBus eI = null;                                                         // For the moment we always enable the root node of the tree

      for (int l = 1; l <= levels; l++)                                         // Each level in the bTree
       {final int         N = powerOf(keys+1, l-1);                             // Number of nodes at this level
        final boolean  root = l == 1;                                           // Root node
        final boolean  leaf = l == levels;                                      // Final level is made of leaf nodes
        final Level   level = new Level(l, N, root, leaf, eI);                  // Details of the level

        for (int n = 1; n <= N; n++)                                            // Each node at this level
         {++nodeId;                                                             // Number of this node
          if (eI == null) eI = bits(n(0, ln), bits, nodeId);                    // For the moment we always enable the root node of the tree
          final WordBus iK = inputWords(n(l, n, ik), keys, bits);               // Bus of input words representing the keys in this node
          final WordBus iD = inputWords(n(l, n, id), keys, bits);               // Bus of input words representing the data in this node
          final WordBus iN = leaf ? null : inputWords(n(l, n, in), keys, bits); // Bus of input words representing the next links in this node
          final BitBus  iT = leaf ? null : inputBits (n(l, n, it), bits);       // Bus representing the top next link
          final String  oF = root ? n(l, lf) : n(l, n, nf);                     // On the root we do not need to combine the found flags for each node - on other levels we do
          final String  oD = root ? n(l, ld) : n(l, n, nd);                     // Output data element if found
          final String  oN = root ? n(l, ln) : n(l, n, nn);                     // Next link if node is not a leaf

          final BtreeNode node = new BtreeNode(n(l, n, output, "node"),         // Create the node
            nodeId, bits, keys, leaf, eI, find, iK, iD, iN, iT, oF, oD, oN);

          level.nodes.put(n, node);                                             // Add the node to this level
          nodes.put(nodeId, node);                                              // Index the node
          node.level = l; node.index = n;                                       // Position of node in tree
         }

        if (!root)                                                              // Or the data elements together for this level. Not necessary on the root because there is only one node.
         {orBits (n(l, lf), collectBits (n(l, nf), N));                         // Collect all the find output fields in this level and Or them together to see if any node found the key. At most one node will find the key if the data has been correctly structured.
          orWords(n(l, ld), collectWords(n(l, nd), N, bits));                   // Collect all the data output fields from this level and Or them together as they will all be zero except for possible to see if any node found the key. At most one node will find the key if the data has been correctly structured.
         }

        eI = leaf ? eI : root ? collectBits(n(l, ln), bits) :                   // Collect all next links nodes on this level
            orWords(n(l, ln),  collectWords(n(l, nn), N, bits));
       }
      this.data  = orWords(data,  collectWords(ld, levels, bits));              // Data found over all layers
      this.found = orBits (found, collectBits (lf, levels));                    // Or of found status over all layers
     }

    Chip chip() {return Chip.this;}                                             // Containing chip
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
        if (p.pin != null)
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
       {final Connection c = new Connection(s, p.gate());                       // Connection needed
        connections.push(c);
       }

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

  static Bit[]stackToBitArray(Stack<Bit> s)                                     // Stack of string to array of string
   {final Bit[]a = new Bit[s.size()];
    for (int i = 0; i < s.size(); i++) a[i] = s.elementAt(i);
    return a;
   }

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

  static String traceBack(Exception e)                                          // Get a stack trace that we can use in Geany
   {final StackTraceElement[]  t = e.getStackTrace();
    final StringBuilder        b = new StringBuilder();
    if (e.getMessage() != null)b.append(e.getMessage()+'\n');

    for(StackTraceElement s : t)
     {final String f = s.getFileName();
      final String c = s.getClassName();
      final String m = s.getMethodName();
      final String l = String.format("%04d", s.getLineNumber());
      if (f.equals("Main.java"))                       continue;
      if (f.equals("Method.java"))                     continue;
      if (f.equals("DirectMethodHandleAccessor.java")) continue;
      b.append("  "+f+":"+l+":"+m+'\n');
     }
    return b.toString();
   }

  static String traceBack() {return traceBack(new Exception());}                // Get a stack trace that we can use in Geany

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
   {if (a.equals(b)) {++testsPassed; return;}
    final boolean n = b.toString().contains("\n");
    testsFailed++;
    if (n) err("Test failed. Got:\n"+b+"\n");
    else   err(a, "does not equal", b);
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
    final Gate and = c.And   ("and", i1, i2);
    final Gate   o = c.Output("o", and);

    final Inputs inputs = c.new Inputs();
    inputs.set(i1, true);
    inputs.set(i2, false);
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
    final Gate  I1 = c.Input ("i1");
    final Gate  I2 = c.Input ("i2");
    final Gate and = c.And   ("and", I1, I2);
    c.Output("o", and);
    final Inputs inputs = c.new Inputs();
    inputs.set(I1, i1);
    inputs.set(I2, i2);
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
    final Gate I1 = c.Input ("i1");
    final Gate I2 = c.Input ("i2");
    final Gate or = c.Or    ("or", I1, I2);
    c.Output("o", or);
    final Inputs inputs = c.new Inputs();
    inputs.set(I1, i1);
    inputs.set(I2, i2);
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
   {final Chip   c = new Chip("Delayed Definitions");
    final Gate   o = c.Output("o", c.new Bit("and"));
    final Gate and = c.And   ("and", c.new Bit("i1"), c.new Bit("i2"));
    final Gate  i1 = c.Input ("i1");
    final Gate  i2 = c.Input ("i2");
    final Inputs inputs = c.new Inputs();
    inputs.set(i1, true);
    inputs.set(i2, false);
    c.simulate(inputs);
    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, false);
    ok(  o.value, false);
    ok(  c.steps ,     3);
   }

  static void test_or()
   {final Chip c = new Chip("Or");
    final Gate i1 = c.Input ("i1");
    final Gate i2 = c.Input ("i2");
    final Gate or = c.Or    ("or", i1, i2);
    final Gate  o = c.Output("o", or);
    final Inputs inputs = c.new Inputs();
    inputs.set(i1, true);
    inputs.set(i2, false);
    c.simulate(inputs);
    ok(i1.value, true);
    ok(i2.value, false);
    ok(or.value, true);
    ok( o.value, true);
    ok( c.steps,    3);
   }

  static void test_notGates()
   {final Chip c = new Chip("NotGates");
    final BitBus b = c.bits("b", 5, 21);
    final Gate   a = c. andBits(  "a",  b);
    final Gate   o = c.  orBits(  "o",  b);
    final Gate  na = c.nandBits( "na",  b);
    final Gate  no = c.norBits ( "no",  b);
    final Gate  oa = c.Output  ( "oa",  a);
    final Gate  oo = c.Output  ( "oo",  o);
    final Gate ona = c.Output  ("ona", na);
    final Gate ono = c.Output  ("ono", no);
    c.simulate();
    ok( a.value, false);
    ok(na.value, true);
    ok( o.value, true);
    ok(no.value, false);
   }

  static void test_zero()
   {final Chip c = new Chip("Zero");
    final Gate z = c.Zero("z");
    final Gate o = c.Output("o", z);
    c.simulate();
    ok(c.steps,  3);
    ok(o.value, false);
   }

  static void test_one()
   {final Chip c = new Chip("One");
    final Gate O = c.One ("O");
    final Gate o = c.Output("o", O);
    c.simulate();
    ok(c.steps  , 3);
    ok(o.value , true);
   }

  static Chip test_and3(boolean A, boolean B, boolean C, boolean D)
   {final Chip   c = new Chip("And3");
    final Gate i11 = c.Input ("i11");
    final Gate i12 = c.Input ("i12");
    final Gate and = c.And   ("and", i11, i12);
    final Gate i21 = c.Input ("i21");
    final Gate i22 = c.Input ("i22");
    final Gate And = c.And   ("And", i21, i22);
    final Gate  Or = c. Or   ("or",  and, And);
    final Gate   o = c.Output("o", Or);

    final Inputs i = c.new Inputs();
    i.set(i11, A);
    i.set(i12, B);
    i.set(i21, C);
    i.set(i22, D);

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
   {final Chip    c = new Chip("Expand");
    final Gate  one = c.One   ("one");
    final Gate zero = c.Zero  ("zero");
    final Gate   or = c.Or    ("or",   one, zero);
    final Gate  and = c.And   ("and",  one, zero);
    final Gate   o1 = c.Output("o1", or);
    final Gate   o2 = c.Output("o2", and);
    c.simulate();
    ok(c.steps,  4);
    ok(o1.value, true);
    ok(o2.value, false);
   }

  static void test_expand2()
   {final Chip    c = new Chip("Expand2");
    final Gate  one = c.One   ("one");
    final Gate zero = c.Zero  ("zero");
    final Gate   or = c.Or    ("or",  one, zero);
    final Gate  and = c.And   ("and", one, zero);
    final Gate  xor = c.Xor   ("xor", one, zero);
    final Gate   o1 = c.Output("o1",  or);
    final Gate   o2 = c.Output("o2",  and);
    final Gate   o3 = c.Output("o3",  xor);
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
      final BitBus C = c.bits("c", N, i);
      final BitBus o = c.outputBits("o", C);
      c.simulate();
      ok(c.steps, 3);
      ok(o.Int(), i);
     }
   }

  static void test_and2Bits()
   {for (int N = 3; N <= 4; ++N)
     {final int N2 = powerTwo(N);
      for  (int i = 0; i < N2; i++)
       {final Chip    c = new Chip ("And2Bits");
        final BitBus i1 = c.bits   ("i1", N, 5);
        final BitBus i2 = c.bits   ("i2", N, i);
        final BitBus  o = c.andBits("o", i1, i2);
        c.outputBits("out", o);
        c.simulate();
        ok(c.steps, 4);
        ok(o.Int(), 5 & i);
       }
     }
   }

  static void test_gt()
   {final Chip    c = new Chip("Gt");
    final Gate  one = c.One   ("o");
    final Gate zero = c.Zero  ("z");
    final Gate   gt = c.Gt    ("gt", one, zero);
    final Gate    o = c.Output("O", gt);
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, true);
   }

  static void test_gt2()
   {final Chip    c = new Chip("Gt2");
    final Gate  one = c.One   ("o");
    final Gate zero = c.Zero  ("z");
    final Gate   gt = c.Gt    ("gt", zero, one);
    final Gate    o = c.Output("O",  gt);
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, false);
   }

  static void test_lt()
   {final Chip    c = new Chip("Lt");
    final Gate  one = c.One   ("o");
    final Gate zero = c.Zero  ("z");
    final Gate   lt = c.Lt    ("lt", one, zero);
    final Gate    o = c.Output("O", lt);
    c.simulate();
    ok(c.steps, 4);
    ok(o.value, false);
   }

  static void test_lt2()
   {final Chip    c = new Chip("Lt2");
    final Gate  one = c.One   ("o");
    final Gate zero = c.Zero  ("z");
    final Gate   lt = c.Lt    ("lt", zero, one);
    final Gate    o = c.Output("O", lt);
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
    final Chip c = new Chip("Aix "+B);
    final WordBus i = c.    words("i", B, bits);
    final BitBus  a = c. andWords("a", i), oa = c.outputBits("oa", a);
    final BitBus  o = c.  orWords("o", i), oo = c.outputBits("oo", o);
    final BitBus  A = c.andWordsX("A", i), oA = c.outputBits("oA", A);
    final BitBus  O = c. orWordsX("O", i), oO = c.outputBits("oO", O);
    c.simulate();
    ok(oa.Int(),  4);
    ok(oo.Int(),  7);
    ok(oA.Int(), 20);
    ok(oO.Int(), 31);
   }

  static void test_compareEq()
   {for (int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final var    c = new Chip("CompareEq "+B);
          final BitBus I = c.bits("i", B, i);
          final BitBus J = c.bits("j", B, j);
          final Gate   o = c.compareEq("o", I, J);
          final Gate   O = c.Output("O", o);
          c.simulate();
          ok(c.steps >= 5 && c.steps <= 6, true);
         }
       }
     }
   }

  static void test_compareGt()
   {for     (int B = 2; B <= 4; ++B)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {final Chip c = new Chip("CompareGt "+B);
          final BitBus I = c.bits("i", B, i);
          final BitBus J = c.bits("j", B, j);
          final Gate   o = c.compareGt("o", I, J);
          final Gate   O = c.Output   ("O", o);
          c.simulate();
          ok(c.steps >= 5 && c.steps <= 9, true);
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
         {final Chip c = new Chip("CompareLt "+B);
          final BitBus I = c.bits("i", B, i);
          final BitBus J = c.bits("j", B, j);
          final Gate   o = c.compareLt("o", I, J);
          final Gate   O = c.Output("O", o);
          c.simulate();
          ok(c.steps >= 5 && c.steps <= 9, true);
          ok(o.value, i < j);
         }
       }
     }
   }

  static void test_chooseFromTwoWords()
   {for (int i = 0; i < 2; i++)
     {final int b = 4;
      final Chip c = new Chip("ChooseFromTwoWords "+b+" "+i);
      final BitBus   A = c.bits("a", b,  3);
      final BitBus   B = c.bits("b", b, 12);
      final Gate     C = c.bit ("c", i);
      final BitBus   o = c.chooseFromTwoWords("o", A, B, C);
      final BitBus out = c.outputBits("out",  o);
      c.simulate();
      ok(c.steps, 9);
      ok(out.Int(), i == 0 ? 3 : 12);
     }
   }

  static void test_enableWord()
   {for (int i = 0; i < 2; i++)
     {final int B = 4;
      final Chip   c = new Chip("EnableWord "+i);
      final BitBus a = c.bits("a", B,  3);
      final Bit    e = c.bit ("e", i);
      final BitBus o = c.enableWord("o", a, e);
      final BitBus O = c.outputBits("out",  o);
      c.simulate();
      ok(c.steps, 5);
      ok(O.Int(), i == 0 ? 0 : 3);
     }
   }

  static void test_monotoneMaskToPointMask()
   {for    (int B = 2; B <= 4; ++B)
     {final int N = powerTwo(B);
      for  (int i = 1; i <= N; i++)
       {final Chip   c = new Chip("monotoneMaskToPointMask "+B);
        final BitBus I = c.new BitBus("i", N);
        for (int j = 1; j <= N; j++) c.bit(I.b(j).name, j < i ? 0 : 1);
        final BitBus o = c.monotoneMaskToPointMask("o", I);
        final BitBus O = c.outputBits("out", o);
        c.simulate();
        ok(O.Int(), powerTwo(i-1));
       }
     }
   }

  static void test_chooseWordUnderMask(int B, int i)
   {final int[]numbers =  {2, 3, 2, 1,  0, 1, 2, 3,  2, 3, 2, 1,  0, 1, 2, 3};
    final int B2 = powerTwo(B);
    final Chip c = new Chip("chooseWordUnderMask "+B);
    final WordBus I = c.words("i", B, Arrays.copyOfRange(numbers, 0, B2));
    final BitBus  m = c.bits ("m", B2, powerTwo(i));
    final BitBus  o = c.chooseWordUnderMask("o", I, m);
    final BitBus  O = c.outputBits("out", o);
    c.simulate();
    ok(O.Int(), numbers[i]);
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
    final BitBus d = c.outputBits("d", c.findBits("data")); // Anneal the node
    final BitBus n = c.outputBits("n", c.findBits("next"));
    final Gate   f = c.Output    ("f", c.new Bit("found"));
    c.simulate();
    ok(c.steps >= 18 && c.steps <= 22, true);
    ok(c.getBit("found"), Found);
    ok(c.findBits("data").Int(), Data);
    ok(c.findBits("next").Int(), Next);
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
    c.outputBits("d", c.findBits("data")); // Anneal the node
    c.Output    ("f", c.new Bit("found"));
    c.simulate();
    ok(c.steps == 10 || c.steps == 12, true);
    ok(c.getBit("found"),        Found);
    ok(c.findBits("data").Int(), Data);
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

  static void test_Btree(Btree b, Inputs i, int find)
   {final Chip c = b.chip();
    i.set(c.findBits("find"), find);
    c.simulate(i);
   }

  static void test_Btree(Btree b, Inputs i, int find, int data)
   {test_Btree(b, i, find);
    Chip c = b.chip();
    ok(c.getBit("found"),        true);
    ok(c.findBits("data").Int(), data);
   }

  static void test_Btree(Btree b, Inputs i, int find, int found, boolean layout)
   {test_Btree(b, i, find, found);

    if (layout) b.chip().drawSingleLevelLayout(6, 1);
   }

  static void test_Btree()
   {final int B = 8, K = 3, L = 2;

    final var    c = new Chip("Btree");
    final BitBus f = c.inputBits("find", B);
    final Btree  b = c.new Btree("tree", f, "found", "data", K, L, B);
    c.outputBits("d", c.findBits("data")); // Anneal the tree
    c.Output    ("f", c.new Bit("found"));

    final Inputs i = c.new Inputs();
    final BtreeNode r = b.tree.get(1).nodes.get(1);
    i.set(r.Keys,   10,  20, 30);
    i.set(r.Data,   11,  22, 33);
    i.set(r.Next,  2,  3,  4);
    i.set(r.Top,               5);
    final BtreeNode l1 = b.tree.get(2).nodes.get(1);
    i.set(l1.Keys,   2,  4,  6);
    i.set(l1.Data,  22, 44, 66);
    final BtreeNode l2 = b.tree.get(2).nodes.get(2);
    i.set(l2.Keys,  13, 15, 17);
    i.set(l2.Data,  31, 51, 71);
    final BtreeNode l3 = b.tree.get(2).nodes.get(3);
    i.set(l3.Keys,  22, 24, 26);
    i.set(l3.Data,  22, 42, 62);
    final BtreeNode l4 = b.tree.get(2).nodes.get(4);
    i.set(l4.Keys,  33, 35, 37);
    i.set(l4.Data,  33, 53, 73);

    test_Btree(b, i, 10, 11);
    test_Btree(b, i, 20, 22);
    test_Btree(b, i, 30, 33);

    test_Btree(b, i,  2, 22, github_actions);
    test_Btree(b, i,  4, 44);
    test_Btree(b, i,  6, 66);

    test_Btree(b, i, 13, 31);
    test_Btree(b, i, 15, 51);
    test_Btree(b, i, 17, 71);

    test_Btree(b, i, 22, 22);
    test_Btree(b, i, 24, 42);
    test_Btree(b, i, 26, 62);

    test_Btree(b, i, 33, 33);
    test_Btree(b, i, 35, 53);
    test_Btree(b, i, 37, 73);

    final int[]skip = {10, 20, 30,  2,4,6,  13,15,17, 22,24,26, 33,35,37};
    for (final int F : IntStream.rangeClosed(0, 100).toArray())
     {if (Arrays.stream(skip).noneMatch(x -> x == F)) test_Btree(b, i, F);
     }
   }

  static void test_simulationStep()
   {final Chip  c = new Chip("Simulation Step");
    final Gate  i = c.   One("i");
    final Gate n9 = c.   Not("n9", i);
    final Gate n8 = c.   Not("n8", n9);
    final Gate n7 = c.   Not("n7", n8);
    final Gate n6 = c.   Not("n6", n7);
    final Gate n5 = c.   Not("n5", n6);
    final Gate n4 = c.   Not("n4", n5);
    final Gate n3 = c.   Not("n3", n4);
    final Gate n2 = c.   Not("n2", n3);
    final Gate n1 = c.   Not("n1", n2);
    final Gate o  = c.Output("o",  n1);

    final Inputs I = c.new Inputs();
    I.set(i, true);
    c.simulate();
    ok(o.value, false);
    ok(c.steps,    12);
   }

  static void test_clock()
   {final Chip c = new Chip("Clock", 8);
    c.minSimulationSteps(16);
    final Stack<Boolean> s = new Stack<>();
    c.simulationStep = ()->{s.push(c.getBit("a"));};
    final Gate and = c.And   ("a", c.clock0.b(1), c.clock0.b(2));
    final Gate out = c.Output("o", and);
    c.simulate();
    ok(c.steps,  19);
    for (int i = 0; i < s.size(); i++) ok(s.elementAt(i), (i+1) % 4 == 0);
   }

  static void test_pulse()
   {final Chip c = new Chip("Pulse");

    Pulse p1 = c.pulse("p1", 16);
    Pulse p2 = c.pulse("p2", 16, 1, 3);
    Pulse p3 = c.pulse("p3", 16, 2, 5);
    Pulse p4 = c.pulse("p4",  4);
    Gate  a  = c.   Or("a", p1, p2, p3);
    Gate  m  = c.   My("m", p4, a);                                             // Pin 1 controls when pin 2 is stored in the memory
    c.Output("o", m);

    c.executionTrace("1 2 3 a    4 m", "%s %s %s %s    %s %s", p1, p2, p3, a, p4,m);
    c.simulationSteps(32);
    c.simulate();
    //c.printExecutionTrace();
    c.ok(STR."""
Step  1 2 3 a    4 m
   1  1 0 0 .    1 .
   2  0 0 0 1    0 .
   3  0 0 0 0    0 0
   4  0 1 0 0    0 0
   5  0 0 0 1    1 0
   6  0 0 1 0    0 0
   7  0 0 1 1    0 0
   8  0 0 0 1    0 0
   9  0 0 0 0    1 1
  10  0 0 0 0    0 1
  13  0 0 0 0    1 1
  14  0 0 0 0    0 1
  17  1 0 0 0    1 1
  18  0 0 0 1    0 1
  19  0 0 0 0    0 0
  20  0 1 0 0    0 0
  21  0 0 0 1    1 0
  22  0 0 1 0    0 0
  23  0 0 1 1    0 0
  24  0 0 0 1    0 0
  25  0 0 0 0    1 1
  26  0 0 0 0    0 1
  29  0 0 0 0    1 1
  30  0 0 0 0    0 1
""");
    ok(c.steps,  32);
   }

  static void test_register()
   {Chip      c = new Chip("Register");
    BitBus   i1 = c.bits("i1", 8, 9);
    BitBus   i2 = c.bits("i2", 8, 6);
    Pulse    pc = c.pulse("choose", 32, 16, 16);
    Pulse    pl = c.pulse("load",    8,  1,  1);
    BitBus    I = c.chooseFromTwoWords("I", i1, i2, pc);
    Register  r = c.register("reg",  I, pl);
    BitBus    o = c.outputBits("o", r);
    Gate     or = c.Output("or",    r.loaded);

    c.executionTrace("c  choose    l  register ld", "%s  %s  %s  %s  %s", pc, I, pl, r, r.loaded);
    c.simulationSteps(32);
    c.simulate();
    //c.printExecutionTrace(); stop();
    c.ok(STR."""
Step  c  choose    l  register ld
   1  0  ........  0  ........  .
   2  0  ........  1  ........  .
   3  0  0000....  0  ........  0
   4  0  0000....  0  ........  1
   5  0  0000.00.  0  0000....  0
   8  0  00001001  0  0000....  0
  10  0  00001001  1  0000....  0
  11  0  00001001  0  0000....  0
  12  0  00001001  0  0000....  1
  13  0  00001001  0  00001001  0
  17  1  00001001  0  00001001  0
  18  1  00001001  1  00001001  0
  19  1  00001001  0  00001001  0
  20  1  00001001  0  00001001  1
  21  1  00001111  0  00001111  0
  24  1  00000110  0  00001111  0
  26  1  00000110  1  00001111  0
  27  1  00000110  0  00001111  0
  28  1  00000110  0  00001111  1
  29  1  00000110  0  00000110  0
""");
    ok(c.steps, 32);
   }

  static void test_delay()
   {final Chip c = new Chip("Delay");
    Pulse p = c.pulse ("pulse", 8, 4);
    Gate  d = c.delay ("load",  p, 3);
    Gate  o = c.Output("out",   d);

    c.executionTrace("p d", "%s %s", p, d);
    c.simulationSteps(16);
    c.simulate();
    //c.printExecutionTrace(); stop();

    c.ok(STR."""
Step  p d
   1  1 .
   3  1 1
   5  0 1
   7  0 0
   9  1 0
  11  1 1
  13  0 1
  15  0 0
""");
    ok(c.steps,  17);
   }

  static void test_select()
   {Chip   c = new Chip("Select");
    Pulse  C = c.pulse("choose", 4, 2);
    Pulse  P = c.pulse("pulse",  5, 3);
    Select S = c.select("choice", C, P);
    Gate   d = c.delay("d", S.Then, 3);
    Gate   e = c.delay("e", S.Else, 3);
    c.Output("od", d);
    c.Output("oe", e);

    c.executionTrace("C P  d e", "%s %s   %s %s", C, P, d, e);
    c.simulationSteps(32);
    c.simulate();
    //c.printExecutionTrace(); stop();

    c.ok(STR."""
Step  C P  d e
   1  1 1   . .
   3  0 1   . .
   4  0 0   . .
   5  1 0   1 0
   6  1 1   1 0
   7  0 1   0 0
   9  1 0   1 0
  11  0 1   0 1
  12  0 1   0 0
  13  1 1   0 0
  14  1 0   1 0
  15  0 0   0 1
  16  0 1   0 1
  17  1 1   0 0
  19  0 0   0 1
  21  1 1   1 0
  22  1 1   0 0
  23  0 1   0 0
  24  0 0   0 1
  25  1 0   1 0
  26  1 1   1 0
  27  0 1   0 0
  29  1 0   1 0
  31  0 1   0 1
  32  0 1   0 0
""");
    ok(c.steps, 33);
   }

  static void test_shift()
   {final Chip   c = new Chip("Shift");
    final BitBus b = c.bits      ("b", 4, 3);
    final BitBus u = c.shiftUp   ("u", b);
    final BitBus o = c.outputBits("o", u);
    final BitBus d = c.shiftDown ("d", b);
    final BitBus O = c.outputBits("O", d);

       c.simulate  ();
    ok(c.findBits("o").Int(), 6);
    ok(c.findBits("O").Int(), 1);
   }

  static void test_binaryAdd()
   {for     (int B = 1; B <= (github_actions ? 4 : 3); B++)
     {final  int B2 = powerTwo(B);
      for   (int i = 0; i < B2; i++)
       {for (int j = 0; j < B2; j++)
         {final Chip      c = new Chip("Binary Add");
          final BitBus    I = c.bits("i", B, i);
          final BitBus    J = c.bits("j", B, j);
          final BinaryAdd a = c.binaryAdd ("carry", "ij",  I, J);
          final BitBus    o = c.outputBits("o", a.sum);
          c.Output    ("co", a.carry);
          c.simulate  ();
          ok(a.sum.Int(),   (i+j) %  B2);
          ok(a.carry.value, (i+j) >= B2);
         }
       }
     }
   }

  static void test_registerSources()
   {final int N = 4;

    final Chip     C = new Chip("Register Sources");
    final BitBus  o1 = C.bits ("o1",  N,  9);
    final BitBus  o2 = C.bits ("o2",  N,  5);
    final Pulse   p1 = C.pulse("p1",  16, 3, 0);
    final Pulse   p2 = C.pulse("p2",  16, 4, 4);

    final Register r = C.register("r",  o1, p1, o2, p2);
    final BitBus   o = C.outputBits("o", r);
    C.anneal(r.loaded);
    C.simulationSteps(20);
    C.executionTrace("Reg_   1 2  l  e     e2    e2", "%s   %s %s  %s  %s  %s  %s", r, p1, p2, r.load, r.e, r.E[0], r.E[1]);
    C.simulate();
    C.ok(STR."""
Step  Reg_   1 2  l  e     e2    e2
   1  ....   1 0  .  ....  ....  ....
   2  ....   1 0  .  ....  .00.  0.0.
   3  ....   1 0  1  ..0.  1001  0000
   4  ....   0 0  1  1001  1001  0000
   5  ....   0 1  1  1001  1001  0000
   6  ....   0 1  0  1001  0000  0000
   7  ....   0 1  1  0000  0000  0101
   8  1001   0 1  1  0101  0000  0101
   9  1001   0 0  1  0101  0000  0101
  11  1001   0 0  0  0101  0000  0000
  12  1001   0 0  0  0000  0000  0000
  13  0101   0 0  0  0000  0000  0000
  17  0101   1 0  0  0000  0000  0000
  19  0101   1 0  1  0000  1001  0000
  20  0101   0 0  1  1001  1001  0000
""");
   }

  static void test_registerSources2()
   {final int N = 4;

    final Chip    C = new Chip("Register Sources");
    final BitBus o1 = C.bits ("o1",  N,  9);
    final BitBus o2 = C.bits ("o2",  N,  5);
    final Pulse  p1 = C.pulse("p1",  16, 3, 0);
    final Pulse  p2 = C.pulse("p2",  16, 4, 4);

    final Register r = C.register("r",  C.new RegIn(o1, p1), C.new RegIn(o2, p2));
    final BitBus   o = C.outputBits("o", r);
    final Gate    or = C.Output("or", r.loaded);
    C.simulationSteps(20);
    C.executionTrace("Reg_ ld   1 2  l  e     e2    e2", "%s  %s   %s %s  %s  %s  %s  %s", r, r.loaded, p1, p2, r.load, r.e, r.E[0], r.E[1]);
    C.simulate();
    //C.printExecutionTrace(); stop();
    C.ok(STR."""
Step  Reg_ ld   1 2  l  e     e2    e2
   1  ....  .   1 0  .  ....  ....  ....
   2  ....  .   1 0  .  ....  .00.  0.0.
   3  ....  .   1 0  1  ..0.  1001  0000
   4  ....  .   0 0  1  1001  1001  0000
   5  ....  .   0 1  1  1001  1001  0000
   6  ....  1   0 1  0  1001  0000  0000
   7  ....  1   0 1  1  0000  0000  0101
   8  1001  1   0 1  1  0101  0000  0101
   9  1001  0   0 0  1  0101  0000  0101
  10  1001  1   0 0  1  0101  0000  0101
  11  1001  1   0 0  0  0101  0000  0000
  12  1001  1   0 0  0  0000  0000  0000
  13  0101  1   0 0  0  0000  0000  0000
  14  0101  0   0 0  0  0000  0000  0000
  17  0101  0   1 0  0  0000  0000  0000
  19  0101  0   1 0  1  0000  1001  0000
  20  0101  0   0 0  1  1001  1001  0000
""");
   }

  static void test_connectBuses()
   {final int N = 4;

    final Chip   C = new Chip("Connect buses");
    final BitBus i = C.bits ("i",  N,  9);
    final BitBus o = C.new BitBus("o", N);
    final BitBus O = C.outputBits("O", o);
    C.connect(o, i);

    C.executionTrace("i     o     O", "%s  %s  %s", i, o, O);
    C.simulate();
    //C.printExecutionTrace(); stop(C);

    C.ok(STR."""
Step  i     o     O
   1  1001  ....  ....
   2  1001  1001  ....
   3  1001  1001  1001
""");
   }

  static void test_8p5i4()
   {final int N = 8;   // 4 bits = 19 steps 8 = 46 steps
    final Chip        C = new Chip("Binary Add");
    final BitBus      a = C.bits("a",  N, 127);
    final BitBus      b = C.bits("b",  N, 128);
    final BinaryAdd   c = C.binaryAdd("carry", "c", a, b);
    final Gate       oc = C.Output("oc", c.carry);
    final BitBus     os = C.outputBits("os", c.sum);
    C.simulationSteps(48);
    C.simulate();
    ok(os.toString(), "11111111");                                              // toString unfortunately required
   }

// 2 3 5 8 13 21 34 55 89 144 233

  static void test_fibonacci()                                                  // First few fibonacci numbers
   {final int N = 4;
    final Chip          C = new Chip("Fibonacci");                              // Create a new chip
    final BitBus     zero = C.bits("zero", N, 0);                               // Zero - the first element of the sequence
    final BitBus      one = C.bits("one",  N, 1);                               // One - the second element of the sequence
    final Pulse        in = C.pulse("ia", 1024,  16);                           // Initialize the registers to their starting values
    final Pulse        la = C.pulse("la",  128,  30, 32, 1);                    // Each pair sum is calculated on a rotating basis
    final Pulse        lb = C.pulse("lb",  128,  30, 64, 1);
    final Pulse        lc = C.pulse("lc",  128,  30,  0, 1);
    final Pulse        ed = C.pulse("ed", 1024, 202);                           // Enable the output register once it has had a chance to stabilize

    final BitBus       ab = C.new BitBus("ab", N);                              // Pre declare the output of the pair sums so that we can use these buses to drive the registers holding the latest fibonacci numbers
    final BitBus       ac = C.new BitBus("ac", N);
    final BitBus       bc = C.new BitBus("bc", N);

    final Register      a = C.register("a", zero, in, bc, la);                  // Registers holding the latest fibonacci number
    final Register      b = C.register("b",  one, in, ac, lb);
    final Register      c = C.register("c", zero, in, ab, lc);

    final Register      d = C.register("d",                                     // Load the output register with the latest fibonacci number and show it is present with a falling edge
      C.new RegIn(zero, in),      C.new RegIn(a, a.loaded),                     // Initially the output register is zero, subsequently it is to the appropriate pair sum
      C.new RegIn(b,   b.loaded), C.new RegIn(c, c.loaded));

    final Gate         ld = C.Or("Ld", ed, d.loaded);                           // Show output register ready with falling edge

    final BinaryAdd   sab = C.binaryAdd("cab", "ab", a, b);                     // Add in pairs
    final BinaryAdd   sac = C.binaryAdd("cac", "ac", a, c);
    final BinaryAdd   sbc = C.binaryAdd("cbc", "bc", b, c);
    C.anneal(sab.carry, sac.carry, sbc.carry);                                  // Ignore the carry bits
    final BitBus       od = C.outputBits("od",   d);                            // Output the latest fibonacci number
    final Gate        old = C.Output    ("old", ld);                            // falling edge shows that the register has been loaded with a new number

    C.executionTrace(
      "d      ld",
      "%s    %s",
      d, ld);
    C.simulationSteps(552);
    C.simulate();
    //C.printExecutionTrace(); stop();
    C.ok(STR."""
Step  d      ld
   1  ....    1
 170  0001    1
 202  0000    1
 204  0000    0
 206  0000    1
 234  0001    1
 236  0001    0
 270  0001    1
 300  0001    0
 302  0001    1
 330  0010    1
 332  0010    0
 334  0010    1
 362  0011    1
 364  0011    0
 398  0011    1
 426  0101    1
 428  0101    0
 430  0101    1
 458  1000    1
 460  1000    0
 462  1000    1
 490  1101    1
 492  1101    0
 526  1101    1
""");
   }

/* Expanded trace
    C.executionTrace(
      "in  la lb lc   da db dc  a    b    c    d     dl",
      "%s   %s  %s  %s   %s  %s  %s   %s %s %s   %s    %s",
      in,  la, lb, lc,  da, db, dc, a, b, c, d, d.load);
    C.simulationSteps(484);
    C.simulate();
    C.printExecutionTrace(); stop();
    C.ok(STR."""
Step  in  la lb lc  a    b    c
   1  1   0  0  0   .... .... ....
  17  0   0  0  0   .... .... ....
  21  0   0  0  0   0001 0001 0001
 129  0   0  0  1   0001 0001 0001
 161  0   1  0  0   0001 0001 0001
 164  0   1  0  0   0001 0001 0010
 193  0   0  1  0   0001 0001 0010
 196  0   0  1  0   0011 0001 0010
 225  0   0  0  0   0011 0001 0010
 228  0   0  0  0   0011 0101 0010
 257  0   0  0  1   0011 0101 0010
 289  0   1  0  0   0011 0101 0010
 292  0   1  0  0   0011 0101 1000
 321  0   0  1  0   0011 0101 1000
 324  0   0  1  0   1101 0101 1000
 353  0   0  0  0   1101 0101 1000
""");
*/

  static void test_insertIntoArray()                                            // Insert a word in an array of words
   {final int B = 4;
    final int[]array = {2, 4, 6, 8};                                            // Array to insert into
    final int M = 0b1111;
    for (int j = 0; j <= B; j++)
     {final int I = 2 * j + 1, mm = (M>>j)<<j;
      final Chip    c = new Chip("InsertIntoArray");                            // Create a new chip
      final WordBus w = c.words("array", B, array);                             // Array to insert into
      final BitBus  m = c.bits("mask",   B, mm);                                // Monotone mask insertion point
      final BitBus  i = c.bits("in",     B, I);                                 // Word to insert

      final WordBus W = c.insertIntoArray("o", w, m, i);                        // Insert
      final WordBus O = c.outputWords    ("O", W);
      c.simulate();
      switch(j)
       {case 0: W.ok(1,2,4,6); break;
        case 1: W.ok(2,3,4,6); break;
        case 2: W.ok(2,4,5,6); break;
        case 3: W.ok(2,4,6,7); break;
        case 4: W.ok(2,4,6,8); break;
       }
     }
   }

  static void test_removeFromArray()                                            // Remove a word from an array, slide the array down to cover the gap and insert a new word at the top to cover the resulting gap there.
   {final int B = 4;
    final int[]array = {2, 4, 6, 8};                                            // Array to remove from
    final int M = 0b1111;
    for (int j = 0; j <= B; j++)
     {final int I = 1, mm = (M>>j)<<j;
      final Chip    c = new Chip("RemoveFromArray");                            // Create a new chip
      final WordBus w = c.words("array", B, array);                             // Array to remove from
      final BitBus  m = c.bits("mask",   B, mm);                                // Monotone mask removal point
      final BitBus  i = c.bits("in",     B, I);                                 // Word to remove

      final RemoveFromArray W = c.removeFromArray("o", w, m, i);                // Remove
      final WordBus O = c.outputWords    ("O", W.out);
      final BitBus  R = c.outputBits     ("R", W.removed);
      c.simulate();
      switch(j)
       {case 0: O.ok(4,6,8,1); R.ok(2); break; // 1111
        case 1: O.ok(2,6,8,1); R.ok(4); break; // 1110
        case 2: O.ok(2,4,8,1); R.ok(6); break; // 1100
        case 3: O.ok(2,4,6,1); R.ok(8); break; // 1000
        case 4: O.ok(2,4,6,8); R.ok(0); break; // 0000 - prevent removal of 8
       }
     }
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
    test_connectBuses();
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
    test_delay();
    test_select();
    test_shift();
    test_binaryAdd();
    test_BtreeNode();
    test_BtreeLeafCompare();
    test_Btree();
    test_8p5i4();
    test_register();
    test_registerSources();
    test_fibonacci();
    test_insertIntoArray();
    test_removeFromArray();
   }

  static void newTests()                                                        // Tests being worked on
   {if (github_actions) return;
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try
     {if (github_actions)
        oldTests();
      newTests();
      gds2Finish();                                                             // Execute resulting Perl code to create GDS2 files
      if (testsFailed == 0) say("PASSed ALL", testsPassed, "tests");
      else say("Passed "+testsPassed+",    FAILed:", testsFailed, "tests.");
     }
    catch(Exception e)
     {System.err.println(traceBack(e));
     }
   }
 }
