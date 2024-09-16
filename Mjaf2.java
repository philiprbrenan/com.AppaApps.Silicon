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
  final int splitIdx;                                                           // Index of splitting key

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
    splitIdx         = maxKeysPerBranch >> 1;                                   // Index of splitting key
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

  int     size     () {return keyDataStored.toInt();}                           // Number of entries in the tree
  boolean emptyTree() {return root.isNull();}                                   // Test for an empty tree

  public String toString()                                                      // Convert tree to string
   {final StringBuilder b = new StringBuilder();
    b.append("Mjaf\n");
    b.append(String.format
     ("BitsPerKey  : %4d  BitsPerData  : %4d  MaxKeysPerLeaf: %4d MaxNodes: %4d\n",
       bitsPerKey, bitsPerData, maxKeysPerLeaf, maxNodes));

    b.append(String.format
     ("nodesCreated: %4d  keyDataStored: %4d  root: %4d\n",
       nodesCreated.toInt(), keyDataStored.toInt(), root.toInt()));

    b.append("Node\n");
    for (int i = 0; i < maxNodes; i++)
     {nodes.setIndex(i);
      final boolean ib = isBranch.toInt() > 0, il = isLeaf.toInt() > 0;
      if (!ib && !il) continue;
      b.append(String.format("%4d ", i));

      if (ib) b.append("      Branch Keys:"+branchKeyNames.print("","")+
                                   " Next:"+nextLevel     .print("","")+
                                        " "+topNode.toInt()+"\n");
      else    b.append("      Leaf   Keys:"+leafKeyNames  .print("","")+
                                   " Data:"+dataValues    .print("","")+
                                       "\n");
     }
    return b.toString();
   }

//D1 Node                                                                       // A branch or a leaf

  class Key extends Memory                                                      // Memory for a key
   {Key(Memory memory)
     {super(bitsPerKey); bits = memory.bits; at = memory.at; width = memory.width;
     }
    Key(int n) {super(memoryFromInt(bitsPerKey, n));}
   }
  Key key(int n) {return new Key(n);}

  class Data extends Memory                                                     // Memory for a data value
   {Data(Memory memory)
     {super(bitsPerData); bits = memory.bits; at = memory.at; width = memory.width;
     }
    Data(int n) {super(memoryFromInt(bitsPerData, n));}
   }
  Data data(int n) {return new Data(n);}

  class Node                                                                    // A node contains a leaf or a branch
   {final int index;                                                            // The index of the node in the memory layout
    Node()                         {index = root.toInt();}                      // Node from root
    Node(int Index)                {index = Index;}                             // Node from an index - useful in testing
    Node(Branch branch)            {index = branch.index;}                      // Node from a branch
    Node(Leaf   leaf)              {index = leaf.index;}                        // Node from a leaf
    Node(Memory memory)            {index = memory.toInt();}                    // Node from memory
    Node(Memory.Variable variable) {index = variable.toInt();}                  // Node from variable
    Memory node() {return memoryFromInt(bitsPerNext, index);}                   // Create memory representing a node index

    boolean isLeaf  () {nodes.setIndex(index); return isLeaf  .get(0);}         // Whether the node represents a leaf
    boolean isBranch() {nodes.setIndex(index); return isBranch.get(0);}         // Whether the node represents a leaf

    void setRoot()  {root.set(index);}                                          // Make this node the root node
    Branch branch() {return new Branch(index);}                                 // Make a branch from this node
    Leaf   leaf()   {return new Leaf (index); }                                 // Make a leaf from this node

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print leaf  horizontally
     {nodes.setIndex(index);
      padStrings(S, level);
      S.elementAt(level).append(debug ? toString() : shortString());
      padStrings(S, level);
     }

    void free()                                                                 // Free a branch
     {nodes.setIndex(index);                                                    // Mark as a branch
      clear();                                                                  // Clear the branch
      nodesFree.push(index);                                                    // Put branch on free chain
     }

    void clear() {nodes.setIndex(index); node.zero();}                          // Clear a branch

    public String shortString() {return "";}                                    // Print a leaf compactly

    void ok(String expected)                                                    // Check node is as expected
     {nodes.setIndex(index);
      Mjaf.ok(toString(), expected);
     }
   }

