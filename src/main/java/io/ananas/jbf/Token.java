package io.ananas.jbf;

public class Token {
    final TokenType type;
    final char lexeme;
    final int line;

    public Token(TokenType type, char lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme;
    }
}
