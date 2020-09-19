#version 410

in vec2 texture_coord;

out vec4 outColor;

uniform sampler2D texImage;
uniform vec3 colormap[256];
uniform float max_color;
uniform float min_color;
uniform int color_interpolation;

void main(void) {
    vec4 value = texture(texImage, texture_coord);
    
    float v = value.x;
    
    // get color
    float color_index = 0.0;
    float alpha = 1;
    if(v <= min_color){
        color_index = 0.0;
        alpha = 0;
    } else if(v >= max_color)
        color_index = 255.0;
    else{
        if(color_interpolation == 0){ // linear
            color_index = (v - min_color)  * 255.0 / (max_color - min_color);
            alpha = (v - min_color)  * 1.0 / (max_color - min_color);
        }
        else{
            if(color_interpolation == 1){ // log1p
                color_index = (log(v+1) - log(min_color+1)) / (log(max_color+1) - log(min_color+1)) * 255;
                alpha = (log(v+1) - log(min_color+1)) / (log(max_color+1) - log(min_color+1));
            } else if(color_interpolation == 2){ // log2-1p
                color_index = (log2(v+1) - log2(min_color+1)) / (log2(max_color+1) - log2(min_color+1)) * 255;
                alpha = (log2(v+1) - log2(min_color+1)) / (log2(max_color+1) - log2(min_color+1));
            }
        }
    }
    
    if(v == 0)
    outColor = vec4(colormap[int(color_index)], 0);
    else
    outColor = vec4(colormap[int(color_index)], 1);
}