//D1 Branch                                                                     // Methods applicable to a branch

  class Branch extends Node                                                     // Describe a branch
   {Branch(int Index) {super(Index);}                                           // Address the branch at the specified index in the memory layout

    Branch(Node top)                                                            // Create a branch with a specified top node
     {this(nodesFree.pop().toInt());                                            // Index of next free node
      clear();                                                                  // Clear the branch
      nodes.setIndex(index); isBranch.set(1);                                   // Mark as a branch
      topNode.set(top.node());                                                  // Set the top node for this branch
     }

    boolean isEmpty() {nodes.setIndex(index);return branchKeyNames.isEmpty();}  // Branch is empty
    boolean isFull()  {nodes.setIndex(index);return branchKeyNames.isFull();}   // Branch is full

    int nKeys() {nodes.setIndex(index); return branchKeyNames.stuckSize();}     // Number of keys in a branch

    Key splitKey()                                                              // Splitting key
     {nodes.setIndex(index);
      return new Key(getKey(splitIdx).duplicate());
     }

    void pushKey(Key memory)                                                    // Push a key into a branch
     {nodes.setIndex(index);branchKeyNames.push(memory);
     }

    void pushNext(Node node)                                                    // Push a next level into a branch
     {nodes.setIndex(index);
      nextLevel.push(node.node());
     }

    Key shiftKey()                                                              // Shift a key into a branch
     {nodes.setIndex(index);
      return new Key(branchKeyNames.shift());
     }

    Node shiftNext()                                                            // Shift index of next node
     {nodes.setIndex(index);
      return new Node(nextLevel.shift());
     }

    Key popKey()                                                                // Pop a key into a branch
     {nodes.setIndex(index);
      return new Key(branchKeyNames.pop());
     }

    Node popNext()                                                              // Pop index of next node
     {nodes.setIndex(index);
      return new Node(nextLevel.pop());
     }

    void insertKey(Key key, int i)                                              // Insert a key into a branch
     {nodes.setIndex(index);
      branchKeyNames.insertElementAt(key, i);
     }

    void insertNext(Node node, int i)                                           // Insert index of next level node
     {nodes.setIndex(index);
      nextLevel.insertElementAt(node.node(), i);
     }

    Key getKey(int i)                                                           // Get the indexed key
     {nodes.setIndex(index);
      return new Key(branchKeyNames.elementAt(i).duplicate());
     }

    Node getNext(int i)                                                         // Get the indexed index of the next level
     {nodes.setIndex(index);
      return new Node(nextLevel.elementAt(i).toInt());
     }

    int findIndexOfKey(Key key)                                                 // Get the indexed key as an integer
     {nodes.setIndex(index);
      return branchKeyNames.indexOf(key);
     }

    void removeKey(int i)                                                       // Remove the indicated key from the branch
     {nodes.setIndex(index);
      branchKeyNames.removeElementAt(i);
     }

    void removeNext(int i)                                                      // Remove the indicated next level from the branch
     {nodes.setIndex(index);
      nextLevel.removeElementAt(i);
     }

    Node lastNext()                                                             // Last next element
     {nodes.setIndex(index);
      final int n = nKeys();
      final int l = nextLevel.elementAt(n-1).toInt();
      return new Node(l);
     }

    Node getTop()                                                               // Get the top bode as an integer
     {nodes.setIndex(index);
      return new Node(topNode);
     }

    void setTop(int TopNode)                                                    // Set the top node
     {nodes.setIndex(index);
      topNode.set(TopNode);
     }

    void put(Key keyName, Node node)                                            // Put a key / next node index value pair into a branch
     {final int K = nKeys();                                                    // Number of keys currently in node
      for (int i = 0; i < K; i++)                                               // Search existing keys for a greater key
       {final Memory  k = getKey(i);                                            // Current key
        final boolean l = keyName.lessThanOrEqual(k);                           // Insert new key in order
        if (l)                                                                  // Insert new key in order
         {insertKey (keyName, i);                                               // Insert key
          insertNext(node,    i);                                               // Insert data
          keyDataStored.inc();                                                  // Created a new entry in the branch
          return;
         }
       }
      pushKey (keyName);                                                        // Either the branch is empty or the new key is greater than every existing key
      pushNext(node);
      keyDataStored.inc();                                                      // Created a new entry in the branch
     }

    void splitRoot()                                                            // Split the root when it is a branch
     {if (isFull())
       {final Key    k = new Key(getKey(splitIdx));
        final Branch l = split();
        final Branch b = new Branch(new Node(index));
        b.put(k, new Node(l));
        b.setRoot();
       }
     }

    Branch split()                                                              // Split a branch into two branches at the indicated key
     {final int K = nKeys(), f = splitIdx;                                      // Number of keys currently in node
      if (f < K-1) {} else stop("Split", f, "too big for branch of size:", K);
      if (f <   1)         stop("First", f, "too small");
      final Node   t = getNext(f);                                              // Top node
      final Branch b = new Branch(t);                                           // Recycle a branch

      for (int i = 0; i < f; i++)                                               // Remove first keys from old node to new node
       {final Key  k = shiftKey();
        final Node n = shiftNext();
        b.put(k, n);
       }
      shiftKey();                                                               // Remove central key which is no longer required
      shiftNext();
      return b;
     }

    boolean joinable(Branch a)                                                  // Check that we can join two leaves
     {return nKeys() + a.nKeys() + 1 <= maxKeysPerBranch;
     }

    void join(Branch Join, Key joinKeyName)                                     // Append the second branch to the first one adding the specified key
     {final int K = nKeys(), J = Join.nKeys();                                  // Number of keys currently in node
      if (K + 1 + J > maxKeysPerLeaf) stop("Join of branch has too many keys",
          K,"+1+",J, "greater than", maxKeysPerBranch);

      nodes.setIndex(Join.index);
      final Memory t = topNode.memory();                                        // TopNode from branch being joined

      nodes.setIndex(index);
      pushKey(joinKeyName);                                                     // Key to separate joined branches
      pushNext(new Node(topNode));                                              // Push current top node
      topNode.set(t);                                                           // New top node is the one from teh branch being joined

      for (int i = 0; i < J; i++)                                               // Add right hand branch
       {final Key  k = Join.getKey (i);
        final Node n = Join.getNext(i);
        pushKey(k);                                                             // Push memory associated with key
        pushNext(n);
       }
      Join.free();
     }

    Node findFirstGreaterOrEqual(Key keyName)                                   // Find first key which is greater than the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {nodes.setIndex(index);
      final int N = nKeys();                                                    // Number of keys currently in node
      for (int i = 0; i < N; i++)                                               // Check each key
       {final Key     k = getKey(i);                                            // Key
        final boolean l = keyName.compareTo(k) <= 0;                            // Compare current key with search key
        if (l) return getNext(i);                                               // Current key is greater than or equal to the search key
       }
      return getTop();
     }

    public String toString()                                                    // Print branch
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      s.append("Branch_"+index+"(");
      final int K = nKeys();
      for  (int i = 0; i < K; i++)
       {s.append(""+getKey(i).toInt()+":"+getNext(i).index+", ");
       }
      if (K > 0) s.setLength(s.length()-2);
      s.append((K > 0 ? ", " : "")+topNode.toInt()+")");
      return s.toString();
     }

    public String shortString()                                                 // Print a branch compactly
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      final int K = nKeys();
      for  (int i = 0; i < K; i++) s.append(""+getKey(i).toInt()+",");
      if (K > 0) s.setLength(s.length()-1);                                     // Remove trailing comma
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print branch horizontally
     {final int N = nKeys();
      for (int i = 0; i < N; i++)
       {final Node n = getNext(i);
        if (n.isBranch())
         {final Branch b = n.branch();
          b.printHorizontally(S, level+1, debug);
          padStrings(S, level);
          S.elementAt(level).append(""+getKey(i).toInt()+" ");
         }
        else
         {n.leaf().printHorizontally(S, level+1, debug);
          padStrings(S, level);
          S.elementAt(level).append(getKey(i).toInt()+" ");
         }
       }
      if (getTop().isBranch())
       {getTop().branch().printHorizontally(S, level+1, debug);
       }
      else
       {getTop().leaf().printHorizontally(S, level+1, debug);
       }
     }
   }

