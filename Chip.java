//------------------------------------------------------------------------------
// Design, simulate and layout a binary tree on a silicon chip.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
// updateEdge() should use stack of memory cells so we do not have to iterate over all gates
// Add fell to gate Chip.toString() and update pin information
// Check gate names match variable names
// Words words( should accept null values and be a class not an interface
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.io.*;
import java.util.*;

//D1 Construct                                                                  // Construct a L<silicon> L<chip> using standard L<lgs>, components and sub chips combined via buses.

public class Chip                                                               // Describe a chip and emulate its operation.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));
  final static long             start = System.nanoTime();                      // Start time

  final String                   name;                                          // Name of chip
  final int                clockWidth;                                          // Number of bits in system clock. Zero implies no clock.

  Integer               layoutLTGates = 100;                                    // Always draw the layout if it has less than this many gates in it or if there is no limit specified
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

  final Map<String, Gate>       gates = new TreeMap<>();                        // Gates by name
  final Map<String, Bits>    bitBuses = new TreeMap<>();                        // Several bits make a bit bus
  final Map<String, Words>  wordBuses = new TreeMap<>();                        // Several bit buses make a word bus
  final Set<String>       outputGates = new TreeSet<>();                        // Output gates
  final Map<String, TreeSet<Gate.WhichPin>>                                     // Pending gate definitions
                              pending = new TreeMap<>();
  final Map<String, Gate>                                                       // Gates that are connected to an output gate
                    connectedToOutput = new TreeMap<>();
  final Map<String, InputUnit> inputs = new TreeMap<>();                        // Input devices connected to the chip

  final Map<String, OutputUnit>                                                 // Output devices connected to the chip
                              outputs = new TreeMap<>();
  final Map<String, Pulse>     pulses = new TreeMap<>();                        // Bits that are externally driven by periodic pulses of a specified duty cycle
  final Map<String, Monitor> monitors = new TreeMap<>();                        // Monitor pulses used to clock registers to ensure they remain viable during execution

  final Bits                 clock0;                                            // Negative clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.
  final Bits                 clock1;                                            // Positive clock input bus name. Changes to this bus do not count to the change count for each step so if nothing else changes the simulation will be considered complete.

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
                        diagramsDrawn = new TreeMap<>();                        // Avoid redrawing the same layout multiple times by only redrawing a new layout if it has a smaller number of levels or is closer to a square
  int                        gsx, gsy;                                          // The global scaling factors to be applied to the dimensions of the gates during layout
  int                layoutX, layoutY;                                          // Dimensions of chip
  Stack<Connection>       connections;                                          // Pairs of gates to be connected
  Diagram                     diagram;                                          // Diagram specifying the layout of the chip

  Chip(String Name)                                                             // Create a new L<chip>.
   {this(Name, 0);                                                              // Name of chip
   }

  Chip()                                                                        // Create a new L<chip> while testing
   {this(currentTestName(), 0);                                                 // Name of chip is the name of the calling test
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
    for (Gate g : gates.values()) g.setSystemGate();                            // Mark all gates produced so far as system gates
   }

  Integer      layoutLTGates(Integer      LayoutLTGates) {return          layoutLTGates =          LayoutLTGates;}  // Always draw the layout if it has less than this many gates in it
  int     maxSimulationSteps(int     MaxSimulationSteps) {return     maxSimulationSteps =     MaxSimulationSteps;}  // Maximum simulation steps
  int     minSimulationSteps(int     MinSimulationSteps) {return     minSimulationSteps =     MinSimulationSteps;}  // Minimum simulation steps
  int singleLevelLayoutLimit(int SingleLevelLayoutLimit) {return singleLevelLayoutLimit = SingleLevelLayoutLimit;}  // Limit on gate scaling dimensions during layout.

  void simulationSteps(int min, int max) {minSimulationSteps(min);   maxSimulationSteps(max);}                      // Stop cleanly between the specified minimum and maximum number of steps
  void simulationSteps(int steps)        {minSimulationSteps(steps); maxSimulationSteps(steps);}                    // Stop cleanly at this number of steps

  enum Operator                                                                 // Gate operations
   {And, Continue, FanOut, Gt, Input, Lt, My,
    Nand, Ngt, Nlt, Nor, Not, Nxor,
    One, Or, Output, Xor, Zero;
   }

  boolean commutative(Operator op)                                              // Whether the pin order matters on the gate or not
   {return op != Operator.Gt  && op != Operator.Lt && op != Operator.My &&
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

  boolean definedGate(Bit bit)                                                  // Check whether a gate has been defined yet
   {final Gate g = gates.get(bit.name);
    return g != null;                                                           // A bit can be forward defined without a definite gate type, although eventually they must be backed by a gate.
   }

  Gate getGate(String name)                                                     // Get details of named gate. Gates that have not been created yet will return null even though their details are pending.
   {if (name == null) stop("No gate name provided");
    final Gate g = gates.get(name);
    if (g == null) stop("No gate named:", name);
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
       {b.append(String.format("%4d  %32s", bitBuses.get(n).bits(), n));
        final Integer v = bitBuses.get(n).Int();
        if (v != null) b.append(String.format("  %d\n", v));
                       b.append(System.lineSeparator());
       }
     }

    if (wordBuses.size() > 0)                                                   // Size of word buses
     {b.append(""+wordBuses.size()+" Word buses\n");
      b.append("Words Bits  Bus_____________________________  Values\n");
      for (String n : wordBuses.keySet())
       {final Words w = wordBuses.get(n);
        b.append(String.format("%4d  %4d  %32s  ", w.words(), w.bits(), n));
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

  class Bit implements CharSequence                                             // Name of a bit that will eventually be generated by a gate
   {final String name;                                                          // Name of the bit.  This is also the name of the gate and the output of the gate. FanOut gates have a second output which is the name suffixed by the secondary marker. The secondary marker is not normally used in a gate name so a name that ends in ! is easily recognized as the second output of a fan out gate.

    Bit(CharSequence Name)                                                      // Named bit
     {name = validateName(Name.toString());                                     // Validate name
     }

    Chip chip() {return Chip.this;}                                             // The chip implementing this bit

    String validateName(String name)                                            // Confirm that a component name looks like a variable name and has not already been used
     {if (name == null) stop("Name needed");
      final String[]words = name.split("_");
      for (int i = 0; i < words.length; i++)
       {final String w = words[i];
        if (!w.matches("\\A([a-zA-Z][a-zA-Z0-9_.:]*|\\d+)\\Z"))
          stop("Invalid gate name:", name, "in word", w);
       }
      return name;
     }

    Boolean value()                                                             // Value of bit
     {final Gate g = getGate(name);
      if (g == null) stop("No gate associated with bit:", name);
      return g.value;
     }

    void ok(Boolean expected)                                                   // Confirm that a bit has the expected value
     {final Boolean value = value();
      int fails = 0;
      if      (false) {}                                                        // The various combinations with null
      else if (value == null && expected == null) {}
      else if (value == null && expected != null) ++fails;
      else if (value != null && expected == null) ++fails;
      else if (value !=         expected)         ++fails;
      if (fails > 0)                                                            // Show error location with a trace back so we know where the failure occurred
       {err("Bit value:", value, "does not match expected", expected);
        ++testsFailed;
       }
      else ++testsPassed;                                                       // Passed
     }

    void anneal() {Output(n(name, "anneal"), this);}                            // Anneal bit if it is not required for some reason although this would waste surface area and power on a real chip.
    public String toString()      {return name;}                                // Name of bit
    public char charAt(int index) {return name.charAt(index);}
    public int  length()          {return name.length();}
    public CharSequence subSequence(int start, int end)
     {return name.substring(start, end);
     }

    void set(Boolean value)                                                     // Set the value of a bit by setting the gate that drives it
     {final Gate g = getGate(name);                                             // Gate providing bit
      if (g == null) stop("No such gate named:", name);                         // Find gate driving bit
      g.value = value;                                                          // Set bit
     }
   }

  Bit bit(CharSequence Name) {return new Bit(Name);}                            // Get a named bit or define such a bit if it does not already exist

  class Gate extends Bit                                                        // Description of a gate that produces a bit
   {final Operator     op;                                                      // Operation performed by gate
    final int         seq;                                                      // Sequence number for gate
    static int        Seq = 0;                                                  // Sequence numbers for gates
    Gate           iGate1,  iGate2;                                             // Gates driving the inputs of this gate as during simulation but not during layout
    Bit  soGate1, soGate2, tiGate1, tiGate2;                                    // Pin assignments on source and target gates used during layout but not during simulation
    final TreeSet<WhichPin>
                      drives = new TreeSet<>();                                 // The names of the gates that are driven by the output of this gate with a possible pin selection attached
    boolean           pulsed = false;                                           // Used by Pulse type gates to create a pulse in response to a falling edge.
    boolean       systemGate = false;                                           // System gate if true. Supresses checking that this gate is driven by something as it is driven externally by the surrounding environment.
    boolean             fell = false;                                           // Gate fell from high to low during the latest step
    Integer distanceToOutput;                                                   // Distance to nearest output. Used during layout to position gate on silicon surface.
    Boolean            value;                                                   // Current output value of this gate
    Boolean        nextValue;                                                   // Next value to be assumed by the gate at the end of the step.
    boolean          changed;                                                   // Value if this gate changed during current simulation step
    int     firstStepChanged;                                                   // First step at which we changed - used to help position gate on silicon surface during layout.
    int      lastStepChanged;                                                   // Last step at which we changed - used to help position gate on silicon surface during layout.
    String     nearestOutput;                                                   // The name of the nearest output so we can sort each layer to position each gate vertically so that it is on approximately the same Y value as its nearest output.
    int               px, py;                                                   // Position in x and y of gate in latest layout

    public Gate(Operator Op, CharSequence Name, Bit Input1, Bit Input2)         // User created gate with a user supplied name and inputs
     {super(Name);
      op  = Op;
      seq = ++Seq;
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

    public Gate(Operator Op, CharSequence Name) {this(Op, Name, null, null);}   // User created gate with a user supplied name

    void setSystemGate() {systemGate = true;}                                   // Mark as a system gate so that it does not get reported as lacking a driver

    String drives()                                                             // Convert drives to a printable string
     {final StringBuilder b = new StringBuilder();
      for (WhichPin s : drives) b.append(s + ", ");
      if (b.length() > 0) b.delete(b.length() - 2, b.length());
      return b.toString();
     }

    class WhichPin implements Comparable<WhichPin>                              // Shows that this gate drives another gate either on a specific pin or on any pin if the gate is commutative
     {final Bit  drives;                                                        // Drives this named gate
      final Boolean pin;                                                        // Null can drive any pin on target, true - must drive input pin 1, false - must drive input pin 2

      WhichPin(Bit Drives, Boolean Pin) {drives = Drives; pin = Pin;}           // Construct a pin drive specification targeting a specified input pin
      WhichPin(Bit Drives)              {this(Drives, null);}                   // Construct a pin drive specification targeting any available input pin
      Gate gate() {return getGate(drives);}                                     // Details of gate being driven

      public int compareTo(WhichPin a)                                          // So we can add and remove entries from the set of driving gates
       {return drives.name.compareTo(a.drives.name);
       }

      public String toString()                                                  // Convert drive to string
       {if (pin == null) return drives.name;
        if (pin)         return drives.name+">1";
                         return drives.name+">2";
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
        s.drives.add(new WhichPin(this));                                       // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin> d = pending.get(Input.name);
        if (d == null)   {d = new TreeSet<>(); pending.put(Input.name, d);}
        d.add(new WhichPin(this));
       }
     }

    void impinge(Bit Input, Boolean pin)                                        // Go to the named gate (which must therefore already exist) and show that it drives this gate on the specified input pin
     {if (definedGate(Input))
       {final Gate s = getGate(Input);
        s.drives.add(new WhichPin(this, pin));                                  // Show that the source gate drives this gate
       }
      else                                                                      // The source gates have not been defined yet so add the impinging definitions to pending
       {TreeSet<WhichPin>  d = pending.get(Input.name);
        if (d == null)    {d = new TreeSet<>(); pending.put(Input.name, d);}
        d.add(new WhichPin(this, pin));
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
        err(op, "gate", name, "does not drive any gate");
        return;
       }

      if (N > 2) stop("Gate", name, "drives", N,                                // At this point each gate should have no more than two inputs
        "gates, but a gate can drive no more than 2 other gates");

      for (WhichPin t : drives)                                                 // Connect targeted gates back to this gate
       {if (t != null)                                                          // Fan out is wider than strictly necessary but tolerated to produce a complete tree.
         {final Gate T = t.gate();
          if      (T.iGate1 == this || T.iGate2 == this) {}                     // Already set
          else if (T.iGate1 == null && t.ok1()) T.iGate1 = this;                // Use input pin 1
          else if (T.iGate2 == null && t.ok2()) T.iGate2 = this;                // Use input pin 2
          else                                                                  // No input pin available
           {if (t.pin == null) stop("Gate name:", T.name, "type", T.op,
              "driven by too many other gates, including one from gate:", name);
            else               stop("Gate:", T.name,
              "does not have enough pins to be driven by:", t, "from", name);
           }
         }
       }
     }

    void compile()                                                              // Check that the gate is being driven or is an input gate
     {if (iGate1 == null && iGate2 == null && !zerad(op))                       // All gates except input gates require at least one input
        stop("Gate name:", name, "type:", op, "is not being driven by any other gate");
     }

    void updateEdge()                                                           // Update a memory gate on a falling edge. The memory bit on pin 2 is loaded when the value on pin 1 goes from high to low because reasoning about trailing edges seems to be easier than reasoning about leading edges
     {if (op == Operator.My)                                                    // Memory updates are triggered by a falling edge on input pin 1
       {if (iGate1.value != null && iGate1.value && iGate1.nextValue != null && !iGate1.nextValue) // Falling edge on pin 1
         {//if (iGate2.value != null)                                           // Unknown must also be propogated
           {changed = iGate2.value != iGate2.nextValue; // Necessary?
            value   = iGate2.nextValue;
           }
         }
       }
      fell = value != null && value && nextValue != null && !nextValue;         // Falling edge
     }

    void updateValue()                                                          // Update the value of the gate
     {if (op != Operator.My)
       {changed = !systemGate && nextValue != value;                            // Suppress changes made by the system clock otherwise a chip with a clock will never stabilize
        value   = nextValue;
       }
     }

    void step()                                                                 // One step in the simulation
     {final Boolean g = iGate1 != null ? iGate1.value : null,
                    G = iGate2 != null ? iGate2.value : null;
      nextValue = switch(op)                                                    // Null means we do not know what the value is.  In some cases involving dyadic commutative gates we only need to know one input to be able to deduce the output.  However, if the gate ouput cannot be computed then its result must be null meaning "could be true or false".
       {case And     ->{if (g != null && G != null)  yield   g &&  G;  else if ((g != null && !g) || (G != null && !G)) yield false; else yield null;}
        case Nand    ->{if (g != null && G != null)  yield !(g &&  G); else if ((g != null && !g) || (G != null && !G)) yield true;  else yield null;}
        case Or      ->{if (g != null && G != null)  yield   g ||  G;  else if ((g != null &&  g) || (G != null &&  G)) yield true;  else yield null;}
        case Nor     ->{if (g != null && G != null)  yield !(g ||  G); else if ((g != null &&  g) || (G != null &&  G)) yield false; else yield null;}
        case Gt      ->{if (g != null && G != null)  yield   g && !G;                                                                else yield null;}
        case Lt      ->{if (g != null && G != null)  yield  !g &&  G;                                                                else yield null;}
        case My      -> null;
        case Ngt     ->{if (g != null && G != null)  yield  !g ||  G;                                                                else yield null;}
        case Nlt     ->{if (g != null && G != null)  yield   g || !G;                                                                else yield null;}
        case Not     ->{if (g != null)               yield  !g;                                                                      else yield null;}
        case Nxor    ->{if (g != null && G != null)  yield !(g ^   G);                                                               else yield null;}
        case Xor     ->{if (g != null && G != null)  yield   g ^   G;                                                                else yield null;}
        case One     -> true;
        case Zero    -> false;
        case Input   -> value;
        case Continue, FanOut, Output -> g;
        default      -> {stop("Unexpected gate type", op); yield null;}
       };
     }

    void fanOut()                                                               // Fan out when more than two gates are driven by this gate
     {final int N = drives.size(), N2 = N / 2;                                  // Number of pins driven by this gate
      if (op == Operator.Output) return;                                        // Output gates do not fan out
      if (N <= 2) return;                                                       // No fan out required because we have maintained the rule that no gate may drive more than two gates directly.

      final WhichPin[]D = drives.toArray(new WhichPin[N]);                      // The input pins driven by this output spread across the fan

      final Gate g = new Gate(Operator.FanOut, n(1, name, "f"));                // Even and greater than 2
      final Gate f = new Gate(Operator.FanOut, n(2, name, "f"));
      final int P = logTwo(N);                                                  // Length of fan out path - we want to make all the fanout paths the same length to simplify timing issues
      final int Q = powerTwo(P-1);                                              // Size of a full half
      for (int i = 0; i < Q; ++i)                                               // Lower half full tree
       {final WhichPin d = D[i];
        g.drives.add(d);                                                        // Transfer drive to the new lower gate
        drives.remove(d);                                                       // Remove drive from current gate
       }
      drives.add(new WhichPin(g));                                              // The old gate drives the new gate
      g.fanOut();                                                               // The lower half gate might need further fan out

      for (int i = Q; i < N; ++i)                                               // Upper half - might not be a complete tree
       {final WhichPin d = D[i];
        f.drives.add(d);                                                        // Transfer drive to new lower gate
        drives.remove(d);                                                       // Remove drive from current gate
       }

      if (2 * N >= 3 * Q)                                                       // Can fill out most of the second half of the tree
       {drives.add(new WhichPin(f));                                            // The old gate drives the new gate
        f.fanOut();                                                             // Fan out lower gate
       }
      else                                                                      // To few entries to fill more than half of the leaves so push the sub tree down one level
       {final Gate e = new Gate(Operator.FanOut, n(name, "f"));                 // Extend the path
        drives.add(new WhichPin(e));                                            // The old gate drives the extension gate
        e.drives.add(new WhichPin(f));                                          // The extension gate drives the new gate
        f.fanOut();                                                             // Fanout the smaller sub stree
       }
     }
   } // Gate

  Gate FanIn(Operator Op, CharSequence Named, Bit...Input)                      // Normal gate - not a fan out gate
   {final String Name = Named.toString();
    final int N = Input.length;
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

    final Operator l = switch(Op)                                               // Lower operator
     {case Nand -> Operator.And;
      case Nor  -> Operator.Or;
      default   -> Op;
     };

    final Operator u = switch(Op)                                               // Upper operator
     {case And, Nand -> {yield N > Q + 1 ? Operator.And : Operator.Continue;}
      case Or,  Nor  -> {yield N > Q + 1 ? Operator.Or  : Operator.Continue;}
      default        -> Op;
     };

    final Gate f = FanIn(l, n(1, Name, "F"), Arrays.copyOfRange(Input, 0, Q));  // Lower half is a full sub tree
    final Gate g = FanIn(u, n(2, Name, "F"), Arrays.copyOfRange(Input, Q, N));  // Upper half might not be full

    if (2 * N >= 3 * Q) return new Gate(Op, Name, f, g);                        // No need to extend path to balance it

    final Gate e = FanIn(Operator.Continue, n(Name, "F"), g);                   // Extension gate to make the upper half paths the same length as the lower half paths
    return new Gate(Op, Name, f, e);
   }

  Gate Input   (CharSequence n)               {return FanIn(Operator.Input,    n);}
  Gate One     (CharSequence n)               {return FanIn(Operator.One,      n);}
  Gate Zero    (CharSequence n)               {return FanIn(Operator.Zero,     n);}
  Gate Output  (CharSequence n, Bit i)        {return FanIn(Operator.Output,   n, i);}
  Gate Continue(CharSequence n, Bit i)        {return FanIn(Operator.Continue, n, i);}
  Gate Not     (CharSequence n, Bit i)        {return FanIn(Operator.Not,      n, i);}

  Gate Nxor    (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Nxor,     n, i, j);}
  Gate Xor     (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Xor,      n, i, j);}
  Gate Gt      (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Gt,       n, i, j);}
  Gate Ngt     (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Ngt,      n, i, j);}
  Gate Lt      (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Lt,       n, i, j);}
  Gate Nlt     (CharSequence n, Bit i, Bit j) {return FanIn(Operator.Nlt,      n, i, j);}
  Gate My      (CharSequence n, Bit i, Bit j) {return FanIn(Operator.My,       n, i, j);} // When pin 1 falls from 1 to 0 record the value of pin 2

  Gate And     (CharSequence n, Bit...i)      {return FanIn(Operator.And,      n, i);}
  Gate Nand    (CharSequence n, Bit...i)      {return FanIn(Operator.Nand,     n, i);}
  Gate Or      (CharSequence n, Bit...i)      {return FanIn(Operator.Or,       n, i);}
  Gate Nor     (CharSequence n, Bit...i)      {return FanIn(Operator.Nor,      n, i);}

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

    Set<String> check = outputGates;                                            // Search backwards from the output gates
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

  void printErrors()                                                            // Print any errors and stop if there are some
   {if (errors.size() > 0)                                                      // Print any recorded errors and stop.
     {say(errors.size(), "errors during compilation of chip:");
      for (String s : errors) say(s);
      stop("Stopping because of chip compile errors");
     }
   }

  void compileChip()                                                            // Check that an input value has been provided for every input pin on the chip.
   {final Gate[]G = gates.values().toArray(new Gate[0]);
    for (Gate  g : G) g.fanOut();                                               // Fan the output of each gate if necessary which might introduce more gates
    for (Pulse p : pulses.values()) p.compile();                                // Compile the details of the pulse
    for (Gate  g : gates.values())  g.compileGate();                            // Each gate on chip
    for (Gate  g : gates.values())  g.compile();                                // Check each gate is being driven
    for (Bit   b : gates.values())   getGate(b);                                // Check that each bit is realized as a gate

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
        else stop("Input gate \""+g.name+"\" has no initial value");
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

    Inputs set(Bit s, boolean value)                                            // Set the value of an input
     {inputs.put(s.name, value);
      return this;
     }

    Inputs set(Bits input, int value)                                           // Set the value of an input bit bus
     {final int bits = input.bits();                                            // Get the size of the input bit bus
      final boolean[]b = bitStack(bits, value);                                 // Bits to set
      for (int i = 1; i <= bits; i++) set(input.b(i), b[i-1]);                  // Load the bits into the input bit bus
      return this;
     }

    Inputs set(Words wb, int...values)                                          // Set the value of an input word bus
     {for   (int w = 1; w <= wb.words(); w++)                                   // Each word
       {final boolean[]b = bitStack(wb.bits(), values[w-1]);                    // Bits from current word
        for (int i = 1; i <= wb.bits(); i++) set(wb.b(w, i), b[i-1]);           // Load the bits into the input bit bus
       }
      return this;
     }

    Boolean get(Bit s) {return inputs.get(s.name);}                             // Get the value of an input
   }

  interface SimulationStep {void step();}                                       // Called each simulation step
  SimulationStep simulationStep = null;                                         // Called each simulation step

  static class ExecutionTrace                                                   // Trace the values of named bits and bit buses as the execution proceeds
   {final Object[]objects;                                                      // Objects to trace
    final String title;                                                         // Title
    final String format;                                                        // Format string
    final Stack<String> trace = new Stack<>();                                  // Execution trace
    final boolean compress;                                                     // Do not print duplicate entries if true
    ExecutionTrace(String Title, String Format, boolean Compress, Object...Objects) // Detail of objects to be traced
     {title = Title; format = Format; compress = Compress; objects = Objects;
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
        if (!compress || !s.equals(t))                                          // Remove duplicate entries if compression requested
          b.append(String.format("%4d  %s\n", i+1, trace.elementAt(i)));
       }
      return b.toString();
     }
   }

  void executionTrace(String Title, String Format, Object...Objects)            // Request an execution trace removing duplicate records
   {   executionTrace = new ExecutionTrace(Title, Format, true, Objects);
   }

  void executionTrack(String Title, String Format, Object...Objects)            // Request an execution trace showing duplicate records
   {   executionTrace = new ExecutionTrace(Title, Format, false, Objects);
   }

  void printExecutionTrace() {if (executionTrace != null) say(executionTrace);} // Detail of objects to be traced

  void ok(String expected)                                                      // Confirm execution trace matches expected output
   {if (executionTrace == null) return;
    ok(executionTrace.toString(), expected);
   }

  Diagram simulate() {return simulate(null);}                                   // Simulate the operation of a chip with no input pins. If the chip has in fact got input pins an error will be reported.

  Diagram simulate(Inputs Inputs)                                               // Simulate the operation of a chip
   {compileChip();                                                              // Check that the inputs to each gate are defined
    initializeGates(Inputs);                                                    // Set the value of each input gate

    for (InputUnit  i : inputs .values()) i.start();                            // Each input  peripheral on chip
    for (OutputUnit o : outputs.values()) o.start();                            // Each output peripheral on chip

    final int actualMaxSimulationSteps =                                        // Actual limit on number of steps
      maxSimulationSteps != null ? maxSimulationSteps : defaultMaxSimulationSteps;
    final boolean miss = minSimulationSteps != null;                            // Minimum simulation steps set

    final Gate      []G = gates   .values().toArray(new Gate      [0]);         // Gates on chip
    final Pulse     []P = pulses  .values().toArray(new Pulse     [0]);         // Pulses
    final InputUnit []I = inputs  .values().toArray(new InputUnit [0]);         // Input peripherals
    final OutputUnit[]O = outputs .values().toArray(new OutputUnit[0]);         // Output peripherals
    final Monitor   []M = monitors.values().toArray(new Monitor   [0]);         // Check register load signals remain viable during execution

    for (steps = 1; steps <= actualMaxSimulationSteps; ++steps)                 // Steps in time
     {loadClock();                                                              // Load the value of the clock into the clock input bus
      for (Gate       g : G) g.step();                                          // Compute next value for  each gate
      for (Pulse      p : P) p.setState();                                      // Load all the pulses for this chip
      for (Gate       g : G) g.updateEdge();                                    // Update each gate triggered by a falling edge transition
      for (Gate       g : G) g.updateValue();                                   // Update each gate not affected by a falling edge after the gates that are
      for (InputUnit  i : I) i.action();                                        // Action on each input peripheral affected by a falling edge
      for (OutputUnit o : O) if (o.fell()) o.action();                          // Action on each output peripheral affected by a falling edge
      if (executionTrace != null) executionTrace.trace();                       // Trace requested
      for (Monitor    m : M) m.check();                                         // Check register load signals remain viable during execution

      if (!changes())                                                           // No changes occurred
       {if (!miss || steps >= minSimulationSteps) return layoutLTGates == null  // No changes occurred and we are beyond the minimum simulation time or no such time was set
        || gates.size() <= layoutLTGates ? draw() : null;                       // Draw the layout if it has less than the specified maximum number of gates for being drawn automatically with out a specific request.
       }
      noChangeGates();                                                          // Reset change indicators
     }
    if (maxSimulationSteps == null)                                             // Not enough steps available by default
      err("Out of time after", actualMaxSimulationSteps, "steps");
    return null;
   }

