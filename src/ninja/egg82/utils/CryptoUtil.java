package ninja.egg82.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

public class CryptoUtil implements ICryptoUtil {
	//vars
	private MD5Digest md5 = new MD5Digest();
	private SHA1Digest sha1 = new SHA1Digest();
	private SHA256Digest sha256 = new SHA256Digest();
	private SHA512Digest sha512 = new SHA512Digest();
	
	private PasswordHasher phpass = new PasswordHasher();
	
	private CFBBlockCipher aes = new CFBBlockCipher(new AESEngine(), 8);
	private CFBBlockCipher triDes = new CFBBlockCipher(new DESedeEngine(), 8);
	private PKCS7Padding pkcs7 = new PKCS7Padding();
	
	private HMac hmac = new HMac(new SHA256Digest());
	
	private SecureRandom rng = new SecureRandom();
	
	//constructor
	public CryptoUtil() {
		
	}
	
	//public
	public byte[] toBytes(String input) {
		return toBytes(input, FileUtil.UTF_8);
	}
	public byte[] toBytes(String input, Charset enc) {
		return input.getBytes(enc);
	}
	public String toString(byte[] input) {
		return toString(input, FileUtil.UTF_8);
	}
	public String toString(byte[] input, Charset enc) {
		return new String(input, enc);
	}
	
	public byte[] base64Encode(byte[] input) {
		return Base64.encode(input);
	}
	public byte[] base64Decode(byte[] input) {
		return Base64.decode(input);
	}
	
	public synchronized byte[] hashMd5(byte[] input) {
		byte[] retVal = new byte[md5.getDigestSize()];
		md5.update(input, 0, input.length);
		md5.doFinal(retVal, 0);
		return retVal;
	}
	public synchronized byte[] hashSha1(byte[] input) {
		byte[] retVal = new byte[sha1.getDigestSize()];
		sha1.update(input, 0, input.length);
		sha1.doFinal(retVal, 0);
		return retVal;
	}
	public synchronized byte[] hashSha256(byte[] input) {
		byte[] retVal = new byte[sha256.getDigestSize()];
		sha256.update(input, 0, input.length);
		sha256.doFinal(retVal, 0);
		return retVal;
	}
	public synchronized byte[] hashSha512(byte[] input) {
		byte[] retVal = new byte[sha512.getDigestSize()];
		sha512.update(input, 0, input.length);
		sha512.doFinal(retVal, 0);
		return retVal;
	}
	
	public byte[] bcrypt(byte[] input, byte[] salt) {
		return BCrypt.generate(input, salt, 4);
	}
	public byte[] scrypt(byte[] input, byte[] salt) {
		return scrypt(input, salt, 262144);
	}
	public byte[] scrypt(byte[] input, byte[] salt, int cost) {
		return SCrypt.generate(input, salt, MathUtil.clamp(1, 2^128, MathUtil.upperPowerOfTwo(cost)), 8, 1, 128);
	}
	public synchronized byte[] phpass(byte[] input) {
		return phpass(input, FileUtil.UTF_8);
	}
	public synchronized byte[] phpass(byte[] input, Charset enc) {
		return toBytes(phpass.createHash(toString(input, enc)), enc);
	}
	
	public synchronized byte[] easyEncrypt(byte[] input, byte[] key) {
		key = hashSha256(key);
		byte[] iv = getRandomBytes(16);
		byte[] encrypted = encryptAes(input, key, iv);
		byte[] combined = combine(iv, encrypted);
		byte[] hmac = hmac256(combined, key);
		return combine(hmac, combined);
	}
	public synchronized byte[] easyDecrypt(byte[] input, byte[] key) {
		byte[] newInput = getPartial(input, input.length - 48, 48);
		key = hashSha256(key);
		byte[] iv = getPartial(input, 16, 32);
		byte[] hmac = getPartial(input, 32);
		byte[] combined = getPartial(input, input.length - 32, 32);
		
		if (!byteArraysAreEqual(hmac256(combined, key), hmac)) {
			throw new RuntimeException("HMAC validation failed.");
		}
		
		return decrypt(newInput, key, iv, aes, pkcs7);
	}
	
	public synchronized byte[] encryptAes(byte[] input, byte[] key, byte[] iv) {
		return encrypt(input, key, iv, aes);
	}
	public synchronized byte[] encryptTripleDes(byte[] input, byte[] key, byte[] iv) {
		return encrypt(input, key, iv, triDes);
	}
	
	public synchronized byte[] encrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode) {
		return encrypt(input, key, iv, mode, pkcs7);
	}
	public byte[] encrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode, BlockCipherPadding padding) {
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(mode, padding);
		
		CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
		cipher.init(true, params);
		
		return crypt(cipher, input);
	}
	public byte[] decrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode, BlockCipherPadding padding) {
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(mode, padding);
		
		CipherParameters params = new ParametersWithIV(new KeyParameter(key), iv);
		cipher.init(false, params);
		
		return crypt(cipher, input);
	}
	
	public synchronized byte[] hmac256(byte[] input, byte[] key) {
		byte[] retVal = new byte[hmac.getMacSize()];
		
		hmac.init(new KeyParameter(key));
		hmac.update(input, 0, input.length);
		hmac.doFinal(retVal, 0);
		
		return retVal;
	}
	public synchronized byte[] getRandomBytes(int length) {
		byte[] retVal = new byte[length];
		rng.nextBytes(retVal);
		return retVal;
	}
	public synchronized double getRandomDouble() {
		byte[] retVal = new byte[8];
		rng.nextBytes(retVal);
		return ByteBuffer.wrap(retVal).getDouble();
	}
	
	public byte[] getPartial(byte[] input, int length) {
		return getPartial(input, length, 0);
	}
	public byte[] getPartial(byte[] input, int length, int index) {
		byte[] retVal = new byte[length];
		System.arraycopy(input, index, retVal, 0, length);
		return retVal;
	}
	public byte[] combine(byte[] a, byte[] b) {
		byte[] retVal = new byte[a.length + b.length];
		System.arraycopy(a, 0, retVal, 0, a.length);
		System.arraycopy(b, 0, retVal, a.length, b.length);
		return retVal;
	}
	public boolean byteArraysAreEqual(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	//private
	private byte[] crypt(PaddedBufferedBlockCipher cipher, byte[] data) {
		byte[] buffer = new byte[cipher.getOutputSize(data.length)];
		int length1 = cipher.processBytes(data, 0, data.length, buffer, 0);
		int length2 = 0;
		try {
			length2 = cipher.doFinal(buffer, length1);
		} catch (Exception ex) {
			throw new RuntimeException("Cannot crypt data using specified cipher.", ex);
		}
		int length = length1 + length2;
		byte[] result = new byte[length];
		System.arraycopy(buffer, 0, result, 0, result.length);
		
		return result;
	}
}
