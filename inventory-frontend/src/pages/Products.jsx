import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import {
  PlusIcon,
  MagnifyingGlassIcon,
  ArrowDownTrayIcon,
} from "@heroicons/react/24/outline";
import { toast } from "react-hot-toast";

import DataTable from "../components/DataTable";
import LoadingSpinner from "../components/LoadingSpinner";
import Modal from "../components/Modal";
import ConfirmDialog from "../components/ConfirmDialog";
import { useAuth } from "../context/AuthContext";
import { productService } from "../services/productService";
import { reportService } from "../services/reportService";
import {
  ROLES,
  STOCK_STATUS_COLORS,
  STOCK_STATUS_LABELS,
} from "../utils/constants";
import { formatCurrency, hasRole } from "../utils/helpers";

const ProductForm = ({ product, onSubmit, onCancel, isLoading }) => {
  const [formData, setFormData] = useState({
    name: product?.name || "",
    category: product?.category || "",
    price: product?.price || "",
    quantity: product?.quantity || "",
    reorderLevel: product?.reorderLevel || "",
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Validate and convert form data
    const price = parseFloat(formData.price);
    const quantity = parseInt(formData.quantity);
    const reorderLevel = parseInt(formData.reorderLevel);

    // Check for invalid values
    if (isNaN(price) || price < 0) {
      toast.error("Please enter a valid price");
      return;
    }

    if (isNaN(quantity) || quantity < 0) {
      toast.error("Please enter a valid quantity");
      return;
    }

    if (isNaN(reorderLevel) || reorderLevel < 0) {
      toast.error("Please enter a valid reorder level");
      return;
    }

    onSubmit({
      ...formData,
      price,
      quantity,
      reorderLevel,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700">Name</label>
        <input
          type="text"
          name="name"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.name}
          onChange={handleChange}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Category
        </label>
        <input
          type="text"
          name="category"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.category}
          onChange={handleChange}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Price</label>
        <input
          type="number"
          name="price"
          step="0.01"
          min="0"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.price}
          onChange={handleChange}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Quantity
        </label>
        <input
          type="number"
          name="quantity"
          min="0"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.quantity}
          onChange={handleChange}
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">
          Reorder Level
        </label>
        <input
          type="number"
          name="reorderLevel"
          min="0"
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500"
          value={formData.reorderLevel}
          onChange={handleChange}
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={isLoading}
          className="px-4 py-2 text-sm font-medium text-white bg-primary-600 border border-transparent rounded-md hover:bg-primary-700 disabled:opacity-50"
        >
          {isLoading ? (
            <LoadingSpinner size="sm" />
          ) : product ? (
            "Update"
          ) : (
            "Create"
          )}
        </button>
      </div>
    </form>
  );
};

