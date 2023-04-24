package GameStarter;

import Screens.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class SoulKnightGame extends Game {
    @Override
    public void create() {
        Gdx.app.log("SoulKnightGame", "created");
        setScreen(new GameScreen());
    }
}
