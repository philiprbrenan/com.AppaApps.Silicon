//------------------------------------------------------------------------------
// Btree with data stored only in the leaves to simplify deletion.
// Philip R Brenan at appaapps dot com, Appa Apps Ltd Inc., 2024
//------------------------------------------------------------------------------
package com.AppaApps.Silicon;                                                   // Design, simulate and layout  a binary tree on a silicon chip.
//                 Shall I compare thee to a summer's day?
//                 Thou art more lovely and more temperate:
//                 Rough winds do shake the darling buds of May,
//                 And summer's lease hath all too short a date;

//                 Sometime too hot the eye of heaven shines,
//                 And often is his gold complexion dimm'd;
//                 For every fair from fair sometime declines,
//                 By chance or nature's changing course untrimm'd;

//                 But now thou art for ever fairly made,
//                 The eye of heaven lights thy face for me,
//                 Nor shall death brag thou wander'st in his shade,
//                 When these lines being read bring life to thee!
import java.util.Stack;                                                         // Used only for printing trees which is not something that will happen on the chip

class Mjaf2 extends Memory.Structure                                             // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission, whereas the leaves (exterior nodes) have even number of keys and matching number of data elements because data is not transferred to the parent on fission  which simplifies deletions with complicating insertions.
 {final int bitsPerKey;                                                         // Number of bits in key
  final int bitsPerData;                                                        // Number of bits in data
  final int maxKeysPerLeaf;                                                     // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches as whether classic Btree algorithm.
  final int maxKeysPerBranch;                                                   // The maximum number of keys per branch.
  final int maxNodes;                                                           // The maximum number of nodes in the tree

  final Memory.Variable  nodesCreated;                                          // Number of nodes created
  final Memory.Variable  keyDataStored;                                         // Current number of key/data pairs currently stored in tree
  final Memory.Variable  root;                                                  // Root
  final Stuck            nodesFree;                                             // Free nodes
  final Stuck            branchKeyNames;                                        // Branch key names
  final Stuck            leafKeyNames;                                          // Leaf key names
  final Stuck            dataValues;                                            // Data values
  final Stuck            nextValue;                                             // Next values
  final Memory.Variable  topNode;                                               // Next node if search key is greater than all keys in this node
  final Memory.Structure branch;                                                // Branch of the tree
  final Memory.Structure leaf;                                                  // Leaf of the tree
  final Memory.Union     branchOrLeaf;                                          // Branch or leaf of the tree
  final Memory.Variable  isLeaf;                                                // Whether the node is a leaf or a branch
  final Memory.Structure node;                                                  // Node of the tree
  final Memory.Array     nodes;                                                 // Array of nodes comprising tree

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  Mjaf2(int BitsPerKey, int BitsPerData, int MaxKeysPerLeaf, int size)          // Define a Btree with a specified maximum number of keys per leaf.
   {super("tree");
    final int N = MaxKeysPerLeaf;
    bitsPerKey  = BitsPerKey;
    bitsPerData = BitsPerData;
    if (N % 2 == 1) stop("# keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("# keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf   = N;
    maxKeysPerBranch = N-1;
    maxNodes = size;

    nodesCreated   = Memory.variable ("nodesCreated", 2*BitsPerKey+1);          // Number of nodes created
    keyDataStored  = Memory.variable ("keyDataStored",  BitsPerKey+1);          // Field to track number of keys stored in twos complement form hence an extra bit for the sign
    root           = Memory.variable ("root",           BitsPerKey+1);          // Root
    nodesFree      = new Stuck("nodesFree",      size,           1+BitsPerKey); // Free nodes
    branchKeyNames = new Stuck("branchKeyNames", maxKeysPerBranch, BitsPerKey); // Branch key names
    leafKeyNames   = new Stuck("leafKeyNames",   maxKeysPerLeaf,   BitsPerKey); // Leaf key names
    dataValues     = new Stuck("dataValues",     maxKeysPerLeaf,   BitsPerData);// Data values
    nextValue      = new Stuck("nextValue",      maxKeysPerBranch, BitsPerKey); // Next values
    topNode        = Memory.variable ("topNode",        BitsPerKey+1);          // Next node if search key is greater than all keys in this node
    branch         = Memory.structure("branch", branchKeyNames, nextValue, topNode); // Branch of the tree
    leaf           = Memory.structure("leaf",   leafKeyNames,   dataValues);    // Leaf of the tree
    branchOrLeaf   = Memory.union    ("branchOrLeaf",   branch, leaf);          // Branch or leaf of the tree
    isLeaf         = Memory.variable ("isLeaf",         1);                     // Whether the node is a leaf or a branch
    node           = Memory.structure("node",  isLeaf,  branchOrLeaf);          // Node of the tree
    nodes          = Memory.array    ("nodes", node,    size);                  // Array of nodes comprising tree
    addField(nodesFree);
    addField(nodesCreated);
    addField(keyDataStored);
    addField(root);
    addField(nodes);
    layout();                                                                   // Layout

    zero();                                                                     // Clear all of memory
    root.setNull();                                                             // Empty tree
    for (int i = 0; i < size; i++) nodesFree.push(i);                           // All nodes are originally free
   }

  static Mjaf2 mjaf(int Key, int Data, int MaxKeysPerLeaf, int size)            // Define a Btree with a specified maximum number of keys per leaf.
   {return new Mjaf2(Key, Data, MaxKeysPerLeaf, size);
   }

  int size() {return keyDataStored.toInt();}                                    // Number of entries in the tree

  boolean emptyTree() {return root.isNull();}                                   // Test for an empty tree

  class Key extends Memory                                                      // Memory for a key
   {Key() {super(bitsPerKey);}
    Key(Memory memory)
     {super(); bits = memory.bits; at = memory.at; width = memory.width;
     }
    Key(int n)
     {super(memoryFromInt(bitsPerKey, n));
     }
   }

  class Data extends Memory                                                     // Memory for a data value
   {Data() {super(bitsPerData);}
    Data(Memory memory)
     {super(); bits = memory.bits; at = memory.at; width = memory.width;
     }
    Data(int n)
     {super(memoryFromInt(bitsPerData, n));
     }
   }

//D1 Leaf                                                                       // Methods applicable to a leaf

  class Leaf                                                                    // Describe a leaf
   {final int index;

    Leaf()                                                                      // Create a leaf
     {index = nodesFree.pop().toInt();                                          // Index of next free node
      clear();                                                                  // Clear the leaf
      nodes.setIndex(index); isLeaf.set(1);                                     // Mark as a leaf
     }

    Leaf(int Index) {index = Index;}                                            // Address a leaf by index

    void clear() {nodes.setIndex(index); node.zero();}                          // Clear a leaf

    boolean isEmpty()                                                           // Leaf is empty
     {nodes.setIndex(index);
      return leafKeyNames.isEmpty();
     }

    boolean isFull()                                                            // Leaf is full
     {nodes.setIndex(index);
      return leafKeyNames.isFull();
     }

    int nKeys() {nodes.setIndex(index); return leafKeyNames.stuckSize();}       // Number of keys in a leaf
    int nData() {nodes.setIndex(index); return dataValues  .stuckSize();}       // Number of data values in a leaf

    void pushKey(Key memory)                                                    // Push a key into a leaf
     {nodes.setIndex(index);
      leafKeyNames.push(memory);
     }

    void pushData(Memory memory)                                                // Push a data value into a leaf
     {nodes.setIndex(index);
      dataValues.push(memory);
     }

    Key shiftKey()                                                             // Push a key into a leaf
     {nodes.setIndex(index);
      return new Key(leafKeyNames.shift());
     }

    Data shiftData()                                                            // Push a data value into a leaf
     {nodes.setIndex(index);
      return new Data(dataValues.shift());
     }

    void insertKey(Key key, int i)                                              // Push a key into a leaf
     {nodes.setIndex(index);
      leafKeyNames.insertElementAt(key, i);
     }

    void insertData(Data data, int i)                                           // Push a data value into a leaf
     {nodes.setIndex(index);
      dataValues  .insertElementAt(data, i);
     }

    Key getKey(int i)                                                           // Get the indexed key
     {nodes.setIndex(index);
      return new Key(leafKeyNames.elementAt(i));
     }

    Data getData(int i)                                                         // Get the indexed data value
     {nodes.setIndex(index);
      return new Data(dataValues.elementAt(i));
     }

    int getKeyAsInt(int i)                                                      // Get the indexed key as an integer
     {nodes.setIndex(index);
      return leafKeyNames.elementAt(i).toInt();
     }

    int getDataAsInt(int i)                                                     // Get the indexed data value as an integer
     {nodes.setIndex(index);
      return dataValues.elementAt(i).toInt();
     }

    Memory memory()                                                             // Get memory associated with a leaf
     {nodes.setIndex(index);
      return leaf.memory();
     }

    void put(Key keyName, Data dataValue)                                       // Put a key / data value pair into a leaf
     {nodes.setIndex(index);
      final int K = leafKeyNames.stuckSize();                                   // Number of keys currently in node
      if (K >= maxKeysPerLeaf) stop("Too many keys in leaf");

      for (int i = 0; i < K; i++)                                               // Search existing keys for a greater key
       {final Memory k = leafKeyNames.elementAt(i);                             // Current key
        if (keyName.lessThanOrEqual(k))                                         // Insert new key in order
         {insertKey (keyName,   i);                                             // Insert key
          insertData(dataValue, i);                                             // Insert data
          keyDataStored.inc();                                                  // Created a new entry in the leaf
          return;
         }
       }
      pushKey (keyName);                                                        // Either the leaf is empty or the new key is greater than every existing key
      pushData(dataValue);
      keyDataStored.inc();                                                      // Created a new entry in the leaf
     }

    Leaf split()                                                                // Split the leaf into two leafs - the new leaf consists of the indicated first elements, the old leaf retains the rest
     {nodes.setIndex(index);
      final int K = leafKeyNames.stuckSize(), f = maxKeysPerLeaf/2;             // Number of keys currently in node
      if (f < K) {} else stop("Split", f, "too big for leaf of size:", K);
      if (f < 1)         stop("First", f, "too small");
      final Leaf l = new Leaf();                                                // New leaf
      for (int i = 0; i < f; i++)                                               // Transfer keys and data
       {final Key    k = shiftKey ();                                           // Current key as memory
        final Memory d = shiftData();                                           // Current data as memory
        l.pushKey(k);                                                           // Transfer keys
        l.pushData(d);                                                          // Transfer data
       }
      return l;                                                                 // Split out leaf
     }
   }

//D1 Search                                                                     // Find a key, data pair

  boolean findAndInsert(Memory keyName, Memory dataValue)                       // Find the leaf for a key and insert the indicated key, data pair into if possible, returning true if the insertion was possible else false.
   {if (emptyTree())                                                            // Empty tree so we can insert directly
     {final Leaf leaf = new Leaf();                                                  // Create a leaf
      //root = leaf();                                                            // Create the root as a leaf
      //((Leaf)root).putLeaf(keyName, dataValue);                                 // insert key, data pair in the leaf
      return true;                                                              // Successfully inserted
     }

//    Node q = root;                                                              // Root of tree
//    for(int i = 0; i < 999 && q != null; ++i)                                   // Step down through tree up to some reasonable limit
//     {if (!(q instanceof Branch)) break;                                        // Stepped to a leaf
//      q = ((Branch)q).findFirstGreaterOrEqual(keyName);                         // Position of key
//     }
//
//    final int g = q.findIndexOfKey(keyName);                                    // We have arrived at a leaf
//    final Leaf l = (Leaf)q;
//    if (g != -1) l.dataValues.setElementAt(dataValue, g);                       // Key already present in leaf
//    else if (l.leafIsFull()) return false;                                      // There's no room in the leaf so return false
//    l.putLeaf(keyName, dataValue);                                              // On a leaf that is not full so we can insert directly
    return true;                                                                // Inserted directly
   }

  static void test_create_leaf()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    m.ok("000000000000000000000000000000000000000000000000001110000000000100011"); final Leaf l1 = m.new Leaf(); ok(l1.index, N-1);
    m.ok("000000000000000000000000100000000000000000000000001110000000000100001"); final Leaf l2 = m.new Leaf(); ok(l2.index, N-2);
    m.ok("000000000000000000000000100000000000000000000000011110000000000100000");
    Memory.Variable k = Memory.variable("key", N);                              // Create a key
    k.layout();
    ok( l1.isEmpty());
    ok(!l1.isFull ());
    for (int i = 0; i < M; i++)
     {k.set(i);
      l1.pushKey (m.new Key (k.memory()));
      l1.pushData(m.new Data(k.memory()));
     }
    ok(!l1.isEmpty());
    ok( l1.isFull ());
    m.ok("111001001111111001001111100000000000000000000000011110000000000100000");
   }

  static void test_split_leaf()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    m.set("111001001111111001001111100000000000000000000000001110000000000100001");
    Leaf k = m.new Leaf(1);
    k.memory().ok("111001001111111001001111");
    Leaf l = k.split();
    ok(k.nKeys(), 2); ok(k.nData(), 2);
    ok(l.nKeys(), 2); ok(l.nKeys(), 2);
    k.memory().ok("111111100011111111100011");
    l.memory().ok("000001000011000001000011");
    m.ok("111111100011111111100011100000100001100000100001111110000000000100000");
   }

  static void test_put_leaf()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Leaf l = m.new Leaf();
    l.put(m.new Key(4), m.new Data(8));
    l.put(m.new Key(3), m.new Data(6));
    l.put(m.new Key(2), m.new Data(4));
    l.put(m.new Key(1), m.new Data(2));
    for (int i = 1; i <= 4; i++)
     {ok(l.getKeyAsInt (i-1), i);
      ok(l.getDataAsInt(i-1), 2*i);
     }
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_create_leaf();
    test_split_leaf();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_put_leaf();
   }

  public static void main(String[] args)                                        // Test if called as a program
   {try                                                                         // Get a traceback in a format clickable in Geany if something goes wrong to speed up debugging.
     {if (github_actions) oldTests(); else newTests();                          // Tests to run
      testSummary();                                                            // Summarize test results
     }
    catch(Exception e)                                                          // Get a traceback in a format clickable in Geany
     {System.err.println(e);
      System.err.println(fullTraceBack(e));
     }
   }
 }
