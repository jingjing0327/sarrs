package com.media.ffmpeg;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.component.player.hardwaredecode.MediaHardwareDecoder;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.Tools;

public class FFMpegPlayer extends MediaPlayer
{
	//父类型成功事件代号
	private static final int MEDIA_NOP = 0; // interface test message
	private static final int MEDIA_PREPARED = 0x100;
	private static final int MEDIA_PLAYBACK_COMPLETE = 1;
	private static final int MEDIA_BUFFERING_UPDATE = 2;
	private static final int MEDIA_SEEK_COMPLETE = 3;
	private static final int MEDIA_SET_VIDEO_SIZE = 4;
	private static final int MEDIA_DECODE_SUCESS = 0x400;
	private static final int MEDIA_SETDATASOURCE = 0x200;//未使用
	private static final int MEDIA_START = 0x300;//未使用
	private static final int MEDIA_HARDWARE = 0x600;//未使用
	private static final int MEDIA_AD_NUMBER = 50;
	private static final int MEDIA_BLOCK = 5;//卡顿
	private static final int MEDIA_END_BLOCK = 6; //卡顿结束
	private static final int MEDIA_START_DISPLAY = 7;//第一帧开始播放
	//父类型中错误事件代号
	public static final int MEDIA_PREPARED_FAIL = 0x101;
	public static final int MEDIA_SETDATASOURCE_FAIL = 0X201;
//	private static final int MEDIA_START_FAIL = 0x301;
	public static final int MEDIA_SOFTDECODE_FAIL = 0x401; //软硬解播放过程中错误
//	public static final int MEDIA_OPENGL_FAIL = 0x501;
	public static final int MEDIA_HARDDECODE_START_FAIL_SWITCH_TO_SOFTDECODE = 0x601; //硬解第一帧错误，需切换到软解
	public static final int MEDIA_HARDDECODE_DURATION_FAIL_SWITCH_TO_SOFTDECODE = 0x701; //硬解过程中错误，需切换到软解
	//private static final int MEDIA_START_BUFFERING = 5;
	
	//子类型错误
	private static final int PREPARE_NO_ERROR = 0x100;
			   //prepare成功
	public static final int 	PREPARE_OPEN_FILE_ERROR 				= 0x101;
	//打开url出错
	public static final int	PREPARE_FIND_INFO_ERROR 				= 0x102;
	 //获取流信息失败

	public static final int    PREPARE_VIDEO_NOSTREAM_ERROR 			= 0x103; 
	//没有视频流
	public static final int	PREPARE_VIDEO_NODECODER_ERROR 			= 0x104;	
	//找不到视频解码器
	public static final int	PREPARE_VIDEO_CODECOPEN_ERROR 			= 0x105;
	//打开视频解码器失败
	public static final int	PREPARE_VIDEO_MALLOC_ERROR 			= 0x106;
	//视频模块分配内存失败
				
	public static final int	PREPARE_AUDIO_NODECODER_ERROR			= 0x107;
	//找不到音频解码器
	public static final int	PREPARE_AUDIO_CODECOPEN_ERROR 			= 0x108;
	//音频解码器打开失败
	public static final int	PREPARE_AUDIO_SAMPLERATE_ERROR	= 0x109;
	 //音频采样率错误

	private static final int	SETDATASOURCE_NO_ERROR 					= 0x200;		
	//保留；未使用
	public static final int	SETDATASOURCE_URLTOOLONG_ERROR 		= 0x201;	
	//在setDataSource中，URL太长(超过4096字节)
	public static final int	SETDATASOURCE_ALLOCCONTEXT_ERROR 		= 0x202;
			//在setDataSource中，分配流的上下文失败

