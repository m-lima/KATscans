layout (location = 0) in vec3 position;

out vec3 vertexOut;
out vec3 vertexOutModel;

uniform mat4 projection;
uniform mat4 view;
uniform mat3 model;

void main()
{
    vertexOut = position * 2.0 - 1.0;
    vertexOutModel = model * vertexOut;
    gl_Position = projection * view * vec4(vertexOutModel, 1.0);
}
