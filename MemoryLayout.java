//------------------------------------------------------------------------------
// Layout memory
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, emulate and layout digital a binary tree on a silicon chip.

import java.util.*;

//D1 Construct                                                                  // Construct a Risc V program and execute it

public class MemoryLayout extends Chip                                          // Variable/Array/Structure definition. Memory definitions can only be laid out once.
 {final String name;                                                            // Name of field
  int at;                                                                       // Offset of variable either from start of memory or from start of a structure
  int width;                                                                    // Number of width in field
  int depth;                                                                    // Depth of field
  MemoryLayout up;                                                              // Chain to containing field
  Stack<MemoryLayout> fields;                                                   // Fields in the super structure in the order they appear in the memory layout. Only relevant in the outer most layout == the super structure,  where it is used for printing the structure and locating sub structures.

  MemoryLayout(String Name) {name  = Name; fields();}                           // Create a new named memory layout
  MemoryLayout()            {this(null);}                                       // Create a new unnamed memory layout

  void fields() {}                                                              // Create these fields

  MemoryLayout width(int Width)   {width = Width; return this;}

  void layout(int at, int depth, MemoryLayout superStructure) {stop("Override");} // Layout this field within the super structure.
  void layout() {fields = new Stack<>(); layout(0, 0, this);}                   // Layout the fields in the structure defined by this field

  void position(int At) {at = At;}                                             // Reposition array elements to take account of the index applied to the array

  String indent() {return "  ".repeat(depth);}                                  // Indentation

  public String toString()                                                      // Print the memory layout header
   {return String.format("%4d  %4d        %s  %s", at, width, indent(), name);
   }

  public String print()                                                         // Walk the field list printing the memory layout headers
   {if (fields == null) return "";                                              // The structure has not been laid out
    final StringBuilder b = new StringBuilder();
    b.append(String.format("%4s  %4s  %4s    %s", "  At", "Wide", "Size", "Field name\n"));
    for(MemoryLayout m : fields) b.append(""+m+"\n");                           // Print all the fields in the structure layout
    return b.toString();
   }

  MemoryLayout getFieldDef(String path)                                         // Path to field
   {final String[]names = path.split("\\.");                                    // Split path
    if (fields == null) return null;                                            // Not compiled
    for (MemoryLayout m : fields)                                               // Each field in structure
     {MemoryLayout p = m;                                                       // Start at this field and try to match the path
      boolean found = true;
      for(int q = names.length; q > 0 && p != null && found; --q, p = p.up)     // Back track through names
       {found = p.name.equals(names[q-1]);                                      // Check path matches
       }
      if (found) return m;                                                      // Return this field if its path matches
     }
    return null;                                                                // No matching path
   }

  Memory memory()                                                               // Create enough memory for this item. The item must be an outermost item
   {if (at > 0) stop("Layout not outer");
    return new Memory(width);
   }

  void set(Memory memory, Memory variable) {memory.set(variable, at);}          // Set a variable in memory
  void set(Memory memory, int variable)                                         // Set a variable in memory from an integer
   {final Memory m = new Memory(width, variable);
    set(memory, m);
   }

  Memory get(Memory memory) {return memory.get(at, width);}                     // Get a variable from memory as copied bits
  int getInt(Memory memory) {return get(memory).getInt();}                      // Get a variable from memory as an integer
  Memory.Sub subMemory(Memory memory) {return memory.sub(at, width);}           // Get a variable from memory as sub memory

  void checkLength(Memory memory)                                               // Check that the memory can accommodate the memory layout
   {final int w = at + width, m = memory.size();
    if (w >= m) stop("Variable beyond the end of memory", w, m);
   }
  static class Variable extends MemoryLayout                                    // Variable
   {Variable(String name, int Width)
     {super(name); width(Width);
     }
    void layout(int At, int Depth, MemoryLayout superStructure)
     {at = At; depth = Depth; superStructure.fields.push(this);
     }
   }

  static class Array extends MemoryLayout                                       // Array definition.
   {int size;                                                                   // Dimension of array
    int index = 0;                                                              // Index of array element to access
    MemoryLayout element;                                                       // The elements of this array
    Array(String Name, MemoryLayout Element, int Size)
     {super(Name);
      size(Size).element(Element);
     }
    Array    size(int Size)             {size    = Size;    return this;}       // Set the size of the array
    Array element(MemoryLayout Element) {element = Element; return this;}       // The type of the element in the array
    int at(int i)                       {return at+i*element.width;}            // Offset of this array element in the structure

    void layout(int At, int Depth, MemoryLayout superStructure)                 // Compile this variable so that the size, width and byte fields are correct
     {at = At; depth = Depth; superStructure.fields.push(this);
      element.layout(at, Depth+1, superStructure);
      element.up = this;                                                        // Chain up
      width = size * element.width;
     }
    void position(int At) {at = At; element.position(at + index * element.width);}

    public String toString()                                                    // Print the field
     {return String.format("%4d  %4d  %4d  %s  %s",
                            at, width, size, indent(), name);
     }

    void setIndex(int Index) {index = Index; position(at);}                     // Sets the index for the current array element allowing us to set and get this element and all its sub elements.

/*  void set(Memory memory, int index, Memory variable)                         // Set an array element in memory
     {final int w = element.width, v = variable.size(), o = at + index * w;     // Size of an array element, size of the supplied variable used to set the array element,  offset in array in bits
      if (w != v) stop("Array element has width", w,
       "but variable being assigned has width", v);
      for (int i = 0; i < element.width; i++)
        memory.bits[o + i] = variable.bits[i];                                  // Set bits of array element
     }

    void set(Memory memory, int index, int variable)                            // Set an array element from an integer
     {final Memory m = new Memory(element.width, variable);
      set(memory, index, m);
     }

    Memory get(Memory memory, int index)                                        // Get an array element from memory as bits
     {final int w = element.width, o = at + index * w;                          // Size of an array element, size of the supplied variable used to set the array element,  offset in array in bits
      final Memory m = new Memory(w);
      for (int i = 0; i < w; i++) m.bits[i] = memory.bits[o + i];               // Copy bits of array element
      return m;
     }

    int getInt(Memory memory, int index)                                        // Get an array element from memory as an integer
     {final Memory m = get(memory, index);                                      // Memory associated with variable
      final int n = min(element.width, Integer.SIZE);
      int v = 0;
      for (int i = 0; i < n; i++) if (m.bits[i]) v |= (1<<i);                   // Convert bits to int
      return v;
     }
*/
   }

  static class Structure extends MemoryLayout                                   // Structure laid out in memory
   {final Map<String,MemoryLayout> subMap   = new TreeMap<>();                  // Unique variables contained inside this variable
    final Stack     <MemoryLayout> subStack = new Stack  <>();                  // Order of variables inside this variable

    Structure(String Name, MemoryLayout...Fields)                               // Fields in the structure
     {super(Name);
      for (int i = 0; i < Fields.length; ++i) addField(Fields[i]);              // Each field in this structure
     }

    void addField(MemoryLayout Field)                                           // Add additional fields
     {Field.up = this;                                                          // Chain up to containing structure
      if (subMap.containsKey(Field.name))
       {stop("Structure already contains field with this name", name, Field.name);
       }
     subMap.put (Field.name, Field);                                            // Add as a sub structure by name
     subStack.push(Field);                                                      // Add as a sub structure in order
     }

    void layout(int at, int Depth, MemoryLayout superStructure)                 // Compile this variable so that the size, width and byte fields are correct
     {int w = 0;
      width = 0;
      depth = Depth;
      superStructure.fields.push(this);
      for(MemoryLayout v : subStack)                                            // Layout sub structure
       {v.at = at+width;
        v.layout(v.at, Depth+1, superStructure);
        width += v.width;
       }
     }

    void position(int At)                                                       // Reposition this structure to allow access to array elements via an index
     {at = At;
      int w = 0;
      for(MemoryLayout v : subStack)                                            // Layout sub structure
       {v.position(v.at = at+w);
        w += v.width;
       }
     }
   }

  static class Union extends MemoryLayout                                       // Union of structures laid out in memory
   {final Map<String,MemoryLayout> subMap = new TreeMap<>();                    // Unique variables contained inside this variable

    Union(String Name, MemoryLayout...Fields)                                   // Fields in the union
     {super(Name);
      for (int i = 0; i < Fields.length; ++i)                                   // Each field in this union
       {final MemoryLayout s = Fields[i];
        s.up = this;                                                            // Chain up to containing structure
        if (subMap.containsKey(s.name)) stop(name, "already contains", s.name);
        subMap.put (s.name, s);                                                 // Add as a sub structure by name
       }
     }

    void layout(int at, int Depth, MemoryLayout superStructure)                 // Compile this variable so that the size, width and byte fields are correct
     {int w = 0;
      width = 0;
      depth = Depth;
      superStructure.fields.push(this);
      for(MemoryLayout v : subMap.values())                                     // Find largest substructure
       {v.at = at;
        v.layout(v.at, Depth+1, superStructure);
        width = max(width, v.width);                                            // Space occupied is determined by largest element of union
       }
     }

    void position(int At)                                                       // Position elemenst of this union to allow arrays to access their elements by an index
     {at = At;
      for(MemoryLayout v : subMap.values()) {v.position(at);}
     }
   }

  Variable  variable (String name, int width)                    {return new Variable (name, width);}
  Array     array    (String name, MemoryLayout   ml, int width) {return new Array    (name, ml, width);}
  Structure structure(String name, MemoryLayout...ml)            {return new Structure(name, ml);}
  Union     union    (String name, MemoryLayout...ml)            {return new Union    (name, ml);}

//D0 Tests                                                                      // Tests

  static void test_variable()
   {MemoryLayout r = new MemoryLayout("");

    Variable  a1 = r.variable ("a1", 4);
    Variable  b1 = r.variable ("b1", 4);
    Variable  c1 = r.variable ("c1", 2);
    Array     C1 = r.array    ("C1", c1, 10);
    Structure s1 = r.structure("inner1", a1, b1, C1);

    Variable  a2 = r.variable ("a2", 4);
    Variable  b2 = r.variable ("b2", 4);
    Variable  c2 = r.variable ("c2", 2);
    Array     C2 = r.array    ("C2", c2, 10);
    Structure s2 = r.structure("inner2", a2, b2, C2);

    Variable  a3 = r.variable ("a3",  8);
    Variable  b3 = r.variable ("b3",  8);
    Variable  c3 = r.variable ("c3",  2);
    Array     C3 = r.array    ("C3", c3, 4);
    Union     u3 = r.union    ("inner3", a3, b3, C3);

    Array     A1 = r.array    ("Array1", s1,  2);
    Array     A2 = r.array    ("Array2", s2,  2);
    Structure T  = r.structure("outer",  s1, s2, u3);

    T.layout();
    T.layout();                                                                 // Layout multiple times is ok
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

    ok(T.getFieldDef("outer.inner1.C1.c1").at,  8);
    ok(T.getFieldDef("outer.inner2.C2.c2").at, 36);
    ok(C2.at(2), 40);

    if (true)                                                                   // Set memory directly
     {final Memory m = T.memory();
         b1.set   (m,   1);
         b2.set   (m,  12);
      ok(b1.getInt(m),  1);
      ok(b2.getInt(m), 12);

      m.ok("0000000000000000000000000000110000000000000000000000000000010000");
      m.shiftRightFillWithSign(1);
      m.ok("0000000000000000000000000000011000000000000000000000000000001000");
      m.shiftLeftFillWithOnes(2);
      m.ok("0000000000000000000000000001100000000000000000000000000000100011");
      m.shiftLeftFillWithZeros(2);
      m.ok("0000000000000000000000000110000000000000000000000000000010001100");
      m.shiftLeftFillWithZeros(25);
      m.ok("1100000000000000000000000000000100011000000000000000000000000000");
      m.shiftRightFillWithSign(2);
      m.ok("1111000000000000000000000000000001000110000000000000000000000000");
      ok(m.countLeadingOnes  (),  4);
      ok(m.countTrailingZeros(), 25);
     }

    if (true)                                                                   // Set array elements
     {Memory m = T.memory();
      for (int i = 0; i < C1.size; i++)
       {C1.setIndex(i);
        c1.set(m, i % 4);
       }
      ok(""+m, "0000000000000000000000000000000000000100111001001110010000000000");
     }
   }

  static void test_double_array()
   {MemoryLayout r = new MemoryLayout();

    Variable  a = r.variable ("a", 2);
    Variable  b = r.variable ("b", 3);
    Variable  c = r.variable ("c", 2);
    Structure s = r.structure("s", a, b, c);
    Array     A = r.array    ("A", s, 4);
    Array     B = r.array    ("B", A, 3);

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

    Memory m = B.memory();
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
   {MemoryLayout  r = new MemoryLayout();

    Variable  a = r.variable ("a", 2);
    Variable  b = r.variable ("b", 3);
    Variable  c = r.variable ("c", 2);
    Structure s = r.structure("s", a, b, c);

    s.layout();
    ok(s.print(), """
  At  Wide  Size    Field name
   0     7          s
   0     2            a
   2     3            b
   5     2            c
""");

    Memory m = s.memory();
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

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_variable();
    test_double_array();
    test_sub_variable();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      Chip.testSummary();
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(Chip.fullTraceBack(e));
     }
   }
 }
