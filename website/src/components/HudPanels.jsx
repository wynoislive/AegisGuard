import React, { useEffect, useState } from 'react';
import { motion, useAnimation } from 'framer-motion';
import { useInView } from 'react-intersection-observer';
import { Activity, Cpu, MonitorPlay, ShieldCheck } from 'lucide-react';
import './components.css';

const CountUp = ({ end }) => {
  const [count, setCount] = useState(0);
  useEffect(() => {
    let start = 0;
    const duration = 2000;
    const increment = end / (duration / 16);
    const timer = setInterval(() => {
      start += increment;
      if (start >= end) {
        setCount(end);
        clearInterval(timer);
      } else {
        setCount(Math.floor(start));
      }
    }, 16);
    return () => clearInterval(timer);
  }, [end]);
  return <div className="stat-counter">{count}</div>;
};

const Panel = ({ children, delay = 0 }) => {
  const controls = useAnimation();
  const [ref, inView] = useInView({ threshold: 0.2, triggerOnce: true });

  useEffect(() => {
    if (inView) {
      controls.start({ opacity: 1, y: 0, scale: 1 });
    }
  }, [controls, inView]);

  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y: 50, scale: 0.95 }}
      animate={controls}
      transition={{ duration: 0.6, delay, type: "spring" }}
      className="hud-panel cyberpunk-panel"
    >
      {children}
    </motion.div>
  );
};

export default function HudPanels() {
  return (
    <section className="hud-grid" style={{ position: 'relative', zIndex: 10 }}>
      
      <Panel delay={0.1}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <ShieldCheck color="var(--primary-red)" />
          <h3>AegisGuard Checks</h3>
        </div>
        <div style={{ marginTop: '10px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', color: '#ff6666' }}>
            <span>Combat Checks:</span> <span>6</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', color: '#ffaaaa' }}>
            <span>Movement Checks:</span> <span>8</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', color: '#aaffaa' }}>
            <span>World Checks:</span> <span>3</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', color: '#aaaaff' }}>
            <span>Packets & Exploits:</span> <span>9</span>
          </div>
        </div>
        <div style={{ borderTop: '1px solid var(--primary-red)', marginTop: '15px', paddingTop: '10px' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--secondary-cyan)', textTransform: 'uppercase' }}>Total Active Defenses</div>
          <CountUp end={34} />
          <div style={{ color: '#00ff00', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#00ff00', boxShadow: '0 0 5px #00ff00' }}></div>
            Status: All Systems Active
          </div>
        </div>
      </Panel>

      <Panel delay={0.3}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Activity color="var(--primary-red)" />
          <h3>Physics Simulation</h3>
        </div>
        <p>Real-time emulation of vanilla movement mechanics. Calculates friction scalars, viscosity graphs, and bounding box interactions to eliminate false positives.</p>
        <div className="sine-wave">
          <div className="sine-path"></div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.7rem', color: 'var(--secondary-cyan)', marginTop: '5px' }}>
          <span>FRICTION</span>
          <span>VISCOSITY</span>
        </div>
      </Panel>

      <Panel delay={0.5}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Cpu color="var(--primary-red)" />
          <h3>Deep Ore Tracking</h3>
        </div>
        <p>Next-generation Xray mitigation. Integrates invisible Honeypot Bait blocks and recursive Vein analyzing directly hooked to WynoWorldGen logic.</p>
        <div style={{ width: '100%', height: '80px', marginTop: '10px', background: 'var(--bg-dark)', border: '1px solid var(--secondary-cyan)', position: 'relative', overflow: 'hidden' }}>
          {/* Mock Node Network */}
          <motion.div 
            animate={{ scale: [1, 1.2, 1], opacity: [0.5, 1, 0.5] }}
            transition={{ repeat: Infinity, duration: 2 }}
            style={{ position: 'absolute', top: '20px', left: '30px', width: 10, height: 10, background: 'var(--secondary-cyan)', borderRadius: '50%', boxShadow: '0 0 10px var(--secondary-cyan)' }}
          />
          <motion.div 
            animate={{ scale: [1, 1.5, 1], opacity: [0.3, 0.8, 0.3] }}
            transition={{ repeat: Infinity, duration: 3, delay: 1 }}
            style={{ position: 'absolute', top: '40px', left: '70px', width: 15, height: 15, background: 'var(--primary-red)', borderRadius: '50%', boxShadow: '0 0 10px var(--primary-red)' }}
          />
          <svg style={{ position: 'absolute', width: '100%', height: '100%', top: 0, left: 0 }}>
            <line x1="35" y1="25" x2="75" y2="45" stroke="rgba(0, 255, 255, 0.5)" strokeWidth="2" strokeDasharray="4" />
          </svg>
        </div>
      </Panel>

      <Panel delay={0.7}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <MonitorPlay color="var(--primary-red)" />
          <h3>Cross-Platform Aware</h3>
        </div>
        <p>Natively integrates with Floodgate and GeyserMC. Automatically scales precision thresholds for Bedrock, Mobile, and Controller inputs to ensure fair play.</p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '30px', marginTop: '20px' }}>
            <div style={{ textAlign: 'center' }}>
              <MonitorPlay color="#fff" size={30} />
              <div style={{ fontSize: '0.7rem', color: '#aaa', marginTop: '5px' }}>JAVA PC</div>
            </div>
            <div style={{ textAlign: 'center', color: 'var(--secondary-cyan)' }}>
              <div style={{ width: '20px', height: '30px', border: '2px solid var(--secondary-cyan)', borderRadius: '4px', margin: '0 auto' }}></div>
              <div style={{ fontSize: '0.7rem', color: 'var(--secondary-cyan)', marginTop: '5px' }}>MOBILE</div>
            </div>
        </div>
      </Panel>

    </section>
  );
}
