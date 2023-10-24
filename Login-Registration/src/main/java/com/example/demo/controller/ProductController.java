package com.example.demo.controller;

import com.example.demo.dto.ProductDto;
import com.example.demo.dto.UserDto;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.ParseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.inicis.std.util.SignatureUtil;

import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String productList(Model model) {
        System.out.println("products 진입");
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "productList";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
            return "editProduct";
        } else {
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id, Product updateProduct) {
        productService.updateProduct(id, updateProduct);
        return "redirect:/products";
    }

    @GetMapping("/add")
    public String addProductForm() {
        return "addProduct";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product newProduct) {
        System.out.println("newProduct : "  + newProduct);
        Product product = productService.addProduct(newProduct);
        return "redirect:/products";
    }

    @GetMapping("/pay")
    public String inicisPayment(Model model) throws Exception {

        String mid = "INIpayTest";
        String signKey = "SU5JTElURV9UUklQTEVERVNfS0VZU1RS";
        String mKey = SignatureUtil.hash(signKey, "SHA-256");
        String timestamp = SignatureUtil.getTimestamp();
        String orderNumber = mid + SignatureUtil.getTimestamp();
        String price = "1000";
        String use_chkfake = "Y";

        Map<String, String> signParam = new HashMap<String, String>();
        signParam.put("oid", orderNumber);
        signParam.put("price", price);
        signParam.put("timestamp", timestamp);

        String signature = SignatureUtil.makeSignature(signParam);
        signParam.put("signKey", signKey);
        String verification = SignatureUtil.makeSignature(signParam);

        model.addAttribute("mid", mid);
        model.addAttribute("signKey", signKey);
        model.addAttribute("timestamp", timestamp);
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("price", price);
        model.addAttribute("use_chkfake", use_chkfake);
        model.addAttribute("signature", signature);
        model.addAttribute("verification", verification);
        model.addAttribute("mKey", mKey);

        System.out.println("inicisPayment 진입2");
        return "INIstdpay_pc_req";
    }

    /*@PostMapping("/return")
    public String prodReturn(Model model, @RequestParam Map<String, String> paramMap) {


        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }


        // 결과 코드 및 메시지 추출
        String resultCode = paramMap.get("resultCode");
        String resultMsg = paramMap.get("resultMsg");
        String key = paramMap.get("key");

        Map<String, String> resultData = new HashMap<>();
        resultData.put("code", "200"); // 예시 값
        resultData.put("msg", "good"); // 예시 값


        // 여기에서 결제 결과를 로깅하거나 필요한 작업을 수행합니다.
        if ("0000".equals(resultCode)) {
            System.out.println("결제정상 굳");
            // 정상적인 결제 처리
            // 추가 작업 수행...
        } else {
            System.out.println("결제실패 배드");
            // 결제 실패 또는 오류 처리
            // 추가 작업 수행...
        }

        // 모델에 결과 데이터 추가
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("resultMsg", resultMsg);

        model.addAttribute("paramMap", paramMap);
        model.addAttribute("resultData", resultData);


        // 결과를 표시할 JSP 페이지로 이동
        return "INIstdpay_pc_return";
    }*/





    @PostMapping("/return")
    public String prodReturn(HttpServletRequest request, Model model) {
        Map<String, String> resultMap = new HashMap<>();

        try {
            //#############################
            // 인증결과 파라미터 일괄 수신
            //#############################
            request.setCharacterEncoding("UTF-8");

            Map<String, String> paramMap = new Hashtable<>();

            Enumeration elems = request.getParameterNames();

            String temp;

            while (elems.hasMoreElements()) {
                temp = (String) elems.nextElement();
                paramMap.put(temp, request.getParameter(temp));
            }

            //##############################
            // 인증성공 resultCode=0000 확인
            // IDC센터 확인 [idc_name=fc,ks,stg]
            // idc_name 으로 수신 받은 값 기준 properties 에 설정된 승인URL과 authURL 이 같은지 비교
            // 승인URL은  https://manual.inicis.com 참조
            //##############################

            if ("0000".equals(paramMap.get("resultCode")) && paramMap.get("authUrl").equals(ResourceBundle.getBundle("properties/idc_name").getString(paramMap.get("idc_name")))) {

                System.out.println("####인증성공/승인요청####");

                //############################################
                // 1.전문 필드 값 설정(***가맹점 개발수정***)
                //############################################

                String mid = paramMap.get("mid");
                String timestamp = SignatureUtil.getTimestamp();
                String charset = "UTF-8";
                String format = "JSON";
                String authToken = paramMap.get("authToken");
                String authUrl = paramMap.get("authUrl");
                String netCancel = paramMap.get("netCancelUrl");
                String merchantData = paramMap.get("merchantData");

                //#####################
                // 2.signature 생성
                //#####################
                Map<String, String> signParam = new HashMap<>();

                signParam.put("authToken", authToken);        // 필수
                signParam.put("timestamp", timestamp);        // 필수

                // signature 데이터 생성 (모듈에서 자동으로 signParam을 알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
                String signature = SignatureUtil.makeSignature(signParam);

                signParam.put("signKey", "SU5JTElURV9UUklQTEVERVNfS0VZU1RS");        // 필수

                // signature 데이터 생성 (모듈에서 자동으로 signParam을 알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
                String verification = SignatureUtil.makeSignature(signParam);

                //#####################
                // 3.API 요청 전문 생성
                //#####################
                Map<String, String> authMap = new Hashtable<>();

                authMap.put("mid", mid);            // 필수
                authMap.put("authToken", authToken);    // 필수
                authMap.put("signature", signature);    // 필수
                authMap.put("verification", verification);    // 필수
                authMap.put("timestamp", timestamp);    // 필수
                authMap.put("charset", charset);        // default=UTF-8
                authMap.put("format", format);

                HttpUtil httpUtil = new HttpUtil();

                try {
                    //#####################
                    // 4.API 통신 시작
                    //#####################
                    System.out.println("4.API 통신 시작");

                    String authResultString = httpUtil.processHTTP(authMap, authUrl);

                    //############################################################
                    //6.API 통신결과 처리(***가맹점 개발수정***)
                    //############################################################

                    String test = authResultString.replace(",", "&").replace(":", "=").replace("\"", "").replace(" ", "").replace("\n", "").replace("}", "").replace("{", "");

                    resultMap = ParseUtil.parseStringToMap(test); //문자열을 MAP형식으로 파싱

                    // 수신결과를 파싱후 resultCode가 "0000"이면 승인성공 이외 실패
                    //throw new Exception("강제 망취소 요청 Exception ");

                } catch (Exception ex) {

                    //####################################
                    // 실패시 처리(***가맹점 개발수정***)
                    //####################################

                    //---- db 저장 실패시 등 예외처리----//
                    System.out.println(ex);

                    //#####################
                    // 망취소 API
                    //#####################
                    String netcancelResultString = httpUtil.processHTTP(authMap, netCancel);    // 망취소 요청 API url(고정, 임의 세팅 금지)

                    System.out.println("## 망취소 API 결과 ##");

                    // 망취소 결과 확인
                    System.out.println("<p>" + netcancelResultString.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</p>");
                }

            } else {

                resultMap.put("resultCode", paramMap.get("resultCode"));
                resultMap.put("resultMsg", paramMap.get("resultMsg"));
            }

        } catch (Exception e) {

            System.out.println(e);
        }

        model.addAttribute("resultMap", resultMap);

        System.out.println("------------------------------------------------------------------------");
        System.out.println("resultMap resultCdoe :::::::::::::::::  " + resultMap.get("resultCode"));

        return "INIstdpay_pc_return";
    }



}
