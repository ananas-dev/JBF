package io.ananas.jbf;

import java.util.ArrayList;
import java.util.List;
import static io.ananas.jbf.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            scanToken();
        }

        tokens.add(new Token(EOF, '\0', line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '+' -> addToken(ADD);
            case '-' -> addToken(SUB);
            case '<' -> addToken(RIGHT);
            case '>' -> addToken(LEFT);
            case '.' -> addToken(WRITE);
            case ',' -> addToken(READ);
            case '[' -> addToken(BEGIN_LOOP);
            case ']' -> addToken(END_LOOP);
            case '\n' -> line++;
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, source.charAt(current - 1), line));
    }
}
