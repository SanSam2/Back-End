package org.example.sansam.status.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusCachingService {

    private final StatusRepository statusRepository;

    @Cacheable(cacheNames = "statusByName",key = "#name.name()", unless = "#result == null")
    public Status get(StatusEnum name){
        return statusRepository.findByStatusName(name);
    }
}
