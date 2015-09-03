package com.leap12.common;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class Crypt {

	public static void main(String[] args) throws Exception {
		String username = "bob@google.org";
		String password = "Password1";
		String secretId = "BlahBlahBlah";
		String SALT2 = "deliciously salty"; // TODO make random

		// salt ?
		//		final Random r = new SecureRandom();
		//		byte[] salt = new byte[32];
		//		r.nextBytes(salt);
		//		/** String encodedSalt = Base64.encodeBase64String(salt); */

		// Get the Key
		byte[] key = (SALT2 + username + password).getBytes();
		System.out.println(key.length);

		MessageDigest sha = MessageDigest.getInstance("SHA-1"); // FIXME: SHA-2 ?
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16); // use only first 128 bit

		// Generate the secret key specs.
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES");
		//			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] encryptedBytes = cipher.doFinal(secretId.getBytes());
		String encryptedString = new String(encryptedBytes); // StrUtl.toString(encryptedBytes);
		Log.d("Before Encrypt  %s", secretId);
		Log.d("After  Encrypt  %s", encryptedString);


		byte[] encryptedBytes2 = encryptedBytes; // encryptedString.getBytes(); // StrUtl.toBytes(encryptedString);

		Log.d("Length %s vs %s", encryptedBytes.length, encryptedBytes2.length);


		Cipher cipher2 = Cipher.getInstance("AES");
		cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] originalBytes = cipher2.doFinal(encryptedBytes2);
		String originalString = new String(originalBytes);
		Log.d("Original String: %s", originalString);
		Log.d("After  Decrypt  %s");
	}

	//	public static class CryptoSecurity {

	public static String key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
	public static byte[] key_Array = Base64.decodeBase64(key);

	public static String encrypt(String strToEncrypt)
	{
		try
		{
			//Cipher _Cipher = Cipher.getInstance("AES");
			//Cipher _Cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			Cipher _Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			// Initialization vector.   
			// It could be any value or generated using a random number generator.
			byte[] iv = { 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1, 7, 7, 7, 7 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			Key SecretKey = new SecretKeySpec(key_Array, "AES");
			_Cipher.init(Cipher.ENCRYPT_MODE, SecretKey, ivspec);

			return Base64.encodeBase64String(_Cipher.doFinal(strToEncrypt.getBytes()));
		} catch (Exception e)
		{
			System.out.println("[Exception]:" + e.getMessage());
		}
		return null;
	}

	public static String decrypt(String EncryptedMessage) {
		try {
			//Cipher _Cipher = Cipher.getInstance("AES");
			//Cipher _Cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			// Initialization vector.   
			// It could be any value or generated using a random number generator.
			byte[] iv = { 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1, 7, 7, 7, 7 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			Key SecretKey = new SecretKeySpec(key_Array, "AES");
			cipher.init(Cipher.DECRYPT_MODE, SecretKey, ivspec);

			byte DecodedMessage[] = Base64.decodeBase64(EncryptedMessage);
			return new String(cipher.doFinal(DecodedMessage));

		} catch (Exception e) {
			System.out.println("[Exception]:" + e.getMessage());

		}
		return null;
	}

	public static void xmain(String[] args) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();

		sb.append("xml file string ...");

		String outputOfEncrypt = encrypt(sb.toString());
		System.out.println("[CryptoSecurity.outputOfEncrypt]:" + outputOfEncrypt);

		String outputOfDecrypt = decrypt(outputOfEncrypt);
		//String outputOfDecrypt = decrypt(sb.toString());        
		System.out.println("[CryptoSecurity.outputOfDecrypt]:" + outputOfDecrypt);
	}

	//}
}
