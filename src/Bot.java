import kotlin.Pair;
import kotlin.Triple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

                        ArrayList<Pair<Cell, Cell>> list = new ArrayList<>();
                        Pair<Cell, Cell> move = new Pair<>(f.cell(column1, row1), f.cell(column2, row2));
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

    void completeMoves(){
        for (;;){
            int changes = 0;
            int i = 0;
            for (; i < branches.size(); i++) {

                ArrayList<Triple<Cell, Cell, Field>> leaves = new ArrayList<>();
                Cell cell1 = branches.get(i).getListOfMoves().get(branches.get(i).getListOfMoves().size()-1).getSecond();

                for (int row2 = 0; row2 < 8; row2++) {
                    for (int column2 = 0; column2 < 8; column2++) {

                        Field f = branches.get(i).getLastField().copy();
                        Pair<String, Integer> info = f.move(f.cell(cell1.column, cell1.row), f.cell(column2, row2));
                        if (info.getFirst() != null) {
                            continue;
                        }
                        Triple<Cell, Cell, Field> leaf = new Triple<>(cell1, f.cell(column2, row2), f.copy());
                        leaves.add(leaf);
                    }
                }

                int k = 0;
                for (Triple<Cell,Cell,Field> leaf : leaves){
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

    void buildAnotherMoves(){
        int i = 0;
        for (; i < branches.size(); i++){

            if (branches.get(i).isFinished()) continue;
            ArrayList<Triple<Cell, Cell, Field>> leaves = new ArrayList<>();

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
                                // else {                                                                       !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            }

                            Triple<Cell, Cell, Field> leaf = new Triple<>(f.cell(column1, row1), f.cell(column2, row2), f.copy());
                            leaves.add(leaf);
                        }
                    }
                }
            }

            int k = 0;
            for (Triple<Cell,Cell,Field> leaf : leaves){
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

    private void setPointsOnBranches(Field startField){
        for (Branch branch : branches) {
            int points = 0;
            Field temp = startField.copy();
            for (Pair<Cell, Cell> currentMove : branch.getListOfMoves()){
                Turn currentTurn = temp.copy().getTurn();
                Pair<String, Integer> info = temp.move(currentMove.getFirst(), currentMove.getSecond());
                if (currentTurn == botColor) {
                    points += info.getSecond();
                } else {
                    points -= info.getSecond();
                }
            }
        }
    }

    String randomMove1(Field field) {
        List<Pair<Cell, Cell>> moves = new ArrayList<>();
        boolean finish = false;

        for (int row1 = 0; row1 < 8; row1++){
            for (int column1 = 0; column1 < 8; column1++){
                for (int row2 = 0; row2 < 8; row2++){
                    for (int column2 = 0; column2 < 8; column2++){

                        if ((row1+column1)%2 != 0 || (row2+column2)%2 != 0)
                            continue;

                        Field temp = field.copy();
                        String message = temp.move(temp.cell(column1, row1), temp.cell(column2, row2)).getFirst();
                        if (message != null) {
                            if (message.equals(Field.blackWin) || message.equals(Field.whiteWin)) {
                                moves.add(new Pair<>(field.cell(column1, row1), field.cell(column2, row2)));
                                finish = true;
                                break;
                            } else {
                                continue;
                            }
                        }
                        moves.add(new Pair<>(field.cell(column1, row1), field.cell(column2, row2)));
                    }
                    if (finish) break;
                }
                if (finish) break;
            }
            if (finish) break;
        }

        if (!moves.isEmpty()) {
            int i = (int) (Math.random() * moves.size());
            Pair<Cell, Cell> lastMove = moves.get(i);
            System.out.println(lastMove.getFirst().pos() + " -> " + lastMove.getSecond().pos());
            return field.move(lastMove.getFirst().pos(), lastMove.getSecond().pos()).getFirst();
        } else {
            switch (field.getPlayer()){
                case WHITE:
                    return Field.whiteWin;
                case BLACK:
                    return Field.blackWin;
            }
        }

        return "";
    }

    String randomMove(Field field) {
        if (!branches.isEmpty()) {
            int i = (int) (Math.random() * branches.size());
            Pair<Cell, Cell> lastMove = branches.get(i).getListOfMoves().get(0);
            System.out.println(lastMove.getFirst().pos() + " -> " + lastMove.getSecond().pos());
            return field.move(lastMove.getFirst().pos(), lastMove.getSecond().pos()).getFirst();
        } else {
            switch (field.getPlayer()) {
                case WHITE:
                    return Field.whiteWin;
                case BLACK:
                    return Field.blackWin;
            }
        }

        return "";
    }

    LinkedList<Branch> getBranches() {
        return branches;
    }
}

class Branch{

    private ArrayList<Pair<Cell, Cell>> listOfMoves;
    private Field lastField;
    private int points = 0;
    private boolean finished = false;

    Branch(ArrayList<Pair<Cell, Cell>> listOfMoves, Field field){
        this.listOfMoves = listOfMoves;
        lastField = field;
        points = 0;
    }

    ArrayList<Pair<Cell, Cell>> getListOfMoves() {
        return listOfMoves;
    }

    void setLastField(Field lastField) {
        this.lastField = lastField;
    }
    Field getLastField() {
        return lastField;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    public int getPoints() {
        return points;
    }

    public void setFinished(){
        finished = true;
    }
    boolean isFinished(){
        return finished;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for (Pair<Cell, Cell> move : listOfMoves){
            str.append(move.getFirst().pos()).append(" -> ").append(move.getSecond().pos()).append("; ");
        }
        return str.toString();
    }

    Branch copy(){
        ArrayList<Pair<Cell, Cell>> listOfMoves = new ArrayList<>();
        for (Pair<Cell, Cell> move : this.listOfMoves){
            Pair<Cell, Cell> copyMove = new Pair<>(move.getFirst().copy(), move.getSecond().copy());
            listOfMoves.add(copyMove);
        }
        return new Branch(listOfMoves, lastField.copy());
    }
}
