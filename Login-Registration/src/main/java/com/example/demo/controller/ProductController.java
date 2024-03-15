package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.ParseUtil;
import com.inicis.std.util.SignatureUtil;
import com.popbill.api.*;
import com.popbill.api.taxinvoice.*;
import jakarta.servlet.http.HttpServletRequest;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;


import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.IssueResponse;
import com.popbill.api.PopbillException;
import com.popbill.api.TaxinvoiceService;
import com.popbill.api.taxinvoice.Taxinvoice;
import com.popbill.api.taxinvoice.TaxinvoiceDetail;


@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private Object MgtKeyType;

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



//        int cnt = epayMngService. getPrchsListCnt (param);
//        Paging paging = Paging. builder()
//                .pageSize (pagingSearchDto. getPageSize))
//                .pagingSize (10)
//                .page (pagingSearchDto. getPage ())
//                .totalRecordCount (cnt)
//                .build();
//        pagingSearchDto. setStartNum(paging. getStartNum);
//        pagingSearchDto. setEndNum(paging. getEndNum);
//        pagingSearchDto.setTotalRecordCount ((int)paging.getTotalRecordCount());
//        param. setPaging (pagingSearchDto);
//
//        List<EpayMngDto> prchsList = epayMngService. getPrchsList(param);
//        model. addAttribute("prchsList", prohslist);
//        model. addAttribute("param", param);
//        model. addAttribute("paging", paging);
//        model. addAttribute("pagingSearchDto", pagingSearchDto);



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
        String price = "1";
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

    @RequestMapping("/close")
    public String prodClose(){
        return "close";
    }

    @PostMapping("/return")
    public String prodReturn(HttpServletRequest request, Model model) {
        Map<String, String> resultMap = new HashMap<>();

        String rtnMsg;

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



                    Product newProduct = new Product();
                    newProduct.setName(resultMap.get("goodName"));
                    newProduct.setPrice(Integer.parseInt(resultMap.get("TotPrice")));
                    newProduct.setTid(resultMap.get("tid"));
                    Product product = productService.addProduct(newProduct);

                    System.out.println("add-product = " + product);


//                    newProduct.setTid(product.getTid() + "7777");
//                    System.out.println("newProduct = " + newProduct);
//                    productService.updateProduct(product.getId(), newProduct);
//                    System.out.println("업데이트 처리완료");


                } catch (Exception ex) {

                    //####################################
                    // 실패시 처리(***가맹점 개발수정***)
                    //####################################

                    //---- db 저장 실패시 등 예외처리----//
                    System.out.println("catch-db 저장 실패시 등 예외처리 " + ex);

                    //#####################
                    // 망취소 API
                    //#####################

                    System.out.println("## 망취소 API 요청전 ##");
                    String netcancelResultString = httpUtil.processHTTP(authMap, netCancel);    // 망취소 요청 API url(고정, 임의 세팅 금지)

                    System.out.println("## 망취소 API 결과 ##");

                    // 망취소 결과 확인
                    System.out.println("<p>" + netcancelResultString.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</p>");

                    System.out.println("netcancelResultString = " + netcancelResultString);
                    System.out.println("paramMapCode-2 = " + paramMap.get("resultCode"));
                    System.out.println("paramMapMsg-2 = " + paramMap.get("resultMsg"));

                }

            } else {

                rtnMsg = paramMap.get("resultMsg");
                System.out.println("rtnMsg = " + rtnMsg);
                model.addAttribute("rtnMsg", rtnMsg);

                resultMap.put("resultCode", paramMap.get("resultCode"));
                resultMap.put("resultMsg", paramMap.get("resultMsg"));
            }

        } catch (Exception e) {

            System.out.println("catch-1 " + e);
        }

        model.addAttribute("resultMap", resultMap);
