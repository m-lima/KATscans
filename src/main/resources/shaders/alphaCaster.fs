#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler2D raycastTexture;
uniform sampler1D transferFunction;

uniform int numSamples;
uniform float lodMultiplier;
uniform ivec2 screenSize;
uniform float slice;

uniform mat3 invModel;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

uniform vec3 minValues;
uniform vec3 maxValues;

float stepSize = lodMultiplier / numSamples;

const vec3 ZERO = vec3(0.0);
const float MIN_ALPHA = 1.0 / 255.0;

out vec4 fragColor;

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = invModel * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);
    vec3 stepValue = rayDirection * stepSize / ratio;

    vec3 pos = texture(raycastTexture, vec2(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y)).rgb;
    if (pos == ZERO) {
        pos = effectiveEyePos;
        pos += slice * rayDirection;
        pos = (pos / ratio) + 0.5;
    } else {
        float dist = distance((pos - 0.5) * ratio, effectiveEyePos);
        if (dist < slice) {
            pos = effectiveEyePos;
            pos += slice * rayDirection;
            pos = (pos / ratio) + 0.5;
        } else {
            pos = effectiveEyePos;
            pos += dist * rayDirection;
            pos = (pos / ratio) + 0.5;
        }
    }

    float dist = distance((vertexOut / ratio) + 0.5, pos);
    float stepDist = length(stepValue);
    float density;
    vec4 transferColor;
    fragColor = vec4(0.0);
    for (;dist > 0.0; dist -= stepDist, pos += stepValue) {
        if (pos.x < minValues.x || pos.x >= maxValues.x ||
            pos.y < minValues.y || pos.y >= maxValues.y ||
            pos.z < minValues.z || pos.z >= maxValues.z) {
            continue;
        }

        density = texture(volumeTexture, pos).x;
        if (density <= 0.0) continue;

        transferColor = texture(transferFunction, density);
        transferColor.a *= transferColor.a;
        transferColor.a *= lodMultiplier;
        if (transferColor.a <= MIN_ALPHA) continue;

        fragColor.rgb = mix(fragColor.rgb, transferColor.rgb, transferColor.a);
        fragColor.a += transferColor.a;

        if (fragColor.a >= 1.0) {
            break;
        }
    }
}
