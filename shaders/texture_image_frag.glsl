#version 330

in vec2 texture_coord;

out vec4 outColor;

uniform sampler2D texImage;

void main(void) {
	outColor = texture(texImage, texture_coord);
}
