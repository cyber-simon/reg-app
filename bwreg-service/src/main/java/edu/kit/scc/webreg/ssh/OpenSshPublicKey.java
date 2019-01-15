package edu.kit.scc.webreg.ssh;

import java.security.PublicKey;

public class OpenSshPublicKey {

	private byte[] bytes;
	private int decoderPos;
	private PublicKey publicKey;
	
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
}
