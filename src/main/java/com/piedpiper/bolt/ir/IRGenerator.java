package com.piedpiper.bolt.ir;

import com.piedpiper.bolt.lexer.TokenType;
import com.piedpiper.bolt.parser.AbstractSyntaxTree;
import com.piedpiper.bolt.semantic.EntityType;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {
    private final List<String> IR = new ArrayList<>();
    private boolean inFunc = false;
    private String typeLabelReverse(TokenType type) {
        if (type == TokenType.KW_INT)
            return "int";
        if (type == TokenType.KW_DOUBLE)
            return "double";
        if (type == TokenType.KW_BOOL)
            return "boolean";
        if (type == TokenType.KW_STR)
            return "string";
        if (type == TokenType.KW_NULL)
            return "null";
        return "unknown";
    }

    public List<String> generate(AbstractSyntaxTree AST) {
        List<String> IR = new ArrayList<>();
        if (AST.matchesLabel("PROGRAM")) {
            for (AbstractSyntaxTree subTree : AST.getChildren()) {
                IR.addAll(generate(subTree));
            }
        }
        else if (AST.matchesLabel("VAR-DECL")) {
            IR.addAll(generateVarDecl(AST.getChildren()));
        }
        else if (AST.matchesValue("+"))
            IR.addAll(generateAddition(AST.getChildren()));
        else if (AST.matchesValue("*"))
            IR.addAll(generateMultiplication(AST.getChildren()));
        else
            IR.add(generateConstant(AST));
        return IR;
    }

    public List<String> generateVarDecl(List<AbstractSyntaxTree> children) {
        List<String> IR = new ArrayList<>();
        if (!inFunc)
            IR.add("global");
        for (int i = 0; i < children.size() - 1; i++) {
            IR.add(
                children.get(i).isTypeLabel() ? typeLabelReverse(children.get(i).getName()) : children.get(i).getValue()
            );
        }
        IR.add("=");
        IR.addAll(generate(children.get(children.size() - 1)));
        return IR;
    }

    private String generateConstant(AbstractSyntaxTree node) {
        return node.getValue();
    }

    private List<String> generateAddition(List<AbstractSyntaxTree> children) {
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(children.get(1)));
        IR.addAll(generate(children.get(0)));
        IR.add("ADD");
        return IR;
    }

    private List<String> generateMultiplication(List<AbstractSyntaxTree> children) {
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(children.get(1)));
        IR.addAll(generate(children.get(0)));
        IR.add("MUL");
        return IR;
    }
}
