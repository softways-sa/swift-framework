/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.softways.dev.eshop.orders.v2.chart;

import java.math.BigDecimal;
import org.joda.time.LocalDate;

/**
 *
 * @author panos
 */
public class Order {

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public Integer getOrders() {
    return orders;
  }

  public void setOrders(Integer orders) {
    this.orders = orders;
  }
  
  private String orderId;
  private Integer quantity;
  private Integer orders;
  private LocalDate orderDate;
  private BigDecimal value;
}
