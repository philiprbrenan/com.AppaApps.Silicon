//------------------------------------------------------------------------------
// A fixed size stack of ordered bit keys controlled by a unary number.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck extends RiscV implements Iterable<Stuck.Element>                    // Stuck: a fixed size stack controlled by a unary number. The unary number zero indicates an empty stuck stack.
 {final Unary u;                                                                // The unary number that controls the stuck stack
  final Element[]s;                                                             // The stuck stack
  final int max;                                                                // The maximum number of entries in the stuck stack.
  final int width;                                                              // The width of each object in the stuck in bits

//D1 Construction                                                               // Create a stuck stack

  Stuck(int Max, int Width)                                                     // Create the stuck stack
   {max = Max; width = Width;
    u = new Unary(Max);                                                         // Create the unary number that indicates the top of the stuck stack
    s = new Element[width];                                                     // The stuck stack
   }

  static Stuck stuck(int max, int width) {return new Stuck(max, width);}        // Create a stuck stack

  void clear() {u.set(0);}                                                      // Clear a stuck stack

  public Stuck clone()                                                          // Clone a stuck stack
   {final Stuck t = new Stuck(max, width);                                      // Create new stuck
    for (int i = 0; i < max; i++) t.s[i] = s[i];                                // Clone stuck stack
    return t;
   }

  class Element                                                                 // An element of the stack
   {final boolean[]data;
    Element(boolean[]Data)                                                      // Elements from array of bits
     {if (Data.length != width) stop("Width of element is", Data.length, "not", width);
      data = new boolean[width];
      for (int i = 0; i < width; i++) data[i] = Data[i];
     }
    Element(int Data)                                                           // Element from integer
     {data = new boolean[width];
      for (int i = 0; i < width; i++) data[i] = (Data & (1<<i)) != 0;
     }
    public String toString()                                                    // Convert to string
     {int v = 0;
      for (int i = 0; i < width; i++) v += data[i] ? 1<<i : 0;
      return ""+v;
     }
    public boolean equals(Element e)                                            // Compare two bit strings for equality
     {for (int i = 0; i < width; i++) if (data[i] != e.data[i]) return false;
      return true;
     }
    boolean[]bits() {return data;}                                              // Bits in element
   }


  Stack<Boolean> bits()                                                         // Stack of bits representing stuck
   {Stack<Boolean> b = u.bits();
    for (int i = 0; i < max; i++) concatBits(b, s[i].data);
    return b;
   }

//D1 Characteristics                                                            // Characteristics of the stuck stack

  int size() {return u.get();}                                                  // The current size of the stuck stack

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck stack

  boolean isFull()  {return size() >= u.max();}                                 // Check the stuck stack is full
  boolean isEmpty() {return size() <= 0;}                                       // Check the stuck stack is empty

  static class StuckMemoryLayout                                                // Memory layout for a stuck stack
   {final Variable  unary;                                                      // Current index of the top of the stuck
    final Variable  element;                                                    // An element of the stuck ,
    final Array     array;                                                      // The array of elements making the stuck
    final Structure stuck;                                                      // Structure of the stuck stack
    StuckMemoryLayout (int max, int width)                                      // Create the a memory layout for a stuck stack of specified size
     {final RiscV r = new RiscV();
      unary   = r.variable ("unary",   max);
      element = r.variable ("element", width);
      array   = r.array    ("array",   element, max);
      stuck   = r.structure("stuck",   unary, array);
      stuck.layout();                                                           // Layout memory
     }
    Memory memory() {return stuck.memory();}                                    // Create a memory to hold the stuck stack
   }

//D1 Actions                                                                    // Place and remove data to/from stuck stack

  void checkLength(boolean[]bits)                                               // Check the width of the supplied bits
   {final int b = bits.length;
    if (b != width) stop("Bits has width", b, "but expected", width);
   }

  void push(boolean[]bits)                                                      // Push an element onto the stuck stack
   {if (!u.canInc()) stop("Stuck is full");
    checkLength(bits);
    s[size()] = new Element(bits);
    u.inc();
   }

  void push(Element i) {push(i.data);}                                          // Push an element onto the stuck stack
  void push(int data) {push(new Element(data));}                                // Push an element onto the stuck stack

  Element pop()                                                                 // Pop an element from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    u.dec();
    return s[size()];
   }

  Element shift()                                                               // Shift an element from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    Element r = s[0];
    final int N = size();
    for (int i = 0; i < N-1; i++) s[i] = s[i+1];
    u.dec();
    return r;
   }

  void unshift(boolean[]bits)                                                   // Unshift an element from the stuck stack
   {if (!u.canInc()) stop("Stuck is full");
    checkLength(bits);
    final int N = size();
    for (int j = N; j > 0; j--) s[j] = s[j-1];
    s[0] = new Element(bits);
    u.inc();
   }

  void unshift(Element i) {unshift(i.data);}                                    // Unshift an element from the stuck stack
  void unshift(int data)  {unshift(new Element(data));}                         // Unshift an element onto the stuck stack

  Element removeElementAt(int i)                                                // Remove the element at 0 based index i and shift the elements above down one position
   {if (!u.canDec()) stop("Stuck is empty");
    final int N = size();
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    final Element r = s[i];
    for (int j = i; j < N-1; j++) s[j] = s[j+1];
    u.dec();
    return r;
   }

  void insertElementAt(boolean[]bits, int i)                                    // Insert an element at the indicated 0-based index after moving the elements above up one position
   {final int N = size();
    if (!u.canInc()) stop("Stuck is full");
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    checkLength(bits);
    for (int j = N; j > i; j--) s[j] = s[j-1];
    s[i] = new Element(bits);
    u.inc();
   }

  void insertElementAt(Element e, int i) {insertElementAt(e.data, i);}          // Insert an element at the indicated 0-based index after moving the elements above up one position
  void insertElementAt(int e, int i) {insertElementAt(new Element(e), i);}      // Insert an element at the indicated 0-based index after moving the elements above up one position

  Element elementAt(int i)                                                      // Get the element at a specified index
   {final int N = size();
    if (i >= N) stop("Too far up");
    if (i <  0) stop("Too far down");
    return s[i];
   }

  void setElementAt(boolean[]bits, int i)                                       // Set the value of the indexed location to the specified element
   {final int N = size();
    if (i >  N) stop("Too far up");
    if (i <  0) stop("Too far down");
    s[i] = new Element(bits);
   }

  void setElementAt(Element e, int i) {setElementAt(e.data, i);}                // Set the value of the indexed location to the specified element
  void setElementAt(int e, int i) {setElementAt(new Element(e), i);}            // Insert an element at the indicated 0-based index after moving the elements above up one position

  Element firstElement() {return elementAt(0);}                                 // Get the value of the first element
  Element  lastElement() {return elementAt(size()-1);}                          // Get the value of the last element

