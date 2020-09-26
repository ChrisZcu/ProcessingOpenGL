package learn;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import processing.core.PApplet;
import processing.opengl.PJOGL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class DrawTriangle extends PApplet {
    private GL3 gl3;
    private int shaderProgram;
    private int vertShader, fragShader;

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
        sendData();
    }

    private void initShader() {
        shaderProgram = gl3.glCreateProgram();

        vertShader = gl3.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl3.glShaderSource(vertShader, 1,
                new String[]{
                        "#version 330 core\n"
                                + "layout (location = 0) in vec4 position;"
                                + "uniform vec4 my_color_in;"
                                + "out vec4 my_color;"
                                + "void main() {"
                                + "    gl_Position = vec4(position.x, position.y, position.z, 1.0);"
                                + "    my_color = my_color_in;"
                                + "}"
                }, null);
        gl3.glCompileShader(vertShader);

        fragShader = gl3.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        gl3.glShaderSource(fragShader, 1,
                new String[]{
                        "#version 330 core\n" +
                                "in vec4 my_color;" +
                                "void main() {" +
                                "    gl_FragColor = my_color;" +
                                "}"
                }, null);
        gl3.glCompileShader(fragShader);

        int myColorInLocation = gl3.glGetUniformLocation(vertShader, "my_color_in");
        System.out.println(myColorInLocation);
        gl3.glUniform4f(myColorInLocation, 1f, 0f, 0f, 1f);

        // attach and link
        gl3.glAttachShader(shaderProgram, vertShader);
        gl3.glAttachShader(shaderProgram, fragShader);
        gl3.glLinkProgram(shaderProgram);

        // program compiled we can free the object
        gl3.glDeleteShader(vertShader);
        gl3.glDeleteShader(fragShader);
    }

    public void sendData() {
        FloatBuffer vertexDataBuffer = GLBuffers.newDirectFloatBuffer(vertices);

        int[] vboHandles = new int[1];
        gl3.glGenBuffers(1, vboHandles, 0);

        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboHandles[0]);
        gl3.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * 3, vertexDataBuffer, GL3.GL_STATIC_DRAW);

        IntBuffer vao = GLBuffers.newDirectIntBuffer(1);
        gl3.glGenVertexArrays(1, vao);
        gl3.glBindVertexArray(vao.get(0));

        gl3.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 0, 0);
        gl3.glEnableVertexAttribArray(0);
    }

    @Override
    public void draw() {
        gl3.glUseProgram(shaderProgram);
        gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
        noLoop();
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{DrawTriangle.class.getName()});
    }
}