<div>
    <p><a href="https://github.com/philiprbrenan/com.AppaApps.Silicon"><img src="https://github.com/philiprbrenan/com.AppaApps.Silicon/workflows/Test/badge.svg"></a>
</div>

# Silicon chip

Design, simulate and layout a silicon chip made of basic gates.

Reasons why you might want to join this project:

http://prb.appaapps.com/zesal/pitchdeck/pitchDeck.html

# Examples

## And

Create a chips that **and**s two input pins together and places the result on
the output pin.

```
  static void test_and()
   {final Chip   c = new Chip("And");
    final Gate  i1 = c.Input ("i1");
    final Gate  i2 = c.Input ("i2");
    final Gate and = c.And   ("and", "i1", "i2");
    final Gate   o = c.Output("o", "and");

    final Inputs inputs = c.new Inputs();
    inputs.set("i1", true);
    inputs.set("i2", false);

    c.simulate(inputs);

    ok( i1.value, true);
    ok( i2.value, false);
    ok(and.value, false);
    ok(  o.value, false);
    ok(  c.steps ,    2);
   }
```

To produce a chip layout mask using [Graphics Design System 2](https://en.wikipedia.org/wiki/GDSII):

![And](images/And.png)

## Compare Greater Than
![Compare Greater Than](images/CompareGt4.png)

## Choose Word Under Mask
![Choose Word Under Mask](images/ChooseWordUnderMask2.png)
