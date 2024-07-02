<div>
    <p><a href="https://github.com/philiprbrenan/RiscV"><img src="https://github.com/philiprbrenan/RiscV/workflows/Test/badge.svg"></a>
</div>

# Risc V

Emulate the
<a href="https://riscv.org/wp-content/uploads/2019/12/riscv-spec-20191213.pdf">RiscV 32I</a>
instruction set using Java.

For instance, to produce the [Fibonacci](https://en.wikipedia.org/wiki/Fibonacci_number) numbers:

```
RiscV    r = new RiscV();                // New Risc V machine and program
Register z = r.x0;                       // Zero
Register N = r.x1;                       // Number of Fibonacci numbers to produce
Register a = r.x2;                       // A
Register b = r.x3;                       // B
Register c = r.x4;                       // C = A + B
Register i = r.x5;                       // Loop counter

Variable p = r.new Variable("p", 2, 10); // Variable declarations
Variable o = r.new Variable("a", 4, 10); // Block of 40 bytes starting at byte address 20
r.addi(N, z, 10);                        // N = 10
r.addi(i, z, 0);                         // i =  0
r.addi(a, z, 0);                         // a =  0
r.addi(b, z, 1);                         // b =  1
Label start = r.new Label("start");      // Start of for loop
r.sb (i, a, o.at());                     // Save latest result in memory
r.add (c, a, b);                         // Latest Fibonacci number
r.add (a, b, z);                         // Move b to a
r.add (b, c, z);                         // Move c to b
r.addi(i, i, 1);                         // Increment loop count
r.blt (i, N, start);                     // Loop
r.emulate();                             // Run the program

//stop(r);
r.ok("""
RiscV      : fibonacci
Step       : 65
Instruction: 10
Registers  :  x1=10 x2=55 x3=89 x4=89 x5=10
Memory     :  21=1 22=1 23=2 24=3 25=5 26=8 27=13 28=21 29=34
""");

//stop(r.printCode());
ok(r.printCode(), """
RiscV Hex Code: fibonacci
Line      Name    Op   D S1 S2   T  F3 F5 F7  A R  Immediate    Value
0000      addi    13   1  0  a   0   0  0  0  0 0          a   a00093
0001      addi    13   5  0  0   0   0  0  0  0 0          0      293
0002      addi    13   2  0  0   0   0  0  0  0 0          0      113
0003      addi    13   3  0  1   0   0  0  0  0 0          1   100193
0004        sb    23  14  5  2   0   0  0  0  0 0         14   228a23
0005       add    33   4  2  3   0   0  0  0  0 0          0   310233
0006       add    33   2  3  0   0   0  0  0  0 0          0    18133
0007       add    33   3  4  0   0   0  0  0  0 0          0    201b3
0008      addi    13   5  5  1   0   0  0  0  0 0          1   128293
0009       blt    63  17  5  1   4   4 1f 7f  0 0   fffffff6 fe12cbe3
""");
```

Modified: 2024-07-02 at 03:59:31
