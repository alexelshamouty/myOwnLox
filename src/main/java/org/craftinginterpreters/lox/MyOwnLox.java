/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author alex_elshamouty
 */
public class MyOwnLox {

    static boolean hadError = false;
    public static void main(String[] args) {
        if(args.length > 1){
            System.out.println("Usage: interpreter [script]");
        } else if ( args.length == 1 ) {
            try {
                runFile(args[0]);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            
        } else{
            try {
                runPrompt();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError){
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        System.out.println("Welcome to myOwnLox");
        for(;;){
            System.out.print("> ");
            String line = reader.readLine();
            if ( line == null ){
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token token: tokens){
            System.out.println(token);
        }
    }

    static void error(int line, String message){
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println("[line " + line + " ] Error" + where + ": " + message);
        hadError = true;
    }
}
