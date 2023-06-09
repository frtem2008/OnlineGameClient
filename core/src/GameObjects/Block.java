package GameObjects;

import GameObjects.Base.GameObject;
import GameObjects.Base.GameObjectType;
import ResourceManager.ResourceManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Block extends GameObject {
    private Color color;

    public Block() {
        super();
        this.type = GameObjectType.BLOCK;
    }

    @Override
    public boolean differsFrom(GameObject gameObject) {
        return super.differsFrom(gameObject) || !Objects.equals(this.color, ((Block) gameObject).color);
    }

    public Block(double x, double y, double w, double h, Color color) {
        super(x, y, w, h);
        this.type = GameObjectType.BLOCK;
        this.color = color;
    }

    @Override
    public void draw(SpriteBatch batcher) {
        batcher.begin();
        batcher.draw(ResourceManager.getTexByName("Block"), (float) x, (float) y, (float) w, (float) h);
        batcher.end();
    }

    @Override
    public void tick(double deltaTime, ConcurrentHashMap<String, GameObject> gameObjects) {
        // do nothing
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(color.getRGB());
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        color = new Color((int) in.readLong());
    }
}
