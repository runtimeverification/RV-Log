package sig;

import com.runtimeverification.rvmonitor.core.ast.Event;
import com.runtimeverification.rvmonitor.core.ast.MonitorFile;
import com.runtimeverification.rvmonitor.core.ast.Property;
import com.runtimeverification.rvmonitor.core.ast.Specification;
import com.runtimeverification.rvmonitor.core.parser.RVParser;
import com.runtimeverification.rvmonitor.java.rvj.JavaParserAdapter;
import reg.RegHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hx312 on 10/02/2015.
 */
public class SignatureFormulaExtractor {
    public static final SignatureFormulaExtractor SigExtractor = InitSigExtractor();
    public static int maxNumOfParams;
    /**
     * Maybe there are multiple specs, each of which contains some events and properties.
     * For the events that do not appear in the corresponding property section, they are valid events, but NOT monitored.
     * It is NOT allowed to have events with same name but different sig.
     * Events and Properties are indexed by spec name because we want to distinguish events in different specs.
     * An event with the same signature can appear in multiple specs, however, it is also possible that the same event
     * is monitored in one spec while NOT monitored in another.
     */
    private HashMap<String, List<String>> specEventsMap = new HashMap<>();
    private HashMap<String, List<Property>> specPropertiesMap = new HashMap<>();

    /**
     * The mappings between spec name and spec level params.
     */
    private HashMap<String, String> specLangParamsMap = new HashMap<>();
    /**
     * Map from the event's name to the action performed when encountering that event.
     */
    private HashMap<String, String> eventActionsMap = new HashMap<>();
    /**
     * The table schema which contains the info about event name and event args.
     */
    private HashMap<String, int[]> TableCol = new HashMap<>();

    /**
     * Make the constructor invisible to outside world.
     */
    private SignatureFormulaExtractor() {

    }

    private static SignatureFormulaExtractor InitSigExtractor() {
        return new SignatureFormulaExtractor();
    }

    private static int[] GetArgsTypeFromStr(String eventArgs) {
        String[] argsStrArr = eventArgs.substring(1, eventArgs.length() - 1).split(",");
        int[] argTypes = new int[argsStrArr.length];

        if (argTypes.length > maxNumOfParams) {
            maxNumOfParams = argTypes.length;
        }

        for (int i = 0; i < argsStrArr.length; i++) {
            String argI = argsStrArr[i].substring(0, argsStrArr[i].indexOf(' '));
            switch (argI) {
                case "Integer":
                    argTypes[i] = RegHelper.INT_TYPE;
                    break;

                case "int":
                    argTypes[i] = RegHelper.INT_TYPE;
                    break;

                case "Float":
                    argTypes[i] = RegHelper.FLOAT_TYPE;
                    break;

                case "float":
                    argTypes[i] = RegHelper.FLOAT_TYPE;
                    break;

                case "Double":
                    argTypes[i] = RegHelper.FLOAT_TYPE;
                    break;

                case "double":
                    argTypes[i] = RegHelper.FLOAT_TYPE;
                    break;

                case "String":
                    argTypes[i] = RegHelper.STRING_TYPE;
                    break;
            }
        }
        return argTypes;
    }

