package com.codingshuttle.projects.airBnbApp.repository;

import com.codingshuttle.projects.airBnbApp.entity.Hotel;
import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import com.codingshuttle.projects.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    void deleteByDateAfterAndRoom(LocalDate today, Room room);

    void deleteByRoom(Room room);

    // This method is get all the hotels in the city that has at least one roomType available for all the dates between startDate and endDate.
    // Here reservedCount is number of rooms which are currently being booked
    //      bookedCount is number of rooms which are already booked and confirmed
    //      totalCount is number of rooms still available
    @Query("""
            SELECT i.hotel
            FROM Inventory i
            WHERE i.city = :city
              AND (i.date BETWEEN :startDate AND :endDate)
              AND i.closed = false
              AND (i.totalCount-i.bookedCount - i.reservedCount) >= :roomsCount
            GROUP BY i.hotel
            HAVING COUNT(i.date) >= :dateCount
            """)
    Page<Hotel> findHotelWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable);

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
              AND (i.date BETWEEN :startDate AND :endDate)
              AND i.closed = false
              AND (i.totalCount-i.bookedCount -i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate checkInDate,
            @Param("endDate") LocalDate checkOutDate,
            @Param("roomsCount") Integer roomsCount);


    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
}
