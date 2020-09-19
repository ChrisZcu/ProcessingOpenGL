#version 330

uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;

in vec3 vertexPos;
in vec2 texCoord;
in vec3 vertexNormal;

out vec3 mvVertexPos;
out vec2 outTexCoord;
out vec3 mvVertexNormal;

void main(void) {
	vec4 mvPos = modelviewMatrix * vec4(vertexPos, 1.0);    
	gl_Position = projectionMatrix * mvPos;
    
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelviewMatrix * vec4(vertexNormal, 0.0)).xyz;
	mvVertexPos = mvPos.xyz;
}
