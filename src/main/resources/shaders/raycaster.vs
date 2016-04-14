layout (location = 0) in vec3 position;

out vec3 vertexOut;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

//uniform float zoom;

void main()
{
    vertexOut = position;
    //gl_Position = vec4(2.0 * (position - 0.5), 1.0);
    gl_Position = projection * view * model * vec4((position - 0.5) * 2, 1.0);
}
