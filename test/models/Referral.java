package models;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.StringArg;
import env.Constants;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import static im.mak.paddle.util.Script.fromFile;

public class Referral extends DApp {

    public static final String ORIGIN_SCRIPT = fromFile("referal.ride");

    public static String keyReferredBy(Account user) {
        return "address_" + user.address() + "_referedBy";
    }

    public static String keyEarnedReward(Account user) {
        return "address_" + user.address() + "_earnedReward";
    }

    public static String keyDeliveredReward(Account referrer, Account referral) {
        return "referer_" + referrer.address() + "_referal_" + referral.address() + "_deliveredReward";
    }

    public static String keyAmount(Account referrer, Account referral) {
        return "referer_" + referrer.address() + "_referal_" + referral.address() + "_amount";
    }

    public Referral(Account account, AssetId eggAssetId, Address incubator, Address farming) {
        super(account.privateKey(), 0,
                ORIGIN_SCRIPT
                        .replace(Constants.EGG_ASSET_ID, eggAssetId.toString())
                        .replace(Constants.INCUBATOR_ADDRESS, incubator.toString())
                        .replace(Constants.FARMING_ADDRESS, farming.toString())
        );
    }

    public Referral(long initialBalance, AssetId eggAssetId, Address incubator, Address farming) {
        this(new Account(initialBalance), eggAssetId, incubator, farming);
    }

    public DAppCall addRefKey(Address referrer, String referrerKey) {
        return new DAppCall(this.address(), Function.as("addRefKey",
                StringArg.as(referrer.toString()), StringArg.as(referrerKey)));
    }

    public DAppCall refPayment(String referrerKey) {
        return new DAppCall(this.address(), Function.as("refPayment", StringArg.as(referrerKey)));
    }

    public DAppCall refPayment(Account referrerKey) {
        return refPayment(referrerKey.address().toString());
    }

}
