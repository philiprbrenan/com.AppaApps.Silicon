//------------------------------------------------------------------------------
// A fixed size stack of ordered bit keys controlled by a unary number.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck extends Memory.Structure                                            // Stuck: a fixed size stack controlled by a unary number. The unary number zero indicates an empty stuck stack.
 {final Unary unary;                                                            // The layout of the stuck stack
  final Memory.Variable element;                                                // An element of the stuck stack
  final Memory.Array    array;                                                  // The array holding the elements of the stuck stack
  final int max;                                                                // The maximum number of entries in the stuck stack.
  final int width;                                                              // The width of each object in the stuck in bits

//D1 Construction                                                               // Create a stuck stack

  Stuck(int Max, int Width)                                                     // Create the stuck stack
   {super("Stuck");                                                             // Containing structure layout
    max = Max; width = Width;
    unary   = new Unary(max);                                                   // Unary number showing which elements in the stack are valid
    element = variable("element", width);                                       // An element of the stuck stack
    array   = array("array", element, max);                                     // An array of elements comprising the stuck stack
    addField(unary);                                                            // Preventing from doing this earlier by Java forcing super to go first
    addField(array);
    layout();                                                                   // Layout the structure of the stuck stack
   }

  static Stuck stuck(int max, int width) {return new Stuck(max, width);}        // Create a stuck stack

  void clear() {unary.zero();}                                                  // Clear a stuck stack

//D1 Characteristics                                                            // Characteristics of the stuck stack

  int stuckSize() {return unary.get();}                                         // The current size of the stuck stack

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck stack

  boolean isFull()  {return stuckSize() >= max;}                                // Check the stuck stack is full
  boolean isEmpty() {return stuckSize() <= 0;}                                  // Check the stuck stack is empty

//D1 Actions                                                                    // Place and remove data to/from stuck stack

  void push(Memory ElementToPush)                                               // Push an element as memory onto the stuck stack
   {if (!unary.canInc()) stop("Stuck is full");                                 // Check there is room on the stack
    final int n = stuckSize();                                                  // Current size of  Stuck Stack
    array.setIndex(n);                                                          // Index stuck memory
    element.set(ElementToPush);                                                 // Set memory of stuck stack from supplied memory
    unary.inc();
   }

  void push(int    Value) {push(memoryFromInt(width, Value));}                  // Push an integer onto the stuck stack
  void push(String Value) {push(memoryFromString(Value));}                      // Push an Memory onto the stuck stack

  Memory pop()                                                                  // Pop an element as memory from the stuck stack
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to pop on the stuck stack
    unary.dec();                                                                // New number of elements on stuck stack
    final int n = stuckSize();                                                  // Current size of  Stuck Stack
    array.setIndex(n);                                                          // Index stuck memory
    return element.memory();                                                    // Get memory from stuck stack
   }

  Memory shift()                                                                // Shift an element as memory from the stuck stack
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to shift on the stuck stack
    unary.dec();                                                                // New number of elements on stuck stack
    array.setIndex(0);                                                          // Index stuck memory
    Memory m = element.memory().duplicate();                                    // Copy the slice of memory
    final int N = stuckSize();                                                  // Current size of  Stuck Stack
    for (int i = 0; i < N; i++)                                                 // Shift the stuck stack down place
     {array.setIndex(i+1);                                                      // Upper element
      Memory n = element.memory();                                              // Get reference to upper element
      array.setIndex(i);                                                        // Index stuck memory
      element.set(n);
     }
    return m;
   }

  void unshift(Memory ElementToUnShift)                                         // Unshift an element as memory onto the stuck stack
   {if (!unary.canInc()) stop("Stuck is full");                                 // Confirm there is room for another element  in the stuck stack
    final int N = stuckSize();                                                  // Current size of stuck stack
    for (int i = N; i > 0; i--)                                                 // Shift the stuck stack down place
     {array.setIndex(i-1);                                                      // Lower element
      Memory n = element.memory();                                              // Get reference to lower element
      array.setIndex(i);                                                        // Index upper element
      element.set(n);                                                           // Set upper element
     }
    array.setIndex(0);                                                          // Index first element
    element.set(ElementToUnShift);
    unary.inc();
   }

  void unshift(int    Value) {unshift(memoryFromInt(width, Value));}            // Unshift an integer onto the stuck stack
  void unshift(String Value) {unshift(memoryFromString(Value));}                // Unshift an Memory onto the stuck stack

  Memory elementAt(int i)                                                       // Return the element at the indicated index
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to shift on the stuck stack
    final int N = stuckSize();                                                  // Current size of stuck stack
    if (i < 0 || i > N) stop("Index out of range", i, N);
    array.setIndex(i);                                                          // Upper element
    return element.memory();                                                    // Get reference to upper element
   }

  void insertElementAt(Memory elementToInsert, int i)                           // Insert an element represented as memory into the stuck stack at the indicated 0-based index after moving the elements above up one position
   {final int N = stuckSize();                                                  // Current size of stuck stack
    if (!unary.canInc()) stop("Stuck is full");
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    for (int j = N; j > i; j--)
     {array.setIndex(j-1);
      final Memory m = element.memory();
      array.setIndex(j);
      element.set(m);
     }
    array.setIndex(i);
    element.set(elementToInsert);
    unary.inc();
   }

  void insertElementAt(int    Value, int index)                                 // Push an integer onto the stuck stack
   {insertElementAt(memoryFromInt(width, Value), index);
   }

  void insertElementAt(String Value, int index)                                 // Push an Memory onto the stuck stack
   {insertElementAt(memoryFromString(Value), index);
   }

  Memory removeElementAt(int i)                                                 // Remove the Memory at 0 based index i and shift the Memorys above down one position
   {if (!unary.canDec()) stop("Stuck is empty");
    final int N = stuckSize();
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    array.setIndex(i);
    final Memory r = elementAt(i).duplicate();
    for (int j = i; j < N-1; j++)
     {array.setIndex(j+1);
      final Memory p = element.memory();
      array.setIndex(j);
      element.set(p);
     }
    unary.dec();
    return r;
   }

  Memory firstElement() {return elementAt(0);}                                  // Get the value of the first Memory
  Memory  lastElement() {return elementAt(stuckSize()-1);}                      // Get the value of the last Memory

