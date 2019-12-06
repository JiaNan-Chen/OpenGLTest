attribute vec4 aPosition;
attribute vec2 aTexturePosition;
varying vec2 vTexturePosition;

void main(){
    gl_Position = aPosition;
    vTexturePosition=aTexturePosition;
}