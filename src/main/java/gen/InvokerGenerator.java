package gen;

import javamop.parser.rvm.core.ast.Property;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;
import fsl.uiuc.Main;
import reg.RegHelper;
import sig.SignatureFormulaExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Generate the code for invoking monitor library's methods when events are read from log files.
 * Created by xiaohe on 2/2/15.
 */
public class InvokerGenerator {
    private static final String END_EVENT = "__END";
    private static final String OTHER_EVENT = "other";

    private JCodeModel CodeModel;
    private String MonitorName;
    private List<String> specNames = new ArrayList<>();
    ;
    private List<String> ActualMonitorNames;
    private SignatureFormulaExtractor.EventsInfo eventsInfo;
    private String outputDir;

    public InvokerGenerator(String outputDir) {
        this.outputDir = outputDir;
    }

    public void generateCustomizedInvoker(String monitorClassPath, SignatureFormulaExtractor.EventsInfo eventsInfo) {
        specNames.addAll(eventsInfo.getSpecPropsMap().keySet());
        HashMap<String, int[]> tableSchema = eventsInfo.getTableCol();
        this.eventsInfo = eventsInfo;

        CodeModel = new JCodeModel();
        this.MonitorName = monitorClassPath;

        assert MonitorName != null;

        this.ActualMonitorNames = new ArrayList<>();
        for (int i = 0; i < specNames.size(); i++) {
            String curSpecName = specNames.get(i);
            List<Property> propsInCurSpec = eventsInfo.getSpecPropsMap().get(curSpecName);
            if (propsInCurSpec == null || propsInCurSpec.size() == 0)
                this.ActualMonitorNames.add(curSpecName + "RawMonitor");
            else
                this.ActualMonitorNames.add(curSpecName + "Monitor");
        }

        try {
            JDefinedClass logReaderClass = CodeModel._class("LogReader");
            initMonitoredEventsList(logReaderClass);
            genIsMonitoredMethod(logReaderClass);

            initFields(logReaderClass);

            initLogReaderClass(logReaderClass);
            initTableInfoMethod(logReaderClass, tableSchema);

            JDefinedClass definedClass = logReaderClass._class(JMod.NONE | JMod.STATIC, "MonitorMethodsInvoker");
            SingleStreamCodeWriter sscw = new SingleStreamCodeWriter(System.out);

            buildInvocationMethod(definedClass, tableSchema);

            if (Main.noticeOtherEvents)
                buildInvokeOtherMethod(definedClass);

            File outputDir = new File(this.outputDir);
            if (!outputDir.exists())
                outputDir.mkdirs();


//            CodeModel.build(sscw);
            CodeModel.build(outputDir);

        } catch (JClassAlreadyExistsException e) {
            // ...
            System.err.println("JClassAlreadyExisting-Exception: " + e.getMessage());
        } catch (IOException e) {
            // ...
            System.err.println("IO-Exception: " + e.getMessage());
        }
    }

    private void genIsMonitoredMethod(JDefinedClass logReaderClass) {
        JMethod isMonitoredEventMethod = logReaderClass.method(JMod.PUBLIC | JMod.STATIC, boolean.class, "isMonitoredEvent");
        JVar eventName = isMonitoredEventMethod.param(String.class, "event");
        JInvocation invok = logReaderClass.staticRef("monitoredEventSet").invoke("contains");
        invok.arg(eventName);

        isMonitoredEventMethod.body()._return(invok);
    }


