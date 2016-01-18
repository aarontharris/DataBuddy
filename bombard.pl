#!/usr/bin/perl

my $iterations = 100;
my $rootPid = $$;
my $forks = 0;

while ( $forks < $iterations ) {
    #print "I am the parent $forks. $$ vs $rootPid\n";
    my $pid = fork();
    if ( $pid > 0 ) {
        $forks++;
    }

    if ( $pid == 0 ) {
        &doWork();
        last;
    }

    select(undef, undef, undef, 0.001);
}

sub doWork() {
    print "Fork[$forks] writing $forks\n";
    &save( $forks );
}

sub save {
    my $value=shift;
    system( qq|curl -X POST -H "Cache-Control: no-cache" -H "Content-Type: application/x-www-form-urlencoded" -d 'action=save&topic=aarontharris&subtopic=gamestate&key=coins&value=$value' 'http://localhost:25566?context=aarontharris'|);
    #my $value=`curl -X POST -H "Cache-Control: no-cache" -H "Content-Type: application/x-www-form-urlencoded" -d 'action=read&topic=aarontharris&subtopic=gamestate&key=coins' 'http://localhost:25566?context=aarontharris'`;
}

