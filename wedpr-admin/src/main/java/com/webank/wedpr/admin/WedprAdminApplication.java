package com.webank.wedpr.admin;

import com.webank.wedpr.components.initializer.WeDPRApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.webank"})
public class WedprAdminApplication {

    public static void main(String[] args) throws Exception {
        long startT = System.currentTimeMillis();
        WeDPRApplication.main(args, "WeDPR-ADMIN");
        System.out.println(
                "WeDPR-ADMIN: start WedprAdminApplication success, timecost: "
                        + (System.currentTimeMillis() - startT)
                        + " ms.");
        System.out.println("Swagger URL(Dev Mode): http://localhost:6850/swagger-ui/index.html");
    }
}
