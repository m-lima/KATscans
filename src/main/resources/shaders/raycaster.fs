in vec3 vertexOut;

uniform sampler3D volumeTexture;

uniform int numSamples;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform bool orthographic;
uniform vec2 windowSize;
uniform vec3 eyePos;

uniform float zoom;
uniform float focalLength;

const float stepSize = 1f / float(numSamples);
const float densityFactor = 40;
const float absorption = 1;

struct Ray {
    vec3 origin;
    vec3 direction;
};

void main()
{   
    vec3 rayDirection;
    vec3 actualEyePos;
    if (orthographic) {
        rayDirection = vec3(0, 0, -1);
        actualEyePos.xy = 2.0 * gl_FragCoord.xy / windowSize - 1.0;
    } else {
        rayDirection.xy = 2.0 * gl_FragCoord.xy / windowSize - 1.0;
        rayDirection.z = -eyePos.z;
    } 
    rayDirection = (inverse(model) * vec4(rayDirection, 0f)).xyz;
    rayDirection = normalize(rayDirection);

    actualEyePos = (inverse(model) * vec4(eyePos, 0f)).xyz;
    //vec3 actualEyePos = eyePos;
    Ray ray = Ray(actualEyePos, normalize(rayDirection));

    vec3 invRay = 1.0 / ray.direction;
    vec3 back = invRay * (-(1 + ray.origin));
    vec3 front = invRay * (1 - ray.origin);
    vec3 tmin = min(back, front);
    vec3 tmax = max(back, front);

    vec2 temp = max(tmin.xx, tmin.yz);
    float near = max(temp.x, temp.y);
    if (near < 0.0) near = 0.0;

    temp = min(tmax.xx, tmax.yz);
    float far = min(temp.x, temp.y);

    vec3 rayStart = ray.origin + ray.direction * near;
    vec3 rayStop = ray.origin + ray.direction * far;
    rayStart = 0.5 * (rayStart + 1.0);
    rayStop = 0.5 * (rayStop + 1.0);

    vec3 pos = rayStart;
    vec3 step = normalize(rayStop - rayStart) * stepSize;
    float travel = distance(rayStop, rayStart);
    float transparency = 1.0;
    vec3 color = vec3(0.0);

    //vec3 eye = actualEyePos;
    //vec3 raydir = -normalize(vertexOut - eye);
    vec3 rayPos = vertexOut;

    vec3 raydir = -rayDirection;
    //vec3 rayPos = (inverse(model) * vec4(2.0 * gl_FragCoord.xy / windowSize - 1.0, 0.0, 0.0)).xyz;

    float maxVal = 0.0;
    for (int i = 0; i < 1000; i++) {
        float density = texture(volumeTexture, rayPos).x * 8.0;
        maxVal = max(maxVal,density);
        rayPos += raydir * 0.01;
    }

    gl_FragColor.rgb = vec3(maxVal);
if (maxVal < 0.1) maxVal = 0;
    gl_FragColor.a = maxVal;

    /*for (int i = 0; i < numSamples && travel > 0.0; ++i, pos += step, travel -= stepSize) {

        float density = texture(volumeTexture, pos).x * densityFactor;
        if (density <= 0.0) {
            continue;
        }

        //transparency *= 1.0 - density * stepSize * absorption;
        //if (transparency <= 0.01) {
        //    break;
        //}

        //color += transparency * density * stepSize;
        color += density * stepSize;
    }

    gl_FragColor.rgb = color;
    gl_FragColor.a = 1.0 - color.r;*/

    //gl_FragColor = vec4(vertexOut, 1.0);
}
