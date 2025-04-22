package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.repository.generic.GenericRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RepositoryRegistry implements ApplicationContextAware {

    private final Map<String, CrudRepository<?, ?>> repositoryMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) {
        Map<String, GenericRepository> beans = context.getBeansOfType(GenericRepository.class);

        for (Map.Entry<String, GenericRepository> entry : beans.entrySet()) {
            String beanName = entry.getKey(); // e.g., "actionRepository"
            String code = resolveCodeFromBeanName(beanName.toLowerCase()); // → "action"

            CrudRepository<?, ?> repo = (CrudRepository<?, ?>) entry.getValue(); // ✅ cast safely
            repositoryMap.put(code, repo);
        }

        log.info("📦 Auto-registered repositories: {}", repositoryMap.keySet());
    }

    private String resolveCodeFromBeanName(String beanName) {
        if (beanName.endsWith(ApplicationConstant.APPLICATION_REPOSITORY_SUFFIX)) {
            return beanName.substring(0, beanName.length() - ApplicationConstant.APPLICATION_REPOSITORY_SUFFIX.length()).toLowerCase();
        }
        return beanName;
    }

    public Optional<CrudRepository<?, ?>> getByCode(String code) {
        return Optional.ofNullable(repositoryMap.get(code));
    }
}
