package org.ttp.ttpspring.Liar.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Setting {

    private List<String> category = new ArrayList<>(Arrays.asList(
            "랜덤", "나라", "동물", "무기", "탈것", "음식", "장소", "직업", "가수"
    ));




}
