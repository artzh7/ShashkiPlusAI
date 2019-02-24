import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kotlin.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Game extends Application {

    private Field field = new Field();

    private Stage fieldWindow;
    private BorderPane layout;
    private GridPane center;
    private Label message;
    private Label turnLabel;

    private Cell2D first, second;

    private Image black_ch, black_k, emptycell, missing, white_ch, white_k;

    {
        try {
            black_ch = new Image(new FileInputStream("textures/black_ch.png"));
            black_k = new Image(new FileInputStream("textures/black_k.png"));
            emptycell = new Image(new FileInputStream("textures/emptycell.png"));
            missing = new Image(new FileInputStream("textures/missing.png"));
            white_ch = new Image(new FileInputStream("textures/white_ch.png"));
            white_k = new Image(new FileInputStream("textures/white_k.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {

        // top - кнопка "новая игра", текущий ход
        // bottom - вывод сообщения GameMessage
        // center - само поле GridPane

        field.setFTMB(field.cell("d4"));
        String path = "src/input/field2.txt";
        field.setCells(path);
        field.setPlayer(PlayerWindow.display());   // окно с выбором цвета фигур

        HBox top = new HBox(10);
        top.setPadding(new Insets(15,15,5,15));
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(0,10,10,10));

        layout = new BorderPane();
        layout.setTop(top);
        layout.setBottom(bottom);

        turnLabel = new Label();
        turnLabel.setAlignment(Pos.TOP_RIGHT);
        refresh();

        Button newGame = new Button("Новая игра");
        top.getChildren().addAll(newGame, turnLabel);

        message = new Label();
        message.setAlignment(Pos.BOTTOM_RIGHT);
        bottom.getChildren().addAll(message);

        fieldWindow = new Stage();
        fieldWindow.setScene(new Scene(layout,420, 500));
        fieldWindow.setMaxHeight(560);
        fieldWindow.setMaxWidth(475);
        fieldWindow.setMinHeight(560);
        fieldWindow.setMinWidth(475);
        fieldWindow.show();

        newGame.setOnMouseClicked(e -> {
            System.out.println("_________________");
            fieldWindow.close();
            first = null;
            second = null;
            message = new Label();
            field = new Field();
            field.setPlayer(PlayerWindow.display());
            refresh();
            fieldWindow.show();
        });
    }

    // перерисовка всей сцены
    private void refresh(){

        center = new GridPane();
        center.setHgap(1);
        center.setVgap(1);
        center.setPadding(new Insets(10));
        if (field.getTurn() == Turn.WHITE)
            turnLabel.setText("                  Ходят белые фигуры");
        else turnLabel.setText("                  Ходят черные фигуры");

        Cell2D[][] cells2D = new Cell2D[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                int column = i;
                int row = j;
                cells2D[i][j] = new Cell2D(i, j);

                if ((i+j)%2 == 0){

                    switch (field.cell(i,j).figure){
                        case WHITE_M:
                            cells2D[i][j].cell.setImage(white_ch);
                            break;
                        case WHITE_K:
                            cells2D[i][j].cell.setImage(white_k);
                            break;
                        case BLACK_M:
                            cells2D[i][j].cell.setImage(black_ch);
                            break;
                        case BLACK_K:
                            cells2D[i][j].cell.setImage(black_k);
                            break;
                        case MISSING:
                            cells2D[i][j].cell.setImage(missing);
                            break;
                    }

                    cells2D[i][j].cell.setOnMouseClicked(e -> {
                        cells2D[column][row].cell.setEffect(new InnerShadow(10, Color.RED));
                        if (first == null)
                            first = cells2D[column][row];
                        else if (second == null)
                            second = cells2D[column][row];
                        if (first != null && second != null){
                            Cell cell1 = field.cell(first.column, first.row);
                            Cell cell2 = field.cell(second.column, second.row);

                            Pair<String, Integer> moveInfo = new Pair<>("", 0);
                            if (!field.gameIsOver())
                                 moveInfo = field.move(cell1, cell2);
                            if (moveInfo.getFirst() != null) {
                                if (moveInfo.getFirst().equals(Field.blackWin) || moveInfo.getFirst().equals(Field.whiteWin)){
                                    field.setGameOver();
                                    displayFinal(moveInfo.getFirst());
                                } else if (!moveInfo.getFirst().isEmpty()) {
                                    message.setText("");
                                }
                            } else message.setText("");

                            first = null;
                            second = null;
                            refresh();
                        }
                    });
                }
                else {
                    cells2D[i][j].cell.setImage(emptycell);
                }

                GridPane.setConstraints(cells2D[i][j].cell, i+1, 8-j);
                for (int k = 8; k >= 1; k--){
                    Label index = new Label(Integer.toString(k) + "  ");
                    index.setAlignment(Pos.TOP_CENTER);
                    GridPane.setConstraints(index, 0, 9-k);
                    center.getChildren().add(index);
                }
                for (int k = 1; k <= 8; k++){
                    Character c = (char) (k + 64);
                    Label index = new Label("       " + c.toString());
                    index.setAlignment(Pos.TOP_CENTER);
                    GridPane.setConstraints(index, k, 9);
                    center.getChildren().add(index);
                }
                center.getChildren().addAll(cells2D[i][j].cell);
            }
        }
        layout.setCenter(center);

        // ------> рандомный ход (to be continued) <------
        String moveMessage;
        if (field.getTurn() != field.getPlayer() && !field.gameIsOver()){
            Bot bot = new Bot(field, 2);
            moveMessage = bot.randomMove(field);
            if (moveMessage != null && (moveMessage.equals(Field.blackWin) || moveMessage.equals(Field.whiteWin))) {
                field.setGameOver();
                displayFinal(moveMessage);
            }
            refresh();
        }
    }

    // окно с информацией о чьей-то победе
    private void displayFinal(String finalMessage){
        Stage finalWindow = new Stage();
        Label text = new Label(finalMessage);
        text.setAlignment(Pos.TOP_CENTER);
        StackPane layout = new StackPane();
        layout.getChildren().add(text);
        finalWindow.setScene(new Scene(layout, 270, 40));
        finalWindow.show();
    }
}

class PlayerWindow{

    private static Turn player = Turn.WHITE;

    static Turn display() {

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);

        Button buttonW = new Button("Белые");
        buttonW.setOnAction(e -> {
            player = Turn.WHITE;
            window.close();
        });
        Button buttonB = new Button("Черные");
        buttonB.setOnAction(e -> {
            player = Turn.BLACK;
            window.close();
        });

        HBox center = new HBox(10);
        center.setPadding(new Insets(10));
        center.getChildren().addAll(buttonW, buttonB);
        center.setAlignment(Pos.TOP_CENTER);

        HBox top = new HBox(10);
        Label text = new Label("Ваши шашки");
        top.setPadding(new Insets(10, 0,0,0));
        top.getChildren().add(text);
        top.setAlignment(Pos.TOP_CENTER);

        BorderPane layout = new BorderPane();
        layout.setTop(top);
        layout.setCenter(center);
        window.setScene(new Scene(layout, 200, 75));
        window.showAndWait();

        return player;
    }
}

class Cell2D {

    ImageView cell = new ImageView();
    int column, row;

    Cell2D(int column, int row){
        this.column = column;
        this.row = row;
    }
}