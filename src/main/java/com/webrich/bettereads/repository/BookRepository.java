package com.webrich.bettereads.repository;

import com.webrich.bettereads.model.Book;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;


@Repository 
public interface BookRepository extends CassandraRepository<Book,String>{
    
}
