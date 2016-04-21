layout (location = 0) in vec3 position;

out vec3 color;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    color = position;
    gl_Position = projection * view * model * vec4(position - 0.5, 1.0);
}
