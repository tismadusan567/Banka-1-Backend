package rs.edu.raf.banka1.bootstrap;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import rs.edu.raf.banka1.model.*;
import rs.edu.raf.banka1.model.entities.*;
import rs.edu.raf.banka1.repositories.*;
import org.tinylog.Logger;

import java.util.Date;
import java.util.List;


@Component
@Profile("test")
public class BootstrapDataTest implements CommandLineRunner {
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final InflationRepository inflationRepository;
    private final ForexRepository forexRepository;
    private final FutureRepository futureRepository;
    private final StockRepository stockRepository;
    private final OptionsRepository optionsRepository;
    private final ListingHistoryRepository listingHistoryRepository;
    private final ExchangeRepository exchangeRepository;
    private final HolidayRepository holidayRepository;

    @Autowired
    public BootstrapDataTest(

            CountryRepository countryRepository, CurrencyRepository currencyRepository, InflationRepository inflationRepository, ForexRepository forexRepository, FutureRepository futureRepository, StockRepository stockRepository, OptionsRepository optionsRepository, ListingHistoryRepository listingHistoryRepository, ExchangeRepository exchangeRepository, HolidayRepository holidayRepository) {

        this.countryRepository = countryRepository;
        this.currencyRepository = currencyRepository;
        this.inflationRepository = inflationRepository;
        this.forexRepository = forexRepository;
        this.futureRepository = futureRepository;
        this.stockRepository = stockRepository;
        this.optionsRepository = optionsRepository;
        this.listingHistoryRepository = listingHistoryRepository;
        this.exchangeRepository = exchangeRepository;
        this.holidayRepository = holidayRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Logger.info("BootstrapDataTest");

        Country country = new Country();
//        country.setId(100000);
        country.setISOCode("TEST");
        country.setCloseTime(new Date(1685379600000L));
        country.setOpenTime(new Date(1685350800000L));
        country.setTimezoneOffset(0);
        countryRepository.save(country);

        Logger.info("curr1");
        //100000
        Currency currency1 = createCurrency("CD1", "test1", "testsym1", "testpolity1");

        Logger.info("curr2");
        //100001
        Currency currency2 = createCurrency("CD2", "test2", "testsym2", "testpolity2");

        currencyRepository.saveAll(List.of(currency1, currency2));

        //100000
        Inflation inflation = new Inflation();
        inflation.setInflationRate(3.57f);
        inflation.setYear(2024);
        inflation.setCurrency(currency1);
        inflationRepository.save(inflation);

        //100001
        ListingForex forex = new ListingForex();
        setListingAttributes(forex, "oanda", 0.85381, 1710929683, "testforex", 0.85376, "Oanda CD1/CD2", 0.8538, 0.8538, "CD1/CD2", 0);
        forex.setBaseCurrency("CD1");
        forex.setQuoteCurrency("CD2");
        forexRepository.save(forex);

        //100002
        ListingFuture future = new ListingFuture();
        setListingAttributes(future, "test_exchange", 46.895, 1710929671, "Future", 45.1708, "testfuture", 46.49, 0.97, "testticker", 3839039);
        future.setContractSize(100);
        future.setContractUnit("testunit");
        future.setOpenInterest(0);
        future.setSettlementDate(1713484800);
        futureRepository.save(future);

        //100003
        ListingStock stock1 = new ListingStock();
        setListingAttributes(stock1, "test_exchange", 46.895, 1710929671, "Stock", 45.1708, "teststock", 46.49, 0.97, "testticker", 3839039);
        stock1.setDividendYield(0.0);
        stock1.setOutstandingShares(295999000);
        stockRepository.save(stock1);

        //100000
        OptionsModel options = new OptionsModel();
        options.setCurrency("CD1");
        options.setExpirationDate(1713484800L);
        options.setImpliedVolatility(0.000010000000000000003);
        options.setOpenInterest(0);
        options.setOptionType("CALL");
        options.setStrikePrice(35.0);
        options.setTicker("testticker");
        optionsRepository.save(options);

        //100000
        ListingHistory history = new ListingHistory();
        history.setChanged(1.240000000000002);
        history.setDate(1710806400L);
        history.setHigh(46.895);
        history.setLow(45.1708);
        history.setPrice(46.49);
        history.setTicker("testticker");
        history.setVolume(3839039);
        listingHistoryRepository.save(history);

        //100000
        Exchange exchange = new Exchange();
        exchange.setCurrency("CD1");
        exchange.setExchangeAcronym("test_acronym");
        exchange.setExchangeName("test_exchange");
        exchange.setMicCode("test_code");
        exchange.setCountry(country);
        exchangeRepository.save(exchange);

        //100000
        ListingStock stock2 = new ListingStock();
        setListingAttributes(stock2, "test_exchange", 46.895, 1710929671, "Stock", 45.1708, "teststock", 46.49, 0.97, "testticker", 3839039);
        stock2.setDividendYield(0.0);
        stock2.setOutstandingShares(295999000);
        stockRepository.save(stock2);

        //100000
        Holiday holiday = new Holiday();
        holiday.setDate(new Date(1583020800000L));
        holiday.setCountry(country);
        holidayRepository.save(holiday);

        Logger.info("BootstrapDataTest loaded!");

    }

    private void setListingAttributes(ListingBase listing, String exchange, double high, int lastRefresh, String listingType, double low, String name, double price, double priceChange, String ticker, int volume) {
        listing.setExchangeName(exchange);
        listing.setHigh(high);
        listing.setLastRefresh(lastRefresh);
        listing.setListingType(listingType);
        listing.setLow(low);
        listing.setName(name);
        listing.setPrice(price);
        listing.setPriceChange(priceChange);
        listing.setTicker(ticker);
        listing.setVolume(volume);
    }

    private Currency createCurrency(String curCode, String curName, String curSymbol, String polity) {
        Currency currency = new Currency();
        currency.setCurrencyCode(curCode);
        currency.setCurrencyName(curName);
        currency.setCurrencySymbol(curSymbol);
        currency.setPolity(polity);
        return currency;
    }
}
