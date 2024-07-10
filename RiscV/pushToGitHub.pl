#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push RiscV code to GitHub
# Philip R Brenan at gmail dot com, Appa Apps Ltd Inc., 2023
#-------------------------------------------------------------------------------
use warnings FATAL => qw(all);
use strict;
use Carp;
use Data::Dump qw(dump);
use Data::Table::Text qw(:all);
use GitHub::Crud qw(:all);
use feature qw(say current_sub);

my $home = currentDirectory;                                                    # Local files
my $user = q(philiprbrenan);                                                    # User
my $repo = q(RiscV);                                                            # Repo
my $wf   = q(.github/workflows/main.yml);                                       # Work flow on Ubuntu
my @java = qw(Chip RiscV);                                                      # Java files

if (1)                                                                          # Documentation from pod to markdown into read me with well known words expanded
 {my @pids;
  for my $j(@java)                                                              # Links to Java files
   {say STDERR qx(ln -s ../$j.java) unless -e qq($j.java);
   }
  push my @files, searchDirectoryTreesForMatchingFiles($home, qw(.java .md .pl));

  for my $s(@files)                                                             # Upload each selected file
   {my $c = readBinaryFile $s;                                                  # Load file

    my $t = swapFilePrefix $s, $home;
    my $w = writeFileUsingSavedToken($user, $repo, $t, $c);
    lll "$w $s $t";
   }
 }

if (1)
 {my $d = dateTimeStamp;
  my $c = q(com/AppaApps/Silicon);
  my $y = <<"END";
# Test $d

name: Test
run-name: Silicon Chip

on:
  push:
    paths:
      - '**/main.yml'

jobs:

  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout\@v3
      with:
        ref: 'main'

    - name: 'JDK 22'
      uses: oracle-actions/setup-java\@v1
      with:
        website: jdk.java.net

    - name: Position
      run: |
        mkdir -p  $c
        cp *.java $c

    - name: Files
      run:
        tree

    - name: Compile
      run: |
        javac -g -d Classes -cp Classes $c/*.java

    - name: Test Risc V
      run: |
        java -cp Classes $c/RiscV
END

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "Ubuntu work flow for $repo written to: $f";
 }
