//------------------------------------------------------------------------------
// Btree with data only in leaves
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout digital a binary tree on a silicon chip.

import java.util.*;

class Mjaf<Type extends Comparable<Type>> extends Chip                          // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission where there is no data to transfer, whereas the leaves have even number of keys because data is not transferred to the parent on fission.
 {final static boolean github_actions =                                         // Whether we are on a github
    "true".equals(System.getenv("GITHUB_ACTIONS"));
  final static long             start = System.nanoTime();                      // Start time

//D1 Construction                                                               // Create a Btee from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  final int maxKeysPerLeaf;                                                     // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches.

  Node<Type> root;                                                              // The root node of the Btree
  int keyDataStored;                                                            // The number of key, data values stored in the Btree

  Mjaf(int MaxKeysPerLeaf)                                                      // Define a Btree with a specified maximum number of keys per leaf.
   {final int N = MaxKeysPerLeaf;
    if (N % 2 == 1) stop("Number of keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("Number of keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf = N;
   }

  Mjaf() {this(2);}                                                             // Define a Btree with a minimal maximum number of keys per node

  static Mjaf<Long> mjaf()                   {return new Mjaf<Long>();}                // Create a new Btree of default type
  static Mjaf<Long> mjaf(int MaxKeysPerLeaf) {return new Mjaf<Long>(MaxKeysPerLeaf);}  // Define a new Btree of default type with a specified maximum number of keys per node

  int nodesCreated = 0;                                                         // Count number of nodes created

  class Node<Type extends Comparable<Type>>                                     // A branch or a leaf: an interior or exterior node.
   {final Stack<Type> keyNames;                                                 // Names of the keys in this branch or leaf
    final int nodeNumber = ++nodesCreated;                                      // Number of this node
    Node() {keyNames = new Stack<Type>();}                                      // Create a node

    int findIndexOfKey(Type keyToFind) {return keyNames.indexOf(keyToFind)+1;}  // Find the 1 based index of a key in a branch node or zero if not found
    Type splitKey     () {return keyNames.elementAt(maxKeysPerLeaf/2);}         // Splitting key
    int size() {return keyNames.size();}                                        // Number of elements in this leaf
    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check node is as expected

    int findFirstGreater(Type keyName)                                          // Find first key which is greater an the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {final int N = size();                                                     // Number of keys currently in node
      for (int i = 0; i < N; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (kn.compareTo(keyName) > 0) return i + 1;                            // Current key is greater than the search key
       }
      return 0;
     }
   }

  class Branch extends Node<Type>                                               // A branch node directs the search to the appropriate leaf
   {final Stack<Node<Type>> nextLevel;
    Node<Type> topNode;

    Branch(Node<Type> Top) {nextLevel = new Stack<Node<Type>>(); topNode = Top;}// Create a new branch
    boolean branchIsFull() {return size() >= maxKeysPerLeaf-1;}                 // Node should be split

    void putBranch(Type keyName, Node<Type> putNode)                            // Insert a new leaf value
     {final int N = nextLevel.size();                                           // Number of keys currently in node
      if (N >= maxKeysPerLeaf) stop("Too many keys in Branch");
      for (int i = 0; i < N; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (keyName.compareTo(kn) < 0)                                          // Insert new key in order
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

    void removeTwig(int Index)                                                  // Remove the indicate key,node pair from a branch
     {final int K = keyNames.size(), i = Index;                                 // Number of keys currently in node
      if (K < i) stop("Index", i, "too big for branch of size:", K);
      if (K < 0) stop("Index", i, "too small");
      keyNames .removeElementAt(i);
      nextLevel.removeElementAt(i);
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

    Branch splitBranchInHalf() {return splitBranch(maxKeysPerLeaf/2);}            // Split a branch in half

    void joinBranch(Branch Join, Type joinKeyName)                              // Append the second branch to the first one adding the specified key
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (J + K >= maxKeysPerLeaf) stop("Join of two many keys", K, "+1+", J, "greater than", maxKeysPerLeaf);
      if (J == 0) stop("Leaf being joined is empty");                           // Nothing to join
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

    void ok(String expected)                                                    // Check leaf
     {Mjaf.ok(toString(), expected);
     }
   } // Branch

  Branch branch(Node<Type> node) {return new Branch(node);}                     // Create an empty leaf

  class Leaf extends Node<Type>                                                 // A leaf node stores the data associated with the keys in the Btree
   {final Stack<Type> dataValues;
    Leaf() {dataValues = new Stack<Type>();}                                    // Create a new leaf
    boolean leafIsFull() {return size() >= maxKeysPerLeaf;}                     // Leaf is full

    void putLeaf(Type keyName, Type dataValue)                                  // Insert a new leaf value
     {final int K = keyNames.size();                                            // Number of keys currently in node
      if (K >= maxKeysPerLeaf) stop("Too many keys in leaf");
      for (int i = 0; i < K; i++)
       {final Type kn = keyNames.elementAt(i);                                  // Key we are checking
        if (keyName.compareTo(kn) < 0)                                          // Insert new key in order
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

    void removeLeafKeyData(int Index)                                           // Remove the indicate key data pair from a leaf
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
      if (J + K > maxKeysPerLeaf) stop("Join of two many keys", K, "+", J, "greater than", maxKeysPerLeaf);
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
   }

  Leaf leaf() {return new Leaf();}                                              // Create an empty leaf

//D1 Search                                                                     // Find a key, data pair

  Type find(Type keyName)                                                       // Find a the data associated with a key
   {if (root == null) return null;                                              // Empty tree
    for(Node<Type> n = root; n != null;)                                        // Step down through tree
     {final int g = n.findFirstGreater(keyName);
      if (n instanceof Branch)                                                  // Stepped to a branch
       {final Branch b = (Branch)n;
        n = g == 0 ? b.topNode : b.nextLevel.elementAt(g-1);
       }
      else                                                                      // Stepped to a leaf
       {final int f = n.findIndexOfKey(keyName);
        if (f == 0) return null;
        final Leaf l = (Leaf)n;
        return l.dataValues.elementAt(f-1);
       }
     }
    return null;                                                                // Key not founmd
   }

//D1 Insertion                                                                  // Insert keys and data into the Btree

  void put(Type keyName, Type dataValue)                                        // Insert a new key, data pair into the Btree                            // Insert a new key, data pair
   {if (count++ > 10) stop("Stopped");

    if (root == null)
     {final Leaf l = new Leaf();
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
        b.putBranch(r.keyNames.firstElement(), l);                              // Insert left hand node all of whiose elements are less than the first element of what was the root
        root = b;
       }
     }

    else if (root instanceof Branch)                                            // Split full root which is a branch not a leaf
     {final Branch r = (Branch)root;
      if (r.branchIsFull())
       {final Branch l = r.splitBranchInHalf();
        final Branch b = branch(r);
        b.putBranch(r.keyNames.firstElement(), l);
        root = b;
       }
     }

    Branch p = (Branch)root;
    Node<Type> q = p;

    for(int i = 0; i < 9; ++i)                                                  // Step down through tree to find the required leaf, splitting as we go
     {if (q instanceof Branch)                                                  // Stepped to a branch
       {final Branch qb = (Branch)q;
        if (qb.branchIsFull())                                                  // Split the branch because it is full and we might need to insert below it requiring a slot in this node
         {final Type   k = qb.splitKey();
          final Branch l = qb.splitBranchInHalf();
          p.putBranch(k, l);
          continue;
         }
        final int g = q.findFirstGreater(keyName);
        p = qb; q = g == 0 ? qb.topNode : qb.nextLevel.elementAt(g-1);
        continue;                                                               // continue to step down
       }

      final Leaf l = (Leaf)q;
      final int  g = l.findIndexOfKey(keyName);
      if (g != 0)                                                               // Key already present in leaf
       {l.dataValues.set(g-1, dataValue);
        return;                                                                 // Data replaced at key
       }

      if (l.leafIsFull())                                                       // Split the node because it is full and we might need to insert below it requiring a slot in this node
       {final Type k = l.splitKey();
        final Leaf e = l.splitLeafInHalf();
        p.putBranch(k, e);
        if (keyName.compareTo(k) < 0) e.putLeaf(keyName, dataValue);            // Insert key in the appropriate split leaf
        else                          l.putLeaf(keyName, dataValue);
        return;
       }
      l.putLeaf(keyName, dataValue);                                            // On a leaf that is not full so we can insert directly
      return;                                                                   // Key, data pair replaced
     }
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

    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 3:6, 4:8, 5:10)"); ok(m.keyDataStored, 4);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 4:8, 5:10)");      ok(m.keyDataStored, 3);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1, 5:10)");           ok(m.keyDataStored, 2);
    l.removeLeafKeyData(1); l.ok("Leaf(1:1)");                 ok(m.keyDataStored, 1);
    l.removeLeafKeyData(0); l.ok("Leaf()");                    ok(m.keyDataStored, 0);
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
    k.ok("Leaf(1:1, 2:4,  3:6)");
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
    B.putBranch(1, a);
    B.putBranch(2, b);
    B.putBranch(3, c);
    B.putBranch(4, d);
    B.putBranch(5, e);
    B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");

    ok(B.findIndexOfKey(3), 3);
    ok(B.splitKey(), 4);
    ok(B.branchIsFull(), false);
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
    ok(B.splitKey(), 4);
    ok(B.branchIsFull(), true);
    ok(B.size(), 6);
    Mjaf<Integer>.Branch C = B.splitBranchInHalf();
    B.ok("Branch(1:1, 2:2, 3:3, 4)");
    C.ok("Branch(5:5, 6:6, 7)");
   }

  static void test_insert()
   {final int N = 8;
    var m = mjaf(4);
    for (long i = 0; i < N; i++) m.put(i, i<<1);
   }

  static void test_find()
   {var m = mjaf(2);
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_create();
    test_leaf_split();
    test_leaf_split_in_half();
    test_branch();
    test_branch_full();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_insert();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {if (args.length > 0 && args[0].equals("compile")) System.exit(0);           // Do a syntax check
    try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(traceBack(e));
     }
   }
 }
