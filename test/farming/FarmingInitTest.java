package farming;

import env.TestEnvironment;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static models.Farming.keyTotalStartHeight;
import static im.mak.paddle.assertj.PaddleAssertions.assertThat;
import static im.mak.paddle.assertj.PaddleAssertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FarmingInitTest extends TestEnvironment {

    @Test
    void canInitItselfOnlyOnce() {
        assertThat(FARMING).hasNoData();

        var initTx = FARMING.invoke(FARMING.init());

        assertThat(FARMING).hasDataExactly(d -> d.integer(keyTotalStartHeight(), initTx.height()));

        assertThrows(() -> FARMING.invoke(FARMING.init())).hasMessageEndingWith("is already initialized");
    }

    @TestFactory
    Stream<DynamicTest> cannotInitByAnotherAccount() {
        var testCounter = new AtomicInteger();
        return Stream.of(getAllAccountsExcept(FARMING))
                .map(account -> dynamicTest(testCounter.getAndIncrement() + ": " + account.address(), () -> {
                    assertThat(FARMING).hasNoData();

                    assertThrows(() -> account.invoke(FARMING.init())).hasMessageEndingWith("admin only");
                }));
    }

    @TestFactory
    Stream<DynamicTest> cannotInitTwiceByAnotherAccount() {
        var testCounter = new AtomicInteger();
        FARMING.invoke(FARMING.init());
        return Stream.of(getAllAccountsExcept(FARMING))
                .map(account -> dynamicTest(testCounter.getAndIncrement() + " " + account.address(), () ->
                        assertThrows(() -> account.invoke(FARMING.init())).hasMessageEndingWith("admin only")));
    }

}
