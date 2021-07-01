import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.Id;
import models.Breeder;
import env.TestEnvironment;
import im.mak.paddle.Account;
import models.Duck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Async.async;

public class BreederTest extends TestEnvironment {

    final Account USER = new Account(WAVES.amount(100));

    Duck DUCK1, DUCK2;

    @BeforeEach
    void before() {
        getEggsFromFaucet(USER, EGG.of(20));
        async(() -> DUCK1 = hatchDuck(USER), () -> DUCK2 = hatchDuck(USER));
    }

    @Test
    void breed() {
        var startTx =
                USER.invoke(BREEDER.startDuckBreeding(), DUCK1.of(1), DUCK2.of(1));
        Id breedingId = startTx.tx().id();

        node().waitForHeight(startTx.height() + Breeder.DELAY_FOR_BREEDING);

        USER.invoke(BREEDER.finishDuckBreeding(breedingId));
    }

}
