//------------------------------------------------------------------------------
// Btree with data stored only in the leaves.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
/*Shall I compare thee to a summerâs day?
  Thou art more lovely and more temperate:
  Rough winds do shake the darling buds of May,
  And summerâs lease hath all too short a date;

  Sometime too hot the eye of heaven shines,
  And often is his gold complexion dimm'd;
  For every fair from fair sometime declines,
  By chance or natureâs changing course untrimm'd;

  But now thou art for ever fairly made,
  The eye of heaven lights thy face for me,
  Nor shall death brag thou wanderâst in his shade,
  When these lines being read give life to thee */
import java.util.*;

class Mjaf<Type extends Comparable<Type>> extends Chip                          // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission, whereas the leaves (exterior nodes) have even number of keys because data is not transferred to the parent on fission.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));
  final static long             start = System.nanoTime();                      // Start time

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  final int maxKeysPerLeaf;                                                     // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches as whether classic Btree algorithm.

  Node<Type> root;                                                              // The root node of the Btree
  int keyDataStored;                                                            // The number of key, data values stored in the Btree

  Mjaf(int MaxKeysPerLeaf)                                                      // Define a Btree with a specified maximum number of keys per leaf.
   {final int N = MaxKeysPerLeaf;
    if (N % 2 == 1) stop("Number of keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("Number of keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf = N;
   }

  Mjaf() {this(4);}                                                             // Define a Btree with a minimal maximum number of keys per node

  static Mjaf<Long> mjaf()      {return new Mjaf<Long>();}                      // Create a new Btree of default type
  static Mjaf<Long> mjaf(int m) {return new Mjaf<Long>(m);}                     // Define a new Btree of default type with a specified maximum number of keys per node

  int size() {return keyDataStored;}                                            // Number of entries in the tree

  int nodesCreated = 0;                                                         // Number of nodes created

  class Node<Type extends Comparable<Type>>                                     // A branch or a leaf: an interior or exterior node.
   {final Stack<Type> keyNames;                                                 // Names of the keys in this branch or leaf
    final int nodeNumber = ++nodesCreated;                                      // Number of this node
    Node() {keyNames = new Stack<Type>();}                                      // Create a node

    int findIndexOfKey(Type keyToFind) {return keyNames.indexOf(keyToFind)+1;}  // Find the one based index of a key in a branch node or zero if not found
    Type splitKey     () {return keyNames.elementAt((maxKeysPerLeaf-1)/2);}     // Splitting key
    int size() {return keyNames.size();}                                        // Number of elements in this leaf
    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check node is as expected
    boolean lessThanOrEqual(Type a, Type b) {return a.compareTo(b) <= 0;}       // Define a new Btree of default type with a specified maximum number of keys per node

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug) {}; // Print horizontally
   }

  class Branch extends Node<Type>                                               // A branch node directs the search to the appropriate leaf
   {final Stack<Node<Type>> nextLevel;
    Node<Type> topNode;

    Branch(Node<Type> Top) {nextLevel = new Stack<Node<Type>>(); topNode = Top;}// Create a new branch
    boolean branchIsFull() {return size() >= maxKeysPerLeaf-1;}                 // Node should be split

    int findFirstGreaterOrEqual(Type keyName)                                   // Find first key which is greater an the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {final int N = size();                                                     // Number of keys currently in node
      for (int i = 0; i < N; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (lessThanOrEqual(keyName, kn)) return i + 1;                         // Current key is greater than or equal to the search key
       }
      return 0;
     }

    void putBranch(Type keyName, Node<Type> putNode)                            // Insert a new node into a branch
     {final int N = nextLevel.size();                                           // Number of keys currently in node
      if (N >= maxKeysPerLeaf) stop("Too many keys in Branch");
      for (int i = 0; i < N; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (lessThanOrEqual(keyName, kn))                                       // Insert new key in order
         {keyNames .insertElementAt(keyName, i);
          nextLevel.insertElementAt(putNode, i);
          return;
         }
        else if (keyName == kn)                                                 // Key already exists
         {stop("Key", keyName, "already exists in branch");
         }
       }

      keyNames .push(keyName);                                                  // Either the leaf is empty or the new key is greater than every existing key
      nextLevel.push(putNode);
     }

    void splitRoot()                                                            // Split the root
     {if (branchIsFull())
       {final Type   k = splitKey();
        final Branch l = splitBranchInHalf();
        final Branch b = branch(this);
        b.putBranch(k, l);
        root = b;
       }
     }

    Branch splitBranch(int First)                                               // Split a branch into two branches in the indicated key
     {final int K = keyNames.size(), f = First;                                 // Number of keys currently in node
      if (f >= K-1) stop("Split", f, "too big for branch of size:", K);
      if (f <  1)   stop("First", f, "too small");
      final Branch b = new Branch(nextLevel.elementAt(f));
      for (int i = 0; i < f; i++)
       {b.keyNames .push(keyNames .remove(0));
        b.nextLevel.push(nextLevel.remove(0));
       }
      keyNames .remove(0);
      nextLevel.remove(0);
      return b;
     }

    Branch splitBranchInHalf() {return splitBranch((maxKeysPerLeaf-1)/2);}      // Split a branch in half

    void joinBranch(Branch Join, Type joinKeyName)                              // Append the second branch to the first one adding the specified key
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (J + K >= maxKeysPerLeaf) stop("Join of branch has too many keys",
        K, "+1+", J, "greater than", maxKeysPerLeaf);
      if (J == 0) stop("Branch being joined is empty");                         // Nothing to join
      if (keyNames.elementAt(K-1).compareTo(Join.keyNames.elementAt(0)) >= 0) stop("First key of node being joined is less than or equal to last node of current leaf");

      keyNames .push(joinKeyName);
      nextLevel.push(topNode); topNode = Join.topNode;

      for (int i = 0; i < J; i++)
       {keyNames  .push(Join.keyNames.elementAt(i));
        nextLevel.push(Join.nextLevel.elementAt(i));
       }
     }

    public String toString()                                                    // Print branch
     {final StringBuilder s = new StringBuilder();
      s.append("Branch(");
      final int K = keyNames.size();
      for (int i = 0; i < K; i++) s.append(""+keyNames.elementAt(i)+":"+nextLevel.elementAt(i).nodeNumber+", ");
      s.append(""+topNode.nodeNumber+")");
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print branch
     {for (int i = 0; i < size(); i++)                                          // Nodes
       {final Node<Type> n = nextLevel.elementAt(i);
        n.printHorizontally(S, level+1, debug);
        padStrings(S, level);
        S.elementAt(level).append(keyNames.elementAt(i));
       }
      topNode.printHorizontally(S, level+1, debug);
     }

    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check leaf
   } // Branch

  Branch branch(Node<Type> node) {return new Branch(node);}                     // Create a new branch

  class Leaf extends Node<Type>                                                 // Create a new leaf
   {final Stack<Type> dataValues;
    Leaf() {dataValues = new Stack<Type>();}                                    // Data associated with keys in leaf
    boolean leafIsFull() {return size() >= maxKeysPerLeaf;}                     // Leaf is full

    void putLeaf(Type keyName, Type dataValue)                                  // Insert a new leaf value
     {final int K = keyNames.size();                                            // Number of keys currently in node
      if (K >= maxKeysPerLeaf) stop("Too many keys in leaf");
      for (int i = 0; i < K; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (lessThanOrEqual(keyName, kn))                                       // Insert new key in order
         {keyNames  .insertElementAt(keyName,   i);
          dataValues.insertElementAt(dataValue, i);
          ++keyDataStored;                                                      // Create a new entry in the leaf
          return;
         }
        else if (keyName == kn)                                                 // Update existing key
         {dataValues.setElementAt(dataValue, i);
          return;
         }
       }

      keyNames  .push(keyName);                                                 // Either the leaf is empty or the new key is greater than every existing key
      dataValues.push(dataValue);
      ++keyDataStored;                                                          // Created a new entry in the leaf
     }

    void removeLeafKeyData(int Index)                                           // Remove the indicated key data pair from a leaf
     {final int K = size(), i = Index;                                          // Number of keys currently in node
      if (K < i) stop("Index", i, "too big for leaf of size:", K);
      if (K < 0) stop("Index", i, "too small");
      keyNames  .removeElementAt(i);
      dataValues.removeElementAt(i);
      --keyDataStored;
     }

    Leaf splitLeaf(int First)                                                   // Split the leaf into two leafs - the new leaf consists of the indicated first elements, the old leaf retains the rest
     {final int K = size(), f = First;                                          // Number of keys currently in node
      if (f >= K) stop("Split", f, "too big for leaf of size:", K);
      if (f <  1) stop("First", f, "too small");
      final Leaf l = leaf();
      for (int i = 0; i < f; i++)
       {l.keyNames  .push(keyNames  .remove(0));
        l.dataValues.push(dataValues.remove(0));
       }
      return l;
     }

    Leaf splitLeafInHalf() {return splitLeaf(maxKeysPerLeaf/2);}                // Split a leaf in half

    void joinLeaf(Leaf Join)                                                    // Join the specified leaf onto the end of this leaf
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (J + K > maxKeysPerLeaf) stop("Join of leaf has too many keys", K,
        "+", J, "greater than", maxKeysPerLeaf);
      if (J == 0) stop("Leaf being joined is empty");                           // Nothing to join
      final Type k1 = keyNames.elementAt(K-1), k2 = Join.keyNames.elementAt(0);
      if (k1.compareTo(k2) >= 0)
        stop("First key of leaf being joined:", k2 ,
        "is less than or equal to last key of current leaf:", k1);

      for (int i = 0; i < J; i++)
       {keyNames  .push(Join.keyNames  .elementAt(i));
        dataValues.push(Join.dataValues.elementAt(i));
       }
     }

    public String toString()                                                    // Print leaf
     {final StringBuilder s = new StringBuilder();
      s.append("Leaf(");
      final int K = keyNames.size();
      for (int i = 0; i < K; i++)
        s.append(""+keyNames.elementAt(i)+":"+dataValues.elementAt(i)+", ");
      if (K > 0) s.setLength(s.length()-2);
      s.append(")");
      return s.toString();
     }

    public String shortString()                                                 // Print a leaf compactly
     {final StringBuilder s = new StringBuilder();
      final int K = keyNames.size();
      for (int i = 0; i < K; i++) s.append(""+keyNames.elementAt(i)+",");
      if (K > 0) s.setLength(s.length()-1);
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print leaf
     {padStrings(S, level);
      S.elementAt(level).append(debug ? toString() : shortString());
      padStrings(S, level);
     }
   }

  Leaf leaf() {return new Leaf();}                                              // Create an empty leaf

