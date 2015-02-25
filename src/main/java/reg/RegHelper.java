package reg;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by xiaohe on 14-11-22.
 * For all the reg ex, left should have optional spaces.
 */
public class RegHelper {
    public static final int INT_TYPE = 0;
    public static final int FLOAT_TYPE = 1;
    public static final int STRING_TYPE = 4;

    // '_' | '[' | ']' | '/' | ':' | '-' | '.' | '!')* | '"'[^'"']*'"'

    public static final String IntReg = "(\\d+)";
    public static final String FloatReg = "(\\d*.\\d+)";
    public static final String DoubleQuotesRegEx = "\"[^\"]+\"";
    public static final String StringRegEx = "(" + "[\\w\\[\\]\\/\\:\\-\\.\\!]+|" + DoubleQuotesRegEx + ")";
    public static final String TimeStamp = "(@\\d+)";


    public final HashMap<String, Pattern> eventTupleRegEx;


    public RegHelper(HashMap<String, Integer[]> tableCol) {
        this.eventTupleRegEx = this.init(tableCol);
    }

    public static void main(String[] args) {

        String str1 = "abc";
        System.out.println("abc is str: " + str1.matches(StringRegEx));

        String str12 = "0_[:.-!/abc";
        System.out.println("str12 is str: " + str12.matches(StringRegEx));

        String str2 = "\"ab0_:c\"";
        System.out.println("str2 is str: " + str2.matches(StringRegEx));

        String ts = "@123";
        System.out.println("@123 is ts: " + ts.matches(TimeStamp));

        String lineSeparator = System.getProperty("line.separator");
        System.out.println("There are " + lineSeparator.length() + " chars in the line separator");
        System.out.println("First char in line separator is `" + (int) lineSeparator.charAt(0) + "`");
        System.out.println("Second line separator is `" + (int) lineSeparator.charAt(1) + "`");
        System.out.println("The \\n 's ascii code is " + ((int) '\n'));
        System.out.println("The \\r 's ascii code is " + ((int) '\r'));
    }

    private HashMap<String, Pattern> init(HashMap<String, Integer[]> tableCol) {
        HashMap<String, Pattern> eventNameAndTupleRegexMap = new HashMap<>();

        for (String eventName : tableCol.keySet()) {
            Integer[] types = tableCol.get(eventName);

            String regex = "";

            if (types.length == 0) {
                regex = "\\s*\\(\\s*\\s)";
            } else {
                switch (types[0]) {
                    case INT_TYPE:
                        regex = IntReg;
                        break;

                    case FLOAT_TYPE:
                        regex = FloatReg;
                        break;

                    case STRING_TYPE:
                        regex = StringRegEx;
                        break;
                }

                regex = "\\s*" + regex; //the first field

                for (int i = 1; i < types.length; i++) {
                    String tmp = "";

                    switch (types[i]) {
                        case INT_TYPE:
                            tmp = IntReg;
                            break;

                        case FLOAT_TYPE:
                            tmp = FloatReg;
                            break;

                        case STRING_TYPE:
                            tmp = StringRegEx;
                            break;
                    }

                    regex += "\\s*,\\s*" + tmp;
                }
                regex = "\\s*\\(" + regex + "\\s*\\)";
            }

            //gen the event name and corresponding tuple's reg ex.
            eventNameAndTupleRegexMap.put(eventName, Pattern.compile(regex));

        }

        return eventNameAndTupleRegexMap;
    }

    public void showEventTupleRegEx() {
        for (String eventName : this.eventTupleRegEx.keySet()) {
            System.out.println(eventName + "'s tuple's reg ex is: " + this.eventTupleRegEx.get(eventName));
        }
    }
}
