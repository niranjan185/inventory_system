# Inventory Management System - Frontend

A modern React-based admin dashboard for the Enterprise Inventory Management System.

## Features

- **Authentication & Authorization**: JWT-based login with role-based access control
- **Dashboard**: Real-time inventory analytics with charts and statistics
- **Product Management**: Full CRUD operations with search and filtering
- **Alert System**: Low stock alerts and notifications (Admin only)
- **Reporting**: Comprehensive reports with CSV export capabilities
- **User Management**: User administration and role management (Admin only)
- **Audit Logs**: Complete activity tracking and audit trails (Admin only)
- **Settings**: User preferences and notification settings

## Tech Stack

- **React 18** with Vite for fast development
- **Tailwind CSS** for modern, responsive styling
- **React Router DOM** for client-side routing
- **Axios** for API communication
- **Recharts** for data visualization
- **React Hot Toast** for notifications
- **Heroicons** for consistent iconography
- **Headless UI** for accessible components

## Getting Started

### Prerequisites

- Node.js 16+ and npm
- Backend API running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Demo Credentials

- **Admin**: admin@inventory.com / admin123
- **User**: user@inventory.com / user123

## Project Structure

```
src/
├── components/          # Reusable UI components
├── pages/              # Page components
├── context/            # React Context providers
├── services/           # API service functions
├── hooks/              # Custom React hooks
├── utils/              # Utility functions and constants
└── styles/             # Global CSS styles
```

## API Integration

The frontend integrates with the Spring Boot backend API:

- Base URL: `http://localhost:8080/api`
- Authentication: JWT Bearer tokens
- Automatic token refresh and error handling

## Features by Role

### User Role

- View dashboard and analytics
- Browse products (read-only)
- Generate reports
- Manage personal settings

### Admin Role

- All user features plus:
- Full product management (CRUD)
- Alert management
- User administration
- Audit log access
- CSV export capabilities

## Development

The application uses modern React patterns:

- Functional components with hooks
- Context API for state management
- Custom hooks for reusable logic
- Responsive design with Tailwind CSS
- TypeScript-ready architecture