//D1 Leaf                                                                       // Methods applicable to a leaf

  class Leaf extends Node                                                       // Describe a leaf
   {Leaf(int Index) {super(Index);}                                             // Address a leaf by index

    Leaf()                                                                      // Create a leaf
     {this(nodesFree.pop().toInt());                                            // Index of next free node
      clear();                                                                  // Clear the leaf
      nodes.setIndex(index); isLeaf.set(1);                                     // Mark as a leaf
     }

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

    Key splitKey()                                                              // Splitting key
     {nodes.setIndex(index);
      return new Key(leafKeyNames.elementAt(splitIdx).duplicate());
     }

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

    void setData(Data data, int i)                                              // Set the index element to the specified data
     {nodes.setIndex(index);
      dataValues.setElementAt(data, i);
     }

    int findIndexOfKey(Key key)                                                 // Get the indexed key as an integer
     {nodes.setIndex(index);
      return leafKeyNames.indexOf(key);
     }

    void removeKey(int i)                                                       // Remove the indicated key
     {nodes.setIndex(index);
      leafKeyNames.removeElementAt(i);
     }

    void removeData(int i)                                                      // Remove the indicated key
     {nodes.setIndex(index);
      dataValues.removeElementAt(i);
     }

    void put(Key keyName, Data dataValue)                                       // Put a key / data value pair into a leaf
     {final int K = nKeys();                                                    // Number of keys currently in node
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
     {final int K = nKeys(), f = maxKeysPerLeaf/2;                              // Number of keys currently in node
      if (f < K) {} else stop("Split", f, "too big for leaf of size:", K);
      if (f < 1)         stop("First", f, "too small");
      final Leaf l = new Leaf();                                                // New leaf
      for (int i = 0; i < f; i++)                                               // Transfer keys and data
       {final Key  k = shiftKey ();                                             // Current key as memory
        final Data d = shiftData();                                             // Current data as memory
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
       {final Key  k = Join.getKey(i);
        final Data d = Join.getData(i);
        pushKey(k);
        pushData(d);
       }
      Join.free();                                                              // Free the leaf that was
     }

    public String toString()                                                    // Print leaf
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      s.append("Leaf_"+index+"(");
      final int K = leafKeyNames.stuckSize();
      for  (int i = 0; i < K; i++)
       {s.append(""+getKey(i).toInt()+":"+getData(i).toInt()+", ");
       }
      if (K > 0) s.setLength(s.length()-2);
      s.append(")");
      return s.toString();
     }

    public String shortString()                                                 // Print a leaf compactly
     {nodes.setIndex(index);
      final StringBuilder s = new StringBuilder();
      final int K = nKeys();
      for  (int i = 0; i < K; i++) s.append(""+getKey(i).toInt()+",");
      if (K > 0) s.setLength(s.length()-1);
      return s.toString();
     }
   }

//D1 Search                                                                     // Find a key, data pair

  Data find(Key keyName)                                                        // Find a the data associated with a key
   {if (emptyTree()) return null;                                               // Empty tree
    Node q = new Node();                                                        // Root as a node

    for(int i = 0; i < 999 ; ++i)                                               // Step down through tree up to some reasonable limit
     {if (q.isLeaf()) break;                                                    // Stepped to a leaf
      q = q.branch().findFirstGreaterOrEqual(keyName);                          // Position of key
     }

    final Leaf l = q.leaf();                                                    // Reached a leaf
    final int g = l.findIndexOfKey(keyName);                                    // We have arrived at a leaf
    return g == -1 ? null : l.getData(g);                                       // Key is not or is present in leaf
   }

  boolean findAndInsert(Key keyName, Data dataValue)                            // Find the leaf for a key and insert the indicated key, data pair into if possible, returning true if the insertion was possible else false.
   {if (emptyTree())                                                            // Empty tree so we can insert directly
     {final Leaf l = new Leaf();                                                // Create the root as a leaf
      l.setRoot();                                                              // Create the root as a leaf
      l.put(keyName, dataValue);                                                // Insert key, data pair in the leaf
      return true;                                                              // Successfully inserted
     }

    Node q = new Node();                                                        // Start at the root
    for(int i = 0; i < 999 ; ++i)                                               // Step down through tree up to some reasonable limit
     {if (q.isLeaf()) break;                                                    // Stepped to a leaf
      q = q.branch().findFirstGreaterOrEqual(keyName);                          // Position of key
     }

    final Leaf l = q.leaf();                                                    // Reached a leaf
    final int g = l.findIndexOfKey(keyName);                                    // We have arrived at a leaf
    if (g != -1) l.setData(dataValue, g);                                       // Key already present in leaf
    else if (l.isFull()) return false;                                          // There's no room in the leaf so return false
    else l.put(keyName, dataValue);                                             // On a leaf that is not full so we can insert directly
    return true;                                                                // Inserted directly
   }

//D1 Insertion                                                                  // Insert keys and data into the Btree

  void put(Key keyName, Data dataValue)                                         // Insert a new key, data pair into the Btree
   {if (findAndInsert(keyName, dataValue)) return;                              // Do a fast insert if possible, thisis increasingly likely in trees with large leaves

    if (new Node().isLeaf())                                                    // Insert into root as a leaf
     {final Leaf r = new Node().leaf();                                         // Root is a leaf
      if (!r.isFull()) r.put(keyName, dataValue);                               // Still room in the root while it is is a leaf
      else                                                                      // Insert into root as a leaf which is full
       {final Leaf   l = r.split();                                             // New left hand side of root
        final Key    k = l.splitKey();                                          // Splitting key
        final Branch b = new Branch(new Node(r.index));                         // New root with old root to right
        b.put(k, new Node(l));                                                  // Insert left hand node all of whose elements are less than the first element of what was the root
        final Leaf f = keyName.lessThanOrEqual(k) ? l : r;                      // Choose leaf
        f.put(keyName, dataValue);                                              // Place in leaf
        b.setRoot();                                                            // The root now has just one entry in it - the splitting eky
       }
      return;
     }
    else new Node().branch().splitRoot();                                       // Split full root which is a branch not a leaf

    Branch p = new Node().branch();                                             // The root has already been split so the parent child relationship will be established
    Node   q = p;                                                               // Child of parent

    for(int i = 0; i < 999; ++i)                                                // Step down through tree to find the required leaf, splitting as we go
     {if (q.isLeaf()) break;                                                    // Stepped to a branch
      final Branch b = q.branch();

      if (b.isFull())                                                           // Split the branch because it is full and we might need to insert below it requiring a slot in this node
       {final Key    k = b.splitKey();                                          // Splitting key
        final Branch l = b.split();                                             // New lower node
        p.put(k, l);                                                            // Place splitting key in parent
        final Branch r = new Node().branch();
        r.splitRoot();                                                          // Root might need to be split to re-establish the invariants at start of loop
        if (keyName.lessThanOrEqual(k)) q = l;                                  // Position on lower node if search key is less than splitting key
       }

      p = q.branch();                                                           // Step parent down
      q = p.findFirstGreaterOrEqual(keyName);                                   // The node does not need splitting
     }

    final Leaf l = q.leaf();
    final int g = l.findIndexOfKey(keyName);                                    // Locate index of key
    if (g != -1) l.setData(dataValue, g);                                       // Key already present in leaf
    else if (l.isFull())                                                        // Split the node because it is full and we might need to insert below it requiring a slot in this node
     {final Key  k = l.splitKey();
      final Leaf e = l.split();
      p.put(k, e);

      if (keyName.lessThanOrEqual(k)) e.put(keyName, dataValue);                // Insert key in the appropriate split leaf
      else                            l.put(keyName, dataValue);
     }
    else l.put(keyName, dataValue);                                             // On a leaf that is not full so we can insert directly
   } // put


//D1 Deletion                                                                   // Delete a key from a Btree

  Data delete(Key keyName)                                                      // Delete a key from a tree
   {if (emptyTree()) return null;                                               // The tree is empty
    final Data foundData = find(keyName);                                       // Find the data associated with the key
    if (foundData == null) return null;                                         // The key is not present so cannot be deleted

    if (new Node().isLeaf())                                                    // Delete from root as a leaf
     {final Leaf r = new Node().leaf();                                         // Root is a leaf
      final int  i = r.findIndexOfKey(keyName);                                 // Only one leaf and the key is known to be in the Btree so it must be in this leaf
      r.removeKey (i);
      r.removeData(i);
      keyDataStored.dec();
      return foundData;
     }

    if (new Node().branch().nKeys() == 1)                                       // If the root is a branch and only has one key so we might be able to merge its children
     {final Branch r = new Node().branch();                                     // Root as a branch
      final Node   A = r.getNext(0);                                            // Step down

      if (A.isLeaf())                                                           // Step down to leaf
       {final Leaf    a = A.leaf(), b = r.getTop().leaf();
        final boolean j = a.joinable(b);                                        // Can we merge the two leaves
        if (j)                                                                  // Merge the two leaves
         {a.join(b);
          a.setRoot();                                                          // New merged root
         }
       }
      else                                                                      // Merge two branches under root
       {final Branch a = new Branch(A.index), b = r.getTop().branch();
        final boolean j = a.joinable(b);                                        // Can we merge the two branches
        if (j)                                                                  // Merge the two branches
         {final Key k = r.getKey(0);
          a.join(b, k);
          a.setRoot();                                                          // New merged root
         }
       }
     }

    Node P = new Node();                                                        // We now know that the root is a branch

    for    (int i = 0; i < 999; ++i)                                            // Step down through tree to find the required leaf, splitting as we go
     {if (P.isLeaf()) break;                                                    // Stepped to a leaf
      final Branch p = P.branch();
      for(int j = 0; j < p.nKeys()-1; ++j)                                      // See if any pair under this node can be merged
       {final Node A = p.getNext(j);
        final Node B = p.getNext(j+1);
        if (A.isLeaf())                                                         // Both nodes are leaves
         {final Leaf a = A.leaf(), b = B.leaf();
          final boolean m = a.joinable(b);                                      // Can we merge the two leaves
          if (m)                                                                // Merge the two leaves
           {a.join(b);
            p.removeKey(j);
            p.removeNext(j+1);
           }
         }
        else                                                                    // Merge two branches
         {final Branch a = A.branch(), b = B.branch();
          final boolean m = a.joinable(b);                                      // Can we merge the two branches
          if (m)                                                                // Merge the two branches
           {final Key k = p.getKey(j); p.removeKey(j);
            a.join(b, k);
            p.removeNext(j+1);
           }
         }
       }

      if (!p.isEmpty())                                                         // Check last pair
       {final Node A = p.lastNext();
        if (A instanceof Leaf)
         {final Leaf a = A.leaf(), b = p.getTop().leaf();
          final boolean j = a.joinable(b);                                      // Can we merge the two leaves
          if (j)                                                                // Merge the two leaves
           {a.join(b);                                                          // Join the two leaves
            p.popKey();                                                         // Remove the last key from parent branch as this is the last pair that is being merged
            p.popNext();                                                        // Remove the last next from parent branch as this is the last pair that is being merged
            p.setTop(a.index);                                                  // The node to goto if the search key is greater than all keys in the branch
           }
         }
        else                                                                    // Merge two branches
         {final Branch a = A.branch(), b = p.getTop().branch();
          final boolean j = a.joinable(b);                                      // Can we merge the last two branches
          if (j)                                                                // Merge the last two branches
           {final Key k = p.popKey();
            a.join(b, k);
            p.popNext();
            p.setTop(a.index);
           }
         }
       }
      P = p.findFirstGreaterOrEqual(keyName);                                   // Find key position in branch
     }
    keyDataStored.dec();                                                        // Remove one entry  - we are on a leaf andf the entry is known to exist

    final Leaf l = P.leaf();                                                    // We know we are at the leaf
    final int  F = l.findIndexOfKey(keyName);                                   // Key is known to be present
    l.removeKey(F);
    l.removeData(F);

    return foundData;
   } // delete

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
    for  (StringBuilder s : S) a.append(s.toString()+"|\n");
    return a.toString();
   }

  String printHorizontally()                                                    // Print a tree horizontally
   {final Stack<StringBuilder> S = new Stack<>();

    if (emptyTree()) return "";                                                 // Empty tree
    S.push(new StringBuilder());

    if (new Node().isLeaf())                                                    // Root is a leaf
     {final Leaf lr = new Leaf(root.toInt());
      lr.printHorizontally(S, 0, false);
      return S.toString()+"\n";
     }

    final Branch b = new Branch(root.toInt());                                  // Root is a branch
    final Node btm = b.getTop();

    final int    N = b.nKeys();

    for (int i = 0; i < N; i++)                                                 // Nodes below root
     {final Node m = b.getNext(i);
      if (m.isLeaf())
       {m.leaf().printHorizontally(S, 1, false);
        S.firstElement().append(" "+b.getKey(i).toInt());
       }
      else
       {m.branch().printHorizontally(S, 1, false);
        S.firstElement().append(" "+b.getKey(i).toInt());
       }
     }

    if (btm.isLeaf())
     {final Leaf l = b.getTop().leaf();
      l.printHorizontally(S, 1, false);
     }
    else
     {final Branch B = b.getTop().branch();
      B.printHorizontally(S, 1, false);
     }
    return joinStrings(S);
   }

