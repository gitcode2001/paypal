package com.example.paypal.controller;

import com.example.paypal.service.IPayService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {
    @Autowired
    private IPayService payService;

    @GetMapping("/")
    public String showPaymentForm(){
        return "payment-form";
    }

    private static final String SUCCESS_URL="http://localhost:8080/success";
    private static final String CANCEL_URL="http://localhost:8080/cancel";

    @PostMapping("/pay")
    public String processPayment(
            @RequestParam("amount") Double amount
    ){
        try{
            Payment payment =payService.createPaymentWithPayPal(
                    amount,
                    "USD",
                    "paypal",
                    "sale",
                    "Mô Tả",
                    CANCEL_URL,
                    SUCCESS_URL
            );
            for(Links link : payment.getLinks()){
                if(link.getRel().equals("approval_url")){
                    return "redirect:"+link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
    @GetMapping("/success")
    public String success(@RequestParam("paymentId")String paymentId, @RequestParam("PayerID") String payerId, Model model){
        try{
            Payment payment = payService.executePayment(paymentId, payerId);
            model.addAttribute("payment", payment);
            return "payment-success";
        }catch (PayPalRESTException e){
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/cancel")
    public String cancel(){
        return "payment-cancel";
    }
}
