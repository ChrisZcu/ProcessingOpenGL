#version 410

in vec3 vertexPos;
in vec4 vertexColor;

uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;

out vec3 EntryPoint;

void main(void) {
	gl_Position = projectionMatrix * modelviewMatrix * vec4(vertexPos, 1.0);
    
    EntryPoint = vertexColor.xyz;
}
