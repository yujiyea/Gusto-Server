package com.umc.gusto.domain.route.repository;

import com.umc.gusto.domain.route.entity.Route;
import com.umc.gusto.domain.route.entity.RouteList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteListRepository extends JpaRepository<RouteList,Long> {
    int countRouteListByRoute(Route route);

    List<RouteList> findByRoute(Route route);
}
