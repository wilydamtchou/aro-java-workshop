package com.m2ibank.transfer.repository;

import com.m2ibank.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceAccountIdOrDestinationAccountId(Long sourceAccountId, Long destinationAccountId);
}
