attribute vec4 aPosition;
attribute vec2 aTexturePosition;
uniform mat4 aModelMatrix;
varying vec2 vTexturePosition;

void main(){
    gl_Position=aModelMatrix*aPosition;
    vTexturePosition=aTexturePosition;
}