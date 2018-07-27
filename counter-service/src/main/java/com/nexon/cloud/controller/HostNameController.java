package com.nexon.cloud.controller;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.nexon.cloud.kafka.CounterMessage;
import com.nexon.cloud.kafka.CounterProcessor;
import com.nexon.cloud.model.Counter;
import com.nexon.cloud.repository.CounterRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HostNameController {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private EurekaClient eurekaClient;

    @GetMapping("/")
    public String getInstanceId() {
        ApplicationInfoManager infoManager = eurekaClient.getApplicationInfoManager();
        InstanceInfo info = infoManager.getInfo();
        return info.getAppName() + " : " + info.getHostName() + " : " + infoManager.getInfo().getPort() + "\n";
    }
}
