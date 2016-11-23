package com.apress.android.glmask;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _rectangleOneProgram, _rectangleTwoProgram;
	private int _rectangleOneAVertexLocation, _rectangleTwoAVertexLocation;
	private FloatBuffer _rectangleOneVFB, _rectangleTwoVFB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initShapes();

		_rectangleOneProgram = loadProgram(_rectangleOneVertexShaderCode, _rectangleOneFragmentShaderCode);
		_rectangleTwoProgram = loadProgram(_rectangleTwoVertexShaderCode, _rectangleTwoFragmentShaderCode);

		_rectangleOneAVertexLocation = GLES20.glGetAttribLocation(_rectangleOneProgram, "aPosition");
		_rectangleTwoAVertexLocation = GLES20.glGetAttribLocation(_rectangleTwoProgram, "aPosition");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(_rectangleTwoProgram);
	    GLES20.glVertexAttribPointer(_rectangleTwoAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _rectangleTwoVFB);
	    GLES20.glEnableVertexAttribArray(_rectangleTwoAVertexLocation);
	    GLES20.glColorMask(false, true, false, true);
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	    GLES20.glColorMask(true, true, true, true);

		GLES20.glUseProgram(_rectangleOneProgram);
	    GLES20.glVertexAttribPointer(_rectangleOneAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _rectangleOneVFB);
	    GLES20.glEnableVertexAttribArray(_rectangleOneAVertexLocation);
	    GLES20.glColorMask(true, false, false, true);
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	    GLES20.glColorMask(true, true, true, true);
	}

	private void initShapes()  {
		float rectangleOneVFA[] = {
				0,		0,		0,
				0,		0.5f,	0,
				0.75f,	0.5f,	0,
				0.75f,	0.5f,	0,
				0.75f,	0,		0,
				0,		0,		0,
			};
		ByteBuffer rectangleOneVBB = ByteBuffer.allocateDirect(rectangleOneVFA.length * 4);
		rectangleOneVBB.order(ByteOrder.nativeOrder());
		_rectangleOneVFB = rectangleOneVBB.asFloatBuffer();
		_rectangleOneVFB.put(rectangleOneVFA);
		_rectangleOneVFB.position(0);

		float rectangleTwoVFA[] = {
				0,		0,		0,
				0,		-0.25f,	0,
				-0.5f,	-0.25f,	0,
				-0.5f,	-0.25f,	0,
				-0.5f,	0,		0,
				0,		0,		0,
			};
		ByteBuffer rectangleTwoVBB = ByteBuffer.allocateDirect(rectangleTwoVFA.length * 4);
		rectangleTwoVBB.order(ByteOrder.nativeOrder());
		_rectangleTwoVFB = rectangleTwoVBB.asFloatBuffer();
		_rectangleTwoVFB.put(rectangleTwoVFA);
		_rectangleTwoVFB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private int loadProgram(String vertexShaderCode, String fragmentShaderCode) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
        return program;
    }

	private final String _rectangleOneVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _rectangleOneFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,1.0,1);	\n"
		+	"}										\n";
	private final String _rectangleTwoVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _rectangleTwoFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(0.0,1.0,1.0,1);	\n"
		+	"}										\n";

}
