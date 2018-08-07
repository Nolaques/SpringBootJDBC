package com.company.controller;

import com.company.dao.BankAccountDAO;
import com.company.exception.BankTransactionException;
import com.company.form.SendMoneyForm;
import com.company.model.BankAccountInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@ComponentScan({"com.company.dao"})
@SpringBootApplication
public class MainController {

    @Autowired
    private BankAccountDAO bankAccountDAO;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showBankAccounts(Model model){
        List<BankAccountInfo> list = bankAccountDAO.getBankAccounts();
        model.addAttribute("accountInfos", list);

        return "accountsPage";
    }

    @RequestMapping(value = "/sendMoney", method = RequestMethod.GET)
    public String viewSendMoneyPage(Model model){
        SendMoneyForm form = new SendMoneyForm(1L,2L,700d);
        model.addAttribute("sendMoneyForm", form);

        return "sendMoneyPage";
    }

    @RequestMapping(value = "/sendMoney", method = RequestMethod.POST)
    public String processSendMoney(Model model, SendMoneyForm sendMoneyForm){
        System.out.println("Send money::" + sendMoneyForm.getAmount());

        try {
            bankAccountDAO.sendMoney(sendMoneyForm.getFromAccountId(),
                        sendMoneyForm.getToAccountId(),
                        sendMoneyForm.getAmount());

        }catch (BankTransactionException e){
            model.addAttribute("errorMessage", "Error: "+e.getMessage());
            return "/sendMoneyPage";
        }
        return "redirect:/";
    }

    public static void main(String[] args) {
        SpringApplication.run(MainController.class, args);
    }
}
