#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/ -I/home/phil/perl/cpan/GitHubCrud/lib/
#-------------------------------------------------------------------------------
# Spell check java
# Philip R Brenan at gmail dot com, Appa Apps Ltd, 2017-2019
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use Carp;
use Text::Hunspell;

my %ignoreSpellings = map {$_=>1} qw(
aq ecall rl v2
bitbus btree fanout favour fibonacci gaul gds2 geany getstacktrace github gsy ing limberger nand nxor perl pre prepended stderr tostring traceback un vias zerad
addi auipc cpu funct3 funct7 imipac jal jalr lui pdf risc riscv rv32i s1 s2 x0
);

my $speller = Text::Hunspell->new(
  "/usr/share/hunspell/en_US.aff",                                              # Hunspell affix file
  "/usr/share/hunspell/en_US.dic"                                               # Hunspell dictionary file
);

die unless $speller;                                                            # Check speller is present

sub check($)                                                                    # Check spelling in file
 {my ($file) = @_;                                                              # File
  my %errors;                                                                   # Mis-spellings
  my $s = readFile($file);
  my @lines = split /\n/, $s;

  for my $i(keys @lines)                                                        # Check spelling of each line
   {my $line = $lines[$i];
    next unless $line =~ m(\A.{80}//(.*)\Z);                                    # Line with comment in specified position
    my $l = $1;
       $l =~ s(".*?")   ()g;
       $l =~ s(\(.*?\)) ()g;
       $l =~ s(\W)      ( )g;
    my @w = map {lc} grep {!/\A[A-Z]/} split /\s+/, $l;                         # Words - remove proper names then normalize

    for my $w(@w)                                                               # Check spelling of each word in the comment on a line
     {next if $ignoreSpellings{$w} or $speller->check($w);
      $errors{$w}{$i+1}++;
     }
   }

  if (my $N = keys %errors)                                                     # Report errors
   {my @e;
    say STDERR "Spelling:\n", join ' ', sort keys %errors;
    for   my $w(sort keys %errors)
     {for my $i(sort keys $errors{$w}->%*)
       {fff $i, $file, $w;
       }
     }

    confess "$N spelling errors in file: $file";
    say STDERR dump [keys %errors];
   }
 }

check(q(../Ban.java));
check(q(../Chip.java));
check(q(../Mjaf.java));
check(q(../RiscV.java));
check(q(../Stuck.java));
check(q(../Unary.java));