//D1 Search                                                                     // Find a key, data pair

  Type find(Type keyName)                                                       // Find a the data associated with a key
   {if (root == null) return null;                                              // Empty tree
    for(Node<Type> n = root; n != null;)                                        // Step down through tree
     {if (n instanceof Branch)                                                  // Stepped to a branch
       {final Branch b = (Branch)n;
        final int    g = b.findFirstGreaterOrEqual(keyName);
        n = g == 0 ? b.topNode : b.nextLevel.elementAt(g-1);
       }
      else                                                                      // Stepped to a leaf
       {final int f = n.findIndexOfKey(keyName);
        if (f == 0) return null;
        final Leaf l = (Leaf)n;
        return l.dataValues.elementAt(f-1);
       }
     }
    return null;                                                                // Key not found
   }

//D1 Insertion                                                                  // Insert keys and data into the Btree

  void put(Type keyName, Type dataValue)                                        // Insert a new key, data pair into the Btree
   {loop: for (int z = 0; z < 9; z++)
     {if (root == null)
       {final Leaf l = new Leaf();
        root = l;
        l.putLeaf(keyName, dataValue);
        return;
       }

      if (root instanceof Leaf)                                                 // Insert into root as a leaf
       {final Leaf r = (Leaf)root;
        if (!r.leafIsFull())
         {r.putLeaf(keyName, dataValue);
          return;
         }
        else                                                                    // Insert into root as a leaf which is full
         {final Leaf   l = r.splitLeafInHalf();
          final Branch b = branch(root);
          final Type   k = l.splitKey();
          b.putBranch(k, l);                                                    // Insert left hand node all of whose elements are less than the first element of what was the root
          root = b;
         }
       }

      else if (root instanceof Branch) ((Branch)root).splitRoot();              // Split full root which is a branch not a leaf

      Branch p = (Branch)root;
      Node<Type> q = p;
      for(int i = 0; i < 9; ++i)                                                // Step down through tree to find the required leaf, splitting as we go
       {if (q instanceof Branch)                                                // Stepped to a branch
         {final Branch qb = (Branch)q;
          if (qb.branchIsFull())                                                // Split the branch because it is full and we might need to insert below it requiring a slot in this node
           {final Type   k = qb.splitKey();
            final Branch l = qb.splitBranchInHalf();
            p.putBranch(k, l);
            continue loop;
           }
          final int g = qb.findFirstGreaterOrEqual(keyName);

          p = qb; q = g == 0 ? qb.topNode : qb.nextLevel.elementAt(g-1);
          continue;                                                             // Continue to step down
         }

        final Leaf l = (Leaf)q;
        final int  g = l.findIndexOfKey(keyName);
        if (g != 0)                                                             // Key already present in leaf
         {l.dataValues.set(g-1, dataValue);
          return;                                                               // Data replaced at key
         }

        if (l.leafIsFull())                                                     // Split the node because it is full and we might need to insert below it requiring a slot in this node
         {final Type k = l.splitKey();
          final Leaf e = l.splitLeafInHalf();
          p.putBranch(k, e);
          if (p.lessThanOrEqual(keyName, k)) e.putLeaf(keyName, dataValue);     // Insert key in the appropriate split leaf
          else                        l.putLeaf(keyName, dataValue);
          return;
         }
        l.putLeaf(keyName, dataValue);                                          // On a leaf that is not full so we can insert directly
        return;                                                                 // Key, data pair replaced
       }
     }
   }