//D1 Search                                                                     // Search a stuck stack.

  public int indexOf(boolean[]bits)                                             // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.
   {final int N = size();
    final Element keyToFind = new Element(bits);
    for (int i = 0; i < N; i++) if (keyToFind.equals(s[i])) return i;
    return -1;                                                                  // Not found
   }

  public int indexOf(Element keyToFind) {return indexOf(keyToFind.data);}       // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.
  public int indexOf(int keyToFind) {return indexOf(new Element(keyToFind));}   // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.

//D1 Iterate                                                                    // Iterate a stuck stack

  public Iterator<Element> iterator() {return new ElementIterator();}           // Create an iterator to iterate through the stack

  class ElementIterator implements Iterator<Element>                            // Iterator for the stack
   {int nextElement = 0;

    public boolean hasNext() {return nextElement < size();}                     // Another element to iterate

    public Element next() {return s[nextElement++];}
   }

//D1 Print                                                                      // Print a stuck stack

  public String toString()                                                      // Print a stuck stack
   {final StringBuilder b = new StringBuilder("Stuck(");
    final int N = size();
    for (int i = 0; i < N; i++) b.append(""+s[i].toString()+", ");
    if (N > 0) b.setLength(b.length()-2);
    b.append(")");
    return b.toString();
   }

