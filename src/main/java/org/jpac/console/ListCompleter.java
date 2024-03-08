package org.jpac.console;

import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jpac.JPac;
import org.jpac.SignalRegistry;

public class ListCompleter implements Completer{
    static final int    SIGMODSINDEX    = 1;
    static final int    SIGMODNAMEINDEX = 2;
    static final String SIGNALS         = "signals";
    static final String MODULES         = "modules";
    static final String SEPARATOR       = ".";

    protected IdentifierTree signalIdentifierTree = null;
    protected IdentifierTree moduleIdentifierTree = null;

    public ListCompleter() {
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
            case SIGMODSINDEX:
                if (buffer.length() == 0) {
                    candidates.add(new Candidate(SIGNALS, SIGNALS,null,null,null,null,false));
                    candidates.add(new Candidate(MODULES, MODULES,null,null,null,null,false));
                } 
                else {
                    if (SIGNALS.startsWith(buffer)){
                        candidates.add(new Candidate(SIGNALS, SIGNALS,null,null,null,null,true));                            
                    }
                    if (MODULES.startsWith(buffer)){
                        candidates.add(new Candidate(MODULES, MODULES,null,null,null,null,true));                            
                    }
                }
                break;
            case SIGMODNAMEINDEX:
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