//D1 Tests                                                                      // Tests

  static void test_create_branch()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    final Branch b = m.new Branch(m.new Node(N+1));

    Memory.Variable key = Memory.variable("key", N);
    key.layout();
    ok( b.index, N-1);
    ok( b.isEmpty());
    ok(!b.isFull ());
    for (int i = 0; i < M-1; i++) b.put(m.key(i), m.new Node(i));
    ok(!b.isEmpty());
    ok( b.isFull ());
    b.ok("Branch_1(0:0, 1:1, 2:2, 3)");

    Branch c = b.split();
    ok(c.nKeys(), 1);
    ok(b.nKeys(), 1);
    ok(b.nKeys(), 1);
    b.ok("Branch_1(2:2, 3)");
    c.ok("Branch_0(0:0, 1)");
   }

  static void test_join_branch()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Branch j = m.new Branch(m.new Node(11));
    Branch k = m.new Branch(m.new Node(13));
    Branch l = m.new Branch(m.new Node(15));
    j.put(m.key(1), m.new Node(8)); ok( k.joinable(j));
    k.put(m.key(3), m.new Node(4)); ok( k.joinable(j));

    k.ok("Branch_2(3:4, 13)");
    j.ok("Branch_3(1:8, 11)");
    ok(k.nKeys(), 1);
    ok(j.nKeys(), 1);
    j.join(k, m.key(2));
    j.ok("Branch_3(1:8, 2:11, 3:4, 13)");
    ok(m.nodesFree.stuckSize(), 2);
   }

  static void test_create_leaf()
   {final int N = 2, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    final Leaf l = m.new Leaf();
    ok(l.index, N-1);

    Memory.Variable key = Memory.variable("key", N);
    key.layout();
    ok( l.isEmpty());
    ok(!l.isFull ());
    for (int i = 0; i < M; i++)
     {key.set(i);
      l.put(m. new Key(key.memory()), m.new Data(key.memory()));
     }
    ok(!l.isEmpty());
    ok( l.isFull ());
    l.ok("Leaf_1(0:0, 1:1, 2:2, 3:3)");

    Leaf k = l.split();
    ok(k.nKeys(), 2); ok(k.nData(), 2);
    ok(l.nKeys(), 2); ok(l.nKeys(), 2);
    k.ok("Leaf_0(0:0, 1:1)");
    l.ok("Leaf_1(2:2, 3:3)");
   }

  static void test_join_leaf()
   {final int N = 4, M = 4;
    Mjaf2 m = mjaf(N, N, M, N);
    Leaf j = m.new Leaf();
    Leaf k = m.new Leaf();
    Leaf l = m.new Leaf();
    j.put(m.key(1), m.data(8)); ok( k.joinable(j));
    j.put(m.key(2), m.data(6)); ok( k.joinable(j));
    k.put(m.key(3), m.data(4)); ok( k.joinable(j));
    k.put(m.key(4), m.data(2)); ok( k.joinable(j));
    l.put(m.key(4), m.data(8)); ok( k.joinable(l));
    l.put(m.key(3), m.data(6)); ok( k.joinable(l));
    l.put(m.key(2), m.data(4)); ok(!k.joinable(l));
    l.put(m.key(1), m.data(2)); ok(!k.joinable(l));

    k.ok("Leaf_2(3:4, 4:2)");
    j.ok("Leaf_3(1:8, 2:6)");
    ok(m.nodesFree.stuckSize(), 1);
    j.join(k);
    j.ok("Leaf_3(1:8, 2:6, 3:4, 4:2)");
    ok(m.nodesFree.stuckSize(), 2);
   }

  static void test_put()
   {final int N = 8, M = 4;
    Mjaf2 m = mjaf(N, N, M, 4*N);

    m.put(m.key(1), m.data(2));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), "[1]\n");

    m.put(m.key(2), m.data(4));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), "[1,2]\n");

    m.put(m.key(3), m.data(6));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), "[1,2,3]\n");

    m.put(m.key(4), m.data(8));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), "[1,2,3,4]\n");

    m.put(m.key(5), m.data(10));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2     |
