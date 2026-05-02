import React from 'react';
import { colors } from '../styles/tokens';
import Spinner from './Spinner';

const variantStyles = {
  primary: { backgroundColor: colors.primary, color: '#fff', border: 'none' },
  success: { backgroundColor: colors.success, color: '#fff', border: 'none' },
  danger: { backgroundColor: colors.danger, color: '#fff', border: 'none' },
  secondary: { backgroundColor: '#fff', color: colors.text, border: `1px solid ${colors.border}` },
};

function Button({
  variant = 'primary',
  loading = false,
  disabled = false,
  type = 'button',
  onClick,
  children,
  style,
  ...rest
}) {
  const isDisabled = disabled || loading;
  const base = {
    padding: '12px 20px',
    borderRadius: '8px',
    fontSize: '15px',
    fontWeight: 600,
    cursor: isDisabled ? 'not-allowed' : 'pointer',
    opacity: isDisabled ? 0.65 : 1,
    transition: 'opacity 0.2s, background-color 0.2s',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
  };
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={isDisabled}
      style={{ ...base, ...variantStyles[variant], ...style }}
      {...rest}
    >
      {loading && <Spinner size={14} thickness={2} color="#fff" />}
      {children}
    </button>
  );
}

export default Button;
