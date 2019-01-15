package edu.kit.scc.webreg.ssh;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

@ApplicationScoped
public class OpenSshKeyDecoder implements Serializable {
    
	private static final long serialVersionUID = 1L;

	public OpenSshPublicKey decode(String opensshPublicKey) {
		OpenSshPublicKey key = new OpenSshPublicKey();
		
        key.setBytes(getKeyBytes(opensshPublicKey));

        try {
            String type = decodeType(key);
            if (type.equals("ssh-rsa")) {
                BigInteger e = decodeBigInt(key);
                BigInteger m = decodeBigInt(key);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
                key.setPublicKey(KeyFactory.getInstance("RSA").generatePublic(spec));
            } else if (type.equals("ssh-dss")) {
                BigInteger p = decodeBigInt(key);
                BigInteger q = decodeBigInt(key);
                BigInteger g = decodeBigInt(key);
                BigInteger y = decodeBigInt(key);
                DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);
                key.setPublicKey(KeyFactory.getInstance("DSA").generatePublic(spec));
            } else if (type.startsWith("ecdsa-sha2-") &&
                    (type.endsWith("nistp256") || type.endsWith("nistp384") || type.endsWith("nistp521"))) {
                // Based on RFC 5656, section 3.1 (https://tools.ietf.org/html/rfc5656#section-3.1)
                String identifier = decodeType(key);
                BigInteger q = decodeBigInt(key);
                ECPoint ecPoint = getECPoint(q, identifier);
                ECParameterSpec ecParameterSpec = getECParameterSpec(identifier);
                ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
                key.setPublicKey(KeyFactory.getInstance("EC").generatePublic(spec));
            } else {
                throw new IllegalArgumentException("Unsupported key type " + type);
            }
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unable to decode public key", e);
        }
    }
    
    private byte[] getKeyBytes(String opensshPublicKey) {
        for (String part : opensshPublicKey.split(" ")) {
            if (Base64.isBase64(part) && part.startsWith("AAAA")) {
                return Base64.decodeBase64(part);
            }
        }
        throw new IllegalArgumentException("no Base64 part to decode");
    }
    
    private String decodeType(OpenSshPublicKey key) {
        int len = decodeInt(key);
        String type = new String(key.getBytes(), key.getDecoderPos(), len);
        key.increaseDecoderPos(len);
        return type;
    }
    
    private int decodeInt(OpenSshPublicKey key) {
    	byte[] bytes = key.getBytes();
    	int pos = key.getDecoderPos();
        int header = ((bytes[pos] & 0xFF) << 24) | ((bytes[pos] & 0xFF) << 16)
                | ((bytes[pos] & 0xFF) << 8) | (bytes[pos] & 0xFF);
        key.increaseDecoderPos(4);
        return header;
    }

    private BigInteger decodeBigInt(OpenSshPublicKey key) {
        int len = decodeInt(key);
        byte[] bigIntBytes = new byte[len];
        System.arraycopy(key.getBytes(), key.getDecoderPos(), bigIntBytes, 0, len);
        key.increaseDecoderPos(len);
        return new BigInteger(bigIntBytes);
    }
    
    ECPoint getECPoint(BigInteger q, String identifier) {
        String name = identifier.replace("nist", "sec") + "r1";
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(name);
        org.bouncycastle.math.ec.ECPoint point = ecSpec.getCurve().decodePoint(q.toByteArray());
        BigInteger x = point.getAffineXCoord().toBigInteger();
        BigInteger y = point.getAffineYCoord().toBigInteger();
        return new ECPoint(x, y);
    }

    ECParameterSpec getECParameterSpec(String identifier) {
        try {
            // http://www.bouncycastle.org/wiki/pages/viewpage.action?pageId=362269#SupportedCurves(ECDSAandECGOST)-NIST(aliasesforSECcurves)
            String name = identifier.replace("nist", "sec") + "r1";
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec(name));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (InvalidParameterSpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to get parameter spec for identifier " + identifier, e);
        }
    }    
}