//D1 Circuits                                                                   // Some useful circuits

//D2 Bits                                                                       // Operations on bits

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

  void setBit(String name, Boolean value)                                       // Set the value of a bit
   {final Gate g = getGate(name);                                               // Gate providing bit
    if (g == null) stop("No such gate named:", name);                           // Find gate driving bit
    g.value = value;                                                            // Set bit
   }

//D2 Buses                                                                      // A bus is an array of bits or an array of arrays of bits

//D3 Bits                                                                       // An array of bits that can be manipulated via one name.

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

  class Bits                                                                    // A bus made out of bits
   {final String name; String name() {return name;}                             // Name of bus
    final int bits;    int    bits() {return bits;}                             // Number of bits of bus - the width of the bus
    Bit       b(int i)               {return new Bit(n(i, name));}              // Name of a bit in the bus
    boolean implemented = false;                                                // When false this is a forward declaration, when true the forward declaration has been backed by real bits
    void anneal()                                                               // Anneal this bit bus so that the annealed gates are not reported as drivign anythinmg.  Such gates hsould be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.
     {for (int i = 1; i <= bits; i++) b(i).anneal();
     }

    Bits(String Name, int Bits)                                                 // Declare the size of a named bit bus
     {name = Name; bits = Bits;
      if (bitBuses.containsKey(name))                                           // If the bits have already been defined, check that the number of bits matches
       {final Bits b = bitBuses.get(name);
        if (b.bits != Bits)
          stop("BitBus", name, "has already been defined with", b.bits,
            "versus", Bits, "requested");
       }
      bitBuses.put(name, this);                                                 // This will be overwritten by teh sub class if we are being called from a sub class - but that is not a problem as we still have this information present
     }

    public void sameSize(Bits b)                                                // Check two buses are the same size and stop if they are not
     {final int A = bits(), B = b.bits();
      if (A != B) stop("Input",  name(), "has width", A, "but input", b.name(),
                       "has width", B);
     }

    public String toString()                                                    // Convert the bits represented by an output bus to a string
     {final StringBuilder s = new StringBuilder();
      for (int i = 1; i <= bits(); i++)
       {final Bit  b = b(i);
        final Gate g = getGate(b);
        if (g != null) s.append(g.value == null ? '.' : g.value ? '1' : '0');
        else stop("No such gate as:", b.name);
       }
      return s.reverse().toString();
     }

    public Integer Int()                                                        // Convert the bits represented by an output bus to an integer
     {int v = 0, p = 1;
      final int B = bits();
      for  (int i = 1; i <= B; i++)                                             // Each bit on bus
       {final Bit b = b(i);
        final Gate g = getGate(b);                                              // We are in an interface and so static
        if (g == null)
         {err("No such gate as:", name(), i);                                   // Generally occurs during testing where we might want to continue to see what other errors  occur
          return null;
         }
        if (g.value == null) return null;                                       // Bit state not known
        if (g.value) v += p;
        p *= 2;
       }
      return v;                                                                 // Value of bit bus
     }

    public void ok(Integer e)                                                   // Confirm the expected values of the bit bus. Write a message describing any unexpected values
     {final Integer g = Int();                                                  // The values we actually got
      final StringBuilder b = new StringBuilder();
      int fails = 0, passes = 0;
      if (false)                       {}
      else if (e == null && g == null) {}
      else if (e != null && g == null) {b.append(String.format("Expected %d, but got null\n", e   )); ++fails;}
      else if (e == null && g != null) {b.append(String.format("Expected null, but got %d\n", g   )); ++fails;}
      else if (!e.toString().equals(g.toString()))
                                       {b.append(String.format("Expected %d, but got %d\n",   e, g)); ++fails;}
      else ++passes;
      if (fails > 0) err(b);
      testsPassed += passes; testsFailed += fails;                              // Passes and fails
     }
   }

  Bits bits(CharSequence name, int bits)                                        // Forward declare a bit bus: get a predefined bit bus or define a new one if there is no predefined bit bus of the same name.  This allows us to create bit buses needed in advance of knowing the gates that will be attached to them - in effect - forward declaring the bit bus.
   {final Bits b = bitBuses.get(name);

    if (b != null)                                                              // Bus already exists
     {if (b.bits() != bits)                                                     // Bus already exists and with different characteristics
       {stop("A bit bus with name:", name, "and bits", b.bits(),
             "has already been defined which differs from:", bits, "bits");
       }
      return b;                                                                 // Treat as reuse if a bus of the same name and size already exists
     }

    return new Bits(name.toString(), bits);                                     // Resulting bit bus
   }

  Bits bits(String name, int bits, int value)                                   // Create a bus set to a specified number.
   {final Bits B = bits(name, bits);
    final boolean[]b = bitStack(bits, value);                                   // Number as a stack of bits padded to specified width
    for(int i = 1; i <= bits; ++i) if (b[i-1]) One(B.b(i)); else Zero(B.b(i));  // Generate constant
    return B;
   }

  class BitBus extends Bits                                                     // A bus made out of bits. Define gates with the names of the bits in the bus to realize the bus.
   {BitBus(CharSequence Name, int Bits)                                         // Create a new bit bus
     {super(Name.toString(), Bits);
      bitBuses.put(name, this);
     }

    public Bit  b   (int i)  {return bit(n(i, name));}                          // Bit in the bus
    public void  anneal()    {outputBits(n(name, "anneal"), this);}             // Anneal this bit bus so that the annealed gates are not reported as driving anything.  Such gates should be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.

    void set(int value)                                                         // Set the value of bus
     {final boolean[]b = bitStack(bits, value);                                 // Bit value
      for (int i = 1; i <= bits; i++) b(i).set(b[i-1]);                         // Set bit value
     }
   }

  class SubBitBus extends Bits                                                  // A bit bus made out of part of another bit bus
   {final Bits source;                                                          // The bit bus from which supplies the bits for this sub bit bus
    final int   start;                                                          // Start of this bus in the source bus

    SubBitBus(CharSequence Name, Bits Source, int Start, int Bits)              // Create a new sub bit bus as part of an existing bit bus
     {super(Name.toString(), Bits);
      source = Source; start = Start;
      bitBuses.put(name, this);
     }

    public Bit b(int i)      {return source.b(start-1+i);}                      // Bit in the bus
   }

  SubBitBus subBitBus(CharSequence Name, Bits Source, int Start, int Bits)      // Create a new sub bit bus as part of an existing bit bus
   {return new SubBitBus(Name, Source, Start, Bits);                            // Create a new sub bit bus as part of an existing bit bus
   }

  class ConCatBits extends Bits                                                 // Concatenate a sequence of bits
   {final Bit[]source;                                                          // The bit bus from which supplies the bits for this sub bit bus

    ConCatBits(CharSequence Name, Bit...Source)                                 // Concatenate bits
     {super(Name.toString(), Source.length);
      source = Source;
      bitBuses.put(name, this);
     }

    public Bit  b    (int i) {return bit(source[i-1]);}                         // Name of a bit in the bus
   }

  ConCatBits conCatBits(CharSequence Name, Bit...Source)                        // Concatenate bits
   {return new ConCatBits(Name, Source);
   }

  Bits inputBits(String name, int bits)                                         // Create an B<input> bus made of bits.
   {final Bits B = bits(name, bits);                                            // Record bus width
    for (int b = 1; b <= bits; ++b) Input(B.b(b));                              // Bus of input gates
    return B;
   }

  Bits outputBits(String name, Bits input)                                      // Create an B<output> bus made of bits.
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bits o = bits(name, bits);                                            // Resulting bit bus
    for (int i = 1; i <= bits; i++) Output(o.b(i), input.b(i));                 // Bus of output gates
    return o;                                                                   // Resulting bit bus
   }

  Bits continueBits(String name, Bits input)                                    // Create a B<continue> bus made of bits.
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bits o = bits(name, bits);                                            // Resulting bit bus
    for (int b = 1; b <= bits; ++b) Continue(o.b(b), input.b(b));               // Bus of continue gates
    return o;                                                                   // Resulting bit bus
   }

  Bits notBits(String name, Bits input)                                         // Create a B<not> bus made of bits.
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bits o = bits(name, bits);                                            // Resulting bit bus
    for (int b = 1; b <= bits; ++b) Not(o.b(b), input.b(b));                    // Bus of not gates
    return o;                                                                   // Resulting bit bus
   }

  Bit andBits(String name, Bits input)                                          // B<And> all the bits in a bus to get one bit
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return And(name, b);                                                        // And all the bits in the bus
   }

  Bits andBits(String output, Bits input1, Bits input2)                         // B<And> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check sizes match
    final Bits o = bits(output, b);                                             // Size of resulting bus
    for (int i = 1; i <= b; ++i) And(o.b(i), input1.b(i), input2.b(i));         // And each pair of bits
    return o;
   }

  Bits andBits(String output, Bits...input)                                     // B<And> one or more equal size bit buses together to make a bit bus of equal size
   {final int N = input.length;                                                 // Number of inputs
    if (N == 0) stop("Input bits required");                                    // Need some bits
    final int B = input[0].bits();                                              // Number of bits in input buses
    for (int i = 0; i < N; i++)                                                 // Confirm length of each input
     {final int l = input[i].bits();
      if (l != B) stop("Input[0] has", B, "bits, but Input["+i+"] has", l);
     }
    final Bits o = bits(output, B);                                             // Output result
    for   (int j = 1; j <= B; j++)                                              // Each bit of the inputs
     {final Bit[]a = new Bit[N];
      for (int i = 0; i < N; i++) a[i] = input[i].b(j);                         // Each input
      And(o.b(j), a);
     }
    return o;                                                                   // And of bits at each index in the inputs
   }

  Bit nandBits(String name, Bits input)                                         // B<Nand> all the bits in a bus to get one bit
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Nand(name, b);                                                       // Nand all the bits in the bus
   }

  Bits nandBits(String output, Bits input1, Bits input2)                        // B<Nand> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nand(o.b(i), input1.b(i), input2.b(i));        // Nand each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  Bit orBits(String name, Bits input)                                           // B<Or> all the bits in a bus to get one bit
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Or(name, b);                                                         // Or all the bits in the bus
   }

  Bits orBits(String output, Bits input1, Bits input2)                          // B<Or> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus
    input1.sameSize(input2);                                                    // Check bus sizes match
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for (int i = 1; i <= b; ++i) Or(o.b(i), input1.b(i), input2.b(i));          // Or each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  Bits orBits(String output, Bits...input)                                      // B<Or> one or more equal size bit buses together to make a bit bus of equal size
   {final int N = input.length;                                                 // Number of inputs
    if (N == 0) stop("Input bits required");                                    // Need some bits
    final int B = input[0].bits();                                              // Number of bits in input buses
    for (int i = 0; i < N; i++)                                                 // Confirm length of each input
     {final int l = input[i].bits();
      if (l != B) stop("Input[0] has", B, "bits, but Input["+i+"] has", l);
     }
    final Bits o = bits(output, B);                                             // Output result
    for   (int j = 1; j <= B; j++)                                              // Each bit of the inputs
     {final Bit[]a = new Bit[N];
      for (int i = 0; i < N; i++) a[i] = input[i].b(j);                         // Each input
      Or(o.b(j), a);
     }
    return o;                                                                   // Or of bits at each index in the inputs
   }

  Bit norBits(String name, Bits input)                                          // B<Nor> all the bits in a bus to get one bit
   {final int bits = input.bits();                                              // Number of bits in input bus
    final Bit[]b = new Bit[bits];                                               // Arrays of names of bits
    for (int i = 1; i <= bits; ++i) b[i-1] = input.b(i);                        // Names of bits
    return Nor(name, b);                                                        // Or all the bits in the bus
   }

  Bits norBits(String output, Bits input1, Bits input2)                         // B<Nor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nor(o.b(i), input1.b(i), input2.b(i));         // Nor each pair of bits
    return o;                                                                   // Resulting bus
   }

  Bits xorBits(String output, Bits input1, Bits input2)                         // B<Xor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for (int i = 1; i <= b; ++i) Xor(o.b(i), input1.b(i), input2.b(i));         // Xor each pair of bits
    return o;                                                                   // Resulting bus
   }

  Bits nxorBits(String output, Bits input1, Bits input2)                        // B<Nxor> two equal size bit buses together to make an equal sized bit bus
   {final int b = input1.bits(), c = input2.bits();                             // Number of bits in input bus 2
    input1.sameSize(input2);                                                    // Check input bus sizes match
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for (int i = 1; i <= b; ++i) Nxor(o.b(i), input1.b(i), input2.b(i));        // Nxor each pair of bits
    return o;                                                                   // Size of resulting bus
   }

  void connect(Bits input1, Bits input2)                                        // Connect two bit buses together.
   {input1.sameSize(input2);                                                    // Check the buses are the same size
    final int bits = input1.bits();                                             // Number of bits in input bus
    for (int b = 1; b <= bits; ++b) Continue(input1.b(b), input2.b(b));         // Connect the buses
   }

  Bits orBitBuses(String output, Bits...input)                                  // B<Or> several equal size bit buses together to make an equal sized bit bus
   {final int N = input.length;
    if (N < 1) stop("Need at least one input bit bus");
    for (int i = 1; i < N; ++i) input[0].sameSize(input[i]);                    // Make sure all the bit buses have the same length
    final int b = input[0].bits();                                              // Number of bits in input bus
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for   (int i = 1; i <= b; ++i)                                              // Each bit
     {final Bit[]or = new Bit[N];                                               // Corresponding bits
      for (int j = 0; j < N; ++j) or[j] = input[j].b(i);                        // Each corresponding bit in each bus
      Or(o.b(i), or);                                                           // Or across buses
     }
    return o;                                                                   // Size of resulting bus
   }

  Bits andBitBuses(String output, Bits...input)                                 // B<And> several equal size bit buses together to make an equal sized bit bus
   {final int N = input.length;
    if (N < 1) stop("Need at least one input bit bus");
    for (int i = 1; i < N; ++i) input[0].sameSize(input[i]);                    // Make sure all the bit buses have the same length
    final int b = input[0].bits();                                              // Number of bits in input bus
    final Bits o = bits(output, b);                                             // Resulting bit bus
    for   (int i = 1; i <= b; ++i)                                              // Each bit
     {final Bit[]and = new Bit[N];                                              // Corresponding bits
      for (int j = 0; j <  N; ++j) and[j] = input[j].b(i);                      // Each corresponding bit in each bus
      And(o.b(i), and);                                                         // And across buses
     }
    return o;                                                                   // Size of resulting bus
   }

  Bits   outputBits(Bits output, Bits input) {return   outputBits(output.name(), input);}
  Bits continueBits(Bits output, Bits input) {return continueBits(output.name(), input);}
  Bits      notBits(Bits output, Bits input) {return      notBits(output.name(), input);}
  Bit       andBits(Bits output, Bits input) {return      andBits(output.name(), input);}
  Bit      nandBits(Bits output, Bits input) {return     nandBits(output.name(), input);}
  Bit        orBits(Bits output, Bits input) {return       orBits(output.name(), input);}
  Bit       norBits(Bits output, Bits input) {return      norBits(output.name(), input);}

