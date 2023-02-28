package com.stock.mx2.service.impl;

 import com.github.pagehelper.PageHelper;

 import com.github.pagehelper.PageInfo;

 import com.google.common.collect.Lists;

 import com.stock.mx2.common.ServerResponse;

 import com.stock.mx2.dao.StockMapper;

 import com.stock.mx2.dao.StockOptionMapper;

 import com.stock.mx2.pojo.Stock;

 import com.stock.mx2.pojo.StockOption;

 import com.stock.mx2.pojo.User;

 import com.stock.mx2.service.IStockOptionService;

 import com.stock.mx2.service.IUserService;

 import com.stock.mx2.utils.stock.sina.SinaStockApi;

 import com.stock.mx2.vo.stock.StockListVO;
 import com.stock.mx2.vo.stock.StockOptionListVO;

 import com.stock.mx2.vo.stock.StockVO;

 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;

 import org.slf4j.Logger;

 import org.slf4j.LoggerFactory;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.stereotype.Service;


 @Service("iStockOptionService")

 public class StockOptionServiceImpl implements IStockOptionService {

   private static final Logger log = LoggerFactory.getLogger(StockOptionServiceImpl.class);

   @Autowired
   StockOptionMapper stockOptionMapper;

   @Autowired
   IUserService iUserService;

   @Autowired
   StockMapper stockMapper;

   public ServerResponse<PageInfo> findMyStockOptions(String keyWords, HttpServletRequest request, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);
     User user = this.iUserService.getCurrentUser(request);

       if (user == null ){
           return ServerResponse.createBySuccessMsg("請先登錄");
       }
     List<StockOption> stockOptions = this.stockOptionMapper.findMyOptionByKeywords(user.getId(), keyWords);

     List<StockOptionListVO> stockOptionListVOS = Lists.newArrayList();
     for (StockOption option : stockOptions) {
       StockOptionListVO stockOptionListVO = assembleStockOptionListVO(option);
       stockOptionListVO.setIsOption("1");
       stockOptionListVOS.add(stockOptionListVO);
     }
     PageInfo pageInfo = new PageInfo(stockOptionListVOS);

     pageInfo.setList(stockOptionListVOS);

     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse isOption(Integer uid, String code) {

     StockOption stockOption = this.stockOptionMapper.isOption(uid, code);

     if (stockOption == null) {
         Map<String,Integer> map = new HashMap<>();
         map.put("status",0);

       return ServerResponse.createBySuccess("未添加",map);

     }
       stockOption.setStatus(1);
     return ServerResponse.createByError("已添加",stockOption);

   }

     public String isMyOption(Integer uid, String code) {
         StockOption stockOption = this.stockOptionMapper.isOption(uid, code);
         if (stockOption == null) {
             return "0";
         }
         return "1";

     }


     private StockOptionListVO assembleStockOptionListVO(StockOption option) {

         StockOptionListVO stockOptionListVO = new StockOptionListVO();



         stockOptionListVO.setId(option.getId().intValue());

         stockOptionListVO.setStockName(option.getStockName());

         stockOptionListVO.setStockCode(option.getStockCode());

         stockOptionListVO.setStockGid(option.getStockGid());

       ArrayList<String> arrayList = new ArrayList<>();
       arrayList.add(option.getStockCode());
       StockVO stockVO = new StockVO();
         if(option.getStockGid().contains("hf")){
         } else {
             stockVO = SinaStockApi.assembleStockVO(SinaStockApi.getSinaStock(arrayList),option.getStockCode());
         }
       stockOptionListVO.setVolume(stockVO.getBuy1());
         stockOptionListVO.setNowPrice(stockVO.getNowPrice());
         stockOptionListVO.setHcrates(stockVO.getBuy2());
         stockOptionListVO.setHcrate(stockVO.getHcrate()!=null?stockVO.getHcrate().toString(): BigDecimal.ZERO.toString());
         stockOptionListVO.setPreclose_px(stockVO.getPreclose_px());
         stockOptionListVO.setOpen_px(stockVO.getOpen_px());

       List<Stock> stockByCode = this.stockMapper.findStockByCode(option.getStockCode());
       Stock stock =  new Stock();
       if (stockByCode.size()>0){
           stock = stockByCode.get(0);
       }
       stockOptionListVO.setStock_plate(stock.getStockPlate());
         stockOptionListVO.setStock_type(stock.getStockType());
       stockOptionListVO.setStock_type_str(stock.getStockTypeStr());

       return stockOptionListVO;

     }

//     @PostConstruct
     public void ty(){
         List<Stock> stockList = stockMapper.findStockList();
         System.out.println(stockList.size());
         int i = 0;
         for (Stock stock : stockList) {
             i++;
             System.out.println(i);
             List<String> arrayList = new ArrayList<>();
             arrayList.add(stock.getStockCode());
             String sinaStock = SinaStockApi.getSinaStock(arrayList);
             List<StockListVO> stockListVOS = SinaStockApi.assembleStockListVO(sinaStock, arrayList);
             if (stockListVOS.size()==0){
                 System.out.println(stock.getStockCode()+"数据为空,");
             }
         }
     }
 }
