import com.wavesplatform.transactions.common.Amount;
import env.TestEnvironment;
import models.Duck;
import org.junit.jupiter.api.Test;

public class AuctionTest extends TestEnvironment {

    @Test
    void breed() {
        getEggsFromFaucet(ALICE, EGG.of(10));
        Duck DUCK = hatchDuck(ALICE);

        ALICE.invoke(AUCTION.initAuction(1, 2, ""), DUCK.of(1)).tx().id();
    }

}