	private static final int	START_NO_ERROR 							= 0x300;
	//保留，未使用
	public static final int	START_STATE_ERROR 						= 0x301;
	//start时，状态错误
				
//	private static final int	DECODE_SFIRSTFRAME_SUCESS            	= 0x400;
//	 //软解码第一帧成功
//	private static final int	DECODE_HFIRSTFRAME_SUCESS            	= 0x401;
	 //硬解码第一帧成功
	public static final int	DECODE_VMALLOC_ERROR 					= 0x402;
	//视频解码时，内存分配失败
	public static final int	DECODE_NODECODER_ERROR 					= 0x403;
	//解码已经开始时，找不到解码器
	public static final int	DECODE_CODECOPEN_ERROR 					= 0x404;
	//解码已经开始时，解码器打开失败

	private static final int	HW_NO_ERROR 							= 0x600;
	 //保留，未使用
	public static final int	HW_SPSPPS_ERROR 						= 0x601;
	//硬解码时，获取sps; pps失败
	public static final int	HW_SPSPPS_MALLOC1_ERROR 				= 0x602;
	//硬解码时，内存分配失败;MALLOC1和MALLOC2标识不同位置的内存分配	
	public static final int	HW_SPSPPS_MALLOC2_ERROR 				= 0x603;
	//硬解码时，内存分配失败;MALLOC1和MALLOC2标识不同位置的内存分配		
	public static final int	HW_SPSPPS_FILLDATA_ERROR 				= 0x604;
	//硬解码时，向解码器写数据失败
 
	
//	private static final int MEDIA_ERROR = 100;
	private static final int MEDIA_INFO = 200;

	public static final int MEDIA_ERROR_UNKNOWN = 1;
	public static final int MEDIA_ERROR_SERVER_DIED = 100;
	public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
	public static final int MEDIA_INFO_UNKNOWN = 1;
	public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
	public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
	public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
	public static final int MEDIA_INFO_METADATA_UPDATE = 802;
	public static final int MEDIA_INFO_FRAMERATE_VIDEO = 900;
	public static final int MEDIA_INFO_FRAMERATE_AUDIO = 901;
	
	public static final int HARDWARE_DECODE = 1;
	public static final int SOFTWARE_DECODE = 0;
	
	public static final int MEDIA_BLOCK_START = 10001;
	public static final int MEDIA_BLOCK_END = 10002;

	private final static String TAG = "FFMpegPlayer";

	private int mNativeContext;
	private int mNativeData = 0;

	private Surface mSurface;
	private AudioTrack mTrack;
	private SurfaceHolder mSurfaceHolder;
	private static Rect mRect = null;
	private EventHandler mEventHandler;
	private PowerManager.WakeLock mWakeLock = null;
	private boolean mScreenOnWhilePlaying;
	private boolean mStayAwake;
    private static GLRenderControler mGlRenderControler;
    private MediaDecoder mVideoDecoder;
    private Context mContext;
	static
	{
		try
		{
			FFMpeg loadLib = new FFMpeg();
			native_init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public FFMpegPlayer()
	{
		Looper looper;
		if ((looper = Looper.myLooper()) != null)
		{
			mEventHandler = new EventHandler(this, looper);
		}
		else if ((looper = Looper.getMainLooper()) != null)
		{
			mEventHandler = new EventHandler(this, looper);
		}
		else
		{
			mEventHandler = null;
		}
//		native_init();
		native_setup(new WeakReference<FFMpegPlayer>(this));
	}

	public FFMpegPlayer( Context context )
	{
		Looper looper;
		if ((looper = Looper.myLooper()) != null)
		{
			mEventHandler = new EventHandler(this, looper);
		}
		else if ((looper = Looper.getMainLooper()) != null)
		{
			mEventHandler = new EventHandler(this, looper);
		}
		else
		{
			mEventHandler = null;
		}
//		native_init();
		native_setup(new WeakReference<FFMpegPlayer>(this));
		mContext = context;
	}

	private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj)
	{

		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}
		if (mp.mEventHandler != null)
		{
//			LogTag.i("postEventFromNative()" + ", what=" + what + ", arg1=" + arg1 + ", arg2=" + arg2 );
			Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
			mp.mEventHandler.sendMessage(m);
		}

	}

	public void setDisplay(SurfaceHolder sh)
	{
		mSurfaceHolder = sh;
		if (sh != null)
		{
			mSurface = sh.getSurface();
		}
		else
		{
			mSurface = null;
		}

		updateSurfaceScreenOn();
		//_setVideoSurface(mSurface);
	}

