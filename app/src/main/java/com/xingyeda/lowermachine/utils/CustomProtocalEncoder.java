package com.xingyeda.lowermachine.utils;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.charset.Charset;

public class CustomProtocalEncoder extends ProtocolEncoderAdapter {

	public CustomProtocalEncoder(Charset charset) {

	}

	// 在此处实现对ProtocalPack包的编码工作，并把它写入输出流中
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		ProtocalPack value = (ProtocalPack) message;
		IoBuffer buf = IoBuffer.allocate(value.getLength());
		buf.setAutoExpand(true);
		buf.putInt(value.getLength());
		buf.put(value.getContent().getBytes("utf-8"));
		buf.flip();
		out.write(buf);

	}
}
