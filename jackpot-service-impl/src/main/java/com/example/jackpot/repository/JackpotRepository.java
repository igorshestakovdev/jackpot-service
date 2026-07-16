package com.example.jackpot.repository;

import com.example.jackpot.entity.Jackpot;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRepository extends CrudRepository<Jackpot, String> {

    @Query("SELECT * FROM jackpots WHERE id = :id FOR UPDATE")
    Optional<Jackpot> findByIdForUpdate(@Param("id") String id);
}
