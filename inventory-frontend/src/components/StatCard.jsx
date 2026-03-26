import React from "react";
import { Link } from "react-router-dom";

const StatCard = ({
  title,
  value,
  icon: Icon,
  change,
  changeType = "neutral",
  loading = false,
  color = "primary",
  link,
}) => {
  const changeColors = {
    positive: "text-green-600",
    negative: "text-red-600",
    neutral: "text-gray-600",
  };

  const iconColors = {
    blue: "text-blue-600",
    green: "text-green-600",
    yellow: "text-yellow-600",
    red: "text-red-600",
    purple: "text-purple-600",
    primary: "text-primary-600",
  };

  if (loading) {
    return (
      <div className="card p-6">
        <div className="animate-pulse">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <div className="h-8 w-8 bg-gray-200 rounded"></div>
            </div>
            <div className="ml-5 w-0 flex-1">
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div className="h-6 bg-gray-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const CardContent = () => (
    <div className="flex items-center">
      <div className="flex-shrink-0">
        {Icon && (
          <Icon
            className={`h-8 w-8 ${iconColors[color] || iconColors.primary}`}
          />
        )}
      </div>
      <div className="ml-5 w-0 flex-1">
        <dl>
          <dt className="text-sm font-medium text-gray-500 truncate">
            {title}
          </dt>
          <dd className="flex items-baseline">
            <div className="text-2xl font-semibold text-gray-900">{value}</div>
            {change && (
              <div
                className={`ml-2 flex items-baseline text-sm font-semibold ${changeColors[changeType]}`}
              >
                {change}
              </div>
            )}
          </dd>
        </dl>
      </div>
    </div>
  );

  if (link) {
    return (
      <Link
        to={link}
        className="card p-6 hover:shadow-lg transition-shadow duration-200"
      >
        <CardContent />
      </Link>
    );
  }

  return (
    <div className="card p-6">
      <CardContent />
    </div>
  );
};

export default StatCard;
