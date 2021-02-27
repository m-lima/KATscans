#version 410

in vec3 vertexOut;
out vec4 fragColor;

void main() {       
    fragColor = vec4(vertexOut, 1.0);
}
