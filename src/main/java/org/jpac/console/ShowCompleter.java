package org.jpac.console;

import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jpac.JPac;
import org.jpac.SignalRegistry;

public class ShowCompleter implements Completer{
    static final int    STATSTAINDEX    = 1;
    static final int    STATEINDEX      = 2;
    static final int    OFINDEX         = 3;
    static final int    MODULEINDEX     = 4;
    static final String STATISTICS      = "statistics";
    static final String STATE           = "state";
    static final String OF              = "of";
    static final String MODULE          = "<module>";
    static final String SEPARATOR       = ".";

    protected IdentifierTree signalIdentifierTree = null;
    protected IdentifierTree moduleIdentifierTree = null;

    public ShowCompleter() {
    }

    public void complete(LineReader reader, ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;
        if (signalIdentifierTree == null){
            signalIdentifierTree = new IdentifierTree();
            SignalRegistry.getInstance().getSignals().values().stream().forEach(s -> signalIdentifierTree.poorIn(s.getQualifiedIdentifier(), IdentifierNode.getType(s)));
        }
        if (moduleIdentifierTree == null){
            moduleIdentifierTree = new IdentifierTree();
            JPac.getInstance().getModules().values().stream().forEach(m -> moduleIdentifierTree.poorIn(m.getQualifiedName(), IdentifierNode.Type.NONE));
        }
        String buffer = commandLine.word().substring(0, commandLine.wordCursor());
        switch(commandLine.wordIndex()){
            case STATSTAINDEX:
                if (buffer.length() == 0) {
                    candidates.add(new Candidate(STATISTICS, STATISTICS,null,null,null,null,false));
                    candidates.add(new Candidate(STATE, STATE,null,null,null,null,false));
                } 
                else {
                    if (STATE.startsWith(buffer)){
                        candidates.add(new Candidate(STATE, STATE,null,null,null,null,false));                            
                    }
                    if (STATISTICS.startsWith(buffer)){
                        candidates.add(new Candidate(STATISTICS, STATISTICS,null,null,null,null,true));                            
                    }
                }
                break;
            case OFINDEX:
                if (buffer.length() == 0) {
                    candidates.add(new Candidate(OF, OF,null,null,null,null,false));
                } 
                else {
                    if (OF.startsWith(buffer)){
                        candidates.add(new Candidate(OF, OF,null,null,null,null,false));                            
                    }
                }
                break;
            case MODULEINDEX:
                if (buffer.length() == 0) {
                    IdentifierNode rootNode = moduleIdentifierTree.getRoot();
                    candidates.add(new Candidate(rootNode.getIdentifier() + SEPARATOR, rootNode.getIdentifier() + SEPARATOR,null,null,null,null,false));
                } 
                else {
                    IdentifierNode node = moduleIdentifierTree.findNodeWhichAtLeastPartiallyMatches(buffer);
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
