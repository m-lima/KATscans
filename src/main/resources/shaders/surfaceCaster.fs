#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler2D raycastTexture;
uniform sampler1D colors;
uniform float thresholdLo;
uniform float thresholdHi;

uniform int numSamples;
uniform float stepFactor = 1.0;
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
uniform vec3 lightPosFront;

float stepSize = stepFactor / numSamples;

const vec3 ZERO = vec3(0.0);

out vec4 fragColor;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

vec3 getGradient(vec3 pos, float gradientStep) {
    vec3 stepX = vec3(gradientStep, 0.0, 0.0);
    vec3 stepY = vec3(0.0, gradientStep, 0.0);
    vec3 stepZ = vec3(0.0, 0.0, gradientStep);

    float x1 = texture(volumeTexture, pos + stepX).x;
    float x2 = texture(volumeTexture, pos - stepX).x;
    float y1 = texture(volumeTexture, pos + stepY).x;
    float y2 = texture(volumeTexture, pos - stepY).x;
    float z1 = texture(volumeTexture, pos + stepZ).x;
    float z2 = texture(volumeTexture, pos - stepZ).x;
    return vec3(x2 - x1, y2 - y1, z2 - z1);
}

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = invModel * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);
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
            pos = effectiveEyePos;
            pos += dist * rayDirection;
            pos = (pos / ratio) + 0.5;
            pos += rand(gl_FragCoord.xy) * stepValue;
        }
    }

    float dist = distance((vertexOut / ratio) + 0.5, pos);
    float stepDist = length(stepValue);
    float density;
    vec3 normal;
    vec3 color;
    float lightReflection;
    bool invert = false;
    bool noGradient = true;
    while (dist > 0.0) {
        if (pos.x < minValues.x || pos.x >= maxValues.x ||
            pos.y < minValues.y || pos.y >= maxValues.y ||
            pos.z < minValues.z || pos.z >= maxValues.z) {
            pos += stepValue;
            dist -= stepDist;
            continue;
        }

        density = texture(volumeTexture, pos).x;
        if (density < thresholdLo) {
            pos += stepValue;
            dist -= stepDist;
            invert = false;
            noGradient = false;
            continue;
        }

        if (density > thresholdHi) {
            pos += stepValue;
            dist -= stepDist;
            invert = true;
            noGradient = false;
            continue;
        }

        if (noGradient) {
            lightReflection = 1.0;
        } else {
            normal = normalize(normalMatrix * getGradient(pos, stepDist * stepFactor));
            if (invert) {
                lightReflection = dot(normal, lightPosFront);
            } else {
                lightReflection = dot(normal, lightPos);
            }
        }

        color = lightReflection * texture(colors, density).rgb;
        fragColor = vec4(color, 1.0);

        break;
    }
}
