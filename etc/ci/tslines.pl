#!/usr/bin/env perl

=head1 NAME

tslines - timestamp input or the output of a process.

=head1 SYNOPSIS

ts [cmd args...]

=head1 DESCRIPTION

The C<tslines> program timestamps lines of text.  Without any arguments, it
prepends timestamps to the lines of its standard output.  With arguments, it
runs the specified program and timestamps its standard output and error.
Standard output and error are timestamped separately and printed to standard
output and error respectively.

Timestamps are relative, offsets from when C<tslines> was started.

=head1 COPYRIGHT

Copyright (c) 2014 Michael Ekstrand.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

=cut

use strict;
use warnings;

use Time::HiRes qw{time sleep};
use IO::Handle;
# use POSIX qw{pipe};

sub cat_with_timestamps(**);
sub fork_cat(**@);
sub print_line($);
sub format_time($);

my $start_time = time;

if (@ARGV) {
    # Let's do some arguments
    pipe my $r_stdout, my $w_stdout or die "pipe: $!";
    fork_cat $r_stdout, \*STDOUT, $w_stdout;
    close $r_stdout;
    pipe my $r_stderr, my $w_stderr or die "pipe: $!";
    fork_cat $r_stderr, \*STDERR, $w_stdout, $w_stderr;
    close $r_stderr;
    open STDOUT, '>&', $w_stdout or die "STDOUT: $!";
    close $w_stdout;
    open STDERR, '>&', $w_stderr or die "STDERR: $!";
    close $w_stderr;
    exec @ARGV or die "exec $ARGV[0]: $!";
} else {
    cat_with_timestamps STDIN, STDOUT;
}

sub cat_with_timestamps(**)
{
    # Try to be intelligent about dealing with \r.
    my ($read, $write) = @_;
    select $write;
    $| = 1;
    my $line = '';
    for (my $c = getc $read; defined $c; $c = getc $read) {
        if ($c eq "\r") {
            print_line "$line\r";
            $line = '';
        } elsif ($c eq "\n") {
            if ($line eq '') {
                print "\n";
            } else {
                print_line "$line\n";
            }
            $line = '';
        } else {
            $line .= $c;
        }
    }
    print_line "$line\n" if $line ne '';
}

sub fork_cat(**@)
{
    my ($read, $write, @close) = @_;
    my $pid = fork;
    die "fork: $!" unless defined $pid;
    unless ($pid) {
        foreach my $fh (@close) {
            close $fh;
        }
        # we are in the child
        cat_with_timestamps $read, $write;
        exit 0;
    }
}

sub print_line($)
{
    my ($line) = @_;
    my $tm = time;
    my $time_str = format_time $tm;
    print "$time_str $line";
}

sub format_time($)
{
    my ($time) = @_;
    my $duration = $time - $start_time;
    my $mins = int($duration / 60);
    my $secs = $duration - $mins * 60;
    sprintf '%02d:%05.2f', $mins, $secs;
}
