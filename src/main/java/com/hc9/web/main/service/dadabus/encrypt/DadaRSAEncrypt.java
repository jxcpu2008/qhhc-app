package com.hc9.web.main.service.dadabus.encrypt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
  
public class DadaRSAEncrypt {  
    /** 字节数据转字符串专用集合  */  
    private static final char[] HEX_CHAR= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};  
  
    /** 从文件中输入流中加载公钥  */  
    public static RSAPublicKey loadDadaBusPublicKey(String dadaBusPublicKeyFilePath) throws Exception{  
        try {  
        	InputStream in = new FileInputStream(dadaBusPublicKeyFilePath); 
            BufferedReader br= new BufferedReader(new InputStreamReader(in));  
            String readLine= null;  
            StringBuilder sb= new StringBuilder();  
            while((readLine= br.readLine())!=null){  
                if(readLine.charAt(0)=='-'){  
                    continue;  
                }else{  
                    sb.append(readLine);  
                }  
            }  
            br.close();
            return loadPublicKey(sb.toString());  
        } catch (IOException e) {  
            throw new Exception("公钥数据流读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("公钥输入流为空");  
        }
    }  
    
    /** 从文件中输入流中加载公钥  */  
    public static RSAPublicKey loadHc9PublicKey(String hc9PublicKeyFilePath) throws Exception{  
        try {  
        	InputStream in = new FileInputStream(hc9PublicKeyFilePath); 
            BufferedReader br= new BufferedReader(new InputStreamReader(in));  
            String readLine= null;  
            StringBuilder sb= new StringBuilder();  
            while((readLine= br.readLine())!=null){  
                if(readLine.charAt(0)=='-'){  
                    continue;  
                }else{  
                    sb.append(readLine);  
                    sb.append('\r');  
                }  
            }  
            br.close();
            return loadPublicKey(sb.toString());  
        } catch (IOException e) {  
            throw new Exception("公钥数据流读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("公钥输入流为空");  
        }
    }  
  
    /** 从字符串中加载公钥  */  
    public static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception{  
        try {  
            byte[] buffer= Base64Utils.decode(publicKeyStr);  
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");  
            X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);  
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec); 
            return publicKey;
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e) {  
            throw new Exception("公钥非法");  
        } catch (NullPointerException e) {  
            throw new Exception("公钥数据为空");  
        }  
    }  
  
    /**  从文件中加载私钥 */  
    public static RSAPrivateKey loadHc9PrivateKey(String hc9PrivateKeyFilePath) throws Exception{  
        try {
        	InputStream in = new FileInputStream(hc9PrivateKeyFilePath);
            BufferedReader br= new BufferedReader(new InputStreamReader(in));  
            String readLine= null;  
            StringBuilder sb= new StringBuilder();  
            while((readLine= br.readLine())!=null){  
                if(readLine.charAt(0)=='-'){  
                    continue;  
                } else {  
                    sb.append(readLine);  
                }  
            }  
            br.close();
            return loadPrivateKey(sb.toString());  
        } catch (IOException e) {  
            throw new Exception("私钥数据读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("私钥输入流为空");  
        }  
    }  
  
    public static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception{  
        try {  
            byte[] buffer= Base64Utils.decode(privateKeyStr);  
            PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);  
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");  
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec); 
            return privateKey;
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e) {  
            throw new Exception("私钥非法");  
        } catch (NullPointerException e) {  
            throw new Exception("私钥数据为空");  
        }  
    }  
  
    /** 
     * 加密过程 
     * @param publicKey 公钥 
     * @param plainTextData 明文数据 
     * @return 
     * @throws Exception 加密过程中的异常信息 
     */  
    public static byte[] encryptByPublicKey(RSAPublicKey publicKey, byte[] plainTextData) throws Exception{  
        if(publicKey== null){  
            throw new Exception("加密公钥为空, 请设置");  
        }  
        Cipher cipher= null;  
        try {  
            cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding", new BouncyCastleProvider());  
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
            byte[] output= cipher.doFinal(plainTextData);  
            return output;  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此加密算法");  
        } catch (NoSuchPaddingException e) {  
            e.printStackTrace();  
            return null;  
        }catch (InvalidKeyException e) {  
            throw new Exception("加密公钥非法,请检查");  
        } catch (IllegalBlockSizeException e) {  
            throw new Exception("明文长度非法");  
        } catch (BadPaddingException e) {  
            throw new Exception("明文数据已损坏");  
        }  
    }  
    
    /** 私钥加密 */
    public static byte[] encryptByPrivateKey(RSAPrivateKey privateKey, byte[] plainTextData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] output= cipher.doFinal(plainTextData);  
        return output;  
    }
  
    /** 
     * 解密过程 
     * @param privateKey 私钥 
     * @param cipherData 密文数据 
     * @return 明文 
     * @throws Exception 解密过程中的异常信息 
     */  
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception{  
        if (privateKey== null){  
            throw new Exception("解密私钥为空, 请设置");  
        }  
        Cipher cipher= null;  
        try {  
            cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());  
            cipher.init(Cipher.DECRYPT_MODE, privateKey);  
            byte[] output= cipher.doFinal(cipherData);  
            return output;  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此解密算法");  
        } catch (NoSuchPaddingException e) {  
            e.printStackTrace();  
            return null;  
        }catch (InvalidKeyException e) {  
            throw new Exception("解密私钥非法,请检查");  
        } catch (IllegalBlockSizeException e) {  
            throw new Exception("密文长度非法");  
        } catch (BadPaddingException e) {  
            throw new Exception("密文数据已损坏");  
        }         
    }  
  
      
    /** 
     * 字节数据转十六进制字符串 
     * @param data 输入数据 
     * @return 十六进制内容 
     */  
    public static String byteArrayToString(byte[] data){  
        StringBuilder stringBuilder= new StringBuilder();  
        for (int i=0; i<data.length; i++){  
            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移  
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0)>>> 4]);  
            //取出字节的低四位 作为索引得到相应的十六进制标识符  
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);  
            if (i<data.length-1){  
                stringBuilder.append(' ');  
            }  
        }  
        return stringBuilder.toString();  
    }  
  
  
    public static void main(String[] args){  
        DadaRSAEncrypt rsaEncrypt= new DadaRSAEncrypt();  
        /** 公钥 */  
        RSAPublicKey publicKey = null;
        //加载公钥  
        try {  
        	//文件输入流对象 
        	 String hc9PublicKeyFilePath = "D:/keystore/dada/hc9_rsa_public_key.pem"; 
        	 publicKey = rsaEncrypt.loadHc9PublicKey(hc9PublicKeyFilePath);
            System.out.println("加载公钥成功");  
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
            System.err.println("加载公钥失败");  
        }
  
        /** 私钥  */  
        RSAPrivateKey privateKey = null; 
        //加载私钥  
        try {  
        	String hc9PrivateKeyFilePath = "D:/keystore/dada/pkcs8_rsa_private_key.pem"; 
        	privateKey = rsaEncrypt.loadHc9PrivateKey(hc9PrivateKeyFilePath);
            System.out.println("加载私钥成功");  
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
            System.err.println("加载私钥失败");  
        }  
  
        //测试字符串  
        String encryptStr= "该字符串是用于加解密测试的明文，看到此字符串表示公钥加密、私钥解密成功！";  
        try {
            //加密  
            byte[] cipher = rsaEncrypt.encryptByPublicKey(publicKey, encryptStr.getBytes());  
            //解密  
            byte[] plainText = rsaEncrypt.decrypt(privateKey, cipher);  
            System.out.println("密文长度:"+ cipher.length);  
            System.out.println(DadaRSAEncrypt.byteArrayToString(cipher));  
            System.out.println("明文长度:"+ plainText.length);  
            System.out.println(DadaRSAEncrypt.byteArrayToString(plainText));  
            System.out.println(new String(plainText));  
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
        }  
    }  
}