	public void start() throws IllegalStateException
	{
		stayAwake(true);
		_start();
	}

	public void stop() throws IllegalStateException
	{
		stayAwake(false);
		_stop();
	}

	public void pause() throws IllegalStateException
	{
		stayAwake(false);
		_pause();
	}

	public void prepareAsync() throws IllegalStateException
	{
		try
		{
			prepare();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void setDataSource(Context context, Uri uri)
	{
		try
		{
			setDataSource(uri.toString());
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private native void _setVideoSurface(Surface surface) throws IOException;

	public native void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException;

	public native void prepare() throws IOException, IllegalStateException;

	private native void _start() throws IllegalStateException;

	private native void _stop() throws IllegalStateException;

	private native void _pause() throws IllegalStateException;

	public native int getVideoWidth();

	public native int getVideoHeight();

	public native String getLastUrl();

	public native String getVersion();

	public native boolean isPlaying();

	public native void seekTo(int msec) throws IllegalStateException;

	public native int getCurrentPosition();

	public native int getDuration();

	private native void _release();

	private native void _reset();

	private native int native_suspend_resume(boolean isSuspend);

	public native void setAudioStreamType(int streamtype);

	private static native final void native_init() throws RuntimeException;

	private native final int native_setup(Object mediaplayer_this);

	private native final void native_finalize();

	public native void native_gl_resize(int w, int h);

	public native void native_gl_render();
	
	public native int getVideoRotate(String path) throws IOException, IllegalArgumentException, IllegalStateException;

	/**
	 * 设置缓冲大小
	 * @param video_size
	 * @param audio_size
	 * @param picture_size
	 * @return
	 * 其中各参数的取值范围:
         10<=video_size<=1000
         30<=audio_size<=3000
         3<=picture_size<=200
                              如java层不设置，则取默认值：
         video_size = 400
         audio_size = 1600
         picture_size = 20

	 */
	public native int setCacheSize(int video_size, int audio_size, int picture_size, int startpic_size);
	
	/**
	 * 表示设置起播的时间，单位秒
	 * @param start_cache 首次几秒起播，默认1s  参数的范围都是：0.2~10.0
	 * @param block_cache 卡顿缓存几秒数据后起播  默认3s  参数的范围都是：0.2~10.0
	 * @return
	 */
	public native int setCacheTime(double start_cache, double block_cache);
	
	public void release()
	{
//		Log.i(TAG, "release()");
		stayAwake(false);
		updateSurfaceScreenOn();
		mOnPreparedListener = null;
		mOnBufferingUpdateListener = null;
		mOnCompletionListener = null;
		mOnSeekCompleteListener = null;
		mOnErrorListener = null;
		mOnInfoListener = null;
		mOnVideoSizeChangedListener = null;
		mGlRenderControler = null;
		_release();
	}

	public void reset()
	{
		stayAwake(false);
		_reset();
		mEventHandler.removeCallbacksAndMessages(null);
	}

	public boolean suspend()
	{
//		Log.i(TAG, "suspend()");
		if (native_suspend_resume(true) < 0)
		{
			return false;
		}

		stayAwake(false);
		mEventHandler.removeCallbacksAndMessages(null);

		return true;
	}

	public boolean resume()
	{
		if (native_suspend_resume(false) < 0)
		{
			return false;
		}

		if (isPlaying())
		{
			stayAwake(true);
		}

		return true;
	}

	public void setWakeMode(Context context, int mode)
	{
		boolean washeld = false;
		if (mWakeLock != null)
		{
			if (mWakeLock.isHeld())
			{
				washeld = true;
				mWakeLock.release();
			}
			mWakeLock = null;
		}

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE, MediaPlayer.class.getName());
		mWakeLock.setReferenceCounted(false);
		if (washeld)
		{
			mWakeLock.acquire();
		}
	}

	public void setScreenOnWhilePlaying(boolean screenOn)
	{
		if (mScreenOnWhilePlaying != screenOn)
		{
			mScreenOnWhilePlaying = screenOn;
			updateSurfaceScreenOn();
		}
	}

	private void stayAwake(boolean awake)
	{
		if (mWakeLock != null)
		{
			if (awake && !mWakeLock.isHeld())
			{
				mWakeLock.acquire();
			}
			else if (!awake && mWakeLock.isHeld())
			{
				mWakeLock.release();
			}
		}
		mStayAwake = awake;
		updateSurfaceScreenOn();
	}

	private void updateSurfaceScreenOn()
	{
		if (mSurfaceHolder != null)
		{
			mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
		}
	}

	private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener mSizeChangedListener)
	{
		mOnVideoSizeChangedListener = mSizeChangedListener;
	}

	private OnSeekCompleteListener mOnSeekCompleteListener;

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener)
	{
		mOnSeekCompleteListener = listener;
	}

	private OnPreparedListener mOnPreparedListener;

	public void setOnPreparedListener(OnPreparedListener listener)
	{
		mOnPreparedListener = listener;
	}

	private OnBufferingUpdateListener mOnBufferingUpdateListener;

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener)
	{
		mOnBufferingUpdateListener = listener;
	}

