package com.leap12.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

	/**
	 * <pre>
	 * 
	 * Since this project is open source, the security implementation should be developed privately. This class is to help you encrypt and decrypt
	 * messages but you'll need to decide the handshake and encryption logic for yourself. Be Careful! Default implementation is clear text.
	 * 
	 * ### Registration
	 * Using a symmetric key securely with a version-aware shared-salt (version aware for backwards compatibility)
	 * - Client and Server are both compiled with the shared-salt
	 * - - Read in at compile-time from a config file so not to include the shared-salt in the code.
	 * - - Why not just include the key with the code?  because then everyone would have the same key
	 * - - and if someone discovered their key, then they'd know everyone's key and they'd know this by looking at the code.
	 * - - compile-time salt from a config file makes the code sharable and when encrypted with user and pass, makes them each unique.
	 * - The user enters username and password
	 * - The client generates a pass-hash from the password and shared-salt
	 * - The client generates a symmetric-key deterministic from the username + pass-hash + shared-salt
	 * - client sends username + pass-hash to server encrypted using only the shared-salt.
	 * - The server decrypts the username + pass-hash using only the shared-salt
	 * - The server generates a symmetric-key deterministic from the username + pass-hash + shared-salt
	 * - The server generates a user-hash deterministic from the username + pass-hash + shared-salt
	 * - server saves username + pass-hash + user-hash, server does not save symmetric-key, key should just be cached in memory per session
	 * - - why not store it?  because if someone got to the database, they could then use that key to impersonate the user
	 * 
	 * ### from now on all communication is done by passing the username in cleartext and the encrypted data
	 * 
	 * </pre>
	 */

	public static final char[] DEFAULT_PALETTE = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+={}[]<>,.".toCharArray();
	private static char[] CHAR_PALETTE = DEFAULT_PALETTE;

	public static void season( String charPalette ) {
		if ( StrUtl.isNotEmpty( charPalette ) ) {
			CHAR_PALETTE = charPalette.toCharArray();
		}
	}


	/**
	 * Convert the given bytes to a String.<br>
	 * Given the same bytes and palette, the same string will be generated every time,<br>
	 * however you will not be able to convert from the string back to the byte[] without losing data
	 */
	public static String toStringLossy( byte[] bytes, char[] palette ) {
		StringBuilder sb = new StringBuilder();
		for ( byte b : bytes ) {
			int intVal = Byte.toUnsignedInt( b );
			int index = intVal % palette.length;
			sb.append( palette[index] );
		}
		return sb.toString();
	}

	private String encryptionAlgorithm = "AES";
	private String transforms = "AES/CBC/PKCS5Padding";
	private String charEncoding = "UTF-8";

	public Crypto() {
	}

	public Crypto( String encryptionAlgorithm, String cipherTransforms, String charEncoding ) {
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.transforms = cipherTransforms;
		this.charEncoding = charEncoding;
	}

	/**
	 * @param unencryptedMsg - the plain-text message you wish to encrypt
	 * @param keyPhrase - the key such as a password
	 * @param initialVector - something extra to localize the encryption (something unique to the program?)
	 * @return encrypted byte[]
	 */
	public byte[] encrypt( byte[] unencryptedMsg, byte[] keyPhrase, byte[] initialVector ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secret = new SecretKeySpec( keyPhrase, encryptionAlgorithm );
		IvParameterSpec iv = new IvParameterSpec( initialVector );
		Cipher cipher = Cipher.getInstance( transforms );
		cipher.init( Cipher.ENCRYPT_MODE, secret, iv );
		byte[] encryptedMsg = cipher.doFinal( unencryptedMsg );
		return encryptedMsg;
	}

	/**
	 * Encrypts plaintext using AES 128bit key and a Chain Block Cipher and returns a base64 encoded string
	 * 
	 * @return Base64 encoded string
	 */
	public String encrypt( String unencryptedMsg, String keyPhrase ) throws
			UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		byte[] msgBytes = unencryptedMsg.getBytes( charEncoding );
		byte[] keyBytes = getKeyBytes( keyPhrase );
		String encString = Base64.getEncoder().encodeToString( encrypt( msgBytes, keyBytes, keyBytes ) );
		return encString;
	}

	/**
	 * @param encryptedMsg - the data you wish to decrypt
	 * @param keyPhrase - the same keyPhrase used to encrypt the encryptedMsg
	 * @param initVector - the same IV used to encrypt the encryptedMsg
	 * @return decryped byte[]
	 */
	public byte[] decrypt( byte[] encryptedMsg, byte[] keyPhrase, byte[] initVector ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance( transforms );
		SecretKeySpec secret = new SecretKeySpec( keyPhrase, encryptionAlgorithm );
		IvParameterSpec iv = new IvParameterSpec( initVector );
		cipher.init( Cipher.DECRYPT_MODE, secret, iv );
		byte[] unencryptedMsg = cipher.doFinal( encryptedMsg );
		return unencryptedMsg;
	}

	/** Decrypts a base64 encoded string using the given key (AES 128bit key and a Chain Block Cipher) */
	public String decrypt( String encryptedMsg, String key ) throws KeyException, GeneralSecurityException, GeneralSecurityException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] encryptedBytes = Base64.getDecoder().decode( encryptedMsg );
		byte[] keyBytes = getKeyBytes( key );
		String unencryptedMsg = new String( decrypt( encryptedBytes, keyBytes, keyBytes ), charEncoding );
		return unencryptedMsg;
	}

	/**
	 * Convert the given bytes to a String.<br>
	 * Given the same bytes, the same string will be generated every time,<br>
	 * however you will not be able to convert from the string back to the byte[] without losing data
	 */
	public String toStringLossy( byte[] bytes ) {
		return toStringLossy( bytes, DEFAULT_PALETTE );
	}

	private byte[] getKeyBytes( String key ) throws UnsupportedEncodingException {
		byte[] keyBytes = new byte[16];
		byte[] tmpBytes = key.getBytes( charEncoding );
		System.arraycopy( tmpBytes, 0, keyBytes, 0, Math.min( tmpBytes.length, keyBytes.length ) );
		return keyBytes;
	}

	public static void main( String[] args ) throws Exception {
		example1( "aaron", "harris", "Hello World" );
		example1( "aaron", "harrif", "Hello World" );
	}

	private static void example1( String user, String pass, String inMsg ) throws Exception {
		try {

			// so that others can't reverse the encMsg
			// Sort of like a 2-key system. The user gets one key and this application holds the other.
			// This way the data can only be unlocked when the two cooperate
			final String SALT = "something unique to my application";

			Crypto crypt = new Crypto();

			// the password should not be the literal clear-text password.
			// it should be a deterministic value derived from the users password.
			// this way the users password is safe and can never be stolen
			// more later on passwords.
			String keyPhrase = user + pass + SALT; // add our secret salt

			// encrypt to byte[]. This byte[] would be stored as a blob some place.
			// it appears as garbage unless you unlock it with the keyphrase.
			// byte[] encMsg = crypt.encryptString( keyPhrase, inMsg );
			String encMsg = crypt.encrypt( inMsg, keyPhrase );
			Log.d( "Encrypted Msg: '%s'", encMsg );

			// descrypt to String. the encMsg should have been previously encrypted
			// and most likely stored some place such as the database.
			// String outMsg = crypt.decryptString( keyPhrase, encMsg );
			String outMsg = crypt.decrypt( encMsg, keyPhrase );

			// This confirms the encrypt decrypt works
			Log.d( "'%s', '%s', '%s', '%s', matched=%s", user, pass, inMsg, outMsg, inMsg.equals( outMsg ) );

			// Here we convert the encoded message to a String that is short, reliable and easy to compare and store.
			// The string is deterministicly generated from the byte[] but is lossy
			// This means that if the same byte[] is given, the same String will be generated.
			// The user's password can be encoded and converted to a string and used as a authentication check.
			// Ideally, the user's password would be encoded on the client and transmitted to the server along with a username.
			// Then on the server the received encoded password is converted to the lossyString
			// and compared to the lossyString we stored for the requested username when they last set their password.
			String simpleEncoded = crypt.toStringLossy( encMsg.getBytes() );
			Log.d( "Enc: '%s'", simpleEncoded );

		} catch ( BadPaddingException e ) {
			Log.e( "Invalid Username or Password" );
		}
	}
}
