package com.livefish;

import GameStarter.SoulKnightGame;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        //config.setForegroundFPS(144);
        config.setForegroundFPS(0);
        config.setWindowedMode(800, 480);
        config.setWindowIcon("bucket.png"); // TODO: 23.04.2023 NORMAL ICON
        config.setTitle("Soul knight client alpha");
        config.enableGLDebugOutput(true, System.out);
        new Lwjgl3Application(new SoulKnightGame(), config);
    }
}
