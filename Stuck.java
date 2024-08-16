//------------------------------------------------------------------------------
// A fixed size stack of ordered keys controlled by a unary number.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

import java.util.*;

class Stuck<Type> extends Chip implements Iterable<Type>                        // Stuck: a fixed size stack controlled by a unary number. The unary number zero indicates an empty stuck stack.
 {final Unary u;                                                                // The unary number that controls the stuck stack
  final Object[]s;                                                              // The stuck stack

//D1 Construction                                                               // Create a stuck stack

  Stuck(int Max)                                                                // Create the stuck stack
   {u = new Unary(Max);                                                         // Create the unary number that indicates the top of the stuck stack
    s = new Object[Max];                                                        // The stuck stack
   }

  static Stuck<Integer> stuck(int max) {return new Stuck<Integer>(max);}        // Create a stuck stack
  void clear() {u.set(0);}                                                      // Clear a stuck stack

  public Stuck<Type> clone()                                                    // Clone a stuck stack
   {final int N = u.max();
    final Stuck<Type> t = new Stuck<>(N);
    for (int i = 0; i < N; i++) t.s[i] = s[i];                                  // Clone stuck stack
    return t;
   }

//D1 Characteristics                                                            // Characteristics of the stuck stack

  int size() {return u.get();}                                                  // The current size of the stuck stack

  void ok(String expected) {ok(toString(), expected);}                          // Check the stuck stack

  boolean isFull()  {return size() >= u.max();}                                 // Check the stuck stack is full
  boolean isEmpty() {return size() <= 0;}                                       // Check the stuck stack is empty

//D1 Actions                                                                    // Place and remove data to/from stuck stack

  void push(Type i)                                                             // Push an element onto the stuck stack
   {if (!u.canInc()) stop("Stuck is full");
    s[size()] = i;
    u.inc();
   }

  @SuppressWarnings("unchecked")
  Type pop()                                                                    // Pop an element from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    u.dec();
    return (Type)s[size()];
   }

  @SuppressWarnings("unchecked")
  Type shift()                                                                  // Shift an element from the stuck stack
   {if (!u.canDec()) stop("Stuck is empty");
    Type r = (Type)s[0];
    final int N = size();
    for (int i = 0; i < N-1; i++) s[i] = s[i+1];
    u.dec();
    return r;
   }

  void unshift(Type i)                                                          // Unshift an element from the stuck stack
   {if (!u.canInc()) stop("Stuck is full");
    final int N = size();
    for (int j = N; j > 0; j--) s[j] = s[j-1];
    s[0] = i;
    u.inc();
   }

  @SuppressWarnings("unchecked")
  Type removeElementAt(int i)                                                   // Remove the element at 0 based index i and shift the elements above down one position
   {if (!u.canDec()) stop("Stuck is empty");
    final int N = size();
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    Type r = (Type)s[i];
    for (int j = i; j < N-1; j++) s[j] = s[j+1];
    u.dec();
    return r;
   }

  void insertElementAt(Type e, int i)                                           // Insert an element at the indicated 0-based index after moving the elements above up one position
   {final int N = size();
    if (!u.canInc()) stop("Stuck is full");
    if (i > N) stop("Too far up");
    if (i < 0) stop("Too far down");
    for (int j = N; j > i; j--) s[j] = s[j-1];
    s[i] = e;
    u.inc();
   }

  @SuppressWarnings("unchecked")
  Type elementAt(int i)                                                         // Get the element at a specified index
   {final int N = size();
    if (i >= N) stop("Too far up");
    if (i <  0) stop("Too far down");
    return (Type)s[i];
   }

  void setElementAt(Type e, int i)                                              // Set the value of the indexed location to the specified element
   {final int N = size();
    if (i >  N) stop("Too far up");
    if (i <  0) stop("Too far down");
    s[i] = e;
   }

  @SuppressWarnings("unchecked")
  Type firstElement() {return elementAt(0);}                                    // Get the value of the first element
  Type  lastElement() {return elementAt(size()-1);}                             // Get the value of the last element

//D1 Search                                                                     // Search a stuck stack.

  public int indexOf(Type keyToFind)                                            // Return 0 based index of the indicated key else -1 if the key is not present in the stuck stack.
   {final int N = size();
    for (int i = 0; i < N; i++) if (keyToFind.equals(s[i])) return i;
    return -1;                                                                  // Not found
   }

//D1 Iterate                                                                    // Iterate a stuck stack

  public Iterator<Type> iterator() {return new Iterate<Type>();}                // Create an iterator for the stuck stack

  class Iterate<Type> implements Iterator<Type>
   {int nextElement = 0;                                                        // Iterate the stuck stack

    public boolean hasNext() {return nextElement < size();}                     // Another element to iterate

    @SuppressWarnings("unchecked")
    public Type next()                                                          // Next element to iterate
     {if (!hasNext()) throw new NoSuchElementException();
      Type e = (Type)s[nextElement];
      nextElement = nextElement + 1;
      return e;
     }
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
   {var s = stuck(4);
    s.push(1); s.push(2); s.push(3); s.ok("Stuck(1, 2, 3)");
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
   {var s = stuck(4);                                           ok(s.size(), 0); ok(s.isEmpty());
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
   {var s = stuck(4);                s.ok("Stuck()");           ok(s.size(), 0); ok(s.isEmpty());
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
    var s = stuck(N);
    for (int i = 1; i <= N; i++) s.push(i);
    s.ok("Stuck(1, 2, 3, 4)");
    ok(s.indexOf(0), -1);
    for (int i = 1; i <= N; i++) ok(s.indexOf(i), i-1);
   }

  static void test_iterate_zero()
   {final int N = 4;
    var s = stuck(N);
    final StringBuilder b = new StringBuilder();
    for (Integer i : s) b.append(""+i+", ");
    if (b.length() > 0) b.setLength(b.length()-2);
    ok(b.toString().equals(""));
   }

  static void test_iterate()
   {final int N = 4;
    var s = stuck(N);
    for (int i = 1; i <= N; i++) s.push(i);
    s.ok("Stuck(1, 2, 3, 4)");
    final StringBuilder b = new StringBuilder();
    for (Integer i : s) b.append(""+i+", ");
    if (b.length() > 0) b.setLength(b.length()-2);
    ok(b.toString().equals("1, 2, 3, 4"));
   }

  static void test_clear()
   {var s = stuck(4);                ok(s.size(), 0);
    s.push(1); s.ok("Stuck(1)");     ok(s.size(), 1);
    s.push(2); s.ok("Stuck(1, 2)");  ok(s.size(), 2);
    s.clear(); ok(s.isEmpty());
    s.push(3); s.ok("Stuck(3)");     ok(s.size(), 1);
    s.push(4); s.ok("Stuck(3, 4)");  ok(s.size(), 2);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_action();
    test_push_shift();
    test_insert_remove();
    test_clear();
    test_search();
    test_iterate_zero();
    test_iterate();
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
