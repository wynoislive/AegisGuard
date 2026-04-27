import React, { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Edges } from '@react-three/drei';

function WireframeCube({ position, delay }) {
  const meshRef = useRef();

  useFrame((state) => {
    const time = state.clock.getElapsedTime();
    meshRef.current.position.y = position[1] + Math.sin(time + delay) * 0.5;
    meshRef.current.rotation.x = time * 0.2;
    meshRef.current.rotation.y = time * 0.3;
  });

  return (
    <mesh position={position} ref={meshRef}>
      <boxGeometry args={[1, 1, 1]} />
      <meshBasicMaterial color="#000000" transparent opacity={0.5} />
      <Edges scale={1.05} threshold={15} color="#FF0000" />
    </mesh>
  );
}

function GridBackground() {
  return (
    <group position={[0, -2, -5]} rotation={[Math.PI / 2, 0, 0]}>
      <gridHelper args={[50, 50, '#FF0000', '#220000']} />
    </group>
  );
}

export default function Background3D() {
  return (
    <div style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: -1 }}>
      <Canvas camera={{ position: [0, 0, 8], fov: 60 }}>
        <ambientLight intensity={0.5} />
        <pointLight position={[10, 10, 10]} color="#FF0000" intensity={2} />
        
        {/* Floating "packets" / "data blocks" representing Minecraft blocks or data */}
        <WireframeCube position={[-4, 1, -2]} delay={0} />
        <WireframeCube position={[4, -1, -3]} delay={2} />
        <WireframeCube position={[-2, -2, -1]} delay={1} />
        <WireframeCube position={[3, 2, -4]} delay={3} />
        
        <GridBackground />
      </Canvas>
      <div className="scanlines"></div>
    </div>
  );
}
