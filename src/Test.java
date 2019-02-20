public class Test {
    public static void main(String[] args) {

        Field field = new Field();
        field.move("c3","d4");

        Bot bot = new Bot(field, 2);

        for (Branch branch : bot.getBranches()){
            System.out.println(branch);
        }
    }
}
