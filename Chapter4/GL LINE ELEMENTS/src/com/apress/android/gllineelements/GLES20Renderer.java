package com.apress.android.gllineelements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

public class GLES20Renderer implements Renderer {
	private int _lineProgram;
	private int _lineAVertexLocation;
	private FloatBuffer _lineVFB;
	private ShortBuffer _lineISB;

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initShapes();

		int lineVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _lineVertexShaderCode);
		int lineFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _lineFragmentShaderCode);
		_lineProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(_lineProgram, lineVertexShader);
		GLES20.glAttachShader(_lineProgram, lineFragmentShader);
		GLES20.glLinkProgram(_lineProgram);

		_lineAVertexLocation = GLES20.glGetAttribLocation(_lineProgram, "aPosition");
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(_lineProgram);
	    GLES20.glVertexAttribPointer(_lineAVertexLocation, 3, GLES20.GL_FLOAT, false, 0, _lineVFB);
	    GLES20.glEnableVertexAttribArray(_lineAVertexLocation);
	    GLES20.glLineWidth(3);
	    GLES20.glDrawElements(GLES20.GL_LINES, 8, GLES20.GL_UNSIGNED_SHORT, _lineISB);
	}

	private void initShapes()  {
		float lineVFA[] = {0.2f,0.2f,0.0f, -0.2f,0.2f,0.0f, -0.2f,-0.2f,0.0f, 0.2f,-0.2f,0.0f};
		ByteBuffer lineVBB = ByteBuffer.allocateDirect(lineVFA.length * 4);
		lineVBB.order(ByteOrder.nativeOrder());
		_lineVFB = lineVBB.asFloatBuffer();
		_lineVFB.put(lineVFA);
		_lineVFB.position(0);

		short lineISA[] = {0,1, 1,2, 2,3, 3,0};
		ByteBuffer lineIBB = ByteBuffer.allocateDirect(lineISA.length * 2);
		lineIBB.order(ByteOrder.nativeOrder());
		_lineISB = lineIBB.asShortBuffer();
		_lineISB.put(lineISA);
		_lineISB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private final String _lineVertexShaderCode = 
			"attribute vec4 aPosition;				\n"
		+	"void main() {							\n"
		+	" gl_Position = aPosition;				\n"
		+	"}										\n";

	private final String _lineFragmentShaderCode = 
			"void main() {							\n"
		+	" gl_FragColor = vec4(1.0,1.0,1.0,1);	\n"
		+	"}										\n";

}
