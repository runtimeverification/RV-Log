package gen;

import com.runtimeverification.rvmonitor.core.ast.Property;
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
 * Created by xiaohe on 2/2/15.
 */
public class InvokerGenerator {
    private JCodeModel CodeModel;
    private String MonitorName;
    private List<String> ActualMonitorNames;
    private SignatureFormulaExtractor.EventsInfo eventsInfo;
    private String outputDir;

    public InvokerGenerator(String outputDir) {
        this.outputDir = outputDir;
    }

    public void generateCustomizedInvoker(String monitorClassPath, SignatureFormulaExtractor.EventsInfo eventsInfo) {
        List<String> specNames = new ArrayList<>();
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
        for (String eventName : eventList) {
            String eventAction = this.eventsInfo.getEventAndActionsMap().get(eventName);
            if (eventAction != null && !eventAction.replaceAll("\\W", "").equals("")) {
                //if the event action is not empty, then add the event to the monitored list
                JInvocation invocation = initMonitorMethodBody.invoke(setOfEvents, "add");
                invocation.arg(eventName);
            } else {
                for (int i = 0; i < allProperties.size(); i++) {
                    if (this.insideProp(allProperties.get(i), eventName)) {
                        //if the event occurs inside some property, then it should be monitored
                        JInvocation invocation = initMonitorMethodBody.invoke(setOfEvents, "add");
                        invocation.arg(eventName);
                        break;
                    }
                }
            }

        }

        initMonitorMethodBody._return(setOfEvents);
    }

    private boolean insideProp(Property p, String eventName) {
        return p.getSyntax().contains(eventName);
    }

    private void initFields(JDefinedClass logReaderClass) {
        JInvocation getPathFromStrInvok = CodeModel.ref(Paths.class).staticInvoke("get");
        getPathFromStrInvok.arg(JExpr.lit("./test-out/violation.txt"));
        logReaderClass.field(JMod.PUBLIC | JMod.STATIC, Path.class, "outputPath", getPathFromStrInvok);

        logReaderClass.field(JMod.PROTECTED | JMod.STATIC, int.class, "maxNumOfParams",
                JExpr.lit(SignatureFormulaExtractor.maxNumOfParams));

    }

    private void initTableInfoMethod(JDefinedClass logReaderClass, HashMap<String, int[]> tableSchema) {
        JType tableSchemaType = CodeModel.ref(HashMap.class).narrow(String.class, int[].class);
        JMethod initTableColMethod = logReaderClass.method(JMod.PRIVATE | JMod.STATIC, tableSchemaType, "initMethodInfo");

        JInvocation initMethodInvok = logReaderClass.staticInvoke(initTableColMethod);
        logReaderClass.field(JMod.PRIVATE | JMod.STATIC, tableSchemaType, "methodInfo", initMethodInvok);

        JBlock body = initTableColMethod.body();
        JExpression initTableExpr = JExpr._new(tableSchemaType);
        JVar tmpTable = body.decl(tableSchemaType, "methodInfoTable", initTableExpr);

        for (String eventName : tableSchema.keySet()) {
            int[] colTypes = tableSchema.get(eventName);
            JArray typeArrExp = JExpr.newArray(CodeModel.INT);
            for (int i = 0; i < colTypes.length; i++) {
                typeArrExp.add(JExpr.lit(colTypes[i]));
            }

            JInvocation putMethodInvok = body.invoke(tmpTable, "put");
            putMethodInvok.arg(eventName);
            putMethodInvok.arg(typeArrExp);
        }
        body._return(tmpTable);
    }

    private void initLogReaderClass(JDefinedClass definedClass) throws IOException {
        String entryPointCode = Main.getContentFromResource("entryPoint.code");
        if (Main.IsMonitoringLivenessProperty) {
            String insertedPrintedMethods = "";
            String tab = "\t\t";
            for (int i = 0; i < this.ActualMonitorNames.size(); i++) {
                insertedPrintedMethods += i == 0 ? '\t' : tab;
                insertedPrintedMethods += this.ActualMonitorNames.get(i) + ".printAllViolations();\n";
            }

            insertedPrintedMethods += "\t}";
            entryPointCode = entryPointCode.substring(0, entryPointCode.lastIndexOf('}')) + insertedPrintedMethods;
        }

        definedClass.direct(entryPointCode);

    }

    private void buildInvocationMethod(JDefinedClass definedClass, HashMap<String, int[]> tableSchema) {
        JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC, Void.TYPE, "invoke");
        String eventNameStr = "eventName";
        String methodArgsStr = "data";
        String objArrListStr = "violationsInCurLogEntry";

        JVar eventNameParam = method.param(String.class, eventNameStr);
        JVar tupleData = method.param(Object[].class, methodArgsStr);

        JType objArrListTy = CodeModel.ref(List.class).narrow(Object[].class);
        JVar violationsInCurLogEntry = Main.IsMonitoringLivenessProperty
                                    ? null : method.param(objArrListTy, objArrListStr);

        //gen the body of the method
        JBlock body = method.body();

        JFieldRef[] hasViolation = new JFieldRef[this.ActualMonitorNames.size()];

        if (!Main.IsMonitoringLivenessProperty) {
            for (int i = 0; i < this.ActualMonitorNames.size(); i++) {
                String RawMonitorNameI = this.ActualMonitorNames.get(i);
                hasViolation[i] = CodeModel.ref(RawMonitorNameI).staticRef("hasViolation");
                body.assign(hasViolation[i], JExpr.lit(false));
            }
        }

        JSwitch jSwitch = body._switch(eventNameParam);

        JClass monitorClass = CodeModel.ref(MonitorName);

        for (String eventName : tableSchema.keySet()) {
            JCase jCase = jSwitch._case(JExpr.lit(eventName));
            JInvocation eventMethodInvok = monitorClass.staticInvoke(eventName + "Event");
            jCase.body().add(eventMethodInvok);

            int[] cols = tableSchema.get(eventName);

            for (int i = 0; i < cols.length; i++) {
                JExpression index = JExpr.lit(i);
                switch (cols[i]) {
                    case RegHelper.INT_TYPE:
                        JType intTy = CodeModel.directClass("Integer");
                        JExpression intArg = JExpr.cast(intTy, tupleData.component(index));
                        eventMethodInvok.arg(intArg);
                        break;

                    case RegHelper.FLOAT_TYPE:
                        JType floatTy = CodeModel.directClass("Double");
                        JExpression floatArg = JExpr.cast(floatTy, tupleData.component(index));
                        eventMethodInvok.arg(floatArg);
                        break;

                    case RegHelper.STRING_TYPE:
                        JType stringTy = CodeModel.directClass("String");
                        JExpression stringArg = JExpr.cast(stringTy, tupleData.component(index));
                        eventMethodInvok.arg(stringArg);
                        break;
                }
            }

            jCase.body()._break();
        }

        if (!Main.IsMonitoringLivenessProperty) {
            for (int i = 0; i < this.ActualMonitorNames.size(); i++) {
                JConditional ifBlock = body._if(hasViolation[i]);
                JInvocation addViolationStmt = violationsInCurLogEntry.invoke("add");
                addViolationStmt.arg(tupleData);
                ifBlock._then().add(addViolationStmt);
            }
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
