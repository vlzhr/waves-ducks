package farming;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import env.TestEnvironment;
import models.Duck;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static im.mak.paddle.assertj.PaddleAssertions.assertThat;
import static im.mak.paddle.assertj.PaddleAssertions.assertThrows;
import static im.mak.paddle.util.Async.async;
import static models.Farming.*;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class StakeNftTest extends TestEnvironment {

    @Test
    void canStakeDuckOfRandomColor() {
        async(
                () -> FARMING.invoke(FARMING.init()),
                () -> getEggsFromFaucet(ALICE, EGG.of(100)));

        Duck duck = hatchDuck(ALICE);
        assumeThat(duck.color()).isNotEqualTo(JACKPOT_COLOR);

        ALICE.invoke(FARMING.buyPerch(duck.color(), ""), EGG.of(1));
        var stakeTx = ALICE.invoke(FARMING.stakeNFT(), duck.of(1));

        assertThat(stakeTx.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                ALICE, duck, 100, 0, stakeTx.height(), 100, 0, 0));
    }

    static Stream<String> colors() {
        return COLORS.stream();
    }

    @ParameterizedTest
    @MethodSource("colors")
    void canStakeDuckOfEachColor(String color) {
        async(
                () -> FARMING.invoke(FARMING.init()),
                () -> getEggsFromFaucet(ALICE, EGG.of(100)));

        Duck duck = hatchFakeDuck(ALICE, "FFFFFFFF", color);

        ALICE.invoke(FARMING.buyPerch(color, ""), EGG.of(1));
        var stakeTx = ALICE.invoke(FARMING.stakeNFT(), duck.of(1));

        assertThat(stakeTx.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                ALICE, duck, 100, 0, stakeTx.height(), 100, 0, 0));
    }

    @Test
    void userCanStakeTwiceSameColorAndGenotype() {
        async(
                () -> FARMING.invoke(FARMING.init()),
                () -> getEggsFromFaucet(ALICE, EGG.of(100)));

        var colorB = COLORS.get(0);
        var duck1 = hatchFakeDuck(ALICE, "AAAAAAAA", colorB);
        var duck2 = hatchFakeDuck(ALICE, "AAAAAAAA", colorB);

        async(
                () -> ALICE.invoke(FARMING.buyPerch(colorB, ""), p -> p.payments(EGG.of(1)).timestamp(System.currentTimeMillis() - 10000)),
                () -> ALICE.invoke(FARMING.buyPerch(colorB, ""), EGG.of(1)));

        var stakeTx1 = ALICE.invoke(FARMING.stakeNFT(), duck1.of(1));
        var stakeTx2 = ALICE.invoke(FARMING.stakeNFT(), duck2.of(1));
        assumeThat(stakeTx2.height()).isEqualTo(stakeTx1.height());

        assertThat(stakeTx1.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                ALICE, duck1, 70, 0, stakeTx1.height(), 70, 0, 1));
        assertThat(stakeTx2.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                ALICE, duck2, 140, 0, stakeTx2.height(), 70, 0, 0));
    }

    @Test
    void twoUsersCanStakeSameColorAndGenotype() {
        async(
                () -> FARMING.invoke(FARMING.init()),
                () -> getEggsFromFaucet(ALICE, EGG.of(100)),
                () -> getEggsFromFaucet(BOBBY, EGG.of(100)));

        var colorB = COLORS.get(0);
        var aliceDuck = hatchFakeDuck(ALICE, "AAAAAAAA", colorB);
        var bobbyDuck = hatchFakeDuck(BOBBY, "AAAAAAAA", colorB);

        async(
                () -> ALICE.invoke(FARMING.buyPerch(colorB, ""), EGG.of(1)),
                () -> BOBBY.invoke(FARMING.buyPerch(colorB, ""), EGG.of(1)));

        var aliceStakeTx = ALICE.invoke(FARMING.stakeNFT(), aliceDuck.of(1));
        var bobbyStakeTx = BOBBY.invoke(FARMING.stakeNFT(), bobbyDuck.of(1));
        assumeThat(bobbyStakeTx.height()).isEqualTo(aliceStakeTx.height());

        assertThat(aliceStakeTx.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                ALICE, aliceDuck, 70, 0, aliceStakeTx.height(), 70, 0, 0));
        assertThat(bobbyStakeTx.stateChanges()).containsExactly(FARMING.expectedStakeNftResult(
                BOBBY, bobbyDuck, 140, 0, bobbyStakeTx.height(), 70, 0, 0));
    }

    @Nested
    class Negative {

        @Test
        void cannotStakeNftIfNotInit() {
            getEggsFromFaucet(ALICE, EGG.of(100));
            var duck = hatchDuck(ALICE);

            assertThrows(() -> ALICE.invoke(FARMING.stakeNFT(), duck.of(1)))
                    .hasMessageEndingWith("not init");
        }

        @TestFactory
        Stream<DynamicTest> cannotStakeNftWithNoPerchesAvailable() {
            async(
                    () -> FARMING.invoke(FARMING.init()),
                    () -> getEggsFromFaucet(ALICE, EGG.of(10_000)));

            return hatchAllColorDucks(ALICE).entrySet().stream().map(duck ->
                    dynamicTest(duck.getKey(), () ->
                            assertThrows(() -> ALICE.invoke(FARMING.stakeNFT(), Amount.of(1, duck.getValue())))
                                    .hasMessageEndingWith("no perches available for the color " + duck.getKey())));
        }

        @TestFactory
        Stream<DynamicTest> cannotStakeNftWithInvalidAsset() {
            async(
                    () -> FARMING.invoke(FARMING.init()),
                    () -> getEggsFromFaucet(ALICE, EGG.of(10_000)));

            var pseudoDuck = ALICE.issueNft(a -> a.name("DUCK-AAAAAAAA-GB")).tx().assetId();

            var testCounter = new AtomicInteger();
            return Stream.of(AssetId.WAVES, EGG.id(), pseudoDuck).map(assetId ->
                    dynamicTest(testCounter.getAndIncrement() + ": " + assetId.toString(), () ->
                            assertThrows(() -> ALICE.invoke(FARMING.stakeNFT(), Amount.of(1, assetId)))
                                    .hasMessageEndingWith("Only ducks are accepted")));
        }

    }

}
