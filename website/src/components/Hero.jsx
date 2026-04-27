import React from 'react';
import { motion } from 'framer-motion';
import './components.css';

export default function Hero() {
  return (
    <section className="hero-section">
      <motion.div 
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 1, ease: "easeOut" }}
        className="title-container"
      >
        <h1 className="main-title glitch-text" data-text="AEGISGUARD">
          AEGIS<span style={{ color: 'var(--primary-red)' }}>GUARD</span> <span style={{ fontSize: '2rem', verticalAlign: 'top', color: 'var(--secondary-cyan)' }}>v1.5</span>
        </h1>
        <h2 className="subtitle">
          ENTERPRISE ANTI-CHEAT <span style={{ color: 'var(--primary-red)' }}>•</span> MINECRAFT PAPER 1.21.1
        </h2>
      </motion.div>

      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1, duration: 1 }}
        style={{ marginTop: '2rem', display: 'flex', gap: '2rem' }}
      >
        <div className="cyberpunk-panel" style={{ padding: '10px 20px' }}>
          <span style={{ color: '#ccc', fontSize: '0.8rem', textTransform: 'uppercase' }}>Developed By</span><br/>
          <strong style={{ color: 'white', fontFamily: 'var(--font-heading)' }}>DEV WYNO</strong>
        </div>
        <div className="cyberpunk-panel" style={{ padding: '10px 20px', borderColor: 'var(--secondary-cyan)' }}>
          <span style={{ color: '#ccc', fontSize: '0.8rem', textTransform: 'uppercase' }}>Powered By</span><br/>
          <strong style={{ color: 'var(--secondary-cyan)', fontFamily: 'var(--font-heading)' }}>TEAM MOODY</strong>
        </div>
      </motion.div>
    </section>
  );
}
