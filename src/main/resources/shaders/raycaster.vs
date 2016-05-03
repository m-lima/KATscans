#version 150

in vec3 position;
out vec3 vertexOut;

uniform vec3 ratio;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    vertexOut = position;    
    gl_Position = projection * view * model * vec4((vertexOut - 0.5) * ratio, 1.0);
}
