package com.countingstars.idempotency.controller;

import com.countingstars.idempotency.annotation.Idempotent;
import com.countingstars.idempotency.dto.BaseRespDTO;
import com.countingstars.idempotency.dto.TestReqDTO;
import com.countingstars.idempotency.dto.TestRespDTO;
import com.countingstars.idempotency.service.IdempotentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/idempotent")
public class IdempotencyController {

    @Autowired
    private IdempotentService idempotencyService;

    @RequestMapping(value = "/genToken", method = RequestMethod.GET)
    public String genToken(HttpServletRequest request) throws Exception {
        return idempotencyService.genToken(request);
    }

    @Idempotent
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public BaseRespDTO test(@RequestBody TestReqDTO reqDTO) throws InterruptedException {
//        Thread.sleep(20000L);
        return new TestRespDTO();
    }

    @Idempotent
    @RequestMapping(value = "/teste", method = RequestMethod.POST)
    public BaseRespDTO teste(@RequestBody TestReqDTO reqDTO) {
        throw new RuntimeException("this is an exception.");
    }

}
