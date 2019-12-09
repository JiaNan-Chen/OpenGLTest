attribute vec4 aPosition;
uniform mat4 aViewMatrix;
uniform mat4 aPerspectiveMatrix;

varying vec4 vColor;

void main(){
    int r;int  g;int  b;
    if ((aPosition.x)==0.0){
        r=1;
    } else {
        r=0;
    }
    if ((aPosition.y)==0.0){
        g=1;
    } else {
        g=0;
    }
    if ((aPosition.z)==0.0){
        b=1;
    } else {
        b=0;
    }
    vColor=vec4(r, g, b, 1);
    gl_Position=aPerspectiveMatrix*aViewMatrix*aPosition;
}