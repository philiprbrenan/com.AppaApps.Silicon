//------------------------------------------------------------------------------
// A fixed size stack of ordered bit keys controlled by a unary number.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck extends Memory.Structure                                            // Stuck: a fixed size stack controlled by a unary number. The unary number zero indicates an empty stuck stack.
 {final Unary unary;                                                            // The layout of the stuck stack
  final Memory.Variable found;                                                  //o Whether the latest find actually found the specified element
  final Memory.Variable full;                                                   //o Whether the stuck is full
  final Memory.Variable notEmpty;                                               //o Whether the stuck contains at least one element or not
  final Memory.Variable index;                                                  //i Index to an element wanted in the stuck
  final Memory.Variable outdex;                                                 //o Index to an element found in the stuck
  final Memory.Variable input;                                                  //i A buffer used to provide input to the stack
  final Memory.Variable output;                                                 //o A buffer holding an output from the stuck
  final Memory.Variable element;                                                // An element of the stuck stack
  final Memory.Array    array;                                                  // The array holding the elements of the stuck stack
  final int max;                                                                // The maximum number of entries in the stuck stack.
  final int width;                                                              // The width of each object in the stuck in bits
  final Chip chip;                                                              // Chip implementing the stuck

//D1 Construction                                                               // Create a stuck stack

  Stuck(String Name, int Max, int Width)                                        // Create the stuck stack
   {super(Name);                                                                // Containing structure layout
    width    = Width; max = Max;
    unary    = Unary.unary(max);                                                // Unary number showing which elements in the stack are valid
    found    = variable("found",       1);                                      // Whether the latest find actually found the specified element
    full     = variable("full",        1);                                      // Whether the stuck is full
    notEmpty = variable("notEmpty",    1);                                      // Whether the stuck contains at least one element or not
    index    = variable("index",   width);                                      // Index to an element wanted in the stuck
    outdex   = variable("outdex",  width);                                      // Index to an element found in the stuck
    input    = variable("input",   width);                                      // A buffer used to load the stuck
    output   = variable("output",  width);                                      // A buffer used to unload the stuck
    element  = variable("element", width);                                      // An element of the stuck stack
    array    = array   ("array",   element, max);                               // An array of elements comprising the stuck stack
    addField(found);                                                            // Whether the latest find actually found the specified element
    addField(full);                                                             // Whether the stuck is full
    addField(notEmpty);                                                         // Whether the stuck contains at least one element or not
    addField(index);                                                            // Index to an element wanted in the stuck
    addField(outdex);                                                           // Index to an element found in the stuck
    addField(input);                                                            // A buffer used to load the stuck
    addField(output);                                                           // A buffer used to unload the stuck
    addField(unary);                                                            // The unary number representing the usage of the stuck
    addField(array);                                                            // Array of nodes
    layout();                                                                   // Layout the structure of the stuck stack

    chip = stuck_chip();                                                        // Create a chip
   }

  Chip stuck_chip()
   {final int      D = 12;
    final Chip     c = new Chip();
    final int      N = width;
    final Pulse    p = c.pulse("p").period(0).delay(D).on(D).b();
    final Register stuckRegister = c.register(n(name, "memory"), N, p);

    return c;
   }

  static Stuck stuck(int max, int width)                                        // Create a stuck stack
   {return new Stuck("Stuck", max, width);
   }

  void clear() {zero();}                                                        // Clear a stuck stack

//D1 Characteristics                                                            // Characteristics of the stuck stack

  int  stuckSize() {return unary.get();}                                        // The current size of the stuck stack
  void StuckSize() {index.set(unary.get());}                                    // The current size of the stuck stack via the index field

  public void ok(String expected) {ok(toString(), expected);}                   // Check the stuck stack

  boolean isFull()  {return stuckSize() >= max;}                                // Check the stuck stack is full
  boolean isEmpty() {return stuckSize() <= 0;}                                  // Check the stuck stack is empty

  void Full()     {full.set(stuckSize() >= max  ? 1 : 0);}                      // Indicate whether the stuck is full or not
  void NotEmpty() {notEmpty.set(stuckSize() > 0 ? 1 : 0);}                      // Indicate the stuck contains at least one element or not

  void setIndex (int Element)   {index .set(memoryFromInt(width, Element));}    // Set the index from an integer
  void setIndex (Memory memory) {index .set(memory);}                           // Set the index from memory

  void setInput(int Element)   {input.set(memoryFromInt(width, Element));}      // Set the buffer from an integer
  void setInput(Memory memory) {input.set(memory);}                             // Set the buffer from memory

//D1 Actions                                                                    // Place and remove data to/from stuck stack

  void push(Memory ElementToPush)                                               // Push an element as memory onto the stuck stack
   {if (!unary.canInc()) stop("Stuck is full");                                 // Check there is room on the stack
    final int n = stuckSize();                                                  // Current size of  Stuck Stack
    array.setIndex(n);                                                          // Index stuck memory
    element.set(ElementToPush);                                                 // Set memory of stuck stack from supplied memory
    unary.inc();                                                                // Show new slot in use
    NotEmpty(); Full();                                                         // Show whether empty or full
   }

  void Push() {push(input);}                                                    // Push the buffer onto the stuck stack
  void push(int Value) {push(memoryFromInt(width, Value));}                     // Push an integer onto the stuck stack

  Memory pop()                                                                  // Pop an element as memory from the stuck stack
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to pop on the stuck stack
    unary.dec();                                                                // New number of elements on stuck stack
    final int n = stuckSize();                                                  // Current size of  Stuck Stack
    array.setIndex(n);                                                          // Index stuck memory
    NotEmpty(); Full();                                                         // Show whether empty or full
    return element.memory();                                                    // Get memory from stuck stack
   }

  void Pop() {output.set(pop());}                                               // Pop an element as memory from the stuck stack

  Memory shift()                                                                // Shift an element as memory from the stuck stack
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to shift on the stuck stack
    unary.dec();                                                                // New number of elements on stuck stack
    array.setIndex(0);                                                          // Index stuck memory
    Memory m = element.memory().duplicate();                                    // Copy the slice of memory
    final int N = stuckSize();                                                  // Current size of  Stuck Stack
    for (int i = 0; i < N; i++)                                                 // Shift the stuck stack down place
     {array.setIndex(i+1);                                                      // Upper element
      Memory e = element.memory();                                              // Get reference to upper element
      array.setIndex(i);                                                        // Index stuck memory
      element.set(e);                                                           // Get referenced element
     }
    NotEmpty(); Full();                                                         // Show whether empty or full
    return m;                                                                   // Return memory of shifted element
   }

  void Shift() {output.set(shift());}                                           // Shift an element as memory from the stuck stack

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
    NotEmpty(); Full();                                                         // Show whether empty or full
   }

  void Unshift() {unshift(input);}                                              // Unshift an element as memory onto the stuck stack

  void unshift(int    Value) {unshift(memoryFromInt(width, Value));}            // Unshift an integer onto the stuck stack
  void unshift(String Value) {unshift(memoryFromString(Value));}                // Unshift an Memory onto the stuck stack

  Memory elementAt(int i)                                                       // Return the element at the indicated index
   {if (!unary.canDec()) stop("Stuck is empty");                                // Confirm there is an element to shift on the stuck stack
    final int N = stuckSize();                                                  // Current size of stuck stack
    if (i < 0 || i > N) stop("Index out of range", i, N);
    array.setIndex(i);                                                          // Upper element
    return element.memory();                                                    // Get reference to upper element
   }

  void ElementAt() {output.set(elementAt(index.toInt()));}                      // Return the element at the indicated index

  void setElementAt(Memory Element, int i)                                      // Set an element of the stuck stack
   {final int N = stuckSize();                                                  // Current size of stuck stack
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    array.setIndex(i);                                                          // Index stuck memory
    element.set(Element);                                                       // Set memory of stuck stack from supplied memory
    if (i == N) unary.inc();                                                    // Creating a new top element
    NotEmpty(); Full();                                                         // Show whether empty or full
   }

  void SetElementAt() {setElementAt(input, index.toInt());}                     // Set an element of the stuck stack

  void setElementAt(int Value, int index)                                       // Unshift an integer onto the stuck stack
   {setElementAt(memoryFromInt(width, Value), index);
   }
  void setElementAt(String Value, int index)                                    // Unshift an Memory onto the stuck stack
   {setElementAt(memoryFromString(Value), index);
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
    NotEmpty(); Full();                                                         // Show whether empty or full
   }

  void insertElementAt(int    Value, int index)                                 // Push an integer onto the stuck stack
   {insertElementAt(memoryFromInt(width, Value), index);
   }

  void insertElementAt(String Value, int index)                                 // Push an Memory onto the stuck stack
   {insertElementAt(memoryFromString(Value), index);
   }

  void insertElementAt() {insertElementAt(input.toInt(), index.toInt());}       // Push an Memory onto the stuck stack

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
    NotEmpty(); Full();                                                         // Show whether empty or full
    return r;
   }

  Memory RemoveElementAt() {return removeElementAt(index.toInt());}             // Remove the Memory at 0 based index i and shift the Memorys above down one position

  Memory firstElement() {return elementAt(0);}                                  // Get the value of the first Memory
  Memory  lastElement() {return elementAt(stuckSize()-1);}                      // Get the value of the last Memory

  void   FirstElement() {output.set(elementAt(0));}                             // Get the value of the first Memory
  void    LastElement() {output.set(elementAt(stuckSize()-1));}                 // Get the value of the last Memory

