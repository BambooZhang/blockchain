package com.bamboo.blockchain.model2;

import com.bamboo.blockchain.utils.BlockTransactionUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 区块2.0
 * 数字资产不在保存在块中，而是存放在交易链中
 *
 *
 */
public class Block {
    /**
     * 区块索引号
     */
    private int index;

    /**
     * 生成区块的时间戳,方便查看使用格式化后的日期格式
     */
    private String timestamp;

    /**
     * 工作量证明，计算正确hash值的时使用的变量
     */
    private int  nonce;

    /**
     * 工作难度
     */
    private int difficulty;

    /**
     * 当前区块的的 SHA256 散列值,区块唯一标识
     */
    private String hash;
    /**
     * 前一个区块的的 SHA256 散列值
     */
    private String preHash;

    /**默克尔根 */
    private String merkleRoot;
    /**每个块包含的交易 */
    public transient List<Transaction> transactions = new ArrayList<Transaction>();


    public Block() {
        super();
    }

    public Block(int index, String timestamp, int nonce, int difficulty, String preHash, String hash) {
        super();
        this.index = index;
        this.timestamp = timestamp;
//        this.transactions = transactions;
        this.nonce = nonce;
        this.difficulty = difficulty;
        this.preHash = preHash;
        this.hash = hash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }


    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }



    /***
     *
     * 使用随机值两次哈希
     * 计算给定的区块的 SHA256 散列值:使用随机值nonce
     * @return
     */
    public String calculateHash() {

        String record = getIndex() + getTimestamp() + getNonce() + getPreHash()+getMerkleRoot();
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] hash = digest.digest(StringUtils.getBytesUtf8(record));
        return Hex.encodeHexString(hash);

    }
    /**
     * 校验区块的合法性（有效性）
     *
     * @param newBlock
     * @param oldBlock
     * @return
     */
    public static boolean isBlockValid(Block newBlock, Block oldBlock) {
        if (oldBlock.getIndex() + 1 != newBlock.getIndex()) {
            return false;
        }
        if (!oldBlock.getHash().equals(newBlock.getPreHash())) {
            return false;
        }
        if (!newBlock.calculateHash().equals(newBlock.getHash())) {
            return false;
        }
        return true;
    }

    /**
     *
     * Increases nonce value until hash target is reached.
     * @return hash
     */
    public String mineBlock() {
        this.merkleRoot = BlockTransactionUtils.getMerkleRoot(transactions);
        String target = BlockTransactionUtils.getDificultyString(difficulty); //Create a string with difficulty * "0"
        hash = calculateHash();
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash =  calculateHash();
        }
        return hash;
    }

    /**
     * 添加交易
     * Add transactions to this block
     * @param transaction
     * @return
     */
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null){
            return false;
        }
        if((!"0".equals(preHash))) {//如果不是创世交易则需要验证
            if((BlockTransactionUtils.processTransaction(transaction) != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }


    /**
     * 区块的生成
     *
     * @param oldBlock
     * @param transactions
     * @return
     */
    public static Block generateBlock(Block oldBlock, int difficulty, List<Transaction> transactions) {
        Block newBlock = new Block();
        newBlock.transactions = transactions;
        newBlock.setIndex(oldBlock.getIndex() + 1);
        newBlock.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        newBlock.setDifficulty(difficulty);
        newBlock.setPreHash(oldBlock.getHash());
        newBlock.setHash(newBlock.mineBlock());
        return newBlock;
    }

}
