package sn.casamance.reservation;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Visit {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String title;
  @Column(length=1200) private String description;
  @Column(nullable=false) private String location;
  @Column(nullable=false) private String category;
  @Column(nullable=false) private String duration;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
  @Column(nullable=false) private String image;
  @Column(nullable=false) private double rating;
  @Column(nullable=false) private int capacity;

  protected Visit() {}
  public Visit(String title, String description, String location, String category, String duration, BigDecimal price, String image, double rating, int capacity) {
    this.title=title; this.description=description; this.location=location; this.category=category; this.duration=duration; this.price=price; this.image=image; this.rating=rating; this.capacity=capacity;
  }
  public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public String getLocation(){return location;}
  public String getCategory(){return category;} public String getDuration(){return duration;} public BigDecimal getPrice(){return price;} public String getImage(){return image;} public double getRating(){return rating;} public int getCapacity(){return capacity;}
}
