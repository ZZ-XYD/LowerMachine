package com.xingyeda.lowermachine.utils;

import android.content.Intent;
import android.os.Looper;

import com.xingyeda.lowermachine.base.MainApplication;
import com.xingyeda.lowermachine.service.HeartBeatService;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class CustomProtocolDecoder implements ProtocolDecoder {

	private final AttributeKey CONTEXT = new AttributeKey(getClass(), "context");
	
	private final Charset charset;
	
	private int maxPackLength = 50 * 1024;

	public CustomProtocolDecoder() {
		this(Charset.defaultCharset());
	}

	public CustomProtocolDecoder(Charset charset) {
		this.charset = charset;
	}

	public int getMaxLineLength() {
		return maxPackLength;
	}

	public void setMaxLineLength(int maxLineLength) {
		if (maxLineLength <= 0) {
			throw new IllegalArgumentException("maxLineLength: "
					+ maxLineLength);
		}
		this.maxPackLength = maxLineLength;
	}

	private Context getContext(IoSession session) {
		Context ctx;
		ctx = (Context) session.getAttribute(CONTEXT);
		if (ctx == null) {
			ctx = new Context();
			session.setAttribute(CONTEXT, ctx);
		}
		return ctx;
	}

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
		final int packHeadLength = 4;
		// 先获取上次的处理上下文，其中可能有未处理完的数据
		Context ctx = getContext(session);
		// 先把当前buffer中的数据追加到Context的buffer当中
		ctx.append(in);
		// 把position指向0位置，把limit指向原来的position位置
		IoBuffer buf = ctx.getBuffer();
		buf.flip();
		// 然后按数据包的协议进行读取
		while (buf.remaining() >= packHeadLength) {
			buf.mark();
			// 读取消息头部分
			int length = buf.getInt();
			MyLog.d(length);
			// 检查读取的包头是否正常，不正常的话清空buffer
			if (length < 0 || length > maxPackLength) {
				buf.clear();
				break;
			}
			// 读取正常的消息包，并写入输出流中，以便IoHandler进行处理
			else if (length >= packHeadLength
					&& length - packHeadLength <= buf.remaining()) {
				int oldLimit2 = buf.limit();
				buf.limit(length);
//				buf.limit(buf.position() + length);
				String content = buf.getString(ctx.getDecoder());
				MyLog.d(content);
				buf.limit(oldLimit2);
				ProtocalPack pack = new ProtocalPack(content);
				out.write(pack);
			} else {
				MyLog.d("length : "+length);
				// 如果消息包不完整
				// 将指针重新移动消息头的起始位置
				buf.reset();
				break;
			}
		}
		if (buf.hasRemaining()) {
			// 将数据移到buffer的最前面
			IoBuffer temp = IoBuffer.allocate(maxPackLength)
					.setAutoExpand(true);
			temp.put(buf);
			temp.flip();
			buf.clear();
			buf.put(temp);

		} else {// 如果数据已经处理完毕，进行清空
			buf.clear();
		}

	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
		Context ctx = (Context) session.getAttribute(CONTEXT);
		if (ctx != null) {
			session.removeAttribute(CONTEXT);
		}
	}
	

	@SuppressWarnings("unused")
	private class Context {
		private final CharsetDecoder decoder;
		private IoBuffer buf;
		private int matchCount = 0;
		private int overflowPosition = 0;

		private Context() {
			decoder = charset.newDecoder();
			buf = IoBuffer.allocate(80).setAutoExpand(true);
		}

		public CharsetDecoder getDecoder() {
			return decoder;
		}

		public IoBuffer getBuffer() {
			return buf;
		}

		
		public int getOverflowPosition() {
			return overflowPosition;
		}

		public int getMatchCount() {
			return matchCount;
		}

		public void setMatchCount(int matchCount) {
			this.matchCount = matchCount;
		}

		public void reset() {
			overflowPosition = 0;
			matchCount = 0;
			decoder.reset();
		}

		public void append(IoBuffer in) {
			getBuffer().put(in);

		}

	}

	public static String ioBufferToString(Object message)
	{
		if (!(message instanceof IoBuffer))
		{
			return "";
		}
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] b = new byte [ioBuffer.limit()];
		ioBuffer.get(b);
		StringBuffer stringBuffer = new StringBuffer();

		for (int i = 0; i < b.length; i++)
		{

			stringBuffer.append((char) b [i]);
		}
		return stringBuffer.toString();
	}
}