    /**
     * A special case is "__END" event, which will be only handled once at the end of the trace.
     *
     * @param logReaderClass
     */
    private void initMonitoredEventsList(JDefinedClass logReaderClass) {
        String methodName = "initMonitoredEventsSet";
        int accessModifier = JMod.PRIVATE | JMod.STATIC;
        JType fieldTy = CodeModel.ref(HashSet.class).narrow(String.class);
        String fieldName = "monitoredEventSet";

        logReaderClass.field(accessModifier, fieldTy,
                fieldName, logReaderClass.staticInvoke(methodName));

        JBlock initMonitorMethodBody = logReaderClass.method(accessModifier, fieldTy, methodName).body();
        JVar setOfEvents = initMonitorMethodBody.decl(fieldTy, "setOfEvents", JExpr._new(fieldTy));

        List<Property> allProperties = new ArrayList<>();
        for (List<Property> c : this.eventsInfo.getSpecPropsMap().values()) {
            allProperties.addAll(c);
        }

        Set<String> eventList = this.eventsInfo.getTableCol().keySet();

        eventList.stream()
                .filter(str -> !str.equals(END_EVENT))
                .filter(str -> !str.equals(OTHER_EVENT))
                .forEach(eventName -> {
                    JInvocation invocation = initMonitorMethodBody.invoke(setOfEvents, "add");
                    invocation.arg(eventName);
                });

        initMonitorMethodBody._return(setOfEvents);
    }

    private void initFields(JDefinedClass logReaderClass) {

        logReaderClass.field(JMod.PROTECTED | JMod.STATIC, int.class, "maxNumOfParams",
                JExpr.lit(SignatureFormulaExtractor.maxNumOfParams));

    }

    private void initTableInfoMethod(JDefinedClass logReaderClass, HashMap<String, int[]> tableSchema) {
        JType tableSchemaType = CodeModel.ref(HashMap.class).narrow(String.class, Integer.class);
        JMethod initTableColMethod = logReaderClass.method(JMod.PRIVATE | JMod.STATIC, tableSchemaType, "initMethodInfo");

        JInvocation initMethodInvok = logReaderClass.staticInvoke(initTableColMethod);
        logReaderClass.field(JMod.PRIVATE | JMod.STATIC, tableSchemaType, "methodInfo", initMethodInvok);

        JBlock body = initTableColMethod.body();
        JExpression initTableExpr = JExpr._new(tableSchemaType);
        JVar tmpTable = body.decl(tableSchemaType, "methodInfoTable", initTableExpr);

        for (String eventName : tableSchema.keySet()) {
            if (eventName.equals(END_EVENT) || eventName.equals(OTHER_EVENT))
                continue;

            JExpression numOfArgsExpr = JExpr.lit(tableSchema.get(eventName).length);

            JInvocation putMethodInvok = body.invoke(tmpTable, "put");
            putMethodInvok.arg(eventName);
            putMethodInvok.arg(numOfArgsExpr);
        }
        body._return(tmpTable);
    }

    private void initLogReaderClass(JDefinedClass definedClass) throws IOException {
        String entryPointCode = Main.getContentFromResource("entryPoint.code");

        if (Main.isMonpolyLog()) {
            entryPointCode = entryPointCode.replaceAll("LogEntryExtractor_CSV",
                    "LogEntryExtractor");
        }

        StringBuilder sb = new StringBuilder();
        if (Main.TimeProp) {
            this.specNames.forEach(specName -> {
                String tr = specName + "RuntimeMonitor.timeReminder";
                sb.append(tr + ".report();\n");
            });
        }

        sb.append("if (com.runtimeverification.rvmonitor.java.rt.RVMStat.statisticsManager != " +
                "null \n&& ! com.runtimeverification.rvmonitor.java.rt.RVMStat.statisticsManager" +
                ".hasAlreadyReported()) {\n");
        sb.append("com.runtimeverification.rvmonitor.java.rt.RVMStat.statisticsManager" +
                ".printStats();\n}\n");

        sb.append("System.out.println(\"Property Satisfied\");\n");
        entryPointCode = entryPointCode.substring(0, entryPointCode.lastIndexOf('}'))
                + sb.toString() + "\n}";


        JMethod endMethod = definedClass.method(JMod.PRIVATE | JMod.STATIC, Void.TYPE, "endEvent");
        JBlock endMethodBody = endMethod.body();
        JClass monitorClass = CodeModel.ref(MonitorName);
        JInvocation invok = monitorClass.staticInvoke("actionsAtTheEnd");
        endMethodBody.add(invok);

        definedClass.direct(entryPointCode);
    }

