package com.bamboo.blockchain.utils;

import com.bamboo.blockchain.model1.Block;
import com.bamboo.blockchain.model1.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;


/***
 * ������+POW������
 * 1.��ʼ�������ɴ�����
 * 2.��ѯ��ʹ��POW�����µĿ�
 * 3.����֤���飺��֤��ϣ+POW��֤
 * 4.�����Ѷ���Ϊ difficulty = 1;
 *
 */
public class BlockPowUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPowUtils.class);
    private static List<Block> blockChain = new LinkedList<Block>();
    private static int difficulty = 1;
    /***
     *
     * ʹ�����ֵ���ι�ϣ
     * �������������� SHA256 ɢ��ֵ:ʹ�����ֵnonce
     * @param block
     * @return
     */
    public static String calculateHash(Block block) {

        String record = (block.getIndex()) + block.getTimestamp() + (block.getVac()) + block.getPreHash()+block.getNonce();
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] hash = digest.digest(StringUtils.getBytesUtf8(record));
        return Hex.encodeHexString(hash);

    }


    /**
     * �������+��ҪPOW����(nonce��Ϊ����)
     * @param oldBlock
     * @param vac
     * @return
     */
    public static Block generateBlock(Block oldBlock, int vac) {
        Block newBlock = new Block();
        newBlock.setIndex(oldBlock.getIndex() + 1);
        newBlock.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        newBlock.setVac(vac);
        newBlock.setPreHash(oldBlock.getHash());
        newBlock.setHash(calculateHash(newBlock));
        newBlock.setDifficulty(difficulty);//�����ѵ���

        /*
         * ����� for ѭ������Ҫ�� ��� i ��ʮ�����Ʊ�ʾ ���� Nonce ����Ϊ���ֵ�������� calculateHash �����ϣֵ��
         * ֮��ͨ������� isHashValid �����ж��Ƿ������Ѷ�Ҫ�������������ظ����ԡ� ���������̻�һֱ������
         * ֱ�����������Ҫ��� Nonce ֵ��֮���¿���뵽���ϡ�
         */
        for (int i = 0;; i++) {
            String hex = String.format("%x", i); // ����һ��16����������Ϊ���ֵ,���ֵ������hash�����ղ�һ����
            newBlock.setNonce(hex);
            if (!isHashValid(calculateHash(newBlock), newBlock.getDifficulty())) {
                LOGGER.info("{}-{} need do more work!",hex , calculateHash(newBlock));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("error:", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                LOGGER.info("{} work done!", calculateHash(newBlock));
                newBlock.setHash(calculateHash(newBlock));
                break;
            }
        }
        return newBlock;
    }

    /**
     * ��������������repeat��ǰ׺str���ַ���
     * @param str
     * @param repeat
     * @return
     */
    private static String repeat(String str, int repeat) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            buf.append(str);
        }
        return buf.toString();
    }

    /**
     * У��HASH�ĺϷ���:��ҪУ��hashֵǰ׺�ĸ���
     *
     * @param hash
     * @param difficulty
     * @return
     */
    public static boolean isHashValid(String hash, int difficulty) {
        String prefix = repeat("0", difficulty);
        return hash.startsWith(prefix);
    }


    /***
     * �����У��
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

        if (!calculateHash(newBlock).equals(newBlock.getHash())) {
            return false;
        }

        return true;
    }


    /**
     * ����б�������㳤�����ñ��㳤������Ϊ������
     *
     * @param oldBlocks
     * @param newBlocks
     * @return �����
     */
    public List<Block> replaceChain(List<Block> oldBlocks, List<Block> newBlocks) {
        if (newBlocks.size() > oldBlocks.size()) {
            return newBlocks;
        }else{
            return oldBlocks;
        }
    }

    public static void main(String[] args) {
        //������
        Block genesisBlock = new Block();
        genesisBlock.setIndex(0);
        genesisBlock.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        genesisBlock.setVac(0);
        genesisBlock.setPreHash("");
        genesisBlock.setHash(calculateHash(genesisBlock));
        blockChain.add(genesisBlock);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        /**
         * ��ѯ�����б�
         * get /
         */
        get("/", (request, response) -> gson.toJson(blockChain));

        /***
         * ����������
         * post / {"vac":75}
         */
        post("/", (request, response) ->{
            String body = request.body();
            Message m = gson.fromJson(body, Message.class);
            if (m == null) {
                return "vac is NULL";
            }
            int vac = m.getVac();
            Block lastBlock = blockChain.get(blockChain.size() - 1);
            Block newBlock = generateBlock(lastBlock, vac);
            if (isBlockValid(newBlock, lastBlock)) {
                blockChain.add(newBlock);
                LOGGER.debug(gson.toJson(blockChain));
            } else {
                return "HTTP 500: Invalid Block Error";
            }
            return "success!";
        });

        LOGGER.info(gson.toJson(blockChain));
    }
}
