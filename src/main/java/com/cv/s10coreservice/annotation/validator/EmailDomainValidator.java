package com.cv.s10coreservice.annotation.validator;

import com.cv.s10coreservice.annotation.ValidEmailDomain;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Component
public class EmailDomainValidator implements ConstraintValidator<ValidEmailDomain, String> {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int DNS_LOOKUP_TIMEOUT_MS = 1500; // 1.5 seconds timeout
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    // Caffeine Cache setup: Expiry after write (10 minutes), max 1000 entries
    private static final Cache<String, Boolean> DOMAIN_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES) // Expire after 10 minutes
            .maximumSize(1000) // Max size 1000 entries
            .build();

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
            return true; // Let @Pattern validator handle bad email formats separately
        }

        String domain = extractDomain(email);

        // Check cache for domain validity
        Boolean isValid = DOMAIN_CACHE.getIfPresent(domain.toLowerCase());
        if (isValid != null) {
            return isValid; // Return cached result if available
        }

        // Cache miss, perform live DNS MX lookup
        isValid = validateDomainWithDnsJava(domain);

        // Store result in cache
        DOMAIN_CACHE.put(domain.toLowerCase(), isValid);

        return isValid;
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        return (atIndex >= 0 && atIndex + 1 < email.length()) ? email.substring(atIndex + 1) : "";
    }

    private boolean validateDomainWithDnsJava(String domain) {
        Callable<Boolean> task = () -> {
            try {
                Lookup lookup = new Lookup(domain, Type.MX);
                SimpleResolver resolver = new SimpleResolver("8.8.8.8"); // Google Public DNS
                resolver.setTimeout(Duration.ofMillis(DNS_LOOKUP_TIMEOUT_MS));
                lookup.setResolver(resolver);
                lookup.setCache(null); // No local caching, always fresh lookup
                Record[] records = lookup.run();

                if (lookup.getResult() != Lookup.SUCCESSFUL) {
                    return false;
                }

                return records != null && records.length > 0;
            } catch (UnknownHostException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        };

        Future<Boolean> future = VIRTUAL_EXECUTOR.submit(task);
        try {
            return future.get(DNS_LOOKUP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            return false;
        } finally {
            future.cancel(true);
        }
    }
}
