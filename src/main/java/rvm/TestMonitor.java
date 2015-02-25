package rvm;

/**
 * Created by xiaohe on 1/27/15.
 */
public class TestMonitor {
    public static void main(String[] args) {
        String s = "test";

        for (int i = 0; i < 12; i++) {
            System.out.println("no." + i);
            String test1 = s + i;
            String test2 = s + i;
            System.out.println("Test1 is `" + test1 + "`, test 2 is `" + test2 + "`");
            System.out.println("hash1 is " + test1.hashCode());
            System.out.println("hash2 is " + test2.hashCode());


//            PubRuntimeMonitor.approveEvent(test1);
//            PubRuntimeMonitor.publishEvent(test2);
        }


    }
}
