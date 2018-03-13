/*
  vvphone is a SIP app for android.
  vvsip is a SIP library for softphone (SIP -rfc3261-)
  Copyright (C) 2003-2010  Bluegoby - <bluegoby@163.com>
 */

package com.vvsip.ansip;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/*
 *语音输出类，功能：播放语音
 */
public class AudioOutput {

	public static boolean beready = false;
	public static boolean restart = false;

	public static final int BUFFER_SIZE = 640;

	AudioTrack track;
	boolean running = false;
	// short[] lin;
	int offset;
	int mRate = 8000;

	AudioOutput(int rate) {

		mRate = rate;

		if (beready == false)
			return;

		running = true;

		int bufsize = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

		int c = bufsize / BUFFER_SIZE;
		bufsize = BUFFER_SIZE * c + BUFFER_SIZE;
		if (bufsize < BUFFER_SIZE * 2 * 2)
			bufsize = BUFFER_SIZE * 2 * 2;
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		track = new AudioTrack(AudioCompatibility.mAudiomanager_stream_type, rate, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);
		Log.i("AudioOutput", "新创建的AudioTrack!");
		track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());

		offset = 0;

		try {
			track.play();
		} catch (Exception e) {
			track.flush();
			track.release();
			track = null;
		}
	}

	/** 停止运行 */
	public int stop() {
		running = false;
		if (track == null)
			return 0;

		track.flush();
		track.stop();
		track.release();
		track = null;
		return 0;
	}

	public int write_bytes(byte[] data, int len) {
		int size;
		if (len > BUFFER_SIZE)
			return 0;

		if (beready == false) {
			restart = false;
			if (track != null) {
				Log.i("AudioOutput", "AudioTrack 现在没有使用:停止");
				track.flush();
				track.stop();
				track.release();
				track = null;
			}
			return 0;
		}

		if (restart == true && Build.VERSION.SDK_INT >= 14) {
			// 让我们考虑重新启动不再需要4.0和以上。
			restart = false;
		}
		if (track == null || restart == true) {
			if (AudioInput.restart == true) {
				/* 等待输入准备就绪… */
				return 0;
			}
			restart = false;
			if (track != null) {
				track.flush();
				track.stop();
				track.release();
				track = null;
			}

			int bufsize = AudioTrack.getMinBufferSize(mRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

			int c = bufsize / BUFFER_SIZE;
			bufsize = BUFFER_SIZE * c + BUFFER_SIZE;
			if (bufsize < BUFFER_SIZE * 2 * 2)
				bufsize = BUFFER_SIZE * 2 * 2;
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			track = new AudioTrack(AudioCompatibility.mAudiomanager_stream_type, mRate, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);
			Log.i("AudioOutput", "新创建的(2)AudioTrack !");
			track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
			offset = 0;

			try {
				track.play();
			} catch (Exception e) {
				track.flush();
				track.release();
				track = null;
				return len;
			}
		}

		if (len == 0)
			return 0; /*
					 * len=0被要求检查，以便在呼叫建立之前准备audiotrack。
					 */

		int diff = 0;
		try {
			diff = offset - track.getPlaybackHeadPosition();
		} catch (Exception e) {
			// 可能是IllegalStateException? 曾经在 galaxy S发生过一次。
			return len;
		}
		if (diff < 1024) {
			Log.w("VvsipService", "inserting silence//diff = " + diff + " samples");
			byte silence[] = new byte[BUFFER_SIZE];
			track.write(silence, 0, BUFFER_SIZE);
			offset += BUFFER_SIZE;
		}
		size = track.write(data, 0, len);
		offset += size;
		return size;
	}

}
