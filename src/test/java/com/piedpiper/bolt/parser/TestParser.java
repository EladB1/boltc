package com.piedpiper.bolt.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.piedpiper.bolt.error.SyntaxError;
import com.piedpiper.bolt.lexer.StaticToken;
import com.piedpiper.bolt.lexer.Token;
import com.piedpiper.bolt.lexer.TokenType;
import com.piedpiper.bolt.lexer.VariableToken;

public class TestParser {

    private List<ParseTree> tokensToTreeNodes(List<Token> tokens) {
        return tokens.stream().map((Token token) -> new ParseTree(token)).collect(Collectors.toList());
    }

    // top-level parse

    // parseLine

    // parseStatement

    // parseExpr

    // parseUnaryOp

    // parseLeftUnaryOp
    @ParameterizedTest
    @ValueSource(strings = {"++", "--", "-", "!"})
    void test_parseLeftUnaryOp_succeedsID(String operator) {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, operator),
            new VariableToken(TokenType.ID, "x")
        );

        ParseTree expectedParseTree = new ParseTree("LEFT-UNARY-OP", tokensToTreeNodes(tokens));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseLeftUnaryOp();

        assertEquals(expectedParseTree, tree);
    }

    @ParameterizedTest
    @ValueSource(strings = {"++", "--", "-"})
    void test_parseLeftUnaryOp_succeedsWithNumLiteral(String operator) {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, operator),
            new VariableToken(TokenType.NUMBER, "5")
        );

        ParseTree expectedParseTree = new ParseTree("LEFT-UNARY-OP", tokensToTreeNodes(tokens));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseLeftUnaryOp();

        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseLeftUnaryOp_succeedsWithBooleanLiteral() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, "!"),
            new StaticToken(TokenType.KW_TRUE)
        );

        ParseTree expectedParseTree = new ParseTree("LEFT-UNARY-OP", tokensToTreeNodes(tokens));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseLeftUnaryOp();

        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseLeftUnaryOp_succeedsWithFunctionCall() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, "!"),
            new VariableToken(TokenType.ID, "isInt"),
            new StaticToken(TokenType.LEFT_PAREN),
            new StaticToken(TokenType.RIGHT_PAREN)
        );

        ParseTree expectedParseTree = new ParseTree("LEFT-UNARY-OP", List.of(
            new ParseTree(tokens.get(0)),
            new ParseTree("FUNC-CALL", tokensToTreeNodes(tokens.subList(1, 4)))
        ));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseLeftUnaryOp();

        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseLeftUnaryOp_succeedsWithArrayAccess() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, "-"),
            new VariableToken(TokenType.ID, "nums"),
            new StaticToken(TokenType.LEFT_SQB),
            new VariableToken(TokenType.NUMBER, "0"),
            new StaticToken(TokenType.RIGHT_SQB)
        );

        ParseTree expectedParseTree = new ParseTree("LEFT-UNARY-OP", List.of(
            new ParseTree(tokens.get(0)),
            new ParseTree("ARRAY-ACCESS", List.of(
                new ParseTree(tokens.get(1)),
                new ParseTree("ARRAY-INDEX", tokensToTreeNodes(tokens.subList(2, 5))
                )
            ))
        ));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseLeftUnaryOp();

        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseLeftUnaryOp_wrongToken() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.OP, "!"),
            new VariableToken(TokenType.STRING, "\"true\"")
        );

        Parser parser = new Parser(tokens);
        SyntaxError error = assertThrows(SyntaxError.class, () -> parser.parseLeftUnaryOp());
        assertEquals("Invalid unary operator on STRING", error.getMessage());
    }


    // parseValue

    // parseFunctionCall

    // parseArrayAccess
    @Test
    void test_parseArrayAccess_succeedsWithArrayIndex() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.ID, "x"),
            new StaticToken(TokenType.LEFT_SQB),
            new VariableToken(TokenType.NUMBER, "12"),
            new StaticToken(TokenType.RIGHT_SQB)
        );

        ParseTree expectedParseTree = new ParseTree("ARRAY-ACCESS", List.of(
            new ParseTree(tokens.get(0)),
            new ParseTree("ARRAY-INDEX", tokensToTreeNodes(tokens.subList(1, 4))
            )
        ));

        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseArrayAccess();

        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseArrayAccess_EOFError() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.ID, "x"),
            new StaticToken(TokenType.LEFT_SQB)
        );
        Parser parser = new Parser(tokens);
        SyntaxError exception = assertThrows(SyntaxError.class, () -> parser.parseArrayAccess());
        assertEquals("Expected NUMBER but reached EOF", exception.getMessage());
    }

    @Test
    void test_parseArrayAccess_wrongTokenError() {
        List<Token> tokens = List.of(
            new VariableToken(TokenType.ID, "x"),
            new StaticToken(TokenType.LEFT_PAREN)
        );
        Parser parser = new Parser(tokens);
        SyntaxError exception = assertThrows(SyntaxError.class, () -> parser.parseArrayAccess());
        assertEquals("Expected LEFT_SQB but got LEFT_PAREN", exception.getMessage());
    }

    // parseArrayIndex
    @Test
    void test_parseArrayIndex_succeeds() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.LEFT_SQB),
            new VariableToken(TokenType.NUMBER, "5"),
            new StaticToken(TokenType.RIGHT_SQB)
        );
        Parser parser = new Parser(tokens);
        ParseTree expectedParseTree = new ParseTree("ARRAY-INDEX", tokensToTreeNodes(tokens));
        ParseTree tree = parser.parseArrayIndex();
        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseArrayIndex_EOFError() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.LEFT_SQB),
            new VariableToken(TokenType.NUMBER, "3")
        );
        Parser parser = new Parser(tokens);
        SyntaxError exception = assertThrows(SyntaxError.class, () -> parser.parseArrayIndex());
        assertEquals("Expected RIGHT_SQB but reached EOF", exception.getMessage());
    }

    @Test
    void test_parseArrayIndex_wrongTokenError() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.LEFT_SQB),
            new VariableToken(TokenType.STRING, "\"value\""),
            new StaticToken(TokenType.RIGHT_SQB)
        );
        Parser parser = new Parser(tokens);
        SyntaxError exception = assertThrows(SyntaxError.class, () -> parser.parseArrayIndex());
        assertEquals("Expected NUMBER but got STRING ('\"value\"')", exception.getMessage());
    }

    // parseArrayLiteral

    // parseConditional

    // parseIf

    // parseElse

    // parseConditionalBody

    // parseBlockBody

    // parseLoop

    // parseFunctionDefinition

    // parseFunctionParameter

    // parseArrayType
    @Test
    void test_parseArrayType_success() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.KW_ARR),
            new VariableToken(TokenType.OP, "<"),
            new StaticToken(TokenType.KW_INT),
            new VariableToken(TokenType.OP, ">")
        );
        ParseTree expectedParseTree = new ParseTree("ARRAY-TYPE", tokensToTreeNodes(tokens));
        Parser parser = new Parser(tokens);
        ParseTree tree = parser.parseArrayType();
        assertEquals(expectedParseTree, tree);
    }

    @Test
    void test_parseArrayType_wrongTokenError() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.KW_ARR),
            new VariableToken(TokenType.OP, ">"),
            new StaticToken(TokenType.KW_INT),
            new VariableToken(TokenType.OP, "<")
        );
        Parser parser = new Parser(tokens);
        SyntaxError error = assertThrows(SyntaxError.class, () -> parser.parseArrayType());
        assertEquals("Expected < but got OP ('>')", error.getMessage());
    }

    @Test
    void test_parseArrayType_wrongTypeError() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.KW_ARR),
            new VariableToken(TokenType.OP, "<"),
            new VariableToken(TokenType.ID, "integer"),
            new VariableToken(TokenType.OP, ">")
        );
        Parser parser = new Parser(tokens);
        SyntaxError error = assertThrows(SyntaxError.class, () -> parser.parseArrayType());
        assertEquals("Expected TYPE but got ID ('integer')", error.getMessage());
    }

    // parseImmutableArrayDeclaration

    // parseArrayDeclaration

    // parseVariableDeclaration

    // parseVariableAssignment

    // parseType
    @ParameterizedTest
    @EnumSource(value = TokenType.class, names = {"KW_INT", "KW_STR", "KW_BOOL", "KW_FLOAT"})
    void test_parseType_primitiveTypeSuccess(TokenType type) {
        List<Token> token = List.of(new StaticToken(type));
        Parser parser = new Parser(token);
        assertEquals(new ParseTree(token.get(0)), parser.parseType());
    }

    @Test
    void test_parseType_fails() {
        List<Token> token = List.of(new StaticToken(TokenType.KW_FN));
        Parser parser = new Parser(token);
        SyntaxError error = assertThrows(SyntaxError.class, () -> parser.parseType());
        assertEquals("Expected TYPE but got KW_FN", error.getMessage());
    }

    // parseControlFlow
    @Test
    void test_parseControlFlow_successReturnOnly() {
        List<Token> token = List.of(new StaticToken(TokenType.KW_RET));
        ParseTree expectedParseTree = new ParseTree("CONTROL-FLOW", tokensToTreeNodes(token));
        Parser parser = new Parser(token);
        assertEquals(expectedParseTree, parser.parseControlFlow());
    }

    @Test
    void test_parseControlFlow_successReturnValue() {
        List<Token> tokens = List.of(
            new StaticToken(TokenType.KW_RET),
            new VariableToken(TokenType.ID, "value"),
            new VariableToken(TokenType.OP, "%"),
            new VariableToken(TokenType.NUMBER, "2")
        );
        ParseTree expectedParseTree = new ParseTree("CONTROL-FLOW", List.of(
            new ParseTree(tokens.get(0)),
            new ParseTree("EXPR", List.of(
                new ParseTree("TERM", List.of(
                    new ParseTree("VALUE", List.of(
                        new ParseTree(tokens.get(1)))
                    ),
                    new ParseTree(tokens.get(2)),
                    new ParseTree("TERM", List.of(
                        new ParseTree("VALUE", List.of(new ParseTree(tokens.get(3))))
                    ))
                ))
            ))
        ));
        Parser parser = new Parser(tokens);
        assertEquals(expectedParseTree, parser.parseControlFlow());
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class, names = {"KW_CNT", "KW_BRK"})
    void test_parseControlFlow_successNonReturn(TokenType type) {
        List<Token> token = List.of(new StaticToken(type));
        ParseTree expectedParseTree = new ParseTree("CONTROL-FLOW", tokensToTreeNodes(token));
        Parser parser = new Parser(token);
        assertEquals(expectedParseTree, parser.parseControlFlow());
    }
}
