package com.bamboo.blockchain.model1;

/**
 * ����1.0
 *
 * ��Ӧ��������:
 * uitils:BlockUtils ��������
 * uitils:BlockPowUtils ʹ��POW������Ѷ�ϵ��
 *
 */
public class Block {
    /**
     * ����������
     */
    private int index;

    /**
     * ���������ʱ���,����鿴ʹ�ø�ʽ��������ڸ�ʽ
     */
    private String timestamp;
    /**�����ʲ�������Ҫ��¼������*/
    private int vac;
    /**
     * ������֤����������ȷhashֵ��ʱʹ�õı���
     */
    private String nonce;

    /**
     * �����Ѷ�
     */
    private int difficulty;

    /**
     * ��ǰ����ĵ� SHA256 ɢ��ֵ,����Ψһ��ʶ
     */
    private String hash;
    /**
     * ǰһ������ĵ� SHA256 ɢ��ֵ
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
