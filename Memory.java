//------------------------------------------------------------------------------
// Bit memory described by a layout.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;
/*
The mind commands the body and it obeys. The mind orders itself and meets
resistance. The mentat must overcome this resistance with logic.
Lady Jessica - Dune - Frank Herbert - 1965.
*/
class Memory extends Chip                                                       // Bit memory described by a layout.
 {final Layout mainLayout;                                                      // Layout of memory
  final boolean[]bits;                                                          // Bits comprising the memory

  Memory(int Size, Layout Layout)                                               // Create main memory with attached layout to describe its structure
   {bits = new boolean[Size];
    zero();
    mainLayout = Layout;
   }

  Memory(Memory memory)                                                         // Deep copy of a memory so we can reference part of it as a new memory without physically having to create the sub part
   {bits = memory.bits;
    final Layout l = memory.mainLayout;
    mainLayout = l != null ? l.duplicate() : null;
   }

  static Memory memory(int size, Layout Layout)                                 // Create memory
   {return new Memory(size, Layout);
   }

  Layout layout() {return mainLayout;}                                          // Make the layout field overridable

  void ok(String expected) {Chip.ok(toString(), expected);}                     // Check memory is as expected

  int size() {return bits.length;}                                              // Size of memory

  void    set(int i, boolean b) {bits[i] = b;}                                  // Set a bit in memory
  boolean get(int i) {return     bits[i];}                                      // Get a bit in memory

  public String toString()                                                      // Memory as string
   {final StringBuilder b = new StringBuilder();
    final int size = size();
    for (int i = size; i > 0; --i) b.append(get(i-1) ? '1' : '0');
    return b.toString();
   }

  void set(Memory source, int offset)                                           // Copy source memory into this memory at the specified offset
   {final int t = size(), s = source.size(), m = min(t, s);
    if (offset + s > t) stop("Cannot write beyond end of memory");
    for (int i = 0; i < m; i++) set(offset+i, source.get(i));                   // Load the specified string into memory
   }

  void set(Memory source) {set(source, 0);}                                     // Set memory from source

  Memory get(int offset, int width)                                             // Get a sub section of this memory
   {final Memory m = new Memory(width, null);
    if (offset + width > size()) stop("Cannot read beyond end of memory");
    for (int i = 0; i < width; i++) m.set(i, get(offset+i));
    return m;
   }

  void zero()                                                                   // Zero a memory
   {final int size = size();
    for (int i = 0; i < size; i++) set(i, false);
   }

  void shiftLeftFillWithZeros(int left)                                         // Shift left filling with zeroes
   {final int size = size();
    for (int i = size; i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;    i < left; ++i) set(i,   false);
   }

  void shiftLeftFillWithOnes(int left)                                          // Shift left filling with ones
   {final int size = size();
    for (int i = size; i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;    i < left; ++i) set(i,   true);
   }

  void shiftRightFillWithZeros(int right)                                       // Shift right filling with zeroes
   {final int size = size();
    for (int i = 0; i < size-right;    ++i) set(i, get(i+right));
    for (int i = size-right; i < size; ++i) set(i, false);
   }

  void shiftRightFillWithSign(int right)                                        // Shift right filling with sign
   {final int size = size();
    for (int i = 0; i < size-right;      ++i) set(i, get(i+right));
    final boolean sign = get(size-1);
    for (int i = size-right; i < size-1; ++i) set(i, sign);
   }

  void set(int value)                                                           // Set memory to the value of an integer
   {final int n = min(size(), Integer.SIZE);
    for (int i = 0; i < n; i++) set(i, (value & (1<<i)) != 0);                  // Convert variable to bits
   }

  int getInt()                                                                  // Get memory as an integer
   {final int n = size();
    int v = 0;
    for (int i = 0; i < n; i++) if (get(i)) v |= (1<<i);                        // Convert bits to int
    return v;
   }

  int countLeadingZeros()                                                       // Count leading zeros
   {int c = 0;
    for (int i = size(); i > 0; --i) if (get(i-1)) return c; else ++c;
    return c;
   }

  int countLeadingOnes()                                                        // Count leading ones
   {int c = 0;
    for (int i = size(); i > 0; --i) if (!get(i-1)) return c; else ++c;
    return c;
   }

  int countTrailingZeros()                                                      // Count trailing zeros
   {final int n = size();
    int c = 0;
    for (int i = 0; i < n; ++i) if (get(i)) return c; else ++c;
    return c;
   }

  int countTrailingOnes()                                                       // Count trailing ones
   {final int n = size();
    int c = 0;
    for (int i = 0; i < n; ++i) if (!get(i)) return c; else ++c;
    return c;
   }

// D2 Sub Memory                                                                // A sub memory provides access to a part of the full memory.  A sub memory can be mapped by a sub structure.

  class Sub extends Memory                                                      // Sub memory - a part of a larger memory rebased to zero
   {final int start;                                                            // Start of sub memory in larger memory
    final int width;                                                            // Width of sub memory
    Layout subLayout;                                                           // Layout of sub memory

    Sub(int Start, int Width)                                                   // Access part of the main memory as a sub memory
     {super(Memory.this);                                                       // Main memory
      if (Start + Width < 0) stop("Sub memory extends before start of main memory");
      if (Start + Width > Memory.this.size()) stop("Sub memory extends beyond end of main memory");
      start = Start;  width = Width; subLayout = null;
     }

    Sub(Layout Layout)                                                          // Access part of the main memory as a sub memory with the specified layout
     {super(Memory.this);                                                       // Main memory
      start = Layout.at; width = Layout.width;                                  // Position in main memory
      subLayout = Layout.duplicate();
     }

    Layout layout() {return subLayout;}                                         // Make the layout field overridable
    int    size  () {return width;}                                             // Width of sub memory

    boolean get(int i)                                                          // Get a bit from a sub memory
     {if (i > width) stop("Trying to read beyond end of sub memory", i, width);
      if (i < 0)     stop("Trying to read before start of sub memory", i);
      return Memory.this.get(i + start);
     }

    void set(int i, boolean b)                                                  // Set a bit in a sub memory
     {if (i > width) stop("Trying to write beyond end of sub memory", i, width);
      if (i < 0)     stop("Trying to write before start of sub memory", i);
      Memory.this.set(i + start, b);
     }
   }

  Sub sub(int start, int width) {return new Sub(start, width);}                 // Create a sub memory
  Sub sub(Layout layout)        {return new Sub(layout);}                       // Create a sub memory with a specified layout

//D1 Layouts                                                                    // Layout memory as variables, arrays, structures, unions

  abstract static class Layout                                                  // Variable/Array/Structure definition. Memory definitions can only be laid out once.
   {final String name;                                                          // Name of field
    int at;                                                                     // Offset of variable either from start of memory or from start of a structure
    int width;                                                                  // Number of width in field
    int depth;                                                                  // Depth of field
    Layout up;                                                                  // Chain to containing field
    final Stack<Layout> fields = new Stack<>();                                 // Fields in the super structure in the order they appear in the memory layout. Only relevant in the outer most layout == the super structure,  where it is used for printing the structure and locating sub structures.

    Layout(String Name) {name = Name;}                                          // Create a new named memory layout

    Layout width   (int Width) {width = Width; return this;}                    // Set width or layout once it is known
    Layout position(int At)    {at    = At;    return this;}                    // Reposition array elements to take account of the index applied to the array

    abstract void layout  (int at, int depth, Layout superStructure);           // Layout this field within the super structure.

    Memory layout()
     {fields.clear();
      layout(0, 0, this);
      return new Memory(width, this);
     }

    String indent() {return "  ".repeat(depth);}                                // Indentation

    public String toString()                                                    // Print the memory layout header
     {return String.format("%4d  %4d        %s  %s", at, width, indent(), name);
     }

    public String print()                                                       // Walk the field list printing the memory layout headers
     {if (fields == null) return "";                                            // The structure has not been laid out
      final StringBuilder b = new StringBuilder();
      b.append(String.format("%4s  %4s  %4s    %s", "  At", "Wide", "Size", "Field name\n"));
      for(Layout m : fields) b.append(""+m+"\n");                               // Print all the fields in the structure layout
      return b.toString();
     }

    Layout getFieldDef(String path)                                             // Path to field
     {final String[]names = path.split("\\.");                                  // Split path
      if (fields == null) return null;                                          // Not compiled
      for (Layout m : fields)                                                   // Each field in structure
       {Layout p = m;                                                           // Start at this field and try to match the path
        boolean found = true;
        for(int q = names.length; q > 0 && p != null && found; --q, p = p.up)   // Back track through names
         {found = p.name.equals(names[q-1]);                                    // Check path matches
         }
        if (found) return m;                                                    // Return this field if its path matches
       }
      return null;                                                              // No matching path
     }

    void set(Memory memory, Memory variable)                                    // Set a variable in memory
     {memory.set(variable, at);
     }

    void set(Memory memory, int variable)                                       // Set a variable in memory from an integer
     {final Memory m = new Memory(width, null);
      m.set(variable);
      set(memory, m);
     }

    Memory get(Memory memory) {return memory.get(at, width);}                   // Get a variable from memory as copied bits
    int getInt(Memory memory) {return get(memory).getInt();}                    // Get a variable from memory as an integer

    Memory.Sub subMemory(Memory memory) {return memory.sub(at, width);}         // Get a variable from memory as sub memory

    abstract Layout duplicate(int At);                                          // Duplicate an element of this layout so we can modify it safely

    Layout duplicate()                                                          // Duplicate a set of nested layouts rebasing their start point to zero
     {final Layout l = duplicate(at);
      l.layout();
      return l;
     }
   }

  static class Variable extends Layout                                          // Variable
   {Variable(String name, int Width)
     {super(name); width(Width);
     }
    void layout(int At, int Depth, Layout superStructure)                       // Layout the variable in the structure
     {at = At; depth = Depth; superStructure.fields.push(this);
     }
    Layout duplicate(int At)                                                    // Duplicate a variable so we can modify it safely
     {final Variable v = new Variable(name, width);
      v.at = at - At; v.depth = depth;
      return v;
     }
   }

  static class Array extends Layout                                             // Array definition.
   {int size;                                                                   // Dimension of array
    int index = 0;                                                              // Index of array element to access
    Layout element;                                                             // The elements of this array
    Array(String Name, Layout Element, int Size)
     {super(Name);
      size(Size).element(Element);
     }
    Array    size(int Size)       {size    = Size;    return this;}             // Set the size of the array
    Array element(Layout Element) {element = Element; return this;}             // The type of the element in the array
    int at(int i)                 {return at+i*element.width;}                  // Offset of this array element in the structure

    void layout(int At, int Depth, Layout superStructure)                       // Compile this variable so that the size, width and byte fields are correct
     {at = At; depth = Depth; superStructure.fields.push(this);
      element.layout(at, Depth+1, superStructure);
      element.up = this;                                                        // Chain up to containing parent layout
      width = size * element.width;
     }

    Layout position(int At)                                                     // Reposition an array
     {at = At;
      element.position(at + index * element.width);
      return this;
     }

    public String toString()                                                    // Print the field
     {return String.format("%4d  %4d  %4d  %s  %s",
                            at, width, size, indent(), name);
     }

    void setIndex(int Index) {index = Index; position(at);}                     // Sets the index for the current array element allowing us to set and get this element and all its sub elements.

    Layout duplicate(int At)                                                    // Duplicate an array so we can modify it safely
     {final Array a = new Array(name, element.duplicate(), size);
      a.width = width; a.at = at - At; a.depth = depth;
      return a;
     }
   }

  static class Structure extends Layout                                         // Structure laid out in memory
   {final Map<String,Layout> subMap   = new TreeMap<>();                        // Unique variables contained inside this variable
    final Stack     <Layout> subStack = new Stack  <>();                        // Order of variables inside this variable

    Structure(String Name, Layout...Fields)                                     // Fields in the structure
     {super(Name);
      for (int i = 0; i < Fields.length; ++i) addField(Fields[i]);              // Each field in this structure
     }

    void addField(Layout Field)                                                 // Add additional fields
     {Field.up = this;                                                          // Chain up to containing structure
      if (subMap.containsKey(Field.name))
       {stop("Structure already contains field with this name", name, Field.name);
       }
      subMap.put (Field.name, Field);                                           // Add as a sub structure by name
      subStack.push(Field);                                                     // Add as a sub structure in order
     }

    void layout(int at, int Depth, Layout superStructure)                       // Compile this variable so that the size, width and byte fields are correct
     {width = 0;
      depth = Depth;
      superStructure.fields.push(this);
      for(Layout v : subStack)                                                  // Layout sub structure
       {v.at = at+width;
        v.layout(v.at, Depth+1, superStructure);
        width += v.width;
       }
     }

    Layout position(int At)                                                     // Reposition this structure to allow access to array elements via an index
     {at = At;
      int w = 0;
      for(Layout v : subStack)                                                  // Layout sub structure
       {v.position(v.at = at+w);
        w += v.width;
       }
      return this;
     }

    Layout duplicate(int At)                                                    // Duplicate a structure so we can modify it safely
     {final Structure s = new Structure(name);
      s.width = width; s.at = at - At; s.depth = depth;
      for(Layout L : subStack)
       {final Layout l = L.duplicate();
        s.subMap.put(l.name, l);
        s.subStack.push(l);
       }
      return s;
     }
   }

  static class Union extends Layout                                             // Union of structures laid out in memory
   {final Map<String,Layout> subMap = new TreeMap<>();                          // Unique variables contained inside this variable

    Union(String Name, Layout...Fields)                                         // Fields in the union
     {super(Name);
      for (int i = 0; i < Fields.length; ++i)                                   // Each field in this union
       {final Layout s = Fields[i];
        s.up = this;                                                            // Chain up to containing structure
        if (subMap.containsKey(s.name)) stop(name, "already contains", s.name);
        subMap.put (s.name, s);                                                 // Add as a sub structure by name
       }
     }

    void layout(int at, int Depth, Layout superStructure)                       // Compile this variable so that the size, width and byte fields are correct
     {width = 0;
      depth = Depth;
      superStructure.fields.push(this);
      for(Layout v : subMap.values())                                           // Find largest substructure
       {v.at = at;
        v.layout(v.at, Depth+1, superStructure);
        width = max(width, v.width);                                            // Space occupied is determined by largest element of union
       }
     }

    Layout position(int At)                                                     // Position elements of this union to allow arrays to access their elements by an index
     {at = At;
      for(Layout v : subMap.values()) {v.position(at);}
      return this;
     }

    Layout duplicate(int At)                                                    // Duplicate a union so we can modify it safely
     {final Union u = new Union(name);
      u.width = width; u.at = at - At; u.depth = depth;
      for(String s : subMap.keySet())
       {final Layout L = subMap.get(s);
        final Layout l = L.duplicate();
        u.subMap.put(l.name, l);
       }
      return u;
     }
   }

  static Variable  variable (String name, int width)              {return new Variable (name, width);}
  static Array     array    (String name, Layout   ml, int width) {return new Array    (name, ml, width);}
  static Structure structure(String name, Layout...ml)            {return new Structure(name, ml);}
  static Union     union    (String name, Layout...ml)            {return new Union    (name, ml);}

//D0                                                                            // Tests

  static void test_memory()
   {Memory m = memory(110, null);
    for (int i = 1; i < 11; i++)
     {m.shiftLeftFillWithOnes(i);
      m.shiftLeftFillWithZeros(i);
     }
                                     m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000000");
    m.shiftRightFillWithSign(1);     m.ok("11011001110001111000011111000001111110000001111111000000011111111000000001111111110000000001111111111000000000");
    m.shiftRightFillWithZeros(1);    m.ok("01101100111000111100001111100000111111000000111111100000001111111100000000111111111000000000111111111100000000");
    m.shiftLeftFillWithOnes(8);      m.ok("11100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000011111111");
    m.shiftLeftFillWithZeros(2);     m.ok("10001111000011111000001111110000001111111000000011111111000000001111111110000000001111111111000000001111111100");
    ok(m.countLeadingOnes  (), 1);
    ok(m.countTrailingZeros(), 2);
   }

  static void test_memory_sub()
   {Memory m = memory(110, null);                                                     // Main memory
    for (int i = 1; i < 11; i++)
     {m.shiftLeftFillWithOnes(i);
      m.shiftLeftFillWithZeros(i);
     }                               m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000000");

    final Memory.Sub s = m.sub(10, 10);                                         // Sub memory
    s.shiftRightFillWithZeros(2);    m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000000111111110000000000");
    s.shiftLeftFillWithOnes(1);      m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000001111111110000000000");
    s.shiftLeftFillWithZeros(1);     m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111100000000000");
    ok(s.countLeadingOnes  (), 9);
    ok(s.countTrailingZeros(), 1);

    final Memory.Sub S = s.sub(4, 2);                                           // Sub sub memory
    S.zero();                        m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011110011100000000000");
   }

  static void test_memory_set_from_memory()
   {Memory m = memory(8, null);
    m.shiftLeftFillWithOnes (2);
    m.shiftLeftFillWithZeros(2);
    m.shiftLeftFillWithOnes (2);
    m.shiftLeftFillWithZeros(2);
    Memory M = memory(16, null);
    m.ok("11001100"); M.ok("0000000000000000");
    Memory s = M.sub((M.size() - m.size())/2, m.size());
    s.set(m);
    s.ok("11001100"); M.ok("0000110011000000");
   }

//D0 Tests                                                                      // Tests

  static void test_variable()
   {Variable  a1 = variable ("a1", 4);
    Variable  b1 = variable ("b1", 4);
    Variable  c1 = variable ("c1", 2);
    Array     C1 = array    ("C1", c1, 10);
    Structure s1 = structure("inner1", a1, b1, C1);

    Variable  a2 = variable ("a2", 4);
    Variable  b2 = variable ("b2", 4);
    Variable  c2 = variable ("c2", 2);
    Array     C2 = array    ("C2", c2, 10);
    Structure s2 = structure("inner2", a2, b2, C2);

    Variable  a3 = variable ("a3",  8);
    Variable  b3 = variable ("b3",  8);
    Variable  c3 = variable ("c3",  2);
    Array     C3 = array    ("C3", c3, 4);
    Union     u3 = union    ("inner3", a3, b3, C3);

    Array     A1 = array    ("Array1", s1,  2);
    Array     A2 = array    ("Array2", s2,  2);
    Structure T  = structure("outer",  s1, s2, u3);

    T.layout();
    ok(T.print(), """
  At  Wide  Size    Field name
   0    64          outer
   0    28            inner1
   0     4              a1
   4     4              b1
   8    20    10        C1
   8     2                c1
  28    28            inner2
  28     4              a2
  32     4              b2
  36    20    10        C2
  36     2                c2
  56     8            inner3
  56     8     4        C3
  56     2                c3
  56     8              a3
  56     8              b3
""");

    Layout U = u3.duplicate();
    ok(U.print(), """
  At  Wide  Size    Field name
   0     8          inner3
   0     8     4      C3
   0     2              c3
   0     8            a3
   0     8            b3
""");

    ok(T.getFieldDef("outer.inner1.C1.c1").at,  8);
    ok(T.getFieldDef("outer.inner2.C2.c2").at, 36);
    ok(C2.at(2), 40);

    if (true)                                                                   // Set memory directly
     {final Memory m = T.layout();
         b1.set   (m,   1);
         b2.set   (m,  12);
      ok(b1.getInt(m),  1);
      ok(b2.getInt(m), 12);

                                     m.ok("0000000000000000000000000000110000000000000000000000000000010000");
      m.shiftRightFillWithSign(1);   m.ok("0000000000000000000000000000011000000000000000000000000000001000");
      m.shiftLeftFillWithOnes(2);    m.ok("0000000000000000000000000001100000000000000000000000000000100011");
      m.shiftLeftFillWithZeros(2);   m.ok("0000000000000000000000000110000000000000000000000000000010001100");
      m.shiftLeftFillWithZeros(25);  m.ok("1100000000000000000000000000000100011000000000000000000000000000");
      m.shiftRightFillWithSign(2);   m.ok("1111000000000000000000000000000001000110000000000000000000000000");
      ok(m.countLeadingOnes  (),  4);
      ok(m.countTrailingZeros(), 25);


      final Memory n = m.sub(s2);
      m.ok("1111000000000000000000000000000001000110000000000000000000000000");
      n.ok(        "0000000000000000000000000100");
      n.layout().getFieldDef("inner2.b2").set(n, 3);
      m.ok("1111000000000000000000000000001101000110000000000000000000000000");
      n.ok(        "0000000000000000000000110100");
     }

    if (true)                                                                   // Set array elements
     {Memory m = T.layout();
      for (int i = 0; i < C1.size; i++)
       {C1.setIndex(i);
        c1.set(m, i % 4);
       }
      ok(""+m, "0000000000000000000000000000000000000100111001001110010000000000");
     }
   }

  static void test_double_array()
   {Variable  a = variable ("a", 2);
    Variable  b = variable ("b", 3);
    Variable  c = variable ("c", 2);
    Structure s = structure("s", a, b, c);
    Array     A = array    ("A", s, 4);
    Array     B = array    ("B", A, 3);

    B.layout();
    ok(B.print(), """
  At  Wide  Size    Field name
   0    84     3    B
   0    28     4      A
   0     7              s
   0     2                a
   2     3                b
   5     2                c
""");

   Layout C = B.duplicate();
   ok(C.print(), """
  At  Wide  Size    Field name
   0    84     3    B
   0    28     4      A
   0     7              s
   0     2                a
   2     3                b
   5     2                c
""");


    Memory m = B.layout();
    for   (int i = 0; i < B.size; i++)
     {B.setIndex(i);
      for (int j = 0; j < A.size; j++)
       {A.setIndex(j);
        a.set(m, 1);
        b.set(m, 3);
        c.set(m, 1);
       }
     }
    m.ok("010110101011010101101010110101011010101101010110101011010101101010110101011010101101");
   }

  static void test_sub_variable()
   {Variable  a = variable ("a", 2);
    Variable  b = variable ("b", 3);
    Variable  c = variable ("c", 2);
    Structure s = structure("s", a, b, c);

    s.layout();
    ok(s.print(), """
  At  Wide  Size    Field name
   0     7          s
   0     2            a
   2     3            b
   5     2            c
""");

    Memory m = s.layout();
    a.set(m, 1);
    b.set(m, 2);
    c.set(m, 3);
    Memory A = a.subMemory(m);
    Memory B = b.subMemory(m);
    Memory C = c.subMemory(m);
    Memory S = s.subMemory(m);
    A.ok("01");
    B.ok("010");
    C.ok("11");
    S.ok("1101001");
    B.set(7);
    B.ok("111");
    S.ok("1111101");
   }

  static void test_array()
   {final int N       = 4;

    Variable  unary   = new Variable("unary",   N);
    Variable  element = new Variable("element", N);
    Array     array   = new Array   ("array",  element, N);
    array.layout();
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_memory();
    test_memory_sub();
    test_memory_set_from_memory();
    test_variable();
    test_double_array();
    test_sub_variable();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_variable();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(fullTraceBack(e));
     }
   }
 }