//D3 Words                                                                      // An array of arrays of bits that can be manipulated via one name.

  interface Words                                                               // Description of a word bus
   {String  name();                                                             // Name of the word bus
    int     bits();                                                             // Bits in each word of the bus
    int    words();                                                             // Words in bus

    default public Integer wordValue(int i)                                     // Get the value of a word in the word bus
     {final Bits b = w(i);                                                      // Bits
      return b.Int();
     }

    default public Integer[]Int()                                               // Convert the words on a word bus into integers
     {final Integer[]r = new Integer[words()];                                  // Words on bus
      for (int j = 1; j <= words(); j++) r[j-1] = wordValue(j);                 // Value of each word
      return r;
     }

    public Bits w(int i);                                                       // Get a bit bus in the word bus
    public Bit  b(int i, int j);                                                // Get a bit from a bit bus in the word bus

    default public void ok(Integer...E) {Chip.ok(Int(), E);}                    // Confirm the expected values of the word bus. Write a message describing any unexpected values

    default public void anneal()                                                // Anneal each word in the bus
     {final int W = words();
      for  (int w = 1; w <= W; ++w) w(w).anneal();
     }
   } // Words

  class WordBus implements Words                                                // Description of a word bus
   {final String name; public String name() {return name;}                      // Name of the word bus
    final int    bits; public int    bits() {return bits;}                      // Bits in each word of the bus
    final int   words; public int   words() {return words;}                     // Words in bus

    WordBus(String Name, int Words, int Bits)                                   // Create bus
     {name = Name; bits = Bits; words = Words;
      final Words w = wordBuses.get(name);                                      // Chip, bits bus name, words, bits per word, options
      if (w != null)
        if (words == w.words() && bits == w.bits()) {}                          // Reuse existing definition if it has the same dimensions
        else stop("A word bus with name:", name, "with words", w.words(),
         "and bits", w.bits(), "has already been defined",
         "which differs from words", words, "and bits", bits);
      wordBuses.put(name, this);                                                // Index bus
      for (int b = 1; b <= words; ++b)                                          // Size of bit bus for each word in the word bus
       {final String s = n(b, name);
        Bits B = bitBuses.get(s);                                               // Check whether the component bitBus exists or not
        if (B == null) B = new BitBus(s, bits);                                 // Create component bitBus if necessary
       }
     }

    WordBus(String Name, Words Words) {this(Name, Words.words(), Words.bits());}// Create a word bus with the same dimensions as the specified word bus.

    public Bits w(int i)        {return bitBuses.get(n(i,    name));}           // Get a bit bus in the word bus
    public Bit  b(int i, int j) {return bit         (n(i, j, name));}           // Get a bit from a bit bus in the word bus
    public String toString()                                                    // Print words
     {final StringBuilder b = new StringBuilder();
      final int W = words();
      for (int w = 1; w <= W; ++w) b.append(" "+w(w).Int());
      return b.toString();
     }
   } // WordBus

  WordBus words(String Name, int Words, int Bits)                               // Forward declaration of a word bus
   {return new WordBus(Name, Words, Bits);
   }

  class SubWordBus implements Words                                             // Select the specified range of words from a word bus
   {final String name; public String name() {return name;}                      // Name of the word bus
    final Words source;                                                         // The underlying source bus
    final int start, length;                                                    // Selected words and bits
    public int  bits() {return source.bits();}                                  // Bits in each word of the bus
    public int words() {return length;}                                         // Words in bus

    SubWordBus(String Name, Words Source, int Start, int Length)                // Create sub word bus
     {name = Name; source = Source; start = Start; length = Length;

      final Words w = wordBuses.get(name);                                      // Chip, bits bus name, words, bits per word, options

      if (w != null)
        stop("A word bus with name:", name, "has already been defined");
      wordBuses.put(name, this);                                                // Index bus
     }

    public Bits w(int i)                                                        // Get a bit bus in the word bus
     {if (i < 1 || i > length) stop("Out of range:", i, "in range", length);
      return bitBuses.get(n(start-1+i, source.name()));
     }
    public Bit  b(int i, int j)                                                 // Get a bit from a bit bus in the word bus
     {if (i < 1 || i > length) stop("Sub word out of range:", i, "in range", length);
      if (j < 1 || j > bits()) stop("Sub bit out of range:",  j, "in range", bits());
      return bit(n(start-1+i, j, source.name()));
     }
    public String toString()                                                    // Print words
     {final StringBuilder b = new StringBuilder();
      final int W = words();
      for (int w = 1; w <= W; ++w) b.append(" "+w(w).Int());
      return b.toString();
     }
   } // SubWordBus

  Words words(String name, int bits, int...values)                              // Create a word bus set to specified numbers.
   {final Words wb = new WordBus(name, values.length, bits);                    // Record bus width

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
        if (B) One(wb.b(w, i)); else Zero(wb.b(w, i));                          // Set bit
       }
     }
    return wb;
   }

  Words inputWords(String name, int words, int bits)                            // Create an B<input> bus made of words.
   {final Words wb = new WordBus(name, words, bits);
    for   (int w = 1; w <= words; ++w)                                          // Each word on the bus
      for (int b = 1; b <= bits;  ++b) Input(wb.b(w, b));                       // Each word on the bus

    return wb;
   }

  Words outputWords(String name, Words wb)                                      // Create an B<output> bus made of words.
   {final Words o = new WordBus(name, wb);                                      // Record bus width
    for   (int w = 1; w <= wb.words(); ++w)                                     // Each word on the bus
     {for (int b = 1; b <= wb.bits (); ++b) Output(o.b(w, b), wb.b(w, b));      // Each bit on each word on the bus
     }
    return o;
   }

  Words notWords(String name, Words wb)                                         // Create a B<not> bus made of words.
   {final Words o = new WordBus(name, wb);                                      // Record bus width
    for   (int w = 1; w <= wb.words(); ++w)                                     // Each word on the bus
     {for (int b = 1; b <= wb.bits ();  ++b) Not(o.b(w, b), wb.b(w, b));        // Each bit of eacvh word on the bus
     }
    return o;
   }

  Bits andWordsX(String name, Words wb)                                         // Create a bit bus of width equal to the number of words in a word bus by and-ing the bits in each word to make the bits of the resulting word.
   {final Bits B = bits(name, wb.words());                                      // One bit for each word
    for   (int w = 1; w <= wb.words(); ++w)                                     // Each word on the bus
     {final Bit[]bits = new Bit[wb.bits()];                                     // Select bits
      for (int b = 1; b <= wb.bits(); ++b) bits[b-1] = wb.b(w, b);              // Bits to and
      And(B.b(w), bits);                                                        // And bits
     }
    return B;
   }

  Bits andWords(String name, Words wb)                                          // Create a bit bus of the same width as each word in a word bus by and-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final Bits B = bits(name, wb.bits());                                       // One bit for each word
    for   (int b = 1; b <= wb.bits();  ++b)                                     // Each bit in the words on the bus
     {final Bit[]words = new Bit[wb.words()];                                   // Select bits
      for (int w = 1; w <= wb.words(); ++w) words[w-1] = wb.b(w, b);            // The current bit in each word in the bus
      And(B.b(b), words);                                                       // Combine inputs using B<and> gates
     }
    return B;
   }

  Bits orWordsX(String name, Words wb)                                          // Create a bit bus of width equal to the number of words in a word bus by or-ing the bits in each word to make the bits of the resulting word.
   {final Bits B = bits(name, wb.words());                                      // One bit for each word
    for   (int w = 1; w <= wb.words(); ++w)                                     // Each word on the bus
     {final Bit[]bits = new Bit[wb.bits()];                                     // Select bits
      for (int b = 1; b <= wb.bits(); ++b) bits[b-1] = wb.b(w, b);              // Bits to or
      Or(B.b(w), bits);                                                         // Or bits
     }
    return B;
   }

  Bits orWords(String name, Words wb)                                           // Create a bit bus of the same width as each word in a word bus by or-ing corresponding bits in each word to make the corresponding bit in the output word.
   {final Bits B = bits(name, wb.bits());                                       // One bit for each word
    for   (int b = 1; b <= wb.bits();  ++b)                                     // Each bit in the words on the bus
     {final Bit[]words = new Bit[wb.words()];                                   // Select bits
      for (int w = 1; w <= wb.words(); ++w) words[w-1] = wb.b(w, b);            // Each word on the bus
      Or(B.b(b), words);                                                        // Combine inputs using B<or> gates
     }
    return B;
   }

//D2 Comparisons                                                                // Compare unsigned binary integers of specified bit widths.

  Bit compareEq(String output, Bits a, Bits b)                                  // Compare two unsigned binary integers of a specified width returning B<1> if they are equal else B<0>.  Each integer is supplied as a bus.
   {final int A = a.bits(), B = b.bits();                                       // Widths of buses
    a.sameSize(b);                                                              // Check buses match in size
    final Bits eq = bits(n(output, "equal"), A);                                // Compare each pair of bits
    for (int i = 1; i <= B; i++) Nxor(eq.b(i), a.b(i), b.b(i));                 // Test each bit pair for equality
    return andBits(output, eq);                                                 // All bits must be equal
   }

  Bit compareEq(String output, Bits a, int b)                                   // Compare an unsigned binary integer to a constant
   {final int  A = a.bits();                                                    // Width of bus
    final Bits B = bits(n(output, "b"), A, b);                                  // Create target of comparison.
    return compareEq(output, a, B);                                             // Compare
   }

  Bit compareGt(CharSequence output, Bits a, Bits b)                            // Compare two unsigned binary integers for greater than.
   {final int A = a.bits(), B = b.bits();                                       // Widths of buses
    a.sameSize(b);
    final String out = output.toString();
    final Bits oe = bits(n(out, "equal"),   A);                                 // Bits equal in input buses
    final Bits og = bits(n(out, "greater"), A);                                 // Bits greater than
    final Bits oc = bits(n(out, "compare"), A);                                 // Bit greater than with all other bits equal

    for (int i = 2; i <= B; i++) Nxor(oe.b(i), a.b(i), b.b(i));                 // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Gt  (og.b(i), a.b(i), b.b(i));                 // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
     {final Bit[]and = new Bit[B+2-j];
      and[0] = og.b(j-1);
      for (int i = j; i <= B; i++) and[i+1-j] = oe.b(i);
      And(oc.b(j), and);
     }

    final Bit[]or = new Bit[B]; or[0] = og.b(B);
    for (int i = 2; i <= B; i++) or[i-1] = oc.b(i);                             // Equals  followed by greater than
    return Or(output, or);                                                      // Any set bit indicates that first is greater then second
   }

  Bit compareLt(CharSequence output, Bits a, Bits b)                            // Compare two unsigned binary integers for less than.
   {final int A = a.bits(), B = b.bits();
    a.sameSize(b);

    final String out = output.toString();
    final Bits oe = bits(n(out, "equal"),   A-1);                               // Bits equal in input buses
    final Bits ol = bits(n(out, "less"),    A);                                 // Bits less than
    final Bits oc = bits(n(out, "compare"), A-1);                               // Bit less than with all other bits equal

    for (int i = 2; i <= B; i++) Nxor(oe.b(i-1), a.b(i), b.b(i));               // Test all but the lowest bit pair for equality
    for (int i = 1; i <= B; i++) Lt  (ol.b(i),   a.b(i), b.b(i));               // Test each bit pair for more than

    for (int j = 2; j <= B; ++j)                                                // More than on one bit and all preceding bits are equal
     {final Bit[]and = new Bit[B+2-j]; and[0] = ol.b(j-1);
      for (int i = j; i <= B; i++) and[i+1-j] = oe.b(i-1);
      And(oc.b(j-1), and);
     }

    final Bit[]or = new Bit[B];  or[0]   = ol.b(B);
    for (int i = 2; i <= B; i++) or[i-1] = oc.b(i-1);                           // Equals followed by less than

    return Or(output, or);                                                      // Any set bit indicates that first is less than second
   }

  Bits findGt(String output, Words w, Bits b)                                   // Bits indicating which words are greater than the specified word
   {final int bb = b.bits(), ww = w.words(), wb = w.bits();
    if (wb != bb) stop("Each word has", wb, "bits,",
      "but the search key has", bb);
    final Bits m = new BitBus(output, ww);
    for (int i = 1; i <= ww; i++) compareGt(m.b(i), w.w(i), b);
    return m;
   }

  Bits findLt(String output, Words w, Bits b)                                   // Bits indicating which words are less than the specified word
   {final int bb = b.bits(), ww = w.words(), wb = w.bits();
    if (wb != bb) stop("Each word has", wb, "bits,",
      "but the search key has", bb);
    final Bits m = new BitBus(output, ww);
    for (int i = 1; i <= ww; i++) compareLt(m.b(i), w.w(i), b);
    return m;
   }

//D2 Choices                                                                    // Choose one word or another

  Bits chooseFromTwoWords(String output, Bits a, Bits b, Bit choose)            // Choose one of two words depending on a choice bit.  The first word is chosen if the bit is B<0> otherwise the second word is chosen.
   {final int A = a.bits(), B = b.bits();
    if (A != B) stop("First input bus", a, "has width", A,
                     ", but second input bus", b, "has width", B);

    final Bits           o = bits(output, B);                                   // Output bus
    final String notChoose = n(output, "NotChoose");                            // Opposite of choice
    final Bit            n = Not(notChoose, choose);                            // Invert choice

    final Bits oa = bits(n(output, "a"), A);                                    // Choose first word
    final Bits ob = bits(n(output, "b"), A);                                    // Choose second word

    for (int i = 1; i <= B; i++)                                                // Each bit
     {final Bit ga = And(oa.b(i), a.b(i), n);                                   // Choose first word if not choice
      final Bit gb = And(ob.b(i), b.b(i), choose);                              // Choose second word if choice
      Or (o.b(i), ga, gb);                                                      // Or results of choice
     }
    return o;                                                                   // Record bus size
   }

  Bits chooseThenElseIfEQ(String output, Bits Then, Bits Else, Bits a, Bits b)  // Choose "then" if bits a and b are equal else "else"
   {final int T = Then.bits(), E = Else.bits(), A = a.bits(), B = b.bits();
    Then.sameSize(Else);
    a.sameSize(b);

    final Bit eq = compareEq(n(output, "equals"), a, b);                        // Compare and b
    return chooseFromTwoWords (output, Else, Then, eq);                         // Choose between else and then based on result of comparison
   }

  Bits chooseThenElseIfEQ(String output, Bits Then, Bits Else, Bits A, int B)   // Choose "then" if bits a equal b else "else"
   {final Bits b = bits(n(output, "bAsBits"), A.bits(), B);                     // Convert argument b to bits
    return chooseThenElseIfEQ(output, Then, Else, A, b);
   }

  Bits enableWord(String output, Bits a, Bit enable)                            // Output a word or zeros depending on a choice bit.  The word is chosen if the choice bit is B<1> otherwise all zeroes are chosen.
   {final int A = a.bits();
    final Bits o = bits(output, A);                                             // Record bus size
    for (int i = 1; i <= A; i++) And(o.b(i), a.b(i), enable);                   // Choose each bit of input word
    return o;
   }

  record EnableWord                                                             // A value to be chosen by an index
   (Bits value,
    Bits index
   ) {}

  EnableWord enableWord(Bits value, Bits index)                                 // A value to be chosen by an index
   {return new EnableWord(value, index);
   }

  Bits enableWord(String output, Bits choose, EnableWord...enableWords)         // Choose a word by its index
   {final int W = enableWords.length;
    if (W == 0) stop("One or more words to choose from needed");
    final int I = choose.bits(), N = enableWords[0].value.bits();
    final Words q = new WordBus(n(output, "equals"), W, N);                     // Resulting word is the same width as the values of the choices
    for (int j = 1; j <= W; j++)                                                // Each possible world
     {final EnableWord e = enableWords[j-1];
      final int        b = e.value.bits();
      final int        i = e.index.bits();
      if (b != N) stop("Word[0] value has", N, "bits, but word["+j+"] value has", b);
      if (i != I) stop("Word[0] index has", I, "bits, but word["+j+"] index has", i);
      final Bit ie = compareEq(n(j, output, "eqIndex"), choose, e.index);       // Compare with each index
      enableWord(q.w(j).name(), e.value, ie);                                   // Could it be this word
     }
    return orWords(output, q);                                                  // And together the enabled words
   }

  Bits enableWordIfEq(String output, Bits result, Bits a, Bits b)               // Set the output to result if a == b else 0
   {final int  B = result.bits();
    final Bits e = new BitBus(n(output), B);                                    // Enabled word
    final Bit  q = compareEq(n(output, "eq"), a, b);                            // Compare
    return enableWord(output, result, q);                                       // Enable the result if a equals b
   }

//D2 Masks                                                                      // Point masks and monotone masks. A point mask has a single bit set to true, the rest are set to false.  The true bit indicates the point at which something is to happen.

//D3 Monotone masks                                                             // A monotone up mask is any bit string whose bits can be sorted into ascending order (false, true) without being changed.  A monotone down mask is one where sorting with sort order (true, false) has no effect.

  class UpMask                                                                  // Monotone up mask
   {final Bits bits;                                                            // Source bits for monotone up mask
    UpMask(Bits Bits)         {bits = Bits;}                                    // Make a monotone up mask from bits
    public String name()      {return bits.name();}                             // Name of mask
    public int    bits()      {return bits.bits();}                             // Number of bits of bus - the width of the bus
    public Bit       b(int i) {return bits.b   (i);}                            // Number of bits of bus - the width of the bus
    public void anneal()      {bits.anneal();}                                  // Anneal this bit bus so that the annealed gates are not reported as drivign anythinmg.  Such gates hsould be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.
   }

  class DownMask                                                                // Monotone down mask
   {final Bits bits;                                                            // Source bits for monotone down mask
    DownMask(Bits Bits)       {bits = Bits;}                                    // Make a monotone down mask from bits
    public String name()      {return bits.name();}                             // Name of mask
    public int    bits()      {return bits.bits();}                             // Number of bits of bus - the width of the bus
    public Bit       b(int i) {return bits.b   (i);}                            // Number of bits of bus - the width of the bus
    public void anneal()      {bits.anneal();}                                  // Anneal this bit bus so that the annealed gates are not reported as drivign anythinmg.  Such gates hsould be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.
   }

//D3 Point masks                                                                // A point mask is the differential of a monotone mask: it has no more then one bit set to true, the rest are set to false.

  class PointMask                                                               // Point mask
   {final Bits bits;                                                            // Source bits for monotone mask
    PointMask(Bits Bits)      {bits = Bits;}                                    // Make a monotone mask from bits
    public String name()      {return bits.name();}                             // Name of mask
    public int    bits()      {return bits.bits();}                             // Number of bits of bus - the width of the bus
    public Bit       b(int i) {return bits.b   (i);}                            // Number of bits of bus - the width of the bus
    public void anneal()      {bits.anneal();}                                  // Anneal this bit bus so that the annealed gates are not reported as drivign anythinmg.  Such gates hsould be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.
   }

  PointMask upMaskToPointMask(String output, UpMask input)                      // Convert a monotone mask B<i> to a point mask B<o> representing the location in the mask of the first bit set to B<1>. If the monotone mask is all B<0>s then point mask is too.
   {final int B = input.bits();                                                 // Number of bits in input monotone mask

    final Bits o = bits(output, B);                                             // Size of resulting bus representing the chosen integer
    for (int i = 1; i <= B;  i++)                                               // Each bit in each possible output number
      if (i > 1) Lt(o.b(i), input.b(i-1), input.b(i));                          // Look for a step from 0 to 1
      else Continue(o.b(i),               input.b(i));                          // First bit is 1 so point is in the first bit

    return new PointMask(o);
   }

  Bits chooseWordUnderMask(String output, Words input, PointMask mask)          // Choose one of a specified number of words B<w>, each of a specified width, using a point mask B<m> placing the selected word in B<o>.  If no word is selected then B<o> will be zero.
   {final Words wb = input;
    final Bits   o = bits(output, wb.bits());
    final int   mi = mask.bits();
    if (mi != wb.words())
      stop("Mask width", mi, "does not match number of words ", wb.words());

    final Words a = new WordBus(n(output, "a"), wb);
    for   (int w = 1; w <= wb.words(); ++w)                                     // And each bit of each word with the mask
      for (int b = 1; b <= wb.bits (); ++b)                                     // Bits in each word
        And(a.b(w, b), mask.b(w), input.b(w, b));

    for   (int b = 1; b <= wb.bits(); ++b)                                      // Bits in each word
     {final Bit[]or = new Bit[wb.words()];
      for (int w = 1; w <= wb.words(); ++w) or[w-1] = a.b(w, b);                // And each bit of each word with the mask
      Or(o.b(b), or);
     }
    return o;
   }

  Words insertIntoArray(String Output,                                          // Shift the words selected by the monotone mask up one position.
    Words Input, UpMask Mask, Bits Insert)
   {final int words = Input.words(), bits = Input.bits();
    if (bits  != Insert.bits())
     {stop("Insert is", Insert.bits(), "bits, but the input words are",
            bits, "wide");
     }
    if (words != Mask.bits())
     {stop("Mask has", Mask.bits(), "bits to select", words, "words");
     }

    final PointMask P = upMaskToPointMask(n(Output, "pm"), Mask);               // Make a point mask from the input monotone mask
    final Bits  N = notBits                (n(Output, "np"), Mask.bits);        // Invert the monotone mask

    final Words u = new WordBus(n(Output, "upper"),  words-1, bits);            // Shifted words  to fill upper part
    final Words l = new WordBus(n(Output, "lower"),  words,   bits);            // Un-shifted words to fill lower part
    final Words o = new WordBus(  Output,            Input);                    // Resulting array of shifted words
    final Words I = new WordBus(n(Output, "Insert"), Input);                    // The first word - insertion

    for (int b = 1; b <= bits;  ++b)                                            // Bits in each word in shift area
     {And(l.b(1, b), Input.b(1, b), N.b(1));                                    // Select lower words
      And(I.b(1, b), Insert.b(b),   P.b(1));                                    // Select lower words
      Or (o.b(1, b), l.b(1, b),     I.b(1, b));                                 // First word of output is the corresponding input word or inserted word depending on the mask
     }

    for   (int w = 2; w <= words; ++w)                                          // Words in upper shifted area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(u.b(w-1, b), Input.b(w-1, b), Mask.b(w-1));                         // Shifted upper bits

    for   (int w = 2; w <= words; ++w)                                          // Words in shift area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
       {And(l.b(w, b), Input.b(w,   b), N.b(w));                                // Un-shifted lower bits
        And(I.b(w, b), Insert.b(b),     P.b(w));                                // Select lower words
        Or (o.b(w, b), u.b(w-1, b),     l.b(w, b), I.b(w, b));                  // Combine un-shifted, insert, shifted
       }
    return o;                                                                   // Copy of input with word inserted at the indicated position
   }

  record RemoveFromArray                                                        // Results of removing a word from a word bus
   (Words    out,                                                               // Resulting word bus with indicated word removed and a new value shifted in at the high end
    Bits removed                                                                // The removed word
   ){}

  RemoveFromArray removeFromArray(String Output,                                // Remove a word from an array, slide the array down to cover the gap and insert a new word at the top to cover the resulting gap there.
    Words Input, UpMask Mask, Bits Insert)
   {final int words = Input.words(), bits = Input.bits();
    if (bits      != Insert.bits())
     {stop("Insert is", Insert.bits(), "bits, but the input words are",
            bits, "wide");
     }
    if (Mask.bits() != words)
     {stop("Mask has", Mask.bits(), "bits to select", words, "words");
     }

    final PointMask P = upMaskToPointMask(n(Output, "pm"), Mask);               // Make a point mask from the input monotone mask          0100
    final Bits  p = notBits              (n(Output, "ip"), P.bits);             // Invert the point mask                                   1011
    final Bits  M = andBits              (n(Output, "sm"), Mask.bits, p);       // This is the original monotone mask minus its first bit  1000
    final Bits  N = notBits              (n(Output, "np"), Mask.bits);          // Invert the monotone mask                                0011

    final Words u = new WordBus(n(Output, "upper"),  words-1, bits);            // Shifted words   to fill upper part
    final Words l = new WordBus(n(Output, "lower"),  words,   bits);            // Un-shifted words to fill lower part
    final Words o = new WordBus(  Output,            Input);                    // Resulting array of shifted words
    final Words S = new WordBus(n(Output, "select"), Input);                    // Select the word to remove

    for   (int w = 1; w <  words; ++w)                                          // Words in upper shifted area
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(u.b(w, b), Input.b(w+1, b), M.b(w+1));                              // Shifted upper bits

    final Bit  nz = orBits(n(Output, "zm"), Mask.bits);                         // True if the mask is not all zeros
    final Bits  U = chooseFromTwoWords(n(Output, "U"),                          // If the mask is zero we want the output to equal the input
                        Input.w(words), Insert, nz);
    connect(o.w(words), U);                                                     // Inserted word if non zero mask else no change on input

    for   (int w = 1; w <  words; ++w)                                          // Combine shifted upper and lower
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word
       {And(l.b(w, b), Input.b(w, b), N.b(w));                                  // Lower words
        Or (o.b(w, b),     u.b(w, b), l.b(w, b));                               // Combine shifted upper and lower
       }

    for   (int w = 1; w <=  words; ++w)                                         // Removed word
      for (int b = 1; b <= bits;  ++b)                                          // Bits in each word in shift area
        And(S.b(w, b), Input.b(w,   b), P.b(w));                                // Select removed word

    N.b(words).anneal();                                                        // No selection required here because the final word is added regardless
    M.b(words).anneal();                                                        // No selection required here because the final word is added regardless
    M.b(1).anneal();                                                            // No selection required here because the final word is added regardless
    final Bits R = orWords(n(Output, "remove"), S);                             // Removed word
    return new RemoveFromArray(o, R);                                           // Resulting word bus and removed word
   }

