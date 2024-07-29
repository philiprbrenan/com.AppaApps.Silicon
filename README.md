<div>
    <p><a href="https://github.com/philiprbrenan/com.AppaApps.Silicon"><img src="https://github.com/philiprbrenan/com.AppaApps.Silicon/workflows/Test/badge.svg"></a>
</div>

# Silicon [chip](https://en.wikipedia.org/wiki/Integrated_circuit) 
Design, simulate and layout a [B-Tree](https://en.wikipedia.org/wiki/B-tree) on a [Silicon](https://en.wikipedia.org/wiki/Silicon) [chip](https://en.wikipedia.org/wiki/Integrated_circuit). 
Reasons why you might want to join this project:

http://prb.appaapps.com/zesal/pitchdeck/pitchDeck.html

Or you might want to design your own [RiscV machine](https://github.com/philiprbrenan/com.AppaApps.Silicon/blob/main/RiscV.java) and [operating system](https://en.wikipedia.org/wiki/Operating_system) to run on it.

# Files

```
Ban   - RiscV 32I Cpu on a silicon chip.
Chip  - Design, simulate and layout a binary tree on a silicon chip.
Mjaf  - Btree with data stored only in the leaves to simplify deletion.
RiscV - Execute Risc V machine code. Little endian RV32I.
Stuck - A fixed size stack of ordered keys controlled by a unary number.
Unary - Unary arithmetic using boolean arrays.
```

# Examples

## And

Create a [chip](https://en.wikipedia.org/wiki/Integrated_circuit) that **and**s two input pins together and places the result on
the output [pin](https://en.wikipedia.org/wiki/555_timer_IC). 
```
    Chip c = new Chip("And");
    Gate i = c.Input ("i");
    Gate I = c.Input ("I");
    Gate a = c.And   ("a", i, I);
    Gate o = c.Output("o", a);

    Inputs inputs = c.new Inputs().set(i, true).set(I, false);

    c.simulate(inputs);

    c.draw();

    i.ok(true);
    I.ok(false);
    a.ok(false);
    o.ok(false);
    ok(c.steps, 3);
```

``draw()`` draws a layout [mask](https://en.wikipedia.org/wiki/Integrated_circuit_layout) for the [chip](https://en.wikipedia.org/wiki/Integrated_circuit) using [Graphics Design System 2](https://en.wikipedia.org/wiki/GDSII):

![And](images/And.png)

## Compare Greater Than

Compare two unsigned 4 [bit](https://en.wikipedia.org/wiki/Bit) integers to check whether the first is greater than
the second.

![Compare Greater Than](images/CompareGt4.png)

## Choose Word Under Mask

Use a [mask](https://en.wikipedia.org/wiki/Integrated_circuit_layout) to choose one [word](https://en.wikipedia.org/wiki/Doc_(computing)) from an [array](https://en.wikipedia.org/wiki/Dynamic_array) of words:

![Choose Word Under Mask](images/ChooseWordUnderMask2.png)

## Btree Node Compare

Locate the [data](https://en.wikipedia.org/wiki/Data) associated with a key in the node of a [B-Tree](https://en.wikipedia.org/wiki/B-tree): 
![Btree Leaf Compare](images/BtreeLeafCompare.png)

## Btree

A complete [B-Tree](https://en.wikipedia.org/wiki/B-tree): 
![Btree](images/Btree.png)

# Gates

A [chip](https://en.wikipedia.org/wiki/Integrated_circuit) is built at the [Register Transfer Level](https://en.wikipedia.org/wiki/Register-transfer_level) out of standard [Boolean](https://en.wikipedia.org/wiki/Boolean_data_type) logic gates. Each [gate](https://en.wikipedia.org/wiki/Logic_gate) produces a [bit](https://en.wikipedia.org/wiki/Bit) value that can be used to drive one input [pin](https://en.wikipedia.org/wiki/555_timer_IC) of another [gate](https://en.wikipedia.org/wiki/Logic_gate) or
one output [pin](https://en.wikipedia.org/wiki/555_timer_IC). 
Each input [pin](https://en.wikipedia.org/wiki/555_timer_IC) of each [gate](https://en.wikipedia.org/wiki/Logic_gate) can only be driven by one output [pin](https://en.wikipedia.org/wiki/555_timer_IC) of a [gate](https://en.wikipedia.org/wiki/Logic_gate). To
allow one [gate](https://en.wikipedia.org/wiki/Logic_gate) output [pin](https://en.wikipedia.org/wiki/555_timer_IC) to drive several input pins, each [gate](https://en.wikipedia.org/wiki/Logic_gate) produces two
copies of its output [bit](https://en.wikipedia.org/wiki/Bit) enabling the construction of equal depth fan out [trees](https://en.wikipedia.org/wiki/Tree_(data_structure)). 
Some [gate](https://en.wikipedia.org/wiki/Logic_gate) types such as ``or`` and ``and`` can have as many input pins as
requested. The remaining [gate](https://en.wikipedia.org/wiki/Logic_gate) types have no more than two input pins.

## Signal fan in and out

The underlying [chip](https://en.wikipedia.org/wiki/Integrated_circuit) is built up out of [Register Transfer Level](https://en.wikipedia.org/wiki/Register-transfer_level) [Boolean](https://en.wikipedia.org/wiki/Boolean_data_type) gates that have two inputs
and one output and a copy of the output giving two input pins and two output
pins per [gate](https://en.wikipedia.org/wiki/Logic_gate). One output [pin](https://en.wikipedia.org/wiki/555_timer_IC) can drive just one input [pin](https://en.wikipedia.org/wiki/555_timer_IC) via a connecting
wire.

There has to be a limit on how many input pins an output [pin](https://en.wikipedia.org/wiki/555_timer_IC) can drive
otherwise we could have situations in which one output [pin](https://en.wikipedia.org/wiki/555_timer_IC) was driving millions
of input pins which would require so much current that the output [pin](https://en.wikipedia.org/wiki/555_timer_IC) would
fuse.

When we request an ``and`` [gate](https://en.wikipedia.org/wiki/Logic_gate) (for example), we might [code](https://en.wikipedia.org/wiki/Computer_program): 
```
A = And("A", b, c, d, e)
```

i.e. we might provide more than two inputs ``b .. e``. Internally, such
gates are broken down into a [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)) of AND gates, see: ``fanIn in Chip.java``.

Also, we might appear to allow one output [pin](https://en.wikipedia.org/wiki/555_timer_IC) to drive more than one input [pin](https://en.wikipedia.org/wiki/555_timer_IC), but internally this is replaced by a [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)) of fan outs, see: ``fanOut in
Chip.java``.

The fan in and fan out [trees](https://en.wikipedia.org/wiki/Tree_(data_structure)) are carefully arranged to ensure that the number
of steps from the root to each leaf is the same along every path so that the
fanned signal converges or diverges at the same rate leading to the same signal
delay along each line of propagation. The alternative would have been to allow
the signals to diverge as they propagate, but this makes debugging very
difficult.

So, logically, you can have as many inputs and outputs as you need for each [Register Transfer Level](https://en.wikipedia.org/wiki/Register-transfer_level) [Boolean](https://en.wikipedia.org/wiki/Boolean_data_type) [gate](https://en.wikipedia.org/wiki/Logic_gate), but at the cost of a ``log(N)`` delay where ``N`` is the number of
bits to be fanned in or out.


# Buses

The single bits transferred by connections between gates can be aggregated into
a [bit bus](https://en.wikipedia.org/wiki/Bus_(computing)) allowing the bits to be manipulated en mass.

A [bit bus](https://en.wikipedia.org/wiki/Bus_(computing)) behaves like a [variable](https://en.wikipedia.org/wiki/Variable_(computer_science)) or like an [array](https://en.wikipedia.org/wiki/Dynamic_array) of [variables](https://en.wikipedia.org/wiki/Variable_(computer_science)) .

## Bit buses

A [bit bus](https://en.wikipedia.org/wiki/Bus_(computing)) corresponds to a [variable](https://en.wikipedia.org/wiki/Variable_(computer_science)) .

```
25 Bit buses
Bits  Bus_____________________________  Value
   3                            data_1  1
   3                            data_2  3
   3                            data_3  5
   3                            enable  7
   3                              find  4
   3                            keys_1  2
   3                            keys_2  4
   3                            keys_3  6
   3                            next_1  1
   3                            next_2  3
   3                            next_3  5
   3                     out_dataFound  3
   3                    out_dataFound1  3
   3                    out_dataFound2  3
   3                            out_id  7
   3                     out_maskEqual  2
   3                      out_maskMore  4
   3                     out_moreFound  5
   3                      out_nextLink  0
   3                     out_nextLink1  0
   3                     out_nextLink2  0
   3                     out_nextLink3  5
   3                     out_nextLink4  5
   3                     out_pointMore  4
   3                               top  7
```

## Words buses

A [word](https://en.wikipedia.org/wiki/Doc_(computing)) [bit bus](https://en.wikipedia.org/wiki/Bus_(computing)) correspond to an [array](https://en.wikipedia.org/wiki/Dynamic_array) of [variables](https://en.wikipedia.org/wiki/Variable_(computer_science)) .

```

3 Word buses
Words Bits  Bus_____________________________
   3     3                              data  1, 3, 5
   3     3                              keys  2, 4, 6
   3     3                              next  1, 3, 5
```

# Structured programming

## If/Then/Else statements

In parallel processing both the ``then`` and ``else`` clauses will be executed
in parallel, simultaneously, regardless of the value of the if condition.  But
we can then ``and`` the condition with the results of each clause and ``or``
these results together to consolidate the results and make subsequent
processing dependent on just one branch. In effect we are converting
conventional [code](https://en.wikipedia.org/wiki/Computer_program): 
```
if (c)
  result = 1
else
  result = 2
```

to parallel [code](https://en.wikipedia.org/wiki/Computer_program) where ``&`` means run in parallel:

```
(a = 1 & b = 2 & C = not c)

result = (a and c) or (b and C);
```

This is essentially the technique used in ``chooseThenElseIfEQ in Chip.java``.

## Switch/Case statements

Use a ladder of ``if`` statements, which is the method used to implement:
``chooseEq in Chip.java``.

## For loops

Often a ``for`` loop can be unrolled because we know how many iterations there
will be in advance.

For example to sum the square roots of the 10 elements of [array](https://en.wikipedia.org/wiki/Dynamic_array) ``a`` we might [write](https://en.wikipedia.org/wiki/Write_(system_call)): 
```
count = 0;
for(i = 1; i <= 10; ++i)
  count += sqrt(a[i]);
```

But we could just as well unroll the loop and [write](https://en.wikipedia.org/wiki/Write_(system_call)): 
```
0+sqrt(a[1])+sqrt(sqrt(a[2])+sqrt(a[3])+ ... sqrt(a[10])
```

We can generate this [code](https://en.wikipedia.org/wiki/Computer_program) by writing:

```
String s  = "0"
for(i = 1; i <= 10; ++i)
  s  += "+sqrt(a["+i+"])"
print(s)

// 0+sqrt(a[1])+sqrt(sqrt(a[2])+ ... sqrt(a[10])
```

If you need to generate names of [variables](https://en.wikipedia.org/wiki/Variable_(computer_science)) inside the loop body, use a name
plus an index, as in:

```
n(i, "a")
```

which will generate names like: ``a_1, a_2, a_3 ...`` which can be used in
expressions as needed.

The [gcc](https://en.wikipedia.org/wiki/GNU_Compiler_Collection) compiler uses the same technique when you specify ``-O3`` requesting [code](https://en.wikipedia.org/wiki/Computer_program) optimized for [speed](https://en.wikipedia.org/wiki/Speed) because it eliminates the overhead of maintaining and
checking the index [variable](https://en.wikipedia.org/wiki/Variable_(computer_science)) ``i`` which can be significant on tight loops.

If the number of iterations is unknown in advance, or the generated [code](https://en.wikipedia.org/wiki/Computer_program) would
be too large, you either have to use a pulse and a register to save results and
coordinate reuse of the [Silicon](https://en.wikipedia.org/wiki/Silicon) - difficult - see ``test_fibonacci in
Chip.java`` - or go up to the RiscV layer, see ``RiscV.java`` and [write](https://en.wikipedia.org/wiki/Write_(system_call)) it in
conventional, but much slower, [assembler](https://en.wikipedia.org/wiki/Assembly_language#Assembler) [code](https://en.wikipedia.org/wiki/Computer_program). I.e. one can do addition in [software](https://en.wikipedia.org/wiki/Software) or [hardware](https://en.wikipedia.org/wiki/Digital_electronics). Our goal is to do as much as possible in [hardware](https://en.wikipedia.org/wiki/Digital_electronics) with
the goal of producing a [database](https://en.wikipedia.org/wiki/Database) system on a [chip](https://en.wikipedia.org/wiki/Integrated_circuit) that is much faster and more
power efficient than any-one else's because they are all written in [software](https://en.wikipedia.org/wiki/Software). 
## Subroutines

There are no subroutines at the [Register Transfer Level](https://en.wikipedia.org/wiki/Register-transfer_level) .  All notional subroutines
have to be inlined. This precludes the use of recursion.  However, there is
nothing stopping you from using subroutines, with or without recursion, at the [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) level to generate the [Register Transfer Level](https://en.wikipedia.org/wiki/Register-transfer_level) [code](https://en.wikipedia.org/wiki/Computer_program) needed to implement an algorithm.

And you can also use ``for`` loops to reuse [code](https://en.wikipedia.org/wiki/Computer_program) to obtain a similar effect to
calling a subroutine.

# Diagnostics

## Trace

Execution traces show how the state of the [chip](https://en.wikipedia.org/wiki/Integrated_circuit) evolves over time.

```
   {final int N = 16;
    Chip      c = chip ();
    Bits      p = c.bits("p", N);
    for (int i  = 1; i <= N; i++)
      c.pulse(p.b(i).name).period(N).delay(i-1).b();

    c.executionTrace = c.new Trace("p")
     {String trace() {return String.format("%s", p);}
     };

    c.simulationSteps(20);
    c.simulate();
    //c.printExecutionTrace(); stop();

    c.ok("""
Step  p
   1  0x1
   2  0x2
   3  0x4
   4  0x8
   5  0x10
   6  0x20
   7  0x40
   8  0x80
   9  0x100
  10  0x200
  11  0x400
  12  0x800
  13  0x1000
  14  0x2000
  15  0x4000
  16  0x8000
  17  0x1
  18  0x2
  19  0x4
  20  0x8
""");
```

## State

Use the ``say(Chip s)`` method to print the current state of the [chip](https://en.wikipedia.org/wiki/Integrated_circuit): 
```
Chip: binary_add  Step: 5 # Gates: 70  Maximum distance: 7  MostCountedDistance: 4  CountAtMostCountedDistance: 16
Seq   Name____________________________ S  Operator  #  11111111111111111111111111111111=#  22222222222222222222222222222222=# Chng Fell Frst Last  Dist                           Nearest  Px__,Py__  Drives these gates
  48                                co      Output  1                        ij_carry_2=1                                  =.    5    0     0    0     0                                co     0,   0
   1                               i_1        Zero  0                                  =.                                  =.    1    0     0    0     3               ij_not_in1_1_anneal     0,   0  i_1_f_1, i_1_f_2
  49                           i_1_f_1      FanOut  0                               i_1=0                                  =.    1    0     0    0     2                               o_2     0,   0  ij_1, ij_carry_1
  50                           i_1_f_2      FanOut  0                               i_1=0                                  =.    1    0     0    0     2               ij_not_in1_1_anneal     0,   0  ij_not_in1_1
   2                               i_2         One  1                                  =.                                  =.    1    0     0    0     6                                co     0,   0  i_2_f_1, i_2_f_2
  51                           i_2_f_1      FanOut  1                               i_2=1                                  =.    1    0     0    0     6                                co     0,   0  i_2_f_1_f_1, i_2_f_1_f_2
  53                       i_2_f_1_f_1      FanOut  1                           i_2_f_1=1                                  =.    1    0     0    0     5                                co     0,   0  ij_carry_2_4_F_1, ij_carry_2_7_F_1
  54                       i_2_f_1_f_2      FanOut  1                           i_2_f_1=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_carry_2_8_F_1, ij_not_in1_2
  52                           i_2_f_2      FanOut  1                               i_2=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_result_2_3_F_1, ij_result_2_8_F_1
  11                              ij_1         Xor  1                           i_1_f_1=0                           j_1_f_1=1    2    0     0    0     1                               o_1     0,   0  o_1
  30                              ij_2          Or  0                          ij_2_F_1=0                          ij_2_F_2=0    5    0     0    0     1                               o_2     0,   0  o_2
  28                          ij_2_F_1          Or  0                     ij_result_2_2=0                     ij_result_2_3=0    4    0     0    0     2                               o_2     0,   0  ij_2
  29                          ij_2_F_2          Or  0                     ij_result_2_5=0                     ij_result_2_8=0    3    0     0    0     2                               o_2     0,   0  ij_2
  12                        ij_carry_1         And  0                           i_1_f_1=0                           j_1_f_1=1    1    0     0    0     6                                co     0,   0  ij_carry_1_f_1, ij_carry_1_f_2
  55                    ij_carry_1_f_1      FanOut  0                        ij_carry_1=0                                  =.    1    0     0    0     6                                co     0,   0  ij_carry_1_f_1_f_1, ij_carry_1_f_1_f_2
  57                ij_carry_1_f_1_f_1      FanOut  0                    ij_carry_1_f_1=0                                  =.    1    0     0    0     5                                co     0,   0  ij_carry_2_6_F_1, ij_carry_2_7_F_1
  58                ij_carry_1_f_1_f_2      FanOut  0                    ij_carry_1_f_1=0                                  =.    1    0     0    0     5                               o_2     0,   0  ij_carry_2_8_F_1, ij_not_carry_1
  56                    ij_carry_1_f_2      FanOut  0                        ij_carry_1=0                                  =.    1    0     0    0     5                               o_2     0,   0  ij_result_2_5_F_1, ij_result_2_8_F_1
  45                        ij_carry_2          Or  1                    ij_carry_2_F_1=1                    ij_carry_2_F_2=0    4    0     0    0     1             ij_not_carry_2_anneal     0,   0  co, ij_not_carry_2
  33                      ij_carry_2_4         And  1                  ij_carry_2_4_F_1=1                  ij_carry_2_4_F_2=1    3    0     0    0     3                                co     0,   0  ij_carry_2_F_1
  31                  ij_carry_2_4_F_1         And  1                       i_2_f_1_f_1=1                ij_not_carry_1_f_1=1    2    0     0    0     4                                co     0,   0  ij_carry_2_4
  32                  ij_carry_2_4_F_2    Continue  1                       j_2_f_1_f_1=1                                  =.    2    0     0    0     4                                co     0,   0  ij_carry_2_4
  36                      ij_carry_2_6         And  0                  ij_carry_2_6_F_1=0                  ij_carry_2_6_F_2=1    2    0     0    0     3                                co     0,   0  ij_carry_2_F_1
  34                  ij_carry_2_6_F_1         And  0                ij_carry_1_f_1_f_1=0                  ij_not_in1_2_f_1=0    1    0     0    0     4                                co     0,   0  ij_carry_2_6
  35                  ij_carry_2_6_F_2    Continue  1                       j_2_f_1_f_1=1                                  =.    2    0     0    0     4                                co     0,   0  ij_carry_2_6
  39                      ij_carry_2_7         And  0                  ij_carry_2_7_F_1=0                  ij_carry_2_7_F_2=0    2    0     0    0     3                                co     0,   0  ij_carry_2_F_2
  37                  ij_carry_2_7_F_1         And  0                       i_2_f_1_f_1=1                ij_carry_1_f_1_f_1=0    1    0     0    0     4                                co     0,   0  ij_carry_2_7
  38                  ij_carry_2_7_F_2    Continue  0                  ij_not_in2_2_f_1=0                                  =.    3    0     0    0     4                                co     0,   0  ij_carry_2_7
  42                      ij_carry_2_8         And  0                  ij_carry_2_8_F_1=0                  ij_carry_2_8_F_2=1    2    0     0    0     3                                co     0,   0  ij_carry_2_F_2
  40                  ij_carry_2_8_F_1         And  0                       i_2_f_1_f_2=1                ij_carry_1_f_1_f_2=0    1    0     0    0     4                                co     0,   0  ij_carry_2_8
  41                  ij_carry_2_8_F_2    Continue  1                       j_2_f_1_f_2=1                                  =.    2    0     0    0     4                                co     0,   0  ij_carry_2_8
  43                    ij_carry_2_F_1          Or  1                      ij_carry_2_4=1                      ij_carry_2_6=0    3    0     0    0     2                                co     0,   0  ij_carry_2
  44                    ij_carry_2_F_2          Or  0                      ij_carry_2_7=0                      ij_carry_2_8=0    2    0     0    0     2                                co     0,   0  ij_carry_2
   5                    ij_not_carry_1         Not  1                ij_carry_1_f_1_f_2=0                                  =.    1    0     0    0     6                               o_2     0,   0  ij_not_carry_1_f_1, ij_not_carry_1_f_2
  59                ij_not_carry_1_f_1      FanOut  1                    ij_not_carry_1=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_carry_2_4_F_1, ij_result_2_2_F_1
  60                ij_not_carry_1_f_2      FanOut  1                    ij_not_carry_1=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_result_2_3_F_1
   6                    ij_not_carry_2         Not  0                        ij_carry_2=1                                  =.    4    0     0    0     1             ij_not_carry_2_anneal     0,   0  ij_not_carry_2_anneal
  15             ij_not_carry_2_anneal      Output  0                    ij_not_carry_2=0                                  =.    4    0     0    0     0             ij_not_carry_2_anneal     0,   0
   7                      ij_not_in1_1         Not  1                           i_1_f_2=0                                  =.    1    0     0    0     1               ij_not_in1_1_anneal     0,   0  ij_not_in1_1_anneal
  13               ij_not_in1_1_anneal      Output  1                      ij_not_in1_1=1                                  =.    1    0     0    0     0               ij_not_in1_1_anneal     0,   0
   8                      ij_not_in1_2         Not  0                       i_2_f_1_f_2=1                                  =.    1    0     0    0     6                               o_2     0,   0  ij_not_in1_2_f_1, ij_not_in1_2_f_2
  61                  ij_not_in1_2_f_1      FanOut  0                      ij_not_in1_2=0                                  =.    1    0     0    0     5                               o_2     0,   0  ij_carry_2_6_F_1, ij_result_2_2_F_1
  62                  ij_not_in1_2_f_2      FanOut  0                      ij_not_in1_2=0                                  =.    1    0     0    0     5                               o_2     0,   0  ij_result_2_5_F_1
   9                      ij_not_in2_1         Not  0                           j_1_f_2=1                                  =.    2    0     0    0     1               ij_not_in2_1_anneal     0,   0  ij_not_in2_1_anneal
  14               ij_not_in2_1_anneal      Output  0                      ij_not_in2_1=0                                  =.    2    0     0    0     0               ij_not_in2_1_anneal     0,   0
  10                      ij_not_in2_2         Not  0                       j_2_f_1_f_2=1                                  =.    2    0     0    0     6                               o_2     0,   0  ij_not_in2_2_f_1, ij_not_in2_2_f_2
  63                  ij_not_in2_2_f_1      FanOut  0                      ij_not_in2_2=0                                  =.    2    0     0    0     5                               o_2     0,   0  ij_carry_2_7_F_2, ij_result_2_3_F_2
  64                  ij_not_in2_2_f_2      FanOut  0                      ij_not_in2_2=0                                  =.    2    0     0    0     5                               o_2     0,   0  ij_result_2_5_F_2
  18                     ij_result_2_2         And  0                 ij_result_2_2_F_1=0                 ij_result_2_2_F_2=1    2    0     0    0     3                               o_2     0,   0  ij_2_F_1
  16                 ij_result_2_2_F_1         And  0                ij_not_carry_1_f_1=1                  ij_not_in1_2_f_1=0    1    0     0    0     4                               o_2     0,   0  ij_result_2_2
  17                 ij_result_2_2_F_2    Continue  1                           j_2_f_2=1                                  =.    2    0     0    0     4                               o_2     0,   0  ij_result_2_2
  21                     ij_result_2_3         And  0                 ij_result_2_3_F_1=1                 ij_result_2_3_F_2=0    3    0     0    0     3                               o_2     0,   0  ij_2_F_1
  19                 ij_result_2_3_F_1         And  1                           i_2_f_2=1                ij_not_carry_1_f_2=1    1    0     0    0     4                               o_2     0,   0  ij_result_2_3
  20                 ij_result_2_3_F_2    Continue  0                  ij_not_in2_2_f_1=0                                  =.    2    0     0    0     4                               o_2     0,   0  ij_result_2_3
  24                     ij_result_2_5         And  0                 ij_result_2_5_F_1=0                 ij_result_2_5_F_2=0    2    0     0    0     3                               o_2     0,   0  ij_2_F_2
  22                 ij_result_2_5_F_1         And  0                    ij_carry_1_f_2=0                  ij_not_in1_2_f_2=0    1    0     0    0     4                               o_2     0,   0  ij_result_2_5
  23                 ij_result_2_5_F_2    Continue  0                  ij_not_in2_2_f_2=0                                  =.    2    0     0    0     4                               o_2     0,   0  ij_result_2_5
  27                     ij_result_2_8         And  0                 ij_result_2_8_F_1=0                 ij_result_2_8_F_2=1    2    0     0    0     3                               o_2     0,   0  ij_2_F_2
  25                 ij_result_2_8_F_1         And  0                           i_2_f_2=1                    ij_carry_1_f_2=0    1    0     0    0     4                               o_2     0,   0  ij_result_2_8
  26                 ij_result_2_8_F_2    Continue  1                           j_2_f_2=1                                  =.    2    0     0    0     4                               o_2     0,   0  ij_result_2_8
   3                               j_1         One  1                                  =.                                  =.    1    0     0    0     3               ij_not_in2_1_anneal     0,   0  j_1_f_1, j_1_f_2
  65                           j_1_f_1      FanOut  1                               j_1=1                                  =.    1    0     0    0     2                               o_2     0,   0  ij_1, ij_carry_1
  66                           j_1_f_2      FanOut  1                               j_1=1                                  =.    1    0     0    0     2               ij_not_in2_1_anneal     0,   0  ij_not_in2_1
   4                               j_2         One  1                                  =.                                  =.    1    0     0    0     6                                co     0,   0  j_2_f_1, j_2_f_2
  67                           j_2_f_1      FanOut  1                               j_2=1                                  =.    1    0     0    0     6                                co     0,   0  j_2_f_1_f_1, j_2_f_1_f_2
  69                       j_2_f_1_f_1      FanOut  1                           j_2_f_1=1                                  =.    1    0     0    0     5                                co     0,   0  ij_carry_2_4_F_2, ij_carry_2_6_F_2
  70                       j_2_f_1_f_2      FanOut  1                           j_2_f_1=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_carry_2_8_F_2, ij_not_in2_2
  68                           j_2_f_2      FanOut  1                               j_2=1                                  =.    1    0     0    0     5                               o_2     0,   0  ij_result_2_2_F_2, ij_result_2_8_F_2
  46                               o_1      Output  1                              ij_1=1                                  =.    2    0     0    0     0                               o_1     0,   0
  47                               o_2      Output  0                              ij_2=0                                  =.    5    0     0    0     0                               o_2     0,   0
8 Bit buses
Bits  Bus_____________________________  Value
   2                                 i  2
   2                                ij  1
   2                          ij_carry  2
   2                      ij_not_carry  1
   2                        ij_not_in1  1
   2                        ij_not_in2  0
   2                                 j  3
   2                                 o  1
```

Modified: 2024-07-29 at 03:02:42
