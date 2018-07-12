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


    ////////////////////RipeMD160消息摘要处理///////////////////////////
    /**
     * RipeMD160消息摘要
     * @param str 待处理的消息摘要数据
     * @return byte[] 消息摘要
     * */
    public static byte[] ripeMD160(String str) throws Exception{
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化MessageDigest
        MessageDigest md=MessageDigest.getInstance("RipeMD160");
        //执行消息摘要
        return md.digest(str.getBytes());

    }
    /**
     * RipeMD160Hex十六进制消息摘要算法值
     * @param str 待处理的消息摘要数据
     * @return String 消息摘要
     * **/
    public static String  ripeMD160Hex(String str) throws Exception{
        //执行消息摘要
        byte[] b=ripeMD160(str);
        //做十六进制的编码处理
        return new String(Hex.encode(b));
    }


    /**
     * ECDSA签名
     * @param privateKey 私钥
     * @param input 原文
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
     * 验证ECDSA签名
     * Verifies a String signature
     *
     * @param publicKey 公钥
     * @param data  要验证的数据文件
     * @param signature  该文件的签名
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
     * 获取key的sha256字符串
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
