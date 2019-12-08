attribute vec4 aPosition;
attribute vec2 aTexturePosition;
uniform mat4 aModelMatrix;
uniform mat4 aViewMatrix;
uniform mat4 aPerspectiveMatrix;
varying vec2 vTexturePosition;

void main(){
    gl_Position=aPerspectiveMatrix*aViewMatrix*aModelMatrix*aPosition;
    vTexturePosition=aTexturePosition;
}