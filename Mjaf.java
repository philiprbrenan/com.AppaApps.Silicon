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

class Mjaf extends RiscV                                                       // Btree algorithm but with data stored only in the leaves.  The branches (interior nodes) have an odd number of keys to facilitate fission, whereas the leaves (exterior nodes) have even number of keys and matching number of data elements because data is not transferred to the parent on fission  which simplifies deletions with complicating insertions.
 {final int bitsPerKey;                                                         // Number of bits in key
  final int bitsPerData;                                                        // Number of bits in data
  final int maxKeysPerLeaf;                                                     // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches as whether classic Btree algorithm.
  final int maxKeysPerBranch;                                                   // The maximum number of keys per leaf.  This should be an even number greater than three. The maximum number of keys per branch is one less. The normal Btree algorithm requires an odd number greater than two for both leaves and branches.  The difference arises because we only store data in leaves not in leaves and branches as whether classic Btree algorithm.
  final NodeStack nodes = new NodeStack();                                      // All the branch nodes - this is our memory allocation scheme
  Node nodesFreeList = null;                                                    // Free nodes list

//D1 Construction                                                               // Create a Btree from nodes which can be branches or leaves.  The data associated with the Btree is stored only in the leaves opposite the keys

  Node root;                                                                    // The root node of the Btree
  int keyDataStored;                                                            // The number of key, data values stored in the Btree

  Mjaf(int BitsPerKey, int BitsPerData, int MaxKeysPerLeaf, int size)          // Define a Btree with a specified maximum number of keys per leaf.
   {final int N = MaxKeysPerLeaf;
    bitsPerKey  = BitsPerKey;
    bitsPerData = BitsPerData;
    if (N % 2 == 1) stop("# keys per leaf must be even not odd:", N);
    if (N     <= 3) stop("# keys per leaf must be greater than three, not:", N);
    maxKeysPerLeaf   = N;
    maxKeysPerBranch = N-1;
    for (int i = 0; i < size; i++) nodes.release(new Leaf());                   // Pre allocate leaves as they are bigger than branches
   }

  Mjaf() {this(16, 16, 4, 1000);}                                               // Define a Btree with a minimal maximum number of keys per node

  static Mjaf mjaf(int Key, int Data, int MaxKeysPerLeaf, int size)             // Define a Btree with a specified maximum number of keys per leaf.
   {return new Mjaf(Key, Data, MaxKeysPerLeaf, size);
   }

  int size() {return keyDataStored;}                                            // Number of entries in the tree

  int nodesCreated = 0;                                                         // Number of nodes created

  class BitString implements Comparable<BitString>                              // Definition of a key or data
   {final boolean[]bits;                                                        // Value of bit string
    BitString(int N) {bits = new boolean[N];}                                   // Create bit string
    int size() {return bits.length;}                                            // Width of bit string

    void set(String Bits)                                                       // Set a bit string from a character string
     {final  int w = size();
      final String b = cleanBoolean(Bits, w);
      for (int i = 0; i < w; i++) bits[i] = b.charAt(bitsPerKey-i-1) == '1';
     }

    void set(boolean[]Bits)                                                     // Set a bit string from a character string
     {final int w = size(), b = bits.length;
      if (b != w) stop("Bits have length", b, "but need", w);
      for (int i = 0; i < w; i++) bits[i] = Bits[i];
     }

    public int compareTo(BitString B)                                           // Compare two but strings
     {final int a = size(), b = B.size();
      if (a != b) stop("Bit strings have different sizes", a, b);
      for (int i = a-1; i >= 0; i--)
       {if (!bits[i] &&  B.bits[i]) return -1;
        if ( bits[i] && !B.bits[i]) return +1;
       }
      return 0;
     }

    public int getInt()                                                         // Get the value of a bit string as integer
     {final int a = size();
      int v = 0;
      for (int i = 0; i < a; i++) if (bits[i]) v += 1<<i;
      return v;
     }
   }

  class Key extends BitString                                                   // Definition of a key
   {Key() {super(bitsPerKey);}
    Key(String     Bits)   {this(); set(Bits);}
    Key(boolean[]  Bits)   {this(); set(Bits);}
    Key(int        Bits)   {this(); set(toBitString(Bits));}
    Key(long       Bits)   {this(); set(toBitString(Bits));}
   }
  Key key(String   Bits) {return new Key(Bits);}
  Key key(boolean[]Bits) {return new Key(Bits);}
  Key key(int      Bits) {return new Key(Bits);}
  Key key(long     Bits) {return new Key(Bits);}

  class Data extends BitString                                                  // Definition of data associated with a key
   {Data() {super(bitsPerData);}
    Data(String   Bits) {this(); set(Bits);}
    Data(boolean[]Bits) {this(); set(Bits);}
    Data(int      Bits) {this(); set(toBitString(Bits));}
    Data(long     Bits) {this(); set(toBitString(Bits));}
   }
  Data data(String   Bits) {return new Data(Bits);}
  Data data(boolean[]Bits) {return new Data(Bits);}
  Data data(int      Bits) {return new Data(Bits);}
  Data data(long     Bits) {return new Data(Bits);}

  abstract class Node implements Comparable<Node>                               // A branch or a leaf: an interior or exterior node. Comparable so we can place them in a tree or set by node number.
   {final Stuck keyNames;                                                       // Names of the keys in this branch or leaf
    final int nodeNumber = ++nodesCreated;                                      // Number of this node
    Node next = null;                                                           // Linked list of free nodes

    Node(int N) {keyNames = new Stuck(N, bitsPerKey);}                          // Create a node

    int findIndexOfKey     (Key keyToFind) {return keyNames.indexOf(keyNames.new Element(keyToFind.bits));}// Find the one based index of a key in a branch node or zero if not found
    boolean lessThanOrEqual(Key a, Key b)  {return a.compareTo(b) <= 0;}        // Define a new Btree of default type with a specified maximum number of keys per node

    int splitIdx() {return maxKeysPerBranch >> 1;}                              // Index of splitting key
    Key splitKey() {return new Key(keyNames.elementAt(splitIdx()).data);}       // Splitting key
    int size    () {return keyNames.size();}                                    // Number of elements in this leaf

    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check node is as expected

    abstract void printHorizontally(Stack<StringBuilder>S, int l, boolean d);   // Print horizontally
    void traverse(Stack<Node>S) {S.push(this);}                                 // Traverse tree placing all its nodes on a stack
    public int compareTo(Node B)                                                // make it possible to orde nodes
     {final Integer a = nodeNumber, b = B.nodeNumber;
      return a.compareTo(b);
     }
   }

  Stack<Node> traverse()                                                        // Traverse tree placing all its nodes on a stack
   {final Stack<Node> S = new Stack<>();
    if (root != null) root.traverse(S);
    return S;
   }

  class Branch extends Node                                                     // A branch node directs the search to the appropriate leaf
   {final Stuck nextLevel;
    Node topNode;

    Branch()                                                                    // Create a new branch
     {super(maxKeysPerBranch);
      nextLevel = new Stuck(maxKeysPerBranch, bitsPerKey);                      // The number of keys in a branch is one less than the number of keys in a leaf
     }

    void clear() {topNode = null; keyNames.clear(); nextLevel.clear();}         // Initialize branch keys and next

    boolean branchIsFull() {return nextLevel.isFull();}                         // Node should be split

    void splitRoot()                                                            // Split the root
     {if (branchIsFull())
       {final Key    k = splitKey();
        final Branch l = splitBranch(), b = branch(this);
        b.putBranch(k, l);
        root = b;
       }
     }

    Branch splitBranch()                                                        // Split a branch into two branches at the indicated key
     {final int K = keyNames.size(), f = splitIdx();                            // Number of keys currently in node
      if (f < K-1) {} else stop("Split", f, "too big for branch of size:", K);
      if (f <   1)         stop("First", f, "too small");
      final Node t = nextLevel.elementAt(f);                               // Top mode
      final Branch    b = branch(t);                                            // Recycle a branch

      for (int i = 0; i < f; i++)                                               // Remove first keys from old node to new node
       {final Key       k = keyNames .removeElementAt(0);
        final Node n = nextLevel.removeElementAt(0);
        b.keyNames .push(k);
        b.nextLevel.push(n);
       }

      keyNames .removeElementAt(0);                                             // Remove central key which is no longer required
      nextLevel.removeElementAt(0);
      return b;
     }

    boolean joinableBranches(Branch a)                                          // Check that we can join two branches
     {return size() + a.size() + 1 <= maxKeysPerBranch;
     }

    void joinBranch(Branch Join, Key joinKeyName)                               // Append the second branch to the first one adding the specified key
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (K + 1 + J > maxKeysPerBranch) stop("Join of branch has too many keys",
          K,"+1+",J, "greater than", maxKeysPerBranch);

      keyNames .push(joinKeyName);                                              // Key to separate two halves
      nextLevel.push(topNode); topNode = Join.topNode;                          // Current top node becomes middle of new node

      for (int i = 0; i < J; i++)                                               // Add right hand branch
       {final Key       k = Join.keyNames .elementAt(i);
        final Node n = Join.nextLevel.elementAt(i);
        keyNames .push(k);
        nextLevel.push(n);
       }
      nodes.release(Join);
     }

    Node findFirstGreaterOrEqual(Key keyName)                                   // Find first key which is greater an the search key. The result is 1 based, a result of zero means all the keys were less than or equal than the search key
     {final int N = size();                                                     // Number of keys currently in node
      for (int i = 0; i < N; i++)                                               // Check each key
       {final Key k = keyNames.elementAt(i);                                    // Key
        final boolean l = lessThanOrEqual(keyName, k);                          // Compare current key with search key
        if (l) return nextLevel.elementAt(i);                                   // Current key is greater than or equal to the search key
       }
      return topNode;
     }

    void putBranch(Key keyName, Node putNode)                                   // Insert a new node into a branch
     {final int N = nextLevel.size();                                           // Number of keys currently in node
      if (N >= maxKeysPerLeaf) stop("Too many keys in Branch");
      for (int i = 0; i < N; i++)                                               // Check each key
       {final Key k = keyNames.elementAt(i);                                    // Key
        final boolean l = lessThanOrEqual(keyName, k);                          // Compare current key with search key
        if (l)                                                                  // Insert new key in order
         {keyNames .insertElementAt(keyName, i);
          nextLevel.insertElementAt(putNode, i);
          return;
         }
       }
      keyNames .push(keyName);                                                  // Either the leaf is empty or the new key is greater than every existing key
      nextLevel.push(putNode);
     }

    public String toString()                                                    // Print branch
     {final StringBuilder s = new StringBuilder("Branch(");
      final int K = keyNames.size();

      for (int i = 0; i < K; i++)                                               // Keys and next level indices
        s.append(""+keyNames.elementAt(i).getInt()+":"+
          nextLevel.elementAt(i).nodeNumber+", ");

      s.append(""+topNode.nodeNumber+")");
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print branch horizontally
     {for (int i = 0; i < size(); i++)
       {nextLevel.elementAt(i).printHorizontally(S, level+1, debug);
        padStrings(S, level);
        S.elementAt(level).append(keyNames.elementAt(i));
       }
      topNode.printHorizontally(S, level+1, debug);
     }

    void ok(String expected) {Mjaf.ok(toString(), expected);}                   // Check leaf

    void traverse(Stack<Node>S)                                            // Traverse tree placing all its nodes on a stack
     {super.traverse(S);
      for (int i = 0; i < size(); i++) nextLevel.elementAt(i).traverse(S);
      topNode.traverse(S);
     }

    class Layout
     {Variable nextLevel = variable ("nextLevel", 4);
      Variable  b1 = variable ("b1", 4);
      Variable  c1 = variable ("c1", 2);
      Array     C1 = array    ("C1", c1, 10);
      Structure s1 = structure("inner1", b1, C1);
    }

   } // Branch

  Branch branch(Node node) {return nodes.recycleBranch(node);}                  // Create a new branch

  class Leaf extends Node                                                       // Create a new leaf
   {final Stuck dataValues;                                                     // Data associated with each key
    Leaf()                                                                      // Data associated with keys in leaf
     {super(maxKeysPerLeaf);
      dataValues = new Stuck<Data>(maxKeysPerLeaf);
     }

    void clear() {keyNames.clear(); dataValues.clear();}                        // Clear leaf

    boolean leafIsFull() {return dataValues.isFull();}                          // Leaf is full

    void putLeaf(Key keyName, Data dataValue)                                   // Insert a new leaf value
     {final int K = keyNames.size();                                            // Number of keys currently in node
      if (K >= maxKeysPerLeaf) stop("Too many keys in leaf");

      for (int i = 0; i < K; i++)
       {final Key k = keyNames.elementAt(i);                                    // Current key
        if (lessThanOrEqual(keyName, k))                                        // Insert new key in order
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

    Leaf splitLeaf()                                                            // Split the leaf into two leafs - the new leaf consists of the indicated first elements, the old leaf retains the rest
     {final int K = size(), f = maxKeysPerLeaf/2;                               // Number of keys currently in node
      if (f < K) {} else stop("Split", f, "too big for leaf of size:", K);
      if (f < 1)         stop("First", f, "too small");
      final Leaf l = leaf();
      for (int i = 0; i < f; i++)                                               // Transfer keys and data
       {final Key  k = keyNames  .removeElementAt(0);                           // Current key
        final Data d = dataValues.removeElementAt(0);                           // Current data
        l.keyNames  .push(k);                                                   // Transfer keys
        l.dataValues.push(d);                                                   // Transfer data
       }
      return l;                                                                 // Split out leaf
     }

    boolean joinableLeaves(Leaf a)                                              // Check that we can join two leaves
     {return size() + a.size() <= maxKeysPerLeaf;
     }

    void joinLeaf(Leaf Join)                                                    // Join the specified leaf onto the end of this leaf
     {final int K = size(), J = Join.size();                                    // Number of keys currently in node
      if (!joinableLeaves(Join)) stop("Join of leaf has too many keys", K,
        "+", J, "greater than", maxKeysPerLeaf);

      for (int i = 0; i < J; i++)
       {final Key  k = Join.keyNames  .elementAt(i);
        final Data d = Join.dataValues.elementAt(i);
        keyNames  .push(k);
        dataValues.push(d);
       }
      nodes.release(Join);
     }

    public String toString()                                                    // Print leaf
     {final StringBuilder s = new StringBuilder();
      s.append("Leaf(");
      final int K = keyNames.size();
      for  (int i = 0; i < K; i++)
        s.append(""+keyNames.elementAt(i).getInt()+":"+
                  dataValues.elementAt(i).getInt()+", ");
      if (K > 0) s.setLength(s.length()-2);
      s.append(")");
      return s.toString();
     }

    public String shortString()                                                 // Print a leaf compactly
     {final StringBuilder s = new StringBuilder();
      final int K = keyNames.size();
      for  (int i = 0; i < K; i++) s.append(""+keyNames.elementAt(i).getInt()+",");
      if (K > 0) s.setLength(s.length()-1);
      return s.toString();
     }

    void printHorizontally(Stack<StringBuilder>S, int level, boolean debug)     // Print leaf  horizontally
     {padStrings(S, level);
      S.elementAt(level).append(debug ? toString() : shortString());
      padStrings(S, level);
     }

    void traverse(Stack<Node>S)                                                 // Traverse tree placing all its nodes on a stack
     {super.traverse(S);
     }
   } // Leaf

  Leaf leaf() {return nodes.recycleLeaf();}                                     // Create an empty leaf

//D1 Search                                                                     // Find a key, data pair

  Data find(Key keyName)                                                   // Find a the data associated with a key
   {if (root == null) return null;                                              // Empty tree
    Node q = root;                                                             // Root of tree
    for(int i = 0; i < 999 && q != null; ++i)                                   // Step down through tree up to some reasonable limit
     {if (!(q instanceof Branch)) break;                                        // Stepped to a leaf
      q = ((Branch)q).findFirstGreaterOrEqual(keyName);                         // Position of key
     }

    final int f = q.findIndexOfKey(keyName);                                    // We have arrived at a leaf
    return f == -1 ? null : ((Leaf)q).dataValues.elementAt(f);                  // Key not found or data associated with key in leaf
   }

  boolean findAndInsert(Key keyName, Data dataValue)                            // Find the leaf for a key and insert the indicated key, data pair into if possible, returning true if the insertion was possible else false.
   {if (root == null)                                                           // Empty tree so we can insert directly
     {root = leaf();                                                            // Create the root as a leaf
      ((Leaf)root).putLeaf(keyName, dataValue);                                 // insert key, data pair in the leaf
      return true;                                                              // Successfully inserted
     }

    Node q = root;                                                         // Root of tree
    for(int i = 0; i < 999 && q != null; ++i)                                   // Step down through tree up to some reasonable limit
     {if (!(q instanceof Branch)) break;                                        // Stepped to a leaf
      q = ((Branch)q).findFirstGreaterOrEqual(keyName);                         // Position of key
     }

    final int g = q.findIndexOfKey(keyName);                                    // We have arrived at a leaf
    final Leaf l = (Leaf)q;
    if (g != -1) l.dataValues.setElementAt(dataValue, g);                       // Key already present in leaf
    else if (l.leafIsFull()) return false;                                      // There's no room in the leaf so return false
    l.putLeaf(keyName, dataValue);                                              // On a leaf that is not full so we can insert directly
    return true;                                                                // inserted directly
   }

//D1 Insertion                                                                  // Insert keys and data into the Btree

  void put(Key keyName, Data dataValue)                                         // Insert a new key, data pair into the Btree
   {if (findAndInsert(keyName, dataValue)) return;                              // Do a fast insert if possible, thisis increasingly likely in trees with large leaves
    if (root instanceof Leaf)                                                   // Insert into root as a leaf
     {if (!((Leaf)root).leafIsFull()) ((Leaf)root).putLeaf(keyName, dataValue); // Still room in the root while it is is a leaf
      else                                                                      // Insert into root as a leaf which is full
       {final Leaf   l = ((Leaf)root).splitLeaf();                              // New left hand side of root
        final Key    k = l.splitKey();                                          // Splitting key
        final Branch b = branch(root);                                          // New root with old root to right
        b.putBranch(k, l);                                                      // Insert left hand node all of whose elements are less than the first element of what was the root
        final Leaf f = l.lessThanOrEqual(keyName, k) ? l : (Leaf)root;          // Choose leaf
        f.putLeaf(keyName, dataValue);                                          // Place in leaf
        root = b;                                                               // The root now has just one entry in it - the splitting eky
       }
      return;
     }
    else ((Branch)root).splitRoot();                                            // Split full root which is a branch not a leaf

    Branch p = (Branch)root; Node q = p;                                   // The root has already been split so the parent child relationship will be established

    for(int i = 0; i < 999; ++i)                                                // Step down through tree to find the required leaf, splitting as we go
     {if (!(q instanceof Branch)) break;                                        // Stepped to a branch

      if (((Branch)q).branchIsFull())                                           // Split the branch because it is full and we might need to insert below it requiring a slot in this node
       {final Key    k = q.splitKey();                                          // Splitting key
        final Branch l = ((Branch)q).splitBranch();                             // New lower node
        p.putBranch(k, l);                                                      // Place splitting key in parent
        ((Branch)root).splitRoot();                                             // Root might need to be split to re-establish the invariants at start of loop
        if (q.lessThanOrEqual(keyName, k)) q = l;                               // Position on lower node if search key is less than splitting key
       }

      p = (Branch)q;                                                            // Step parent down
      q = p.findFirstGreaterOrEqual(keyName);                                   // The node does not need splitting
     }

    final Leaf l = (Leaf)q;
    final int  g = l.findIndexOfKey(keyName);                                   // Locate index of key
    if (g != -1) l.dataValues.setElementAt(dataValue, g);                       // Key already present in leaf
    else if (l.leafIsFull())                                                    // Split the node because it is full and we might need to insert below it requiring a slot in this node
     {final Key  k = l.splitKey();
      final Leaf e = l.splitLeaf();
      p.putBranch(k, e);
      if (p.lessThanOrEqual(keyName, k)) e.putLeaf(keyName, dataValue);         // Insert key in the appropriate split leaf
      else                               l.putLeaf(keyName, dataValue);
     }
    else l.putLeaf(keyName, dataValue);                                         // On a leaf that is not full so we can insert directly
   } // put

//D1 Deletion                                                                   // Delete a key from a Btree

  Data delete(Key keyName)                                                      // Delete a key from a tree
   {if (root == null) return null;                                              // The tree is empty
    final Data foundData = find(keyName);                                       // Find the data associated with the key
    if (foundData == null) return null;                                         // The key is not present so cannot be deleted

    if (root instanceof Leaf)                                                   // Delete from root as a leaf
     {final Leaf r = (Leaf)root;
      final int  i = r.findIndexOfKey(keyName);                                 // Only one leaf and the key is known to be in the Btree so it must be in this leaf
      r.keyNames  .removeElementAt(i);
      r.dataValues.removeElementAt(i);
      --keyDataStored;

      return foundData;
     }

    if (root.size() == 1)                                                       // If the root is a branch and only has one key so we might be able to merge its children
     {final Branch r = (Branch)root;
      final Node A = r.nextLevel.firstElement();

      if (A instanceof Leaf)
       {final Leaf    a = (Leaf)A, b = (Leaf)(r.topNode);
        final boolean j = a.joinableLeaves(b);                                  // Can we merge the two leaves
        if (j)                                                                  // Merge the two leaves
         {a.joinLeaf(b);
          root = a;                                                             // New merged root
         }
       }
      else                                                                      // Merge two branches under root
       {final Branch  a = (Branch)A, b = (Branch)(r.topNode);
        final boolean j = a.joinableBranches(b);                                // Can we merge the two branches
        if (j)                                                                  // Merge the two branches
         {final Key k = r.keyNames.firstElement();
          a.joinBranch(b, k);
          root = a;                                                             // New merged root
         }
       }
     }

    Node P = root;                                                         // We now know that the root is a branch

    for    (int i = 0; i < 999; ++i)                                            // Step down through tree to find the required leaf, splitting as we go
     {if (!(P instanceof Branch)) break;                                        // Stepped to a branch
      final Branch p = (Branch)P;
      for(int j = 0; j < p.size()-1; ++j)                                       // See if any pair under this node can be merged
       {final Node A = p.nextLevel.elementAt(j);
        final Node B = p.nextLevel.elementAt(j+1);
        if (A instanceof Leaf)
         {final Leaf a = (Leaf)A, b = (Leaf)B;
          final boolean m = a.joinableLeaves(b);                                // Can we merge the two leaves
          if (m)                                                                // Merge the two leaves
           {a.joinLeaf(b);
            p.keyNames .removeElementAt(j);
            p.nextLevel.removeElementAt(j+1);
           }
         }
        else                                                                    // Merge two branches
         {final Branch a = (Branch)A, b = (Branch)B;
          final boolean m = a.joinableBranches(b);                              // Can we merge the two branches
          if (m)                                                                // Merge the two branches
           {final Key k = p.keyNames.removeElementAt(j);
            a.joinBranch(b, k);
            p.nextLevel.removeElementAt(j+1);
           }
         }
       }

      if (p.size() > 0)                                                         // Check last pair
       {final Node A = p.nextLevel.lastElement();
        if (A instanceof Leaf)
         {final Leaf a = (Leaf)A, b = (Leaf)p.topNode;
          final boolean j = a.joinableLeaves(b);                                // Can we merge the two leaves
          if (j)                                                                // Merge the two leaves
           {a.joinLeaf(b);
            p.keyNames .pop();
            p.nextLevel.pop();
            p.topNode = a;
           }
         }
        else                                                                    // Merge two branches
         {final Branch a = (Branch)A, b = (Branch)p.topNode;
          final boolean j = a.joinableBranches(b);                              // Can we merge the last two branches
          if (j)                                                                // Merge the last two branches
           {final Key k = p.keyNames.pop();
            a.joinBranch(b, k);
            p.nextLevel.pop();
            p.topNode = a;
           }
         }
       }
      P = p.findFirstGreaterOrEqual(keyName);                                   // Find key position in branch
     }
    --keyDataStored;                                                            // Remove one entry  - we are on a leaf andf the entry is known to exist
    final int  F = P.findIndexOfKey(keyName);                                   // Key is known to be present
           P .keyNames  .removeElementAt(F);
    ((Leaf)P).dataValues.removeElementAt(F);
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
    for (StringBuilder s : S) a.append(s.toString()+"|\n");
    return a.toString();
   }

  String printHorizontally()                                                    // Print a tree horizontally
   {final Stack<StringBuilder> S = new Stack<>();
    if (root == null) return "";                                                // Empty tree
    S.push(new StringBuilder());

    if (root instanceof Leaf)                                                   // Root is a leaf
     {((Leaf)root).printHorizontally(S, 0, false);
      return S.toString()+"\n";
     }

    final Branch b = (Branch)root;                                              // Root is a branch
    final int    N = b.size();
    for (int i = 0; i < N; i++)                                                 // Nodes below root
     {b.nextLevel.elementAt(i).printHorizontally(S, 1, false);
      S.firstElement().append(" "+b.keyNames.elementAt(i));
     }
    b.topNode.printHorizontally(S, 1, false);
    return joinStrings(S);
   }

//D1 Memory                                                                     // Preallocated memory for branches and leaves

  class NodeStack                                                               // Memory for branches and leaves
   {Integer size = 0, max = null, min = null;                                   // Statistics

    void release(Node n)                                                   // Release a branch
     {n.next = nodesFreeList; nodesFreeList = n;                                // Add node to free list
      ++size;
      final int m = size;
      if (max == null) max = m; else max = max(max, m);
     }

    Node recycle()                                                         // Recycle a node
     {if (nodesFreeList == null) stop("No more memory for branches/leaves");
      final int m = size - 1;
      if (min == null) min = m; else min = min(min, m);
      final Node n = nodesFreeList; nodesFreeList = n.next;                // Remove node from free list
      --size; n.next = null;                                                    // Clear free list entry
      return n;
     }

    Branch recycleBranch(Node node)                                        // Recycle a branch
     {recycle();
      final Branch b = new Branch();                                            // In Java we have to create a new object
      b.clear();                                                                // Pointless in Java but in assembler we will need to do this
      b.topNode = node;
      return b;
     }

    Leaf recycleLeaf()                                                          // Recycle a leaf
     {recycle();
      final Leaf l = new Leaf();                                                // In Java we have to create a new object
      l.clear();                                                                // Pointless in Java but in assembler we will need to do this
      return l;
     }

    public String toString()                                                    // Print statistics
     {return String.format("Nodes min: %4d, current: %4d, max: %4d", min, size, max);
     }
   }

//D0 Tests                                                                      // Test the BTree

  static void test_create()
   {var m = mjaf(12, 12, 6, 5);
    var l = m.leaf();
    l.putLeaf(m.key(2), m.data( 4));
    l.putLeaf(m.key(4), m.data( 8));
    l.putLeaf(m.key(1), m.data( 1));
    l.putLeaf(m.key(3), m.data( 6));
    l.putLeaf(m.key(5), m.data(10));
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10)");
    ok(l.size(),        5);
    ok(l.findIndexOfKey(m.key(3)), 2);
    ok(l.findIndexOfKey(m.key(9)), -1);
    ok(m.keyDataStored, 5);
   }

  static void test_leaf_split()
   {var m = mjaf(12, 12, 6, 5);
    var l = m.leaf();
    l.putLeaf(m.key(2), m.data(4));
    l.putLeaf(m.key(4), m.data(8));
    l.putLeaf(m.key(1), m.data(1));
    l.putLeaf(m.key(3), m.data(6));
    l.putLeaf(m.key(5), m.data(10));
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10)");
   }

  static void test_leaf_split_in_half()
   {var m = mjaf(12, 12, 6, 5);
    var l = m.leaf();
    l.putLeaf(m.key(2), m.data(4));
    l.putLeaf(m.key(4), m.data(8));
    l.putLeaf(m.key(1), m.data(1));
    l.putLeaf(m.key(3), m.data(6));
    l.putLeaf(m.key(5), m.data(10));
    l.putLeaf(m.key(6), m.data(12));
    l.ok("Leaf(1:1, 2:4, 3:6, 4:8, 5:10, 6:12)");
    var k = l.splitLeaf();
    k.ok("Leaf(1:1, 2:4, 3:6)");
    l.ok("Leaf(4:8, 5:10, 6:12)");
   }

  static void test_branch()
   {var m = mjaf(12, 12, 6, 8);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    Mjaf.Branch B = m.branch(f);
    B.putBranch(m.key(1), a); ok(B.branchIsFull(), false);
    B.putBranch(m.key(2), b); ok(B.branchIsFull(), false);
    B.putBranch(m.key(3), c); ok(B.branchIsFull(), false);
    B.putBranch(m.key(4), d); ok(B.branchIsFull(), false);
    B.putBranch(m.key(5), e); ok(B.branchIsFull(), true);
    //B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");

    ok(B.findIndexOfKey(m.key(3)), 2);
    ok(B.splitKey(), 3);
    ok(B.size(), 5);
   }

  static void test_branch_full()
   {var m = mjaf(12, 12, 6, 8);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    Mjaf.Branch B = m.branch(f);
    B.putBranch(m.key(1), a);
    B.putBranch(m.key(2), b);
    B.putBranch(m.key(3), c);
    B.putBranch(m.key(4), d);
    B.putBranch(m.key(5), e);
    //B.ok("Branch(1:1, 2:2, 3:3, 4:4, 5:5, 6)");
    ok(B.splitKey(), 3);
    ok(B.branchIsFull(), true);
    ok(B.size(), 5);
    Mjaf.Branch C = B.splitBranch();
    //C.ok("Branch(1:1, 2:2, 3)");
    //B.ok("Branch(4:4, 5:5, 6)");
   }

  static void test_pad_strings()
   {final Stack<StringBuilder> S = new Stack<>();
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
   {var m = mjaf(12, 12, 6, 6);
    var a = m.leaf();
    var b = m.leaf();
    var c = m.leaf();
    var d = m.leaf();
    var e = m.leaf();
    var f = m.leaf();
    var g = m.leaf();
    Mjaf.Branch B = m.branch(g);
    B.putBranch(m.key( 2), a);
    B.putBranch(m.key( 4), b);
    B.putBranch(m.key( 6), c);
    B.putBranch(m.key( 8), d);
    B.putBranch(m.key(10), e);
    B.putBranch(m.key(12), f);
    B.ok("Branch(2:1, 4:2, 6:3, 8:4, 10:5, 12:6, 7)");

    ok(B.findFirstGreaterOrEqual(m.key(4)), 2);
    ok(B.findFirstGreaterOrEqual(m.key(3)), 2);
    ok(B.findFirstGreaterOrEqual(m.key(2)), 1);
   }

  static void test_insert(int N, boolean debug, String expected)
   {var m = mjaf(12, 12, 4, N<<1);
    for (long i = 0; i < N; i++) m.put(m.key(i), m.data(i<<1));
    if (debug) say(m.printHorizontally());
    ok(m.printHorizontally(), expected);
    if (debug) stop("Debugging stopped");
   }

  static void test_insert_reverse(int N, boolean debug, String expected)
   {var m = mjaf(12, 12, 4, N<<1);
    for (long i = N; i >= 0; i--) m.put(m.key(i), m.data(i<<1));
    if (debug) say(m.printHorizontally());
    ok(m.printHorizontally(), expected);
    if (debug) stop("Debugging stopped");
   }

  static void test_insert()
   {if (true) test_insert(9, !true, """
    1    3    5     |
0,1  2,3  4,5  6,7,8|
""");

    if (true) test_insert(10, !true, """
    1    3    5       |
0,1  2,3  4,5  6,7,8,9|
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
        3    5    7   |
0,1,2,3  4,5  6,7  8,9|
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
                    9            |
       3   5   7          11     |
0,1,2,3 4,5 6,7 8,9  10,11  12,13|
""");
   }

  static long[]random_array()                                                   // Random array
   {final long[]r = {27, 442, 545, 317, 511, 578, 391, 993, 858, 586, 472, 906, 658, 704, 882, 246, 261, 501, 354, 903, 854, 279, 526, 686, 987, 403, 401, 989, 650, 576, 436, 560, 806, 554, 422, 298, 425, 912, 503, 611, 135, 447, 344, 338, 39, 804, 976, 186, 234, 106, 667, 494, 690, 480, 288, 151, 773, 769, 260, 809, 438, 237, 516, 29, 376, 72, 946, 103, 961, 55, 358, 232, 229, 90, 155, 657, 681, 43, 907, 564, 377, 615, 612, 157, 922, 272, 490, 679, 830, 839, 437, 826, 577, 937, 884, 13, 96, 273, 1, 188};
    return r;
   }

  static void test_insert_random()
   {final long[]r = random_array();
    var m = mjaf(12, 12, 4, r.length);
    for (int i = 0; i < r.length; ++i) m.put(m.key(r[i]), m.data(i));
    //stop(m.printHorizontally());
    ok(m.printHorizontally(), """
                                                                                                                                                                                                                                                511                                                                                                                                                                                                                     |
                                                               186                                                               317                                                                                                                                                                                        658                                                                                                                                         |
                                   103                                                     246                                                                               403                                   472                                                                    578                                                           704                                           858                         912                                   |
       27     39        72                   135                                 234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
    if (github_actions) for (int i = 0; i < r.length; ++i) ok(m.find(m.key(r[i])), m.data(i));
    ok(m.find(m.key(r.length+1l)), null);
   }

  static void test_find()
   {final int N = 64;
    var m = mjaf(12, 12, 4, N<<1);
    for (long i = 0; i < N; i++) m.put    (m.key(i), m.data(i<<1));
    for (long i = 0; i < N; i++) ok(m.find(m.key(i)), m.data(i+i));
   }

  static void test_find_reverse()
   {final int N = 64;
    var m = mjaf(12, 12, 4, N<<1);
    for (long i = N; i >= 0; i--) m.put    (m.key(i),  m.data(i<<1));
    for (long i = N; i >= 0; i--) ok(m.find(m.key(i)), m.data(i+i));
   }

  static void test_delete()
   {final long[]r = random_array();
    var m = mjaf(12, 12, 4, r.length);
    for (int i = 0; i < r.length; ++i) m.put(m.key(r[i]), m.data(i));
    for (int i = 0; i < r.length; ++i)
     {var a = m.delete(m.key(r[i]));
      ok(a, (long)i);
      if (!true)                                                                // Write case statement to check deletions
       {say("        case", i, "-> /*", r[i], "*/ ok(m.printHorizontally(), \"\"\"");
        say(m.printHorizontally());
        say("\"\"\");");
       }
      else switch(i) {
        case 0 -> /* 27 */ ok(m.printHorizontally(), """
                                                                                                                                                                                                                                             511                                                                                                                                                                                                                     |
                                                                                                                              317                                                                                                                                                                                        658                                                                                                                                         |
                                103                                                     246                                                                               403                                   472                                                                    578                                                           704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 1 -> /* 442 */ ok(m.printHorizontally(), """
                                                                                                                               317                                                                                                        511                                                                          658                                                                                                                                         |
                                103                                                     246                                                                                403                               472                                                                    578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                      344       358           391                 425           442                         501                      545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 2 -> /* 545 */ ok(m.printHorizontally(), """
                                                                                                                               317                                                                                                        511                                                                      658                                                                                                                                         |
                                103                                                     246                                                                                403                               472                                                                578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                      344       358           391                 425           442                         501                  545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511    516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 3 -> /* 317 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                        511                                                                      658                                                                                                                                         |
                                103                                                     246                                                                            403                               472                                                                578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442                         501                  545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503,511    516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 4 -> /* 511 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                    511                                                                      658                                                                                                                                         |
                                103                                                     246                                                                            403                               472                                                            578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442                         501              545       560                         611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 5 -> /* 578 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                                    511                                                                658                                                                                                                                         |
                                103                                                     246                                                                            403                               472                                                      578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                  344       358           391                 425           442                         501                      560                     611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344   354,358   376,377,391   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 6 -> /* 391 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                              511                                                                658                                                                                                                                         |
                                103                                                     246                                                                      403                               472                                                      578                                                            704                                           858                         912                                   |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                         501                      560                     611           650                          686                         806           830                         903                             961       987       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 7 -> /* 993 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                              511                                                                658                                                                                                                                   |
                                103                                                     246                                                                      403                               472                                                      578                                                            704                                           858                         912                             |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                         501                      560                     611           650                          686                         806           830                         903                             961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 8 -> /* 858 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                              511                                                                658                                                                                                                               |
                                103                                                     246                                                                      403                               472                                                      578                                                            704                                       858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                         501                      560                     611           650                          686                         806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526,554,560   564,576,577   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 9 -> /* 586 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                              511                                                            658                                                                                                                               |
                                103                                                     246                                                                      403                               472                                                      578                                                        704                                       858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                         501                      560                 611           650                          686                         806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447,472   480,490,494,501   503    516,526,554,560   564,576,577   611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 10 -> /* 472 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                          511                                                            658                                                                                                                               |
                                103                                                     246                                                                      403                           472                                                      578                                                        704                                       858                                                         |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                     501                      560                 611           650                          686                         806           830                     903           912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   906,907,912   922,937,946,961   976,987,989|
""");
        case 11 -> /* 906 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                          511                                                            658                                                                                                                           |
                                103                                                     246                                                                      403                           472                                                      578                                                        704                                       858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                     501                      560                 611           650                          686                         806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 12 -> /* 658 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                          511                                                      658                                                                                                                           |
                                103                                                     246                                                                      403                           472                                                      578                                                  704                                       858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                     501                      560                             650                      686                         806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 13 -> /* 704 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                          511                                                      658                                                                                                                       |
                                103                                                     246                                                                      403                           472                                                      578                                              704                                       858                                                     |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                     501                      560                             650                      686                     806           830                     903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   882,884,903   907,912   922,937,946,961   976,987,989|
""");
        case 14 -> /* 882 */ ok(m.printHorizontally(), """
                                                                                                                           317                                                                                          511                                                      658                                                                                                                   |
                                103                                                     246                                                                      403                           472                                                      578                                              704                                       858                                                 |
    27     39        72                   135               186               234                 261           279                          358       391                 425           442                     501                      560                             650                      686                     806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237,246   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 15 -> /* 246 */ ok(m.printHorizontally(), """
                                                                                                                       317                                                                                          511                                                      658                                                                                                                   |
                                103                                                 246                                                                      403                           472                                                      578                                              704                                       858                                                 |
    27     39        72                   135               186               234             261           279                          358       391                 425           442                     501                      560                             650                      686                     806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,261   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 16 -> /* 261 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                          511                                                      658                                                                                                                   |
                                103                                                 246                                                                  403                           472                                                      578                                              704                                       858                                                 |
    27     39        72                   135               186               234         261           279                          358       391                 425           442                     501                      560                             650                      686                     806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494,501   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 17 -> /* 501 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                      511                                                      658                                                                                                                   |
                                103                                                 246                                                                  403                           472                                                  578                                              704                                       858                                                 |
    27     39        72                   135               186               234         261           279                          358       391                 425           442                 501                      560                             650                      686                     806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,354,358   376,377   401,403   422,425   436,437,438   447   480,490,494   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 18 -> /* 354 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                511                                                      658                                                                                                                   |
                                103                                                 246                                                            403                           472                                                  578                                              704                                       858                                                 |
    27     39        72                   135               186               234         261           279                      358                         425           442                 501                      560                             650                      686                     806           830                 903       912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,903   907,912   922,937,946,961   976,987,989|
""");
        case 19 -> /* 903 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                511                                                      658                                                                                                             |
                                103                                                 246                                                            403                           472                                                  578                                              704                                       858                                           |
    27     39        72                   135               186               234         261           279                      358                         425           442                 501                      560                             650                      686                     806           830                     912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839,854   884,907,912   922,937,946,961   976,987,989|
""");
        case 20 -> /* 854 */ ok(m.printHorizontally(), """
                                                                                                                   317                                                                                511                                                      658                                                                                                         |
                                103                                                 246                                                            403                           472                                                  578                                              704                                   858                                           |
    27     39        72                   135               186               234         261           279                      358                         425           442                 501                      560                             650                      686                     806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260   272,273,279   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 21 -> /* 279 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                511                                                      658                                                                                                         |
                                103                                                 246                                                      403                           472                                                  578                                              704                                   858                                           |
    27     39        72                   135               186               234                 279                      358                         425           442                 501                      560                             650                      686                     806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503    516,526,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 22 -> /* 526 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                511                                                  658                                                                                                         |
                                103                                                 246                                                      403                           472                                                                                               704                                   858                                           |
    27     39        72                   135               186               234                 279                      358                         425           442                 501                  560           578               650                      686                     806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503    516,554,560   564,576,577   611,612,615,650   657    667,679,681,686   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 23 -> /* 686 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                    658                                                                                                     |
                                103                                                 246                                                      403                           472                 511                                                                      704                                   858                                           |
    27     39        72                   135               186               234                 279                      358                         425           442                 501                 560           578               650                  686                     806           830                 912               961           |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615,650   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,987,989|
""");
        case 24 -> /* 987 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                    658                                                                                                 |
                                103                                                 246                                                      403                           472                 511                                                                      704                                   858                                       |
    27     39        72                   135               186               234                 279                      358                         425           442                 501                 560           578               650                  686                     806           830                 912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401,403   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615,650   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,989|
""");
        case 25 -> /* 403 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                                658                                                                                                 |
                                103                                                 246                                                  403                           472                 511                                                                      704                                   858                                       |
    27     39        72                   135               186               234                 279                      358                     425           442                 501                 560           578               650                  686                     806           830                 912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,401   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615,650   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,989|
""");
        case 26 -> /* 401 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                            658                                                                                                 |
                                103                                                 246                                              403                           472                 511                                                                      704                                   858                                       |
    27     39        72                   135               186               234                 279                      358                 425           442                 501                 560           578               650                  686                     806           830                 912               961       |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615,650   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976,989|
""");
        case 27 -> /* 989 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                            658                                                                                             |
                                103                                                 246                                              403                           472                 511                                                                      704                                   858                                   |
    27     39        72                   135               186               234                 279                      358                 425           442                 501                 560           578               650                  686                     806           830                 912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615,650   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976|
""");
        case 28 -> /* 650 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                        658                                                                                             |
                                103                                                 246                                              403                           472                 511                                                                  704                                   858                                   |
    27     39        72                   135               186               234                 279                      358                 425           442                 501                 560           578           650                  686                     806           830                 912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438   447   480,490,494   503   516,554,560   564,576,577   611,612,615   657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976|
""");
        case 29 -> /* 576 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                                  658                                                                                             |
                                103                                                 246                                              403                           472                 511                                                            704                                   858                                   |
    27     39        72                   135               186               234                 279                      358                 425           442                 501                 560       578                              686                     806           830                 912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   436,437,438   447   480,490,494   503   516,554,560   564,577   611,612,615,657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976|
""");
        case 30 -> /* 436 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                            658                                                                                             |
                                103                                                 246                                              403                     472                 511                                                            704                                   858                                   |
    27     39        72                   135               186               234                 279                      358                 425                         501                 560       578                              686                     806           830                 912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494   503   516,554,560   564,577   611,612,615,657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976|
""");
        case 31 -> /* 560 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                        658                                                                                             |
                                103                                                 246                                                                      472                 511                                                        704                                   858                                   |
    27     39        72                   135               186               234                 279                      358       403       425                         501             560       578                              686                     806           830                 912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494   503   516,554   564,577   611,612,615,657    667,679,681   690   769,773,804,806   809,826,830   839   884,907,912   922,937,946,961   976|
""");
        case 32 -> /* 806 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                        658                                                                                       |
                                103                                                 246                                                                      472                 511                                                        704                             858                                   |
    27     39        72                   135               186               234                 279                      358       403       425                         501             560       578                              686                 806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494   503   516,554   564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 33 -> /* 554 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                                  658                                                                                       |
                                103                                                 246                                                                      472                 511                                                  704                             858                                   |
    27     39        72                   135               186               234                 279                      358       403       425                         501                 578                              686                 806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377   422,425   437,438,447   480,490,494   503   516,564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 34 -> /* 422 */ ok(m.printHorizontally(), """
                                                                                                             317                                                                                            658                                                                                       |
                                103                                                 246                                                                472                                                                      704                             858                                   |
    27     39        72                   135               186               234                 279                      358           425                         501   511           578                              686                 806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288,298    338,344,358   376,377,425   437,438,447   480,490,494   503   516,564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 35 -> /* 298 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                            658                                                                                       |
                                103                                                 246                                                            472                                                                      704                             858                                   |
    27     39        72                   135               186               234                 279                  358           425                         501   511           578                              686                 806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377,425   437,438,447   480,490,494   503   516,564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 36 -> /* 425 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                        658                                                                                       |
                                103                                                 246                                                        472                                                                      704                             858                                   |
    27     39        72                   135               186               234                 279                  358       425                         501   511           578                              686                 806                             912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   503   516,564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907,912   922,937,946,961   976|
""");
        case 37 -> /* 912 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                        658                                                                                   |
                                103                                                 246                                                        472                                                                                                      858                               |
    27     39        72                   135               186               234                 279                  358       425                         501   511           578                              686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   503   516,564,577   611,612,615,657    667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 38 -> /* 503 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                       |
                                103                                                 246                                                        472                                           658                                                 858                               |
    27     39        72                   135               186               234                 279                  358       425                         511           578                             686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516,564,577   611,612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 39 -> /* 611 */ ok(m.printHorizontally(), """
                                                                                                         317                                                                                                                                                                   |
                                103                                                 246                                                        472                                       658                                                 858                               |
    27     39        72                   135               186               234                 279                  358       425                         511           578                         686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106,135   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 40 -> /* 135 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                                   |
                                103                                             246                                                        472                                       658                                                 858                               |
    27     39        72               135               186               234                 279                  358       425                         511           578                         686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438,447   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 41 -> /* 447 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                               |
                                103                                             246                                                    472                                       658                                                 858                               |
    27     39        72               135               186               234                 279                  358       425                     511           578                         686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,344,358   376,377   437,438   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 42 -> /* 344 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                         |
                                103                                             246                                              472                                       658                                                 858                               |
    27     39        72               135               186               234                 279              358                             511           578                         686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    338,358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 43 -> /* 338 */ ok(m.printHorizontally(), """
                                                                                                     317                                                                                                                                                     |
                                103                                             246                                          472                                       658                                                 858                               |
    27     39        72               135               186               234                 279          358                             511           578                         686   704           806                         912               961   |
1,13  29,39  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 44 -> /* 39 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                                     |
                            103                                             246                                          472                                       658                                                 858                               |
       39        72               135               186               234                 279          358                             511           578                         686   704           806                         912               961   |
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681   690   769,773,804   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 45 -> /* 804 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                               |
                            103                                             246                                          472                                       658                                           858                               |
       39        72               135               186               234                 279          358                             511           578                             704       806                         912               961   |
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   976|
""");
        case 46 -> /* 976 */ ok(m.printHorizontally(), """
                                                                                                 317                                                                                                                                            |
                            103                                             246                                          472                                       658                                           858                            |
       39        72               135               186               234                 279          358                             511           578                             704       806                         912               961|
1,13,29  43,55,72  90,96,103   106   151,155,157,186   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 47 -> /* 186 */ ok(m.printHorizontally(), """
                                                                                             317                                                                                                                                            |
                            103                                         246                                          472                                       658                                           858                            |
       39        72               135           186               234                 279          358                             511           578                             704       806                         912               961|
1,13,29  43,55,72  90,96,103   106   151,155,157   188,229,232,234   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 48 -> /* 234 */ ok(m.printHorizontally(), """
                                                                                       317                                                                                                                                            |
                            103                                   246                                          472                                       658                                           858                            |
       39        72                           186           234                 279          358                             511           578                             704       806                         912               961|
1,13,29  43,55,72  90,96,103   106,151,155,157   188,229,232   237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 49 -> /* 106 */ ok(m.printHorizontally(), """
                                                                                 317                                                                                                                                            |
                            103                             246                                          472                                       658                                           858                            |
       39        72                       186                             279          358                             511           578                             704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   667,679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 50 -> /* 667 */ ok(m.printHorizontally(), """
                                                                                 317                                                                                                                                        |
                            103                             246                                          472                                       658                                       858                            |
       39        72                       186                             279          358                             511           578                         704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273   288    358   376,377,437,438   480,490,494   516,564,577   612,615,657   679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 51 -> /* 494 */ ok(m.printHorizontally(), """
                                                                                 317                                                                                                                                    |
                            103                             246                                          472                                   658                                       858                            |
       39        72                       186                             279          358                         511           578                         704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273   288    358   376,377,437,438   480,490   516,564,577   612,615,657   679,681,690   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 52 -> /* 690 */ ok(m.printHorizontally(), """
                                                                                 317                                                                                                                                |
                            103                             246                                          472                                   658                                   858                            |
       39        72                       186                             279          358                         511           578                     704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273   288    358   376,377,437,438   480,490   516,564,577   612,615,657   679,681   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 53 -> /* 480 */ ok(m.printHorizontally(), """
                                                                                 317                                                                                                                            |
                            103                             246                                          472                               658                                   858                            |
       39        72                       186                             279          358                     511           578                     704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273   288    358   376,377,437,438   490   516,564,577   612,615,657   679,681   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 54 -> /* 288 */ ok(m.printHorizontally(), """
                                                                           317                                                                                                                            |
                            103                                                                    472                               658                                   858                            |
       39        72                       186               246                  358                     511           578                     704       806                         912               961|
1,13,29  43,55,72  90,96,103   151,155,157   188,229,232,237   260,272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 55 -> /* 151 */ ok(m.printHorizontally(), """
                                                                       317                                                                                                                            |
                            103                                                                472                               658                                   858                            |
       39        72                   186               246                  358                     511           578                     704       806                         912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   260,272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681   769,773   809,826,830,839   884,907   922,937,946,961   |
""");
        case 56 -> /* 773 */ ok(m.printHorizontally(), """
                                                                       317                                                                                                                      |
                            103                                                                472                               658                             858                            |
       39        72                   186               246                  358                     511           578                         806                         912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   260,272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681,769   809,826,830,839   884,907   922,937,946,961   |
""");
        case 57 -> /* 769 */ ok(m.printHorizontally(), """
                                                                       317                                                                                                                  |
                            103                                                                472                               658                         858                            |
       39        72                   186               246                  358                     511           578                     806                         912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   260,272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681   809,826,830,839   884,907   922,937,946,961   |
""");
        case 58 -> /* 260 */ ok(m.printHorizontally(), """
                                                                   317                                                                                                                  |
                            103                                                            472                               658                         858                            |
       39        72                   186               246              358                     511           578                     806                         912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681   809,826,830,839   884,907   922,937,946,961   |
""");
        case 59 -> /* 809 */ ok(m.printHorizontally(), """
                                                                   317                                                                                                              |
                            103                                                            472                               658                     858                            |
       39        72                   186               246              358                     511           578                     806                     912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   272,273    358   376,377,437,438   490   516,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 60 -> /* 438 */ ok(m.printHorizontally(), """
                                                                   317                                                                                                          |
                            103                                                        472                               658                     858                            |
       39        72                   186               246              358                 511           578                     806                     912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232,237   272,273    358   376,377,437   490   516,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 61 -> /* 237 */ ok(m.printHorizontally(), """
                                                               317                                                                                                          |
                            103                                                    472                               658                     858                            |
       39        72                   186           246              358                 511           578                     806                     912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232   272,273    358   376,377,437   490   516,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 62 -> /* 516 */ ok(m.printHorizontally(), """
                                                               317                                                                                                    |
                            103                                                    472                         658                     858                            |
       39        72                   186           246              358                         578                     806                     912               961|
1,13,29  43,55,72  90,96,103   155,157   188,229,232   272,273    358   376,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 63 -> /* 29 */ ok(m.printHorizontally(), """
                                                            317                                                                                                    |
                         103                                                    472                         658                     858                            |
    39        72                   186           246              358                         578                     806                     912               961|
1,13  43,55,72  90,96,103   155,157   188,229,232   272,273    358   376,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 64 -> /* 376 */ ok(m.printHorizontally(), """
                                                            317                                                                                              |
                         103                                                                          658                     858                            |
    39        72                   186           246                      472           578                     806                     912               961|
1,13  43,55,72  90,96,103   155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 65 -> /* 72 */ ok(m.printHorizontally(), """
                                                         317                                                                                              |
                      103                                                                          658                     858                            |
    39     72                   186           246                      472           578                     806                     912               961|
1,13  43,55  90,96,103   155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,946,961   |
""");
        case 66 -> /* 946 */ ok(m.printHorizontally(), """
                                                         317                                                                                       |
                      103                                                                          658                     858                     |
    39     72                   186           246                      472           578                     806                     912           |
1,13  43,55  90,96,103   155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,961|
""");
        case 67 -> /* 103 */ ok(m.printHorizontally(), """
                                                    317                                                                                       |
                 103                                                                          658                     858                     |
          72               186           246                      472           578                     806                     912           |
1,13,43,55  90,96   155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937,961|
""");
        case 68 -> /* 961 */ ok(m.printHorizontally(), """
                                                    317                                                                                   |
                 103                                                                          658                                         |
          72               186           246                      472           578                     806           858       912       |
1,13,43,55  90,96   155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657   679,681   826,830,839   884,907   922,937|
""");
        case 69 -> /* 55 */ ok(m.printHorizontally(), """
               103                                317                                        658                                         |
       72                186           246                      472           578                      806           858       912       |
1,13,43  90,96    155,157   188,229,232   272,273    358,377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 70 -> /* 358 */ ok(m.printHorizontally(), """
               103                                317                                    658                                         |
       72                186           246                  472           578                      806           858       912       |
1,13,43  90,96    155,157   188,229,232   272,273    377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 71 -> /* 232 */ ok(m.printHorizontally(), """
               103                            317                                    658                                         |
       72                186       246                  472           578                      806           858       912       |
1,13,43  90,96    155,157   188,229   272,273    377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 72 -> /* 229 */ ok(m.printHorizontally(), """
               103                      317                                    658                                         |
       72                    246                  472           578                      806           858       912       |
1,13,43  90,96    155,157,188   272,273    377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 73 -> /* 90 */ ok(m.printHorizontally(), """
                                    317                                    658                                         |
       72  103           246                  472           578                      806           858       912       |
1,13,43  96   155,157,188   272,273    377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 74 -> /* 155 */ ok(m.printHorizontally(), """
                               317                                    658                                         |
          103       246                  472           578                      806           858       912       |
1,13,43,96   157,188   272,273    377,437   490,564,577   612,615,657    679,681   826,830,839   884,907   922,937|
""");
        case 75 -> /* 657 */ ok(m.printHorizontally(), """
                               317                                658                                         |
          103       246                  472           578                  806           858       912       |
1,13,43,96   157,188   272,273    377,437   490,564,577   612,615    679,681   826,830,839   884,907   922,937|
""");
        case 76 -> /* 681 */ ok(m.printHorizontally(), """
                               317                                658                                   |
          103       246                  472           578              806           858               |
1,13,43,96   157,188   272,273    377,437   490,564,577   612,615    679   826,830,839   884,907,922,937|
""");
        case 77 -> /* 43 */ ok(m.printHorizontally(), """
                          317                                658                                   |
       103                          472           578              806           858               |
1,13,96   157,188,272,273    377,437   490,564,577   612,615    679   826,830,839   884,907,922,937|
""");
        case 78 -> /* 907 */ ok(m.printHorizontally(), """
                          317                                658                             |
       103                          472           578                          858           |
1,13,96   157,188,272,273    377,437   490,564,577   612,615    679,826,830,839   884,922,937|
""");
        case 79 -> /* 564 */ ok(m.printHorizontally(), """
                          317                            658                             |
       103                          472       578                          858           |
1,13,96   157,188,272,273    377,437   490,577   612,615    679,826,830,839   884,922,937|
""");
        case 80 -> /* 377 */ ok(m.printHorizontally(), """
                          317                      658                             |
       103                              578                          858           |
1,13,96   157,188,272,273    437,490,577   612,615    679,826,830,839   884,922,937|
""");
        case 81 -> /* 615 */ ok(m.printHorizontally(), """
                                              658                             |
       103               317           578                      858           |
1,13,96   157,188,272,273   437,490,577   612    679,826,830,839   884,922,937|
""");
        case 82 -> /* 612 */ ok(m.printHorizontally(), """
                                        658                             |
       103               317                              858           |
1,13,96   157,188,272,273   437,490,577    679,826,830,839   884,922,937|
""");
        case 83 -> /* 157 */ ok(m.printHorizontally(), """
                                    658                             |
       103           317                              858           |
1,13,96   188,272,273   437,490,577    679,826,830,839   884,922,937|
""");
        case 84 -> /* 922 */ ok(m.printHorizontally(), """
                                    658                         |
       103           317                              858       |
1,13,96   188,272,273   437,490,577    679,826,830,839   884,937|
""");
        case 85 -> /* 272 */ ok(m.printHorizontally(), """
                                658                         |
       103       317                              858       |
1,13,96   188,273   437,490,577    679,826,830,839   884,937|
""");
        case 86 -> /* 490 */ ok(m.printHorizontally(), """
                            658                         |
       103       317                          858       |
1,13,96   188,273   437,577    679,826,830,839   884,937|
""");
        case 87 -> /* 679 */ ok(m.printHorizontally(), """
                            658                     |
       103       317                      858       |
1,13,96   188,273   437,577    826,830,839   884,937|
""");
        case 88 -> /* 830 */ ok(m.printHorizontally(), """
                            658                 |
       103       317                  858       |
1,13,96   188,273   437,577    826,839   884,937|
""");
        case 89 -> /* 839 */ ok(m.printHorizontally(), """
                            658           |
       103       317                      |
1,13,96   188,273   437,577    826,884,937|
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
       }
      ok(m.size(), r.length - i - 1);
     }
   }

  static void test_delete_reverse()
   {final long[]r = random_array();
    var m = mjaf(12, 12, 4, r.length);
    //say("Delete at start");
    //say(m.nodes);
    for (int i = 0; i < r.length; ++i) m.put(m.key(r[i]), m.data(i));
    //say("Delete after load");
    //say(m.nodes);
    for (int i = r.length-1; i >= 0; --i)
     {ok(m.delete(m.key(r[i])), m.data(i));
      if (!true)                                                                 // Write case statement to check deletions
       {say("        case", i, "-> /*", r[i], "*/ ok(m.printHorizontally(), \"\"\"");
        say(m.printHorizontally());
        say("\"\"\");");
       }
      else switch(i) {
        case 99 -> /* 188 */ ok(m.printHorizontally(), """
                                                                                                                                                                                                                                            511                                                                                                                                                                                                                     |
                                                                                                                             317                                                                                                                                                                                        658                                                                                                                                         |
                                   103                                                 246                                                                               403                                   472                                                                    578                                                           704                                           858                         912                                   |
       27     39        72                   135               186           234                 261           279                     344       358           391                 425               442                         501                      545       560                         611           650                         686                         806           830                         903                             961       987       |
1,13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,273,279   288,298,317   338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658   667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 98 -> /* 1 */ ok(m.printHorizontally(), """
                                                                                                                            317                                                                                                            511                                                                          658                                                                                                                                         |
                                 103                                                 246                                                                                403                                   472                                                                    578                                                            704                                           858                         912                                   |
     27     39        72                   135               186           234                 261           279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,273,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 97 -> /* 273 */ ok(m.printHorizontally(), """
                                                                                                                        317                                                                                                            511                                                                          658                                                                                                                                         |
                                 103                                                 246                                                                            403                                   472                                                                    578                                                            704                                           858                         912                                   |
     27     39        72                   135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27  29,39  43,55,72  90,96,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 96 -> /* 96 */ ok(m.printHorizontally(), """
                                                                                                                    317                                                                                                            511                                                                          658                                                                                                                                         |
                             103                                                 246                                                                            403                                   472                                                                    578                                                            704                                           858                         912                                   |
           39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                         903                             961       987       |
13,27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 95 -> /* 13 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                            511                                                                          658                                                                                                                                         |
                          103                                                 246                                                                            403                                   472                                                                    578                                                            704                                           858                         912                                   |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                         903                             961       987       |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,884,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 94 -> /* 884 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                            511                                                                          658                                                                                                                                     |
                          103                                                 246                                                                            403                                   472                                                                    578                                                            704                                           858                     912                                   |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                     903                             961       987       |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,937,946,961   976,987   989,993|
""");
        case 93 -> /* 937 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                            511                                                                          658                                                                                                                               |
                          103                                                 246                                                                            403                                   472                                                                    578                                                            704                                           858                     912                             |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                         611           650                          686                         806           830                     903                         961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,577,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 92 -> /* 577 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                            511                                                                      658                                                                                                                               |
                          103                                                 246                                                                            403                                   472                                                                578                                                            704                                           858                     912                             |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                     611           650                          686                         806           830                     903                         961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,826,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 91 -> /* 826 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                            511                                                                      658                                                                                                                           |
                          103                                                 246                                                                            403                                   472                                                                578                                                            704                                       858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425               442                         501                      545       560                     611           650                          686                         806       830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,437,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 90 -> /* 437 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                        511                                                                      658                                                                                                                           |
                          103                                                 246                                                                            403                               472                                                                578                                                            704                                       858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442                         501                      545       560                     611           650                          686                         806       830                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   839,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 89 -> /* 839 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                        511                                                                      658                                                                                                                       |
                          103                                                 246                                                                            403                               472                                                                578                                                            704                                   858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442                         501                      545       560                     611           650                          686                         806       830                 903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,830   854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 88 -> /* 830 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                        511                                                                      658                                                                                                                 |
                          103                                                 246                                                                            403                               472                                                                578                                                            704                             858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442                         501                      545       560                     611           650                          686                         806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,679,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 87 -> /* 679 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                        511                                                                      658                                                                                                             |
                          103                                                 246                                                                            403                               472                                                                578                                                                                        858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442                         501                      545       560                     611           650                      686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,490,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658    667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 86 -> /* 490 */ ok(m.printHorizontally(), """
                                                                                                                 317                                                                                                    511                                                                                                                                                                                     |
                          103                                                 246                                                                            403                               472                                                            578                               658                                                     858                                                     |
        39        72                135               186           234                 261       279                      344       358           391                 425           442                     501                      545       560                     611           650                     686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261   272,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 85 -> /* 272 */ ok(m.printHorizontally(), """
                                                                                                           317                                                                                                    511                                                                                                                                                                                     |
                          103                                                 246                                                                      403                               472                                                            578                               658                                                     858                                                     |
        39        72                135               186           234                     279                      344       358           391                 425           442                     501                      545       560                     611           650                     686       704               806                     903           912           961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   922,946,961   976,987,989,993|
""");
        case 84 -> /* 922 */ ok(m.printHorizontally(), """
                                                                                                           317                                                                                                    511                                                                                                                                                                                 |
                          103                                                 246                                                                      403                               472                                                            578                               658                                                     858                                                 |
        39        72                135               186           234                     279                      344       358           391                 425           442                     501                      545       560                     611           650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,157,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 83 -> /* 157 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                    511                                                                                                                                                                                 |
                          103                                             246                                                                      403                               472                                                            578                               658                                                     858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442                     501                      545       560                     611           650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611   612,615,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 82 -> /* 612 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                    511                                                                                                                                                                             |
                          103                                             246                                                                      403                               472                                                            578                           658                                                     858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442                     501                      545       560                     611       650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611   615,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 81 -> /* 615 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                                    511                                                                                                                                                                       |
                          103                                             246                                                                      403                               472                                                            578                     658                                                     858                                                 |
        39        72                135           186           234                     279                      344       358           391                 425           442                     501                      545       560                         650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344   354,358   376,377,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 80 -> /* 377 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                              511                                                                                                                                                                       |
                          103                                             246                                                                403                               472                                                            578                     658                                                     858                                                 |
        39        72                135           186           234                     279                              358       391                 425           442                     501                      545       560                         650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   564,576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 79 -> /* 564 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                              511                                                                                                                                                                   |
                          103                                             246                                                                403                               472                                                        578                     658                                                     858                                                 |
        39        72                135           186           234                     279                              358       391                 425           442                     501                      545       560                     650                     686       704               806                     903           912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,907,912   946,961   976,987,989,993|
""");
        case 78 -> /* 907 */ ok(m.printHorizontally(), """
                                                                                                       317                                                                                              511                                                                                                                                                               |
                          103                                             246                                                                403                               472                                                        578                     658                                                     858                                             |
        39        72                135           186           234                     279                              358       391                 425           442                     501                      545       560                     650                     686       704               806                     903       912       961               |
27,29,39  43,55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 77 -> /* 43 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                              511                                                                                                                                                               |
                       103                                             246                                                                403                               472                                                        578                     658                                                     858                                             |
        39     72                135           186           234                     279                              358       391                 425           442                     501                      545       560                     650                     686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   657,658   667,681,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 76 -> /* 681 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                              511                                                                                                                                                           |
                       103                                             246                                                                403                               472                                                        578                     658                                                 858                                             |
        39     72                135           186           234                     279                              358       391                 425           442                     501                      545       560                     650                 686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   657,658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 75 -> /* 657 */ ok(m.printHorizontally(), """
                                                                                                    317                                                                                              511                                                                                                                                                       |
                       103                                             246                                                                403                               472                                                        578                 658                                                 858                                             |
        39     72                135           186           234                     279                              358       391                 425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,155,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 74 -> /* 155 */ ok(m.printHorizontally(), """
                                                                                                317                                                                                              511                                                                                                                                                       |
                       103                                         246                                                                403                               472                                                        578                 658                                                 858                                             |
        39     72                135       186           234                     279                              358       391                 425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72  90,103   106,135   151,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 73 -> /* 90 */ ok(m.printHorizontally(), """
                                                                                            317                                                                                              511                                                                                                                                                       |
                   103                                         246                                                                403                               472                                                        578                 658                                                 858                                             |
        39                   135       186           234                     279                              358       391                 425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135   151,186   229,232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 72 -> /* 229 */ ok(m.printHorizontally(), """
                                                                                      317                                                                                              511                                                                                                                                                       |
                   103                                   246                                                                403                               472                                                        578                 658                                                 858                                             |
        39                           186       234                     279                              358       391                 425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   232,234   237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 71 -> /* 232 */ ok(m.printHorizontally(), """
                                                                                317                                                                                              511                                                                                                                                                       |
                   103                             246                                                                403                               472                                                        578                 658                                                 858                                             |
        39                           186                         279                              358       391                 425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354,358   376,391   401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 70 -> /* 358 */ ok(m.printHorizontally(), """
                                                                                317                                                                                        511                                                                                                                                                       |
                   103                             246                                                          403                               472                                                        578                 658                                                 858                                             |
        39                           186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  55,72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 69 -> /* 55 */ ok(m.printHorizontally(), """
                                                                             317                                                                                        511                                                                                                                                                       |
                                                246                                                          403                               472                                                        578                 658                                                 858                                             |
        39      103               186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                     903       912       961               |
27,29,39  72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903   906,912   946,961   976,987,989,993|
""");
        case 68 -> /* 961 */ ok(m.printHorizontally(), """
                                                                             317                                                                                        511                                                                                                                                                 |
                                                246                                                          403                               472                                                        578                 658                                                 858                                       |
        39      103               186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                             912   961               |
27,29,39  72,103   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912   946   976,987,989,993|
""");
        case 67 -> /* 103 */ ok(m.printHorizontally(), """
                                                                         317                                                                                        511                                                                                                                                                 |
                                            246                                                          403                               472                                                        578                 658                                                 858                                       |
        39  103               186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                             912   961               |
27,29,39  72   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912   946   976,987,989,993|
""");
        case 66 -> /* 946 */ ok(m.printHorizontally(), """
                                                                         317                                                                                        511                                                                                                                                              |
                                            246                                                          403                               472                                                        578                 658                                                 858                                    |
        39  103               186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                             912961               |
27,29,39  72   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 65 -> /* 72 */ ok(m.printHorizontally(), """
                                                                     317                                                                                        511                                                                                                                                              |
                                        246                                                          403                               472                                                        578                 658                                                 858                                    |
        103               186                         279                          358                         425           442                     501                      545       560                     650             686       704               806                             912961               |
27,29,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   376,391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 64 -> /* 376 */ ok(m.printHorizontally(), """
                                                                     317                                                                                    511                                                                                                                                              |
                                        246                                                      403                               472                                                        578                 658                                                 858                                    |
        103               186                         279                          358                     425           442                     501                      545       560                     650             686       704               806                             912961               |
27,29,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 63 -> /* 29 */ ok(m.printHorizontally(), """
                                                                  317                                                                                    511                                                                                                                                              |
                                     246                                                      403                               472                                                        578                 658                                                 858                                    |
     103               186                         279                          358                     425           442                     501                      545       560                     650             686       704               806                             912961               |
27,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    516,526,545   554,560   576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 62 -> /* 516 */ ok(m.printHorizontally(), """
                                                                  317                                                                                    511                                                                                                                                        |
                                     246                                                      403                               472                                                  578                 658                                                 858                                    |
     103               186                         279                          358                     425           442                     501                  545                             650             686       704               806                             912961               |
27,39   106,135,151,186   234,237,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 61 -> /* 237 */ ok(m.printHorizontally(), """
                                                              317                                                                                    511                                                                                                                                        |
                                 246                                                      403                               472                                                  578                 658                                                 858                                    |
     103               186                     279                          358                     425           442                     501                  545                             650             686       704               806                             912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,438,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 60 -> /* 438 */ ok(m.printHorizontally(), """
                                                              317                                                                                511                                                                                                                                        |
                                 246                                                      403                           472                                                  578                 658                                                 858                                    |
     103               186                     279                          358                     425       442                     501                  545                             650             686       704               806                             912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686   690,704   769,773,804,806   809,854,858   882,903,906,912      976,987,989,993|
""");
        case 59 -> /* 809 */ ok(m.printHorizontally(), """
                                                              317                                                                                511                                                                                                                                  |
                                 246                                                      403                           472                                                                      658                                           858                                    |
     103               186                     279                          358                     425       442                     501                  545               578           650                     704               806                         912961               |
27,39   106,135,151,186   234,246   260,261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   769,773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 58 -> /* 260 */ ok(m.printHorizontally(), """
                                                          317                                                                                511                                                                                                                                  |
                                 246                                                  403                           472                                                                      658                                           858                                    |
     103               186                 279                          358                     425       442                     501                  545               578           650                     704               806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   769,773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 57 -> /* 769 */ ok(m.printHorizontally(), """
                                                          317                                                                                511                                                                                                                              |
                                 246                                                  403                           472                                                                      658                                       858                                    |
     103               186                 279                          358                     425       442                     501                  545               578           650                     704           806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   773,804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 56 -> /* 773 */ ok(m.printHorizontally(), """
                                                          317                                                                                511                                                                                                                          |
                                 246                                                  403                           472                                                                      658                                   858                                    |
     103               186                 279                          358                     425       442                     501                  545               578           650                     704       806                         912961               |
27,39   106,135,151,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 55 -> /* 151 */ ok(m.printHorizontally(), """
                                                      317                                                                                511                                                                                                                          |
                             246                                                  403                           472                                                                      658                                   858                                    |
     103           186                 279                          358                     425       442                     501                  545               578           650                     704       806                         912961               |
27,39   106,135,186   234,246   261,279   288,298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 54 -> /* 288 */ ok(m.printHorizontally(), """
                                                  317                                                                                511                                                                                                                          |
                             246                                              403                           472                                                                      658                                   858                                    |
     103           186                 279                      358                     425       442                     501                  545               578           650                     704       806                         912961               |
27,39   106,135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   480,494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 53 -> /* 480 */ ok(m.printHorizontally(), """
                                                  317                                                                            511                                                                                                                          |
                             246                                              403                           472                                                                  658                                   858                                    |
     103           186                 279                      358                     425       442                 501                  545               578           650                     704       806                         912961               |
27,39   106,135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,690,704   804,806   854,858   882,903,906,912      976,987,989,993|
""");
        case 52 -> /* 690 */ ok(m.printHorizontally(), """
                                                  317                                                                            511                                                                                                                    |
                             246                                              403                           472                                                                  658                             858                                    |
     103           186                 279                      358                     425       442                 501                  545               578           650                 704                                 912961               |
27,39   106,135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   494,501   503,511    526,545   554,560,576,578   586,611,650   658   667,686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 51 -> /* 494 */ ok(m.printHorizontally(), """
                                                  317                                                                      511                                                                                                                    |
                             246                                              403                           472                                                            658                             858                                    |
     103           186                 279                      358                     425       442                                545               578           650                 704                                 912961               |
27,39   106,135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   667,686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 50 -> /* 667 */ ok(m.printHorizontally(), """
                                                  317                                                                      511                                                                                                                |
                             246                                              403                           472                                                            658                         858                                    |
     103           186                 279                      358                     425       442                                545               578           650             704                                 912961               |
27,39   106,135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 49 -> /* 106 */ ok(m.printHorizontally(), """
                                              317                                                                      511                                                                                                                |
                         246                                              403                           472                                                            658                         858                                    |
     103       186                 279                      358                     425       442                                545               578           650             704                                 912961               |
27,39   135,186   234,246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 48 -> /* 234 */ ok(m.printHorizontally(), """
                                        317                                                                      511                                                                                                                |
                   246                                              403                           472                                                            658                         858                                    |
             186             279                      358                     425       442                                545               578           650             704                                 912961               |
27,39,135,186   246   261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 47 -> /* 186 */ ok(m.printHorizontally(), """
                                  317                                                                      511                                                                                                                |
                                                              403                           472                                                            658                         858                                    |
         186           279                      358                     425       442                                545               578           650             704                                 912961               |
27,39,135   246,261,279   298,317    338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   804,806,854,858   882,903,906,912      976,987,989,993|
""");
        case 46 -> /* 976 */ ok(m.printHorizontally(), """
                                                                                                          511                                                                                                         |
                                 317                         403                           472                                                            658                         858                             |
         186           279                     358                     425       442                                545               578           650             704                                 961           |
27,39,135   246,261,279   298,317   338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   804,806,854,858   882,903,906,912   987,989,993|
""");
        case 45 -> /* 804 */ ok(m.printHorizontally(), """
                                                                                                          511                                                                                                     |
                                 317                         403                           472                                                            658                                                     |
         186           279                     358                     425       442                                545               578           650             704           858               961           |
27,39,135   246,261,279   298,317   338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 44 -> /* 39 */ ok(m.printHorizontally(), """
                                                                                                       511                                                                                                     |
                              317                         403                                                                                          658                                                     |
      186           279                     358                     425       442       472                      545               578           650             704           858               961           |
27,135   246,261,279   298,317   338,344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 43 -> /* 338 */ ok(m.printHorizontally(), """
                                                                                                   511                                                                                                     |
                              317                     403                                                                                          658                                                     |
      186           279                 358                     425       442       472                      545               578           650             704           858               961           |
27,135   246,261,279   298,317   344,354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 42 -> /* 344 */ ok(m.printHorizontally(), """
                                                                                               511                                                                                                     |
                              317                 403                                                                                          658                                                     |
      186           279             358                     425       442       472                      545               578           650             704           858               961           |
27,135   246,261,279   298,317   354   391,401,403   422,425   436,442   447,472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 41 -> /* 447 */ ok(m.printHorizontally(), """
                                                                                         511                                                                                                     |
                              317                 403                                                                                    658                                                     |
      186           279             358                             442   472                      545               578           650             704           858               961           |
27,135   246,261,279   298,317   354   391,401,403   422,425,436,442   472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 40 -> /* 135 */ ok(m.printHorizontally(), """
                                                                                     511                                                                                                     |
                          317                 403                                                                                    658                                                     |
  186           279             358                             442   472                      545               578           650             704           858               961           |
27   246,261,279   298,317   354   391,401,403   422,425,436,442   472   501,503,511    526,545   554,560,576,578   586,611,650   658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 39 -> /* 611 */ ok(m.printHorizontally(), """
                                                                                     511                                                                                               |
                          317                 403                                                                              658                                                     |
  186           279             358                             442   472                      545               578                     704           858               961           |
27   246,261,279   298,317   354   391,401,403   422,425,436,442   472   501,503,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 38 -> /* 503 */ ok(m.printHorizontally(), """
                                                                               511                                                                                               |
                          317                 403                                                                        658                                                     |
  186           279             358                             442                      545               578                     704           858               961           |
27   246,261,279   298,317   354   391,401,403   422,425,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858   882,903,906,912   987,989,993|
""");
        case 37 -> /* 912 */ ok(m.printHorizontally(), """
                                                                               511                                                                                           |
                          317                 403                                                                        658                                                 |
  186           279             358                             442                      545               578                     704           858           961           |
27   246,261,279   298,317   354   391,401,403   422,425,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858   882,903,906   987,989,993|
""");
        case 36 -> /* 425 */ ok(m.printHorizontally(), """
                                                                         511                                                                                           |
                          317                                                                                      658                                                 |
  186           279                         403           442                      545               578                     704           858           961           |
27   246,261,279   298,317   354,391,401,403   422,436,442   472,501,511    526,545   554,560,576,578   586,650,658   686,704   806,854,858   882,903,906   987,989,993|
""");
        case 35 -> /* 298 */ ok(m.printHorizontally(), """
                     317                                            511                                        658                                                 |
              279                      403           442                      545               578                      704           858           961           |
27,246,261,279   317    354,391,401,403   422,436,442   472,501,511    526,545   554,560,576,578   586,650,658    686,704   806,854,858   882,903,906   987,989,993|
""");
        case 34 -> /* 422 */ ok(m.printHorizontally(), """
                     317                                        511                                        658                                                 |
              279                      403       442                      545               578                      704           858           961           |
27,246,261,279   317    354,391,401,403   436,442   472,501,511    526,545   554,560,576,578   586,650,658    686,704   806,854,858   882,903,906   987,989,993|
""");
        case 33 -> /* 554 */ ok(m.printHorizontally(), """
                     317                                        511                                    658                                                 |
              279                      403       442                      545           578                      704           858           961           |
27,246,261,279   317    354,391,401,403   436,442   472,501,511    526,545   560,576,578   586,650,658    686,704   806,854,858   882,903,906   987,989,993|
""");
        case 32 -> /* 806 */ ok(m.printHorizontally(), """
                     317                                        511                                    658                                             |
              279                      403       442                      545           578                      704       858           961           |
27,246,261,279   317    354,391,401,403   436,442   472,501,511    526,545   560,576,578   586,650,658    686,704   854,858   882,903,906   987,989,993|
""");
        case 31 -> /* 560 */ ok(m.printHorizontally(), """
                     317                                        511                                658                                             |
              279                      403       442                      545       578                      704       858           961           |
27,246,261,279   317    354,391,401,403   436,442   472,501,511    526,545   576,578   586,650,658    686,704   854,858   882,903,906   987,989,993|
""");
        case 30 -> /* 436 */ ok(m.printHorizontally(), """
                     317                                    511                                658                                             |
              279                      403   442                      545       578                      704       858           961           |
27,246,261,279   317    354,391,401,403   442   472,501,511    526,545   576,578   586,650,658    686,704   854,858   882,903,906   987,989,993|
""");
        case 29 -> /* 576 */ ok(m.printHorizontally(), """
                     317                                    511                          658                                             |
              279                      403   442                          578                      704       858           961           |
27,246,261,279   317    354,391,401,403   442   472,501,511    526,545,578   586,650,658    686,704   854,858   882,903,906   987,989,993|
""");
        case 28 -> /* 650 */ ok(m.printHorizontally(), """
                     317                                    511                      658                                             |
              279                      403   442                          578                  704       858           961           |
27,246,261,279   317    354,391,401,403   442   472,501,511    526,545,578   586,658    686,704   854,858   882,903,906   987,989,993|
""");
        case 27 -> /* 989 */ ok(m.printHorizontally(), """
                     317                                    511                      658                                       |
              279                      403   442                          578                          858           961       |
27,246,261,279   317    354,391,401,403   442   472,501,511    526,545,578   586,658    686,704,854,858   882,903,906   987,993|
""");
        case 26 -> /* 401 */ ok(m.printHorizontally(), """
                     317                              511                      658                                       |
              279                  403                              578                          858           961       |
27,246,261,279   317    354,391,403   442,472,501,511    526,545,578   586,658    686,704,854,858   882,903,906   987,993|
""");
        case 25 -> /* 403 */ ok(m.printHorizontally(), """
                                               511                      658                                       |
              279           403                              578                          858           961       |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658    686,704,854,858   882,903,906   987,993|
""");
        case 24 -> /* 987 */ ok(m.printHorizontally(), """
                                               511                      658                                   |
              279           403                              578                          858           961   |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658    686,704,854,858   882,903,906   993|
""");
        case 23 -> /* 686 */ ok(m.printHorizontally(), """
                                               511                      658                             |
              279           403                              578                      858               |
27,246,261,279   317,354,391   442,472,501,511    526,545,578   586,658    704,854,858   882,903,906,993|
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
                         511                                   |
          403                              658       858       |
27,317,391   442,472,511    545,578,586,658   704,858   906,993|
""");
        case 13 -> /* 704 */ ok(m.printHorizontally(), """
                         511                             |
          403                              658           |
27,317,391   442,472,511    545,578,586,658   858,906,993|
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
    //say("Statistics for delete");
    //say(m.nodes);
   }

  static void test_save()
   {final int N = 10;
    var m = mjaf(12, 12, 4, N<<1);
    for (long i = 0; i < N; i++) m.put(m.key(i), m.data(i<<1));
    var nodes = m.traverse();
    ok(nodes.elementAt(0) instanceof Mjaf.Branch);
    ok(nodes.elementAt(1) instanceof Mjaf.Leaf);
    ok(nodes.elementAt(2) instanceof Mjaf.Leaf);
    ok(nodes.elementAt(3) instanceof Mjaf.Leaf);
    //say(m.printHorizontally());
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
    test_save();
   }

  static void newTests()                                                        // Tests being worked on
   {oldTests();
    test_save();
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
