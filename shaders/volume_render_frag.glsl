#version 410

out vec4 outColor;

in vec3 EntryPoint;

uniform float StepSize = 0.001;
uniform int cycle = 1600;
uniform int windowWidth = 1024;
uniform int windowCenter = 1024 / 2;

uniform vec2 FBOSize;
uniform sampler2D ExitPoints;
uniform sampler3D VolTex;

void main(void) {
    vec3 exitPoint = texture(ExitPoints, gl_FragCoord.st/FBOSize).xyz;
    if(EntryPoint == exitPoint)
        discard;
    
    vec3 dir = exitPoint - EntryPoint;
    float length = length(dir);
    vec3 deltaDir = normalize(dir) * StepSize;
    float deltaDirLen = length(deltaDir);
    vec3 voxelCoord = EntryPoint;
    vec4 colorAcum = vec4(0.0);
    float lengthAcum = 0.0;
    float alphaAcum = 0.0;
    
    float max_intensity = -1;
    
    for(int i = 0; i < cycle; i++)
    {
        float intensity =  texture(VolTex, voxelCoord).x;
        if(intensity > max_intensity)
            max_intensity = intensity;
        //  colorSample = texture(TransferFunc, intensity);
        //        // modulate the value of colorSample.a
        //        // front-to-back integration
        //        if (colorSample.a > 0.0) {
        //            // accomodate for variable sampling rates (base interval defined by mod_compositing.frag)
        //            colorSample.a = 1.0 - pow(1.0 - colorSample.a, StepSize*200.0f);
        //            colorAcum.rgb += (1.0 - colorAcum.a) * colorSample.rgb * colorSample.a;
        //            colorAcum.a += (1.0 - colorAcum.a) * colorSample.a;
        //        }
        voxelCoord += deltaDir;
        lengthAcum += deltaDirLen;
        if (lengthAcum >= length )
        {
            //            colorAcum.rgb = colorAcum.rgb*colorAcum.a + (1 - colorAcum.a)*bgColor.rgb;
            break;  // terminate if opacity > 1 or the ray is outside the volume
        }
        //        else if (colorAcum.a > 1.0)
        //        {
        //            colorAcum.a = 1.0;
        //            break;
        //        }
    }
    
    int grayStart = (windowCenter - windowWidth / 2);
    int grayEnd = (windowCenter + windowWidth / 2);
    
    float cv = 0;
    if(max_intensity <= grayStart)
        cv = 0;
    else if(max_intensity >= grayEnd)
        cv = 1;
    else
        cv = (max_intensity - grayStart) * 1.0 / windowWidth;
    
    outColor = vec4(cv, cv, cv, cv);
    
    // for test exit point and entry point
    //    outColor = vec4(exitPoint, 1.0);
    //    outColor = vec4(EntryPoint, 1.0);
}
