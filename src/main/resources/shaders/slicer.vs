#version 150

in vec3 position;

void main()
{
    gl_Position = vec4(position * 2 - vec3(1.0), 1.0);
}
