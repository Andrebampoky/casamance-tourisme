package sn.casamance.reservation;
import java.math.BigDecimal;
record VisitSnapshot(Long id,String title,String description,String location,String category,String duration,BigDecimal price,String image,double rating,int capacity){}
