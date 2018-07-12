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
 * 基础版+POW的区块
 * 1.初始化并生成创世块
 * 2.查询和使用POW创建新的块
 * 3.简单验证区块：验证哈希+POW验证
 * 4.设置难度数为 difficulty = 1;
 *
 */
public class BlockPowUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPowUtils.class);
    private static List<Block> blockChain = new LinkedList<Block>();
    private static int difficulty = 1;
    /***
     *
     * 使用随机值两次哈希
     * 计算给定的区块的 SHA256 散列值:使用随机值nonce
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
     * 块的生成+需要POW工作(nonce作为变量)
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
        newBlock.setDifficulty(difficulty);//设置难道数

        /*
         * 这里的 for 循环很重要： 获得 i 的十六进制表示 ，将 Nonce 设置为这个值，并传入 calculateHash 计算哈希值。
         * 之后通过上面的 isHashValid 函数判断是否满足难度要求，如果不满足就重复尝试。 这个计算过程会一直持续，
         * 直到求得了满足要求的 Nonce 值，之后将新块加入到链上。
         */
        for (int i = 0;; i++) {
            String hex = String.format("%x", i); // 生成一个16机制数字作为随机值,这个值决定了hash的最终不一致性
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
     * 辅助方法：生成repeat个前缀str的字符串
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
     * 校验HASH的合法性:需要校验hash值前缀的个数
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
     * 区块的校验
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
     * 如果有别的链比你长，就用比你长的链作为区块链
     *
     * @param oldBlocks
     * @param newBlocks
     * @return 结果链
     */
    public List<Block> replaceChain(List<Block> oldBlocks, List<Block> newBlocks) {
        if (newBlocks.size() > oldBlocks.size()) {
            return newBlocks;
        }else{
            return oldBlocks;
        }
    }

    public static void main(String[] args) {
        //创世块
        Block genesisBlock = new Block();
        genesisBlock.setIndex(0);
        genesisBlock.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        genesisBlock.setVac(0);
        genesisBlock.setPreHash("");
        genesisBlock.setHash(calculateHash(genesisBlock));
        blockChain.add(genesisBlock);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        /**
         * 查询区块列表
         * get /
         */
        get("/", (request, response) -> gson.toJson(blockChain));

        /***
         * 创建新区块
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
