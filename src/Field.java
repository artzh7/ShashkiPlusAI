import kotlin.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Field{

    private Cell[][] cells = new Cell[8][8];
    private Cell figureThatMustBeat;
    private Turn turn = Turn.WHITE;             // текущий ход
    private Turn player;                        // цвет фигур игрока
    private boolean gameOver = false;           // для вывода окна окончания игры / чтобы рандомный ход не задумывался над последним шагом

    Field(){
        for (int column = 0; column < 8; column++){
            for (int row = 0; row < 8; row++){
                cells[column][row] = new Cell(column, row);
            }
        }
    }

    private Field(Cell[][] cells, Cell fTMB, Turn turn){    // используется при копировании поля
        System.arraycopy(cells, 0, this.cells, 0, 8);
        this.figureThatMustBeat = fTMB;
        this.turn = Turn.valueOf(turn.toString());
    }

    private Field(String inputPath) throws FileNotFoundException {

        File input = new File(inputPath);
        Scanner scanner = new Scanner(input);

        Cell[][] cells1 = new Cell[8][8];
        int row = 7;
        while (scanner.hasNextLine() && row >= 0){
            String line = scanner.nextLine();
            for (int column = 0; column < 8; column++){
                switch (line.charAt(column)){
                    case 'w':
                        cells1[column][row] = new Cell(column, row, Figure.WHITE_M);
                        break;
                    case 'W':
                        cells1[column][row] = new Cell(column, row, Figure.WHITE_K);
                        break;
                    case 'b':
                        cells1[column][row] = new Cell(column, row, Figure.BLACK_M);
                        break;
                    case 'B':
                        cells1[column][row] = new Cell(column, row, Figure.BLACK_K);
                        break;
                    case '.':
                        cells1[column][row] = new Cell(column, row, Figure.MISSING);
                        break;
                }
            }
            row--;
        }
        cells = cells1;
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

    Cell cell(String pos){
        String position = pos.toUpperCase();
        int column = (int) position.charAt(0) - 65;
        int row = (int) position.charAt(1) - 49;
        return cells[column][row];
    }

    Cell cell(int column, int row){
        return cells[column][row];
    }

    private int upgradeMen(){
        int points = 0;
        for (int column = 0; column < 8; column++){
            if (cells[column][7].figure == Figure.WHITE_M) {
                cells[column][7].figure = Figure.WHITE_K;
                points += 100;
            }
            if (cells[column][0].figure == Figure.BLACK_M) {
                cells[column][0].figure = Figure.BLACK_K;
                points += 100;
            }
        }
        return points;
    }

    private void changeTurn(){
        if (turn == Turn.WHITE) turn = Turn.BLACK;
        else turn = Turn.WHITE;
    }

    private String gameOver() { // проверка на окончание игры
                // требует доработки: игра может быть закончена и тогда, когда фигуры не могут двигаться
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
    static final String whiteWin = "Игра окончена! Выиграли белые фигуры";

    private boolean ableForBeating(Cell cell) {

        if (turn == Turn.WHITE && cell.figure != Figure.WHITE_M && cell.figure != Figure.WHITE_K) return false;
        else if (turn == Turn.BLACK && cell.figure != Figure.BLACK_M && cell.figure != Figure.BLACK_K) return false;

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

    private boolean ableForBeating(List<Cell> cellList, Control control){

        if (cellList == null) return false;
        if (cellList.size() <= 2) return false;

        if (cellList.get(0).figure == Figure.WHITE_M || cellList.get(0).figure == Figure.BLACK_M) {
            if (control == Control.DIAGONAL) return ableToBeat(cellList.subList(0, 3));
            else return ableToBeat(cellList);
        }

        for (int i = 3; i <= cellList.size(); i++){
            if (ableToBeat(cellList.subList(0, i))) return true;
        }
        return false;
    }

    enum Control {
        DEFAULT, DIAGONAL
    }

    private boolean ableToBeat(List<Cell> cellList){

        if (cellList == null) return false;
        if (cellList.size() <= 2) return false;
        if (cellList.get(cellList.size()-1).figure != Figure.MISSING) return false;

        switch (cellList.get(0).figure){
            case WHITE_M:
                return (cellList.get(1).figure == Figure.BLACK_M || cellList.get(1).figure == Figure.BLACK_K)
                        && cellList.size() == 3
                        && turn == Turn.WHITE;
            case BLACK_M:
                return (cellList.get(1).figure == Figure.WHITE_M || cellList.get(1).figure == Figure.WHITE_K)
                        && cellList.size() == 3
                        && turn == Turn.BLACK;
        }

        for (int i = 1; i < cellList.size()-1; i++) {
            if (cellList.get(i).figure != Figure.MISSING && cellList.get(i+1).figure != Figure.MISSING)
                return false;
        }

        boolean exists = false;
        switch (cellList.get(0).figure){
            case WHITE_K:
                for (int i = 1; i < cellList.size()-1; i++){
                    if (cellList.get(i).figure == Figure.BLACK_M || cellList.get(i).figure == Figure.BLACK_K){
                        exists = true;
                        break;
                    }
                }
                break;
            case BLACK_K:
                for (int i = 1; i < cellList.size()-1; i++){
                    if (cellList.get(i).figure == Figure.WHITE_M || cellList.get(i).figure == Figure.WHITE_K){
                        exists = true;
                        break;
                    }
                }
                break;
        }
        if (!exists) return false;

        switch (cellList.get(0).figure){
            case WHITE_K:
                for (int i = 1; i < cellList.size()-1; i++){
                    if (!(cellList.get(i).figure != Figure.WHITE_M && cellList.get(i).figure != Figure.WHITE_K))
                        return false;
                }
                break;
            case BLACK_K:
                for (int i = 1; i < cellList.size()-1; i++){
                    if (!(cellList.get(i).figure != Figure.BLACK_M && cellList.get(i).figure != Figure.BLACK_K))
                        return false;
                }
                break;
        }

        return true;
    }

    private boolean ableToMove(List<Cell> cellList){
        if (cellList == null) return false;

        Cell first = cellList.get(0);
        Cell last = cellList.get(cellList.size()-1);

        // ход совершается не на пустую клетку -> false
        if (last.figure != Figure.MISSING) return false;
        // какая-то фигура должна бить, а ею ходят -> false
        if (figureThatMustBeat != null && !figureThatMustBeat.equals(first)) return false;

        if (figureThatMustBeat == null) {

            if (first.figure == Figure.WHITE_M) {
                if (last.row - first.row < 0) return false;
            } else if (first.figure == Figure.BLACK_M) {
                if (last.row - first.row > 0) return false;
            } // если шашка ходит не в нужную сторону -> false

            if (turn == Turn.WHITE && first.figure != Figure.WHITE_M && first.figure != Figure.WHITE_K)
                return false; // если ход белыми фигурами и первая фигура НЕ белая
            if (turn == Turn.BLACK && first.figure != Figure.BLACK_M && first.figure != Figure.BLACK_K)
                return false; // если ход черными фигурами и первая фигура НЕ черная

            if ((first.figure == Figure.WHITE_M || first.figure == Figure.BLACK_M) && cellList.size() != 2)
                return false; // если простая шашка перемещается НЕ на одну клетку
            if (first.figure == Figure.WHITE_K || first.figure == Figure.BLACK_K) {
                for (int i = 1; i < cellList.size() - 1; i++)
                    if (cellList.get(i).figure != Figure.MISSING)
                        return false; // если между исходной и новой клеткой попадутся фигуры
            }
        }

        return true;
    }

    private int deleteFigureBetween(Cell cell1, Cell cell2) {
        List<Cell> temp = listOfCells(cell1, cell2);
        assert temp != null;
        int points = 0;
        for (int i = 1; i < temp.size()-1; i++){
            Cell c = temp.get(i);
            switch (c.figure){
                case WHITE_K:
                case BLACK_K:
                    points += 150;
                    break;
                case WHITE_M:
                case BLACK_M:
                    points += 50;
                    break;
            }
            cells[c.column][c.row].figure = Figure.MISSING;
        }
        return points;
    }

    Pair<String, Integer> move(String pos1, String pos2) {
        Cell cell1 = cell(pos1);
        Cell cell2 = cell(pos2);
        return move(cell1, cell2);
    }

    Pair<String, Integer> move(Cell cell1, Cell cell2) {

        boolean mustBeat = false;       // true, если должен быть ход с битьем
        boolean beatingMove = false;    // true, если этот ход с битьем

        int points = 0;

        // если есть фигура, которая должна бить, но действие совершается не ею, ход не совершается
        if (figureThatMustBeat != null){
            if (!figureThatMustBeat.equals(cell1)) return new Pair<>("Нужно бить прошлой фигурой", 0);
            else mustBeat = true;
        }
        /* иначе проверяет поле на наличие фигуры (того же цвета, что и совершает ход),
           которая обязана совершить ход с битьем (теоретически: "иначе ее возьмут за фук") */
        else {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (ableForBeating(cells[i][j])) {
                        mustBeat = true;
                        break;
                    }
                }
                if (mustBeat) break;
            }
        }

        List<Cell> list = listOfCells(cell1, cell2);    // шашки между [cell1:cell2]
        if (ableForBeating(list, Control.DEFAULT))      // если ход с битьем
            beatingMove = true;

        if (mustBeat && !beatingMove){  // если игрок может бить ТЕОРЕТИЧЕСКИ, но не бьет
            if (figureThatMustBeat != null) return new Pair<>("Этой фигурой нужно бить, а не ходить", 0);
            else return new Pair<>("Нужно бить, а не ходить", 0);
        }

        // если игрок не может сходить битьем ТЕОРЕТИЧЕСКИ, но бьет ПРАКТИЧЕСКИ
        if (!mustBeat && beatingMove) return new Pair<>("ТАКОЕ НЕ МОГЛО ПРОИЗОЙТИ", 0);
        // если игрок не бьет, но ходит некорректно
        if (!beatingMove && !ableToMove(list)) return new Pair<>("Неверный ход", 0);
        // если дамка бьет неправильно
        if (beatingMove && (cell1.figure == Figure.WHITE_K || cell1.figure == Figure.BLACK_K) && !ableToBeat(list))
            return new Pair<>("Неверный ход", 0);


        if (beatingMove)
            points += deleteFigureBetween(cell1, cell2);     // если все проверки пройдены, смахиваем фигуры с поля
        cells[cell2.column][cell2.row].figure = cell1.figure;
        cells[cell1.column][cell1.row].figure = Figure.MISSING;

        points += upgradeMen();
        String totals = gameOver();
        if (totals != null && (totals.equals(Field.whiteWin) || totals.equals(Field.blackWin)))
            points += 300;

        // если был ход с битьем, смотрим, нужно ли менять ход
        if (beatingMove){
            figureThatMustBeat = cells[cell2.column][cell2.row];
            if (!ableForBeating(figureThatMustBeat)) {
                figureThatMustBeat = null;
                changeTurn();
            }
        } else changeTurn();

        return new Pair<>(totals, points);
    }

    private List<Cell> listOfCells(Cell cell1, Cell cell2) {

        if (cell1.getFigure() == Figure.MISSING)
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

    Field copy(){

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

    void setPlayer(Turn player) {
        this.player = player;
    }
    Turn getPlayer() {
        return player;
    }
    Turn getTurn() {
        return turn;
    }
    void setTurn(Turn turn) {
        this.turn = turn;
    }
    boolean gameIsOver() {
        return gameOver;
    }
    void setGameOver() {
        gameOver = true;
    }
//    public void setFTMB(Cell fTMB) {
//        this.figureThatMustBeat = fTMB;
//    }
//    public Cell getFTMB() {
//        return figureThatMustBeat;
//    }
}

class Cell{

    final int column;
    final int row;
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

    Cell(int column, int row, Figure figure){
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

    String pos(){
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

    Figure getFigure() {
        return figure;
    }
}

enum Figure{
    WHITE_M,    // белая шашка
    BLACK_M,    // черная шашка
    WHITE_K,    // белая дамка
    BLACK_K,    // черная дамка
    MISSING     // клетку никто не занимает
}

enum Turn{
    WHITE, BLACK
}