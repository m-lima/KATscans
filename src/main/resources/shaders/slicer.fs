uniform float slice;
uniform vec2 screenSize;
uniform sampler3D volumeTexture;

void main()
{   
    //gl_FragColor = vec4(0.0, gl_FragCoord.y / screenSize.y, gl_FragCoord.x / screenSize.x, 1.0);
    vec4 color = texture(volumeTexture, vec3(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y, slice));
    color *= 8;
    gl_FragColor = vec4(color.r, color.r, color.r, 1.0);
}
