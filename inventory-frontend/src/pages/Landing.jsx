import { Link } from "react-router-dom";
import {
  ChartBarIcon,
  CubeIcon,
  BellIcon,
  DocumentChartBarIcon,
  ShieldCheckIcon,
  ClockIcon,
} from "@heroicons/react/24/outline";

const Landing = () => {
  const features = [
    {
      icon: CubeIcon,
      title: "Product Management",
      description:
        "Efficiently manage your inventory with real-time stock tracking and automated alerts.",
    },
    {
      icon: ChartBarIcon,
      title: "Analytics & Reports",
      description:
        "Get detailed insights with comprehensive reports and data visualization.",
    },
    {
      icon: BellIcon,
      title: "Smart Alerts",
      description:
        "Never run out of stock with intelligent low-stock notifications.",
    },
    {
      icon: DocumentChartBarIcon,
      title: "Audit Logs",
      description:
        "Complete audit trail for all inventory movements and changes.",
    },
    {
      icon: ShieldCheckIcon,
      title: "Role-Based Access",
      description: "Secure access control with admin and user role management.",
    },
    {
      icon: ClockIcon,
      title: "Real-Time Updates",
      description:
        "Live dashboard updates with instant synchronization across all devices.",
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <CubeIcon className="h-8 w-8 text-indigo-600 mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">InventoryPro</h1>
            </div>
            <div className="flex space-x-4">
              <Link
                to="/login"
                className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md text-sm font-medium"
              >
                Get Started
              </Link>
            </div>
          </div>
        </div>
      </header>
      {/* Hero Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center">
          <h2 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
            Smart Inventory
            <span className="text-indigo-600"> Management</span>
          </h2>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            Streamline your inventory operations with our comprehensive
            management system. Track products, monitor stock levels, and get
            real-time insights to optimize your business.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-3 rounded-lg text-lg font-semibold transition-colors"
            >
              Start Free Trial
            </Link>
            <Link
              to="/login"
              className="border border-indigo-600 text-indigo-600 hover:bg-indigo-50 px-8 py-3 rounded-lg text-lg font-semibold transition-colors"
            >
              Sign In
            </Link>
          </div>

          {/* Demo credentials info */}
          <div className="mt-12 p-6 bg-white rounded-xl shadow-lg max-w-md mx-auto">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Try Demo Accounts
            </h3>
            <div className="text-sm text-gray-600 space-y-2">
              <div className="flex justify-between">
                <span className="font-medium">Admin:</span>
                <span>admin@inventory.com / admin123</span>
              </div>
              <div className="flex justify-between">
                <span className="font-medium">User:</span>
                <span>user@inventory.com / user123</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="bg-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h3 className="text-3xl font-bold text-gray-900 mb-4">
              Everything You Need to Manage Inventory
            </h3>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Our platform provides all the tools you need to efficiently manage
              your inventory, from small businesses to enterprise operations.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <div
                key={index}
                className="bg-gray-50 p-6 rounded-xl hover:shadow-lg transition-shadow"
              >
                <feature.icon className="h-12 w-12 text-indigo-600 mb-4" />
                <h4 className="text-xl font-semibold text-gray-900 mb-2">
                  {feature.title}
                </h4>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
      {/* Stats Section */}
      <section className="bg-indigo-600 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold text-white mb-2">99.9%</div>
              <div className="text-indigo-200">Uptime Guarantee</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-white mb-2">10K+</div>
              <div className="text-indigo-200">Products Managed</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-white mb-2">24/7</div>
              <div className="text-indigo-200">Real-time Monitoring</div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-gray-50 py-20">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h3 className="text-3xl font-bold text-gray-900 mb-4">
            Ready to Transform Your Inventory Management?
          </h3>
          <p className="text-lg text-gray-600 mb-8">
            Join thousands of businesses that trust InventoryPro to manage their
            inventory efficiently.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-3 rounded-lg text-lg font-semibold transition-colors"
            >
              Get Started Today
            </Link>
            <div className="text-sm text-gray-500 flex items-center justify-center">
              No credit card required • Free 30-day trial
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-white border-t">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center mb-4 md:mb-0">
              <CubeIcon className="h-6 w-6 text-indigo-600 mr-2" />
              <span className="text-gray-900 font-semibold">InventoryPro</span>
            </div>
            <div className="text-sm text-gray-500">
              © 2026 InventoryPro. All rights reserved.
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Landing;