	private OnCompletionListener mOnCompletionListener;

	public void setOnCompletionListener(OnCompletionListener listener)
	{
		mOnCompletionListener = listener;
	}
	
	private OnSuccessListener mOnSuccessListener;

	public void setOnSuccessListener(OnSuccessListener listener)
	{
		mOnSuccessListener = listener;
	}

	private OnErrorListener mOnErrorListener;

	public void setOnErrorListener(OnErrorListener listener)
	{
		mOnErrorListener = listener;
	}

	private OnInfoListener mOnInfoListener;

	public void setOnInfoListener(OnInfoListener listener)
	{
		mOnInfoListener = listener;
	}
	
	
	private OnAdNumberListener mOnAdNumberListener;

	public void setOnAdNumberListener(OnAdNumberListener listener)
	{
		mOnAdNumberListener = listener;
	}
	
	
	private OnBlockListener mOnBlockListener;

	public void setOnBlockListener(OnBlockListener listener)
	{
		mOnBlockListener = listener;
	}
	
	private OnDisplayListener mOnDisplayListener;

	public void setOnDisplayListener(OnDisplayListener listener)
	{
		mOnDisplayListener = listener;
	}
	
	public interface OnAdNumberListener{
		/**
		 * @param mediaPlayer
		 * @param number 0 是第一个广告 1是第二个广告 2是第三个广告
		 */
		void onAdNumber(FFMpegPlayer mediaPlayer,int number);
	}
	
	public interface OnBlockListener{
		/**
		 * 卡顿
		 */
		void onBlock(FFMpegPlayer mediaPlayer, int blockInfo);
	}

	public interface OnDisplayListener{
		/**
		 * @param mediaPlayer
		 */
		void onDisplay(FFMpegPlayer mediaPlayer);
	}
	
	private OnHardDecodeErrorListner mOnHardDecodeErrorListener;
	
	public void setOnHardDecoddErrorListener(OnHardDecodeErrorListner listener)
	{
		mOnHardDecodeErrorListener = listener;
	}
	
	/**
	 * 硬解播放失败，需要切换到软解
	 */
	public interface OnHardDecodeErrorListner {
		void onError(FFMpegPlayer mediaPlayer, int arg1, int arg2);
	}
	
	private class EventHandler extends Handler
	{
		private FFMpegPlayer mMediaPlayer;

		public EventHandler(FFMpegPlayer ffmpegPlayer, Looper looper)
		{
			super(looper);
			mMediaPlayer = ffmpegPlayer;
		}