//D2 Registers                                                                  // Create registers

  class Monitor                                                                 // Monitor the load signals for a register to ensure that they remain viable at all times
   {final String name;                                                          // Name of Monitor
    final Bit [] bits;                                                          // Bits to monitor
    final Boolean[]B;                                                           // Snapshot of the current bit values

    Monitor(String Name, Bit[]Bits)
     {name = Name; bits = Bits;
      monitors.put(name, this);
      B = new Boolean[bits.length];
     }

    void check()                                                                // Check load signals are viable
     {for (int i = 0; i < bits.length; i++)                                     // Capture each load signal state
       {final Bit  b = bits[i];
        final Gate g = getGate(b);
        B[i] = g.value;
       }
      int h = 0, l = 0, n = 0;
      for (int i = 0; i < bits.length; i++)                                     // Signal statistics
        if (B[i] == null) ++n; else if (B[i]) ++h; else ++l;
      if (h == 1) return;                                                       // Load signals are combined with 'or' so if one signal is high we have a definte result and no clash with any other load signal.
      if (h == 0 && n == 0) return;                                             // All load signals are low so we have a definite result and no clashes
      final StringBuilder s = new StringBuilder();                              // Report signals in error
      for (int i = 0; i < bits.length; i++)
       {if      (B[i] == null ) s.append(""+i+" => null");
        else if (B[i])          s.append(""+i+" => 1");
       }
      say("Monitor", name, "has", h, "1 bits and", n, "null bits:", s);
     }
   }

  Monitor monitor(String name, Bit...bits) {return new Monitor(name, bits);}    // Create a new monitor

  class Register extends Bits                                                   // Description of a register
   {final Bits    reg;                                                          // The bit bus produced by the register

    Register(String Output, Bits Input, Pulse Load)                             // Load register from input on falling edge of load signal.
     {super(Output, Input.bits());
      final int B = Input.bits();                                               // Number of bits in register
      reg         = Chip.this.bits(Output, B);                                  // Bit bus created by register
      for (int i  = 1; i <= B; i++) My(n(i, Output), Load, Input.b(i));         // Create the memory bits
     }
   }

  Register register(String name, Bits input, Pulse load)                        // Create a register loaded from one source
   {return new Register(name, input, load);
   }

//D2 Pulses                                                                     // Timing signals. At one point I thought that it should be possible to have one pulse trigger ither pulses and to route pulses so that complex for loop and if structures could be realize. But this adds a lot of complexity and unreliability.  Eventually I concluded that it would be better to only allow external pulses at the gate layer making it possible for higher levels to perform more complex control sequences through software rather than hardware.

 class Pulse extends Gate                                                       // A periodic pulse that drives an input bit.
   {final int  period;                                                          // Length of pulse. A pulse of zero is considered to be a pulse of infinite period - i.e. it only happens once. a pulse of lebgth 1 is not much use either as it will be always either on or off.
    final int      on;                                                          // How long the pulse is in the on state in each period in simulations steps
    final int   delay;                                                          // Offset of the on phase of the pulse in simulations steps
    final int   start;                                                          // Number of periods to wait before starting this pulse.
    Pulse(String Name, int Period, int On, int Delay, int Start)                // Pulse definition as an input gate. Start n means start after n periods.
     {super(Operator.Input, Name, null, null);                                  // Pulse name
      setSystemGate();                                                          // The input gate associated with this pulse. The gate will be driven by the simulator.
      period = Period; on = On; delay = Delay; start = Start;                   // Pulse timing details
      if (period  > 0)                                                          // Zero length pulses are considered to be infinite in length and thus occur only once
       {if (on    > period) stop("On", on, "is greater than period", period);
        if (delay > period) stop("Delay", delay, "is greater than period", period);
        if (on + delay > period) stop("On + Delay", on, "+", delay, "is greater than period", period);
       }
      else if (period == 1) stop("Use One or Zero instead");                    // Constant pulses are deprecated
      if (on    < 1) stop("On",    "must be positive, not", on);                // Pulse must be on some of the time
      if (delay < 0) stop("Delay", "cannot be negative:",   delay);             // Delay time must be positive
      if (start < 0) stop("Start", "cannot be negative",    start);             // Start cycle must zero or more
      pulses.put(name, this);                                                   // Save pulse on input chain
     }
    void setState()                                                             // Set gate implementing pulse to current state.
     {final int s = steps - 1;                                                  // Steps taken
      final int i = period > 0 ? s % period : s;                                // Position in period
      nextValue = i >= start*period ? i >= delay && i < on+delay : false;       // Next value for pulse.  Set like this so that the falling edge will be seen and acted on
     }
   }

  Pulse pulse(String Name, int Period, int On, int Delay, int Start)            // Create an external pulse
   {return new Pulse(Name, Period, On, Delay, Start);
   }

  Pulse pulse(String Name, int Period, int On, int Delay)                       // Create an external pulse that starts immediately
   {return new Pulse(Name, Period, On, Delay, 0);
   }

  Pulse pulse(String Name, int Period, int On)                                  // Create an external  pulse with no delay
   {return new Pulse(Name, Period, On, 0, 0);
   }

  Pulse pulse(String Name, int Period)                                          // Create an external  pulse with no delay
   {return new Pulse(Name, Period, 1, 0, 0);
   }

  class PulseBuilder                                                            // Build a pulse specification
   {String name;
    int period = 0;                                                             // Default to one time only
    int on     = 1;                                                             // On for one step
    int delay  = 0;                                                             // No delay
    int start  = 0;                                                             // Start on the first cycle
    PulseBuilder name  (String Name) {name   = Name  ; return this;}
    PulseBuilder period(int  Period) {period = Period; return this;}
    PulseBuilder on    (int      On) {on     = On    ; return this;}
    PulseBuilder delay (int   Delay) {delay  = Delay ; return this;}
    PulseBuilder start (int   Start) {start  = Start ; return this;}
    Pulse build()
     {return new Pulse(name, period, on, delay, start);
     }
   }

  PulseBuilder pulseBuilder(String name)                                        // Create a new pulse builder with default values
   {return new PulseBuilder().name(name);
   }

//D2 Arithmetic Base 1                                                          // Arithmetic in base 1

  class Shift extends Bits                                                      // Shift bits one place up or down filling with a specified value
   {final String output;                                                        // Name of the shifted bits
    final Bits   source;                                                        // Source of the bits to shift
    final boolean    up;                                                        // Shift up if true else down
    final boolean  fill;                                                        // Value to fill emptied positions with
    final Gate fillGate;                                                        // The gate providing the fill value

    Shift(String Output, Bits Source, boolean Up,  boolean Fill)
     {super(Output, Source.bits());
      output   = Output; source = Source; up = Up; fill = Fill;
      fillGate = fill ? One(n(output, "one")) : Zero(n(output, "zero"));        // The fill bit
     }

    public String name() {return name;}                                         // Name of bus
    public int    bits() {return source.bits();}                                // Number of bits of bus - the width of the bus

    public Bit b(int i)                                                         // Name of an individual bit in the shifted bus
     {if ((up && i == 1) || (!up && i == source.bits())) return fillGate;
      return source.b(i + (up ? -1 : +1));
     }

    public void  anneal() {outputBits(n(name, "anneal"), this);}                // Anneal this bit bus so that the annealed gates are not reported as driving anything.  Such gates should be avoided in real chips as they waste surface area and power while doing nothing, but anneal often simplifies testing by allowing us to ignore such gates for the duration of the test.
   }

  Bits shiftUp(String output, Bits input, boolean fill)                         // Shift an input bus up one place to add 1 in base 1 or multiply by two in base 2.
   {return new Shift(output, input, true, fill);
   }

  Bits shiftUp(String output, Bits input)                                       // Shift an input bus up one place inserting zero to add 1 in base 1 or multiply by two in base 2
   {return shiftUp(output, input, false);
   }

  Bits shiftUpOne(String output, Bits input)                                    // Shift an input bus up one place inserting one
   {return shiftUp(output, input, true);
   }

  Bits shiftDown(String output, Bits input)                                     // Shift an input bus down one place to subtract 1 in base 1 or divide by two in base 2
   {return new Shift(output, input, false, false);
   }

//D2 Arithmetic Base 2                                                          // Arithmetic in base 2

  record BinaryAdd                                                              // Results of a binary add
   (Bit carry,                                                                  // Carry out gate
    Bits  sum                                                                   // Sum
   )
   {public Bit  carry() {return carry;}
    public Bits sum  () {return sum;}
   }

  BinaryAdd binaryAdd(String output, Bits in1, Bits in2)                        // Add two bit buses of the same size to make a bit bus one bit wider
   {final int b = in1.bits();                                                   // Number of bits in input monotone mask
    final int B = in2.bits();                                                   // Number of bits in input monotone mask
    if (b != B) stop("Input bit buses must have the same size, not", b, B);     // Check sizes match
    final Bits o = bits   (  output,               b);                          // Result bits
    final Bits c = bits   (n(output, "carry"),     b);                          // Carry bits
    final Bits C = notBits(n(output, "not_carry"), c);                          // Not of carry bits
    final Bits n = notBits(n(output, "not_in1"), in1);                          // Not of input 1
    final Bits N = notBits(n(output, "not_in2"), in2);                          // Not of input 2
    Xor(o.b(1), in1.b(1), in2.b(1));                                            // Low order bit has no carry in
    And(c.b(1), in1.b(1), in2.b(1));                                            // Low order bit carry out
    n.b(1).anneal(); N.b(1).anneal(); C.b(b).anneal();                          // These bits are not needed, but get defined, so we anneal them off to prevent error messages
// #  c 1 2  R C
// 1  0 0 0  0 0
// 2  0 0 1  1 0                                                                // We only need 1 bits so do not define a full bus as we would have to anneal a lot of unused gates which would be wasteful.
// 3  0 1 0  1 0
// 4  0 1 1  0 1
// 5  1 0 0  1 0
// 6  1 0 1  0 1
// 7  1 1 0  0 1
// 8  1 1 1  1 1
    final String R = n(output, "result"), K = n(output, "carry");               // Result bit bus name
    final Bits i = in1;                                                         // Input 1
    final Bits I = in2;                                                         // Input 2
    for (int j = 2; j <= b; j++)                                                // Create the remaining bits of the shifted result
     {Gate r2 = And(n(j, 2, R), C.b(j-1), n.b(j), I.b(j));                      // Result
      Gate r3 = And(n(j, 3, R), C.b(j-1), i.b(j), N.b(j));
      Gate r5 = And(n(j, 5, R), c.b(j-1), n.b(j), N.b(j));
      Gate r8 = And(n(j, 8, R), c.b(j-1), i.b(j), I.b(j));
      Or(o.b(j), r2, r3, r5, r8);

      Gate c4 = And(n(j, 4, K), C.b(j-1), i.b(j), I.b(j));                      // Carry
      Gate c6 = And(n(j, 6, K), c.b(j-1), n.b(j), I.b(j));
      Gate c7 = And(n(j, 7, K), c.b(j-1), i.b(j), N.b(j));
      Gate c8 = And(n(j, 8, K), c.b(j-1), i.b(j), I.b(j));
      Or(c.b(j), c4, c6, c7, c8);
     }

    return new BinaryAdd(c.b(b), o);                                            // Carry out of the highest bit, result
   }

  Bits binaryTwosComplement(String output, Bits in)                             // Form the binary twos complement of a number
   {final int         B = in.bits();                                            // Number of bits
    final Bits        n =   notBits(n(output, "not"), in);                      // Not of input
    final Bits      one =      bits(n(output, "one"), in.bits(), 1);            // A one of the correct width
    final BinaryAdd add = binaryAdd(n(output), n, one);                         // Add one to form twos complement
    return add.sum;                                                             // Ignore sum
   }

  Bits binaryTCAdd(String output, Bits in1, Bits in2)                           // Twos complement addition
   {final int b = in1.bits();                                                   // Number of bits in input monotone mask
    final int B = in2.bits();                                                   // Number of bits in input monotone mask
    if (b != B) stop("Input bit buses must have the same size, not", b, B);     // Check sizes match
    final BinaryAdd sub = binaryAdd(n(output), in1, in2);                       // Effect the addition
    return sub.sum;
   }

  Bits binaryTCAdd(String output, Bits in1, int in2)                            // Twos complement addition of an immediate value
   {return binaryTCAdd(output, in1, bits(n(output, "in2"), in1.bits(), in2));
   }

  Bits binaryTCSubtract(String output, Bits in1, Bits in2)                      // Twos complement subtraction
   {final int b = in1.bits();                                                   // Number of bits in input monotone mask
    final int B = in2.bits();                                                   // Number of bits in input monotone mask
    if (b != B) stop("Input bit buses must have the same size, not", b, B);     // Check sizes match
    final Bits        m = binaryTwosComplement(n(output, "subtract"), in2);     // Twos complement of subtrahend
    final BinaryAdd sub = binaryAdd(n(output), in1, m);                         // Effect the subtraction
    return sub.sum;
   }

  Bits binaryTCSubtract(String output, Bits in1, int in2)                       // Twos complement subtraction of an immediate value
   {return binaryTCSubtract(output, in1, bits(n(output, "in2"),in1.bits(),in2));
   }

// #   1 2 c   R
// 1   0 0 0   0
// 2   0 0 1   1
// 3   0 1 0   0
// 4   0 1 1   0
// 5   1 0 0   1
// 6   1 0 1   1
// 7   1 1 0   0
// 8   1 1 1   1

  Bit binaryTCCompareLt(String output, Bits in1, Bits in2)                      // Twos complement less than comparison
   {final int b = in1.bits();                                                   // Number of bits in first input
    final int B = in2.bits();                                                   // Number of bits in second input
    if (b != B) stop("Input bit buses must have the same size, not", b, B);     // Check sizes match
    final Bits i1 = new SubBitBus(n(output, "n1"),  in1, 1, b - 1);             // Numeric part of first number
    final Bit  s1 = in1.b(b);                                                   // Sign of first number
    final Bits i2 = new SubBitBus(n(output, "n2"),  in2, 1, b - 1);             // Numeric part of second number
    final Bit  s2 = in2.b(b);                                                   // Sign of second number
    final Bit   c = compareLt    (n(output, "cmp"), i1, i2);                    // Compare number

    final Bit  S1 = Not(n(output, "notS1"), s1);                                // Invert
    final Bit  S2 = Not(n(output, "notS2"), s2);
    final Bit   C = Not(n(output, "notC"),   c);

    final Bit r2 = And(n(output, "r2"), S1, S2, c);                             // Locate ones
    final Bit r5 = And(n(output, "r5"), s1, S2, C);
    final Bit r6 = And(n(output, "r6"), s1, S2, c);
    final Bit r8 = And(n(output, "r7"), s1, s2, c);
    final Bit  o = Or(output, r2, r5, r6, r8);                                  // True if less than.
    return o;
   }

  Bits shiftLeftMultiple(String output, Bits input, Bits shift)                 // Shift the input bits left by the number of positions specified in shift filling in with zeroes.
   {final int B = input.bits();                                                 // Number of bits in input
    final Bits[]s = new Bits[B+1];                                              // Shifted bits
    final Bits  z = bits(n(output, "zero"), B, 0);                              // Zero
    for (int i = 0; i <= B; i++)                                                // Each possible shift
     {final Bit[]S = new Bit[B];                                                // Bits in possible shift
      for (int j = 0; j < i; ++j) S[j] =     z.b(1+j);                          // Shift in zeros
      for (int j = i; j < B; ++j) S[j] = input.b(1+j - i);                      // Shift in bits
      final Bits t = new ConCatBits(n(i, output, "concat"), S);                 // Concatenate the bits
      final Bits I =   bits(n(i, output, "shift"), shift.bits(), i);            // Select this shift
      s[i] = enableWordIfEq(n(i, output, "enable"), t, shift, I);               // Enable shifted bits
     }
    return orBits(output, s);                                                   // Or together the shifts
   }

  Bits shiftRightMultiple(String output, Bits input, Bits shift)                // Shift the input bits right by the number of positions specified in shift filling in with zeroes.
   {final int B = input.bits();                                                 // Number of bits in input
    final Bits[]s = new Bits[B+1];                                              // Shifted bits
    final Bits  z = bits(n(output, "zero"), B, 0);                              // Zero
    for (int i = 0; i <= B; i++)                                                // Each possible shift
     {final Bit[]S = new Bit[B];                                                // Bits in possible shift
      for (int j = 0; j < i; ++j) S[B-1-j] = z.b(1+j);                          // Shift in zeros
      for (int j = i; j < B; ++j) S[B-1-j] = input.b(B-j+i);                    // Shift in bits
      final Bits t = new ConCatBits(n(i, output, "concat"), S);                 // Concatenate the bits
      final Bits I =   bits(n(i, output, "shift"), shift.bits(), i);            // Select this shift
      s[i] = enableWordIfEq(n(i, output, "enable"), t, shift, I);               // Enable shifted bits
     }
    return orBits(output, s);                                                   // Or together the shifts
   }

  Bits shiftRightArithmetic(String output, Bits input, Bits shift)              // Shift the input bits right by the number of positions specified in shift filling in the highest bit
   {final int B = input.bits();                                                 // Number of bits in input
    final Bits[]s = new Bits[B+1];                                              // Shifted bits
    for (int i = 0; i <= B; i++)                                                // Each possible shift
     {final Bit[]S = new Bit[B];                                                // Bits in possible shift
      for (int j = 0; j < i; ++j) S[B-1-j] = input.b(B);                        // Shift in highest bit
      for (int j = i; j < B; ++j) S[B-1-j] = input.b(B-j+i);                    // Shift in bits
      final Bits t = new ConCatBits(n(i, output, "concat"), S);                 // Concatenate the bits
      final Bits I =   bits(n(i, output, "shift"), shift.bits(), i);            // Select this shift
      s[i] = enableWordIfEq(n(i, output, "enable"), t, shift, I);               // Enable shifted bits
     }
    return orBits(output, s);                                                   // Or together the shifts
   }

  Bits binaryTCSignExtend(String output, Bits input, int width)                 // Extend the sign of a twos complement integer to the specified width
   {final int B = input.bits(), W = width;                                      // Number of bits in input
    final String n = input.name();                                              // Name of input field
    if (B == width) return input;                                               // No need to extend
    if (B >  width) return new SubBitBus(output, input, 1, width);              // Field already wider
    final Bit[]s = new Bit[width];                                              // Result
    for (int i = 1;   i <= B; i++) s[i-1] = input.b(i);                         // Existing bits
    for (int i = B+1; i <= W; i++) s[i-1] = input.b(B);                         // Sign bit
    return new ConCatBits(output, s);                                           // Concatenate the bits
   }

