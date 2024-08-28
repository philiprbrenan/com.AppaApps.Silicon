//------------------------------------------------------------------------------
// Basic
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.

class Memory extends Chip                                                       // Bit memory
 {final boolean[]bits;                                                          // Bits comprising the memory

  Memory(int Size)                                                              // Size of memory
   {bits = new boolean[Size];
    zero();
   }

  Memory(int Width, int value)                                                  // Create a memory of specified width and set it to a specified value from an integer
   {this(Width);
    set(value);
   }

  static Memory memory(int size) {return new Memory(size);}                     // Create memory

  void ok(String expected) {Chip.ok(toString(), expected);}                     // Check memory is as expected

  void    set(int i, boolean b) {bits[i] = b;}                                  // Set a bit
  boolean get(int i) {return     bits[i];}                                      // Get a bit

  void zero()                                                                   // Zero a memory
   {final int size = bits.length;
    for (int i = 0; i < size; i++) set(i, false);
   }

  void shiftLeftFillWithZeros(int left)                                         // Shift left filling with zeroes
   {final int size = bits.length;
    for (int i = size; i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;    i < left; ++i) set(i,   false);
   }

  void shiftLeftFillWithOnes(int left)                                          // Shift left filling with ones
   {final int size = bits.length;
    for (int i = size; i > left; --i) set(i-1, get(i-1-left));
    for (int i = 0;    i < left; ++i) set(i,   true);
   }

  void shiftRightFillWithZeros(int right)                                       // Shift right filling with zeroes
   {final int size = bits.length;
    for (int i = 0; i < size-right;    ++i) set(i, get(i+right));
    for (int i = size-right; i < size; ++i) set(i, false);
   }

  void shiftRightFillWithSign(int right)                                        // Shift right filling with sign
   {final int size = bits.length;
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

  public String toString()                                                      // Memory as string
   {final StringBuilder b = new StringBuilder();
    final int size = size();
    for (int i = size; i > 0; --i) b.append(get(i-1) ? '1' : '0');
    return b.toString();
   }

  int size() {return bits.length;}                                              // Size of memory

  void set(Memory source, int offset)                                           // Copy source memory into this memory at the specified offset
   {final int t = size(), s = source.size(), m = min(t, s);
    if (offset + s > t) stop("Cannot write beyond end of memory");
    for (int i = 0; i < m; i++) set(offset+i, source.get(i));                   // Load the specified string into memory
   }

  Memory get(int offset, int width)                                             // Get a sub section of this memory
   {final Memory m = new Memory(width);
    if (offset + width > size()) stop("Cannot read beyond end of memory");
    for (int i = 0; i < width; i++) m.set(i, get(offset+i));
    return m;
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

//D0                                                                            // Tests

  static void test_memory()
   {Memory m = memory(110);
    for (int i = 1; i < 11; i++)
     {m.shiftLeftFillWithOnes(i);
      m.shiftLeftFillWithZeros(i);
     }
    ok(m, "10110011100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000000");
    m.shiftRightFillWithSign(1);
    ok(m, "11011001110001111000011111000001111110000001111111000000011111111000000001111111110000000001111111111000000000");
    m.shiftRightFillWithZeros(1);
    ok(m, "01101100111000111100001111100000111111000000111111100000001111111100000000111111111000000000111111111100000000");
    m.shiftLeftFillWithOnes(8);
    ok(m, "11100011110000111110000011111100000011111110000000111111110000000011111111100000000011111111110000000011111111");
    m.shiftLeftFillWithZeros(2);
    ok(m, "10001111000011111000001111110000001111111000000011111111000000001111111110000000001111111111000000001111111100");
    ok(m.countLeadingOnes  (), 1);
    ok(m.countTrailingZeros(), 2);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_memory();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_memory();
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
