//------------------------------------------------------------------------------
// A fixed size stack of ordered bit keys controlled by a unary number.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck extends RiscV                                                       // Stuck: a fixed size stack controlled by a unary number. The unary number zero indicates an empty stuck stack.
 {final Unary             unary;                                                // The layout of the stuck stack
  final StuckMemoryLayout stuckMemoryLayout;                                    // The layout of the stuck stack
  final Stack<Memory>     stuckMemory = new Stack<>();                          // The memory of the stuck stack
  final int max;                                                                // The maximum number of entries in the stuck stack.
  final int width;                                                              // The width of each object in the stuck in bits

//D1 Construction                                                               // Create a stuck stack

  Stuck(int Max, int Width)                                                     // Create the stuck stack
   {max = Max; width = Width;
    unary             = new Unary(max);                                         // The stuck stack
    stuckMemoryLayout = new StuckMemoryLayout("stuck");                         // The stuck stack memory layout
    stuckMemory.push(stuckMemoryLayout.memory());                               // The stuck stack memory
   }

  void stuckMemoryOpen(Memory memory)                                           // Assign some memory for a unary number
   {final int m = memory.size(), w = stuckMemoryLayout.width;                   // Check memory sizes
    if (m != w)
      stop("Memory size is different from expected", w, "but got", m);

    stuckMemory.push(memory);                                                   // Open memory for stuck stack
    unaryMemoryOpen();                                                          // Derive the unary memory from the stuck memory
   }

  Memory getStuckMemory() {return stuckMemory.lastElement();}                   // Get memory containing stuck stack

  void stuckMemoryClose()                                                       // Finished with current memeory
   {if (stuckMemory.size() == 0) stop("Memory stack underflow");
    unaryMemoryClose();                                                         // Close unary memory
    stuckMemory.pop();                                                          // Close stuck meory
   }
  static Stuck stuck(int max, int width) {return new Stuck(max, width);}        // Create a stuck stack

  void clear() {getStuckMemory().zero();}                                       // Clear a stuck stack

//D1 Characteristics                                                            // Characteristics of the stuck stack

  void unaryMemoryOpen()                                                        // Set the memory for the unary number
   {unary.memoryOpen(stuckMemoryLayout.unary.subMemory(getStuckMemory()));
   }

  void unaryMemoryClose() {unary.memoryClose();}                                // Release the memory for the unary number

  int size()                                                                    // The current size of the stuck stack
   {stuckMemoryClose();                                                          // Get the memory for the unary number controlling the stack
    final int n = unary.get();                                                 // Value of unary is the size of the stuck stack
    stuckMemoryClose();
    return n;                                                                   // Get the memory for the unary number controlling the stack
   }

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck stack

  boolean isFull()  {return size() >= max;}                                     // Check the stuck stack is full
  boolean isEmpty() {return size() <= 0;}                                       // Check the stuck stack is empty

  class StuckMemoryLayout extends Structure                                     // Memory layout for a stuck stack
   {final Variable unary;                                                       // Unary vector to show used positions in stuck stack
    final Variable element;                                                     // Element of stuck stack
    final Array    array;                                                       // Array of Memorys implementing the stuck stack

    StuckMemoryLayout(String name)                                              // Create the a memory layout for a unary number
     {super(name);                                                              // Unary vector to show used positions in stuck stack
      unary   = new Unary(max).layout;                                          // Unary vector to show used positions in stuck stack
      element = new Variable("Memory", width);                                  // Memory on stuck stack
      array   = new Array   ("array",  element, max);                           // Array of Memorys implementing the stuck stack
      addField(unary);                                                          // Add the unary indicator
      addField(array);                                                          // Add the array
      layout();                                                                 // Layout memory
say(print());
     }
   }

  StuckMemoryLayout stuckMemoryLayout() {return new StuckMemoryLayout("Stuck");}// Memory layout for a stuck stack

//D1 Actions                                                                    // Place and remove data to/from stuck stack

  void push(Memory stuckMemory, Memory elementMemory)                                                      // Push an Memory onto the stuck stack
   {stuckMemoryOpen(stuckMemory);
    if (!unary.canInc()) stop("Stuck is full");                                 // Check there is room on the stack
    final int n = unary.get();                                                  // Current size of  Stuck Stack
    stuckMemoryLayout.array.setIndex(n);                                        // Index stuck memory
    stuckMemoryLayout.element.set(stuckMemory, elementMemory);                  // Set memory of stuck stack from supplied memory
    unary.inc();
    stuckMemoryClose();
   }
/*
  void push(Memory i) {push(i.data);}                                          // Push an Memory onto the stuck stack
  void push(int data) {push(new Memory(data));}                                // Push an Memory onto the stuck stack

  Memory pop()                                                                 // Pop an Memory from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    u.dec();
    return s[size()];
   }

  Memory shift()                                                               // Shift an Memory from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    Memory r = s[0];
    final int N = size();
    for (int i = 0; i < N-1; i++) s[i] = s[i+1];
    u.dec();
    return r;
   }

  void unshift(boolean[]bits)                                                   // Unshift an Memory from the stuck stack
   {if (!u.canInc()) stop("Stuck is full");
    checkLength(bits);
    final int N = size();
    for (int j = N; j > 0; j--) s[j] = s[j-1];
    s[0] = new Memory(bits);
    u.inc();
   }

  void unshift(Memory i) {unshift(i.data);}                                    // Unshift an Memory from the stuck stack
  void unshift(int data)  {unshift(new Memory(data));}                         // Unshift an Memory onto the stuck stack

  Memory removeElementAt(int i)                                                // Remove the Memory at 0 based index i and shift the Memorys above down one position
   {if (!u.canDec()) stop("Stuck is empty");
    final int N = size();
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    final Memory r = s[i];
    for (int j = i; j < N-1; j++) s[j] = s[j+1];
    u.dec();
    return r;
   }

  void insertElementAt(boolean[]bits, int i)                                    // Insert an Memory at the indicated 0-based index after moving the Memorys above up one position
   {final int N = size();
    if (!u.canInc()) stop("Stuck is full");
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    checkLength(bits);
    for (int j = N; j > i; j--) s[j] = s[j-1];
    s[i] = new Memory(bits);
    u.inc();
   }

  void insertElementAt(Memory e, int i) {insertElementAt(e.data, i);}          // Insert an Memory at the indicated 0-based index after moving the Memorys above up one position
  void insertElementAt(int e, int i) {insertElementAt(new Memory(e), i);}      // Insert an Memory at the indicated 0-based index after moving the Memorys above up one position

  Memory MemoryAt(int i)                                                      // Get the Memory at a specified index
   {final int N = size();
    if (i >= N) stop("Too far up");
    if (i <  0) stop("Too far down");
    return s[i];
   }

  void setMemoryAt(boolean[]bits, int i)                                       // Set the value of the indexed location to the specified Memory
   {final int N = size();
    if (i >  N) stop("Too far up");
    if (i <  0) stop("Too far down");
    s[i] = new Memory(bits);
   }

  void setMemoryAt(Memory e, int i) {setMemoryAt(e.data, i);}                // Set the value of the indexed location to the specified Memory
  void setMemoryAt(int e, int i) {setMemoryAt(new Memory(e), i);}            // Insert an Memory at the indicated 0-based index after moving the Memorys above up one position

  Memory firstMemory() {return MemoryAt(0);}                                 // Get the value of the first Memory
  Memory  lastMemory() {return MemoryAt(size()-1);}                          // Get the value of the last Memory

//D1 Search                                                                     // Search a stuck stack.

  public int indexOf(boolean[]bits)                                             // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.
   {final int N = size();
    final Memory keyToFind = new Memory(bits);
    for (int i = 0; i < N; i++) if (keyToFind.equals(s[i])) return i;
    return -1;                                                                  // Not found
   }

  public int indexOf(Memory keyToFind) {return indexOf(keyToFind.data);}       // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.
  public int indexOf(int keyToFind) {return indexOf(new Memory(keyToFind));}   // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.

//D1 Iterate                                                                    // Iterate a stuck stack

  public Iterator<Memory> iterator() {return new MemoryIterator();}           // Create an iterator to iterate through the stack

  class MemoryIterator implements Iterator<Memory>                            // Iterator for the stack
   {int nextMemory = 0;

    public boolean hasNext() {return nextMemory < size();}                     // Another Memory to iterate

    public Memory next() {return s[nextMemory++];}
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
*/
//D0 Tests                                                                      // Test stuck stack

  static void test_action()
   {Stuck  s = stuck(4, 4);
    Memory S = s.stuckMemoryLayout.memory();
    MemoryLayout e = s.stuckMemoryLayout().element;
                 e.layout();
    Memory       E = e.memory();

    E.set(1); s.push(S, E);
    E.set(2); s.push(S, E);
    E.set(3); s.push(S, E);
    say(S.size());
    //s.ok("Stuck(1, 2, 3)");
   }
/*  var a = s.pop();                 s.ok("Stuck(1, 2)");       ok(a, 3);
    s.unshift(3);                    s.ok("Stuck(3, 1, 2)");
    var b = s.shift();               s.ok("Stuck(1, 2)");       ok(b, 3);
    s.insertElementAt(3, 2);         s.ok("Stuck(1, 2, 3)");
    s.insertElementAt(4, 2);         s.ok("Stuck(1, 2, 4, 3)"); ok(s.isFull());

    var c = s.removeElementAt(2);    s.ok("Stuck(1, 2, 3)");    ok(c, 4);
    var d = s.removeElementAt(2);    s.ok("Stuck(1, 2)");       ok(d, 3);
    s.insertElementAt(3, 0);         s.ok("Stuck(3, 1, 2)");
    var e = s.removeElementAt(0);    s.ok("Stuck(1, 2)");       ok(e, 3);
    var f = s.MemoryAt(0);          ok(f, 1);
    var g = s.MemoryAt(1);          ok(g, 2);
    s.setMemoryAt(4, 1);            s.ok("Stuck(1, 4)");
    s.setMemoryAt(2, 0);            s.ok("Stuck(2, 4)");
    s.removeElementAt(0);            s.ok("Stuck(4)");          ok(!s.isEmpty());
    s.removeElementAt(0);            s.ok("Stuck()");           ok( s.isEmpty());
   }

  static void test_push_shift()
   {var s = stuck(4, 4);                                        ok(s.size(), 0); ok(s.isEmpty());
    s.push(1);                       s.ok("Stuck(1)");          ok(s.size(), 1);
    s.push(2);                       s.ok("Stuck(1, 2)");       ok(s.size(), 2);
    s.push(3);                       s.ok("Stuck(1, 2, 3)");    ok(s.size(), 3);
    s.push(4);                       s.ok("Stuck(1, 2, 3, 4)"); ok(s.size(), 4);
    var f = s.firstMemory();                                   ok(f, 1);
    var l = s.lastMemory();                                    ok(l, 4);
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
    for (Memory i : s) b.append(""+i+", ");
    if (b.length() > 0) b.setLength(b.length()-2);
    ok(b.toString().equals(""));
   }

  static void test_iterate()
   {final int N = 4;
    var s = stuck(N, 4);
    for (int i = 1; i <= N; i++) s.push(i);
    s.ok("Stuck(1, 2, 3, 4)");
    final StringBuilder b = new StringBuilder();
    for (Memory i : s) b.append(""+i+", ");
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
*/
  static void oldTests()                                                        // Tests thought to be in good shape
   {test_action();
    //test_push_shift();
    //test_insert_remove();
    //test_clear();
    //test_search();
    //test_iterate_zero();
    //test_iterate();
    //test_structure();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_action();
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
