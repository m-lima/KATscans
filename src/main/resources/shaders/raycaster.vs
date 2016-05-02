#version 150

in vec3 position;

out vec3 vertexOut;
out vec4 vertexOutModel;

uniform vec3 ratio;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    vertexOut = position - 0.5;
    vertexOut *= ratio;
    vertexOutModel = model * vec4(vertexOut, 1.0);

    //vertexOut = position;
    //vertexOutModel = model * vec4((vertexOut - 0.5) * ratio, 1.0);
    
    gl_Position = projection * view * vertexOutModel;
}
