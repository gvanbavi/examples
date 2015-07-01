package cern.lhc.betabeating.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This will generate .java Source files for external programs.
 * 
 * The purpose of this file/program is to automatically generate Source files for handling external Programs with some type safety. 
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class InternalProgramWriter {
    /*
     * Usage: Change programName, necessaryArguments and optionalArguments. (If argument format should be changed, look for //argument format should be changed here )
     * Run.
     * Create java files from output.
     * Do Changes if needed.
     * Remember to do changes in ProgramPaths if needed.
     */
    
    final private static String programName = "SvdClean";
    final private static List<String> necessaryArguments = Arrays.asList(new String[] {"turn", "p", "sumsquare", "sing_val", "file"});
    final private static List<String> optionalArguments = Arrays.asList(new String[] {"std_dev"});
    
    
    //probably no changes needed from here
    public static void main(String[] args) {
        prepare();
        allArguments.addAll(necessaryArguments);
        allArguments.addAll(optionalArguments);
        printMainJavaFile();
        System.out.println("###########################################################");
        printDataJavaFile();
    }
    private static List<String> allArguments = new ArrayList<String>();

    private static void prepare() {
            if (!Character.isUpperCase(programName.charAt(0)))
                throw new IllegalArgumentException("First character in programName should be uppercase: " + programName);
            if (necessaryArguments.size() == 0 && optionalArguments.size() == 0)
                throw new IllegalArgumentException("No Arguments?");
    }
    
    private static String getProgramNameLowerCaseFirst()
    {
        return programName.replace(programName.charAt(0), Character.toLowerCase(programName.charAt(0)));
    }
    
    private static String downFirst(String string)
    {
        return string.replace(string.charAt(0), Character.toLowerCase(string.charAt(0)));
    }
    
    private static String upFirst(String string)
    {
        return string.replace(string.charAt(0), Character.toUpperCase(string.charAt(0)));
    }

    private static void printMainJavaFile() {
        System.out.println("package cern.lhc.betabeating.external.programs;\n" + 
        		"\n" + 
        		"import cern.lhc.betabeating.external.ProgramPaths;\n" + 
        		"import cern.lhc.betabeating.external.Systemcall;\n" + 
        		"import cern.lhc.betabeating.external.SystemcallData;\n" + 
        		"import cern.lhc.betabeating.external.interfaces.Program;" + 
        		"\n" + 
        		"public class " + programName + " implements Program{\n" + 
        		"    private static final String applicationName = \"" + programName + "\";\n" + 
        		"    private ProgramPaths programPaths = null;\n" + 
        		"    \n" + 
        		"    public " + programName + "(ProgramPaths programPaths) {\n" + 
        		"        this.programPaths = programPaths;\n" + 
        		"    }\n" + 
        		"    public int execute(" + programName + "Data " + getProgramNameLowerCaseFirst() + ") {\n" + 
        		"        String path = programPaths.getPathForClass(getClass());\n" + 
        		"        String arguments = " + getProgramNameLowerCaseFirst() + ".getArguments();\n" + 
        		"        \n" + 
        		"        String command = path + arguments;\n" + 
        		"        String[] environment = null;\n" + 
        		"        String pathForLogCommand = " + getProgramNameLowerCaseFirst() + ".getOutputPath();\n" + 
        		"        boolean logCommand = true;\n" + 
        		"        SystemcallData systemcallData = new SystemcallData(command, applicationName, environment, logCommand, pathForLogCommand);\n" + 
        		"        return Systemcall.execute(systemcallData);\n" + 
        		"    }\n" + 
        		"}");
    }

    private static void printDataJavaFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package cern.lhc.betabeating.external.programs;\n");
        stringBuilder.append("\n"); 
        stringBuilder.append("import cern.lhc.betabeating.external.interfaces.ProgramData;");
        stringBuilder.append("\n");
        stringBuilder.append("public class " + programName + "Data implements ProgramData{\n");
        for (String allArgumentsItem : allArguments)
            stringBuilder.append("private String " + downFirst(allArgumentsItem) + ";\n");
        stringBuilder.append("    \n");
        //member variables done
        stringBuilder.append("    private " + programName + "Data() {\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("    \n");
       //constructor done
        stringBuilder.append("    @Override\n");
        stringBuilder.append("    public String getArguments() {\n");
        stringBuilder.append("        StringBuilder stringBuilder = new StringBuilder();\n");
        stringBuilder.append("        stringBuilder\n");
        for (String allArgumentsItem : necessaryArguments)
            stringBuilder.append("                     .append(\" --" + downFirst(allArgumentsItem) + "=\").append(" + downFirst(allArgumentsItem) + ")\n"); //argument format should be changed here
        stringBuilder.append("                     ;\n");
        for (String allArgumentsItem : optionalArguments)
        {
            stringBuilder.append("        if (" + allArgumentsItem + " != null)\n");
            stringBuilder.append("            stringBuilder.append(\" --" + allArgumentsItem + "=\").append(" + allArgumentsItem + ");"); //argument format should be changed here
        }
        stringBuilder.append("        return stringBuilder.toString();\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public static " + programName + "DataCreate prepareObject()\n");
        stringBuilder.append("    {\n");
        stringBuilder.append("        return new " + programName + "DataCreate(new " + programName + "Data());\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("    \n");
        for (String allArgumentsItem : allArguments)
        {
            stringBuilder.append("    private void set" + upFirst(allArgumentsItem) + "(String " + downFirst(allArgumentsItem) + ") {\n");
            stringBuilder.append("        this." + downFirst(allArgumentsItem) + " = " + downFirst(allArgumentsItem) + ";\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("\n");
        }
        //setter done
        for (String allArgumentsItem : allArguments)
        {
            stringBuilder.append("    public String get" + upFirst(allArgumentsItem) + "() {\n");
            stringBuilder.append("        return " + downFirst(allArgumentsItem) + ";\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("\n");
        }
        //getter done
        stringBuilder.append("    public static class " + programName + "DataCreate\n");
        stringBuilder.append("    {\n");
        stringBuilder.append("        private " + programName + "Data " + getProgramNameLowerCaseFirst() + "Data;\n");
        for (String necessaryArgumentsItem : necessaryArguments)
            stringBuilder.append("        private boolean isSet" + upFirst(necessaryArgumentsItem) + ";\n");
        stringBuilder.append("        \n");
        stringBuilder.append("        private " + programName + "DataCreate(" + programName + "Data " + getProgramNameLowerCaseFirst() + "Data) {\n");
        stringBuilder.append("            this." + getProgramNameLowerCaseFirst() + "Data = " + getProgramNameLowerCaseFirst() + "Data;\n");
        stringBuilder.append("        }\n");
        stringBuilder.append("        \n");
        for (String necessaryArgumentsItem : necessaryArguments)
        {
            stringBuilder.append("        public " + programName + "DataCreate set" + upFirst(necessaryArgumentsItem) + "(String " + downFirst(necessaryArgumentsItem) + ") {\n");
            stringBuilder.append("            " + getProgramNameLowerCaseFirst() + "Data.set" + upFirst(necessaryArgumentsItem) + "(" + downFirst(necessaryArgumentsItem) + ");\n");
            stringBuilder.append("            isSet" + upFirst(necessaryArgumentsItem) + " = true;\n");
            stringBuilder.append("            return this;\n");
            stringBuilder.append("        }\n");
            stringBuilder.append("        \n");
        }
        for (String optionalArgumentsItem : optionalArguments)
        {
            stringBuilder.append("        public " + programName + "DataCreate set" + upFirst(optionalArgumentsItem) + "(String " + downFirst(optionalArgumentsItem) + ") {\n");
            stringBuilder.append("            " + getProgramNameLowerCaseFirst() + "Data.set" + upFirst(optionalArgumentsItem) + "(" + downFirst(optionalArgumentsItem) + ");\n");
            stringBuilder.append("            return this;\n");
            stringBuilder.append("        }\n");
            stringBuilder.append("        \n");
        }
        //chained functions done
        stringBuilder.append("        public " + programName + "Data create()\n");
        stringBuilder.append("        {\n");
        for (String necessaryArgumentsItem : necessaryArguments)
        {
            stringBuilder.append("            if (!isSet" + upFirst(necessaryArgumentsItem) + ")\n");
            stringBuilder.append("                throw new IllegalStateException(\"Parameter " + upFirst(necessaryArgumentsItem) + " not set\");\n");
        }
        stringBuilder.append("            return " + getProgramNameLowerCaseFirst() + "Data;\n");
        stringBuilder.append("        }\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("}");
        System.out.println(stringBuilder.toString());
    }
}