//D2 B-tree                                                                     // Circuits useful in the construction and traversal of B-trees.

  class BtreeNode                                                               // Description of a node in a binary tree
   {final String     Output;                                                    // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
    final int            Id;                                                    // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
    final int             B;                                                    // Width of each word in the node.
    final int             K;                                                    // Number of keys == number of data words each B bits wide in the node.
    final boolean      Leaf;                                                    // Width of each word in the node.
    final Bits       Enable;                                                    // B bit wide bus naming the currently enabled node by its id.
    final Bits         Find;                                                    // B bit wide bus naming the key to be found
    final Words        Keys;                                                    // Keys in this node, an array of N B bit wide words.
    final Words        Data;                                                    // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
    final Words        Next;                                                    // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
    final Bits          Top;                                                    // The top next link making N+1 next links in all.
    final Bits  KeysEnabled;                                                    // One bit for each key showing whether the key is a valid key if true, else an empty slot.  These bits form a monotone mask.
    int        level, index;                                                    // Level and position in  level for this node

    BtreeNode                                                                   // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
     (String        Output,                                                     // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      int               Id,                                                     // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
      int                B,                                                     // Width of each word in the node.
      int                K,                                                     // Number of keys == number of data words each B bits wide in the node.
      boolean         Leaf,                                                     // Width of each word in the node.
      Bits          Enable,                                                     // B bit wide bus naming the currently enabled node by its id.
      Bits            Find,                                                     // B bit wide bus naming the key to be found
      Words           Keys,                                                     // Keys in this node, an array of N B bit wide words.
      Words           Data,                                                     // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
      Words           Next,                                                     // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
      Bits             Top,                                                     // The top next link making N+1 next links in all.
      DownMask KeysEnabled)                                                     // One bit for each key showing whether the key is a valid key if true, else an empty slot if false.
     {if (K     <= 2) stop("The number of keys the node can hold must be greater than 2, not", K);
      if (K % 2 == 0) stop("The number of keys the node can hold must be odd, not even:",      K);
      this.Output = Output;
      this.Id     = Id;     this.B       = B;       this.K       = K; this.Leaf = Leaf;
      this.Enable = Enable; this.Find    = Find;    this.Keys    = Keys;
      this.Data   = Data;   this.Next    = Next;    this.Top     = Top;
      this.KeysEnabled = KeysEnabled.bits;
     }

    record Search                                                               // Results of generating gates to search a node
     (BtreeNode node,                                                           // Node searched
      Bit      found,                                                           // Bit that shows whether the key was found
      Bits      data,                                                           // The data associated with the key of the key was found else zero
      Bits      next                                                            // The id of the next node to search if the key was not found, else zero
     ){}

    Search search()                                                             // Search a Btree node
     {final String   Found = n(Output, "Found");                                // Found the search key
      final String OutData = n(Output, "OutData");                              // Data found
      final String OutNext = n(Output, "OutNext");                              // Next link if not a leaf and not found else zero

      final String id = n(Output, "id");                                        // Id for this node
      final String df = n(Output, "dataFound");                                 // Data found before application of enable
      final String en = n(Output, "enabled");                                   // Whether this node is enabled for searching
      final String f2 = n(Output, "foundBeforeEnable");                         // Whether the key was found or not but before application of enable
      final String me = n(Output, "maskEqual");                                 // Point mask showing key equal to search key
      final String mm = n(Output, "maskMore");                                  // Monotone mask showing keys more than the search key
      final String mv = n(Output, "matchesAndValid");                           // Matches and valid
      final String mf = n(Output, "moreFound");                                 // The next link for the first key greater than the search key if such a key is present int the node
      final String nf = n(Output, "notFound");                                  // True if we did not find the key
      final String nv = n(Output, "nextValid");                                 // Bit bus showing which next links are valid. The valid links point to the node that contain keys less than the corresponding key. If the corresponding key is erqual then the key has been found and the actual next link will be computed as zero.
      final String n2 = n(Output, "nextLink2");
      final String n3 = n(Output, "nextLink3");
      final String n4 = n(Output, "nextLink4");
      final String nm = n(Output, "noMore");                                    // No key in the node is greater than the search key
      final String pm = n(Output, "pointMore");                                 // Point mask showing the first key in the node greater than the search key
      final String pt = n(Output, "pointMoreTop");                              // A single bit that tells us whether the top link is the next link
      final String pn = n(Output, "pointMoreTop_notFound");                     // Top is the next link, but only if the key was not found

      final Bits nodeId = bits     (id, B, Id);                                 // Save id of node
      final Bit  enable = compareEq(en, nodeId, Enable);                        // Check whether this node is enabled

      for (int i = 1; i <= K; i++)
       {compareEq(n(i, me), Keys.w(i), Find);                                   // Compare equal point mask
        if (!Leaf) compareGt(n(i, mm), Keys.w(i), Find);                        // Compare more  monotone mask
       }

      final Bits      matches      = bits(me, K);                               // Equal bit bus for each key
      final PointMask matchesValid = new PointMask(andBitBuses(mv, matches, KeysEnabled)); // Equal bit bus for each valid key

      final Bits selectedData = chooseWordUnderMask(df, Data, matchesValid);    // Choose data under equals mask
      final Bit  keyWasFound  = orBits             (f2,       matchesValid.bits);    // Show whether key was found
      final Bits outData      = enableWord    (OutData, selectedData, enable);  // Enable data found
      final Bit  found        = And             (Found, keyWasFound,  enable);  // Enable found flag

      final Bits      outNext;                                                  // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      if (!Leaf)                                                                // Find next link with which to enable next layer
       {final Bits      Mm = bits                  (mm, K);                     // Monotone mask more for compare greater than on keys so we can find next link
        final UpMask    Nv = new UpMask(andBitBuses(nv, Mm, KeysEnabled));      // Monotone mask more for compare greater than on valid keys
        final Bit       Nm = norBits               (nm, Nv.bits);               // True if the more monotone mask is all zero indicating that all of the keys in the node are less than or equal to the search key
        final PointMask Pm = upMaskToPointMask     (pm, Nv);                    // Convert monotone more mask to point mask
        final Bits      Mf = chooseWordUnderMask   (mf, Next, Pm);              // Choose next link using point mask from the more monotone mask created
        final Bits      N4 = chooseFromTwoWords    (n4, Mf,   Top, Nm);         // Choose top or next link
        final Bit       Pt = norBits               (pt, Pm.bits);               // The top link is the next link
        final Bit       Nf = Not                   (nf, found);                 // Not found
        final Bits      N3 = enableWord            (n3, N4,   Nf);              // Disable next link if we found the key
        final Bit       Pn = And                   (pn, Pt,   Nf);              // Top is the next link, but only if the key was not found
        final Bits      N2 = chooseFromTwoWords    (n2, N3,   Top, Pn);         // Either the next link or the top link
        outNext = enableWord                 (OutNext,  N2,   enable);          // Next link only if this node is enabled
       }
      else outNext = null;                                                      // Not relevant in a leaf

      return new Search(this, found, outData, outNext);                         // Results of search
     }

    record Insert                                                               // Buses created by insert operation
     (Words           keys,                                                     // Keys with new key inserted
      Words           data,                                                     // Data with new data inserted
      Words           next,                                                     // Next links with new link inserted
      DownMask enabledKeys,                                                     // Enabled keys mask
      Bits            iKey,                                                     // Insertion key
      Bits            iData,                                                    // Insertion data
      Bits            iNext                                                     // Insertion next
     ){}

    Insert Insert(String Output, Bits iKey, Bits iData, Bits iNext,             // Insert a new key, data pair in a leaf node at the position shown by the monotone mask.
      UpMask position)
     {final Words    k = insertIntoArray(n(Output, "outKeys"), Keys, position, iKey);
      final Words    d = insertIntoArray(n(Output, "outData"), Data, position, iData);
      final Bits    bv = shiftUpOne     (n(Output, "outKeysEnabled"), KeysEnabled);
      final DownMask v = new DownMask(bv);
      final Words    n = iNext == null ? null :
        insertIntoArray(n(Output, "outNext"), Next, position, iNext);
      return new Insert(k, d, n, v, iKey, iData, iNext);                        // Insertion results
     }

    record Split                                                                // The results of splitting a node
     (BtreeNode parent,                                                         // New version of the parent node one of whose children is being split
      BtreeNode  lower,                                                         // The lower half of the node being split
      BtreeNode  upper,                                                         // New version of the node being split.  This arrangement preserves the top if this node happens to be the top node
      Insert    insert,                                                         // Insertion results
      Bits         pgt,                                                         // Find keys greater than the search key in the parent node
      Bits        pnek,                                                         // Parent keys not enabled
      Bits       pnegt,                                                         // Bits    of parent keys either greater or not enabled
      UpMask        gt                                                          // Up mask of parent keys either greater or not enabled because the keys are contiguous, ordered and start in word one.
     ){}

    Split split(BtreeNode b, int lowerId)                                       // Split the specified child node of this parent node into two sibling nodes (lower, upper).  The upper node retains the id of the original child node  while the lower node takes the supplied id. Returns the new version of the parent, child and sibling.
     {final int k = K / 2;
      final Words    ak = new SubWordBus(n(Output, "aKeys"),  b.Keys, 1,  k);
      final Words    ad = new SubWordBus(n(Output, "aData"),  b.Data, 1,  k);
      final Words    an = new SubWordBus(n(Output, "aNext"),  b.Next, 1,  k);
      final Bits    bav = bits(n(Output, "aKeysEnabled"), K, (1<<k) - 1);       // Keys enabled in lower child
      final DownMask av = new DownMask  (bav);

      final Words    bk = new SubWordBus(n(Output, "bKeys"),  b.Keys, 2+k, k);
      final Words    bd = new SubWordBus(n(Output, "bData"),  b.Data, 2+k, k);
      final Words    bn = new SubWordBus(n(Output, "bNext"),  b.Next, 2+k, k);
      final Bits    bbv = bits(n(Output, "bKeysEnabled"), K, (1<<k) - 1);       // Keys enabled in upper child
      final DownMask bv = new DownMask  (bbv);

      final Bits    pgt = findGt(n(Output,  "pGreater"),  Keys, b.Keys.w(1 + k));// Find keys greater than the search key in the parent node
      final Bits   pnek = notBits(n(Output, "pNeKeys"),   KeysEnabled);         // Parent keys not enabled
      final Bits  pnegt =  orBits(n(Output, "pNeGt"),     pnek, pgt);           // Bits    of parent keys either greater or not enabled
      final UpMask   gt = new UpMask(pnegt);                                    // Up mask of parent keys either greater or not enabled because the keys are contiguous, ordered and start in word one.
      final Insert    i = Insert(n(Output, "inParent"), b.Keys.w(1+k),          // Insert splitting key, data, next in parent as indicated by greater than monotone mask
        b.Data.w(1+k), bits(n(Output, "lowerId"), B, lowerId), gt);

      final BtreeNode np = new BtreeNode(n(Output, "splitP"), Id,      B, K,   Leaf, Enable, Find, i.keys, i.data, i.next, Top, i.enabledKeys); // Top of parent is unchanged because we always split downwards.
      final BtreeNode na = new BtreeNode(n(Output, "splitA"), lowerId, B, K, b.Leaf, Enable, Find, ak,     ad,     an,     b.Next.w(1+k),  av); // Top of lower child is next(1) of upper child
      final BtreeNode nb = new BtreeNode(n(Output, "splitB"), b.Id,    B, K, b.Leaf, Enable, Find, bk,     bd,     bn,     b.Top,          bv); // Top of uypper child is unchanged
      return new Split(np, na, nb, i, pgt, pnek, pnegt, gt);                    // Results of splitting the node
     }

    void anneal()                                                               // Anneal this node during testing of methods other than search.
     {     Enable.anneal();
             Find.anneal();
             Keys.anneal();
             Data.anneal();
             Next.anneal();
              Top.anneal();
      KeysEnabled.anneal();
     }

    static BtreeNode Test                                                       // Create a new B-Tree node. The node is activated only when its preset id appears on its enable bus otherwise it produces zeroes regardless of its inputs.
     (Chip          c,                                                          // Chip to contain node
      String   Output,                                                          // Output name showing results of comparison - specifically a bit that is true if the key was found else false if it were not.
      int          Id,                                                          // A unique unsigned integer B bits wide that identifies this node. Only the currently enabled node does a comparison.
      int           B,                                                          // Width of each word in the node.
      int           N,                                                          // Number of keys == number of data words each B bits wide in the node.
      int      enable,                                                          // B bit wide bus naming the currently enabled node by its id.
      int        find,                                                          // B bit wide bus naming the key to be found
      int[]      keys,                                                          // Keys in this node, an array of N B bit wide words.
      int[]      data,                                                          // Data in this node, an array of N B bit wide words.  Each data word corresponds to one key word.
      int[]      next,                                                          // Next links.  An array of N B bit wide words that represents the next links in non leaf nodes.
      int         top,                                                          // The top next link making N+1 next links in all.
      int keysEnabled)                                                          // Keys that are currently valid: one bit per valid key withe first key in the first bit position
     {if (                N != keys.length) stop("Wrong number of keys, need", N, "got", keys.length);
      if (                N != data.length) stop("Wrong number of data, need", N, "got", data.length);
      if (next != null && N != next.length) stop("Wrong number of next, need", N, "got", next.length);

      final Bits     e = c.bits (n(Output, "e"),  B, enable);
      final Bits     f = c.bits (n(Output, "f"),  B, find);
      final Words    k = c.words(n(Output, "k"),  B, keys);
      final Bits    bK = c.bits (n(Output, "bK"), N, keysEnabled);
      final DownMask K = c.new DownMask(bK);
      final Words    d = c.words(n(Output, "d"),  B, data);
      final Words    n = next != null ? c.words(n(Output, "n"), B, next) : null;
      final Bits     t = next != null ? c.bits (n(Output, "t"), B, top)  : null;
      final boolean leaf = next == null;
      return c.new BtreeNode(Output, Id, B, N, leaf, e, f, k, d, n, t, K);
     }
   }

  class Btree                                                                   // Construct and search a Btree.
   {final int      bits;                                                        // Number of bits in a key, datum, or next link
    final int      keys;                                                        // Number of keys in a node
    final int    levels;                                                        // Number of levels in the tree
    final String output;                                                        // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
    final Bits     find;                                                        //i The search key bus
    final Gate    found;                                                        //o A bit indicating whether the key was found in the tree or not
    final Bits     data;                                                        //o The data corresponding to the search key if the found bit is set, else all zeros
    final TreeMap<Integer, Level>      tree = new TreeMap<>();                  // Levels within the tree
    final TreeMap<Integer, BtreeNode> nodes = new TreeMap<>();                  // Nodes within tree by id number

    class Level
     {final int     level;                                                      // Number of level
      final int         N;                                                      // Number of nodes at this level
      final boolean  root;                                                      // Root node
      final boolean  leaf;                                                      // Final level is made of leaf nodes
      final Bits   enable;                                                      // Node selector for this level
      final TreeMap<Integer, BtreeNode> nodes;                                  // Nodes in this level
      Level(int l, int n, boolean Root, boolean Leaf, Bits Enable)              // Create a level description
       {nodes = new TreeMap<>();
        level = l; N = n; root = Root; leaf = Leaf; enable = Enable;
        tree.put(l, this);                                                      // Record details of the level to enable debugging
       }
     }

    Btree                                                                       // Construct a Btree.
     (String output,                                                            // The name of this tree. This name will be prepended to generate the names of the gates used to construct this tree.
      Bits     find,                                                            //i The search key bus
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

      if (find.bits() != bits)                                                  // Check the key to be found has the same width as the keys to be searched
        stop("Find bus must be", bits, "wide, not", find.bits());

      Bits eI = null;                                                           // For the moment we always enable the root node of the tree
      final Bits    bke = bits(n(output, "keysEnabled"), keys, (1<<keys)-1);    // All keys enabled
      final DownMask ke = new DownMask(bke);                                    // All keys enabled

      final Bit []pF = new Bit [levels];                                        // Consolidate the results over all levels
      final Bits[]pD = new Bits[levels];
      final Bits[]pN = new Bits[levels];

      for (int l = 1; l <= levels; l++)                                         // Each level in the bTree
       {final int         N = powerOf(keys+1, l-1);                             // Number of nodes at this level
        final boolean  root = l == 1;                                           // Root node
        final boolean  leaf = l == levels;                                      // Final level is made of leaf nodes
        final Level   level = new Level(l, N, root, leaf, eI);                  // Details of the level

        final Bit []oF = new Bit [N];                                           // Consolidate the results of each node on this level
        final Bits[]oD = new Bits[N];
        final Bits[]oN = new Bits[N];

        for (int n = 1; n <= N; n++)                                            // Each node at this level
         {++nodeId;                                                             // Number of this node
          if (eI == null) eI = bits(n(0, ln), bits, nodeId);                    // For the moment we always enable the root node of the tree
          final Words  iK = inputWords(n(l, n, ik), keys, bits);                // Bus of input words representing the keys in this node
          final Words  iD = inputWords(n(l, n, id), keys, bits);                // Bus of input words representing the data in this node
          final Words  iN = leaf ? null : inputWords(n(l, n, in), keys, bits);  // Bus of input words representing the next links in this node
          final Bits   iT = leaf ? null : inputBits (n(l, n, it), bits);        // Bus representing the top next link

          final BtreeNode node = new BtreeNode(n(l, n, output, "node"),         // Create the node
            nodeId, bits, keys, leaf, eI, find, iK, iD, iN, iT, ke);

          final BtreeNode.Search search = node.search();                        // Create silicon to search the node

          oF[n-1] = search.found;
          oD[n-1] = search.data;
          oN[n-1] = search.next;

          level.nodes.put(n, search.node);                                      // Add the node to this level
          nodes.put (nodeId, search.node);                                      // Index the node
          node.level = l; node.index = n;                                       // Position of node in tree
         }

        if (root)                                                               // Found and data for root
         {pF[l-1] = oF[0];
          pD[l-1] = oD[0];
         }
        else                                                                    // Or the data elements together for this level not at the root
         {pF[l-1] = Or        (n(l, lf), oF);                                   // Collect all the find output fields in this level and Or them together to see if any node found the key. At most one node will find the key if the data has been correctly structured.
          pD[l-1] = orBitBuses(n(l, ld), oD);                                   // Collect all the data output fields from this level and Or them together as they will all be zero except for possible to see if any node found the key. At most one node will find the key if the data has been correctly structured.
         }

        eI = leaf ? eI : root ? oN[0] : orBitBuses(n(l, ln),  oN);              // Collect all next links nodes on this level
       }
      this.data  = orBitBuses(n(output, "data"),  pD);                          // Data found over all layers
      this.found = Or        (n(output, "found"), pF);                          // Or of found status over all layers
     }

    Chip chip() {return Chip.this;}                                             // Containing chip
   }

//D1 Peripherals                                                                // Peripherals represent interactions between the chip and the outside world by providing data to the chip or receiving data from the chip.

  record In                                                                     // An input specification used to create an input peripheral
   (int value,                                                                  // Value of input
    int delay                                                                   // Delay before this input appears
   ){}

  class InputUnit                                                               // An input peripheral
   {final String  name;                                                         // Name of peripheral
    final In   []   in;                                                         // Inputs provided by the peripheral
    final int    width;                                                         // Width of input in bits
    final Gate    edge;                                                         // Falling edge shows new value is available
    final BitBus  bits;                                                         // Gate whose falling edge drives the peripheral
    int current;                                                                // Index of current element being input
    int nextStep;                                                               // Step for next change
    InputUnit(String Name, int Width, In...In)                                  // Load input peripheral with predetermined values that appear after a specified delay
     {name = Name; width = Width; in = In;
      edge =     Input(n(name, "edge"));                                        // Falling edge shows that a new value is available
      edge.setSystemGate();
      bits = new BitBus(n(name, "bits"), width);                                // Create an input bus
      for (int i = 1; i <= width; i++)                                          // Mark the bits as system gates so that they do not require external inputs.
       {final Bit b = bits.b(i);
        final Gate g = Input(b);
        g.setSystemGate();
       }
      inputs.put(name, this);                                                   // Save input peripheral
     }
    public String name()  {return name;}                                        // Name of peripheral
    public Bit    edge()  {return edge;}                                        // Bit whose falling edge triggers the peripheral
    public Integer[]log() {return null;}                                        // Log of activity on peripheral

    public void start()                                                         // Action to be performed at start
     {if (in.length == 0) return;
      final In i = in[0];
      bits.set(  i == null ? null : i.value);
      nextStep = i == null ? 0    : i.delay;
      current = 0;
     }
    public void action()                                                        // Action to be performed on a falling edge
     {if (current < in.length)
       {final In i = in[current];
        final Boolean prev = edge.value;
        edge.value = steps == nextStep-1;
        edge.fell = prev != null && prev && !edge.value;                        // Show whether we had a falling edge to drive connected processes that relay on falling edges
        bits.set(i.value);
        if (steps == nextStep)
         {nextStep += i.delay;
          current++;
         }
       }
      else
       {nextStep   = 0;
        edge.value = false;
       }
     }
    public String toString()                                                    // Action to be performed on a falling edge
     {final StringBuilder b = new StringBuilder();
      for (In i: in) b.append(", new In("+i.value+", "+i.delay+")");
      final String s = b.length() > 0 ? b.toString().substring(2) : "";
      return s;
     }
   }

  class OutputUnit                                                              // An output peripheral
   {final String  name;                                                         // Name of peripheral
    final String logTo;                                                         // Name of log to write to
    final static Map<String,Stack<Integer>> logs = new TreeMap<>();             // Output from all peripherals in log streams
    Stack<Integer> log;                                                         // Output from this peripheral
    final Bits  driver;                                                         // These bits drive the peripheral
    final Bit   edge;                                                           // Falling edge of this pulse writes a new log entry capturing the date of the driving bits
    Gate gEdge;                                                                 // Gate whose falling edge drives the peripheral

    OutputUnit(String Name, String LogTo, Bits Driver, Bit Edge)                // Record driver to specified log on falling edge
     {name = Name; logTo = LogTo; driver = Driver; edge = Edge;
      outputs.put(name, this);                                                  // Save output peripheral
     }

    OutputUnit(String Name, Bits Driver, Bit Edge)                              // Record driver to default log on falling edge
     {this(Name, Name, Driver, Edge);
     }

    Integer[]log() {return log.toArray(new Integer[0]);}                        // Log of activity on peripheral

    void start()                                                                // Action to be performed at start
     {if (!logs.containsKey(logTo)) logs.put(logTo, new Stack<Integer>());      // Start log. Logs with the same name will be merged in entry order.
      log = logs.get(logTo);                                                    // The log that this peripheral should write to.
      log.clear();                                                              // Clear the log. This log might get cleared multiple times if there are multiple writers to it, but the clears all happen at the start so the subsequent ones have no effect.
      gEdge = getGate(edge);                                                    // Locate gate associated with falling edge bit
     }

    void action()                                                               // Action to be performed on a falling edge
     {final Integer i = driver.Int();
      log.push(i);
     }

    boolean fell()                                                              // Check gate had a falling edge
     {final boolean f = gEdge.fell;
      return f;
     }

    public String toString()                                                    // Content of log.
     {final StringBuilder b = new StringBuilder();
      for (Integer i: log) b.append(", "+i);
      final String s = b.length() > 0 ? b.toString().substring(2) : "";
      return name+".ok("+s+");";
     }

    void ok(Integer ... expected)                                               // Confirm log is as expected
     {final Integer[]g = log.toArray(new Integer[0]);
      final Integer[]e = expected;
      Chip.ok(g, e);
     }
   }

