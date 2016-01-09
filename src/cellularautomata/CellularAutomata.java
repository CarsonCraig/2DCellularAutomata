/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cellularautomata;

import java.awt.Font;
import java.util.Random;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

/**
 *
 * @author carson
 */
public class CellularAutomata {

    public static int pixel = 1;
    public static int borderSize = 0;
    public static int currentRound;
    public static int brushRadius = 1;
    public static final int rounds = 50;

    public static int[][][] state;
    public static int[][][] neighbourhood = new int[7][7][6];
    public static int[][] path = new int[6][10];

    public static int screenState = 0, menuState = 6;

    public static Random newRandom = new Random();

    public static int black, white, red, blue, green, purple; 
    public static boolean leftClick, rightClick;

    public static boolean vsync = true;

    public static void main(String[] args) {

        for (int yi = 0; yi < 6; yi++) {

            for (int xi = 0; xi < 10; xi++) {

                path[yi][xi] = 1;
            }
        }
        resetState();

        renderGL();

        Font titleFont = new Font("Times New Roman", Font.BOLD, 40);
        TrueTypeFont trueTypeTitleFont = new TrueTypeFont(titleFont, false);

        Font dimensionsFont = new Font("Times New Roman", Font.BOLD, 20);
        TrueTypeFont trueTypeDimensionsFont = new TrueTypeFont(dimensionsFont, false);
        
        Font paintBrushFont = new Font("Times New Roman", Font.BOLD, 30);
        TrueTypeFont trueTypePaintBrushFont = new TrueTypeFont(paintBrushFont, false);
        
        gameLoop(trueTypeTitleFont, trueTypeDimensionsFont, trueTypePaintBrushFont);

    }

