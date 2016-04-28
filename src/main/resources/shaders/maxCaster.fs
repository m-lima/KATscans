#version 150

in vec3 vertexOut;
in vec4 vertexOutModel;

uniform sampler3D volumeTexture;

uniform int numSamples;
uniform int lodMultiplier;

uniform mat3 invModel;
uniform bool orthographic;
uniform vec3 eyePos;
uniform vec3 ratio;

int actualSamples = (numSamples * lodMultiplier) >> 4;
float stepSize = 1f / actualSamples;
int maxDistance = int(sqrt(3.0) * actualSamples) << 1;

out vec4 fragColor;

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {       
    vec3 effectiveEyePos = eyePos;
    if (orthographic) {
        effectiveEyePos.xy = vertexOutModel.xy;
    } 

    effectiveEyePos = invModel * effectiveEyePos;
    vec3 rayDirection = normalize(vertexOut - effectiveEyePos);

    vec3 stepValue = rayDirection * stepSize / ratio;
    vec3 pos = vertexOut + rand(gl_FragCoord.xy) * stepValue;
    pos = pos / ratio + 0.5;

    float density;
    float color = 0.0;
    for (int i = 0; i < maxDistance; i++, pos += stepValue) {
        if (pos.x < 0.0 || pos.x > 1.0 ||
            pos.y < 0.0 || pos.y > 1.0 ||
            pos.z < 0.0 || pos.z > 1.0) {
            break;
        }

        density = texture(volumeTexture, pos).x;
        if (density <= 0.0) continue;
        color = max(density, color);
        if (color >= 1.0) {
            color = 1.0;
            break;
        }
    }

    fragColor.rgb = vec3(color);
    if (color < 0.1) color = 0.0;
    fragColor.a = color;
}