//D1 Print                                                                      // Print a tree

  static void padStrings(Stack<StringBuilder> S, int level)                     // Pad a stack of strings so they all have the same length
   {for (int i = S.size(); i <= level; ++i) S.push(new StringBuilder());
    int m = 0;
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;
    for (StringBuilder s : S)
      if (s.length() < m) s.append(" ".repeat(m - s.length()));
   }

  static String joinStrings(Stack<StringBuilder> S)                             // Join lines
   {final StringBuilder a = new StringBuilder();
    for (StringBuilder s : S) a.append(s.toString()+"|\n");
    return a.toString();
   }

  String printHorizontally()                                                    // Print a tree horizontally
   {final Stack<StringBuilder> S = new Stack<>();
    if (root == null) return "";
    S.push(new StringBuilder());
    if (root instanceof Leaf)
     {final Leaf l = (Leaf)root;
      l.printHorizontally(S, 0, false);
      return S.toString();
     }
    final Branch b = (Branch)root;
    final int    N = b.size();
    for (int i = 0; i < N; i++)
     {final Node<Type> n = b.nextLevel.elementAt(i);
      n.printHorizontally(S, 1, false);
      S.firstElement().append(" "+b.keyNames.elementAt(i));
     }
    b.topNode.printHorizontally(S, 1, false);
    return joinStrings(S);
   }