    public static void gameLoop(TrueTypeFont trueTypeTitleFont, TrueTypeFont trueTypeDimensionsFont, TrueTypeFont trueTypePaintBrushFont) {
        while (!Display.isCloseRequested()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            int indexX = (int) Math.ceil((Mouse.getX() - 90) / 62);
            int indexY = 5 - ((int) Math.ceil(Mouse.getY() / 86));

            if (menuState == 6 && screenState == 1) {
                int dwheel = Mouse.getDWheel();
                if (dwheel < 0 && indexY < 6 && indexY >= 0 && indexX < 10 && indexX >= 0) {
                    if (path[indexY][indexX] < 6) {
                        path[indexY][indexX]++;
                    }
                } else if (dwheel > 0 && indexY < 6 && indexY >= 0 && indexX < 10 && indexX >= 0) {
                    if (path[indexY][indexX] > 1) {
                        path[indexY][indexX]--;
                    }
                }
            }
            if (screenState == 2) {

                int dwheel = Mouse.getDWheel();
                if (dwheel < 0 && currentRound > 0) {
                    currentRound--;

                } else if (dwheel > 0) {
                    updateAlive();
                }
            }

            input(trueTypeTitleFont);

            if (screenState == 0) {
                drawTitle(trueTypeTitleFont, trueTypePaintBrushFont, trueTypeDimensionsFont);
            }
            if (screenState == 1) {
                drawMenuLoop(trueTypeTitleFont, trueTypeDimensionsFont, trueTypePaintBrushFont);
            }
            if (screenState == 2) {
                drawBoard();
            }

            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }

    public static void renderGL() {
        try { //Trys to create a game window size 500x700.
            Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(800, 600));
            Display.create();
        } catch (LWJGLException e) { //Catches exception if game window is not created.
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //Sets colour to white.
        GL11.glClearDepth(1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, 800, 600);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, 800, 600, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        Display.setVSyncEnabled(vsync);
    }

    public static void setDisplayMode(int width, int height, boolean fullscreen) {

        // return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width)
                && (Display.getDisplayMode().getHeight() == height)
                && (Display.isFullscreen() == fullscreen)) {
            return;
        }

        try {
            DisplayMode targetDisplayMode = null;

            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;

                for (int i = 0; i < modes.length; i++) {
                    DisplayMode current = modes[i];

                    if ((current.getWidth() == width) && (current.getHeight() == height)) {
                        if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                            if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }

                        // if we've found a match for bpp and frequence against the
                        // original display mode then it's probably best to go for this one
                        // since it's most likely compatible with the monitor
                        if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                                && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width, height);
            }

            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
                return;
            }

            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);

        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
        }
    }

    public static void input(TrueTypeFont trueTypeTitleFont) {
        int squareX = (int) Math.ceil(Mouse.getX() / 89);
        int squareY = (int) Math.ceil(Mouse.getY() / 86);

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_F) {
                    setDisplayMode(800, 600, !Display.isFullscreen());
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_V) {
                    vsync = !vsync;
                    Display.setVSyncEnabled(vsync);
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                    if (screenState == 0) {
                        screenState = 1;
                    } else if (screenState == 1) {
                        screenState = 2;
                    } else if (screenState == 2) {
                        screenState = 1;
                    }

                }
                if (screenState == 1 && menuState != 6 && menuState != 7 && squareX - 1 >= 0 && squareX - 1 < 7) {

                    if (Keyboard.getEventKey() == Keyboard.KEY_0) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 0;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_1) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 1;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_2) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 2;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_3) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 3;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_4) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 4;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_5) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 5;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_6) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 6;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_7) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 7;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_8) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 8;
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_9) {
                        neighbourhood[6 - squareY][squareX - 1][menuState] = 9;
                    }

                }
                if (screenState == 2) {

                    if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
                        

                        for (int yi = 0; yi < 600 / pixel; yi++) {

                            for (int xi = 0; xi < 800 / pixel; xi++) {

                                int switchColour = 0;

                                while (switchColour == 0 && (black==1 || white==1 || red==1 || blue==1 || green==1 || purple==1)) {
                                    currentRound = 0;
                                    state[xi][yi][currentRound] = (int) (newRandom.nextDouble() * 6) + 1;
                                    if ((black==1 && state[xi][yi][currentRound] == 1) || (white==1 && state[xi][yi][currentRound] == 2) || (red==1 && state[xi][yi][currentRound] == 3) || (blue==1 && state[xi][yi][currentRound] == 4) || (green==1 && state[xi][yi][currentRound] == 5) || (purple==1 && state[xi][yi][currentRound] == 6)) {
                                        switchColour = 1;
                                    }
                                }

                            }
                        }

                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
                        

                        for (int yi = 0; yi < 600 / pixel; yi++) {

                            for (int xi = 0; xi < 800 / pixel; xi++) {

                                int switchColour = 0;

                                while (switchColour == 0 && (black==2 || white==2 || red==2 || blue==2 || green==2 || purple==2)) {
                                    currentRound = 0;
                                    state[xi][yi][currentRound] = (int) (newRandom.nextDouble() * 6) + 1;
                                    if ((black==2 && state[xi][yi][currentRound] == 1) || (white==2 && state[xi][yi][currentRound] == 2) || (red==2 && state[xi][yi][currentRound] == 3) || (blue==2 && state[xi][yi][currentRound] == 4) || (green==2 && state[xi][yi][currentRound] == 5) || (purple==2 && state[xi][yi][currentRound] == 6)) {
                                        switchColour = 1;
                                    }
                                }

                            }
                        }
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_BACK) {
                        resetState();
                    }
                    if (Keyboard.getEventKey() == Keyboard.KEY_G) {
                        if (pixel >= 10) {
                            if (borderSize == 0) {
                                borderSize = 1;
                            } else {
                                borderSize = 0;
                            }
                        }
                    }
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_UP) && screenState == 2) {
            updateAlive();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && screenState == 2 && currentRound > 0) {
            currentRound--;
        }

        try {
            Mouse.setGrabbed(false);
            Mouse.create();
        } catch (LWJGLException e) { //Catches exception if game window is not created.
            e.printStackTrace();
            System.exit(0);
        }

        if (Mouse.isButtonDown(0) && !leftClick) {
            if (squareX == 0 && screenState == 1) {
                if (squareY == 0) {
                    menuState = 0;
                } else if (squareY == 1) {
                    menuState = 1;
                } else if (squareY == 2) {
                    menuState = 2;
                } else if (squareY == 3) {
                    menuState = 3;
                } else if (squareY == 4) {
                    menuState = 4;
                } else if (squareY == 5) {
                    menuState = 5;
                } else if (squareY == 6) {
                    menuState = 6;
                }
            } else if (squareX == 8 && screenState == 1) {
                if (squareY == 0) {
                    if (menuState == 6) {
                        pixel = 100;
                        resetState();
                    } else if (purple>=0){
                       if (purple==0 || purple==2)purple=1;
                       else if (purple==1)purple=0;
                    }
                } else if (squareY == 1) {
                    if (menuState == 6) {
                        pixel = 50;
                        resetState();
                    } else if (green>=0){
                       if (green==0 || green==2)green=1;
                       else if (green==1)green=0;
                    }
                } else if (squareY == 2) {
                    if (menuState == 6) {
                        pixel = 25;
                        resetState();
                    } else if (blue>=0){
                       if (blue==0 || blue==2)blue=1;
                       else if (blue==1)blue=0;
                    }
                } else if (squareY == 3) {
                    if (menuState == 6) {
                        pixel = 10;
                        resetState();
                    } else if (red>=0){
                       if (red==0 || red==2)red=1;
                       else if (red==1)red=0;
                    }
                } else if (squareY == 4) {
                    if (menuState == 6) {
                        pixel = 4;
                        resetState();
                    } else if (white>=0){
                       if (white==0 || white==2)white=1;
                       else if (white==1)white=0;
                    }
                } else if (squareY == 5) {
                    if (menuState == 6) {
                        pixel = 2;
                        resetState();
                    } else if (black>=0){
                       if (black==0 || black==2)black=1;
                       else if (black==1)black=0;
                    }
                } else if (squareY == 6) {
                    if (menuState == 6) {
                        pixel = 1;
                        resetState();
                    }
                }
            }
            leftClick = true;
        }

                if (Mouse.isButtonDown(1) && !rightClick) {
            if (squareX == 8 && screenState == 1 && menuState != 6) {
                if (squareY == 0) {
                       if (purple==0 || purple==1)purple=2;
                       else if (purple==2)purple=0;
                } else if (squareY == 1) {
                       if (green==0 || green==1)green=2;
                       else if (green==2)green=0;
                } else if (squareY == 2) {
                       if (blue==0 || blue==1)blue=2;
                       else if (blue==2)blue=0;
                } else if (squareY == 3) {
                       if (red==0 || red==1)red=2;
                       else if (red==2)red=0;
                } else if (squareY == 4) {
                       if (white==0 || white==1)white=2;
                       else if (white==2)white=0;
                } else if (squareY == 5) {
                       if (black==0 || black==1)black=2;
                       else if (black==2)black=0;
                } 
                
            }
            rightClick = true;
        }
        
        
        
        if (squareY == 6 && squareX == 8 && screenState == 1 && menuState != 6) {
            if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                brushRadius = 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                brushRadius = 2;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
                brushRadius = 3;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_4)) {
                brushRadius = 4;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_5)) {
                brushRadius = 5;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_6)) {
                brushRadius = 6;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_7)) {
                brushRadius = 7;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_8)) {
                brushRadius = 8;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_9)) {
                brushRadius = 9;
            }
        }

        if (!Mouse.isButtonDown(0)) {
            leftClick = false;
        }
        if (!Mouse.isButtonDown(1)) {
            rightClick = false;
        }

        int coordinateX = Mouse.getX();
        int coordinateY = 600 - (Mouse.getY());

        if (coordinateX < 800 && coordinateY < 600) {

            if (screenState == 2) {
                paintBucket(coordinateX, coordinateY);
            }

        }

    }

    public static void drawTitle(TrueTypeFont trueTypeTitleFont, TrueTypeFont trueTypePaintBrushFont, TrueTypeFont trueTypeDimensionsFont) {
        GL11.glEnable(GL11.GL_TEXTURE_2D); //Enables textures.

        trueTypeTitleFont.drawString((800 - trueTypeTitleFont.getWidth("Cellular Automata")) / 2, (600 - trueTypeTitleFont.getHeight("Cellular Automata")) / 2, "Cellular Automata", Color.white);

        trueTypeDimensionsFont.drawString(650, 570, "By Carson Craig", Color.white);
        
        GL11.glDisable(GL11.GL_TEXTURE_2D); //Disables textures.

    }

    public static void drawMenuLoop(TrueTypeFont trueTypeTitleFont, TrueTypeFont trueTypeDimensionsFont, TrueTypeFont trueTypePaintBrushFont) {

        if (menuState == 0) {
            drawWeight(7, 6, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 1) {
            drawWeight(7, 5, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 2) {
            drawWeight(7, 4, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 3) {
            drawWeight(7, 3, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 4) {
            drawWeight(7, 2, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 5) {
            drawWeight(0, 1, trueTypeTitleFont, trueTypePaintBrushFont);
        } else if (menuState == 6) {
            drawPath(trueTypeTitleFont, trueTypeDimensionsFont);
        }
    }

    public static void drawPath(TrueTypeFont trueTypeTitleFont, TrueTypeFont trueTypeDimensionsFont) {
        updateColour(7);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(712, 0);
        GL11.glVertex2f(712, 600);
        GL11.glVertex2f(0, 600);
        GL11.glEnd();

        for (int yi = 0; yi < 7; yi++) {
            updateColour(7);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(712, yi * 86);
            GL11.glVertex2f(800, yi * 86);
            GL11.glVertex2f(800, yi * 86 + 85);
            GL11.glVertex2f(712, yi * 86 + 85);
            GL11.glEnd();

        }

        for (int yi = 0; yi < 7; yi++) {

            for (int xi = 0; xi < 10; xi++) {

                if (yi == 0) {
                    updateColour(0);
                } else {
                    updateColour(path[yi - 1][xi]);
                }

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(xi * 62 + 90, yi * 86);
                GL11.glVertex2f(xi * 62 + 61 + 90, yi * 86);
                GL11.glVertex2f(xi * 62 + 61 + 90, yi * 86 + 85);
                GL11.glVertex2f(xi * 62 + 90, yi * 86 + 85);
                GL11.glEnd();
            }
        }

        for (int yi = 0; yi < 7; yi++) {

            for (int xi = 0; xi < 9; xi = xi + 8) {

                updateColour(yi);

                if (xi == 0) {
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex2f(0, yi * 86);
                    GL11.glVertex2f(89, yi * 86);
                    GL11.glVertex2f(89, yi * 86 + 85);
                    GL11.glVertex2f(0, yi * 86 + 85);
                    GL11.glEnd();
                }

            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D); //Enables textures.

        for (int i = 0; i < 9; i++) {
            trueTypeTitleFont.drawString(110 + 62 * i, 20, i + 1 + "", Color.black);
        }
        trueTypeTitleFont.drawString(668, 20, "0", Color.black);

        String dimensions = "";
        boolean dimensionsSelected = false;

        for (int yi = 0; yi < 7; yi++) {
            if (yi == 0) {
                dimensions = "800 x 600";
                if (pixel == 1) {
                    dimensionsSelected = true;
                    borderSize = 0;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 1) {
                dimensions = "400 x 300";
                if (pixel == 2) {
                    dimensionsSelected = true;
                    borderSize = 0;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 2) {
                dimensions = "200 x 150";
                if (pixel == 4) {
                    dimensionsSelected = true;
                    borderSize = 0;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 3) {
                dimensions = "  80 x 60";
                if (pixel == 10) {
                    dimensionsSelected = true;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 4) {
                dimensions = "  32 x 24";
                if (pixel == 25) {
                    dimensionsSelected = true;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 5) {
                dimensions = "  16 x 12";
                if (pixel == 50) {
                    dimensionsSelected = true;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (yi == 6) {
                dimensions = "    8 x 6";
                if (pixel == 100) {
                    dimensionsSelected = true;
                } else {
                    dimensionsSelected = false;
                }
            }
            if (dimensionsSelected) {
                trueTypeDimensionsFont.drawString(713, 28 + 86 * yi, dimensions, Color.yellow);
            } else {
                trueTypeDimensionsFont.drawString(713, 28 + 86 * yi, dimensions, Color.white);
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);

    }

    public static void drawWeight(int background, int squares, TrueTypeFont trueTypeTitleFont, TrueTypeFont trueTypePaintBrushFont) {

        updateColour(background);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(711, 0);
        GL11.glVertex2f(711, 600);
        GL11.glVertex2f(0, 600);
        GL11.glEnd();

        Color.black.bind();

        for (int yi = 0; yi < 7; yi++) {

            for (int xi = 0; xi < 9; xi++) {

                updateColour(squares);

                if (xi == 0) {
                    updateColour(yi);
                }

                if (xi == 8) {
                    updateColour(0);
                }

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(xi * 89, yi * 86);
                GL11.glVertex2f(xi * 89 + 88, yi * 86);
                GL11.glVertex2f(xi * 89 + 88, yi * 86 + 85);
                GL11.glVertex2f(xi * 89, yi * 86 + 85);
                GL11.glEnd();

            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D); //Enables textures.

        for (int yi = 0; yi < 7; yi++) {
            for (int xi = 0; xi < 7; xi++) {
                if (xi != 3 || yi != 3) {
                    if (squares == 1 || squares == 6) {
                        trueTypeTitleFont.drawString(120 + 89 * xi, 20 + 86 * yi, neighbourhood[yi][xi][menuState] + "", Color.white);
                    } else {
                        trueTypeTitleFont.drawString(120 + 89 * xi, 20 + 86 * yi, neighbourhood[yi][xi][menuState] + "", Color.black);
                    }
                }
            }
        }

        String stateString = "";

        for (int yi = 1; yi < 7; yi++) {

            if (yi == 1) {

                if (black== 1) {
                    stateString = " Left";
                } else if (black == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 25 + 86 * yi, stateString, Color.black);
            }
            if (yi == 2) {

                if (white==1) {
                    stateString = " Left";
                } else if (white == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 25 + 86 * yi, stateString, Color.white);
            }
            if (yi == 3) {

                if (red==1) {
                    stateString = " Left";
                } else if (red == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 25 + 86 * yi, stateString, Color.red);
            }
            if (yi == 4) {

                if (blue==1) {
                    stateString = " Left";
                } else if (blue == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 25 + 86 * yi, stateString, Color.blue);
            }
            if (yi == 5) {

                if (green==1) {
                    stateString = " Left";
                } else if (green == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 25 + 86 * yi, stateString, Color.green.darker(.7f));
            }
            if (yi == 6) {

                if (purple==1) {
                    stateString = " Left";
                } else if (purple == 2)
                    stateString = "Right";
                else {
                    stateString = "  Off";
                }
                trueTypePaintBrushFont.drawString(720, 20 + 86 * yi, stateString, Color.magenta.darker(.7f));
            }

        }

        if (screenState == 1 && menuState != 6) {
            trueTypeTitleFont.drawString(745, 20, brushRadius + "", Color.black);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public static void drawBoard() {

        updateColour(0);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(800, 0);
        GL11.glVertex2f(800, 600);
        GL11.glVertex2f(0, 600);
        GL11.glEnd();

        for (int yi = 0; yi < 600 / pixel; yi++) {

            for (int xi = 0; xi < 800 / pixel; xi++) {

                updateColour(state[xi][yi][currentRound]);

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(xi * pixel + borderSize, yi * pixel + borderSize);
                GL11.glVertex2f(xi * pixel + pixel - borderSize, yi * pixel + borderSize);
                GL11.glVertex2f(xi * pixel + pixel - borderSize, yi * pixel + pixel - borderSize);
                GL11.glVertex2f(xi * pixel + borderSize, yi * pixel + pixel - borderSize);
                GL11.glEnd();

            }
        }
    }

    public static void updateAlive() {

        if (currentRound >= rounds * pixel * pixel - 1) {
            for (int yi = 0; yi < 600 / pixel; yi++) {

                for (int xi = 0; xi < 800 / pixel; xi++) {
                    state[xi][yi][0] = state[xi][yi][currentRound];

                }
            }
            currentRound = 0;
        }

        for (int yi = 0; yi < 600 / pixel; yi++) {

            for (int xi = 0; xi < 800 / pixel; xi++) {

                int alive = 0;

                for (int yi2 = -3; yi2 <= 3; yi2++) {

                    for (int xi2 = -3; xi2 <= 3; xi2++) {

                        if (xi + xi2 >= 0 && yi + yi2 >= 0 && xi + xi2 < 800 / pixel && yi + yi2 < 600 / pixel) {
                            alive = alive + neighbourhood[yi2 + 3][xi2 + 3][6 - state[xi + xi2][yi + yi2][currentRound]];

                        }
                    }

                }
                if (alive % 10 == 0) {
                    state[xi][yi][currentRound + 1] = path[state[xi][yi][currentRound] - 1][9];
                } else {
                    state[xi][yi][currentRound + 1] = path[state[xi][yi][currentRound] - 1][(alive % 10) - 1];
                }

            }

        }
        currentRound++;

    }

    public static void updateColour(int index) {
        if (index == 0) {
            Color.gray.bind();
        } else if (index == 1) {
            Color.black.bind();
        } else if (index == 2) {
            Color.white.bind();
        } else if (index == 3) {
            Color.red.bind();
        } else if (index == 4) {
            Color.blue.bind();
        } else if (index == 5) {
            Color.green.darker().bind();
        } else if (index == 6) {
            Color.magenta.darker(.7f).bind();
        } else if (index == 7) {
            Color.darkGray.darker(.7f).bind();
        }

    }

    public static void paintBucket(int coordinateX, int coordinateY) {
        int switchColour;

        if (Mouse.isButtonDown(0)) {
            for (int yi = 0; yi < 600 / pixel; yi++) {

                for (int xi = 0; xi < 800 / pixel; xi++) {

                    state[xi][yi][0] = state[xi][yi][currentRound];

                }
            }
            currentRound = 0;

            for (int yi = -(brushRadius - 1); yi <= (brushRadius - 1); yi++) {

                for (int xi = -(brushRadius - 1); xi <= (brushRadius - 1); xi++) {
                    switchColour = 0;
                    if ((int) Math.ceil(coordinateX / pixel) + xi >= 0 && (int) Math.ceil(coordinateY / pixel) + yi >= 0 && (int) Math.ceil(coordinateX / pixel) + xi < 800 / pixel && (int) Math.ceil(coordinateY / pixel) + yi < 600 / pixel) {
                        while (switchColour == 0 && (black==1 || white ==1|| red==1 || blue==1 || green==1 || purple==1)) {
                            state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] = (int) (newRandom.nextDouble() * 6) + 1;
                            if ((black==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 1) || (white==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 2) || (red==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 3) || (blue==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 4) || (green==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 5) || (purple==1 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 6)) {
                                switchColour = 1;
                            }
                        }

                    }
                }
            }

        }
        if (Mouse.isButtonDown(1)) {
            for (int yi = 0; yi < 600 / pixel; yi++) {

                for (int xi = 0; xi < 800 / pixel; xi++) {

                    state[xi][yi][0] = state[xi][yi][currentRound];

                }
            }
            currentRound = 0;

            for (int yi = -(brushRadius - 1); yi <= (brushRadius - 1); yi++) {

                for (int xi = -(brushRadius - 1); xi <= (brushRadius - 1); xi++) {
                    switchColour = 0;
                    if ((int) Math.ceil(coordinateX / pixel) + xi >= 0 && (int) Math.ceil(coordinateY / pixel) + yi >= 0 && (int) Math.ceil(coordinateX / pixel) + xi < 800 / pixel && (int) Math.ceil(coordinateY / pixel) + yi < 600 / pixel) {
                        while (switchColour == 0 && (black==2 || white==2 || red==2 || blue==2 || green==2 || purple==2)) {
                            state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] = (int) (newRandom.nextDouble() * 6) + 1;
                            if ((black==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 1) || (white==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 2) || (red==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 3) || (blue==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 4) || (green==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 5) || (purple==2 && state[(int) Math.ceil(coordinateX / pixel) + xi][(int) Math.ceil(coordinateY / pixel) + yi][currentRound] == 6)) {
                                switchColour = 1;
                            }
                        }
                    }
                }
            }
        }

    }

    public static void resetState() {
        state = new int[800 / pixel][600 / pixel][rounds * pixel * pixel];

        currentRound = 0;

        for (int yi = 0; yi < 600 / pixel; yi++) {

            for (int xi = 0; xi < 800 / pixel; xi++) {

                state[xi][yi][0] = 1;
            }
        }
    }

}
