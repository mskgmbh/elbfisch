package org.jpac.console;

import java.util.HashMap;

import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignedInteger;

public class IdentifierNode {
        public enum Type{
            LOGICAL,
            SIGNEDINTEGER,
            DECIMAL,
            CHARSTRING,
            NONE
        }
        protected String                          identifier;
        protected IdentifierNode                  parent;
        protected HashMap<String, IdentifierNode> children;
        protected Type                            type;

        public IdentifierNode(IdentifierNode parent, String identifier, Type type){
            this.parent     = parent;
            this.identifier = identifier;
            this.type       = type;
            children        = new HashMap<>();
        }
        
        public IdentifierNode getParent(){
            return  parent;
        }

        public String getIdentifier(){
            return identifier;
        }

        public String getQualifiedIdentifier(){
            if (parent == null){
                return identifier;
            }
            return parent.getQualifiedIdentifier() + "." + identifier;
        }

        
        public Type getType(){
            return type;
        }
        
        // Add a child node to the current node if not already present and returns the inserted node
        public IdentifierNode addChildNode(IdentifierNode node){
            if (!children.containsKey(node.getIdentifier())){
                children.put(node.getIdentifier(), node);
            }
            return children.get(node.getIdentifier());
        }

        public IdentifierNode getChildNode(String identifier){
            return children.get(identifier);
        }

        public HashMap<String,IdentifierNode> getChildren(){
            return children;
        }

        public boolean hasChildren(){
            return !children.isEmpty();
        }

        static IdentifierNode.Type getType(Signal signal){
            if (signal instanceof Logical)
                return IdentifierNode.Type.LOGICAL;
            if (signal instanceof SignedInteger)
                return IdentifierNode.Type.SIGNEDINTEGER;
            if (signal instanceof Decimal)
                return IdentifierNode.Type.DECIMAL;
            if (signal instanceof CharString)
                return IdentifierNode.Type.CHARSTRING;
            return IdentifierNode.Type.NONE;
        }        
}    
