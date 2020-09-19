#version 150

layout (lines_adjacency) in;
layout (triangle_strip, max_vertices = 4) out;

uniform vec2 viewport;
uniform float miter_limit;	// 1.0: always miter, -1.0: never miter, 0.75: default
uniform float lineWidth;

in Vertex{
    vec4 vsColor;
    float cameraDist;
} vertex[];

out VertexColor{
    noperspective vec4 color;
} vertexOut;

vec2 screen_space(vec4 vertex){
    return vec2(vertex.xy / vertex.w) * viewport;
}

void main(void){
    if(vertex[1].vsColor.w == -1) return;
    if(vertex[2].vsColor.w == -1) return;
    
    vec2 p0 = screen_space(gl_in[0].gl_Position);
    vec2 p1 = screen_space(gl_in[1].gl_Position);
    vec2 p2 = screen_space(gl_in[2].gl_Position);
    vec2 p3 = screen_space(gl_in[3].gl_Position);
    
    // perform naive culling
    
    vec2 area = viewport * 2;
    if((p1.x < -area.x || p1.x > area.x || p1.y < -area.y || p1.y > area.y)
       && (p2.x < -area.x || p2.x > area.x || p2.y < -area.y || p2.y > area.y)) return;
    
    float aveCameraDist = vertex[1].cameraDist / 2 + vertex[2].cameraDist / 2;
    
    float width = lineWidth;
    if(width < 1){
        if(aveCameraDist > 0.7)
            width = 1;
        else
            width = min(6, 2 / aveCameraDist);
    }
    
    // direction of the line segments
    vec2 v0 = normalize(p1 - p0);
    vec2 v1 = normalize(p2 - p1);
    vec2 v2 = normalize(p3 - p2);
    
    // normal of the line segments
    vec2 n0 = vec2(-v0.y, v0.x);
    vec2 n1 = vec2(-v1.y, v1.x);
    vec2 n2 = vec2(-v2.y, v2.x);
    
    // miter lines
    vec2 miter_a = normalize(n0 + n1);
    vec2 miter_b = normalize(n1 + n2);
    
    // lengths of the miter
    float length_a = width / 2 / dot(miter_a, n1);
    float length_b = width / 2 / dot(miter_b, n1);
    
    // prevent excessively long miters at sharp corners
    if(dot(v0, v1) < -miter_limit){
        miter_a = n1;
        length_a = width / 2;
//
//        if(dot(v0, n1) > 0){
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4((p1 + width / 2 * n0) / viewport, 0.0, 1.0);
//            EmitVertex();
//            
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4((p1 + width / 2 * n1) / viewport, 0.0, 1.0);
//            EmitVertex();
//            
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4(p1 / viewport, 0.0, 1.0);
//            EmitVertex();
//            EndPrimitive();
//        } else {
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4((p1 - width / 2 * n0) / viewport, 0.0, 1.0);
//            EmitVertex();
//            
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4((p1 - width / 2 * n1) / viewport, 0.0, 1.0);
//            EmitVertex();
//            
//            vertexOut.color = vertex[1].vsColor;
//            gl_Position = vec4(p1 / viewport, 0.0, 1.0);
//            EmitVertex();
//            EndPrimitive();
//        }
    }
    
    if(dot(v1, v2) < -miter_limit){
        miter_b = n1;
        length_b = width / 2;
    }
    
    vertexOut.color = vertex[1].vsColor;
    gl_Position = vec4((p1 + length_a * miter_a) / viewport, 0.0, 1.0);
    EmitVertex();
    
    vertexOut.color = vertex[1].vsColor;
    gl_Position = vec4((p1 - length_a * miter_a) / viewport, 0.0, 1.0);
    EmitVertex();
    
    vertexOut.color = vertex[2].vsColor;
    gl_Position = vec4((p2 + length_b * miter_b) / viewport, 0.0, 1.0);
    EmitVertex();
    
    vertexOut.color = vertex[2].vsColor;
    gl_Position = vec4((p2 - length_b * miter_b) / viewport, 0.0, 1.0);
    EmitVertex();
    EndPrimitive();
}