//        System.out.println("------------------------------------------------------------------------");
//        System.out.println("resultMap resultCdoe :::::::::::::::::  " + resultMap.get("resultCode"));

        return "INIstdpay_pc_return";
    }

    public static class SHA512 {

        public String hash(String data_hash) {
            String salt = data_hash;
            String hex = null;

            try {
                MessageDigest msg = MessageDigest.getInstance("SHA-512");
                msg.update(salt.getBytes());
                hex = String.format("%0128x", new BigInteger(1, msg.digest()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return hex;
        }
    }

    @RequestMapping("/refund")
    public static void main(String[] args) throws Exception {

        System.out.println("refund 진입");

        SHA512 sha512 = new SHA512();
        Date date_now = new Date(System.currentTimeMillis());
        SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");

        //step1. 요청을 위한 파라미터 설정
        String key = "ItEQKi3rY7uvDS8l";
        String mid = "INIpayTest";
        String type = "refund";
        String timestamp = fourteen_format.format(date_now);
        String clientIp = "127.0.0.1";
        Map<String, Object> data1 = new HashMap<String, Object>();
        data1.put("tid", "StdpayCARDINIpayTest20231122085633372275");
        data1.put("msg", "환불");

//        String key = "2jHOlQBdude18UWl";
//        String mid = "SIRxilitol";



//        String key = "iKzlYioaXHnMMXg9";
//        String key = "YxgjvCoMhnrCza==";
//        String mid = "INIpayTest";
//        String type = "refund";
//        String timestamp = fourteen_format.format(date_now);
//        String clientIp = "127.0.0.1";
//        Map<String, Object> data1 = new HashMap<String, Object>();
//        data1.put("tid", "StdpayCARDINIpayTest20231122085633372275");
//        data1.put("msg", "환불");


//        StdpayCARDINIpayTest20231109171948308457
//                StdpayCARDINIpayTest20231114100246789900
//        StdpayCARDINIpayTest20231114102149315852
//                StdpayCARDINIpayTest20231114105054544504
//        StdpayCARDINIpayTest20231114105713666182
//                StdpayCARDINIpayTest20231114110212513428
//        StdpayCARDINIpayTest20231109171948308452
//                StdpayCARDINIpayTest20231114145117653931
//        StdpayCARDINIpayTest20231116180312688330
//                StdpayCARDINIpayTest20231116180312688330
//        StdpayCARDINIpayTest20231116180312688330
//                StdpayCARDINIpayTest20231117105221798884
//        StdpayCARDINIpayTest20231109171948307777
//                StdpayCARDINIpayTest20231109171948307777
//        StdpayCARDINIpayTest20231117163143869376
//                StdpayCARDINIpayTest20231117165230013487
//        StdpayCARDINIpayTest20231117165230013487
//                StdpayCARDINIpayTest20231120155312826529
//        StdpayCARDINIpayTest20231120192432456896
//                StdpayCARDINIpayTest20231121100148074545
//        StdpayVBNKINIpayTest20231121164126887357


        JSONObject data = new JSONObject(data1);


        // Hash Encryption
        String plainTxt = key + mid + type + timestamp + data ;

        System.out.println("plainTxt = " + plainTxt);

        plainTxt = plainTxt.replaceAll("\\\\", "");

        System.out.println("plainTxt = " + plainTxt);

        String hashData = sha512.hash(plainTxt);

        System.out.println("hashData = " + hashData);


        // reqeust URL
        String apiUrl = "https://iniapi.inicis.com/v2/pg/refund";

        JSONObject respJson = new JSONObject();
        respJson.put("mid", mid);
        respJson.put("type", type);
        respJson.put("timestamp",timestamp);
        respJson.put("clientIp",clientIp);
        respJson.put("data",data);
        respJson.put("hashData",hashData);


        //step2. key=value 로 post 요청
        try {
            URL reqUrl = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();

            if (conn != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestMethod("POST");
                conn.setDefaultUseCaches(false);
                conn.setDoOutput(true);

                if (conn.getDoOutput()) {
                    conn.getOutputStream().write(respJson.toString().getBytes("UTF-8"));
                    conn.getOutputStream().flush();
                    conn.getOutputStream().close();
                }

                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

                //step3. 요청 결과
                System.out.println(br.readLine());
                br.close();
            }

        }catch(Exception e ) {
            e.printStackTrace();
        }
    }





    @ResponseBody
    @RequestMapping("/vacctinput")
    public String handleVirtualAccountInput(@RequestParam HashMap<String, String> paramMap, HttpServletRequest request) throws Exception {

        System.out.println("vacctinput 진입");

        String REMOTE_IP = request.getRemoteAddr();
        String PG_IP = REMOTE_IP.substring(0, 10);

        System.out.println("PG_IP = " + PG_IP);

        if (PG_IP.equals("203.238.37") || PG_IP.equals("39.115.212") || PG_IP.equals("183.109.71")) {
            String file_path = "/home/was/INIpayJAVA/vacct";

            String id_merchant = paramMap.get("id_merchant");
            String no_tid = paramMap.get("no_tid");
            String no_oid = paramMap.get("no_oid");
            String no_vacct = paramMap.get("no_vacct");
            String amt_input = paramMap.get("amt_input");
            String nm_inputbank = paramMap.get("nm_inputbank");
            String nm_input = paramMap.get("nm_input");

            System.out.println("id_merchant = " + id_merchant);
            System.out.println("no_tid = " + no_tid);
            System.out.println("no_oid = " + no_oid);
            System.out.println("no_vacct = " + no_vacct);
            System.out.println("amt_input = " + amt_input);
            System.out.println("nm_inputbank = " + nm_inputbank);
            System.out.println("nm_input = " + nm_input);

            try {
                System.out.println("try");
                writeLog(file_path, id_merchant, no_tid, no_oid, no_vacct, amt_input, nm_inputbank, nm_input);
                System.out.println("write");
                return "OK";
            } catch (Exception e) {
                System.out.println("e = " + e);
                return e.getMessage();
            }
        } else {
            System.out.println("else PG_IP = " + PG_IP);
            return "Invalid IP";
        }
    }

    private void writeLog(String file_path, String id_merchant, String no_tid, String no_oid, String no_vacct,
                          String amt_input, String nm_inputbank, String nm_input) throws Exception {
        File file = new File(file_path);
        file.createNewFile();

        FileWriter file2 = new FileWriter(file_path + "/vacctinput_" + getDate() + ".log", true);

        file2.write("\n************************************************\n");
        file2.write("PageCall time : " + getTime());
        file2.write("\nID_MERCHANT : " + id_merchant);
        file2.write("\nNO_TID : " + no_tid);
        file2.write("\nNO_OID : " + no_oid);
        file2.write("\nNO_VACCT : " + no_vacct);
        file2.write("\nAMT_INPUT : " + amt_input);
        file2.write("\nNM_INPUTBANK : " + nm_inputbank);
        file2.write("\nNM_INPUT : " + nm_input);
        file2.write("\n************************************************\n");

        file2.close();
    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder times = new StringBuilder();
        times.append(Integer.toString(calendar.get(Calendar.YEAR)));
        if ((calendar.get(Calendar.MONTH) + 1) < 10) {
            times.append("0");
        }
        times.append(Integer.toString(calendar.get(Calendar.MONTH) + 1));
        if ((calendar.get(Calendar.DATE)) < 10) {
            times.append("0");
        }
        times.append(Integer.toString(calendar.get(Calendar.DATE)));

        return times.toString();
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder times = new StringBuilder();

        times.append("[");
        if ((calendar.get(Calendar.HOUR_OF_DAY)) < 10) {
            times.append("0");
        }
        times.append(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
        times.append(":");
        if ((calendar.get(Calendar.MINUTE)) < 10) {
            times.append("0");
        }
        times.append(Integer.toString(calendar.get(Calendar.MINUTE)));
        times.append(":");
        if ((calendar.get(Calendar.SECOND)) < 10) {
            times.append("0");
        }
        times.append(Integer.toString(calendar.get(Calendar.SECOND)));
        times.append("]");

        return times.toString();
    }





    @Autowired
    private TaxinvoiceService taxinvoiceService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "test.fsec";

    @RequestMapping(value = "tax", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Taxinvoice/index";
    }



    @RequestMapping(value = "registIssue", method = RequestMethod.GET)
    public String registIssue(Model m) {

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20211123");

        // 과금방향, [정과금, 역과금] 중 선택기재, "역과금"은 역발행세금계산서 발행에만 가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");


        /*********************************************************************
         *                          공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum("1234567890");

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20211123-001");

        // 공급자 대표자성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 업종");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);


        /*********************************************************************
         *                           공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호, '-' 제외 10자리
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리까지 사업자번호별 중복없는 고유번호 할당
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행시 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);


        /*********************************************************************
         *                           세금계산서 기재정보
         *********************************************************************/

        // [필수] 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // [필수] 세액 합계
        taxinvoice.setTaxTotal("10000");

        // [필수] 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 기재 상 일련번호
        taxinvoice.setSerialNum("123");

        // 기재 상 현금
        taxinvoice.setCash("");

        // 기재 상 수표
        taxinvoice.setChkBill("");

        // 기재 상 어음
        taxinvoice.setNote("");

        // 기재 상 외상미수금
        taxinvoice.setCredit("");

        // 기재 상 비고
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");
        taxinvoice.setKwon((short) 1);
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부
        taxinvoice.setBankBookYN(false);

        /*********************************************************************
         *               수정세금계산서 정보 (수정세금계산서 작성시 기재)
         * - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조
         & - [참고] 수정세금계산서 작성방법 안내 [http://blog.linkhubcorp.com/650]
         *********************************************************************/

        // [수정세금계산서 작성시 필수] 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // [수정세금계산서 작성시 필수] 원본세금계산서의 국세청승인번호 기재
        taxinvoice.setOrgNTSConfirmNum("");


        /*********************************************************************
         *                       상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211123"); // 거래일자
        detail.setItemName("품목명");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고");

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211123"); // 거래일자
        detail.setItemName("품목명2");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2");

        taxinvoice.getDetailList().add(detail);


        /*********************************************************************
         *                       추가담당자 정보
         *********************************************************************/

        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        addContact.setSerialNum(1);
        addContact.setContactName("추가 담당자명");
        addContact.setEmail("test2@test.com");

        taxinvoice.getAddContactList().add(addContact);


        // 거래명세서 동시작성여부
        Boolean WriteSpecification = false;

        // 거래명세서 문서번호
        String DealInvoiceKey = null;

        // 즉시 발행 메모
        String Memo = "즉시 발행 메모";

        // 지연발행 강제여부
        // 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {

            System.out.println("before");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(taxinvoice);
            System.out.println(json);




//            IssueResponse response = taxinvoiceService.registIssue("1234567890", taxinvoice, WriteSpecification);
            IssueResponse response = taxinvoiceService.registIssue("1234567890", taxinvoice, WriteSpecification, Memo, ForceIssue, DealInvoiceKey);


//            세금계산서 열람주소 성공케이스
//            https://test.popbill.com/Taxinvoice/024020709085000001

            System.out.println("after");

            m.addAttribute("Response", response);

        } catch (PopbillException e) {

            m.addAttribute("Response", e);

            // 예외 발생 시, e.getCode() 로 오류 코드를 확인하고, e.getMessage()로 오류 메시지를 확인합니다.
            System.out.println("오류 코드" + e.getCode());
            System.out.println("오류 메시지" + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }





//        return "response";
        return "Taxinvoice/issueResponse";
    }



    @RequestMapping(value = "checkMgtKeyInUse", method = RequestMethod.GET)
    public String checkMgtKeyInUse(Model m) {
        /**
         * 파트너가 세금계산서 관리 목적으로 할당하는 문서번호의 사용여부를 확인합니다.
         * - 이미 사용 중인 문서번호는 중복 사용이 불가하고, 세금계산서가 삭제된 경우에만 문서번호의 재사용이 가능합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/info#CheckMgtKeyInUse
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        com.popbill.api.taxinvoice.MgtKeyType keyType = com.popbill.api.taxinvoice.MgtKeyType.SELL;

        // 세금계산서 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT001";

        String isUseStr;

        try {

            boolean IsUse = taxinvoiceService.checkMgtKeyInUse(CorpNum, keyType, mgtKey);

            isUseStr = (IsUse) ? "사용중" : "미사용중";

            System.out.println("IsUse = " + IsUse);

            m.addAttribute("Response", isUseStr);

        } catch (PopbillException e) {
            m.addAttribute("Response", e);
//            m.addAttribute("Exception", e);
//            return "exception";
        }

//        return "result";
        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "checkIsMember", method = RequestMethod.GET)
    public String checkIsMember(Model m) throws PopbillException {
        /**
         * 사업자번호를 조회하여 연동회원 가입여부를 확인합니다.
         * - LinkID는 연동신청 시 팝빌에서 발급받은 링크아이디 값입니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#CheckIsMember
         */

        // 조회할 사업자번호, '-' 제외 10자리
        String corpNum = "1234567890";

        try {
            Response response = taxinvoiceService.checkIsMember(corpNum, "FSEC");

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Response", e);
//            m.addAttribute("Exception", e);
//            return "exception";
        }

//        return "response";
        return "Taxinvoice/issueResponse";
    }




}





//CREATE TABLE TAX_INVOICE_INFO (
//    TAX_INVOICE_SN INT PRIMARY KEY AUTO_INCREMENT,
//    ISSUE_TYPE VARCHAR(3) COMMENT '발행형태',
//    TAX_TYPE VARCHAR(2) COMMENT '과세형태',
//    CHARGE_DIRECTION VARCHAR(3) COMMENT '과금방향',
//    SERIAL_NUM VARCHAR(30) COMMENT '일련번호',
//    KWON INT COMMENT '책번호 "권" 항목',
//    HO INT COMMENT '책번호 "호" 항목',
//    WRITE_DATE VARCHAR(8) COMMENT '작성일자',
//    PURPOSE_TYPE VARCHAR(2) COMMENT '결제대금 수취여부',
//    SUPPLY_COST_TOTAL VARCHAR(18) COMMENT '공급가액 합계',
//    TAX_TOTAL VARCHAR(18) COMMENT '세액합계',
//    TOTAL_AMOUNT VARCHAR(18) COMMENT '합계금액',
//    CASH VARCHAR(18) COMMENT '현금',
//    CHK_BILL VARCHAR(18) COMMENT '수표',
//    CREDIT VARCHAR(18) COMMENT '외상',
//    NOTE VARCHAR(18) COMMENT '어음',
//    REMARK1 VARCHAR(150) COMMENT '비고1',
//    REMARK2 VARCHAR(150) COMMENT '비고2',
//    REMARK3 VARCHAR(150) COMMENT '비고3',
//    INVOICER_MGT_KEY VARCHAR(24) COMMENT '공급자 문서번호',
//    INVOICER_CORP_NUM VARCHAR(10) COMMENT '공급자 사업자번호',
//    INVOICER_TAX_REG_ID VARCHAR(4) COMMENT '공급자 종사업장 식별번호',
//    INVOICER_CORP_NAME VARCHAR(200) COMMENT '공급자 상호',
//    INVOICER_CEO_NAME VARCHAR(100) COMMENT '공급자 대표자 성명',
//    INVOICER_ADDR VARCHAR(300) COMMENT '공급자 주소',
//    INVOICER_BIZ_TYPE VARCHAR(100) COMMENT '공급자 업태',
//    INVOICER_BIZ_CLASS VARCHAR(100) COMMENT '공급자 종목',
//    INVOICER_CONTACT_NAME VARCHAR(100) COMMENT '공급자 담당자 섬영',
//    INVOICER_DEPT_NAME VARCHAR(100) COMMENT '공급자 담당자 부서명',
//    INVOICER_TEL VARCHAR(20) COMMENT '공급자 담당자 연락처',
//    INVOICER_HP VARCHAR(20) COMMENT '공급자 담당자 휴대폰',
//    INVOICER_EMAIL VARCHAR(100) COMMENT '공급자 담당자 이메일',
//    INVOICER_SMS_SEND_YN BOOLEAN COMMENT '공급자 알림문자 전송 여부',
//    INVOICEE_MGT_KEY VARCHAR(24) COMMENT '공급받는자 문서번호',
//    INVOICEE_TYPE VARCHAR(255) COMMENT '공급받는자 구분',
//    INVOICEE_CORP_NUM VARCHAR(13) COMMENT '공급받는자 등록번호',
//    INVOICEE_TAX_REG_ID VARCHAR(4) COMMENT '공급받는자 종사업장 식별번호',
//    INVOICEE_CORP_NAME VARCHAR(200) COMMENT '공급받는자 상호',
//    INVOICEE_CEO_NAME VARCHAR(100) COMMENT '공급받는자 대표자 성명',
//    INVOICEE_ADDR VARCHAR(300) COMMENT '공급받는자 주소',
//    INVOICEE_BIZ_TYPE VARCHAR(100) COMMENT '공급받는자 업태',
//    INVOICEE_BIZ_CLASS VARCHAR(100) COMMENT '공급받는자 종목',
//    INVOICEE_CONTACT_NAME1 VARCHAR(100) COMMENT '공급받는자 담당자 성명',
//    INVOICEE_DEPT_NAME1 VARCHAR(100) COMMENT '공급받는자 담당자 부서명',
//    INVOICEE_TEL1 VARCHAR(20) COMMENT '공급받는자 담당자 연락처',
//    INVOICEE_HP1 VARCHAR(20) COMMENT '공급받는자 담당자 휴대폰',
//    INVOICEE_EMAIL1 VARCHAR(100) COMMENT '공급받는자 담당자 이메일',
//    INVOICEE_SMS_SEND_YN BOOLEAN COMMENT '공급받는자 알림문자 전송 여부',
//    TRUSTEE_MGT_KEY VARCHAR(24) COMMENT '수탁자 문서번호',
//    TRUSTEE_CORP_NUM VARCHAR(10) COMMENT '수탁자 사업자번호',
//    TRUSTEE_TAX_REG_ID VARCHAR(4) COMMENT '수탁자 종사업장 식별번호',
//    TRUSTEE_CORP_NAME VARCHAR(200) COMMENT '수탁자 상호',
//    TRUSTEE_CEO_NAME VARCHAR(100) COMMENT '수탁자 대표자 성명',
//    TRUSTEE_ADDR VARCHAR(300) COMMENT '수탁자 주소',
//    TRUSTEE_BIZ_TYPE VARCHAR(100) COMMENT '수탁자 업태',
//    TRUSTEE_BIZ_CLASS VARCHAR(100) COMMENT '수탁자 종목',
//    TRUSTEE_CONTACT_NAME VARCHAR(100) COMMENT '수탁자 담당자 성명',
//    TRUSTEE_DEPT_NAME VARCHAR(100) COMMENT '수탁자 담당자 부서명',
//    TRUSTEE_TEL VARCHAR(20) COMMENT '수탁자 담당자 연락처',
//    TRUSTEE_HP VARCHAR(20) COMMENT '수탁자 담당자 휴대폰',
//    TRUSTEE_EMAIL VARCHAR(100) COMMENT '수탁자 담당자 이메일',
//    TRUSTEE_SMS_SEND_YN BOOLEAN COMMENT '수탁자 알림문자 전송 여부',
//    MODIFY_CODE INT COMMENT '수정 사유코드',
//    ORG_NTS_CONFIRM_NUM VARCHAR(24) COMMENT '원본 세금계산서 국세청 승인번호',
//    BUSINESS_LICENSE_YN BOOLEAN COMMENT '팝빌에 등록된 사업자등록증 첨부 여부',
//    BANK_BOOK_YN BOOLEAN COMMENT '팝빌에 등록된 통장사본 첨부 여부',
//    DEAL_INVOICE_KEY VARCHAR(24) COMMENT '거래명세서 문서번호'
//    CODE INT COMMENT '응답코드',
//    MESSAGE VARCHAR(300) COMMENT '응답메시지',
//    NTS_CONFIRM_NUM VARCHAR(24) COMMENT '국세청 승인번호'
//);


