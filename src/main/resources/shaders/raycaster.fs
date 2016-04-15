in vec3 vertexOut;
in vec3 vertexOutModel;

uniform sampler3D volumeTexture;

uniform int numSamples;
uniform mat3 model;
uniform bool orthographic;
uniform vec3 eyePos;

const float stepSize = 2.0 * sqrt(2.0) / float(numSamples);
const float densityFactor = 8.0;
const float absorption = 1.0;

void main()
{       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = inverse(model) * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 invRay = 1.0 / rayDirection;
    vec3 back = invRay * (-(1.0 + effectiveEyePos));
    vec3 front = invRay * (1.0 - effectiveEyePos);
    vec3 tmin = min(back, front);
    vec3 tmax = max(back, front);

    vec2 temp = max(tmin.xx, tmin.yz);
    float near = max(temp.x, temp.y);
    if (near < 0.0) near = 0.0;

    temp = min(tmax.xx, tmax.yz);
    float far = min(temp.x, temp.y);

    vec3 rayStart = effectiveEyePos + rayDirection * near;
    vec3 rayStop = effectiveEyePos + rayDirection * far;

    vec3 pos = rayStart;
    vec3 stepValue = normalize(rayStop - rayStart) * stepSize;
    float travel = distance(rayStop, rayStart);

    float colorOut = 0.0;
    float density;
    for (int i = 0; i < numSamples && travel > 0.0; ++i, pos += stepValue, travel -= stepSize) {
        density = texture(volumeTexture, pos * 0.5 + 0.5).x * densityFactor;
        if (density <= 0.0) continue;
        //colorOut += density * stepSize;
        colorOut = max(density, colorOut);
        if (colorOut >= 1.0) break;
    }

    gl_FragColor.rgb = vec3(colorOut);
    if (colorOut < 0.1) colorOut = 0.0;
    gl_FragColor.a = colorOut;

//#define COLOR_CUBE
#ifdef COLOR_CUBE
    float minLimit = -1.0;
    float maxLimit = 1.0;
    vec4 saturated = vec4((vertexOut.x == maxLimit || vertexOut.x == minLimit) ? 1.0 : 0.0,
                          (vertexOut.y == maxLimit || vertexOut.y == minLimit) ? 1.0 : 0.0,
                          (vertexOut.z == maxLimit || vertexOut.z == minLimit) ? 1.0 : 0.0,
                          0.25);
    gl_FragColor += saturated;
#endif
}
