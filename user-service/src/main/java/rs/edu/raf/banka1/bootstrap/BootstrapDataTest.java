package rs.edu.raf.banka1.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.tinylog.Logger;
import rs.edu.raf.banka1.model.*;
import rs.edu.raf.banka1.repositories.*;
import rs.edu.raf.banka1.services.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@Profile("test")
public class BootstrapDataTest implements CommandLineRunner {
    private final EmployeeRepository employeeRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankAccountService bankAccountService;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CurrencyRepository currencyRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final LoanRepository loanRepository;
    private final CardRepository cardRepository;
    private final CapitalRepository capitalRepository;
    private final CapitalService capitalService;
    private final MarketService marketService;
    private final TransferService transferService;
    private final EmployeeService employeeService;
    private final OrderRepository orderRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BootstrapDataTest(
            final EmployeeRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final PermissionRepository permissionRepository,
            final CurrencyRepository currencyRepository,
            final CompanyRepository companyRepository,
            final BankAccountService bankAccountService,
            final CustomerRepository customerRepository,
            final LoanRequestRepository loanRequestRepository,
            final LoanRepository loanRepository,
            final CardRepository cardRepository,
            final MarketService marketService,
            final CapitalService capitalService,
            final CapitalRepository capitalRepository,
            final EmployeeService employeeService,
            final OrderRepository orderRepository,
            final TransferService transferService,
            final BankAccountRepository bankAccountRepository
    ) {

        this.employeeRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.customerRepository = customerRepository;
        this.bankAccountService = bankAccountService;
        this.companyRepository = companyRepository;
        this.currencyRepository = currencyRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.loanRepository = loanRepository;
        this.cardRepository = cardRepository;
        this.marketService = marketService;
        this.capitalService = capitalService;
        this.capitalRepository = capitalRepository;
        this.employeeService = employeeService;
        this.orderRepository = orderRepository;
        this.transferService = transferService;
        this.bankAccountRepository = bankAccountRepository;
    }

