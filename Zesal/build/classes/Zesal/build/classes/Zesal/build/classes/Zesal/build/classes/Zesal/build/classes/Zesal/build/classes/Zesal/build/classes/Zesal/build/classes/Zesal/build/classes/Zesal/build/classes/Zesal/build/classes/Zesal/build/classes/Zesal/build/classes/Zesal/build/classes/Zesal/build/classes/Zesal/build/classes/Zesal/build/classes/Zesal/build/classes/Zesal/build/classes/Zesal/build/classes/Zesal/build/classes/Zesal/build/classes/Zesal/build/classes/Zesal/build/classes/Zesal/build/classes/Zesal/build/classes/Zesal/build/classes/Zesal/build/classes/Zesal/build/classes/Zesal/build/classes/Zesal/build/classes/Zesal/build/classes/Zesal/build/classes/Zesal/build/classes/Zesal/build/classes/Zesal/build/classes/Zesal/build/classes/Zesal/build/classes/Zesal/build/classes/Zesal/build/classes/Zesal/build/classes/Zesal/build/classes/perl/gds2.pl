use v5.34;
use Data::Table::Text qw(:all);
use Data::Dump qw(dump);
use GDS2;
clearFolder("gds/", 999);
if (1)
 {my $gdsOut = "and";
  my @debug; my $debug = 0;
  my $f = "gds/$gdsOut.gds";
  push @debug, "Chip: $gdsOut" if $debug;
  createEmptyFile($f);
  my $g = new GDS2(-fileName=>">$f");
  $g->printInitLib(-name=>$gdsOut);
  $g->printBgnstr (-name=>$gdsOut);
  $g->printText(-layer=>102, -xy=>[8, 32],  -string=>"and, gsx=1, gsy=1, gates=4, levels=1");
# Gate: 0
  if (1)
   {my $gsx = 1;
    my $gsy = 1;
    my $x   = 4 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 1;
    my $Y   = $y + 6 * 1;
    my $n   = "I";
    my $o   = "Input";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"I Input");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 1;
    my $gsy = 1;
    my $x   = 2 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 1;
    my $Y   = $y + 6 * 1;
    my $n   = "a";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"a And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 1;
    my $gsy = 1;
    my $x   = 4 * 4;
    my $y   = 3 * 4;
    my $X   = $x + 6 * 1;
    my $Y   = $y + 6 * 1;
    my $n   = "i";
    my $o   = "Input";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i Input");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 1;
    my $gsy = 1;
    my $x   = 0 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 1;
    my $Y   = $y + 6 * 1;
    my $n   = "o";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o Output");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Wire:   16    4    12    4    1

  if (1)
   {my $L = 4;
    my $x = 16;
    my $y = 4;
    my $X = 12;
    my $Y = 4;
    my $s = "I";
    my $t = "a";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 12;
      my $y = 4;
      my $X = $x+5;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:    8    4     4    4    1

  if (1)
   {my $L = 4;
    my $x = 8;
    my $y = 4;
    my $X = 4;
    my $Y = 4;
    my $s = "a";
    my $t = "o";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 4;
      my $y = 4;
      my $X = $x+5;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   16   12    12    8    1

  if (1)
   {my $L = 4;
    my $x = 16;
    my $y = 12;
    my $X = 12;
    my $Y = 8;
    my $s = "i";
    my $t = "a";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 14;
      my $y = 12;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 14;
      my $y = 8;
      my $X = $x+1;
      my $Y = $y+5;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 12;
      my $y = 8;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
  $g->printEndstr;
  $g->printEndlib;
  owf("gds/$gdsOut.txt", join "\n", @debug) if $debug;
 }
if (1)
 {my $gdsOut = "compare_gt_4";
  my @debug; my $debug = 0;
  my $f = "gds/$gdsOut.gds";
  push @debug, "Chip: $gdsOut" if $debug;
  createEmptyFile($f);
  my $g = new GDS2(-fileName=>">$f");
  $g->printInitLib(-name=>$gdsOut);
  $g->printBgnstr (-name=>$gdsOut);
  $g->printText(-layer=>102, -xy=>[24, 160],  -string=>"compare_gt_4, gsx=3, gsy=2, gates=28, levels=1");
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 0 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "O";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O Output");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "i_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "i_2";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 26 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "i_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 26 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "i_4";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 20 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "j_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"j_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 8 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "j_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"j_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 30 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "j_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"j_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 30 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "j_4";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"j_4 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 6 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 12 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 12 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 18 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_2_F_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_2_F_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 6 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_2_F_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_2_F_2 And");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 18 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 10 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_3_F_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_3_F_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_3_F_2";
    my $o   = "Continue";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_3_F_2 Continue");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 18 * 4;
    my $y   = 20 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_compare_4";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_compare_4 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_equal_2";
    my $o   = "Nxor";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_equal_2 Nxor");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 10 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_equal_3";
    my $o   = "Nxor";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_equal_3 Nxor");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 22 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_equal_4";
    my $o   = "Nxor";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_equal_4 Nxor");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_equal_4_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_equal_4_f_1 FanOut");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_equal_4_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_equal_4_f_2 FanOut");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 6 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_greater_1";
    my $o   = "Gt";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_greater_1 Gt");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 30 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_greater_2";
    my $o   = "Gt";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_greater_2 Gt");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 22 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_greater_3";
    my $o   = "Gt";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_greater_3 Gt");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 2;
    my $x   = 18 * 4;
    my $y   = 8 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 2;
    my $n   = "o_greater_4";
    my $o   = "Gt";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_greater_4 Gt");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Wire:  144   64   132   24    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 64;
    my $X = 132;
    my $Y = 24;
    my $s = "i_1";
    my $t = "o_greater_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 64;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 24;
      my $X = $x+1;
      my $Y = $y+41;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 24;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144    8   132    8    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 8;
    my $X = 132;
    my $Y = 8;
    my $s = "i_2";
    my $t = "o_equal_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 132;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144   16   132   72    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 16;
    my $X = 132;
    my $Y = 72;
    my $s = "i_2";
    my $t = "o_greater_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 18;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 18;
      my $X = $x+1;
      my $Y = $y+55;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 72;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 18;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120  112   132   40    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 112;
    my $X = 132;
    my $Y = 40;
    my $s = "i_3";
    my $t = "o_equal_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 112;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 130;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+73;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 130;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120  104   108   88    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 104;
    my $X = 108;
    my $Y = 88;
    my $s = "i_3";
    my $t = "o_greater_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 118;
      my $y = 104;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 118;
      my $y = 88;
      my $X = $x+1;
      my $Y = $y+17;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 88;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96  112   132   88    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 112;
    my $X = 132;
    my $Y = 88;
    my $s = "i_4";
    my $t = "o_equal_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 112;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 98;
      my $y = 94;
      my $X = $x+1;
      my $Y = $y+19;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 98;
      my $y = 94;
      my $X = $x+35;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 88;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 98;
      my $y = 94;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96  104    84   32    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 104;
    my $X = 84;
    my $Y = 32;
    my $s = "i_4";
    my $t = "o_greater_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 104;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+73;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 32;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144   80   132   32    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 80;
    my $X = 132;
    my $Y = 32;
    my $s = "j_1";
    my $t = "o_greater_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 78;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 78;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+47;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 32;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 78;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  144   32   132   16    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 32;
    my $X = 132;
    my $Y = 16;
    my $s = "j_2";
    my $t = "o_equal_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 30;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 30;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+15;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 16;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 134;
      my $y = 30;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  144   40   132   80    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 40;
    my $X = 132;
    my $Y = 80;
    my $s = "j_2";
    my $t = "o_greater_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 146;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+27;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 66;
      my $X = $x+5;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 66;
      my $X = $x+1;
      my $Y = $y+15;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 80;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 146;
      my $y = 66;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 142;
      my $y = 66;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120  120   132   48    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 120;
    my $X = 132;
    my $Y = 48;
    my $s = "j_3";
    my $t = "o_equal_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 120;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 122;
      my $y = 54;
      my $X = $x+1;
      my $Y = $y+67;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 122;
      my $y = 54;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 48;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 122;
      my $y = 54;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120  128   108   96    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 128;
    my $X = 108;
    my $Y = 96;
    my $s = "j_3";
    my $t = "o_greater_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 126;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 126;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 96;
      my $X = $x+1;
      my $Y = $y+31;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 96;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 126;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96  120   132   96    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 120;
    my $X = 132;
    my $Y = 96;
    my $s = "j_4";
    my $t = "o_equal_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 120;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 102;
      my $y = 102;
      my $X = $x+1;
      my $Y = $y+19;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 102;
      my $y = 102;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 96;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 102;
      my $y = 102;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96  128    84   40    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 128;
    my $X = 84;
    my $Y = 40;
    my $s = "j_4";
    my $t = "o_greater_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 126;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 126;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+87;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 40;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 126;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   24    8    12    8    1

  if (1)
   {my $L = 4;
    my $x = 24;
    my $y = 8;
    my $X = 12;
    my $Y = 8;
    my $s = "o";
    my $t = "O";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 12;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48    8    36    8    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 8;
    my $X = 36;
    my $Y = 8;
    my $s = "o_F_1";
    my $t = "o";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 36;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48   72    36   16    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 72;
    my $X = 36;
    my $Y = 16;
    my $s = "o_F_2";
    my $t = "o";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 46;
      my $y = 72;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 46;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+57;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 36;
      my $y = 16;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72    8    60    8    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 8;
    my $X = 60;
    my $Y = 8;
    my $s = "o_compare_2";
    my $t = "o_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96    8    84    8    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 8;
    my $X = 84;
    my $Y = 8;
    my $s = "o_compare_2_F_1";
    my $t = "o_compare_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   24    84   16    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 24;
    my $X = 84;
    my $Y = 16;
    my $s = "o_compare_2_F_2";
    my $t = "o_compare_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 16;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   64    60   72    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 64;
    my $X = 60;
    my $Y = 72;
    my $s = "o_compare_3";
    my $t = "o_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 70;
      my $y = 64;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 70;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 72;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   48    84   56    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 48;
    my $X = 84;
    my $Y = 56;
    my $s = "o_compare_3_F_1";
    my $t = "o_compare_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 48;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 54;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 54;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   64    84   64    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 64;
    my $X = 84;
    my $Y = 64;
    my $s = "o_compare_3_F_2";
    my $t = "o_compare_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 64;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   80    60   80    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 80;
    my $X = 60;
    my $Y = 80;
    my $s = "o_compare_4";
    my $t = "o_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 80;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120    8   108    8    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 8;
    my $X = 108;
    my $Y = 8;
    my $s = "o_equal_2";
    my $t = "o_compare_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 108;
      my $y = 8;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120   40   108   32    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 40;
    my $X = 108;
    my $Y = 32;
    my $s = "o_equal_3";
    my $t = "o_compare_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 118;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 118;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 32;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120   48   108   48    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 48;
    my $X = 108;
    my $Y = 48;
    my $s = "o_equal_3";
    my $t = "o_compare_3_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 108;
      my $y = 48;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120   88   132   64    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 88;
    my $X = 132;
    my $Y = 64;
    my $s = "o_equal_4";
    my $t = "o_equal_4_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 88;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 126;
      my $y = 70;
      my $X = $x+1;
      my $Y = $y+19;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 126;
      my $y = 70;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 132;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 126;
      my $y = 70;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120   96   108   80    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 96;
    my $X = 108;
    my $Y = 80;
    my $s = "o_equal_4";
    my $t = "o_equal_4_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 96;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 98;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 80;
      my $X = $x+1;
      my $Y = $y+19;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 80;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 98;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120   64   108   24    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 64;
    my $X = 108;
    my $Y = 24;
    my $s = "o_equal_4_f_1";
    my $t = "o_compare_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 62;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 62;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 24;
      my $X = $x+1;
      my $Y = $y+39;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 24;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 114;
      my $y = 62;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  120   56   108   56    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 56;
    my $X = 108;
    my $Y = 56;
    my $s = "o_equal_4_f_1";
    my $t = "o_compare_3_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 108;
      my $y = 56;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   80    84   80    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 80;
    my $X = 84;
    my $Y = 80;
    my $s = "o_equal_4_f_2";
    my $t = "o_compare_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 80;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120   24   108   16    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 24;
    my $X = 108;
    my $Y = 16;
    my $s = "o_greater_1";
    my $t = "o_compare_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 118;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 118;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 16;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  120   80   108   40    1

  if (1)
   {my $L = 4;
    my $x = 120;
    my $y = 80;
    my $X = 108;
    my $Y = 40;
    my $s = "o_greater_2";
    my $t = "o_compare_3_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 78;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 78;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+39;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 108;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 110;
      my $y = 78;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   88    84   88    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 88;
    my $X = 84;
    my $Y = 88;
    my $s = "o_greater_3";
    my $t = "o_compare_4";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 88;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   32    60   16    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 32;
    my $X = 60;
    my $Y = 16;
    my $s = "o_greater_4";
    my $t = "o_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 70;
      my $y = 32;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 70;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+17;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 16;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
  $g->printEndstr;
  $g->printEndlib;
  owf("gds/$gdsOut.txt", join "\n", @debug) if $debug;
 }
if (1)
 {my $gdsOut = "choose_word_under_mask_2";
  my @debug; my $debug = 0;
  my $f = "gds/$gdsOut.gds";
  push @debug, "Chip: $gdsOut" if $debug;
  createEmptyFile($f);
  my $g = new GDS2(-fileName=>">$f");
  $g->printInitLib(-name=>$gdsOut);
  $g->printBgnstr (-name=>$gdsOut);
  $g->printText(-layer=>102, -xy=>[24, 112],  -string=>"choose_word_under_mask_2, gsx=3, gsy=1, gates=28, levels=1");
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 0 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "O_1";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O_1 Output");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 0 * 4;
    my $y   = 13 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "O_2";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O_2 Output");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_1_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 5 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_1_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 7 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_2_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 11 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_2_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 13 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_3_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 17 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_3_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 19 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_4_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 23 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "i_4_2";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4_2 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 3 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "m_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 9 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "m_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 15 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "m_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 24 * 4;
    my $y   = 21 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "m_4";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_4 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 6 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 12 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_1_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 12 * 4;
    my $y   = 6 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_1_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 6 * 4;
    my $y   = 13 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 12 * 4;
    my $y   = 11 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_2_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 12 * 4;
    my $y   = 16 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_2_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 1 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_1_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_1_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 9 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_1_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_1_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 3 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_2_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_2_1 And");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 11 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_2_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_2_2 And");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 5 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_3_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_3_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 13 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_3_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_3_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 7 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_4_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_4_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 3;
    my $gsy = 1;
    my $x   = 18 * 4;
    my $y   = 15 * 4;
    my $X   = $x + 6 * 3;
    my $Y   = $y + 6 * 1;
    my $n   = "o_a_4_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_4_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Wire:   96    4    84    4    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 4;
    my $X = 84;
    my $Y = 4;
    my $s = "i_1_1";
    my $t = "o_a_1_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 4;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   24    84   36    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 24;
    my $X = 84;
    my $Y = 36;
    my $s = "i_1_2";
    my $t = "o_a_1_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 24;
      my $X = $x+1;
      my $Y = $y+13;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 36;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   28    84   16    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 28;
    my $X = 84;
    my $Y = 16;
    my $s = "i_2_1";
    my $t = "o_a_2_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 26;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 26;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+11;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 16;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 26;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   44    84   44    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 44;
    my $X = 84;
    my $Y = 44;
    my $s = "i_2_2";
    my $t = "o_a_2_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 84;
      my $y = 44;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   52    84   24    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 52;
    my $X = 84;
    my $Y = 24;
    my $s = "i_3_1";
    my $t = "o_a_3_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 50;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 50;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 24;
      my $X = $x+1;
      my $Y = $y+27;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 50;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   68    84   56    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 68;
    my $X = 84;
    my $Y = 56;
    my $s = "i_3_2";
    my $t = "o_a_3_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 68;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 56;
      my $X = $x+1;
      my $Y = $y+13;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 56;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   76    84   32    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 76;
    my $X = 84;
    my $Y = 32;
    my $s = "i_4_1";
    my $t = "o_a_4_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 74;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 74;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+43;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 32;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 90;
      my $y = 74;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   92    84   64    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 92;
    my $X = 84;
    my $Y = 64;
    my $s = "i_4_2";
    my $t = "o_a_4_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 90;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 90;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+27;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 64;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 90;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   12    84    8    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 12;
    my $X = 84;
    my $Y = 8;
    my $s = "m_1";
    my $t = "o_a_1_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 12;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 8;
      my $X = $x+1;
      my $Y = $y+5;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 8;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96   16    84   40    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 16;
    my $X = 84;
    my $Y = 40;
    my $s = "m_1";
    my $t = "o_a_1_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 18;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 18;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 18;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   40    84   12    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 40;
    my $X = 84;
    my $Y = 12;
    my $s = "m_2";
    my $t = "o_a_2_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 98;
      my $y = 14;
      my $X = $x+1;
      my $Y = $y+27;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 14;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 12;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 98;
      my $y = 14;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   36    84   48    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 36;
    my $X = 84;
    my $Y = 48;
    my $s = "m_2";
    my $t = "o_a_2_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 36;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 38;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 38;
      my $X = $x+1;
      my $Y = $y+11;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 48;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 38;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   64    84   20    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 64;
    my $X = 84;
    my $Y = 20;
    my $s = "m_3";
    my $t = "o_a_3_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 64;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 102;
      my $y = 22;
      my $X = $x+1;
      my $Y = $y+43;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 22;
      my $X = $x+9;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 20;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 20;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 102;
      my $y = 22;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 94;
      my $y = 22;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   60    84   52    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 60;
    my $X = 84;
    my $Y = 52;
    my $s = "m_3";
    my $t = "o_a_3_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 58;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 58;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 52;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 52;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 86;
      my $y = 58;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   88    84   28    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 88;
    my $X = 84;
    my $Y = 28;
    my $s = "m_4";
    my $t = "o_a_4_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 86;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 78;
      my $y = 86;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 78;
      my $y = 30;
      my $X = $x+1;
      my $Y = $y+57;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 78;
      my $y = 30;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 84;
      my $y = 28;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 78;
      my $y = 86;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 78;
      my $y = 30;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96   84    84   60    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 84;
    my $X = 84;
    my $Y = 60;
    my $s = "m_4";
    my $t = "o_a_4_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 96;
      my $y = 82;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 82;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 60;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 60;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 82;
      my $y = 82;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   24    4    12    4    1

  if (1)
   {my $L = 4;
    my $x = 24;
    my $y = 4;
    my $X = 12;
    my $Y = 4;
    my $s = "o_1";
    my $t = "O_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 12;
      my $y = 4;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48    4    36    4    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 4;
    my $X = 36;
    my $Y = 4;
    my $s = "o_1_F_1";
    my $t = "o_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 36;
      my $y = 4;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48   24    36    8    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 24;
    my $X = 36;
    my $Y = 8;
    my $s = "o_1_F_2";
    my $t = "o_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 46;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 46;
      my $y = 8;
      my $X = $x+1;
      my $Y = $y+17;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 36;
      my $y = 8;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   24   52    12   52    1

  if (1)
   {my $L = 4;
    my $x = 24;
    my $y = 52;
    my $X = 12;
    my $Y = 52;
    my $s = "o_2";
    my $t = "O_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 12;
      my $y = 52;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48   48    36   52    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 48;
    my $X = 36;
    my $Y = 52;
    my $s = "o_2_F_1";
    my $t = "o_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 46;
      my $y = 48;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 46;
      my $y = 48;
      my $X = $x+1;
      my $Y = $y+5;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 36;
      my $y = 52;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48   64    36   56    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 64;
    my $X = 36;
    my $Y = 56;
    my $s = "o_2_F_2";
    my $t = "o_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 46;
      my $y = 64;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 46;
      my $y = 56;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 36;
      my $y = 56;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72    4    60    4    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 4;
    my $X = 60;
    my $Y = 4;
    my $s = "o_a_1_1";
    my $t = "o_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 4;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   40    60   44    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 40;
    my $X = 60;
    my $Y = 44;
    my $s = "o_a_1_2";
    my $t = "o_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 70;
      my $y = 40;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 70;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+5;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 44;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   12    60    8    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 12;
    my $X = 60;
    my $Y = 8;
    my $s = "o_a_2_1";
    my $t = "o_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 70;
      my $y = 12;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 70;
      my $y = 8;
      my $X = $x+1;
      my $Y = $y+5;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 8;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   48    60   48    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 48;
    my $X = 60;
    my $Y = 48;
    my $s = "o_a_2_2";
    my $t = "o_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 48;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   24    60   24    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 24;
    my $X = 60;
    my $Y = 24;
    my $s = "o_a_3_1";
    my $t = "o_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 24;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   56    60   64    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 56;
    my $X = 60;
    my $Y = 64;
    my $s = "o_a_3_2";
    my $t = "o_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 70;
      my $y = 56;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 70;
      my $y = 56;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 64;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   28    60   28    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 28;
    my $X = 60;
    my $Y = 28;
    my $s = "o_a_4_1";
    my $t = "o_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 60;
      my $y = 28;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   72   64    60   68    1

  if (1)
   {my $L = 4;
    my $x = 72;
    my $y = 64;
    my $X = 60;
    my $Y = 68;
    my $s = "o_a_4_2";
    my $t = "o_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 72;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 66;
      my $X = $x+13;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 60;
      my $y = 66;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
  $g->printEndstr;
  $g->printEndlib;
  owf("gds/$gdsOut.txt", join "\n", @debug) if $debug;
 }
if (1)
 {my $gdsOut = "choose_word_under_mask_3";
  my @debug; my $debug = 0;
  my $f = "gds/$gdsOut.gds";
  push @debug, "Chip: $gdsOut" if $debug;
  createEmptyFile($f);
  my $g = new GDS2(-fileName=>">$f");
  $g->printInitLib(-name=>$gdsOut);
  $g->printBgnstr (-name=>$gdsOut);
  $g->printText(-layer=>102, -xy=>[48, 672],  -string=>"choose_word_under_mask_3, gsx=6, gsy=2, gates=96, levels=1");
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 0 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "O_1";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O_1 Output");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 0 * 4;
    my $y   = 54 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "O_2";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O_2 Output");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 0 * 4;
    my $y   = 106 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "O_3";
    my $o   = "Output";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"O_3 Output");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_1_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 10 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_1_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_1_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_1_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 22 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_2_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 30 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_2_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 34 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_2_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_2_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 42 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_3_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 50 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_3_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 54 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_3_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_3_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 62 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_4_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 70 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_4_2";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4_2 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 74 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_4_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_4_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 82 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_5_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_5_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 90 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_5_2";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_5_2 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 94 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_5_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_5_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 102 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_6_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_6_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 110 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_6_2";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_6_2 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 114 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_6_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_6_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 122 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_7_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_7_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 130 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_7_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_7_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 134 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_7_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_7_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 142 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_8_1";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_8_1 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 150 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_8_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_8_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 154 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "i_8_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"i_8_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_1";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_1 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 6 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_1_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_1_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_1_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_1_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_2";
    my $o   = "One";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_2 One");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 26 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_2_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_2_f_1 FanOut");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 38 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_2_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_2_f_2 FanOut");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 36 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_3";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_3 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 46 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_3_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_3_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 58 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_3_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_3_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 52 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_4";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_4 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 66 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_4_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_4_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 78 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_4_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_4_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 70 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_5";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_5 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 86 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_5_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_5_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 98 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_5_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_5_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 86 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_6";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_6 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 106 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_6_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_6_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 118 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_6_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_6_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 104 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_7";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_7 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 126 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_7_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_7_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 138 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_7_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_7_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 72 * 4;
    my $y   = 120 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_8";
    my $o   = "Zero";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_8 Zero");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 146 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_8_f_1";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_8_f_1 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 60 * 4;
    my $y   = 158 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "m_8_f_2";
    my $o   = "FanOut";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"m_8_f_2 FanOut");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 12 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_1_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_1_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 12 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_1_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_1_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 26 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 22 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_2_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_2_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 32 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_1_F_2_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_1_F_2_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 12 * 4;
    my $y   = 54 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 50 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 42 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_1_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_1_F_1 Or");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 52 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_1_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_1_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 74 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 62 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_2_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_2_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 72 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_2_F_2_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_2_F_2_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 12 * 4;
    my $y   = 106 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 98 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 82 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_1_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_1_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 92 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_1_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_1_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 24 * 4;
    my $y   = 122 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 102 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_2_F_1";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_2_F_1 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 36 * 4;
    my $y   = 112 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_3_F_2_F_2";
    my $o   = "Or";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_3_F_2_F_2 Or");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 2 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_1_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_1_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 34 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_1_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_1_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 66 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_1_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_1_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 6 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_2_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_2_1 And");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 1
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 38 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_2_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_2_2 And");
    $g->printBoundary(-layer=>101, -xy=>[$x+1/3,$Y-1, $x+2/3,$Y-1, $x+2/3,$Y, $x+1/3,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 70 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_2_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_2_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 10 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_3_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_3_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 42 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_3_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_3_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 74 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_3_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_3_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 14 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_4_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_4_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 46 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_4_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_4_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 78 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_4_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_4_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 18 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_5_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_5_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 50 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_5_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_5_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 82 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_5_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_5_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 22 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_6_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_6_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 54 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_6_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_6_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 86 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_6_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_6_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 26 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_7_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_7_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 58 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_7_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_7_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 90 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_7_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_7_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 30 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_8_1";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_8_1 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 62 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_8_2";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_8_2 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Gate: 0
  if (1)
   {my $gsx = 6;
    my $gsy = 2;
    my $x   = 48 * 4;
    my $y   = 94 * 4;
    my $X   = $x + 6 * 6;
    my $Y   = $y + 6 * 2;
    my $n   = "o_a_8_3";
    my $o   = "And";
    push @debug, sprintf("Gate         %4d %4d %4d %4d  %8s  %s", $x, $y, $X, $Y, $o, $n) if $debug;
    $g->printBoundary(-layer=>0, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
    $g->printText(-xy=>[($x+$X)/2, ($y+$Y)/2], -string=>"o_a_8_3 And");
    $g->printBoundary(-layer=>100, -xy=>[$x,$Y-1, $x+1,$Y-1, $x+1,$Y, $x,$Y]);
   }
# Wire:  240    8   216    8    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 8;
    my $X = 216;
    my $Y = 8;
    my $s = "i_1_1";
    my $t = "o_a_1_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 216;
      my $y = 8;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240   48   216  136    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 48;
    my $X = 216;
    my $Y = 136;
    my $s = "i_1_2";
    my $t = "o_a_1_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 48;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 48;
      my $X = $x+1;
      my $Y = $y+89;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 136;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240   64   216  264    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 64;
    my $X = 216;
    my $Y = 264;
    my $s = "i_1_3";
    my $t = "o_a_1_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 66;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 66;
      my $X = $x+1;
      my $Y = $y+199;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 264;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 66;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240   88   216   32    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 88;
    my $X = 216;
    my $Y = 32;
    my $s = "i_2_1";
    my $t = "o_a_2_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 86;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 86;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+55;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 32;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 86;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  128   216  152    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 128;
    my $X = 216;
    my $Y = 152;
    my $s = "i_2_2";
    my $t = "o_a_2_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 128;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 130;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 130;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 152;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 130;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  144   216  280    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 144;
    my $X = 216;
    my $Y = 280;
    my $s = "i_2_3";
    my $t = "o_a_2_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 144;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 144;
      my $X = $x+1;
      my $Y = $y+137;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 280;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240  168   216   48    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 168;
    my $X = 216;
    my $Y = 48;
    my $s = "i_3_1";
    my $t = "o_a_3_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 166;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 166;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 48;
      my $X = $x+1;
      my $Y = $y+119;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 48;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 166;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  200   216  176    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 200;
    my $X = 216;
    my $Y = 176;
    my $s = "i_3_2";
    my $t = "o_a_3_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 198;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 198;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 176;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 176;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 198;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  224   216  296    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 224;
    my $X = 216;
    my $Y = 296;
    my $s = "i_3_3";
    my $t = "o_a_3_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 224;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 226;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 226;
      my $X = $x+1;
      my $Y = $y+71;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 296;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 226;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  248   216   64    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 248;
    my $X = 216;
    my $Y = 64;
    my $s = "i_4_1";
    my $t = "o_a_4_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 246;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 246;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 64;
      my $X = $x+1;
      my $Y = $y+183;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 64;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 246;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  280   216  192    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 280;
    my $X = 216;
    my $Y = 192;
    my $s = "i_4_2";
    my $t = "o_a_4_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 278;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 278;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 192;
      my $X = $x+1;
      my $Y = $y+87;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 192;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 278;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  304   216  312    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 304;
    my $X = 216;
    my $Y = 312;
    my $s = "i_4_3";
    my $t = "o_a_4_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 304;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 304;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 312;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240  328   216   80    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 328;
    my $X = 216;
    my $Y = 80;
    my $s = "i_5_1";
    my $t = "o_a_5_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 326;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 326;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 80;
      my $X = $x+1;
      my $Y = $y+247;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 80;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 326;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  360   216  208    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 360;
    my $X = 216;
    my $Y = 208;
    my $s = "i_5_2";
    my $t = "o_a_5_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 358;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 358;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 208;
      my $X = $x+1;
      my $Y = $y+151;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 208;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 358;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  376   216  336    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 376;
    my $X = 216;
    my $Y = 336;
    my $s = "i_5_3";
    my $t = "o_a_5_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 376;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 336;
      my $X = $x+1;
      my $Y = $y+41;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 336;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240  408   216   96    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 408;
    my $X = 216;
    my $Y = 96;
    my $s = "i_6_1";
    my $t = "o_a_6_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 408;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 242;
      my $y = 102;
      my $X = $x+1;
      my $Y = $y+307;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 102;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 96;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 242;
      my $y = 102;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  440   216  224    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 440;
    my $X = 216;
    my $Y = 224;
    my $s = "i_6_2";
    my $t = "o_a_6_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 438;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 438;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 230;
      my $X = $x+1;
      my $Y = $y+209;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 230;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 224;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 438;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 210;
      my $y = 230;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  456   216  352    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 456;
    my $X = 216;
    my $Y = 352;
    my $s = "i_6_3";
    my $t = "o_a_6_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 454;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 454;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 352;
      my $X = $x+1;
      my $Y = $y+103;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 352;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 454;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  488   216  112    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 488;
    my $X = 216;
    my $Y = 112;
    my $s = "i_7_1";
    my $t = "o_a_7_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 488;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 246;
      my $y = 118;
      my $X = $x+1;
      my $Y = $y+371;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 118;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 112;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 246;
      my $y = 118;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  520   216  240    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 520;
    my $X = 216;
    my $Y = 240;
    my $s = "i_7_2";
    my $t = "o_a_7_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 518;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 518;
      my $X = $x+35;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 246;
      my $X = $x+1;
      my $Y = $y+273;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 246;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 240;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 518;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 206;
      my $y = 246;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  536   216  368    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 536;
    my $X = 216;
    my $Y = 368;
    my $s = "i_7_3";
    my $t = "o_a_7_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 534;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 534;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 368;
      my $X = $x+1;
      my $Y = $y+167;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 368;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 534;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  568   216  128    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 568;
    my $X = 216;
    my $Y = 128;
    my $s = "i_8_1";
    my $t = "o_a_8_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 568;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 250;
      my $y = 134;
      my $X = $x+1;
      my $Y = $y+435;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 134;
      my $X = $x+35;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 128;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 250;
      my $y = 134;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  600   216  256    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 600;
    my $X = 216;
    my $Y = 256;
    my $s = "i_8_2";
    my $t = "o_a_8_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 598;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 598;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 256;
      my $X = $x+1;
      my $Y = $y+343;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 256;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 222;
      my $y = 598;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  616   216  384    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 616;
    my $X = 216;
    my $Y = 384;
    my $s = "i_8_3";
    my $t = "o_a_8_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 616;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 384;
      my $X = $x+1;
      my $Y = $y+233;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 384;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288   16   264   24    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 16;
    my $X = 264;
    my $Y = 24;
    my $s = "m_1";
    my $t = "m_1_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 16;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 24;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288    8   264   72    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 8;
    my $X = 264;
    my $Y = 72;
    my $s = "m_1";
    my $t = "m_1_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 8;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 10;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 10;
      my $X = $x+1;
      my $Y = $y+63;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 72;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 10;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240   24   216   16    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 24;
    my $X = 216;
    my $Y = 16;
    my $s = "m_1_f_1";
    my $t = "o_a_1_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 238;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 238;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 16;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  240   32   216  144    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 32;
    my $X = 216;
    my $Y = 144;
    my $s = "m_1_f_1";
    my $t = "o_a_1_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 32;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 34;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 34;
      my $X = $x+1;
      my $Y = $y+111;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 144;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 34;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240   80   216  272    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 80;
    my $X = 216;
    my $Y = 272;
    my $s = "m_1_f_2";
    my $t = "o_a_1_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 80;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 254;
      my $y = 80;
      my $X = $x+1;
      my $Y = $y+187;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 266;
      my $X = $x+21;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 266;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 272;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 254;
      my $y = 266;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 234;
      my $y = 266;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288   80   264  104    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 80;
    my $X = 264;
    my $Y = 104;
    my $s = "m_2";
    my $t = "m_2_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 80;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 80;
      my $X = $x+1;
      my $Y = $y+25;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 104;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288   72   264  152    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 72;
    my $X = 264;
    my $Y = 152;
    my $s = "m_2";
    my $t = "m_2_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 72;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 74;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 74;
      my $X = $x+1;
      my $Y = $y+79;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 152;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 74;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  112   216   24    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 112;
    my $X = 216;
    my $Y = 24;
    my $s = "m_2_f_1";
    my $t = "o_a_2_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 110;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 110;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 30;
      my $X = $x+1;
      my $Y = $y+81;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 30;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 24;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 110;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 210;
      my $y = 30;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  104   216  160    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 104;
    my $X = 216;
    my $Y = 160;
    my $s = "m_2_f_1";
    my $t = "o_a_2_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 104;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 106;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 106;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 114;
      my $X = $x+21;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 114;
      my $X = $x+1;
      my $Y = $y+45;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 210;
      my $y = 158;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 158;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 230;
      my $y = 106;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 230;
      my $y = 114;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 210;
      my $y = 114;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 210;
      my $y = 158;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  160   216  288    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 160;
    my $X = 216;
    my $Y = 288;
    my $s = "m_2_f_2";
    my $t = "o_a_2_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 160;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 162;
      my $X = $x+39;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 162;
      my $X = $x+1;
      my $Y = $y+125;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 286;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 286;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 162;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 202;
      my $y = 286;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  152   264  184    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 152;
    my $X = 264;
    my $Y = 184;
    my $s = "m_3";
    my $t = "m_3_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 152;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 152;
      my $X = $x+1;
      my $Y = $y+33;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 184;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288  144   264  232    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 144;
    my $X = 264;
    my $Y = 232;
    my $s = "m_3";
    my $t = "m_3_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 144;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 146;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 146;
      my $X = $x+1;
      my $Y = $y+87;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 232;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 146;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  192   216   40    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 192;
    my $X = 216;
    my $Y = 40;
    my $s = "m_3_f_1";
    my $t = "o_a_3_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 190;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 190;
      my $X = $x+35;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 46;
      my $X = $x+1;
      my $Y = $y+145;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 46;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 40;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 206;
      my $y = 190;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 206;
      my $y = 46;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  184   216  168    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 184;
    my $X = 216;
    my $Y = 168;
    my $s = "m_3_f_1";
    my $t = "o_a_3_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 182;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 182;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 168;
      my $X = $x+1;
      my $Y = $y+15;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 168;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 182;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  240   216  304    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 240;
    my $X = 216;
    my $Y = 304;
    my $s = "m_3_f_2";
    my $t = "o_a_3_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 240;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 258;
      my $y = 240;
      my $X = $x+1;
      my $Y = $y+35;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 274;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 234;
      my $y = 274;
      my $X = $x+1;
      my $Y = $y+31;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 304;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 258;
      my $y = 274;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 234;
      my $y = 274;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  216   264  264    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 216;
    my $X = 264;
    my $Y = 264;
    my $s = "m_4";
    my $t = "m_4_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 216;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 216;
      my $X = $x+1;
      my $Y = $y+49;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 264;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288  208   264  312    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 208;
    my $X = 264;
    my $Y = 312;
    my $s = "m_4";
    my $t = "m_4_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 208;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 210;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 210;
      my $X = $x+1;
      my $Y = $y+103;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 312;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 210;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  272   216   56    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 272;
    my $X = 216;
    my $Y = 56;
    my $s = "m_4_f_1";
    my $t = "o_a_4_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 270;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 198;
      my $y = 270;
      my $X = $x+43;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 198;
      my $y = 62;
      my $X = $x+1;
      my $Y = $y+209;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 198;
      my $y = 62;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 56;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 198;
      my $y = 270;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 198;
      my $y = 62;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  264   216  184    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 264;
    my $X = 216;
    my $Y = 184;
    my $s = "m_4_f_1";
    my $t = "o_a_4_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 262;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 262;
      my $X = $x+47;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 186;
      my $X = $x+1;
      my $Y = $y+77;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 186;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 184;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 262;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 194;
      my $y = 186;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  320   216  320    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 320;
    my $X = 216;
    my $Y = 320;
    my $s = "m_4_f_2";
    my $t = "o_a_4_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 216;
      my $y = 320;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288  288   264  344    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 288;
    my $X = 264;
    my $Y = 344;
    my $s = "m_5";
    my $t = "m_5_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 288;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 288;
      my $X = $x+1;
      my $Y = $y+57;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 344;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288  280   264  392    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 280;
    my $X = 264;
    my $Y = 392;
    my $s = "m_5";
    my $t = "m_5_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 280;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 282;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 282;
      my $X = $x+1;
      my $Y = $y+111;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 392;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 282;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  352   216   72    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 352;
    my $X = 216;
    my $Y = 72;
    my $s = "m_5_f_1";
    my $t = "o_a_5_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 352;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 262;
      my $y = 78;
      my $X = $x+1;
      my $Y = $y+275;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 78;
      my $X = $x+47;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 72;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 262;
      my $y = 78;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  344   216  200    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 344;
    my $X = 216;
    my $Y = 200;
    my $s = "m_5_f_1";
    my $t = "o_a_5_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 342;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 342;
      my $X = $x+51;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 206;
      my $X = $x+1;
      my $Y = $y+137;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 206;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 200;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 342;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 190;
      my $y = 206;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  400   216  328    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 400;
    my $X = 216;
    my $Y = 328;
    my $s = "m_5_f_2";
    my $t = "o_a_5_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 398;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 398;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 328;
      my $X = $x+1;
      my $Y = $y+71;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 328;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 226;
      my $y = 398;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  352   264  424    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 352;
    my $X = 264;
    my $Y = 424;
    my $s = "m_6";
    my $t = "m_6_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 286;
      my $y = 352;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 352;
      my $X = $x+1;
      my $Y = $y+73;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 424;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  288  344   264  472    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 344;
    my $X = 264;
    my $Y = 472;
    my $s = "m_6";
    my $t = "m_6_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 344;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 346;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 346;
      my $X = $x+1;
      my $Y = $y+127;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 472;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 346;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  432   216   88    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 432;
    my $X = 216;
    my $Y = 88;
    my $s = "m_6_f_1";
    my $t = "o_a_6_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 430;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 240;
      my $y = 430;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 266;
      my $y = 94;
      my $X = $x+1;
      my $Y = $y+337;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 94;
      my $X = $x+51;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 88;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 266;
      my $y = 430;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 266;
      my $y = 94;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  424   216  216    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 424;
    my $X = 216;
    my $Y = 216;
    my $s = "m_6_f_1";
    my $t = "o_a_6_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 422;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 422;
      my $X = $x+55;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 222;
      my $X = $x+1;
      my $Y = $y+201;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 222;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 216;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 422;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 186;
      my $y = 222;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  480   216  344    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 480;
    my $X = 216;
    my $Y = 344;
    my $s = "m_6_f_2";
    my $t = "o_a_6_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 478;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 478;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 344;
      my $X = $x+1;
      my $Y = $y+135;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 344;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 218;
      my $y = 478;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  424   264  504    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 424;
    my $X = 264;
    my $Y = 504;
    my $s = "m_7";
    my $t = "m_7_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 424;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 426;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 426;
      my $X = $x+1;
      my $Y = $y+79;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 504;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 286;
      my $y = 426;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  416   264  552    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 416;
    my $X = 264;
    my $Y = 552;
    my $s = "m_7";
    my $t = "m_7_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 416;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 418;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 418;
      my $X = $x+1;
      my $Y = $y+135;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 552;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 278;
      my $y = 418;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  512   216  104    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 512;
    my $X = 216;
    my $Y = 104;
    my $s = "m_7_f_1";
    my $t = "o_a_7_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 510;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 182;
      my $y = 510;
      my $X = $x+59;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 182;
      my $y = 106;
      my $X = $x+1;
      my $Y = $y+405;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 182;
      my $y = 106;
      my $X = $x+35;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 104;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 182;
      my $y = 510;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 182;
      my $y = 106;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  504   216  232    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 504;
    my $X = 216;
    my $Y = 232;
    my $s = "m_7_f_1";
    my $t = "o_a_7_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 502;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 240;
      my $y = 502;
      my $X = $x+31;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 270;
      my $y = 238;
      my $X = $x+1;
      my $Y = $y+265;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 238;
      my $X = $x+55;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 232;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 270;
      my $y = 502;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 270;
      my $y = 238;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  560   216  360    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 560;
    my $X = 216;
    my $Y = 360;
    my $s = "m_7_f_2";
    my $t = "o_a_7_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 558;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 558;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 360;
      my $X = $x+1;
      my $Y = $y+199;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 360;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 214;
      my $y = 558;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  488   264  584    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 488;
    my $X = 264;
    my $Y = 584;
    my $s = "m_8";
    my $t = "m_8_f_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 488;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 490;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 490;
      my $X = $x+1;
      my $Y = $y+95;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 584;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 282;
      my $y = 490;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  288  480   264  632    1

  if (1)
   {my $L = 4;
    my $x = 288;
    my $y = 480;
    my $X = 264;
    my $Y = 632;
    my $s = "m_8";
    my $t = "m_8_f_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 288;
      my $y = 480;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 274;
      my $y = 482;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 274;
      my $y = 482;
      my $X = $x+1;
      my $Y = $y+151;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 264;
      my $y = 632;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 274;
      my $y = 482;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  592   216  120    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 592;
    my $X = 216;
    my $Y = 120;
    my $s = "m_8_f_1";
    my $t = "o_a_8_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 592;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 242;
      my $y = 478;
      my $X = $x+1;
      my $Y = $y+115;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 242;
      my $y = 478;
      my $X = $x+33;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 274;
      my $y = 126;
      my $X = $x+1;
      my $Y = $y+353;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 126;
      my $X = $x+59;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 120;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 242;
      my $y = 478;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 274;
      my $y = 478;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 274;
      my $y = 126;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  584   216  248    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 584;
    my $X = 216;
    my $Y = 248;
    my $s = "m_8_f_1";
    my $t = "o_a_8_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 582;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 582;
      my $X = $x+63;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 254;
      my $X = $x+1;
      my $Y = $y+329;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 254;
      my $X = $x+39;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 248;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 582;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 178;
      my $y = 254;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  240  640   216  376    1

  if (1)
   {my $L = 4;
    my $x = 240;
    my $y = 640;
    my $X = 216;
    my $Y = 376;
    my $s = "m_8_f_2";
    my $t = "o_a_8_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 240;
      my $y = 638;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 638;
      my $X = $x+39;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 382;
      my $X = $x+1;
      my $Y = $y+257;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 382;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 216;
      my $y = 376;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 202;
      my $y = 638;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 202;
      my $y = 382;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   48    8    24    8    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 8;
    my $X = 24;
    my $Y = 8;
    my $s = "o_1";
    my $t = "O_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 24;
      my $y = 8;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96    8    72    8    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 8;
    my $X = 72;
    my $Y = 8;
    my $s = "o_1_F_1";
    my $t = "o_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 72;
      my $y = 8;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144    8   120    8    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 8;
    my $X = 120;
    my $Y = 8;
    my $s = "o_1_F_1_F_1";
    my $t = "o_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 8;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144   48   120   16    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 48;
    my $X = 120;
    my $Y = 16;
    my $s = "o_1_F_1_F_2";
    my $t = "o_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 48;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+33;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 16;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96  104    72   16    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 104;
    my $X = 72;
    my $Y = 16;
    my $s = "o_1_F_2";
    my $t = "o_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 104;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+89;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 72;
      my $y = 16;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144   96   120  104    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 96;
    my $X = 120;
    my $Y = 104;
    my $s = "o_1_F_2_F_1";
    my $t = "o_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 96;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 96;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 104;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  128   120  112    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 128;
    my $X = 120;
    my $Y = 112;
    my $s = "o_1_F_2_F_2";
    my $t = "o_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 128;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 112;
      my $X = $x+1;
      my $Y = $y+17;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 112;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48  216    24  216    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 216;
    my $X = 24;
    my $Y = 216;
    my $s = "o_2";
    my $t = "O_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 24;
      my $y = 216;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96  208    72  216    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 208;
    my $X = 72;
    my $Y = 216;
    my $s = "o_2_F_1";
    my $t = "o_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 208;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 208;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 72;
      my $y = 216;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  176   120  200    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 176;
    my $X = 120;
    my $Y = 200;
    my $s = "o_2_F_1_F_1";
    my $t = "o_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 176;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 176;
      my $X = $x+1;
      my $Y = $y+25;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 200;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  208   120  208    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 208;
    my $X = 120;
    my $Y = 208;
    my $s = "o_2_F_1_F_2";
    my $t = "o_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 120;
      my $y = 208;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96  296    72  224    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 296;
    my $X = 72;
    my $Y = 224;
    my $s = "o_2_F_2";
    my $t = "o_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 296;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 224;
      my $X = $x+1;
      my $Y = $y+73;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 72;
      my $y = 224;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  256   120  296    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 256;
    my $X = 120;
    my $Y = 296;
    my $s = "o_2_F_2_F_1";
    my $t = "o_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 256;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 256;
      my $X = $x+1;
      my $Y = $y+41;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 296;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  296   120  304    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 296;
    my $X = 120;
    my $Y = 304;
    my $s = "o_2_F_2_F_2";
    my $t = "o_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 296;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 302;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 302;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   48  424    24  424    1

  if (1)
   {my $L = 4;
    my $x = 48;
    my $y = 424;
    my $X = 24;
    my $Y = 424;
    my $s = "o_3";
    my $t = "O_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 24;
      my $y = 424;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:   96  400    72  424    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 400;
    my $X = 72;
    my $Y = 424;
    my $s = "o_3_F_1";
    my $t = "o_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 400;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 400;
      my $X = $x+1;
      my $Y = $y+25;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 72;
      my $y = 424;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  336   120  392    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 336;
    my $X = 120;
    my $Y = 392;
    my $s = "o_3_F_1_F_1";
    my $t = "o_3_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 336;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 336;
      my $X = $x+1;
      my $Y = $y+57;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 392;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  376   120  400    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 376;
    my $X = 120;
    my $Y = 400;
    my $s = "o_3_F_1_F_2";
    my $t = "o_3_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 376;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 378;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 378;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 400;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 378;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:   96  488    72  432    1

  if (1)
   {my $L = 4;
    my $x = 96;
    my $y = 488;
    my $X = 72;
    my $Y = 432;
    my $s = "o_3_F_2";
    my $t = "o_3";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 94;
      my $y = 488;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 94;
      my $y = 432;
      my $X = $x+1;
      my $Y = $y+57;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 72;
      my $y = 432;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  416   120  488    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 416;
    my $X = 120;
    my $Y = 488;
    my $s = "o_3_F_2_F_1";
    my $t = "o_3_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 142;
      my $y = 416;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 142;
      my $y = 416;
      my $X = $x+1;
      my $Y = $y+73;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 488;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  144  456   120  496    1

  if (1)
   {my $L = 4;
    my $x = 144;
    my $y = 456;
    my $X = 120;
    my $Y = 496;
    my $s = "o_3_F_2_F_2";
    my $t = "o_3_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 144;
      my $y = 456;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 458;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 458;
      my $X = $x+1;
      my $Y = $y+39;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 120;
      my $y = 496;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 138;
      my $y = 458;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192    8   168    8    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 8;
    my $X = 168;
    my $Y = 8;
    my $s = "o_a_1_1";
    my $t = "o_1_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 168;
      my $y = 8;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  144   168  168    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 144;
    my $X = 168;
    my $Y = 168;
    my $s = "o_a_1_2";
    my $t = "o_2_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 190;
      my $y = 144;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 144;
      my $X = $x+1;
      my $Y = $y+25;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 168;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  272   168  328    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 272;
    my $X = 168;
    my $Y = 328;
    my $s = "o_a_1_3";
    my $t = "o_3_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 272;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 274;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 274;
      my $X = $x+1;
      my $Y = $y+55;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 328;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 274;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192   24   168   16    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 24;
    my $X = 168;
    my $Y = 16;
    my $s = "o_a_2_1";
    my $t = "o_1_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 190;
      my $y = 24;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 16;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 16;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  160   168  176    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 160;
    my $X = 168;
    my $Y = 176;
    my $s = "o_a_2_2";
    my $t = "o_2_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 160;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 162;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 162;
      my $X = $x+1;
      my $Y = $y+15;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 176;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 162;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  288   168  336    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 288;
    my $X = 168;
    my $Y = 336;
    my $s = "o_a_2_3";
    my $t = "o_3_F_1_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 288;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 290;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 290;
      my $X = $x+1;
      my $Y = $y+47;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 336;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 290;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192   48   168   48    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 48;
    my $X = 168;
    my $Y = 48;
    my $s = "o_a_3_1";
    my $t = "o_1_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 168;
      my $y = 48;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  176   168  208    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 176;
    my $X = 168;
    my $Y = 208;
    my $s = "o_a_3_2";
    my $t = "o_2_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 176;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 178;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 178;
      my $X = $x+1;
      my $Y = $y+31;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 208;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 178;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  304   168  368    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 304;
    my $X = 168;
    my $Y = 368;
    my $s = "o_a_3_3";
    my $t = "o_3_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 304;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 306;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 306;
      my $X = $x+1;
      my $Y = $y+63;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 368;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 306;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192   56   168   56    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 56;
    my $X = 168;
    my $Y = 56;
    my $s = "o_a_4_1";
    my $t = "o_1_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 168;
      my $y = 56;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  192   168  216    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 192;
    my $X = 168;
    my $Y = 216;
    my $s = "o_a_4_2";
    my $t = "o_2_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 192;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 194;
      my $X = $x+15;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 194;
      my $X = $x+1;
      my $Y = $y+23;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 216;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 194;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  320   168  376    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 320;
    my $X = 168;
    my $Y = 376;
    my $s = "o_a_4_3";
    my $t = "o_3_F_1_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 320;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 320;
      my $X = $x+1;
      my $Y = $y+55;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 374;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 374;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 194;
      my $y = 374;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192   80   168   88    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 80;
    my $X = 168;
    my $Y = 88;
    my $s = "o_a_5_1";
    my $t = "o_1_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 190;
      my $y = 80;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 80;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 88;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  208   168  248    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 208;
    my $X = 168;
    my $Y = 248;
    my $s = "o_a_5_2";
    my $t = "o_2_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 208;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 210;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 210;
      my $X = $x+1;
      my $Y = $y+9;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 218;
      my $X = $x+9;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 178;
      my $y = 218;
      my $X = $x+1;
      my $Y = $y+31;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 248;
      my $X = $x+11;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 186;
      my $y = 210;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 186;
      my $y = 218;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
    if (1)
     {my $x = 178;
      my $y = 218;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  336   168  408    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 336;
    my $X = 168;
    my $Y = 408;
    my $s = "o_a_5_3";
    my $t = "o_3_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 336;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 338;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 338;
      my $X = $x+1;
      my $Y = $y+71;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 408;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 338;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192   96   168   96    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 96;
    my $X = 168;
    my $Y = 96;
    my $s = "o_a_6_1";
    my $t = "o_1_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 168;
      my $y = 96;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  224   168  256    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 224;
    my $X = 168;
    my $Y = 256;
    my $s = "o_a_6_2";
    my $t = "o_2_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 224;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 226;
      my $X = $x+19;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 226;
      my $X = $x+1;
      my $Y = $y+31;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 256;
      my $X = $x+7;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 174;
      my $y = 226;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  352   168  416    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 352;
    my $X = 168;
    my $Y = 416;
    my $s = "o_a_6_3";
    my $t = "o_3_F_2_F_1";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 190;
      my $y = 352;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 352;
      my $X = $x+1;
      my $Y = $y+65;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 416;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  112   168  128    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 112;
    my $X = 168;
    my $Y = 128;
    my $s = "o_a_7_1";
    my $t = "o_1_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>5, -xy=>$xy);
    $g->printBoundary(-layer=>6, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 190;
      my $y = 112;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 190;
      my $y = 112;
      my $X = $x+1;
      my $Y = $y+17;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 128;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  240   168  288    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 240;
    my $X = 168;
    my $Y = 288;
    my $s = "o_a_7_2";
    my $t = "o_2_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 240;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 242;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 242;
      my $X = $x+1;
      my $Y = $y+47;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 288;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 242;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  368   168  448    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 368;
    my $X = 168;
    my $Y = 448;
    my $s = "o_a_7_3";
    my $t = "o_3_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 368;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 370;
      my $X = $x+23;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 370;
      my $X = $x+1;
      my $Y = $y+79;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 448;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 170;
      my $y = 370;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  128   168  136    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 128;
    my $X = 168;
    my $Y = 136;
    my $s = "o_a_8_1";
    my $t = "o_1_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 128;
      my $X = $x+1;
      my $Y = $y+7;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 134;
      my $X = $x+25;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 168;
      my $y = 134;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
 }
# Wire:  192  256   168  296    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 256;
    my $X = 168;
    my $Y = 296;
    my $s = "o_a_8_2";
    my $t = "o_2_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 256;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 258;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 258;
      my $X = $x+1;
      my $Y = $y+39;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 296;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 258;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
# Wire:  192  384   168  456    1

  if (1)
   {my $L = 4;
    my $x = 192;
    my $y = 384;
    my $X = 168;
    my $Y = 456;
    my $s = "o_a_8_3";
    my $t = "o_3_F_2_F_2";
    push @debug, sprintf("Wire         %4d %4d %4d %4d %4d  %32.32s=>%s", $x, $y, $X, $Y, $L, $s, $t) if $debug;
    my $xy = [$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1];
    my $XY = [$X,$Y, $X+1,$Y, $X+1,$Y+1, $X,$Y+1];
    $g->printBoundary(-layer=>4, -xy=>$xy);
    $g->printBoundary(-layer=>4, -xy=>$XY);
    $g->printBoundary(-layer=>5, -xy=>$XY);
    $g->printBoundary(-layer=>6, -xy=>$XY);
    if (1)
     {my $x = 192;
      my $y = 384;
      my $X = $x+1;
      my $Y = $y+3;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 386;
      my $X = $x+27;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+1, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 386;
      my $X = $x+1;
      my $Y = $y+71;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 456;
      my $X = $x+3;
      my $Y = $y+1;
      push @debug, sprintf("Segment      %4d %4d %4d %4d %4d", $x, $y, $X, $Y, $L) if $debug;
      $g->printBoundary(-layer=>$L+3, -xy=>[$x,$y, $X,$y, $X,$Y, $x,$Y]);
     }
    if (1)
     {my $x = 166;
      my $y = 386;
      push @debug,  sprintf("Interconnect %4d %4d           %4d", $x, $y, $L) if $debug;
      $g->printBoundary(-layer=>$L+2, -xy=>[$x,$y, $x+1,$y, $x+1,$y+1, $x,$y+1]);
     }
 }
  $g->printEndstr;
  $g->printEndlib;
  owf("gds/$gdsOut.txt", join "\n", @debug) if $debug;
 }
