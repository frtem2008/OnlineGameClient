package Game;

import GameObjects.Base.GameObject;
import GameObjects.GameObjectsCreateReadFunctionsTable;
import GameObjects.Base.GameObjectType;
import GameObjects.Player;
import Online.Base.ReadFunctions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Externalizable {
    private final ConcurrentHashMap<String, GameObject> gameObjects;
    private final ConcurrentHashMap<String, GameObject> gameObjectsToDelete;
    private final ConcurrentHashMap<String, Player> players;

    public Game() {
        this.players = new ConcurrentHashMap<>();
        this.gameObjects = new ConcurrentHashMap<>();
        this.gameObjectsToDelete = new ConcurrentHashMap<>();
    }

    private void deleteMarkedGameObjects() {
        for (GameObject toDelete : gameObjectsToDelete.values()) {
            deleteGameObject(toDelete);
        }
        gameObjectsToDelete.clear();
    }

    private void deleteGameObject(GameObject toDelete) {
        toDelete.markedForDelete = true;
        gameObjects.remove(toDelete.getUUID());
        if (toDelete instanceof Player)
            players.remove(((Player) toDelete).name);
    }
    public void resolveUpdate(Game newGame) {
        // remove all deleted game objects
        // add new and update existing game objects
        for (GameObject newGameObject : newGame.gameObjects.values()) {
            if (newGameObject.markedForDelete)
                deleteGameObject(newGameObject);
            else
                gameObjects.put(newGameObject.getUUID(), newGameObject);
        }
        // instantly delete all unneeded game objects
        deleteMarkedGameObjects();
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameObjects=" + gameObjects + "\n" +
                "players=" + players + "\n" +
                '}';
    }

    private void sendObjectMap(ConcurrentHashMap<String, GameObject> gameObjects, ObjectOutput out) throws IOException {
        for (GameObject gameObject : gameObjects.values()) {
            out.writeUTF(gameObject.type.toString());
            gameObject.writeExternal(out);
        }
    }


    private Externalizable readGameObject(ObjectInput in) throws IOException {
        String gameObjectType = in.readUTF();
        GameObjectType type = GameObjectType.valueOf(gameObjectType);

        ReadFunctions functions = GameObjectsCreateReadFunctionsTable.gameObjectFunctionsMap.get(type.gameObjectClass);
        Externalizable received;
        if (functions != null) {
            try {
                received = functions.constructor().newInstance();
                functions.readMethod().invoke(received, in);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException("No read functions associated with class: " + gameObjectType);
        }
        return received;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new IllegalStateException("Can not write a game to server!");
    }

    public void writeFully(ObjectOutput out) throws IOException {
        throw new IllegalStateException("Can not write a game to server!");
    }

    public ConcurrentHashMap<String, GameObject> getGameObjects() {
        return gameObjects;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        gameObjects.clear();
        long size = in.readLong();
        for (int i = 0; i < size; i++) {
            Externalizable received = readGameObject(in);
            if (received instanceof Player)
                players.put(((Player) received).name, (Player) received);
            if (received instanceof GameObject) {
                gameObjects.put(((GameObject) received).getUUID(), (GameObject) received);
            } else
                throw new IllegalStateException("Received not a game object: " + received);
        }
    }
}
