package com.inventory.inventory_system.service;

import com.inventory.inventory_system.dto.InventoryReport;
import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepository;

    public InventoryReport generateInventoryReport() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        
        InventoryReport report = new InventoryReport();
        report.setTotalProducts(allProducts.size());
        report.setLowStockProducts(lowStockProducts.size());
        
        // Calculate total inventory value
        double totalValue = allProducts.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        report.setTotalInventoryValue(totalValue);
        
        // Generate category summaries
        Map<String, List<Product>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory));
        
        List<InventoryReport.CategorySummary> categorySummaries = productsByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<Product> products = entry.getValue();
                    int count = products.size();
                    double categoryValue = products.stream()
                            .mapToDouble(p -> p.getPrice() * p.getQuantity())
                            .sum();
                    return new InventoryReport.CategorySummary(category, count, categoryValue);
                })
                .collect(Collectors.toList());
        
        report.setCategorySummaries(categorySummaries);
        
        return report;
    }

    public String generateProductsCsv() {
        List<Product> products = productRepository.findAll();
        
        StringWriter writer = new StringWriter();
        writer.append("ID,Name,Category,Price,Quantity,Reorder Level,Total Value,Stock Status\n");
        
        for (Product product : products) {
            double totalValue = product.getPrice() * product.getQuantity();
            String stockStatus = product.getQuantity() <= product.getReorderLevel() ? "LOW STOCK" : "IN STOCK";
            
            writer.append(String.valueOf(product.getId())).append(",")
                  .append(escapeSpecialCharacters(product.getName())).append(",")
                  .append(escapeSpecialCharacters(product.getCategory())).append(",")
                  .append(String.valueOf(product.getPrice())).append(",")
                  .append(String.valueOf(product.getQuantity())).append(",")
                  .append(String.valueOf(product.getReorderLevel())).append(",")
                  .append(String.valueOf(totalValue)).append(",")
                  .append(stockStatus).append("\n");
        }
        
        return writer.toString();
    }

    public String generateLowStockCsv() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        
        StringWriter writer = new StringWriter();
        writer.append("ID,Name,Category,Current Quantity,Reorder Level,Shortage\n");
        
        for (Product product : lowStockProducts) {
            int shortage = product.getReorderLevel() - product.getQuantity();
            
            writer.append(String.valueOf(product.getId())).append(",")
                  .append(escapeSpecialCharacters(product.getName())).append(",")
                  .append(escapeSpecialCharacters(product.getCategory())).append(",")
                  .append(String.valueOf(product.getQuantity())).append(",")
                  .append(String.valueOf(product.getReorderLevel())).append(",")
                  .append(String.valueOf(shortage)).append("\n");
        }
        
        return writer.toString();
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}