1,2  3,4,5|
""");

    m.put(m.key(6), m.data(12));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2       |
1,2  3,4,5,6|
""");

    m.put(m.key(7), m.data(14));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2    4     |
1,2  3,4  5,6,7|
""");

    m.put(m.key(8), m.data(16));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2    4       |
1,2  3,4  5,6,7,8|
""");

    m.put(m.key(9), m.data(18));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2    4    6     |
1,2  3,4  5,6  7,8,9|
""");

    m.put(m.key(10), m.data(20));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
    2    4    6        |
1,2  3,4  5,6  7,8,9,10|
""");

    m.put(m.key(11), m.data(22));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4                 |
   2         6    8        |
1,2  3,4  5,6  7,8  9,10,11|
""");

    m.put(m.key(12), m.data(24));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4                    |
   2         6    8           |
1,2  3,4  5,6  7,8  9,10,11,12|
""");

    m.put(m.key(13), m.data(26));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4                         |
   2         6    8     10         |
1,2  3,4  5,6  7,8  9,10   11,12,13|
""");

    m.put(m.key(14), m.data(28));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4                            |
   2         6    8     10            |
1,2  3,4  5,6  7,8  9,10   11,12,13,14|
""");

    m.put(m.key(15), m.data(30));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4         8                       |
   2         6          10      12         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14,15|
