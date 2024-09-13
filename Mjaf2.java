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

class Mjaf2 extends Memory.Structure                                            // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission, whereas the leaves (exterior nodes) have even number of keys and matching number of data elements because data is not transferred to the parent on fission  which simplifies deletions with complicating insertions.
 {final int bitsPerKey;                                                         // Number of bits in key
  final int bitsPerData;                                                        // Number of bits in data
  final int bitsPerNext;                                                        // Number of bits in next level
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
  final Stuck            nextLevel;                                             // Index of next level node
  final Memory.Variable  topNode;                                               // Next node if search key is greater than all keys in this node
  final Memory.Structure branch;                                                // Branch of the tree
  final Memory.Structure leaf;                                                  // Leaf of the tree
  final Memory.Union     branchOrLeaf;                                          // Branch or leaf of the tree
  final Memory.Variable  isBranch;                                              // The node is a branch if true
  final Memory.Variable  isLeaf;                                                // The node is a leaf if true
  final Memory.Structure node;                                                  // Node of the tree
  final Memory.Array     nodes;                                                 // Array of nodes comprising tree

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  Mjaf2(int BitsPerKey, int BitsPerData, int MaxKeysPerLeaf, int size)          // Define a Btree with a specified maximum number of keys per leaf.
   {super("tree");
    final int N = MaxKeysPerLeaf;
    bitsPerKey  = BitsPerKey;
    bitsPerNext = BitsPerKey + 1;
    bitsPerData = BitsPerData;
    if (N % 2 == 1) stop("# keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("# keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf   = N;
    maxKeysPerBranch = N-1;
    maxNodes = size;

    nodesCreated   = Memory.variable ("nodesCreated", 2*BitsPerKey+1);          // Number of nodes created
    keyDataStored  = Memory.variable ("keyDataStored",  BitsPerKey+1);          // Field to track number of keys stored in twos complement form hence an extra bit for the sign
    root           = Memory.variable ("root",           BitsPerKey+1);          // Root
    nodesFree      = new Stuck("nodesFree",      size,             bitsPerNext);// Free nodes
    branchKeyNames = new Stuck("branchKeyNames", maxKeysPerBranch, BitsPerKey); // Branch key names
    leafKeyNames   = new Stuck("leafKeyNames",   maxKeysPerLeaf,   BitsPerKey); // Leaf key names
    dataValues     = new Stuck("dataValues",     maxKeysPerLeaf,   BitsPerData);// Data values
    nextLevel      = new Stuck("nextLevel",      maxKeysPerBranch, bitsPerNext);// Next values
    topNode        = Memory.variable ("topNode",                   bitsPerNext);// Next node if search key is greater than all keys in this node
    branch         = Memory.structure("branch", branchKeyNames, nextLevel, topNode); // Branch of the tree
    leaf           = Memory.structure("leaf",   leafKeyNames,   dataValues);    // Leaf of the tree
    branchOrLeaf   = Memory.union    ("branchOrLeaf",   branch, leaf);          // Branch or leaf of the tree
    isBranch       = Memory.variable ("isBranch",       1);                     // The node is a branch if true
    isLeaf         = Memory.variable ("isLeaf",         1);                     // The node is a leaf if true
    node           = Memory.structure("node",  isLeaf,  isBranch, branchOrLeaf);// Node of the tree
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

  boolean isBranch(Memory index)                                                   // Test whether the memory represents a branch
   {nodes.setIndex(index.toInt());
    return isBranch.get(0);
   }

  boolean isLeaf(Memory index)                                                     // Test whether the memory represents a leaf
   {nodes.setIndex(index.toInt());
    return isLeaf.get(0);
   }

  boolean rootIsBranch()                                                   // Test whether the memory represents a branch
   {nodes.setIndex(root.toInt());
    return isBranch.get(0);
   }

  boolean rootIsLeaf()                                                     // Test whether the memory represents a leaf
   {nodes.setIndex(root.toInt());
    return isLeaf.get(0);
   }

//D1 Branch                                                                     // Methods applicable to a branch

  Memory node(int i)                                                            // Create memory representing a node index for testing purposes
   {final Memory.Variable next = Memory.variable("next", bitsPerNext);
    next.layout();
    next.set(i);
    return next.memory();
   }

  class Branch                                                                  // Describe a branch
   {final int index;

    Branch(int Index)                                                           // Create a branch with a specified index
     {index = Index;                                                            // Index of next free node
     }

    Branch(Memory top)                                                          // Create a branch with a specified top node
     {this(nodesFree.pop().toInt());                                            // Index of next free node
      clear();                                                                  // Clear the branch
      nodes.setIndex(index); isBranch.set(1);                                   // Mark as a branch
      topNode.set(top);
     }

    void free()                                                                 // Free a branch
     {nodes.setIndex(index);                                                    // Mark as a branch
      clear();                                                                  // Clear the branch
      nodesFree.push(index);                                                    // Put branch on free chain
     }

    void clear() {nodes.setIndex(index); node.zero();}                          // Clear a branch

    boolean isEmpty()                                                           // Branch is empty
     {nodes.setIndex(index);
      return branchKeyNames.isEmpty();
     }

    boolean isFull()                                                            // Branch is full
     {nodes.setIndex(index);
      return branchKeyNames.isFull();
     }

    boolean isBranch()                                                          // Node is a branch
     {nodes.setIndex(index);
      return isBranch.toInt() > 0;
     }

    int nKeys  () {nodes.setIndex(index); return branchKeyNames.stuckSize();}   // Number of keys in a branch
    int nLevels() {nodes.setIndex(index); return nextLevel     .stuckSize();}   // Number of next levels but not including the top node

    void pushKey(Key memory)                                                    // Push a key into a branch
     {nodes.setIndex(index);
      branchKeyNames.push(memory);
     }

    void pushNext(Memory memory)                                                // Push a next level into a branch
     {nodes.setIndex(index);
      nextLevel.push(memory);
     }

    Key shiftKey()                                                              // Shift a key into a branch
     {nodes.setIndex(index);
      return new Key(branchKeyNames.shift());
     }

    Memory shiftNext()                                                          // Shift index of next node
     {nodes.setIndex(index);
      return nextLevel.shift();
     }

    void insertKey(Key key, int i)                                              // Insert a key into a branch
     {nodes.setIndex(index);
      branchKeyNames.insertElementAt(key, i);
     }

    void insertNext(Memory next, int i)                                         // Insert index of next level node
     {nodes.setIndex(index);
      nextLevel.insertElementAt(next, i);
     }

    Key getKey(int i)                                                           // Get the indexed key
     {nodes.setIndex(index);
      return new Key(branchKeyNames.elementAt(i));
     }

    Memory getNext(int i)                                                       // Get the indexed index of the next level
     {nodes.setIndex(index);
      return dataValues.elementAt(i);
     }

    int getKeyAsInt(int i)                                                      // Get the indexed key as an integer
     {nodes.setIndex(index);
      return branchKeyNames.elementAt(i).toInt();
     }

    int getNextAsInt(int i)                                                     // Get the indexed next index value as an integer
     {nodes.setIndex(index);
      return nextLevel.elementAt(i).toInt();
     }

    Memory memory()                                                             // Get memory associated with a branch
     {nodes.setIndex(index);
      return branch.memory();
     }

    void put(Key keyName, Memory nextLevel)                                     // Put a key / next node index value pair into a branch
     {final int K = nKeys();                                                    // Number of keys currently in node
      for (int i = 0; i < K; i++)                                               // Search existing keys for a greater key
       {final Memory k = branchKeyNames.elementAt(i);                           // Current key
        final boolean l = keyName.lessThanOrEqual(k);                           // Insert new key in order
        if (l)                                                                  // Insert new key in order
         {insertKey (keyName,   i);                                             // Insert key
          insertNext(nextLevel, i);                                             // Insert data
          keyDataStored.inc();                                                  // Created a new entry in the branch
          return;
         }
       }
      pushKey (keyName);                                                        // Either the branch is empty or the new key is greater than every existing key
      pushNext(nextLevel);
      keyDataStored.inc();                                                      // Created a new entry in the branch
     }

    void splitRoot()                                                            // Split the root when it is a branch
     {if (isFull())
       {final Key    k = new Key(branchKeyNames.elementAt(maxKeysPerBranch/2));
        final Branch l = split(), b = new Branch(memory());
        b.put(k, l.memory());
        root.set(b.index);
       }
     }

    Branch split()                                                              // Split a branch into two branches at the indicated key
     {nodes.setIndex(index);
      final int K = branchKeyNames.stuckSize(), f = maxKeysPerBranch/2;         // Number of keys currently in node
      if (f < K-1) {} else stop("Split", f, "too big for branch of size:", K);
      if (f <   1)         stop("First", f, "too small");
      final Memory t = nextLevel.elementAt(f);                                  // Top node
      final Branch b = new Branch(t);                                           // Recycle a branch

      for (int i = 0; i < f; i++)                                               // Remove first keys from old node to new node
       {nodes.setIndex(index);
        final Key    k = shiftKey();
        final Memory n = shiftNext();
        b.put(k, n);
       }
      nodes.setIndex(index);
      shiftKey();                                                               // Remove central key which is no longer required
      shiftNext();
      return b;
     }

    boolean joinable(Branch a)                                                  // Check that we can join two leaves
     {return nKeys() + a.nKeys() <= maxKeysPerBranch;
     }

    void join(Branch Join, Key joinKeyName)                                     // Append the second branch to the first one adding the specified key
     {final int K = nKeys(), J = Join.nKeys();                                  // Number of keys currently in node
      if (K + 1 + J > maxKeysPerBranch) stop("Join of branch has too many keys",
          K,"+1+",J, "greater than", maxKeysPerBranch);


      nodes.setIndex(Join.index);
      final Memory t = topNode.memory();                                        // TopNode from branch being joined

      nodes.setIndex(index);
      pushKey(joinKeyName);                                                     // Key to separate joined branches
      pushNext(topNode.memory());                                               // Push current top node
      topNode.set(t);                                                           // New top node is the one from teh branch being joined

      for (int i = 0; i < J; i++)                                               // Add right hand branch
       {nodes.setIndex(Join.index);
        final Key    k = new Key(branchKeyNames.elementAt(i));
        final Memory n = nextLevel.elementAt(i);
        nodes.setIndex(index);
        pushKey(k);                                                             // Push memory associated with key
        pushNext(n);
       }
      Join.free();
     }

    Memory findFirstGreaterOrEqual(Key keyName)                                 // Find first key which is greater than the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {nodes.setIndex(index);
      final int N = nKeys();                                                    // Number of keys currently in node
      for (int i = 0; i < N; i++)                                               // Check each key
       {final Key k = new Key(branchKeyNames.elementAt(i));                     // Key
        final boolean l = keyName.compareTo(k) <= 0;                           // Compare current key with search key
        if (l) return nextLevel.elementAt(i);                                   // Current key is greater than or equal to the search key
       }
      return topNode;
     }

    public String toString()                                                    // Print branch
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      s.append("Branch(");
      final int K = nKeys();
      for  (int i = 0; i < K; i++)
       {s.append(""+branchKeyNames.elementAt(i).toInt()+":"+
                         nextLevel.elementAt(i).toInt()+", ");
       }
      if (K > 0) s.setLength(s.length()-2);
      s.append((K > 0 ? ", " : "")+topNode.toInt()+")");
      return s.toString();
     }

    public String shortString()                                                 // Print a branch compactly
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      final int K = nKeys();
      for  (int i = 0; i < K; i++) s.append(""+branchKeyNames.elementAt(i).toInt()+",");
      if (K > 0) s.setLength(s.length()-1);
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print branch  horizontally
     {nodes.setIndex(index);
      padStrings(S, level);
      S.elementAt(level).append(debug ? toString() : shortString());
      padStrings(S, level);
     }

    void ok(String expected)                                                    // Check node is as expected
     {nodes.setIndex(index);
      Mjaf.ok(toString(), expected);
     }
   }

//D1 Leaf                                                                       // Methods applicable to a leaf

  class Leaf                                                                    // Describe a leaf
   {final int index;

    Leaf(int Index) {index = Index;}                                            // Address a leaf by index

    Leaf()                                                                      // Create a leaf
     {this(nodesFree.pop().toInt());                                            // Index of next free node
      clear();                                                                  // Clear the leaf
      nodes.setIndex(index); isLeaf.set(1);                                     // Mark as a leaf
     }

    void free()                                                                 // Free a leaf
     {nodes.setIndex(index);                                                    // Mark as a leaf
      clear();                                                                  // Clear the leaf
      nodesFree.push(index);                                                    // Put leaf on free chain
     }

    void clear() {nodes.setIndex(index); node.zero();}                          // Clear a leaf

    boolean isEmpty()                                                           // Leaf is empty
     {nodes.setIndex(index);
      return leafKeyNames.isEmpty();
     }

    boolean isFull()                                                            // Leaf is full
     {nodes.setIndex(index);
      return leafKeyNames.isFull();
     }

    boolean isLeaf()                                                            // Node is a leaf
     {nodes.setIndex(index);
      return isLeaf.toInt() > 0;
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

    Key shiftKey()                                                              // Push a key into a leaf
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

    boolean joinable(Leaf a)                                                    // Check that we can join two leaves
     {return nKeys() + a.nKeys() <= maxKeysPerLeaf;
     }

    void join(Leaf Join)                                                        // Join the specified leaf onto the end of this leaf
     {final int K = nKeys(), J = Join.nKeys();                                  // Number of keys currently in node
      if (!joinable(Join)) stop("Join of leaf has too many keys", K,
        "+", J, "greater than", maxKeysPerLeaf);

      for (int i = 0; i < J; i++)
       {nodes.setIndex(Join.index);
        final Memory k = leafKeyNames.elementAt(i);
        final Memory d = dataValues.elementAt(i);
        nodes.setIndex(index);
        leafKeyNames.push(k);
        dataValues.push(d);
       }
      Join.free();                                                              // Free the leaf that was
     }

    public String toString()                                                    // Print leaf
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      s.append("Leaf(");
      final int K = leafKeyNames.stuckSize();
      for  (int i = 0; i < K; i++)
        s.append(""+leafKeyNames.elementAt(i).toInt()+":"+
                      dataValues.elementAt(i).toInt()+", ");
      if (K > 0) s.setLength(s.length()-2);
      s.append(")");
      return s.toString();
     }

    public String shortString()                                                 // Print a leaf compactly
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      final int K = leafKeyNames.stuckSize();
      for  (int i = 0; i < K; i++) s.append(""+leafKeyNames.elementAt(i).toInt()+",");
      if (K > 0) s.setLength(s.length()-1);
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print leaf  horizontally
     {nodes.setIndex(index);
      padStrings(S, level);
      S.elementAt(level).append(debug ? toString() : shortString());
      padStrings(S, level);
     }

    void ok(String expected)                                                    // Check node is as expected
     {nodes.setIndex(index);
      Mjaf.ok(toString(), expected);
     }
   }

//D1 Insertion                                                                  // Insert keys and data into the Btree
/*
  void put(Key keyName, Data dataValue)                                         // Insert a new key, data pair into the Btree
   {//if (findAndInsert(keyName, dataValue)) return;                              // Do a fast insert if possible, thisis increasingly likely in trees with large leaves
    if (isLeaf(root))                                                           // Insert into root as a leaf
     {if (!((Leaf)root).leafIsFull()) ((Leaf)root).putLeaf(keyName, dataValue); // Still room in the root while it is is a leaf
      else                                                                      // Insert into root as a leaf which is full
       {final Leaf   l = ((Leaf)root).splitLeaf();                              // New left hand side of root
        final Key    k = l.splitKey();                                          // Splitting key
        final Branch b = branch(root);                                          // New root with old root to right
        b.putBranch(k, l);                                                      // Insert left hand node all of whose elements are less than the first element of what was the root
        final Leaf f = lessThanOrEqual(keyName, k) ? l : (Leaf)root;            // Choose leaf
        f.putLeaf(keyName, dataValue);                                          // Place in leaf
        root = b;                                                               // The root now has just one entry in it - the splitting eky
       }
      return;
     }
    else ((Branch)root).splitRoot();                                            // Split full root which is a branch not a leaf

    Branch p = (Branch)root; Node q = p;                                        // The root has already been split so the parent child relationship will be established

    for(int i = 0; i < 999; ++i)                                                // Step down through tree to find the required leaf, splitting as we go
     {if (!(q instanceof Branch)) break;                                        // Stepped to a branch

      if (((Branch)q).branchIsFull())                                           // Split the branch because it is full and we might need to insert below it requiring a slot in this node
       {final Key    k = q.splitKey();                                          // Splitting key
        final Branch l = ((Branch)q).splitBranch();                             // New lower node
        p.putBranch(k, l);                                                      // Place splitting key in parent
        ((Branch)root).splitRoot();                                             // Root might need to be split to re-establish the invariants at start of loop
        if (lessThanOrEqual(keyName, k)) q = l;                                 // Position on lower node if search key is less than splitting key
       }

      p = (Branch)q;                                                            // Step parent down
      q = p.findFirstGreaterOrEqual(keyName);                                   // The node does not need splitting
     }

    final Leaf l = (Leaf)q;
    final int  g = l.findIndexOfKey(keyName);                                   // Locate index of key
    if (g != -1) l.dataValues.setElementAt((Memory)dataValue, g);               // Key already present in leaf
    else if (l.leafIsFull())                                                    // Split the node because it is full and we might need to insert below it requiring a slot in this node
     {final Key  k = l.splitKey();
      final Leaf e = l.splitLeaf();
      p.putBranch(k, e);
      if (lessThanOrEqual(keyName, k)) e.putLeaf(keyName, dataValue);           // Insert key in the appropriate split leaf
      else                             l.putLeaf(keyName, dataValue);
     }
    else l.putLeaf(keyName, dataValue);                                         // On a leaf that is not full so we can insert directly
   } // put
*/

//D1 Print                                                                      // Print a tree

  static void padStrings(Stack<StringBuilder> S, int level)                     // Pad a stack of strings so they all have the same length
   {for (int i = S.size(); i <= level; ++i) S.push(new StringBuilder());
    int m = 0;
    for (StringBuilder s : S) m = m < s.length() ? s.length() : m;
    for (StringBuilder s : S)
      if (s.length() < m) s.append(" ".repeat(m - s.length()));
   }

//D1 Tests                                                                      // Tests

  static void test_create_branch()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Memory.Variable next = Memory.variable("next", N+1);
    next.layout();
    next.set(M);
    final Branch b = m.new Branch(next.memory());
    ok( m.isBranch(b.memory()));
    ok(!m.isLeaf  (b.memory()));

    Memory.Variable key = Memory.variable("key", N);
    key.layout();
    ok( b.index, N-1);
    ok( b.isEmpty());
    ok(!b.isFull ());
    ok( b.isBranch());
    for (int i = 0; i < M-1; i++) b.put(m.new Key(i), m.node(i));
    ok(!b.isEmpty());
    ok( b.isFull ());
    b.ok("Branch(0:0, 1:1, 2:2, 4)");

    Branch c = b.split();
    ok(c.nKeys(), 1);
    ok(c.nLevels(), 1);
    ok(b.nKeys(), 1);
    ok(b.nKeys(), 1);
    b.ok("Branch(2:2, 4)");
    c.ok("Branch(0:0, 1)");
   }

  static void test_put_branch()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Memory.Variable next = Memory.variable("next", M+1);
    next.layout();
    next.set(M);
    Branch b = m.new Branch(next.memory());

    for (int i = 0; i < 3; i++) b.put(m.new Key(3-i), m.node(2*i));

    ok(b.getKeyAsInt(0), 1);  ok(b.getNextAsInt(0), 4);
    ok(b.getKeyAsInt(1), 2);  ok(b.getNextAsInt(1), 2);
    ok(b.getKeyAsInt(2), 3);  ok(b.getNextAsInt(2), 0);
   }

  static void test_join_branch()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Branch j = m.new Branch(m.node(11));
    Branch k = m.new Branch(m.node(13));
    Branch l = m.new Branch(m.node(15));
    j.put(m.new Key(1), m.node(8)); ok( k.joinable(j));
    k.put(m.new Key(3), m.node(4)); ok( k.joinable(j));

    k.ok("Branch(3:4, 13)");
    j.ok("Branch(1:8, 11)");
    ok(k.nKeys(), 1);
    ok(j.nKeys(), 1);
    j.join(k, m.new Key(2));
    j.ok("Branch(1:8, 2:11, 3:4, 13)");
    ok(m.nodesFree.stuckSize(), 2);
   }

  static void test_create_leaf()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    final Leaf l = m.new Leaf(); ok(l.index, N-1);
    ok(!m.isBranch(l.memory()));
    ok( m.isLeaf  (l.memory()));

    Memory.Variable key = Memory.variable("key", N);
    key.layout();
    ok( l.isEmpty());
    ok(!l.isFull ());
    ok( l.isLeaf());
    for (int i = 0; i < M; i++)
     {key.set(i);
      l.put(m.new Key(key.memory()), m.new Data(key.memory()));
     }
    ok(!l.isEmpty());
    ok( l.isFull ());
    l.ok("Leaf(0:0, 1:1, 2:2, 3:3)");

    Leaf k = l.split();
    ok(k.nKeys(), 2); ok(k.nData(), 2);
    ok(l.nKeys(), 2); ok(l.nKeys(), 2);
    k.ok("Leaf(0:0, 1:1)");
    l.ok("Leaf(2:2, 3:3)");
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

  static void test_join_leaf()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Leaf j = m.new Leaf();
    Leaf k = m.new Leaf();
    Leaf l = m.new Leaf();
    j.put(m.new Key(1), m.new Data(8)); ok( k.joinable(j));
    j.put(m.new Key(2), m.new Data(6)); ok( k.joinable(j));
    k.put(m.new Key(3), m.new Data(4)); ok( k.joinable(j));
    k.put(m.new Key(4), m.new Data(2)); ok( k.joinable(j));
    l.put(m.new Key(4), m.new Data(8)); ok( k.joinable(l));
    l.put(m.new Key(3), m.new Data(6)); ok( k.joinable(l));
    l.put(m.new Key(2), m.new Data(4)); ok(!k.joinable(l));
    l.put(m.new Key(1), m.new Data(2)); ok(!k.joinable(l));

    k.ok("Leaf(3:4, 4:2)");
    j.ok("Leaf(1:8, 2:6)");
    ok(m.nodesFree.stuckSize(), 1);
    j.join(k);
    j.ok("Leaf(1:8, 2:6, 3:4, 4:2)");
    ok(m.nodesFree.stuckSize(), 2);
   }

  static void test_root()
   {final int N = 4, M = 4;
    Mjaf2  m = mjaf(N, N, M, N);
    Leaf   l = m.new Leaf();
    Branch b = m.new Branch(m.node(1));
    m.root.set(l.index); ok(m.rootIsLeaf());
    m.root.set(b.index); ok(m.rootIsBranch());
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_create_branch();
    test_put_branch();
    test_join_branch();
    test_create_leaf();
    test_put_leaf();
    test_join_leaf();
   }

  static void newTests()                                                        // Tests being worked on
   {//oldTests();
    test_root();
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
