package com.apress.android.gltexture;

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
	private static final String TAG = "GLES20Renderer";
	private int _planeProgram;
	private int _planeAPositionLocation;
	private int _planeACoordinateLocation;
	private int _planeUMVPLocation;
	private int _planeUSamplerLocation;
	private FloatBuffer _planeVFB;
	private FloatBuffer _planeTFB;
	private ShortBuffer _planeISB;

	private float[] _RMatrix			= new float[16];
	private float[] _ViewMatrix			= new float[16];
	private float[] _ProjectionMatrix	= new float[16];
	private float[] _MVPMatrix			= new float[16];

	private int _textureId;
	public Context _context;
	private static volatile float _zAngle;

	public GLES20Renderer(Context context) {
		_context = context;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		initplane();

		float ratio	= (float) width / height;
		float zNear = 0.1f;
		float zFar = 1000;
		float fov = 0.95f; // 0.2 to 1.0
		float size = (float) (zNear * Math.tan(fov / 2));
		/*	
		 * 行为					变换				方法					[4*4]矩阵
		 * 
		    ------------- 视图变换								mVMatrix[16]
		   
		    眼睛相对物体的位置改变				Matrix.setLookAtM(mVMatrix, 0,//偏移量
												cx, cy, cz,//相机位置,
												tx, ty, tz,//观察点位置
												upx, upy, upz//顶部朝向
											)
											
			------------- 模型变换								mMMatrix[16]
			
			物体平移							Matrix.translateM(
												mMMatrix, 0,//偏移量
												x, y, z//平移量
											)
			物体按坐标缩放比例缩放				Matrix.scaleM(
											 	mMMatrix,
											 	sx,sy, sz//缩放因子
											)
			物体旋转							Matrix.rotateM(
												mMMatrix, 0,//偏移量
												angle,//旋转角度
												x, y, z//需要旋转的轴
											)
			------------- 投影变换								mPMatrix[16]
				
			凸透镜眼睛						Matrix.frustumM(
												mPMatrix, 0,//偏移量
												left,right,
												buttom,top,
												near,far//near>0
											)
			平面透镜眼睛						Matrix.orthoM(
												mPMatrix,
												0,//偏移量
												left,right,
												buttom,top,
												near,far//near>0
											)
											
			如果眼睛不是凸透镜而是平面透镜, 则不会产生近小远大的效果
			
			
			 变换叠加: 可通过变换矩阵相乘得出想要的变换矩阵： 
			 Matrix.multiplyMM(
				mMVPMatrix,0,	//存放结果的总变换矩阵 和 偏移量
				mMatrixLeft,0, 	//左矩阵偏移量
				mMtrixRight,0 	//右矩阵偏移量
				
			绘制方式
			 点
				GLES20.GL_POINTS
			线
				GLES20.GL_LINES
				GLES20.GL_LINE_STRIP
				GLES20.GL_LINE_LOOP
			面
				GLES20.TRIANGLES
				GLES20.TRIANGLE_TRIP//条带,可间断
				 				将最开始的两个顶点出发，然后遍历每个顶点(第三个顶点开始)，这些顶点将和他的前2个顶点一起组成一个三角形

								v1，v2，v3，v4 
								先v1, v2, v3, 再 v3, v2, v4 并且确定这些顶点可以形成某个多边形。
				GLES20.TRIANGLE_FAN
				// 扇面
				GL_TRIANGLE_FAN - 在跳过开始的2个顶点，然后遍历每个顶点
								让OpenGL将这些'顶点'和它'前一个'，以及数组的'第一个顶点'一起组成一个'三角形' 
								v1，v2，v3，v4 
								先v1，v2，v3再v1，v3，v4这样可以形成一个扇形状得图形
				
			非索引法: GLES20.glDrawArrays
			索引法 ：GLES20.glDrawElement
				                            
		 */
		Matrix.setLookAtM(_ViewMatrix, 0, 0, 0, 50, 0, 0, 0, 0, 1, 0);
		 

		Matrix.frustumM(_ProjectionMatrix, 0, -size, size, -size / ratio, size / ratio, zNear, zFar);
		//Matrix.perspectiveM(m, offset, fovy, aspect, zNear, zFar);
		Matrix.setIdentityM(_RMatrix, 0);

		_planeProgram = loadProgram(_planeVertexShaderCode, _planeFragmentShaderCode);

		_planeAPositionLocation = GLES20.glGetAttribLocation(_planeProgram, "aPosition");
		_planeACoordinateLocation = GLES20.glGetAttribLocation(_planeProgram, "aCoord");
		_planeUMVPLocation = GLES20.glGetUniformLocation(_planeProgram, "uMVP");
		_planeUSamplerLocation = GLES20.glGetUniformLocation(_planeProgram, "uSampler");

		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		_textureId = textures[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);
        InputStream is1 = _context.getResources().openRawResource(R.drawable.brick);
        Bitmap img1;
        try {
        	img1 = BitmapFactory.decodeStream(is1);
        	/*
        	 *  hdpi ldpi mdip xhdpi都是64*64pix
        	 *  
        	 *  '渲染表面'上绘制一个'纹理'
        	 *  纹理的'纹理元素'可能无法精确地映射到OpenGL生成的'片段'	
        	 *  
        	 *  当我们尽力把几个纹理元素挤进一个片段时，缩小就发生了 	GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST
        	 *  当我们把一个纹理元素扩展到许多片段时，放大就发生了 	GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST
        	 *	
        	 *	纹理过滤器:最近邻过滤GL_NEAREST和双线性插值GL_LINEAR
        	 *
        	 *	最近邻过滤GL_NEAREST:
        	 *		放大纹理时，它的锯齿效果
        	 *		缩小纹理时，因为'没有足够的片段'用来绘制'所有的纹理单元'，许多细节将会丢失
        	 *
        	 *	双线性插值GL_LINEAR:(与平滑着色一样)
        	 *		平滑像素之间的过渡
        	 *		双线性插值，是因为它是沿着两个维度插值的
        	 *
        	 *	GL_LINEAR确点:
        	 *		对于缩小到超过一定的大小时,有越多的纹理元素拥挤到每一个片段,LINEAR不好用
        	 *		因为OpenGL的双线性过滤只给每个片段使用四个纹理元素
        	 *		而且 每一帧都要选择不同的纹理元素，这还会引起噪音以及移动中的物体闪烁	
        	 *	
        	 *
        	 *	MIP贴图技术:
        	 *		生成一组优化过的不同大小的纹理
        	 *		当生成这组纹理的时候，OpenGL会使用所有的纹理元素生成'每个级别'的'纹理'
        	 *		当过滤纹理时，还要确保所有的纹理元素都能被使用。
        	 *		在渲染时，OpenGL会根据'每个片段的纹理元素数量'为每个'片段'选择'最合适的级别'。
        	 *
        	 *		使用MIP贴图，会占用更多的内存
        	 *
        	 *		MIP贴图的使用，OpenGL将选择最合适的纹理级别，然后用优化过的纹理做双线性插值
        	 *
        	 *	双线性插值来使用MIP贴图 --> 帧间有时候能看到明显的跳跃或者线条
        	 *	三线性过滤--> 每个片段总共要使用8个纹理元素插值
        	 *  三线性插值来使用MIP贴图 --> 更平滑的图像
        	 *  
        	 *  GL_NEAREST	最近邻过滤
        	 *  GL_NEAREST_MIPMAP_NEAREST  使用MIP贴图(每个级别内部用NEAREST)的最近邻过滤
        	 *  GL_NEAREST_MIPMAP_LINEAR 使用MIP贴图级别之间插值的最近邻过滤
        	 *  GL_LINEAR   双线性插值
        	 *  GL_LINEAR_MIPMAP_NEAREST  使用MIP贴图的双线性插值
        	 *  GL_LINEAR_MIPMAP_LINEAR  三线性插值（使用MIP贴图级别之间插值的双线性过滤）
        	 *  
        	 *  每种情况允许的纹理过滤模式
				缩小 	
					GL_NEAREST
					GL_NEAREST_MIPMAP_NEAREST
					GL_NEAREST_MIPMAP_LINEAR
					GL_LINEAR
					GL_LINEAR_MIPMAP_NEAREST
					GL_LINEAR_MIPMAP_LINEAR
				放大 	
					GL_NEAREST
					GL_LINEAR
        	 *
        	 */
        	
        	Log.d(TAG , String.format("dpi %d w %d h %d", 
        		 	img1.getDensity(),
                	img1.getHeight() ,
                	img1.getWidth() 
        			));
       
        	
        	
        } finally {
        	try {
        		is1.close();
        	} catch(IOException e) {
        		//e.printStackTrace();
        	}
        }
		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);  
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		
		// '纹理的尺寸'也有一个最大值
		//  它根据不同的实现而变化
		//  但是通常都比较大，比如2048*2048
		//  纹理不必是正方形，但是每个维度都应该是'2的幂 POT '
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img1, 0);
		 
		
		GLES20.glUseProgram(_planeProgram);
		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);
		GLES20.glUniform1i(_planeUSamplerLocation, 0);
		
	}

	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(_RMatrix, 0);
		Matrix.rotateM(_RMatrix, 0, _zAngle, 0, 0, 1);
		Matrix.multiplyMM(_MVPMatrix, 0, _ViewMatrix, 0, _RMatrix, 0);
		Matrix.multiplyMM(_MVPMatrix, 0, _ProjectionMatrix, 0, _MVPMatrix, 0);
		Matrix.setIdentityM(_MVPMatrix, 0);
		
