package com.capstone.landlordInsurance.controller;

import com.capstone.landlordInsurance.dto.PremiumResponseDto;
import com.capstone.landlordInsurance.dto.QuoteRequestDto;
import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.entity.Premium;
import com.capstone.landlordInsurance.entity.Quote;
import com.capstone.landlordInsurance.repository.PremiumRepository;
import com.capstone.landlordInsurance.service.BrokerService;
import com.capstone.landlordInsurance.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/quote")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private PremiumRepository premiumRepository;

    @Autowired
    private BrokerService brokerService;

    @PostMapping
    public ResponseEntity<?>  createQuote(@RequestBody QuoteRequestDto quoteRequestDTO) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            PremiumResponseDto response = quoteService.createQuote(quoteRequestDTO, brokerEmail);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Something went wrong";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping
    public ResponseEntity<?> getAllQuotes() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            String brokerEmail = auth.getName();
            Broker broker = brokerService.findByEmail(brokerEmail);
            if (broker == null) {
                throw new RuntimeException("Broker not found");
            }

            Long brokerId = broker.getBrokerId();
            List<Quote> quotes = quoteService.getQuotesByBrokerId(brokerId);
            return new ResponseEntity<>(quotes, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Error in fetching quotes";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/premium/{id}")
    public ResponseEntity<?> getPremiumByQuoteId(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();
            Quote quote = quoteService.getQuoteById(id);
            if (!quote.getBroker().getEmail().equals(brokerEmail)) {
                throw new RuntimeException("Access denied for this quote!");
            }
            Optional<Premium> premium = premiumRepository.findByQuoteId(id);

            if (premium.isPresent()) {
                PremiumResponseDto responseDto = quoteService.mapToPremiumDto(premium.get());
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            }
            throw new RuntimeException();
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Error in fetching premium";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuoteById(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            Quote quote = quoteService.getQuoteById(id);
            if (quote == null) {
                throw new RuntimeException("Quote does not exist!");
            } return new ResponseEntity<>(quote, HttpStatus.OK);
        } catch(Exception e) {
            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Error occurred!";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuote(@PathVariable Long id, @RequestBody QuoteRequestDto updatedQuote) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            PremiumResponseDto quote = quoteService.updateQuoteById(id, updatedQuote);
            if (quote == null) {
                throw new RuntimeException("Quote does not exist!");
            } return new ResponseEntity<>(quote, HttpStatus.OK);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Error occurred!";
            response.put("error", msg);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuote(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean deleted = quoteService.deleteQuote(id);
            if (!deleted) {
                throw new RuntimeException("Quote not found");
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/bind")
    public ResponseEntity<String> bindQuote(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            quoteService.updateQuoteStatus(id, "bound");
            return new ResponseEntity<>("Quote status updated to bound", HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            quoteService.updateQuoteStatus(id, "deleted");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<String> restoreQuote(@PathVariable Long id) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            quoteService.updateQuoteStatus(id, "pending");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/paginated")
//    This function will return combinations of these filters, applied on dashboard page in frontend:
//
//    search only
//    status only
//    days only
//    search + status
//    search + days
//    status + days
//    search + status + days
    public ResponseEntity<?> getPaginatedQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String search){

        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();

            Broker broker = brokerService.findByEmail(brokerEmail);
            if(broker == null){
                throw new RuntimeException("Broker not found");
            }

            Sort sort = sortOrder.equalsIgnoreCase("asc")
                    ? Sort.by("createdAt").ascending()
                    : Sort.by("createdAt").descending();

            LocalDateTime startDate = null;

            //Days from which quote is to be displayed
            if (days != null) {
                startDate = LocalDateTime.now().minusDays(days);
            }

            Pageable pageable = PageRequest.of(page, size, sort);

            //It will fetch directly from our query
            Page<Quote> paginatedQuotes =
                    quoteService.filterQuotes(
                            broker.getBrokerId(),
                            status,
                            startDate,
                            search,
                            pageable
                    );

            // Build Response
            Map<String, Object> response = new HashMap<>();
            response.put("content", paginatedQuotes.getContent());
            response.put("totalPages", paginatedQuotes.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
//            return new ResponseEntity<>(paginatedQuotes, HttpStatus.OK);
        } catch(Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage() != null ? e.getMessage() : "Error in fetching Quotes");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getQuoteStats() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String brokerEmail = auth.getName();

            Broker broker = brokerService.findByEmail(brokerEmail);
            if (broker == null) {
                throw new RuntimeException("Broker not found");
            }

            Long brokerId = broker.getBrokerId();

            long totalQuotes = quoteService.countAllQuotesByBrokerId(brokerId);

            // Count bound quotes
            long totalBoundQuotes = quoteService.countBoundQuotesByBrokerId(brokerId);

            // Build Response
            Map<String, Object> response = new HashMap<>();
            response.put("totalQuotes", totalQuotes);
            response.put("totalBoundQuotes", totalBoundQuotes);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage() != null ? e.getMessage() : "Error in fetching quote stats.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
