#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;

uniform int numSamples;
uniform int lodMultiplier;

uniform mat4 model;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

int actualSamples = numSamples * lodMultiplier / 16;
float stepSize = 1f / float(actualSamples);

out vec4 fragColor;

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
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

    float density;
    vec3 coord;
    float color = 0.0;
    for (int i = 0; i < actualSamples * 3; ++i, pos += stepValue) {
        coord = pos / ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        density = texture(volumeTexture, coord).x;
        if (density <= 0.0) continue;
        color = max(density, color);
        if (color >= 1.0) {
            color = 1.0;
            break;
        }
    }

    fragColor.rgb = vec3(color);
    if (color < 0.1) color = 0.0;
    fragColor.a = color;

//#define COLOR_CUBE
#ifdef COLOR_CUBE
    float minLimit = -0.5;
    float maxLimit = 0.5;
    vec4 saturated = vec4((vertexOut.x == maxLimit || vertexOut.x == minLimit) ? 1.0 : 0.0,
                          (vertexOut.y == maxLimit || vertexOut.y == minLimit) ? 1.0 : 0.0,
                          (vertexOut.z == maxLimit || vertexOut.z == minLimit) ? 1.0 : 0.0,
                          0.25);
    fragColor += saturated;
#endif
}
