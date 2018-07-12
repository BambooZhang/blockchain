package com.bamboo.blockchain.model1;

/**
 * 区块1.0
 *
 * 对应的启动类:
 * uitils:BlockUtils 基础区块
 * uitils:BlockPowUtils 使用POW区块和难度系数
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
    /**虚拟资产。我们要记录的数据*/
    private int vac;
    /**
     * 工作量证明，计算正确hash值的时使用的变量
     */
    private String nonce;

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

    public Block() {
        super();
    }

    public Block(int index, String timestamp, String nonce, int difficulty, String preHash, String hash) {
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

    public int getVac() {
        return vac;
    }

    public void setVac(int vac) {
        this.vac = vac;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
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

}
