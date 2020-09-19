#version 410

in vec3 vertexPos;
in vec4 vertexColor;
in vec2 texcoord;

uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;

out vec2 texture_coord;

void main(void) {
	gl_Position = projectionMatrix * modelviewMatrix * vec4(vertexPos, 1.0);
	texture_coord = texcoord;
}
