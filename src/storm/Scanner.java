package storm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static storm.TokenType.*;


/*
* This class is used for lexical analysis.
* The source code is passed in and outputs a list of tokens.
* */

public class Scanner {
    private final String source; //Source code.
    private final List<Token> tokens = new ArrayList<>(); //List of generated tokens.
    private int start = 0; //Start position for current lex.
    private int current = 0; //Current position in source code.
    private int line = 1; //Current line number in source code.

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }




    public Scanner(String source) {
        this.source = source;
    }

    /*
    * Method that initiates scanning of source code.
    * Returns a list of tokens.
    * */
    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line)); //Add token EOF (end of file) at the end of the source code.
        return tokens;
    }

    /*
    * Scans and matches current character in the source code to a possible token.
    * */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            //Match checks the next character in the source code to determine type of multi character tokens.
            case '!': {
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            }
            case '=': {
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            }
            case '<': {
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            }
            case '>': {
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            }
            case 'o': {
                if(match('r')) {
                    addToken(OR);
                }
                break;
            }
            case '/': if(match('/')) {
                while(peek() != '\n' && !isAtEnd()) advance(); //Ensures a comment covers the remainder of current line
            } else {
                addToken(SLASH);
            }
            break;
            case ' ': //Match to ignore whitespace
                case '\r':
                    case '\t':
                        break;
            case '\n': {
                line++;
                break;
            }
            case '"': string(); break;
            default:
                if(isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }

                else {
                    Storm.error(line, "Unexpected character '" + c + "'");
                    break;
                }
            }
        }

    /*
    * Handles a sequence of characters that form identifiers or keywords.
    * */
    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }

    /*
    * Handles numeric literals
    * */
    private void number() {
        while(isDigit(peek())) advance();

        //Look for fractional.
        if(peek() == '.' && isDigit(peekNext())) {
            advance();
            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /*
    * Handles string literals.
    * */

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Storm.error(line, "Unexpected end of string");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1); //Trims surrounding quotes.
        addToken(STRING, value);
    }

    /*
    * Called when handling multi character keywords.
    * Matches current character with the next expected character.
    * */
    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /*
    * This method enables peeking at the current character without consuming it.
    * */
    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /*
    * THis method enables peeking at the next upcoming character without consuming it.
    * */
    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /*
    * Checks if a character is alphabetic or a underscore
    * */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /*
    * Checks if character is alphanumeric.
    * */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /*
    * Checks if character is a digit
    * */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /*
    * Determines if the scanner has reached the end of the source code.
    * */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /*
    * Advances the scanner to the next character
    * */
    private char advance() {
        return source.charAt(current++);
    }

    //Temporary overload method to handle no input of literal.
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /*
    * Adds token to the token list with given type and literal value.
    * */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
