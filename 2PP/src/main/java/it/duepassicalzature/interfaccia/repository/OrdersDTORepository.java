package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.DTO.OrdersDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersDTORepository extends JpaRepository<OrdersDTO, Integer> {
}
