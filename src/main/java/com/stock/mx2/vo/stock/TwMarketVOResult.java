package com.stock.mx2.vo.stock;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TwMarketVOResult {

    List<TwMarketVO> TwMarket;


//    public String toString() {
//        return "TwMarketVOResult(TwMarket=" + getTwMarket() + ")";
//    }
//
//    public int hashCode() {
//        int PRIME = 59;
//        int result = 1;
//        Object $TwMarket = getTwMarket();
//        return result * 59 + (($TwMarket == null) ? 43 : $TwMarket.hashCode());
//    }
//
//    protected boolean canEqual(Object other) {
//        return other instanceof TwMarketVOResult;
//    }
//
//    public boolean equals(Object o) {
//        if (o == this) return true;
//        if (!(o instanceof TwMarketVOResult)) return false;
//        TwMarketVOResult other = (TwMarketVOResult) o;
//        if (!other.canEqual(this)) return false;
//        Object this$TwMarket = getTwMarket(), other$TwMarket = other.getTwMarket();
//        return !((this$TwMarket == null) ? (other$TwMarket != null) : !this$TwMarket.equals(other$TwMarket));
//    }
//
//    public void setTwMarket(List<TwMarketVO> TwMarket) {
//        this.TwMarket = TwMarket;
//    }
//
//
//    public List<TwMarketVO> getTwMarket() {
//        return this.TwMarket;
//    }

}

