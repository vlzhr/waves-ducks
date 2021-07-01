package models;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.transactions.data.IntegerEntry;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.StringArg;
import env.Constants;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static im.mak.paddle.util.Script.fromFile;
import static java.util.Arrays.asList;

public class Incubator extends DApp {

    public static final String ORIGIN_SCRIPT = fromFile("ducks-incubator.ride");
    public static final int DELAY_FOR_HATCHING = 2;
    public static final int DISCOUNT = 10;
    public static final int TYPES_AMOUNT = 6;

    public static String keyHatchingStatus(Account user, Id hatchId) {
        return user.address() + "_" + hatchId +  "_status";
    }

    public static String keyHatchingFinishHeight(Account user, Id hatchId) {
        return user.address() + "_" + hatchId +  "_fh";
    }

    public static String keyDuckId(Account user, Id hatchId) {
        return user.address() + "_" + hatchId +  "_fh";
    }

    public static String keyStatsAmount(String duckName) {
        return "stats_" + duckName + "_amount";
    }

    public static String keyStatsQuantity(String duckGen) {
        return "stats_" + duckGen + "_quantity";
    }

    public static String keyDucksAmount() {
        return "ducks_amount";
    }

    public static String keyDucksLastPrice() {
        return "ducks_last_price";
    }

    public Incubator(Account account, AssetId eggAssetId, Address referral) {
        super(account.privateKey(), 0,
                ORIGIN_SCRIPT
                        .replace(Constants.EGG_ASSET_ID, eggAssetId.toString())
                        .replace(Constants.REFERRAL_ADDRESS, referral.toString())
        );
    }

    public Incubator(long initialBalance, AssetId eggAssetId, Address referral) {
        this(new Account(initialBalance), eggAssetId, referral);
    }

    public DAppCall startDuckHatching(String referrer) {
        return new DAppCall(this.address(), Function.as("startDuckHatching", StringArg.as(referrer)));
    }

    public DAppCall startDuckHatching() {
        return startDuckHatching("");
    }

    public DAppCall finishDuckHatching(Id txId) {
        return new DAppCall(this.address(), Function.as("finishDuckHatching", StringArg.as(txId.toString())));
    }

    public long getDucksAmount() {
        var entries = getData(asList(keyDucksAmount()));
        return entries.size() > 0 ? ((IntegerEntry) entries.get(0)).value() : 0;
    }

    public long getStatsAmount(String duckName) {
        var entries = getData(asList(keyStatsAmount(duckName)));
        return entries.size() > 0 ? ((IntegerEntry) entries.get(0)).value() : 0;
    }

    public long getStatsQuantity(String duckGen) {
        var entries = getData(asList(keyStatsQuantity(duckGen)));
        return entries.size() > 0 ? ((IntegerEntry) entries.get(0)).value() : 0;
    }

    /**
     * Implements calculations from the "countEggsNeededAmount" user function
     */
    public long calculatePrice() {
        return new BigDecimal(100 + (getDucksAmount() * DISCOUNT)/(10 * TYPES_AMOUNT))
                .setScale(2, RoundingMode.DOWN)
                .sqrt(MathContext.DECIMAL64)
                .round(new MathContext(2))
                .unscaledValue()
                .longValueExact();
    }

}