    private final ScheduledExecutorService resetLimitExecutor = Executors.newScheduledThreadPool(1);
    @Override
    public void run(String... args) throws Exception {

        System.out.println("BootstrapDataTest loading data...");
        List<String> permissionNames = List.of(
                "can_manage_users",
                "readUser",
                "addUser",
                "modifyUser",
                "deleteUser",
                "manageLoans",
                "manageLoanRequests",
                "modifyCustomer",
                "manageOrderRequests"
        );
        long permissionId = 2L;
        List<Permission> permissions = new ArrayList<>();
        for(String permissionName : permissionNames) {
            permissions.add(createPermission(permissionId, permissionName, permissionName));
            permissionId++;
        }
        permissionRepository.saveAll(permissions);

        Company company1 = createCompany(1, "Banka1", "test", "test", "test", "test", "test", "test");
        Company company2 = createCompany(2, "Banka1_test2", "test2", "test2", "test2", "test2", "test2", "test2");
        companyRepository.save(company1);
        companyRepository.save(company2);

        Employee employee1 = createEmployee(
                100,
                true,
                "admin@admin.com",
                "admin", "admin", "admin", "admin",
                "123456789",
                "admin"
        );

        employee1.setPermissions(new HashSet<>(permissions));
        employeeRepository.save(employee1);

        Customer customer1 = createCustomer(
                101,
                true,
                "user@test.com",
                "petar",
                "412325124",
                "petrovic",
                "admin",
                "1111111111"
        );

        Customer customer2 = createCustomer(
                102,
                true,
                "user123@test.com",
                "mika",
                "215412512",
                "mikic",
                "admin",
                "22222324"
        );

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Customer customer3 = createCustomer(
                103,
                true,
                "pravno_lice@test.com",
                "petar1",
                "712325127",
                "petrovic1",
                "admin",
                "33333333"
        );

        Customer customer4 = createCustomer(
                104,
                true,
                "pravno_lice_buyer@test.com",
                "miroslav",
                "65656565",
                "lazanski",
                "admin",
                "63333663"
        );

        customer3.setCompany(company1);
        customer4.setCompany(company2);

        customerRepository.save(customer3);
        customerRepository.save(customer4);

        Currency currency1 = createCurrency (10000, true, "TEST", "TST", "TEST", "TEST", "TST", 1.0, 1.0);
        Currency currency2 = createCurrency (10001, true, "TEST1", "TSS", "TEST1", "TEST1", "TST1", 1.0, 1.0);
        Currency currency3 = createCurrency (10003, true, "RSD", "RSD", "RSD", "RSD", "RSD", 1.0, 1.0);

        currencyRepository.save(currency1);
        currencyRepository.save(currency2);
        currencyRepository.save(currency3);

        BankAccount bankAccount1 = createBankAccount(true, AccountType.CURRENT, 10000.0,12000.0, 100.0, null, 1L, 1710959528, currency1, customer1, 2710959528L, 100, "test", "1234567890", "subtest");
        BankAccount bankAccount2 = createBankAccount(true, AccountType.CURRENT,  10000.0,12000.0, 100.0, null, 1L, 1710959558, currency2, customer1, 2710959528L, 101,"test1", "0987654321", "subtest");
        BankAccount bankAccount3 = createBankAccount(true, AccountType.CURRENT, 12000.0,14000.0, 100.0, company1, 1L, 1710959558, currency2, null, 2710959528L, 102, "test1", "7151517151", "subtest");
        BankAccount bankAccount4 = createBankAccount(true, AccountType.CURRENT,12000.0, 14000.0, 100.0, company2, 1L, 1710959558, currency2, null, 2710959528L, 103, "test1", "1515151717", "subtest");

        BankAccount bankAccount5 = createBankAccount(false, AccountType.BUSINESS,1000000.0, 1000000.0, null, company1, null, 1714003200, currency1, null, 1871769600L, 9, "Banks account", "131242095807818250", null);
        BankAccount bankAccount6 = createBankAccount(false, AccountType.BUSINESS,1000000.0, 1000000.0, null, company1, null, 1714003200, currency2, null, 1871769600L, 9, "Banks account", "131242095807818251", null);
        BankAccount bankAccount7 = createBankAccount(false, AccountType.BUSINESS,1000000.0, 1000000.0, null, company1, null, 1714003200, currency3, null, 1871769600L, 9, "Banks account", "131242095807818251", null);

        bankAccountRepository.saveAll(List.of(bankAccount1, bankAccount2, bankAccount3, bankAccount4, bankAccount5, bankAccount6, bankAccount7));

        Loan loan = new Loan();
        loan.setAccountNumber("1234567890");
        loan.setCurrency("TST");
        loan.setEffectiveInterestRate(0.0);
        loan.setInstallmentAmount(0.0);
        loan.setLoanAmount(0.0);
        loan.setLoanType(LoanType.PERSONAL);
        loan.setNominalInterestRate(0.0);
        loan.setRemainingDebt(0.0);
        loan.setRepaymentPeriod(0);
        loan.setAgreementDate(0L);
//        loan.setId(100);
        loan.setMaturityDate(0L);
        loan.setNextInstallmentDate(0L);
        loanRepository.save(loan);

        //todo: promeni listing_id
        Capital capital1 = createCapital(ListingType.STOCK, 0.0, 30.0, bankAccount1, 1001L, 100003L, "testticker", 0.0);
        Capital capital2 = createCapital(ListingType.FOREX, 0.0, 36.0, bankAccount1, 1002L, 100001L, "testticker", 5.0);
        Capital capital3 = createCapital(ListingType.FUTURE, 0.0, 35.0, bankAccount3, 1003L, 100002L, "testticker", 3.0);
        Capital capital4 = createCapital(ListingType.FOREX, 0.0, 35.0, bankAccount3, 1004L, 100001L, "testticker", 0.0);

        capitalRepository.saveAll(List.of(capital1, capital2, capital3, capital4));

        String[] command = {"/bin/bash", "-c", "echo 'finished' > /tmp/finished.txt"};
        Process process = Runtime.getRuntime().exec(command);
        int res = process.waitFor();
        Logger.info(res);
    }

