package app;

import processing.core.PApplet;
import processing.opengl.PShader;

public class ExampleTry extends PApplet {
    PShader toon;

    public void settings() {
        size(640, 360);
    }

    public void setup() {
        background(102);
        noStroke();
        fill(204);
        toon = loadShader("ToonFrag.glsl", "ToonVert.glsl");
    }

    public void draw() {
        shader(toon);
        background(0);
        float dirY = (float) ((mouseY / (height) - 0.5) * 2);
        float dirX = (float) ((mouseX / (width) - 0.5) * 2);
        directionalLight(204, 204, 204, -dirX, -dirY, -1);
        translate(width/2, height/2);
        sphere(120);
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{ExampleTry.class.getName()});
    }
}
