#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler1D colors;
uniform float threshold;

uniform int numSamples;
uniform int lodMultiplier;

uniform mat4 model;
uniform mat4 view;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

int actualSamples = numSamples * lodMultiplier;
float stepSize = 1f / float(actualSamples);

vec3 stepX = vec3(stepSize * lodMultiplier * ratio.x, 0.0, 0.0);
vec3 stepY = vec3(0.0, stepSize * lodMultiplier * ratio.y, 0.0);
vec3 stepZ = vec3(0.0, 0.0, stepSize * lodMultiplier * ratio.z);

out vec4 fragColor;

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float luma(vec4 color) {
  return dot(color.rgb, vec3(0.299, 0.587, 0.114));
}

vec3 getGradient(vec3 pos, float value)
{
    float x = texture(volumeTexture, pos + stepX).x;
    float y = texture(volumeTexture, pos + stepY).x;
    float z = texture(volumeTexture, pos + stepZ).x;
    return vec3(x - value, y - value, z - value);
}

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = (inverse(model) * vec4(effectiveEyePos, 1.0)).xyz;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 stepValue = rayDirection * stepSize;
    vec3 pos = vertexOut + rand(gl_FragCoord.xy) * stepValue;

    vec3 coord;
    float density;
    vec3 normal;
    vec3 color;
    fragColor = vec4(0.0);
    for (int i = 0; i < actualSamples * 3; ++i, pos += stepValue) {
        coord = pos / ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        
        density = texture(volumeTexture, coord).x;
        if (density <= threshold) continue;
        normal = normalize(mat3(transpose(inverse(view * model))) * getGradient(coord, density));
        //normal = normalize(mat3(view * model) * getGradient(coord, density));

        vec3 light = normalize(vec3(-2, 2, 1));
        float df = abs(dot(normal, light));
        color = df * texture(colors, density).rgb;
        fragColor = vec4(color, 1.0);

        break;
    }
}
