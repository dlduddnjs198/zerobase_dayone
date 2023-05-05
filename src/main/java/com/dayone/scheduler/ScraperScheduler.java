package com.dayone.scheduler;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.repository.CompanyRepository;
import com.dayone.persist.repository.DividendRepository;
import com.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dayone.model.constants.CacheKey.KEY_FINANCE;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler{

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;
    // 일정 주기마다 수행

    @CacheEvict(value = KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("scraping scheduler is started");
        // 지정된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();
        // 회사마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getName(), company.getTicker()));
            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값을 저장
            scrapedResult.getDividends().stream()
                    // dividend 모델을 dividend 엔티티에 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 앨리먼트를 하나씩 dividend 레포에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndAndDate(e.getCompanyId(),e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e);
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


    }
}
