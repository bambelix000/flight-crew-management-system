import React, { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import Toast from './Toast.jsx';

const ToastContext = createContext(null);

let nextId = 1;

export function ToastProvider({ children, autoDismissMs = 4000 }) {
  const [toasts, setToasts] = useState([]);
  const timersRef = useRef(new Map());

  const remove = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    const timer = timersRef.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timersRef.current.delete(id);
    }
  }, []);

  const push = useCallback((type, message) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type, message }]);
    if (autoDismissMs > 0) {
      const timer = setTimeout(() => remove(id), autoDismissMs);
      timersRef.current.set(id, timer);
    }
    return id;
  }, [autoDismissMs, remove]);

  const api = useMemo(() => ({
    success: (msg) => push('success', msg),
    error: (msg) => push('error', msg),
    info: (msg) => push('info', msg),
    dismiss: remove,
  }), [push, remove]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div
        aria-live="polite"
        style={{
          position: 'fixed',
          top: 16,
          right: 16,
          display: 'flex',
          flexDirection: 'column',
          gap: 10,
          zIndex: 9999,
        }}
      >
        {toasts.map((t) => (
          <Toast key={t.id} toast={t} onClose={() => remove(t.id)} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
