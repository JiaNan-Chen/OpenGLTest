precision mediump float;

uniform sampler2D uTextureUnit;

varying vec2 vTexturePosition;

void main(){
    gl_FragColor =  texture2D(uTextureUnit, vTexturePosition);
}