//D1 Layout                                                                     // Layout the gates and connect them with wires

  Diagram drawLayout(Diagram d)                                                 // Draw unless we have already drawn it at a lower number of levels or we are forced to draw regardless
   {final Diagram D = diagramsDrawn.get(name);                                  // Have we already drawn this diagram
    if (D == null || d.betterThan(D)) d.gds2();                                 // Never drawn or this drawing is better
    return d;
   }

  Diagram draw(int GlobalScaleX, int GlobalScaleY)                              // Try different gate scaling factors in hopes of finding a single level wiring diagram.  Returns the wiring diagram with the fewest wiring levels found.
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

  Diagram draw() {return draw(1,1);}                                            // Try different gate scaling factors in hopes of finding a single level wiring diagram.  Returns the wiring diagram with the fewest wiring levels found.

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

  static class Connection                                                       // Pairs of gates which we wish to connect
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
    for (Gate g : connectedToOutput.values()) g.px = g.distanceToOutput * sx;   // Position each gate in x as long as it is eventually connected to an output, or it is an output that has a gate

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
        if (!drawn.contains(name))                                              // Only draw each named gate once
         {drawn.add(name);                                                      // Show that we have drawn this gate already
          g.py = y; y += sy; extra += dy;
          for (; extra >= gsy; extra -= gsy) y += gsy;                          // Enough extra has accumulated to be distributed
         }
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

    record Pixel(int x, int y)                                                  // Pixel on the diagram
     {public String toString() {return "["+x+","+y+"]";}
     }

    static class Segment                                                        // Segment containing some pixels
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
     {final Level  level;                                                       // The level we are on
      final Pixel  start;                                                       // Start of desired path
      final Pixel finish;                                                       // Finish of desired path
      Stack<Pixel>  path = new Stack<>();                                       // Path from start to finish
      Stack<Pixel>     o = new Stack<>();                                       // Cells at current edge of search
      Stack<Pixel>     n = new Stack<>();                                       // Cells at new edge of search
      short[][]        d = new short[width][height];                            // Distance at each cell
      Integer      turns = null;                                                // Number of turns along path
      short        depth = 1;                                                   // Length of path
      boolean      found;                                                       // Whether a connection was found or not

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
       {for (depth = 2; o.size() != 0 && depth < 999999; ++depth)               // Keep going until we cannot go any further
         {n = new Stack<>();                                                    // Cells at new edge of search

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

      Search searchLevels()                                                     // Search the levels for a placement of this wire
       {for (Level l : levels)                                                  // Search each existing level for a placement
         {final Search s = new Search(l, start, finish);                        // Search
          if (s.found) return s;                                                // Found a level on which we can connect this wire
         }
        return null;
       }

      Wire(Gate SourceGate, Gate TargetGate, int x, int y, int X, int Y)        // Create a wire and place it
       {sourceGate = SourceGate; targetGate = TargetGate;
        start = new Pixel(p(x), p(y)); finish = new Pixel(p(X), p(Y));
        Search S = searchLevels();                                              // Search each existing level for a placement
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

    boolean perl = false;
    try                                                                         // Check for Perl
     {final var pb = new ProcessBuilder("perl", "-V");
      pb.redirectErrorStream(false);                                            // STDERR will be captured and returned to the caller
      final var P = pb.start();                                                 // Execute the command
      final int r = P.waitFor();                                                // Wait for process to finish and close it
      perl = r == 0;
     }
    catch(Exception E) {}

    if (perl) try                                                               // Execute the file as a Perl script to create the GDS2 output - following code courtesy of Mike Limberger.
     {final var pb = new ProcessBuilder("perl", pf);
      pb.redirectErrorStream(false);                                            // STDERR will be captured and returned to the caller
      final var P = pb.start();                                                 // Execute the command

      final var E = P.getErrorStream();                                         // Read and print STDERR
      for (int  c = E.read(); c > -1; c = E.read()) System.err.print((char)c);
      E.close();

      final int r = P.waitFor();                                                // Wait for process to finish and close it
      if (r != 0) say("Perl script exited with code: " + r);
     }
    catch(Exception E)
     {say("An error occurred while executing Perl script: "+pf+
          " error: "+ E.getMessage());
      System.exit(1);
     }
   }

//D2 Groupings                                                                  // Group chips into two classes and see if any single bit predicts the classification.  Finding a bit that does predict a classification can help resolve edge cases. We could try testing all dyadic gates to see if there is any correlation between their outputs and any other pins indicating that the gate might be redundant. Use class Grouping to achieve this.

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

  static String binaryString(int n, int width)                                  // Convert a integer to a binary string of specified width
   {final String b = "0".repeat(width)+Long.toBinaryString(n);
    return b.substring(b.length() - width);
   }

//D2 Numeric routines                                                           // Numeric routines

  static int max(int n, int...rest)                                             // Maximum of some numbers
   {int m = n;
    for (int i = 0; i < rest.length; i++) m = m < rest[i] ? rest[i] : m;
    return m;
   }

  static int min(int n, int...rest)                                             // Minimum of some numbers
   {int m = n;
    for (int i = 0; i < rest.length; i++) m = m > rest[i] ? rest[i] : m;
    return m;
   }

  static double max(double n, double...rest)                                    // Maximum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = m < rest[i] ? rest[i] : m;
    return m;
   }

  static double min(double n, double...rest)                                    // Minimum number from a list of one or more numbers
   {double m = n;
    for (int i = 0; i < rest.length; ++i) m = m > rest[i] ? rest[i] : m;
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

  static String currentTestName()                                               // Name of the current test
   {final StackTraceElement[] T = Thread.currentThread().getStackTrace();       // Current stack trace
    for (StackTraceElement t : T)                                               // Locate deepest method that starts with test
     {final String c = t.getMethodName();
      if (c.matches("\\Atest_\\w+\\Z")) return c.substring(5);
     }
    return null;                                                                // Not called in a test
   }

  static String sourceFileName()                                                // Name of source file containing this method
   {final StackTraceElement e = Thread.currentThread().getStackTrace()[2];      // 0 is getStackTrace, 1 is this routine, 2 is calling method
    return e.getFileName();
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

  static void stop(Object...O)                                                  // Say something, provide an error trace and stop
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
    else   err(a, "does not equal", b);
   }

  static void ok(String got, String expected)                                   // Confirm two strings match
   {final String G = got, E = expected;
    final int lg = G.length(), le = E.length();
    final StringBuilder b = new StringBuilder();

    boolean matchesLen = true, matches = true;
    if (le != lg)                                                               // Failed on length
     {matchesLen = false;
      err(b, "Mismatched length, expected", le, "got", lg, "for text:\n"+G);
     }

    int l = 1, c = 1;
    final int N = le < lg ? le : lg;
    for (int i = 0; i < N && matches; i++)                                      // Check each character
     {final int  e = E.charAt(i), g = G.charAt(i);
      if (e != g)                                                               // Character mismatch
       {final String ee = e == '\n' ? "new-line" : ""+(char)e;
        final String gg = g == '\n' ? "new-line" : ""+(char)g;

         say(b, "Differs at line: "+String.format("%04d", l),
            "character "+c+", expected="+ee+"= got="+gg+"=");
        say(b, "      0----+----1----+----2----+----3----+----4----+----5----+----6");
        final String[]t = G.split("\\n");
        for(int k = 0; k < t.length; k++)                                       // Details of  mismatch
         {say(b, String.format("%04d  ", k+1)+t[k]);
          if (l == k+1) say(b, " ".repeat(6+c)+'^');
         }
        matches = false;
       }
      if (e == '\n') {++l; c = 0;} else c++;
     }

    if (matchesLen && matches) ++testsPassed; else {++testsFailed; err(b);}     // Show error location with a trace back so we know where the failure occurred
   }

  static void ok(Integer[]G, Integer[]E)                                        // Check that two integer arrays are are equal
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
      else if (!e.equals(g))           {b.append(String.format("Index %d expected %d, but got %d\n",   i, e, g)); ++fails;}
      else ++passes;
     }
    if (fails > 0) err(b);
    testsPassed += passes; testsFailed += fails;                                // Passes and fails
   }

  static void testSummary()                                                     // Print a summary of the testing
   {final double delta = (System.nanoTime() - start) / (double)(1<<30);         // Run time in seconds
    final String d = String.format("tests in %5.2f seconds.", delta);           // Format run time
    if (false) {}                                                               // Analyze results of tests
    else if (testsPassed == 0 && testsFailed == 0) say("No",    d);
    else if (testsFailed == 0)   say("PASSed ALL", testsPassed, d);
    else say("Passed "+testsPassed+",    FAILed:", testsFailed, d);
   }

//D0

  static void test_max_min()
   {ok(min(3,  2,  1),  1);
    ok(max(1,  2,  3),  3);
    ok(min(3d, 2d, 1d), 1d);
    ok(max(1d, 2d, 3d), 3d);
   }

  static void test_source_file_name()
   {ok(sourceFileName().equals("Chip.j") || sourceFileName().equals("Chip.java"), true);
   }

  static void test_and()
   {Chip c = new Chip();
    Bit  i = c.Input ("i");
    Bit  I = c.Input ("I");
    Bit  a = c.And   ("a", i, I);
    Bit  o = c.Output("o", a);

    Inputs inputs = c.new Inputs().set(i, true).set(I, false);

    c.simulate(inputs);

    c.draw();

    i.ok(true);
    I.ok(false);
    a.ok(false);
    o.ok(false);
    ok(c.steps, 3);
   }

  static Chip test_and_grouping(Boolean i1, Boolean i2)
   {Chip  c = new Chip();
    Bit  I1 = c.Input ("i1");
    Bit  I2 = c.Input ("i2");
    Bit and = c.And   ("and", I1, I2);
    Bit   o = c.Output("o", and);
    Inputs inputs = c.new Inputs();
    inputs.set(I1, i1);
    inputs.set(I2, i2);
    c.simulate(inputs);
    o.ok(i1 && i2);
    return c;
   }

  static void test_and_grouping()
   {Grouping g = new Grouping();
    g.put(false, test_and_grouping(true, false));
    g.put( true, test_and_grouping(true, true));
    var a = g.analyze();
    ok(a.size(), 3);
    ok(a.get("and"), true);
    ok(a.get( "i2"), true);
    ok(a.get(  "o"), true);
   }

  static Chip test_or_grouping(Boolean i1, Boolean i2)
   {Chip c = new Chip();
    Bit  I1 = c.Input ("i1");
    Bit  I2 = c.Input ("i2");
    Bit  or = c.Or    ("or", I1, I2);
    Bit   o = c.Output("o", or);
    Inputs inputs = c.new Inputs();
    inputs.set(I1, i1);
    inputs.set(I2, i2);
    c.simulate(inputs);
    o.ok(i1 || i2);
    return c;
   }

  static void test_or_grouping()
   {Grouping g = new Grouping();
    g.put(false, test_or_grouping(true, false));
    g.put( true, test_or_grouping(true, true));
    var a = g.analyze();
    ok(a.size(), 1);
    ok(a.get( "i2"), true);
   }

  static void test_delayed_definitions()
   {Chip   c = new Chip();
    Bit    o = c.Output("o",   c.new Bit("and"));
    Bit  and = c.And   ("and", c.new Bit("i1"), c.new Bit("i2"));
    Bit   i1 = c.Input ("i1");
    Bit   i2 = c.Input ("i2");
    Inputs inputs = c.new Inputs();
    inputs.set(i1, true);
    inputs.set(i2, false);
    c.simulate(inputs);
     i1.ok(true);
     i2.ok(false);
    and.ok(false);
      o.ok(false);
        ok(c.steps, 3);
   }

  static void test_or()
   {Chip c = new Chip();
    Bit i1 = c.Input ("i1");
    Bit i2 = c.Input ("i2");
    Bit or = c.Or    ("or", i1, i2);
    Bit  o = c.Output("o", or);
    Inputs inputs = c.new Inputs();
    inputs.set(i1, true);
    inputs.set(i2, false);
    c.simulate(inputs);
    i1.ok(true);
    i2.ok(false);
    or.ok(true);
     o.ok(true);
    ok( c.steps,    3);
   }

  static void test_not_gates()
   {Chip  c = new Chip();
    Bits  b = c.bits("b", 5, 21);
    Bit   a = c. andBits(  "a",  b);
    Bit   o = c.  orBits(  "o",  b);
    Bit  na = c.nandBits( "na",  b);
    Bit  no = c.norBits ( "no",  b);
    Bit  oa = c.Output  ( "oa",  a);
    Bit  oo = c.Output  ( "oo",  o);
    Bit ona = c.Output  ("ona", na);
    Bit ono = c.Output  ("ono", no);
    c.simulate();
     a.ok(false);
    na.ok(true);
     o.ok(true);
    no.ok(false);
   }

  static void test_zero()
   {Chip c = new Chip();
    Bit  z = c.Zero  ("z");
    Bit  o = c.Output("o", z);
    c.simulate();
    o.ok(false);
    ok(c.steps,  3);
   }

  static void test_one()
   {Chip c = new Chip();
    Bit  O = c.One   ("O");
    Bit  o = c.Output("o", O);
    c.simulate();
    ok(c.steps, 3);
    o.ok(true);
   }

  static Chip test_and3(boolean A, boolean B, boolean C, boolean D)
   {Chip   c = new Chip();
    Bit  i11 = c.Input ("i11");
    Bit  i12 = c.Input ("i12");
    Bit  and = c.And   ("and", i11, i12);
    Bit  i21 = c.Input ("i21");
    Bit  i22 = c.Input ("i22");
    Bit  And = c.And   ("And", i21, i22);
    Bit   Or = c. Or   ("or",  and, And);
    Bit    o = c.Output("o", Or);

    Inputs i = c.new Inputs();
    i.set(i11, A);
    i.set(i12, B);
    i.set(i21, C);
    i.set(i22, D);

    c.simulate(i);

    ok(c.steps,  4);
    o.ok((A && B) || (C && D));
    return c;
   }

  static void test_andOr()
   {boolean  t = true, f = false;
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

    var a = g.analyze();
    ok(a.size(), 1);
    ok(a.containsKey("i11"), true);
    ok(a.get("i11"),         false);
   }

  static void test_expand()
   {Chip    c = new Chip();
    Bit   one = c.One   ("one");
    Bit  zero = c.Zero  ("zero");
    Bit    or = c.Or    ("or",   one, zero);
    Bit   and = c.And   ("and",  one, zero);
    Bit    o1 = c.Output("o1", or);
    Bit    o2 = c.Output("o2", and);
    c.simulate();
    ok(c.steps,  4);
    o1.ok(true);
    o2.ok(false);
   }

  static void test_expand2()
   {Chip    c = new Chip();
    Bit   one = c.One   ("one");
    Bit  zero = c.Zero  ("zero");
    Bit    or = c.Or    ("or",  one, zero);
    Bit   and = c.And   ("and", one, zero);
    Bit   xor = c.Xor   ("xor", one, zero);
    Bit    o1 = c.Output("o1",  or);
    Bit    o2 = c.Output("o2",  and);
    Bit    o3 = c.Output("o3",  xor);
    c.simulate();
    ok(c.steps, 5);
    o1.ok(true);
    o2.ok(false);
    o3.ok(true);
   }

  static void test_output_bits()
   {int N = 4, N2 = powerTwo(N);
    for  (int i = 0; i < N2; i++)
     {Chip c = new Chip();
      Bits C = c.bits("c", N, i);
      Bits o = c.outputBits("o", C);
      c.simulate();
      ok(c.steps, 3);
      ok(o.Int(), i);
     }
   }

  static void test_and_two_bits()
   {for (int N = 3; N <= 4; ++N)
     {int N2 = powerTwo(N);
      for  (int i = 0; i < N2; i++)
       {Chip    c = new Chip ("And2Bits");
        Bits i1 = c.bits   ("i1", N, 5);
        Bits i2 = c.bits   ("i2", N, i);
        Bits  o = c.andBits("o", i1, i2);
        c.outputBits("out", o);
        c.simulate();
        ok(c.steps, 4);
        ok(o.Int(), 5 & i);
       }
     }
   }

  static void test_gt()
   {Chip    c = new Chip();
    Bit   one = c.One   ("o");
    Bit  zero = c.Zero  ("z");
    Bit    gt = c.Gt    ("gt", one, zero);
    Bit     o = c.Output("O", gt);
    c.simulate();
    ok(c.steps, 4);
    o.ok(true);
   }

  static void test_gt2()
   {Chip    c = new Chip();
    Bit   one = c.One   ("o");
    Bit  zero = c.Zero  ("z");
    Bit    gt = c.Gt    ("gt", zero, one);
    Bit     o = c.Output("O",  gt);
    c.simulate();
    ok(c.steps, 4);
    o.ok(false);
   }

  static void test_lt()
   {Chip    c = new Chip();
    Bit   one = c.One   ("o");
    Bit  zero = c.Zero  ("z");
    Bit    lt = c.Lt    ("lt", one, zero);
    Bit     o = c.Output("O", lt);
    c.simulate();
    ok(c.steps, 4);
    o.ok(false);
   }

  static void test_lt2()
   {Chip    c = new Chip();
    Bit   one = c.One   ("o");
    Bit  zero = c.Zero  ("z");
    Bit    lt = c.Lt    ("lt", zero, one);
    Bit     o = c.Output("O", lt);
    c.simulate();
    ok(c.steps, 4);
    o.ok(true);
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
   {int B = 3;
    int[]bits =  {5, 4, 7, 6, 7};
    Chip c = new Chip("aix_"+B);
    Words i = c.    words("i", B, bits);
    Bits  a = c. andWords("a", i), oa = c.outputBits("oa", a);
    Bits  o = c.  orWords("o", i), oo = c.outputBits("oo", o);
    Bits  A = c.andWordsX("A", i), oA = c.outputBits("oA", A);
    Bits  O = c. orWordsX("O", i), oO = c.outputBits("oO", O);
    c.simulate();
    ok(oa.Int(),  4);
    ok(oo.Int(),  7);
    ok(oA.Int(), 20);
    ok(oO.Int(), 31);
   }

  static void test_compare_eq()
   {for (int B = 2; B <= 4; ++B)
     {int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {Chip c = new Chip("compare_eq_"+B);
          Bits I = c.bits     ("i", B, i);
          Bits J = c.bits     ("j", B, j);
          Bit  o = c.compareEq("o", I, J);
          Bit  p = c.compareEq("p", I, j);
          o.anneal(); p.anneal();
          c.simulate();
          o.ok(i == j);
          p.ok(i == j);
         }
       }
     }
   }

  static void test_compare_gt()
   {for (int B = 2; B <= 4; ++B)
     {int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {Chip c = new Chip("compare_gt_"+B);
          Bits I = c.bits     ("i", B, i);
          Bits J = c.bits     ("j", B, j);
          Bit  o = c.compareGt("o", I, J);
          Bit  O = c.Output   ("O", o);
          c.simulate();
          ok(c.steps >= 5 && c.steps <= 9, true);
          o.ok(i > j);
          if (B == 4 && i == 1 && j == 2) c.draw(3, 2);
         }
       }
     }
   }

  static void test_compare_lt()
   {for     (int B = 2; B <= 4; ++B)
     {int B2 = powerTwo(B);
      for   (int i = 0; i < B2; ++i)
       {for (int j = 0; j < B2; ++j)
         {Chip c = new Chip("compare_lt_"+B);
          Bits I = c.bits("i", B, i);
          Bits J = c.bits("j", B, j);
          Bit  o = c.compareLt("o", I, J);
          Bit  O = c.Output("O", o);
          c.simulate();
          ok(c.steps >= 5 && c.steps <= 9, true);
          o.ok(i < j);
         }
       }
     }
   }

  static void test_choose_from_two_words()
   {for (int i = 0; i < 2; i++)
     {int b = 4;
      Chip c = new Chip("choose_from_two_words_"+b+"_"+i);
      Bits A = c.bits("a", b,  3);
      Bits B = c.bits("b", b, 12);
      Bit  C = i > 0 ?  c.One("c") : c.Zero("c");
      Bits o = c.chooseFromTwoWords("o", A, B, C);
      Bits O = c.outputBits("out",  o);
      c.simulate();
      ok(c.steps, 9);
      O.ok(i == 0 ? 3 : 12);
     }
   }

  static void test_enable_word()
   {for (int i = 0; i < 2; i++)
     {int  B = 4;
      Chip c = new Chip("enable_word_"+i);
      Bits a = c.bits("a", B,  3);
      Bit  e = i > 0 ?  c.One("e") : c.Zero("e");
      Bits o = c.enableWord("o", a, e);
      Bits O = c.outputBits("out",  o);
      c.simulate();
      ok(c.steps, 5);
      O.ok(i == 0 ? 0 : 3);
     }
   }

  static void test_enable_word_equal()
   {int B = 4;
    final int[]x = {0, 2, 4, 6, 8};
    for (int i = 0; i < x.length; i++)
     {Chip C = new Chip("enable_word_equal_"+i);
      var e1 = new EnableWord(C.bits("a1", B, 2), C.bits("b1", B, 1));
      var e2 = new EnableWord(C.bits("a2", B, 4), C.bits("b2", B, 2));
      var e3 = new EnableWord(C.bits("a3", B, 6), C.bits("b3", B, 3));
      var e4 = new EnableWord(C.bits("a4", B, 8), C.bits("b4", B, 4));
      Bits c = C.bits("c", B, i);
      Bits q = C.enableWord("o", c, e1, e2, e3, e4);
      Bits O = C.outputBits("out",  q);
      C.simulate();
      O.ok(x[i]);
     }
   }

  static void test_enable_word_if_equal()
   {int B = 4;
    final int[]x = {0, 2, 4, 6, 8};
    for (int i = 0; i < x.length; i++)
     {Chip C = new Chip("enable_word_if_equal_"+i);
      Bits I = C.bits("i", B, i);
      Bits e = C.bits("e", B, 4);
      Bits r = C.bits("r", B, i+1);
      Bits s = C.enableWordIfEq("R", r, I, e);
      Bits O = C.outputBits("out",  s);
      C.simulate();
      O.ok(i == 4 ? i + 1 : 0);
     }
   }

  static void test_monotone_mask_to_point_mask()
   {for (int B = 2; B <= 4; ++B)
     {int N = powerTwo(B);
      for  (int i = 1; i <= N; i++)
       {Chip c = new Chip("monotone_mask_to_point_mask_"+B);
        Bits I = c.new BitBus("i", N);
        for (int j = 1; j <= N; j++) if (j < i) c.One(I.b(j)); else c.Zero(I.b(j));
        Bits O = c.outputBits("O", I);
        c.simulate();
        ok(O.Int(), powerTwo(i-1)-1);
       }
     }
   }

  static void test_choose_word_under_mask(int B, int i)
   {int[]numbers =  {2, 3, 2, 1,  0, 1, 2, 3,  2, 3, 2, 1,  0, 1, 2, 3};
    int  B2 = powerTwo(B);
    Chip  c = new Chip("choose_word_under_mask_"+B);
    Words I = c.words("i", B, Arrays.copyOfRange(numbers, 0, B2));
    Bits  m = c.bits ("m", B2, powerTwo(i));
    Bits  o = c.chooseWordUnderMask("o", I, c.new PointMask(m));
    Bits  O = c.outputBits("O", o);
    c.simulate();
    O.ok(numbers[i]);
    c.draw(3, 1);
   }

  static void test_choose_word_under_mask()
   {int N = github_actions ? 4 : 3;
    for   (int B = 2; B <= N;          B++)
      for (int i = 1; i < powerTwo(B); i++)
        test_choose_word_under_mask(B, i);
   }

  static Chip test_btree_node(int find, int validKeys, int enable, boolean Found, int Data, int Next)
   {int[]keys = {2, 4, 6};
    int[]data = {1, 3, 5};
    int[]next = {1, 3, 5};
    int   top = 7;
    int     B = 3;
    int     N = 3;
    int    id = 7;
    var     c = new Chip("btree_node_"+B);

    BtreeNode b = BtreeNode.Test                                                // Search a node
     (c, "node", id, B, N, enable, find, keys, data, next, top, validKeys);
    BtreeNode.Search s = b.search();
    Bits d = c.outputBits("d", s.data);
    Bits n = c.outputBits("n", s.next);
    Bit  f = c.Output    ("f", s.found);
    c.simulate();
//stop(c);
//  ok(c.steps >= 18 && c.steps <= 22, true);
    f.ok(Found);
    d.ok(Data);
    n.ok(Next);
    return c;
   }

  static void test_btree_node()
   {test_btree_node(1, 0b111, 7, false, 0, 1);
    test_btree_node(2, 0b111, 7,  true, 1, 0);
    test_btree_node(3, 0b111, 7, false, 0, 3);
    test_btree_node(4, 0b111, 7,  true, 3, 0);
    test_btree_node(5, 0b111, 7, false, 0, 5);
    test_btree_node(6, 0b111, 7,  true, 5, 0);
    test_btree_node(7, 0b111, 7, false, 0, 7);

    test_btree_node(1, 0b111, 1, false, 0, 0);
    test_btree_node(2, 0b111, 1, false, 0, 0);
    test_btree_node(3, 0b111, 1, false, 0, 0);
    test_btree_node(4, 0b111, 1, false, 0, 0);
    test_btree_node(5, 0b111, 1, false, 0, 0);
    test_btree_node(6, 0b111, 1, false, 0, 0);
    test_btree_node(7, 0b111, 1, false, 0, 0);

    test_btree_node(1, 0b011, 7, false, 0, 1);                                  // Only the first two keys are valid
    test_btree_node(2, 0b011, 7,  true, 1, 0);
    test_btree_node(3, 0b011, 7, false, 0, 3);
    test_btree_node(4, 0b011, 7,  true, 3, 0);
    test_btree_node(5, 0b011, 7, false, 0, 7);
    test_btree_node(6, 0b011, 7, false, 0, 7);
    test_btree_node(7, 0b011, 7, false, 0, 7);

    test_btree_node(1, 0b011, 1, false, 0, 0);
    test_btree_node(2, 0b011, 1, false, 0, 0);
    test_btree_node(3, 0b011, 1, false, 0, 0);
    test_btree_node(4, 0b011, 1, false, 0, 0);
    test_btree_node(5, 0b011, 1, false, 0, 0);
    test_btree_node(6, 0b011, 1, false, 0, 0);
    test_btree_node(7, 0b011, 1, false, 0, 0);
   }

  static Chip test_btree_leaf_compare(int find, int enable, boolean Found, int Data, int Next)
   {int[]keys = {2, 4, 6};
    int[]data = {1, 3, 5};
    int[]next = null;
    int   top = 7;
    int    ke = 0b111;
    int     B = 3;
    int     N = 3;
    int    id = 7;
    var     c = new Chip("btree_leaf_compare_"+B);

    BtreeNode b = BtreeNode.Test(c, "node", id, B, N, enable, find, keys, data, next, top, ke);
    BtreeNode.Search s = b.search();
    Bits      d = c.outputBits("d", s.data);
    Gate      f = c.Output    ("f", s.found);
    c.simulate();
    //ok(c.steps == 10 || c.steps == 12, true);
    f.ok(Found);
    d.ok(Data);
    return c;
   }

  static void test_btree_leaf_compare()
   {test_btree_leaf_compare(1, 7, false, 0, 1);
    test_btree_leaf_compare(2, 7,  true, 1, 0);
    test_btree_leaf_compare(3, 7, false, 0, 3);
    test_btree_leaf_compare(4, 7,  true, 3, 0);
    test_btree_leaf_compare(5, 7, false, 0, 5);
    test_btree_leaf_compare(6, 7,  true, 5, 0);
    test_btree_leaf_compare(7, 7, false, 0, 7);

    test_btree_leaf_compare(1, 1, false, 0, 0);
    test_btree_leaf_compare(2, 1, false, 0, 0);
    test_btree_leaf_compare(3, 1, false, 0, 0);
    test_btree_leaf_compare(4, 1, false, 0, 0);
    test_btree_leaf_compare(5, 1, false, 0, 0);
    test_btree_leaf_compare(6, 1, false, 0, 0);
    test_btree_leaf_compare(7, 1, false, 0, 0);
   }

  static void test_Btree(Btree b, Inputs i, int find)
   {Chip c = b.chip();
    i.set(b.find, find);
    c.simulate(i);
   }

  static void test_Btree(Btree b, Inputs i, int find, int data)
   {test_Btree(b, i, find);
    Chip c = b.chip();
    b.found.ok(true);
    b.data .ok(data);
   }

  static void test_Btree(Btree b, Inputs i, int find, int found, boolean layout)
   {test_Btree(b, i, find, found);

    //if (layout) b.chip().draw(6, 1);                                          // Layout chip - takes time so supressed during development
   }

  static void test_Btree()
   {int B = 8, K = 3, L = 2;

    Chip   c = new Chip();
    Bits   f = c.inputBits("find", B);
    Btree  b = c.new Btree("tree", f, K, L, B);
    b.data.anneal();                                                            // Anneal the tree
    b.found.anneal();

    Inputs i = c.new Inputs();
    BtreeNode r = b.tree.get(1).nodes.get(1);
    i.set(r.Keys,   10,  20, 30);
    i.set(r.Data,   11,  22, 33);
    i.set(r.Next,  2,  3,  4);
    i.set(r.Top,               5);
    BtreeNode l1 = b.tree.get(2).nodes.get(1);
    i.set(l1.Keys,   2,  4,  6);
    i.set(l1.Data,  22, 44, 66);
    BtreeNode l2 = b.tree.get(2).nodes.get(2);
    i.set(l2.Keys,  13, 15, 17);
    i.set(l2.Data,  31, 51, 71);
    BtreeNode l3 = b.tree.get(2).nodes.get(3);
    i.set(l3.Keys,  22, 24, 26);
    i.set(l3.Data,  22, 42, 62);
    BtreeNode l4 = b.tree.get(2).nodes.get(4);
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

    int[]skip = {10, 20, 30,  2,4,6,  13,15,17, 22,24,26, 33,35,37};
    for (int F : java.util.stream.IntStream.rangeClosed(0, 100).toArray())
     {if (Arrays.stream(skip).noneMatch(x -> x == F)) test_Btree(b, i, F);
     }
   }

  static void test_simulation_step()
   {Chip  c = new Chip();
    Bit   i = c.   One("i");
    Bit  n9 = c.   Not("n9", i);
    Bit  n8 = c.   Not("n8", n9);
    Bit  n7 = c.   Not("n7", n8);
    Bit  n6 = c.   Not("n6", n7);
    Bit  n5 = c.   Not("n5", n6);
    Bit  n4 = c.   Not("n4", n5);
    Bit  n3 = c.   Not("n3", n4);
    Bit  n2 = c.   Not("n2", n3);
    Bit  n1 = c.   Not("n1", n2);
    Bit  o  = c.Output("o",  n1);

    Inputs I = c.new Inputs();
    I.set(i, true);
    c.simulate();
    o.ok(false);
    ok(c.steps,    12);
   }

  static void test_clock()
   {Chip c = new Chip("clock", 8);
    c.minSimulationSteps(16);
    Stack<Boolean> s = new Stack<>();
    c.simulationStep = ()->{s.push(c.getBit("a"));};
    Bit  and = c.And   ("a", c.clock0.b(1), c.clock0.b(2));
    Bit  out = c.Output("o", and);
    c.simulate();
    ok(c.steps,  19);
    for (int i = 0; i < s.size(); i++) ok(s.elementAt(i), (i+1) % 4 == 0);
   }

  static void test_pulse()
   {Chip c = new Chip();

    Pulse p1 = c.pulse("p1", 16);
    Pulse p2 = c.pulse("p2", 16, 2, 3);
    Pulse p3 = c.pulse("p3", 16, 1, 5);
    Pulse p4 = c.pulse("p4",  4);
    Bit   a  = c.   Or("a", p1, p2, p3);
    Bit   m  = c.   My("m", p4, a);                                             // A falling edge on pin 1 loads the value in pin 2 into the memory bit
    c.Output("o", m);

    c.executionTrace("1 2 3 a    4 m", "%s %s %s %s    %s %s", p1, p2, p3, a, p4,m);
    c.simulationSteps(32);
    c.simulate();
    //c.printExecutionTrace();
    c.ok("""
Step  1 2 3 a    4 m
   1  1 0 0 .    1 .
   2  0 0 0 .    0 .
   3  0 0 0 1    0 .
   4  0 1 0 0    0 .
   5  0 1 0 0    1 .
   6  0 0 1 1    0 1
   7  0 0 0 1    0 1
   9  0 0 0 0    1 1
  10  0 0 0 0    0 0
  13  0 0 0 0    1 0
  14  0 0 0 0    0 0
  17  1 0 0 0    1 0
  18  0 0 0 0    0 0
  19  0 0 0 1    0 0
  20  0 1 0 0    0 0
  21  0 1 0 0    1 0
  22  0 0 1 1    0 1
  23  0 0 0 1    0 1
  25  0 0 0 0    1 1
  26  0 0 0 0    0 0
  29  0 0 0 0    1 0
  30  0 0 0 0    0 0
""");
   }

  static void test_register()
   {Chip       c = new Chip();
    Bits      i1 = c.bits("i1", 8, 9);
    Bits      i2 = c.bits("i2", 8, 6);
    Pulse     pc = c.pulse("choose", 32, 16, 16);
    Pulse     pl = c.pulse("load",    8,  1,  1);
    Pulse     pr = c.pulse("result", 16,  1, 12);
    Bits       I = c.chooseFromTwoWords("I", i1, i2, pc);
    Register   r = c.register("reg",  I, pl);
    OutputUnit p = c.new OutputUnit("pReg", r, pr);
    Bits       o = c.outputBits("o", r);

    c.executionTrace("c  choose    l  register ld", "%s  %s  %s  %s  %s", pc, I, pl, r, pr);
    c.simulationSteps(64);
    c.simulate();
    p.ok(9, 6, 9, 6);
    //c.printExecutionTrace(); stop();
    c.ok("""
Step  c  choose    l  register ld
   1  0  ........  0  ........  0
   2  0  ........  1  ........  0
   3  0  0000....  0  ........  0
   5  0  0000....  0  0000....  0
   6  0  0000.00.  0  0000....  0
   9  0  00001001  0  0000....  0
  10  0  00001001  1  0000....  0
  11  0  00001001  0  0000....  0
  13  0  00001001  0  00001001  1
  14  0  00001001  0  00001001  0
  17  1  00001001  0  00001001  0
  18  1  00001001  1  00001001  0
  19  1  00001001  0  00001001  0
  22  1  00001111  0  00001001  0
  25  1  00000110  0  00001001  0
  26  1  00000110  1  00001001  0
  27  1  00000110  0  00001001  0
  29  1  00000110  0  00000110  1
  30  1  00000110  0  00000110  0
  33  0  00000110  0  00000110  0
  34  0  00000110  1  00000110  0
  35  0  00000110  0  00000110  0
  38  0  00000000  0  00000110  0
  41  0  00001001  0  00000110  0
  42  0  00001001  1  00000110  0
  43  0  00001001  0  00000110  0
  45  0  00001001  0  00001001  1
  46  0  00001001  0  00001001  0
  49  1  00001001  0  00001001  0
  50  1  00001001  1  00001001  0
  51  1  00001001  0  00001001  0
  54  1  00001111  0  00001001  0
  57  1  00000110  0  00001001  0
  58  1  00000110  1  00001001  0
  59  1  00000110  0  00001001  0
  61  1  00000110  0  00000110  1
  62  1  00000110  0  00000110  0
""");
   }

  static void test_delay_bits()
   {final int N = 16;
    Chip      c = new Chip ();
    Bits      p = c.bits("p", N);
    for (int i  = 1; i <= N; i++)
      c.pulseBuilder(p.b(i).name).period(N).delay(i-1).build();

    c.executionTrack("p", "%s", p);
    c.simulationSteps(20);
    c.simulate();
    //c.printExecutionTrace(); stop();

    c.ok("""
Step  p
   1  0000000000000001
   2  0000000000000010
   3  0000000000000100
   4  0000000000001000
   5  0000000000010000
   6  0000000000100000
   7  0000000001000000
   8  0000000010000000
   9  0000000100000000
  10  0000001000000000
  11  0000010000000000
  12  0000100000000000
  13  0001000000000000
  14  0010000000000000
  15  0100000000000000
  16  1000000000000000
  17  0000000000000001
  18  0000000000000010
  19  0000000000000100
  20  0000000000001000
""");
   }

  static void test_shift()
   {Chip c = new Chip();
    Bits b = c.bits      ("b", 4, 3);
    Bits u = c.shiftUp   ("u",  b); Bits ou = c.outputBits("ou", u);
    Bits U = c.shiftUpOne("U",  b); Bits oU = c.outputBits("oU", U);
    Bits d = c.shiftDown ("d",  b); Bits od = c.outputBits("od", d);

    c.simulate();
    ou.ok(6);
    oU.ok(7);
    od.ok(1);
   }

  static void test_binaryAdd()
   {for     (int B = 1; B <= (github_actions ? 4 : 3); B++)
     { int B2 = powerTwo(B);
      for      (int i = 0; i < B2; i++)
       {for    (int j = 0; j < B2; j++)
         {Chip      c = new Chip("binary_add");
          Bits      I = c.bits("i", B, i);
          Bits      J = c.bits("j", B, j);
          BinaryAdd a = c.binaryAdd ("ij",  I, J);
          Bits      o = c.outputBits("o", a.sum());
          c.Output    ("co", a.carry);
          c.simulate  ();
          a.sum  .ok((i+j) %  B2);
          a.carry.ok((i+j) >= B2);
         }
       }
     }
   }

  static void test_connect_buses()
   {int N = 4;

    Chip C = new Chip();
    Bits i = C.bits ("i",  N,  9);
    Bits o = C.new BitBus("o", N);
    Bits O = C.outputBits("O", o);
    C.connect(o, i);

    C.executionTrace("i     o     O", "%s  %s  %s", i, o, O);
    C.simulate();
    //C.printExecutionTrace(); stop(C);

    C.ok("""
Step  i     o     O
   1  1001  ....  ....
   2  1001  1001  ....
   3  1001  1001  1001
""");
   }

  static void test_8p5i4()
   {int N = 8;
    Chip      C = new Chip();
    Bits      a = C.bits      ("a",  N, 127);
    Bits      b = C.bits      ("b",  N, 128);
    BinaryAdd c = C.binaryAdd ("c",  a, b);
    Bit      oc = C.Output    ("oc", c.carry());
    Bits     os = C.outputBits("os", c.sum);
    C.simulationSteps(48);
    C.simulate();
    ok(os.toString(), "11111111");                                              // toString unfortunately required
   }

