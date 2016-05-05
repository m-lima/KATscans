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
uniform mat3 normalMatrix;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

uniform vec3 lightPos = normalize(vec3(-2.0, 2.0, 5.0));

int actualSamples = numSamples * lodMultiplier;
float stepSize = 1f / actualSamples;

float stepDist;
vec3 rayDirection;
vec3 effectiveEyePos;

const vec3 zero = vec3(0.0);

out vec4 fragColor;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

float luma(vec3 pos) {
    float value = texture(volumeTexture, pos).x;
    vec4 color = texture(transferFunction, value);
    return dot(color.rgb, vec3(0.299, 0.587, 0.114)) * color.a;
}

vec3 getGradient(vec3 pos) {
    vec3 stepX = vec3(stepDist * lodMultiplier, 0.0, 0.0);
    vec3 stepY = vec3(0.0, stepDist * lodMultiplier, 0.0);
    vec3 stepZ = vec3(0.0, 0.0, stepDist * lodMultiplier);

    float x1 = luma(pos + stepX);
    float x2 = luma(pos - stepX);
    float y1 = luma(pos + stepY);
    float y2 = luma(pos - stepY);
    float z1 = luma(pos + stepZ);
    float z2 = luma(pos - stepZ);
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

    pos += rand(gl_FragCoord.xy) * stepValue;

    float dist = distance((vertexOut / ratio) + 0.5, pos);
    float density;
    vec4 transferColor;
    float lightReflection;
    vec3 normal;
    stepDist = length(stepValue);
    fragColor = vec4(0.0);
    for (;dist > 0.0; dist -= stepDist, pos += stepValue) {
        density = texture(volumeTexture, pos).x;
        if (density <= 0.0) continue;

        if (pos == slicePos) {
            lightReflection = 1.0;
        } else {
            normal = normalize(normalMatrix * getGradient(pos));
            lightReflection = dot(normal, lightPos);
        }

        transferColor = texture(transferFunction, density);
        transferColor.rgb *= lightReflection;

        transferColor.a /= lodMultiplier;
        if (transferColor.a <= 0.0) continue;

        fragColor.rgb = mix(fragColor.rgb, transferColor.rgb, transferColor.a);
        fragColor.a += transferColor.a;

        if (fragColor.a >= 1.0) {
            break;
        }
    }
}
