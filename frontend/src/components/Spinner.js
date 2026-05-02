import React from 'react';
import { colors } from '../styles/tokens';

const styleId = 'fc-spinner-keyframes';

function ensureKeyframes() {
  if (typeof document === 'undefined') return;
  if (document.getElementById(styleId)) return;
  const el = document.createElement('style');
  el.id = styleId;
  el.textContent = '@keyframes fc-spin { to { transform: rotate(360deg); } }';
  document.head.appendChild(el);
}

function Spinner({ size = 20, color = colors.primary, thickness = 2 }) {
  ensureKeyframes();
  return (
    <span
      aria-label="Ładowanie"
      role="status"
      style={{
        display: 'inline-block',
        width: size,
        height: size,
        border: `${thickness}px solid ${colors.borderLight}`,
        borderTopColor: color,
        borderRadius: '50%',
        animation: 'fc-spin 0.8s linear infinite',
        boxSizing: 'border-box',
      }}
    />
  );
}

export default Spinner;