""");

    m.put(m.key(16), m.data(32));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4         8                          |
   2         6          10      12            |
1,2  3,4  5,6  7,8  9,10   11,12   13,14,15,16|
""");

    m.put(m.key(17), m.data(34));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4         8                               |
   2         6          10      12      14         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16,17|
""");

    m.put(m.key(18), m.data(36));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
         4         8                                  |
   2         6          10      12      14            |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16,17,18|
""");

    m.put(m.key(19), m.data(38));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                   8                                       |
        4                       12                         |
   2         6          10              14      16         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16   17,18,19|
""");

    m.put(m.key(20), m.data(40));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                   8                                          |
        4                       12                            |
   2         6          10              14      16            |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16   17,18,19,20|
""");

    m.put(m.key(21), m.data(42));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                   8                                               |
        4                       12                                 |
   2         6          10              14      16      18         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16   17,18   19,20,21|
""");

    for (int i = 22; i < 32; i++) m.put(m.key(i), m.data(i<<2));

    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                   8                             16                                                        |
        4                       12                              20              24                         |
   2         6          10              14              18              22              26      28         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16   17,18   19,20   21,22   23,24   25,26   27,28   29,30,31|
""");
   }

  static void test_put2()
   {final int N = 8, M = 4;
    Mjaf2 m = mjaf(N, N, M, 4*N);

    for (int i = 0; i < 64; i++) m.put(m.key(i>>1), m.data(i));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                   7                           15                                                           |
        3                     11                              19              23                            |
   1         5         9              13              17              21              25      27            |
0,1  2,3  4,5  6,7  8,9  10,11   12,13   14,15   16,17   18,19   20,21   22,23   24,25   26,27   28,29,30,31|
""");
     ok(m.find(m.key( 9)).toInt() == 19);
     ok(m.find(m.key(10)).toInt() == 21);
     ok(m.find(m.key(32))         == null);
        m.put (m.key( 9), m.data(18));
     ok(m.find(m.key( 9)).toInt() == 18);
   }

  static void test_put_reverse()
   {final int N = 8, M = 4;
    Mjaf2 m = mjaf(N, N, M, 4*N);

    for (int i = 15; i > 0; --i)
     {m.put(m.key(i), m.data(i<<1));
     }
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                7           11             |
     3    5         9              13      |
1,2,3  4,5  6,7  8,9  10,11   12,13   14,15|
""");
     ok(m.find(m.key( 9)).toInt() == 18);
     ok(m.find(m.key(15)).toInt() == 30);
     ok(m.find(m.key(0))          == null);
   }

  static int[]random_array()                                                    // Random array
   {final int[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    return r;
   }

  static void test_put_random()
   {final int[] r = random_array();
    final Mjaf2 m = mjaf(12, 12, 4, r.length);
    for (int i = 0; i < r.length; ++i) m.put(m.key(r[i]), m.data(i));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                                                                                                                                                                                                                                                                   511                                                                                                                                                                                                                                    |
                                                                    186                                                                    317                                                                                                                                                                                                      658                                                                                                                                                   |
                                      103                                                         246                                                                                      403                                      472                                                                        578                                                                704                                              858                           912                                      |
       27      39         72                     135                                   234                   261            279                       344        358            391                   425                442                           501                       545        560                           611            650                           686                           806            830                           903                               961        987        |
1,13,27   29,39   43,55,72   90,96,103    106,135    151,155,157,186    188,229,232,234    237,246    260,261    272,273,279    288,298,317    338,344    354,358    376,377,391    401,403    422,425    436,437,438,442    447,472    480,490,494,501    503,511    516,526,545    554,560    564,576,577,578    586,611    612,615,650    657,658    667,679,681,686    690,704    769,773,804,806    809,826,830    839,854,858    882,884,903    906,907,912    922,937,946,961    976,987    989,993|
""");
    for (int i = 0; i < r.length; ++i) ok(m.find(m.key(r[i])), m.data(i));
    ok(m.find(m.key(r.length+1)) == null);
   }

  static Mjaf2 test_delete_one(int Print, int count, int delete, String e, String d)
   {final int N = 8, M = 4;
    final boolean print = Print > 0;
    Mjaf2 m = mjaf(N, N, M, 4*N);

    for (int i = 0; i < count; i++) m.put(m.key(i+1), m.data(2*(i+1)));
    if (print) stop(m.printHorizontally());
    test_delete_two(m, Print, delete, d);
    return m;
   }

  static void test_delete_two(Mjaf2 m, int Print, int delete, String d)
   {final int N = 8, M = 4;
    final boolean print = Print > 0;

    m.delete(m.key(delete));
    if (print) stop(m.printHorizontally(), d);
                 ok(m.printHorizontally(), d);
   }

  static void test_delete()
   {Mjaf2 m;
    test_delete_one(0, 1, 1, """
[1]
""", """
[]
""");
    test_delete_one(0, 2, 1, """
[1,2]
""", """
[2]
""");
    test_delete_one(0, 2, 2, """
[1,2]
""", """
[1]
""");
    test_delete_one(0, 3, 1, """
[1,2,3]
""", """
[2,3]
""");
    test_delete_one(0, 3, 2, """
[1,2,3]
""", """
[1,3]
""");
    test_delete_one(0, 3, 3, """
[1,2,3]
""", """
[1,2]
""");
    test_delete_one(0, 5, 1, """
    2     |
1,2  3,4,5|
""", """
  2     |
2  3,4,5|
""");
    m = test_delete_one(0, 5, 1, """
    2     |
1,2  3,4,5|
""", """
  2     |
2  3,4,5|
""");
    test_delete_two(m, 0, 2, """
[3,4,5]
""");
    m = test_delete_one(0, 7, 5, """
    2    4     |
1,2  3,4  5,6,7|
""", """
        4   |
1,2,3,4  6,7|
""");
    test_delete_two(m, 0, 2, """
      4   |
1,3,4  6,7|
""");
    test_delete_two(m, 0, 3, """
    4   |
1,4  6,7|
""");
    test_delete_two(m, 0, 1, """
[4,6,7]
""");

    m = test_delete_one(0, 31, 5, """
                   8                             16                                                        |
        4                       12                              20              24                         |
   2         6          10              14              18              22              26      28         |
1,2  3,4  5,6  7,8  9,10   11,12   13,14   15,16   17,18   19,20   21,22   23,24   25,26   27,28   29,30,31|
""", """
                                              16                                                        |
               8                                             20              24                         |
       4  6          10      12      14              18              22              26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18   19,20   21,22   23,24   25,26   27,28   29,30,31|
""");
    test_delete_two(m, 0, 20, """
                                              16                                                   |
               8                                                        24                         |
       4  6          10      12      14                 20      22              26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18,19   21,22   23,24   25,26   27,28   29,30,31|
""");
    test_delete_two(m, 0, 21, """
                8                             16                      24                        |
       4  6          10      12      14                 20   22              26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18,19   22   23,24   25,26   27,28   29,30,31|
""");
    test_delete_two(m, 0, 22, """
                8                             16                 24                        |
       4  6          10      12      14                 22              26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18,19   23,24   25,26   27,28   29,30,31|
""");
    test_delete_two(m, 0, 23, """
                8                             16              24                        |
       4  6          10      12      14                 22           26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18,19   24   25,26   27,28   29,30,31|
""");

    test_delete_two(m, 0, 24, """
                8                             16            24                        |
       4  6          10      12      14                 22         26      28         |
1,2,3,4  6  7,8  9,10   11,12   13,14   15,16   17,18,19      25,26   27,28   29,30,31|
""");

    test_delete_two(m, 0, 6, """
               8                             16            24                        |
       4 6          10      12      14                 22         26      28         |
1,2,3,4    7,8  9,10   11,12   13,14   15,16   17,18,19      25,26   27,28   29,30,31|
""");
   }

  static void oldTests()                                                        // Tests thought to be in good shape
   {test_create_branch();
    test_join_branch();
    test_create_leaf();
    test_join_leaf();
    test_put();
    test_put2();
    test_put_reverse();
    test_put_random();
    test_delete();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_delete();
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
