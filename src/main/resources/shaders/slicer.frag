#version 410

uniform float slice;
uniform ivec2 screenSize;
uniform sampler3D volumeTexture;
uniform sampler1D transferFunction;

out vec4 fragColor;

void main()
{   
    float value = texture(volumeTexture, vec3(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y, slice)).x;
    fragColor = texture(transferFunction, value);
}
