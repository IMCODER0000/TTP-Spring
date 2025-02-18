package org.ttp.ttpspring.Liar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiarGameTestController {



    @GetMapping("/api/liar/test")
    public String hello() {
        return "hello from liar game";
    }



}
