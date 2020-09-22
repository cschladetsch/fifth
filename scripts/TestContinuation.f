#
# Test basic continuations
#
# Store a continuation that simply executes 'plus'
# Note the quote character ' before the c. Otherwise, an object
# named c will attempted to be resolved (and fail)
{ + } 'c =
# Use it to add 2 numbers together
1 2 c& 3 == assert
# Make a continuation that doubles what's on the stack
{ 2 * } 'd =
2 d& 4 == assert
3 d& 6 == assert
2 2 d& d& 8 == assert
# Can also use locals in continuations.
# This stores whatever is on the stack to a local called 'a',
# then uses it to calculate 'a*2 + a'
{ 'a = a 2 * a + } 'e =
3 e& 9 == assert # 3*2 + 3 = 9
# ensure the data-stack is empty
depth 0 == assert

