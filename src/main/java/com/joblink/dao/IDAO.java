package com.joblink.dao;

import java.util.List;

// Interface générique pour les opérations CRUD
public interface IDAO<T> {
    T create(T obj);
    T getById(int id);
    List<T> getAll();
    T update(T obj);
    void delete(int id);
}