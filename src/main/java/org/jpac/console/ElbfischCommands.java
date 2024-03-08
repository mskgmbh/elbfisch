package org.jpac.console;

import org.jline.console.CommandMethods;
import org.jline.console.CommandInput;
import org.jline.reader.Completer;
import org.jpac.statistics.Histogram;
import org.jline.console.impl.JlineCommandRegistry;
import java.util.Map;
import java.util.concurrent.RunnableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.FutureTask;

import org.jpac.AbstractModule;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.JPac;
import org.jpac.Logical;
import org.jpac.Signal;
import org.jpac.SignalRegistry;
import org.jpac.SignedInteger;
import org.jpac.snapshot.Snapshot;

public class ElbfischCommands extends JlineCommandRegistry{
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_BLACK  = "\u001B[30m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE   = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";  
    private static final String ANSI_BOLD   = "\u001B[1m";

    private final Map<String, List<String>> commandInfo;
    protected IdentifierTree                signalIdentifierTree;
    protected IdentifierTree                moduleIdentifierTree;    

    public ElbfischCommands() {
        super();

        Map<String, CommandMethods> commandExecute = new HashMap<>();
        commandExecute.put(SetSignal.key, new CommandMethods(SetSignal::command, SetSignal::completer));
        commandExecute.put(GetSignal.key, new CommandMethods(GetSignal::command, GetSignal::completer));
        commandExecute.put(GenerateHistogramOrSnapshot.key, new CommandMethods(GenerateHistogramOrSnapshot::command, GenerateHistogramOrSnapshot::completer));
        commandExecute.put(ListSignalsOrModules.key, new CommandMethods(ListSignalsOrModules::command, ListSignalsOrModules::completer)); 
        registerCommands(commandExecute);
        commandInfo = new HashMap<>();
        commandInfo.put(SetSignal.key, Arrays.asList("set signal <signal> to <value>"));
        commandInfo.put(GetSignal.key, Arrays.asList("get signal <signal>"));
        commandInfo.put(GenerateHistogramOrSnapshot.key, Arrays.asList("generate histogram of timeconsumption of modules per cycle, generate snapshot of current state of signals and modules"));
        commandInfo.put(ListSignalsOrModules.key, Arrays.asList("list signals <pattern>, list modules <pattern>. Use preceding or trailing '*' as wildcard."));
        signalIdentifierTree = new IdentifierTree();
        moduleIdentifierTree = new IdentifierTree();        
    }

    public static class SetSignal{
        public static String key = "set";
        public static void command(CommandInput input){
            System.out.println("SetSignal entered: ");
            for (var s : input.args())
                System.out.println(s);
            StringBuffer result     = new StringBuffer();
            String searchString  = input.args()[1];
            String valueAsString = input.args()[3];
            if (searchString.endsWith("*")){
                result.append(ANSI_RED + "search string ending with '*' not allowed in this context" + ANSI_RESET  + "\r\n");
            }
            else{
                SignalRegistry.getInstance().getSignals().values().stream()
                    .filter(s -> matches(s.getQualifiedIdentifier(), searchString))
                    .forEach(s -> {result.append(setValue(s, valueAsString) + "\r\n");});
                if (result.length() == 0){
                    result.append(ANSI_RED + "no signals matching '" + searchString + "' found" + ANSI_RESET  + "\r\n");
                }
            }
            System.out.println(result.toString());                
        }

        public static List<Completer> completer(String command){
            List<Completer> completers = new ArrayList<>();
            completers.add(new SetSignalCompleter());
            return completers;
        }
    }

    public static class GetSignal{
        public static String key = "get";
        public static void command(CommandInput input){
            System.out.println("GetSignal entered: ");
            for (var s : input.args())
                System.out.println(s);
        }

        public static List<Completer> completer(String command){
            List<Completer> completers = new ArrayList<>();
            completers.add(new GetSignalCompleter());
            return completers;
        }
    }
    
    public static class GenerateHistogramOrSnapshot{
        private static final String HISTOGRAM   = "histogram";
        private static final String SNAPSHOT    = "snapshot";        
        public  static final String key         = "generate";

