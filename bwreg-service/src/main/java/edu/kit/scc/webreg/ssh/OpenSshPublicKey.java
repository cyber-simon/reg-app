package edu.kit.scc.webreg.ssh;

import java.io.Serializable;
import java.security.PublicKey;

import edu.kit.scc.webreg.entity.SshPubKeyEntity;

public class OpenSshPublicKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private SshPubKeyEntity pubKeyEntity;
	
	private byte[] bytes;

	private int decoderPos;

	private PublicKey publicKey;

	private String baseDate;

	private String decoderResult;
	
	private String fingerprint;
	
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

	public SshPubKeyEntity getPubKeyEntity() {
		return pubKeyEntity;
	}

	public void setPubKeyEntity(SshPubKeyEntity pubKeyEntity) {
		this.pubKeyEntity = pubKeyEntity;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}
}
