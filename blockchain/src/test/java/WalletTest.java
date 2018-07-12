import com.bamboo.blockchain.model2.Wallet;
import org.junit.Before;
import org.junit.Test;

public class WalletTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testWallet() throws Exception {
        Wallet wallet=new Wallet();
       // System.out.println("钱包地址：\n"+wallet.getAddress());
        System.out.println("钱包公钥：\n"+wallet.publicKey);
        System.out.println("钱包私钥：\n"+wallet.privateKey);
    }
}