package rvm;

import com.runtimeverification.rvmonitor.java.rt.RuntimeOption;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class Delete12RawMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<Delete12RawMonitor> {

    Delete12RawMonitor_Set() {
        this.size = 0;
        this.elements = new Delete12RawMonitor[4];
    }

    final void event_delete(String user, String db, String p, String data) {
        int numAlive = 0;
        for (int i = 0; i < this.size; i++) {
            Delete12RawMonitor monitor = this.elements[i];
            if (!monitor.isTerminated()) {
                elements[numAlive] = monitor;
                numAlive++;

                monitor.event_delete(user, db, p, data);
            }
        }
        for (int i = numAlive; i < this.size; i++) {
            this.elements[i] = null;
        }
        size = numAlive;
    }

    final void event_insert(String user, String db, String p, String data) {
        int numAlive = 0;
        for (int i = 0; i < this.size; i++) {
            Delete12RawMonitor monitor = this.elements[i];
            if (!monitor.isTerminated()) {
                elements[numAlive] = monitor;
                numAlive++;

                monitor.event_insert(user, db, p, data);
            }
        }
        for (int i = numAlive; i < this.size; i++) {
            this.elements[i] = null;
        }
        size = numAlive;
    }
}

class Delete12RawMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
    protected Object clone() {
        try {
            Delete12RawMonitor ret = (Delete12RawMonitor) super.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public static class DeleteRecord {
        public final long ts;
        public final String user;
        public final String deleteData;

        public DeleteRecord(long ts, String user, String deleteData) {
            this.ts = ts;
            this.user = user;
            this.deleteData = deleteData;
        }

        public boolean equals(DeleteRecord other) {
            if (other == null)
                return false;
            if (this.ts != other.ts)
                return false;

            return (this.user.equals(other.user) &&
                    this.deleteData.equals(other.deleteData));
        }

        public String print() {
            return "@" + ts + " delete (" + user + ", db1, " + deleteData + ")\n";
        }
    }

    public static boolean hasViolation;
    //the suspiciousRecords is the list of suspicious records.
    private static Set<DeleteRecord> suspiciousRecords = new HashSet<>();
    private static HashMap<Long, Set<String>> cond2HasHopeDataList = new HashMap<>();
    private static Set<String> db1InsertedData = new HashSet<>();
    //db2InsertedData is eq to cond2 is not satisfiable
    private static Set<String> db2InsertedData = new HashSet<>();


    @Override
    public final int getState() {
        return -1;
    }

    final boolean event_delete(String user, String db, String p, String data) {
        System.out.println("Find delete event " + user + ", db:" + db + ", data:" + data);
        RVM_lastevent = 0;
        {
            if (data.equals("unknown"))
                return true;

            if (db.equals("db1")) {
                System.out.println("Find delete event which delete data in db1");
                this.suspiciousRecords.add(new DeleteRecord(LogEntryExtractor.TimeStamp, user, data));

                if (db2InsertedData.contains(data))
                    return true;

                else {
                    if (db1InsertedData.contains(data)) {
                        Set<String> dataList = this.cond2HasHopeDataList.get(LogEntryExtractor.TimeStamp);
                        if (dataList == null) {
                            dataList = new HashSet<>();
                            this.cond2HasHopeDataList.put(LogEntryExtractor.TimeStamp, dataList);
                        }

                        dataList.add(data);
                    }
                }
            } else if (db.equals("db2")) {
                this.removeValidatedRecords(data);
            }

        }

        return true;
    }

    private void removeValidatedRecords(String data) {
        List<DeleteRecord> removedRecords = new ArrayList<>();
        for (DeleteRecord deleteRecord : suspiciousRecords) {
            if (deleteRecord.deleteData.equals(data)) {
                removedRecords.add(deleteRecord);
            }
        }
        suspiciousRecords.removeAll(removedRecords);
    }

    public static void printAllViolations() {
        System.out.println("There are " + suspiciousRecords.size() + " tuples in the suspicious list");
        for (DeleteRecord suspiciousRecord : suspiciousRecords) {
            if (db2InsertedData.contains(suspiciousRecord.deleteData)) {
                System.out.println("Violation: " + suspiciousRecord.print());
            } else {
                Set<String> hopefulDataList = cond2HasHopeDataList.get(suspiciousRecord.ts);
                if (hopefulDataList == null)
                    System.out.println("Violation: " + suspiciousRecord.print());

                else if (hopefulDataList.contains(suspiciousRecord.deleteData)) {
                    System.out.println("Satisfy the second condition of the disjunctive formula");
                } else {
                    System.out.println("Violation: " + suspiciousRecord.print());
                }
            }
        }
    }

    final boolean event_insert(String user, String db, String p, String data) {
        RVM_lastevent = 1;
        {
            if (data.equals("unknown"))
                return false;

            if (db.equals("db1")) {
                this.db1InsertedData.add(data);
            } else if (db.equals("db2")) {
                this.db2InsertedData.add(data);
            }

        }
        return true;
    }

    final void reset() {
        RVM_lastevent = -1;
    }

    @Override
    protected final void terminateInternal(int idnum) {
        switch (idnum) {
        }
        switch (RVM_lastevent) {
            case -1:
                return;
            case 0:
                //delete
                return;
            case 1:
                //insert
                return;
        }
        return;
    }

}

public final class Delete12RuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
    private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager Delete12MapManager;

    static {
        Delete12MapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
        Delete12MapManager.start();
    }

    // Declarations for the Lock
    static final ReentrantLock Delete12_RVMLock = new ReentrantLock();
    static final Condition Delete12_RVMLock_cond = Delete12_RVMLock.newCondition();

    private static boolean Delete12_activated = false;

    // Declarations for Indexing Trees
    private static final Delete12RawMonitor Delete12__Map = new Delete12RawMonitor();

    public static int cleanUp() {
        int collected = 0;
        // indexing trees
        return collected;
    }

    // Removing terminated monitors from partitioned sets
    static {
        TerminatedMonitorCleaner.start();
    }

    // Setting the behavior of the runtime library according to the compile-time option
    static {
        RuntimeOption.enableFineGrainedLock(false);
    }

    public static final void deleteEvent(String user, String db, String p, String data) {
        System.out.println("Find delete event " + user + ", db:" + db + ", data:" + data);

        Delete12_activated = true;
        while (!Delete12_RVMLock.tryLock()) {
            Thread.yield();
        }

        Delete12RawMonitor matchedEntry = null;
        {
            // FindOrCreateEntry
            matchedEntry = Delete12__Map;
        }
        // D(X) main:1
        if ((matchedEntry == null)) {
            // D(X) main:4
            Delete12RawMonitor created = new Delete12RawMonitor();
            matchedEntry = created;
        }
        // D(X) main:8--9
        System.out.println("Going to exec raw monitor's event method");
        matchedEntry.event_delete(user, db, p, data);

        Delete12_RVMLock.unlock();
    }

    public static final void insertEvent(String user, String db, String p, String data) {
        Delete12_activated = true;
        while (!Delete12_RVMLock.tryLock()) {
            Thread.yield();
        }

        Delete12RawMonitor matchedEntry = null;
        {
            // FindOrCreateEntry
            matchedEntry = Delete12__Map;
        }
        // D(X) main:1
        if ((matchedEntry == null)) {
            // D(X) main:4
            Delete12RawMonitor created = new Delete12RawMonitor();
            matchedEntry = created;
        }
        // D(X) main:8--9
        matchedEntry.event_insert(user, db, p, data);

        Delete12_RVMLock.unlock();
    }

}