//D1 Search                                                                     // Search a stuck stack.

  int indexOf(Memory elementToFind)                                             // Set found to true and index to the 0 based index of the indicated memory else -1 if the memory is not present in the stuck stack.
   {final int N = stuckSize();
    for (int i = 0; i < N; i++)
     {final Memory m = elementAt(i);
      if (m.equals(elementToFind)) return i;
     }
    return -1;                                                                  // Not found
   }

  void IndexOf()                                                                // Return 0 based index of the indicated memory else -1 if the memory is not present in the stuck stack.
   {final Memory elementToFind = input;
    final int N = stuckSize();
    for (int i = 0; i < N; i++)
     {final Memory m = elementAt(i);
      if (m.equals(elementToFind))
       {outdex.set(i);
        found.set(1);
        return;
       }
     }
    found.set(0);
    return;
   }

  int indexOf(int    Value) {return indexOf(memoryFromInt(width, Value));}      // Zero based index of an integer in a stuck stack
  int indexOf(String Value) {return indexOf(memoryFromString(Value));}          // Zero based index of a string in a stuck stack

//D1 Print                                                                      // Print a stuck stack

  String print(String Name, String End)                                         // Print a stuck stack
   {final StringBuilder b = new StringBuilder(Name);
    final int N = stuckSize();
    for (int i = 0; i < N; i++) b.append(""+elementAt(i).toInt()+", ");
    if (N > 0) b.setLength(b.length()-2);
    b.append(End);
    return b.toString();
   }

  public String toString() {return print("Stuck(", ")");}                       // Print a stuck stack with label

