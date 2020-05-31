package dts.core;

import lombok.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Environment.getInstance().runSimulation(3);
    }



    static void tmpTest() {
        LinkedHashSet<Tmp> entryLog = new LinkedHashSet<>();

        Tmp tmp3 = Tmp.builder().tmpInt(3).tmpStr("HEY HEY 3").build();

        entryLog.add(Tmp.builder().tmpInt(1).tmpStr("HEY HEY 1").build());
        entryLog.add(Tmp.builder().tmpInt(2).tmpStr("HEY HEY 2").build());
        entryLog.add(tmp3);
        System.out.println("TEST 1");
        entryLog.forEach(System.out::println);

        entryLog.removeIf(tmp -> tmp.tmpInt == 2);
        System.out.println("TEST 2");
        entryLog.forEach(System.out::println);


        tmp3.tmpStr = "HEY HEY HEY 4";
        System.out.println("TEST 3");
        entryLog.forEach(System.out::println);


        entryLog.removeIf(tmp -> tmp.tmpInt == 3);
        System.out.println("TEST 4");
        entryLog.forEach(System.out::println);

    }

    static void tmpTest2() {
        List<List<String>> tmp = null;

//        tmp3(tmp);
    }

    static void tmp3(List<List> tmp) {

    }
}

@ToString
@Builder
@Data
class Tmp {
    int tmpInt;
    String tmpStr;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tmp) {
            return Objects.equals(tmpInt, ((Tmp) obj).tmpInt);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tmpInt);
    }

}
