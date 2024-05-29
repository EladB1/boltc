package com.piedpiper.bolt.ir;

import com.piedpiper.bolt.lexer.StaticToken;
import com.piedpiper.bolt.lexer.TokenType;
import com.piedpiper.bolt.lexer.VariableToken;
import com.piedpiper.bolt.parser.AbstractSyntaxTree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIRGenerator {
    private final IRGenerator generator = new IRGenerator();

    @Test
    void test_generate_simple_variable() {
        // Source code: int x = 0;
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("VAR-DECL", new StaticToken(TokenType.KW_INT), new VariableToken(TokenType.ID, "x"), new VariableToken(TokenType.NUMBER, "0"))
        ));
        List<String> result = generator.generate(AST);
        System.out.println(result.toString());
        assertEquals(5, result.size());
        assertEquals("global", result.get(0));
        assertEquals("int", result.get(1));
        assertEquals("x", result.get(2));
        assertEquals("=", result.get(3));
        assertEquals("0", result.get(4));
    }

    @Test
    void test_generate_arithmetic_variable() {
        // Source code: int x = 1.0 * 3;
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("VAR-DECL", List.of(
                new AbstractSyntaxTree(new StaticToken(TokenType.KW_INT)),
                new AbstractSyntaxTree(new VariableToken(TokenType.ID, "x")),
                new AbstractSyntaxTree(new VariableToken(TokenType.OP, "*"), new VariableToken(TokenType.NUMBER, "1.0"), new VariableToken(TokenType.NUMBER, "3"))
            ))
        ));
        List<String> result = generator.generate(AST);
        System.out.println(result.toString());
        assertEquals(7, result.size());
        assertEquals("global", result.get(0));
        assertEquals("int", result.get(1));
        assertEquals("x", result.get(2));
        assertEquals("=", result.get(3));
        assertEquals("3", result.get(4));
        assertEquals("1.0", result.get(5));
        assertEquals("MUL", result.get(6));
    }

    @Test
    void test_generate_multi_arithmetic_variable() {
        // Source code: int x = 1.0 * (3 + 4);
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("VAR-DECL", List.of(
                new AbstractSyntaxTree(new StaticToken(TokenType.KW_INT)),
                new AbstractSyntaxTree(new VariableToken(TokenType.ID, "x")),
                new AbstractSyntaxTree(new VariableToken(TokenType.OP, "*"), List.of(
                    new AbstractSyntaxTree(new VariableToken(TokenType.NUMBER, "1.0")),
                    new AbstractSyntaxTree(new VariableToken(TokenType.OP, "+"), new VariableToken(TokenType.NUMBER, "3"), new VariableToken(TokenType.NUMBER, "4"))
                ))
            ))
        ));
        List<String> result = generator.generate(AST);
        System.out.println(result.toString());
        assertEquals(9, result.size());
        assertEquals("global", result.get(0));
        assertEquals("int", result.get(1));
        assertEquals("x", result.get(2));
        assertEquals("=", result.get(3));
        assertEquals("4", result.get(4));
        assertEquals("3", result.get(5));
        assertEquals("ADD", result.get(6));
        assertEquals("1.0", result.get(7));
        assertEquals("MUL", result.get(8));
    }

    @Test
    void test_generate_multi_comparison() {
        // Source code: 2 < 5 && 2 != 3
        VariableToken two = new VariableToken(TokenType.NUMBER, "2");
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree(new VariableToken(TokenType.OP, "&&"), List.of(
                new AbstractSyntaxTree(new VariableToken(TokenType.OP, "<"), two, new VariableToken(TokenType.NUMBER, "5")),
                new AbstractSyntaxTree(new VariableToken(TokenType.OP, "!="), two, new VariableToken(TokenType.NUMBER, "3"))
            ))
        ));

        List<String> result = generator.generate(AST);
        System.out.println(result.toString());
        assertEquals(7, result.size());
        assertEquals("3", result.get(0));
        assertEquals("2", result.get(1));
        assertEquals("NE", result.get(2));
        assertEquals("5", result.get(3));
        assertEquals("2", result.get(4));
        assertEquals("LT", result.get(5));
        assertEquals("AND", result.get(6));
    }

    @Test
    void test_generateFunctionCall_builtin_noParams() {
        // Source code: getEnv();
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("FUNC-CALL", new VariableToken(TokenType.ID, "getEnv"))
        ));

        List<String> result = generator.generate(AST);

        assertEquals(3, result.size());
        assertEquals("CALL", result.get(0));
        assertEquals("getEnv", result.get(1));
        assertEquals("0", result.get(2));
    }

    @Test
    void test_generateFunctionCall_builtin_oneParam() {
        // Source code: println(1+6);
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("FUNC-CALL", List.of(
                new AbstractSyntaxTree(new VariableToken(TokenType.ID, "println")),
                new AbstractSyntaxTree("FUNC-PARAMS", List.of(
                    new AbstractSyntaxTree(new VariableToken(TokenType.OP, "+"), new VariableToken(TokenType.NUMBER, "1"), new VariableToken(TokenType.NUMBER, "6"))
                ))
            ))
        ));

        List<String> result = generator.generate(AST);

        assertEquals(4, result.size());
        assertEquals("CALL", result.get(0));
        assertEquals("println", result.get(1));
        assertEquals("1", result.get(2));
        assertEquals("(6 1 ADD)", result.get(3));
    }

    @Test
    void test_generateFunctionCall_builtin_multiParam() {
        // Source code: slice("hello, world!", 1, 6);
        AbstractSyntaxTree AST = new AbstractSyntaxTree("PROGRAM", List.of(
            new AbstractSyntaxTree("FUNC-CALL", List.of(
                new AbstractSyntaxTree(new VariableToken(TokenType.ID, "slice")),
                new AbstractSyntaxTree("FUNC-PARAMS", new VariableToken(TokenType.STRING, "hello, world!"), new VariableToken(TokenType.NUMBER, "1"), new VariableToken(TokenType.NUMBER, "6"))
            ))
        ));

        List<String> result = generator.generate(AST);

        assertEquals(4, result.size());
        assertEquals("CALL", result.get(0));
        assertEquals("slice", result.get(1));
        assertEquals("3", result.get(2));
        assertEquals("(\"hello, world!\",1,6)", result.get(3));
    }
}
