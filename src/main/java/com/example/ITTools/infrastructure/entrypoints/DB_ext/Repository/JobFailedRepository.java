package com.example.ITTools.infrastructure.entrypoints.DB_ext.Repository;

import com.example.ITTools.infrastructure.entrypoints.DB_ext.Model.JobFailed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobFailedRepository extends JpaRepository<JobFailed, Integer> {

}
