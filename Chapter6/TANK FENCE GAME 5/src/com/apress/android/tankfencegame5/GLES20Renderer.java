package com.apress.android.tankfencegame5;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class GLES20Renderer implements Renderer {
	private int _tankProgram;
	private int _planeProgram;
	private int _enemyProgram;
	private int _missilesProgram;

	private int _tankAPositionLocation;
	private int _planeAPositionLocation;
	private int _enemyAPositionLocation;
	private int _tankUMVPLocation;
	private int _planeUMVPLocation;
	private int _enemiesUMLocation;
	private int _enemiesUVPLocation;

	private int _missilesUVPLocation;
	private int _missilesAPositionLocation;

	private FloatBuffer _tankVFB;
	private ShortBuffer _tankISB;
	private FloatBuffer _planeVFB;
	private ShortBuffer _planeISB;
	private FloatBuffer _enemyVFB;
	private ShortBuffer _enemyISB;
	private FloatBuffer _missilesVFB;
	private ShortBuffer _missilesISB;

	private int[] _tankBuffers		= new int[2];
	private int[] _planeBuffers		= new int[2];
	private int[] _enemyBuffers		= new int[2];
	private int[] _missilesBuffers	= new int[2];

	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _planeVPMatrix		= new float[16];
	private float[] _tankTMatrix		= new float[16];
	private float[] _tankRMatrix		= new float[16];
	private float[] _tankMVPMatrix		= new float[16];
	private float[] _enemiesVPMatrix	= new float[16];
	private float[] _missilesMMatrix	= new float[16];
	private float[] _missilesVPMatrix	= new float[16];
	private final float[] _tankCenter	= new float[]{0,0,0,1};

	private static volatile float _zAngle;
	public static volatile boolean _buttonMissilePressed = false;
	private static List<Missile> _missiles = new ArrayList<Missile>(100);
	private static List<Enemy> _enemies = new ArrayList<Enemy>(10);

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		System.gc();

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);

		Counter.reset();

		inittank();
		initplane();
		initenemy();

		_tankProgram		= loadProgram(_tankVertexShaderCode, _tankFragmentShaderCode);
		_planeProgram		= loadProgram(_planeVertexShaderCode, _planeFragmentShaderCode);
		_enemyProgram		= loadProgram(_enemyVertexShaderCode, _enemyFragmentShaderCode);
		_missilesProgram	= loadProgram(_missilesVertexShaderCode, _missilesFragmentShaderCode);

		_tankAPositionLocation		= GLES20.glGetAttribLocation(_tankProgram, "aPosition");
		_tankUMVPLocation			= GLES20.glGetUniformLocation(_tankProgram, "uMVP");
		_planeAPositionLocation		= GLES20.glGetAttribLocation(_planeProgram, "aPosition");
		_planeUMVPLocation			= GLES20.glGetUniformLocation(_planeProgram, "uMVP");
		_enemyAPositionLocation		= GLES20.glGetAttribLocation(_enemyProgram, "aPosition");
		_enemiesUVPLocation			= GLES20.glGetUniformLocation(_enemyProgram, "uVP");
		_enemiesUMLocation			= GLES20.glGetUniformLocation(_enemyProgram, "uM");
		_missilesAPositionLocation	= GLES20.glGetAttribLocation(_missilesProgram, "aPosition");
		_missilesUVPLocation		= GLES20.glGetUniformLocation(_missilesProgram, "uVP");

		// 10.0005, 10.0, 0.1005
		GLES20Renderer._enemies.add(new Enemy(2 * 10.0005f, 2 * 10.0f, 0, 1.00005f));
		GLES20Renderer._enemies.add(new Enemy(-4 * 10.0005f, 2 * 10.0f, 0, -1.00005f));
		GLES20Renderer._enemies.add(new Enemy(-4 * 10.0005f, -4 * 10.0f, 0, 1.00005f));
		GLES20Renderer._enemies.add(new Enemy(2 * 10.0005f, -4 * 10.0f, 0, -1.00005f));
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		System.gc();

		GLES20.glViewport(0, 0, width, height);

		float ratio	= (float) width / height;
		float zNear	= 0.1f;
		float zFar	= 1000;
		float fov	= 0.95f; // 0.2 to 1.0
		float size	= (float) (zNear * Math.tan(fov / 2));
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 120, 0, 0, 0, 0, 1, 0);
		// Matrix.rotateM(_ViewMatrix, 0, 90, 1, 0, 0);
		// Matrix.setLookAtM(_ViewMatrix, 0, 0, -20, 50, 0, 0, 0, 0, 1, 0);
		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		Matrix.multiplyMM(_planeVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);
		System.arraycopy(_planeVPMatrix, 0, _missilesVPMatrix, 0, 16);
		System.arraycopy(_planeVPMatrix, 0, _enemiesVPMatrix, 0, 16);
		// Matrix.multiplyMM(_missilesVPMatrix, 0, _ProjectionMatrix, 0, _ViewMatrix, 0);
		Matrix.setIdentityM(_tankTMatrix, 0);
		Matrix.setIdentityM(_tankRMatrix, 0);
	}

	public void onDrawFrame(GL10 gl) {
		System.gc();

		long deltaTime,startTime,endTime;
		startTime	= SystemClock.uptimeMillis() % 1000;
		gl.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if(GLES20Renderer._zAngle >= 360) {
			GLES20Renderer._zAngle = GLES20Renderer._zAngle - 360;
		}
		if(GLES20Renderer._zAngle <= -360) {
			GLES20Renderer._zAngle = GLES20Renderer._zAngle + 360;
		}

		updateModel(Counter.getUpDownValue(), GLES20Renderer._zAngle);
		if(GLES20Renderer._missiles.size() > 0) {
			initMissiles();
		}
		if(GLES20Renderer._enemies.size() > 0) {
			// initenemy();
			float[] enemiesMMatrix	= new float[16];

			ListIterator<Enemy> enemiesIterator	= GLES20Renderer._enemies.listIterator();
			while(enemiesIterator.hasNext()) {
				Enemy enemy	= enemiesIterator.next();
				enemy.interpolateXY();

				if((enemy.getDestinationPositionX() > -20 && enemy.getDestinationPositionX() < 0) 
						&& (enemy.getDestinationPositionY() > -20 && enemy.getDestinationPositionY() < 0)) {
					enemiesIterator.remove();
				} else {
					float dx, dy;
					Matrix.setIdentityM(enemiesMMatrix, 0);
					Matrix.translateM(enemiesMMatrix, 0, enemy.getSourcePositionX(), enemy.getSourcePositionY(), 0);
					Log.d("enemy.getDestinationPositionX()", Float.valueOf(enemy.getDestinationPositionX()).toString());

					dx	= enemy.getDestinationPositionX() - enemy.getSourcePositionX();
					dy	= enemy.getDestinationPositionY() - enemy.getSourcePositionY();
					Matrix.translateM(enemiesMMatrix, 0, dx, dy, 0);
					renderEnemies(enemiesMMatrix);
				}
			}
		}
		renderModel(gl);

		endTime		= SystemClock.uptimeMillis() % 1000;
		deltaTime	= Math.abs(endTime - startTime);
		if (deltaTime < 20) {
			try {
				Thread.sleep(20 - deltaTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateModel(int upDown, float zAngle) {
		Matrix.setIdentityM(_tankTMatrix, 0);
		Matrix.setIdentityM(_tankRMatrix, 0);
		Matrix.translateM(_tankTMatrix, 0, 0, upDown, 0);
		Matrix.rotateM(_tankRMatrix, 0, zAngle, 0, 0, 1);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _tankRMatrix, 0, _tankTMatrix, 0);
		System.arraycopy(_tankMVPMatrix, 0, _missilesMMatrix, 0, 16);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ViewMatrix, 0, _tankMVPMatrix, 0);
		Matrix.multiplyMM(_tankMVPMatrix, 0, _ProjectionMatrix, 0, _tankMVPMatrix, 0);

		float[] missileCenter	= new float[4];
		// Matrix.multiplyMM(_missilesMMatrix, 0, _tankRMatrix, 0, _tankTMatrix, 0);
		Matrix.multiplyMV(missileCenter, 0, _missilesMMatrix, 0, _tankCenter, 0);

		if(GLES20Renderer._buttonMissilePressed) {
			GLES20Renderer._buttonMissilePressed	= false;
			Missile missile	= new Missile(missileCenter[0], missileCenter[1], missileCenter[2], zAngle);
			GLES20Renderer._missiles.add(missile);
		}

		ListIterator<Missile> missilesIterator		= GLES20Renderer._missiles.listIterator();
		while(missilesIterator.hasNext()) {
			Missile missile	= missilesIterator.next();
			if(missile.getDestinationPositionX() < -50 || missile.getDestinationPositionX() > 50 
					|| missile.getDestinationPositionY() < -30 || missile.getDestinationPositionY() > 30) {
				missilesIterator.remove();
			} else {
				missile.interpolateXY();
			}
		}
	}

	private void renderModel(GL10 gl) {
		GLES20.glUseProgram(_tankProgram);
		GLES20.glUniformMatrix4fv(_tankUMVPLocation, 1, false, _tankMVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glVertexAttribPointer(_tankAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_tankAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 84, GLES20.GL_UNSIGNED_SHORT, 0);

		GLES20.glUseProgram(_planeProgram);
		GLES20.glUniformMatrix4fv(_planeUMVPLocation, 1, false, _planeVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[0]);
		GLES20.glVertexAttribPointer(_planeAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_planeAPositionLocation);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _planeBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

		GLES20.glUseProgram(_missilesProgram);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _missilesBuffers[0]);
		GLES20.glVertexAttribPointer(_missilesAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_missilesAPositionLocation);
		GLES20.glUniformMatrix4fv(_missilesUVPLocation, 1, false, _missilesVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _missilesBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_POINTS, GLES20Renderer._missiles.size(), GLES20.GL_UNSIGNED_SHORT, 0);
	}

	private void renderEnemies(float[] enemiesMMatrix) {
		GLES20.glUseProgram(_enemyProgram);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[0]);
		GLES20.glVertexAttribPointer(_enemyAPositionLocation, 3, GLES20.GL_FLOAT, false, 12, 0);
		GLES20.glEnableVertexAttribArray(_enemyAPositionLocation);
		GLES20.glUniformMatrix4fv(_enemiesUMLocation, 1, false, enemiesMMatrix, 0);
		GLES20.glUniformMatrix4fv(_enemiesUVPLocation, 1, false, _enemiesVPMatrix, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _enemyBuffers[1]);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 24, GLES20.GL_UNSIGNED_SHORT, 0);
	}

	private void initMissiles() {
		ListIterator<Missile> missileIterator	= _missiles.listIterator();
		float[] missilesVFA	= new float[GLES20Renderer._missiles.size() * 3];
		short[] missilesISA	= new short[GLES20Renderer._missiles.size()];
		int vertexIterator	= -1;
		short indexIterator	= -1;
		while(missileIterator.hasNext()) {
			Missile missile	= missileIterator.next();
			vertexIterator++;
			missilesVFA[vertexIterator]	= missile.getDestinationPositionX();
			vertexIterator++;
			missilesVFA[vertexIterator]	= missile.getDestinationPositionY();
			vertexIterator++;
			missilesVFA[vertexIterator]	= missile.getDestinationPositionZ();
			indexIterator++;
			missilesISA[indexIterator]	= indexIterator;
		}

		ByteBuffer missilesVBB	= ByteBuffer.allocateDirect(missilesVFA.length * 4);
		missilesVBB.order(ByteOrder.nativeOrder());
		_missilesVFB			= missilesVBB.asFloatBuffer();
		_missilesVFB.put(missilesVFA);
		_missilesVFB.position(0);

		ByteBuffer missilesIBB	= ByteBuffer.allocateDirect(missilesISA.length * 2);
		missilesIBB.order(ByteOrder.nativeOrder());
		_missilesISB			= missilesIBB.asShortBuffer();
		_missilesISB.put(missilesISA);
		_missilesISB.position(0);

		GLES20.glGenBuffers(2, _missilesBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _missilesBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, missilesVFA.length * 4, _missilesVFB, GLES20.GL_DYNAMIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _missilesBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, missilesISA.length * 2, _missilesISB, GLES20.GL_DYNAMIC_DRAW);
	}

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle	= angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	private void inittank() {
		float[] tankVFA = {
				-1.562685f,-2.427994f,0.000000f,
				-1.562685f,1.139500f,0.000000f,
				1.562685f,1.139500f,0.000000f,
				1.562685f,-2.427994f,0.000000f,
				-1.562685f,-2.427994f,2.000000f,
				-1.562685f,1.139500f,2.000000f,
				1.562685f,1.139500f,2.000000f,
				1.562685f,-2.427994f,2.000000f,
				-0.781342f,1.139500f,0.500000f,
				0.781342f,1.139500f,0.500000f,
				-0.781342f,1.139500f,1.500000f,
				0.781342f,1.139500f,1.500000f,
				-0.781342f,3.437026f,0.500000f,
				0.781342f,3.437026f,0.500000f,
				-0.781342f,3.437026f,1.500000f,
				0.781342f,3.437026f,1.500000f,
		};

		short[] tankISA = {
				4,5,1,
				2,1,8,
				6,7,3,
				4,0,7,
				0,1,2,
				7,6,5,
				9,8,12,
				5,6,11,
				6,2,9,
				10,8,1,
				14,15,13,
				11,9,13,
				14,12,8,
				10,11,15,
				0,4,1,
				9,2,8,
				2,6,3,
				7,0,3,
				3,0,2,
				4,7,5,
				13,9,12,
				10,5,11,
				11,6,9,
				5,10,1,
				12,14,13,
				15,11,13,
				10,14,8,
				14,10,15,
		};

		ByteBuffer tankVBB	= ByteBuffer.allocateDirect(tankVFA.length * 4);
		tankVBB.order(ByteOrder.nativeOrder());
		_tankVFB			= tankVBB.asFloatBuffer();
		_tankVFB.put(tankVFA);
		_tankVFB.position(0);

		ByteBuffer tankIBB	= ByteBuffer.allocateDirect(tankISA.length * 2);
		tankIBB.order(ByteOrder.nativeOrder());
		_tankISB			= tankIBB.asShortBuffer();
		_tankISB.put(tankISA);
		_tankISB.position(0);

		GLES20.glGenBuffers(2, _tankBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _tankBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, tankVFA.length * 4, _tankVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _tankBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, tankISA.length * 2, _tankISB, GLES20.GL_STATIC_DRAW);
	}

	private void initplane() {
		float[] planeVFA = {
				10.000000f,-10.000000f,0.000000f,
				-10.000000f,-10.000000f,0.000000f,
				10.000000f,10.000000f,0.000000f,
				-10.000000f,10.000000f,0.000000f,
		};

		short[] planeISA = {
				2,3,1,
				0,2,1,
		};

		ByteBuffer planeVBB	= ByteBuffer.allocateDirect(planeVFA.length * 4);
		planeVBB.order(ByteOrder.nativeOrder());
		_planeVFB			= planeVBB.asFloatBuffer();
		_planeVFB.put(planeVFA);
		_planeVFB.position(0);

		ByteBuffer planeIBB	= ByteBuffer.allocateDirect(planeISA.length * 2);
		planeIBB.order(ByteOrder.nativeOrder());
		_planeISB			= planeIBB.asShortBuffer();
		_planeISB.put(planeISA);
		_planeISB.position(0);

		GLES20.glGenBuffers(2, _planeBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _planeBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, planeVFA.length * 4, _planeVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _planeBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, planeISA.length * 2, _planeISB, GLES20.GL_STATIC_DRAW);
	}

	private void initenemy() {
		float[] enemyVFA = {
				10.816487f,8.585787f,-0.003767f,
				10.816488f,11.414213f,-0.003767f,
				8.367024f,10.000001f,0.007534f,
				10.817415f,8.585787f,0.197270f,
				10.817416f,11.414213f,0.197270f,
				8.367951f,10.000001f,0.208572f,
		};

		short[] enemyISA = {
				1,0,2,
				5,3,4,
				0,1,4,
				1,2,5,
				2,0,3,
				3,0,4,
				4,1,5,
				5,2,3,
		};

		ByteBuffer enemyVBB	= ByteBuffer.allocateDirect(enemyVFA.length * 4);
		enemyVBB.order(ByteOrder.nativeOrder());
		_enemyVFB			= enemyVBB.asFloatBuffer();
		_enemyVFB.put(enemyVFA);
		_enemyVFB.position(0);

		ByteBuffer enemyIBB	= ByteBuffer.allocateDirect(enemyISA.length * 2);
		enemyIBB.order(ByteOrder.nativeOrder());
		_enemyISB			= enemyIBB.asShortBuffer();
		_enemyISB.put(enemyISA);
		_enemyISB.position(0);

		GLES20.glGenBuffers(2, _enemyBuffers, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _enemyBuffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, enemyVFA.length * 4, _enemyVFB, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _enemyBuffers[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, enemyISA.length * 2, _enemyISB, GLES20.GL_STATIC_DRAW);
	}

	private int loadShader(int type, String source)  {
		int shader	= GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		return shader;
	}

	private int loadProgram(String vertexShaderCode, String fragmentShaderCode) {
		int vertexShader	= loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader	= loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		int program			= GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
        return program;
    }

	private final String _tankVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _tankFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(0,0,1,1);			\n"
		+	"}										\n";

	private final String _planeVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uMVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uMVP * vertex;										\n"
		+	"}																	\n";

	private final String _planeFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(1);				\n"
		+	"}										\n";

	private final String _enemyVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uM;													\n"
		+	"uniform mat4 uVP;													\n"
		+	"void main() {														\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uM * vertex;										\n"
		+	" gl_Position = uVP * gl_Position;									\n"
		+	"}																	\n";

	private final String _enemyFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH		\n"
		+	"precision highp float;					\n"
		+	"#else									\n"
		+	"precision mediump float;				\n"
		+	"#endif									\n"
		+	"void main() {							\n"
		+	" gl_FragColor = vec4(1,0,0,1);			\n"
		+	"}										\n";

	private final String _missilesVertexShaderCode = 
			"attribute vec3 aPosition;											\n"
		+	"uniform mat4 uVP;													\n"
		+	"varying vec3 vColor;												\n"
		+	"void main() {														\n"
		+	" gl_PointSize = 15.0;												\n"
		+	" vColor = aPosition;												\n"
		+	" vec4 vertex = vec4(aPosition[0],aPosition[1],aPosition[2],1.0);	\n"
		+	" gl_Position = uVP * vertex;										\n"
		+	"}																	\n";

	private final String _missilesFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH							\n"
		+	"precision highp float;										\n"
		+	"#else														\n"
		+	"precision mediump float;									\n"
		+	"#endif														\n"
		+	"varying vec3 vColor;										\n"
		+	"void main() {												\n"
		+	" float color[3];											\n"
		+	" color[0] = vColor[0] * vColor[1] * 0.05;					\n"
		+	" color[1] = color[0] * vColor[1] * 0.05;					\n"
		+	" color[2] = color[1] * vColor[0] * 0.05;					\n"
		+	" gl_FragColor = vec4(color[0], color[1], color[2], 1.0);	\n"
		+	"}															\n";

}
