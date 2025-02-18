package org.ttp.ttpspring.Liar.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CategoryContent {


    private List<String> food = new ArrayList<>(Arrays.asList(
            "햄버거", "피자", "치킨", "짬뽕", "라자냐", "라면"
    ));

    private List<String> job = new ArrayList<>(Arrays.asList(
            "경찰", "의사", "판사", "사육사", "말발굽관리사", "헤어디자이너"
    ));





}
