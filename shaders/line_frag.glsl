#version 150

in VertexColor {
    noperspective vec4 color;
} vVertexIn;

out vec4 outColor;

void main(void) {
    outColor = vVertexIn.color;
}
