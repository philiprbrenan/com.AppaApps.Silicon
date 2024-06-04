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

if (1)                                                                          # Documentation from pod to markdown into read me with well known words expanded
 {push my @files, searchDirectoryTreesForMatchingFiles($home, qw(.j .md .pl .png));

  for my $s(@files)                                                             # Upload each selected file
   {next if $s =~ m(/backup/);
    next if $s =~ m(/images/);
    next if $s =~ m(/java/perl/);
    next if $s =~ m(/README);
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

    - name: Cpan
      run:  sudo cpan install -T Data::Dump Data::Table::Text GDS2 Digest::SHA1

    - name: Test Risc V
      run: |
        mkdir -p com/AppaApps/Silicon/
        cp RiscV.j com/AppaApps/Silicon/
        java --enable-preview --source 22  com/AppaApps/Silicon/RiscV.j

    - name: Test silicon chips
      run: |
        mkdir -p com/AppaApps/Silicon/
        cp Chip.j com/AppaApps/Silicon/
        java --enable-preview --source 22  com/AppaApps/Silicon/Chip.j

    - name: Files
      run: |
        tree -h

    - uses: actions/upload-artifact\@v4
      with:
        name: Chip
        path: .

    - run: |
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
