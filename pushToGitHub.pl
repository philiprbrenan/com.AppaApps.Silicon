#!/usr/bin/perl -I/home/phil/perl/cpan/DataTableText/lib/
#-------------------------------------------------------------------------------
# Push com.AppaApps.Silicon.Chip code to GitHub
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
my $repo = q(com.AppaApps.Silicon);                                             # Repo
my $wf   = q(.github/workflows/main.yml);                                       # Work flow on Ubuntu

if (0)                                                                          # Compile java files
 {push my @files, searchDirectoryTreesForMatchingFiles($home, qw(.java));
  my @pids;
  for my $s(@files)                                                             # Upload each selected file
   {next if $s =~ m(java/backup);
    say $s;
    if (my $pid = fork) {push @pids, $pid} else
     {my $c = "javac -g -d Classes -cp Classes $s";                             # Compile java
      say STDERR qq($c);
      say STDERR qx($c);
      exit;
     }
   }
  waitPids @pids;
  exit;
 }

if (1)                                                                          # Documentation from pod to markdown into read me with well known words expanded
 {push my @files, searchDirectoryTreesForMatchingFiles($home, qw(.java .md .pl .png));

  for my $s(@files)                                                             # Upload each selected file
   {next if $s =~ m(/backup/);
    next if $s =~ m(/images/);
    next if $s =~ m(/java/perl/);
    my $c = readBinaryFile $s;                                                  # Load file

    if ($s =~ m(/README))                                                       # Expand README
     {$c .= "\nModified: ".dateTimeStamp."\n";                                  # Ensure that the README images links get updated as well
      $c =  expandWellKnownWordsAsUrlsInMdFormat $c;                            # Expand well known terms
     }

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

concurrency:
  group: \${{ github.workflow }}-\${{ github.ref }}
  cancel-in-progress: true

jobs:

  test:
    permissions: write-all
    runs-on: ubuntu-latest

    strategy:
      matrix:
        task: [Chip, RiscV, Ban, Mjaf, Unary, Stuck]

    steps:
    - uses: actions/checkout\@v3
      with:
        ref: 'main'

    - name: 'JDK 22'
      uses: oracle-actions/setup-java\@v1
      with:
        website: jdk.java.net

    - name: Install Tree
      run:
        sudo apt install tree

    - name: Position
      run: |
        mkdir -p com/AppaApps/Silicon/tests
        cp       *.java $c
        cp tests/*.java $c/tests

    - name: Files
      run:
        tree

    - name: Compile
      run: |
        javac -g -d Classes -cp Classes $c/*.java $c/tests/*.java

    - name: Test Risc V
      if: matrix.task == 'RiscV'
      run: |
        java -cp Classes $c/RiscV

    - name: Test Ban
      if: matrix.task == 'Ban'
      run: |
        java -cp Classes $c/Ban

    - name: Test Unary
      if: matrix.task == 'Unary'
      run: |
        java -cp Classes $c/Unary

    - name: Test Stuck
      if: matrix.task == 'Stuck'
      run: |
        java -cp Classes $c/Stuck

    - name: Test Mjaf
      if: matrix.task == 'Mjaf'
      run: |
        java -cp Classes $c/Mjaf

    - name: Cpan
      if: matrix.task == 'Chip'
      run:  sudo cpan install -T Data::Dump Data::Table::Text GDS2 Digest::SHA1

    - name: Test silicon chips
      if: matrix.task == 'Chip'
      run: |
        java -cp Classes $c/Chip

    - name: Files
      if: matrix.task == 'Chip'
      run: |
        tree -h

    - name: Upload artifact
      uses: actions/upload-artifact\@v4
      if: matrix.task == 'Chip'
      with:
        name: Chip
        path: .

    - name: Upload GDS2
      if: matrix.task == 'ChipSuppress'
      run: |
        rm -f gds.zip
        zip gds gds/*
        ls -lah gds.zip
        git config user.name "aaa"
        git config user.email "aaa\@aaa.com"
        git pull
        git add gds.zip
        git commit -m "GDS2 tape-out"
        git push
END

  my $f = writeFileUsingSavedToken $user, $repo, $wf, $y;                       # Upload workflow
  lll "Ubuntu work flow for $repo written to: $f";
 }
