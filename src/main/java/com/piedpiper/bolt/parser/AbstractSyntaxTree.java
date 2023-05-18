package com.piedpiper.bolt.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.piedpiper.bolt.lexer.TokenType;
import com.piedpiper.bolt.lexer.VariableToken;
import com.piedpiper.bolt.lexer.Token;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class AbstractSyntaxTree {
    private String type;
    private Token token;
    private List<AbstractSyntaxTree> children = new ArrayList<>();

    public AbstractSyntaxTree(String type) {
        this.type = type;
        this.token = null;
    }

    public AbstractSyntaxTree(String type, List<AbstractSyntaxTree> children) {
        this.type = type;
        this.token = null;
        this.children.addAll(children); // setting this.children = children causes the list to become immutable
    }

    public AbstractSyntaxTree(Token token) {
        this.type = "terminal";
        this.token = token;
    }

    public TokenType getName() {
        if (token == null)
            return null;
        return token.getName();
    }

    public String getValue() {
        if (token == null)
            return null;
        return token.getValue();
    }

    public AbstractSyntaxTree(String type, Token token) {
        this.type = type;
        this.token = null;
        this.children.add(new AbstractSyntaxTree(token));
    }

    public AbstractSyntaxTree(Token token, List<AbstractSyntaxTree> children) {
        this.type = "terminal";
        this.token = token;
        this.children.addAll(children);
    }

    public static List<AbstractSyntaxTree> tokensToNodes(List<Token> tokens) {
        return tokens.stream().map(AbstractSyntaxTree::new).collect(Collectors.toList());
    }

    // go in sequential order of parents and created nested tree nodes; the last node contains tree
    public static AbstractSyntaxTree createNestedTree(AbstractSyntaxTree tree, String... parents) {
        if (parents.length == 1)
            return new AbstractSyntaxTree(parents[0], List.of(tree));

        AbstractSyntaxTree root = new AbstractSyntaxTree(parents[0]);
        int end = parents.length - 1;
        AbstractSyntaxTree node = root;
        AbstractSyntaxTree subTree;
        for (int i = 1; i < end; i++) {
            subTree = new AbstractSyntaxTree(parents[i]);
            node.appendChildren(subTree);
            node = subTree;
        }
        node.appendChildren(
            new AbstractSyntaxTree(parents[end], List.of(tree))
        );
        return root;
    }

    // go in sequential order of parents and created nested tree nodes; the last node contains token
    public static AbstractSyntaxTree createNestedTree(Token token, String...parents) {
        return createNestedTree(new AbstractSyntaxTree(token), parents);
    }

    // go in sequential order of parents and created nested tree nodes; the last node contains tokens
    public static AbstractSyntaxTree createNestedTree(List<Token> tokens, String... parents) {
        if (parents.length == 1)
            return new AbstractSyntaxTree(parents[0], tokensToNodes(tokens));
        
        AbstractSyntaxTree root = new AbstractSyntaxTree(parents[0]);
        int end = parents.length - 1;
        AbstractSyntaxTree node = root;
        AbstractSyntaxTree subTree;
        for (int i = 1; i < end; i++) {
            subTree = new AbstractSyntaxTree(parents[i]);
            node.appendChildren(subTree);
            node = subTree;
        }
        node.appendChildren(
            new AbstractSyntaxTree(parents[end], tokensToNodes(tokens))
        );
        return root;
    }

    public void appendChildren(AbstractSyntaxTree... children) {
        Collections.addAll(this.children, children);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indentLevel) {
        String indentation = getNestedIndentation(indentLevel);
        StringBuilder output = new StringBuilder(indentation + "AST => ");
        if (type.equals("terminal")) {
            output.append("Token: ").append(token.getName());
            if (token instanceof VariableToken)
                output.append(" ('").append(token.getValue()).append("')");
            if (token.getLineNumber() != 0)
                output.append(", line: ").append(token.getLineNumber());
        }
        else {
            output.append(type);
        }
        if (!children.isEmpty()) {
            output.append(", children: [");
            for (AbstractSyntaxTree child : children) {
                output.append("\n").append(child.toString(indentLevel + 1));
            }
            output.append("\n").append(indentation).append("]");
        }
        return output.toString();
    }

    private String getNestedIndentation(int indentLevel) {
        return "\t".repeat(Math.max(0, indentLevel));
    }
}