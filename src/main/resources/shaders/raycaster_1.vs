layout (location = 0) in vec3 position;

out vec3 vertexOut;
out vec3 vertexOutModel;
out float translation;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    translation = -0.5;
    
    /*
    vertexOut = position - 0.5;
    vertexOutModel = mat3(model) * (position - 0.5);
    gl_Position = projection * view * model * vec4(position - 0.5, 1.0);
    */
    
    vertexOut = position + translation;
    vertexOutModel = mat3(model) * (position + translation);
    gl_Position = projection * view * model * vec4(position + translation, 1.0);
}
