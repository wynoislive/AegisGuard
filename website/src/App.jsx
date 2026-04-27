import Background3D from './components/Background3D';
import Hero from './components/Hero';
import HudPanels from './components/HudPanels';
import AlertPopup from './components/AlertPopup';
import LoadingScreen from './components/LoadingScreen';

function App() {
  return (
    <div className="app-container">
      <LoadingScreen />
      <Background3D />
      <Hero />
      <HudPanels />
      <AlertPopup />
      
      <footer style={{ marginTop: 'auto', padding: '2rem', textAlign: 'center', color: '#666', borderTop: '1px solid #220000', backgroundColor: 'var(--bg-dark)' }}>
        <p style={{ fontFamily: 'var(--font-heading)', color: 'var(--primary-red)' }}>AEGISGUARD</p>
        <p style={{ fontSize: '0.8rem', marginTop: '10px' }}>&copy; {new Date().getFullYear()} Wyno. Protect your server. Dominate the game.</p>
      </footer>
    </div>
  );
}

export default App;
