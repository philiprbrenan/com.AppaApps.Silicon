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
 {boolean[]bits     = null;                                                     // Bits comprising the memory
  Layout mainLayout = null;                                                     // Layout of memory
  int at            = 0 ;                                                       // Position of memory in bits
  int width         = 0;                                                        // Number of bits in memory

  Memory duplicateMemory()                                                      // Deep copy of layout of a memory so we can adjust array positions
   {final Memory m = new Memory();
    m.bits = bits;
    if (mainLayout != null) m.mainLayout = mainLayout.duplicate();
    return m;
   }

  static Memory memory(int size)                                                // Create memory of specified size
   {final Memory m = new Memory();
    m.bits = new boolean[size];
    for (int i = 0; i < size; i++) m.bits[i] = false;
    m.width = size;
    return m;
   }

  Memory memory(int At, int Width)                                              // Create memory within memory
   {final Memory m = new Memory();
    m.bits  = bits;
    m.at    = at + At;
    m.width = Width;
    return m;
   }

  int at()        {return at;}                                                  // Position of field in memory
  int size()      {return width;}                                               // Size of the memory

  boolean get(int i)                                                            // Get a bit from memory
   {final int w = size(), a = at();
    if (i > w) stop("Trying to read beyond end of memory",   i, w);
    if (i < 0) stop("Trying to read before start of memory", i);
    return bits[a + i];
   }

  void set(int i, boolean b)                                                    // Set a bit in memory
   {final int w = size(), a = at();
    if (i > w) stop("Trying to write beyond end of memory",   i, w);
    if (i < 0) stop("Trying to write before start of memory", i);
    bits[a + i] = b;
   }

  void ok(String expected) {Chip.ok(toString(), expected);}                     // Check memory is as expected

  public String toString()                                                      // Memory as string
   {final StringBuilder b = new StringBuilder();
    for (int i = size(); i > 0; --i) b.append(get(i-1) ? '1' : '0');
    return b.toString();
   }

  void set(Memory source, int offset)                                           // Copy source memory into this memory at the specified offset
   {final int t = size(), s = source.size(), m = min(t, s);
    if (offset + s > t) stop("Cannot write beyond end of memory");
    for (int i = 0; i < m; i++) set(offset+i, source.get(i));                   // Load the specified string into memory
   }

  void set(Memory source) {set(source, 0);}                                     // Set memory from source

//  Memory get(int At, int Width)                                                 // Get a sub section of this memory
//   {return memory(At, Width);
//   }

  void zero()                                                                   // Zero a memory
   {final int size = size();
    for (int i = 0; i < size; i++) set(i, false);
   }

  void shiftLeftFillWithZeros(int left)                                         // Shift left filling with zeroes
   {for (int i = size(); i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;      i < left; ++i) set(i,   false);
   }

  void shiftLeftFillWithOnes(int left)                                          // Shift left filling with ones
   {for (int i = size(); i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;      i < left; ++i) set(i,   true);
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

  int get()                                                                     // Get memory as an integer
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
    for (int i = 0; i < n; ++i) if (get(i)) return i;
    return n;
   }

  int countTrailingOnes()                                                       // Count trailing ones
   {final int n = size();
    for (int i = 0; i < n; ++i) if (!get(i)) return i;
    return n;
   }

// D2 Sub Memory                                                                // A sub memory provides access to a part of the full memory.  A sub memory can be mapped by a sub structure.

  abstract static class Layout extends Memory                                   // Variable/Array/Structure definition. Memory definitions can only be laid out once.
   {final String name;                                                          // Name of field
    int at;                                                                     // Offset of variable either from start of memory or from start of a structure
    int width;                                                                  // Number of width in field
    int depth;                                                                  // Depth of field
    Layout up;                                                                  // Chain to containing field
    Layout superStructure;                                                      // Containing super structure
    final Stack<Layout> fields = new Stack<>();                                 // Fields in the super structure in the order they appear in the memory layout. Only relevant in the outer most layout == the super structure,  where it is used for printing the structure and locating sub structures.

    Layout(String Name) {name = Name;}                                          // Create a new named memory layout

    Layout width   (int Width) {width = Width; return this;}                    // Set width or layout once it is known
    Layout position(int At)    {at    = At;    return this;}                    // Reposition array elements to take account of the index applied to the array

    void isSuperStructure()                                                     // Test whether this layout is a super structure containing all the other layouts
     {if (superStructure != this) stop("Not a super structure");
     }

    void isSubStructure()                                                       // Test whether this layout is a sub structure contained in a super structure
     {if (superStructure == this) stop("Not a sub structure");
     }

    abstract void layout(int at, int depth, Layout superStructure);             // Layout this field within the super structure.

    int size() {return width;}                                                  // Width of sub memory

    Memory mainMemory()                                                         // Create main memory with attached layout to describe its structure
     {if (up != null) stop("Not an outermost layout");
      final Memory m = new Memory();
      m.bits = new boolean[size()];
      m.zero();
      m.mainLayout = this;
      m.width = size();
      return m;
     }

    Memory subMemory()                                                          // Create main memory with attached layout to describe its structure
     {if (up == null) stop("Not an inner layout");
      final Memory m = new Memory();
      m.bits = superStructure.bits;
      m.mainLayout = superStructure;
      m.at    = at();
      m.width = size();
      return m;
     }

//D1 Layouts                                                                    // Layout memory as variables, arrays, structures, unions

    Layout layout()                                                             // Layout the structure based on the fields that describe it
     {fields.clear();
      layout(0, 0, this);
      bits = new boolean[width];
      for(Layout l : fields) l.superStructure = this;                           // Locate the super structure containing this field
      for(Layout l : fields) l.bits           = this.bits;                      // Locate the bits containing this layout element
      return this;
     }

    void sameSize(Layout layout)                                                // Check that two layouts have the same size
     {if (width != layout.width) stop("Layouts have different widths",
        width, layout.width);
     }

    String indent() {return "  ".repeat(depth);}                                // Indentation

    String printEntry()                                                         // Print the memory layout header
     {return String.format("%4d  %4d        %s  %s", at, width, indent(), name);
     }

    String print()                                                              // Walk the field list printing the memory layout headers
     {if (fields == null) return "";                                            // The structure has not been laid out
      final StringBuilder b = new StringBuilder();
      b.append(String.format("%4s  %4s  %4s    %s",
                             "  At", "Wide", "Size", "Field name\n"));
      for(Layout l : fields) b.append(""+l.printEntry()+"\n");                  // Print all the fields in the structure layout
      return b.toString();
     }

    public String toString()                                                    // Memory as string
     {final Memory m = memory(at, width);
      return m.toString();
     }

    Layout getField(String path)                                                // Path to field
     {final String[]names = path.split("\\.");                                  // Split path
      if (fields == null) return null;                                          // Not compiled
      search: for (Layout m : fields)                                           // Each field in structure
       {Layout p = m;                                                           // Start at this field and try to match the path
        for(int q = names.length; q > 0 && p != null; --q, p = p.up)            // Back track through names
         {if (!p.name.equals(names[q-1])) continue search;                      // Check path matches
         }
        return m;                                                               // Return this field if its path matches
       }
      return null;                                                              // No matching path
     }

    void shareMemory(Layout layout)                                             // Use the memory of the specified layout as long as it is the same size
     {sameSize(layout);                                                         // Make sure the two layouts are the same size
      bits = layout.bits;
     }

   //void set(Memory variable) {set(variable, at);}                              // Set a variable in memory

    void set(int variable)                                                      // Set a variable in memory from an integer
     {final Memory m = memory(width);
      m.set(variable);
      superStructure.set(m, at);
     }

    Memory memory() {return superStructure.memory(at, width);}                  // Get the memory associated with this layout

    int get()                                                                   // Get an integer representing the value of the memory
     {final Memory m = memory();
      return m.get();
     }

    void set(Layout source)                                                     // Set this variable from the supplied variable
     {if (width != source.width)
        stop("Variables have different widths", width, source.width);
      superStructure.set(source.memory(), at);                                           // Set memory for this variable from memory reffered to by source variable
     }

    abstract Layout duplicate(int At);                                          // Duplicate an element of this layout so we can modify it safely

    Layout duplicate()                                                          // Duplicate a set of nested layouts rebasing their start point to zero
     {final Layout l = duplicate(at);
      l.layout();
      l.shareMemory(this);
      return l;
     }

    void ok(int expected) {Chip.ok(get(), expected);}                           // Check the value of a variable
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
     {depth = Depth; superStructure.fields.push(this);                          // Relative to super structure
      element.layout(At, Depth+1, superStructure);                              // Layout sub structure
      position(at);                                                             // Position on index
      element.up = this;                                                        // Chain up to containing parent layout
      width = size * element.width;
     }

    Layout position(int At)                                                     // Reposition an array
     {at = At;
      element.position(at + index * element.width);
      return this;
     }

    String printEntry()                                                         // Print the field
     {return String.format("%4d  %4d  %4d  %s  %s",
                            at, width, size, indent(), name);
     }

    void setIndex(int Index) {index = Index; position(at);}                     // Sets the index for the current array element allowing us to set and get this element and all its sub elements.

    Layout duplicate(int At)                                                    // Duplicate an array so we can modify it safely
     {final Array a = new Array(name, element.duplicate(), size);
      a.width = width; a.at = at - At; a.depth = depth; a.index = index;
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
       {stop("Structure already contains field with this name",
             name, Field.name);
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
      for (int i = 0; i < Fields.length; ++i) addField(Fields[i]);              // Each field in this union
     }

    void addField(Layout Field)                                                 // Add a field to the union
     {final String n = Field.name;
      Field.up = this;                                                          // Chain up to containing structure
      if (subMap.containsKey(n)) stop(name, "already contains", n);
      subMap.put (n, Field);                                                    // Add as a sub structure by name
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
      for(Layout v : subMap.values()) v.position(at);
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
   {Memory m = memory(110);
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
   {Memory m = memory(110);
    for (int i = 1; i < 11; i++)
     {m.shiftLeftFillWithOnes(i);
      m.shiftLeftFillWithZeros(i);
     }                               m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000000");

    final Memory s = m.memory(10, 10);
    s.shiftRightFillWithZeros(2);    m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000000111111110000000000");
    s.shiftLeftFillWithOnes(1);      m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000001111111110000000000");
    s.shiftLeftFillWithZeros(1);     m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111100000000000");
    ok(s.countLeadingOnes  (), 9);
    ok(s.countTrailingZeros(), 1);

    final Memory S = s.memory(4, 2);
    S.zero();                        m.ok("10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011110011100000000000");
   }

  static void test_memory_set_from_memory()
   {Memory m = memory(8);
    m.shiftLeftFillWithOnes (2);
    m.shiftLeftFillWithZeros(2);
    m.shiftLeftFillWithOnes (2);
    m.shiftLeftFillWithZeros(2);
    Memory M = memory(16);
    m.ok("11001100"); M.ok("0000000000000000");
    Memory s = M.memory((M.size() - m.size())/2, m.size());
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

    ok(T.getField("outer.inner1.C1.c1").at,  8);
    ok(T.getField("outer.inner2.C2.c2").at, 36);
    ok(C2.at(2), 40);

    if (true)                                                                   // Set memory directly
     {b1.set( 1);
      b2.set(12);
      b1.ok ( 1);
      b2.ok (12);
                                     T.ok("0000000000000000000000000000110000000000000000000000000000010000");
      T.shiftRightFillWithSign(1);   T.ok("0000000000000000000000000000011000000000000000000000000000001000");
      T.shiftLeftFillWithOnes(2);    T.ok("0000000000000000000000000001100000000000000000000000000000100011");
      T.shiftLeftFillWithZeros(2);   T.ok("0000000000000000000000000110000000000000000000000000000010001100");
      T.shiftLeftFillWithZeros(25);  T.ok("1100000000000000000000000000000100011000000000000000000000000000");
      T.shiftRightFillWithSign(2);   T.ok("1111000000000000000000000000000001000110000000000000000000000000");
      ok(T.countLeadingOnes  (),  4);
      ok(T.countTrailingZeros(), 25);


      T.ok("1111000000000000000000000000000001000110000000000000000000000000");
      s2.ok(       "0000000000000000000000000100");
      b2.set(3);
      T.ok("1111000000000000000000000000001101000110000000000000000000000000");
      s2.ok(        "0000000000000000000000110100");
     }

    if (true)                                                                   // Set array elements
     {Memory m = T.layout();
      for (int i = 0; i < C1.size; i++)
       {C1.setIndex(i);
        c1.set(i % 4);
       }
      m.ok("0000000000000000000000000000000000000100111001001110010000000000");
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
        a.set(1);
        b.set(3);
        c.set(1);
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
    a.set(1);
    b.set(2);
    c.set(3);
    a.ok("01");
    b.ok("010");
    c.ok("11");
    s.ok("1101001");
    b.set(7);
    b.ok("111");
    s.ok("1111101");
   }

  static void test_variable_assign()
   {Variable  a = variable ("a", 2);
    Variable  b = variable ("b", 3);
    Variable  c = variable ("c", 2);
    Structure s = structure("s", a, b, c);

    s.layout();
    Structure S = (Structure)s.duplicate();

    ok(s.print(), """
  At  Wide  Size    Field name
   0     7          s
   0     2            a
   2     3            b
   5     2            c
""");

    ok(S.print(), """
  At  Wide  Size    Field name
   0     7          s
   0     2            a
   2     3            b
   5     2            c
""");

    Variable A = (Variable)s.getField("a");
    Variable C = (Variable)S.getField("c");
    A.set(3);  A.ok(3);
    a.ok(3);   b.ok(0); c.ok(0);
    C.set(A);
    a.ok(3);   b.ok(0); c.ok(3);
   }

  static void test_array()
   {final int N       = 4;

    Variable  element = new Variable("element", N);
    Array     array   = new Array   ("array",  element, N);
              array.layout();
    array.setIndex(1);
    ok(array.print(), """
  At  Wide  Size    Field name
   0    16     4    array
   4     4            element
""");

    element.set(3);
    element.ok(3);
    ok(element.get(), ((Variable)array.getField("element")).get());

    Array a = (Array)array.duplicate();
    ok(a.print(), """
  At  Wide  Size    Field name
   0    16     4    array
   4     4            element
""");

    array.setIndex(2);
    Array b = (Array)array.duplicate();
    ok(b.print(), """
  At  Wide  Size    Field name
   0    16     4    array
   8     4            element
""");


    Variable A = (Variable)(a.getField("element"));
    Variable B = (Variable)(b.getField("element"));

    a.ok("0000000000110000");
    b.ok("0000000000110000");
    B.set(A);
    a.ok("0000001100110000");
    b.ok("0000001100110000");
   }

  static void test_sub_variable2()
   {Variable  a = variable ("a", 4);
    Variable  b = variable ("b", 4);
    Variable  c = variable ("c", 2);
    Structure s = structure("inner", a, b, c);

    s.layout();
    ok(s.print(), """
  At  Wide  Size    Field name
   0    10          inner
   0     4            a
   4     4            b
   8     2            c
""");
    s.set(0b1100110011);
    s.ok(  "1100110011");
    b.ok("0011");
    b.set(0b101);
    b.ok("0101");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_memory();
    test_memory_sub();
    test_memory_set_from_memory();
    test_variable();
    test_double_array();
    test_sub_variable();
    test_sub_variable2();
    test_variable_assign();
    test_array();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
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
