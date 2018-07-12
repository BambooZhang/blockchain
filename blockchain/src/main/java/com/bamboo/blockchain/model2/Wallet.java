package com.bamboo.blockchain.model2;

import com.bamboo.blockchain.utils.BlockTransactionUtils;
import com.bamboo.blockchain.utils.EncryptUtils;
import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 钱包：私钥,公钥
 * <p>
 *     用户安装的时候生成私钥(ECDSA椭圆曲线数字签名)和公钥，根据公钥生成钱包地址：在这里只要用户运行第一次就生成一个钱包
 * </p>
 */
public class Wallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();//钱包里未使用过的交易数据

	public Wallet() {
		generateKeyPair();
	}

	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); //256
	        KeyPair keyPair = keyGen.generateKeyPair();//生成非对称秘钥
	        // Set the public and private keys from the keyPair
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();

		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}


	// key转成sha256的字符串
	public String getPublicKey() throws Exception {
		return EncryptUtils.sha256(this.publicKey.getEncoded());
	}
	// key转成sha256的字符串
	public String getPrivateKey() throws Exception {
		return EncryptUtils.sha256(this.privateKey.getEncoded());
	}
	/**
	 * 根据指定钱包公钥生成钱包地址，加密算法：RIPEMD160(SHA256(PubKey))
	 * @return
	 */
	public String getAddress() throws Exception {
			byte[] b = EncryptUtils.ripeMD160(EncryptUtils.sha256(this.publicKey.getEncoded()));
			return new String(Hex.encode(b));
	}

	// 同步我自己的交易数据到本地,并计算剩余额度
	public float getBalance() {
		float total = 0;
        for (Map.Entry<String, TransactionOutput> item: BlockTransactionUtils.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
            	total += UTXO.value ;
            }
        }
		return total;
	}

	//向别人转账：创建一个新的交易
	public Transaction sendFunds(PublicKey recipient,float value ) {
		if(getBalance() < value) {// 判断是否有足够的额度支付
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();//输入项缓存


		//遍历自己的所有未交易数据计算总额能否不小于当前交易的额度
		//只要有几个交易的额度和>=当前交易额度则停止遍历，使用这个几个交易作为输入项
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total >= value) break;
		}

		//使用这些输入项创建交易
		Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		//清除即将使用的这几个交易数据,但是不能清理公共缓存中的全额交易数据,因为要做交易验证使用
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}

		return newTransaction;
	}



	public static void main(String arg []) throws  Exception {
		Wallet wallet=new Wallet();
		 System.out.println("钱包地址RIPEMD160(SHA256(PubKey))：\n"+wallet.getAddress());
		System.out.println("钱包公钥SHA256：\n"+wallet.getPublicKey());
		System.out.println("钱包私钥SHA256：\n"+wallet.getPrivateKey());
		byte[] b = EncryptUtils.ECDSASig(wallet.privateKey,"1");
		System.out.println("签名SHA256：\n"+EncryptUtils.sha256(b));
		System.out.println("验证签名：\n"+EncryptUtils.verifyECDSASig(wallet.publicKey,"1",b));

		String data ;// = EncryptUtils.getKey(wallet.publicKey) + EncryptUtils.getKey(wallet.publicKey) + Float.toString(1l)	;
		data="d605307d2bd576d5833a80a16942a9268ad3af6399f9facc06f9b24615dbdd4891e55acc79c73e35c9560916882a67b5563dd4eec120dde633b80cf7bb0481c2100.0";
		;
		System.out.println("验证签名：\n"+EncryptUtils.sha256(EncryptUtils.ECDSASig(wallet.privateKey,data)));
	}
}


