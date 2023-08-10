// package com.example.client2.config;

// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import com.netflix.appinfo.ApplicationInfoManager;

// import jakarta.annotation.PostConstruct;

// @Component
// public class EurekaMetadataConfig {

// @Autowired
// private ApplicationInfoManager aim;

// @PostConstruct
// public void init() {
// Map<String, String> map = aim.getInfo().getMetadata();
// map.put("version", "1");
// // map.put("version", "2");
// }

// }
