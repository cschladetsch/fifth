#
# Test basic continuations
#
# Make a 'function' that just returns 1
{1} 'a= a& 1 == assert

# Make a function that adds whatever two things are on the stack
{+} 'b=
a& 2 b& 3 == assert

# Make a continuation that doubles what's on the stack
{ 2 * } 'd =
2 d& 4 == assert
2 d& d& 8 == assert

# Can also use locals in continuations.
# This stores whatever is on the stack to a local called 'a',
# then uses it to calculate 'a*2 + a'
{ 'a= a 2 * a + } 'e =
3 e& 9 == assert

depth 0 == assert
"Done" print
