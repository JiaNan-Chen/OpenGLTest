attribute vec4 aPosition;
attribute vec2 aTexturePosition;
uniform mat4 aModelMatrix;
uniform mat4 aViewMatrix;
varying vec2 vTexturePosition;

void main(){
    gl_Position=aViewMatrix*aModelMatrix*aPosition;
    vTexturePosition=aTexturePosition;
}