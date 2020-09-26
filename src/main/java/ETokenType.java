public enum ETokenType {
    None,

    True, False,
    Number, String,

    Plus, Minus,
    Multiply, Divide, Modulo,

    Not, Equiv, NotEquiv,
    Less, Greater, LessEqual, GreaterEqual,

    Assert,
    Print,

    Resume, Suspend, Replace,

    Get, Store, Ident, QuotedIdent,
    Erase, Exists,

    Whitespace,

    Dup, Swap, Rot, RotN, Drop,

    OpenSquareBracket, CloseSquareBracket,

    OpenParan, CloseParan,

    OpenBrace, CloseBrace,

    Break,
    Exit,
    Dump,
    ShowStack,

    Comment,

    Depth, Clear,

    If, IfElse, While, For,

    Size, ToArray, FromArray, At, Set,
    ForEach,
}
