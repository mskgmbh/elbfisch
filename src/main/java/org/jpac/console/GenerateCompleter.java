package org.jpac.console;

import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class GenerateCompleter implements Completer{
    static final int    WHATINDEX = 1;
    static final String HISTOGRAM       = "histogram";
    static final String SNAPSHOT        = "snapshot";

    protected IdentifierTree signalIdentifierTree = null;

    public GenerateCompleter() {
    }

    public void complete(LineReader reader, ParsedLine commandLine, final List<Candidate> candidates) {
        assert commandLine != null;
        assert candidates != null;
        String buffer = commandLine.word().substring(0, commandLine.wordCursor());
        switch(commandLine.wordIndex()){
            case WHATINDEX:
                if (buffer.length() == 0) {
                    candidates.add(new Candidate(HISTOGRAM, HISTOGRAM,null,null,null,null,true));
                    candidates.add(new Candidate(SNAPSHOT, SNAPSHOT,null,null,null,null,true));
                } else if (HISTOGRAM.startsWith(buffer)){
                            candidates.add(new Candidate(HISTOGRAM, HISTOGRAM,null,null,null,null,true));                            
                } else if (SNAPSHOT.startsWith(buffer)){
                    candidates.add(new Candidate(SNAPSHOT, SNAPSHOT,null,null,null,null,true));                            
                }
                break;
            default:
                // ignore
        }
    }
}
