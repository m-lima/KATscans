#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler1D colors;
uniform float threshold;

uniform int numSamples;
uniform int lodMultiplier;

uniform mat4 view;
uniform mat4 model;
uniform mat3 invModel;
uniform mat3 normalMatrix;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

uniform vec3 lightPos = normalize(vec3(-2.0, 2.0, 1.0));

int actualSamples = numSamples * lodMultiplier;
float stepSize = 1f / actualSamples;
int maxDistance = int(sqrt(3.0) * actualSamples) << 1;

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

    effectiveEyePos = invModel * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 stepValue = rayDirection * stepSize / ratio;
    vec3 pos = vertexOut + rand(gl_FragCoord.xy) * stepValue;
    pos = pos / ratio + 0.5;

    float density;
    vec3 normal;
    vec3 color;
    float lightReflection;
    fragColor = vec4(0.0);
    for (int i = 0; i < maxDistance; i++, pos += stepValue) {
        if (pos.x < 0.0 || pos.x > 1.0 ||
            pos.y < 0.0 || pos.y > 1.0 ||
            pos.z < 0.0 || pos.z > 1.0) {
            break;
        }
        
        density = texture(volumeTexture, pos).x;
        if (density <= threshold) continue;
        //normal = normalize(mat3(inverse(model)) * getGradient(pos, density));
        //normal = normalize(mat3(transpose(inverse(view * model))) * getGradient(pos, density));
        //normal = normalize(mat3(transpose(inverse(view * model))) * getGradient(pos, density));
        normal = normalize(normalMatrix * getGradient(pos, density));

        float lightReflection = abs(dot(normal, lightPos));
        color = lightReflection * texture(colors, density).rgb;
        fragColor = vec4(color, 1.0);

        break;
    }
}
