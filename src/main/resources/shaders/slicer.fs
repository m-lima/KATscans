uniform float slice;
uniform vec2 screenSize;
uniform sampler3D volumeTexture;
uniform sampler1D transferFunction;

void main()
{   
    float value = texture(volumeTexture, vec3(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y, slice)).x;
    vec4 color = texture(transferFunction, value);
    gl_FragColor = color;
}
