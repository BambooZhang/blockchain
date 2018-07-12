package com.bamboo.blockchain.utils;

import com.bamboo.blockchain.model2.Transaction;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.util.ArrayList;
import java.util.List;


public class EncryptUtils {


    //base64
    public static String base64Encode(String data) {

        return Base64.encodeBase64String(data.getBytes());
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    public static byte[] base64Decode(String data) {

        return Base64.decodeBase64(data.getBytes());
    }

    public static String base64Encode(Key key) {
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }


    //MD5
    public static String md5(String data) {

        return DigestUtils.md5Hex(data);
    }

    //sha1
    public static String sha1(String data) {

        return DigestUtils.sha1Hex(data);
    }

    //sha256Hex
    public static String sha256(String data) {
        return DigestUtils.sha256Hex(data);
    }
    //sha256Hex
    public static String sha256(byte[] data) {
        return DigestUtils.sha256Hex(data);
    }


    ////////////////////RipeMD160��ϢժҪ����///////////////////////////
    /**
     * RipeMD160��ϢժҪ
     * @param str ���������ϢժҪ����
     * @return byte[] ��ϢժҪ
     * */
    public static byte[] ripeMD160(String str) throws Exception{
        //����BouncyCastleProvider��֧��
        Security.addProvider(new BouncyCastleProvider());
        //��ʼ��MessageDigest
        MessageDigest md=MessageDigest.getInstance("RipeMD160");
        //ִ����ϢժҪ
        return md.digest(str.getBytes());

    }
    /**
     * RipeMD160Hexʮ��������ϢժҪ�㷨ֵ
     * @param str ���������ϢժҪ����
     * @return String ��ϢժҪ
     * **/
    public static String  ripeMD160Hex(String str) throws Exception{
        //ִ����ϢժҪ
        byte[] b=ripeMD160(str);
        //��ʮ�����Ƶı��봦��
        return new String(Hex.encode(b));
    }


    /**
     * ECDSAǩ��
     * @param privateKey ˽Կ
     * @param input ԭ��
     * @return
     */
    // Applies ECDSA Signature and returns the result ( as bytes ).
    public static byte[] ECDSASig(PrivateKey privateKey, String input) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     * ��֤ECDSAǩ��
     * Verifies a String signature
     *
     * @param publicKey ��Կ
     * @param data  Ҫ��֤�������ļ�
     * @param signature  ���ļ���ǩ��
     * @return
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Short hand helper to turn Object into a json string
    public static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    // Returns difficulty string target, to compare to hash. eg difficulty of 5
    // will return "00000"
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    /**
     * ��ȡkey��sha256�ַ���
     * @param key
     * @return
     */
    public static String getKey(Key key) {
        return sha256(key.getEncoded());
    }




    public static void main(String arg [])  throws Exception{
        System.out.println(EncryptUtils.ripeMD160Hex("111"));
    }
}
