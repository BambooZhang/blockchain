package com.bamboo.blockchain.utils;

import com.bamboo.blockchain.model2.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;


/***
 * 使用交易的区块
 * 1.初始化并生成创世块
 * 2.查询和使用POW创建新的块
 * 3.简单验证区块：验证哈希+POW验证
 * 4.设置难度数为 difficulty = 1;
 * 5.数字资产存放在交易中，并使用交易树模型存放
 *
 */
public class BlockTransactionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockTransactionUtils.class);
    private static int difficulty = 1;

    /** 本地存储的区块链 */
    private static List<Block> blockChain = new ArrayList<Block>();
    public static int bestHeight=0;//本地区块的初始高度
    //未打包的交易块
    private static Block unpackBlock = new Block();//作为交易缓存,只要交易打包成功则把该值置空
    public static Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();//所有未使用的交易输出项全记录，只要交易成功就把输入项从该记录中清除



    // 初始化交易
    public static Transaction genesisTransaction;//创世交易
    public static Wallet walletA;//当前运行人的钱包
    public static Wallet walletB;//给转账的人的钱包






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
        newBlock.setPreHash(oldBlock.getHash());
        newBlock.setHash(newBlock.calculateHash());
        newBlock.setDifficulty(difficulty);//设置难道数

        /*
         * 这里的 for 循环很重要： 获得 i 的十六进制表示 ，将 Nonce 设置为这个值，并传入 calculateHash 计算哈希值。
         * 之后通过上面的 isHashValid 函数判断是否满足难度要求，如果不满足就重复尝试。 这个计算过程会一直持续，
         * 直到求得了满足要求的 Nonce 值，之后将新块加入到链上。
         */
        for (int i = 0;; i++) {
           // String hex =i; // 生成一个16机制数字作为随机值,这个值决定了hash的最终不一致性
            newBlock.setNonce(i);
            if (!isHashValid(newBlock.calculateHash(), newBlock.getDifficulty())) {
                LOGGER.info("{}-{} need do more work!",i , newBlock.calculateHash());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("error:", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                LOGGER.info("{} work done!", newBlock.calculateHash());
                newBlock.setHash(newBlock.calculateHash());
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

        if (!oldBlock.calculateHash().equals(newBlock.getHash())) {
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

    // Returns difficulty string target, to compare to hash. eg difficulty of 5
    // will return "00000"
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    /**
     * 根据交易链获取merkle根
     * @param transactions
     * @return
     */
    public static String getMerkleRoot(List<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<String>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(EncryptUtils.sha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }


    /**
     * 处理交易
     * @return
     */
    public static boolean processTransaction(Transaction transaction) {

        if(transaction.verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //Gathers transaction inputs (Making sure they are unspent):
        for(TransactionInput i : transaction.inputs) { // 交易输入项和全局交易量进行对比
            i.UTXO = UTXOs.get(i.transactionOutputId);
        }

        //Checks if transaction is valid:
        if(transaction.getInputsValue() < transaction.minimumTransaction) {// 检查交易额度是否达到限制标准
            System.out.println("Transaction Inputs too small: " + transaction.getInputsValue());
            System.out.println("Please enter the amount greater than " + transaction.minimumTransaction);
            return false;
        }

        //Generate transaction outputs:
        // 计算交易的剩余额度，并产生交易输出(发送人的剩余额度,接收人的额度)，这里不给矿工交易费用
        float leftOver = transaction.getInputsValue() - transaction.value; //get value of inputs then the left over change:
        transaction.transactionId = transaction.calulateHash();
        transaction.outputs.add(new TransactionOutput( transaction.reciepient, transaction.value,transaction.transactionId)); //send value to recipient
        transaction.outputs.add(new TransactionOutput( transaction.sender, leftOver,transaction.transactionId)); //send the left over 'change' back to sender

        //Add outputs to Unspent list
        for(TransactionOutput o : transaction.outputs) {// 把交易输出放入未交易的全记录当中
            UTXOs.put(o.id , o);
        }

        //Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : transaction.inputs) {// 把未交易的全纪录清除已经参与过的交易输入项
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            UTXOs.remove(i.UTXO.id);
        }

        return true;
    }


    public static void main(String[] args) throws  Exception {
        final Gson gson = new GsonBuilder().create();
        final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();



        File dataFile = new File("block.bin");// 文件作为存储节点数据(SPV所需结构存储)
        Wallet coinbase = new Wallet();// 启动系统钱包
        walletA = new Wallet();// 钱包A
        walletB = new Wallet();// 钱包B
        if (!dataFile.exists()) {//如果本地缓存不存在则先初始交易,并向运行人的钱包转账一笔作为初始块
            // hard code genesisBlock
            //create genesis transaction, which sends 100 NoobCoin to walletA:
            genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
            genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
            genesisTransaction.transactionId = "0"; //manually set the transaction id
            genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
            UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

            LOGGER.info("Creating and Mining Genesis block... ");
            //创世块
            Block genesisBlock = new Block();
            genesisBlock.setPreHash("0");
            genesisBlock.setIndex(1);
            genesisBlock.setDifficulty(difficulty);
            genesisBlock.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//当前的时间戳
            genesisBlock.addTransaction(genesisTransaction);
            genesisBlock.setHash(genesisBlock.mineBlock());
            blockChain.add(genesisBlock);
            FileUtils.writeStringToFile(dataFile,gson.toJson(genesisBlock));


        }else{
            //已经存在则吧数据取出来
            List<String> list = FileUtils.readLines(dataFile);
            for(String line:list){
                Block b =gson.fromJson(line, Block.class);
                Transaction  transaction =b.getTransactions().get(0);
                UTXOs.put(transaction.outputs.get(0).id, transaction.outputs.get(0));
                blockChain.add(b);
            }
        }
        TimeUnit.SECONDS.sleep(2);
        //pretty print
        LOGGER.info(prettyGson.toJson(blockChain));


         bestHeight = blockChain.size();//设置区块的参照高度


        //sart http server if not config this default value 4567
        port(4567);

        /**
         * 查询区块列表
         * get /
         */
        get("/", (request, response) -> prettyGson.toJson(blockChain));


        /**
         * 查询区块列表
         * get /
         */
        post("/",  (request, response) ->{
            String body = request.body();
            TxMessage m = gson.fromJson(body, TxMessage.class);
            if (m == null) {
                return "TxMessage is NULL";
            }

            String cmd=m.getType();//类型
            String payload =m.getData();//数据

            if ("VERACK".equalsIgnoreCase(cmd)) {
                // 对方确认知道了,并给我区块高度
                bestHeight = Integer.parseInt(m.getData());
                //哈希暂时不校验
            } else if ("VERSION".equalsIgnoreCase(cmd)) {
                // 对方发来握手信息
                // 获取区块高度和版本号信息
                bestHeight = Integer.parseInt(m.getData());
                //我方回复：知道了
              //  pt.peerWriter.write("VERACK " + blockChain.size() + " " + blockChain.get(blockChain.size() - 1).getHash());
            } else if ("BLOCK".equalsIgnoreCase(cmd)) {
                //把对方给的块存进链中
                Block newBlock = gson.fromJson(payload, Block.class);
                if (!blockChain.contains(newBlock)) {
                    LOGGER.info("Attempting to add Block: " + payload);
                    // 校验区块，如果成功，将其写入本地区块链
                    if (Block.isBlockValid(newBlock, blockChain.get(blockChain.size() - 1))) {
                        blockChain.add(newBlock);
                        LOGGER.info("Added block " + newBlock.getIndex() + " with hash: ["+ newBlock.getHash() + "]");
                        FileUtils.writeStringToFile(dataFile,"\r\n"+gson.toJson(newBlock),true);
                      //  peerNetwork.broadcast("BLOCK " + payload);
                        return gson.toJson(payload);
                    }
                }
            } else if ("GET_BLOCK".equalsIgnoreCase(cmd)) {
                //把对方请求的块给对方
                Block block = blockChain.get(Integer.parseInt(payload));
                if (block != null) {
                    LOGGER.info("Sending block " + payload + " to peer");
                   // pt.peerWriter.write("BLOCK " + gson.toJson(block));
                    return gson.toJson(block);
                }
            } else if ("ADDR".equalsIgnoreCase(cmd)) {
                // 对方发来地址，建立连接并保存
               /* if (!peers.contains(payload)) {
                    String peerAddr = payload.substring(0, payload.indexOf(":"));
                    int peerPort = Integer.parseInt(payload.substring(payload.indexOf(":") + 1));
                    peerNetwork.connect(peerAddr, peerPort);
                    peers.add(payload);
                    PrintWriter out = new PrintWriter(peerFile);
                    for (int k = 0; k < peers.size(); k++) {
                        out.println(peers.get(k));
                    }
                    out.close();
                }*/
            } else if ("GET_ADDR".equalsIgnoreCase(cmd)) {
                //对方请求更多peer地址，随机给一个
                Random random = new Random();
               // pt.peerWriter.write("ADDR " + peers.get(random.nextInt(peers.size())));
            }else if("getbalance".equals(cmd)){// 1查看余额
                LOGGER.info("\nWalletA's balance is: " + walletA.getBalance());
                return  "A's balance is"+walletA.getBalance();
            } else if("send".equals(cmd)){// 2向账户B转账
                int vac = Integer.parseInt(m.getData());
                LOGGER.info("\nWalletA's balance is: " + walletA.getBalance());
                LOGGER.info("\nWalletA is Attempting to send funds ("+vac+") to WalletB...");
                Transaction tx = walletA.sendFunds(walletB.publicKey, vac);
                if(tx!=null && unpackBlock.addTransaction(tx)){
                    return  "send funds success";
                }else {
                    return  "Enough funds";
                }
            } else if ("mine".equalsIgnoreCase(cmd)) {//3 设置难度并且挖矿
                try {
                    int difficulty = Integer.parseInt(m.getData());
                    // 挖矿打包新的块
                    if(unpackBlock.transactions.isEmpty()){
                        return "Block write failed!No Transaction existed!";
                    }else{
                        Block newBlock = Block.generateBlock(blockChain.get(blockChain.size() - 1), difficulty,unpackBlock.transactions);
                        if (Block.isBlockValid(newBlock, blockChain.get(blockChain.size() - 1))) {
                            blockChain.add(newBlock);
                            unpackBlock=new Block();//清除缓存

                            FileUtils.writeStringToFile(dataFile,"\r\n"+gson.toJson(newBlock),true);
                            return "Block write Success!";
                        } else {
                            return "500: Invalid vac Error";
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("invalid vac - Virtual Asset Count(Integer)");
                    return "invalid vac : Invalid vac Error";
                }
            }else{
                return "HTTP 500: Invalid Block Error";
            }


            // ********************************
            // 		比较区块高度,同步区块
            // ********************************
            int localHeight = blockChain.size();
            if (bestHeight > localHeight) {//如果本地区块高度没有其他的区块高度高则需要获取其他区块数据并同步到本地
                LOGGER.info("Local chain height: " + localHeight+" Best chain Height: " + bestHeight);
                TimeUnit.MILLISECONDS.sleep(300);

                for (int i = localHeight; i < bestHeight; i++) {
                    LOGGER.info("request get block[" + i + "]...");
                    //peerNetwork.broadcast("GET_BLOCK " + i);
                    return "请把你的区块第["+i+"]条数据发给我，我需要同步到本地";
                }
            }



            return "success!";
        });

        LOGGER.info(gson.toJson(blockChain));
    }
}
