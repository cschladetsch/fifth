#
# Test basic string literals
#
"foo" "foo" == assert
"foo" "bar" == not assert
"Hello, " "world" + dup print "Hello, world" == assert
depth 0 == assert