    private Capital createCapital(ListingType listing_type, double reserved,  double total, BankAccount bankAccount, Long id, Long listing_id, String ticker, double public_total) {
        Capital capital = new Capital();
        capital.setListingType(listing_type);
        capital.setReserved(reserved);
        capital.setTotal(total);
        capital.setBankAccount(bankAccount);
//        capital.setId(id);
        capital.setListingId
                (listing_id);
        capital.setTicker(ticker);
        capital.setPublicTotal(public_total);
        return capital;
    }

    private BankAccount createBankAccount(boolean account_status, AccountType account_type, double available_balance, double balance,  Double maintenance_cost, Company company, Long created_by_agent_id, long creation_date, Currency currency, Customer customer, long expiration_date, long id, String account_name, String account_number, String subtype_of_account) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountStatus(account_status);
        bankAccount.setAccountType(account_type);
        bankAccount.setBalance(balance);
        bankAccount.setAvailableBalance(available_balance);
        bankAccount.setMaintenanceCost(maintenance_cost);
        bankAccount.setCompany(company);
        bankAccount.setCreatedByAgentId(created_by_agent_id);
        bankAccount.setCreationDate(creation_date);
        bankAccount.setCurrency(currency);
        bankAccount.setCustomer(customer);
        bankAccount.setExpirationDate(expiration_date);
//        bankAccount.setId(id);
        bankAccount.setAccountName(account_name);
        bankAccount.setAccountNumber(account_number);
        bankAccount.setSubtypeOfAccount(subtype_of_account);
        return bankAccount;
    }

    private Currency createCurrency(long id, boolean active, String country, String curCode, String curDesc, String curName, String curSymbol, double toRSD, double fromRSD) {
        Currency currency = new Currency();
//        currency.setId(id);
        currency.setActive(active);
        currency.setCountry(country);
        currency.setCurrencyCode(curCode);
        currency.setCurrencyDesc(curDesc);
        currency.setCurrencyName(curName);
        currency.setCurrencySymbol(curSymbol);
        currency.setToRSD(toRSD);
        currency.setFromRSD(fromRSD);
        return currency;
    }

    private Company createCompany(long id, String companyName, String faxNumber, String idNumber, String jobId, String pib, String regNumber, String telNumber) {
        Company company = new Company();
//        company.setId(id);
        company.setCompanyName(companyName);
        company.setFaxNumber(faxNumber);
        company.setIdNumber(idNumber);
        company.setJobId(jobId);
        company.setPib(pib);
        company.setRegistrationNumber(regNumber);
        company.setTelephoneNumber(telNumber);
        return company;
    }

    private Customer createCustomer(long id, boolean active, String email, String firstName, String jmbg, String lastName, String password, String phoneNumber) {
        Customer customer = new Customer();
//        customer.setUserId(id);
        customer.setActive(active);
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setJmbg(jmbg);
        customer.setLastName(lastName);
        customer.setPassword(password);
        customer.setPhoneNumber(phoneNumber);
        return customer;
    }

    private Employee createEmployee(long id, boolean active, String email, String firstName, String jmbg, String lastName, String password, String phoneNumber, String position) {
        Employee employee = new Employee();
//        employee.setUserId(id);
        employee.setActive(active);
        employee.setEmail(email);
        employee.setFirstName(firstName);
        employee.setJmbg(jmbg);
        employee.setLastName(lastName);
        employee.setPassword(password);
        employee.setPhoneNumber(phoneNumber);
        employee.setPosition(position);
        return employee;
    }

    private Permission createPermission(long id, String description, String name) {
        Permission p = new Permission();
//        p.setPermissionId(id);
        p.setDescription(description);
        p.setName(name);

        return p;
    }
}