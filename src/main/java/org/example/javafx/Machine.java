package org.example.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;

public class Machine extends Application {
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 450;

    private Stage mainStage;

    Timeline gameLoop;

    private CHIP8 chip8;
    private Screen screen;
    private Keyboard keyboard;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        mainStage.setTitle("CHIP-8-Emulator");
        screen = new Screen();
        keyboard = new Keyboard();
        chip8 = new CHIP8(keyboard, screen);
        byte[] rom = Files.readAllBytes(Path.of("C:\\Users\\Hiennd-yopaz\\Documents\\code\\javafx\\room\\TETRIS"));
        chip8.loadRom(rom);
        VBox root = new VBox();
        root.getChildren().add(screen);
        Scene mainScene  = new Scene(root);
        mainScene.setOnKeyPressed(e-> keyboard.setKeyDown(e.getCode()));
        mainScene.setOnKeyReleased(e -> keyboard.setKeyUp(e.getCode()));
        mainStage.setScene(mainScene);
        mainStage.setMaxWidth(SCREEN_WIDTH);
        mainStage.setMaxHeight(SCREEN_HEIGHT);
        mainStage.setMinWidth(SCREEN_WIDTH);
        mainStage.setMinHeight(SCREEN_HEIGHT);
        mainStage.setResizable(false);

        gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);

        // Construct the keyframe telling the application what to happen inside the game loop.
        KeyFrame kf = new KeyFrame(
                Duration.seconds(0.003),
                actionEvent -> {
                    try {
                        chip8.emulateCycle();

                    }catch (RuntimeException e){
                        e.printStackTrace();
                        gameLoop.stop();
                    }

                    // Render
                    if(chip8.isDrawFlag()){
                      screen.render();
                      chip8.setDrawFlag(false);
                    }

                    // update timers
                    if (chip8.getDelay_timer() > 0) {
                        chip8.setDelay_timer(chip8.getDelay_timer() - 1);
                    }

                    if (chip8.getSound_timer() > 0) {
                        if (chip8.getSound_timer() == 1) {
                            System.out.println("Make Sound!");
                        }
                        chip8.setSound_timer(chip8.getSound_timer() - 1);
                    }
                }
        );
        gameLoop.getKeyFrames().add(kf);
        gameLoop.play();
        mainStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
