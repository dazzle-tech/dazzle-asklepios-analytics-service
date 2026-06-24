package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActiveIngredientsRepository  extends JpaRepository<ActiveIngredients, Long> {

//    ActiveIngredients findById(Long id);

}
