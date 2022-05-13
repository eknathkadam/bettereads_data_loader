package com.webrich.bettereads.repository;

import com.webrich.bettereads.model.Author;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface AuthorRepository extends CassandraRepository<Author,String>{
    
}
