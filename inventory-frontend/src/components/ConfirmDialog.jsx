import React from "react";
import { ExclamationTriangleIcon } from "@heroicons/react/24/outline";
import Modal from "./Modal";

const ConfirmDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title = "Confirm Action",
  message = "Are you sure you want to perform this action?",
  confirmText = "Confirm",
  cancelText = "Cancel",
  type = "danger",
}) => {
  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  const buttonClasses = {
    danger: "btn-danger",
    warning: "bg-yellow-600 hover:bg-yellow-700 text-white",
    primary: "btn-primary",
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} size="sm">
      <div className="mt-2">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <ExclamationTriangleIcon
              className={`h-6 w-6 ${
                type === "danger"
                  ? "text-red-600"
                  : type === "warning"
                    ? "text-yellow-600"
                    : "text-primary-600"
              }`}
            />
          </div>
          <div className="ml-3">
            <p className="text-sm text-gray-500">{message}</p>
          </div>
        </div>
      </div>

      <div className="mt-6 flex justify-end space-x-3">
        <button type="button" className="btn-secondary" onClick={onClose}>
          {cancelText}
        </button>
        <button
          type="button"
          className={`btn ${buttonClasses[type]}`}
          onClick={handleConfirm}
        >
          {confirmText}
        </button>
      </div>
    </Modal>
  );
};

export default ConfirmDialog;
