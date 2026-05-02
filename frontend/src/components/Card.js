import React from 'react';
import { card as cardStyle } from '../styles/tokens';

function Card({ children, style, ...rest }) {
  return (
    <div style={{ ...cardStyle, ...style }} {...rest}>
      {children}
    </div>
  );
}

export default Card;
