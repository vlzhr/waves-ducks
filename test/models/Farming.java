package models;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.StringArg;
import env.Constants;
import im.mak.paddle.Account;
import im.mak.paddle.assertj.StateChangesAssert;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static im.mak.paddle.util.Script.fromFile;

public class Farming extends DApp {

    public static final String ORIGIN_SCRIPT = fromFile("farming.ride");
    public static final long PERCH_PRICE = 100;
    public static final List<String> COLORS = List.of("B", "R", "G", "Y");
    public static final String JACKPOT_COLOR = "U";

    public Farming(Account account, AssetId eggAssetId, Address incubator, Address breeder, Address referral) {
        super(account.privateKey(), 0,
                ORIGIN_SCRIPT
                        .replace(Constants.EGG_ASSET_ID, eggAssetId.toString())
                        .replace(Constants.INCUBATOR_ADDRESS, incubator.toString())
                        .replace(Constants.BREEDER_ADDRESS, breeder.toString())
                        .replace(Constants.REFERRAL_ADDRESS, referral.toString())
        );
    }

    public Farming(long initialBalance, AssetId eggAssetId, Address incubator, Address breeder, Address referral) {
        this(new Account(initialBalance), eggAssetId, incubator, breeder, referral);
    }

    public static String getRandomColor() {
        return COLORS.get(new Random().nextInt(COLORS.size()));
    }

    public static String keyTotalStartHeight() {
        return "total_startHeight";
    }

    public static String keyPerchesAvailable(Account user, String color) {
        return "address_" + user.address() + "_perchesAvailable_" + color;
    }

    public static String keyTotalFarmingPower() {
        return "total_farmingPower";
    }

    public static String keyTotalLastCheckInterest() {
        return "total_lastCheckInterest";
    }

    public static String keyTotalLastCheckInterestHeight() {
        return "total_lastCheckInterestHeight";
    }

    public static String keyFarmingPower(Account user, AssetId duckId) {
        return "address_" + user.address() + "_asset_" + duckId + "_farmingPower";
    }

    public static String keyLastCheckInterest(Account user, AssetId duckId) {
        return "address_" + user.address() + "_asset_" + duckId + "_lastCheckInterest";
    }

    public static String keyWithdrawnAmount(Account user, AssetId duckId) {
        return "address_" + user.address() + "_asset_" + duckId + "_withdrawnAmount";
    }

    public static String keyLastCheckFarmedAmount(Account user, AssetId duckId) {
        return "address_" + user.address() + "_asset_" + duckId + "_lastCheckFarmedAmount";
    }

    public static String keyPerchColor(Account user, AssetId jackpotDuckId) {
        return "address_" + user.address() + "_asset_" + jackpotDuckId + "_perchColor";
    }

    public DAppCall init() {
        return new DAppCall(this.address(), Function.as("init"));
    }

    public DAppCall buyPerch(String color, String referrer) {
        return new DAppCall(this.address(), Function.as("buyPerch",
                StringArg.as(color), StringArg.as(referrer)));
    }

    public DAppCall buyPerch(String color, Address referrer) {
        return buyPerch(color, referrer.toString());
    }

    public DAppCall stakeNFT() {
        return new DAppCall(this.address(), Function.as("stakeNFT"));
    }

    public DAppCall unstakeNFT(AssetId asset) {
        return new DAppCall(this.address(), Function.as("unstakeNFT", StringArg.as(asset.toString())));
    }

    public DAppCall stakeJackpot(String color) {
        return new DAppCall(this.address(), Function.as("stakeJackpot", StringArg.as(color)));
    }

    public DAppCall unstakeJackpot(AssetId asset) {
        return new DAppCall(this.address(), Function.as("unstakeJackpot", StringArg.as(asset.toString())));
    }

    public DAppCall claimReward(AssetId asset) {
        return new DAppCall(this.address(), Function.as("claimReward", StringArg.as(asset.toString())));
    }

    public Consumer<StateChangesAssert.StateChangesFields> expectedStakeNftResult(
            Account user, Duck duck, long totalPower, long totalInterest, long checkHeight,
            long power, long interest, long perchesAvailable) {
        return expected -> expected
                .integerEntry(keyTotalFarmingPower(), totalPower)
                .integerEntry(keyTotalLastCheckInterest(), totalInterest)
                .integerEntry(keyTotalLastCheckInterestHeight(), checkHeight)
                .integerEntry(keyFarmingPower(user, duck.id()), power)
                .integerEntry(keyLastCheckInterest(user, duck.id()), interest)
                .integerEntry(keyPerchesAvailable(user, duck.color()), perchesAvailable);
    }

}
