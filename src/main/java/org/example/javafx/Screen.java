package org.example.javafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Screen extends Canvas {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private int scale = 12;
    private final GraphicsContext graphicsContext;

    public int[][] graphic = new int[WIDTH][HEIGHT];
    // Graphics in Chip 8 is a black  and white screen of 2048
    // pixels (62*32).

    public Screen() {
        super(800,400);
        setFocusTraversable(true);

        graphicsContext = this.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0,0,800,400);
        clear();
    }

    public void clear(){
        System.out.println("CLEAR");
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                graphic[x][y] = 0;
            }
        }
    }

    public void render() {
//        printDebug();
        for (int x = 0; x < graphic.length; x++) {
            for (int y = 0; y < graphic[y].length; y++) {
                if(graphic[x][y] == 1){
                    graphicsContext.setFill(Color.WHITE);
                } else {
                    graphicsContext.setFill(Color.BLACK);
                }
                graphicsContext.fillRect(x*scale, y*scale, scale, scale);
            }
        }
    }

    public void printDebug(){
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                System.out.print(graphic[x][y] + "");
            }
            System.out.println();
        }
    }

    public int getPixel(int x, int y) {
        return graphic[x][y];
    }

    public void setPixel(int x, int y) {
        graphic[x][y] ^= 1;
    }
}
