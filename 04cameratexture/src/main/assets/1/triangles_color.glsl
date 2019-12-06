#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTextureUnit;

varying vec2 vTexturePosition;

void main(){
    gl_FragColor = texture2D( uTextureUnit, vTexturePosition );
}