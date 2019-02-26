import kotlin.Pair;
import kotlin.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class Bot {

    private LinkedList<Branch> branches; // различные ветки ходов
    private Turn botColor;

    Bot(Field startField, int depth) {
        botColor = startField.getTurn();
        branches = buildFirstMoves(startField);
        completeMoves();
        for (int i = 0; i < depth; i++){
            buildAnotherMoves();
            completeMoves();
        }
        setPointsOnBranches(startField);
    }

    private LinkedList<Branch> buildFirstMoves(Field startField){
        LinkedList<Branch> branches1 = new LinkedList<>();
        boolean finish = false;

        for (int row1 = 0; row1 < 8; row1++) {
            for (int column1 = 0; column1 < 8; column1++) {
                for (int row2 = 0; row2 < 8; row2++) {
                    for (int column2 = 0; column2 < 8; column2++) {

                        if ((row1+column1)%2 != 0 || (row2+column2)%2 != 0)
                            continue;

                        Field f = startField.copy();
                        Pair<String, Integer> info = f.move(f.cell(column1, row1), f.cell(column2, row2));
                        if (info.getFirst() != null) {
                            if (info.getFirst().equals(Field.blackWin) || info.getFirst().equals(Field.whiteWin)) {
                                finish = true;
                            } else continue;
                        }

                        ArrayList<Pair<String, String>> list = new ArrayList<>();
                        Pair<String, String> move = new Pair<>(f.cell(column1, row1).pos(), f.cell(column2, row2).pos());
                        list.add(move);
                        Branch branch = new Branch(list, f);

                        if (!finish) branches1.add(branch);
                        else {
                            branches1 = new LinkedList<>();
                            branch.setFinished();
                            branches1.add(branch);
                            break;
                        }
                    }
                    if (finish) break;
                }
                if (finish) break;
            }
            if (finish) break;
        }

        return branches1;
    }

    private void completeMoves(){
        for (;;){
            int changes = 0;
            int i = 0;
            for (; i < branches.size(); i++) {

                ArrayList<Triple<String, String, Field>> leaves = new ArrayList<>();
                String cell1 = branches.get(i).getListOfMoves().get(branches.get(i).getListOfMoves().size()-1).getSecond();

                for (int row2 = 0; row2 < 8; row2++) {
                    for (int column2 = 0; column2 < 8; column2++) {

                        Field f = branches.get(i).getLastField().copy();
                        Pair<String, Integer> info = f.move(cell1, f.cell(column2, row2).pos());
                        if (info.getFirst() != null) {
                            continue;
                        }
                        Triple<String, String, Field> leaf = new Triple<>(cell1, f.cell(column2, row2).pos(), f.copy());
                        leaves.add(leaf);
                    }
                }

                int k = 0;
                for (Triple<String,String,Field> leaf : leaves){
                    Branch newBranch = branches.get(i+k).copy();
                    newBranch.getListOfMoves().add(new Pair<>(leaf.getFirst(), leaf.getSecond()));
                    newBranch.setLastField(leaf.getThird());
                    branches.add(i, newBranch);
                    changes++;
                    k++;
                }
                if (leaves.size() != 0) {
                    branches.remove(i + leaves.size());
                    i += leaves.size() - 1;
                }
            }
            if (changes == 0) {
                break;
            }
        }
    }

    private void buildAnotherMoves(){
        int i = 0;
        for (; i < branches.size(); i++){

            if (branches.get(i).isFinished()) continue;
            ArrayList<Triple<String, String, Field>> leaves = new ArrayList<>();

            for (int row1 = 0; row1 < 8; row1++) {
                for (int column1 = 0; column1 < 8; column1++) {
                    for (int row2 = 0; row2 < 8; row2++) {
                        for (int column2 = 0; column2 < 8; column2++) {

                            Field f = branches.get(i).getLastField().copy();
                            Pair<String, Integer> info = f.move(f.cell(column1, row1), f.cell(column2, row2));
                            if (info.getFirst() != null) {
                                if (!info.getFirst().equals(Field.blackWin) && !info.getFirst().equals(Field.whiteWin)) {
                                    continue;
                                }
                            }

                            Triple<String, String, Field> leaf = new Triple<>(f.cell(column1, row1).pos(), f.cell(column2, row2).pos(), f.copy());
                            leaves.add(leaf);
                        }
                    }
                }
            }

            int k = 0;
            for (Triple<String,String,Field> leaf : leaves){
                Branch newBranch = branches.get(i+k).copy();
                newBranch.getListOfMoves().add(new Pair<>(leaf.getFirst(), leaf.getSecond()));
                newBranch.setLastField(leaf.getThird());
                branches.add(i, newBranch);
                k++;
            }
            if (leaves.size() != 0) {
                branches.remove(i + leaves.size());
                i += leaves.size() - 1;
            }
        }
    }

    void setPointsOnBranches(Field startField){
        for (Branch branch : branches) {
            int points = 0;
            Field temp = startField.copy();
            for (Pair<String, String> currentMove : branch.getListOfMoves()){
                Turn currentTurn = temp.copy().getTurn();
                Pair<String, Integer> info = temp.move(currentMove.getFirst(), currentMove.getSecond());
                if (currentTurn == botColor) {
                    points += info.getSecond();
                } else {
                    points -= info.getSecond();
                }
            }
            branch.setPoints(points);
        }
    }

    String smartMove(Field field) {
        Pair<String, String> smartMove = resultOfEvaluation(branches);
        if (smartMove != null) {
            System.out.println(smartMove.getFirst() + " -> " + smartMove.getSecond());
            return field.move(smartMove.getFirst(), smartMove.getSecond()).getFirst();
        }
        else {
            switch (field.getPlayer()) {
                case WHITE:
                    return Field.whiteWin;
                case BLACK:
                    return Field.blackWin;
            }
        }
        return "";
    }

    private Pair<String, String> resultOfEvaluation(LinkedList<Branch> branches) {
        if (branches.isEmpty()) return null;

        ArrayList<Pair<String, String>> uniqueFirstMoves = new ArrayList<>();
        for (Branch branch : branches) {
            if (!uniqueFirstMoves.contains(branch.getListOfMoves().get(0))){
                uniqueFirstMoves.add(branch.getListOfMoves().get(0));
            }
        }
        if (uniqueFirstMoves.size() == 1) return uniqueFirstMoves.get(0);

        ArrayList<Double> arrayOfPoints = new ArrayList<>();
        int i = 0;
        double sum = 0;
        int amount = 0;
        for (Branch branch : branches) {
            if (!branch.getListOfMoves().get(0).equals(uniqueFirstMoves.get(i))) {
                assert amount != 0;
                arrayOfPoints.add(i, sum / amount);
                i++;
                sum = 0;
                amount = 0;
            }
            sum += branch.getPoints();
            amount++;
        }

        double max = arrayOfPoints.get(0);
        for (int k = 1; k < arrayOfPoints.size(); k++){
            if (arrayOfPoints.get(k) > max) max = arrayOfPoints.get(k);
        }
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int k = 0; k < arrayOfPoints.size(); k++){
            if (arrayOfPoints.get(k) == max)
                indexes.add(k);
        }
        i = (int) (Math.random() * indexes.size());
        return uniqueFirstMoves.get(indexes.get(i));
    }

    LinkedList<Branch> getBranches() {
        return branches;
    }
}

