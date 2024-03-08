package org.jpac.console;

public class IdentifierTree {
    static final String DELIMITER = "\\.";
    protected IdentifierNode root = null;

    public IdentifierNode getRoot(){
        return root;
    }

    public void poorIn(String signalIdentifier, IdentifierNode.Type type){
        String[] tokens = signalIdentifier.split(DELIMITER);
        if (root == null){
            root = new IdentifierNode(null, tokens[0], IdentifierNode.Type.NONE);
        }
        IdentifierNode node = root;
        for (int i = 1; i < tokens.length; i++){
            node = node.addChildNode(new IdentifierNode(node, tokens[i], (i == tokens.length - 1) ? type : IdentifierNode.Type.NONE));
        }
    }

    public IdentifierNode findNode(String partialSignalIdentifier){
        String[] tokens = partialSignalIdentifier.split(DELIMITER);
        IdentifierNode node = root;
        for (int i = 1; i < tokens.length; i++){
            node = node.getChildNode(tokens[i]);
        }
        return node;
    }

    public IdentifierNode findNodeWhichAtLeastPartiallyMatches(String partialSignalIdentifier){
        String[] tokens = partialSignalIdentifier.split(DELIMITER);
        IdentifierNode node = root;
        boolean found = false;
        for (int i = 1; i < tokens.length && !found; i++){
            final int                  fi       = i;
            final IdentifierNode fnode    = node;      
            IdentifierNode       nextNode = node.getChildren().keySet().stream().filter(key -> key.startsWith(tokens[fi])).map(key -> fnode.getChildNode(key)).findFirst().orElse(null);       
            node = nextNode;
        }
        return node;
    }
}    
