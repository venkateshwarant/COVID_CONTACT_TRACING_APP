package cash.z.ecc.android.sdk.demoapp.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.pkcs.PrivateKeyInfo;
import org.spongycastle.asn1.pkcs.RSAPrivateKey;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import static android.security.keystore.KeyProperties.DIGEST_SHA1;
import static android.security.keystore.KeyProperties.DIGEST_SHA256;
import static android.security.keystore.KeyProperties.DIGEST_SHA512;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_SIGN;
import static android.security.keystore.KeyProperties.PURPOSE_VERIFY;
import static java.nio.charset.StandardCharsets.UTF_8;


public class RSA {
    public static Charset CharsetUTF_8;

    public static final String ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA;

    private static final String PUBLIC_HEADER = "RSA PUBLIC KEY";
    private static final String PRIVATE_HEADER = "RSA PRIVATE KEY";
    private static final String CSR_HEADER = "CERTIFICATE REQUEST";

    private String keyTag;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PKCS10CertificationRequest csr;

    public RSA() {
        this.setupCharset();
    }

    public RSA(String keyTag) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
        this.setupCharset();
        this.keyTag = keyTag;
        this.loadFromKeystore();
    }
    public RSA(Context context) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        this.setupCharset();
        String publicKey="";
        String privateKey="";
        if(!SharedPreferencesHelper.get(context, ".publickey","").toString().isEmpty()&&
                !SharedPreferencesHelper.get(context, ".privatekey","").toString().isEmpty()){
            setPublicKey(SharedPreferencesHelper.get(context, ".publickey","").toString());
            setPrivateKey(SharedPreferencesHelper.get(context, ".privatekey","").toString());
        }else{
            generate();
            publicKey= getPublicKey();
            privateKey= getPrivateKey();
            SharedPreferencesHelper.put(context, ".publickey",publicKey);
            SharedPreferencesHelper.put(context, ".privatekey",privateKey);
        }
    }
    private void setupCharset() {
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            CharsetUTF_8 = UTF_8;
        } else {
            CharsetUTF_8 = Charset.forName("UTF-8");
        }
    }

    public String getPublicKey() throws IOException {
        byte[] pkcs1PublicKey = publicKeyToPkcs1(this.publicKey);
        return dataToPem(PUBLIC_HEADER, pkcs1PublicKey);
        // return Base64.encodeToString(this.publicKey.getEncoded(), Base64.DEFAULT);
    }

    public String getPrivateKey() throws IOException {
        byte[] pkcs1PrivateKey = privateKeyToPkcs1(this.privateKey);
        return dataToPem(PRIVATE_HEADER, pkcs1PrivateKey);
    }

    public void setPublicKey(String publicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKey = pkcs1ToPublicKey(publicKey);
    }

    public void setPrivateKey(String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] pkcs1PrivateKey = pemToData(privateKey);
        this.privateKey = pkcs1ToPrivateKey(pkcs1PrivateKey);
    }


    // This function will be called by encrypt and encrypt64
    private byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        String encodedMessage = null;
        final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
        byte[] cipherBytes = cipher.doFinal(data);
        return cipherBytes;
    }

    // Base64 input
    public String encrypt64(String b64Message) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        byte[] data = Base64.decode(b64Message, Base64.DEFAULT);
        byte[] cipherBytes = encrypt(data);
        return Base64.encodeToString(cipherBytes, Base64.DEFAULT);
    }

    // UTF-8 input
    public String encrypt(String message) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        byte[] data = message.getBytes(CharsetUTF_8);
        byte[] cipherBytes = encrypt(data);
        return Base64.encodeToString(cipherBytes, Base64.DEFAULT);
    }

    private byte[] decrypt(byte[] cipherBytes) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        String message = null;
        final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        byte[] data = cipher.doFinal(cipherBytes);
        return data;
    }

    // UTF-8 input
    public String decrypt(String message) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        byte[] cipherBytes = Base64.decode(message, Base64.DEFAULT);
        byte[] data = decrypt(cipherBytes);
        return new String(data, CharsetUTF_8);
    }

    // Base64 input
    public String decrypt64(String b64message) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        byte[] cipherBytes = Base64.decode(b64message, Base64.DEFAULT);
        byte[] data = decrypt(cipherBytes);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    private String sign(byte[] messageBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {

        Signature privateSignature = Signature.getInstance(algorithm);
        privateSignature.initSign(this.privateKey);
        privateSignature.update(messageBytes);
        byte[] signature = privateSignature.sign();
        return Base64.encodeToString(signature, Base64.DEFAULT);
    }

    // b64 message
    public String sign64(String b64message, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {
        byte[] messageBytes = Base64.decode(b64message, Base64.DEFAULT);
        return sign(messageBytes, algorithm);
    }


    //utf-8 message
    public String sign(String message, String signature) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {
        byte[] messageBytes = message.getBytes(CharsetUTF_8);
        return sign(messageBytes, signature);
    }

    private boolean verify(byte[] signatureBytes, byte[] messageBytes, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance(algorithm);
        publicSignature.initVerify(this.publicKey);
        publicSignature.update(messageBytes);
        return publicSignature.verify(signatureBytes);
    }

    // b64 message
    public boolean verify64(String signature, String message, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance(algorithm);
        publicSignature.initVerify(this.publicKey);
        byte[] messageBytes = Base64.decode(message, Base64.DEFAULT);
        byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        return verify(signatureBytes, messageBytes, algorithm);
    }

    // utf-8 message
    public Boolean verify(String signature, String message, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance(algorithm);
        publicSignature.initVerify(this.publicKey);
        byte[] messageBytes = message.getBytes(CharsetUTF_8);
        byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        return verify(signatureBytes, messageBytes, algorithm);
    }

    private String dataToPem(String header, byte[] keyData) throws IOException {
        PemObject pemObject = new PemObject(header, keyData);
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        return stringWriter.toString();
    }

    private byte[] pemToData(String pemKey) throws IOException {
        Reader keyReader = new StringReader(pemKey);
        PemReader pemReader = new PemReader(keyReader);
        PemObject pemObject = pemReader.readPemObject();
        return pemObject.getContent();
    }

    private PublicKey pkcs1ToPublicKey(String publicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Reader keyReader = null;
        try {
            keyReader = new StringReader(publicKey);
            PEMParser pemParser = new PEMParser(keyReader);
            SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } finally {
            if (keyReader != null) {
                keyReader.close();
            }
        }
    }

    private PrivateKey pkcs1ToPrivateKey(byte[] pkcs1PrivateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ASN1InputStream in = new ASN1InputStream(pkcs1PrivateKey);
        ASN1Primitive obj = in.readObject();
        RSAPrivateKey keyStruct = RSAPrivateKey.getInstance(obj);
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(keyStruct.getModulus(), keyStruct.getPrivateExponent());
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    private byte[] publicKeyToPkcs1(PublicKey publicKey) throws IOException {
        SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        ASN1Primitive primitive = spkInfo.parsePublicKey();
        return primitive.getEncoded();
    }

    private byte[] privateKeyToPkcs1(PrivateKey privateKey) throws IOException {
        PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        ASN1Encodable encodeable = pkInfo.parsePrivateKey();
        ASN1Primitive primitive = encodeable.toASN1Primitive();
        return primitive.getEncoded();
    }

    public void loadFromKeystore() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(this.keyTag, null);

        if (privateKeyEntry != null) {
            this.privateKey = privateKeyEntry.getPrivateKey();
            this.publicKey = privateKeyEntry.getCertificate().getPublicKey();
        }
    }

    public void deletePrivateKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        keyStore.deleteEntry(this.keyTag);
        this.privateKey = null;
        this.publicKey = null;
    }

    public void generate() throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        this.generate(2048);
    }

    public void generate(int keySize) throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
        kpg.initialize(keySize);

        KeyPair keyPair = kpg.genKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public void generate(String keyTag, Context context) throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        this.generate(keyTag, 2048, context);
    }
    @TargetApi(18)
    public void generate(String keyTag, int keySize, Context context) throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM, "AndroidKeyStore");
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            kpg.initialize(
                    new KeyGenParameterSpec.Builder(
                            keyTag,
                            PURPOSE_ENCRYPT | PURPOSE_DECRYPT | PURPOSE_SIGN | PURPOSE_VERIFY
                    )
                            .setKeySize(keySize)
                            .setDigests(DIGEST_SHA256, DIGEST_SHA512, DIGEST_SHA1)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .build()
            );
        } else {
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.YEAR, 1);
            KeyPairGeneratorSpec.Builder keyPairGeneratorSpec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(keyTag)
                    .setSubject(new X500Principal(
                            String.format("CN=%s, OU=%s", keyTag, context.getPackageName())
                    ))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(Calendar.getInstance().getTime())
                    .setEndDate(endDate.getTime());
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                keyPairGeneratorSpec.setKeySize(keySize).setKeyType(ALGORITHM);
            }
            kpg.initialize(keyPairGeneratorSpec.build());
        }

        KeyPair keyPair = kpg.genKeyPair();
        this.publicKey = keyPair.getPublic();

    }





    public String getCSR() throws IOException {
        byte  CSRder[] = this.csr.getEncoded();
        return dataToPem(CSR_HEADER, CSRder);
    }

}