const Products = () => {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [deleteProduct, setDeleteProduct] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [exportLoading, setExportLoading] = useState(false);

  const isAdmin = hasRole(user?.role, [ROLES.ADMIN]);
  const filterParam = searchParams.get("filter");

  const fetchProducts = async () => {
    try {
      console.log("Products page: Starting to fetch products...");
      console.log("Filter param:", filterParam);
      console.log("User role:", user?.role);

      setLoading(true);
      const [productsData, categoriesData] = await Promise.all([
        filterParam === "low-stock"
          ? productService.getLowStockProducts()
          : productService.getAllProducts(),
        productService.getCategories(),
      ]);

      console.log("Products page: Data fetched successfully");
      console.log("Products:", productsData);
      console.log("Categories:", categoriesData);

      setProducts(productsData);
      setCategories(categoriesData);
    } catch (error) {
      console.error("Products page: Error fetching products:", error);
      toast.error("Failed to fetch products: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [filterParam]);

  const handleCreateProduct = async (productData) => {
    setFormLoading(true);
    try {
      await productService.createProduct(productData);
      toast.success("Product created successfully");
      setShowModal(false);
      fetchProducts();
    } catch (error) {
      toast.error(error.message || "Failed to create product");
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateProduct = async (productData) => {
    setFormLoading(true);
    try {
      console.log("Updating product:", editingProduct.id, productData);
      await productService.updateProduct(editingProduct.id, productData);
      toast.success("Product updated successfully");
      setShowModal(false);
      setEditingProduct(null);
      fetchProducts();
    } catch (error) {
      console.error("Error updating product:", error);
      if (error.message.includes("timeout")) {
        toast.error(
          "Update is taking longer than expected. Please check if the changes were saved and refresh the page.",
        );
      } else {
        toast.error(error.message || "Failed to update product");
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteProduct = async () => {
    try {
      await productService.deleteProduct(deleteProduct.id);
      toast.success("Product deleted successfully");
      setDeleteProduct(null);
      fetchProducts();
    } catch (error) {
      toast.error(error.message || "Failed to delete product");
    }
  };

  const handleExportProducts = async () => {
    setExportLoading(true);
    try {
      if (filterParam === "low-stock") {
        await reportService.exportLowStockCSV();
        toast.success("Low stock products exported successfully!");
      } else {
        await reportService.exportProductsCSV();
        toast.success("Products exported successfully!");
      }
    } catch (error) {
      console.error("Error exporting products:", error);
      toast.error("Failed to export products: " + error.message);
    } finally {
      setExportLoading(false);
    }
  };

  const filteredProducts = products.filter((product) => {
    const matchesSearch =
      product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.category.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory =
      !selectedCategory || product.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const columns = [
    {
      key: "name",
      label: "Name",
      sortable: true,
    },
    {
      key: "category",
      label: "Category",
      sortable: true,
    },
    {
      key: "price",
      label: "Price",
      sortable: true,
      render: (value) => formatCurrency(value),
    },
    {
      key: "quantity",
      label: "Quantity",
      sortable: true,
    },
    {
      key: "reorderLevel",
      label: "Reorder Level",
      sortable: true,
    },
    {
      key: "stockStatus",
      label: "Status",
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
            value === "IN_STOCK"
              ? "bg-green-100 text-green-800"
              : value === "LOW_STOCK"
                ? "bg-yellow-100 text-yellow-800"
                : "bg-red-100 text-red-800"
          }`}
        >
          {STOCK_STATUS_LABELS[value]}
        </span>
      ),
    },
    {
      key: "totalValue",
      label: "Total Value",
      sortable: true,
      render: (value) => formatCurrency(value),
    },
  ];

  if (isAdmin) {
    columns.push({
      key: "actions",
      label: "Actions",
      render: (_, product) => (
        <div className="flex space-x-2">
          <button
            onClick={() => {
              setEditingProduct(product);
              setShowModal(true);
            }}
            className="text-primary-600 hover:text-primary-900 text-sm font-medium"
          >
            Edit
          </button>
          <button
            onClick={() => setDeleteProduct(product)}
            className="text-red-600 hover:text-red-900 text-sm font-medium"
          >
            Delete
          </button>
        </div>
      ),
    });
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">
          {filterParam === "low-stock" ? "Low Stock Products" : "Products"}
        </h1>
        <div className="flex items-center space-x-3">
          {/* Export Button */}
          <button
            onClick={handleExportProducts}
            disabled={exportLoading}
            className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
          >
            {exportLoading ? (
              <LoadingSpinner size="sm" />
            ) : (
              <ArrowDownTrayIcon className="h-4 w-4 mr-2" />
            )}
            Export CSV
          </button>

          {/* Add Product Button (Admin only) */}
          {isAdmin && (
            <button
              onClick={() => {
                setEditingProduct(null);
                setShowModal(true);
              }}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <PlusIcon className="h-4 w-4 mr-2" />
              Add Product
            </button>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow space-y-4 sm:space-y-0 sm:flex sm:items-center sm:space-x-4">
        <div className="flex-1">
          <div className="relative">
            <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search products..."
              className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:ring-primary-500 focus:border-primary-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        <div className="sm:w-48">
          <select
            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-white shadow rounded-lg">
        <DataTable
          data={filteredProducts}
          columns={columns}
          emptyMessage="No products found"
        />
      </div>

      {/* Product Form Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingProduct(null);
        }}
        title={editingProduct ? "Edit Product" : "Add Product"}
      >
        <ProductForm
          product={editingProduct}
          onSubmit={editingProduct ? handleUpdateProduct : handleCreateProduct}
          onCancel={() => {
            setShowModal(false);
            setEditingProduct(null);
          }}
          isLoading={formLoading}
        />
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deleteProduct}
        onClose={() => setDeleteProduct(null)}
        onConfirm={handleDeleteProduct}
        title="Delete Product"
        message={`Are you sure you want to delete "${deleteProduct?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        confirmColor="red"
      />
    </div>
  );
};

export default Products;