    public static void printMethodSig(HashMap<String, int[]> tableCol) {
        if (tableCol == null)
            return;

        for (String s : tableCol.keySet()) {
            System.out.print("Event " + s + "'s sig: (");
            int[] typesOfArgs = tableCol.get(s);
            for (int i = 0; i < typesOfArgs.length - 1; i++) {
                switch (typesOfArgs[i]) {
                    case RegHelper.INT_TYPE:
                        System.out.print("int, ");
                        break;

                    case RegHelper.FLOAT_TYPE:
                        System.out.print("double, ");
                        break;

                    case RegHelper.STRING_TYPE:
                        System.out.print("string, ");
                        break;
                }
            }

            if (typesOfArgs.length > 0) {
                switch (typesOfArgs[typesOfArgs[typesOfArgs.length - 1]]) {
                    case RegHelper.INT_TYPE:
                        System.out.print("int)");
                        break;

                    case RegHelper.FLOAT_TYPE:
                        System.out.print("double)");
                        break;

                    case RegHelper.STRING_TYPE:
                        System.out.print("string)");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        Path logPath = Paths.get("./test/pub-approve/rvm/Pub.rvm");
        EventsInfo eventsInfo = SigExtractor.extractEventsInfoFromSigFile(logPath);

        printMethodSig(eventsInfo.getTableCol());
        System.out.println("The max num of params in the spec is " + SigExtractor.maxNumOfParams);

        System.out.println("The specs are ");
        HashMap<String, List<Property>> skipList = eventsInfo.getSpecPropsMap();
        for (String s : skipList.keySet()) {
            System.out.println("Spec: " + s);
        }
    }

    private void reset() {
        this.specEventsMap.clear();
        this.specPropertiesMap.clear();
        this.specLangParamsMap.clear();
        this.eventActionsMap.clear();
        this.TableCol.clear();
    }

    public EventsInfo extractEventsInfoFromSigFile(Path file) throws IOException {
        this.reset();

        String fileContent = new String(Files.readAllBytes(file));
        List<String> listOfRawMonitoringCode = JavaParserAdapter.getRawMonitoringCode(fileContent);
        //remove the raw monitoring code from the rvm spec so that it can be handled by rv-parser
        for (int i = 0; i < listOfRawMonitoringCode.size(); i++) {
            String rawCode = listOfRawMonitoringCode.get(i);
            fileContent = fileContent.replace("raw:" + rawCode, "");
        }

        final Reader source = new StringReader(fileContent);
        final MonitorFile monitorFile = RVParser.parse(source);

        List<Specification> specifications = monitorFile.getSpecifications();

        for (int i = 0; i < specifications.size(); i++) {
            Specification spec = specifications.get(i);

//            System.out.println("Spec's name is " + spec.getName());
//            System.out.println("Param of the spec is " + spec.getLanguageParameters());

            String specName = spec.getName();


            assert (!this.specEventsMap.containsKey(specName)) : "The specification should not be duplicated!";
            this.specLangParamsMap.put(specName, spec.getLanguageParameters());

            List<Event> eventsInCurSpec = spec.getEvents();
            List<Property> propsInCurSpec = spec.getProperties();


            //the list contains all the valid events' names
            List<String> validEventNameList = new ArrayList<>();
            this.specEventsMap.put(specName, validEventNameList);
            this.specPropertiesMap.put(specName, propsInCurSpec);


            for (int j = 0; j < eventsInCurSpec.size(); j++) {
                Event event = eventsInCurSpec.get(j);
                String eventName = event.getName();
                String eventArgs = event.getDefinition();
                String eventAction = event.getAction();

//                System.out.println("Event " + eventName + ": ");
//                System.out.println("Args: " + eventArgs);

                assert !validEventNameList.contains(eventName) : "Does not support duplicate event";

                validEventNameList.add(eventName);

                //only a single sig is allowed for each event, multiple different sig for the same event will be caught
                String existingAction4CurEvent = this.eventActionsMap.get(eventName);

                if (existingAction4CurEvent == null) {
                    this.eventActionsMap.put(eventName, eventAction);
                } else if (!existingAction4CurEvent.replaceAll("\\W", "").equals(eventAction.replaceAll("\\W", ""))) {
                    assert false : eventName + " should have consistent event action in different places.";
                } else {
                    //no need to overwrite.
                }

                //going to analyze the types of the event args.
                int[] eventArgsTypes = GetArgsTypeFromStr(eventArgs);
                int[] existingEventArgsInfo = this.TableCol.get(eventName);

                if (existingEventArgsInfo == null) {
                    this.TableCol.put(eventName, eventArgsTypes);
                } else if (!Arrays.equals(eventArgsTypes, existingEventArgsInfo)) {
                    assert false : eventName + " should have consistent argument types!";
                } else {
                    //do nothing here
                }
            }
        }
        return new EventsInfo(this.TableCol, this.specPropertiesMap, this.eventActionsMap);
    }

    public static final class EventsInfo {
        private HashMap<String, int[]> tableCol;
        private HashMap<String, List<Property>> specPropsMap;
        private HashMap<String, String> eventAndActionsMap;

        public EventsInfo(HashMap<String, int[]> tableCol, HashMap<String, List<Property>> specPropsMap, HashMap<String, String> eventAndActionsMap) {
            this.tableCol = tableCol;
            this.specPropsMap = specPropsMap;
            this.eventAndActionsMap = eventAndActionsMap;
        }

        public HashMap<String, int[]> getTableCol() {
            return tableCol;
        }

        public HashMap<String, List<Property>> getSpecPropsMap() {
            return specPropsMap;
        }

        public HashMap<String, String> getEventAndActionsMap() {
            return eventAndActionsMap;
        }
    }
}