//D0 Tests                                                                      // Test stuck stack

  static void test_action()
   {var s = stuck(4, 4);
    s.push(1);
    s.push(2);
    s.push(3);
    s.ok("Stuck(1, 2, 3)");

    var a = s.pop();                 s.ok("Stuck(1, 2)");       ok(a, 3);
    s.unshift(3);                    s.ok("Stuck(3, 1, 2)");
    var b = s.shift();               s.ok("Stuck(1, 2)");       ok(b, 3);
    s.insertElementAt(3, 2);         s.ok("Stuck(1, 2, 3)");
    s.insertElementAt(4, 2);         s.ok("Stuck(1, 2, 4, 3)"); ok(s.isFull());

    var c = s.removeElementAt(2);    s.ok("Stuck(1, 2, 3)");    ok(c, 4);
    var d = s.removeElementAt(2);    s.ok("Stuck(1, 2)");       ok(d, 3);
    s.insertElementAt(3, 0);         s.ok("Stuck(3, 1, 2)");
    var e = s.removeElementAt(0);    s.ok("Stuck(1, 2)");       ok(e, 3);
    var f = s.elementAt(0);          ok(f, 1);
    var g = s.elementAt(1);          ok(g, 2);
    s.setElementAt(4, 1);            s.ok("Stuck(1, 4)");
    s.setElementAt(2, 0);            s.ok("Stuck(2, 4)");
    s.removeElementAt(0);            s.ok("Stuck(4)");          ok(!s.isEmpty());
    s.removeElementAt(0);            s.ok("Stuck()");           ok( s.isEmpty());
   }

  static void test_push_shift()
   {var s = stuck(4, 4);                                        ok(s.size(), 0); ok(s.isEmpty());
    s.push(1);                       s.ok("Stuck(1)");          ok(s.size(), 1);
    s.push(2);                       s.ok("Stuck(1, 2)");       ok(s.size(), 2);
    s.push(3);                       s.ok("Stuck(1, 2, 3)");    ok(s.size(), 3);
    s.push(4);                       s.ok("Stuck(1, 2, 3, 4)"); ok(s.size(), 4);
    var f = s.firstElement();                                   ok(f, 1);
    var l = s.lastElement();                                    ok(l, 4);
    var a = s.shift();               s.ok("Stuck(2, 3, 4)");    ok(s.size(), 3); ok(a, 1);
    var b = s.shift();               s.ok("Stuck(3, 4)");       ok(s.size(), 2); ok(b, 2);
    var c = s.shift();               s.ok("Stuck(4)");          ok(s.size(), 1); ok(c, 3);
    var d = s.shift();               s.ok("Stuck()");           ok(s.size(), 0); ok(d, 4);
   }

  static void test_insert_remove()
   {var s = stuck(4, 4);             s.ok("Stuck()");           ok(s.size(), 0); ok(s.isEmpty());
    s.insertElementAt(1, 0);         s.ok("Stuck(1)");          ok(s.size(), 1);
    s.insertElementAt(2, 1);         s.ok("Stuck(1, 2)");       ok(s.size(), 2);
    s.insertElementAt(3, 2);         s.ok("Stuck(1, 2, 3)");    ok(s.size(), 3);
    s.insertElementAt(4, 3);         s.ok("Stuck(1, 2, 3, 4)"); ok(s.size(), 4); ok(s.isFull());
    var a = s.removeElementAt(0);    s.ok("Stuck(2, 3, 4)");    ok(s.size(), 3); ok(a, 1);
    var b = s.removeElementAt(0);    s.ok("Stuck(3, 4)");       ok(s.size(), 2); ok(b, 2);
    var c = s.removeElementAt(0);    s.ok("Stuck(4)");          ok(s.size(), 1); ok(c, 3);
    var d = s.removeElementAt(0);    s.ok("Stuck()");           ok(s.size(), 0); ok(d, 4);
   }

  static void test_search()
   {final int N = 4;
    var s = stuck(N, 4);
    for (int i = 1; i <= N; i++) s.push(i);
    s.ok("Stuck(1, 2, 3, 4)");
    ok(s.indexOf(0), -1);
    for (int i = 1; i <= N; i++) ok(s.indexOf(i), i-1);
   }

  static void test_iterate_zero()
   {final int N = 4;
    var s = stuck(N, 4);
    final StringBuilder b = new StringBuilder();
    for (Element i : s) b.append(""+i+", ");
    if (b.length() > 0) b.setLength(b.length()-2);
    ok(b.toString().equals(""));
   }

  static void test_iterate()
   {final int N = 4;
    var s = stuck(N, 4);
    for (int i = 1; i <= N; i++) s.push(i);
    s.ok("Stuck(1, 2, 3, 4)");
    final StringBuilder b = new StringBuilder();
    for (Element i : s) b.append(""+i+", ");
    if (b.length() > 0) b.setLength(b.length()-2);
    ok(b.toString().equals("1, 2, 3, 4"));
   }

  static void test_clear()
   {var s = stuck(4, 4);             ok(s.size(), 0);
    s.push(1); s.ok("Stuck(1)");     ok(s.size(), 1);
    s.push(2); s.ok("Stuck(1, 2)");  ok(s.size(), 2);
    s.clear(); ok(s.isEmpty());
    s.push(3); s.ok("Stuck(3)");     ok(s.size(), 1);
    s.push(4); s.ok("Stuck(3, 4)");  ok(s.size(), 2);
   }

  static void test_structure()
   {StuckMemoryLayout s = new StuckMemoryLayout(4, 4);
    Memory            m = s.memory();
    s.unary.set(m, 7);
    s.array.set(m, 0,  1);
    s.array.set(m, 1,  3);
    s.array.set(m, 2,  7);
    ok(m, "00000111001100010111");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_action();
    test_push_shift();
    test_insert_remove();
    test_clear();
    test_search();
    test_iterate_zero();
    test_iterate();
    test_structure();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
     }
   }
 }
