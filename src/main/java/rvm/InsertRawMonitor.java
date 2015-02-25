package rvm;

/**
 * Created by hx312 on 27/01/2015.
 */
public class InsertRawMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
    public static boolean hasViolation;

    protected Object clone() {
        try {
            InsertRawMonitor ret = (InsertRawMonitor) super.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    @Override
    public final int getState() {
        return -1;
    }

    final boolean event_insert(String user, String db, String p, String data) {
        RVM_lastevent = 0;
        {
            if (db.equals("db2") && !user.equals("script1")) {
                hasViolation = true;
            }
        }
        return true;
    }

    final void reset() {
        RVM_lastevent = -1;
    }

    // RVMRef_user was suppressed to reduce memory overhead
    // RVMRef_db was suppressed to reduce memory overhead

    @Override
    protected final void terminateInternal(int idnum) {
        switch (idnum) {
            case 0:
                break;
            case 1:
                break;
        }
        switch (RVM_lastevent) {
            case -1:
                return;
            case 0:
                //insert
                return;
        }
        return;
    }

}
