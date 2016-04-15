layout (location = 0) in vec3 position;

out vec3 vertexOut;
out vec3 vertexOutModel;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    vertexOut = position * 2.0 - 1.0;
    vertexOutModel = mat3(model) * (position * 2.0 - 1.0);
    gl_Position = projection * view * model * vec4(position * 2.0 - 1.0, 1.0);
}
