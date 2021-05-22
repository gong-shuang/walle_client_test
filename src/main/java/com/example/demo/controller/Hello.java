package com.example.demo.controller;

import com.mpush.api.Client;
import com.mpush.api.ClientListener;
import com.mpush.client.ClientConfig;
import com.mpush.util.DefaultLogger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
public class Hello {

    public static int index=0;

    //  http://127.0.0.1:8088/pushTest?count=10
    @GetMapping("/pushTest")
    public String init(@RequestParam String count){

        String[] sss= {"10","500"};
        if(count!=null && count.length() > 0){
            sss[0]=count;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    push(sss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        int lenght = Integer.parseInt(sss[0]);
        return "create " + sss[0] + " user, from user-"+ index +" to user-"+(index + lenght);
    }

    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
//    private static final String allocServer = "http://81.69.253.141:9999/";
    private static final String allocServer = "http://10.12.99.9:9999/";
//    private static final String allocServer = "http://124.196.13.44:9999/";
//    private static final String allocServer = "http://49.232.157.15:9999/";

    public static void push(String[] args) throws Exception {
        int count = 10;
        String serverHost = "127.0.0.1";
        int sleep = 1000;  //毫秒

        if (args != null && args.length > 0) {
            count = Integer.parseInt(args[0]);
            if (args.length > 1) {
                sleep = Integer.parseInt(args[1]);
            }
        }

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        ClientListener listener = new L(scheduledExecutor);
        Client client = null;
        String cacheDir = Hello.class.getResource("/").getFile();
        for (int i = index; i < count + index; i++) {
            client = ClientConfig
                    .build()
                    .setPublicKey(publicKey)
                    .setAllotServer(allocServer)
//                    .setServerHost(serverHost)
                    .setServerPort(3000)
                    .setDeviceId("deviceId-test" + i)
                    .setOsName("android")
                    .setOsVersion("6.0")
                    .setClientVersion("2.0")
                    .setUserId("user-" + i)
                    .setTags("tag-" + i)
                    .setSessionStorageDir(cacheDir + i)
                    .setLogger(new DefaultLogger())
                    .setLogEnabled(true)
                    .setEnableHttpProxy(true)
                    .setClientListener(listener)
                    .create();
            client.start();
            Thread.sleep(sleep);
        }

        index = count + index;
    }

    public static class L implements ClientListener {
        private final ScheduledExecutorService scheduledExecutor;
        boolean flag = true;

        public L(ScheduledExecutorService scheduledExecutor) {
            this.scheduledExecutor = scheduledExecutor;
        }

        @Override
        public void onConnected(Client client) {
            flag = true;
        }

        @Override
        public void onDisConnected(Client client) {
            flag = false;
        }

        @Override
        public void onHandshakeOk(final Client client, final int heartbeat) {
            scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    client.healthCheck();
                }
            }, 10, 10, TimeUnit.SECONDS);

            //client.push(PushContext.build("test"));

        }

        @Override
        public void onReceivePush(Client client, byte[] content, int messageId) {
            if (messageId > 0) client.ack(messageId);
        }

        @Override
        public void onKickUser(String deviceId, String userId) {

        }

        @Override
        public void onBind(boolean success, String userId) {

        }

        @Override
        public void onUnbind(boolean success, String userId) {

        }
    }



}
