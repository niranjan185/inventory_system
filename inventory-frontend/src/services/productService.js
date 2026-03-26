import api from "./api";

export const productService = {
  // Get all products
  getAllProducts: async () => {
    console.log("ProductService: Fetching all products...");
    try {
      const response = await api.get("/products");
      console.log(
        "ProductService: Products fetched successfully:",
        response.data,
      );
      return response.data;
    } catch (error) {
      console.error("ProductService: Error fetching products:", error);
      throw error;
    }
  },

  // Get product by ID
  getProductById: async (id) => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  // Get products by category
  getProductsByCategory: async (category) => {
    const response = await api.get(`/products/category/${category}`);
    return response.data;
  },

  // Get low stock products (Admin only)
  getLowStockProducts: async () => {
    const response = await api.get("/products/low-stock");
    return response.data;
  },

  // Create product (Admin only)
  createProduct: async (productData) => {
    console.log("ProductService: Creating product with data:", productData);
    try {
      const response = await api.post("/products", productData);
      console.log(
        "ProductService: Product created successfully:",
        response.data,
      );
      return response.data;
    } catch (error) {
      console.error("ProductService: Error creating product:", error);
      throw error;
    }
  },

  // Update product (Admin only)
  updateProduct: async (id, productData) => {
    console.log("ProductService: Updating product with ID:", id, productData);
    try {
      const response = await api.put(`/products/${id}`, productData);
      console.log(
        "ProductService: Product updated successfully:",
        response.data,
      );
      return response.data;
    } catch (error) {
      console.error("ProductService: Error updating product:", error);

      // If it's a timeout error, provide more helpful message
      if (error.message.includes("timeout")) {
        throw new Error(
          "The update is taking longer than expected. This may be due to email notifications being sent. Please check if your changes were saved.",
        );
      }

      throw error;
    }
  },

  // Bulk update products (Admin only)
  bulkUpdateProducts: async (products) => {
    const response = await api.put("/products/bulk-update", products);
    return response.data;
  },

  // Delete product (Admin only)
  deleteProduct: async (id) => {
    const response = await api.delete(`/products/${id}`);
    return response.data;
  },

  // Get unique categories
  getCategories: async () => {
    console.log("ProductService: Fetching categories...");
    try {
      const response = await api.get("/products");

      // Check if response.data is an array
      if (!Array.isArray(response.data)) {
        console.error(
          "ProductService: Expected array but got:",
          typeof response.data,
          response.data,
        );
        throw new Error("Invalid response format - expected array of products");
      }

      const products = response.data;
      const categories = [
        ...new Set(products.map((product) => product.category)),
      ];
      console.log(
        "ProductService: Categories fetched successfully:",
        categories,
      );
      return categories.sort();
    } catch (error) {
      console.error("ProductService: Error fetching categories:", error);
      // If the main products endpoint fails, return empty array for categories
      return [];
    }
  },
};
