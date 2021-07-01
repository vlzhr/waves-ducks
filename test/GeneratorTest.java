import com.wavesplatform.transactions.common.Id;
import models.Incubator;
import env.TestEnvironment;
import im.mak.paddle.Account;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.token.Waves.WAVES;

public class GeneratorTest extends TestEnvironment {

    final Account USER = new Account(WAVES.amount(100));

    @Test
    void generate() {
        getEggsFromFaucet(USER, EGG.of(10));
        var hatchingInfo = USER.invoke(INCUBATOR.startDuckHatching(), EGG.of(10));
        Id hatchingId = hatchingInfo.tx().id();

        node().waitForHeight(hatchingInfo.height() + Incubator.DELAY_FOR_HATCHING);

        USER.invoke(INCUBATOR.finishDuckHatching(hatchingId));
    }

}
