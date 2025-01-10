package org.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftinginterpreters.lox.TokenType.AND;
import static org.craftinginterpreters.lox.TokenType.BANG;
import static org.craftinginterpreters.lox.TokenType.BANG_EQUAL;
import static org.craftinginterpreters.lox.TokenType.CLASS;
import static org.craftinginterpreters.lox.TokenType.COMMA;
import static org.craftinginterpreters.lox.TokenType.DOT;
import static org.craftinginterpreters.lox.TokenType.ELSE;
import static org.craftinginterpreters.lox.TokenType.EOF;
import static org.craftinginterpreters.lox.TokenType.EQUAL;
import static org.craftinginterpreters.lox.TokenType.EQUAL_EQUAL;
import static org.craftinginterpreters.lox.TokenType.FALSE;
import static org.craftinginterpreters.lox.TokenType.FOR;
import static org.craftinginterpreters.lox.TokenType.FUN;
import static org.craftinginterpreters.lox.TokenType.GREATER_EQUAL;
import static org.craftinginterpreters.lox.TokenType.IDENTIFIER;
import static org.craftinginterpreters.lox.TokenType.IF;
import static org.craftinginterpreters.lox.TokenType.LEFT_BRACE;
import static org.craftinginterpreters.lox.TokenType.LEFT_PAREN;
import static org.craftinginterpreters.lox.TokenType.LESS_EQUAL;
import static org.craftinginterpreters.lox.TokenType.MINUS;
import static org.craftinginterpreters.lox.TokenType.NIL;
import static org.craftinginterpreters.lox.TokenType.NUMBER;
import static org.craftinginterpreters.lox.TokenType.OR;
import static org.craftinginterpreters.lox.TokenType.PLUS;
import static org.craftinginterpreters.lox.TokenType.PRINT;
import static org.craftinginterpreters.lox.TokenType.RETURN;
import static org.craftinginterpreters.lox.TokenType.RIGHT_BRACE;
import static org.craftinginterpreters.lox.TokenType.RIGHT_PAREN;
import static org.craftinginterpreters.lox.TokenType.SEMICOLON;
import static org.craftinginterpreters.lox.TokenType.SLASH;
import static org.craftinginterpreters.lox.TokenType.STAR;
import static org.craftinginterpreters.lox.TokenType.STRING;
import static org.craftinginterpreters.lox.TokenType.SUPER;
import static org.craftinginterpreters.lox.TokenType.THIS;
import static org.craftinginterpreters.lox.TokenType.TRUE;
import static org.craftinginterpreters.lox.TokenType.VAR;
import static org.craftinginterpreters.lox.TokenType.WHILE;

class Scanner{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0 ;
    private int line = 0;
    private static final Map<String, TokenType> keywords;

    static{
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("fun", FUN);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
    public Scanner(String source) {
        this.source = source;
    }
    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        this.tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private void scanToken(){
        char c = advance();

        switch(c){
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

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=')? EQUAL_EQUAL: EQUAL);
                break;
            case '<':
                addToken(match('=')? LESS_EQUAL: EQUAL);
                break;
            case '>':
                addToken(match('=')? GREATER_EQUAL: EQUAL);
                break;
            case '"':
                string();
                break;
            case '/':
                if(match('/')){
                    //basically ignore all whitespaces
                    while (peek() != '\n' && !isAtEnd()) advance(); 
                } else if(peek() == '*'){
                    // case of dealing with cstyle comment
                    cstylecomment();
                } else {
                    //tihs is just division
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            default:
                if(isDigit(c)){
                    number();
                } else if(isAlpha(c)){
                    identifier();
                }else{
                    MyOwnLox.error(line, "Unexpected Character");
                }
                break;
        }
    }

    private void cstylecomment(){
        //if it's not at the end and it's not */ then we keep advancing anc counting lines
        advance();
        while(peekNext() != '/' && !isAtEnd()){
            if(peek() == '\n'){
                line++;
            }
            advance();
            //look ahead two letter
            if(peek() == '*' && peekNext() == '/'){
                advance();
                advance();
                break;
            }
        }
    }
    private void identifier(){
        while(isAlphaNumeric(peek())) advance(); //is it a digit, number or an underscore.. jesus we can just use regular expressions :D
        String text = source.substring(start, current);//Just get what we collected.
        TokenType type = keywords.get(text); //Let's get the type from the static reserves words map we created
        if(type == null) type = IDENTIFIER; //maybe it's a varibale name or whatever
        addToken(type);
    }
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c){
        return (
            (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ) ||  c == '_';
    }
    private boolean isDigit(char c){
        return c >= '0' && c <= '9'; 
    }

    private void number(){
        //If the next letter is a digit, increase counter
        // we don't care about the return value of the function as we will substring it
        while(isDigit(peek())) advance();

        //We need to deal with fractions
        if(peek() == '.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start,current)));
    }

    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }
    private void string(){
        //Let's imagine an empty string first like "" where start is 0 and current is 0. When we peak, we look at current and if it's " then we basically end the while loop
        // We also know that current >= length. Length is two and current is 0 in this case.. so we know for sure we have one more to go
        // now we check if we are at the end, which we aren't
        // Now we advance to 1
        // Substring now is going to be between 1 and 0 which is what? I don't know :D 

        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            MyOwnLox.error(line, "Unterminated string.");
        }

        advance();
        //Just returning the string without the quotes
        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current); //advance will take care of the incrementing
    }

    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance(){
        // It returns the current and then advances.
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
