package models;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.StringArg;
import env.Constants;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import static im.mak.paddle.util.Script.fromFile;

public class Breeder extends DApp {

    public static final String ORIGIN_SCRIPT = fromFile("ducks-breeder.ride");
    public static final int DELAY_FOR_BREEDING = 2;

    public Breeder(Account account, Address incubator) {
        super(account.privateKey(), 0,
                ORIGIN_SCRIPT
                        .replace(Constants.INCUBATOR_ADDRESS, incubator.toString())
        );
    }

    public Breeder(long initialBalance, Address incubator) {
        this(new Account(initialBalance), incubator);
    }

    public DAppCall startDuckBreeding() {
        return new DAppCall(this.address(), Function.as("startDuckBreeding"));
    }

    public DAppCall finishDuckBreeding(Id txId) {
        return new DAppCall(this.address(), Function.as("finishDuckHatching", StringArg.as(txId.toString())));
    }

}
