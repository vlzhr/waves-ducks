package env;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.wavesj.StateChanges;
import models.*;
import im.mak.paddle.Account;
import im.mak.paddle.token.Asset;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static models.Breeder.DELAY_FOR_BREEDING;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Async.async;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.*;
import static models.Incubator.*;

public class TestEnvironment {

    // Users
    protected Account ALICE;
    protected Account BOBBY;
    protected Account CAROL;
    protected Account DAVID;

    protected Account FEE_AGGREGATOR;
    protected Account EGG_ISSUER;
    protected Asset EGG;

    protected Incubator INCUBATOR;
    protected Breeder BREEDER;
    protected Referral REFERRAL;
    protected Farming FARMING;
    protected Auction AUCTION;

    @BeforeEach
    void beforeEach() {
        ALICE = new Account();
        BOBBY = new Account();
        CAROL = new Account();
        DAVID = new Account();

        FEE_AGGREGATOR = new Account();
        EGG_ISSUER = new Account();

        var incubator = new Account();
        var breeder = new Account();
        var referral = new Account();
        var farming = new Account();
        var auction = new Account();

        node().faucet().massTransfer(mt -> mt
                .to(ALICE, WAVES.amount(100))
                .to(BOBBY, WAVES.amount(100))
                .to(CAROL, WAVES.amount(100))
                .to(DAVID, WAVES.amount(100))
                .to(FEE_AGGREGATOR, WAVES.amount(1))
                .to(EGG_ISSUER, WAVES.amount(10))
                .to(incubator, WAVES.amount(1))
                .to(breeder, WAVES.amount(1))
                .to(referral, WAVES.amount(1))
                .to(farming, WAVES.amount(1))
                .to(auction, WAVES.amount(1))
        );

        EGG = new Asset(
                EGG_ISSUER.issue(a -> a.name("EGG-" + currentTimeMillis() % 1000).decimals(8).quantity(1_000_000_00000000L))
                        .tx().assetId());

        async(
                () -> INCUBATOR = new Incubator(incubator, EGG.id(), referral.address()),
                () -> BREEDER = new Breeder(breeder, incubator.address()),
                () -> REFERRAL = new Referral(referral, EGG.id(), incubator.address(), farming.address()),
                () -> FARMING = new Farming(farming, EGG.id(), incubator.address(), breeder.address(), referral.address()),
                () -> AUCTION = new Auction(auction, incubator.address(), breeder.address(), FEE_AGGREGATOR.address())
        );
    }

    protected void getEggsFromFaucet(Account recipient, long amount) {
        EGG_ISSUER.transfer(recipient, amount, EGG.id());
    }

    protected void getEggsFromFaucet(Account recipient, Amount amount) {
        if (EGG.id().equals(amount.assetId()))
            getEggsFromFaucet(recipient, amount.value());
        else
            throw new IllegalArgumentException("Faucet provides only Eggs");
    }

    protected Duck hatchDuck(Account user) {
        var startInfo = user.invoke(INCUBATOR.startDuckHatching(), EGG.of(10));
        var startId = startInfo.tx().id();
        var startHeight = startInfo.height();
        node().waitForHeight(startHeight + DELAY_FOR_HATCHING);

        StateChanges result = user.invoke(INCUBATOR.finishDuckHatching(startId)).stateChanges();
        return Duck.of(result.transfers().get(0).assetId());
    }

    protected Duck hatchFakeDuck(Account user, String genotype, String color) {
        var issueTx = INCUBATOR.issueNft(n -> n
                .name("DUCK-" + genotype + "-G" + color)
                .description("{\"genotype\": \"" + genotype + "\", \"crossbreeding\": true}"));
        var duck = Duck.of(issueTx.tx().assetId());
        var price = INCUBATOR.calculatePrice();

        async(
                () -> INCUBATOR.writeData(d -> d
                        .string(keyHatchingStatus(user, issueTx.tx().id()), "HATCHING_FINISHED")
                        .integer(keyHatchingFinishHeight(user, issueTx.tx().id()), issueTx.height())
                        .integer(keyDucksAmount(), INCUBATOR.getDucksAmount() + 1)
                        .integer(keyDucksLastPrice(), price)
                        .string(issueTx.tx().assetId().toString(), issueTx.tx().assetId().toString())
                        .integer(keyStatsAmount(issueTx.tx().name()), INCUBATOR.getStatsAmount(issueTx.tx().name()) + 1)
                        .integer(keyStatsQuantity(duck.farmGen()), INCUBATOR.getStatsQuantity(duck.farmGen()) + 1)),
                () -> user.transfer(INCUBATOR, Amount.of(price, EGG.id())),
                () -> INCUBATOR.transfer(user, duck.of(1)));

        return duck;
    }

