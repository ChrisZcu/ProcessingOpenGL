#version 330

uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;

uniform bool perVertexColor;
uniform float pointSize;

uniform vec4 uniformColor;
in vec3 vertexPos;
in vec4 vertexColor;

out vec4 vsColor;
out float distToCamera;

void main(void) {
	if (perVertexColor)
		vsColor = vertexColor;
    else
        vsColor = uniformColor;

    vec4 cameraPos = modelviewMatrix * vec4(vertexPos, 1.0);
    distToCamera = -cameraPos.z;
    
	gl_PointSize = pointSize;
	gl_Position = projectionMatrix * cameraPos;
}