        public static void command(CommandInput input){
            System.out.println("Generate entered: ");
            for (var s : input.args())
                System.out.println(s);
            //String cmd  = input.args()[0];
            String arg = input.args()[1];
            if (arg.equals(HISTOGRAM)){
                System.out.println("generate histogram");
                String result = null;
                RunnableFuture<ArrayList<Histogram>> rf = new FutureTask<>(() -> JPac.getInstance().getHistograms());
                JPac.getInstance().invokeLater(rf);
                try{
                    File file = new File(JPac.getInstance().getHistogramFile());                        
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
                    rf.get().forEach(h -> out.println(h.toCSV()));
                    out.close();
                    result =  "histogram stored to " + file.getCanonicalPath() + "\r\n";
                } 
                catch (IOException | InterruptedException | java.util.concurrent.ExecutionException exc) {
                    result = exc.getMessage();
                }
                System.out.println(result.toString());   
            }            
            else if (arg.equals(SNAPSHOT)){
                System.out.println("generate snapshot");
                String result = null;
                RunnableFuture<Snapshot> rf = new FutureTask<>(() -> JPac.getInstance().getSnapshot());
                JPac.getInstance().invokeLater(rf);
                try{
                    rf.get().dump(JPac.getInstance().getDataDir());
                    result =  "snapshot stored to " + rf.get().getFilename() + "\r\n";
                } catch (Exception exc) {
                    result = ANSI_RED + "failed to store snapshot: " + exc.getMessage() + ANSI_RESET  + "\r\n";
                }
                System.out.println(result.toString());                
            }
        }

        public static List<Completer> completer(String command){
            List<Completer> completers = new ArrayList<>();
            completers.add(new GenerateCompleter());
            return completers;
        }          
    }

    public static class ListSignalsOrModules{
        private static final String SIGNALS   = "signals";
        private static final String MODULES   = "modules";        
        public static String    key = "list";
        public static void command(CommandInput input){
            System.out.println("list entered: ");
            for (var s : input.args())
                System.out.println(s);
            String result = null;
            String arg    = input.args()[1];
            String param  = input.args()[2];
            if (arg.equals(SIGNALS)){
                System.out.println("list signals");
                StringBuffer listOfSignalIdentifiers = new StringBuffer();
                String        searchString           = param;
                SignalRegistry.getInstance().getSignals().values().stream()
                        .filter(s -> matches(s.getQualifiedIdentifier(), searchString))
                        .sorted((s1,s2) -> s1.getQualifiedIdentifier().compareTo(s2.getQualifiedIdentifier()))
                        .forEach(s -> listOfSignalIdentifiers.append(s + "\r\n"));
                result = listOfSignalIdentifiers.toString();
                System.out.println(result.toString());   
            }            
            else if (arg.equals(MODULES)){
                System.out.println("list modules");
                StringBuffer listOfModuleIdentifiers = new StringBuffer();
                String       searchString            = param;
                RunnableFuture<Collection<AbstractModule>> rf = new FutureTask<>(() -> JPac.getInstance().getModules().values());
                JPac.getInstance().invokeLater(rf);
                try{
                    rf.get().stream()
                        .filter(m -> matches(m.getQualifiedName(), searchString))
                        .sorted((m1,m2) -> m1.getQualifiedName().compareTo(m2.getQualifiedName()))
                        .forEach(m -> listOfModuleIdentifiers.append(m.getQualifiedName()));
                }
                catch(Exception exc){
                    //nothing to do
                };
                if (listOfModuleIdentifiers.length() == 0){
                    listOfModuleIdentifiers.append("no matching modules found");                        
                }
                result = listOfModuleIdentifiers.toString();
                System.out.println(result.toString());              }
        }

        public static List<Completer> completer(String command){
            List<Completer> completers = new ArrayList<>();
            completers.add(new ListCompleter());
            return completers;
        }          
    }  
    
    @Override
    public List<String> commandInfo(String command) {
        return commandInfo.get(command);
    }

    static protected boolean matches(String identifier , String searchString){
        boolean match = false;
        if (searchString.startsWith("*")){
            if (searchString.endsWith("*")){
                // searchString = "*" or "*xxxxxx*"
                match = searchString.trim().length() == 1 || identifier.contains(searchString.substring(1, searchString.length()-1));
            }
            else{
                // searchString = "*xxxxxx"
                match = identifier.endsWith(searchString.substring(1));                
            }
        }
        else if (searchString.endsWith("*")){
            // searchString = "xxxxxx*"
            String subString = searchString.substring(0,searchString.length()-1);
            match = identifier.startsWith(subString);                            
        }
        else{
            // searchString = "xxxxxx"
            match = identifier.equals(searchString);                                        
        }
        return match;
    } 
    
    static protected String setValue(Signal signal, String valueAsString){
        String result = null;
        try{
            if (signal instanceof Logical){
                ((Logical)signal).setDeferred(Boolean.parseBoolean(valueAsString));
            } else if (signal instanceof SignedInteger){
                ((SignedInteger)signal).setDeferred(Integer.parseInt(valueAsString));
            } else if (signal instanceof Decimal){
                ((Decimal)signal).setDeferred(Double.parseDouble(valueAsString));            
            } else if (signal instanceof CharString){
                ((CharString)signal).setDeferred(valueAsString);                                
            }
            result = ANSI_GREEN + signal + " set to '" + valueAsString + "'" + ANSI_RESET;
        }
        catch(NumberFormatException exc){
            result = ANSI_RED + "failed to set " + signal + ANSI_RESET;
        }
        return result;
    }    
}
