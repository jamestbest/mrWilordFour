package com.mygdx.game.Math;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Matrix4 {

    /*
        https://en.wikipedia.org/wiki/Orthographic_projection
        https://learnopengl.com/Getting-started/Camera
        https://ncalculators.com/matrix/4x4-matrix-multiplication-calculator.htm
        M00 M01 M02 M03
        M10 M11 M12 M13
        M20 M21 M22 M23
        M30 M31 M32 M33
    */

    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    public Matrix4(){

    }

    public Matrix4(Matrix4 matrix){
        copyMatrix(matrix);
    }

    public void setToOrthographic(float left, float right, float bottom, float top, float near, float far){
        clear();
        m00 = 2 / (right - left);
        m11 = 2 / (top - bottom);
        m22 = -2 / (far - near);

        m03 = - (right + left) / (right - left);
        m13 = - (top + bottom) / (top - bottom);
        m23 = - (far + near) / (far - near);
        m33 = 1;
    }

    public void identityMatrix(){
        clear();
        m00 = 1;
        m11 = 1;
        m22 = 1;
        m33 = 1;
    }

    public void clear(){
        m00 = 0;
        m01 = 0;
        m02 = 0;
        m03 = 0;

        m10 = 0;
        m11 = 0;
        m12 = 0;
        m13 = 0;

        m20 = 0;
        m21 = 0;
        m22 = 0;
        m23 = 0;

        m30 = 0;
        m31 = 0;
        m32 = 0;
        m33 = 0;
    }

    public void lookAt(Vector3 position, Vector3 target, Vector3 up){
        Vector3 tempPos = new Vector3(position);
        Vector3 direction = tempPos.sub(target).nor();
        Vector3 directionCopy = new Vector3(direction);
        Vector3 upCopy = new Vector3(up);
        Vector3 right = upCopy.crs(directionCopy).nor();

        m00 = right.x;
        m01 = right.y;
        m02 = right.z;
        m10 = up.x;
        m11 = up.y;
        m12 = up.z;
        m20 = direction.x;
        m21 = direction.y;
        m22 = direction.z;

        Matrix4 temp2 = new Matrix4();
        temp2.m03 = -position.x;
        temp2.m13 = -position.y;
        temp2.m23 = -position.z;
        temp2.m00 = 1;
        temp2.m11 = 1;
        temp2.m22 = 1;
        temp2.m33 = 1;

        multiply(temp2);
    }

    public void multiply(Matrix4 o){
        float m00 = this.m00 * o.m00 + this.m01 * o.m10 + this.m02 * o.m20 + this.m03 * o.m30;
        float m01 = this.m00 * o.m01 + this.m01 * o.m11 + this.m02 * o.m21 + this.m03 * o.m31;
        float m02 = this.m00 * o.m02 + this.m01 * o.m12 + this.m02 * o.m22 + this.m03 * o.m32;
        float m03 = this.m00 * o.m03 + this.m01 * o.m13 + this.m02 * o.m23 + this.m03 * o.m33;

        float m10 = this.m10 * o.m00 + this.m11 * o.m10 + this.m12 * o.m20 + this.m13 * o.m30;
        float m11 = this.m10 * o.m01 + this.m11 * o.m11 + this.m12 * o.m21 + this.m13 * o.m31;
        float m12 = this.m10 * o.m02 + this.m11 * o.m12 + this.m12 * o.m22 + this.m13 * o.m32;
        float m13 = this.m10 * o.m03 + this.m11 * o.m13 + this.m12 * o.m23 + this.m13 * o.m33;

        float m20 = this.m20 * o.m00 + this.m21 * o.m10 + this.m22 * o.m20 + this.m23 * o.m30;
        float m21 = this.m20 * o.m01 + this.m21 * o.m11 + this.m22 * o.m21 + this.m23 * o.m31;
        float m22 = this.m20 * o.m02 + this.m21 * o.m12 + this.m22 * o.m22 + this.m23 * o.m32;
        float m23 = this.m20 * o.m03 + this.m21 * o.m13 + this.m22 * o.m23 + this.m23 * o.m33;

        float m30 = this.m30 * o.m00 + this.m31 * o.m10 + this.m32 * o.m20 + this.m33 * o.m30;
        float m31 = this.m30 * o.m01 + this.m31 * o.m11 + this.m32 * o.m21 + this.m33 * o.m31;
        float m32 = this.m30 * o.m02 + this.m31 * o.m12 + this.m32 * o.m22 + this.m33 * o.m32;
        float m33 = this.m30 * o.m03 + this.m31 * o.m13 + this.m32 * o.m23 + this.m33 * o.m33;

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;

        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;

        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public String toString(){
        return "Matrix4: \n" +
                m00 + " " + m01 + " " + m02 + " " + m03 + "\n" +
                m10 + " " + m11 + " " + m12 + " " + m13 + "\n" +
                m20 + " " + m21 + " " + m22 + " " + m23 + "\n" +
                m30 + " " + m31 + " " + m32 + " " + m33 + "\n";
    }

    public void copyMatrix(Matrix4 o){
        m00 = o.m00;
        m01 = o.m01;
        m02 = o.m02;
        m03 = o.m03;

        m10 = o.m10;
        m11 = o.m11;
        m12 = o.m12;
        m13 = o.m13;

        m20 = o.m20;
        m21 = o.m21;
        m22 = o.m22;
        m23 = o.m23;

        m30 = o.m30;
        m31 = o.m31;
        m32 = o.m32;
        m33 = o.m33;
    }

    public com.badlogic.gdx.math.Matrix4 getGdxMatrix(){
        com.badlogic.gdx.math.Matrix4 out = new com.badlogic.gdx.math.Matrix4();
        out.val[0] = m00;
        out.val[4] = m01;
        out.val[8] = m02;
        out.val[12] = m03;
        out.val[5] = m11;
        out.val[9] = m12;
        out.val[13] = m13;
        out.val[2] = m20;
        out.val[6] = m21;
        out.val[10] = m22;
        out.val[14] = m23;
        out.val[3] = m30;
        out.val[7] = m31;
        out.val[11] = m32;
        out.val[15] = m33;
        return out;
    }
}
