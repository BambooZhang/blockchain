package com.bamboo.blockchain.utils;

import com.bamboo.blockchain.model1.Block;
import com.bamboo.blockchain.model1.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;


/***
 * 基础版的区块
 * 1.初始化并生成创世块
 * 2.查询和创建新的块
 * 3.简单验证区块：验证哈希
 *
 */
public class BlockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockUtils.class);
    private static List<Block> blockChain = new LinkedList<>();
    /***
     * 计算给定的区块的 SHA256 散列值
     * @param block
     * @return
     */
    public static String calculateHash(Block block) {

        String record = (block.getIndex()) + block.getTimestamp() + (block.getVac()) + block.getPreHash();

        return EncryptUtils.sha256(record);

    }


    /**
     * 块的生成
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

        return newBlock;
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
