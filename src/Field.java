import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class Field{

    private Cell[][] cells = new Cell[8][8];
    private Cell figureThatMustBeat;
    private Turn turn = Turn.WHITE;     // текущий ход
    private Turn player;   // цвет фигур игрока
    private boolean gameOver = false;

    Field(){
        for (int column = 0; column < 8; column++){
            for (int row = 0; row < 8; row++){
                cells[column][row] = new Cell(column, row);
            }
        }
    }

    private Field(Cell[][] cells, Cell fTMB, Turn turn){
        System.arraycopy(cells, 0, this.cells, 0, 8);
        this.figureThatMustBeat = fTMB;
        this.turn = Turn.valueOf(turn.toString());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int row = 7; row >= 0; row--){
            for (int column = 0; column < 8; column++){
                switch (cells[column][row].figure){
                    case WHITE_M:
                        sb.append("w");
                        break;
                    case BLACK_M:
                        sb.append("b");
                        break;
                    case WHITE_K:
                        sb.append("W");
                        break;
                    case BLACK_K:
                        sb.append("B");
                        break;
                    case MISSING:
                        sb.append(" ");
                        break;
                }
            }
            sb.append("\n");
        }
        sb.append("___________________");
        return sb.toString();
    }

    private Cell cell(String pos){
        String position = pos.toUpperCase();
        int column = (int) position.charAt(0) - 65;
        int row = (int) position.charAt(1) - 49;
        return cells[column][row];
    }

    Cell cell(int column, int row){
        return cells[column][row];
    }

    private void upgradeMen(){
        for (int column = 0; column < 8; column++){
            if (cells[column][7].figure == Figure.WHITE_M)
                cells[column][7].figure = Figure.WHITE_K;
            if (cells[column][0].figure == Figure.BLACK_M)
                cells[column][0].figure = Figure.BLACK_K;
        }
    }

    private void changeTurn(){
        if (turn == Turn.WHITE) turn = Turn.BLACK;
        else turn = Turn.WHITE;
    }

    private String gameOver() {
        boolean blackNaMeste = false;
        boolean whiteNaMeste = false;
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if (cells[i][j].figure == Figure.BLACK_M || cells[i][j].figure == Figure.BLACK_K) {
                    blackNaMeste = true;
                    break;
                }
            }
            if (blackNaMeste) break;
        }
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if (cells[i][j].figure == Figure.WHITE_M || cells[i][j].figure == Figure.WHITE_K) {
                    whiteNaMeste = true;
                    break;
                }
            }
            if (whiteNaMeste) break;
        }

        if (blackNaMeste && !whiteNaMeste)
            return blackWin;
        else if (whiteNaMeste && !blackNaMeste)
            return whiteWin;

        return null;
    }

    static final String blackWin = "Игра окончена! Выиграли черные фигуры";
    static final String whiteWin = "Игра окончена! Выиграли черные фигуры";

    // может ли фигура бить (учитывается текущий ход)
    private boolean ableForBeating(Cell cell) {

        if (turn == Turn.WHITE && cell.figure != Figure.WHITE_M && cell.figure != Figure.WHITE_K)
            return false;
        else if (turn == Turn.BLACK && cell.figure != Figure.BLACK_M && cell.figure != Figure.BLACK_K)
            return false;

        int dmain = cell.column - cell.row;
        int dside = cell.column + cell.row;
        List<Cell> list;

        Cell mainTop, mainBottom, sideTop, sideBottom;

        if (dmain < 0){
            mainTop = cells[7+dmain][7];
            mainBottom = cells[0][-dmain];
        } else {
            mainTop = cells[7][7-dmain];
            mainBottom = cells[dmain][0];
        }
        if (dside < 7){
            sideTop = cells[0][dside];
            sideBottom = cells[dside][0];
        } else {
            sideTop = cells[dside-7][7];
            sideBottom = cells[7][dside-7];
        }

        list = listOfCells(cell, mainTop);
        if (ableForBeating(list, Control.DIAGONAL)) return true;
        list = listOfCells(cell, sideBottom);
        if (ableForBeating(list, Control.DIAGONAL)) return true;
        list = listOfCells(cell, mainBottom);
        if (ableForBeating(list, Control.DIAGONAL)) return true;
        list = listOfCells(cell, sideTop);
        if (ableForBeating(list, Control.DIAGONAL)) return true;

        return false;
    }

    // анализ ВОЗМОЖНОСТИ совершить beating ход
    private boolean ableForBeating(List<Cell> cellList, Control control){
        if (cellList == null)
            return false;

        if (cellList.size() <= 2) return false;
        if (cellList.get(0).figure == Figure.WHITE_M || cellList.get(0).figure == Figure.BLACK_M) {
            if (control == Control.DIAGONAL) {
                return ableToBeat(cellList.subList(0, 3));
            } else {
                return ableToBeat(cellList);
            }
        }

        for (int i = 1; i < cellList.size()-1; i++){
            if (cellList.get(0).figure == Figure.WHITE_K) {
                if (cellList.get(i).figure == Figure.WHITE_M || cellList.get(i).figure == Figure.WHITE_K)
                    return false;
                if (cellList.get(i).figure == Figure.BLACK_M || cellList.get(i).figure == Figure.BLACK_K)
                    return cellList.get(i + 1).figure == Figure.MISSING;
            } else if (cellList.get(0).figure == Figure.BLACK_K) {
                if (cellList.get(i).figure == Figure.BLACK_M || cellList.get(i).figure == Figure.BLACK_K)
                    return false;
                if (cellList.get(i).figure == Figure.WHITE_M || cellList.get(i).figure == Figure.WHITE_K) {
                    return cellList.get(i + 1).figure == Figure.MISSING;
                }
            }
        }
        return false;
    }

    enum Control {
        DEFAULT, DIAGONAL
    }

    // анализ КОРРЕКТНОСТИ совершения beating хода
    private boolean ableToBeat(List<Cell> cellList){
        if (cellList == null)
            return false;

        if (cellList.size() <= 2) return false;

        switch (cellList.get(0).figure){
            case WHITE_M:
                return (cellList.get(1).figure == Figure.BLACK_M || cellList.get(1).figure == Figure.BLACK_K)
                        && cellList.get(2).figure == Figure.MISSING && cellList.size() == 3 && turn == Turn.WHITE;
            case BLACK_M:
                return (cellList.get(1).figure == Figure.WHITE_M || cellList.get(1).figure == Figure.WHITE_K)
                        && cellList.get(2).figure == Figure.MISSING && cellList.size() == 3 && turn == Turn.BLACK;
        }

        boolean f;
        switch (cellList.get(0).figure){
            case WHITE_K:
                f = false;
                for (int i = 1; i < cellList.size()-1; i++){
                    if (cellList.get(i).figure == Figure.WHITE_M || cellList.get(i).figure == Figure.WHITE_K)
                        return false;
                    if (cellList.get(i).figure == Figure.BLACK_M || cellList.get(i).figure == Figure.BLACK_K){
                        if (!f) f = true;
                        else return false;
                    }
                }
                return f;
            case BLACK_K:
                f = false;
                for (int i = 1; i < cellList.size()-1; i++){
                    if (cellList.get(i).figure == Figure.BLACK_M || cellList.get(i).figure == Figure.BLACK_K)
                        return false;
                    if (cellList.get(i).figure == Figure.WHITE_M || cellList.get(i).figure == Figure.WHITE_K){
                        if (!f) f = true;
                        else return false;
                    }
                }
                return f;
        }

        return false;
    }

    // анализ КОРРЕКТНОСТИ совершения обычного хода
    private boolean ableToMove(List<Cell> cellList){
        if (cellList == null)
            return false;

        Cell first = cellList.get(0);
        Cell last = cellList.get(cellList.size()-1);

        if (last.figure != Figure.MISSING)
            return false;

        if (figureThatMustBeat != null && !figureThatMustBeat.equals(first))
            return false;

        if (figureThatMustBeat == null) {
            switch (first.figure){
                case WHITE_M:
                    if (last.row - first.row < 0) return false;
                    break;
                case BLACK_M:
                    if (last.row - first.row > 0) return false;
                    break;
            }
            if (turn == Turn.WHITE){
                if (first.figure != Figure.WHITE_M && first.figure != Figure.WHITE_K)
                    return false;
                switch (first.figure){
                    case WHITE_M:
                        if (cellList.size() != 2)
                            return false;
                        break;
                    case WHITE_K:
                        for (int i = 1; i < cellList.size()-1; i++)
                            if (cellList.get(i).figure != Figure.MISSING)
                                return false;
                        break;
                }
            } else if (turn == Turn.BLACK){
                if (first.figure != Figure.BLACK_M && first.figure != Figure.BLACK_K)
                    return false;
                switch (first.figure){
                    case BLACK_M:
                        if (cellList.size() != 2)
                            return false;
                        break;
                    case BLACK_K:
                        for (int i = 1; i < cellList.size()-1; i++)
                            if (cellList.get(i).figure != Figure.MISSING)
                                return false;
                        break;
                }
            }
        }
        return true;
    }

    private void deleteFigureBetween(Cell cell1, Cell cell2) {
        List<Cell> temp = listOfCells(cell1, cell2);
        assert temp != null;
        for (int i = 1; i < temp.size()-1; i++){
            Cell c = temp.get(i);
            cells[c.column][c.row].figure = Figure.MISSING;
        }
    }

    public String move(String pos1, String pos2) {
        Cell cell1 = cell(pos1);
        Cell cell2 = cell(pos2);
        return move(cell1, cell2);
    }

    String move(Cell cell1, Cell cell2) {

        boolean mustBeat = false;       // обязан ли игрок бить
        boolean beatingMove = false;    // ход с битьем

        // если есть фигура, которая должна бить, но ход совершается не ею, то GameMessage
        if (figureThatMustBeat != null){
            if (!figureThatMustBeat.equals(cell1))
                return "Нужно бить прошлой фигурой";
            else
                mustBeat = true;
        } // проверяет поле на наличие фигуры (того же цвета, что и совершает ход), которая должна бить
        else switch (turn){
            case WHITE:
                for (int i = 0; i < 8; i++){
                    for (int j = 0; j < 8; j++){
                        if ((cells[i][j].figure == Figure.WHITE_M || cells[i][j].figure == Figure.WHITE_K)
                                && ableForBeating(cells[i][j])){
                            mustBeat = true;
                            break;
                        }
                        if (mustBeat) break;
                    }
                }
                break;
            case BLACK:
                for (int i = 0; i < 8; i++){
                    for (int j = 0; j < 8; j++){
                        if ((cells[i][j].figure == Figure.BLACK_M || cells[i][j].figure == Figure.BLACK_K)
                                && ableForBeating(cells[i][j])){
                            mustBeat = true;
                            break;
                        }
                        if (mustBeat) break;
                    }
                }
                break;
        }

        List<Cell> list = listOfCells(cell1, cell2);
        if (ableForBeating(list, Control.DEFAULT))
            beatingMove = true;

        if (mustBeat && !beatingMove){
            if (figureThatMustBeat != null)
                return "Этой фигурой нужно бить, а не ходить";
            else
                return "Нужно бить, а не ходить";
        }
                                                        if (!mustBeat && beatingMove)
                                                            return "ТАКОЕ НЕ МОГЛО ПРОИЗОЙТИ";
        if (!beatingMove && !ableToMove(list))
            return "Неверный ход";

        if (beatingMove) deleteFigureBetween(cell1, cell2);
        cells[cell2.column][cell2.row].figure = cell1.figure;
        cells[cell1.column][cell1.row].figure = Figure.MISSING;

        upgradeMen();
        String totals = gameOver();

        // если был ход с битьем, смотрим, нужно ли менять ход
        if (beatingMove){
            figureThatMustBeat = cells[cell2.column][cell2.row];
            if (!ableForBeating(figureThatMustBeat)) {
                figureThatMustBeat = null;
                changeTurn();
            }
        } else changeTurn();

        return totals;
    }

    String randomMove() {
        List<Pair<Cell, Cell>> moves = new ArrayList<>();
        boolean finish = false;

        for (int row1 = 0; row1 < 8; row1++){
            for (int column1 = 0; column1 < 8; column1++){
                for (int row2 = 0; row2 < 8; row2++){
                    for (int column2 = 0; column2 < 8; column2++){

                        if ((row1+column1)%2 != 0 || (row2+column2)%2 != 0)
                            continue;

                        Field temp = this.copy();
                        String message = temp.move(temp.cell(column1, row1), temp.cell(column2, row2));
                        if (message != null) {
                            if (message.equals(blackWin) || message.equals(whiteWin)) {
                                moves = new ArrayList<>();
                                moves.add(new Pair<>(this.cell(column1, row1), temp.cell(column2, row2)));
                                finish = true;
                                break;
                            } else {
                                continue;
                            }
                        }
                        moves.add(new Pair<>(this.cell(column1, row1), temp.cell(column2, row2)));
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
            return this.move(lastMove.getFirst(), lastMove.getSecond());
        }

        return "";
    }

    // ряд фигур [cell1, cell2]
    private List<Cell> listOfCells(Cell cell1, Cell cell2) {

        if (cell1.figure == Figure.MISSING)
            return null;

        if (cell1.column - cell1.row != cell2.column - cell2.row
                && cell1.column + cell1.row != cell2.column + cell2.row)
            return null;

        List<Cell> list = new ArrayList<>();
        int dcolumn = cell2.column - cell1.column;
        int drow = cell2.row - cell1.row;
        if (dcolumn == 0 || drow == 0){
            list.add(cell1);
            return list;
        }

        for (int co = cell1.column, ro = cell1.row;
             co != cell2.column + Math.abs(dcolumn)/dcolumn;
             co += Math.abs(dcolumn)/dcolumn, ro += Math.abs(drow)/drow)
        { list.add(this.cells[co][ro]); }

        return list;
    }

    private Field copy(){

        Cell[][] newCells = new Cell[8][8];
        // System.arraycopy(this.cells, 0, newCells, 0, 8);
        for (int column = 0; column < 8; column++)
            for (int row = 0; row < 8; row++)
                newCells[column][row] = this.cells[column][row].copy();

        Cell fTMB;
        if (this.figureThatMustBeat == null) {
            fTMB = null;
        } else {
            fTMB = this.figureThatMustBeat.copy();
        }
        Turn turn = Turn.valueOf(this.turn.toString());

        return new Field(newCells, fTMB, turn);
    }

    public void setPlayer(Turn player) {
        this.player = player;
    }

    public Turn getPlayer() {
        return player;
    }

    public Turn getTurn() {
        return turn;
    }

    public boolean gameIsOver() {
        return gameOver;
    }

    public void setGameOver(boolean b) {
        gameOver = b;
    }
}

class Cell{

    int column;
    int row;
    Figure figure;

    Cell(int column, int row){
        this.column = column;
        this.row = row;

        if ((column + row) % 2 == 0) {
            if (row <= 2) {
                this.figure = Figure.WHITE_M;
            } else if (row >= 5) {
                this.figure = Figure.BLACK_M;
            } else {
                this.figure = Figure.MISSING;
            }
        } else {
            this.figure = Figure.MISSING;
        }
    }

    private Cell(int column, int row, Figure figure){
        this.column = column;
        this.row = row;
        this.figure = figure;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this) return true;
        if (!(obj instanceof Cell)) return false;

        Cell cell = (Cell) obj;
        return (this.column == cell.column) && (this.row == cell.row)
                && (this.figure == cell.figure);
    }

    @Override
    public String toString(){
        return "(" + this.column + " " + this.row + " " + this.figure + ")";
    }

    public String pos(){
        char c1 = (char) (this.column + 65);
        String c2 = Integer.toString(this.row + 1);
        return c1 + c2;
    }

    Cell copy(){

        int column = this.column;
        int row = this.row;
        Figure figure = Figure.valueOf(this.figure.toString());

        return new Cell(column, row, figure);
    }


}

enum Figure{
    WHITE_M,
    BLACK_M,
    WHITE_K,
    BLACK_K,
    MISSING
}

enum Turn{
    WHITE, BLACK
}