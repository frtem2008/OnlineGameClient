package ResourceManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static Map<String, Texture> textures;
    private static Map<String, Sound> sounds;
    private static Map<String, Music> music;

    public static void init() {
    }

    private static Texture loadTexture(String filePath) {
        return new Texture(Gdx.files.internal(filePath));
    }

    private static Sound loadSound(String filePath) {
        return Gdx.audio.newSound(Gdx.files.internal(filePath));
    }

    private static Music loadMusic(String filePath) {
        return Gdx.audio.newMusic(Gdx.files.internal(filePath));
    }

    public static void addTexture(String name, String filePath) {
        textures.put(name, loadTexture(filePath));
    }

    public static void addSound(String name, String filePath) {
        sounds.put(name, loadSound(filePath));
    }

    public static void addMusic(String name, String filePath) {
        music.put(name, loadMusic(filePath));
    }

    public static Texture getTexByName(String texName) {
        return textures.get(texName);
    }

    public static Sound getSoundByName(String soundName) {
        return sounds.get(soundName);
    }

    public static Music getMusicByName(String musicName) {
        return music.get(musicName);
    }

    public static void loadTextures() {
        textures = new HashMap<>();

        /* Load some textures */
        addTexture("Player", "bucket.png");
        addTexture("Block", "tile.png");
    }

    public static void loadSounds() {
        sounds = new HashMap<>();

        /* Load some sounds */
        addSound("drop", "drop.wav");
    }

    public static void loadMusic() {
        music = new HashMap<>();
        /* Load some music */
        addMusic("Background", "Close to you.mp3");
    }

    public static void loadAll() {
        loadTextures();
        loadSounds();
        loadMusic();
    }

    public static void disposeAll() {
        music.forEach((s, music1) -> music1.dispose());
        sounds.forEach((s, sound) -> sound.dispose());
        textures.forEach((s, texture) -> texture.dispose());
    }
}
