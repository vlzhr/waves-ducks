package models;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.IntegerArg;
import com.wavesplatform.transactions.invocation.StringArg;
import env.Constants;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import static im.mak.paddle.util.Script.fromFile;

public class Auction extends DApp {

    public static final String ORIGIN_SCRIPT = fromFile("ducks-auction.ride");

    public Auction(Account account, Address incubator, Address breeder, Address feeAggregator) {
        super(account.privateKey(), 0,
                ORIGIN_SCRIPT
                        .replace(Constants.INCUBATOR_ADDRESS, incubator.toString())
                        .replace(Constants.BREEDER_ADDRESS, breeder.toString())
                        .replace(Constants.FEE_AGGREGATOR_ADDRESS, feeAggregator.toString())
        );
    }

    public Auction(long initialBalance, Address incubator, Address breeder, Address feeAggregator) {
        this(new Account(initialBalance), incubator, breeder, feeAggregator);
    }

    public DAppCall initAuction(long startPrice, long instantPrice, String description) {
        return new DAppCall(this.address(), Function.as("initAuction",
                IntegerArg.as(startPrice), IntegerArg.as(instantPrice), StringArg.as(description)));
    }

    public DAppCall cancelAuction(Id auctionId) {
        return new DAppCall(this.address(), Function.as("cancelAuction", StringArg.as(auctionId.toString())));
    }

    public DAppCall instantBuy(Id auctionId) {
        return new DAppCall(this.address(), Function.as("instantBuy", StringArg.as(auctionId.toString())));
    }

    public DAppCall placeBid(Id auctionId) {
        return new DAppCall(this.address(), Function.as("placeBid", StringArg.as(auctionId.toString())));
    }

    public DAppCall cancelBid(Id auctionId, Id bidId) {
        return new DAppCall(this.address(), Function.as("cancelBid",
                StringArg.as(auctionId.toString()), StringArg.as(bidId.toString())));
    }

    public DAppCall acceptBid(Id auctionId, Id bidId) {
        return new DAppCall(this.address(), Function.as("acceptBid",
                StringArg.as(auctionId.toString()), StringArg.as(bidId.toString())));
    }

}
