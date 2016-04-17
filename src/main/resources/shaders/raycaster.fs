in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;

uniform int numSamples;
uniform mat4 model;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

const float stepSize = sqrt(3.0) / float(numSamples);
const float densityFactor = 8.0;
const float absorption = 1.0;

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = (inverse(model) * vec4(effectiveEyePos, 1.0)).xyz;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 pos = vertexOut;
    vec3 stepValue = rayDirection * stepSize;

    vec4 colorOut;
    float density;
    vec3 coord;
    int found = 0;
    for (int i = 0; i < numSamples; ++i, pos += stepValue) {
        coord = pos * ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        density = texture(volumeTexture, coord).x;
        /*if (density >= 0.0015 && density <= 0.0017) {
            colorOut = vec4(1, 0, 0, 1);
            break;
        }*/
        if (density >= 0.017 && density <= 0.02) {
            //colorOut = vec4(0, 1, 0, 1);
            //break;
            found++;
        }
        //if (density <= 0.0) continue;
        ////colorOut += density * stepSize;
        //colorOut = max(density, colorOut);
        //if (colorOut >= 1.0) break;
    }

    gl_FragColor = vec4(0, 1, 0, found * 0.15);
    //gl_FragColor.rgb = vec3(colorOut);
    //if (colorOut < 0.1) colorOut = 0.0;
    //gl_FragColor.a = colorOut;

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
