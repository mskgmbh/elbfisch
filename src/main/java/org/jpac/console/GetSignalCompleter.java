package org.jpac.console;

import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jpac.SignalRegistry;

public class GetSignalCompleter implements Completer{
    static final int    SIGNALNAMEINDEX = 1;
    static final int    TOINDEX         = 2;
    static final int    VALUEINDEX      = 3;
    static final String SEPARATOR       = ".";

    protected IdentifierTree signalIdentifierTree = null;

    public GetSignalCompleter() {
    }

    public void complete(LineReader reader, ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;
        if (signalIdentifierTree == null){
            signalIdentifierTree = new IdentifierTree();
            SignalRegistry.getInstance().getSignals().values().stream().forEach(s -> signalIdentifierTree.poorIn(s.getQualifiedIdentifier(), IdentifierNode.getType(s)));
        }        
        String buffer = commandLine.word().substring(0, commandLine.wordCursor());
        switch(commandLine.wordIndex()){
            case SIGNALNAMEINDEX:
                if (buffer.length() == 0) {
                    IdentifierNode rootNode = signalIdentifierTree.getRoot();
                    candidates.add(new Candidate(rootNode.getIdentifier() + SEPARATOR, rootNode.getIdentifier() + SEPARATOR,null,null,null,null,false));
                } 
                else {
                    IdentifierNode node = signalIdentifierTree.findNodeWhichAtLeastPartiallyMatches(buffer);
                    if (node != null){
                        node.getChildren().values().forEach(n -> {
                                candidates.add(new Candidate(n.getQualifiedIdentifier() + (n.hasChildren() ? SEPARATOR : ""), n.getIdentifier(),null,null,null,null, !n.hasChildren()));
                            });
                    }
                }
                break;
            default:
                // ignore
        }
    }
}
