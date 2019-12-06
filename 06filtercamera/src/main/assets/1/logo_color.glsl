precision mediump float;

uniform sampler2D uTextureUnit;

varying vec2 vTexturePosition;
uniform float uAlpha;

void main(){
    vec4 logoTexture =  texture2D(uTextureUnit, vTexturePosition);

    gl_FragColor=vec4(logoTexture.xyz, logoTexture.w*uAlpha);
}