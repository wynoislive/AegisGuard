import React, { useRef, useState, useEffect } from 'react';
import { useProgress, useGLTF, Center, Resize } from '@react-three/drei';
import { Canvas, useFrame } from '@react-three/fiber';

function SpinningDragon() {
  const dragonUrl = "/AegisGuard/models/minecraft_rainbow_dragon.glb";
  const { scene } = useGLTF(dragonUrl);
  const meshRef = useRef();

  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.rotation.y += 0.05; // Spin the dragon rapidly
      // Levitate it smoothly
      meshRef.current.position.y = Math.sin(state.clock.elapsedTime * 3) * 2; 
    }
  });

  return (
    <group ref={meshRef}>
      <Resize scale={8}>
        <Center>
          <primitive object={scene} />
        </Center>
      </Resize>
    </group>
  );
}

export default function LoadingScreen() {
  const { active, progress } = useProgress();
  const [fadingOut, setFadingOut] = useState(false);
  const [unmounted, setUnmounted] = useState(false);

  // When loading finishes (active becomes false), trigger the CSS fade out first,
  // then totally unmount the component after 1 second for a silky smooth transition.
  useEffect(() => {
    if (!active && progress === 100) {
      setFadingOut(true);
      setTimeout(() => {
        setUnmounted(true);
      }, 1000);
    }
  }, [active, progress]);

  if (unmounted) return null;

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
      background: 'radial-gradient(circle at center, #110000 0%, #000000 100%)', 
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center', color: '#00FFFF',
      fontFamily: 'Orbitron, sans-serif', zIndex: 99999,
      opacity: fadingOut ? 0 : 1,
      transition: 'opacity 1s ease-in-out',
      pointerEvents: fadingOut ? 'none' : 'all'
    }}>
      <div style={{ width: '400px', height: '400px' }}>
        {/* We use a strict Suspense so if the loader dragon isn't ready it won't crash */}
        <React.Suspense fallback={null}>
          <Canvas camera={{ position: [0, 0, 15], fov: 50 }} dpr={1}>
            <ambientLight intensity={1} />
            <pointLight position={[5, 5, 5]} intensity={10} color="#00FFFF" />
            <SpinningDragon />
          </Canvas>
        </React.Suspense>
      </div>
      <h2 style={{ letterSpacing: '4px', marginTop: '20px', textTransform: 'uppercase', textShadow: '0 0 10px #00FFFF' }}>
        {progress < 10 ? 'ESTABLISHING SECURE LINK...' : `SYNCING ASSETS: ${Math.round(progress)}%`}
      </h2>
      <div style={{
        width: '300px', height: '4px', background: '#333', marginTop: '15px', overflow: 'hidden', boxShadow: '0 0 10px #00FFFF'
      }}>
        <div style={{
          width: `${progress}%`, height: '100%', background: '#00FFFF', transition: 'width 0.2s', boxShadow: '0 0 10px #00FFFF'
        }}></div>
      </div>
      <div style={{ marginTop: '30px', fontSize: '0.8rem', color: '#666', letterSpacing: '2px' }}>
        AEGISGUARD NEURAL ENGINE
      </div>
    </div>
  );
}