    private void buildInvokeOtherMethod(JDefinedClass definedClass) {
        JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC, Void.TYPE, "invokeOther");
        JBlock body = method.body();
        JClass monitorClass = CodeModel.ref(MonitorName);
        JInvocation eventMethodInvok = monitorClass.staticInvoke(OTHER_EVENT + "Event");
        body.add(eventMethodInvok);
    }

    private void buildInvocationMethod(JDefinedClass definedClass, HashMap<String, int[]> tableSchema) {
        JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC, Void.TYPE, "invoke");
        String eventNameStr = "eventName";
        String methodArgsStr = "data";

        JVar eventNameParam = method.param(String.class, eventNameStr);
        JVar tupleData = method.param(String[].class, methodArgsStr);

        //gen the body of the method
        JBlock body = method.body();
        JSwitch jSwitch = body._switch(eventNameParam);

        JClass monitorClass = CodeModel.ref(MonitorName);

        for (String eventName : tableSchema.keySet()) {
            if (eventName.equals(END_EVENT) || eventName.equals(OTHER_EVENT))
                continue;

            JCase jCase = jSwitch._case(JExpr.lit(eventName));
            JInvocation eventMethodInvok = monitorClass.staticInvoke(eventName + "Event");
            jCase.body().add(eventMethodInvok);

            int[] cols = tableSchema.get(eventName);

            for (int i = 0; i < cols.length; i++) {
                JExpression index = JExpr.lit(i);
                switch (cols[i]) {
                    case RegHelper.INT_TYPE:
                        JClass integerCls = CodeModel.directClass("Integer");
                        JInvocation parseIntMethInvok = integerCls.staticInvoke("parseInt");
                        parseIntMethInvok.arg(tupleData.component(index));

                        eventMethodInvok.arg(parseIntMethInvok);
                        break;

                    case RegHelper.LONG_TYPE:
                        JClass longCls = CodeModel.directClass("Long");
                        JInvocation parseLongMethInvok = longCls.staticInvoke("parseLong");
                        parseLongMethInvok.arg(tupleData.component(index));

                        eventMethodInvok.arg(parseLongMethInvok);
                        break;

                    case RegHelper.FLOAT_TYPE:
                        JClass doubleCls = CodeModel.directClass("Double");
                        JInvocation parseDoubleMethInvok = doubleCls.staticInvoke("parseDouble");
                        parseDoubleMethInvok.arg(tupleData.component(index));

                        eventMethodInvok.arg(parseDoubleMethInvok);
                        break;

                    case RegHelper.STRING_TYPE:
                        JExpression stringArg = tupleData.component(index);
                        eventMethodInvok.arg(stringArg);
                        break;

                    case RegHelper.Bool_Type:
                        JClass booleanCls = CodeModel.directClass("Boolean");
                        JInvocation parseBoolMethInvok = booleanCls.staticInvoke("parseBoolean");
                        parseBoolMethInvok.arg(tupleData.component(index));

                        eventMethodInvok.arg(parseBoolMethInvok);
                        break;
                }
            }

            jCase.body()._break();
        }
    }


    public static void main(String[] args) throws IOException {
        String monitorName;
        monitorName = "rvm.InsertRuntimeMonitor";
//        monitorName = "rvm.PubRuntimeMonitor";
        InvokerGenerator ig = new InvokerGenerator("./target/generated-sources/CodeModel");
        Path path2SigFile = Paths.get("./test/pub-approve/rvm/Pub.rvm");
        SignatureFormulaExtractor.EventsInfo eventsInfo = SignatureFormulaExtractor.SigExtractor.
                extractEventsInfoFromSigFile(path2SigFile);

        ArrayList<String> specList = new ArrayList<>();
        specList.add("Insert");

        SignatureFormulaExtractor.printMethodSig(eventsInfo.getTableCol());

        ig.generateCustomizedInvoker(monitorName, eventsInfo);
    }
}
