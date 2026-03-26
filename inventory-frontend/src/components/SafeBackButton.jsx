import { ArrowLeftIcon } from "@heroicons/react/24/outline";
import { useAppNavigation } from "../hooks/useAppNavigation";

const SafeBackButton = ({ className = "", fallbackPath = "/dashboard" }) => {
  const { goBack, navigateToPage } = useAppNavigation();

  const handleBack = () => {
    // Try to go back, but if there's no safe history, go to fallback
    try {
      if (window.history.length > 1) {
        goBack();
      } else {
        navigateToPage(fallbackPath);
      }
    } catch (error) {
      // If anything goes wrong, go to safe fallback
      navigateToPage(fallbackPath);
    }
  };

  return (
    <button
      onClick={handleBack}
      className={`flex items-center space-x-2 text-gray-600 hover:text-gray-900 transition-colors ${className}`}
    >
      <ArrowLeftIcon className="h-5 w-5" />
      <span>Back</span>
    </button>
  );
};

export default SafeBackButton;
