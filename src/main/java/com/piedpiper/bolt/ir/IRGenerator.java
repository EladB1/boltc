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
        else if (AST.matchesValue("+") || AST.matchesValue("-") || AST.matchesValue("^") || AST.matchesValue("&"))
            IR.addAll(generateAddition(AST));
        else if (AST.matchesValue("*") || AST.matchesValue("/") || AST.matchesValue("%") || AST.matchesValue("**"))
            IR.addAll(generateMultiplication(AST));
        else if (AST.matchesValue("==") || AST.matchesValue("!=") || AST.matchesValue("<") || AST.matchesValue("<=") || AST.matchesValue(">") || AST.matchesValue(">="))
            IR.addAll(generateComparison(AST));
        else if (AST.matchesValue("||") || AST.matchesValue("&&"))
            IR.addAll(generateLogicalExpression(AST));
        else if (AST.matchesLabel("UNARY-OP"))
            IR.addAll(generateUnaryOp(AST));
        else if (AST.matchesLabel("FUNC-CALL"))
            IR.addAll(generateFunctionCall(AST));
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
        if (node.isStringLiteral())
            return "\"" + node.getValue() + "\"";
        return node.getValue();
    }

    private List<String> generateAddition(AbstractSyntaxTree operation) {
        List<AbstractSyntaxTree> children = operation.getChildren();
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(children.get(1)));
        IR.addAll(generate(children.get(0)));
        if (operation.matchesValue("+"))
            IR.add("ADD");
        if (operation.matchesValue("-"))
            IR.add("SUB");
        if (operation.matchesValue("^"))
            IR.add("XOR");
        if (operation.matchesValue("&"))
            IR.add("B_AND");
        return IR;
    }

    private List<String> generateMultiplication(AbstractSyntaxTree operation) {
        List<AbstractSyntaxTree> children = operation.getChildren();
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(children.get(1)));
        IR.addAll(generate(children.get(0)));
        if (operation.matchesValue("*"))
            IR.add("MUL");
        if (operation.matchesValue("/"))
            IR.add("DIV");
        if (operation.matchesValue("%"))
            IR.add("REM");
        if (operation.matchesValue("**"))
            IR.add("POW");
        return IR;
    }

    private List<String> generateUnaryOp(AbstractSyntaxTree node) {
        AbstractSyntaxTree left = node.getChildren().get(0);
        AbstractSyntaxTree right = node.getChildren().get(1);
        List<String> IR = new ArrayList<>();
        if (left.matchesValue("!")) {
            IR.addAll(generate(right));
            IR.add("NOT");
        }
        if (left.matchesValue("++")) {
            IR.add("1");
            IR.addAll(generate(right));
            IR.add("ADD");
        }
        if (right.matchesValue("++")) {
            IR.add("1");
            IR.addAll(generate(left));
            IR.add("ADD");
        }
        if (left.matchesValue("--")) {
            IR.add("1");
            IR.addAll(generate(right));
            IR.add("SUB");
        }
        // TODO: Differentiate between increment/decrement operator on either side of variable
        if (right.matchesValue("--")) {
            IR.add("1");
            IR.addAll(generate(left));
            IR.add("SUB");
        }
        if (left.matchesValue("-")) {
            IR.addAll(generate(right));
            IR.add("0");
            IR.add("SUB");
        }
        return IR;
    }

    private List<String> generateComparison(AbstractSyntaxTree comparison) {
        List<AbstractSyntaxTree> children = comparison.getChildren();
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(children.get(1)));
        IR.addAll(generate(children.get(0)));
        if (comparison.matchesValue("=="))
            IR.add("EQ");
        if (comparison.matchesValue("!="))
            IR.add("NE");
        if (comparison.matchesValue("<"))
            IR.add("LT");
        if (comparison.matchesValue("<="))
            IR.add("LE");
        if (comparison.matchesValue(">"))
            IR.add("GT");
        if (comparison.matchesValue(">="))
            IR.add("GE");
        return IR;
    }

    private List<String> generateLogicalExpression(AbstractSyntaxTree operator) {
        List<String> IR = new ArrayList<>();
        IR.addAll(generate(operator.getChildren().get(1)));
        IR.addAll(generate(operator.getChildren().get(0)));
        if (operator.matchesValue("||"))
            IR.add("OR");
        if (operator.matchesValue("&&"))
            IR.add("AND");
        return IR;
    }

    private List<String> generateTernary(AbstractSyntaxTree node) {
        List<String> IR = new ArrayList<>();

        return IR;
    }

    private List<String> generateArrayLiteral(AbstractSyntaxTree node) {
        List<String> IR = new ArrayList<>();

        return IR;
    }

    private List<String> generateArrayIndex(AbstractSyntaxTree node) {
        List<String> IR = new ArrayList<>();

        return IR;
    }

    // TODO: Match builtins to their IR names
    private List<String> generateFunctionCall(AbstractSyntaxTree node) {
        List<String> IR = new ArrayList<>();
        IR.add("CALL");
        IR.add(node.getChildren().get(0).getValue());
        if (node.countChildren() == 1)
            IR.add("0");
        else {
            List<AbstractSyntaxTree> params = node.getChildren().get(1).getChildren();
            IR.add(String.valueOf(params.size()));
            StringBuilder sb = new StringBuilder();
            List<String> paramIR;
            sb.append("(");
            for (int i = 0; i < params.size(); i++) {
                paramIR = generate(params.get(i));
                for (int j = 0; j < paramIR.size(); j++) {
                    sb.append(paramIR.get(j));
                    if (j < paramIR.size() - 1)
                        sb.append(" ");
                }
                if (i < params.size() - 1)
                    sb.append(",");
            }
            sb.append(")");
            IR.add(sb.toString());
        }
        return IR;
    }
}
