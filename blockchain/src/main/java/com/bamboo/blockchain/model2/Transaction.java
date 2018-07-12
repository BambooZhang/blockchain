package com.bamboo.blockchain.model2;

import com.bamboo.blockchain.utils.EncryptUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
	
	public String transactionId; //Contains a hash of transaction*
	public PublicKey sender; //Senders address/public key.
	public PublicKey reciepient; //Recipients address/public key.
	public float value; //Contains the amount we wish to send to the recipient.
	public byte[] signature; //交易签名
	
	public List<TransactionInput> inputs = new ArrayList<TransactionInput>();//交易输入
	public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();//交易输出

	private static int sequence = 0; //A rough count of how many transactions have been generated

	/** 最小交易额  */
	public static final float minimumTransaction = 0.1f;

	// Constructor:
	public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}


	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it, This behavior may not be optimal.
			total += i.UTXO.value;
		}
		return total;
	}

	//交易数据生成签名
	public void generateSignature(PrivateKey privateKey) {
		String data = EncryptUtils.getKey(sender) + EncryptUtils.getKey(reciepient) + Float.toString(value)	;
		signature = EncryptUtils.ECDSASig(privateKey,data);
	}

	// 验证交易签名是否是真实的
	public boolean verifySignature() {
		String data = EncryptUtils.getKey(sender) + EncryptUtils.getKey(reciepient) + Float.toString(value)	;
		return EncryptUtils.verifyECDSASig(sender, data, signature);
	}
	
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

	public String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return EncryptUtils.sha256(
				EncryptUtils.getKey(sender) + EncryptUtils.getKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
}
