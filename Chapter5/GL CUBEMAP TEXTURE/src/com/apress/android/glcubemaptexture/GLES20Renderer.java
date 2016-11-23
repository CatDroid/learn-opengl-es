package com.apress.android.glcubemaptexture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class GLES20Renderer implements Renderer {
	private int _cubeProgram;
	private FloatBuffer _cubeVFB;
	private ShortBuffer _cubeISB;
	private FloatBuffer _cubeTFB;
	private int _cubeAPositionLocation;
	private int _cubeUMVPLocation;
	private int _cubeUSamplerLocation;
	private int _cubeACoordinateLocation;

	private float[] _ViewMatrix = new float[16];
	private float[] _ProjectionMatrix = new float[16];
	private float[] _MVPMatrix = new float[16];
	private float[] _RMatrix = new float[16];

	private int _textureId;
	public Context _context;
	private static volatile float _zAngle;

	public GLES20Renderer(Context context) {
		_context = context;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.5f, 1);
	    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	    GLES20.glDepthFunc(GLES20.GL_LEQUAL);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width/2, height/2);
		initShapes();

		_cubeProgram = loadProgram(_cubeVertexShaderCode, _cubeFragmentShaderCode);

		_cubeAPositionLocation = GLES20.glGetAttribLocation(_cubeProgram, "aPosition");
		_cubeUMVPLocation = GLES20.glGetUniformLocation(_cubeProgram, "uMVP");
		_cubeACoordinateLocation = GLES20.glGetAttribLocation(_cubeProgram, "aCoord");
		_cubeUSamplerLocation = GLES20.glGetUniformLocation(_cubeProgram, "uSampler");

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.4f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 10, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.setIdentityM(_RMatrix, 0);

		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		_textureId = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, _textureId);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST); // GLES20.GL_LINEAR
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is1 = _context.getResources().openRawResource(R.drawable.brick1);
        Bitmap img1;
        try {
        	img1 = BitmapFactory.decodeStream(is1);
        } finally {
        	try {
        		is1.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, img1, 0);
        InputStream is2 = _context.getResources().openRawResource(R.drawable.brick2);
        Bitmap img2;
        try {
        	img2 = BitmapFactory.decodeStream(is2);
        } finally {
        	try {
        		is2.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, img2, 0);
        InputStream is3 = _context.getResources().openRawResource(R.drawable.brick3);
        Bitmap img3;
        try {
        	img3 = BitmapFactory.decodeStream(is3);
        } finally {
        	try {
        		is3.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, img3, 0);
        InputStream is4 = _context.getResources().openRawResource(R.drawable.brick4);
        Bitmap img4;
        try {
        	img4 = BitmapFactory.decodeStream(is4);
        } finally {
        	try {
        		is4.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, img4, 0);
        InputStream is5 = _context.getResources().openRawResource(R.drawable.brick5);
        Bitmap img5;
        try {
        	img5 = BitmapFactory.decodeStream(is5);
        } finally {
        	try {
        		is5.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, img5, 0);
        InputStream is6 = _context.getResources().openRawResource(R.drawable.brick6);
        Bitmap img6;
        try {
        	img6 = BitmapFactory.decodeStream(is6);
        } finally {
        	try {
        		is6.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, img6, 0);
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(_RMatrix, 0);
		Matrix.rotateM(_RMatrix, 0, 30, 1, 0, 0);
		Matrix.rotateM(_RMatrix, 0, 30, 0, 1, 0);
		Matrix.rotateM(_RMatrix, 0, _zAngle, 0, 0, 1);
		Matrix.multiplyMM(_MVPMatrix, 0, _ViewMatrix, 0, _RMatrix, 0);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _MVPMatrix, 0);

		GLES20.glUseProgram(_cubeProgram);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		checkError("glActiveTexture");
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, _textureId);
		checkError("glBindTexture");
		GLES20.glUniform1i(_cubeUSamplerLocation, 0);
		checkError("glUniform1i");

		GLES20.glVertexAttribPointer(_cubeAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, _cubeVFB);
		GLES20.glEnableVertexAttribArray(_cubeAPositionLocation);
        GLES20.glVertexAttribPointer(_cubeACoordinateLocation, 3, GLES20.GL_FLOAT, false, 12, _cubeTFB);
        GLES20.glEnableVertexAttribArray(_cubeACoordinateLocation);
		GLES20.glUniformMatrix4fv(_cubeUMVPLocation, 1, false, _MVPMatrix, 0);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 33, GLES20.GL_UNSIGNED_SHORT, _cubeISB);
	}

	private void initShapes()  {
		float[] cubeVFA = {
				-0.5f,-0.5f,0.5f,   0.5f,-0.5f,0.5f,   0.5f,0.5f,0.5f,   -0.5f,0.5f,0.5f,
				-0.5f,-0.5f,-0.5f,  0.5f,-0.5f,-0.5f,  0.5f,0.5f,-0.5f,  -0.5f,0.5f,-0.5f
				};

		short[] cubeISA = {
				0,4,5,  0,1,5,  5,6,2,  5,1,2,
				5,6,7,  5,4,7,  7,6,2,  7,3,2,
				7,3,0,  7,4,0,  0,3,2,  0,1,2
				};

		float[] cubeTFA = {
				-1,-1,1,   1,-1,1,   1,1,1,   -1,1,1,
				-1,-1,-1,  1,-1,-1,  1,1,-1,  -1,1,-1
				};

		ByteBuffer cubeVBB = ByteBuffer.allocateDirect(cubeVFA.length * 4);
		cubeVBB.order(ByteOrder.nativeOrder());
		_cubeVFB = cubeVBB.asFloatBuffer();
		_cubeVFB.put(cubeVFA);
		_cubeVFB.position(0);
		ByteBuffer cubeIBB = ByteBuffer.allocateDirect(cubeISA.length * 2);
		cubeIBB.order(ByteOrder.nativeOrder());
		_cubeISB = cubeIBB.asShortBuffer();
		_cubeISB.put(cubeISA);
		_cubeISB.position(0);
		ByteBuffer cubeTBB = ByteBuffer.allocateDirect(cubeTFA.length * 4);
		cubeTBB.order(ByteOrder.nativeOrder());
		_cubeTFB = cubeTBB.asFloatBuffer();
		_cubeTFB.put(cubeTFA);
		_cubeTFB.position(0);
	}

	private int loadShader(int type, String source)  {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		checkError("glShaderSource");
		GLES20.glCompileShader(shader);
		checkError("glCompileShader");
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

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle = angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	public void checkError(String function) {
		Log.d("Error : " + function, Integer.valueOf(GLES20.glGetError()).toString());
	}

	private final String _cubeVertexShaderCode = 
			"attribute vec4 aPosition;			\n"
		+	"attribute vec3 aCoord;				\n"
		+	"varying vec3 vCoord;				\n"
		+	"uniform mat4 uMVP;					\n"
		+	"void main() {						\n"
		+	" gl_Position = uMVP * aPosition;	\n"
		+	" vCoord = aCoord;					\n"
		+	"}									\n";

	private final String _cubeFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH				\n"
		+	"precision highp float;							\n"
		+	"#else											\n"
		+	"precision mediump float;						\n"
		+	"#endif											\n"
		+	"varying vec3 vCoord;							\n"
		+	"uniform samplerCube uSampler;					\n"
		+	"void main() {									\n"
		+	" gl_FragColor = textureCube(uSampler,vCoord);	\n"
		+	"}												\n";

}