		@Override
		public void handleMessage(Message msg)
		{
			Log.e("xll","FFMPEG msg "+msg.what);
			if (mMediaPlayer.mNativeContext == 0)
			{
//				boolean error_was_handled = false;
//				if (mOnErrorListener != null)
//				{
//					error_was_handled = mOnErrorListener.onError(mMediaPlayer, msg.what, msg.arg1);
//				}
				Log.e("xll","FFMPEG msg native context =0");
				return;
			}
			Log.e("xll","FFMPEG msg "+msg.what);

			switch (msg.what)
			{
				case MEDIA_PREPARED:
					if (mOnPreparedListener != null)
						mOnPreparedListener.onPrepared(mMediaPlayer);
					return;

				case MEDIA_PLAYBACK_COMPLETE:
					if (mOnCompletionListener != null)
						mOnCompletionListener.onCompletion(mMediaPlayer);
					stayAwake(false);
					return;

				case MEDIA_BUFFERING_UPDATE:
					if (mOnBufferingUpdateListener != null)
					{
						mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer, msg.arg1);
					}
					return;

				case MEDIA_SEEK_COMPLETE:
					if (mOnSeekCompleteListener != null)
						mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
					return;

				case MEDIA_SET_VIDEO_SIZE:
					if (mOnVideoSizeChangedListener != null)
					{
						if (null == mRect)
						{
							mRect = new Rect();
							mRect.set(0, 0, msg.arg1, msg.arg2);
						}

						mOnVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer, msg.arg1, msg.arg2);
					}
					return;
					
				case MEDIA_AD_NUMBER:
					if (mOnAdNumberListener != null)
						mOnAdNumberListener.onAdNumber(mMediaPlayer, msg.arg1);
					return;
				case MEDIA_BLOCK:
					if (mOnBlockListener != null)
						mOnBlockListener.onBlock(mMediaPlayer, MEDIA_BLOCK_START);
					return;
					
				case MEDIA_END_BLOCK:
					if (mOnBlockListener != null)
						mOnBlockListener.onBlock(mMediaPlayer, MEDIA_BLOCK_END);
					return;
				case MEDIA_DECODE_SUCESS:
					if (mOnSuccessListener != null)
					{
						mOnSuccessListener.onSuccess();
					}
					return;
				case MEDIA_START_DISPLAY:
					if(mOnDisplayListener != null){
						mOnDisplayListener.onDisplay(mMediaPlayer);
					}
					return;
				case MEDIA_PREPARED_FAIL:
				case MEDIA_SETDATASOURCE_FAIL:
				case MEDIA_SOFTDECODE_FAIL:
//				case MEDIA_OPENGL_FAIL:
					boolean error_was_handled = false;
					if (mOnErrorListener != null)
					{
						error_was_handled = mOnErrorListener.onError(mMediaPlayer, msg.what, msg.arg1);
					}
					if (mOnCompletionListener != null && !error_was_handled)
					{
						mOnCompletionListener.onCompletion(mMediaPlayer);
					}
					stayAwake(false);
					return;
					
				case MEDIA_HARDDECODE_START_FAIL_SWITCH_TO_SOFTDECODE:
				case MEDIA_HARDDECODE_DURATION_FAIL_SWITCH_TO_SOFTDECODE:
					if (mOnHardDecodeErrorListener != null)
					{
						mOnHardDecodeErrorListener.onError(mMediaPlayer, msg.what, msg.arg1);
					}
					return;

				case MEDIA_INFO:
					if (mOnInfoListener != null)
					{
						mOnInfoListener.onInfo(mMediaPlayer, msg.arg1, msg.arg2);
					}
					return;

				case MEDIA_NOP:
					break;

