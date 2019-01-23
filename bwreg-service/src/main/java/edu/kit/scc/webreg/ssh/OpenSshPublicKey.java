package edu.kit.scc.webreg.ssh;

import java.security.PublicKey;

public class OpenSshPublicKey {

	private byte[] bytes;
	private int decoderPos;
	private PublicKey publicKey;
	private String baseDate;
	private String decoderResult;
	
	public OpenSshPublicKey() {
		super();
		decoderPos = 0;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public int getDecoderPos() {
		return decoderPos;
	}
	
	public void setDecoderPos(int decoderPos) {
		this.decoderPos = decoderPos;
	}
	
	public void increaseDecoderPos(int steps) {
		this.decoderPos += steps;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public String getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}

	public String getDecoderResult() {
		return decoderResult;
	}

	public void setDecoderResult(String decoderResult) {
		this.decoderResult = decoderResult;
	}
}
