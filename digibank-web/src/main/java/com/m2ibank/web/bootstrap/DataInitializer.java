package com.m2ibank.web.bootstrap;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerService customerService;
    private final AccountService accountService;

    public DataInitializer(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        // Init data
        // initData();
    }

    protected void initData() {
        if (customerService.getAllCustomers().isEmpty()) {
            CustomerRequest customer1 = new CustomerRequest();
            customer1.setFullName("Alice Ndzi");
            customer1.setEmail("alice@m2ibank.com");
            customer1.setPhoneNumber("+237600000001");
            customer1.setNationalId("CNI000001");

            CustomerRequest customer2 = new CustomerRequest();
            customer2.setFullName("Brian Tchoumi");
            customer2.setEmail("brian@m2ibank.com");
            customer2.setPhoneNumber("+237600000002");
            customer2.setNationalId("CNI000002");

            CustomerResponse savedCustomer1 = customerService.createCustomer(customer1);
            CustomerResponse savedCustomer2 = customerService.createCustomer(customer2);

            AccountRequest account1 = new AccountRequest();
            account1.setCustomerId(savedCustomer1.getId());
            account1.setAccountType(AccountType.CURRENT);
            account1.setInitialBalance(new BigDecimal("150000.00"));

            AccountRequest account2 = new AccountRequest();
            account2.setCustomerId(savedCustomer2.getId());
            account2.setAccountType(AccountType.SAVINGS);
            account2.setInitialBalance(new BigDecimal("90000.00"));

            accountService.createAccount(account1);
            accountService.createAccount(account2);
        }
    }
}
