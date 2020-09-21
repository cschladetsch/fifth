# test basic continuations

# store a continuation that simply executes 'plus'
{ + } 'c =

# use it to add 2 numbers together
1 2 c & 3 == assert

depth 0 == assert

