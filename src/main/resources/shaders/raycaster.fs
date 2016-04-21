in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler1D transferFunction;

uniform int numSamples;
uniform mat4 model;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

const float stepSize = sqrt(3.0) / float(numSamples);

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = (inverse(model) * vec4(effectiveEyePos, 1.0)).xyz;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 pos = vertexOut;
    vec3 stepValue = rayDirection * stepSize;

    float density;
    vec3 coord;
    vec4 color = vec4(0.0);
    vec4 transferColor;
    for (int i = 0; i < numSamples; ++i, pos += stepValue) {
        coord = pos * ratio + 0.5;
        if (coord.x < 0.0 || coord.x > 1.0 ||
            coord.y < 0.0 || coord.y > 1.0 ||
            coord.z < 0.0 || coord.z > 1.0) {
            break;
        }
        
        density = texture(volumeTexture, coord).x;
        transferColor = texture(transferFunction, density);

        //color = mix(color, transferColor, transferColor.a);
        color.rgb = mix(color.rgb, transferColor.rgb, transferColor.a);
        color.a += transferColor.a * (256.0 / numSamples);

        if (color.a >= 1.0) {
            break;
        }
    }

    gl_FragColor = color;

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
