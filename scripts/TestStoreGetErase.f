#
# Test store/get value, as well as erase and exists
#
'a exists not assert
1 'a =
'a exists assert
a 2  == not assert
a 2 + 3 == assert
'a erase
'a exists not assert