// 2 3 5 8 13 21 34 55 89 144 233

  static void test_fibonacci()                                                  // First few fibonacci numbers
   {final int N = 8, D = 22;                                                    // Number of bits in number, wait time to allow latest number to be computed from prior two
    Chip        C = new Chip();                                                 // Create a new chip
    Bits     zero = C.bits ("zero", N, 0);                                      // Zero - the first element of the sequence
    Bits      one = C.bits ("one",  N, 1);                                      // One - the second element of the sequence
    Pulse      ia = C.pulse("ia", 0,   D);                                      // Initialize the registers to their starting values
    Pulse      ib = C.pulse("ib", 0, 2*D);
    Pulse      la = C.pulse("la", 3*D, D, 0*D);                                 // Each pair sum is calculated on a rotating basis
    Pulse      lb = C.pulse("lb", 3*D, D, 1*D);
    Pulse      lc = C.pulse("lc", 3*D, D, 2*D);

    Bits       ab = C.bits("ab", N);                                            // Pre-declare the output of the pair sums so that we can use these buses to drive the registers holding the latest fibonacci numbers
    Bits       ac = C.bits("ac", N);
    Bits       bc = C.bits("bc", N);

    Register    a = C.register("a", bc, la);                                    // Registers holding the latest fibonacci number
    Register    b = C.register("b", ac, lb);
    Register    c = C.register("c", ab, lc);

    OutputUnit fa = C.new OutputUnit("fa", "f", a, la);                         // Log latest number
    OutputUnit fb = C.new OutputUnit("fb", "f", b, lb);
    OutputUnit fc = C.new OutputUnit("fc", "f", c, lc);

    BinaryAdd sbc = C.binaryAdd("sbc", b, c);                                   // a
    BinaryAdd sac = C.binaryAdd("sac", a, c);                                   // b
    BinaryAdd sab = C.binaryAdd("sab", a, b);                                   // c
    sab.carry.anneal(); sac.carry.anneal(); sbc.carry.anneal();                 // Ignore the carry bits

    Bits       BC = C.chooseFromTwoWords("bc", sbc.sum, zero, ia);              // a
    Bits       AC = C.chooseFromTwoWords("ac", sac.sum, one,  ib);              // b
    Bits       AB = C.continueBits      ("ab", sab.sum);                        // c

    C.executionTrace(
      "ia la ib lb lc   a         b         c",
      "%s  %s  %s  %s  %s    %s  %s  %s",
      ia, la, ib, lb, lc, a, b, c);
    C.simulationSteps(380);
    C.simulate();
    fa.ok(null, null, null, 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233);
    //C.printExecutionTrace(); stop();
    C.ok("""
Step  ia la ib lb lc   a         b         c
   1  1  1  1  0  0    ........  ........  ........
  23  0  0  1  1  0    ........  ........  ........
  25  0  0  1  1  0    00000000  ........  ........
  45  0  0  0  0  1    00000000  ........  ........
  47  0  0  0  0  1    00000000  00000001  ........
  67  0  1  0  0  0    00000000  00000001  ........
  69  0  1  0  0  0    00000000  00000001  00000001
  89  0  0  0  1  0    00000000  00000001  00000001
  91  0  0  0  1  0    00000010  00000001  00000001
 111  0  0  0  0  1    00000010  00000001  00000001
 113  0  0  0  0  1    00000010  00000011  00000001
 133  0  1  0  0  0    00000010  00000011  00000001
 135  0  1  0  0  0    00000010  00000011  00000101
 155  0  0  0  1  0    00000010  00000011  00000101
 157  0  0  0  1  0    00001000  00000011  00000101
 177  0  0  0  0  1    00001000  00000011  00000101
 179  0  0  0  0  1    00001000  00001101  00000101
 199  0  1  0  0  0    00001000  00001101  00000101
 201  0  1  0  0  0    00001000  00001101  00010101
 221  0  0  0  1  0    00001000  00001101  00010101
 223  0  0  0  1  0    00100010  00001101  00010101
 243  0  0  0  0  1    00100010  00001101  00010101
 245  0  0  0  0  1    00100010  00110111  00010101
 265  0  1  0  0  0    00100010  00110111  00010101
 267  0  1  0  0  0    00100010  00110111  01011001
 287  0  0  0  1  0    00100010  00110111  01011001
 289  0  0  0  1  0    10010000  00110111  01011001
 309  0  0  0  0  1    10010000  00110111  01011001
 311  0  0  0  0  1    10010000  11101001  01011001
 331  0  1  0  0  0    10010000  11101001  01011001
 333  0  1  0  0  0    10010000  11101001  11111001
 353  0  0  0  1  0    10010000  11101001  11111001
 355  0  0  0  1  0    11100010  11101001  11111001
 375  0  0  0  0  1    11100010  11101001  11111001
 377  0  0  0  0  1    11100010  11011011  11111001
""");
   }

  static void test_insert_into_array()                                          // Insert a word in an array of words
   {int B = 4;
    int[]array = {2, 4, 6, 8};                                                  // Array to insert into
    int M = 0b1111;
    for (int j = 0; j <= B; j++)
     {int I = 2 * j + 1, mm = (M>>j)<<j;
      Chip  c = new Chip();                                                     // Create a new chip
      Words w = c.words("array", B, array);                                     // Array to insert into
      Bits  m = c.bits("mask",   B, mm);                                        // Monotone mask insertion point
      Bits  i = c.bits("in",     B, I);                                         // Word to insert

      Words W = c.insertIntoArray("o", w, c.new UpMask(m), i);                  // Insert
      Words O = c.outputWords    ("O", W);
      c.simulate();
      switch(j)
       {case 0 -> W.ok(1,2,4,6);
        case 1 -> W.ok(2,3,4,6);
        case 2 -> W.ok(2,4,5,6);
        case 3 -> W.ok(2,4,6,7);
        case 4 -> W.ok(2,4,6,8);
       }
     }
   }

  static void test_remove_from_array()                                          // Remove a word from an array, slide the array down to cover the gap and insert a new word at the top to cover the resulting gap there.
   {int B = 4;
    int[]array = {2, 4, 6, 8};                                                  // Array to remove from
    int M = 0b1111;
    for (int j = 0; j <= B; j++)
     {int I = 1, mm = (M>>j)<<j;
      Chip  c = new Chip();                                                     // Create a new chip
      Words w = c.words("array", B, array);                                     // Array to remove from
      Bits  m = c.bits("mask",   B, mm);                                        // Monotone mask removal point
      Bits  i = c.bits("in",     B, I);                                         // Word to remove

      RemoveFromArray W = c.removeFromArray("o", w, c.new UpMask(m), i);        // Remove
      Words O = c.outputWords    ("O", W.out);
      Bits  R = c.outputBits     ("R", W.removed);
      c.simulate();
      switch(j)
       {case 0 -> {O.ok(4,6,8,1); R.ok(2);} // 1111
        case 1 -> {O.ok(2,6,8,1); R.ok(4);} // 1110
        case 2 -> {O.ok(2,4,8,1); R.ok(6);} // 1100
        case 3 -> {O.ok(2,4,6,1); R.ok(8);} // 1000
        case 4 -> {O.ok(2,4,6,8); R.ok(0);} // 0000 - prevent removal of 8
       }
     }
   }

  static void test_btree_insert(int position)
   {int Key = 1, Data = 2, Next = 3;
    int[]keys = {2, 4, 6};
    int[]data = {3, 5, 7};
    int[]next = {1, 3, 5};
    int valid = 0b111;
    int   top = 7;
    int     B = 3;
    int     M = 3;
    int    id = 7;

    Chip    c = new Chip();

    BtreeNode b = BtreeNode.Test(c, "node", id, B, M, 0, 0,                     // Create a disabled node as id != enabled
      keys, data, next, top, valid);
    b.anneal();                                                                 // Anneal unused gates

    Bits  k = c.bits("inKeys",   B, Key);                                       // New Key
    Bits  d = c.bits("inData",   B, Data);                                      // New data
    Bits  n = c.bits("inNext",   B, Next);                                      // New links
    Bits  p = c.bits("insertAt", B, position);                                  // Insert position at first position in monotone mask to be true
    BtreeNode.Insert i = b.Insert("out", k, d, n, c.new UpMask(p));             // Insert results
    Words K = c.outputWords("ok", i.keys);                                      // Modified keys
    Words D = c.outputWords("od", i.data);                                      // Modified data
    Words N = c.outputWords("on", i.next);                                      // Modified next
    Bits  E = c.outputBits ("oe", i.enabledKeys.bits);                          // Enabled keys
    c.simulate();
    switch(position)
     {case 0b111 -> {K.ok(1,2,4); D.ok(2,3,5); N.ok(3,1,3); E.ok(0b111);}
      case 0b110 -> {K.ok(2,1,4); D.ok(3,2,5); N.ok(1,3,3); E.ok(0b111);}
      case 0b100 -> {K.ok(2,4,1); D.ok(3,5,2); N.ok(1,3,3); E.ok(0b111);}
      default -> stop("Test expectations required for", position);
     }
   }

  static void test_btree_insert()
   {test_btree_insert(0b111);
    test_btree_insert(0b110);
    test_btree_insert(0b100);
   }

  static void test_btree_split_node(int test)
   {int   Key  = 1, Data = 2, Next = 3;
    int[]pKeys = {20, 30, 40};
    int[]k1    = {21, 22, 23};
    int[]k2    = { 1,  2,  3};
    int[]bKeys = test == 1 ? k1 : k2;
    int[]pData = {51, 52, 53};
    int[]d1    = {54, 55, 56};
    int[]d2    = {57, 58, 59};
    int[]bData = test == 1 ? d1 : d2;
    int[]pNext = {61, 62, 63};
    int[]n1    = {64, 65, 66};
    int[]n2    = {67, 68, 69};
    int[]bNext = test == 1 ? n1 : n2;
    int   pTop = 71, bTop = 72;
    int      B = 8;
    int      M = 3;
    int     id = 7;

    Chip      c = new Chip();

    BtreeNode p = BtreeNode.Test(c, "parent", id, B, M, 0, 0, pKeys, pData, pNext, pTop, 0b11);
    BtreeNode b = BtreeNode.Test(c, "node",   id, B, M, 0, 0, bKeys, bData, bNext, bTop, 0b11);
    p.anneal();
    b.anneal();

    BtreeNode.Split s = p.split(b, 70);                                         // Split a node
    Words pk = c.outputWords("pk", s.parent.Keys);
    Words pd = c.outputWords("pd", s.parent.Data);
    Words pn = c.outputWords("pn", s.parent.Next);
    Bits  pe = c.outputBits ("pe", s.parent.KeysEnabled);

    Words ak = c.outputWords("ak", s.lower.Keys);
    Words ad = c.outputWords("ad", s.lower.Data);
    Words an = c.outputWords("an", s.lower.Next);
    Bits  ae = c.outputBits ("ae", s.lower.KeysEnabled);
    Bits  at = c.outputBits ("at", s.lower.Top);

    Words bk = c.outputWords("bk", s.upper.Keys);
    Words bd = c.outputWords("bd", s.upper.Data);
    Words bn = c.outputWords("bn", s.upper.Next);
    Bits  be = c.outputBits ("be", s.upper.KeysEnabled);
    Bits  bt = c.outputBits ("bt", s.upper.Top);

    c.simulate();

    switch(test)
     {case 1 ->                                                                 // Split right most, upper most child
       {p.Keys.ok(20, 30, 40);
        b.Keys.ok(21, 22, 23);
        p.Data.ok(51, 52, 53);
        b.Data.ok(54, 55, 56);
        p.Next.ok(61, 62, 63);
        b.Next.ok(64, 65, 66);
        p.Top.ok(71);
        b.Top.ok(72);
            pk.ok(20, 22, 30);
            ak.ok(21);
            bk.ok(23);

            pd.ok(51, 55, 52);
            ad.ok(54);
            bd.ok(56);

            pn.ok(61, 70, 62);
            an.ok(64);
            at.ok(65);
            bn.ok(66);
            bt.ok(72);
       }
      case 2 ->                                                                 // Split left most, lowest child
       {p.Keys.ok(20, 30, 40);
        b.Keys.ok( 1,  2,  3);
        p.Data.ok(51, 52, 53);
        b.Data.ok(57, 58, 59);
        p.Next.ok(61, 62, 63);
        b.Next.ok(67, 68, 69);
        p.Top.ok(71);
        b.Top.ok(72);
            pk.ok( 2, 20, 30);
            ak.ok( 1);
            bk.ok( 3);

            pd.ok(58, 51, 52);
            ad.ok(57);
            bd.ok(59);

            pn.ok(70, 61, 62);
            an.ok(67);
            at.ok(68);
            bn.ok(69);
            bt.ok(72);
       }
     }
   }

  static void test_btree_split_node()
   {test_btree_split_node(1);
    test_btree_split_node(2);
   }

  static void test_sub_bit_bus()
   {int   N = 8;
    Chip  c = new Chip();
    Bits  b = c.bits("b", N, 5<<2);
    Bits  B = c.new SubBitBus("B", b, 3, 4);
    Bits ob = c.outputBits("ob", b);
    Bits oB = c.outputBits("oB", B);
     c.simulate();
     b.ok(20);
     B.ok( 5);
    oB.ok( 5);
   }

  static void test_sub_word_bus()
   {int    N = 8;
    Chip   c = new Chip();
    Words  w = c.words("w", N, 3, 5, 7, 9, 11);
    Words  W = c.new SubWordBus("W", w, 3, 2);
    Words ow = c.outputWords("ow", w);
    Words oW = c.outputWords("oW", W);
     c.simulate();
     w.ok(3, 5, 7, 9, 11);
     W.ok(7, 9);
    oW.ok(7, 9);
   }

  static void test_find_words()
   {int   N = 8;
    Chip  c = new Chip    ();
    Words w = c.words     ("w", N, 3, 5, 7, 9, 11);
    Bits  b = c.bits      ("b", N, 6);
    Bits  g = c.findGt    ("g", w, b);
    Bits  G = c.outputBits("G", g);
    Bits  l = c.findLt    ("l", w, b);
    Bits  L = c.outputBits("L", l);
    c.simulate();
    G.ok(0b11100);
    L.ok(0b00011);
   }

  static void test_binary_increment()                                           // Increment repetitively
   {final int  N = 4, wait = 24;                                                // Bit width of number to increment, wait time for addition to complete
    Chip       C = new Chip();                                                  // Create a new chip
    Bits    zero = C.bits ("zero",   N,  0);                                    // Zero - the first element of the sequence
    Bits     one = C.bits ("one",    N,  1);                                    // One - the amount to be added
    Pulse     pz = C.pulseBuilder("pz").on(wait).build();                       // Load zero at start or latest number later
    Pulse     pi = C.pulseBuilder("pi").period(wait).on(N).delay(wait-N-1).build(); // Let the sum  hapoen then reload teh register with a short enough pulse to make the additions happen quickly and a short enough on time that the new number does not feedback
    Bits       n = C.new BitBus("n", N);                                        // Variable

    Register   r = C.register  ("r", n, pi);                                    // Register holding bits to increment
    OutputUnit o = C.new OutputUnit("o", r, pi);                                // Log the value of the register just before it is changed by the incoming new result.

    BinaryAdd  i = C.binaryAdd("i", r, one);                                    // Increment
                   C.chooseFromTwoWords("n", i.sum, zero, pz);                  // Initially zero, later the latest sum

    C.executionTrace("pz  pi  r", "%s   %s   %s", pz, pi, r);
    C.simulationSteps(640);
    C.simulate();

    o.ok(null, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    //C.printExecutionTrace(); say(o); //stop();
    C.ok("""
Step  pz  pi  r
   1  1   0   ....
  20  1   1   ....
  24  1   0   ....
  25  0   0   0000
  44  0   1   0000
  48  0   0   0000
  49  0   0   0001
  68  0   1   0001
  72  0   0   0001
  73  0   0   0010
  92  0   1   0010
  96  0   0   0010
  97  0   0   0011
 116  0   1   0011
 120  0   0   0011
 121  0   0   0100
 140  0   1   0100
 144  0   0   0100
 145  0   0   0101
 164  0   1   0101
 168  0   0   0101
 169  0   0   0110
 188  0   1   0110
 192  0   0   0110
 193  0   0   0111
 212  0   1   0111
 216  0   0   0111
 217  0   0   1000
 236  0   1   1000
 240  0   0   1000
 241  0   0   1001
 260  0   1   1001
 264  0   0   1001
 265  0   0   1010
 284  0   1   1010
 288  0   0   1010
 289  0   0   1011
 308  0   1   1011
 312  0   0   1011
 313  0   0   1100
 332  0   1   1100
 336  0   0   1100
 337  0   0   1101
 356  0   1   1101
 360  0   0   1101
 361  0   0   1110
 380  0   1   1110
 384  0   0   1110
 385  0   0   1111
 404  0   1   1111
 408  0   0   1111
 409  0   0   0000
 428  0   1   0000
 432  0   0   0000
 433  0   0   0001
 452  0   1   0001
 456  0   0   0001
 457  0   0   0010
 476  0   1   0010
 480  0   0   0010
 481  0   0   0011
 500  0   1   0011
 504  0   0   0011
 505  0   0   0100
 524  0   1   0100
 528  0   0   0100
 529  0   0   0101
 548  0   1   0101
 552  0   0   0101
 553  0   0   0110
 572  0   1   0110
 576  0   0   0110
 577  0   0   0111
 596  0   1   0111
 600  0   0   0111
 601  0   0   1000
 620  0   1   1000
 624  0   0   1000
 625  0   0   1001
""");
   }

  static void test_input_peripheral()                                           // Input from a peripheral
   {int         N = 4;
    Chip        c = new Chip();
    InputUnit   i = c.new InputUnit ("i", N, new In(4,3), new In(2,2), new In(1,1));
    OutputUnit  o = c.new OutputUnit("o", i.bits, i.edge);
    c.executionTrack("o     e", "%s  %s", i.bits, i.edge);
    c.simulationSteps(10);
    c.simulate();
    o.ok(4, 2, 1);
    //c.printExecutionTrace(); stop();
    c.ok("""
Step  o     e
   1  0100  0
   2  0100  1
   3  0100  0
   4  0010  0
   5  0010  1
   6  0010  0
   7  0001  1
   8  0001  0
   9  0001  0
  10  0001  0
""");
   }

  static void test_twos_complement_arith()
   {int         N = 4;
    Chip        c = new Chip();
    Bits    seven = c.bits            ("seven", N, 7);
    Bits      two = c.bits            ("two",   N, 2);
    Bits     nine = c.binaryTCAdd     ("nine",  seven, two);
    Bits     five = c.binaryTCSubtract("five",  seven, two);
    nine.anneal();
    five.anneal();
    c.simulate();
    seven.ok(7);
      two.ok(2);
     nine.ok(9);
     five.ok(5);
   }

// Iiii iii Jjjj jjj I J C O Result
// 1110 110 1110 110 1 1 0 1 false
// 1110 110 1111 111 1 1 1 0 true
// 1110 110 0000 000 1   0 0 true
// 1110 110 0001 001 1   0 0 true
// 1110 110 0010 010 1   0 0 true
// 1111 111 1110 110 1 1 0 1 false
// 1111 111 1111 111 1 1 0 1 false
// 1111 111 0000 000 1   0 0 true
// 1111 111 0001 001 1   0 0 true
// 1111 111 0010 010 1   0 0 true
// 0000 000 1110 110   1 1 1 false
// 0000 000 1111 111   1 1 1 false
// 0000 000 0000 000     0 0 false
// 0000 000 0001 001     1 1 true
// 0000 000 0010 010     1 1 true
// 0001 001 1110 110   1 1 1 false
// 0001 001 1111 111   1 1 1 false
// 0001 001 0000 000     0 0 false
// 0001 001 0001 001     0 0 false
// 0001 001 0010 010     1 1 true
// 0010 010 1110 110   1 1 1 false
// 0010 010 1111 111   1 1 1 false
// 0010 010 0000 000     0 0 false
// 0010 010 0001 001     0 0 false
// 0010 010 0010 010     0 0 false

  static void test_twos_complement_compare_lt()
   {int        N = 4;
    for   (int i = -8; i <= 7; i++)
     {for (int j = -8; j <= 7; j++)
       {Chip   c = new Chip();
        Bits   I = c.bits("i", N, i);
        Bits   J = c.bits("j", N, j);
        Bit    L = c.binaryTCCompareLt("c", I, J);
        L.anneal();
        c.simulate();
        L.ok(i < j);
       }
     }
   }

  static void test_shiftLeftMultiple()
   {int      N = 4;
    for (int i = 0; i <= N; i++)
     {Chip   c = new Chip();
      Bits one = c.bits("one", N, 1);
      Bits   I = c.bits("i",   N, i);
      Bits   s = c.shiftLeftMultiple("s", one, I);
      s.anneal();
      c.simulate();
      s.ok(switch(i)
       {case  0 -> 0b0001;
        case  1 -> 0b0010;
        case  2 -> 0b0100;
        case  3 -> 0b1000;
        default -> 0b0000;
       });
     }
   }

  static void test_shiftRightMultiple()
   {int      N = 4;
    for (int i = 0; i <= N; i++)
     {Chip   c = new Chip();
      Bits one = c.bits("one", N, 8);
      Bits   I = c.bits("i",   N, i);
      Bits   s = c.shiftRightMultiple("s", one, I);
      s.anneal();
      c.simulate();
      s.ok(switch(i)
       {case  0 -> 0b1000;
        case  1 -> 0b0100;
        case  2 -> 0b0010;
        case  3 -> 0b0001;
        default -> 0b0000;
       });
     }
   }

  static void test_shiftRightArithmetic()
   {int      N = 4;
    for (int i = 1; i <= N; i++)
     {Chip   c = new Chip();
      Bits one = c.bits("one", N, 0b1000);
      Bits   I = c.bits("i",   N, i);
      Bits   s = c.shiftRightArithmetic("s", one, I);
      s.anneal();
      c.simulate();
      s.ok(switch(i)
       {case  0 -> 0b1000;
        case  1 -> 0b1100;
        case  2 -> 0b1110;
        default -> 0b1111;
       });
     }
   }

  static void test_signExtend(int length, int result)
   {Chip   c = new Chip();
    Bits   i = c.bits("i", 4, 0b1001);
    Bits   I = c.binaryTCSignExtend("I", i, length);
    I.anneal(); i.anneal();
    c.simulate();
    I.ok(result);
   }

  static void test_signExtend()
   {test_signExtend(2,        0b01);
    test_signExtend(4,      0b1001);
    test_signExtend(8, 0b1111_1001);
   }

  static void test_then_if_eq_else()
   {final int N = 4;
    Chip   c = new Chip();
    Bits   t = c.bits("t", N, 1);
    Bits   e = c.bits("e", N, 2);
    Bits   a = c.bits("a", N, 3);
    Bits   T = c.chooseThenElseIfEQ("T", t, e, a, 3);
    Bits   E = c.chooseThenElseIfEQ("E", t, e, a, 2);
    T.anneal(); E.anneal();
    c.simulate();
    T.ok(1);
    E.ok(2);
   }

  static void test_bits_forward()
   {final int B = 4;
    Chip   c = new Chip();
    Bit zero = c.Zero("zero");
    Bit  one = c.One  ("one");
    Bits   b = c.bits("b",   2);
    Bits  bb = c.bits("b",   2);
    Bit   b0 = c.Not(b.b(1), one);
    Bit   b1 = c.Not(b.b(2), zero);
    b0.anneal(); b1.anneal();
    c.simulate();
    b0.ok(false);
    b1.ok(true);
   }

  static void test_words_forward()
   {final int W = 2, B = 4;
    Chip   c = new Chip();
    Bits one = c.bits("one", B, 1);
    Bits two = c.bits("two", B, 2);
    Words  w = c.words("w", W, B);
    Words ww = c.words("w", W, B);
    Bits  w1 = c.notBits(w.w(1), one);
    Bits  w2 = c.notBits(w.w(2), two);
    w1.anneal(); w2.anneal();
    c.simulate();
    w1.ok(14);
    w2.ok(13);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_max_min();
    test_source_file_name();
    test_and_two_bits();
    test_and();
    test_and_grouping();
    test_or_grouping();
    test_or();
    test_not_gates();
    test_zero();
    test_one();
    test_andOr();
    test_clock();
    test_delayed_definitions();
    test_simulation_step();
    test_connect_buses();
    test_expand();
    test_expand2();
    test_output_bits();
    test_gt();
    test_gt2();
    test_lt();
    test_lt2();
    test_aix();
    test_pulse();
    test_compare_eq();
    test_compare_gt();
    test_compare_lt();
    test_choose_from_two_words();
    test_enable_word();
    test_enable_word_equal();
    test_monotone_mask_to_point_mask();
    test_choose_word_under_mask();
    test_delay_bits();
    test_shift();
    test_binaryAdd();
    test_btree_node();
    test_btree_leaf_compare();
    test_Btree();
    test_8p5i4();
    test_register();
    test_fibonacci();
    test_insert_into_array();
    test_remove_from_array();
    test_btree_insert();
    test_sub_bit_bus();
    test_sub_word_bus();
    test_find_words();
    test_btree_split_node();
    test_binary_increment();
    test_input_peripheral();
    test_enable_word_if_equal();
    test_twos_complement_arith();
    test_twos_complement_compare_lt();
    test_shiftLeftMultiple();
    test_shiftRightMultiple();
    test_shiftRightArithmetic();
    test_signExtend();
    test_then_if_eq_else();
    test_bits_forward();
    test_words_forward();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      gds2Finish();                                                             // Execute resulting Perl code to create GDS2 files
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(traceBack(e));
     }
   }
 }
