package gameController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Controller {

    @FXML
    private TextField name, remoteHostname;
    @FXML
    private Label usernameField, scoreCounter, networkMessageLabel;
    @FXML
    private Button stateButton, closeButton, nameButton, submitButton;
    @FXML
    private Rectangle t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16;
    @FXML
    private Label l01, l02, l03, l04, l05, l06, l07, l08, l09, l10, l11, l12, l13, l14, l15, l16;

    private Label[] labels;
    private Rectangle[] tiles;

    private int score = 0, newTilePosition = 0;
    private boolean gameState = false;

    Map<Integer, Color> colorMap = new HashMap<>() {
        @Override
        public Color get(Object key) {
            if (!containsKey(key)) return Color.valueOf("Gray");
            return super.get(key);
        }
    };

    @FXML
    private void initialize() {
        tiles = new Rectangle[]{ t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16 };
        labels = new Label[]{ l01, l02, l03, l04, l05, l06, l07, l08, l09, l10, l11, l12, l13, l14, l15, l16 };

        colorMap.put(2, Color.valueOf("Blue"));
        colorMap.put(4, Color.valueOf("Turquoise"));
        colorMap.put(8, Color.valueOf("Brown"));
        colorMap.put(16, Color.valueOf("Magenta"));
        colorMap.put(32, Color.valueOf("Yellow"));
        colorMap.put(64, Color.valueOf("Cyan"));
        colorMap.put(128, Color.valueOf("Green"));
        colorMap.put(256, Color.valueOf("Orange"));
        colorMap.put(512, Color.valueOf("Lime"));
        colorMap.put(1024, Color.valueOf("Navy"));
        colorMap.put(2048, Color.valueOf("Pink"));
    }

    private void startGame() {
        // save Username to avoid unnecessary overload
        nameButton.setText("Change name");
        name.setDisable(true);
        String username = name.getText();
        if (Objects.equals(username, "")) username = "Player";

        usernameField.setText("Player: " + username);

        score = 0;
        scoreCounter.setText("Points: 0");
        stateButton.setText("Reset Game");
        stateButton.setTextFill(Color.valueOf("#2ad3ac"));
        gameState = true;

        Random ran = new Random();

        int position = ran.nextInt(16);
        int value = ran.nextInt(2);
        if (value == 1) {
            changeTile(position, 2);
        }
        else {
            changeTile(position, 4);
        }
    }
    private void resetGame() {
        for (int i = 0; i < 16; i++) {
            tiles[i].setFill(Color.valueOf("Gray"));
            labels[i].setText("");
        }
        scoreCounter.setText("Points: ");
        stateButton.setText("Start Game");
        stateButton.setTextFill(Color.valueOf("#17b517"));
        scoreCounter.setTextFill(Color.valueOf("Black"));
        remoteHostname.setVisible(false);
        submitButton.setVisible(false);
        networkMessageLabel.setVisible(false);
        score = 0;
        gameState = false;
    }

    private void updateScore(int points) {
        score += points;
        scoreCounter.setText("Points: " + score);
    }

    private void changeTile(int position, int tileValue) {
        tiles[position].setFill(colorMap.get(tileValue));
        labels[position].setText(String.valueOf(tileValue));
        if (tileValue == 0) labels[position].setText("");
    }
    private void addNewTile(){
        if (isBlocked()) {
            scoreCounter.setTextFill(Color.valueOf("Red"));

            remoteHostname.setVisible(true);
            submitButton.setVisible(true);

        }

        if (newTilePosition != 0) labels[newTilePosition].setUnderline(false);

        ArrayList<Integer> emptyTiles = new ArrayList<>();

        for (int tile = 0; tile < 16; tile++) {
            if (Objects.equals(labels[tile].getText(), "")) emptyTiles.add(tile);
        }

        if (emptyTiles.isEmpty()) return;

        Random ran = new Random();

        int newPosition = ran.nextInt(emptyTiles.size());

        int value = ran.nextInt(6);

        switch (value) {
            case 0, 1, 2 -> changeTile(emptyTiles.get(newPosition), 2);
            case 3, 4 -> changeTile(emptyTiles.get(newPosition), 4);
            case 5 -> changeTile(emptyTiles.get(newPosition), 8);
        }

        labels[emptyTiles.get(newPosition)].setUnderline(true);
        newTilePosition = emptyTiles.get(newPosition);
    }

    private void mergeUp() {
        for (int col = 0; col < 4; col++) {
            for (int tile = col; tile < (12 + col); tile += 4) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;
                if (Objects.equals(labels[tile].getText(), "2048")) continue;
                if (tiles[tile].getFill() == tiles[tile + 4].getFill()) {
                    int value = Integer.parseInt(labels[tile].getText()) * 2;
                    updateScore(value);
                    changeTile(tile, value);
                    changeTile(tile + 4, 0);
                }
            }
        }
    }
    private void mergeDown() {
        for (int col = 3; col >= 0; col--) {
            for (int tile = col + 12; tile >= 4; tile -= 4) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;
                if (Objects.equals(labels[tile].getText(), "2048")) continue;
                if (tiles[tile].getFill() == tiles[tile - 4].getFill()) {
                    int value = Integer.parseInt(labels[tile].getText()) * 2;
                    updateScore(value);
                    changeTile(tile, value);
                    changeTile(tile - 4, 0);
                }
            }
        }
    }
    private void mergeLeft() {
        for (int row = 0; row < 4; row++) {
            for (int tile = row * 4; tile < row * 4 + 3; tile++) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;
                if (Objects.equals(labels[tile].getText(), "2048")) continue;
                if (tiles[tile].getFill() == tiles[tile + 1].getFill()) {
                    int value = Integer.parseInt(labels[tile].getText()) * 2;
                    updateScore(value);
                    changeTile(tile, value);
                    changeTile(tile + 1, 0);
                }
            }
        }
    }
    private void mergeRight() {
        for (int row = 0; row < 4; row++) {
            for (int tile = (row * 4) + 3; tile > (row * 4); tile--) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;
                if (Objects.equals(labels[tile].getText(), "2048")) continue;
                if (tiles[tile].getFill() == tiles[tile - 1].getFill()) {
                    int value = Integer.parseInt(labels[tile].getText()) * 2;
                    updateScore(value);
                    changeTile(tile, value);
                    changeTile(tile - 1, 0);
                }
            }
        }
    }

    private int checkAvailableUp(int position) {
        for (int i = (position % 4); i < position; i += 4) {
            if (tiles[i].getFill() == colorMap.get(0)) return i;
        }
        return position;
    }
    private int checkAvailableDown(int position) {
        for (int i = 12 + (position % 4); i > position; i -= 4) {
            if (tiles[i].getFill() == colorMap.get(0)) return i;
        }
        return position;
    }
    private int checkAvailableLeft(int position) {
        for (int i = ((position / 4) * 4); i < position; i++) {
            if (tiles[i].getFill() == colorMap.get(0)) return i;
        }
        return position;
    }
    private int checkAvailableRight(int position) {
        for (int i = 3 + ((position / 4) * 4); i > position; i--) {
            if (tiles[i].getFill() == colorMap.get(0)) return i;
        }
        return position;
    }

    private void moveUp() {
        for (int col = 0; col < 4; col++) {
            for (int tile = col; tile < (13 + col); tile += 4) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;

                int targetPos = checkAvailableUp(tile);
                if (targetPos != tile) {
                    changeTile(targetPos, Integer.parseInt(labels[tile].getText()));
                    changeTile(tile, 0);
                }
            }
        }
    }
    private void moveDown() {
        for (int col = 3; col >= 0; col--) {
            for (int tile = col + 12; tile >= 0; tile -= 4) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;

                int targetPos = checkAvailableDown(tile);
                if (targetPos != tile) {
                    changeTile(targetPos, Integer.parseInt(labels[tile].getText()));
                    changeTile(tile, 0);
                }
            }
        }
    }
    private void moveRight() {
        for (int row = 0; row < 4; row++) {
            for (int tile = (row * 4) + 3; tile >= (row * 4); tile--) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;

                int targetPos = checkAvailableRight(tile);
                if (targetPos != tile) {
                    changeTile(targetPos, Integer.parseInt(labels[tile].getText()));
                    changeTile(tile, 0);
                }
            }
        }
    }
    private void moveLeft() {
        for (int row = 0; row < 4; row++) {
            for (int tile = row * 4; tile < (row * 4) + 4; tile++) {
                if (tiles[tile].getFill() == colorMap.get(0)) continue;

                int targetPos = checkAvailableLeft(tile);
                if (targetPos != tile) {
                    changeTile(targetPos, Integer.parseInt(labels[tile].getText()));
                    changeTile(tile, 0);
                }
            }
        }
    }

    private boolean isBlocked() {
        for (int tile = 0; tile < 16; tile++) {
            String tileValue = labels[tile].getText();

            if (Objects.equals(tileValue, "")) return false;

            if (tile - 4 > 0 && Objects.equals(labels[tile - 4].getText(), tileValue)) return false;
            if (tile + 4 < 16 && Objects.equals(labels[tile + 4].getText(), tileValue)) return false;

            if (tile - 1 > tile / 4 * 4 && Objects.equals(labels[tile - 1].getText(), tileValue)) return false;
            if (tile + 1 < tile / 4 * 4 && Objects.equals(labels[tile + 1].getText(), tileValue)) return false;
        }
        return true;
    }

    public void buttonActionUp() {
        moveUp();
        mergeUp();
        moveUp();
        addNewTile();
    }
    public void buttonActionDown() {
        moveDown();
        mergeDown();
        moveDown();
        addNewTile();
    }
    public void buttonActionLeft() {
        moveLeft();
        mergeLeft();
        moveLeft();
        addNewTile();
    }
    public void buttonActionRight() {
        moveRight();
        mergeRight();
        moveRight();
        addNewTile();
    }
    public void saveUsername() {
        if(name.isDisabled()) {
            name.setDisable(false);
            nameButton.setText("Save Name");
            return;
        }
        nameButton.setText("Change Name");
        name.setDisable(true);
        String username = name.getText().replace(" ", "_");
        if (Objects.equals(username, "")) username = "Player";
        usernameField.setText("Player: " + username);
    }
    public void changeGameState() {
        if (gameState) resetGame();
        else startGame();
    }
    public boolean getGameState() {
        return gameState;
    }
    public void stopGame()
    {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    public void submitScore () {
        String hostname = remoteHostname.getText();

        if (Objects.equals(hostname, "")) return;

        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + hostname))
                    .POST(HttpRequest.BodyPublishers.ofString(String.format("username=%s&score=%s", usernameField.getText().split(" ")[1], scoreCounter.getText().split(" ")[1])))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response);
        } catch (IOException | InterruptedException e) {
            networkMessageLabel.setText("Score could not be sent");
            throw new RuntimeException(e);
        }
    }
}
