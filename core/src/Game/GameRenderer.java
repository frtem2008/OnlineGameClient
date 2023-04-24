package Game;

import ResourceManager.ResourceManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.io.Closeable;

public class GameRenderer implements Closeable {
    private Game toRender;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private SpriteBatch batcher;

    private double fps;
    private long lastFpsMeasureTime;
    private final long fpsMeasureDeltaTimeMillis = 1000;

    public GameRenderer(Game game, float screenWidth, float screenHeight) {
        this.toRender = game;

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(true, screenWidth, screenHeight);

        this.batcher = new SpriteBatch();
        this.batcher.setProjectionMatrix(camera.combined);

        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setProjectionMatrix(camera.combined);

        this.lastFpsMeasureTime = System.currentTimeMillis();
        this.fps = Gdx.graphics.getFramesPerSecond();

        //load resources
        ResourceManager.loadAll();
    }

    public void setToRender(Game toRender) {
        this.toRender = toRender;
    }

    public void render() {
        Gdx.gl.glClearColor(0.30f, 0.47f, 0.8f, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        toRender.getGameObjects().forEach((s, gameObject) -> gameObject.draw(batcher));

        if (System.currentTimeMillis() - lastFpsMeasureTime > fpsMeasureDeltaTimeMillis) {
            lastFpsMeasureTime = System.currentTimeMillis();
            fps = Gdx.graphics.getFramesPerSecond();
            Gdx.app.log("Renderer", "Game fps: " + fps);
        }
    }

    @Override
    public void close() {
        batcher.dispose();
        shapeRenderer.dispose();
        ResourceManager.disposeAll();
    }
}
