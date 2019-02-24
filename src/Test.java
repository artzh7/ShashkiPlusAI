import java.io.FileNotFoundException;

public class Test {
    public static void main(String[] args) throws FileNotFoundException {

        Field field = new Field();
        String path = "src/input/field2.txt";
        field.setCells(path);
        field.setFTMB(field.cell("d4"));

        field.move("d4","e3");
//
//        Bot bot = new Bot(field, 2);
//
//        for (Branch branch : bot.getBranches()){
//            System.out.println(branch);
//        }
//
//        bot.randomMove(field);
    }
}
