package ninja.egg82.utils;

import java.nio.charset.Charset;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;

public interface ICryptoUtil {
	//functions
	byte[] toBytes(String input);
	byte[] toBytes(String input, Charset enc);
	String toString(byte[] input);
	String toString(byte[] input, Charset enc);
	
	byte[] base64Encode(byte[] input);
	byte[] base64Decode(byte[] input);
	
	byte[] hashMd5(byte[] input);
	byte[] hashSha1(byte[] input);
	byte[] hashSha256(byte[] input);
	byte[] hashSha512(byte[] input);
	
	byte[] bcrypt(byte[] input, byte[] salt);
	byte[] scrypt(byte[] input, byte[] salt);
	byte[] scrypt(byte[] input, byte[] salt, int cost);
	byte[] phpass(byte[] input);
	byte[] phpass(byte[] input, Charset enc);
	
	byte[] easyEncrypt(byte[] input, byte[] key);
	byte[] easyDecrypt(byte[] input, byte[] key);
	
	byte[] encryptAes(byte[] input, byte[] key, byte[] iv);
	byte[] encryptTripleDes(byte[] input, byte[] key, byte[] iv);
	
	byte[] encrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode);
	byte[] encrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode, BlockCipherPadding padding);
	byte[] decrypt(byte[] input, byte[] key, byte[] iv, BlockCipher mode, BlockCipherPadding padding);
	
	byte[] hmac256(byte[] input, byte[] key);
	byte[] getRandomBytes(int length);
	double getRandomDouble();
	
	byte[] getPartial(byte[] input, int length);
	byte[] getPartial(byte[] input, int length, int index);
	byte[] combine(byte[] a, byte[] b);
	boolean byteArraysAreEqual(byte[] a, byte[] b);
}