//		GLES20.glUseProgram(_planeProgram);
//
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);
//		GLES20.glUniform1i(_planeUSamplerLocation, 0);

		GLES20.glUniformMatrix4fv(_planeUMVPLocation, 1, false, _MVPMatrix, 0);
		
		// '属性' 与  '组成成分数量(组件数量)'
		//  把内存的数据 划分成 size个属性 每个size属性大小是 size * GLES20.GL_FLOAT 个字节 
		//  每个属性起始地址 相差 stride 个字节 
		
		// size 指定每个顶点属性的组件数量。必须为1、2、3或者4。初始值为4。（如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
		// type 指定数组中每个组件的数据类型  GL_UNSIGNED_BYTE,  GL_FLOAT
		// normalized 指定当被访问时,固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）
		// stride 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0
		// GLES20.glVertexAttribPointer(_planeAPositionLocation, 3, GLES20.GL_FLOAT, false, 3*4, _planeVFB);
		GLES20.glVertexAttribPointer(_planeAPositionLocation,4 , GLES20.GL_FLOAT, false, 4*4, _planeVFB);
		
		
		GLES20.glEnableVertexAttribArray(_planeAPositionLocation);
        GLES20.glVertexAttribPointer(_planeACoordinateLocation, 2, GLES20.GL_FLOAT, false, 8, _planeTFB);
        GLES20.glEnableVertexAttribArray(_planeACoordinateLocation);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3, GLES20.GL_UNSIGNED_SHORT, _planeISB);
		System.gc();
	}

	public static void setZAngle(float angle) {
		GLES20Renderer._zAngle = angle;
	}

	public static float getZAngle() {
		return GLES20Renderer._zAngle;
	}

	private void initplane() {
		/*
		 * 一个维度叫做S，而另一个称为T。
		 * 
		 * 当我们想要把一个纹理应用于一个三角形或一组三角形的时候，
		 * 我们要为每个'顶点'指定一组'ST纹理坐标'，
		 * 
		 * 以便OpenGL知道需要用‘哪个纹理’的‘哪个部分’画到每个三角形上
		 * 
		 * 这些纹理坐标有时也会被称为‘UV纹理坐标‘
		 * 
		 * 纹理坐标 作为varying  会插值  
		 * 
		 * 插值纹理坐标 会传到 片段着色器 (varying)
		 */
//		float[] planeVFA = {
//				10.000000f,-10.000000f,0.000000f, 	1.f,
//				-10.000000f,-10.000000f,0.000000f, 	1.f,
//				10.000000f,10.000000f,0.000000f, 	1.f,
//				-10.000000f,10.000000f,0.000000f,	1.f, 
		// 如果最后一个是0的话  没有显示 , 但是可以不写(默认w坐标是1)
		//  
//		};
		
		// 如果不作MVP的话  坐标要改成归一化
		float[] planeVFA = {
			1.000000f,-1.00000f,0.000000f,1.f , 
			-1.000000f,-1.000000f,0.000000f,1.f , 
			1.000000f,1.000000f,0.000000f,1.f , 
			-1.000000f,1.000000f,0.000000f,1.f , 
		};

		/*
		 * android使用 左上角 作为纹理空间的 0,0
		 * 
		 * OpenGL使用 左下角 作为纹理空间的 0,0
		 * 
		 * 也就是给到 Texture2D
		 * 
		 * (0,0)
		 * .--------------------- S (1,0)
		 * |
		 * |       /\
		 * |      /  \
		 * |     /----\
		 * |    /      \_
		 * |
		 * |                    (1,1)
		 * T  (0,1)
		 */
		float[] planeTFA = {
				// 1,0, 0,0, 1,1, 0,1 
				 0.5f,0.5f, 0,1, 3,0, 0,0   
		};
		/* 
		 * 超过1的纹理坐标 根据纹理环绕方式 GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT 重复 
		 * 												或者  GLES20.GL_CLAMP_TO_EDGE 边缘截取
		 * 													 GLES20.GL_MIRRORED_REPEAT 镜像重复 
		 * 
		 * (0,0)            (1,0)           (2,0)            (3.0)
		 * .------------------.-----------------.----------------.-------- > S 
		 * |
		 * |       /\  					/\				 /\
		 * |      /  \				   /  \				/  \
		 * |     /----\				  /----\		   /----\
		 * |    /      \_			 /      \_        /      \_ 
		 * |
		 * .(0,1)             .(1,1)
		 * |                   
		 * T           
		 * 
		 */

		short[] planeISA = { // 顶点坐标 和 纹理坐标 属性 中 获取 第2/3/1的属性
				2,3,1,
				//0,2,1,
				/*  两个三角形 相同方向
				 *     3<--------- 2 
				 *      |        / ^
				 *      |     /    |
				 *      |   /      |
				 *    1 \/  ------> 0
				 */
		};

		ByteBuffer planeVBB = ByteBuffer.allocateDirect(planeVFA.length * 4);
		planeVBB.order(ByteOrder.nativeOrder());
		_planeVFB = planeVBB.asFloatBuffer();
		_planeVFB.put(planeVFA);
		_planeVFB.position(0);

		ByteBuffer planeTBB = ByteBuffer.allocateDirect(planeTFA.length * 4);
		planeTBB.order(ByteOrder.nativeOrder());
		_planeTFB = planeTBB.asFloatBuffer();
		_planeTFB.put(planeTFA);
		_planeTFB.position(0);

		ByteBuffer planeIBB = ByteBuffer.allocateDirect(planeISA.length * 2);
		planeIBB.order(ByteOrder.nativeOrder());
		_planeISB = planeIBB.asShortBuffer();
		_planeISB.put(planeISA);
		_planeISB.position(0);
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

	//
	private final String _planeVertexShaderCode = 
			"attribute vec4 aPosition;			\n"
		+	"attribute vec2 aCoord;				\n"
		+	"varying vec2 vCoord;				\n"
		+	"uniform mat4 uMVP;					\n"
		+	"void main() {						\n"
		+	" gl_Position =  uMVP *aPosition;	\n"
		+	" vCoord = aCoord;					\n"
		+	"}									\n";

	private final String _planeFragmentShaderCode = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH				\n"
		+	"precision highp float;							\n"
		+	"#else											\n"
		+	"precision mediump float;						\n"
		+	"#endif											\n"
		+	"varying vec2 vCoord;							\n"
		+	"uniform sampler2D uSampler;					\n"
		+	"void main() {									\n"
		+	" gl_FragColor = texture2D(uSampler,vCoord);	\n"
		+	"}												\n";

}