//D0 Tests                                                                      // Test stuck stack

  static void test_push()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    ok(s.isEmpty()); s.notEmpty.ok(0); s.full.ok(0); s.ok("Stuck()");
    s.push(1);       s.notEmpty.ok(1); s.full.ok(0); s.ok("Stuck(1)");
    s.push(2);       s.notEmpty.ok(1); s.full.ok(0); s.ok("Stuck(1, 2)");
    s.push(3);       s.notEmpty.ok(1); s.full.ok(0); s.ok("Stuck(1, 2, 3)");
    s.push(12);      s.notEmpty.ok(1); s.full.ok(1); s.ok("Stuck(1, 2, 3, 12)");
    ok(s.stuckSize(), 4);
    s.StuckSize(); s.index.ok(4);
    ok(s.isFull());
    s.ok("Stuck(1, 2, 3, 12)");
   }

  static void test_push_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.setInput(1); s.Push(); s.ok("Stuck(1)");
    s.setInput(2); s.Push(); s.ok("Stuck(1, 2)");
    s.setInput(3); s.Push(); s.ok("Stuck(1, 2, 3)");
   }

  static void test_pop()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
                    ok(s.stuckSize(), 4);
    s.pop().ok(12); ok(s.stuckSize(), 3);
    s.pop().ok( 3); ok(s.stuckSize(), 2);
    s.pop().ok( 2); ok(s.stuckSize(), 1);
    s.pop().ok( 1); ok(s.stuckSize(), 0);
   }

  static void test_pop_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
                    ok(s.stuckSize(), 4);
    s.Pop();
    s.output.ok(12);
    ok(s.stuckSize(), 3);
    s.Pop(); s.output.ok( 3); ok(s.stuckSize(), 2);
    s.Pop(); s.output.ok( 2); ok(s.stuckSize(), 1);
    s.Pop(); s.output.ok( 1); ok(s.stuckSize(), 0);
   }

  static void test_shift()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
                                    ok(s.stuckSize(), 4); s.ok("Stuck(1, 2, 3, 12)");
    Memory a = s.shift(); a.ok( 1); ok(s.stuckSize(), 3); s.ok("Stuck(2, 3, 12)");
    Memory b = s.shift(); b.ok( 2); ok(s.stuckSize(), 2); s.ok("Stuck(3, 12)");
    Memory c = s.shift(); c.ok( 3); ok(s.stuckSize(), 1); s.ok("Stuck(12)");
    Memory d = s.shift(); d.ok(12); ok(s.stuckSize(), 0); s.ok("Stuck()");
   }

  static void test_shift_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);

    s.Shift(); s.output.ok( 1);
    s.Shift(); s.output.ok( 2);
    s.Shift(); s.output.ok( 3);
    s.Shift(); s.output.ok(12);
   }

  static void test_unshift()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
                  ok(s.stuckSize(), 0);
    s.unshift(1); ok(s.stuckSize(), 1); s.ok("Stuck(1)");
    s.unshift(2); ok(s.stuckSize(), 2); s.ok("Stuck(2, 1)");
    s.unshift(3); ok(s.stuckSize(), 3); s.ok("Stuck(3, 2, 1)");
    s.unshift(9); ok(s.stuckSize(), 4); s.ok("Stuck(9, 3, 2, 1)");
   }

  static void test_unshift_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
                  ok(s.stuckSize(), 0);
    s.input.set(1); s.Unshift(); ok(s.stuckSize(), 1); s.ok("Stuck(1)");
    s.input.set(2); s.Unshift(); ok(s.stuckSize(), 2); s.ok("Stuck(2, 1)");
    s.input.set(3); s.Unshift(); ok(s.stuckSize(), 3); s.ok("Stuck(3, 2, 1)");
    s.input.set(9); s.Unshift(); ok(s.stuckSize(), 4); s.ok("Stuck(9, 3, 2, 1)");
   }

  static void test_element_at()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
    s.elementAt(0).ok(1);
    s.elementAt(1).ok(2);
    s.elementAt(2).ok(3);
    s.elementAt(3).ok(12);
   }

  static void test_element_at_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
    s.index.set(0); s.ElementAt(); s.output.ok(1);
    s.index.set(1); s.ElementAt(); s.output.ok(2);
    s.index.set(2); s.ElementAt(); s.output.ok(3);
    s.index.set(3); s.ElementAt(); s.output.ok(12);
   }

  static void test_insert_element_at()
   {final int W = 4, M = 8;
    Stuck s = stuck(M, W);
    s.insertElementAt(3, 0); s.ok("Stuck(3)");
    s.insertElementAt(2, 1); s.ok("Stuck(3, 2)");
    s.insertElementAt(1, 2); s.ok("Stuck(3, 2, 1)");
    s.insertElementAt(4, 1); s.ok("Stuck(3, 4, 2, 1)");
   }

  static void test_insert_element_at_buffer()
   {final int W = 4, M = 8;
    Stuck s = stuck(M, W);
    s.setInput(3); s.setIndex(0); s.insertElementAt(); s.ok("Stuck(3)");
    s.setInput(2); s.setIndex(1); s.insertElementAt(); s.ok("Stuck(3, 2)");
    s.setInput(1); s.setIndex(2); s.insertElementAt(); s.ok("Stuck(3, 2, 1)");
    s.setInput(4); s.setIndex(1); s.insertElementAt(); s.ok("Stuck(3, 4, 2, 1)");
   }

  static void test_remove_element_at()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
    s.removeElementAt(1).ok(2);   s.ok("Stuck(1, 3, 12)");
    s.removeElementAt(1).ok(3);   s.ok("Stuck(1, 12)");
    s.removeElementAt(0).ok(1);   s.ok("Stuck(12)");
    s.removeElementAt(0).ok(12);  s.ok("Stuck()");
   }

  static void test_remove_element_at_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
    s.setIndex(1); s.RemoveElementAt().ok(2);   s.ok("Stuck(1, 3, 12)");
    s.setIndex(1); s.RemoveElementAt().ok(3);   s.ok("Stuck(1, 12)");
    s.setIndex(0); s.RemoveElementAt().ok(1);   s.ok("Stuck(12)");
    s.setIndex(0); s.RemoveElementAt().ok(12);  s.ok("Stuck()");
   }

  static void test_first_last()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
                s.notEmpty.ok(0); s.full.ok(0);
    s.push( 1); s.notEmpty.ok(1); s.full.ok(0);
    s.push( 2); s.notEmpty.ok(1); s.full.ok(0);
    s.push( 3); s.notEmpty.ok(1); s.full.ok(0);
    s.push(12); s.notEmpty.ok(1); s.full.ok(1);
    s.firstElement().ok(1);
    s.lastElement() .ok(12);
   }

  static void test_first_last_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(12);
    s.FirstElement(); s.output.ok(1);
    s.LastElement();  s.output.ok(12);
   }

  static void test_index_of()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(6);
    ok(s.indexOf(1),  0);
    ok(s.indexOf(2),  1);
    ok(s.indexOf(3),  2);
    ok(s.indexOf(6),  3);
    ok(s.indexOf(7), -1);
   }

  static void test_index_of_buffer()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(6);
    s.setInput(1); s.IndexOf(); s.found.ok(1); s.outdex.ok( 0);
    s.setInput(2);
      s.IndexOf();
        s.found.ok(1);
          s.outdex.ok( 1);
    s.setInput(3); s.IndexOf(); s.found.ok(1); s.outdex.ok( 2);
    s.setInput(6); s.IndexOf(); s.found.ok(1); s.outdex.ok( 3);
    s.setInput(7); s.IndexOf(); s.found.ok(0);
   }

  static void test_clear()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.full.ok(0); s.notEmpty.ok(0);
    s.push(1); s.push(2); s.push(3); s.push(6);
    s.full.ok(1); s.notEmpty.ok(1); s.clear();
    s.full.ok(0); s.notEmpty.ok(0);
    s.ok("Stuck()");
   }

  static void test_print()
   {final int W = 4, M = 4;
    Stuck s = stuck(M, W);
    s.push(1); s.push(2); s.push(3); s.push(6);
    s.ok("Stuck(1, 2, 3, 6)");
    ok(s.print("[","]"), "[1, 2, 3, 6]");
   }

  static void test_set_element_at()
   {final int W = 4, M = 8;
    Stuck s = stuck(M, W);

    s.setElementAt(1, 0); s.ok("Stuck(1)");
    s.setElementAt(2, 1); s.ok("Stuck(1, 2)");
    s.setElementAt(3, 2); s.ok("Stuck(1, 2, 3)");
    s.setElementAt(4, 0); s.ok("Stuck(4, 2, 3)");
    s.setElementAt(5, 1); s.ok("Stuck(4, 5, 3)");
    s.setElementAt(6, 2); s.ok("Stuck(4, 5, 6)");
    s.setElementAt(7, 0); s.ok("Stuck(7, 5, 6)");
   }

  static void test_set_element_at_buffer()
   {final int W = 4, M = 8;
    Stuck s = stuck(M, W);

    s.setInput(1); s.setIndex(0); s.SetElementAt(); s.ok("Stuck(1)");
    s.setInput(2); s.setIndex(1); s.SetElementAt(); s.ok("Stuck(1, 2)");
    s.setInput(3); s.setIndex(2); s.SetElementAt(); s.ok("Stuck(1, 2, 3)");
    s.setInput(4); s.setIndex(0); s.SetElementAt(); s.ok("Stuck(4, 2, 3)");
    s.setInput(5); s.setIndex(1); s.SetElementAt(); s.ok("Stuck(4, 5, 3)");
    s.setInput(6); s.setIndex(2); s.SetElementAt(); s.ok("Stuck(4, 5, 6)");
    s.setInput(7); s.setIndex(0); s.SetElementAt(); s.ok("Stuck(7, 5, 6)");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_push();               test_push_buffer();
    test_pop();                test_pop_buffer();
    test_shift();              test_shift_buffer();
    test_unshift();            test_unshift_buffer();
    test_element_at();         test_element_at_buffer();
    test_insert_element_at();  test_insert_element_at_buffer();
    test_remove_element_at();  test_remove_element_at_buffer();
    test_first_last();         test_first_last_buffer();
    test_index_of();           test_index_of_buffer();
    test_clear();
    test_print();
    test_set_element_at();
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
