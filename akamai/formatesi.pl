#!/usr/bin/perl -w

my ($body) = '';
while(<>) {
    $body .= $_;
}

my ($indent) = '';

sub firstParts($) {
    my ($data)  = @_;
    if (length($data) > 200) {
        return substr($data, 0, 200) . "...";
    }
    return $data;
}

while ($body =~ s@^(.*?)(</?esi:[a-z]+)(.*)$@@s) {
    my ($pre) = $1;
    my ($esi) = $2;
    $body = $3;

    if ($pre ne '') {
        print "${indent}CONTENT [[$pre]]\n";
    }
    my ($isClose) = ($esi =~ m:^</:);
    my ($isSelfClose) = 0;
    my ($tag) = $esi;
    while (1) {
        if ($body =~ s/^(\s*[a-zA-Z-0-9_]+=["][^"]*["])//) {
            $tag .= $1;
            next;
        }

        if ($body =~ s/^(\s*>)//) {
            $tag .= $1;
            last;
        }

        if ($body =~ s:^(\s*/>)::) {
            $tag .= $1;
            $isSelfClose = 1;
            last;
        }

        die("FAILED: Unexpected body content '" . firstParts($body) . "'\n");
    }

    if ($isClose && !$isSelfClose) {
        if ($indent eq '') {
            die("FAILED: Unbalanced close at '$tag'\n");
        }
        $indent =~ s/..$//;
    }
    print "${indent}$tag\n";
    if (!($isClose || $isSelfClose)) {
        $indent .= "  ";
    }
}
if ($body ne '') {
    print "${indent}CONTENT [[$body]]\n";
}