//D1 Search                                                                     // Search a stuck stack.

  public int indexOf(Memory elementToFind)                                      // Return 0 based index of the indicated memory else -1 if the memory is not present in the stuck stack.
   {final int N = stuckSize();
    for (int i = 0; i < N; i++)
     {final Memory m = elementAt(i);
      if (m.equals(elementToFind)) return i;
     }
    return -1;                                                                  // Not found
   }

  int indexOf(int    Value) {return indexOf(memoryFromInt(width, Value));}      // Zero based index of an integer in a stuck stack
  int indexOf(String Value) {return indexOf(memoryFromString(Value));}          // Zero based index of a string in a stuck stack

//D1 Print                                                                      // Print a stuck stack

  public String toString()                                                      // Print a stuck stack
   {final StringBuilder b = new StringBuilder("Stuck(");
    final int N = stuckSize();
    for (int i = 0; i < N; i++) b.append(""+elementAt(i).toInt()+", ");
    if (N > 0) b.setLength(b.length()-2);
    b.append(")");
    return b.toString();
   }

//D0 Tests                                                                      // Test stuck stack

  static void test_push()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);

    s.push(1);
    s.push(2);
    s.push(3);
    ok(s.stuckSize(), 3);
    s.ok("Stuck(1, 2, 3)");
   }

  static void test_pop()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("00000011001000010111");    ok(s.stuckSize(), 3);
    s.pop().ok(3);  ok(s.stuckSize(), 2);
    s.pop().ok(2);  ok(s.stuckSize(), 1);
    s.pop().ok(1);  ok(s.stuckSize(), 0);
   }

  static void test_shift()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("00000011001000010111");
                     ok(s.stuckSize(), 3); s.ok("Stuck(1, 2, 3)");
    s.shift().ok(1); ok(s.stuckSize(), 2); s.ok("Stuck(2, 3)");
    s.shift().ok(2); ok(s.stuckSize(), 1); s.ok("Stuck(3)");
    s.shift().ok(3); ok(s.stuckSize(), 0); s.ok("Stuck()");
   }

  static void test_unshift()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
                  ok(s.stuckSize(), 0);
    s.unshift(1); ok(s.stuckSize(), 1); s.ok("Stuck(1)");
    s.unshift(2); ok(s.stuckSize(), 2); s.ok("Stuck(2, 1)");
    s.unshift(3); ok(s.stuckSize(), 3); s.ok("Stuck(3, 2, 1)");
//  say("s.ok(\""+s+"\");");
   }

  static void test_element_at()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("00000011001000010111");
    s.elementAt(0).ok(1);
    s.elementAt(1).ok(2);
    s.elementAt(2).ok(3);
   }

  static void test_insert_element_at()
   {final int W = 4, M = 8;
    Stuck s = stuck(M, W);
    s.insertElementAt(3, 0);
    s.insertElementAt(2, 1);
    s.insertElementAt(1, 2);
    s.insertElementAt(4, 1);
   }

  static void test_remove_element_at()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("00000011001000010111");
    s.removeElementAt(1).ok(2); s.ok("Stuck(1, 3)");
    s.removeElementAt(1).ok(3); s.ok("Stuck(1)");
    s.removeElementAt(0).ok(1); s.ok("Stuck()");
   }

  static void test_first_last()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("00000011001000010111");
    s.firstElement().ok(1);
    s.lastElement() .ok(3);
   }

  static void test_index_of()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("01100011001000011111");
    ok(s.indexOf(1), 0);
    ok(s.indexOf(2), 1);
    ok(s.indexOf(3), 2);
    ok(s.indexOf(6), 3);
    ok(s.indexOf(7),-1);
   }

  static void test_clear()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("01100011001000011111");
    s.clear();
    s.set("01100011001000010000");
   }

  static void test_print()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.set("01100011001000011111");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_push();
    test_pop();
    test_shift();
    test_unshift();
    test_element_at();
    test_insert_element_at();
    test_remove_element_at();
    test_first_last();
    test_index_of();
    test_clear();
    test_print();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_print();
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
