#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;
uniform sampler2D raycastTexture;
uniform sampler1D colors;
uniform float threshold;

uniform int numSamples;
uniform ivec2 screenSize;
uniform float slice;

uniform mat4 view;
uniform mat4 model;
uniform mat3 invModel;
uniform mat3 normalMatrix;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

uniform vec3 lightPos = normalize(vec3(-2.0, 2.0, 5.0));

int actualSamples = numSamples;
float stepSize = 1f / actualSamples;

float stepDist;
vec3 rayDirection;
vec3 effectiveEyePos;

const vec3 zero = vec3(0.0);

out vec4 fragColor;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

vec3 getGradient(vec3 pos, float value) {
    vec3 stepX = vec3(stepDist, 0.0, 0.0);
    vec3 stepY = vec3(0.0, stepDist, 0.0);
    vec3 stepZ = vec3(0.0, 0.0, stepDist);

    float x1 = texture(volumeTexture, pos + stepX).x;
    float x2 = texture(volumeTexture, pos - stepX).x;
    float y1 = texture(volumeTexture, pos + stepY).x;
    float y2 = texture(volumeTexture, pos - stepY).x;
    float z1 = texture(volumeTexture, pos + stepZ).x;
    float z2 = texture(volumeTexture, pos - stepZ).x;
    return vec3(x2 - x1, y2 - y1, z2 - z1);
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
    if (pos == zero) {
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
    stepDist = length(stepValue);
    float density;
    vec3 normal;
    vec3 color;
    float lightReflection;
    while (dist > 0.0) {
        density = texture(volumeTexture, pos).x;
        if (density <= threshold) {
            pos += stepValue;
            dist -= stepDist;
            continue;
        }

        if (pos == slicePos) {
            lightReflection = 1.0;
        } else {
            normal = normalize(normalMatrix * getGradient(pos, density));
            lightReflection = dot(normal, lightPos);
        }

        color = lightReflection * texture(colors, density).rgb;
        fragColor = vec4(color, 1.0);

        break;
    }
}
