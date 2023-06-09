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

public class Player extends GameObject {
    private double speedX, speedY;
    public Color color;
    public String name;

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }


    public Player() {
        super();
        type = GameObjectType.PLAYER;
        x = y = w = h = -1;
        name = null;
        color = null;
    }

    public Player(String name, double x, double y, long w, long h, Color color) {
        super(x, y, w, h);
        this.type = GameObjectType.PLAYER;
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name=" + name +
                "speedX=" + speedX +
                ", speedY=" + speedY +
                ", color=" + color +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }


    @Override
    public void tick(double deltaTime, ConcurrentHashMap<String, GameObject> gameObjects) {
        x += speedX * deltaTime;
        y += speedY * deltaTime;
    }

    @Override
    public void draw(SpriteBatch batcher) {
        batcher.begin();
        batcher.draw(ResourceManager.getTexByName("Player"), (float) x, (float) y, (float) w, (float) h);
        batcher.end();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        assert name != null;
        out.writeUTF(name);
        super.writeExternal(out);
        out.writeLong(color.getRGB());
        out.writeDouble(speedX);
        out.writeDouble(speedY);
        out.flush();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        super.readExternal(in);
        color = new Color((int) in.readLong());
        speedX = in.readDouble();
        speedY = in.readDouble();
    }

    @Override
    public boolean differsFrom(GameObject gameObject) {
        return super.differsFrom(gameObject) ||
                this.speedX != ((Player) gameObject).speedX ||
                this.speedY != ((Player) gameObject).speedY ||
                !Objects.equals(this.color, ((Player) gameObject).color);
    }
}
