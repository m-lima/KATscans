#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler2D raycastTexture;
uniform sampler1D transferFunction;

uniform int numSamples;
uniform float stepFactor;
uniform ivec2 screenSize;
uniform float slice;

uniform mat3 invModel;
uniform mat3 normalMatrix;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

uniform vec3 minValues;
uniform vec3 maxValues;

uniform vec3 lightPos;

float stepSize = stepFactor / numSamples;

float stepDist;
vec3 rayDirection;
vec3 effectiveEyePos;

vec3 stepX;
vec3 stepY;
vec3 stepZ;

const vec3 ZERO = vec3(0.0);
const float MIN_ALPHA = 1.0 / 255.0;

out vec4 fragColor;

float luma(vec3 pos) {
    float value = texture(volumeTexture, pos).x;
    vec4 color = texture(transferFunction, value);
    color.a *= color.a;

    if (color.a <= 0.0) {
        return 0.0;
    } else {
        color.rgb *= color.a;
        return dot(color.rgb, vec3(0.299, 0.587, 0.114));
    }
}

vec3 getGradient(vec3 pos) {
    float x1 = luma(pos + stepX);
    float x2 = luma(pos - stepX);
    float y1 = luma(pos + stepY);
    float y2 = luma(pos - stepY);
    float z1 = luma(pos + stepZ);
    float z2 = luma(pos - stepZ);
    return vec3(x2 - x1, y2 - y1, z2 - z1);
}

float rand(){
    return fract(sin(dot(gl_FragCoord.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {       
    effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = invModel * effectiveEyePos;
    rayDirection = normalize(vertexOut - effectiveEyePos);
    vec3 stepValue = rayDirection * stepSize / ratio;

    vec3 slicePos = effectiveEyePos;
    slicePos += slice * rayDirection;
    slicePos = (slicePos / ratio) + 0.5;

    vec3 pos = texture(raycastTexture, vec2(gl_FragCoord.x / screenSize.x, gl_FragCoord.y / screenSize.y)).rgb;
    if (pos == ZERO) {
        pos = slicePos;
    } else {
        float dist = distance((pos - 0.5) * ratio, effectiveEyePos);
        if (dist < slice) {
            pos = slicePos;
        } else {
            pos = ((effectiveEyePos + (dist * rayDirection)) / ratio) + 0.5 + (rand() * stepValue);
        }
    }

    pos += rand() * stepValue;

    stepDist = length(stepValue);
    float fixedStep = stepDist;
    stepX = vec3(fixedStep, 0.0, 0.0);
    stepY = vec3(0.0, fixedStep, 0.0);
    stepZ = vec3(0.0, 0.0, fixedStep);

    float dist = distance((vertexOut / ratio) + 0.5, pos);
    float density;
    vec4 transferColor;
    float lightReflection;
    vec3 normal;
    bool noGradient = (pos == slicePos);
    fragColor = vec4(0.0);
    for (;dist > 0.0; dist -= stepDist, pos += stepValue) {
        if (pos.x < minValues.x || pos.x >= maxValues.x ||
            pos.y < minValues.y || pos.y >= maxValues.y ||
            pos.z < minValues.z || pos.z >= maxValues.z) {
            noGradient = true;
            continue;
        }

        density = texture(volumeTexture, pos).x;
        if (density <= 0.0) continue;

        transferColor = texture(transferFunction, density);
        transferColor.a *= transferColor.a;
        if (transferColor.a <= MIN_ALPHA) continue;

        if (noGradient) {
            lightReflection = 1.0;
        } else {
            normal = normalize(normalMatrix * getGradient(pos));
            lightReflection = dot(normal, lightPos);
        }
        transferColor.rgb *= lightReflection;

        fragColor.rgb = mix(fragColor.rgb, transferColor.rgb, transferColor.a);
        fragColor.a += transferColor.a;

        if (fragColor.a >= 1.0) {
            break;
        }
    }
}
