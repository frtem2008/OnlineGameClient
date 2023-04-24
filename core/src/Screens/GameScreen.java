package Screens;

import Game.GameRenderer;
import Game.GameUpdater;
import Game.Game;
import Userinput.Keyboard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {
    private GameRenderer gameRenderer;
    private GameUpdater gameUpdater;
    private Game game;

    public GameScreen() {
        Gdx.app.log("Gamescreen", "Attached");
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();


        game = new Game();
        gameRenderer = new GameRenderer(game, screenWidth, screenHeight);
        gameUpdater = new GameUpdater(game, gameRenderer);

        // TODO: 24.04.2023 do normal keyboard
        Gdx.input.setInputProcessor(new Keyboard());
    }

    @Override
    public void render(float delta) {
        gameRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GameScreen", "resizing");
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "show called");
    }

    @Override
    public void hide() {
        Gdx.app.log("GameScreen", "hide called");
        dispose();
    }

    @Override
    public void pause() {
        Gdx.app.log("GameScreen", "pause called");
    }

    @Override
    public void resume() {
        Gdx.app.log("GameScreen", "resume called");
    }

    @Override
    public void dispose() {
        gameUpdater.close();
        gameRenderer.close();
    }
}
