package com.leap12.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Since this project is open source, the security implementation should be developed privately. This class is to help you encrypt and decrypt
 * messages but you'll need to decide the handshake and encryption logic for yourself. Be Careful! Default implementation is clear text.
 */
public class Crypt {

	public static void main( String[] args ) throws Exception {
		test();
	}

	private static void test() throws Exception {
		Crypt crypt = new Crypt();


		try {
			String SALT = "shouldBeSomethingRandom";
			String user, pass, inMsg, outMsg, keyPhrase;
			byte[] encMsg;

			user = "aaron";
			pass = "harris";
			inMsg = "Hello World";
			keyPhrase = user + pass + SALT;

			encMsg = crypt.encryptString( keyPhrase, inMsg );
			outMsg = crypt.decryptString( keyPhrase, encMsg );

			Log.d( "'%s', '%s', '%s', '%s', matched=%s", user, pass, inMsg, outMsg, inMsg.equals( outMsg ) );
		} catch ( BadPaddingException e ) {
			Log.e( "Invalid Username or Password" );
		}
	}

	private final Charset mCharSet;

	/** Uses {@link StandardCharsets#UTF_8} by default */
	public Crypt() {
		this( StandardCharsets.UTF_8 );
	}

	/** @see {@link StandardCharsets} */
	public Crypt( Charset charSet ) {
		this.mCharSet = charSet;
	}

	private SecretKeySpec getKey( String keyPhrase ) throws NoSuchAlgorithmException {
		byte[] key = ( keyPhrase ).getBytes( mCharSet );

		// 10ms the first time
		MessageDigest sha = MessageDigest.getInstance( "SHA-1" ); // FIXME: SHA-2 ?
		key = sha.digest( key );
		key = Arrays.copyOf( key, 16 ); // use only first 128 bit

		SecretKeySpec secretKeySpec = new SecretKeySpec( key, "AES" );
		return secretKeySpec;
	}

	/**
	 * @param keyPhrase Used to generate the encryption/decryption key. Maybe a password or username and password and salt combo?
	 * @param unencryptedMessage The message you wish to encrypt
	 * @return The encrypted byte-array form of the given unencryptedMessage.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt( String keyPhrase, byte[] unencryptedMessage ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKeySpec = getKey( keyPhrase );
		Cipher cipher = Cipher.getInstance( "AES" ); // says getInstance(), means newInstance()
		cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );
		byte[] encryptedMessage = cipher.doFinal( unencryptedMessage );
		return encryptedMessage;
	}

	/**
	 * @param keyPhrase Used to generate the encryption/decryption key. Maybe a password or username and password and salt combo?
	 * @param unencryptedMessage The message you wish to encrypt
	 * @return The encrypted byte-array form of the given unencryptedMessage.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encryptString( String keyPhrase, String unencryptedMessage ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encrypt( keyPhrase, unencryptedMessage.getBytes( mCharSet ) );
	}

	/**
	 * @param keyPhrase Used to generate the encryption/decryption key. Maybe a password or username and password and salt combo?
	 * @param encryptedMessage The message you wish to decrypt
	 * @return The decrypted byte-array form of the given encryptedMessage.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decrypt( String keyPhrase, byte[] encryptedMessage ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec secretKeySpec = getKey( keyPhrase );
		Cipher cipher = Cipher.getInstance( "AES" ); // says getInstance(), means newInstance()
		cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );
		byte[] unencryptedmessage = cipher.doFinal( encryptedMessage );
		return unencryptedmessage;
	}

	/**
	 * @param keyPhrase Used to generate the encryption/decryption key. Maybe a password or username and password and salt combo?
	 * @param encryptedMessage The message you wish to decrypt
	 * @return The decrypted string form of the given encryptedMessage.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decryptString( String keyPhrase, byte[] encryptedMessage ) throws
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return new String( decrypt( keyPhrase, encryptedMessage ), mCharSet );
	}
}
