package com.piedpiper.bolt.semantic;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import static java.util.Map.entry;

import java.util.ArrayList;

import com.piedpiper.bolt.lexer.TokenType;
import com.piedpiper.bolt.parser.AbstractSyntaxTree;

import lombok.Data;

@Data
public class EntityType {
    private final Map<TokenType, NodeType> typeMappings = Map.ofEntries(
        entry(TokenType.KW_BOOL, NodeType.BOOLEAN),
        entry(TokenType.KW_INT, NodeType.INT),
        entry(TokenType.KW_FLOAT, NodeType.FLOAT),
        entry(TokenType.KW_STR, NodeType.STRING),
        entry(TokenType.KW_ARR, NodeType.ARRAY),
        entry(TokenType.KW_NULL, NodeType.NULL)
    );
    private List<NodeType> type;

    public EntityType(NodeType type) {
        this.type = List.of(type);
    }

    public EntityType(NodeType... types) {
        this.type = Arrays.asList(types);
    }

    public EntityType(AbstractSyntaxTree AST) {
        if (typeMappings.containsKey(AST.getName())) {
            if (!AST.hasChildren())
                this.type = List.of(typeMappings.get(AST.getName()));
            else {
                List<NodeType> types = new ArrayList<>();
                AbstractSyntaxTree node = AST;
                while (node.hasChildren()) {
                    types.add(typeMappings.get(node.getName()));
                    node = node.getChildren().get(0);
                }
                types.add(typeMappings.get(node.getName()));
                this.type = types;
            }
        }
    }

    public boolean isSubType(EntityType entityType) {
        List<NodeType> typeList = entityType.getType();
        if (type.size() < typeList.size())
            return false;
        for (int i = 0; i < typeList.size(); i++) {
            if (!typeList.get(i).equals(type.get(i)))
                return false;
        }
        return true;
    }

    public boolean isType(NodeType type) {
        if (this.type.size() != 1)
            return false;
        return this.type.get(0) == type;
    }
}
