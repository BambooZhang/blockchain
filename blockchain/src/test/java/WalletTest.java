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
       // System.out.println("Ǯ����ַ��\n"+wallet.getAddress());
        System.out.println("Ǯ����Կ��\n"+wallet.publicKey);
        System.out.println("Ǯ��˽Կ��\n"+wallet.privateKey);
    }
}