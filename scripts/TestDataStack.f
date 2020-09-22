#
# Test data stack manipulation.
#
1 drop depth 0 == assert
1 2 swap 1 == assert 2 == assert
1 2 swap swap 2 == assert 1 == assert
1 2 3 depth 3 == assert
1 2 3 clear depth 0 == assert
1 2 3 drop drop depth 1 == assert
