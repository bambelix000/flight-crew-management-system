import React from 'react';
import { colors } from '../styles/tokens';

const palette = {
  success: { bg: colors.successBg, border: colors.successBorder, text: colors.successText },
  error: { bg: colors.dangerBg, border: colors.dangerBorder, text: colors.danger },
  info: { bg: colors.infoBg, border: '#bee3f8', text: colors.info },
};

function Toast({ toast, onClose }) {
  const tone = palette[toast.type] || palette.info;
  return (
    <div
      role="status"
      style={{
        backgroundColor: tone.bg,
        color: tone.text,
        border: `1px solid ${tone.border}`,
        padding: '12px 16px',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
        minWidth: '260px',
        maxWidth: '400px',
        fontSize: '14px',
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        gap: '12px',
      }}
    >
      <span style={{ flex: 1 }}>{toast.message}</span>
      <button
        onClick={onClose}
        aria-label="Zamknij powiadomienie"
        style={{
          background: 'none',
          border: 'none',
          color: tone.text,
          cursor: 'pointer',
          fontSize: '18px',
          lineHeight: 1,
          padding: 0,
        }}
      >
        ×
      </button>
    </div>
  );
}

export default Toast;
