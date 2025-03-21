package org.craftinginterpreters.lox;

import java.util.List;

import static org.craftinginterpreters.lox.TokenType.BANG;
import static org.craftinginterpreters.lox.TokenType.BANG_EQUAL;
import static org.craftinginterpreters.lox.TokenType.CLASS;
import static org.craftinginterpreters.lox.TokenType.EOF;
import static org.craftinginterpreters.lox.TokenType.EQUAL_EQUAL;
import static org.craftinginterpreters.lox.TokenType.FALSE;
import static org.craftinginterpreters.lox.TokenType.FOR;
import static org.craftinginterpreters.lox.TokenType.FUN;
import static org.craftinginterpreters.lox.TokenType.GREATER;
import static org.craftinginterpreters.lox.TokenType.GREATER_EQUAL;
import static org.craftinginterpreters.lox.TokenType.IF;
import static org.craftinginterpreters.lox.TokenType.LEFT_PAREN;
import static org.craftinginterpreters.lox.TokenType.LESS;
import static org.craftinginterpreters.lox.TokenType.LESS_EQUAL;
import static org.craftinginterpreters.lox.TokenType.MINUS;
import static org.craftinginterpreters.lox.TokenType.NIL;
import static org.craftinginterpreters.lox.TokenType.NUMBER;
import static org.craftinginterpreters.lox.TokenType.PLUS;
import static org.craftinginterpreters.lox.TokenType.PRINT;
import static org.craftinginterpreters.lox.TokenType.RETURN;
import static org.craftinginterpreters.lox.TokenType.RIGHT_PAREN;
import static org.craftinginterpreters.lox.TokenType.SEMICOLON;
import static org.craftinginterpreters.lox.TokenType.SLASH;
import static org.craftinginterpreters.lox.TokenType.STAR;
import static org.craftinginterpreters.lox.TokenType.STRING;
import static org.craftinginterpreters.lox.TokenType.TRUE;
import static org.craftinginterpreters.lox.TokenType.VAR;
import static org.craftinginterpreters.lox.TokenType.WHILE;

class Parser{

    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    public Expr parse(){
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expr = comparision();
        while (match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private boolean match(TokenType... types){
        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private Token previous(){
        return tokens.get(current - 1);
    }
    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }
    private Token peek(){
        return tokens.get(current);
    }
    private Expr comparision(){
        Expr expr = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term(){
        Expr expr = factor();

        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if(match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }
    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }
        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

    private Token consume(TokenType type, String message){
        if(check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        MyOwnLox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();
        while(!isAtEnd()){
            if(previous().type == SEMICOLON) return;
            switch(peek().type){
                case CLASS: case FOR: case FUN: case IF: case PRINT:
                    case RETURN: case VAR: case WHILE:
                        return;
            }
            advance();
        }
    }
}