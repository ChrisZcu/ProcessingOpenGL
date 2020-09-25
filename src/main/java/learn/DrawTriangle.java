package learn;

import com.jogamp.opengl.GL3;
import processing.core.PApplet;
import processing.opengl.PJOGL;

public class DrawTriangle extends PApplet {
    private GL3 gl3;

    private float[] vertices = {
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            0f, 0.5f, 0f
    };

    @Override
    public void settings() {
        size(1000, 800, P2D);
        PJOGL.profile = 3;
    }

    @Override
    public void setup() {
        gl3 = ((PJOGL) beginPGL()).gl.getGL3();
        endPGL();//?

        initShader();
    }

    private void initShader() {
        int[] vboHandles = new int[1];
        gl3.glGenBuffers(1, vboHandles, 0);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);

    }

    @Override
    public void draw() {

        noLoop();
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{DrawTriangle.class.getName()});
    }
}