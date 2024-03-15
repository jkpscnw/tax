package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

@Controller
@RequestMapping("/javaHttpsExample")
public class JavaHttpsExample {

//    상세한 분석을 위한 JAVA 버전 디버그 코드를 전달해 드립니다.
//    현재 오류가 발생하는 서버에서 아래의 코드를 실행 후, 결과를 txt 파일로 저장하여 메일로 회신 부탁드리겠습니다.
//    코드상의 httpsURL을 아래의 URL로 변경해서 모두 실행해 주십시오.
//    https://auth.linkhub.co.kr/Time
//    https://popbill.linkhub.co.kr/Time
//    https://popbill-test.linkhub.co.kr/Time
//    파일명 : JavaHttpsExample.java

        @GetMapping
        public static void main(String[] args) throws Exception {

            System.out.println("test ===");
//            String httpsURL = "https://static-auth.linkhub.co.kr/Time";
//            String httpsURL = "https://popbill.linkhub.co.kr/Time";
            String httpsURL = "https://popbill-test.linkhub.co.kr/Time";

            System.out.println("httpsURL = " + httpsURL);

            URL myUrl = new URL(httpsURL);
            HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                System.out.println(inputLine);
            }
            br.close();
        }

//
//    다음은 커맨드 명령입니다.
//    $ javac JavaHttpsExample.java
//    $ java -Djavax.net.debug=ssl JavaHttpsExample > result.txt
//
//// jdk 1.9 이상
//    $ javac JavaHttpsExample.java
//    $ java -Djavax.net.debug=all JavaHttpsExample > result.txt 2>&1


}
