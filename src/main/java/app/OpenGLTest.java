package app;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import org.lwjgl.vulkan.VkRect2D;
import processing.core.PApplet;
import processing.opengl.PGL;
import processing.opengl.PJOGL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class OpenGLTest extends PApplet {
    private float[] vertexData = {
            -0.5f, 1f,
            -0.5f, 0f
    };

    GL3 gl3;
    private int shaderProgram;
    private int fragShader;
    private int vertexShader;
    int[] vboHandles;
    private IntBuffer vao = GLBuffers.newDirectIntBuffer(1);

    @Override
    public void settings() {
        size(1000, 800, P2D);
        PJOGL.profile = 3;
    }

    @Override
    public void setup() {
        gl3 = ((PJOGL) beginPGL()).gl.getGL3();
        endPGL();
        shaderProgram = gl3.glCreateProgram();//创建shaderprogram

        // fragment shader
        fragShader = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        gl3.glShaderSource(fragShader, 1, new String[]{
                "#ifdef GL_ES\n" +
                        "precision mediump float;\n" +
                        "precision mediump int;\n" +
                        "#endif\n" +
                        "\n" +
                        "varying vec4 vertColor;\n" +
                        "\n" +
                        "void main() {\n" +
                        "  gl_FragColor = vertColor;\n" +
                        "}"
        }, null);
        gl3.glCompileShader(fragShader);

        vertexShader = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl3.glShaderSource(vertexShader, 1, new String[]{
                "#version 330 \n"
                        + "layout (location = 0) in vec4 position;"
                        + "layout (location = 1) in vec4 color;"
                        + "smooth out vec4 theColor;"
                        + "void main(){"
                        + "gl_Position = position;"
                        + "theColor = color;"
                        + "}"
        }, null);
        gl3.glCompileShader(vertexShader);

        gl3.glAttachShader(shaderProgram, vertexShader);
        gl3.glAttachShader(shaderProgram, fragShader);
        gl3.glLinkProgram(shaderProgram);

        gl3.glDeleteShader(vertexShader);
        gl3.glDeleteShader(fragShader);

        //buffer init
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        vboHandles = new int[1];
        gl3.glGenBuffers(1, vboHandles, 0);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
        gl3.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GL3.GL_STATIC_DRAW);
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

        gl3.glGenVertexArrays(1, vao);
        gl3.glBindVertexArray(vao.get(0));
    }


    @Override
    public void draw() {
        gl3.glUseProgram(shaderProgram);

        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
        gl3.glEnableVertexAttribArray(0);
        gl3.glEnableVertexAttribArray(1);
        gl3.glVertexAttribPointer(0, 2, GL3.GL_FLOAT, false, 0, 0);
        gl3.glVertexAttribPointer(1, 2, GL3.GL_FLOAT, false, 0, 0);

        gl3.glDrawArrays(GL3.GL_LINES, 0, vertexData.length / 2);
        gl3.glDisableVertexAttribArray(0);
        gl3.glDisableVertexAttribArray(1);

    }

    public static void main(String[] args) {
        PApplet.main(new String[]{OpenGLTest.class.getName()});
    }
}
