package com.sist.web.service;
import java.util.*;

import org.springframework.stereotype.Service;

import com.sist.web.vo.*;

import lombok.RequiredArgsConstructor;

import com.sist.web.mapper.*;
@Service
@RequiredArgsConstructor
public class FoodService {
   private final FoodMapper fMapper;
   public List<FoodVO> foodListData(Map map)
   {
	   List<FoodVO> list=fMapper.foodListData(map);
	   for(FoodVO vo:list)
	   {
		   String[] temp=vo.getTheme().split(",");
		   vo.setTheme(temp[0]+","+temp[1]);
	   }
	   return list;
   }
   public int foodTotalPage(String search)
   {
	   return fMapper.foodTotalPage(search);
   }
   
}