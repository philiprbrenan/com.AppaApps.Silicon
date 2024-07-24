//------------------------------------------------------------------------------
// Btree with data stored only in the leaves to simplify deletion.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
//                 Shall I compare thee to a summerâs day?
//                 Thou art more lovely and more temperate:
//                 Rough winds do shake the darling buds of May,
//                 And summerâs lease hath all too short a date;

//                 Sometime too hot the eye of heaven shines,
//                 And often is his gold complexion dimm'd;
//                 For every fair from fair sometime declines,
//                 By chance or natureâs changing course untrimm'd;

//                 But now thou art for ever fairly made,
//                 The eye of heaven lights thy face for me,
//                 Nor shall death brag thou wanderâst in his shade,
//                 When these lines being read give life to thee!
import java.util.*;

class Mjaf<Key extends Comparable<Key>, Data> extends Chip                      // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission, whereas the leaves (exterior nodes) have even number of keys and matching number of data elements because data is not transferred to the parent on fission  which simplifies deletions with complicating insertions.
 {final int maxKeysPerLeaf;                                                     // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches as whether classic Btree algorithm.

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  Node<Key> root;                                                               // The root node of the Btree
  int keyDataStored;                                                            // The number of key, data values stored in the Btree

  Mjaf(int MaxKeysPerLeaf)                                                      // Define a Btree with a specified maximum number of keys per leaf.
   {final int N = MaxKeysPerLeaf;
    if (N % 2 == 1) stop("# keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("# keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf = N;
   }

  Mjaf() {this(4);}                                                             // Define a Btree with a minimal maximum number of keys per node

  static Mjaf<Long, Long> mjaf()      {return new Mjaf<>();}                    // Create a new Btree of default type
  static Mjaf<Long, Long> mjaf(int m) {return new Mjaf<>(m);}                   // Define a new Btree of default type with a specified maximum number of keys per node

  int size() {return keyDataStored;}                                            // Number of entries in the tree

  int nodesCreated = 0;                                                         // Number of nodes created

  abstract class Node<Key extends Comparable<Key>>                              // A branch or a leaf: an interior or exterior node.
   {final Stack<Key> keyNames;                                                  // Names of the keys in this branch or leaf
    final int nodeNumber = ++nodesCreated;                                      // Number of this node
    Node() {keyNames = new Stack<Key>();}                                       // Create a node

    int findIndexOfKey(Key keyToFind) {return keyNames.indexOf(keyToFind)+1;}   // Find the one based index of a key in a branch node or zero if not found
    int splitIdx() {return (maxKeysPerLeaf - 1) / 2;}                           // Index of splitting key
    Key splitKey() {return keyNames.elementAt(splitIdx());}                     // Splitting key
    int size    () {return keyNames.size();}                                    // Number of elements in this leaf
    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check node is as expected
    boolean lessThanOrEqual(Key a, Key b) {return a.compareTo(b) <= 0;}         // Define a new Btree of default type with a specified maximum number of keys per node

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug) {}; // Print horizontally
   }

  class Branch extends Node<Key>                                                // A branch node directs the search to the appropriate leaf
   {final Stack<Node<Key>> nextLevel;
    Node<Key> topNode;

    Branch(Node<Key> Top) {nextLevel = new Stack<Node<Key>>(); topNode = Top;}  // Create a new branch
    boolean branchIsFull() {return size() >= maxKeysPerLeaf-1;}                 // Node should be split

    int findFirstGreaterOrEqual(Key keyName)                                    // Find first key which is greater an the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {final int N = size();                                                     // Number of keys currently in node
      for (int i = 0; i < N; i++)
       {final Key kn = keyNames.elementAt(i);                                   // Key we are checking
        if (lessThanOrEqual(keyName, kn)) return i + 1;                         // Current key is greater than or equal to the search key
       }
      return 0;
     }

    void putBranch(Key keyName, Node<Key> putNode)                              // Insert a new node into a branch
     {final int N = nextLevel.size();                                           // Number of keys currently in node
      if (N >= maxKeysPerLeaf) stop("Too many keys in Branch");
      for (int i = 0; i < N; i++)
       {final Key kn = keyNames.elementAt(i);                                   // Key we are checking
        if (lessThanOrEqual(keyName, kn))                                       // Insert new key in order
         {keyNames .insertElementAt(keyName, i);
          nextLevel.insertElementAt(putNode, i);
          return;
         }
       }
      keyNames .push(keyName);                                                  // Either the leaf is empty or the new key is greater than every existing key
      nextLevel.push(putNode);
     }

    void splitRoot()                                                            // Split the root
     {if (branchIsFull())
       {final Key    k = splitKey();
        final Branch l = splitBranchInHalf(), b = branch(this);
        b.putBranch(k, l);
        root = b;
       }
     }

    Branch splitBranch(int First)                                               // Split a branch into two branches in the indicated key
     {final int K = keyNames.size(), f = First;                                 // Number of keys currently in node
      if (f < K-1) {} else stop("Split", f, "too big for branch of size:", K);
      if (f <   1)         stop("First", f, "too small");
      final Branch b = new Branch(nextLevel.elementAt(f));
      for (int i = 0; i < f; i++)
       {b.keyNames .push(keyNames .remove(0));
        b.nextLevel.push(nextLevel.remove(0));
       }
      keyNames .remove(0);
      nextLevel.remove(0);
      return b;
     }

    Branch splitBranchInHalf() {return splitBranch(splitIdx());}                // Split a branch in half

    void joinBranch(Branch Join, Key joinKeyName)                               // Append the second branch to the first one adding the specified key
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (J + K >= maxKeysPerLeaf) stop("Join of branch has too many keys",
        K, "+1+", J, "greater than", maxKeysPerLeaf);

      keyNames .push(joinKeyName);
      nextLevel.push(topNode); topNode = Join.topNode;

      for (int i = 0; i < J; i++)
       {keyNames .push(Join.keyNames .elementAt(i));
        nextLevel.push(Join.nextLevel.elementAt(i));
       }
     }

    public String toString()                                                    // Print branch
     {final StringBuilder s = new StringBuilder("Branch(");
      final int K = keyNames.size();

      for (int i = 0; i < K; i++)
        s.append(""+keyNames.elementAt(i)+":"+
          nextLevel.elementAt(i).nodeNumber+", ");

      s.append(""+topNode.nodeNumber+")");
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print branch
     {for (int i = 0; i < size(); i++)
       {nextLevel.elementAt(i).printHorizontally(S, level+1, debug);
        padStrings(S, level);
        S.elementAt(level).append(keyNames.elementAt(i));
       }
      topNode.printHorizontally(S, level+1, debug);
     }

    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check leaf
   } // Branch

  Branch branch(Node<Key> node) {return new Branch(node);}                      // Create a new branch

  class Leaf extends Node<Key>                                                  // Create a new leaf
   {final Stack<Data> dataValues;                                               // Data associated with each key
    Leaf() {dataValues = new Stack<Data>();}                                    // Data associated with keys in leaf
    boolean leafIsFull() {return size() >= maxKeysPerLeaf;}                     // Leaf is full

    void putLeaf(Key keyName, Data dataValue)                                   // Insert a new leaf value
     {final int K = keyNames.size();                                            // Number of keys currently in node
      if (K >= maxKeysPerLeaf) stop("Too many keys in leaf");

      for (int i = 0; i < K; i++)
       {if (lessThanOrEqual(keyName, keyNames.elementAt(i)))                    // Insert new key in order
         {keyNames  .insertElementAt(keyName,   i);
          dataValues.insertElementAt(dataValue, i);
          ++keyDataStored;                                                      // Create a new entry in the leaf
          return;
         }
       }

      keyNames  .push(keyName);                                                 // Either the leaf is empty or the new key is greater than every existing key
      dataValues.push(dataValue);
      ++keyDataStored;                                                          // Created a new entry in the leaf
     }

    void removeLeafKeyData(int Index)                                           // Remove the indicated key data pair from a leaf
     {final int K = size(), i = Index;                                          // Number of keys currently in node
      if (i < K) {} else stop("Index", i, "too big for leaf of size:", K);
      if (i < 0)         stop("Index", i, "too small");
      keyNames  .removeElementAt(i);
      dataValues.removeElementAt(i);
      --keyDataStored;
     }

    Leaf splitLeaf(int First)                                                   // Split the leaf into two leafs - the new leaf consists of the indicated first elements, the old leaf retains the rest
     {final int K = size(), f = First;                                          // Number of keys currently in node
      if (f < K) {} else stop("Split", f, "too big for leaf of size:", K);
      if (f < 1)         stop("First", f, "too small");
      final Leaf l = leaf();
      for (int i = 0; i < f; i++)                                               // Transfer keys and data
       {l.keyNames  .push(keyNames  .remove(0));                                // Transfer keys
        l.dataValues.push(dataValues.remove(0));                                // Transfer data
       }
      return l;                                                                 // Split out leaf
     }

    Leaf splitLeafInHalf() {return splitLeaf(maxKeysPerLeaf/2);}                // Split a leaf in half

    void joinLeaf(Leaf Join)                                                    // Join the specified leaf onto the end of this leaf
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (J + K > maxKeysPerLeaf) stop("Join of leaf has too many keys", K,
        "+", J, "greater than", maxKeysPerLeaf);

      for (int i = 0; i < J; i++)
       {keyNames  .push(Join.keyNames  .elementAt(i));
        dataValues.push(Join.dataValues.elementAt(i));
       }
     }

    public String toString()                                                    // Print leaf
     {final StringBuilder s = new StringBuilder();
      s.append("Leaf(");
      final int K = keyNames.size();
      for  (int i = 0; i < K; i++)
        s.append(""+keyNames.elementAt(i)+":"+dataValues.elementAt(i)+", ");
      if (K > 0) s.setLength(s.length()-2);
      s.append(")");
      return s.toString();
     }

    public String shortString()                                                 // Print a leaf compactly
     {final StringBuilder s = new StringBuilder();
      final int K = keyNames.size();
      for  (int i = 0; i < K; i++) s.append(""+keyNames.elementAt(i)+",");
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

  Data find(Key keyName)                                                        // Find a the data associated with a key
   {if (root == null) return null;                                              // Empty tree
    for(Node<Key> n = root; n != null;)                                         // Step down through tree
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

  void put(Key keyName, Data dataValue)                                         // Insert a new key, data pair into the Btree
   {if (root == null)
     {final Leaf l = leaf();
      root = l;
      l.putLeaf(keyName, dataValue);
      return;
     }

    if (root instanceof Leaf)                                                   // Insert into root as a leaf
     {final Leaf r = (Leaf)root;
      if (!r.leafIsFull())
       {r.putLeaf(keyName, dataValue);
        return;
       }
      else                                                                      // Insert into root as a leaf which is full
       {final Leaf   l = r.splitLeafInHalf();
        final Branch b = branch(root);
        b.putBranch(l.splitKey(), l);                                           // Insert left hand node all of whose elements are less than the first element of what was the root
        root = b;
       }
     }

    else if (root instanceof Branch) ((Branch)root).splitRoot();                // Split full root which is a branch not a leaf

    Branch p = (Branch)root; Node<Key> q = p;                                   // The root has already been split so the parent child relationship will be established

    for(int i = 0; i < 999; ++i)                                                // Step down through tree to find the required leaf, splitting as we go
     {if (q instanceof Branch)                                                  // Stepped to a branch
       {final Branch  qb = (Branch)q;
        if (qb.branchIsFull())                                                  // Split the branch because it is full and we might need to insert below it requiring a slot in this node
         {final Key    k = qb.splitKey();
          final Branch l = qb.splitBranchInHalf();
          p.putBranch(k, l);
          ((Branch)root).splitRoot();                                           // Root might need to be split to re-establish the invariants at start of loop
          q = p = (Branch)root;                                                 // Root might have changed after prior split
          continue;
         }

        final int g = qb.findFirstGreaterOrEqual(keyName);
        p = qb; q = g == 0 ? qb.topNode : qb.nextLevel.elementAt(g-1);
        continue;                                                               // Continue to step down
       }

      final Leaf l = (Leaf)q;
      final int  g = l.findIndexOfKey(keyName);
      if (g != 0)                                                               // Key already present in leaf
       {l.dataValues.setElementAt(dataValue, g-1);
        return;                                                                 // Data replaced at key
       }

      if (l.leafIsFull())                                                       // Split the node because it is full and we might need to insert below it requiring a slot in this node
       {final Key k = l.splitKey();
        final Leaf e = l.splitLeafInHalf();
        p.putBranch(k, e);
        if (p.lessThanOrEqual(keyName, k)) e.putLeaf(keyName, dataValue);       // Insert key in the appropriate split leaf
        else                               l.putLeaf(keyName, dataValue);
        return;
       }
      l.putLeaf(keyName, dataValue);                                            // On a leaf that is not full so we can insert directly
      return;                                                                   // Key, data pair replaced
     }
   }

//D1 Deletion                                                                   // Delete a key from a Btree

  Data delete(Key keyName)                                                      // Delete a key from a tree
   {if (root == null) return null;                                              // The tree is empty
    final Data foundData = find(keyName);                                       // Find the data associated with the key
    if (foundData == null) return null;                                         // The key is not present so cannot be deleted

    if (root instanceof Leaf)                                                   // Delete from root as a leaf
     {final Leaf r = (Leaf)root;
      r.removeLeafKeyData(r.findIndexOfKey(keyName)-1);                         // Only one leaf and the key is known to be in the Btree so it must be in this leaf
      return foundData;
     }

    if (root.size() == 1)                                                       // If the root is a branch and only has one key so we might be able to merge its children
     {final Branch r = (Branch)root;
      final Node<Key> A = r.nextLevel.firstElement();
      final Node<Key> B = r.topNode;
      if (A instanceof Leaf)
       {final Leaf a = (Leaf)A, b = (Leaf)B;
        if (a.size() + b.size() <= maxKeysPerLeaf)                              // Can we merge the two leaves
         {a.joinLeaf(b);
          root = a;                                                             // New merged root
         }
       }
      else                                                                      // Merge two branches under root
       {final Branch a = (Branch)A, b = (Branch)B;
        if (a.size() + b.size() < maxKeysPerLeaf)                               // Can we merge the two branches
         {a.joinBranch(b, r.keyNames.firstElement());
          root = a;                                                             // New merged root
         }
       }
     }

    Node<Key> P = root;                                                         // We now know that the root is a branch

    for    (int i = 0; i < 999; ++i)                                            // Step down through tree to find the required leaf, splitting as we go
     {if (P instanceof Branch)                                                  // Stepped to a branch
       {final Branch p = (Branch)P;
        for(int j = 0; j < p.size()-1;   ++j)                                   // See if any pair under this node can be merged
         {final Node<Key> A = p.nextLevel.elementAt(j);
          final Node<Key> B = p.nextLevel.elementAt(j+1);
          if (A instanceof Leaf)
           {final Leaf a = (Leaf)A, b = (Leaf)B;
            if (a.size() + b.size() <= maxKeysPerLeaf)                          // Can we merge the two leaves
             {a.joinLeaf(b);
              p.keyNames.remove(j);
              p.nextLevel.remove(j+1);
             }
           }
          else                                                                  // Merge two branches
           {final Branch a = (Branch)A, b = (Branch)B;
            if (a.size() + b.size() < maxKeysPerLeaf)                           // Can we merge the two branches
             {a.joinBranch(b, p.keyNames.remove(j));
              p.nextLevel.remove(j+1);
             }
           }
         }

        if (p.size() > 0)                                                       // Check last pair
         {final Node<Key> A = p.nextLevel.lastElement();
          final Node<Key> B = p.topNode;
          if (A instanceof Leaf)
           {final Leaf a = (Leaf)A, b = (Leaf)B;
            if (a.size() + b.size() <= maxKeysPerLeaf)                          // Can we merge the two leaves
             {a.joinLeaf(b);
              p.keyNames.pop();
              p.nextLevel.pop();
              p.topNode = a;
             }
           }
          else                                                                  // Merge two branches
           {final Branch a = (Branch)A, b = (Branch)B;
            if (a.size() + b.size() < maxKeysPerLeaf)                           // Can we merge the last two branches
             {a.joinBranch(b, p.keyNames.pop());
              p.nextLevel.pop();
              p.topNode = a;
             }
           }
         }
        final int g = p.findFirstGreaterOrEqual(keyName);                       // Find key position in branch
        P = g == 0 ? p.topNode : p.nextLevel.elementAt(g-1);
        continue;
       }

      --keyDataStored;                                                          // Remove one entry
      final Leaf l = (Leaf)P;                                                   // Reached a leaf
      final int  F = l.findIndexOfKey(keyName)-1;                               // Key is known to be present
      l.keyNames  .remove(F);
      l.dataValues.remove(F);
      return foundData;
     }
    return null;
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
     {((Leaf)root).printHorizontally(S, 0, false);
      return S.toString()+"\n";
     }

    final Branch b = (Branch)root;
    final int    N = b.size();
    for (int i = 0; i < N; i++)
     {b.nextLevel.elementAt(i).printHorizontally(S, 1, false);
      S.firstElement().append(" "+b.keyNames.elementAt(i));
     }
    b.topNode.printHorizontally(S, 1, false);
    return joinStrings(S);
   }

//D0 Tests                                                                      // Test the BTree

  static void test_create()
   {var m = new Mjaf<Integer,Integer>(6);
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
   {var m = new Mjaf<Integer,Integer>(6);
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
   {var m = new Mjaf<Integer,Integer>(6);
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
   {var m = new Mjaf<Integer,Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    Mjaf<Integer,Integer>.Branch B = m.branch(f);
    B.putBranch(1, a); ok(B.branchIsFull(), false);
    B.putBranch(2, b); ok(B.branchIsFull(), false);
    B.putBranch(3, c); ok(B.branchIsFull(), false);
    B.putBranch(4, d); ok(B.branchIsFull(), false);
    B.putBranch(5, e); ok(B.branchIsFull(), true);
    B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");

    ok(B.findIndexOfKey(3), 3);
    ok(B.splitKey(), 3);
    ok(B.size(), 5);

    Mjaf<Integer,Integer>.Branch A = B.splitBranch(3);
    A.ok("Branch(1:1, 2:2, 3:3, 4)");
    B.ok("Branch(5:5, 6)");
    A.joinBranch(B, 4);
    A.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");
   }

  static void test_branch_full()
   {var m = new Mjaf<Integer,Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    var g = m.leaf();
    Mjaf<Integer,Integer>.Branch B = m.branch(g);
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
    Mjaf<Integer,Integer>.Branch C = B.splitBranchInHalf();
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
   {var m = new Mjaf<Integer,Integer>(6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    var g = m.leaf();
    Mjaf<Integer,Integer>.Branch B = m.branch(g);
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

  static long[]random_array()                                                   // Random array
   {final long[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    return r;
   }

  static void test_insert_random()
   {final long[]r = random_array();
    var m = mjaf(4);
    for (int i = 0; i < r.length; ++i) m.put(r[i], (long)i);
    ok(m.printHorizontally(), """
                                                                                                                                                                                                                                                511                                                                                                                                                                                                                     |
                                                               186                                                               317                                                                                                                                                                                        658                                                                       858                                                               |
                                   103                                                     246                                                                               403                                   472                                                                    578                                                           704                                                                       912                                   |
       27     39        72                   135                                 234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
    if (github_actions) for (int i = 0; i < r.length; ++i) ok(m.find(r[i]), (long)i);
    ok(m.find(r.length+1l), null);
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

  static void test_delete()
   {final long[]r = random_array();
    var m = mjaf(4);
    for (int i = 0; i < r.length; ++i) m.put(r[i], (long)i);
    for (int i = 0; i < r.length; ++i)
     {ok(m.delete(r[i]), (long)i);
      if (false)                                                                // Write case statement to check deletions
       {say("        case", i, "-> /*", r[i], "*/ ok(m.printHorizontally(), \"\"\"");
        say(m.printHorizontally());
        say("\"\"\");");
       }
      ok(m.size(), r.length - i - 1);
      switch(i)
       {case 0 -> /* 27 */ ok(m.printHorizontally(), """
                                                                                                                                                                                                                                             511                                                                                                                                                                                                                     |
                                                                                                                              317                                                                                                                                                                                        658                                                                       858                                                               |
                                103                                                     246                                                                               403                                   472                                                                    578                                                           704                                                                       912                                   |
    27     39        72                   135               186               234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 1 -> /* 442 */ ok(m.printHorizontally(), """
                                                                                                                               317                                                                                                                                                                                    658                                                                                                                                         |
                                103                                                     246                                                                                403                                                           511                                       578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                      344       358           391                 425           442       472               501                     545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 2 -> /* 545 */ ok(m.printHorizontally(), """
                                                                                                                               317                                                                                                                                                                                658                                                                                                                                         |
                                103                                                     246                                                                                403                                                           511                                   578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                      344       358           391                 425           442       472               501                 545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511   516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 3 -> /* 317 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                658                                                                                                                                         |
                                103                                                     246                                                                            403                                                           511                                   578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442       472               501                 545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511   516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 4 -> /* 511 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                            658                                                                                                                                         |
                                103                                                     246                                                                            403                                                       511                                   578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442       472               501             545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 5 -> /* 578 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                      658                                                                                                                                         |
                                103                                                     246                                                                            403                                                       511                             578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442       472               501                     560                     611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 6 -> /* 391 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                658                                                                                                                                         |
                                103                                                     246                                                                      403                                                       511                                                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442       472               501                     560           578       611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 7 -> /* 993 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                658                                                                                                                                   |
                                103                                                     246                                                                      403                                                       511                                                                                                                                          858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442       472               501                     560           578       611           650                          686       704               806           830                         903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 8 -> /* 858 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                                                 |
                                103                                                     246                                                                      403                                                       511                                                               658                                                                   858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442       472               501                     560           578       611           650                         686       704               806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526,554,560   564,576,577   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 9 -> /* 586 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                                             |
                                103                                                     246                                                                      403                                                       511                                                           658                                                                   858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442       472               501                     560           578   611           650                         686       704               806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503   516,526,554,560   564,576,577   611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 10 -> /* 472 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                                         |
                                103                                                     246                                                                      403                                                   511                                                           658                                                                   858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442   472               501                     560           578   611           650                         686       704               806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577   611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 11 -> /* 906 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                                     |
                                103                                                     246                                                                      403                                                   511                                                           658                                                                   858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442   472               501                     560           578   611           650                         686       704               806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577   611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 12 -> /* 658 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                               |
                                103                                                     246                                                                      403                                                   511                                                     658                                                                   858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442   472               501                     560               611           650                     686       704               806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 13 -> /* 704 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                           |
                                103                                                     246                                                                      403                                                   511                                                     658                                                               858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442   472               501                     560               611           650                     686   704               806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 14 -> /* 882 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                                                                                                                                                                                       |
                                103                                                     246                                                                      403                                                   511                                                     658                                                               858                                                 |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442   472               501                     560               611           650                     686   704               806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 15 -> /* 246 */ ok(m.printHorizontally(), """
                                                                                                                       317                                                                                                                                                                                                                                                                       |
                                103                                                 246                                                                      403                                                   511                                                     658                                                               858                                                 |
    27     39        72                   135               186               234             261           279                          358       391                 425           442   472               501                     560               611           650                     686   704               806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 16 -> /* 261 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                                                                                                                                                                                                       |
                                103                                                 246                                                                  403                                                   511                                                     658                                                               858                                                 |
    27     39        72                   135               186               234         261           279                          358       391                 425           442   472               501                     560               611           650                     686   704               806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 17 -> /* 501 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                                                                                                                                                                                                 |
                                103                                                 246                                                                  403                                             511                                                     658                                                               858                                                 |
    27     39        72                   135               186               234         261           279                          358       391                 425               472           501                     560               611           650                     686   704               806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438,447   480,490,494   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 18 -> /* 354 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                                                                                                                                                                                           |
                                103                                                 246                                                            403                                             511                                                     658                                                               858                                                 |
    27     39        72                   135               186               234         261           279                      358                         425               472           501                     560               611           650                     686   704               806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 19 -> /* 903 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                                                                                                                                                                                     |
                                103                                                 246                                                            403                                             511                                                     658                                                               858                                           |
    27     39        72                   135               186               234         261           279                      358                         425               472           501                     560               611           650                     686   704               806           830                     912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,907,912   922,937,946,961   976,987,989|
""");
        case 20 -> /* 854 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                                                                                                                                                                                 |
                                103                                                 246                                                            403                                             511                                                     658                                                           858                                           |
    27     39        72                   135               186               234         261           279                      358                         425               472           501                     560               611           650                     686   704               806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 21 -> /* 279 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                                                 |
                                103                                                 246                                                      403                                             511                                                     658                                                           858                                           |
    27     39        72                   135               186               234                 279                      358                         425               472           501                     560               611           650                     686   704               806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,526,554,560   564,576,577,611   612,615,650   657   667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 22 -> /* 526 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                                           |
                                103                                                 246                                                      403                                             511                                               658                                                           858                                           |
    27     39        72                   135               186               234                 279                      358                         425               472           501                 560               611                                 686   704               806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 23 -> /* 686 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                                     |
                                103                                                 246                                                      403                                             511                                               658                                                     858                                           |
    27     39        72                   135               186               234                 279                      358                         425               472           501                 560               611                             686   704               806                             912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976,987,989|
""");
        case 24 -> /* 987 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                                 |
                                103                                                 246                                                      403                                             511                                               658                                                     858                                       |
    27     39        72                   135               186               234                 279                      358                         425               472           501                 560               611                             686   704               806                             912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976,989|
""");
        case 25 -> /* 403 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                             |
                                103                                                 246                                                  403                                             511                                               658                                                     858                                       |
    27     39        72                   135               186               234                 279                      358                     425               472           501                 560               611                             686   704               806                             912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976,989|
""");
        case 26 -> /* 401 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                         |
                                103                                                 246                                              403                                             511                                               658                                                     858                                       |
    27     39        72                   135               186               234                 279                      358                 425               472           501                 560               611                             686   704               806                             912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976,989|
""");
        case 27 -> /* 989 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                     |
                                103                                                 246                                              403                                             511                                               658                                                     858                                   |
    27     39        72                   135               186               234                 279                      358                 425               472           501                 560               611                             686   704               806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,650,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 28 -> /* 650 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                                 |
                                103                                                 246                                              403                                             511                                           658                                                     858                                   |
    27     39        72                   135               186               234                 279                      358                 425               472           501                 560               611                         686   704               806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438,447   480,490,494   503   516,554,560   564,576,577,611   612,615,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 29 -> /* 576 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                             |
                                103                                                 246                                              403                                             511                                       658                                                     858                                   |
    27     39        72                   135               186               234                 279                      358                 425               472           501                 560           611                         686   704               806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438,447   480,490,494   503   516,554,560   564,577,611   612,615,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 30 -> /* 436 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                       |
                                103                                                 246                                              403                                       511                                       658                                                     858                                   |
    27     39        72                   135               186               234                 279                      358                 425           472                             560           611                         686   704               806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494,503   516,554,560   564,577,611   612,615,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 31 -> /* 560 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                                   |
                                103                                                 246                                                                                        511                                   658                                                     858                                   |
    27     39        72                   135               186               234                 279                      358       403       425           472                         560           611                         686   704               806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494,503   516,554   564,577,611   612,615,657   667,679,681   690   769,773,804,806   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 32 -> /* 806 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                             |
                                103                                                 246                                                                                        511                                   658                                               858                                   |
    27     39        72                   135               186               234                 279                      358       403       425           472                         560           611                             704           806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494,503   516,554   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 33 -> /* 554 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                         |
                                103                                                 246                                                                                        511                               658                                               858                                   |
    27     39        72                   135               186               234                 279                      358       403       425           472                     560           611                             704           806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494,503   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 34 -> /* 422 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                                                                   |
                                103                                                 246                                                                                  511                               658                                               858                                   |
    27     39        72                   135               186               234                 279                      358           425           472                     560           611                             704           806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,425   437,438,447   480,490,494,503   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 35 -> /* 298 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                                   |
                                103                                                 246                                                                              511                               658                                               858                                   |
    27     39        72                   135               186               234                 279                  358           425           472                     560           611                             704           806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377,425   437,438,447   480,490,494,503   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 36 -> /* 425 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                               |
                                103                                                 246                                                                          511                               658                                               858                                   |
    27     39        72                   135               186               234                 279                  358       425           472                     560           611                             704           806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494,503   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 37 -> /* 912 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                           |
                                103                                                 246                                                                          511                               658                                               858                               |
    27     39        72                   135               186               234                 279                  358       425           472                     560           611                             704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494,503   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 38 -> /* 503 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                       |
                                103                                                 246                                                                      511                               658                                               858                               |
    27     39        72                   135               186               234                 279                  358       425           472                 560           611                             704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516   564,577,611   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 39 -> /* 611 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                 |
                                103                                                 246                                                                      511                         658                                               858                               |
    27     39        72                   135               186               234                 279                  358       425           472                         611                             704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 40 -> /* 135 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                                 |
                                103                                             246                                                                      511                         658                                               858                               |
    27     39        72               135               186               234                 279                  358       425           472                         611                             704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 41 -> /* 447 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                             |
                                103                                             246                                                                  511                                                                           858                               |
    27     39        72               135               186               234                 279                  358       425       472                         611           658               704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 42 -> /* 344 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                       |
                                103                                             246                                                            511                                                                           858                               |
    27     39        72               135               186               234                 279              358               472                         611           658               704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 43 -> /* 338 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                   |
                                103                                             246                                                        511                                                                           858                               |
    27     39        72               135               186               234                 279          358               472                         611           658               704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 44 -> /* 39 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                                   |
                            103                                             246                                                        511                                                                           858                               |
       39        72               135               186               234                 279          358               472                         611           658               704           806                         912               961   |
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 45 -> /* 804 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                               |
                            103                                             246                                                        511                                                                       858                               |
       39        72               135               186               234                 279          358               472                         611           658               704       806                         912               961   |
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 46 -> /* 976 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                            |
                            103                                             246                                                        511                                                                       858                            |
       39        72               135               186               234                 279          358               472                         611           658               704       806                         912               961|
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 47 -> /* 186 */ ok(m.printHorizontally(), """
                                                                                             317                                                                                                                                            |
                            103                                         246                                                        511                                                                       858                            |
       39        72               135           186               234                 279          358               472                         611           658               704       806                         912               961|
1,13,29  43,55,72  90,96,103   106   151,155,157   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 48 -> /* 234 */ ok(m.printHorizontally(), """
                                                                                       317                                                                                                                                            |
                            103                                   246                                                        511                                                                       858                            |
       39        72                           186           234                 279          358               472                         611           658               704       806                         912               961|
1,13,29  43,55,72  90,96,103   106,151,155,157   188,229,232   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 49 -> /* 106 */ ok(m.printHorizontally(), """
                                                                               317                                                                                                                                            |
                            103                                                                                      511                                                                       858                            |
       39        72                       186               246                      358               472                         611           658               704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273,288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 50 -> /* 667 */ ok(m.printHorizontally(), """
                             103                                                317                                    511                                                                    858                            |
       39        72                        186               246                      358               472                          611           658           704       806                          912               961|
1,13,29  43,55,72  90,96,103    151,155,157   188,229,232,237   260,272,273,288    358   376,377,437,438   480,490,494    516,564,577   612,615,657   679,681,690   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 51 -> /* 494 */ ok(m.printHorizontally(), """
                             103                                                317                                511                                                                    858                            |
       39        72                        186               246                      358               472                      611           658           704       806                          912               961|
1,13,29  43,55,72  90,96,103    151,155,157   188,229,232,237   260,272,273,288    358   376,377,437,438   480,490    516,564,577   612,615,657   679,681,690   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 52 -> /* 690 */ ok(m.printHorizontally(), """
                             103                                                317                                511                                                                858                            |
       39        72                        186               246                      358               472                      611           658       704       806                          912               961|
1,13,29  43,55,72  90,96,103    151,155,157   188,229,232,237   260,272,273,288    358   376,377,437,438   480,490    516,564,577   612,615,657   679,681   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 53 -> /* 480 */ ok(m.printHorizontally(), """
                             103                                                317                            511                                                                858                            |
       39        72                        186               246                      358               472                  611           658       704       806                          912               961|
1,13,29  43,55,72  90,96,103    151,155,157   188,229,232,237   260,272,273,288    358   376,377,437,438   490    516,564,577   612,615,657   679,681   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 54 -> /* 288 */ ok(m.printHorizontally(), """
                             103                                            317                            511                                                                858                            |
       39        72                        186               246                  358               472                  611           658       704       806                          912               961|
1,13,29  43,55,72  90,96,103    151,155,157   188,229,232,237   260,272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 55 -> /* 151 */ ok(m.printHorizontally(), """
                             103                                        317                            511                                                                858                            |
       39        72                    186               246                  358               472                  611           658       704       806                          912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   260,272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681   769,773   809,826,830,839    884,907   922,937,946,961   |
""");
        case 56 -> /* 773 */ ok(m.printHorizontally(), """
                             103                                        317                            511                                                          858                            |
       39        72                    186               246                  358               472                  611           658           806                          912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   260,272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681,769   809,826,830,839    884,907   922,937,946,961   |
""");
        case 57 -> /* 769 */ ok(m.printHorizontally(), """
                             103                                        317                            511                                                      858                            |
       39        72                    186               246                  358               472                  611           658       806                          912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   260,272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681   809,826,830,839    884,907   922,937,946,961   |
""");
        case 58 -> /* 260 */ ok(m.printHorizontally(), """
                             103                                    317                            511                                                      858                            |
       39        72                    186               246              358               472                  611           658       806                          912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681   809,826,830,839    884,907   922,937,946,961   |
""");
        case 59 -> /* 809 */ ok(m.printHorizontally(), """
                             103                                    317                            511                                                  858                            |
       39        72                    186               246              358               472                  611           658       806                      912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   272,273    358   376,377,437,438   490    516,564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 60 -> /* 438 */ ok(m.printHorizontally(), """
                             103                                    317                        511                                                  858                            |
       39        72                    186               246              358           472                  611           658       806                      912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232,237   272,273    358   376,377,437   490    516,564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 61 -> /* 237 */ ok(m.printHorizontally(), """
                             103                                317                        511                                                  858                            |
       39        72                    186           246              358           472                  611           658       806                      912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232   272,273    358   376,377,437   490    516,564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 62 -> /* 516 */ ok(m.printHorizontally(), """
                             103                                317                        511                                              858                            |
       39        72                    186           246              358           472              611           658       806                      912               961|
1,13,29  43,55,72  90,96,103    155,157   188,229,232   272,273    358   376,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 63 -> /* 29 */ ok(m.printHorizontally(), """
                          103                                317                        511                                              858                            |
    39        72                    186           246              358           472              611           658       806                      912               961|
1,13  43,55,72  90,96,103    155,157   188,229,232   272,273    358   376,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 64 -> /* 376 */ ok(m.printHorizontally(), """
                          103                                317                  511                                              858                            |
    39        72                    186           246                      472              611           658       806                      912               961|
1,13  43,55,72  90,96,103    155,157   188,229,232   272,273    358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 65 -> /* 72 */ ok(m.printHorizontally(), """
                       103                                                    511                                              858                            |
    39     72                    186           246       317           472              611           658       806                      912               961|
1,13  43,55  90,96,103    155,157   188,229,232   272,273   358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,946,961   |
""");
        case 66 -> /* 946 */ ok(m.printHorizontally(), """
                       103                                                    511                                              858                     |
    39     72                    186           246       317           472              611           658       806                      912           |
1,13  43,55  90,96,103    155,157   188,229,232   272,273   358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,961|
""");
        case 67 -> /* 103 */ ok(m.printHorizontally(), """
                  103                                                    511                                              858                     |
          72                186           246       317           472              611           658       806                      912           |
1,13,43,55  90,96    155,157   188,229,232   272,273   358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937,961|
""");
        case 68 -> /* 961 */ ok(m.printHorizontally(), """
                  103                                                    511                                              858                 |
          72                186           246       317           472              611           658       806                      912       |
1,13,43,55  90,96    155,157   188,229,232   272,273   358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 69 -> /* 55 */ ok(m.printHorizontally(), """
               103                                                    511                                              858                 |
       72                186           246       317           472              611           658       806                      912       |
1,13,43  90,96    155,157   188,229,232   272,273   358,377,437   490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 70 -> /* 358 */ ok(m.printHorizontally(), """
               103                                              511                                              858                 |
       72                186           246       317                      611           658       806                      912       |
1,13,43  90,96    155,157   188,229,232   272,273   377,437,490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 71 -> /* 232 */ ok(m.printHorizontally(), """
               103                                          511                                              858                 |
       72                186       246       317                      611           658       806                      912       |
1,13,43  90,96    155,157   188,229   272,273   377,437,490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 72 -> /* 229 */ ok(m.printHorizontally(), """
               103                                    511                                              858                 |
       72                    246       317                      611           658       806                      912       |
1,13,43  90,96    155,157,188   272,273   377,437,490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 73 -> /* 90 */ ok(m.printHorizontally(), """
                                                  511                                              858                 |
       72  103           246       317                      611           658       806                      912       |
1,13,43  96   155,157,188   272,273   377,437,490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 74 -> /* 155 */ ok(m.printHorizontally(), """
                                             511                                              858                 |
          103       246       317                      611           658       806                      912       |
1,13,43,96   157,188   272,273   377,437,490    564,577   612,615,657   679,681   826,830,839    884,907   922,937|
""");
        case 75 -> /* 657 */ ok(m.printHorizontally(), """
                                             511                                          858                 |
          103       246       317                      611       658       806                      912       |
1,13,43,96   157,188   272,273   377,437,490    564,577   612,615   679,681   826,830,839    884,907   922,937|
""");
        case 76 -> /* 681 */ ok(m.printHorizontally(), """
                                             511                                    858                 |
          103       246       317                              658   806                      912       |
1,13,43,96   157,188   272,273   377,437,490    564,577,612,615   679   826,830,839    884,907   922,937|
""");
        case 77 -> /* 43 */ ok(m.printHorizontally(), """
                                        511                                                       |
       103               317                              658   806           858       912       |
1,13,96   157,188,272,273   377,437,490    564,577,612,615   679   826,830,839   884,907   922,937|
""");
        case 78 -> /* 907 */ ok(m.printHorizontally(), """
                                        511                                               |
       103               317                              658               858           |
1,13,96   157,188,272,273   377,437,490    564,577,612,615   679,826,830,839   884,922,937|
""");
        case 79 -> /* 564 */ ok(m.printHorizontally(), """
                                        511                                           |
       103               317                          658               858           |
1,13,96   157,188,272,273   377,437,490    577,612,615   679,826,830,839   884,922,937|
""");
        case 80 -> /* 377 */ ok(m.printHorizontally(), """
                                    511                                           |
       103               317                      658               858           |
1,13,96   157,188,272,273   437,490    577,612,615   679,826,830,839   884,922,937|
""");
        case 81 -> /* 615 */ ok(m.printHorizontally(), """
                                    511                                       |
       103               317                  658               858           |
1,13,96   157,188,272,273   437,490    577,612   679,826,830,839   884,922,937|
""");
        case 82 -> /* 612 */ ok(m.printHorizontally(), """
                                    511                                   |
       103               317              658               858           |
1,13,96   157,188,272,273   437,490    577   679,826,830,839   884,922,937|
""");
        case 83 -> /* 157 */ ok(m.printHorizontally(), """
                                511                                   |
       103           317              658               858           |
1,13,96   188,272,273   437,490    577   679,826,830,839   884,922,937|
""");
        case 84 -> /* 922 */ ok(m.printHorizontally(), """
                                511                               |
       103           317              658               858       |
1,13,96   188,272,273   437,490    577   679,826,830,839   884,937|
""");
        case 85 -> /* 272 */ ok(m.printHorizontally(), """
                            511                               |
       103       317              658               858       |
1,13,96   188,273   437,490    577   679,826,830,839   884,937|
""");
        case 86 -> /* 490 */ ok(m.printHorizontally(), """
                      511                               |
       103                  658               858       |
1,13,96   188,273,437    577   679,826,830,839   884,937|
""");
        case 87 -> /* 679 */ ok(m.printHorizontally(), """
        103                658            858       |
1,13,96    188,273,437,577    826,830,839    884,937|
""");
        case 88 -> /* 830 */ ok(m.printHorizontally(), """
        103                658        858       |
1,13,96    188,273,437,577    826,839    884,937|
""");
        case 89 -> /* 839 */ ok(m.printHorizontally(), """
        103                658           |
1,13,96    188,273,437,577    826,884,937|
""");
        case 90 -> /* 437 */ ok(m.printHorizontally(), """
        103            658           |
1,13,96    188,273,577    826,884,937|
""");
        case 91 -> /* 826 */ ok(m.printHorizontally(), """
        103            658       |
1,13,96    188,273,577    884,937|
""");
        case 92 -> /* 577 */ ok(m.printHorizontally(), """
        103        658       |
1,13,96    188,273    884,937|
""");
        case 93 -> /* 937 */ ok(m.printHorizontally(), """
        103           |
1,13,96    188,273,884|
""");
        case 94 -> /* 884 */ ok(m.printHorizontally(), """
        103       |
1,13,96    188,273|
""");
        case 95 -> /* 13 */ ok(m.printHorizontally(), """
     103       |
1,96    188,273|
""");
        case 96 -> /* 96 */ ok(m.printHorizontally(), """
[1,188,273]
""");
        case 97 -> /* 273 */ ok(m.printHorizontally(), """
[1,188]
""");
        case 98 -> /* 1 */ ok(m.printHorizontally(), """
[188]
""");
        case 99 -> /* 188 */ ok(m.printHorizontally(), """
[]
""");
        default -> {}
       }
     }
   }

  static void test_delete_reverse()
   {final long[]r = random_array();
    var m = mjaf(4);
    for (int i = 0; i < r.length; ++i) m.put(r[i], (long)i);
    for (int i = r.length-1; i >= 0; --i)
     {ok(m.delete(r[i]), (long)i);
      if (false)                                                                // Write case statement to check deletions
       {say("        case", i, "-> /*", r[i], "*/ ok(m.printHorizontally(), \"\"\"");
        say(m.printHorizontally());
        say("\"\"\");");
       }
      //ok(m.size(), r.length - i - 1);
      switch(i)
       {case 99 -> /* 188 */ ok(m.printHorizontally(), """
                                                                                                                                                                                                                                            511                                                                                                                                                                                                                     |
                                                                                                                             317                                                                                                                                                                                        658                                                                       858                                                               |
                                   103                                                 246                                                                               403                                   472                                                                    578                                                           704                                                                       912                                   |
       27     39        72                   135               186           234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 98 -> /* 1 */ ok(m.printHorizontally(), """
                                                                                                                            317                                                                                                                                                                                        658                                                                                                                                         |
                                 103                                                 246                                                                                403                                   472                         511                                       578                                                            704                                           858                         912                                   |
     27     39        72                   135               186           234                 261           279                      344       358           391                 425               442                         501                     545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 97 -> /* 273 */ ok(m.printHorizontally(), """
                                                                                                                        317                                                                                                                                                                                        658                                                                                                                                         |
                                 103                                                 246                                                                            403                                   472                         511                                       578                                                            704                                           858                         912                                   |
     27     39        72                   135               186           234                 261       279                      344       358           391                 425               442                         501                     545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 96 -> /* 96 */ ok(m.printHorizontally(), """
                                                                                                                    317                                                                                                                                                                                        658                                                                                                                                         |
                             103                                                 246                                                                            403                                   472                         511                                       578                                                            704                                           858                         912                                   |
           39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                     545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 95 -> /* 13 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                        658                                                                                                                                         |
                          103                                                 246                                                                            403                                   472                         511                                       578                                                            704                                           858                         912                                   |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                     545       560                         611           650                          686                         806           830                         903                             961       987       |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 94 -> /* 884 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                        658                                                                                                                                   |
                          103                                                 246                                                                            403                                   472                         511                                       578                                                                                                          858                                                         |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                     545       560                         611           650                          686       704               806           830                     903           912               961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,937,946,961   976,987,989,993|
""");
        case 93 -> /* 937 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                        658                                                                                                                               |
                          103                                                 246                                                                            403                                   472                         511                                       578                                                                                                          858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                     545       560                         611           650                          686       704               806           830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 92 -> /* 577 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                    658                                                                                                                               |
                          103                                                 246                                                                            403                                                               511                                   578                                                                                                          858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425               442       472               501                     545       560                     611           650                          686       704               806           830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 91 -> /* 826 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                    658                                                                                                                           |
                          103                                                 246                                                                            403                                                               511                                   578                                                                                                      858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425               442       472               501                     545       560                     611           650                          686       704               806       830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 90 -> /* 437 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                658                                                                                                                           |
                          103                                                 246                                                                            403                                                           511                                   578                                                                                                      858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442       472               501                     545       560                     611           650                          686       704               806       830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 89 -> /* 839 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                658                                                                                                                       |
                          103                                                 246                                                                            403                                                           511                                   578                                                                                                  858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442       472               501                     545       560                     611           650                          686       704               806       830                 903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 88 -> /* 830 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                658                                                                                                                 |
                          103                                                 246                                                                            403                                                           511                                   578                                                                                            858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442       472               501                     545       560                     611           650                          686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 87 -> /* 679 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                                658                                                                                                             |
                          103                                                 246                                                                            403                                                           511                                   578                                                                                        858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442       472               501                     545       560                     611           650                      686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 86 -> /* 490 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                                                                                            658                                                                                                             |
                          103                                                 246                                                                            403                                                       511                                   578                                                                                        858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442       472           501                     545       560                     611           650                      686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 85 -> /* 272 */ ok(m.printHorizontally(), """
                                                                                                           317                                                                                                                                                                            658                                                                                                             |
                          103                                                 246                                                                      403                                                       511                                   578                                                                                        858                                                     |
        39        72                135               186           234                     279                      344       358           391                 425           442       472           501                     545       560                     611           650                      686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 84 -> /* 922 */ ok(m.printHorizontally(), """
                                                                                                           317                                                                                                                                                                            658                                                                                                         |
                          103                                                 246                                                                      403                                                       511                                   578                                                                                        858                                                 |
        39        72                135               186           234                     279                      344       358           391                 425           442       472           501                     545       560                     611           650                      686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 83 -> /* 157 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                                            658                                                                                                         |
                          103                                             246                                                                      403                                                       511                                   578                                                                                        858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442       472           501                     545       560                     611           650                      686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 82 -> /* 612 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                                        658                                                                                                         |
                          103                                             246                                                                      403                                                       511                                   578                                                                                    858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442       472           501                     545       560                     611       650                      686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611   615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 81 -> /* 615 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                                  658                                                                                                         |
                          103                                             246                                                                      403                                                       511                                   578                                                                              858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442       472           501                     545       560                         650                      686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 80 -> /* 377 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                            658                                                                                                         |
                          103                                             246                                                                403                                                       511                                                                                                                    858                                                 |
        39        72                135           186           234                     279                              358       391                 425           442       472           501                     545       560           578           650                      686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   564,576,578   586,611,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 79 -> /* 564 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                                                                                                                                   |
                          103                                             246                                                                403                                                       511                                                       658                                                     858                                                 |
        39        72                135           186           234                     279                              358       391                 425           442       472           501                     545       560       578           650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 78 -> /* 907 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                                                                                                                                                                               |
                          103                                             246                                                                403                                                       511                                                       658                                                     858                                             |
        39        72                135           186           234                     279                              358       391                 425           442       472           501                     545       560       578           650                     686       704               806                     903       912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 77 -> /* 43 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                                                                                                                                                                                               |
                       103                                             246                                                                403                                                       511                                                       658                                                     858                                             |
        39     72                135           186           234                     279                              358       391                 425           442       472           501                     545       560       578           650                     686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 76 -> /* 681 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                                                                                                                                                                                           |
                       103                                             246                                                                403                                                       511                                                       658                                                 858                                             |
        39     72                135           186           234                     279                              358       391                 425           442       472           501                     545       560       578           650                 686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560   576,578   586,611,650   657,658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 75 -> /* 657 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                                                                                                                                                                                     |
                       103                                             246                                                                403                                                       511                                                 658                                                 858                                             |
        39     72                135           186           234                     279                              358       391                 425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 74 -> /* 155 */ ok(m.printHorizontally(), """
                                                                                                317                                                                                                                                                                                                                                                     |
                       103                                         246                                                                403                                                       511                                                 658                                                 858                                             |
        39     72                135       186           234                     279                              358       391                 425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 73 -> /* 90 */ ok(m.printHorizontally(), """
                                                                                            317                                                                                                                                                                                                                                                     |
                   103                                         246                                                                403                                                       511                                                 658                                                 858                                             |
        39                   135       186           234                     279                              358       391                 425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135   151,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 72 -> /* 229 */ ok(m.printHorizontally(), """
                                                                                      317                                                                                                                                                                                                                                                     |
                   103                                   246                                                                403                                                       511                                                 658                                                 858                                             |
        39                           186       234                     279                              358       391                 425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 71 -> /* 232 */ ok(m.printHorizontally(), """
                                                                                317                                                                                                                                                                                                                                                     |
                                                   246                                                                403                                                       511                                                 658                                                 858                                             |
        39         103               186                         279                              358       391                 425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 70 -> /* 358 */ ok(m.printHorizontally(), """
                                                                                317                                                                                                                                                                                                                                               |
                                                   246                                                          403                                                       511                                                 658                                                 858                                             |
        39         103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 69 -> /* 55 */ ok(m.printHorizontally(), """
                                                                             317                                                                                                                                                                                                                                               |
                                                246                                                          403                                                       511                                                 658                                                 858                                             |
        39      103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                     903       912       961               |
27,29,39  72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 68 -> /* 961 */ ok(m.printHorizontally(), """
                                                                             317                                                                                                                                                                                                                                         |
                                                246                                                          403                                                       511                                                 658                                                 858                                       |
        39      103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                             912   961               |
27,29,39  72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912   946   976,987,989,993|
""");
        case 67 -> /* 103 */ ok(m.printHorizontally(), """
                                                                         317                                                                                                                                                                                                                                         |
                                            246                                                          403                                                       511                                                 658                                                 858                                       |
        39  103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                             912   961               |
27,29,39  72   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912   946   976,987,989,993|
""");
        case 66 -> /* 946 */ ok(m.printHorizontally(), """
                                                                         317                                                                                                                                                                                                                                      |
                                            246                                                          403                                                       511                                                 658                                                 858                                    |
        39  103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                             912961               |
27,29,39  72   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 65 -> /* 72 */ ok(m.printHorizontally(), """
                                                                     317                                                                                                                                                                                                                                      |
                                        246                                                          403                                                       511                                                 658                                                 858                                    |
        103               186                         279                          358                         425           442       472           501                     545               578           650             686       704               806                             912961               |
27,29,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 64 -> /* 376 */ ok(m.printHorizontally(), """
                                                                     317                                                                                                                                                                                                                                  |
                                        246                                                      403                                                       511                                                 658                                                 858                                    |
        103               186                         279                          358                     425           442       472           501                     545               578           650             686       704               806                             912961               |
27,29,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 63 -> /* 29 */ ok(m.printHorizontally(), """
                                                                  317                                                                                                                                                                                                                                  |
                                                                                              403                                                       511                                                 658                                                 858                                    |
     103               186           246           279                          358                     425           442       472           501                     545               578           650             686       704               806                             912961               |
27,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   516,526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 62 -> /* 516 */ ok(m.printHorizontally(), """
                                                                  317                                                                                                                                                                                                                            |
                                                                                              403                                                       511                                           658                                                 858                                    |
     103               186           246           279                          358                     425           442       472           501                 545               578                         686       704               806                             912961               |
27,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 61 -> /* 237 */ ok(m.printHorizontally(), """
                                                              317                                                                                                                                                                                                                            |
                                                                                          403                                                       511                                           658                                                 858                                    |
     103               186       246           279                          358                     425           442       472           501                 545               578                         686       704               806                             912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 60 -> /* 438 */ ok(m.printHorizontally(), """
                                                              317                                                                                                                                                                                                                        |
                                                                                          403                                                   511                                           658                                                 858                                    |
     103               186       246           279                          358                     425       442       472           501                 545               578                         686       704               806                             912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 59 -> /* 809 */ ok(m.printHorizontally(), """
                                                              317                                                                                                                                                                                                                  |
                                                                                          403                                                   511                                           658                                           858                                    |
     103               186       246           279                          358                     425       442       472           501                 545               578                                 704               806                         912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   769,773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 58 -> /* 260 */ ok(m.printHorizontally(), """
                                                          317                                                                                                                                                                                                                  |
                                                                                      403                                                   511                                           658                                           858                                    |
     103               186       246       279                          358                     425       442       472           501                 545               578                                 704               806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   769,773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 57 -> /* 769 */ ok(m.printHorizontally(), """
                                                          317                                                                                                                                                                                                              |
                                                                                      403                                                   511                                           658                                       858                                    |
     103               186       246       279                          358                     425       442       472           501                 545               578                                 704           806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 56 -> /* 773 */ ok(m.printHorizontally(), """
                                                          317                                                                                                                                                                                                          |
                                                                                      403                                                   511                                           658                                   858                                    |
     103               186       246       279                          358                     425       442       472           501                 545               578                                 704       806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 55 -> /* 151 */ ok(m.printHorizontally(), """
                                                    317                                                                                                                                                                                                          |
                                                                                403                                                   511                                           658                                   858                                    |
     103           186               279                          358                     425       442       472           501                 545               578                                 704       806                         912961               |
27,39   106,135,186   234,246,261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 54 -> /* 288 */ ok(m.printHorizontally(), """
                                                317                                                                                                                                                                                                          |
                                                                            403                                                   511                                           658                                   858                                    |
     103           186               279                      358                     425       442       472           501                 545               578                                 704       806                         912961               |
27,39   106,135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 53 -> /* 480 */ ok(m.printHorizontally(), """
                                                317                                                                                                                                                                                                    |
                                                                            403                                             511                                           658                                   858                                    |
     103           186               279                      358                             442       472       501                 545               578                                 704       806                         912961               |
27,39   106,135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472   494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 52 -> /* 690 */ ok(m.printHorizontally(), """
                                                317                                                                                                                                                                                              |
                                                                            403                                             511                                           658                             858                                    |
     103           186               279                      358                             442       472       501                 545               578                             704                                 912961               |
27,39   106,135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472   494,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 51 -> /* 494 */ ok(m.printHorizontally(), """
                                                317                                                                                                                                                                                        |
                                                                            403                                       511                                                                           858                                    |
     103           186               279                      358                             442           501                 545               578               658           704                                 912961               |
27,39   106,135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511   526,545   554,560,576,578   586,611,650,658   667,686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 50 -> /* 667 */ ok(m.printHorizontally(), """
                                                317                                                                    511                                                                        858                                    |
     103           186               279                      358           403               442           501                  545               578               658       704                                  912961               |
27,39   106,135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   804,806,854,858    882,903,906,912      976,987,989,993|
""");
        case 49 -> /* 106 */ ok(m.printHorizontally(), """
                                            317                                                                    511                                                                        858                                    |
     103       186               279                      358           403               442           501                  545               578               658       704                                  912961               |
27,39   135,186   234,246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   804,806,854,858    882,903,906,912      976,987,989,993|
""");
        case 48 -> /* 234 */ ok(m.printHorizontally(), """
                                      317                                                                    511                                                                        858                                    |
             186           279                      358           403               442           501                  545               578               658       704                                  912961               |
27,39,135,186   246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   804,806,854,858    882,903,906,912      976,987,989,993|
""");
        case 47 -> /* 186 */ ok(m.printHorizontally(), """
                                  317                                                                    511                                                                        858                                    |
         186           279                      358           403               442           501                  545               578               658       704                                  912961               |
27,39,135   246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   804,806,854,858    882,903,906,912      976,987,989,993|
""");
        case 46 -> /* 976 */ ok(m.printHorizontally(), """
                                  317                                                                    511                                                                        858                             |
         186           279                      358           403               442           501                  545               578               658       704                                  961           |
27,39,135   246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   804,806,854,858    882,903,906,912   987,989,993|
""");
        case 45 -> /* 804 */ ok(m.printHorizontally(), """
                                  317                                                                    511                                                                    858                             |
         186           279                      358           403               442           501                  545               578               658       704                              961           |
27,39,135   246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 44 -> /* 39 */ ok(m.printHorizontally(), """
                               317                                                                    511                                                                    858                             |
      186           279                      358           403               442           501                  545               578               658       704                              961           |
27,135   246,261,279   298,317    338,344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 43 -> /* 338 */ ok(m.printHorizontally(), """
                               317                                                                511                                                                    858                             |
      186           279                  358           403               442           501                  545               578               658       704                              961           |
27,135   246,261,279   298,317    344,354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 42 -> /* 344 */ ok(m.printHorizontally(), """
                               317                                                            511                                                                    858                             |
      186           279              358           403               442           501                  545               578               658       704                              961           |
27,135   246,261,279   298,317    354   391,401,403   422,425,436,442   447,472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 41 -> /* 447 */ ok(m.printHorizontally(), """
                               317                                                      511                                                                    858                             |
      186           279                          403               442       501                  545               578               658       704                              961           |
27,135   246,261,279   298,317    354,391,401,403   422,425,436,442   472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 40 -> /* 135 */ ok(m.printHorizontally(), """
                           317                                                      511                                                                    858                             |
  186           279                          403               442       501                  545               578               658       704                              961           |
27   246,261,279   298,317    354,391,401,403   422,425,436,442   472,501   503,511    526,545   554,560,576,578   586,611,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 39 -> /* 611 */ ok(m.printHorizontally(), """
                           317                                                      511                                                                858                             |
  186           279                          403               442       501                  545               578           658       704                              961           |
27   246,261,279   298,317    354,391,401,403   422,425,436,442   472,501   503,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 38 -> /* 503 */ ok(m.printHorizontally(), """
                           317                                                511                                                                858                             |
  186           279                          403               442                      545               578           658       704                              961           |
27   246,261,279   298,317    354,391,401,403   422,425,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906,912   987,989,993|
""");
        case 37 -> /* 912 */ ok(m.printHorizontally(), """
                           317                                                511                                                                858                         |
  186           279                          403               442                      545               578           658       704                          961           |
27   246,261,279   298,317    354,391,401,403   422,425,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906   987,989,993|
""");
        case 36 -> /* 425 */ ok(m.printHorizontally(), """
                           317                                            511                                                                858                         |
  186           279                          403           442                      545               578           658       704                          961           |
27   246,261,279   298,317    354,391,401,403   422,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906   987,989,993|
""");
        case 35 -> /* 298 */ ok(m.printHorizontally(), """
                     317                                            511                                                                858                         |
              279                      403           442                      545               578           658       704                          961           |
27,246,261,279   317    354,391,401,403   422,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906   987,989,993|
""");
        case 34 -> /* 422 */ ok(m.printHorizontally(), """
                                                               511                                                                858                         |
              279   317               403       442                      545               578           658       704                          961           |
27,246,261,279   317   354,391,401,403   436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858    882,903,906   987,989,993|
""");
        case 33 -> /* 554 */ ok(m.printHorizontally(), """
                                                               511                                                            858                         |
              279   317               403       442                      545           578           658       704                          961           |
27,246,261,279   317   354,391,401,403   436,442   472,501,511    526,545   560,576,578   586,650,658   686,704   806,854,858    882,903,906   987,989,993|
""");
        case 32 -> /* 806 */ ok(m.printHorizontally(), """
                                                               511                                                        858                         |
              279   317               403       442                      545           578           658       704                      961           |
27,246,261,279   317   354,391,401,403   436,442   472,501,511    526,545   560,576,578   586,650,658   686,704   854,858    882,903,906   987,989,993|
""");
        case 31 -> /* 560 */ ok(m.printHorizontally(), """
                                                               511                                                  858                         |
              279   317               403       442                      545       578           658                              961           |
27,246,261,279   317   354,391,401,403   436,442   472,501,511    526,545   576,578   586,650,658   686,704,854,858    882,903,906   987,989,993|
""");
        case 30 -> /* 436 */ ok(m.printHorizontally(), """
                                                           511                                                  858                         |
              279   317               403   442                      545       578           658                              961           |
27,246,261,279   317   354,391,401,403   442   472,501,511    526,545   576,578   586,650,658   686,704,854,858    882,903,906   987,989,993|
""");
        case 29 -> /* 576 */ ok(m.printHorizontally(), """
                                                           511                                            858                         |
              279   317               403   442                          578           658                              961           |
27,246,261,279   317   354,391,401,403   442   472,501,511    526,545,578   586,650,658   686,704,854,858    882,903,906   987,989,993|
""");
        case 28 -> /* 650 */ ok(m.printHorizontally(), """
                                                           511                                                                   |
              279   317               403   442                          578       658               858           961           |
27,246,261,279   317   354,391,401,403   442   472,501,511    526,545,578   586,658   686,704,854,858   882,903,906   987,989,993|
""");
        case 27 -> /* 989 */ ok(m.printHorizontally(), """
                                                           511                                                               |
              279   317               403   442                          578       658               858           961       |
27,246,261,279   317   354,391,401,403   442   472,501,511    526,545,578   586,658   686,704,854,858   882,903,906   987,993|
""");
        case 26 -> /* 401 */ ok(m.printHorizontally(), """
                                                     511                                                               |
              279   317           403                              578       658               858           961       |
27,246,261,279   317   354,391,403   442,472,501,511    526,545,578   586,658   686,704,854,858   882,903,906   987,993|
""");
        case 25 -> /* 403 */ ok(m.printHorizontally(), """
                                               511                                                               |
              279           403                              578       658               858           961       |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658   686,704,854,858   882,903,906   987,993|
""");
        case 24 -> /* 987 */ ok(m.printHorizontally(), """
                                               511                                                           |
              279           403                              578       658               858           961   |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658   686,704,854,858   882,903,906   993|
""");
        case 23 -> /* 686 */ ok(m.printHorizontally(), """
                                               511                                                     |
              279           403                              578       658           858               |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658   704,854,858   882,903,906,993|
""");
        case 22 -> /* 526 */ ok(m.printHorizontally(), """
                                               511                                                 |
              279           403                          578       658           858               |
27,246,261,279   317,354,391   442,472,501,511    545,578   586,658   704,854,858   882,903,906,993|
""");
        case 21 -> /* 279 */ ok(m.printHorizontally(), """
                                           511                                                 |
          279           403                          578       658           858               |
27,246,261   317,354,391   442,472,501,511    545,578   586,658   704,854,858   882,903,906,993|
""");
        case 20 -> /* 854 */ ok(m.printHorizontally(), """
                                           511                                           |
          279           403                                  658       858               |
27,246,261   317,354,391   442,472,501,511    545,578,586,658   704,858   882,903,906,993|
""");
        case 19 -> /* 903 */ ok(m.printHorizontally(), """
                                           511                                       |
          279           403                                  658       858           |
27,246,261   317,354,391   442,472,501,511    545,578,586,658   704,858   882,906,993|
""");
        case 18 -> /* 354 */ ok(m.printHorizontally(), """
                                       511                                       |
          279       403                                  658       858           |
27,246,261   317,391   442,472,501,511    545,578,586,658   704,858   882,906,993|
""");
        case 17 -> /* 501 */ ok(m.printHorizontally(), """
                                   511                                       |
          279       403                              658       858           |
27,246,261   317,391   442,472,511    545,578,586,658   704,858   882,906,993|
""");
        case 16 -> /* 261 */ ok(m.printHorizontally(), """
                               511                                       |
      279       403                              658       858           |
27,246   317,391   442,472,511    545,578,586,658   704,858   882,906,993|
""");
        case 15 -> /* 246 */ ok(m.printHorizontally(), """
                         511                                       |
          403                              658       858           |
27,317,391   442,472,511    545,578,586,658   704,858   882,906,993|
""");
        case 14 -> /* 882 */ ok(m.printHorizontally(), """
           403            511                658        858       |
27,317,391    442,472,511    545,578,586,658    704,858    906,993|
""");
        case 13 -> /* 704 */ ok(m.printHorizontally(), """
           403            511                658           |
27,317,391    442,472,511    545,578,586,658    858,906,993|
""");
        case 12 -> /* 658 */ ok(m.printHorizontally(), """
           403            511            658           |
27,317,391    442,472,511    545,578,586    858,906,993|
""");
        case 11 -> /* 906 */ ok(m.printHorizontally(), """
           403            511            658       |
27,317,391    442,472,511    545,578,586    858,993|
""");
        case 10 -> /* 472 */ ok(m.printHorizontally(), """
           403        511            658       |
27,317,391    442,511    545,578,586    858,993|
""");
        case 9 -> /* 586 */ ok(m.printHorizontally(), """
           403        511        658       |
27,317,391    442,511    545,578    858,993|
""");
        case 8 -> /* 858 */ ok(m.printHorizontally(), """
           403                658   |
27,317,391    442,511,545,578    993|
""");
        case 7 -> /* 993 */ ok(m.printHorizontally(), """
           403                658|
27,317,391    442,511,545,578    |
""");
        case 6 -> /* 391 */ ok(m.printHorizontally(), """
       403               |
27,317    442,511,545,578|
""");
        case 5 -> /* 578 */ ok(m.printHorizontally(), """
       403           |
27,317    442,511,545|
""");
        case 4 -> /* 511 */ ok(m.printHorizontally(), """
       403       |
27,317    442,545|
""");
        case 3 -> /* 317 */ ok(m.printHorizontally(), """
[27,442,545]
""");
        case 2 -> /* 545 */ ok(m.printHorizontally(), """
[27,442]
""");
        case 1 -> /* 442 */ ok(m.printHorizontally(), """
[27]
""");
        case 0 -> /* 27 */ ok(m.printHorizontally(), """
[]
""");
       }
     }
   }

  static void test_delete_reverse_squeeze()
   {final long[]r = random_array();
    var m = mjaf(4);
    for (int i = 0; i < r.length; ++i) m.put(r[i], (long)i);
    m.delete(690l); say(m.printHorizontally());
    m.delete(704l); say(m.printHorizontally());
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
    test_delete();
    test_delete_reverse();
    //test_delete_reverse_squeeze();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    //test_delete();
    //test_delete_reverse();
    //test_delete_reverse_squeeze();
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

// 690,704
