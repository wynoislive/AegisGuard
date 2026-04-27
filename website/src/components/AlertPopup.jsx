import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ShieldAlert, Server } from 'lucide-react';
import './components.css';

export default function AlertPopup() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    // Randomly pop up the alert every few seconds
    const interval = setInterval(() => {
      setVisible(true);
      setTimeout(() => setVisible(false), 4000);
    }, 8000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ position: 'fixed', bottom: '30px', right: '30px', zIndex: 1000 }}>
      <AnimatePresence>
        {visible && (
          <motion.div
            initial={{ x: 100, opacity: 0, scale: 0.8 }}
            animate={{ x: 0, opacity: 1, scale: 1 }}
            exit={{ x: 100, opacity: 0, scale: 0.8 }}
            transition={{ type: 'spring', stiffness: 200, damping: 20 }}
            className="mock-discord-alert cyberpunk-panel"
            style={{ padding: '15px', width: '300px' }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px', borderBottom: '1px solid var(--primary-red-dim)', paddingBottom: '5px' }}>
              <ShieldAlert color="var(--primary-red)" size={20} />
              <strong style={{ color: 'var(--primary-red)', fontFamily: 'var(--font-heading)' }}>VIOLATION DETECTED</strong>
              <span className="badge">BOT</span>
            </div>
            
            <div style={{ fontSize: '0.85rem', lineHeight: '1.4' }}>
              <p><strong>Player:</strong> <span style={{color: 'var(--secondary-cyan)'}}>Cheater123</span></p>
              <p><strong>Check:</strong> KillAura (Type A)</p>
              <p><strong>VL:</strong> 15.2</p>
              <p><strong>Ping:</strong> 45ms</p>
              <p style={{ display: 'flex', alignItems: 'center', gap: '5px', marginTop: '8px' }}>
                <Server size={14} /> Server: WynoNetwork
              </p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
