package edu.kit.scc.webreg.ssh;

import java.io.Serializable;
import java.security.PublicKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OpenSshPublicKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	
	@JsonIgnore
	private byte[] bytes;

	@JsonIgnore
	private int decoderPos;

	@JsonIgnore
	private PublicKey publicKey;

	@JsonIgnore
	private String baseDate;

	@JsonIgnore
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
