#version 150

uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;

uniform bool perVertexColor;

uniform vec4 uniformColor;

in vec3 vertexPos;
in vec4 vertexColor;

out Vertex{
    vec4 vsColor;
    float cameraDist;
} vertex;

void main(void) {
	if (perVertexColor)
		vertex.vsColor = vertexColor;
    else
        vertex.vsColor = uniformColor;
    
    vec4 cameraPos = modelviewMatrix * vec4(vertexPos, 1.0);
    vertex.cameraDist = abs(cameraPos.z);
    
	gl_Position = projectionMatrix * cameraPos;
}
