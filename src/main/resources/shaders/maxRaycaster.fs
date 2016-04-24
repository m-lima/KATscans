in vec3 vertexOut;
in vec4 vertexOutModel;

layout(binding=0) uniform sampler3D volumeTexture;

uniform int numSamples;
uniform int lodMultuplier;
uniform float densityFactor;

uniform mat4 model;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

const int actualSamples = numSamples * lodMultuplier;
const float stepSize = sqrt(3.0) / float(actualSamples);

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = (inverse(model) * vec4(effectiveEyePos, 1.0)).xyz;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 pos = vertexOut;
    vec3 stepValue = rayDirection * stepSize;

    float colorOut = 0;
    float density;
    vec3 coord;
    for (int i = 0; i < actualSamples; ++i, pos += stepValue) {
        coord = pos * ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        density = texture(volumeTexture, coord).x * densityFactor;
        if (density <= 0.0) continue;
        colorOut = max(density, colorOut);
        if (colorOut >= 1.0) break;
    }

    gl_FragColor.rgb = vec3(colorOut);
    if (colorOut < 0.1) colorOut = 0.0;
    gl_FragColor.a = colorOut;

//#define COLOR_CUBE
#ifdef COLOR_CUBE
    float minLimit = -0.5;
    float maxLimit = 0.5;
    vec4 saturated = vec4((vertexOut.x == maxLimit || vertexOut.x == minLimit) ? 1.0 : 0.0,
                          (vertexOut.y == maxLimit || vertexOut.y == minLimit) ? 1.0 : 0.0,
                          (vertexOut.z == maxLimit || vertexOut.z == minLimit) ? 1.0 : 0.0,
                          0.25);
    gl_FragColor += saturated;
#endif
}