				default:
					return;
			}
		}
	}

	public static void initAudioTrack(Object mediaplayer_ref, int sampleRateInHz, int channelConfig) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}

		int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
		mp.mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
		mp.mTrack.play();
	}

	public static void writeAudioTrack(Object mediaplayer_ref, byte[] audioData, int size) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}
		mp.mTrack.write(audioData, 0, size);
	}

	public static void releaseAudioTrack(Object mediaplayer_ref) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}
		if (mp.mTrack != null)
		{
			mp.mTrack.stop();
			mp.mTrack.release();
			mp.mTrack = null;
		}
	}

	private native void _setAudioTrack(AudioTrack track) throws IOException;

	public static void JavaDraw(Object mediaplayer_ref) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}

		Canvas canvas = mp.mSurfaceHolder.lockCanvas(mRect);

		if (canvas == null)
		{
			return;
		}
		_nativeDraw(canvas);
		mp.mSurfaceHolder.unlockCanvasAndPost(canvas);
	}

	private static native void _nativeDraw(Canvas canvas);

    public interface GLRenderControler{

		 void setGLStartRenderMode();

		 void setGLStopRenderMode();
	 }

	public void setRenderControler(GLRenderControler controler) {
		mGlRenderControler = controler;
	}

	public static void stopRenderMode() {
		if (mGlRenderControler != null)
			mGlRenderControler.setGLStopRenderMode();
	}

	public static void startRenderMode() {
		if (mGlRenderControler != null)
			mGlRenderControler.setGLStartRenderMode();
	}
	
	public static void initVideoDecoder( Object mediaplayer_ref, int width, int height ) throws IOException
	{
//		Log.i(TAG, "initVideoDecoder");
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return;
		}
		/*
		ActivityInfo  ainfo    = rinfo.activityInfo;
        String div     = System.getProperty("path.separator");
        String packagename  = ainfo.packageName;
        String dexPath       = ainfo.applicationInfo.dataDir;
        String dexOutputDir  = getApplicationInfo().dataDir;
        getApplicationContext().getFilesDir().getAbsolutePath()
        */
		if( mp.mContext != null )
		{
//	        File dexInternalStoragePath = new File(mp.mContext.getDir("dex", Context.MODE_PRIVATE), "playerhardwaredecode.jar");  
//	        File optimizedDexOutputPath = mp.mContext.getDir("outdex", Context.MODE_PRIVATE);
//	        // Initialize the class loader with the secondary dex file.
//	        DexClassLoader cl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
//	                optimizedDexOutputPath.getAbsolutePath(),
//	                null,
//	                mp.mContext.getClassLoader());
//	        //mp.mContext = null;
//	        Class decoder = null;
//	        try {
//				//mp.mVideoDecoder = new MediaDecoder();
//		        decoder = cl.loadClass("com.letv.component.player.hardwaredecode.MediaHardwareDecoder");
//		                
//		        mp.mVideoDecoder = (MediaDecoder) decoder.newInstance();    
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
			mp.mVideoDecoder = new MediaHardwareDecoder();
			mp.mVideoDecoder.setPlayer(mp);
			mp.mVideoDecoder.createDecoder(width, height, mp.mSurface);
		}
	}
	
	public static void stopVideoDecoder( Object mediaplayer_ref ) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null )
		{
			return;
		}
		if(mp.mVideoDecoder != null)
		{
			mp.mVideoDecoder.stopCodec();
			mp.mVideoDecoder = null;
		}
	}
	
	public static int fillInputBuffer( Object mediaplayer_ref, byte[] data, long pts, int flush ) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null || mp.mVideoDecoder == null)
		{
			return -1;
		}

		return mp.mVideoDecoder.fillInputBuffer(data, pts, flush);
	}
	
	public static int flushCodec( Object mediaplayer_ref ) throws IOException
	{
		FFMpegPlayer mp = (FFMpegPlayer) ((WeakReference<?>) mediaplayer_ref).get();
		if (mp == null)
		{
			return -1;
		}
		
		if( mp.mVideoDecoder != null )
		{
			mp.mVideoDecoder.flushCodec();
		}
		
		return 0;
	}
	
	public void setDecoderSurface( Surface surface )
	{
		mSurface = surface;
	}
	
	public native int _native_sync( long pts ) throws IOException;
	
	public native int setHardwareDecode( int hwDecode ) throws IOException;
	
	public native void setHwCapbility(int avc_profile, int avc_level);
	
	public native void startHwRender();
	
	/**
	 * 鎾斁鍣ㄦ挱鏀炬垚鍔熷洖璋�
	 * @author chenyueguo
	 */
	public interface OnSuccessListener {
		public void onSuccess();
	}
}
