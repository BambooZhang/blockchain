package com.bamboo.blockchain.model2;


public class TransactionInput {
	public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
	public com.bamboo.blockchain.model2.TransactionOutput UTXO; //Contains the Unspent transaction output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
