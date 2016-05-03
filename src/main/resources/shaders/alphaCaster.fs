#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler2D raycastTexture;
uniform sampler1D transferFunction;

uniform int numSamples;
uniform int lodMultiplier;
uniform ivec2 screenSize;
uniform float slice;

uniform mat3 invModel;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

int actualSamples = numSamples * lodMultiplier;
float stepSize = 1f / actualSamples;

const vec3 zero = vec3(0.0);

out vec4 fragColor;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = invModel * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);
    vec3 stepValue = rayDirection * stepSize / ratio;

    vec3 pos = texture(raycastTexture, vec2(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y)).rgb;
    if (pos == zero) {
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

    pos += rand(gl_FragCoord.xy) * stepValue;

    float dist = distance((vertexOut / ratio) + 0.5, pos);
    float stepDist = length(stepValue);
    float density;
    vec3 transferColor;
    vec4 src;
    fragColor = vec4(0.0);
    for (;dist > 0.0; dist -= stepDist, pos += stepValue) {
        density = texture(volumeTexture, pos).x;
        if (density <= 0.0) continue;
        transferColor = texture(transferFunction, density).rgb;
        src = vec4(0.5 * (transferColor + 1.0), dot(transferColor, transferColor));
        src.rgb *= src.a;
        fragColor = (1.0 - fragColor.a) * src + fragColor;        

        if (fragColor.a >= 1.0) {
            break;
        }
    }
}
