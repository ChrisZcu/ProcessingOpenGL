#version 330

in vec4 vsColor;

uniform bool omitVertex;
uniform float omitDist;
in float distToCamera;

out vec4 outColor;

void main(void) {
//    if(omitVertex && distToCamera > omitDist)
//        outColor = vec4(1.0, 1.0, 1.0, 0.0);
//    else
        outColor = vsColor;
}