class Branch{

    private ArrayList<Pair<String, String>> listOfMoves;
    private Field lastField;
    private int points = 0;
    private boolean finished = false;

    Branch(ArrayList<Pair<String, String>> listOfMoves, Field field){
        this.listOfMoves = listOfMoves;
        lastField = field;
        points = 0;
    }

    ArrayList<Pair<String, String>> getListOfMoves() {
        return listOfMoves;
    }

    void setLastField(Field lastField) {
        this.lastField = lastField;
    }
    Field getLastField() {
        return lastField;
    }

    void setPoints(int points) {
        this.points = points;
    }
    public int getPoints() {
        return points;
    }

    void setFinished(){
        finished = true;
    }
    boolean isFinished(){
        return finished;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for (Pair<String, String> move : listOfMoves){
            str.append(move.getFirst()).append(" -> ").append(move.getSecond()).append("; ");
        }
        str.append("points: ").append(points);
        return str.toString();
    }

    Branch copy(){
        ArrayList<Pair<String, String>> listOfMoves = new ArrayList<>();
        for (Pair<String, String> move : this.listOfMoves){
            Pair<String, String> copyMove = new Pair<>(move.getFirst(), move.getSecond());
            listOfMoves.add(copyMove);
        }
        return new Branch(listOfMoves, lastField.copy());
    }
}
