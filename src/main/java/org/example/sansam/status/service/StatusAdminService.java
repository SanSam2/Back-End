package org.example.sansam.status.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.status.domain.Status;
import org.example.sansam.status.domain.StatusEnum;
import org.example.sansam.status.repository.StatusRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusAdminService {

    private final StatusRepository statusRepository;

    @CachePut(cacheNames = "statusByName", key = "#status.getStatusName().name()")
    public Status update(Status status){
        return statusRepository.save(status);
    }

    @CacheEvict(cacheNames = "statusByName", key = "#name.name()")
    public void evict(StatusEnum statusName){

    }
}