    //TODO breedFakeDuck(...)
    protected List<Duck> breedDuck(Account user, AssetId duckIdA, AssetId duckIdB) {
        var startInfo = user.invoke(BREEDER.startDuckBreeding(),
                Amount.of(1, duckIdA), Amount.of(1, duckIdB));
        var startId = startInfo.tx().id();
        var startHeight = startInfo.height();
        node().waitForHeight(startHeight + DELAY_FOR_BREEDING);

        StateChanges result = user.invoke(BREEDER.finishDuckBreeding(startId)).stateChanges();
        return result.transfers()
                .stream()
                .map(t -> Duck.of(t.assetId()))
                .collect(toList());
    }

    protected List<Duck> hatchAndBreedDuck(Account user) {
        final Duck[] ducks = new Duck[2];
        async(() -> ducks[0] = hatchDuck(user), () -> ducks[1] = hatchDuck(user));

        return breedDuck(user, ducks[0], ducks[1]);
    }

    protected Map<String, Duck> hatchFakeAllColorDucks(Account user) {
        Map<String, Duck> ducks = new ConcurrentHashMap<>();
        async(Farming.COLORS
                .stream()
                .<Runnable> map(color -> () -> {
                    var duck = hatchFakeDuck(user, "AAAAAAAA", color);
                    ducks.put(duck.color(), duck);
                }).toArray(Runnable[]::new));
        return ducks;
    }

    protected Map<String, Duck> hatchAllColorDucks(Account user) {
        Map<String, Duck> ducks = new ConcurrentHashMap<>();
        var threadSafe = new Object() {
            ConcurrentMap<Id, Integer> hatching = new ConcurrentHashMap<>();
        };

        while(ducks.size() < 4) {
            var height = node().getHeight();

            List<AssetId> duckIds = Collections.synchronizedList(new ArrayList<>());
            Map<Boolean, List<Map.Entry<Id, Integer>>> parts = threadSafe.hatching
                    .entrySet()
                    .stream()
                    .collect(partitioningBy(e -> e.getValue() <= height));

            //remove filtered items
            threadSafe.hatching = parts.get(false)
                    .stream()
                    .collect(toConcurrentMap(ConcurrentMap.Entry::getKey, ConcurrentMap.Entry::getValue));

            Runnable[] hatches = parts.get(true)
                    .stream()
                    .<Runnable> map(e -> () -> {
                        Id id = e.getKey();
                        user.invoke(INCUBATOR.finishDuckHatching(id));

                        duckIds.add(AssetId.as(INCUBATOR.getStringData(user.address() + "_" + id + "_di")));
                    })
                    .toArray(Runnable[]::new);

            async(hatches);

            for (AssetId duckId : duckIds) {
                var duckName = node().getAssetDetails(duckId).name();
                var duckColor = duckName.substring(duckName.length() - 1);

                if (!ducks.containsKey(duckColor) && !duckColor.equals("U")) {
                    ducks.put(duckColor, new Duck(duckId));

                    if (ducks.size() == 4)
                        return ducks;
                }
            }

            Runnable[] hatchTxs = IntStream
                    .range(0, 15)
                    .boxed()
                    .<Runnable> map(i -> () -> {
                        var startInfo = user.invoke(INCUBATOR.startDuckHatching(),
                                inv -> inv.payments(EGG.of(10)).timestamp(currentTimeMillis() - 10_000L * i));
                        threadSafe.hatching.put(startInfo.tx().id(), startInfo.height() + DELAY_FOR_HATCHING);
                    }).toArray(Runnable[]::new);
            async(hatchTxs);

            node().waitForHeight(height + 1);
        }

        return ducks;
    }

    protected Account[] getAllAccounts() {
        return new Account[]{ALICE, BOBBY, CAROL, DAVID, FEE_AGGREGATOR, EGG_ISSUER,
                INCUBATOR, BREEDER, REFERRAL, FARMING, AUCTION};
    }

    protected Account[] getAllAccountsExcept(Account... exceptAccounts) {
        List<Address> exceptAddresses = Arrays.stream(exceptAccounts)
                .map(Account::address)
                .collect(toList());
        return Arrays.stream(getAllAccounts())
                .filter(a -> !exceptAddresses.contains(a.address()))
                .toArray(Account[]::new);
    }

}