//D0 Tests                                                                      // Test the BTree

  static void test_create()
   {var m = new Mjaf<Integer>(6);
    var l = m.leaf();
    l.putLeaf(2,4);
    l.putLeaf(4,8);
    l.putLeaf(1,1);
    l.putLeaf(3,6);
    l.putLeaf(5,10);
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10)");
    ok(l.size(),        5);
    ok(l.findIndexOfKey(3), 3);
    ok(l.findIndexOfKey(9), 0);
    ok(m.keyDataStored, 5);

    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 3:6, 4:8, 5:10)"); ok(m.size(), 4);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 4:8, 5:10)");      ok(m.size(), 3);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 5:10)");           ok(m.size(), 2);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1)");                 ok(m.size(), 1);
    l.removeLeafKeyData(0); l.ok("Leaf()");                    ok(m.size(), 0);
   }

  static void test_leaf_split()
   {var m = new Mjaf<Integer>(6);
    var l = m.leaf();
    l.putLeaf(2,4);
    l.putLeaf(4,8);
    l.putLeaf(1,1);
    l.putLeaf(3,6);
    l.putLeaf(5,10);
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10)");
    var k = l.splitLeaf(2);
    k.ok("Leaf(1:1, 2:4)");
    l.ok("Leaf(3:6, 4:8, 5:10)");
    ok(l.findIndexOfKey(4), 2);
    k.joinLeaf(l);
    k.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10)");
   }

  static void test_leaf_split_in_half()
   {var m = new Mjaf<Integer>(6);
    var l = m.leaf();
    l.putLeaf(2,4);
    l.putLeaf(4,8);
    l.putLeaf(1,1);
    l.putLeaf(3,6);
    l.putLeaf(5,10);
    l.putLeaf(6,12);
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10, 6:12)");
    var k = l.splitLeafInHalf();
    k.ok("Leaf(1:1, 2:4, 3:6)");
    l.ok("Leaf(4:8, 5:10, 6:12)");
   }

  static void test_branch()
   {var m = new Mjaf<Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    Mjaf<Integer>.Branch B = m.branch(f);
    B.putBranch(1, a); ok(B.branchIsFull(), false);
    B.putBranch(2, b); ok(B.branchIsFull(), false);
    B.putBranch(3, c); ok(B.branchIsFull(), false);
    B.putBranch(4, d); ok(B.branchIsFull(), false);
    B.putBranch(5, e); ok(B.branchIsFull(), true);
    B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");

    ok(B.findIndexOfKey(3), 3);
    ok(B.splitKey(), 3);
    ok(B.size(), 5);

    Mjaf<Integer>.Branch A = B.splitBranch(3);
    A.ok("Branch(1:1, 2:2, 3:3, 4)");
    B.ok("Branch(5:5, 6)");
    A.joinBranch(B, 4);
    A.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");
   }

  static void test_branch_full()
   {var m = new Mjaf<Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    var g = m.leaf();
    Mjaf<Integer>.Branch B = m.branch(g);
    B.putBranch(1, a);
    B.putBranch(2, b);
    B.putBranch(3, c);
    B.putBranch(4, d);
    B.putBranch(5, e);
    B.putBranch(6, f);
    B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6:6, 7)");
    ok(B.splitKey(), 3);
    ok(B.branchIsFull(), true);
    ok(B.size(), 6);
    Mjaf<Integer>.Branch C = B.splitBranchInHalf();
    C.ok("Branch(1:1, 2:2, 3)");
    B.ok("Branch(4:4, 5:5, 6:6, 7)");
   }

  static void test_pad_strings()
   {final Stack<StringBuilder> S = new Stack<StringBuilder>();
    padStrings(S, 0); S.lastElement().append(11);
    padStrings(S, 1); S.lastElement().append(22);
    padStrings(S, 2); S.lastElement().append(33);
    padStrings(S, 3); S.lastElement().append(44);
    padStrings(S, 3);
    final String s = joinStrings(S);
    ok(s, """
11      |
  22    |
    33  |
      44|
""");
   }

  static void test_branch_greater_than_or_equal()
   {var m = new Mjaf<Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    var g = m.leaf();
    Mjaf<Integer>.Branch B = m.branch(g);
    B.putBranch(2, a);
    B.putBranch(4, b);
    B.putBranch(6, c);
    B.putBranch(8, d);
    B.putBranch(10, e);
    B.putBranch(12, f);
    B.ok("Branch(2:1, 4:2, 6:3, 8:4, 10:5, 12:6, 7)");

    ok(B.findFirstGreaterOrEqual(4), 2);
    ok(B.findFirstGreaterOrEqual(3), 2);
    ok(B.findFirstGreaterOrEqual(2), 1);
   }

  static void test_insert(int N, boolean debug, String expected)
   {var m = mjaf(4);
    for (long i = 0; i < N; i++) m.put(i, i<<1);
    if (debug) say(m.printHorizontally());
    ok(m.printHorizontally(), expected);
   }

  static void test_insert_reverse(int N, boolean debug, String expected)
   {var m = mjaf(4);
    for (long i = N; i >= 0; i--) m.put(i, i<<1);
    if (debug) say(m.printHorizontally());
    ok(m.printHorizontally(), expected);
   }

  static void test_insert()
   {if (true) test_insert(9, !true, """
    1    3    5     |
0,1  2,3  4,5  6,7,8|
""");

    if (true) test_insert(10, !true, """
        3           |
   1        5       |
0,1 2,3  4,5 6,7,8,9|
""");

    if (true) test_insert(64, !true, """
                                        15                                                       31                                                                                                             |
               7                                                    23                                                       39                          47                                                     |
       3                 11                           19                          27                           35                          43                          51            55                         |
   1       5       9            13             17            21            25            29             33            37            41            45            49            53            57     59           |
0,1 2,3 4,5 6,7 8,9 10,11  12,13  14,15   16,17  18,19  20,21  22,23  24,25  26,27  28,29  30,31   32,33  34,35  36,37  38,39  40,41  42,43  44,45  46,47  48,49  50,51  52,53  54,55  56,57  58,59  60,61,62,63|
""");
   }

  static void test_insert_reverse()
   {if (true) test_insert_reverse(9, !true, """
            5       |
       3        7   |
0,1,2,3 4,5  6,7 8,9|
""");

    if (true) test_insert_reverse(10, !true, """
              6        |
     2   4        8    |
0,1,2 3,4 5,6  7,8 9,10|
""");

    if (true) test_insert_reverse(64, !true, """
                                                                                                    32                                                       48                                                      |
                                           16                          24                                                       40                                                       56                          |
                 8           12                          20                          28                           36                          44                           52                          60            |
     2   4   6        10            14            18            22            26            30             34            38            42            46             50            54            58            62     |
0,1,2 3,4 5,6 7,8 9,10  11,12  13,14  15,16  17,18  19,20  21,22  23,24  25,26  27,28  29,30  31,32   33,34  35,36  37,38  39,40  41,42  43,44  45,46  47,48   49,50  51,52  53,54  55,56  57,58  59,60  61,62  63,64|
""");

    if (true) test_insert_reverse(12, !true, """
                  8           |
     2   4   6         10     |
0,1,2 3,4 5,6 7,8  9,10  11,12|
""");

    if (true) test_insert_reverse(13, !true, """
            5        9            |
       3        7          11     |
0,1,2,3 4,5  6,7 8,9  10,11  12,13|
""");
   }

  static void test_insert_random()
   {final long[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    var m = mjaf(4);
    for (int i = 0; i < r.length; ++i) m.put(r[i], (long)i);
    ok(m.printHorizontally(), """
                                                                                                                                                                                                                                                511                                                                                                                                                                                                                     |
                                                               186                                                               317                                                                                                                                                                                        658                                                                       858                                                               |
                                   103                                                     246                                                                               403                                   472                                                                    578                                                           704                                                                       912                                   |
       27     39        72                   135                                 234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
   }

  static void test_find()
   {final int N = 64;
    var m = mjaf(4);
    for (long i = 0; i < N; i++) m.put(i, i<<1);
    for (long i = 0; i < N; i++) ok(m.find(i), i+i);
   }

  static void test_find_reverse()
   {final int N = 64;
    var m = mjaf(4);
    for (long i = N; i >= 0; i--) m.put(i, i<<1);
    for (long i = N; i >= 0; i--) ok(m.find(i), i+i);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_create();
    test_leaf_split();
    test_leaf_split_in_half();
    test_branch();
    test_branch_full();
    test_pad_strings();
    test_insert();
    test_insert_reverse();
    test_find();
    test_find_reverse();
    test_insert_random();
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
