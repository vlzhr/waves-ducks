package models;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.token.Asset;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Duck extends Asset {

    public static Duck of(AssetId assetId) {
        return new Duck(assetId);
    }

    protected final String name;
    protected final String color;

    public Duck(AssetId assetId) {
        super(assetId);
        name = getDetails().name();
        color = name.substring(name.length() - 1);
    }

    public String color() {
        return color;
    }

    public String genotype() {
        return name.substring(5, 13);
    }

    public String generation() {
        return String.valueOf(name.charAt(14));
    }

    /**
     * Implements calculations from the "getGen" user function at Farming contract
     */
    public String farmGen() {
        return Arrays
                .stream(genotype().split(""))
                .sorted()
                .collect(groupingBy(c -> c, counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 0)
                .map(g -> g.getValue() + g.getKey())
                .collect(joining()) + "-" + generation();
    }

}
